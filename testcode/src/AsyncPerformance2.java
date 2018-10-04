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
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncListener;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerSimple;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerThreaded;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class AsyncPerformance2 implements FormulaAsyncListener {
    private static Connection conn;
    int cellCount = 5000;
    long initTime;
    boolean testStarted = false;
    final boolean sync=false;
    private long controlReturnedTime;
    private long computationDoneTime;


    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://127.0.0.1:5432/ibd";
        String driver = "org.postgresql.Driver";
        String userName = "mangesh";
        String password = "mangesh";
        DBHandler.connectToDB(url, driver, userName, password);

        SheetImpl.simpleModel = true;
        SheetImpl.disablePrefetch();
        //FormulaAsyncScheduler formulaAsyncScheduler = new FormulaAsyncSchedulerPriority();
        FormulaAsyncScheduler formulaAsyncScheduler = new FormulaAsyncSchedulerSimple();
        Thread asyncThread = new Thread(formulaAsyncScheduler);
        asyncThread.start();


        GraphCompressor  graphCompressor = new GraphCompressor();
        //Thread graphThread = new Thread(graphCompressor);
        //graphThread.start();

        //simpleTest(formulaAsyncScheduler);
        //realTest("survey", "Escalating OSA with Cost Share.xlsx", "Cost Share", formulaAsyncScheduler);

        //graphCompressor.shutdown();
        //graphThread.join();
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

    private static void compressGraphNode(String bookName, String sheetname, CellRegion cellRegion) {
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

    private static void compressDependencies(ArrayList<CellRegion> dependencies) {
        while (dependencies.size() > 20) {
            int best_i = 0, best_j = 0, best_area = Integer.MAX_VALUE;
            CellRegion best_bounding_box = null;
            for (int i = 0; i < dependencies.size() - 1; i++) {
                for (int j = i + 1; j < dependencies.size(); j++) {
                    CellRegion bounding = dependencies.get(i).getBoundingBox(dependencies.get(j));
                    int new_area = bounding.getCellCount() -
                            dependencies.get(i).getCellCount() - dependencies.get(j).getCellCount();
                    CellRegion overlap = dependencies.get(i).getOverlap(dependencies.get(j));
                    if (new_area==0)
                    {
                        best_area = new_area;
                        best_i = i;
                        best_j = j;
                        best_bounding_box = bounding;
                        i=dependencies.size();
                        break;
                    }

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
            dependencies.remove(best_j);
            dependencies.remove(best_i);
            dependencies.add(best_bounding_box);
        }
    }

    public void simpleTest()
    {
        SBook book = BookBindings.getBookByName("testBook" + System.currentTimeMillis());
        /* Cleaner for sync computation */
        if (sync)
            FormulaCacheCleaner.setCurrent(new FormulaCacheCleaner(book.getBookSeries()));
        SSheet sheet = book.getSheet(0);
        //sheet.setSyncComputation(true);

        for (int i=1;i<=cellCount;i++)
            sheet.getCell(i,2).setValue(System.currentTimeMillis());

        sheet.getCell(0,0).setValue(10);
        for (int i=1;i<=cellCount;i++)
            sheet.getCell(i,0).setFormulaValue("A1" + "+" + (System.currentTimeMillis()%5000) + "+" + i);
        //sheet.clearCache();



       //compressGraphNode(sheet.getBook().getBookName(), sheet.getSheetName(), new CellRegion(0,0));

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sheet.setSyncComputation(sync);

        DirtyManagerLog.instance.init();
        CellImpl.disableDBUpdates = true;
        testStarted = true;

        initTime = System.currentTimeMillis();
        System.out.println("Starting Asyn " + initTime);

        System.out.println("Before Update "
                + sheet.getCell(cellCount, 0).getValue());

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
    }

    @Override
    public void update(SBook book, SSheet sheet, CellRegion cellRegion, String value, String formula) {
        //System.out.println("Computed " + cellRegion + " " + formula);
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


            DirtyManagerLog.instance.groupPrint(sheetCells, controlReturnedTime, initTime);

            long totalWaitTime = cells.stream()
                    .mapToLong(e -> DirtyManagerLog.instance.getDirtyTime(e.getCellRegion()))
                    .sum();
            System.out.println("Total Wait time " + totalWaitTime);
            System.out.println("Avg Wait time " + totalWaitTime / cells.size());
            synchronized (this) {
                notify();
            }
        }
    }
}
