package networkcompression.tests;

import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SBook;

/**
 *
 * The parent class for all async test cases. All subclasses should define
 * at least two constructors. One constructor should initialize all members
 * except the test book. The other constructor should initialize all members
 * and the test book.
 */
public abstract class AsyncBaseTest {

    protected SBook    book  = null;
    protected SSheet   sheet = null;

    public AsyncBaseTest () { }

    public AsyncBaseTest (SBook book) {
        this.book   = book;
        this.sheet  = book.getSheet(0);
    }

    /**
     * @return True if the results of this test case are correct
     * after `updateCell()` is called.
     */
    public boolean verify () { return true; }

    /**
     * Calls `getValue()` on all cells used in this test case to
     * ensure that lazy computation is triggered for them.
     */
    public void touchAll () {
        CellRegion region = this.getRegion();
        for (int r = region.getRow(); r < region.getLastRow(); r++) {
            for (int c = region.getColumn(); c < region.getLastColumn(); c++) {
                this.sheet.getCell(r, c).getValue();
            }
        }
    }

    /**
     * @return The book associated with this test case.
     */
    public SBook getBook () { return this.book; }

    /**
     * @return The sheet associated with this test case.
     */
    public SSheet getSheet () { return this.sheet; }

    /**
     * This method should initialize `sheet` with a particular
     * formula network structure. If the test book is imported,
     * then this method should simply have the following line:
     *
     *  sheet.setDelayComputation(false);
     *
     */
    public abstract void initSheet ();

    /**
     * This method should return the cell that this test case
     * will update for dependency identification.
     *
     */
    public abstract Ref getCellToUpdate ();

    /**
     * This method should update the cell returned by
     * `getCellToUpdate()`.
     *
     */
    public abstract void updateCell ();

    /**
     * @return A cell region that encompasses all the cells
     * that this test uses. This region does not need to be
     * exact. Including a few unused cells shouldn't cause
     * any problems.
     */
    public abstract CellRegion getRegion ();

    /**
     * @return A fresh copy of the current test case with its
     * test sheet and test book initialized.
     */
    public abstract AsyncBaseTest newTest ();

    /**
     * @return The human-readable string representation of
     * this test. Tests with the same test parameters should
     * have the same string representation. If this is not
     * the case, file naming errors may occur.
     */
    @Override
    public abstract String toString ();

}
