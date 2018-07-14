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

import org.zkoss.poi.ss.formula.eval.RefEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.poi.ss.formula.TwoDEval;

/**
 * Common logic for COUNT, COUNTA and COUNTIF
 *
 * @author Josh Micich
 */
final class CountUtils {

	private CountUtils() {
		// no instances of this class
	}

	/**
	 * Common interface for the matching criteria.
	 */
	public interface I_MatchPredicate {
		boolean matches(ValueEval x);
	}
    public interface I_MatchAreaPredicate extends I_MatchPredicate {
        boolean matches(TwoDEval x, int rowIndex, int columnIndex);
    }

	/**
	 * @return the number of evaluated cells in the range that match the specified criteria
	 */
	public static int countMatchingCellsInArea(TwoDEval areaEval, I_MatchPredicate criteriaPredicate) {
		int result = 0;

		int height = areaEval.getHeight();
		int width = areaEval.getWidth();
		for (int rrIx=0; rrIx<height; rrIx++) {
			for (int rcIx=0; rcIx<width; rcIx++) {
				ValueEval ve = areaEval.getValue(rrIx, rcIx);

                if(criteriaPredicate instanceof I_MatchAreaPredicate){
                    I_MatchAreaPredicate areaPredicate = (I_MatchAreaPredicate)criteriaPredicate;
                    if(!areaPredicate.matches(areaEval, rrIx, rcIx)) continue;
                }

				if(criteriaPredicate.matches(ve)) {
					result++;
				}
			}
		}
		return result;
	}
	/**
	 * @return 1 if the evaluated cell matches the specified criteria
	 */
	public static int countMatchingCell(RefEval refEval, I_MatchPredicate criteriaPredicate) {
		if(criteriaPredicate.matches(refEval.getInnerValueEval())) {
			return 1;
		}
		return 0;
	}
	public static int countArg(ValueEval eval, I_MatchPredicate criteriaPredicate) {
		if (eval == null) {
			throw new IllegalArgumentException("eval must not be null");
		}
		if (eval instanceof TwoDEval) {
			return countMatchingCellsInArea((TwoDEval) eval, criteriaPredicate);
		}
		if (eval instanceof RefEval) {
			return CountUtils.countMatchingCell((RefEval) eval, criteriaPredicate);
		}
		return criteriaPredicate.matches(eval) ? 1 : 0;
	}
}
