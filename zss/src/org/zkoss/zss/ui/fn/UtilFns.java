/* UtilFns.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 17, 2007 5:12:20 PM     2007, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.fn;

import java.util.Iterator;
import java.util.Set;

import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.impl.MergeMatrixHelper;
import org.zkoss.zss.ui.sys.SpreadsheetCtrl;


/**
 * 
 * This class is for Spreadsheet Taglib use only, don't use it as a utility .
 * @author Dennis.Chen
 * @deprecated since 3.0.0
 */
public class UtilFns {

	/**
	 * Gets Column name of a sheet
	 */
	static public String getColumntitle(Spreadsheet ss,int index){
		return ss.getColumntitle(index);
	}
	
	
	/**
	 * Gets Row name of a sheet
	 */
	static public String getRowtitle(Spreadsheet ss,int index){
		return ss.getRowtitle(index);
	}
	
//	/**
//	 * Gets Cell text by given row and column
//	 */
//	static public String getCelltext(XSheet sheet, int row,int column){
//		return XUtils.getCellHtmlText(sheet, row, column);
//	}
//	
//	static public String getCellFormatText(XSheet sheet, int row,int column) {
//		return XUtils.getCellFormatText(sheet, row, column);
//	}
//
//	//Gets Cell edit text by given row and column
//	static public String getEdittext(XSheet sheet, int row,int column){
//		return XUtils.getEditText(sheet, row, column);
//	}
	
	static public Integer getRowBegin(Spreadsheet ss){
		return Integer.valueOf(0);
	}
	static public Integer getRowEnd(Spreadsheet ss){
		int max = ss.getCurrentMaxVisibleRows(); //ZSS-1084
		max = max<=20?max-1:20;
		return Integer.valueOf(max);//Integer.valueOf(ss.getMaxrow()-1);
	}
	static public Integer getColBegin(Spreadsheet ss){
		return Integer.valueOf(0);
	}
	static public Integer getColEnd(Spreadsheet ss){
		int max = ss.getCurrentMaxVisibleColumns(); //ZSS-1084

		max = max<=10?max-1:10;
		
		int row_top = getRowBegin(ss).intValue();
		int row_bottom = getRowEnd(ss).intValue();
		
		SSheet sheet = ss.getSelectedSSheet();
		MergeMatrixHelper mmhelper = ((SpreadsheetCtrl)ss.getExtraCtrl()).getMergeMatrixHelper(sheet);
		Set blocks = mmhelper.getRangesByColumn(max);
		Iterator iter = blocks.iterator();
		while(iter.hasNext()){
			AreaRef rect = (AreaRef)iter.next();
			int top = rect.getRow();
			//int left = rect.getLeft();
			int right = rect.getLastColumn();
			//int bottom = rect.getBottom();
			if(top<row_top || top<row_bottom){
				continue;
			}
			
			if(max<right){
				max = right;
			}
		}
		
		return Integer.valueOf(max);//Integer.valueOf(ss.getMaxcolumn()-1);
	}
	
//	static public String getRowOuterAttrs(Spreadsheet ss,int row){
//		return ((SpreadsheetCtrl)ss.getExtraCtrl()).getRowOuterAttrs(row);
//	}
//	
//	static public String getCellOuterAttrs(Spreadsheet ss,int row,int col){
//		return ((SpreadsheetCtrl)ss.getExtraCtrl()).getCellOuterAttrs(row,col);
//	}
//	
//	static public String getCellInnerAttrs(Spreadsheet ss,int row,int col){
//		return ((SpreadsheetCtrl)ss.getExtraCtrl()).getCellInnerAttrs(row,col);
//	}
//	static public String getTopHeaderOuterAttrs(Spreadsheet ss,int col){
//		return ((SpreadsheetCtrl)ss.getExtraCtrl()).getTopHeaderOuterAttrs(col);
//	}
//	static public String getTopHeaderInnerAttrs(Spreadsheet ss,int col){
//		return ((SpreadsheetCtrl)ss.getExtraCtrl()).getTopHeaderInnerAttrs(col);
//	}
//	
//	static public String getLeftHeaderOuterAttrs(Spreadsheet ss,int row){
//		return ((SpreadsheetCtrl)ss.getExtraCtrl()).getLeftHeaderOuterAttrs(row);
//	}
//	
//	static public String getLeftHeaderInnerAttrs(Spreadsheet ss,int row){
//		return ((SpreadsheetCtrl)ss.getExtraCtrl()).getLeftHeaderInnerAttrs(row);
//	}
	
	static public Boolean getTopHeaderHiddens(Spreadsheet ss, int col) {
		return ((SpreadsheetCtrl)ss.getExtraCtrl()).getTopHeaderHiddens(col);
	}
	
	static public Boolean getLeftHeaderHiddens(Spreadsheet ss,int row){
		return ((SpreadsheetCtrl)ss.getExtraCtrl()).getLeftHeaderHiddens(row);
	}
}
