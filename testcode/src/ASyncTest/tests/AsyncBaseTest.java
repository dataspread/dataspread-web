package ASyncTest.tests;

import ASyncTest.utils.Util;

import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SBook;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

/**
 * The parent class for all tests. All subclasses should have at
 * least two constructors. One constructor should be public and
 * initialize all test parameters except the test book. The other
 * constructor should be private and initialize all members with
 * the test book.
 */
public abstract class AsyncBaseTest {

    protected SBook book;
    protected SSheet sheet;

    protected AsyncBaseTest() {
        this(Util.createEmptyBook());
    }

    protected AsyncBaseTest(SBook book) {
        this.book = book;
        this.sheet = book.getSheet(0);
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
        return new ArrayList<>(this.getSheet().getDependencyTable().getDependents(this.getCellToUpdate()));
    }

    /**
     * This method should update the cell returned by
     * `getCellToUpdate()`.
     */
    public abstract void updateCell();

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

}
