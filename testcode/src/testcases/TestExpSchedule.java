package testcases;

import org.zkoss.zss.model.SSheet;

import java.util.Random;

public class TestExpSchedule implements AsyncTestcase {
    // TODO: fix overflow
    private SSheet _sheet;
    private int _N;
    private int _M;
    private int answer;

    public TestExpSchedule(SSheet sheet, int N) {
        _sheet = sheet;
        _N = N;
        _M = 6;

        Random random = new Random(7);

        sheet.setDelayComputation(true);

        sheet.getCell(0, 0).setValue(random.nextInt(1000)+100);

        for (int i = 0; i < N; i++) {
            int num = random.nextInt(1000) + 100;
            sheet.getCell(i, 0).setValue(num);
            answer = 20 * num;
        }

        for (int i = 0; i < _M; i++) {
            int size = (N*(i+1))/_M;
            sheet.getCell(i, 1).setFormulaValue("SUM(A1:A" + (size)+")");
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
        for (int i = 0; i < _M; i++) {
            Object v = _sheet.getCell(i, 1).getValue();
            something += (double) v;
        }
        System.out.println(something);
    }

    @Override
    public boolean verify() {
        return true;
    }
}
