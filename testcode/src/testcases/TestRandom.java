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
        _name.add(CellReference.convertNumToColString(0)+"1");

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
            StringBuffer buf = new StringBuffer("1");
            for (int j = 0; j < i; j++) {
                if (random.nextBoolean()) {
                    buf.append("+" + _name.get(j));
                }
            }
            if (i % 100 == 0)
                System.out.println(i);
            if (buf.length() == 1) {
                sheet.getCell(_r.get(i), _c.get(i)).setValue(1);
            } else {
                sheet.getCell(_r.get(i), _c.get(i)).setFormulaValue(buf.toString());
            }
            System.out.println(CellReference.convertNumToColString(_c.get(i))+(_r.get(i)+1)+" is set to "+buf.toString());
        }

        sheet.setDelayComputation(false);
    }

    @Override
    public void change() {
        _sheet.getCell(0, 0).setValue(20);
    }

    @Override
    public boolean verify() {
        double something = 0.0;
        for (int i = 0; i < _N; i++) {
            Object v = _sheet.getCell(_r.get(i), _c.get(i)).getValue();
            System.out.println(CellReference.convertNumToColString(_c.get(i))+(_r.get(i)+1)+" has value "+ v);
        }
        System.out.println(something);
        return true;
    }
}
