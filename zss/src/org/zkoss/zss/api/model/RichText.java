/*
{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2014/8/5, Created by henri
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/

package org.zkoss.zss.api.model;

import java.util.List;

/**
 * A rich text can have multiple segments which have different fonts for each one in a cell.
 * @author henrichen
 * @since 3.6.0
 */
public interface RichText {
	/**
	 * @since 3.6.0
	 */
	public interface Segment {
		public String getText();	
		public Font getFont();
	}
	
	public String getText();
	public Font getFont();
	
	public List<Segment> getSegments();
	
	public void addSegment(String text, Font font);
	
	public void clearSegments();
}
