package networkcompression.tests;

import networkcompression.utils.Util;

import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;

import java.nio.file.Path;

/**
 *
 * An example of how to import your own excel file for testing.
 * The corresponding excel file for this test case is:
 *
 *      |    A
 *  ----------------
 *  1   |   =10
 *  ----------------
 *  2   |   =A1 + 1
 *  ----------------
 *  3   |   =A2 + 2
 *  ----------------
 *  ... |
 *  ----------------
 *  10  |   =A9 + 9
 *
 * IMPORTANT: If you would like to import your own excel sheet
 * for testing, you may encounter an error if the sheet is named
 * Sheet1 or similar. To remedy this, simply rename the sheet to
 * something else.
 */
public class TestCustomStructure extends AsyncBaseTest {

    private Path path;

    public TestCustomStructure (Path pathToExcelFile) {
        this.path = pathToExcelFile;
    }

    public TestCustomStructure (SBook book, Path pathToExcelFile) {
        super(book);
        this.path = pathToExcelFile;
    }

    @Override
    public boolean verify () { return Double.compare((double) sheet.getCell(9, 0).getValue(), 55.0) == 0; }

    @Override
    public void initSheet () { sheet.setDelayComputation(false); }

    @Override
    public Ref getCellToUpdate () { return sheet.getCell(0, 0).getRef(); }

    @Override
    public void updateCell () { sheet.getCell(0, 0).setValue(10); }

    @Override
    public CellRegion getRegion () { return new CellRegion(0, 0, 9, 0); }

    @Override
    public AsyncBaseTest newTest () { return new TestCustomStructure(Util.importBook(this.path), this.path); }

    @Override
    public String toString () {
        String fileName = this.path.toFile().getName();
        int index = fileName.lastIndexOf('.');
        if (index != -1) {
            return fileName.substring(0, index);
        } else {
            return "TestCustomStructure" + System.currentTimeMillis();
        }
    }

}
