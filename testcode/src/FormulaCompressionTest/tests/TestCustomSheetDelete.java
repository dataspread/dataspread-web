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
public class TestCustomSheetDelete extends BaseTest {

    private Path path;
    private int rowIndex;
    private int colIndex;
    private String updatedCell;
    private final int modifyCells = 1000;
    private String direction;

    public TestCustomSheetDelete(final Path path, String cell, String direction) {
        super(Util.importBook(path));
        this.path = path;
        this.direction = direction;

        this.updatedCell = cell;
        colIndex = cell.charAt(0) - 'A';
        rowIndex = Integer.parseInt(cell.substring(1)) - 1;
    }

    public String getFileName() {
        return path.getFileName().toString();
    }

    public String getUpdatedCell() {
        return updatedCell;
    }

    @Override
    public void init() {
        sheet.setDelayComputation(true);
        loadBatch();
        refreshDepTable();

        if (direction.compareToIgnoreCase("column") == 0) {
            for (int i = rowIndex; i< rowIndex + modifyCells; i++) {
                sheet.getCell(i, colIndex).setValue(null);
            }
        } else {
            for (int j = colIndex; j< colIndex + modifyCells; j++) {
                sheet.getCell(rowIndex, j).setValue(null);
            }
        }

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
        refreshDepTable();
        sheet.getCell(rowIndex, colIndex).setValue(10);
    }

    @Override
    public String toString() {
        String fileName = path.toFile().getName();
        int index = fileName.lastIndexOf('.');
        if (index != -1) {
            return fileName.substring(0, index);
        } else {
            return "TestCustomSheetDelete" + System.currentTimeMillis();
        }
    }
}

