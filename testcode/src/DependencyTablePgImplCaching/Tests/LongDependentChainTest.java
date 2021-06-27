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
 *  |   1   |   ?   |   =A[1]           |
 *  |   2   |   ?   |   =A[2] * B[1]    |
 *  |  ...  |  ...  |      ...          |
 *  |   N   |   ?   |  =A[N] * B[N-1]   |
 */
public class LongDependentChainTest extends BaseTest {

    private static final int ROWS = 50;

    @Override
    public SSheet createSheet() {
        SBook book = FormulaCompressionTest.utils.Util.createEmptyBook();
        SSheet sheet = null;
        if (book != null) {
            Random random = new Random(42);
            sheet = book.getSheet(0);
            sheet.setDelayComputation(true);
            sheet.getCell(0, 0).setValue(random.nextInt(1000));
            sheet.getCell(0, 1).setFormulaValue("A1");
            for (int i = 1; i < ROWS; i++) {
                sheet.getCell(i, 0).setValue(random.nextInt(1000));
                sheet.getCell(i, 1).setFormulaValue("A" + (i + 1) + "*" + "B" + i);
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
                assertEquals(ROWS - i - 1, sheet.getDependencyTable().getActualDependents(Bi).size());
            }
        });
    }

    @Test
    public void testDirectDependentsCaching() {
        runTest(sheet -> prcToDepRef -> depToPrcRef -> {
            for (int i = 0; i < ROWS - 1; i++) {
                final Ref Ai = sheet.getCell(i, 0).getRef();
                final Ref Bi = sheet.getCell(i, 1).getRef();
                assertTrue(prcToDepRef.containsKey(Ai));
                assertTrue(prcToDepRef.containsKey(Bi));
                assertEquals(1, prcToDepRef.get(Ai).size());
                assertEquals(1, prcToDepRef.get(Bi).size());
            }
            assertTrue(prcToDepRef.containsKey(sheet.getCell(ROWS - 1, 0).getRef()));
            assertTrue(prcToDepRef.containsKey(sheet.getCell(ROWS - 1, 1).getRef()));
            assertEquals(1, prcToDepRef.get(sheet.getCell(ROWS - 1, 0).getRef()).size());
            assertEquals(0, prcToDepRef.get(sheet.getCell(ROWS - 1, 1).getRef()).size());
        });
    }

    @Test
    public void testDirectPrecedentsCaching() {
        runTest(sheet -> prcToDepRef -> depToPrcRef -> {
            assertTrue(depToPrcRef.containsKey(sheet.getCell(0, 0).getRef()));
            assertTrue(depToPrcRef.containsKey(sheet.getCell(0, 1).getRef()));
            assertEquals(0, depToPrcRef.get(sheet.getCell(0, 0).getRef()).size());
            assertEquals(1, depToPrcRef.get(sheet.getCell(0, 1).getRef()).size());
            for (int i = 1; i < ROWS; i++) {
                final Ref Ai = sheet.getCell(i, 0).getRef();
                final Ref Bi = sheet.getCell(i, 1).getRef();
                assertTrue(depToPrcRef.containsKey(Ai));
                assertTrue(depToPrcRef.containsKey(Bi));
                assertEquals(0, depToPrcRef.get(Ai).size());
                assertEquals(2, depToPrcRef.get(Bi).size());
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
                assertEquals(ROWS - i, sheet.getDependencyTable().getActualDependents(Ai).size());
                assertEquals(ROWS - i - 1, sheet.getDependencyTable().getActualDependents(Bi).size());
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
                assertTrue(prcToDepRef.containsKey(Bi));
                assertTrue(depToPrcRef.containsKey(Bi));
                if (i == 0) {
                    assertFalse(prcToDepRef.containsKey(Ai));
                    assertEquals(0, depToPrcRef.get(Bi).size());
                } else {
                    assertTrue(prcToDepRef.containsKey(Ai));
                    assertEquals(1, prcToDepRef.get(Ai).size());
                    assertEquals(ROWS - i, sheet.getDependencyTable().getActualDependents(Ai).size());
                    assertEquals(0, depToPrcRef.get(Ai).size());
                }
                assertEquals(i == ROWS - 1 ? 0 : 1, prcToDepRef.get(Bi).size());
                assertEquals(ROWS - i - 1, sheet.getDependencyTable().getActualDependents(Bi).size());
                assertEquals(i == 0 ? 0 : 2, depToPrcRef.get(Bi).size());
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
        });
    }

}
