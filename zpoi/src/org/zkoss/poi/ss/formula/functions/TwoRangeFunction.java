package org.zkoss.poi.ss.formula.functions;

import org.zkoss.poi.ss.formula.eval.*;

/**
 * Abstract class to be inherited by union, intersection, set difference, 
 * and cartesian product classes within RelationalOperatorFunction.
 * The only inputs should be two regions of cells.
 * Created by Danny on 9/22/2016.
 */
public abstract class TwoRangeFunction implements Function {


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

    public final ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        
        try {
            
            if(args.length != 2) {
                
                return ErrorEval.VALUE_INVALID;
            
            }

            AreaEval area1 = convertRangeArg(args[0]);
            AreaEval area2 = convertRangeArg(args[1]);
            ArrayEval array1 = convertToArray(area1);
            ArrayEval array2 = convertToArray(area2);

            ValueEval result = evaluate(array1, array2, srcRowIndex, srcColumnIndex);   
            return result;
            
        }
        catch (EvaluationException e) {
            return e.getErrorEval();
        }
        
    }

    protected abstract ValueEval evaluate(ArrayEval range1, ArrayEval range2, int srcRowIndex, int srcColumnIndex);

    /**
     * Parts of function taken from Fixed1ArgFunction.java
     * @param area
     * @return
     */
    //ZSS-852
    private ArrayEval convertToArray(AreaEval area) {

        final int width = area.getWidth();
        final int height = area.getHeight();

        ValueEval[][] results = new ValueEval[height][];

        for (int r = 0; r < height; ++r) {

            results[r] = new ValueEval[width];

            for (int c = 0; c < width; ++c) {

                final ValueEval ve = area.getRelativeValue(r, c);
                results[r][c] = ve;
            }
        }

        return new ArrayEval(results, area.getFirstRow(), area.getFirstColumn(), area.getLastRow(), area.getLastColumn(), area.getRefEvaluator()); //ZSS-962
    }
    
}
