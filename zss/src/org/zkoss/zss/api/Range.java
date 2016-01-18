/* Range.java

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

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.CellData;
import org.zkoss.zss.api.model.CellStyle;
import org.zkoss.zss.api.model.CellStyle.BorderType;
import org.zkoss.zss.api.model.Chart;
import org.zkoss.zss.api.model.Chart.Grouping;
import org.zkoss.zss.api.model.Chart.LegendPosition;
import org.zkoss.zss.api.model.Chart.Type;
import org.zkoss.zss.api.model.Color;
import org.zkoss.zss.api.model.EditableCellStyle;
import org.zkoss.zss.api.model.EditableFont;
import org.zkoss.zss.api.model.Font;
import org.zkoss.zss.api.model.Font.Boldweight;
import org.zkoss.zss.api.model.Font.TypeOffset;
import org.zkoss.zss.api.model.Font.Underline;
import org.zkoss.zss.api.model.Hyperlink;
import org.zkoss.zss.api.model.Hyperlink.HyperlinkType;
import org.zkoss.zss.api.model.Picture;
import org.zkoss.zss.api.model.Picture.Format;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.api.model.SheetProtection;
import org.zkoss.zss.api.model.Validation;
import org.zkoss.zss.api.model.Validation.AlertStyle;
import org.zkoss.zss.api.model.Validation.OperatorType;
import org.zkoss.zss.api.model.Validation.ValidationType;
import org.zkoss.zss.range.SRange;

/**
 * Range can represent a cell, a row, a column, a selection of cells containing one or 
 * more contiguous blocks of cells, or a 3-D blocks of cells. <br/>
 * You have to use this class's API to do any operation of the {@link Sheet}, then the upload will sync to the UI automatically.<br/>
 * To get the instance of a {@link Range}, please use the {@link Ranges} API.
 * 
 * <br/>
 * Note : the range API doesn't check the sheet protection, if you care it, you have to check it by calling {@link #isProtected()} before you do any operation.
 * 
 * @author dennis
 * @see Ranges
 * @since 3.0.0
 */
public interface Range {
	
	/**
	 * @author dennis
	 * @deprecated since 3.5 It is always synchronized on Book by a read-write lock
	 */
	public enum SyncLevel{
		BOOK,
		NONE//for you just visit and do nothing
	}

	public enum PasteType{
		ALL,
		ALL_EXCEPT_BORDERS,
		COLUMN_WIDTHS,
		COMMENTS,
		FORMATS/*all formats*/,
		FORMULAS/*include values and formulas*/,
		FORMULAS_AND_NUMBER_FORMATS,
		VALIDATAION,
		VALUES,
		VALUES_AND_NUMBER_FORMATS;
	}
	
	public enum PasteOperation{
		ADD,
		SUB,
		MUL,
		DIV,
		NONE;
	}
	
	public enum ApplyBorderType{
		FULL,
		EDGE_BOTTOM,
		EDGE_RIGHT,
		EDGE_TOP,
		EDGE_LEFT,
		OUTLINE,
		INSIDE,
		INSIDE_HORIZONTAL,
		INSIDE_VERTICAL,
		DIAGONAL,
		DIAGONAL_DOWN,
		DIAGONAL_UP
	}
	
	/** Shift direction of insert api**/
	public enum InsertShift{
		DEFAULT,
		RIGHT,
		DOWN
	}
	/** 
	 * Copy origin format/style of insert
	 **/
	public enum InsertCopyOrigin{
		FORMAT_NONE,
		FORMAT_LEFT_ABOVE,
		FORMAT_RIGHT_BELOW,
	}
	/** Shift direction of delete api**/
	public enum DeleteShift{
		DEFAULT,
		LEFT,
		UP
	}
	
	public enum SortDataOption{
		NORMAL_DEFAULT,
		TEXT_AS_NUMBERS
	}
	
	public enum AutoFilterOperation{
		AND,
		BOTTOM10,
		BOTOOM10PERCENT,
		OR,
		TOP10,
		TOP10PERCENT,
		VALUES
	}
	
	public enum AutoFillType{
		COPY,
		DAYS,
		DEFAULT,
		FORMATS,
		MONTHS,
		SERIES,
		VALUES,
		WEEKDAYS,
		YEARS,
		GROWTH_TREND,
		LINER_TREND
	}
	
	public enum SheetVisible {
		VISIBLE,
		HIDDEN,
		VERY_HIDDEN,
	}
	
	//ZSS-939: the sequence must be consistent with org.zkoss.zss.model.impl.CellAttribute
	//@since 3.8.0
	public enum CellAttribute {
		ALL, TEXT, STYLE, SIZE, MERGE, COMMENT;
	}

	/**
	 * Sets the synchronization level of this range
	 * @param syncLevel
	 * @deprecated It is always synchronized on Book by a read-write lock
	 */
	public void setSyncLevel(SyncLevel syncLevel);
	
	public ReadWriteLock getLock();
	
	/**
	 * Gets the book of this range
	 * @return book
	 */
	public Book getBook();
	
	/**
	 * Gets the sheet of this range
	 * @return sheet
	 */
	public Sheet getSheet();
	
	/**
	 * Gets the left column of this range
	 * @return the left column
	 */
	public int getColumn();
	
	/**
	 * Gets the top row of this range
	 * @return the top row
	 */
	public int getRow();
	
	/**
	 * Gets the right/last column of this range
	 * @return
	 */
	public int getLastColumn();
	
	/**
	 * Gets the bottom/last row of this range
	 * @return
	 */
	public int getLastRow();
	
	
	/**
	 * Gets the row count of this range
	 * @return count of row of this range
	 */
	public int getRowCount();
	
	/**
	 * Gets the column count of this range
	 * @return count of column of this range
	 */
	public int getColumnCount();
	
	/**
	 * Gets cell-style-helper, this helper helps you to create new style, font or color 
	 * @return
	 */
	public CellStyleHelper getCellStyleHelper();
	
	/**
	 * Runs runer under synchronization protection
	 * @param run the runner
	 * @see #setSyncLevel(SyncLevel)
	 */
	public void sync(RangeRunner run);
	/**
	 * Visits all cells in this range with synchronization protection, make sure you call this in a limited range, 
	 * don't use it for all row/column selection, it will spend much time to iterate the cell 
	 * @param visitor the cell visitor 
	 * @see #setSyncLevel(SyncLevel)
	 */
	public void visit(final CellVisitor visitor);

	/**
	 * Return a new range that shift it row and column according to the offset, but has same height and width of original range.
	 * @param rowOffset row offset of the new range, zero base
	 * @param colOffset column offset of the new range, zero base
	 * @return the new range
	 */
	public Range toShiftedRange(int rowOffset,int colOffset);
	
	/**
	 * Returns a new range having on cell according to the offset 
	 * @param rowOffset row offset of the cell, zero base
	 * @param colOffset column offset of the cell, zero base
	 * @return the new range of the cell
	 */
	public Range toCellRange(int rowOffset,int colOffset);
	
	/**
	 *  Return a range that represents all columns and between the first-row and last-row of this range.
	 *  It is a useful when you want to manipulate entire row (such as delete row)
	 **/
	public Range toRowRange();
	
	/**
	 *  Return a range that represents all rows and between the first-column and last-column of this range
	 *  It is a useful when you want to manipulate entire column (such as delete column)
	 **/
	public Range toColumnRange();
	
	/**
	 * Check if this range represents a whole column, which mean all rows are included, 
	 */
	public boolean isWholeColumn();
	/**
	 * Check if this range represents a whole row, which mean all column are included, 
	 */
	public boolean isWholeRow();
	/**
	 * Check if this range represents a whole sheet, which mean all column and row are included, 
	 */
	public boolean isWholeSheet();
	
	/*
	 * ==================================================
	 * operation of cell area  relative API
	 * ==================================================
	 */
	
	/**
	 * Clears contents
	 */
	public void clearContents();

	/**
	 * Clears styles
	 */
	public void clearStyles();
	
	/**
	 * Clears contents, styles and unmerge this range.
	 * @Since 3.5.0
	 */
	public void clearAll();

	/**
	 * Pastes to destination
	 * @param dest the destination 
	 * @return a Range contains the final pasted range. paste to a protected sheet will always cause paste return null.
	 */
	public Range paste(Range dest);
	
	/**
	 * Cut and paste to destination
	 */
	public Range paste(Range dstRange, boolean cut);	
	
	/**
	 * Pastes to destination
	 * @param dest the destination
	 * @param type the paste type
	 * @param op the paste operation
	 * @param skipBlanks skip blanks or not
	 * @param transpose transpose the cell or not
	 * @return a Range contains the final pasted range. paste to a protected sheet will always cause paste return null.
	 * @throws IllegalOpArgumentException 
	 */
	public Range pasteSpecial(Range dest,PasteType type,PasteOperation op,boolean skipBlanks,boolean transpose);
	
	/**
	 * apply borders
	 * @param applyType the apply type
	 * @param borderType the border type
	 * @param htmlColor the color (#rgb-hex-code, e.x #FF00FF)
	 */
	public void applyBorders(ApplyBorderType applyType,BorderType borderType,String htmlColor);

	/**
	 * @return true if any merged cell inside (fully contains or overlaps) this range
	 */
	public boolean hasMergedCell();
	
	/**
	 * @return true if entire range is a merged cell.
	 */
	public boolean isMergedCell();
	
	/**
	 * Merges the range
	 * @param across true if merge horizontally only
	 */
	public void merge(boolean across);
	
	/**
	 * Unmerge the range
	 */
	public void unmerge();
	
	/**
	 * Insert new cells to the area of this range.<br/> 
	 * To insert a row, you have to call {@link Range#toRowRange()} first, to insert a column, you have to call {@link Range#toColumnRange()} first.
	 * @param shift the shift direction of original cells
	 * @param copyOrigin copy the format from nearby cells when inserting new cells 
	 */
	public void insert(InsertShift shift,InsertCopyOrigin copyOrigin);
	
	/**
	 * Delete cells of the range. <br/>
	 * To delete a row, you have to call {@link Range#toRowRange()} first, to delete a column, you have to call {@link Range#toColumnRange()} first.
	 * @param shift the shift direction when deleting.
	 */
	public void delete(DeleteShift shift);
	
	/**
	 * Sort range 
	 * @param desc true for descent, false for ascent
	 */
	public void sort(boolean desc);
	
	/**
	 * Sort range
	 * @param desc true for descent, false for ascent
	 * @param header includes header or not
	 * @param matchCase matches character chase of not
	 * @param sortByRows sorts by row or not
	 * @param dataOption data option for sort
	 */
	public void sort(boolean desc,
			boolean header, 
			boolean matchCase, 
			boolean sortByRows, 
			SortDataOption dataOption);
	
	/**
	 * Sort range
	 * @param index1 the sort index 1
	 * @param desc1 true for descent, false for ascent of index 1
	 * @param header includes header or not
	 * @param matchCase matches character chase of not
	 * @param sortByRows  sorts by row or not
	 * @param dataOption1 data option 1 for sort
	 * @param index2 the sort index 2
	 * @param desc2 true for descent, false for ascent of index 2
	 * @param dataOption2 data option 2 for sort
	 * @param index3 the sort index 3
	 * @param desc3 true for descent, false for ascent of index 3
	 * @param dataOption3 data option31 for sort
	 */
	public void sort(Range index1,boolean desc1,SortDataOption dataOption1,
			Range index2,boolean desc2,SortDataOption dataOption2,
			Range index3,boolean desc3,SortDataOption dataOption3,
			boolean header,
			/*int orderCustom, //not implement*/
			boolean matchCase, 
			boolean sortByRows 
			/*int sortMethod, //not implement*/
			);
	
	/**
	 * According to current range, fills data to destination range automatically
	 * @param dest the destination range
	 * @param fillType the fill type
	 */
	public void autoFill(Range dest,AutoFillType fillType);
	
	/**
	 * Fills cells by copying from first/top row data
	 */
	public void fillDown();
	
	/**
	 * Fills cells by copying from last/right column data
	 */
	public void fillLeft();
	
	/**
	 * Fills cells by copying from bottom row data
	 */
	public void fillUp();
	
	/**
	 * Fills cells by copying from first/left column data
	 */
	public void fillRight();
	
	/** Shifts/moves cells with a offset row and column**/
	public void shift(int rowOffset,int colOffset);
	
	/**
	 * Sets the width(in pixel) of column in this range, it effect to whole column. 
	 * @param widthPx width in pixel
	 * @see #toColumnRange()
	 */
	public void setColumnWidth(int widthPx);
	/**
	 * Sets the height(in pixel) of row in this range, it effect to whole row.
	 * @param heightPx height in pixel
	 * @see #toRowRange()
	 */
	public void setRowHeight(int heightPx);
	
	/**
	 * Sets the height(in pixel) of row in this range and specify it's custom size or not.
	 * @param heightPx height in pixel
	 * @param isCustom true if it's set by users manually, false if it's determined by the system automatically
	 * @sicne 3.0.1
	 */
	public void setRowHeight(int heightPx, boolean isCustom);
	/* 
	 * ==================================================
	 * cell relative API
	 * ==================================================
	 */
	/**
	 * Sets cell style, applies it to all cells of this range 
	 * @param nstyle new cell style
	 * @see #getCellStyleHelper()
	 * @see #getCellStyle()
	 */
	public void setCellStyle(CellStyle nstyle);
	
	/**
	 * Sets cell editText, applies it to all cells of this range 
	 * @param editText the eidtText, it could be a string, integer string, date string or a formula (start with '=')
	 * @throws IllegalFormulaException
	 */
	public void setCellEditText(String editText);
	
	/**
	 * Sets cell data value, applies it to all cells
	 * @param value the cell value, could be null, String, Number, Date or Boolean
	 */
	public void setCellValue(Object value);
	
	/**
	 * Sets cell hyperlink, applies it too all cells
	 * @param type the hyperlink type
	 * @param address the address, e.x http://www.zkoss.org
	 * @param label the label to display
	 */
	public void setCellHyperlink(HyperlinkType type,String address,String label);
	
	
	/**
	 * Gets the first cell(top-left) {@link Hyperlink} object of this range.
	 * @return
	 */
	public Hyperlink getCellHyperlink();
	
	/**
	 * Gets the first cell(top-left) style of this range
	 * 
	 * @return cell-style of this cell, if this cell doesn't has cell-style, it will check row/column's cell-style and then sheet's default cell-style 
	 */
	public CellStyle getCellStyle();
	
	/**
	 * Gets the first cell(top-left) data of this range
	 * @return
	 */
	public CellData getCellData();
	
	/**
	 * Gets the first cell(top-left) edit text of this range
	 * @return edit text
	 * @see CellData#getEditText()
	 */
	public String getCellEditText();
	
	/**
	 * Gets the first cell(top-left) formatted text of this range
	 * @return format text
	 * @see CellData#getFormatText()
	 */
	public String getCellFormatText();
	
	/**
	 * Gets the first cell(top-left)  format of this range
	 * @return
	 * @since 3.5.0
	 */
	public String getCellDataFormat();
	
	/**
	 * Gets the first cell(top-left) value of this this range
	 * @return value object
	 * @see CellData#getValue()
	 */
	public Object getCellValue();
	
	/* 
	 * ==================================================
	 * sheet relative API
	 * ==================================================
	 */
	
	/**
	 * @deprecated use {@link #protectSheet(String password,  
	 *		boolean allowSelectingLockedCells, boolean allowSelectingUnlockedCells,  
	 *		boolean allowFormattingCells, boolean allowFormattingColumns, boolean allowFormattingRows, 
	 *		boolean allowInsertColumns, boolean allowInsertRows, boolean allowInsertingHyperlinks,
	 *		boolean allowDeletingColumns, boolean allowDeletingRows, 
	 *		boolean allowSorting, boolean allowFiltering, 
	 *		boolean allowUsingPivotTables, boolean drawingObjects, boolean scenarios)} instead
	 * Enable sheet protection and apply a password, or null to disable protection
	 */
	public void protectSheet(String password);

	/**
	 * Displays sheet grid-lines or not
	 * @param enable true to display
	 */
	public void setDisplaySheetGridlines(boolean enable);
	
	/**
	 * @return true if display sheet grid-lines is enabled
	 */
	public boolean isDisplaySheetGridlines();
	
	/**
	 * Hide or unhide rows or columns.<br/> 
	 * To hide/unhide a row, you have to call {@link Range#toRowRange()} first, to hide/un-hide a column, you have to call {@link Range#toColumnRange()} 
	 * or a whole column range. 
	 * @param hidden hide or not
	 */
	public void setHidden(boolean hidden);
	
	/**
	 * Sets the sheet name
	 * @param name new sheet name, it must be not same as another sheet name in it's owner book.
	 */
	public void setSheetName(String name);
	
	/**
	 * Gets the sheet name
	 * @return sheet name
	 */
	public String getSheetName();
	
	/**
	 * Sets the sheet order
	 * @param pos the position
	 */
	public void setSheetOrder(int pos);
	
	/**
	 * Gets the sheet order
	 * @return
	 */
	public int getSheetOrder();
	
	/**
	 * @return true if the specified range is protected; i.e. sheet is protected
	 * and some cells in the range are locked
	 */
	public boolean isProtected();
	
	/**
	 * @return true if auto filter is enabled.
	 */
	public boolean isAutoFilterEnabled();
	
	/**
	 * To find a range of cells for applying auto filter according to this range.
	 * Usually, these two ranges are different.
	 * This method searches the filtering range through a specific rules. 
	 * @return a range of cells for applying auto filter or null if can't find one from this Range. 
	 */
	// Refer to ZSS-246.
	public Range findAutoFilterRange();
	
	/**
	 * Enable/disable autofilter of the sheet
	 * @param enable true to enable
	 **/
	public void enableAutoFilter(boolean enable);

	/**
	 * Enables autofilter and set extra condition
	 * @param field the filed index (according to current range, 1 base)
	 * @param filterOp auto filter operation
	 * @param criteria1 criteria for autofilter
	 * @param criteria2 criteria for autofilter
	 * @param showButton true/false for show/hide dropdown button, null will keep the original setting.
	 */
	public void enableAutoFilter(int field, AutoFilterOperation filterOp, Object criteria1, Object criteria2, Boolean showButton);
	
	/**
	 * Clears condition of filter, show all the data
	 **/
	public void resetAutoFilter();
	
	/** 
	 * Re-applies the filter, filter by last condition and data again. Call this if the data was change and you want to re-new the filter result.
	 **/
	public void applyAutoFilter();
	
	/**
	 * Adds picture to sheet
	 * @param anchor the anchor for picture
	 * @param image the image binary array
	 * @param format the image format
	 * @return the new added picture
	 */
	public Picture addPicture(SheetAnchor anchor,byte[] image,Format format);
	
	/**
	 * Deletes picture that in sheet
	 * @param picture
	 */
	public void deletePicture(Picture picture);
	
	/**
	 * Moves picture
	 * @param anchor the anchor to re-allocate
	 * @param picture the picture to re-allocate
	 */
	public void movePicture(SheetAnchor anchor,Picture picture);
	
	/**
	 * Adds chart to sheet
	 * @param anchor the destination anchor of the chart
	 * @param type the chart type
	 * @param grouping the chart grouping
	 * @param pos the legend position
	 * @return the new added chart
	 */
	//currently, we only support to modify chart in XSSF
	public Chart addChart(SheetAnchor anchor,Type type, Grouping grouping, LegendPosition pos);
	
	/**
	 * Deletes chart
	 * @param chart the chart to delete
	 */
	//currently, we only support to modify chart in XSSF
	public void deleteChart(Chart chart);
	
	/**
	 * Moves chart to new location
	 * @param anchor the new location to move
	 * @param chart the chart to move
	 */
	//currently, we only support to modify chart in XSSF
	public void moveChart(SheetAnchor anchor,Chart chart);
	
	/**
	 * Creates a new sheet
	 * @param name the sheet name, it must not be same as another sheet name in book of this range
	 * @return the new created sheet
	 */
	public Sheet createSheet(String name);
	
	
	/**
	 * Clone this sheet; create a sheet and copy the contents of this sheet; then add to the end of the book. 
	 * @param name the sheet name, it must not be the same as another sheet name in book of this range
	 * @return the new created sheet
	 * @since 3.6.0
	 */
	public Sheet cloneSheet(String name);
	
	/**
	 * Set the freeze panel
	 * @param rowfreeze the number of row to freeze, 0 means no freeze
	 * @param columnfreeze the number of column to freeze, 0 means no freeze
	 */
	public void setFreezePanel(int rowfreeze,int columnfreeze);

	
	/**
	 * Deletes sheet.
	 * Note: You couldn't delete last sheet of a book.
	 */
	public void deleteSheet();
	
	
	/**
	 * Notify this range has been changed.
	 */
	public void notifyChange();
	
	
	/**
	 * Notify the whole book of specified variables change. Then spreadsheet will re-evaluate those cells that reference to these variables.
	 * @param variables changed variables 
	 */
	public void notifyChange(String[] variables);
	
	
	
	/**
	 * get formatted string of this range
	 * @return
	 */
	public String asString();
	
	/**
	 * a cell style helper to create style relative object for cell
	 * @author dennis
	 */
	public interface CellStyleHelper {

		/**
		 * create a new cell style and clone attribute from src if it is not null
		 * @param src the source to clone, could be null
		 * @return the new cell style
		 */
		public EditableCellStyle createCellStyle(CellStyle src);

		/**
		 * create a new font and clone attribute from src if it is not null
		 * @param src the source to clone, could be null
		 * @return the new font
		 */
		public EditableFont createFont(Font src);
		
		/**
		 * create a color object from a htmlColor expression
		 * @param htmlColor html color expression, ex. #FF00FF
		 * @return a Color object
		 */
		public Color createColorFromHtmlColor(String htmlColor);
		
		/**
		 * find the font with given condition
		 * @param boldweight
		 * @param color
		 * @param fontHeight
		 * @param fontName
		 * @param italic
		 * @param strikeout
		 * @param typeOffset
		 * @param underline
		 * @return null if not found
		 */
		public Font findFont(Boldweight boldweight, Color color,
				int fontHeight, String fontName, boolean italic,
				boolean strikeout, TypeOffset typeOffset, Underline underline);

		/**
		 * Check if this style still available
		 */
		public boolean isAvailable(CellStyle style);
	}

	/**
	 * Notify the component that a chart has change, e.g. call it after chart data changes.
	 * @param chart the chart that contains change
	 * @since 3.5.0
	 */
	public void updateChart(Chart chart);
	
	
	/**
	 * Get internal range implementation
	 * @since 3.5.0
	 */
	public SRange getInternalRange();
	
	/**
	 * Create a {@link Name} that refer to this range.
	 * @param nameName name of the {@link Name} that you can refer in formulas.
	 * @since 3.5.0
	 */
	public void createName(String nameName);
	
	/**
	 * @return true if the sheet is protected
	 * @since 3.5.0
	 */
	public boolean isSheetProtected();
	
	/**
	 * Protect a {@link Sheet} so that it cannot be modified. You can call 
	 * this method on a protected sheet to change its allowed options if the 
	 * same password is supplied; otherwise, it is ignored. A protected sheet
	 * can be unprotected by calling {@link #unprotectSheet(String password)}.
	 * 
	 * change the protection options; make sure provide 
	 * @param password a case-sensitive password for the sheet; null or empty string means protect the sheet without password.
	 * @param allowSelectingLockedCells true to allow select locked cells; default to true.
	 * @param allowSelectingUnlockedCells true to allow select unlocked cells; default to true.
	 * @param allowFormattingCells true to allow user to format any cell on the protected sheet; default false.
	 * @param allowFormattingColumns true to allow user to format any columns on the protected sheet; default false.
	 * @param allowFormattingRows true to allow user to format any rows on the protected sheet; default false.
	 * @param allowInsertColumns true to allow user to insert columns on the protected sheet; default false.
	 * @param allowInsertRows true to allow user to insert rows on the protected sheet; default false.
	 * @param allowInsertingHyperlinks true to allow user to insert hyperlinks on the protected sheet; default false.
	 * @param allowDeletingColumns true to allow user to delete columns on the protected sheet; default false.
	 * @param allowDeletingRows true to allow user to delete rows on the protected sheet; default false.
	 * @param allowSorting true to allow user to sort on the protected sheet; default false.
	 * @param allowFiltering true to allow user to set filters on the protected sheet; default false.
	 * @param allowUsingPivotTables true to allow user to use pivot table reports on the protected sheet; default false.
	 * @param drawingObjects true to protect objects; default to false.
	 * @param scenarios true to protect scenarios; default to true.
	 * 
	 * @see #unprotectSheet(String password)
	 * @see #isProtected()
	 * @since 3.5.0
	 */
	public void protectSheet(String password,  
			boolean allowSelectingLockedCells, boolean allowSelectingUnlockedCells,  
			boolean allowFormattingCells, boolean allowFormattingColumns, boolean allowFormattingRows, 
			boolean allowInsertColumns, boolean allowInsertRows, boolean allowInsertingHyperlinks,
			boolean allowDeletingColumns, boolean allowDeletingRows, 
			boolean allowSorting, boolean allowFiltering, 
			boolean allowUsingPivotTables, boolean drawingObjects, boolean scenarios);
	
	/**
	 * Removes protection from a sheet. This method has no effect if the sheet 
	 * isn't protected.
	 * @param password a case-sensitive password used to unprotect the sheet. If
	 * the sheet isn't protected with a password, this argument is ignored. If you
	 * omit this argument for a sheet that is protected with a password, you'll
	 * be prompted for the password.
	 * @since 3.5.0 
	 */
	public boolean unprotectSheet(String password);
	
	/**
	 * Gets {@link SheetProtection} which tells what are allowed operations for 
	 * a protected sheet of this range.
	 * @return
	 * @since 3.5.0
	 */
	public SheetProtection getSheetProtection();

	/**
	 * validate the user's input.
	 * @param editText
	 * @return the Validation that constraint the input; null if none.
	 */
	public Validation validate(final String editText);

	/**
	 * Add if not exist or modify an existing {@link Validation} to this range.
	 * 
	 * There are two ways to input list items for data validation in argument
	 * "formula1" and "formula2". One is to specify a range via a formula such 
	 * as "=A1:A4" where the values in the cells of the range is the constrained 
	 * value list; another is to input literally the constrained value list
	 * delimited by comma such as "1, 2, 3, 4". Note that when you input a
	 * formula, must lead the formula with an equal sign('='). 
	 *  
	 * @param validationType the type of this validation  
	 * @param ignoreBlank true if blank values are permitted. 
	 * @param operatorType the operator for this validation
	 * @param inCellDropDown true if want to display dropdown list for acceptable values.
	 * @param formula1 the value or expression associated with conditional format or data validation.
	 * @param formula2 the 2nd part of a conditional format or data validation. Useful only when operatorType is BETWEEN or NOT_BETWEEN.
	 * @param showInput true to show the input message.
	 * @param inputTitle title for the data-validation input dialog box.
	 * @param inputMessage message for the data-validation input dialog box.
	 * @param showError true to show the error message.
	 * @param alertStyle validation alert style.
	 * @param errorTitle title of the data validation error dialog.
	 * @param errorMessage data validation error message.
	 * 
	 * @See {@link #getValidation()}
	 * @since 3.5.0
	 */
	public void setValidation(
			ValidationType validationType,
			boolean ignoreBlank,
			OperatorType operatorType,
			boolean inCellDropDown,
			String formula1,
			String formula2,
			
			boolean showInput,
			String inputTitle,
			String inputMessage,
			
			boolean showError,
			AlertStyle alertStyle,
			String errorTitle,
			String errorMessage);
	
	
	/**
	 * Gets read only {@link Validation} associated with the left-top cell 
	 * of this range; return null if no associated validation.
	 * 
	 * @see #setValidation(ValidationType, boolean, OperatorType, boolean, String, String, boolean, String, String, boolean, AlertStyle, String, String)
	 * @since 3.5.0
	 */
	public List<Validation> getValidations();
	
	/**
	 * Delete the {@link Validation} associated in the specified range.
	 * @since 3.5.0 
	 */
	public void deleteValidation();

	//ZSS-742 Support API for input rich text
	/**
	 * Set into the left top cell of this Range the specified text in html format.
	 * @param html
	 * @since 3.6.0
	 */
	public void setCellRichText(String html);
	
	//ZSS-742
	/**
	 * Returns text in html format; null if not a rich text.
	 * @since 3.6.0
	 */
	public String getCellRichText();
	
	/**
	 * Get font if exists or create font if not exists from the book of this 
	 * range and return it.
	 * 
	 * @return font of the specified font metrics
	 * @since 3.6.0
	 */
	public Font getOrCreateFont(Boldweight boldweight, Color color,
			int fontHeight, String fontName, boolean italic, boolean strikeout,
			TypeOffset typeOffset, Underline underline);

	/**
	 * Enforce evaluation(if not cached) and refresh UI of this range and its 
	 * dependent cells if the includeDependents is true. This method is equal
	 * to {@link Range#refresh(boolean includeDependents, boolean false, boolean true)}. 
	 * @since 3.6.0
	 */
	public void refresh(boolean includeDependants);
	
	/**
	 * Turn on(true)/off(false) of auto refresh of this range and return 
	 * previous on/off status; default is true.
	 * 
	 * @param auto whether refresh automatically
	 * @return previous status
	 * @since 3.6.0
	 */
	public boolean setAutoRefresh(boolean auto);

	/**
	 * Update data model and refresh UI of this range and its dependent cells 
	 * if the argument includeDependents is true. 
	 * 
	 * Note that when you set parameter clearCache to true, the cached formula 
	 * result in data model will be cleared first.
	 * 
	 * If you set parameter enforceEval to true, data model associated with 
	 * this range will be evaluated immediately; otherwise will be 
	 * evaluated on demand.
	 *  
	 * @since 3.7.0
	 */
	public void refresh(boolean includeDependents, boolean clearCache, boolean enforceEval);
	
	/**
	 * Setup sheet's visibility; can be VISIBLE, HIDDEN, or VERY_HIDDEN.
	 * <ul>
	 * 	<li>VISIBLE: the sheet is visible</li>
	 *  <li>HIDDEN: the sheet is hidden but can be unhidden using UI</li>
	 *  <li>VERY_HIDDEN: the sheet is hidden but can be unhidden only via this API.</li>
	 * </ul>
	 * @param visible
	 * @see SheetVisible
	 * @since 3.7.0
	 */
	public void setSheetVisible(SheetVisible visible);

	/**
	 * Return the comment rich edit text of the left top cell of this Range.
	 * @return the rich edit text of this Range.
	 * @since 3.7.0
	 */
	public String getCommentRichText();

	/**
	 * Set comment rich text into the left top cell of this range; null to 
	 * delete it. 
	 * @since 3.7.0
	 */
	public void setCommentRichText(String html);

	/**
	 * Set comment visibility into the left top cell of this range; if no
	 * comment at the cell, it simply ignored.
	 * @param visible
	 * @since 3.7.0
	 */
	public void setCommentVisible(boolean visible);
	
	/**
	 * Returns whether the comment is always visible at the left top cell of
	 * this range; if no comment at the cell, it returns false.
	 * @return
	 * @since 3.7.0
	 */
	public boolean isCommentVisible();

	/**
	 * Notify this range has been changed on the specifield attribute.
	 * @since 3.8.0
	 */
	public void notifyChange(CellAttribute cellAttr);

	/**
     * Set(Rename) the name of the Name(or Table) as specified in this 
     * Range(a Book or a Sheet).
	 * @param namename name of the Name(or Table)
	 * @param newname new name for the Name(or Table) 
	 * @since 3.8.0
	 */
	public void setNameName(String namename, String newname);
	
	/**
	 * Save the specified value into this range as a String no matter the value 
	 * is prefixed with '=' or not. 
	 * @param value
	 * @since 3.8.0
	 */
	public void setStringValue(String value);
}
