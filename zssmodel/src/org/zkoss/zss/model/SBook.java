/* SBook.java

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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.zkoss.zss.model.util.CellStyleMatcher;
import org.zkoss.zss.model.util.FontMatcher;

/**
 * The root of Spreadsheet's data model which contains sheets.
 * @author dennis
 * @since 3.5.0
 */
public interface SBook {

	/**
	 * Get the book name, a book name is unique for book in {@link SBookSeries}
	 * @return book name;
	 */
	public String getBookName();

	/**
	 * Set the book name, if schema created, update table.
	 * @return return error if name already exists;
	 */
	public boolean setBookName(String bookName);


	/**
	 * Check for schema and create if not there
	 * @return book name;
	 */

	public void checkDBSchema();

	/**
	 * Get the book series, it contains a group of book that might refer to other by book name
	 * @return book series
	 */
	public SBookSeries getBookSeries();
	/**
	 * Get sheet at the index
	 * @param idx the sheet index
	 * @return the sheet at the index
	 */
	public SSheet getSheet(int idx);
	
	/**
	 * Get the index of sheet
	 * @param sheet the sheet
	 * @return the index
	 */
	public int getSheetIndex(SSheet sheet);
	
	/**
	 * Get the index of sheet
	 * @param sheetName
	 * @return the index
	 * @since 3.6.0
	 */
	public int getSheetIndex(String sheetName);
	
	/**
	 * Get the number of sheet
	 * @return the number of sheet
	 */
	public int getNumOfSheet();
	
	/**
	 * Get the sheet by name
	 * @param name the name of sheet
	 * @return the sheet, or null if not found
	 */
	public SSheet getSheetByName(String name);
	
	/**
	 * Get the sheet by id
	 * @param id the id of sheet
	 * @return the sheet, or null if not found
	 */
	public SSheet getSheetById(String id);
	
	/**
	 * Create a sheet
	 * @param name the name of sheet
	 * @return the sheet
	 */
	public SSheet createSheet(String name);
	
	/**
	 * Get all sheets
	 * @return an unmodifiable sheet list
	 */
	public List<SSheet> getSheets();
	
	/**
	 * Create a sheet and copy the contain form the sheet sheet
	 * @param name the name of sheet
	 * @param src the source sheet to copy
	 * @return the sheet
	 */
	public SSheet createSheet(String name, SSheet src);
	
	/**
	 * Set the sheet to a new name
	 * @param sheet the sheet
	 * @param newname the new name
	 */
	public void setSheetName(SSheet sheet, String newname);
	
	/**
	 * Delete the sheet
	 * @param sheet the sheet
	 */
	public void deleteSheet(SSheet sheet);
	
	/**
	 * Move the sheet to new position
	 * @param sheet the sheet
	 * @param index the new position
	 */
	public void moveSheetTo(SSheet sheet, int index);
	
	/**
	 * Get the default style of this book
	 * @return
	 */
	public SCellStyle getDefaultCellStyle();
	
	/**
	 * Set the default style of this book
	 * @since 3.6.0
	 */
	public void setDefaultCellStyle(SCellStyle cellStyle);
	
	/**
	 *Create a cell style
	 * @param inStyleTable if true, the new created style will be stored inside this book, 
	 * then you can use {@link #searchCellStyle(CellStyleMatcher)} to search and reuse this style.
	 * @return 
	 */
	public SCellStyle createCellStyle(boolean inStyleTable);
	
	/**
	 * Create a cell style and copy the style from the src style.
	 * @param src the source style to copy from.
	 * @param inStyleTable if true, the new created style will be stored inside this book, 
	 * then you can use {@link #searchCellStyle(CellStyleMatcher)} to search and reuse this style.
	 * @return 
	 */
	public SCellStyle createCellStyle(SCellStyle src,boolean inStyleTable);
	
	/**
	 * Search the style table and return the first matched style. 
	 * @param matcher the style matcher
	 * @return the matched style.
	 */
	public SCellStyle searchCellStyle(CellStyleMatcher matcher);
	
	
	public SFont getDefaultFont();

	public SFont createFont(boolean inFontTable);
	
	public SFont createFont(SFont src,boolean inFontTable);
	
	public SFont searchFont(FontMatcher matcher);
	
	public SColor createColor(byte r, byte g, byte b);
	public SColor createColor(String htmlColor);
	
	/**
	 * Get the max row size of this book
	 */
	public int getMaxRowSize();
	
	/**
	 * Get the max column size of this book
	 */
	public int getMaxColumnSize();
	
	/**
	 * Get the max row index of this book, it is {@link #getMaxRowSize()}-1
	 */
	public int getMaxRowIndex();
	
	/**
	 * Get the max column index of this book, it is {@link #getMaxColumnIndex()}-1
	 */
	public int getMaxColumnIndex();
	
	/**
	 * add event listener to this book
	 * @param listener the listener
	 */
	public void addEventListener(ModelEventListener listener);
	
	/**
	 * remove event listener from this book
	 * @param listener the listener
	 */
	public void removeEventListener(ModelEventListener listener);
	
	/**
	 * Get the runtime custom attribute that stored in this book
	 * @param name the attribute name
	 * @return the value, or null if not found
	 */
	public Object getAttribute(String name);
	
	/**
	 * Set the runtime custom attribute to stored in this book, the attribute is only use for developer to stored runtime data in the book,
	 * values will not stored to excel when exporting.
	 * @param name name the attribute name
	 * @param value the attribute value
	 */
	public Object setAttribute(String name,Object value);
	
	/**
	 * Get the unmodifiable runtime attributes map
	 * @return
	 */
	public Map<String,Object> getAttributes();
	
	/**
	 * Create a defined name on specified sheet 
	 * @return created defined name 
	 */
	public SName createName(String name,String applyToSheetName);
	public SName createName(String name);
	
	public void setNameName(SName name,String newname, String applyToSheetName);
	public void setNameName(SName name,String newname);
	
	/**
	 * Delete a defined name 
	 */
	public void deleteName(SName name);
	
	public int getNumOfName();
	
	public SName getName(int idx);
	
	public SName getNameByName(String namename, String sheetName);
	public SName getNameByName(String namename);
	
	public List<SName> getNames();

	public void setShareScope(String scope);

	public String getShareScope();

	/**
	 * Optimize CellStyle, usually called when export book or after many style operation 
	 * @return
	 */
	public void optimizeCellStyle();
	
	/**
	 * Add {@link SPictureData} into this book
	 * @param format picture format
	 * @param data picture raw data
	 * @return the created SPictureData
	 * @since 3.6.0
	 */
	public SPictureData addPictureData(SPicture.Format format, byte[] data);
	
	/**
	 * Get {@link SPictureData} of the specified index from this book; null if not exist
	 * @param index
	 * @return SPictureData of the specified index from this book; null if not exist
	 * @since 3.6.0
	 */
	public SPictureData getPictureData(int index);
	
	/**
	 * Returns all {@link SPictureData} fo this book.
	 * @since 3.6.0
	 */
	public Collection<SPictureData> getPicturesDatas();

	/**
	 * Get the nth default style of this book
	 * @return
	 * @since 3.7.0
	 */
	public SCellStyle getDefaultCellStyle(int index);
	
	/**
	 * Add one more default cell style and return its index.
	 * @param cellStyle
	 * @since 3.7.0
	 */
	public int addDefaultCellStyle(SCellStyle cellStyle);
		
	/**
	 * Get the named style of this book
	 * @param name
	 * @return
	 * @since 3.7.0
	 */
	public SNamedStyle getNamedStyle(String name);
	
	/**
	 * Add one more named cell style. 
	 * @param namedStyle
	 * @since 3.7.0
	 */
	public void addNamedCellstyle(SNamedStyle namedStyle);

	/**
	 * Get all default cell styles.
	 * @return
	 * @since 3.7.0
	 */
	public Collection<SCellStyle> getDefaultCellStyles();
	
	/**
	 * Get all name styles.
	 * @return
	 * @since 3.7.0
	 */
	public Collection<SNamedStyle> getNamedStyles();
	
	/**
	 * Get if book is changed
	 * @return
	 * @since 3.8.0
	 */
	public boolean isDirty();
	
	/**
	 * Set dirty flag
	 * @see #isDirty()
	 * @since 3.8.0
	 * @param dirty
	 */
	public void setDirty(boolean dirty);
	
	/**
	 * Returns existing or create SCellStyle for hyperlink.
	 * @return
	 * @since 3.8.0
	 */
	public SCellStyle getOrCreateDefaultHyperlinkStyle();

	public String getId();
	public void setIdAndLoad(String id);

	public boolean hasSchema();
}
