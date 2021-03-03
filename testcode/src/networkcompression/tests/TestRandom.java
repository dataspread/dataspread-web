package networkcompression.tests;

import networkcompression.utils.Util;
import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TestRandom extends AsyncBaseTest {

    private final int ITERATIONS;
    private int maxRow = Integer.MIN_VALUE;
    private int maxCol = Integer.MIN_VALUE;

    public TestRandom (final int iterations) {
        ITERATIONS = iterations;
    }

    public TestRandom (SBook book, final int iterations) {
        super(book);
        ITERATIONS = iterations;
    }

    @Override
    public void initSheet () {
        List<String>  cellNames  = new ArrayList<>();
        List<Integer> rowIndices = new ArrayList<>();
        List<Integer> colIndices = new ArrayList<>();

        rowIndices.add(0);
        colIndices.add(0);
        cellNames.add("A1");

        Random random = new Random(7);

        int w = (int) Math.max(3, Math.round(Math.sqrt(ITERATIONS)*1.5));
        for (int i = 1; i < ITERATIONS; i++) {
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
            cellNames.add(CellReference.convertNumToColString(c)+(r+1));
        }

        sheet.setDelayComputation(true);

        sheet.getCell(0, 0).setValue(1000);
        for (int i = 1; i < ITERATIONS; i++) {
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
    public Ref getCellToUpdate () { return sheet.getCell(0, 0).getRef(); }

    @Override
    public void updateCell () { sheet.getCell(0, 0).setValue(20); }

    @Override
    public CellRegion getRegion () { return new CellRegion(0, 0, maxRow, maxCol); }

    @Override
    public AsyncBaseTest newTest () { return new TestRandom(Util.createEmptyBook(), ITERATIONS); }

    @Override
    public String toString () { return "TestRate" + ITERATIONS; }
}
