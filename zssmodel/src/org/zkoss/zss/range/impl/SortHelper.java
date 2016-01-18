/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2014/2/24 , Created by Hawk
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
 */
package org.zkoss.zss.range.impl;

import java.io.Serializable;
import java.lang.reflect.*;
import java.util.*;

import org.zkoss.zk.ui.UiException;
import org.zkoss.zss.model.*;
import org.zkoss.zss.model.SCell.CellType;
import org.zkoss.zss.model.impl.CellBuffer;
import org.zkoss.zss.model.impl.PasteCellHelper;
import org.zkoss.zss.model.sys.formula.*;
import org.zkoss.zss.range.*;
import org.zkoss.zss.range.SRange.SortDataOption;

/**
 * Manipulate cells according to sorting criteria and options.
 * If sorting regions contains blank cells, it will ignore it. It only sort those cells with data.
 * @author Hawk
 * @since 3.5.0
 */
//porting implementation from BookHelper.sort()
public class SortHelper extends RangeHelperBase {

	public static final int SORT_HEADER_NO  = 0;
	public static final int SORT_HEADER_YES = 1;
	static final Double ZERO = new Double(0);
	private CellRegion sortingRegion; //a region contains only data to sort without headers
	private List<CellRegion> mergedRegionBeforeSorting = new LinkedList<CellRegion>(); //merged regions which sorting region contains
	private List<CellRegion> mergedRegionAfterSorting = new LinkedList<CellRegion>(); //merged regions changed by sorting
	
	public SortHelper(SRange range) {
		super(range);
	}
	
	/**
	 * Major procedure for sorting a selected range:
	 * record merged region, sort, unmerge merged regions, restore merged region
	 * @param key1 the range that contains first keys to sort 
	 * @param descending1 the order to sort first key
	 * @param dataOption1 BookHelper.SORT_TEXT_AS_NUMBERS, BookHelper.SORT_NORMAL_DEFAULT
	 * @param key2
	 * @param descending2
	 * @param dataOption2
	 * @param key3
	 * @param descending3
	 * @param dataOption3
	 * @param header
	 * @param matchCase
	 * @param sortByRows sort by keys in rows, true means "left to right", and false means "top to bottom"
	 */
	public void sort(SRange key1, boolean descending1, SortDataOption dataOption1, SRange key2, boolean descending2, SortDataOption dataOption2, SRange key3,
			boolean descending3, SortDataOption dataOption3, int header, boolean matchCase, boolean sortByRows) {
		checkMergedRegions(sortByRows);
		sortingRegion = findSortingRegion(header, sortByRows);
		if (sortingRegion == null){ //invalid sorting region, do not sort
			return;
		}
		int keyCount = 0;
		if (key1 != null) {
			++keyCount;
			if (key2 != null) {
				++keyCount;
				if (key3 != null) {
					++keyCount;
				}
			}
		}
		if (keyCount == 0) {
			throw new UiException("Must specify at least the key1");
		}
		final SortDataOption[] dataOptions = new SortDataOption[keyCount];
		final boolean[] descs = new boolean[keyCount];
		final int[] keyIndexes = new int[keyCount];
		keyIndexes[0] = rangeToIndex(key1, sortByRows);
		descs[0] = descending1;
		dataOptions[0] = dataOption1;
		if (keyCount > 1) {
			keyIndexes[1] = rangeToIndex(key2, sortByRows);
			descs[1] = descending2;
			dataOptions[1] = dataOption2;
		}
		if (keyCount > 2) {
			keyIndexes[2] = rangeToIndex(key3, sortByRows);
			descs[2] = descending3;
			dataOptions[2] = dataOption3;
		}
		validateKeyIndexes(keyIndexes, sortByRows);
		
		//keep merged regions
		for(CellRegion r : sheet.getMergedRegions()){
			if (sortingRegion.contains(r)){
				mergedRegionBeforeSorting.add(r);
			}
		}
		//collect keys upon specified row (column) index
		final List<SortKey> sortKeys = collectKeys(sortByRows, keyCount, dataOptions, keyIndexes);
		
		Collections.sort(sortKeys, new KeyComparator(descs, matchCase));
		sheet.removeMergedRegion(sortingRegion, true);
		if (sortByRows){
			repositionColumns(sortKeys);
		}else{
			repositionRows(sortKeys);
		}
		// restore merged regions
		for (CellRegion r : mergedRegionAfterSorting){
			sheet.addMergedRegion(r);
		}
	}

	/**
	 * If merged regions inside selected range meets the following conditions, cancel sorting:
	 *  1. When sorting "top to bottom", selected range cannot contain any cell that is merged across rows. 
	 *  For "left to right", merged cells across columns are not allowed.
	 *  2. Selected range cannot have any cell that contains part of merged cells
	 */
	private void checkMergedRegions(boolean sortByRows) {
		//Merged cells should not exceed selected cells scope.
		CellRegion selection = new CellRegion(getRow(), getColumn(), getLastRow(), getLastColumn());
		for (CellRegion region: sheet.getMergedRegions()){
			if (selection.contains(region)){
				if (sortByRows){  //merged cells cannot across columns
					if (region.getColumn() != region.getLastColumn()){
						throw new InvalidModelOpException("Cannot sort a range that conains merged cells across columns.");
					}
				}else{ //merged cells cannot across rows
					if (region.getRow() != region.getLastRow()){
						throw new InvalidModelOpException("Cannot sort a range that conains merged cells across rows.");
					}
				}
			}else{
				if (selection.overlaps(region)){
					throw new InvalidModelOpException("Cannot sort a range that conains part of merged cells.");
				}
			}
		}
	}

	/**
	 * collect keys and original row (column) index upon user-specified key row (column) index for sorting
	 */
	private List<SortKey> collectKeys(boolean sortByRows, int keyCount,
			final SortDataOption[] dataOptions, final int[] keyIndexes) {
		List<SortKey> sortKeys = new ArrayList<SortKey>(sortByRows ? sortingRegion.getColumnCount() : sortingRegion.getRowCount());
		if (sortByRows) { //keyIndex contains row index
			for (int columnIndex = sortingRegion.getColumn(); columnIndex <= sortingRegion.getLastColumn(); ++columnIndex) {
				Object[] values = new Object[keyCount];
				for(int j = 0; j < keyCount; ++j) {
					values[j] = getCellValue(sheet.getCell(keyIndexes[j], columnIndex), dataOptions[j]);
				}
				SortKey sortKey = new SortKey(columnIndex, values);
				sortKeys.add(sortKey);
			}
		} else { //sortByColumn, default case, keyIndex contains column index
			for (int rowIndex = sortingRegion.getRow(); rowIndex <= sortingRegion.getLastRow(); ++rowIndex) {
				final SRow row = sheet.getRow(rowIndex);
				if (row.isNull()) {
					continue; //nothing to sort
				}
				final Object[] values = new Object[keyCount];
				for(int j = 0; j < keyCount; ++j) {
					values[j] = getCellValue(sheet.getCell(rowIndex, keyIndexes[j]), dataOptions[j]);
				}
				final SortKey sortKey = new SortKey(rowIndex, values);
				sortKeys.add(sortKey);
			}
		}
		return sortKeys;
	}
	

	/**
	 * find the region that only contains data to sort without headers, blank rows and columns.
	 * @return null for invalid sorting region
	 */
	private CellRegion findSortingRegion(int header, boolean sortByRows) {
		int row = getRow();
		int column = getColumn();
		int lastRow = getLastRow();
		int lastColumn = getLastColumn();
		if (header == SORT_HEADER_YES) {
			if (sortByRows){
				column++;
			}else{
				row++;
			}
		}
		//TODO users might intentionally want blank cells to be sorted, so ignore blanks cells might be incorrect
		//ignore blanks rows and columns
		row = Math.max(row, sheet.getStartRowIndex());
		lastRow = Math.min(lastRow, sheet.getEndRowIndex());
		if (sortByRows){
			int nonBlankColumn = range.getSheet().getBook().getMaxColumnIndex();
			int nonBlankLastColumn = 0;
			for (int index = row; index <= lastRow; ++index) {
				nonBlankColumn = Math.min(nonBlankColumn, sheet.getStartCellIndex(index));
				nonBlankLastColumn = Math.max(nonBlankLastColumn, sheet.getEndCellIndex(index));
			}
			//non-blank range cannot smaller than selection
			column = Math.max(column, nonBlankColumn);
			lastColumn = Math.min(lastColumn, nonBlankLastColumn);
		}
		
		if ((row > lastRow || column > lastColumn)
				|| (row < 0 || lastRow < 0 || column < 0 || lastColumn < 0)) {
			return null;
		}
		sortingRegion = new CellRegion(row, column, lastRow, lastColumn);
		return sortingRegion;
	}

	private int rangeToIndex(SRange range, boolean sortByRows) {
		return sortByRows ? range.getRow() : range.getColumn();
	}
	
	private void validateKeyIndexes(int[] keyIndexes, boolean sortByRows) {
		if (!sortByRows) {
			for(int j = keyIndexes.length - 1; j >= 0; --j) {
				final int keyIndex = keyIndexes[j]; 
				if (keyIndex < sortingRegion.getColumn() || keyIndex > sortingRegion.getLastColumn()) {
					throw new UiException("The given key is out of the sorting range: "+keyIndex);
				}
			}
		} else {
			for(int j = keyIndexes.length - 1; j >= 0; --j) {
				final int keyIndex = keyIndexes[j]; 
				if (keyIndex < sortingRegion.getRow() || keyIndex > sortingRegion.getLastRow()) {
					throw new UiException("The given key is out of the sorting range: "+keyIndex);
				}
			}
		}
	}
	
	/**
	 * Change order of cells in column-wise according to sorting result. We might move them left or right.
	 * @param sortKeys
	 */
	private void  repositionColumns(List<SortKey> sortKeys) {
		int cellCount = sortingRegion.getRowCount();
		int columnBufferIndex = 0;
		List<SortResult> sortResults = new ArrayList<SortHelper.SortResult>(sortKeys.size());
		//copy original columns in a buffer
		for(Iterator<SortKey> it = sortKeys.iterator(); it.hasNext();columnBufferIndex++) {
			SortKey sortKey = it.next();
			int oldColumnIndex = sortKey.getIndex();
			int newColumnIndex = sortingRegion.getColumn() + columnBufferIndex;
			CellBuffer[] cellBuffer = new CellBuffer[cellCount];
			for (int r=0 ; r < cellCount ; r++){
				cellBuffer[r] = CellBuffer.bufferAll(sheet.getCell(sortingRegion.getRow()+r, oldColumnIndex));
			}
			sortResults.add(new SortResult(oldColumnIndex, newColumnIndex, cellBuffer));
			// move merged regions upon new column index
			for (CellRegion r : mergedRegionBeforeSorting){
				if (r.getColumn() == oldColumnIndex){
					int columnOffset = newColumnIndex - oldColumnIndex;
					mergedRegionAfterSorting.add(new CellRegion(r.getRow(), r.getColumn()+columnOffset, r.getLastRow(), r.getLastColumn()+columnOffset));
				}
			}
		}
		//copy cells to sorted new index
		final PasteCellHelper pasteHelper = new PasteCellHelper(sheet);
		for (SortResult result : sortResults){
			final int offset = result.newIndex-result.oldIndex;
			if (offset == 0) continue;
			for (int r = 0 ; r < result.cellBuffer.length ; r++){
				// ZSS-693
				SCell cell = sheet.getCell(sortingRegion.getRow()+r, result.newIndex);
				pasteToNewCell(pasteHelper, result.cellBuffer[r], cell, null, 0,  offset);
			}
		}
	}
	

	/**
	 * Change order of cells in row-wise according to sorting result. We might move them up or down.
	 * @param sortKeys
	 * @return
	 */
	private void repositionRows(List<SortKey> sortKeys) {
		int cellCount = sortingRegion.getColumnCount();
		int rowBufferIndex = 0;
		List<SortResult> sortResults = new ArrayList<SortHelper.SortResult>(sortKeys.size());
		//copy original rows in a buffer
		for(Iterator<SortKey> it = sortKeys.iterator(); it.hasNext();rowBufferIndex++) {
			SortKey sortKey = it.next();
			int oldRowIndex = sortKey.getIndex();
			int newRowIndex = sortingRegion.getRow() + rowBufferIndex;
			CellBuffer[] cellBuffer = new CellBuffer[cellCount];
			for (int c=0 ; c < cellCount ; c++){
				cellBuffer[c] = CellBuffer.bufferAll(sheet.getCell(oldRowIndex, sortingRegion.getColumn()+c));
			}
			sortResults.add(new SortResult(oldRowIndex, newRowIndex, cellBuffer));
			// move merged regions upon new row index
			for (CellRegion r : mergedRegionBeforeSorting){
				if (r.getRow() == oldRowIndex){
					int rowOffset = newRowIndex - oldRowIndex;
					mergedRegionAfterSorting.add(new CellRegion(r.getRow()+rowOffset, r.getColumn(), r.getLastRow()+rowOffset, r.getLastColumn()));
				}
			}
		}
		//copy cells to sorted new index
		final PasteCellHelper pasteHelper = new PasteCellHelper(sheet);
		for (SortResult result : sortResults){
			final int offset = result.newIndex-result.oldIndex;
			if (offset == 0) continue;
			for (int c = 0 ; c < result.cellBuffer.length ; c++){
				final int col = sortingRegion.getColumn()+c;
				SCell cell = sheet.getCell(result.newIndex, col);
				pasteToNewCell(pasteHelper, result.cellBuffer[c], cell, null, offset, 0);
			}
		}
	}
	
	//ZSS-693
	private void pasteToNewCell(PasteCellHelper helper, CellBuffer buffer, 
			SCell cell, SheetRegion cutFrom, int rowOffset, int columnOffset) {
		if(buffer.isNull()){
			final int r = cell.getRowIndex();
			final int c = cell.getColumnIndex();
			cell.getSheet().clearCell(r, c, r, c);
		}else{
			helper.pasteValue(buffer,cell,cutFrom,
					buffer.getType() == CellType.FORMULA,rowOffset,
					columnOffset,false,0,0);
			buffer.applyStyle(cell);
			buffer.applyHyperlink(cell);
			buffer.applyComment(cell);
			//buffer.applyValidation(destCell); //20140618, henrichen: Sorting does not affect DataValidation
		}
	}
	
	/**
	 * Store index change and cell data after sorting
	 * @since 3.5.0
	 */
	class SortResult{
		int oldIndex;
		int newIndex;
		CellBuffer[] cellBuffer;
		
		SortResult(int oldIndex, int newIndex, CellBuffer[] cellBuffer){
			this.oldIndex = oldIndex;
			this.newIndex = newIndex;
			this.cellBuffer = cellBuffer;
		}
		
		@Override
		public String toString() {
			return oldIndex +" -> "+newIndex+"("+cellBuffer.length+" cells)";
		}
	}
	
	//convert cell value upon data option 
	private Object getCellValue(SCell cell, SortDataOption dataOption) {
		Object val = cell.getValue();
		if (val instanceof String && dataOption == SortDataOption.TEXT_AS_NUMBERS) {
			try {
				val = new Double((String)val);
			} catch(NumberFormatException ex) {
				val = ZERO;
			}
		}
		return val;
	}
	
	public static class SortKey {
		final private int _index; //original row/column index
		final private Object[] _values; //cell value
		public SortKey(int index, Object[] values) {
			this._index = index;
			this._values = values;
		}
		public int getIndex() {
			return _index;
		}
		public Object[] getValues() {
			return _values;
		}
	}
	
	private static class KeyComparator implements Comparator<SortKey>, Serializable {
		final private boolean[] _descs;
		final private boolean _matchCase;
//		final private int _sortMethod; //TODO byNumberOfStroks, byPinyYin
//		final private int _type; //TODO PivotTable only: byLabel, byValue
		
		public KeyComparator(boolean[] descs, boolean matchCase) {
			_descs = descs;
			_matchCase = matchCase;
		}
		@Override
		public int compare(SortKey o1, SortKey o2) {
			final Object[] values1 = o1.getValues();
			final Object[] values2 = o2.getValues();
			return compare(values1, values2);
		}

		private int compare(Object[] values1, Object[] values2) {
			final int len = values1.length;
			for(int j = 0; j < len; ++j) {
				int result = compareValue(values1[j], values2[j], _descs[j]);
				if (result != 0) {
					return result;
				}
			}
			return 0;
		}
		//1. null is always sorted at the end
		//2. Error(Byte) > Boolean > String > Double
		private int compareValue(Object val1, Object val2, boolean desc) {
			if (val1 == val2) {
				return 0;
			}
			final int order1 = val1 instanceof Byte ? 4 : val1 instanceof Boolean ? 3 : val1 instanceof String ? 2 : val1 instanceof Number ? 1 : desc ? 0 : 5;
			final int order2 = val2 instanceof Byte ? 4 : val2 instanceof Boolean ? 3 : val2 instanceof String ? 2 : val2 instanceof Number ? 1 : desc ? 0 : 5;
			int ret = 0;
			if (order1 != order2) {
				ret = order1 - order2;
			} else { //order1 == order2
				switch(order1) {
				case 4: //error, no order among different errors
					ret = 0;
					break;
				case 3: //Boolean
					ret = ((Boolean)val1).compareTo((Boolean)val2);
					break;
				case 2: //RichTextString
					ret = compareString(val1.toString(), val2.toString());
					break;
				case 1: //Double
					ret =((Double)val1).compareTo((Double)val2);
					break;
				default:
					throw new UiException("Unknown value type: "+val1.getClass());
				}
			}
			return desc ? -ret : ret;
		}
		private int compareString(String s1, String s2) {
			return _matchCase ? compareString0(s1, s2) : s1.compareToIgnoreCase(s2);
		}
		//bug 59 Sort with case sensitive should be in special spreadsheet order
		private int compareString0(String s1, String s2) {
			final int len1 = s1.length();
			final int len2 = s2.length();
			final int len = len1 > len2 ? len2 : len1;
			for (int j = 0; j < len; ++j) {
				final int ret = compareChar(s1.charAt(j), s2.charAt(j));
				if ( ret != 0) {
					return ret;
				}
			}
			return len1 - len2;
		}
		private int compareChar(char ch1, char ch2) {
			final char uch1 = Character.toUpperCase(ch1);
			final char uch2 = Character.toUpperCase(ch2);
			return uch1 == uch2 ? 
					(ch2 - ch1) : //yes, a < A
					(uch1 - uch2); //yes, a < b, a < B, A < b, and A < B
		}
	}
	
	private SCell getProxyInstance(SCell cell, SheetRegion srcRegion, int rowOffset, int columnOffset){
		return (SCell)Proxy.newProxyInstance(
				FormulaMovingCell.class.getClassLoader(),
				new Class[] { SCell.class },
				new FormulaMovingCell(cell, srcRegion, rowOffset, columnOffset));
	}
	
	/**
	 * Move the formula before setting to real cell.
	 * @author Hawk
	 * @since 3.5.0
	 */
	class FormulaMovingCell implements InvocationHandler{
		private final SCell proxiedCell;
		private final SheetRegion srcRegion;
		private final int rowOffset;
		private final int columnOffset;
		private final FormulaParseContext context;

		
		public FormulaMovingCell(SCell cell, SheetRegion srcRegion, int rowOffset, int columnOffset){
			this.proxiedCell = cell;
			this.srcRegion = srcRegion;
			this.rowOffset = rowOffset;
			this.columnOffset = columnOffset;
			context = new FormulaParseContext(proxiedCell, null);
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			
			if (method.getName().equals("setFormulaValue")){
				FormulaExpression fexpr = getFormulaEngine().parse(args[0].toString(), context);
				FormulaExpression movedFormula = getFormulaEngine().movePtgs(fexpr, srcRegion, rowOffset, columnOffset, context);
				proxiedCell.setFormulaValue(movedFormula.getFormulaString());
				return null;
			}
			return method.invoke(proxiedCell, args);
		}
	}
	

}
