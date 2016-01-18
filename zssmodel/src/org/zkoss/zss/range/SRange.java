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

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import org.zkoss.zss.model.*;
import org.zkoss.zss.model.SAutoFilter.FilterOp;
import org.zkoss.zss.model.SChart.ChartGrouping;
import org.zkoss.zss.model.SChart.ChartLegendPosition;
import org.zkoss.zss.model.SChart.ChartType;
import org.zkoss.zss.model.SDataValidation.AlertStyle;
import org.zkoss.zss.model.SDataValidation.OperatorType;
import org.zkoss.zss.model.SDataValidation.ValidationType;
import org.zkoss.zss.model.SHyperlink.HyperlinkType;
import org.zkoss.zss.model.SPicture.Format;
import org.zkoss.zss.model.impl.CellAttribute;
/**
 * The main API to manipulate Spreadsheet's book model.
 * It may represent one or more cells, a row, a column, or a selection of a block of cells. 
 * You can use this class to perform most user operations. 
 * @author dennis
 * @since 3.5.0
 */
public interface SRange {
	
	public ReadWriteLock getLock();
	
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
	
	public enum FillType{
		//fillType of #autoFill
		DEFAULT,// = 0x01; //system determine
		FORMATS,// = 0x02; //formats only
		VALUES,// = 0x04; //value+formula+validation+hyperlink (no comment)
		COPY,// = 0x06; //value+formula+validation+hyperlink, formats
		DAYS,// = 0x10;
		WEEKDAYS,// = 0x20;
		MONTHS,// = 0x30;
		YEARS,// = 0x40;
		HOURS,// = 0x50;
		GROWTH_TREND,// = 0x100; //multiplicative relation
		LINER_TREND,// = 0x200; //additive relation
		SERIES// = LINER_TREND;
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
		TEXT_AS_NUMBERS //convert text to number
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
	
	public enum SheetVisible {
		VISIBLE,
		HIDDEN,
		VERY_HIDDEN,
	}
	
//	public NSheet getSheet();
//	public int getRow();
//	public int getColumn();
//	public int getLastRow();
//	public int getLastColumn();
//	
//	public void setEditText(String editText);
//	public String getEditText();
//	
//	public void setValue(Object value);
//	public void clear();
//	public void notifyChange();
//	public boolean isWholeRow();
//	public NRange getRows();
//	public void setRowHeight(int heightPx);
//	public boolean isWholeColumn();
//	public NRange getColumns();
//	public void setColumnWidth(int widthPx);
//	boolean isWholeSheet();
	
	////////////////////////////////////
//	/**
//	 * Returns rich text string of this Range.
//	 * @return rich text string of this Range.
//	 */
//	public RichTextString getText();
//	
//	/**
//	 * Returns formatted text + text color of this Range.
//	 * @return formatted text + text color of this Range.
//	 */
//	public XFormatText getFormatText();
	
	/**
	 * Returns the hyperlink of this Range.
	 * @return hyperlink of this Range
	 */
	public SHyperlink getHyperlink();
	
	//ZSS-742
	/**
	 * Return the rich edit text of the left top cell of this Range.
	 * @return the rich edit text of this Range.
	 * @since 3.6.0
	 */
	public String getRichText();

	//ZSS-742: Support API for input rich text
	/**
	 * Set rich text into the left top cell of this range. 
	 * @since 3.6.0
	 */
	public void setRichText(String html);
	
	/**
	 * Return the edit text of this Range.
	 * @return the edit text of this Range.
	 */
	public String getEditText();
	
	/**
	 * Set plain text as input by the end user.
	 * @param txt the string input by the end user.
	 */
	public void setEditText(String txt);
	
	/**
	 * cut the selected range and paste to destination range.
	 * @param dstRange
	 * @return the real destination range.
	 * @since 3.0.0
	 */
	public SRange copy(SRange dstRange, boolean cut);

	/**
	 * Copy data from this range to the specified destination range.
	 * @param dstRange the destination range.
	 * @return the real destination range.
	 */
	public SRange copy(SRange dstRange);
	
	/**
	 * Pastes to a destination Range from this range.
	 * @param dstRange the destination range to be pasted into.
	 * @param pasteType the part of the range to be pasted.
	 * @param pasteOp the paste operation
	 * @param skipBlanks true to not have blank cells in the ranage to paste into destination Range; default false.
	 * @param transpose true to transpose rows and columns when pasting to this range; default false.
	 * @return real destination range that was pasted into.
	 */
	public SRange pasteSpecial(SRange dstRange, PasteType pasteType, PasteOperation pasteOp, boolean skipBlanks, boolean transpose);
	
	/**
	 * Insert cells of this Range. 
	 * @param shift can be {@link #SHIFT_DEFAULT}, {{@link #SHIFT_DOWN}, or {@link #SHIFT_RIGHT}.
	 * @param copyOrigin from where to copy the format to the insert area({@link #FORMAT_LEFTABOVE} /{@link #FORMAT_RIGHTBELOW})
	 */
	public void insert(InsertShift shift, InsertCopyOrigin copyOrigin);
	
	/**
	 * Delete cells of this Range. 
	 * @param shift can be {@link #SHIFT_DEFAULT}, {{@link #SHIFT_UP}, or {@link #SHIFT_LEFT}.
	 */
	public void delete(DeleteShift shift);

	/**
	 * Sort this Range according the specified parameters
	 * @param key1 key1 for sorting
	 * @param descending1 true to do descending sort; false to do ascending sort for key1. 
	 * @param dataOption1 see numeric String as number or not for key1.
	 * @param key2 key2 for sorting
	 * @param descending2 true to do descending sort; false to do ascending sort for key2.
	 * @param dataOption2 see numeric String as number or not for key2.
	 * @param key3 key3 for sorting
	 * @param descending3 true to do descending sort; false to do ascending sort for key3.
	 * @param dataOption3 see numeric String as number or not for key3.
	 * @param hasHeader whether sort range includes header
	 * @param matchCase true to match the string cases; false to ignore string cases
	 * @param sortByRows true to sort by rows(change columns orders); false to sort by columns(change row orders). 
	 */
	public void sort(SRange key1, boolean descending1, SortDataOption dataOption1, SRange key2, boolean descending2, SortDataOption dataOption2, SRange key3, boolean descending3, SortDataOption dataOption3,
			int hasHeader, boolean matchCase, boolean sortByRows);

	/**
	 * Merge cells of this range into a merged cell.
	 * @param across true to merge cells in each row; default to false.
	 */
	public void merge(boolean across);

	/**
	 * Un-merge a merged cell in this range to separated cells.
	 */
	public void unmerge();
	
//	/**
//	 * Adds/Remove border around this range.
//	 */
//	public void borderAround(BorderStyle lineStyle, String color);

	/**
	 * Adds/Remove border of all cells within this range upon the specified border type.
	 * @param borderIndex one of {@link ApplyBorderType}
 	 * @param lineStyle border line style, one of {@link SCellStyle.BorderStyle} 
	 * @param color color in HTML format; i.e., #rrggbb.
	 */
	public void setBorders(ApplyBorderType borderIndex, SBorder.BorderType lineStyle, String color);

	/**
	 * Move this range to a new place as specified by row offset (negative value to move up; 
	 * positive value to move down) and column offset(negative value to move left; positive value to move right)
	 * @param nRow how many rows to move this range
	 * @param nCol how many columns to move this range
	 */
	public void move(int nRow, int nCol);
	
	/**
	 * Sets column width in unit of pixel
	 * @param widthPx 
	 */
	public void setColumnWidth(int widthPx);
	
	/**
	 * Sets row height in unit of pixel
	 * @param hightPx
	 */
	public void setRowHeight(int heightPx);

	/**
	 * Sets the width(in pixel) of column in this range, it effect to whole column. 
	 * @param widthPx width in pixel
	 * @param custom mark it as custom value
	 * @see #toColumnRange()
	 */
	public void setColumnWidth(int widthPx,boolean custom);
	/**
	 * Sets the height(in pixel) of row in this range, it effect to whole row.
	 * @param widthPx width in pixel
	 * @param custom mark it as custom value
	 * @see #toRowRange()
	 */
	public void setRowHeight(int heightPx,boolean custom);
	
	/**
	 * Returns associate {@link SSheet} of this range.
	 * @return associate {@link SSheet} of this range.
	 */
	public SSheet getSheet();
	
//	/**
//	 * Return the range that contains the cell specified in row, col (relative to this Range).
//	 * @param row row index relative to this Range(note that it is 0-based)
//	 * @param col column index relative to this Range(note that it is 0-based)
//	 * @return the range that contains the cell specified in row, col (relative to this Range).
//	 */
//	public SRange getCells(int row, int col);
	
	/**
	 * Sets a Style object to this Range.
	 * @param style the style object
	 */
	public void setCellStyle(SCellStyle style);
	
	/**
	 * Clear the cell styles of this Range
	 */
	public void clearCellStyles();
	
	/**
	 * Perform an auto fill on the specified destination Range. Note the given destination Range
	 * must include this source Range.
	 * @param dstRange destination range to do the auto fill. Note the given destination Range must include this source Range
	 * @param fillType the fillType
	 */
	public void fill(SRange dstRange, FillType fillType);
	
	/**
	 * Clears the data from this Range.
	 */
	public void clearContents();
	
	/**
	 * Fills down from the top cells of this Range to the rest of this Range.
	 */
	public void fillDown();
	
	/**
	 * Fills left from the rightmost cells of this Range to the rest of this Range.
	 */
	public void fillLeft();
	
	/**
	 * Fills right from the leftmost cells of this Range to the rest of this Range.
	 */
	public void fillRight();

	/**
	 * Fills up from the bottom cells of this Range to the rest of this Range.
	 */
	public void fillUp();
	
	/**
	 * To find a range of cells for applying auto filter according to this range.
	 * Usually, these two ranges are different.
	 * This method searches the filtering range through a specific rules. 
	 * @see org.zkoss.zss.api.Range#findAutoFilterRange()
	 * @return a range of cells for applying auto filter or null if can't find one from this Range. 
	 * @since 3.0.0
	 */
	// Refer to ZSS-246.
	SRange findAutoFilterRange();
	
	/**
	 * Filters a list specified by this Range and returns an AutoFilter object.
	 * @param field offset of the field on which you want to base the filter on (1-based; i.e. leftmost column in this range is field 1).
	 * @param filterOp, Use FILTEROP_AND and FILTEROP_OR with criteria1 and criterial2 to construct compound criteria.
	 * @param criteria1 "=" to find blank fields, "<>" to find non-blank fields. If null, means ALL. If filterOp == AutoFilter#FILTEROP_TOP10, 
	 * then this shall specifies the number of items (e.g. "10"). 
	 * @param criteria2 2nd criteria; used with criteria1 and filterOP to construct compound criteria.
	 * @param showButton true to show the autoFilter drop-down arrow for the filtered field; false to hide the autoFilter drop-down arrow; null
	 * to keep as is.
	 * @return the applied AutoFiltering
	 */
	public SAutoFilter enableAutoFilter(int field, FilterOp filterOp, Object criteria1, Object criteria2, Boolean showButton);
	
	/**
	 * Enable the auto filter and return it, get null if you disable it. 
	 * @return the autofilter if enable, or null if disable. 
	 */
	public SAutoFilter enableAutoFilter(boolean enable);
	
	
	/**
	 * Reset the autofilter, clear the condition, shows all the hidden row
	 */
	public void resetAutoFilter();
	
	/**
	 * Apply the autofilter with the old condition and current cell values
	 */
	public void applyAutoFilter();

	/**
	 * Sets whether this rows or columns are hidden(useful only if this Range cover entire column or entire row)
	 * @param hidden true to hide this rows or columns
	 */
	public void setHidden(boolean hidden);
	
	/**
	 * Sets whether show the gridlines of the sheets in this Range.
	 * @param show true to show the gridlines; false to not show the gridlines. 
	 */
	public void setDisplayGridlines(boolean show);
	
    /**
     * @deprecated use {@link #protectSheet(String password,
	 *		boolean allowSelectingLockedCells,
	 *		boolean allowSelectingUnlockedCells, boolean allowFormattingCells,
	 *		boolean allowFormattingColumns, boolean allowFormattingRows,
	 *		boolean allowInsertColumns, boolean allowInsertRows,
	 *		boolean allowInsertingHyperlinks, boolean allowDeletingColumns,
	 *		boolean allowDeletingRows, boolean allowSorting,
	 *		boolean allowFiltering, boolean allowUsingPivotTables,
	 *		boolean drawingObjects, boolean scenarios)} instead.
     * Sets the protection enabled as well as the password per the current 
     * {@link SheetProtection}.
     * @param password to set for protection.
     */
    public void protectSheet(String password);
	
	/**
	 * Sets the hyperlink of this Range
	 * @param linkType the type of target to link. One of the {@link Hyperlink#LINK_URL}, 
	 * {@link Hyperlink#LINK_DOCUMENT}, {@link Hyperlink#LINK_EMAIL}, {@link Hyperlink#LINK_FILE}
	 * @param address the address
	 * @param display the text to display link
	 */
	public void setHyperlink(HyperlinkType linkType, String address, String display);
	
//	/**
//	 * Returns an {@link XAreas} which is a collection of each single selected area(also Range) of this multiple-selected Range. 
//	 * If this Range is a single selected Range, this method return the Areas which contains only this Range itself.
//	 * @return an {@link XAreas} which is a collection of each single selected area(also Range) of this multiple-selected Range.
//	 */
//	public XAreas getAreas();
	
	/**
	 * Returns a {@link SRange} that represent columns of the 1st selected area of this Range. Note that only the 1st selected area is considered if this Range is a multiple-selected Range. 
	 * @return a {@link SRange} that represent columns of this Range and contains all rows of the column.
	 */
	public SRange getColumns();
	
	/**
	 * Returns a {@link SRange} that represent rows of the 1st selected area of this Range. Note that only the 1st selected area is considered if this Range is a multiple-selected Range. 
	 * @return a {@link SRange} that represent rows of this Range and contains all columns of the rows.
	 */
	public SRange getRows();

//	/**
//	 * Returns a {@link NRange} that represent all dependents of the left-top cell of the 1st selected area of this Range. 
//	 * Note that only the left-top cell of the 1st selected area is considered if this Range is a multiple-selected Range.
//	 * This could be multiple-selected Range if there are more than one dependent. 
//	 * @return a {@link NRange} that represent all dependents of the left-top cell of the 1st selected area of this Range.
//	 */
//	public NRange getDependents();
	
//	/**
//	 * Returns a {@link NRange} that represent all direct dependents of the left-top cell of the 1st selected area of this Range. 
//	 * Note that only the left-top cell of the 1st selected area is considered if this Range is a multiple-selected Range. 
//	 * This method could return multiple-selected Range if there are more than one dependent. 
//	 * @return a {@link NRange} that represent all direct dependents of the left-top cell of the 1st selected area of this Range.
//	 */
//	public NRange getDirectDependents();
	
//	/** 
//	 * Returns a {@link NRange} that represent all precedents of the left-top cell of the 1st selected area of this Range. 
//	 * Note that only the left-top cell of the 1st selected area is considered if this Range is a multiple-selected Range.
//	 * This method could return multiple-selected Range if there are more than one precedent. 
//	 * @return a {@link NRange} that represent all precedents of the left-top cell of the 1st selected area of this Range.
//	 */
//	public NRange getPrecedents();
//	
//	/**
//	 * Returns a {@link NRange} that represent all direct precedents of the left-top cell of the 1st selected area of this Range. 
//	 * Note that only the left-top cell of the 1st selected area is considered if this Range is a multiple-selected Range. 
//	 * This method could return multiple-selected Range if there are more than one precedent. 
//	 * @return a {@link NRange} that represent all direct precedents of the left-top cell of the 1st selected area of this Range.
//	 */
//	public NRange getDirectPrecedents();
	
	/**
	 * Returns the number of the 1st row of the 1st area in this Range(0-based; i.e. row1 return 0)
	 * @return the number of the 1st row of the 1st area in this Range(0-based; i.e. row1 return 0)
	 */
	public int getRow();
	
	/**
	 * Returns the number of the 1st column of the 1st area in this Range(0-based; i.e. Column A return 0)
	 * @return the number of the 1st column of the 1st area in this Range(0-based; i.e. Column A return 0)
	 */
	public int getColumn();
	
	/**
	 * Returns the number of the last row of the 1st area in this Range(0-based; i.e. row1 return 0)
	 * @return the number of the last row of the 1st area in this Range(0-based; i.e. row1 return 0)
	 */
	public int getLastRow();
	
	/**
	 * Returns the number of the last column of the 1st area in this Range(0-based; i.e. Column A return 0)
	 * @return the number of the last column of the 1st area in this Range(0-based; i.e. Column A return 0)
	 */
	public int getLastColumn();
	
//	/**
//	 * Returns the number of contained objects in this Range.
//	 * @return the number of contained objects in this Range.
//	 */
//	public long getCount();

	/**
	 * Set value into this Range.
	 * @param value the value
	 */
	public void setValue(Object value);
	
	/**
	 * Returns left top cell value of this Range.
	 * @return left top cell value of this Range
	 */
	public Object getValue();
	
	/**
	 * Returns a {@link SRange} that represents a range that offset from this Range. 
	 * @param rowOffset positive means downward; 0 means don't change row; negative means upward.
	 * @param colOffset positive means rightward; 0 means don't change column; negative means leftward.
	 * @return a {@link SRange} that represents a range that offset from this Range.
	 */
	public SRange getOffset(int rowOffset, int colOffset);
	
//	/**
//	 * Returns a {@link NRange} that bounds current Left-top cell of this Range with a combination of blank Rows and Columns.
//	 * @return a {@link NRange} that bounds current Left-top cell of this Range with a combination of blank Rows and Columns.
//	 */
//	public NRange getCurrentRegion();
//	
//	/**
//	 * Reapply current {@link AutoFilter}.
//	 */
//	public void applyFilter();
//	
//	/**
//	 * Clear all application of the current {@link AutoFilter}. 
//	 */
//	public void showAllData();

	/**
	 * Add a chart into the sheet of this Range 
	 * @param anchor
	 * @param isThreeD TODO
	 * @return the created chart 
	 */
	public SChart addChart(ViewAnchor anchor, ChartType type, ChartGrouping grouping, ChartLegendPosition pos, boolean isThreeD);


	/**
	 * Add a picture into the sheet of this Range
     * @return the created picture
	 */
	public SPicture addPicture(ViewAnchor anchor, byte[] image, Format format);

	/**
	 * Delete an existing picture from the sheet of this Range.
	 * @param picture the picture to be deleted
	 */
	public void deletePicture(SPicture picture);
	
	/**
	 * Update picture anchor. Can be used to resize or move a picture.
	 * @param picture the picture to change
	 * @param anchor the new anchor
	 */
	public void movePicture(SPicture picture, ViewAnchor anchor);

	/**
	 * Move the chart to the new anchor.
	 * @param chart the chart to change anchor
	 * @param anchor the new anchor
	 */
	public void moveChart(SChart chart, ViewAnchor anchor);
	
	/**
	 * Notify the model that a chart contains change, for example, chart data changes.
	 * @param chart the chart that changes
	 */
	public void updateChart(SChart chart);
	
	/**
	 * Delete an existing chart from the sheet of this Range.
	 * @param chart the chart to be deleted
	 */
	public void deleteChart(SChart chart);
	
	/**
	 * Returns whether the plain text input by the end user is valid or not;
	 * note the validation only applies to the left-top cell of this Range.
	 * @param txt the string input by the end user.
	 * @return null if a valid input to the specified range; otherwise, the DataValidation
	 */
	public SDataValidation validate(String txt);
	
	/**
	 * Returns whether any cell is protected and locked in this Range.
	 * @return true if any cell is protected and locked in this Range.
	 */
	public boolean isAnyCellProtected();

//	/**
//	 * Move focus of the sheet of this Range(used for book collaboration).
//	 * @param token the token to identify the focus
//	 */
//	public void notifyMoveFriendFocus(Object token);
//	
//	/**
//	 * Delete focus of the sheet of this Range(used for book collaboration). 
//	 * @param token the token to identify the registration
//	 */
//	public void notifyDeleteFriendFocus(Object token);
	
	/**
	 * Send a custom model event to all book's listener, the event name must not conflict with that in {@link ModelEvents} 
	 * @param customEventName the event custom event
	 * @param data the data
	 * @param writeLock use write lock when notify , set true if the synchronized book listener will modify the book.
	 */
	public void notifyCustomEvent(String customEventName,Object data,boolean writeLock);
	
	/**
	 * Delete sheet of this Range.
	 */
	public void deleteSheet();
	
	/**
	 * Create sheet of this book as specified in this Range.
	 * @param name the name of the new created sheet; null would use default 
	 * "SheetX" name where X is the next sheet number.
	 */
	public SSheet createSheet(String name);
	
	/**
	 * Clone sheet as specified in this Range.
	 * @param name the name of the new created sheet; null would use defulat
	 * "SheetX" name where X is the next sheet number.
	 */
	public SSheet cloneSheet(String name);
	
	/**
     * Set(Rename) the name of the sheet as specified in this Range.
	 * @param name
	 */
	public void setSheetName(String name);
	
    /**
     * Sets the order of the sheet as specified in this Range.
     *
     * @param pos the position that we want to insert the sheet into (0 based)
     */
	public void setSheetOrder(int pos);
	
	/**
	 * Check if this range cover an entire rows (form 0, and last row to the max available row of a sheet) 
	 */
	public boolean isWholeRow();
	
	/**
	 * Check if this range cover an entire columns (form 0, and last row to the max available column of a sheet) 
	 */
	public boolean isWholeColumn();
	
	/**
	 * Check if this range cover an entire sheet 
	 */
	public boolean isWholeSheet();
	
	
	/**
	 * Notify this range has been changed.
	 */
	public void notifyChange();
	
	/**
	 * Notify the variables in this range has been changed.
	 * @param variables
	 */
	public void notifyChange(String[] variables);
	
	/**
	 * Set the freeze panel
	 * @param numOfRow the number of row to freeze, 0 means no freeze
	 * @param numOfColumn the number of column to freeze, 0 means no freeze
	 */
	public void setFreezePanel(int numOfRow, int numOfColumn);

	/**
	 * Gets the first(top-left) cell's formatted text,
	 * if the cell's format is the special LOCALE aware format (such as m/d/yyyy), 
	 * it will formats the value by transferred format (e.g m/d/yyyy will transfer to yyyy/m/d in TW locale) 
	 * @return
	 */
	public String getCellFormatText();
	
	/**
	 * Gets the first(top-left) cell's format,
	 * if the cell's format is the special LOCALE aware format (such as m/d/yyyy), 
	 * it transfer the format by LOCALE(e.g m/d/yyyy will transfer to yyyy/m/d in TW locale) 
	 * @return
	 */
	public String getCellDataFormat();
	
	/**
	 * Gets the first(top-left) cell's style
	 * @return
	 */
	public SCellStyle getCellStyle();

	/** 
	 * Gets the first region's protection status; return true if the sheet of
	 * the first region is protected and some cells in the region is locked.
	 */
	public boolean isProtected();
	
	/**
	 * Gets whether the sheet of the first region of this Range is protected.
	 * @return
	 */
	public boolean isSheetProtected();

	/**
	 * Clear data contains, style and unmerge this range 
	 */
	public void clearAll();
	
	/**
	 * Create a {@link SName} that refer to this range.
	 * @param nameName name of the {@link SName} that you can refer in formulas.
	 */
	public void createName(String nameName);

	/**
	 * Protect a {@link Sheet} so that it cannot be modified.
	 * @param password a case-sensitive password for the sheet; null means sheet is not password protected.
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
	 */
	public void protectSheet(String password,  
			boolean allowSelectingLockedCells, boolean allowSelectingUnlockedCells,  
			boolean allowFormattingCells, boolean allowFormattingColumns, boolean allowFormattingRows, 
			boolean allowInsertColumns, boolean allowInsertRows, boolean allowInsertingHyperlinks,
			boolean allowDeletingColumns, boolean allowDeletingRows, 
			boolean allowSorting, boolean allowFiltering, boolean allowUsingPivotTables, 
			boolean drawingObjects, boolean scenarios);
	
	/**
	 * Removes protection from a sheet. This method has no effect if the sheet 
	 * isn't protected.
	 * @param password a case-sensitive password used to unprotect the sheet. If
	 * the sheet isn't protected with a password, this argument is ignored. If you
	 * omit this argument for a sheet that is protected with a password, you'll
	 * be prompted for the password. 
	 */
	public boolean unprotectSheet(String password);
	
	/**
	 * Gets {@link SSheetProtection} protection options that tells what are 
	 * allowed operations for the protected sheet of the first region of this 
	 * range.
	 * @return
	 */
	public SSheetProtection getSheetProtection();

	/**
	 * Add if not exist or modify an existing {@link SDataValidation} to this range.
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
	 * Gets {@link SDataValidation}s associated with this range; if more than 
	 * one validation is present, will return at most two.
	 * 
	 * @see #setValidation(ValidationType, boolean, OperatorType, boolean, String, String, boolean, String, String, boolean, AlertStyle, String, String)
	 */
	public List<SDataValidation> getValidations();
	
	/**
	 * Delete the {@link SDataValidation} associated in the specified range. 
	 */
	public void deleteValidation();
	
	/**
	 * Get font if exists or create font if not exists from the book of this 
	 * range and return it.
	 * 
	 * @return font of the specified font metrics
	 * @since 3.6.0
	 */
	public SFont getOrCreateFont(SFont.Boldweight boldweight, String htmlColor,
			int fontHeight, String fontName, boolean italic, boolean strikeout,
			SFont.TypeOffset typeOffset, SFont.Underline underline);

	/**
	 * Evaluate(if not cached), update data model, and refresh UI of this range 
	 * and its dependent cells if the includeDependents is true. 
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
	 * Set Sheet visibility; can be SheetVisible.VISIBLE, SheetVisible.HIDDEN,
	 * and SheetVisible.VERY_HIDDEN.
	 * <ul>
	 * 	<li>VISIBLE: the sheet is visible</li>
	 *  <li>HIDDEN: the sheet is hidden but can be unhidden using UI</li>
	 *  <li>VERY_HIDDEN: the sheet is hidden but can be unhidden only via this API.</li>
	 * </ul>
	 * @since 3.7.0
	 * @see SheetVisible
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
	 * Notify this range has been changed with the specified attribute.
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
