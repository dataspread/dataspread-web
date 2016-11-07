package org.zkoss.poi.ss.formula.functions;

import org.zkoss.poi.ss.formula.eval.*;

import java.util.Arrays;

/**
 * Abstract class to be inherited by project and rename functions in RelationalOperatorFunction.
 * Created by Danny on 9/22/2016.
 */
public abstract class RangeSchemaFunction implements Function {

    /**
     * Function taken from Sumif.java
     * convertRangeArg takes a ValueEval and attempts to convert it to
     * an AreaEval or RefEval
     *
     * @param eval
     * @return
     * @throws EvaluationException
     */
    private static AreaEval convertRangeArg(ValueEval eval) throws EvaluationException {
        if (eval instanceof AreaEval) {
            return (AreaEval) eval;
        }
        if (eval instanceof RefEval) {
            return ((RefEval) eval).offset(0, 0, 0, 0);
        }
        throw new EvaluationException(ErrorEval.VALUE_INVALID);
    }
    
    public final ValueEval evaluate (ValueEval[] args, int srcCellRow, int srcCellCol) {
        
        try {

            if (args.length < 2) {
                
                return ErrorEval.VALUE_INVALID;
                
            }
            
            AreaEval range = convertRangeArg(args[0]);
            ValueEval[] schema = Arrays.copyOfRange(args, 1, args.length);

            return evaluate(range, schema, srcCellRow, srcCellCol);            
            
        }
        catch (EvaluationException e) {
            return e.getErrorEval();
        }        
        
    }

    protected abstract ValueEval evaluate(AreaEval range, ValueEval[] args, int srcRowIndex, int srcColumnIndex);
}
