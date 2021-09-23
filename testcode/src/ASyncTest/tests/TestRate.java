package ASyncTest.tests;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.util.Random;

public class TestRate extends AsyncBaseTest {

    private final int rows;
    private int answer;

    public static AsyncTestFactory getFactory(final int rows) {
        return new AsyncTestFactory() {
            @Override
            public AsyncBaseTest createTest() {
                return new TestRate(rows);
            }

            @Override
            public String toString() {
                return "TestRate" + rows;
            }
        };
    }

    public TestRate(final int rows) {
        super();
        this.rows = rows;
    }

    @Override
    public void init() {
        Random random = new Random(7);

        sheet.setDelayComputation(true);

        sheet.getCell(0, 0).setValue(random.nextInt(1000) + 100);

        for (int i = 0; i < rows; i++) {
            int num = random.nextInt(1000);
            sheet.getCell(i, 1).setValue(num);
            sheet.getCell(i, 2).setFormulaValue("A1 * B" + (i + 1));
            answer = 20 * num;
        }

        sheet.setDelayComputation(false);
    }

    @Override
    public void touchAll() {
        double result = 0.0;
        for (int i = 0; i < rows; i++) {
            Object v = sheet.getCell(i, 2).getValue();
            result += (double) v;
        }
        System.out.println(result);
    }

    @Override
    public boolean verify() {
        try {
            Object value_raw = sheet.getCell(rows - 1, 2).getValue();
            double value = (double) value_raw;
            return Math.abs(value - answer) <= 1e-6;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Ref getCellToUpdate() {
        return sheet.getCell(0, 0).getRef();
    }

    @Override
    public void updateCell() {
        sheet.getCell(0, 0).setValue(20);
    }

    @Override
    public CellRegion getRegion() {
        return new CellRegion(0, 0, rows - 1, 2);
    }

}
