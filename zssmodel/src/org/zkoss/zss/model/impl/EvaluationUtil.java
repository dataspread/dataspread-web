/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/01 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.impl;

import java.util.Collection;
import java.util.List;

import org.zkoss.zss.model.ErrorValue;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public class EvaluationUtil {
	
	static public int sizeOf(Object obj){
		if(obj==null){
			return 0;
		}else if(obj instanceof Collection){
			return ((Collection)obj).size();
		}else if(obj.getClass().isArray()){
			return ((Object[])obj).length;
		}else if(obj instanceof ErrorValue){
			return 0;
		}else{
			return 1;
		}
	}
	
	static public Object valueOf(Object obj,int index){
		if(obj instanceof List){//faster before collection
			return ((List)obj).get(index);
		}else if(obj instanceof Collection){
			return ((Collection)obj).toArray()[index];
		}else if(obj.getClass().isArray()){
			return ((Object[])obj)[index];
		}else if(obj instanceof ErrorValue){
			return ((ErrorValue)obj).getErrorString();
		}else{
			return obj;
		}
	}
}
