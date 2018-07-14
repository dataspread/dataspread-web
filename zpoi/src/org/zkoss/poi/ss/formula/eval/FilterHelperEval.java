/* ArrayEval.java

	Purpose:
		
	Description:
		
	History:
		Oct 21, 2010 2:19:12 PM, Created by henrichen

Copyright (C) 2010 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.poi.ss.formula.eval;

import org.zkoss.poi.ss.formula.OperationEvaluationContext;
import org.zkoss.poi.ss.formula.TwoDEval;
import org.zkoss.poi.ss.formula.WorkbookEvaluator;
import org.zkoss.poi.ss.formula.functions.RelTableUtils;
import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.zel.impl.lang.EvaluationContext;

import java.util.Arrays;

/**
 * Constant value relational table eval.
 * @author tana
 *
 */
public class FilterHelperEval implements ValueEval {

	public final WorkbookEvaluator _evaluator;
	public final OperationEvaluationContext _ec;
	public final Ptg[] _ptgs;
	public final boolean _ignoreDependency;
	public final boolean _ignoreDereference;

	public FilterHelperEval(WorkbookEvaluator evaluator, OperationEvaluationContext ec, Ptg[] ptgs, boolean ignoreDependency, boolean ignoreDereference) {
		_evaluator = evaluator;
		_ec = ec;
		_ptgs = ptgs;
		_ignoreDependency = ignoreDependency;
		_ignoreDereference = ignoreDereference;
	}

}
