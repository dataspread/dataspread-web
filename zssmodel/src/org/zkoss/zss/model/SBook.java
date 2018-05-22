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

import org.zkoss.zss.model.util.CellStyleMatcher;
import org.zkoss.zss.model.util.FontMatcher;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    String getBookName();

	/**
	 * Set the book name, if schema created, update table.
	 * @return return error if name already exists;
	 */
    boolean setBookName(String bookName);


	/**
	 * Check for schema and create if not there
	 * @return book name;
	 */

    void checkDBSchema();

	/**
	 * Get the book series, it contains a group of book that might refer to other by book name
	 * @return book series
	 */
    SBookSeries getBookSeries();
	/**
	 * Get sheet at the index
	 * @param idx the sheet index
	 * @return the sheet at the index
	 */
    SSheet getSheet(int idx);
	
	/**
	 * Get the index of sheet
	 * @param sheet the sheet
	 * @return the index
	 */
    int getSheetIndex(SSheet sheet);
	
	/**
	 * Get the index of sheet
	 * @param sheetName
	 * @return the index
	 * @since 3.6.0
	 */
    int getSheetIndex(String sheetName);
	
	/**
	 * Get the number of sheet
	 * @return the number of sheet
	 */
    int getNumOfSheet();
	
	/**
	 * Get the sheet by name
	 * @param name the name of sheet
	 * @return the sheet, or null if not found
	 */
    SSheet getSheetByName(String name);
	
	/**
	 * Get the sheet by id
	 * @param id the id of sheet
	 * @return the sheet, or null if not found
	 */
    SSheet getSheetById(String id);
	
	/**
	 * Create a sheet
	 * @param name the name of sheet
	 * @return the sheet
	 */
    SSheet createSheet(String name);
	
	/**
	 * Get all sheets
	 * @return an unmodifiable sheet list
	 */
    List<SSheet> getSheets();
	
	/**
	 * Create a sheet and copy the contain form the sheet sheet
	 * @param name the name of sheet
	 * @param src the source sheet to copy
	 * @return the sheet
	 */
    SSheet createSheet(String name, SSheet src);
	
	/**
	 * Set the sheet to a new name
	 * @param sheet the sheet
	 * @param newname the new name
	 */
    void setSheetName(SSheet sheet, String newname);
	
	/**
	 * Delete the sheet
	 * @param sheet the sheet
	 */
    void deleteSheet(SSheet sheet);
	
	/**
	 * Move the sheet to new position
	 * @param sheet the sheet
	 * @param index the new position
	 */
    void moveSheetTo(SSheet sheet, int index);
	
	/**
	 * Get the default style of this book
	 * @return
	 */
    SCellStyle getDefaultCellStyle();
	
	/**
	 * Set the default style of this book
	 * @since 3.6.0
	 */
    void setDefaultCellStyle(SCellStyle cellStyle);
	
	/**
	 *Create a cell style
	 * @param inStyleTable if true, the new created style will be stored inside this book, 
	 * then you can use {@link #searchCellStyle(CellStyleMatcher)} to search and reuse this style.
	 * @return 
	 */
    SCellStyle createCellStyle(boolean inStyleTable);
	
	/**
	 * Create a cell style and copy the style from the src style.
	 * @param src the source style to copy from.
	 * @param inStyleTable if true, the new created style will be stored inside this book, 
	 * then you can use {@link #searchCellStyle(CellStyleMatcher)} to search and reuse this style.
	 * @return 
	 */
    SCellStyle createCellStyle(SCellStyle src, boolean inStyleTable);
	
	/**
	 * Search the style table and return the first matched style. 
	 * @param matcher the style matcher
	 * @return the matched style.
	 */
    SCellStyle searchCellStyle(CellStyleMatcher matcher);
	
	
	SFont getDefaultFont();

	SFont createFont(boolean inFontTable);
	
	SFont createFont(SFont src, boolean inFontTable);
	
	SFont searchFont(FontMatcher matcher);
	
	SColor createColor(byte r, byte g, byte b);
	SColor createColor(String htmlColor);
	
	/**
	 * Get the max row size of this book
	 */
    int getMaxRowSize();
	
	/**
	 * Get the max column size of this book
	 */
    int getMaxColumnSize();
	
	/**
	 * Get the max row index of this book, it is {@link #getMaxRowSize()}-1
	 */
    int getMaxRowIndex();
	
	/**
	 * Get the max column index of this book, it is {@link #getMaxColumnIndex()}-1
	 */
    int getMaxColumnIndex();
	
	/**
	 * add event listener to this book
	 * @param listener the listener
	 */
    void addEventListener(ModelEventListener listener);
	
	/**
	 * remove event listener from this book
	 * @param listener the listener
	 */
    void removeEventListener(ModelEventListener listener);
	
	/**
	 * Get the runtime custom attribute that stored in this book
	 * @param name the attribute name
	 * @return the value, or null if not found
	 */
    Object getAttribute(String name);
	
	/**
	 * Set the runtime custom attribute to stored in this book, the attribute is only use for developer to stored runtime data in the book,
	 * values will not stored to excel when exporting.
	 * @param name name the attribute name
	 * @param value the attribute value
	 */
    Object setAttribute(String name, Object value);
	
	/**
	 * Get the unmodifiable runtime attributes map
	 * @return
	 */
    Map<String,Object> getAttributes();
	
	/**
	 * Create a defined name on specified sheet 
	 * @return created defined name 
	 */
    SName createName(String name, String applyToSheetName);
	SName createName(String name);
	
	void setNameName(SName name, String newname, String applyToSheetName);
	void setNameName(SName name, String newname);
	
	/**
	 * Delete a defined name 
	 */
    void deleteName(SName name);
	
	int getNumOfName();
	
	SName getName(int idx);
	
	SName getNameByName(String namename, String sheetName);
	SName getNameByName(String namename);
	
	List<SName> getNames();

	void setShareScope(String scope);

	String getShareScope();

	/**
	 * Optimize CellStyle, usually called when export book or after many style operation 
	 * @return
	 */
    void optimizeCellStyle();
	
	/**
	 * Add {@link SPictureData} into this book
	 * @param format picture format
	 * @param data picture raw data
	 * @return the created SPictureData
	 * @since 3.6.0
	 */
    SPictureData addPictureData(SPicture.Format format, byte[] data);
	
	/**
	 * Get {@link SPictureData} of the specified index from this book; null if not exist
	 * @param index
	 * @return SPictureData of the specified index from this book; null if not exist
	 * @since 3.6.0
	 */
    SPictureData getPictureData(int index);
	
	/**
	 * Returns all {@link SPictureData} fo this book.
	 * @since 3.6.0
	 */
    Collection<SPictureData> getPicturesDatas();

	/**
	 * Get the nth default style of this book
	 * @return
	 * @since 3.7.0
	 */
    SCellStyle getDefaultCellStyle(int index);
	
	/**
	 * Add one more default cell style and return its index.
	 * @param cellStyle
	 * @since 3.7.0
	 */
    int addDefaultCellStyle(SCellStyle cellStyle);
		
	/**
	 * Get the named style of this book
	 * @param name
	 * @return
	 * @since 3.7.0
	 */
    SNamedStyle getNamedStyle(String name);
	
	/**
	 * Add one more named cell style. 
	 * @param namedStyle
	 * @since 3.7.0
	 */
    void addNamedCellstyle(SNamedStyle namedStyle);

	/**
	 * Get all default cell styles.
	 * @return
	 * @since 3.7.0
	 */
    Collection<SCellStyle> getDefaultCellStyles();
	
	/**
	 * Get all name styles.
	 * @return
	 * @since 3.7.0
	 */
    Collection<SNamedStyle> getNamedStyles();
	
	/**
	 * Get if book is changed
	 * @return
	 * @since 3.8.0
	 */
    boolean isDirty();
	
	/**
	 * Set dirty flag
	 * @see #isDirty()
	 * @since 3.8.0
	 * @param dirty
	 */
    void setDirty(boolean dirty);
	
	/**
	 * Returns existing or create SCellStyle for hyperlink.
	 * @return
	 * @since 3.8.0
	 */
    SCellStyle getOrCreateDefaultHyperlinkStyle();

	String getId();
	boolean setNameAndLoad(String bookName, String bookId);

	boolean hasSchema();
}
