/* WidgetLoader.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Apr 23, 2008 11:46:52 AM     2008, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.sys;

//import org.zkoss.zss.model.Sheet;
//import org.zkoss.poi.ss.usermodel.Picture;
//import org.zkoss.poi.ss.usermodel.ZssChartX;
//import org.zkoss.zss.model.sys.XSheet;
import java.io.Serializable;

import org.zkoss.zss.model.SChart;
import org.zkoss.zss.model.SPicture;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.ui.Spreadsheet;

/**
 * 
 * A controller interface to add/remove widget into/from spreadsheet 
 * 
 * @author Dennis.Chen
 *
 */
public interface WidgetLoader extends Serializable {

	/**
	 * Initial a widget loader with a spreadsheet
	 * @param spreadsheet
	 */
	public void init(Spreadsheet spreadsheet);
	
	/**
	 * indicate the spreadsheet is invalidated.
	 */
	public void invalidate();
	
	
	/**
	 * indicate the selected sheet of a spreadsheet is changed.   
	 * @param sheet
	 */
	public void onSheetSelected(SSheet sheet);
	
	/**
	 * indicate the sheet is dis-selected
	 * @param sheet
	 */
	public void onSheetClean(SSheet sheet);
	
	/**
	 * indicate the sheet's freeze panel is changed.
	 * @param sheet
	 */
	public void onSheetFreeze(SSheet sheet);
	
	/**
	 * call when spreadsheet try to load a block of cell to client side. 
	 * handler should take care this method and load corresponding widgets, which in the block , to client side.
	 * this method will be invoked by spreadsheet, you should not call this method directly.
	 */
	//public void onLoadOnDeman(String sheetid,int left,int top,int right,int bottom);

	public void addChartWidget(SSheet sheet, SChart chart);
	public void deleteChartWidget(SSheet sheet, String chartId); // ZSS-358: keep chart ID for notifying; must assume that chart data was gone.
	public void updateChartWidget(SSheet sheet, SChart chart);
	
	public void addPictureWidget(SSheet sheet, SPicture picture);
	public void deletePictureWidget(SSheet sheet, String pictureId); // ZSS-397: picture data is gone after deleting
	public void updatePictureWidget(SSheet sheet, SPicture picture);

	//ZSS-455 Chart/Image doesn't move location after change column/row width/height
	//ZSS-306 a chart doesn't shrink its size when deleting rows or columns it overlaps
	public void onColumnChange(SSheet sheet, int left, int right);
	public void onRowChange(SSheet sheet, int top, int bottom);
	
	
}
