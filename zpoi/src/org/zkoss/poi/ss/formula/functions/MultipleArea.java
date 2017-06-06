package org.zkoss.poi.ss.formula.functions;

import org.zkoss.poi.ss.formula.eval.*;

public class MultipleArea implements Function {
    private static AreaEval convertRangeArg(ValueEval eval) throws EvaluationException {
        if (eval instanceof AreaEval) {
            return (AreaEval) eval;
        }
        if (eval instanceof RefEval) {
            return ((RefEval) eval).offset(0, 0, 0, 0);
        }
        throw new EvaluationException(ErrorEval.VALUE_INVALID);
    }

    @Override
    public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        if (args.length < 2) {
            return ErrorEval.VALUE_INVALID;
        }
        try {
            AreaEval area1 = convertRangeArg(args[0]);
            AreaEval area2 = convertRangeArg(args[1]);
            return new MultipleEval(area1.getWidth(), area1.getHeight(), area2);
        } catch (EvaluationException e) {
            e.getErrorEval();
        }
        return ErrorEval.VALUE_INVALID;
    }
}
