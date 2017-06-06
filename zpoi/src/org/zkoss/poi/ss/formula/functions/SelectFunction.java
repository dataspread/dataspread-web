package org.zkoss.poi.ss.formula.functions;

import org.zkoss.poi.ss.formula.eval.*;

/**
 * Abstract class to be inherited by select function within RelationalOperatorFunction
 * The inputs should be a region of cells and conditions (if any).
 * The first input arg[0] should be the region, and the rest of args are conditions
 * Created by Danny on 9/22/2016.
 */
public abstract class SelectFunction implements Function {

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

            if (args.length < 1) {

                return ErrorEval.VALUE_INVALID;

            }

            AreaEval range = convertRangeArg(args[0]);

            /** TODO
             * no need to separate conditions here
             */
            //no conditions
            if (args.length == 1) {

                return evaluate(range, srcCellRow, srcCellCol);

            }
            //evaluate with conditions
            else {

                ValueEval condition = args[1];
                return evaluate(range, condition, srcCellRow, srcCellCol);

            }

        }
        catch (EvaluationException e) {
            return e.getErrorEval();
        }

    }

    protected abstract ValueEval evaluate(AreaEval range, int srcRowIndex, int srcColumnIndex);

    protected abstract ValueEval evaluate(AreaEval range, ValueEval condition, int srcRowIndex, int srcColumnIndex);
    
}
