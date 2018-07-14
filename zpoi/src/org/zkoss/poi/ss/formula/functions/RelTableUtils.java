package org.zkoss.poi.ss.formula.functions;

import org.zkoss.poi.ss.formula.eval.*;

public class RelTableUtils {

	public static RelTableEval getRelTableArg(ValueEval eval) throws EvaluationException {
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
	public static RelTableEval convertAreaToTable(AreaEval area) {

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

	public static String attributeString(ValueEval eval) throws EvaluationException {
		if (eval instanceof StringEval) {
			return ((StringEval) eval).getStringValue();
		} else if (eval instanceof NumberEval) {
			return ((NumberEval) eval).getStringValue();
		} else if (eval instanceof BoolEval) {
			return ((BoolEval) eval).getStringValue();
		}
		throw EvaluationException.invalidValue();
	}

}
