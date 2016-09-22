package org.zkoss.poi.ss.formula.functions;

import org.zkoss.poi.ss.formula.eval.*;

/**
 * Abstract class to be inherited by union, intersection, set difference, 
 * and cartesian product classes within RelationalOperatorFunction.
 * The only inputs should be two regions of cells.
 * Created by Danny on 9/22/2016.
 */
public abstract class TwoRangeFunction implements Function {
    
    
    public final ValueEval evaluate (ValueEval[] args, int srcCellRow, int srcCellCol) {
        
        try {
            
            if(args.length != 2) {
                
                return ErrorEval.VALUE_INVALID;
            
            }
            
            AreaEval range1 = convertRangeArg(args[0]);
            AreaEval range2 = convertRangeArg(args[1]);
            
            ValueEval result = evaluate(range1, range2);   
            return result;
            
        }
        catch (EvaluationException e) {
            return e.getErrorEval();
        }
        
    }

    
    protected abstract ValueEval evaluate(AreaEval range1, AreaEval range2);


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
