package FormulaCompressionTest.tests;

import FormulaCompressionTest.runners.TestStats;
import FormulaCompressionTest.utils.Util;

import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.zkoss.util.Pair;
import org.zkoss.zss.model.impl.BookImpl;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SBook;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * The parent class for all tests. All subclasses should have at
 * least two constructors. One constructor should be public and
 * initialize all test parameters except the test book. The other
 * constructor should be private and initialize all members with
 * the test book.
 */
public abstract class BaseTest {

    protected SBook book;
    protected SSheet sheet;
    protected List<Ref> dependencies = null;

    protected BaseTest() {
        this(Util.createEmptyBook());
    }

    protected BaseTest(SBook book) {
        this.book = book;
        this.sheet = book.getSheet(0);
    }

    public void configDepTable(int depTblCacheSize,
                               int compConstant) {
        sheet.getDependencyTable().configDepedencyTable(depTblCacheSize, compConstant);
    }

    public void refreshDepTable() {
        sheet.getDependencyTable()
                .refreshCache(book.getBookName(),sheet.getSheetName());
    }

    public long getLastAddBatchTime() {
        return sheet.getDependencyTable().getLastAddBatchTime();
    }

    public long getLastLookupTime() {
        return sheet.getDependencyTable().getLastLookupTime();
    }

    public long getLastRefreshCacheTime() {
        return sheet.getDependencyTable().getLastRefreshCacheTime();
    }

    public void loadBatch() {
        List<Pair<Ref, Ref>> loadedBatch = sheet.getDependencyTable().getLoadedBatch(book.getBookName(), sheet.getSheetName());
        sheet.getDependencyTable().addBatch(book.getBookName(), sheet.getSheetName(), loadedBatch);
    }

    /**
     * @return The book associated with this test case.
     */
    public SBook getBook() {
        return book;
    }

    /**
     * @return The sheet associated with this test case.
     */
    public SSheet getSheet() {
        return sheet;
    }

    /**
     * This method should initialize `sheet` with a particular
     * formula network structure. If the test book is imported,
     * then this method should simply have the following line:
     *
     *  sheet.setDelayComputation(false);
     *
     */
    public abstract void init();

    /**
     * Calls `getValue()` on all cells used in this test case
     * to ensure that lazy computation is triggered for them.
     */
    public void touchAll() {
        CellRegion region = this.getRegion();
        for (int r = region.getRow(); r < region.getLastRow(); r++) {
            for (int c = region.getColumn(); c < region.getLastColumn(); c++) {
                sheet.getCell(r, c).getValue();
            }
        }
    }

    /**
     * @return True if the results of this test case are correct
     * after `updateCell()` is called.
     */
    public boolean verify() {
        return true;
    }

    /**
     * @return The cells that this test case uses.
     */
    public Collection<CellRegion> getCells() {
        return Util.getSheetCells(this.getSheet(), this.getRegion());
    }

    /**
     * This method should return the cell that this test case
     * will update for dependency identification.
     */
    public abstract Ref getCellToUpdate();

    /**
     * @return A list that contains all dependents of this test
     * case's updated cell.
     */
    public List<Ref> getDependenciesOfUpdatedCell() {
        if (dependencies == null) {
            dependencies =
                    new ArrayList<>(this.getSheet().getDependencyTable()
                            .getDependents(this.getCellToUpdate()));
        }
        return dependencies;
    }

    public void genCellsToUpdate(Set<CellRegion> cellsToUpdateSet,
                                 TestStats testStats) {
        List<Ref> dependenciesMultpl = new ArrayList<>();
        Set<CellRegion> dependenciesSingle = new HashSet<>();
        for (Ref dependency :
                Util.addAndReturn(getDependenciesOfUpdatedCell(), getCellToUpdate())) {
            if (dependency.getCellCount() == 1) {
                dependenciesSingle.add(new CellRegion(dependency));
            } else {
                dependenciesMultpl.add(dependency);
            }
        }
        for (CellRegion sheetCell : getCells()) {
            boolean matched = false;
            if (dependenciesSingle.contains(sheetCell)) {
                testStats.numberOfCellsToUpdate++;
                matched = true;
            }
            for (Ref dependency : dependenciesMultpl) {
                CellRegion reg = new CellRegion(dependency);
                if (reg.contains(sheetCell)) {
                    testStats.numberOfCellsToUpdate++;
                    matched = true;
                }
            }
            if (matched) {
                cellsToUpdateSet.add(sheetCell);
            }
        }
    }

    /**
     * This method should update the cell returned by
     * `getCellToUpdate()`.
     */
    public abstract void updateCell();

    public void execAfterUpdate() {}

    /**
     * @return A cell region that encompasses all the cells
     * that this test uses. This region does not need to be
     * exact. Including a few unused cells shouldn't cause
     * any problems.
     */
    public CellRegion getRegion() {
        return new CellRegion(
                sheet.getStartRowIndex(),
                sheet.getStartColumnIndex(),
                sheet.getEndRowIndex(),
                sheet.getEndColumnIndex()
        );
    }

    public void cleanup() {
        // Clean up sheet table
        while (book.getNumOfSheet() > 0) {
            book.deleteSheet(book.getSheet(0));
        }

        // Delete row from book table
        BookImpl.deleteBook(book.getBookName(), book.getId());

        // Clean up other tables
        String query = "DELETE FROM " + DBHandler.userBooks + " WHERE booktable = ?";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, book.getId());
            statement.execute();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }
}
