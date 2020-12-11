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
import org.zkoss.zss.model.impl.sys.DependencyTableImplCostApprox;
import org.zkoss.zss.model.impl.sys.DependencyTablePGImpl;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncListener;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerCostApprox;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.formula.DirtyManager;
import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;
import testcases.*;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class AsyncPerformanceCostApprox implements FormulaAsyncListener {
    // Test params
    final static boolean graphInDB = false;
    final static boolean areaUnderCurveGraph = true;

    // Test stats
    private boolean testStarted = false;
    private long initTime;
    private long controlReturnedTime;
    private long updatedCells = 0;
    private long cellsToUpdate = 0;

    private Set<CellRegion> cellsToUpdateSet;

    private static Connection conn;

    final CellRegion window = null;
    //final CellRegion window = new CellRegion(0, 0, 50, 10);

    // Sheet->{session->visibleRange}
    Map<Object, Map<String, int[]>> uiVisibleMap;


    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://127.0.0.1:5433/dataspread";
        String driver = "org.postgresql.Driver";
        String userName = "dataspreaduser";
        String password = "password";

        Properties props = new Properties();
        props.setProperty("user", userName);
        props.setProperty("password", password);
        conn = DriverManager.getConnection(url, props);

        if (graphInDB)
            EngineFactory.dependencyTableClazz = DependencyTablePGImpl.class;
        else
            EngineFactory.dependencyTableClazz = DependencyTableImplCostApprox.class;

        DBHandler.connectToDB(url, driver, userName, password);

        SheetImpl.simpleModel = true;
        //SheetImpl.disablePrefetch();
        //FormulaAsyncScheduler formulaAsyncScheduler = new FormulaAsyncSchedulerPriority();

        //singleTest(TestRunningTotalDumb.class, 10000, false, 0, false);
        multipleTests();
    }

    public static void multipleTests() {
        final Class testCases[] = {TestRandHops.class};
        final int testSizes[]   = {150000};

        final String names[]                   = {"brn1", "brn2", "dependents001"};
        final boolean syncs[]                  = {true, false, false};
        final int compressionSizes[]           = {0, 0, 0};
        final boolean schedulerPrioritizes[]   = {false, false, true};
        final int schedulerHopsToUse[]         = {1, 1, 1};
        final boolean schedulerUsePrecedents[] = {true, true, false};

//        final String names[]                   = {"precedents001"};
//        final boolean syncs[]                  = {false};
//        final int compressionSizes[]           = {0};
//        final boolean schedulerPrioritizes[]   = {true};
//        final int schedulerHopsToUse[]         = {1};
//        final boolean schedulerUsePrecedents[] = {true};

        for (int testSize : testSizes) {
            for (Class testCase : testCases) {
                for (int run = 0; run < 11; run++) {
                    for (int setup = 0; setup < names.length; setup++) {
                        PrintStream p = null;
                        try {
                            String name = names[setup];
                            boolean sync = syncs[setup];
                            boolean schedulerPrioritize = schedulerPrioritizes[setup];
                            int compressionSize = compressionSizes[setup];
                            int hops = schedulerHopsToUse[setup];
                            boolean usePrecedents = schedulerUsePrecedents[setup];

                            String testFullName = "test_" + testSize + "_" + name + "_" + run  + ".txt";
                            FileOutputStream f = new FileOutputStream("./AsyncCostApproxTestReportsRandHops/" + name + "/" + testFullName);
                            p = new PrintStream(f);
                            System.setOut(p);

                            System.out.println(testFullName);
                            System.err.println(" *********** NEW CASE *********** " + testFullName);
                            singleTest(testCase, testSize, sync, compressionSize, schedulerPrioritize, hops, usePrecedents);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (p != null) {
                                p.flush();
                            }
                            System.gc();
                        }
                        try {
                            Thread.sleep(5000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static void singleTest(Class testCase, int testSize, boolean sync, int compressionSize, boolean schedulerPrioritize, int hops, boolean precedents) throws Exception {
        DirtyManager.dirtyManagerInstance.reset();

        // Set the number of hops and the cost approximation method here!
        FormulaAsyncSchedulerCostApprox costApproxAsyncScheduler = new FormulaAsyncSchedulerCostApprox();
        costApproxAsyncScheduler.setHops(hops);
        if (precedents) {
            costApproxAsyncScheduler.usePrecedents();
        } else {
            costApproxAsyncScheduler.useDependents();
        }

        Thread asyncThread = new Thread(costApproxAsyncScheduler);
        asyncThread.start();

        AsyncPerformanceCostApprox asyncPerformance = new AsyncPerformanceCostApprox();
        FormulaAsyncScheduler.initFormulaAsyncListener(asyncPerformance);
        FormulaAsyncScheduler.setPrioritize(schedulerPrioritize);
        asyncPerformance.simpleTest(testCase, testSize, sync, compressionSize);

        ((FormulaAsyncScheduler) costApproxAsyncScheduler).shutdown();

        while (!((FormulaAsyncSchedulerCostApprox) FormulaAsyncScheduler.getScheduler()).isDead) {
            Thread.sleep(10);
        }
        ((FormulaAsyncSchedulerCostApprox) FormulaAsyncScheduler.getScheduler()).started = false;
        ((FormulaAsyncSchedulerCostApprox) FormulaAsyncScheduler.getScheduler()).isDead = false;
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

    private static void compressPGGraphNode(String bookName, String sheetname, CellRegion cellRegion, int graphCompressionSize) {
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
            compressDependencies(deps, graphCompressionSize);
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

    private static void compressDependencies1(ArrayList<Ref> dependencies, int graphCompressionSize) {
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

                    if (best_area == 0) {
                        break;
                    }
                }
                if (best_area == 0) {
                    break;
                }
            }
            // Merge i,j
            //ystem.out.println(best_i + " " + best_j + " " + dependencies.get(best_i) + " " + dependencies.get(best_j) + " " + best_bounding_box);
            dependencies.remove(best_j);
            dependencies.remove(best_i);
            dependencies.add(best_bounding_box);
        }
    }

    private static void compressDependencies(ArrayList<CellRegion> dependencies, int graphCompressionSize) {
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


    public void simpleTest(Class testCase, int testSize, boolean sync, int compressionSize) throws Exception {
        boolean graphCompression = compressionSize > 0;
        testStarted = false;
        updatedCells = 0;
        cellsToUpdate = 0;
        CellImpl.disableDBUpdates = false;

        long timeNow = System.currentTimeMillis();
        SBook book = BookBindings.getBookByName("testBook" + timeNow);
        System.out.println("TIME BEGIN: "+timeNow);
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

        // Generate test case. Change this into the test case.

        AsyncTestcase test;
        if (!testCase.equals(TestMultiLevelAgg.class) && !testCase.equals(TestMultiAgg.class)) {
            Constructor ctor = testCase.getConstructor(SSheet.class, int.class);
            test = (AsyncTestcase) ctor.newInstance(new Object[] { sheet, testSize });
        } else {
            Constructor ctor = testCase.getConstructor(SSheet.class, int.class, int.class);
            test = (AsyncTestcase) ctor.newInstance(new Object[] { sheet, testSize, 20 });
        }

        //AsyncTestcase test = new TestRunningTotalDumb(sheet, testSize);
        sheet.setSyncComputation(sync);
        System.err.println("Data Creation done");
        System.out.println("Data Creation done");

        // Obtain the collection of all cells.
        Collection<CellRegion> sheetCells;
        if (window == null) {
            sheetCells = sheet.getCells(new CellRegion(0, 0, testSize+5, 5)).stream().map(SCell::getCellRegion)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else {
            sheetCells = sheet.getCells(window).stream().map(SCell::getCellRegion)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }


        // Initiate dependency, do compression if asked.
        System.out.println("Start obtaining dependents");
        Ref updatedCell = sheet.getCell(0, 0).getRef();
        ArrayList<Ref> dependencies1 = new ArrayList<>(sheet.getDependencyTable().getDependents(updatedCell));

        System.out.println("Dependencies " + dependencies1.size());
        cellsToUpdate = 0;
        cellsToUpdateSet = new HashSet<>();
        if (graphCompression) {
            // For PG graph
            if (graphInDB) {
                compressPGGraphNode(sheet.getBook().getBookName(),
                        sheet.getSheetName(), new CellRegion(0, 0), compressionSize);
            } else {
                compressDependencies1(dependencies1, compressionSize);
                sheet.getDependencyTable().addPreDep(updatedCell, new HashSet<>(dependencies1));
            }
            dependencies1.add(updatedCell);
            // Count cells that needs to be updated before you are done
            for (CellRegion sheetCell : sheetCells) {
                boolean matched = false;
                for (Ref dependency : dependencies1) {
                    CellRegion reg = new CellRegion(dependency);
                    if (reg.contains(sheetCell)) {
                        cellsToUpdate++;
                        matched = true;
                    }
                }
                if (matched) {
                    cellsToUpdateSet.add(sheetCell);
                }
            }
        } else {
            dependencies1.add(updatedCell);
            // Count cells that needs to be updated before you are done
            Set<CellRegion> dependenciesSingle = new HashSet<>();
            ArrayList<Ref> dependenciesMult = new ArrayList<>();
            for (Ref dependency : dependencies1) {
                if (dependency.getCellCount() == 1) {
                    dependenciesSingle.add(new CellRegion(dependency));
                } else {
                    dependenciesMult.add(dependency);
                }
            }
            for (CellRegion sheetCell : sheetCells) {
                boolean matched = false;
                if (dependenciesSingle.contains(sheetCell)) {
                    cellsToUpdate++;
                    matched = true;
                }
                for (Ref dependency : dependenciesMult) {
                    CellRegion reg = new CellRegion(dependency);
                    if (reg.contains(sheetCell)) {
                        cellsToUpdate++;
                        matched = true;
                    }
                }
                if (matched) {
                    cellsToUpdateSet.add(sheetCell);
                }
            }
        }

        System.err.println("Cells to Update (counting dupes) " + cellsToUpdate);
        System.out.println("Cells to Update (counting dupes) " + cellsToUpdate);

        // Begin Test
        DirtyManagerLog.instance.init();
        CellImpl.disableDBUpdates = true;
        testStarted = true;

        initTime = System.currentTimeMillis();
        System.out.println("Starting Asyn " + initTime);
        //  Thread.sleep(30000);

        // ****** DO THE ONE-CELL VALUE CHANGE ******
        test.change();
        //Thread.sleep(30000);


        controlReturnedTime = System.currentTimeMillis();
        System.out.println("Control returned  " + controlReturnedTime);
        System.out.println("Time to update = " + (controlReturnedTime - initTime));
        System.out.println("Before Waiting Correct?: " + (test.verify() ? "YES" : "NO"));
        ((FormulaAsyncSchedulerCostApprox) FormulaAsyncScheduler.getScheduler()).started = true;
        if (!sync) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        test.touchAll();
        long touchedTime = System.currentTimeMillis();

        // By here, the final value should be correct
        System.out.println("Final Value Correct?: " + (test.verify() ? "YES" : "NO"));
        System.err.println("Final Value Correct?: " + (test.verify() ? "YES" : "NO"));

        if (sync) {
            System.out.println(0 + "\t" + sheetCells.size());
            System.out.println(touchedTime - initTime + "\t" + sheetCells.size());
            System.out.println(touchedTime - initTime + "\t" + "0");
        } else {
            if (areaUnderCurveGraph) {
                double area = DirtyManagerLog.instance.groupPrint(sheetCells, controlReturnedTime, initTime, true);
                System.out.println("Updated cells = " + updatedCells);
                System.out.println("TIME END: "+System.currentTimeMillis());
                System.out.println("AREA UNDER CURVE IS " + area);
            }
            else {
                DirtyManagerLog.instance.groupPrint(sheetCells, controlReturnedTime, initTime); // utilize group print
                System.out.println("Updated cells = " + updatedCells);
                System.out.println("TIME END: "+System.currentTimeMillis());
            }
        }

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
            //System.out.println("Computed "+updatedCells+"/"+cellsToUpdate);
            /*if (updatedCells == cellsToUpdate)
                synchronized (this) {
                    notify();
                }*/
            cellsToUpdateSet.remove(cellRegion);
            if (cellsToUpdateSet.size() == 0) {
                synchronized (this) {
                    notify();
                }
            }
        }
    }
}
