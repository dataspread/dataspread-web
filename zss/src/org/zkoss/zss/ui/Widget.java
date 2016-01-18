/* Widget.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Apr 17, 2008 2:52:50 PM     2008, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui;

import java.io.Serializable;


/**
 * A widget, like a embedded object of spreadsheet, for example a image, a chart or a dropdown 
 * @author Dennis.Chen
 */
public interface Widget extends Serializable {

	/**
	 * is this widget should attach to a cell, 
	 * which mean it is show with load on demand mechanism with a cell,
	 * it load when cell be loaded, remove when cell is removed in client sided
	 * @return true if this widget should attach to the anchor cell
	 */
	//public boolean isAttached();
	
	/**
	 * get cell row index to anchor
	 * @return row anchor
	 */
	public int getRow();

	/**
	 * set cell row index to anchor
	 * @param rowanchcor row anchor
	 */
	public void setRow(int rowanchcor);

	/**
	 * get cell column index to anchor
	 * @return column anchor
	 */
	public int getColumn();

	/**
	 * set cell column index to anchor
	 * @param columnanchor column anchor
	 */
	public void setColumn(int columnanchor);

	/**
	 * get the left margin of the anchored cell 
	 * @return left margin
	 */
	public int getLeft();

	/**
	 * set the left margin of the anchored cell
	 * @param offsetleft left offset
	 */
	public void setLeft(int offsetleft);

	/**
	 * get the top margin of the anchored cell
	 * @return top offset
	 */
	public int getTop();

	/**
	 * set the top margin of the anchored cell
	 * @param offsettop top offset
	 */
	public void setTop(int offsettop);
	
	/**
	 * set the z index of this widget
	 * @return z-index
	 */
	public int getZindex();
	
	
	/**
	 * set the z-index of this widget
	 * @param zindex the z-index
	 */
	public void setZindex(int zindex);
	
	/**
	 * The real UI implementation of this widget.
	 * This method give developer a chance to do more advance control of a widget.
	 */
	//public Component getComponent();
	
	/**
	 * the panel to stay. available value is corner,top,left or default
	 * @return
	 */
	public String getPanel();
	
	/**
	 * the id of this widget
	 * @return
	 */
	public String getId();
	
}
