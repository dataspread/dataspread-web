import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.impl.SheetImpl;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

/**
 * Created by zekun.fan@gmail.com on 7/26/17.
 */
public class OfflineFormulaContext {

    public static void main(String[] args) throws Exception {
        SBook testBook = BookBindings.getBookByName("testbook");
        try {
            SheetImpl testSheet = (SheetImpl) testBook.createSheet("testsheet");

            AbstractCellAdv testCell = testSheet.createCell(0, 0),
                    testFormula = testSheet.createCell(0, 1);
            testCell.setValue(1.0, null, false);

            testFormula.setValue("=A1*2", null, false);
            //FormulaAsyncScheduler.getScheduler().addTask(1, new RefImpl(testFormula));
            Thread.sleep(2000);
            System.out.println(testFormula.getValue());
        }finally {
            FormulaAsyncScheduler.getScheduler().shutdown();
        }
    }
}
