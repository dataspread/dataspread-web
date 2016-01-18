/* Ranges.java

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
package org.zkoss.zss.api;

import org.zkoss.zss.api.impl.RangeImpl;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.api.model.impl.SheetImpl;
import org.zkoss.zss.api.model.impl.BookImpl;
import org.zkoss.zss.range.SRanges;

/**
 * The facade class provides you multiple ways to get a {@link Range}.
 * 
 * @author dennis
 * @see Range 
 * @since 3.0.0
 */
public class Ranges {

	/** 
	 * Returns the associated {@link Range} of the whole specified {@link Sheet}. 
	 *  
	 * @param sheet the {@link Sheet} the Range will refer to.
	 * @return the associated {@link Range} of the whole specified {@link Sheet}. 
	 */
	public static Range range(Sheet sheet){
		return new RangeImpl(SRanges.range(((SheetImpl)sheet).getNative()),sheet);
	}
	
	/** Returns the associated {@link Range} of the specified {@link Sheet} and area reference string (e.g. "A1:D4" or "Sheet2!A1:D4")
	 * note that if reference string contains sheet name, the returned range will refer to the named sheet. 
	 *  
	 * @param sheet the {@link Sheet} the Range will refer to.
	 * @param reference the area the Range will refer to (e.g. "A1:D4").
	 * @return the associated {@link Range} of the specified {@link Sheet} and area reference string (e.g. "A1:D4"). 
	 */
	public static Range range(Sheet sheet, String areaReference){
		return new RangeImpl(SRanges.range(((SheetImpl)sheet).getNative(),areaReference),sheet);
	}
	
	/** Returns the associated {@link Range} of the specified name of a NamedRange (e.g. "MyRange");
	 * 
	 * @param sheet the {@link Sheet} the Range will refer to.
	 * @param name the name of NamedRange  (e.g. "MyRange"); .
	 * @return the associated {@link Range} of the specified name 
	 */
	public static Range rangeByName(Sheet sheet, String name){
		return new RangeImpl(SRanges.rangeByName(((SheetImpl)sheet).getNative(),name),sheet);
	}	
	
	/** Returns the associated {@link XRange} of the specified {@link XSheet} and area. 
	 *  
	 * @param sheet the {@link Sheet} the Range will refer to.
	 * @param tRow top row index of the area the Range will refer to.
	 * @param lCol left column index of the area the Range will refer to.
	 * @param bRow bottom row index of the area the Range will refer to.
	 * @param rCol right column index fo the area the Range will refer to.
	 * @return the associated {@link Range} of the specified {@link Sheet} and area.
	 */
	public static Range range(Sheet sheet, int tRow, int lCol, int bRow, int rCol){
		return new RangeImpl(SRanges.range(((SheetImpl)sheet).getNative(),tRow,lCol,bRow,rCol),sheet);
	}
	
	/** Returns the associated {@link Range} of the specified {@link Sheet} and cell row and column. 
	 *  
	 * @param sheet the {@link Sheet} the Range will refer to.
	 * @param row row index of the cell the Range will refer to.
	 * @param col column index of the cell the Range will refer to.
	 * @return the associated {@link Range} of the specified {@link Sheet} and cell . 
	 */
	public static Range range(Sheet sheet, int row, int col){	
		return new RangeImpl(SRanges.range(((SheetImpl)sheet).getNative(),row,col),sheet);
	}
	
	/** Returns the associated {@link Range} of the specified {@link Sheet} and cell row and column. 
	 *  
	 * @param sheet the {@link Sheet} the Range will refer to.
	 * @param selection the selection of spreadsheet
	 */
	public static Range range(Sheet sheet, AreaRef selection){	
		return range(sheet,selection.getRow(),selection.getColumn(),selection.getLastRow(),selection.getLastColumn());
	}
	
	/**
	 * Gets cell reference expression
	 * @param row 0-based row index 
	 * @param col 0-based column index 
	 * @return the cell reference string (e.g. A1)
	 */
	public static String getCellRefString(int row,int col){
		return getCellRefString(null,row,col);
	}
	
	/**
	 * Gets cell reference expression string
	 * @param sheet sheet
	 * @param row 0-based row index
	 * @param col 0-based column index
	 * @return the cell reference string (e.g. Sheet1!A1)
	 */
	public static String getCellRefString(Sheet sheet,int row,int col){
		org.zkoss.poi.ss.util.CellReference cf = new org.zkoss.poi.ss.util.CellReference(sheet==null?null:sheet.getSheetName(),row, col,false,false);
		return cf.formatAsString();
	}
	
	/**
	 * Gets area reference expression
	 * @param tRow top row
	 * @param lCol left column
	 * @param bRow bottom/last row
	 * @param rCol right/last column
	 * @return the area reference string (e.g. A1:B2)
	 */
	public static String getAreaRefString(int tRow, int lCol, int bRow, int rCol){
		return getAreaRefString(null,tRow, lCol, bRow, rCol);
	}
	
	/**
	 * Gets area reference expression
	 * @param sheet sheet
	 * @param tRow top row
	 * @param lCol left column
	 * @param bRow bottom row
	 * @param rCol right column
	 * @return the area reference string (e.g. Sheet1!A1:B2)
	 */
	public static String getAreaRefString(Sheet sheet, int tRow, int lCol, int bRow, int rCol){
		String sn = sheet==null?null:sheet.getSheetName();
		org.zkoss.poi.ss.util.AreaReference af = new org.zkoss.poi.ss.util.AreaReference(new org.zkoss.poi.ss.util.CellReference(sn,tRow,lCol,false,false), new org.zkoss.poi.ss.util.CellReference(sn,bRow,rCol,false,false));
		return af.formatAsString();
	}
	
	/**
	 * Gets area reference expression
	 * @param sheet sheet
	 * @param area area
	 * @return the area reference string (e.g. Sheet1!A1:B2)
	 */
	public static String getAreaRefString(Sheet sheet, AreaRef area){
		return getAreaRefString(sheet, area.getRow(),area.getColumn(),area.getLastRow(),area.getLastColumn());
	}
	
	/**
	 * Get column reference string
	 * @param column 0-based column index
	 * @return the column reference string (e.g A, AB)
	 */
	public static String getColumnRefString(int column){
		return org.zkoss.poi.ss.util.CellReference.convertNumToColString(column);
	}
	
	/**
	 * Get row reference
	 * @param row 0-based row index
	 * @return the column reference string (e.g 1, 12)
	 */
	public static String getRowRefString(int row){
		int excelRowNum = row + 1;
		return Integer.toString(excelRowNum);
	}
	
	/** 
	 * Returns the associated {@link Range} of the whole specified {@link Book}. 
	 *  
	 * @param book the {@link Book} the Range will refer to.
	 * @return the associated {@link Range} of the whole specified {@link Book}. 
	 */
	public static Range range(Book book){
		return new RangeImpl(SRanges.range(((BookImpl)book).getNative()),book);
	}
}
