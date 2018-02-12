package testformula;

import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.model.GraphCompressor;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.FormulaCacheCleaner;
import org.zkoss.zss.model.impl.SheetImpl;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerSimple;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import java.sql.*;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;

public class AsyncPerformance {
    private static final int range=100000;
    private static final int modification=1000;
    private static Connection conn;

    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://127.0.0.1:5432/ibd";
        String driver = "org.postgresql.Driver";
        String userName = "mangesh";
        String password = "mangesh";

        String url2 = "jdbc:postgresql://127.0.0.1:5432/XLAnalysis";
        Properties props = new Properties();
        props.setProperty("user",userName);
        props.setProperty("password",password);
        conn = DriverManager.getConnection(url2, props);

        DBHandler.connectToDB(url, driver, userName, password);

        SheetImpl.simpleModel = true;
        SheetImpl.disablePrefetch();
        FormulaAsyncScheduler formulaAsyncScheduler = new FormulaAsyncSchedulerSimple();
        Thread thread = new Thread(formulaAsyncScheduler);
        thread.start();

        GraphCompressor  graphCompressor = new GraphCompressor();
        Thread thread2 = new Thread(formulaAsyncScheduler);

        SBook book= BookBindings.getBookByName("testBook");
        /* Cleaner for sync computation */
        FormulaCacheCleaner.setCurrent(new FormulaCacheCleaner(book.getBookSeries()));
        SSheet sheet = book.getSheet(0);

        sheet.setSyncComputation(true);

        ////////////////////////////
        //loadSheet(sheet, "survey","weather.xlsx", "weather");
       // loadSheet(sheet, "survey","CS 465 User Evaulations.xlsx",
       //         "Sheet1");

        //loadSheet(sheet, "survey","Escalating OSA with Cost Share.xlsx", "Cost Share");



       // Collection<SCell> formula_cells = sheet.getCells().stream()
       //         .filter(e->e.getType()== SCell.CellType.FORMULA)
       //         .collect(Collectors.toList());
        //////////////////////////

        //sheet.getCell(0,0).setValue("500");


        sheet.setSyncComputation(true);
        int cellCount = 50;
        for (int i=1;i<=cellCount;i++)
            sheet.getCell(i,0).setFormulaValue("A" + i + "+1");


        Thread.sleep(2000);
       //sheet.clearCache();
        long startTime, endTime;
       /* Time to update A1 */


        sheet.clearCache();
        startTime = System.currentTimeMillis();

        sheet.getCell(0,0).setValue("300");
        System.out.println("Final Value "
                + sheet.getCell(cellCount,0).getValue());
        endTime = System.currentTimeMillis();
        System.out.println("Sync time to update = " + (endTime-startTime));


        sheet.setSyncComputation(false);
        sheet.clearCache();

        System.out.println("Starting Asyn ");
        startTime = System.currentTimeMillis();

        sheet.getCell(0,0).setValue("200");
        //System.out.println("Final Value "
        //        + sheet.getCell(cellCount,0).getValue());
       endTime = System.currentTimeMillis();
       System.out.println("Async time to update = " + (endTime-startTime));
       formulaAsyncScheduler.waitForCompletion();
       endTime = System.currentTimeMillis();
       System.out.println("Final Value "
                + sheet.getCell(cellCount,0).getValue());
       System.out.println("Async time to complete = " + (endTime-startTime));

        //Get total dirty time for all cells
        formulaAsyncScheduler.shutdown();
        thread.join();

        //Get total dirty time for all cells
        Collection<SCell> cells = sheet.getCells().stream()
                .filter(e->e.getType()== SCell.CellType.FORMULA)
                .collect(Collectors.toList());
        long totalWaitTime = cells.stream()
                .mapToLong(e-> DirtyManagerLog.instance.getDirtyTime(e.getCellRegion()))
                .sum();
        System.out.println("Total Wait time " + totalWaitTime);
        System.out.println("Avg Wait time " + totalWaitTime/cells.size());


    }

    public static void getBadCells()
    {
        try(AutoRollbackConnection autoRollbackConnection = DBHandler.instance.getConnection())
        {
            String stmt = "WITH RECURSIVE deps AS (  SELECT \n" +
                    "bookname::text, sheetname::text, range::text,\n" +
                    "dep_bookname, dep_sheetname, dep_range::text,\n" +
                    "must_expand FROM dependency \n" +
                    "UNION   SELECT \n" +
                    "t.bookname, t.sheetname, t.range,\n" +
                    "d.dep_bookname, d.dep_sheetname, d.dep_range::text, d.must_expand \n" +
                    "FROM dependency d    INNER JOIN deps t    ON \n" +
                    "d.bookname   =  t.dep_bookname    AND t.must_expand  \n" +
                    "AND d.sheetname =  t.dep_sheetname    AND d.range    \n" +
                    "&& t.dep_range::box) SELECT\n" +
                    "bookname, sheetname, range, count(1) FROM deps\n" +
                    "group by bookname, sheetname, range\n" +
                    "order by 4 desc";
            Statement statement = autoRollbackConnection.createStatement();
            ResultSet rs = statement.executeQuery(stmt);



        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void loadSheet(SSheet sheet,
                                 String ds, String bookName, String sheetName) throws SQLException {
        //Connection connection = pgs_db.getConnection();
        String stmt = "SELECT * FROM " + ds + "_sheetdata WHERE filename = ? and sheetname = ?";
        PreparedStatement preparedStatement = conn.prepareStatement(stmt);
        preparedStatement.setString(1, bookName);
        preparedStatement.setString(2, sheetName);
        ResultSet rs = preparedStatement.executeQuery();
        // Row and columns interchanged in the dataset.
// Update in two pass
        try(AutoRollbackConnection autoRollbackConnection = DBHandler.instance.getConnection())
        {
            while(rs.next())
            {
                int row = rs.getInt("row");
                int col = rs.getInt("col");
                String value = rs.getString("formula");
                if (rs.wasNull())
                    value = rs.getString("value");
                else
                    value = "=" + value;
                System.out.println("Updating value:" + value);
                sheet.getCell(row, col).setStringValue(value,
                        autoRollbackConnection,true);
                //sheet.getCell(row, col).setValueParse(value,
                //        autoRollbackConnection,
                //        0, true);
            }
        }
        rs.close();
        preparedStatement.close();

        // Fix the formula
        try(AutoRollbackConnection autoRollbackConnection = DBHandler.instance.getConnection()) {
            sheet.getCells()
                    .stream()
                    .filter(e -> e.getType() == SCell.CellType.FORMULA)
                    .forEach(e -> e
                            .setValueParse(e.getStringValue(),
                                    autoRollbackConnection, 0, true));
        }


    }


}
