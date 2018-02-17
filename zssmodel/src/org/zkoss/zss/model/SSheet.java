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
package org.zkoss.zss.model;

import org.model.DBContext;
import org.zkoss.zss.model.impl.Model;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A sheet of a book. It's the main class you can manipulate rows, columns, cells, pictures, charts, and data validation.
 * @author dennis
 * @since 3.5.0
 */
public interface SSheet {
	
	/**
	 * Get the owner book
	 * @return the owner book
	 */
	SBook getBook();
	
	/**
	 * Get the sheet name
	 * @return the sheet name
	 */
	String getSheetName();
	
	/**
	 * @return an iterator of existing rows excluding those blank rows
	 */
	Iterator<SRow> getRowIterator();


	Model getDataModel();

	void setDataModel(String model);


	/**
	 * @return an iterator of existing columns excluding those blank columns
	 */
	Iterator<SColumn> getColumnIterator();

	Iterator<SColumnArray> getColumnArrayIterator();
	
	/**
	 * Set up a column array, if one array range overlaps another, it throws IllegalStateException.
	 * If you setup a column array that is not continuous, (for example, 0~2, 5~6), then it will create a missing column array automatically
	 * to make them continuous.(3~4 in the example). 
	 * @param colunmIdx index of the starting column
	 * @param lastColumnIdx index of the end column
	 * @return the new created column array
	 */
	SColumnArray setupColumnArray(int colunmIdx, int lastColumnIdx);

	Iterator<SCell> getCellIterator(int row);
	
	/**
	 * @return default row height in pixels
	 */
	int getDefaultRowHeight();
	
	/**
	 * set default row height in pixels
	 */
	void setDefaultRowHeight(int height);
	
	/**
	 * @return default column width in pixels
	 */
	int getDefaultColumnWidth();
	
	/**
	 * set default column width in pixels 
	 */
	void setDefaultColumnWidth(int width);


	SRow getRow(int rowIdx);
	
	/**
	 * @see #setupColumnArray
	 */
	SColumnArray getColumnArray(int columnIdx);

	SColumn getColumn(int columnIdx);
	
	/**
	 * 
	 * This method always returns not-null cell object. Use {@link SCell#isNull()} to know it's null (blank) or not.
	 */
	SCell getCell(int rowIdx, int columnIdx);

    SCell getCell(CellRegion region);

	Collection<SCell> getCells(CellRegion region);
	Collection<SCell> getCells();

	/**
	 * @return return a cell with specified cell reference, e.g. A2, B3. Area reference, A1:A2, is not acceptable.
	 * @see #getCell(int, int) 
	 */
	SCell getCell(String cellRefString);
	/**
	 * @return interal sheet object ID
	 */
	String getId();

	int getDBId();

	void setDBId(int dbId);

	SSheetViewInfo getViewInfo();

	SPrintSetup getPrintSetup();

	int getStartRowIndex();

	int getEndRowIndex();

	int getStartColumnIndex();

	int getEndColumnIndex();

	int getStartCellIndex(int rowIdx);

	int getEndCellIndex(int rowIdx);
	
//	public void clearRow(int rowIdx, int rowIdx2);
//	public void clearColumn(int columnIdx,int columnIdx2);
	/**
	 * @see #clearCell(CellRegion)
	 */
	void clearCell(int rowIdx, int columnIdx, int lastRowIdx, int lastColumnIdx);
	
	/**
	 * Clear cells in specified region
	 */
	void clearCell(CellRegion region);
	
	/**
	 * Move a region of cells specified by 4 indexes.
	 * @see #moveCell(CellRegion, int, int)
	 */
	void moveCell(int rowIdx, int columnIdx, int lastRowIdx, int lastColumnIdx, int rowOffset, int columnOffset);
	
	/**
	 * Move one or more cells.
	 * @param region the region of cells to move
	 * @param rowOffset positive number to move down, negative to move up
	 * @param columnOffset positive number to move right, negative to move left
	 */
	void moveCell(CellRegion region, int rowOffset, int columnOffset);
	
	/**
	 * insert rows specified by first and last index 
	 */
	void insertRow(int rowIdx, int lastRowIdx);
	
	/**
	 * delete rows specified by first and last index 
	 */
	void deleteRow(int rowIdx, int lastRowIdx);
	
	/**
	 * insert columns specified by first and last index 
	 */
	void insertColumn(int columnIdx, int lastColumnIdx);
	
	/**
	 * delete columns specified by first and last index 
	 */
	void deleteColumn(int columnIdx, int lastColumnIdx);

	/**
	 * Clear the cache
	 */
	void clearCache(CellRegion cellRegion);
	void clearCache();

	/**
	 * @see #insertCell(CellRegion, boolean)
	 */
	void insertCell(int rowIdx, int columnIdx, int lastRowIndex, int lastColumnIndex, boolean horizontal);
	
	/**
	 * Insert a region of cells and shift existing cells.
	 * @param region the region of cells to insert
	 * @param horizontal TRUE for shifting right, FALSE for shifting down
	 */
	void insertCell(CellRegion region, boolean horizontal);
	
	/**
	 * Delete a region of cells and shift existing cells.
	 * @param region the region of cells to delete
	 * @param horizontal TRUE for shifting left, FALSE for shifting up
	 */
	void deleteCell(CellRegion region, boolean horizontal);
	
	/**
	 * @see #deleteCell(CellRegion, boolean) 
	 */
	void deleteCell(int rowIdx, int columnIdx, int lastRowIndex, int lastColumnIndex, boolean horizontal);
	
	/** Add a picture into this sheet with raw picture data and format.
	 * @param format picture format as specified in {@link SPicture.Format}
	 * @param data raw byte data of the picture
	 * @param anchor where to anchor this picture
	 * @return the added {@link SPicture}
	 */
	SPicture addPicture(SPicture.Format format, byte[] data, ViewAnchor anchor);
	
	/**
	 * Add a picture into the sheet with known picture data index.
	 * @param index {@link SPictureData} index
	 * @param anchor where to anchor this picture
	 * @return the {@link SPicture} added
	 * @since 3.6.0
	 */
	SPicture addPicture(int index, ViewAnchor anchor);

	SPicture getPicture(String picid);

	void deletePicture(SPicture picture);

	int getNumOfPicture();

	SPicture getPicture(int idx);

	List<SPicture> getPictures();


	SChart addChart(SChart.ChartType type, ViewAnchor anchor);

	SChart getChart(String chartid);

	void deleteChart(SChart chart);

	int getNumOfChart();

	SChart getChart(int idx);

	List<SChart> getCharts();


	List<CellRegion> getMergedRegions();
	/**
	 * Remove the merged area that are contained by region 
	 * @param region
	 * @param removeOverlaps true if you want to remove the merged areas that are just overlapped.
	 */
	void removeMergedRegion(CellRegion region, boolean removeOverlaps);
	/**
	 * Add a merged area, you can't assign a area that overlaps existed merged area.
	 * @param region
	 */
	void addMergedRegion(CellRegion region);

	int getNumOfMergedRegion();

	CellRegion getMergedRegion(int idx);
	
	/**
	 * Get the merged region that overlapped the region
	 * @return the regions that overlaps
	 */
	List<CellRegion> getOverlapsMergedRegions(CellRegion region, boolean excludeContains);
	
	/**
	 * Get the merged region that are contained by region.
	 * @return the regions that are contained
	 */
	List<CellRegion> getContainsMergedRegions(CellRegion region);

	CellRegion getMergedRegion(int row, int column);

	CellRegion getMergedRegion(String cellRefString);

	SDataValidation addDataValidation(CellRegion region);

	SDataValidation addDataValidation(CellRegion region, SDataValidation src);

	SDataValidation getDataValidation(String id);

	void deleteDataValidation(SDataValidation validation);

	int getNumOfDataValidation();

	SDataValidation getDataValidation(int idx);

	List<SDataValidation> getDataValidations();
	@Deprecated
	void removeDataValidationRegion(CellRegion region);
	/**
	 * Delete data validations that are covered by the specified region.
	 * @param region the cover region
	 * @return the data validations deleted
	 * @since 3.6.0
	 */
	List<SDataValidation> deleteDataValidationRegion(CellRegion region);
	
	/**
	 * @param row
	 * @param column
	 * @return the first data validation at row, column
	 */
	SDataValidation getDataValidation(int row, int column);
	
	/**
	 * Get the runtime custom attribute that stored in this sheet
	 * @param name the attribute name
	 * @return the value, or null if not found
	 */
	Object getAttribute(String name);
	
	/**
	 * Set the runtime custom attribute to stored in this sheet, the attribute is only use for developer to stored runtime data in the sheet,
	 * values will not stored to excel when exporting.
	 * @param name name the attribute name
	 * @param value the attribute value
	 */
	Object setAttribute(String name, Object value);
	
	/**
	 * Get the unmodifiable runtime attributes map
	 * @return
	 */
	Map<String, Object> getAttributes();

	/**
	 * Check if the sheet is protected
	 * @return
	 */
	boolean isProtected();
	
	/**
	 * Sets password to protect sheet, set null to unprotect it.
	 * @param password
	 */
	void setPassword(String password);
	
	/**
	 * Internal Use only.
	 * @return
	 */
	short getHashedPassword();
	
	/**
	 * Internal User only.
	 */
	void setHashedPassword(short hashpass);
	
	/**
	 * Gets the auto filter information if there is.
	 * @return the auto filter, or null if not found
	 */
	SAutoFilter getAutoFilter();
	
	/**
	 * Creates a new auto filter, the old one will be drop directly.
	 * @param region the auto filter region
	 * @return the new auto filter.
	 */
	SAutoFilter createAutoFilter(CellRegion region);
	
	
	/**
	 * Delete current autofilter if it has
	 */
	void deleteAutoFilter();
	
	/**
	 * Clear auto filter if there is.
	 */
	void clearAutoFilter();
	
	/**
	 * paste cell from src sheet to this sheet, the sheets must in same book
	 * @param src src sheet and it's region to paste
	 * @param dest destination region in this sheet
	 * @param option the copy option
	 * @return the final effected region
	 */
	CellRegion pasteCell(SheetRegion src, CellRegion dest, PasteOption option);
	
	/**
	 * Gets enhanced protection.
	 */
	SSheetProtection getSheetProtection();
	
	/**
	 * Get the sheet current visible state.
	 * @since 3.7.0
	 */
	SheetVisible getSheetVisible();
	
	/**
	 * Set the sheet current visible state.
	 * @since 3.7.0
	 */
	void setSheetVisible(SheetVisible state);


	/**
	 * Create a model for the sheet
	 */
	void createModel(DBContext dbContext, String modelName);

	/**
	 * Copy a model for the sheet
	 */
	void cloneModel(DBContext dbContext, String modelName, SSheet src);

    /**
     * Create a model for the sheet
     */
    void deleteModel(DBContext dbContext);

	/**
	 * Add a new table
	 * @param table
	 * @since 3.8.0
	 */
	void addTable(STable table);
	
	/**
	 * Get tables in this sheet
	 * @return
	 * @since 3.8.0
	 */
	List<STable> getTables();

	/**
	 * Remove the table of the specified table name.
	 * @param tableName
	 * @since 3.8.0
	 */
	void removeTable(String tableName);

	/**
	 * New way of hashing sheet protection password.
	 *
	 * @param hashValue
	 * @since 3.8.1
	 */
	void setHashValue(String hashValue);
	
	/**
	 * New way of hashing sheet protection password.
	 *
	 * @param spinCount
	 * @since 3.8.1
	 */
	void setSpinCount(String spinCount);

	/**
	 * New way of hashing sheet protection password.
	 *
	 * @param saltValue
	 * @since 3.8.1
	 */
	void setSaltValue(String saltValue);
	
	/**
	 * New way of hashing sheet protection password.
	 *
	 * @param algName
	 * @since 3.8.1
	 */
	void setAlgName(String algName);


	enum SheetVisible {
		VISIBLE,    // This sheet is visible
		HIDDEN,        // This sheet is hidden (but can be unhide via UI dialog)
		VERY_HIDDEN,    // This sheet is hidden and only can be unhhide by API
	}

	void setSyncComputation(boolean syncComputation);
}
