package DependencyTablePgImplCaching.Tests;

import org.junit.*;
import static org.junit.Assert.*;

import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SBook;
import java.util.Random;

/**
 * Test sheet:
 *
 *  |       |   A   |         B         |
 *  |===================================|
 *  |   1   |   ?   |  =SUM(A[1]:A[1])  |
 *  |   2   |   ?   |  =SUM(A[1]:A[2])  |
 *  |  ...  |  ...  |      ...          |
 *  |   N   |   ?   |  =SUM(A[1]:A[N])  |
 */
public class RunningSumTest extends BaseTest {

    private static final int ROWS = 50;

    @Override
    public SSheet createSheet() {
        SBook book = FormulaCompressionTest.utils.Util.createEmptyBook();
        SSheet sheet = null;
        if (book != null) {
            Random random = new Random(42);
            sheet = book.getSheet(0);
            sheet.setDelayComputation(true);
            for (int i = 0; i < ROWS; i++) {
                sheet.getCell(i, 0).setValue(random.nextInt(1000));
                sheet.getCell(i, 1).setFormulaValue("SUM(A1:A" + (i + 1) + ")");
            }
            sheet.setDelayComputation(false);
            cacheAll(sheet, ROWS, 2);
        }
        return sheet;
    }

    @Test
    public void testActualDependentsCaching() {
        runTest(sheet -> prcToDepRef -> depToPrcRef -> {
            for (int i = 0; i < ROWS; i++) {
                final Ref Ai = sheet.getCell(i, 0).getRef();
                final Ref Bi = sheet.getCell(i, 1).getRef();
                assertTrue(prcToDepRef.containsKey(Ai));
                assertTrue(prcToDepRef.containsKey(Bi));
                assertEquals(ROWS - i, sheet.getDependencyTable().getActualDependents(Ai).size());
                assertEquals(0, sheet.getDependencyTable().getActualDependents(Bi).size());
            }
        });
    }

    @Test
    public void testDirectDependentsCaching() {
        runTest(sheet -> prcToDepRef -> depToPrcRef -> {
            for (int i = 0; i < ROWS; i++) {
                final Ref Ai = sheet.getCell(i, 0).getRef();
                final Ref Bi = sheet.getCell(i, 1).getRef();
                assertTrue(prcToDepRef.containsKey(Ai));
                assertTrue(prcToDepRef.containsKey(Bi));
                assertEquals(ROWS - i, prcToDepRef.get(Ai).size());
                assertEquals(0, prcToDepRef.get(Bi).size());
            }
        });
    }

    @Test
    public void testDirectPrecedentsCaching() {
        runTest(sheet -> prcToDepRef -> depToPrcRef -> {
            for (int i = 0; i < ROWS; i++) {
                final Ref Ai = sheet.getCell(i, 0).getRef();
                final Ref Bi = sheet.getCell(i, 1).getRef();
                assertTrue(depToPrcRef.containsKey(Ai));
                assertTrue(depToPrcRef.containsKey(Bi));
                assertEquals(0, depToPrcRef.get(Ai).size());
                assertEquals(1, depToPrcRef.get(Bi).size());
                assertEquals(i + 1, depToPrcRef.get(Bi).iterator().next().getCellCount());
            }
        });
    }

    @Test
    public void testDependentsCaching() {
        runTest(sheet -> prcToDepRef -> depToPrcRef -> {
            for (int i = 0; i < ROWS; i++) {
                final Ref Ai = sheet.getCell(i, 0).getRef();
                final Ref Bi = sheet.getCell(i, 1).getRef();
                assertTrue(prcToDepRef.containsKey(Ai));
                assertTrue(prcToDepRef.containsKey(Bi));
                assertEquals(ROWS - i, prcToDepRef.get(Ai).size());
                assertEquals(0, prcToDepRef.get(Bi).size());
            }
        });
    }

    @Test
    public void testClearDependents() {
        runTest(sheet -> prcToDepRef -> depToPrcRef -> {
            sheet.getDependencyTable().clearDependents(sheet.getCell(0, 0).getRef());
            for (int i = 0; i < ROWS; i++) {
                final Ref Ai = sheet.getCell(i, 0).getRef();
                final Ref Bi = sheet.getCell(i, 1).getRef();
                assertTrue(depToPrcRef.containsKey(Bi));
                if (i == 0) {
                    assertFalse(prcToDepRef.containsKey(Ai));
                    assertEquals(0, depToPrcRef.get(Bi).size());
                } else {
                    assertTrue(prcToDepRef.containsKey(Ai));
                    assertEquals(ROWS - i, prcToDepRef.get(Ai).size());
                    assertEquals(ROWS - i, prcToDepRef.get(Ai).size());
                    assertEquals(1, depToPrcRef.get(Bi).size());
                    assertEquals(i + 1, depToPrcRef.get(Bi).iterator().next().getCellCount());
                }
            }
        });
    }

    @Test
    public void testAddDependent() {
        runTest(sheet -> prcToDepRef -> depToPrcRef -> {
            final Ref lastA = sheet.getCell(ROWS - 1, 0).getRef();
            final Ref firstB = sheet.getCell(0, 1).getRef();
            sheet.getDependencyTable().add(firstB, lastA);
            assertEquals(2, depToPrcRef.get(firstB).size());
            assertEquals(2, prcToDepRef.get(lastA).size());
            assertEquals(2, sheet.getDependencyTable().getActualDependents(lastA).size());
        });
    }

}
