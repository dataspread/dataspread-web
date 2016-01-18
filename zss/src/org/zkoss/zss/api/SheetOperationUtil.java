/* SheetOperationUtil.java

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zkoss.image.AImage;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Chart;
import org.zkoss.zss.api.model.Picture;
import org.zkoss.zss.api.model.Picture.Format;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.api.model.impl.SheetImpl;
import org.zkoss.zss.model.SSheet;

/**
 * The utility to help UI to deal with user's sheet operation of a Range.
 * This utility is the default implementation for handling a spreadsheet, it is also the example for calling {@link Range} APIs 
 * @author dennis
 * @since 3.0.0
 */
public class SheetOperationUtil {

	/**
	 * Toggles autofilter on or off
	 * @param range the range to toggle
	 */
	public static void toggleAutoFilter(Range range) {
		if(range.isProtected() && !range.getSheetProtection().isAutoFilterAllowed())
			return;
		range.enableAutoFilter(!range.isAutoFilterEnabled());
	}
	
	/**
	 * Resets autofilter
	 * @param range the range to reset
	 */
	public static void resetAutoFilter(Range range) {
		if(range.isProtected() && !range.getSheetProtection().isAutoFilterAllowed())
			return;
		range.resetAutoFilter();
	}
	
	/**
	 * Re-apply autofilter
	 * @param range the range to apply
	 */
	public static void applyAutoFilter(Range range) {
		if(range.isProtected() && !range.getSheetProtection().isAutoFilterAllowed())
			return;
		range.applyAutoFilter();
	}
	
	/**
	 * Add picture to the range
	 * @param range the range to add picture to
	 * @param image the image
	 */
	public static Picture addPicture(Range range, AImage image){
		return addPicture(range,image.getByteData(),getPictureFormat(image),image.getWidth(),image.getHeight());
	}
	
	/**
	 * Add picture to the range
	 * @param range the range to add picture
	 * @param binary the image binary data
	 * @param format the image format
	 * @param widthPx the width of image to place
	 * @param heightPx the height of image to place
	 */
	public static Picture addPicture(Range range, byte[] binary, Format format,int widthPx, int heightPx){
		SheetAnchor anchor = toFilledAnchor(range.getSheet(), range.getRow(),range.getColumn(),
				widthPx, heightPx);
		return addPicture(range,anchor,binary,format);
		
	}
	
	/**
	 * Add picture to the range
	 * @param range the range to add picture
	 * @param anchor the picture location
	 * @param binary the image binary data
	 * @param format the image format
	 */
	public static Picture addPicture(Range range, SheetAnchor anchor, byte[] binary, Format format){
		if(range.isProtected() && !range.getSheetProtection().isObjectsEditable())
			return null;
		return range.addPicture(anchor, binary, format);
	}
	
	/**
	 * Gets the picture format
	 * @param image the image
	 * @return image format, or null if doens't support
	 */
	public static Format getPictureFormat(AImage image) {
		String format = image.getFormat();
		if ("dib".equalsIgnoreCase(format)) {
			return Format.DIB;
		} else if ("emf".equalsIgnoreCase(format)) {
			return Format.EMF;
		} else if ("wmf".equalsIgnoreCase(format)) {
			return Format.WMF;
		} else if ("jpeg".equalsIgnoreCase(format)) {
			return Format.JPEG;
		} else if ("pict".equalsIgnoreCase(format)) {
			return Format.PICT;
		} else if ("png".equalsIgnoreCase(format)) {
			return Format.PNG;
		}
		return null;
	}
	
	/**
	 * Move a picture to specified row and column.
	 * @param range a range represented the sheet that contains the picture
	 * @param picture the picture to move
	 * @param rowIndex destination row index, 0-based
	 * @param columnIndex destination column index, 0-based
	 */
	public static void movePicture(Range range, Picture picture, int rowIndex, int columnIndex){
		if(range.isProtected() && !range.getSheetProtection().isObjectsEditable())
			return;
		if (hasPicture(range, picture)){
			SheetAnchor fromAnchor = picture.getAnchor();
			int rowOffset = fromAnchor.getLastRow() - fromAnchor.getRow();
			int columnOffset = fromAnchor.getLastColumn() - fromAnchor.getColumn();
			
			rowIndex = rowIndex < 0? 0 : rowIndex;
			columnIndex = columnIndex < 0? 0 : columnIndex;
			SheetAnchor toAnchor = new SheetAnchor(rowIndex, columnIndex,
					fromAnchor.getXOffset(), fromAnchor.getYOffset(),
					rowIndex+rowOffset, columnIndex+columnOffset,
					fromAnchor.getLastXOffset(), fromAnchor.getLastYOffset());
			
			range.movePicture(toAnchor, picture);
		}
	}
	
	/**
	 * Delete a picture
	 * @param range a range that represents the sheet that contains the picture
	 * @param picture the picture to delete
	 */
	public static void deletePicture(Range range, Picture picture){
		
		if(range.isProtected() && !range.getSheetProtection().isObjectsEditable())
			return;
		if (hasPicture(range, picture)){
			range.deletePicture(picture);
		}
	}
	
	/**
	 * Determine whether the range of sheet has the picture
	 * @param range
	 * @param picture
	 * @return true if the range has, otherwise false
	 */
	public static boolean hasPicture(Range range, Picture picture){
		for (Picture p : range.getSheet().getPictures()){
			if (p.getId().equals(picture.getId())){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Adds chart to range
	 * @param range the range to add chart
	 * @param data the chart data
	 * @param type the chart type
	 * @param grouping the grouping type
	 * @param pos the legend position type
	 */
	public static Chart addChart(Range range, Chart.Type type, Chart.Grouping grouping,
			Chart.LegendPosition pos) {
		SheetAnchor anchor = toChartAnchor(range);
		return addChart(range,anchor, type, grouping, pos);
	}
	
	/**
	 * Adds chart to range
	 * @param range the range to add chart
	 * @param anchor the chart location
	 * @param data the chart data
	 * @param type the chart type
	 * @param grouping the grouping type
	 * @param pos the legend position type
	 */
	public static Chart addChart(Range range, SheetAnchor anchor, Chart.Type type, Chart.Grouping grouping,
			Chart.LegendPosition pos) {
		if(range.isProtected() && !range.getSheetProtection().isObjectsEditable())
			return null;
		return range.addChart(anchor,type, grouping, pos);
	}
	
	/**
	 * Create a anchor by range's row/column data
	 * @param range the range for chart.
	 * @return a new anchor
	 */
	public static SheetAnchor toChartAnchor(Range range) {
		int row = range.getRow();
		int col = range.getColumn();
		int lRow = range.getLastRow();
		int lCol = range.getLastColumn();
		int w = lCol-col+1;
		//shift 2 column right for the selection width 
		return new SheetAnchor(row, lCol+2, 
				row==lRow?row+7:lRow+1, col==lCol?lCol+7+w:lCol+2+w);
	}
	
	/**
	 * Move a chart to specified row and column.
	 * @param range the range that represents the sheet contains the chart to move.  
	 * @param chart the chart to move
	 * @param rowIndex destination row index, 0-based.
	 * @param columnIndex destination column index, 0-based.
	 */
	public static void moveChart(Range range, Chart chart, int rowIndex, int columnIndex){
		if(range.isProtected() && !range.getSheetProtection().isObjectsEditable())
			return;
		if (hasChart(range, chart)){
			SheetAnchor fromAnchor = chart.getAnchor();
			int rowOffset = fromAnchor.getLastRow() - fromAnchor.getRow();
			int columnOffset = fromAnchor.getLastColumn() - fromAnchor.getColumn();
			
			rowIndex = rowIndex < 0? 0 : rowIndex;
			columnIndex = columnIndex < 0? 0 : columnIndex;
			SheetAnchor toAnchor = new SheetAnchor(rowIndex, columnIndex,
					fromAnchor.getXOffset(), fromAnchor.getYOffset(),
					rowIndex+rowOffset, columnIndex+columnOffset,
					fromAnchor.getLastXOffset(), fromAnchor.getLastYOffset());
			
			range.moveChart(toAnchor, chart);
		}
	}
	
	/**
	 * Delete a chart.
	 * @param range the range that represents the sheet contains the chart to delete.
	 * @param chart the chart to delete.
	 */
	public static void deleteChart(Range range, Chart chart){
		if(range.isProtected() && !range.getSheetProtection().isObjectsEditable())
			return;
		if (hasChart(range, chart)){
			range.deleteChart(chart);
		}
	}

	/**
	 * Determine whether the range of sheet has the chart.
	 * @param range
	 * @param chart
	 * @return true if the range has, otherwise false
	 */
	public static boolean hasChart(Range range, Chart chart){
		for (Chart c : range.getSheet().getCharts()){
			if (c.getId().equals(chart.getId())){
				return true;
			}
		}
		return false;
	}
	
	public static void protectSheet(Range range, String password, String newpasswrod) {
		//TODO the spec?
//		if (range.isProtected())
//			return;
		
		range.protectSheet(newpasswrod);
	}
	
	public static void protectSheet(Range range, String password,  
			boolean allowSelectingLockedCells, boolean allowSelectingUnlockedCells,  
			boolean allowFormattingCells, boolean allowFormattingColumns, boolean allowFormattingRows, 
			boolean allowInsertColumns, boolean allowInsertRows, boolean allowInsertingHyperlinks,
			boolean allowDeletingColumns, boolean allowDeletingRows, 
			boolean allowSorting, boolean allowFiltering, boolean allowUsingPivotTables, 
			boolean drawingObjects, boolean scenarios) {
	
		range.protectSheet(password, allowSelectingLockedCells, allowSelectingUnlockedCells,  
				allowFormattingCells, allowFormattingColumns, allowFormattingRows, 
				allowInsertColumns, allowInsertRows, allowInsertingHyperlinks,
				allowDeletingColumns, allowDeletingRows, 
				allowSorting, allowFiltering, allowUsingPivotTables, 
				drawingObjects, scenarios);
	}
	
	public static boolean unprotectSheet(Range range, String password) {
		return range.unprotectSheet(password);
	}

	/**
	 * Enables/disables sheet gridlines.
	 * @param range the range to be applied
	 * @param enable true for enable
	 */
	public static void displaySheetGridlines(Range range,boolean enable) {
		range.setDisplaySheetGridlines(enable);
	}

	/**
	 * Add a new sheet to this book
	 * @param range the range to be applied
	 * @param prefix the sheet name prefix, it will selection a new name with this prefix and a counter.
	 */
	public static void addSheet(Range range, final String prefix) {
		range.sync(new RangeRunner(){
			public void run(Range range) {
				String name;
				Book book = range.getBook();
				int numSheet = book.getNumberOfSheets();
				do{
					numSheet++;
					name = prefix+numSheet;
				}while(book.getSheet(name)!=null);
				range.createSheet(name);
			}});
	}
	
	/**
	 * Add a new sheet to this book
	 * @param range the range to be applied
	 * @param name the sheet name, it must not same as another sheet name in this book
	 */
	public static void createSheet(Range range, final String name) {
		range.sync(new RangeRunner(){
			public void run(Range range) {
				Book book = range.getBook();
				if(book.getSheet(name)!=null){
					// don't do it
					return;
				}
				//it is possible throw a exception if there is another sheet has same name
				range.createSheet(name);
			}});
	}

	/**
	 * Rename a sheet
	 * @param range the range to be applied
	 * @param newname the new name of the sheet, it must not same as another sheet name in this book
	 */
	public static void renameSheet(Range range, final String newname) {
		range.sync(new RangeRunner() {
			public void run(Range range) {
				if(range.getSheetName().equals(newname)){
					return;
				}
				
				Book book = range.getBook();
				if (book.getSheet(newname) != null) {
					// don't do it
					return;
				}
				range.setSheetName(newname);
			}
		});
	}

	/**
	 * Sets the sheet order
	 * @param range the range to be applied
	 * @param pos the sheet position
	 */
	public static void setSheetOrder(Range range, int pos) {
		range.setSheetOrder(pos);
	}

	/**
	 * Deletes the sheet, notice that it prevents you from deleting the last sheet
	 * @param range the range to be applied
	 */
	public static void deleteSheet(Range range) {
		range.sync(new RangeRunner() {
			public void run(Range range) {
				int num = range.getBook().getNumberOfSheets();
				if (num <= 1) {
					// don't do it
					return;
				}
				range.deleteSheet();
			}
		});

	}
	
	/**
	 * Copy the sheet with the naming pattern "ORIGINAL_FILENAME (n)". 
	 * @param range the range to be applied
	 */
	public static void CopySheet(Range range) {
		range.sync(new RangeRunner() {
			public void run(Range range) {
				String prefix = range.getSheetName();
				int num = 1;
				String name = null;
				
				Pattern pattern = Pattern.compile("(.*) \\(([0-9]+)\\)$");
				Matcher matcher = pattern.matcher(prefix);
				if(matcher.find()) {
					prefix = matcher.group(1);
					num = Integer.parseInt(matcher.group(2));
				}

				for(int i = 0, length = range.getBook().getNumberOfSheets(); i <= length; i++) {
					String n = prefix + " (" + ++num + ")";
					if(range.getBook().getSheet(n) == null) {
						name = n;
						break;
					}
				}
				
				range.cloneSheet(name);
			}
		});

	}

	/**
	 * return a {@link SheetAnchor} based on a cell and a picture width and height
	 * @param sheet target sheet where the anchor locates
	 * @param row 0-based row index
	 * @param column 0-base column index
	 * @param widthPx a picture's width in pixel
	 * @param heightPx a picture's height in pixel
	 * @return a {@link SheetAnchor}
	 */
	public static SheetAnchor toFilledAnchor(Sheet sheet,int row, int column, int widthPx, int heightPx){
		int lRow = 0;
		int lColumn = 0;
		int lX = 0;
		int lY = 0;
		
		SSheet ws = ((SheetImpl)sheet).getNative();
		for(int i = column;;i++){
			if(ws.getColumn(i).isHidden()){
				continue;
			}
			int wPx = sheet.getColumnWidth(i);
			widthPx -= wPx;
			if(widthPx<=0){
				lColumn = i; // ZSS-476, shouldn't minus 1
				lX = wPx + widthPx;//offset
				break;
			}
		}
		
		for(int i = row;;i++){
			if(ws.getRow(i).isHidden()){
				continue;
			}
			
			int hPx = sheet.getRowHeight(i);
			heightPx -= hPx;
			if(heightPx<=0){
				lRow = i; // ZSS-476, shouldn't minus 1
				lY = hPx + heightPx;
				break;
			}
		}
		
		return new SheetAnchor(row,column,0,0,lRow,lColumn,lX,lY);
	}
}
