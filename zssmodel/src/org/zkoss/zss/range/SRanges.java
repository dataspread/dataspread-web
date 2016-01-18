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
package org.zkoss.zss.range;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.InvalidModelOpException;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SName;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SheetRegion;
import org.zkoss.zss.range.impl.RangeImpl;

/**
 * A collection of factory methods to create a {@link SRange} object.
 * @author dennis
 * @since 3.5.0
 */
public class SRanges {
	/** 
	 * Returns the associated {@link SRange} of the whole specified {@link SSheet}. 
	 *  
	 * @param sheet the {@link SSheet} the Range will refer to.
	 * @return the associated {@link SRange} of the whole specified {@link SSheet}. 
	 */
	public static SRange range(SSheet sheet){
		return new RangeImpl(sheet);
	}
	
	/** Returns the associated {@link SRange} of the specified {@link SSheet} and area reference string (e.g. "A1:D4" or "NSheet2!A1:D4")
	 * note that if reference string contains sheet name, the returned range will refer to the named sheet. 
	 *  
	 * @param sheet the {@link SSheet} the Range will refer to.
	 * @param reference the area the Range will refer to (e.g. "A1:D4").
	 * @return the associated {@link SRange} of the specified {@link SSheet} and area reference string (e.g. "A1:D4"). 
	 */
	public static SRange range(SSheet sheet, String areaReference){
		SheetRegion region = new SheetRegion(sheet, areaReference);
		
		return new RangeImpl(sheet,region.getRow(),region.getColumn(),region.getLastRow(),region.getLastColumn());
	}
	
	/** Returns the associated {@link SRange} of the specified {@link SSheet} and cell-region 
	 *  
	 * @param sheet the {@link SSheet} the Range will refer to.
	 * @param the cellRegion
	 * @return the associated {@link SRange} of the specified {@link SSheet} and the cell-region 
	 */
	public static SRange range(SSheet sheet, CellRegion region){
		return new RangeImpl(sheet,region);
	}	
	
	/** Returns the associated {@link SRange} of the specified name of a NamedRange (e.g. "MyRange");
	 * 
	 * @param sheet the {@link SSheet} the Range will refer to.
	 * @param name the name of NamedRange  (e.g. "MyRange"); .
	 * @return the associated {@link SRange} of the specified name 
	 */
	public static SRange rangeByName(SSheet sheet, String name){
		SBook book = sheet.getBook();
		SName n = book.getNameByName(name);
		if(n==null){
			throw new InvalidModelOpException("can't find name "+name);
		}
		sheet = book.getSheetByName(n.getRefersToSheetName());
		CellRegion region = n.getRefersToCellRegion();
		if(sheet==null || region==null){
			throw new InvalidModelOpException("bad name "+name+ " : "+n.getRefersToFormula());
		}
		
		return new RangeImpl(sheet,region.row,region.column,region.lastRow,region.lastColumn);
	}	
	
	/** Returns the associated {@link XRange} of the specified {@link XNSheet} and area. 
	 *  
	 * @param sheet the {@link SSheet} the Range will refer to.
	 * @param tRow top row index of the area the Range will refer to.
	 * @param lCol left column index of the area the Range will refer to.
	 * @param bRow bottom row index of the area the Range will refer to.
	 * @param rCol right column index fo the area the Range will refer to.
	 * @return the associated {@link SRange} of the specified {@link SSheet} and area.
	 */
	public static SRange range(SSheet sheet, int tRow, int lCol, int bRow, int rCol){
		return new RangeImpl(sheet,tRow,lCol,bRow,rCol);
	}
	
	/** Returns the associated {@link SRange} of the specified {@link SSheet} and cell row and column. 
	 *  
	 * @param sheet the {@link SSheet} the Range will refer to.
	 * @param row row index of the cell the Range will refer to.
	 * @param col column index of the cell the Range will refer to.
	 * @return the associated {@link SRange} of the specified {@link SSheet} and cell . 
	 */
	public static SRange range(SSheet sheet, int row, int col){	
		return new RangeImpl(sheet,row,col);
	}

	/** 
	 * Returns the associated {@link SRange} of the whole specified {@link SSheet}. 
	 *  
	 * @param sheet the {@link SSheet} the Range will refer to.
	 * @return the associated {@link SRange} of the whole specified {@link SSheet}.
	 * @since 3.8.0 
	 */
	public static SRange range(SBook book){
		return new RangeImpl(book);
	}
	
}
