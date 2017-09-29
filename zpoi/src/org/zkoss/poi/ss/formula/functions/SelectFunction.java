package org.zkoss.poi.ss.formula.functions;

import org.zkoss.poi.ss.formula.WorkbookEvaluator;
import org.zkoss.poi.ss.formula.eval.*;

/**
 * Abstract class to be inherited by select function within RelationalOperatorFunction
 * The inputs should be a region of cells and conditions (if any).
 * The first input arg[0] should be the region, and the rest of args are conditions
 * Created by Danny on 9/22/2016.
 */
public abstract class SelectFunction implements Function {

	public final ValueEval evaluate(ValueEval[] args, int srcCellRow, int srcCellCol) {
		try {
			if (args.length < 1 || args.length > 2) {
				return ErrorEval.VALUE_INVALID;
			}

			RelTableEval table = RelTableUtils.getRelTableArg(args[0]);

			if (args.length == 1) {
				return table;
			} else {
				ValueEval helperArg = args[1];
				if (helperArg instanceof SelectHelperEval) {
					SelectHelperEval helper = (SelectHelperEval) helperArg;
					return evaluate(table, helper, srcCellRow, srcCellCol);
				} else {
					throw EvaluationException.invalidValue();
				}

			}
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}

	protected abstract ValueEval evaluate(RelTableEval table, SelectHelperEval helper, int srcRowIndex, int srcColumnIndex);

}
