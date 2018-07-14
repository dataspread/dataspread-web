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

import org.zkoss.poi.ss.formula.TwoDEval;
import org.zkoss.poi.ss.formula.eval.BlankEval;
import org.zkoss.poi.ss.formula.eval.ErrorEval;
import org.zkoss.poi.ss.formula.eval.NumberEval;
import org.zkoss.poi.ss.formula.eval.RefEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.poi.ss.formula.functions.CountUtils.I_MatchPredicate;
import org.zkoss.poi.ss.formula.functions.CountUtils.I_MatchAreaPredicate;

/**
 * Counts the number of cells that contain data within the list of arguments.
 *
 * Excel Syntax
 * COUNTA(value1,value2,...)
 * Value1, value2, ...   are 1 to 30 arguments representing the values or ranges to be counted.
 *
 * @author Josh Micich
 */
public final class Counta implements Function {
    private final I_MatchPredicate _predicate;

    public Counta(){
        _predicate = defaultPredicate;
    }

    private Counta(I_MatchPredicate criteriaPredicate){
        _predicate = criteriaPredicate;
    }

	public ValueEval evaluate(ValueEval[] args, int srcCellRow, int srcCellCol) {
		int nArgs = args.length;
		if (nArgs < 1) {
			// too few arguments
			return ErrorEval.VALUE_INVALID;
		}

		if (nArgs > 30) {
			// too many arguments
			return ErrorEval.VALUE_INVALID;
		}

		int temp = 0;

		for(int i=0; i<nArgs; i++) {
			temp += CountUtils.countArg(args[i], _predicate);

		}
		return new NumberEval(temp);
	}

	private static final I_MatchPredicate defaultPredicate = new I_MatchPredicate() {

		public boolean matches(ValueEval valueEval) {
			// Note - observed behavior of Excel:
			// Error values like #VALUE!, #REF!, #DIV/0!, #NAME? etc don't cause this COUNTA to return an error
			// in fact, they seem to get counted

			if(valueEval == BlankEval.instance) {
				return false;
			}
			// Note - everything but BlankEval counts
			return true;
		}
	};
    private static final I_MatchPredicate subtotalPredicate = new I_MatchAreaPredicate() {
        public boolean matches(ValueEval valueEval) {
            return defaultPredicate.matches(valueEval);
        }

        /**
         * don't count cells that are subtotals
         */
        public boolean matches(TwoDEval areEval, int rowIndex, int columnIndex) {
            return !areEval.isSubTotal(rowIndex, columnIndex);
        }
    };

    public static Counta subtotalInstance() {
        return new Counta(subtotalPredicate);
    }
    
    //ZSS-962
    public static Counta subtotalIgnoreHiddenInstance() {
    	return new Counta(subtotalIgnoreHiddenPredicate);
    }

    //ZSS-962
    private static final I_MatchPredicate subtotalIgnoreHiddenPredicate = new I_MatchAreaPredicate() {
        public boolean matches(ValueEval valueEval) {
        	return (!(valueEval instanceof RefEval) || !((RefEval)valueEval).isHidden())
            	&& defaultPredicate.matches(valueEval);
        }

        /**
         * don't count cells that are subtotals
         */
        public boolean matches(TwoDEval areEval, int rowIndex, int columnIndex) {
            return !areEval.isHidden(rowIndex, columnIndex) 
            		&& !areEval.isSubTotal(rowIndex, columnIndex);
        }
    };
}
