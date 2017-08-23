package org.zkoss.poi.ss.formula.functions;

import org.zkoss.poi.ss.formula.eval.*;

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


    /**
     * If the second argument is not an array containing attributes, throw an exception.
     *
     * @param eval
     * @return
     * @throws EvaluationException
     */
    private static ArrayEval convertArrayArg(ValueEval eval) throws EvaluationException {
        if (eval instanceof ArrayEval) {
            return (ArrayEval) eval;
        }
        throw new EvaluationException(ErrorEval.VALUE_INVALID);
    }


    public final ValueEval evaluate(ValueEval[] args, int srcCellRow, int srcCellCol) {

        try {

            if (args.length != 2) {

                return ErrorEval.VALUE_INVALID;

            }

            AreaEval range = convertRangeArg(args[0]);
            ArrayEval attributes = convertArrayArg(args[1]);
            String[] attributeNames = extractAttributeNames(attributes);

            return evaluate(range, attributeNames, srcCellRow, srcCellCol);

        } catch (EvaluationException e) {
            return e.getErrorEval();
        }

    }


    /**
     * Extract the attribute names from the ArrayEval containing them.
     *
     * @param attributes
     * @return
     * @throws EvaluationException
     */
    private String[] extractAttributeNames(ArrayEval attributes) throws EvaluationException {

        int height = attributes.getHeight();
        int width = attributes.getWidth();
        String[] attributeNames = new String[width];

        //input should be a 1-D array of attributes
        if (height != 1) {
            throw new EvaluationException(ErrorEval.VALUE_INVALID);
        }

        int row = 0;
        for (int col = 0; col < width; col++) {
            StringEval attr = (StringEval) attributes.getValue(row, col);
            attributeNames[col] = attr.getStringValue();
        }

        return attributeNames;
    }

    protected abstract ValueEval evaluate(AreaEval range, String[] attributes, int srcRowIndex, int srcColumnIndex) throws EvaluationException;
}
