package testcases;
import org.zkoss.zss.model.SSheet;

import java.util.*;

public class TestImbalancedHops implements AsyncTestcase {
    // TODO: fix overflow
    private SSheet _sheet;
    private int _N;
    private int answer;
    private int maxChainLength = 100;

    /**
     * Creates the following graph structure:
     *
     * A1 -> B(maxChainLength + 1) -> B(maxChainLength + 2)
     * |        |
     * |        -----> B(maxChainLength + 3)
     * |        |
     * |        -----> B(maxChainLength + 4)
     * |        |
     * |        ...
     * |        |
     * |        -----> B(maxChainLength + N)
     * |
     * ----> B(1) -> B(2) -> ... -> B(maxChainLength)
     *
     * There are 2 routes, but going down one route will make more cells available
     * in a shorter amount of time. The number of dependents of B(N+1) is controlled
     * by the instance variable `k`.
     */
    // TODO:
    //  As shown in the comment above, we use cells in column B to create a majority
    //  of the graph. This makes the code simple to write, but if we pass in a very
    //  large N we will quickly run out of cells in column B. For now it isn't a
    //  major issue, but just something to keep in mind.
    public TestImbalancedHops(SSheet sheet, int N) {
        _sheet = sheet;
        _N = N;
        answer = 20;

        Random random = new Random(7);

        sheet.setDelayComputation(true);

        sheet.getCell(0, 0).setValue(random.nextInt(1000)+100);

        // Create deep chain
        sheet.getCell(0, 1).setFormulaValue("A1");
        for (int i = 1; i < maxChainLength; i++) {
            sheet.getCell(i, 1).setFormulaValue("B" + i);
        }

        // Create shallow chain
        sheet.getCell(maxChainLength, 1).setFormulaValue("A1");
        for (int i = maxChainLength + 1; i < maxChainLength + N + 1; i++) {
            sheet.getCell(i, 1).setFormulaValue("B" + (maxChainLength + 1));
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

