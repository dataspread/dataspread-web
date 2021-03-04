package networkcompression.tests;

import networkcompression.utils.Util;

import org.zkoss.zss.model.sys.dependency.DependencyTable;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;

import java.nio.file.Path;

public class TestRealWorldSheet extends AsyncBaseTest {

    private final Path  PATH;

    private SCell cellToUpdate;

    public TestRealWorldSheet (Path path) {
        PATH = path;
    }

    private TestRealWorldSheet (SBook book, Path path) {
        super(book);
        PATH = path;
    }

    /**
     * NOTE: Using sheet.getCellIterator() and sheet.getRowIterator() doesn't seem to work here.
     */
    @Override
    public void init() {
        DependencyTable deps = sheet.getDependencyTable();
        int maxDependents = Integer.MIN_VALUE;
        for (int r = sheet.getStartRowIndex(); r <= sheet.getEndRowIndex(); r++) {
            for (int c =  sheet.getStartColumnIndex(); c <= sheet.getEndColumnIndex(); c++) {
                SCell cell = sheet.getCell(r, c);
                int numDependents = deps.getDependents(cell.getRef()).size();
                if (numDependents > maxDependents) {
                    maxDependents = numDependents;
                    cellToUpdate = cell;
                }
            }
        }
        sheet.setDelayComputation(false);
    }

    @Override
    public Ref getCellToUpdate() { return cellToUpdate.getRef(); }

    @Override
    public void updateCell() { cellToUpdate.setValue(0); }

    @Override
    public CellRegion getRegion() {
        return new CellRegion(
            sheet.getStartRowIndex(),
            sheet.getStartColumnIndex(),
            sheet.getEndRowIndex(),
            sheet.getEndColumnIndex()
        );
    }

    @Override
    public AsyncBaseTest newTest() { return new TestRealWorldSheet(Util.importBook(PATH), PATH); }

    @Override
    public String toString() {
        String fileName = PATH.toFile().getName();
        int index = fileName.lastIndexOf('.');
        if (index != -1) {
            return fileName.substring(0, index);
        } else {
            return "TestRealWorldSheet" + System.currentTimeMillis();
        }
    }
}
