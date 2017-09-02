package org.zkoss.poi.ss.formula.functions;

import org.zkoss.poi.ss.formula.eval.*;

/**
 * Abstract class to be inherited by project and rename functions in RelationalOperatorFunction.
 * Created by Danny on 9/22/2016.
 */
public abstract class RelTableSchemaFunction implements Function {

	public final ValueEval evaluate(ValueEval[] args, int srcCellRow, int srcCellCol) {
		int nArgs = args.length;
		if (nArgs < 1) {
			// too few arguments
			return ErrorEval.VALUE_INVALID;
		}

		if (nArgs > 30) {
			// too many arguments
			return ErrorEval.VALUE_INVALID;
		}

		try {
			RelTableEval range = RelTableUtils.getRelTableArg(args[0]);
			String[] attributes = new String[nArgs-1];
			for (int i = 0; i < nArgs-1; i++) {
				attributes[i] = RelTableUtils.attributeString(args[i+1]);
			}
			return evaluate(range, attributes, srcCellRow, srcCellCol);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}

	protected abstract ValueEval evaluate(RelTableEval range, String[] attributes, int srcRowIndex, int srcColumnIndex);
}
