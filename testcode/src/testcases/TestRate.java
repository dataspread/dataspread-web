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
            sheet.getCell(i, 1).setValue(random.nextInt(1000));
            sheet.getCell(i, 2).setFormulaValue("A1 * B" + (i+1));
            if (i % 100 == 0)
                System.out.println(i);
        }

        sheet.setDelayComputation(false);
    }

    @Override
    public void change() {
        _sheet.getCell(0, 0).setValue(20);
    }

    @Override
    public boolean verify() {
        return true;
    }
}
