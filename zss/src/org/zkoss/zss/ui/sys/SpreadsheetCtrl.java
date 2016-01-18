/* SpreadsheetCtrl.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 18, 2007 12:18:09 PM     2007, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.sys;


//import org.zkoss.zss.model.Sheet;
import org.zkoss.json.JSONObject;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.ui.Widget;
import org.zkoss.zss.ui.impl.HeaderPositionHelper;
import org.zkoss.zss.ui.impl.MergeMatrixHelper;


/**
 * Special controller interface .
 * Only spreadsheet developer need to use this interface. 
 * @author Dennis.Chen
 *
 */
public interface SpreadsheetCtrl {

	final public static String CHILD_PASSING_KEY = "zsschildren";
	
	public static final int DEFAULT_LOAD_COLUMN_SIZE = 40;
	public static final int DEFAULT_LOAD_ROW_SIZE = 50;
	
	public enum CellAttribute {
		ALL(1), TEXT(2), STYLE(3), SIZE(4), MERGE(5), COMMENT(6);
		
		int value;
		CellAttribute(int value) {
			this.value = value;
		}
		
		public String toString() {
			return "" + value;
		}
	}
	
	public enum Header {
		NONE, ROW, COLUMN, BOTH;
		
	}
	
	public HeaderPositionHelper getRowPositionHelper(String sheetId);
	
	public HeaderPositionHelper getColumnPositionHelper(String sheetId);
	
	public MergeMatrixHelper getMergeMatrixHelper(SSheet sheet);
	
	
	public AreaRef getSelectionArea();
	public AreaRef getFocusArea();
	public AreaRef getLoadedArea();
	public AreaRef getVisibleArea();
	
	public WidgetHandler getWidgetHandler();
	
	public JSONObject getRowHeaderAttrs(SSheet sheet, int rowStart, int rowEnd);
	
	public JSONObject getColumnHeaderAttrs(SSheet sheet, int colStart, int colEnd);
	
	public JSONObject getRangeAttrs(SSheet sheet, Header containsHeader, int left, int top, int right, int bottom);
	
	/**
	 * Add widget to the {@link WidgetHandler} of this spreadsheet, 
	 * 
	 * @param widget a widget
	 * @return true if success to add a widget
	 */
	public boolean addWidget(Widget widget);
	
	/**
	 * Remove widget from the {@link WidgetHandler} of this spreadsheet, 
	 * @param widget
	 * @return true if success to remove a widget
	 */
	public boolean removeWidget(Widget widget);
	
	public Boolean getTopHeaderHiddens(int col);
	
	public Boolean getLeftHeaderHiddens(int row);
	
	
	/**
	 * Sets user action manager ctrl
	 * @since 3.0.0
	 */
	public void setUserActionManagerCtrl(UserActionManagerCtrl actionHandler);
	
	/**
	 * @return user action manager ctrl
	 * @since 3.0.0
	 */
	public UserActionManagerCtrl getUserActionManagerCtrl();
	
	/**
	 * @return
	 * @since 3.0.0
	 */
	public FreezeInfoLoader getFreezeInfoLoader();

	/**
	 * @since 3.8.0
	 * @param sheet
	 * @param containsHeader
	 * @param type
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @return
	 */
	public JSONObject getRangeAttrs(SSheet sheet, Header containsHeader, CellAttribute type, int left, int top, int right, int bottom);
}
