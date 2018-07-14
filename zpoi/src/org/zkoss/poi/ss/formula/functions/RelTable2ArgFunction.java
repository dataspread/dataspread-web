package org.zkoss.poi.ss.formula.functions;

import org.zkoss.poi.ss.formula.eval.*;

/**
 * Abstract class to be inherited by union, intersection, set difference, 
 * and cartesian product classes within RelationalOperatorFunction.
 * The only inputs should be two tables or regions of cells.
 * Created by Danny on 9/22/2016.
 */
public abstract class RelTable2ArgFunction extends Fixed2ArgFunction {

	@Override
	public final ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
		try {
			RelTableEval table0 = RelTableUtils.getRelTableArg(arg0);
			RelTableEval table1 = RelTableUtils.getRelTableArg(arg1);

			ValueEval result = evaluate(table0, table1, srcRowIndex, srcColumnIndex);
			return result;
		}
		catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}

	protected abstract ValueEval evaluate(RelTableEval range1, RelTableEval range2, int srcRowIndex, int srcColumnIndex);

}
