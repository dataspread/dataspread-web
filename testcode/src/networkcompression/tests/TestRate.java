package networkcompression.tests;

import networkcompression.utils.Util;

import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;

import java.util.Random;

public class TestRate extends AsyncBaseTest {

    private final int ROWS;
    private int answer = 20;

    public TestRate (final int rows) {
        ROWS = rows;
    }

    public TestRate (SBook book, final int rows) {
        super(book);
        ROWS = rows;
    }

    @Override
    public boolean verify() {
        try {
            Object value_raw = sheet.getCell(ROWS - 1, 2).getValue();
            double value = (double) value_raw;
            return Math.abs(value - answer) <= 1e-6;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void initSheet() {
        Random random = new Random(7);

        sheet.setDelayComputation(true);

        sheet.getCell(0, 0).setValue(random.nextInt(1000)+100);

        for (int i = 0; i < ROWS; i++) {
            int num = random.nextInt(1000);
            sheet.getCell(i, 1).setValue(num);
            sheet.getCell(i, 2).setFormulaValue("A1 * B" + (i+1));
            answer = 20 * num;
        }

        sheet.setDelayComputation(false);
    }

    @Override
    public Ref getCellToUpdate() { return sheet.getCell(0, 0).getRef(); }

    @Override
    public void updateCell() { sheet.getCell(0, 0).setValue(20); }

    @Override
    public CellRegion getRegion() { return new CellRegion(0, 0, ROWS - 1, 2); }

    @Override
    public AsyncBaseTest newTest() { return new TestRate(Util.createEmptyBook(), ROWS); }

    @Override
    public String toString() { return "TestRate" + ROWS; }
}
