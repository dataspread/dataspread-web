package testformula;

import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.model.GraphCompressor;
import org.postgresql.geometric.PGbox;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.AbstractBookSeriesAdv;
import org.zkoss.zss.model.impl.FormulaCacheCleaner;
import org.zkoss.zss.model.impl.SheetImpl;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerPriority;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.dependency.DependencyTable;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import java.sql.*;
import java.util.*;
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
        FormulaAsyncScheduler formulaAsyncScheduler = new FormulaAsyncSchedulerPriority();
        //FormulaAsyncScheduler formulaAsyncScheduler = new FormulaAsyncSchedulerSimple();
        Thread thread = new Thread(formulaAsyncScheduler);
        thread.start();

        GraphCompressor  graphCompressor = new GraphCompressor();
        Thread thread2 = new Thread(formulaAsyncScheduler);

        //simpleTest(formulaAsyncScheduler);
        realTest("survey", "Escalating OSA with Cost Share.xlsx", "Cost Share", formulaAsyncScheduler);

        formulaAsyncScheduler.shutdown();
        thread.join();
    }


    public static void realTest(String ds, String bookName, String sheetName, FormulaAsyncScheduler formulaAsyncScheduler) throws SQLException {
        String dbBookName = ds + "_" + bookName + "_" + sheetName;
        // Check if book exists.
        SBook book= BookBindings.getBookByNameDontLoad(dbBookName);
        if (book==null)
        {
            book= BookBindings.getBookByName(dbBookName);
            loadSheet(book.getSheet(0), ds,bookName, sheetName);
        }
        SSheet sheet = book.getSheet(0);
        FormulaCacheCleaner.setCurrent(new FormulaCacheCleaner(book.getBookSeries()));
        final DependencyTable dt =
                ((AbstractBookSeriesAdv) book.getBookSeries()).getDependencyTable();

        List<CellRegion> badCells = getBadCells(dbBookName, "Sheet1");
        long startTime, endTime;

        sheet.setSyncComputation(true);

        sheet.clearCache();
        dt.getLastLookupTime(); // init lookup time
        startTime = System.currentTimeMillis();
        //sheet.getCell(badCells.get(0).getRow(),badCells.get(0).getColumn()).setValue(startTime%100);
        //System.out.println("Final Value "
        //        + sheet.getCell(cellCount,0).getValue());
        endTime = System.currentTimeMillis();

        System.out.println("Sync time to update = " + (endTime - startTime) + " " + dt.getLastLookupTime());

        sheet.setSyncComputation(false);
        DependencyTable table = ((AbstractBookSeriesAdv) sheet.getBook().getBookSeries()).getDependencyTable();

        CellRegion badCell = badCells.get(2);

        for (int i = 0; i < 10; i++) {
            sheet.clearCache();
            DirtyManagerLog.instance.init();

            System.out.println("Starting Asyn ");
            startTime = System.currentTimeMillis();
            sheet.getCell(badCell).setValue(startTime % 100);
            endTime = System.currentTimeMillis();
            System.out.println("Async time to update = " + (endTime - startTime) + " " + dt.getLastLookupTime());
            formulaAsyncScheduler.waitForCompletion();
            endTime = System.currentTimeMillis();
            System.out.println("Async time to complete = " + (endTime - startTime));

            // Right now considering dependents with FP
            Set<Ref> dependents = table.getDependents(sheet.getCell(badCell).getRef());
            long totalWaitTime = dependents.stream()
                    .mapToLong(e -> DirtyManagerLog.instance.getDirtyTime(new CellRegion(e)))
                    .sum();
            System.out.println("Total Wait time " + totalWaitTime);
            System.out.println("Avg Wait time " + totalWaitTime / dependents.size());
        }
    }

    public static void simpleTest(FormulaAsyncScheduler formulaAsyncScheduler)
    {
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


        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        Collection<SCell> cells = sheet.getCells().stream()
                .filter(e->e.getType()== SCell.CellType.FORMULA)
                .collect(Collectors.toList());
        long totalWaitTime = cells.stream()
                .mapToLong(e-> DirtyManagerLog.instance.getDirtyTime(e.getCellRegion()))
                .sum();
        System.out.println("Total Wait time " + totalWaitTime);
        System.out.println("Avg Wait time " + totalWaitTime/cells.size());
    }

    public static ArrayList<CellRegion> getBadCells(String bookName, String sheetname)
    {
        ArrayList<CellRegion> badCells = new ArrayList<>();
        ArrayList<Integer> impactedCells = new ArrayList<>();
        String sql = "WITH RECURSIVE deps AS (  SELECT \n" +
                "bookname::text, sheetname::text, range::text,\n" +
                "dep_bookname, dep_sheetname, dep_range::text,\n" +
                "must_expand FROM dependency\n" +
                "WHERE bookname = ?\n" +
                "AND sheetname = ?\n" +
                "UNION   SELECT\n" +
                "t.bookname, t.sheetname, t.range,\n" +
                "d.dep_bookname, d.dep_sheetname, d.dep_range::text, d.must_expand\n" +
                "FROM dependency d    INNER JOIN deps t    ON\n" +
                "d.bookname   =  t.dep_bookname    AND t.must_expand\n" +
                "AND d.sheetname =  t.dep_sheetname    AND d.range\n" +
                "&& t.dep_range::box)\n" +
                "SELECT\n" +
                "bookname, sheetname, range::box,\n" +
                "sum(area(dep_range::box) + height(dep_range::box) + width(dep_range::box) + 1)\n" +
                "FROM deps\n" +
                "group by bookname, sheetname, range\n" +
                "order by 4 desc;";


        try(AutoRollbackConnection autoRollbackConnection = DBHandler.instance.getConnection();
        PreparedStatement statement = autoRollbackConnection.prepareStatement(sql))
        {
            statement.setString(1, bookName);
            statement.setString(2,sheetname);
            ResultSet rs = statement.executeQuery();
            while (rs.next())
            {
                if (rs.getInt(4) < 100)
                    break;
                PGbox range = (PGbox) rs.getObject(3);
                // The order of points is based on how postgres stores them
                badCells.add(new CellRegion((int) range.point[1].x,
                        (int) range.point[1].y,
                        (int) range.point[0].x,
                        (int) range.point[0].y));
                impactedCells.add(rs.getInt(4));

            }
            rs.close();

            System.out.println("Bad cellls");
            for (int i =0; i<badCells.size();i++)
            {
                System.out.println(badCells.get(i) + " " + impactedCells.get(i));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return badCells;
    }

    public static void loadSheet(SSheet sheet,
                                 String ds, String bookName, String sheetName) throws SQLException {
        //Connection connection = pgs_db.getConnection();
        Map<CellRegion, String> sheetData = new HashMap<>();

        String stmt = "SELECT * FROM " + ds + "_sheetdata WHERE filename = ? and sheetname = ?";
        PreparedStatement preparedStatement = conn.prepareStatement(stmt);
        preparedStatement.setString(1, bookName);
        preparedStatement.setString(2, sheetName);
        ResultSet rs = preparedStatement.executeQuery();

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
                // System.out.println("Updating value:" + value);
                // sheetData.put(new CellRegion(row,col), value);

                sheet.getCell(row, col).setStringValue(value,
                        autoRollbackConnection,true);
            }
            autoRollbackConnection.commit();
        }
        rs.close();
        preparedStatement.close();

        // Fix the formula
        try(AutoRollbackConnection autoRollbackConnection = DBHandler.instance.getConnection()) {
            sheet.getCells()
                    .stream()
                    .filter(e -> e.getStringValue().startsWith("="))
                    .forEach(e -> e
                            .setValueParse(e.getStringValue(),
                                    autoRollbackConnection, 0, true));
            autoRollbackConnection.commit();
        }


    }


}
