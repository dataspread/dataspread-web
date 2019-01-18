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
import testcases.AsyncTestcase;
import testcases.TestRunningTotalDumb;
import testcases.TestRunningTotalSmart;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class AsyncPerformance3 implements FormulaAsyncListener {
    final int testSize = 1000;
    final boolean sync = false;
    final boolean graphCompression = true;
    static int graphCompressionSize = 2;

    long initTime;
    boolean testStarted = false;

    final static boolean graphInDB = false;
    private static Connection conn;

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

        AsyncPerformance3 asyncPerformance = new AsyncPerformance3();
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
        /*if (sync)
            FormulaCacheCleaner.setCurrent(new FormulaCacheCleaner(book.getBookSeries()));*/

        uiVisibleMap = new HashMap<>();
        FormulaAsyncScheduler.updateVisibleMap(uiVisibleMap);


        SSheet sheet = book.getSheet(0);

        // Update visible Map
        // Sheet->{session->visibleRange}
        if (window!=null)
            uiVisibleMap.put(sheet, ImmutableMap.of("Session1", new int[]{window.getRow(), window.getLastRow()}));



        System.out.println("Starting data creation");
        sheet.setSyncComputation(true);
        AsyncTestcase test = new TestRunningTotalSmart(sheet, testSize);
        sheet.setSyncComputation(sync);
        System.out.println("Data Creation done");


        // Get dependencies ready
        Ref updatedCell = sheet.getCell(0, 0).getRef();
        ArrayList<Ref> dependencies1 = new ArrayList<>(sheet.getDependencyTable().getDependents(updatedCell));
        cellsToUpdate = dependencies1.size();

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

        // Begin Test
        DirtyManagerLog.instance.init();
        CellImpl.disableDBUpdates = true;
        testStarted = true;

        initTime = System.currentTimeMillis();
        System.out.println("Starting Asyn " + initTime);

        test.change();

        controlReturnedTime = System.currentTimeMillis();

        System.out.println("Before Waiting Correct?: " + (test.verify() ? "YES" : "NO"));

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



        System.out.println("Final Value Correct?: " + (test.verify() ? "YES" : "NO"));


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
