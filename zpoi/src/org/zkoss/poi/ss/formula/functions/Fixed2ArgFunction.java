/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.zkoss.poi.ss.formula.functions;

import org.zkoss.poi.ss.formula.eval.*;

/**
 * Convenience base class for functions that must take exactly two arguments.
 *
 * @author Josh Micich
 * @author Henri Chen (henrichen@zkoss.org)
 */
public abstract class Fixed2ArgFunction implements Function2Arg {
	public final ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {

		if (args.length != 2 && !(args.length == 3 && args[0] instanceof OverrideEval)) {
			return ErrorEval.VALUE_INVALID;
		}

		// if args[0] is OverrideEval, need to do evaluateArray
		if (args.length == 3) {

			final ValueEval arg0 = args[1];
			final ValueEval arg1 = args[2];

			if (arg0 instanceof AreaEval) {
				return evaluateArray(srcRowIndex, srcColumnIndex, arg0, arg1);
			} else if (arg1 instanceof AreaEval) {
				return evaluateArray(srcRowIndex, srcColumnIndex, arg1, arg0);
			}
		}

		//ZSS-852
		final ValueEval arg0 = args[0];
		final ValueEval arg1 = args[1];

		if (this instanceof Operator) {
			if (arg0 instanceof AreaEval) {
				return evaluateArray(srcRowIndex, srcColumnIndex, arg0, arg1);
			} else if (arg1 instanceof AreaEval) {
				return evaluateArray(srcRowIndex, srcColumnIndex, arg1, arg0);
			}
		}

		return evaluate(srcRowIndex, srcColumnIndex, arg0, arg1);
	}

	//ZSS-852
	protected ValueEval evaluateArray(int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
		AreaEval ae0 = (AreaEval) arg0;
		final int w0 = ae0.getWidth();
		final int h0 = ae0.getHeight();
		ValueEval[][] results = new ValueEval[h0][];
		if (!(arg1 instanceof AreaEval)) {
			for (int r = 0; r < h0; ++r) {
				results[r] = new ValueEval[w0];
				for (int c = 0; c < w0; ++c) {
					final ValueEval ve0 = ae0.getRelativeValue(r, c);
					final ValueEval ve = this.evaluate(srcRowIndex, srcColumnIndex, ve0, arg1); //recursive
					if (ve0 instanceof ErrorEval) return ve0;
					if (ve instanceof ErrorEval) return ve;
					results[r][c] = ve;
				}
			}
		} else {
			AreaEval ae1 = (AreaEval) arg1;
			final int w1 = ae1.getWidth();
			final int h1 = ae1.getHeight();
			if (w1 != w0 || h1 != h0) {
				return ErrorEval.VALUE_INVALID;
			}
			for (int r = 0; r < h0; ++r) {
				results[r] = new ValueEval[w0];
				for (int c = 0; c < w0; ++c) {
					final ValueEval ve0 = ae0.getRelativeValue(r, c);
					final ValueEval ve1 = ae1.getRelativeValue(r, c);
					final ValueEval ve = this.evaluate(srcRowIndex, srcColumnIndex, ve0, ve1); //recursive
					if (ve0 instanceof ErrorEval) return ve0;
					if (ve1 instanceof ErrorEval) return ve1;
					if (ve instanceof ErrorEval) return ve;
					results[r][c] = ve;
				}
			}
		}
		return new ArrayEval(results, ae0.getFirstRow(), ae0.getFirstColumn(), ae0.getLastRow(), ae0.getLastColumn(), ae0.getRefEvaluator());//ZSS-962
	}
}
