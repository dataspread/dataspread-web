package testcases;
import org.zkoss.zss.model.SSheet;

import java.util.*;

public class TestMultipleDeepHops implements AsyncTestcase {
    // TODO: fix overflow
    private SSheet _sheet;
    private int _N;
    private int answer;
    private int maxChainLength = 100;
    private int k = 10;

    /**
     * Creates the following graph structure:
     *
     * A1 -> B(1) -> B(?) -> ... -> B(?)
     * |
     * ----> B(2) -> B(?) -> ... -> B(?)
     * |
     * ...
     * |
     * ----> B(N/(k-1)) -> B(?) -> ... -> B(?)
     * |
     * ----> B(N/k)
     * |
     * ...
     * |
     * ----> B(N)
     *
     * That is, some portion of the graph consists of deep chains and the other portion does not
     */
    // TODO:
    //  As shown in the comment above, we use cells in column B to create a majority
    //  of the graph. This makes the code simple to write, but if we pass in a very
    //  large N we will quickly run out of cells in column B. For now it isn't a
    //  major issue, but just something to keep in mind.
    public TestMultipleDeepHops(SSheet sheet, int N) {
        _sheet = sheet;
        _N = N;
        answer = 20;

        Random random = new Random(7);

        sheet.setDelayComputation(true);

        sheet.getCell(0, 0).setValue(random.nextInt(1000)+100);
        for (int i = 0; i < N; i++) {
            sheet.getCell(i, 1).setFormulaValue("A1");
        }

        int rowCntr = N;
        for (int i = 0; i < N / k; i++) {
            sheet.getCell(rowCntr++, 0).setValue("B" + (i + 1));
            int start = rowCntr;
            for (int j = start; j < start + maxChainLength; j++, rowCntr++) {
                sheet.getCell(j, 1).setFormulaValue("B" + j);
            }
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

