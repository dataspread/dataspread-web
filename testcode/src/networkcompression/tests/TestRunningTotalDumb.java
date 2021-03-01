package networkcompression.tests;

import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.CellRegion;

import java.util.*;

/**
 *
 * An example of how to create an async test case.
 */
public class TestRunningTotalDumb extends AsyncBaseTest {

    private final int ROWS;

    public TestRunningTotalDumb (final boolean isTemplate, final int rows) {
        super(isTemplate);
        this.ROWS = rows;
    }

    @Override
    public void initSheet() {
        Random random = new Random(7);

        sheet.setDelayComputation(true);

        sheet.getCell(0, 0).setValue(random.nextInt(1000) + 100);
        sheet.getCell(0, 1).setFormulaValue("A1");

        for (int i = 1; i < ROWS; i++) {
            int num = random.nextInt(1000);
            sheet.getCell(i, 0).setValue(num);
            sheet.getCell(i, 1).setFormulaValue("SUM(A1:A" + (i + 1) + ")");
        }

        sheet.setDelayComputation(false);
    }

    @Override
    public Ref getCellToUpdate() { return super.sheet.getCell(0, 0).getRef(); }

    @Override
    public void updateCell() { super.sheet.getCell(0, 0).setValue(20); }

    @Override
    public CellRegion getRegion () { return new CellRegion(0, 0, ROWS, 1); }

    @Override
    public AsyncBaseTest duplicate (final boolean init) { return new TestRunningTotalDumb(init, ROWS); }

    @Override
    public String toString () { return "TestRunningTotalDumb" + ROWS; }

}
