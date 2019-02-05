package testcases;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SSheet;

import java.util.Random;

public class TestMultiLevelAgg implements AsyncTestcase {
    private SSheet _sheet;
    private int _N; // number of students
    private int _M; // number of test scores
    private double answer;
    public TestMultiLevelAgg(SSheet sheet, int N, int M) {
        _sheet = sheet;
        _N = N;
        _M = M;

        Random random = new Random(8);

        sheet.setDelayComputation(true);

        /* Change this part */
        sheet.getCell(0, 0).setValue(random.nextInt(100)+10);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                int num = random.nextInt(10);
                sheet.getCell(i, j).setValue(num);
                answer += num;
            }
            CellRegion region = new CellRegion(i,0,i,M-1);
            String stringRegion = region.getReferenceString();
            String formula = "SUM(" + stringRegion + ")";
            sheet.getCell(i, M).setFormulaValue(formula); // @KELLY fix this
        }
        CellRegion bigSumRegion = new CellRegion(0, M, N-1, M);
        String bigSumRegionString = bigSumRegion.getReferenceString();
        String formula = "AVERAGE(" + bigSumRegionString + ")";
        sheet.getCell(N,M).setFormulaValue(formula);
        answer /= _N;
        /* Change this part */

        sheet.setDelayComputation(false);
    }

    @Override
    public void change() {
        _sheet.getCell(0, 0).setValue((double)_sheet.getCell(0,0).getValue() + 1);
    }

    @Override
    public boolean verify() {
        double something = 0;
        try {
            for (int i = 0; i < _N; i++) {
                Object v = _sheet.getCell(i, _M).getValue();
                something += (double) v;
            }
            something /= _N;
            System.out.println("MANUAL AVERAGE GIVES: " + something);

            Object value_raw = _sheet.getCell(_N,_M).getValue();

            double value = (double) value_raw;

            System.out.println("MY ANSWER: " + value);
            System.out.println("CORRECT ANSWER: " + answer);
            return Math.abs(value - answer) <= 1e-6;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void touchAll() {
        double something = 0;
        for (int i = 0; i < _N; i++) {
            Object v = _sheet.getCell(i, _M).getValue();
            something += (double) v;
        }
        something /= _N;

        something += (double) _sheet.getCell(_N,_M).getValue();

        System.out.println("Touched Everything: " + something);
    }
}
