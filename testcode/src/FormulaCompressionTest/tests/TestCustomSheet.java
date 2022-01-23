package FormulaCompressionTest.tests;

        import FormulaCompressionTest.utils.Util;
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
public class TestCustomSheet extends BaseTest {

    private Path path;
    private int rowIndex;
    private int colIndex;

    public TestCustomSheet(final Path path, String cell) {
        super(Util.importBook(path));
        this.path = path;

        colIndex = cell.charAt(0) - 'A';
        rowIndex = Integer.parseInt(cell.substring(1)) - 1;
    }

    @Override
    public void init() {
        sheet.setDelayComputation(true);
        loadBatch();
        refreshDepTable();
        sheet.setDelayComputation(false);
    }

    @Override
    public boolean verify() {
        // return Double.compare((double) sheet.getCell(9, 0).getValue(), 55.0) == 0;
        return true;
    }

    @Override
    public Ref getCellToUpdate() {
        return sheet.getCell(rowIndex, colIndex).getRef();
    }

    @Override
    public void updateCell() {
        sheet.getCell(rowIndex, colIndex).setValue(10);
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
}

