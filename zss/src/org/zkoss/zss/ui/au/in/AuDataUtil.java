/* AuDataUtil.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/9/25 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.au.in;

import java.util.Map;

/**
 * @author dennis
 * @since 3.0.0
 */
/*package*/ class AuDataUtil {

	
	public static int getInt(Map data,String key){
		Object obj = data.get(key);
		if(obj instanceof Number){
			return ((Number)obj).intValue();
		}else if(obj instanceof String){
			return Integer.parseInt((String)obj);
		}
		throw new ClassCastException("cannot cast "+(obj==null?null:obj.getClass())+" to Number");
	}
	
	public static String getString(Map data,String key){
		Object obj = data.get(key);
		return obj==null?"":obj.toString();
	}
}
