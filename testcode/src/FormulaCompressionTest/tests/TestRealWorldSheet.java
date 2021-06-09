package FormulaCompressionTest.tests;

import FormulaCompressionTest.utils.Util;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.sys.dependency.DependencyTable;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.nio.file.Path;

public class TestRealWorldSheet extends BaseTest {

    private SCell cellToUpdate;
    private Path path;

    public TestRealWorldSheet(final Path path) {
        super(Util.importBook(path));
        this.path = path;
    }

    /**
     * NOTE: Using sheet.getCellIterator() and sheet.getRowIterator() doesn't seem to work here.
     */
    @Override
    public void init() {
        DependencyTable deps = sheet.getDependencyTable();
        int maxDependents = Integer.MIN_VALUE;
        for (int r = sheet.getStartRowIndex(); r <= sheet.getEndRowIndex(); r++) {
            for (int c = sheet.getStartColumnIndex(); c <= sheet.getEndColumnIndex(); c++) {
                SCell cell = sheet.getCell(r, c);
                int numDependents = deps.getDependents(cell.getRef()).size();
                if (numDependents > maxDependents) {
                    maxDependents = numDependents;
                    cellToUpdate = cell;
                }
            }
        }
    }

    @Override
    public Ref getCellToUpdate() {
        return cellToUpdate.getRef();
    }

    @Override
    public void updateCell() {
        cellToUpdate.setValue(0);
    }

    @Override
    public String toString() {
        String fileName = path.toFile().getName();
        int index = fileName.lastIndexOf('.');
        if (index != -1) {
            return fileName.substring(0, index);
        } else {
            return "TestRealWorldSheet" + System.currentTimeMillis();
        }
    }

}
