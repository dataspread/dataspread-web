/* Hyperlink.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/5/1 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.api.model;

/**
 * This interface allows you to get a cell's hyperlink data.
 * @author dennis
 * @since 3.0.0
 */
public interface Hyperlink {
	public enum HyperlinkType{
	    URL,
	    DOCUMENT,
	    EMAIL,
	    FILE
	}
	
	
	public HyperlinkType getType();
	
	public String getAddress();
	
	public String getLabel();
}
