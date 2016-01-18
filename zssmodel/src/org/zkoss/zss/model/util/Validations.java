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

package org.zkoss.zss.model.util;
/**
 * This class contains methods for validating arguments of a method.
 * @author dennis
 * @since 3.5.0
 */
public class Validations {

	public static void argNotNull(Object... obj){
		String message = "argument is null";
		if(obj==null){
			throw new IllegalArgumentException(message);
		}
		for(int i=0;i<obj.length;i++){
			if(obj[i]==null){
				throw new IllegalArgumentException(message +" at "+ i);
			}
		}
	}
	
	public static void argInstance(Object obj,Class clz){
		argInstance("can't cast to ",obj,clz);
	}
	public static void argInstance(String message,Object obj,Class clz){
		if(obj!=null && !clz.isAssignableFrom(obj.getClass())){
			throw new IllegalArgumentException(message+" "+clz);
		}
	}
	
}
