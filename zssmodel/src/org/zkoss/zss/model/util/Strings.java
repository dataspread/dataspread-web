package org.zkoss.zss.model.util;

/**
 * The class contains methods for checking a string.
 * @author dennis
 * @since 3.5.0
 */
public class Strings {

	
	public static boolean isEmpty(String str){
		return str==null||str.length()==0;
	}
	public static boolean isBlank(String str){
		return str==null || str.trim().length()==0;
	}
}
