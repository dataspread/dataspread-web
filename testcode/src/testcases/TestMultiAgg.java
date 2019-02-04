package testcases;

import org.zkoss.zss.model.SSheet;

import java.util.Random;

public class TestMultiAgg implements AsyncTestcase {
    private SSheet _sheet;
    private int _N; // data
    private int _M; // number of aggs to do over data
    private int answer;

    public TestMultiAgg(SSheet sheet, int N, int M) {
        _sheet = sheet;
        _N = N;
        _M = M;

        Random random = new Random(7);

        sheet.setDelayComputation(true);

        /* Change this part */
        sheet.getCell(0, 0).setValue(random.nextInt(100)+10);
        for (int i = 0; i < N; i++) {
            int num = random.nextInt(100);
            sheet.getCell(i, 0).setValue(num);
            if (i % 100 == 0)
                System.out.println(i);
            answer += num;
        }
        for (int i = 0; i < M; i++) {
            sheet.getCell(i, 1).setFormulaValue("SUM(A1:A" + N + ")");
        }
        /* Change this part */

        sheet.setDelayComputation(false);
    }

    @Override
    public void change() {
        _sheet.getCell(0, 0).setValue((double)_sheet.getCell(0,0).getValue() + 1);
    }

    @Override
    public boolean verify() {
        boolean correct = true;
        double calculatedAnswer = 0;
        try {
            for (int i = 0; i < _M; i++) {
                Object v = _sheet.getCell(i, 1).getValue();
                calculatedAnswer = (double) v;
                if (Math.abs(((double)v) - answer) <= 1e-6) {
                    System.out.println("SOMETHING IS WRONG");
                    System.out.println("For cell " + i);
                    System.out.println("MY ANSWER: " + v);
                    System.out.println("CORRECT ANSWER: " + answer);
                    correct = false;
                }
            }
            if (correct) {
                System.out.println("MY ANSWER: " + calculatedAnswer);
                System.out.println("CORRECT ANSWER: " + answer);
                System.out.println("KELLY YOU DID SOMETHING RIGHT");
            }
            else {
                System.out.println("KELLY THIS IS BAD");
            }
            return correct;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void touchAll() {
        double something = 0;
        for (int i = 0; i < _M; i++) {
            Object v = _sheet.getCell(i, 1).getValue();
            something += (double) v;
        }
        System.out.println("Touched Everyting " + something);
    }
}
