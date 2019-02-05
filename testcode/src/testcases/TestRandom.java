package testcases;

import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.zss.model.SSheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestRandom implements AsyncTestcase {
    // TODO: fix overflow
    private SSheet _sheet;
    private int _N;
    private int answer;
    private List<Integer> _r;
    private List<Integer> _c;
    private List<String> _name;

    public TestRandom(SSheet sheet, int N) {
        _sheet = sheet;
        _N = N;
        _r = new ArrayList<>();
        _c = new ArrayList<>();
        _name = new ArrayList<>();

        _r.add(0);
        _c.add(0);
        _name.add("A1");

        Random random = new Random(7);

        int w = (int) Math.max(3, Math.round(Math.sqrt(N)*1.5));
        for (int i = 1; i < N; i++) {
            boolean done = false;
            int r = 0;
            int c = 0;
            while (!done) {
                done = true;
                r = random.nextInt(w);
                c = random.nextInt(w);
                for (int j = 0; j < i; j++) {
                    if (r == _r.get(j) && c == _c.get(j)) {
                        done = false;
                        break;
                    }
                }
            }
            _r.add(r);
            _c.add(c);
            _name.add(CellReference.convertNumToColString(c)+(r+1));
        }

        sheet.setDelayComputation(true);

        _sheet.getCell(0, 0).setValue(1000);
        for (int i = 1; i < N; i++) {
            StringBuffer buf = new StringBuffer("0");
            for (int j = 0; j < i; j++) {
                if (random.nextInt(8) < 2) {
                    buf.append("+" + _name.get(j));
                }
            }
            if (buf.length() == 1) {
                sheet.getCell(_r.get(i), _c.get(i)).setValue(0);
            } else {
                sheet.getCell(_r.get(i), _c.get(i)).setFormulaValue(buf.toString());
            }
            //System.out.println(CellReference.convertNumToColString(_c.get(i))+(_r.get(i)+1)+" is set to "+buf.toString());
        }

        sheet.setDelayComputation(false);
    }

    @Override
    public void change() {
        _sheet.getCell(0, 0).setValue(20);
    }

    @Override
    public void touchAll() {
        for (int i = 0; i < _N; i++) {
            _sheet.getCell(_r.get(i), _c.get(i)).getValue();
        }
    }

    @Override
    public boolean verify() {
        try {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
