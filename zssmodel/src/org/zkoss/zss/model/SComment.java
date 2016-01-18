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
package org.zkoss.zss.model;
/**
 * A comment is some texts written by a user which is separated from text of a cell.
 * @author dennis
 * @since 3.5.0
 */
public interface SComment {

	public boolean isVisible();
	public void setVisible(boolean visible);
	
	public boolean isRichText(); 
	
	public String getText();
	public void setText(String text);
	
	public void setRichText(SRichText text);
	/** Set a empty rich text value and return the instance which to be edited **/
	public SRichText setupRichText();
	public SRichText getRichText();
	
	public String getAuthor();
	public void setAuthor(String author);
}
