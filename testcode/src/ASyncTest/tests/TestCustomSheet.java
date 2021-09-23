package ASyncTest.tests;

import ASyncTest.utils.Util;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.nio.file.Path;

/**
 * An example of how to import your own excel file for testing.
 * The corresponding excel file for this test case is:
 * <p>
 * |    A
 * ----------------
 * 1   |   =1
 * ----------------
 * 2   |   =A1 + 1
 * ----------------
 * 3   |   =A2 + 2
 * ----------------
 * ... |
 * ----------------
 * 10  |   =A9 + 9
 * <p>
 * IMPORTANT: If you would like to import your own excel sheet
 * for testing, you may encounter an error if the sheet is named
 * Sheet1 or similar. To remedy this, simply rename the sheet to
 * something else.
 */
public class TestCustomSheet extends AsyncBaseTest {

    public static AsyncTestFactory getFactory(final Path path) {
        return new AsyncTestFactory() {
            @Override
            public AsyncBaseTest createTest() {
                return new TestCustomSheet(path);
            }

            @Override
            public String toString() {
                String fileName = path.toFile().getName();
                int index = fileName.lastIndexOf('.');
                if (index != -1) {
                    return fileName.substring(0, index);
                } else {
                    return "TestCustomSheet" + System.currentTimeMillis();
                }
            }
        };
    }

    public TestCustomSheet(final Path path) {
        super(Util.importBook(path));
    }

    @Override
    public void init() {
        sheet.setDelayComputation(false);
    }

    @Override
    public boolean verify() {
        return Double.compare((double) sheet.getCell(9, 0).getValue(), 55.0) == 0;
    }

    @Override
    public Ref getCellToUpdate() {
        return sheet.getCell(0, 0).getRef();
    }

    @Override
    public void updateCell() {
        sheet.getCell(0, 0).setValue(10);
    }

}
