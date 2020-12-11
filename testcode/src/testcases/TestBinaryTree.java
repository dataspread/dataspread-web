package testcases;
import org.zkoss.zss.model.SSheet;
import java.util.Random;

public class TestBinaryTree implements AsyncTestcase {
    // TODO: fix overflow
    private SSheet _sheet;
    private int _N;
    private int answer;

    public TestBinaryTree(SSheet sheet, int N) {
        _sheet = sheet;
        _N = N;
        answer = 20;

        Random random = new Random(7);
        int num;

        sheet.setDelayComputation(true);

        sheet.getCell(0, 0).setValue(random.nextInt(1000)+100);
        sheet.getCell(0, 1).setFormulaValue("A1");
        sheet.getCell(1, 1).setFormulaValue("A1");

        int rowCount = 2;
        for (int i = 2, j = 1; i < N; i += 2, j++) {
            sheet.getCell(i, 1).setFormulaValue("B" + j);
            sheet.getCell(i+1, 1).setFormulaValue("B" + j);
            rowCount+=2;
        }
        System.err.println("Rows created: " + rowCount);

        sheet.setDelayComputation(false);
    }

    @Override
    public void change() {
        _sheet.getCell(0, 0).setValue(20);
    }

    @Override
    public void touchAll() {
        double something = 0.0;
        for (int i = 0; i < _N; i++) {
            Object v = _sheet.getCell(i, 1).getValue();
            something += (double) v;
        }
        System.out.println(something);
    }

    @Override
    public boolean verify() {
        try {
            Object value_raw = _sheet.getCell(_N - 1, 1).getValue();
            double value = (double) value_raw;
            return Math.abs(value - answer) <= 1e-6;
        } catch (Exception e) {
            return false;
        }
    }
}

