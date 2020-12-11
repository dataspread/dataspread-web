package testcases;
import org.zkoss.zss.model.SSheet;

import java.util.*;

public class TestDeepHops implements AsyncTestcase {
    // TODO: fix overflow
    private SSheet _sheet;
    private int _N;
    private int answer;
    private int maxChainLength = 100;

    /**
     * Creates the following graph structure:
     *
     * A1 -> B(1) -> B(N+1) -> B(N+2) -> ... -> B(N+maxChainLength)
     * |
     * ----> B(2)
     * |
     * ----> B(3)
     * |
     * ...
     * |
     * ----> B(N)
     *
     */
    // TODO:
    //  As shown in the comment above, we use cells in column B to create a majority
    //  of the graph. This makes the code simple to write, but if we pass in a very
    //  large N we will quickly run out of cells in column B. For now it isn't a
    //  major issue, but just something to keep in mind.
    public TestDeepHops(SSheet sheet, int N) {
        _sheet = sheet;
        _N = N;
        answer = 20;

        Random random = new Random(7);

        sheet.setDelayComputation(true);

        sheet.getCell(0, 0).setValue(random.nextInt(1000)+100);
        for (int i = 0; i < N; i++) {
            sheet.getCell(i, 1).setFormulaValue("A1");
        }

        sheet.getCell(N, 0).setValue("B1");
        for (int i = N + 1; i < N + maxChainLength + 1; i++) {
            sheet.getCell(i, 1).setFormulaValue("B" + i);
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
            // System.err.println("expected: " + answer);
            // System.err.println("got: " + value);
            return Math.abs(value - answer) <= 1e-6;
        } catch (Exception e) {
            return false;
        }
    }
}

