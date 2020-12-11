package testcases;
import org.zkoss.zss.model.SSheet;

import java.util.*;

public class TestRandHops implements AsyncTestcase {
    // TODO: fix overflow
    private SSheet _sheet;
    private int _N;
    private int answer;
    private int maxChainLength = 100;

    /**
     * Creates the following graph structure:
     *
     * A1
     * |--> B1 --> B2 --> ... (length is random) ... --> B?
     * |
     * |--> B? --> B? --> ... (length is random) ... --> B?
     * |
     * |--> B? --> B? --> ... (length is random) ... --> B?
     * |
     * ...
     */
    // TODO:
    //  As shown in the comment above, we use cells in column B to create a majority
    //  of the graph. This makes the code simple to write, but if we pass in a very
    //  large N we will quickly run out of cells in column B. For now it isn't a
    //  major issue, but just something to keep in mind.
    public TestRandHops(SSheet sheet, int N) {
        _sheet = sheet;
        _N = N;
        answer = 0;

        Random random = new Random(7);

        sheet.setDelayComputation(true);

        sheet.getCell(0, 0).setValue(random.nextInt(1000)+100);

        int rowCntr = 1;
        for (int i = 0; i < N; i++) {
            sheet.getCell(rowCntr-1, 1).setFormulaValue("A1");
            rowCntr++;
            for (int j = 0; j < 1+random.nextInt(maxChainLength); j++) {
                sheet.getCell(rowCntr-1, 1).setFormulaValue("B" + (rowCntr-1) + " + A1");
                rowCntr++;
            }
        }

        System.err.println("Exact Cell Count: " + rowCntr);
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
        return true;
    }
}

