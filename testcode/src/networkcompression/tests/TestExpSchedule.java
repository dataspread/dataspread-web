package networkcompression.tests;

import networkcompression.utils.Util;

import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;

import java.util.Random;

public class TestExpSchedule extends AsyncBaseTest {

    private final int CELLS_IN_COLUMN_A;
    private final int CELLS_IN_COLUMN_B;
    private double answer = 0;

    public TestExpSchedule (final int a, final int b) {
        CELLS_IN_COLUMN_A = a;
        CELLS_IN_COLUMN_B = b;
    }

    private TestExpSchedule (SBook book, final int a, final int b) {
        super(book);
        CELLS_IN_COLUMN_A = a;
        CELLS_IN_COLUMN_B = b;
    }

    @Override
    public void initSheet() {
        Random random = new Random(7);

        sheet.setDelayComputation(true);

        sheet.getCell(0, 0).setValue(random.nextInt(1000)+100);

        for (int i = 0; i < CELLS_IN_COLUMN_A; i++) {
            int num = random.nextInt(1000) + 100;
            sheet.getCell(i, 0).setValue(num);
            answer = 20 * num;
        }

        for (int i = 0; i < CELLS_IN_COLUMN_B; i++) {
            int size = (CELLS_IN_COLUMN_A*(i+1))/CELLS_IN_COLUMN_B;
            sheet.getCell(i, 1).setFormulaValue("SUM(A1:A" + size + ")");
        }

        sheet.setDelayComputation(false);
    }

    @Override
    public Ref getCellToUpdate() { return sheet.getCell(0, 0).getRef(); }

    @Override
    public void updateCell() { sheet.getCell(0, 0).setValue(20); }

    @Override
    public CellRegion getRegion() { return new CellRegion(0, 0, CELLS_IN_COLUMN_A, 1); }

    @Override
    public AsyncBaseTest newTest() { return new TestExpSchedule(Util.createEmptyBook(), CELLS_IN_COLUMN_A, CELLS_IN_COLUMN_B); }

    @Override
    public String toString() { return "TestExpSchedule-(" + CELLS_IN_COLUMN_A + ", " + CELLS_IN_COLUMN_B + ")"; }
}
