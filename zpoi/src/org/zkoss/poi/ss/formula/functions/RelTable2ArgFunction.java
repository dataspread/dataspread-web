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
			RelTableEval table0 = getRelTableArg(arg0);
			RelTableEval table1 = getRelTableArg(arg1);

			ValueEval result = evaluate(table0, table1, srcRowIndex, srcColumnIndex);
			return result;
		}
		catch (EvaluationException e) {
			return e.getErrorEval();
		}
	}

	private static RelTableEval getRelTableArg(ValueEval eval) throws EvaluationException {
		if (eval instanceof RelTableEval) {
			return (RelTableEval) eval;
		} else if (eval instanceof AreaEval) {
			return convertAreaToTable((AreaEval) eval);
		} else if (eval instanceof RefEval) {
			return convertAreaToTable(((RefEval) eval).offset(0, 0, 0, 0));
		}
		throw EvaluationException.invalidValue();
	}

	/**
	 * Parts of function taken from Fixed1ArgFunction.java
	 * @param area
	 * @return a RelTableEval.
	 *     If eval is one cell containing a RelTableEval value, that is the return value.
	 *     Otherwise it creates and returns a RelTableEval from the region.
	 */
	private static RelTableEval convertAreaToTable(AreaEval area) {

		final int width = area.getWidth();
		final int height = area.getHeight();

		if (width == 1 && height == 1 && area.getRelativeValue(0, 0) instanceof RelTableEval) {
			return (RelTableEval) area.getRelativeValue(0, 0);
		} else {
			ValueEval[][] results = new ValueEval[height][];
			for (int r = 0; r < height; ++r) {
				results[r] = new ValueEval[width];
				for (int c = 0; c < width; ++c) {
					final ValueEval ve = area.getRelativeValue(r, c);
					results[r][c] = ve;
				}
			}
			return new RelTableEval(results, height-1, width);
		}
	}

	protected abstract ValueEval evaluate(RelTableEval range1, RelTableEval range2, int srcRowIndex, int srcColumnIndex);

}
