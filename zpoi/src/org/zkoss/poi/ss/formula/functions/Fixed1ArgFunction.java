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

import org.zkoss.poi.ss.formula.eval.AreaEval;
import org.zkoss.poi.ss.formula.eval.ArrayEval;
import org.zkoss.poi.ss.formula.eval.ErrorEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;

/**
 * Convenience base class for functions that must take exactly one argument.
 *
 * @author Josh Micich
 * @author Henri Chen (henrichen@zkoss.org)
 */
public abstract class Fixed1ArgFunction implements Function1Arg {
	public final ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
		if (args.length != 1) {
			return ErrorEval.VALUE_INVALID;
		}

		//ZSS-852
		final ValueEval arg0 = args[0];
		if (arg0 instanceof AreaEval && this instanceof Operator) {
			return evaluateArray(srcRowIndex, srcColumnIndex, arg0);
		}

		return evaluate(srcRowIndex, srcColumnIndex, arg0);
	}

	//ZSS-852
	private ValueEval evaluateArray(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
		AreaEval ae0 = (AreaEval) arg0;
		final int w0 = ae0.getWidth();
		final int h0 = ae0.getHeight();
		ValueEval[][] results = new ValueEval[h0][];
		for (int r = 0; r < h0; ++r) {
			results[r] = new ValueEval[w0];
			for (int c = 0; c < w0; ++c) {
				final ValueEval ve0 = ae0.getRelativeValue(r, c);
				final ValueEval ve = this.evaluate(srcRowIndex, srcColumnIndex, ve0); //recursive
				if (ve0 instanceof ErrorEval) return ve0;
				if (ve instanceof ErrorEval) return ve;
				results[r][c] = ve;
			}
		}
		return new ArrayEval(results, ae0.getFirstRow(), ae0.getFirstColumn(), ae0.getLastRow(), ae0.getLastColumn(), ae0.getRefEvaluator()); //ZSS-962
	}
}
