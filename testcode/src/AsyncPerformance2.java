import com.google.common.collect.ImmutableMap;
import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.postgresql.geometric.PGbox;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.impl.FormulaCacheCleaner;
import org.zkoss.zss.model.impl.GraphCompressor;
import org.zkoss.zss.model.impl.SheetImpl;
import org.zkoss.zss.model.impl.sys.DependencyTableImplV2;
import org.zkoss.zss.model.impl.sys.DependencyTablePGImpl;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncListener;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerSimple;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class AsyncPerformance2 implements FormulaAsyncListener {
    int cellCount = 5000;
    long initTime;
    boolean testStarted = false;
    final boolean sync=false;
    final boolean graphCompression = true;
    final static boolean graphInDB = false;
    private static Connection conn;

    static int graphCompressionSize = 20;
    final CellRegion window = null;
    //final CellRegion window = new CellRegion(0, 0, 50, 10);
    private long controlReturnedTime;
    private long updatedCells = 0;
    private long cellsToUpdate = 0;
    // Sheet->{session->visibleRange}
    Map<Object, Map<String, int[]>> uiVisibleMap;


    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://127.0.0.1:5432/ibd";
        String driver = "org.postgresql.Driver";
        String userName = "mangesh";
        String password = "mangesh";

        String url2 = "jdbc:postgresql://127.0.0.1:5432/XLAnalysis";
        Properties props = new Properties();
        props.setProperty("user", userName);
        props.setProperty("password", password);
        conn = DriverManager.getConnection(url2, props);

        if (graphInDB)
            EngineFactory.dependencyTableClazz = DependencyTablePGImpl.class;
        else
            EngineFactory.dependencyTableClazz = DependencyTableImplV2.class;

        DBHandler.connectToDB(url, driver, userName, password);

        SheetImpl.simpleModel = true;
        //SheetImpl.disablePrefetch();
        //FormulaAsyncScheduler formulaAsyncScheduler = new FormulaAsyncSchedulerPriority();
        FormulaAsyncScheduler formulaAsyncScheduler = new FormulaAsyncSchedulerSimple();
        Thread asyncThread = new Thread(formulaAsyncScheduler);
        asyncThread.start();

        AsyncPerformance2 asyncPerformance = new AsyncPerformance2();
        FormulaAsyncScheduler.initFormulaAsyncListener(asyncPerformance);
        asyncPerformance.simpleTest();

        formulaAsyncScheduler.shutdown();
        asyncThread.join();
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

    public static void loadSheet(SSheet sheet,
                                 String ds, String bookName, String sheetName) throws SQLException {
        //Connection connection = pgs_db.getConnection();
        Map<CellRegion, String> sheetData = new HashMap<>();

        String stmt = "SELECT * FROM " + ds + "_sheetdata WHERE filename = ? and sheetname = ?";
        PreparedStatement preparedStatement = conn.prepareStatement(stmt);
        preparedStatement.setString(1, bookName);
        preparedStatement.setString(2, sheetName);
        ResultSet rs = preparedStatement.executeQuery();

        int i = 0;
        // Update in two pass
        try (AutoRollbackConnection autoRollbackConnection = DBHandler.instance.getConnection()) {
            while (rs.next()) {
                int row = rs.getInt("row");
                int col = rs.getInt("col");
                String value = rs.getString("formula");
                if (rs.wasNull()) {
                    value = rs.getString("value");
                    sheet.getCell(row, col).setStringValue(value,
                            autoRollbackConnection, true);

                } else {
                    value = "=" + value;
                    sheet.getCell(row, col).setNumberValue(1.0,
                            autoRollbackConnection, true);
                }
                // System.out.println("Updating value:" + value);
                // sheetData.put(new CellRegion(row,col), value);
                i++;
                if (i % 1000 == 0)
                    System.out.println("Pass 1 " + i);
            }
            autoRollbackConnection.commit();
        }
        rs.close();


        i = 0;

        sheet.setDelayComputation(true);

        // Pass 2
        rs = preparedStatement.executeQuery();
        try (AutoRollbackConnection autoRollbackConnection = DBHandler.instance.getConnection()) {
            while (rs.next()) {
                int row = rs.getInt("row");
                int col = rs.getInt("col");
                String value = rs.getString("formula");
                if (rs.wasNull()) {
                } else {
                    sheet.getCell(row, col).setFormulaValue(value,
                            autoRollbackConnection, true);
                }
                // System.out.println("Updating value:" + value);
                // sheetData.put(new CellRegion(row,col), value);
                i++;
                if (i % 1000 == 0)
                    System.out.println("Pass 2 " + i);

            }

            autoRollbackConnection.commit();
        }
        rs.close();


        preparedStatement.close();

        // Fix the formula
        /*
        try(AutoRollbackConnection autoRollbackConnection = DBHandler.instance.getConnection()) {
            sheet.getCells()
                    .stream()
                    .filter(e -> e.getStringValue().startsWith("="))
                    .forEach(e -> e
                            .setValueParse(e.getStringValue().replaceAll("\\$",""),
                                    autoRollbackConnection, 0, true));
            autoRollbackConnection.commit();
        } */
        sheet.setDelayComputation(false);
        System.out.println("Sheet loaded");

    }


    private static void compressPGGraphNode(String bookName, String sheetname, CellRegion cellRegion) {
        System.out.println("compressGraphNode " + cellRegion);
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

            System.out.println("Original Deps" + deps.size());
            compressDependencies(deps);
            System.out.println("compressed Deps" + deps.size());

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


    private static void compressDependencies1(ArrayList<Ref> dependencies) {
        while (dependencies.size() > graphCompressionSize) {
            //System.out.println("dependencies.size() " + dependencies.size());
            int best_i = 0, best_j = 0, best_area = Integer.MAX_VALUE;
            Ref best_bounding_box = null;
            for (int i = 0; i < dependencies.size() - 1; i++) {
                for (int j = i + 1; j < dependencies.size(); j++) {
                    Ref bounding = dependencies.get(i).getBoundingBox(dependencies.get(j));
                    int new_area = bounding.getCellCount() -
                            dependencies.get(i).getCellCount() - dependencies.get(j).getCellCount();
                    Ref overlap = dependencies.get(i).getOverlap(dependencies.get(j));
                    if (overlap != null)
                        new_area += overlap.getCellCount();
                    if (new_area==0) {
                        best_area = new_area;
                        best_i = i;
                        best_j = j;
                        best_bounding_box = bounding;
                        i=dependencies.size();
                        break;
                    }


                    if (new_area < best_area) {
                        best_area = new_area;
                        best_i = i;
                        best_j = j;
                        best_bounding_box = bounding;
                    }
                }
            }
            // Merge i,j
            //ystem.out.println(best_i + " " + best_j + " " + dependencies.get(best_i) + " " + dependencies.get(best_j) + " " + best_bounding_box);
            dependencies.remove(best_j);
            dependencies.remove(best_i);
            dependencies.add(best_bounding_box);
        }
    }

    private static void compressDependencies(ArrayList<CellRegion> dependencies) {
        while (dependencies.size() > graphCompressionSize) {
            int best_i = 0, best_j = 0, best_area = Integer.MAX_VALUE;
            CellRegion best_bounding_box = null;
            for (int i = 0; i < dependencies.size() - 1; i++) {
                for (int j = i + 1; j < dependencies.size(); j++) {
                    CellRegion bounding = dependencies.get(i).getBoundingBox(dependencies.get(j));
                    int new_area = bounding.getCellCount() -
                            dependencies.get(i).getCellCount() - dependencies.get(j).getCellCount();
                    CellRegion overlap = dependencies.get(i).getOverlap(dependencies.get(j));
                    if (overlap != null)
                        new_area += overlap.getCellCount();
                    if (new_area==0) {
                        best_area = new_area;
                        best_i = i;
                        best_j = j;
                        best_bounding_box = bounding;
                        i=dependencies.size();
                        break;
                    }


                    if (new_area < best_area) {
                        best_area = new_area;
                        best_i = i;
                        best_j = j;
                        best_bounding_box = bounding;
                    }
                }
            }
            // Merge i,j
            System.out.println(best_i + " " + best_j + " " + dependencies.get(best_i) + " " + dependencies.get(best_j) + " " + best_bounding_box);
            dependencies.remove(best_j);
            dependencies.remove(best_i);
            dependencies.add(best_bounding_box);
        }
    }



    public void simpleTest() {
        SBook book = BookBindings.getBookByName("testBook" + System.currentTimeMillis());
        //SBook book = BookBindings.getBookByName("ejnnyuhp8");
        /* Cleaner for sync computation */
        if (sync)
            FormulaCacheCleaner.setCurrent(new FormulaCacheCleaner(book.getBookSeries()));

        uiVisibleMap = new HashMap<>();
        FormulaAsyncScheduler.updateVisibleMap(uiVisibleMap);


        SSheet sheet = book.getSheet(0);

        // Update visible Map
        // Sheet->{session->visibleRange}
        if (window!=null)
            uiVisibleMap.put(sheet, ImmutableMap.of("Session1", new int[]{window.getRow(), window.getLastRow()}));

        System.out.println("Starting data creation");
        sheet.setSyncComputation(true);
        Test_Rate_CreateSheet(sheet);
        sheet.setSyncComputation(sync);
        System.out.println("Data Creation done");




        Ref updatedCell = sheet.getCell(0, 0).getRef();
        ArrayList<Ref> dependencies1 = new ArrayList<>(sheet.getDependencyTable().getDependents(updatedCell));
        cellsToUpdate = dependencies1.size();

        //Test graph compression
        /*int start = dependencies1.size();
        for (int j=start;j>0;j--)
        {
            graphCompressionSize = j;
            compressDependencies1(dependencies1);
            System.out.println(j + "\t" +
                    (dependencies1.stream().mapToInt(Ref::getCellCount).sum()-start));
        }

        System.out.println("Done ---------------"); */
        /////////////////////////////////

        System.out.println("Dependencies " + cellsToUpdate);
        if (graphCompression) {
            // For PG graph
            if (graphInDB) {
                compressPGGraphNode(sheet.getBook().getBookName(),
                        sheet.getSheetName(), new CellRegion(0, 0));
            } else {
                compressDependencies1(dependencies1);
                sheet.getDependencyTable().addPreDep(updatedCell, new HashSet<>(dependencies1));
            }
        }
        System.out.println("After Compression Dependencies Table size " + dependencies1.size());
        cellsToUpdate= dependencies1.stream().mapToInt(Ref::getCellCount).sum();
        System.out.println("After Compression Dependencies " + cellsToUpdate);
        sheet.clearCache();

        DirtyManagerLog.instance.init();
        CellImpl.disableDBUpdates = true;
        testStarted = true;

        initTime = System.currentTimeMillis();
        System.out.println("Starting Asyn " + initTime);

        sheet.getCell(0, 0).setValue(200);

        System.out.println("Before Waiting "
                + sheet.getCell(cellCount, 0).getValue());

        controlReturnedTime = System.currentTimeMillis();
        System.out.println("Control retuned  " + controlReturnedTime);

        System.out.println("Time to update = " + (controlReturnedTime-initTime) );

        if (!sync) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        Collection<CellRegion> sheetCells;

        if (window == null)
            sheetCells = sheet.getCells().stream().map(SCell::getCellRegion)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        else
            sheetCells = sheet.getCells(window).stream().map(SCell::getCellRegion)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());



        System.out.println("Final Value "
                + sheet.getCell(cellCount, 0).getValue());


        //Get total dirty time for all cells
        Collection<SCell> cells = sheet.getCells().stream()
                .filter(e -> e.getType() == SCell.CellType.FORMULA)
                .collect(Collectors.toList());


        DirtyManagerLog.instance.groupPrint(sheetCells, controlReturnedTime, initTime);

        long totalWaitTime = cells.stream()
                .mapToLong(e -> DirtyManagerLog.instance.getDirtyTime(e.getCellRegion()))
                .sum();
        System.out.println("Total Wait time " + totalWaitTime);
        System.out.println("Avg Wait time " + totalWaitTime / cells.size());

        System.out.println("Updated cells  = " + updatedCells);

    }

    // Exp 1
    private void Test1_CreateSheet(SSheet sheet) {
        for (int i = 1; i <= cellCount; i++)
            sheet.getCell(i, 2).setValue(System.currentTimeMillis());

        sheet.getCell(0, 0).setValue(10);
        for (int i = 1; i <= cellCount; i++)
            sheet.getCell(i, 0).setFormulaValue("A1" + "+" + (System.currentTimeMillis() % 5000) + "+" + i);
    }

    // exp 2
    private void Test2_CreateSheet(SSheet sheet) {
        for (int i = 1; i <= cellCount; i++)
            sheet.getCell(i, 2).setValue(System.currentTimeMillis());

        sheet.getCell(0, 0).setValue(10);
        for (int i = 1; i <= cellCount; i++)
            sheet.getCell(i, 0).setFormulaValue("A1" + "+" + (System.currentTimeMillis() % 5000) + "+" + i);

        for (int i = 1; i <= cellCount; i += 10)
            for (int j = 0; j < 5; j++)
                sheet.getCell(i + j, 0).setFormulaValue("SUM(B1:B50)");
    }


    private void Test3_CreateSheet(SSheet sheet) {
        Random random = new Random(7);

        for (int i = 1; i <= cellCount * 4; i++)
            sheet.getCell(i, 2).setValue(System.currentTimeMillis());

        for (int i = 1; i <= 2000; i++)
            sheet.getCell(i, 0).setFormulaValue("A1 + SUM(B1:B" + random.nextInt(cellCount * 4) + ")");
    }

    // Vlookup test
    private void Test4_CreateSheet(SSheet sheet) {
        Random random = new Random(7);

        sheet.setDelayComputation(true);
        int tableRows = 17689;
        for (int i = 0; i < tableRows; i++) {
            sheet.getCell(i, 0).setValue(i);
            sheet.getCell(i, 1).setValue(System.currentTimeMillis());
            if (i % 1000 == 0)
                System.out.println(i);
        }

        for (int i = 1; i <= tableRows / 2.6; i++) {
            sheet.getCell(i, 3).setFormulaValue("VLOOKUP(" + random.nextInt(tableRows) + ",A1:B"
                    + tableRows + ",2,FALSE)");
            if (i % 1000 == 0)
                System.out.println(i);

        }
        sheet.setDelayComputation(false);
    }




    // Real test with a sheet
    private void Test5_CreateSheet(SSheet sheet) {
        try {
            loadSheet(sheet, "survey", "Escalating OSA with Cost Share.xlsx", "Cost Share");
            //loadSheet(sheet, "survey", "3.17.17_1985-2015_industrials_pull.v7.xlsx",
            //        "Inflows and Outflows");

            //loadSheet(sheet, "survey","Mario.xlsx","Sheet1");
            //   SBook book= BookBindings.getBookByNameDontLoad("ejnnyuhp8");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //ArrayList<CellRegion> badCells = getBadCells("testBook1540434363055", "Sheet1");
        //ArrayList<CellRegion> badCells = getBadCells(sheet.getBook().getBookName(), sheet.getSheetName());
        //sheet.getCell(0,0).setValue(0.0);
        sheet.getCell("J7").setFormulaValue("A1+100");
    }

    private void Test6_CreateSheet(SSheet sheet) {
        try {
            loadSheet(sheet, "survey", "harvestdata.jun172015.c046.xlsx", "harvestdata.jun172015.c046");
            //loadSheet(sheet, "survey", "3.17.17_1985-2015_industrials_pull.v7.xlsx",
            //        "Inflows and Outflows");

            //loadSheet(sheet, "survey","Mario.xlsx","Sheet1");
            //   SBook book= BookBindings.getBookByNameDontLoad("ejnnyuhp8");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ArrayList<CellRegion> badCells = getBadCells(sheet.getBook().getBookName(), sheet.getSheetName());
        System.out.println(" Bad cells " + badCells);
        //sheet.getCell(0,0).setValue(0.0);
        sheet.getCell("J7").setFormulaValue("A1+100");
    }


    // Long chanin
    private void Test10_CreateSheet(SSheet sheet) {
        Random random = new Random(7);

        sheet.setDelayComputation(true);
        int N = 1000;

        sheet.getCell(0, 0).setValue(random.nextInt(10000));



        for (int i = 1; i < N; i++) {
            sheet.getCell(i, 0).setFormulaValue("A" + i + " + " + random.nextInt(1000));
            if (i % 100 == 0)
                System.out.println(i);
        }

        sheet.setDelayComputation(false);
    }


    private void Test_Rate_CreateSheet(SSheet sheet) {
        Random random = new Random(7);

        sheet.setDelayComputation(true);
        int N = 1000;

        sheet.getCell(0, 0).setValue(random.nextInt(10000));


        for (int i = 0; i < N; i++) {
            sheet.getCell(i, 1).setFormulaValue("" + random.nextInt(1000));
            sheet.getCell(i, 2).setFormulaValue("A1 * B" + (i+1));
            if (i % 100 == 0)
                System.out.println(i);
        }

        sheet.setDelayComputation(false);
    }

    private void Test_RunningTotalSmart_CreateSheet(SSheet sheet) {
        Random random = new Random(7);

        sheet.setDelayComputation(true);
        int N = 5000;

        sheet.getCell(0, 0).setValue(random.nextInt(10000));
        sheet.getCell(0, 1).setFormulaValue("A1");


        for (int i = 1; i < N; i++) {
            sheet.getCell(i, 0).setFormulaValue("" + random.nextInt(1000));
            sheet.getCell(i, 1).setFormulaValue("A" + (i+1) + " + B" + (i));
            if (i % 100 == 0)
                System.out.println(i);
        }

        sheet.setDelayComputation(false);
    }

    private void Test_RunningTotalDumb_CreateSheet(SSheet sheet) {
        Random random = new Random(7);

        sheet.setDelayComputation(true);
        int N = 1000;


        for (int i = 0; i < N; i++) {
            sheet.getCell(i, 0).setValue(random.nextInt(1000));
            sheet.getCell(i, 1).setFormulaValue("SUM(A1:A" + (i+1) + ")");
            if (i % 100 == 0)
                System.out.println(i);
        }

        sheet.setDelayComputation(false);
    }


    public static ArrayList<CellRegion> getBadCells(String bookName, String sheetname) {
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
                "order by 4 desc " +
                "LIMIT 1;";


        try (AutoRollbackConnection autoRollbackConnection = DBHandler.instance.getConnection();
             PreparedStatement statement = autoRollbackConnection.prepareStatement(sql)) {
            statement.setString(1, bookName);
            statement.setString(2, sheetname);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return badCells;
    }

    @Override
    public void update(SBook book, SSheet sheet, CellRegion cellRegion, Object value, String formula) {
        if (testStarted) {
            //System.out.println("Computed " + cellRegion + " " + formula);
            updatedCells++;
            if (updatedCells == cellsToUpdate)
                synchronized (this) {
                    notify();
                }
        }
    }
}
