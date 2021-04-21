package testformula;

import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.postgresql.geometric.PGbox;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.AbstractBookSeriesAdv;
import org.zkoss.zss.model.impl.FormulaCacheCleaner;
import org.zkoss.zss.model.impl.GraphCompressor;
import org.zkoss.zss.model.impl.SheetImpl;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncListener;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerThreaded;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.dependency.DependencyTable;
import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class AsyncPerformance implements FormulaAsyncListener {
    private static final int range=100000;
    private static final int modification=1000;
    private static Connection conn;
    int cellCount = 200;
    long initTime;
    boolean testStarted = false;
    private long controlReturnedTime;
    private long computationDoneTime;


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
        //FormulaAsyncScheduler formulaAsyncScheduler = new FormulaAsyncSchedulerPriority();
        FormulaAsyncScheduler formulaAsyncScheduler = new FormulaAsyncSchedulerThreaded();
        Thread asyncThread = new Thread(formulaAsyncScheduler);
        asyncThread.start();


        GraphCompressor  graphCompressor = new GraphCompressor();
        //Thread graphThread = new Thread(graphCompressor);
        //graphThread.start();

        //simpleTest(formulaAsyncScheduler);
        //realTest("survey", "Escalating OSA with Cost Share.xlsx", "Cost Share", formulaAsyncScheduler);


        //graphCompressor.shutdown();
        //graphThread.join();
        AsyncPerformance asyncPerformance = new AsyncPerformance();
        FormulaAsyncScheduler.getScheduler().setFormulaAsyncListener(asyncPerformance);
        asyncPerformance.simpleTest();

        formulaAsyncScheduler.shutdown();
        asyncThread.join();

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
        sheet.getCell(badCells.get(0).getRow(), badCells.get(0).getColumn()).setValue(startTime % 100);
        System.out.println("Final Value "
                + sheet.getCell(badCells.get(0).getRow(), badCells.get(0).getColumn()).getValue());
        endTime = System.currentTimeMillis();

        System.out.println("Sync time to update = " + (endTime - startTime) + " " + dt.getLastLookupTime());

        sheet.setSyncComputation(false);
        DependencyTable table = ((AbstractBookSeriesAdv) sheet.getBook().getBookSeries()).getDependencyTable();

        CellRegion badCell = badCells.get(0);

        Random random = new Random();
        List<CellRegion> sheetCells = sheet.getCells().stream().map(SCell::getCellRegion)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        int cellsInSheet = sheetCells.size();

        int total_area_under_curve = 0;
        for (int i = 0; i < 0; i++) {
            sheet.clearCache();
            DirtyManagerLog.instance.init();

            System.out.println("Starting Asyn ");
            dt.getLastLookupTime(); // Reset time
            startTime = System.currentTimeMillis();
            sheet.getCell(badCell).setValue(random.nextInt());
            endTime = System.currentTimeMillis();
            System.out.println("Async time to update = " + (endTime - startTime) + " " + dt.getLastLookupTime());
            total_area_under_curve += (endTime - startTime) * cellsInSheet;

            System.out.println((endTime - startTime) + "\t" + cellsInSheet);

            // System.out.println("Async time to complete = " + (endTime - startTime));

            //TODO: nolonger wait for completion.
            //formulaAsyncScheduler.waitForCompletion();
            // endTime = System.currentTimeMillis();

            // Right now considering dependents with FP
            //Set<Ref> dependents = table.getDependents(sheet.getCell(badCell).getRef());


            System.out.println("Before Compression ");
            System.out.println(startTime + "\t" + cellsInSheet);
            System.out.println(endTime + "\t" + cellsInSheet);
          //  DirtyManagerLog.instance.groupPrint(sheetCells);

            // long totalWaitTime = dependents.stream()
            //         .mapToLong(e -> DirtyManagerLog.instance.getDirtyTime(new CellRegion(e)))
            //         .sum();
            // System.out.println("Total Wait time " + totalWaitTime);

            //totalWaitTime = sheet.getCells().stream()
            //        .mapToLong(e -> DirtyManagerLog.instance.getDirtyTime(e.getCellRegion()))
            //        .sum();
            // System.out.println("Total Wait time Cells " + totalWaitTime);
            // DirtyManagerLog.instance.print();

            //total_area_under_curve += totalWaitTime;
            //System.out.println("Avg Wait time " + totalWaitTime / dependents.size());
        }
        System.out.println("Avg  area under curve " + total_area_under_curve / 10);


        //compressGraphNode(dbBookName, "Sheet1", badCell);

        total_area_under_curve = 0;
        for (int i = 0; i < 1; i++) {
            sheet.clearCache();
            DirtyManagerLog.instance.init();

            System.out.println("Starting Asyn " + System.currentTimeMillis());
            dt.getLastLookupTime(); // Reset time
            startTime = System.currentTimeMillis();
            sheet.getCell(badCell).setValue(random.nextInt());
            endTime = System.currentTimeMillis();
            System.out.println("Async time to update = " + (endTime - startTime) + " " + dt.getLastLookupTime());
            System.out.println("Control returned to user = " + endTime);

            total_area_under_curve += (endTime - startTime) * cellsInSheet;

            //formulaAsyncScheduler.waitForCompletion();
            //endTime = System.currentTimeMillis();
            //  System.out.println("Async time to complete = " + (endTime - startTime));



            // Right now considering dependents with FP
            //Set<Ref> dependents = table.getDependents(sheet.getCell(badCell).getRef());
            //long totalWaitTime = dependents.stream()
            //        .mapToLong(e -> DirtyManagerLog.instance.getDirtyTime(new CellRegion(e)))
            //        .sum();
            //System.out.println("Total Wait time " + totalWaitTime);
            //total_area_under_curve += totalWaitTime;


//            totalWaitTime = sheet.getCells().stream()
//                    .mapToLong(e -> DirtyManagerLog.instance.getDirtyTime(e.getCellRegion()))
            //                   .sum();
            System.out.println("After Compression ");
            System.out.println(startTime + "\t" + cellsInSheet);
            System.out.println(endTime + "\t" + cellsInSheet);
            //DirtyManagerLog.instance.groupPrint(sheetCells);


            // System.out.println("Avg Wait time " + totalWaitTime / dependents.size());
        }
        System.out.println("Compressed version Avg  area under curve " + total_area_under_curve / 10);

        revertGraphCompression(dbBookName);
    }


    private static void revertGraphCompression(String bookName) {
        String deleteSql = "DELETE FROM dependency " +
                "WHERE bookname = ? ";

        String insertSql = "INSERT INTO dependency " +
                "SELECT * FROM full_dependency " +
                "WHERE bookname = ? ";

        try (AutoRollbackConnection autoRollbackConnection = DBHandler.instance.getConnection();
             PreparedStatement stmtSelect = autoRollbackConnection.prepareStatement(deleteSql);
             PreparedStatement stmtInsert = autoRollbackConnection.prepareStatement(insertSql)) {


            stmtSelect.setString(1, bookName);
            stmtInsert.setString(1, bookName);

            stmtSelect.execute();
            stmtInsert.execute();
            autoRollbackConnection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static void compressGraphNode(String bookName, String sheetname, CellRegion cellRegion) {
        String selectSql = "WITH RECURSIVE deps AS (  SELECT " +
                "bookname::text, sheetname::text, range::text, " +
                "dep_bookname, dep_sheetname, dep_range::text, " +
                "must_expand FROM full_dependency " +
                "WHERE bookname = ? " +
                "AND sheetname = ? " +
                "AND range && ?  " +
                "UNION   SELECT " +
                "t.bookname, t.sheetname, t.range, " +
                "d.dep_bookname, d.dep_sheetname, d.dep_range::text, d.must_expand " +
                "FROM full_dependency d    INNER JOIN deps t    ON " +
                "d.bookname   =  t.dep_bookname    AND t.must_expand " +
                "AND d.sheetname =  t.dep_sheetname    AND d.range " +
                "                                          && t.dep_range::box) " +
                "SELECT " +
                "bookname, sheetname, range, " +
                "dep_bookname, dep_sheetname, dep_range::box " +
                "FROM deps";
        String deleteSql = "DELETE FROM dependency " +
                "WHERE bookname = ? " +
                "AND sheetname = ? " +
                "AND range && ? ";

        String insertSql = "INSERT INTO dependency VALUES (?,?,?,?,?,?,?)";

        try (AutoRollbackConnection autoRollbackConnection = DBHandler.instance.getConnection();
             PreparedStatement stmtSelect = autoRollbackConnection.prepareStatement(selectSql);
             PreparedStatement stmtDelete = autoRollbackConnection.prepareStatement(deleteSql);
             PreparedStatement stmtInsert = autoRollbackConnection.prepareStatement(insertSql)) {
            stmtSelect.setString(1, bookName);
            stmtSelect.setString(2, sheetname);
            stmtSelect.setObject(3, new PGbox(cellRegion.getRow(),
                    cellRegion.getColumn(), cellRegion.getLastRow(),
                    cellRegion.getLastColumn()), Types.OTHER);

            ArrayList<CellRegion> deps = new ArrayList<>();
            ResultSet rs = stmtSelect.executeQuery();
            while (rs.next()) {
                PGbox range = (PGbox) rs.getObject(6);
                // The order of points is based on how postgres stores them
                deps.add(new CellRegion((int) range.point[1].x,
                        (int) range.point[1].y,
                        (int) range.point[0].x,
                        (int) range.point[0].y));
            }
            rs.close();

            //System.out.println("Original Deps" + deps.size());
            compressDeps(deps);
            //System.out.println("compressed Deps" + deps.size());

            stmtDelete.setString(1, bookName);
            stmtDelete.setString(2, sheetname);
            stmtDelete.setObject(3, new PGbox(cellRegion.getRow(),
                    cellRegion.getColumn(), cellRegion.getLastRow(),
                    cellRegion.getLastColumn()), Types.OTHER);
            stmtDelete.execute();


            stmtInsert.setString(1, bookName);
            stmtInsert.setString(2, sheetname);
            stmtInsert.setObject(3, new PGbox(cellRegion.getRow(),
                    cellRegion.getColumn(), cellRegion.getLastRow(),
                    cellRegion.getLastColumn()), Types.OTHER);
            stmtInsert.setString(4, bookName);
            stmtInsert.setString(5, sheetname);
            stmtInsert.setBoolean(7, false);

            for (CellRegion dependant : deps) {
                stmtInsert.setObject(6, new PGbox(dependant.getRow(),
                        dependant.getColumn(), dependant.getLastRow(),
                        dependant.getLastColumn()), Types.OTHER);
                stmtInsert.execute();
            }
            autoRollbackConnection.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void compressDeps(ArrayList<CellRegion> deps) {
        while (deps.size() > 30) {
            int best_i = 0, best_j = 0, best_area = Integer.MAX_VALUE;
            CellRegion best_bounding_box = null;
            for (int i = 0; i < deps.size() - 1; i++) {
                for (int j = i + 1; j < deps.size(); j++) {
                    CellRegion bounding = deps.get(i).getBoundingBox(deps.get(j));
                    int new_area = bounding.getCellCount() -
                            deps.get(i).getCellCount() - deps.get(j).getCellCount();
                    CellRegion overlap = deps.get(i).getOverlap(deps.get(j));
                    if (overlap != null)
                        new_area -= overlap.getCellCount();
                    if (new_area < best_area) {
                        best_area = new_area;
                        best_i = i;
                        best_j = j;
                        best_bounding_box = bounding;
                    }
                }
            }
            // Merge i,j
            //System.out.println(best_i + " " + best_j + " " + deps.get(best_i) + " " + deps.get(best_j) + " " + best_bounding_box);
            deps.remove(best_j);
            deps.remove(best_i);
            deps.add(best_bounding_box);
        }
    }

    public void simpleTest()
    {
        SBook book = BookBindings.getBookByName("testBook" + System.currentTimeMillis());
        /* Cleaner for sync computation */
        // FormulaCacheCleaner.setCurrent(new FormulaCacheCleaner(book.getBookSeries()));
        SSheet sheet = book.getSheet(0);
        sheet.setSyncComputation(false);

        for (int i=1;i<=4000;i++)
            sheet.getCell(i,2).setValue(System.currentTimeMillis());


        for (int i=1;i<=cellCount;i++)
            sheet.getCell(i,0).setFormulaValue("A" + i + "+1");
        sheet.clearCache();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DirtyManagerLog.instance.init();
        testStarted = true;

        initTime = System.currentTimeMillis();
        System.out.println("Starting Asyn " + initTime);

        sheet.getCell(0, 0).setValue(200);

        System.out.println("Before Waiting "
                + sheet.getCell(cellCount, 0).getValue());

        controlReturnedTime = System.currentTimeMillis();
        System.out.println("Control retuned  " + controlReturnedTime);

        System.out.println("Async time to update = " + (controlReturnedTime-initTime));

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<CellRegion> getBadCells(String bookName, String sheetname)
    {
        ArrayList<CellRegion> badCells = new ArrayList<>();
        ArrayList<Integer> impactedCells = new ArrayList<>();
        String sql = "WITH RECURSIVE deps AS (  SELECT  " +
                "bookname::text, sheetname::text, range::text, " +
                "dep_bookname, dep_sheetname, dep_range::text, " +
                "must_expand FROM dependency " +
                "WHERE bookname = ? " +
                "AND sheetname = ? " +
                "UNION   SELECT " +
                "t.bookname, t.sheetname, t.range, " +
                "d.dep_bookname, d.dep_sheetname, d.dep_range::text, d.must_expand " +
                "FROM dependency d    INNER JOIN deps t    ON " +
                "d.bookname   =  t.dep_bookname    AND t.must_expand " +
                "AND d.sheetname =  t.dep_sheetname    AND d.range " +
                "&& t.dep_range::box) " +
                "SELECT " +
                "bookname, sheetname, range::box, " +
                "sum(area(dep_range::box) + height(dep_range::box) + width(dep_range::box) + 1) " +
                "FROM deps " +
                "group by bookname, sheetname, range " +
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

           // System.out.println("Bad cellls");
          //  for (int i =0; i<badCells.size();i++)
          //  {
          //      System.out.println(badCells.get(i) + " " + impactedCells.get(i));
          //  }
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


    @Override
    public void update(SBook book, SSheet sheet, CellRegion cellRegion, Object value, String formula) {
        if (cellRegion.row == cellCount && testStarted) {

            List<CellRegion> sheetCells = sheet.getCells().stream().map(SCell::getCellRegion)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            computationDoneTime = System.currentTimeMillis();
            System.out.println("Final Value "
                    + sheet.getCell(cellCount, 0).getValue());
            System.out.println("Async time to complete = " + (computationDoneTime - initTime));


            //Get total dirty time for all cells
            Collection<SCell> cells = sheet.getCells().stream()
                    .filter(e -> e.getType() == SCell.CellType.FORMULA)
                    .collect(Collectors.toList());


            //  DirtyManagerLog.instance.groupPrint(sheetCells, controlReturnedTime, initTime);

            long totalWaitTime = cells.stream()
                    .mapToLong(e -> DirtyManagerLog.instance.getDirtyTime(e.getCellRegion()))
                    .sum();
            System.out.println("Total Wait time " + totalWaitTime);
            System.out.println("Avg Wait time " + totalWaitTime / cells.size());
        }
    }
}
