/* STableColumn.java

	Purpose:
		
	Description:
		
	History:
		Dec 9, 2014 2:25:58 PM, Created by henrichen

	Copyright (C) 2014 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Table column
 * @author henri
 * @since 3.8.0
 */
public interface STableColumn {
	String getName();
	void setName(String name);
	String getTotalsRowLabel();
	void setTotalsRowLabel(String label);
	STotalsRowFunction getTotalsRowFunction();
	void setTotalsRowFunction(STotalsRowFunction func);
	String getTotalsRowFormula();
	void setTotalsRowFormula(String formula);
	
	//the order is important which consist with Excel's model order
	public static enum STotalsRowFunction { 
		none("0"),
		sum("109"),
		min("105"),
		max("104"),
		average("101"),
		count("103"),
		countNums("102"),
		stdDev("107"),
		var("110"),
		custom("0");
		
		private final String _code;
		private STotalsRowFunction(String code) {
			_code = code;
		}
		
		public String getCode() {
			return _code;
		}
		
		//ZSS-989
		private static final Map<String, STotalsRowFunction> _map = 
				new HashMap<String, STotalsRowFunction>();
		static {
			_map.put(sum._code, sum);
			_map.put(min._code, min);
			_map.put(max._code, max);
			_map.put(average._code, average);
			_map.put(count._code, count);
			_map.put(countNums._code, countNums);
			_map.put(stdDev._code, stdDev);
			_map.put(var._code, var);
		}
		
		public static STotalsRowFunction valueOfCode(String code) {
			final STotalsRowFunction func = _map.get(code); 
			return func == null ? STotalsRowFunction.custom : func;  
		}
	}
	
}
