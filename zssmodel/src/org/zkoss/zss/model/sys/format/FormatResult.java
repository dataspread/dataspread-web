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
package org.zkoss.zss.model.sys.format;
import java.text.Format;
import org.zkoss.zss.model.SColor;
import org.zkoss.zss.model.SRichText;

/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public interface FormatResult {

	/**
	 * @return true if the result contains rich format, contains font and color
	 */
	boolean isRichText();
	
	/**
	 * @return true if the result is data formatted
	 */
	boolean isDateFormatted();
	
	/**
	 * @return the java format object if the result was done by it, return null if the result is not done by a java format object
	 */
	Format getFormater();
	
	SRichText getRichText();
	
	String getText();
	
	SColor getColor();
	
//	boolean getConditionApplied();
	
//	String getHtml();
	
}
