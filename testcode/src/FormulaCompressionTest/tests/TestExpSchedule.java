package FormulaCompressionTest.tests;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.util.Random;

public class TestExpSchedule extends BaseTest {

    private final int columnARows;
    private final int columnBRows;
    private int answer;

    public TestExpSchedule(final int columnARows, final int columnBRows) {
        super();
        this.columnARows = columnARows;
        this.columnBRows = columnBRows;
    }

    @Override
    public void init() {
        Random random = new Random(7);

        sheet.setDelayComputation(true);

        sheet.getCell(0, 0).setValue(random.nextInt(1000) + 100);

        for (int i = 0; i < columnARows; i++) {
            int num = random.nextInt(1000) + 100;
            sheet.getCell(i, 0).setValue(num);
            answer = 20 * num;
        }

        for (int i = 0; i < columnBRows; i++) {
            int size = (columnARows * (i + 1)) / columnBRows;
            sheet.getCell(i, 1).setFormulaValue("SUM(A1:A" + size + ")");
        }

        sheet.setDelayComputation(false);
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
        return new CellRegion(0, 0, Math.max(columnARows, columnBRows), 1);
    }

    @Override
    public String toString() {
        return "TestExpSchedule-(" + columnARows + ", " + columnBRows + ")";
    }
}
