/* FilterMouseEvent.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		May 20, 2011 11:45:18 AM, Created by henrichen
}}IS_NOTE

Copyright (C) 2011 Potix Corporation. All Rights Reserved.
*/
package org.zkoss.zss.ui.event;

import org.zkoss.zk.ui.Component;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.model.Sheet;

/**
 * A class from handle event which about mouse event on a filter.
 * @author henrichen
 */
public class CellFilterEvent extends CellMouseEvent {
	private static final long serialVersionUID = 20110520114618L;
	private final int _field;
	private final AreaRef _filterArea;
	@Deprecated
	public CellFilterEvent(String name, Component target, int x,int y, int keys,Sheet sheet, int row ,int col,int clientx,int clienty, int field) {
		super(name, target, x, y, keys, sheet, row, col, clientx, clienty);
		_field = field;
		_filterArea = null;
	}
	/**
	 * @since 3.0.0
	 */
	public CellFilterEvent(String name, Component target, Sheet sheet, int row ,int col, AreaRef filterArea, int field, int x,int y, int keys,int clientx,int clienty) {
		super(name, target, sheet, row, col, x, y, keys, clientx, clienty);
		_field = field;
		_filterArea = filterArea;
	}
	/**
	 * Gets Filter field; see @link{Range#autoFilter()}.
	 * @return sheet the related sheet 
	 */
	public int getField(){
		return _field;
	}
	
	/**
	 * Gets filter area of the auto filter
	 * @return
	 */
	public AreaRef getFilterArea(){
		return _filterArea;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString());
		sb.append("[").append("field:").append(getField())
			.append("]");
		return sb.toString();
	}
}
