package FormulaCompressionTest.tests;

import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.zss.model.CellRegion;

import java.util.ArrayList;
import java.util.Random;
import java.util.List;

public class TestRandom extends BaseTest {

    private final int iterations;
    private int maxRow = Integer.MIN_VALUE;
    private int maxCol = Integer.MIN_VALUE;

    public TestRandom(final int iterations) {
        super();
        this.iterations = iterations;
    }

    @Override
    public void init() {
        List<String>  cellNames  = new ArrayList<>();
        List<Integer> rowIndices = new ArrayList<>();
        List<Integer> colIndices = new ArrayList<>();

        rowIndices.add(0);
        colIndices.add(0);
        cellNames.add("A1");

        Random random = new Random(7);

        int w = (int) Math.max(3, Math.round(Math.sqrt(iterations) * 1.5));
        for (int i = 1; i < iterations; i++) {
            boolean done = false;
            int r = 0;
            int c = 0;
            while (!done) {
                done = true;
                r = random.nextInt(w);
                c = random.nextInt(w);
                for (int j = 0; j < i; j++) {
                    if (r == rowIndices.get(j) && c == colIndices.get(j)) {
                        done = false;
                        break;
                    }
                }
            }
            maxRow = Math.max(maxRow, r);
            maxCol = Math.max(maxCol, c);
            rowIndices.add(r);
            colIndices.add(c);
            cellNames.add(CellReference.convertNumToColString(c) + (r + 1));
        }

        sheet.setDelayComputation(true);

        sheet.getCell(0, 0).setValue(1000);
        for (int i = 1; i < iterations; i++) {
            StringBuilder buf = new StringBuilder("0");
            for (int j = 0; j < i; j++) {
                if (random.nextInt(8) < 2) {
                    buf.append("+").append(cellNames.get(j));
                }
            }
            if (buf.length() == 1) {
                sheet.getCell(rowIndices.get(i), colIndices.get(i)).setValue(0);
            } else {
                sheet.getCell(rowIndices.get(i), colIndices.get(i)).setFormulaValue(buf.toString());
            }
            //System.out.println(CellReference.convertNumToColString(_c.get(i))+(_r.get(i)+1)+" is set to "+buf.toString());
        }

        sheet.setDelayComputation(false);
    }

    @Override
    public Ref getCellToUpdate() {
        return sheet.getCell(0, 0).getRef();
    }

    @Override
    public void updateCell() {
        sheet.getCell(0, 0).setValue(20);
    }

    @Override
    public CellRegion getRegion() {
        return new CellRegion(0, 0, maxRow, maxCol);
    }

    @Override
    public String toString() {
        return "TestRandom" + iterations;
    }
}
