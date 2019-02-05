package testcases;

import org.zkoss.zss.model.SSheet;

import java.util.Random;

public class TestRate implements AsyncTestcase {
    // TODO: fix overflow
    private SSheet _sheet;
    private int _N;
    private int answer;

    public TestRate(SSheet sheet, int N) {
        _sheet = sheet;
        _N = N;

        Random random = new Random(7);

        sheet.setDelayComputation(true);

        sheet.getCell(0, 0).setValue(random.nextInt(1000)+100);

        for (int i = 0; i < N; i++) {
            int num = random.nextInt(1000);
            sheet.getCell(i, 1).setValue(num);
            sheet.getCell(i, 2).setFormulaValue("A1 * B" + (i+1));
            answer = 20 * num;
        }

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
            Object v = _sheet.getCell(i, 2).getValue();
            something += (double) v;
        }
        System.out.println(something);
    }

    @Override
    public boolean verify() {
        try {
            Object value_raw = _sheet.getCell(_N - 1, 2).getValue();
            double value = (double) value_raw;
            return Math.abs(value - answer) <= 1e-6;
        } catch (Exception e) {
            return false;
        }
    }
}
