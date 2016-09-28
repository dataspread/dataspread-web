package org.zkoss.poi.ss.formula.functions;

import org.zkoss.poi.ss.formula.eval.*;

import java.util.Arrays;

/**
 * Abstract class to be inherited by join function in RelationalOperatorFunction.
 * Created by Danny on 9/22/2016.
 */
public abstract class JoinFunction implements Function {

    public final ValueEval evaluate (ValueEval[] args, int srcCellRow, int srcCellCol) {
        
        try {
            
            if (args.length < 3) {
                
                return ErrorEval.VALUE_INVALID;
                
            }
            
            AreaEval range1 = convertRangeArg(args[0]);
            AreaEval range2 = convertRangeArg(args[1]);
            ValueEval[] conditions = Arrays.copyOfRange(args, 1, args.length);
            
            return evaluate(range1, range2, conditions);            
            
        }
        catch (EvaluationException e) {
            return e.getErrorEval();
        }
        
    }
    
    
    protected abstract ValueEval evaluate(AreaEval range1, AreaEval range2, ValueEval[] args);


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
            return ((RefEval)eval).offset(0, 0, 0, 0);
        }
        throw new EvaluationException(ErrorEval.VALUE_INVALID);
    }
    
}
