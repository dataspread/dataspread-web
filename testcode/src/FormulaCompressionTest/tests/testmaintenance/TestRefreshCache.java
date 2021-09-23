package FormulaCompressionTest.tests.testmaintenance;

import FormulaCompressionTest.tests.BaseTest;
import org.zkoss.util.Pair;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.util.List;
import java.util.Random;

public class TestRefreshCache extends BaseTest {

    private final int rows;
    private final int modifyRows;
    private int answer;

    public TestRefreshCache(final int rows, final int modifyRows) {
        super();
        this.rows = rows;
        this.modifyRows = modifyRows;
    }

    @Override
    public void init() {
        answer = 20;

        Random random = new Random(7);

        sheet.setDelayComputation(true);

        sheet.getCell(0, 0).setValue(random.nextInt(1000) + 100);
        sheet.getCell(0, 1).setFormulaValue("A1");

        for (int i = 1; i < rows; i++) {
            int num = random.nextInt(1000);
            sheet.getCell(i, 0).setValue(num);
            sheet.getCell(i, 1).setFormulaValue("SUM(A1:A" + (i + 1) + ")");
            answer += num;
        }

        List<Pair<Ref, Ref>> loadedBatch =
                sheet.getDependencyTable().getLoadedBatch(book.getBookName(), sheet.getSheetName());
        sheet.getDependencyTable().addBatch(book.getBookName(), sheet.getSheetName(), loadedBatch);

        for (int i = 1; i< modifyRows; i++) {
            sheet.getCell(i, 1).setFormulaValue("A" + (i + 1) + " + B" + (i));
        }

        sheet.setDelayComputation(false);
    }

    @Override
    public void touchAll() {
        double result = 0.0;
        for (int i = 0; i < rows; i++) {
            Object v = sheet.getCell(i, 1).getValue();
            if (v != null) result += (double) v;
        }
        System.out.println(result);
    }

    @Override
    public boolean verify() {
        return true;
    }

    @Override
    public Ref getCellToUpdate() {
        return sheet.getCell(0, 0).getRef();
    }

    @Override
    public void updateCell() {
        refreshDepTable();
        sheet.getCell(0, 0).setValue(20);
    }

    @Override
    public void execAfterUpdate() {
    }

    @Override
    public CellRegion getRegion() {
        return new CellRegion(0, 0, rows - 1, 1);
    }

    @Override
    public String toString() {
        return "TestRefreshCache" + rows;
    }
}
