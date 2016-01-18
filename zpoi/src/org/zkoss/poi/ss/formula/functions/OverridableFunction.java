/* OverridableFunction.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/9/27 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.poi.ss.formula.functions;

import java.lang.reflect.Method;

import org.zkoss.poi.ss.formula.eval.ErrorEval;
import org.zkoss.poi.ss.formula.eval.NotImplementedException;
import org.zkoss.poi.ss.formula.eval.ValueEval;

/**
 * A Overridable function to provide the chance of override poi's basic functions
 * @author dennis
 *
 */
public class OverridableFunction implements Function{
	private final String _functionName;
	private final Function _original;
	private Function _func; 
	private static Class _funcClass;
	
	private static String _funcClassName = "org.zkoss.zssex.formula.ELEvalFunction";
	private static String _hasFuncName = "hasFunction";
	
	static {
		try {
			_funcClass = Thread.currentThread().getClass().forName(_funcClassName);
		} catch (ClassNotFoundException e) {
			//ignore
		}		
	}
	
	public OverridableFunction(String name,Function original) {
		_functionName = name;
		_original = original;
		if (_funcClass != null) {
			try {
				_func = (Function) _funcClass.getConstructor(String.class).newInstance(name);
			} catch(Exception ex) {
				//ignore
			}
		}
	}

	public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
		
		if(_func!=null && hasFunction(_func)){
			return _func.evaluate(args, srcRowIndex, srcColumnIndex);
		}
		try{
			return _original.evaluate(args, srcRowIndex, srcColumnIndex);
		}catch(NotImplementedException x){
			return ErrorEval.NAME_INVALID;
		}
	}
	

	private boolean hasFunction(Function fn) {
		try{
			Method m = fn.getClass().getMethod(_hasFuncName);
			Object obj = m.invoke(fn, null);
			return ((Boolean)obj).booleanValue();
		}catch(Exception x){}
		return false;
	}

	public String getFunctionName() {
		return _functionName;
	}
}
