
import org.zkoss.zss.model.impl.*;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.formula.EvaluationResult;
import org.zkoss.zss.model.sys.formula.FormulaEngine;
import org.zkoss.zss.model.sys.formula.FormulaEvaluationContext;
import org.zkoss.zss.model.sys.formula.FormulaExpression;

/**
 * Created by zekun.fan@gmail.com on 7/26/17.
 */
public class OfflineFormulaContext {

    public static void main(String[] args) throws Exception {
        BookImpl testBook=new BookImpl("testbook");
        SheetImpl testSheet=(SheetImpl) testBook.createSheet("testsheet");
        /*
        CellImpl testCell=testSheet.createCell(0,0),
                testFormula=testSheet.createCell(0,1);
        testCell.setValue(1.0,null,false);
        testFormula.setValue("=A1*2",null,false);
        FormulaEngine fe = EngineFactory.getInstance().createFormulaEngine();
        EvaluationResult result = fe.evaluate((FormulaExpression) testFormula.getValue(false),
                new FormulaEvaluationContext(testFormula,new RefImpl(testFormula)));
        */
    }
}
