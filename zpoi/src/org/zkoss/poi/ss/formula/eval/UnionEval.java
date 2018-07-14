/* UnionEval.java

	Purpose:
		
	Description:
		
	History:
		Feb 24, 2015 11:36:10 AM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.poi.ss.formula.eval;

/**
 * Represent many {@link ValueEval}s unioned by Union operator(comma operator).
 * @author henri
 * @since 3.9.7
 */
//ZSS-933
public class UnionEval extends ValuesEval { 
	public UnionEval(ValueEval[] evals) {
		super(evals);
	}
}
