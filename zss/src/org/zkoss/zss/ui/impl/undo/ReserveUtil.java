/* ReserveUtil.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/7/25, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
 */
package org.zkoss.zss.ui.impl.undo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.zkoss.zss.api.IllegalFormulaException;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.CellStyleHolder;
import org.zkoss.zss.model.SColumn;
import org.zkoss.zss.model.SHyperlink;
import org.zkoss.zss.model.SRow;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SCell.CellType;
import org.zkoss.zss.range.SRange;
import org.zkoss.zss.range.SRanges;

/**
 * 
 * @author dennis
 * 
 */
public class ReserveUtil {
	public static final int RESERVE_CONTENT = 1;
	public static final int RESERVE_STYLE = 2;
	public static final int RESERVE_MERGE = 4;
	public static final int RESERVE_ALL = RESERVE_CONTENT|RESERVE_STYLE|RESERVE_MERGE;
	public static ReservedResult reserve(SSheet sheet, int row, int column,
			int lastRow, int lastColumn, int reserveType) {

		boolean reserveContent = false;
		boolean reserveStyle = false;
		boolean reserveMerge = false;
		if((reserveType & RESERVE_CONTENT)!=0){
			reserveContent = true;
		}
		if((reserveType & RESERVE_STYLE)!=0){
			reserveStyle = true;
		}
		if((reserveType & RESERVE_MERGE)!=0){
			reserveMerge = true;
		}

		ReservedResult result = new ReservedResult(sheet,row,column,lastRow,lastColumn,reserveType);

		if(result.isWholeSheet()){
			reserveWholeSheet(result,reserveContent,reserveStyle);
		}else if(result.isWholeRow()){
			reserveWholeRow(result,reserveContent,reserveStyle);
		}else if(result.isWholeColumn()){
			reserveWholeColumn(result,reserveContent,reserveStyle);
		}else{
			reserveCell(result,reserveContent,reserveStyle);
		}
		
		if(reserveMerge){
			result.setMergeInfo(reserveMergeInfo(sheet,row,column,lastRow,lastColumn));
		}
		return result;
	}
	
	private static void reserveCell(ReservedResult result,boolean reserveContent, boolean reserveStyle) {
		SSheet sheet = result.getSheet();
		Map<Integer,ReservedRow> reservedRows = new TreeMap<Integer, ReservedRow>();
		for(int r = result.getRow();r<=result.getLastRow();r++){
			SRow row = sheet.getRow(r);
			if(row.isNull() && !reserveStyle){
				continue;
			}
			ReservedRow reservedRow = new ReservedRow(r);
			reservedRows.put(r, reservedRow);
			for(int c = result.getColumn();c<=result.getLastColumn();c++){
				SCell cell = sheet.getCell(r,c);
				if(cell.isNull() && !reserveStyle){
					continue;
				}
				ReservedCell rcell = new ReservedCell(c);
				reservedRow.addCell(rcell);
				
				if(reserveContent){
					ReservedCellContent content = ReservedCellContent.reserve(cell);
					rcell.setContent(content);
				}
				
				if(reserveStyle){
					SCellStyle style = cell.getCellStyle(true);
					rcell.setStyle(style);
				}				
			}
		}
		result.setRowsInfo(reservedRows);
	}

	private static void reserveWholeColumn(ReservedResult result,boolean reserveContent, boolean reserveStyle) {
		SSheet sheet = result.getSheet();
		Map<Integer,ReservedRow> reservedRows = new TreeMap<Integer, ReservedRow>();
		for(int r = result.getRow();r<=result.getLastRow();r++){
			SRow row = sheet.getRow(r);
			if(row.isNull() && !reserveStyle){
				continue;
			}
			ReservedRow reservedRow = new ReservedRow(r);
			reservedRows.put(r, reservedRow);
			
			if(reserveStyle){
				SCellStyle style = row.getCellStyle(true);
				reservedRow.setStyle(style);
				reservedRow.setCustomHeight(row.isCustomHeight());
				reservedRow.setHeight(row.getHeight());				
			}
			
			HashSet<Integer> cellProcessed = new HashSet<Integer>();
			
			Iterator<SCell> cellIter = sheet.getCellIterator(r);
			
			while(cellIter.hasNext()){
				SCell cell = cellIter.next();
				ReservedCell rcell = new ReservedCell(cell.getColumnIndex());
				reservedRow.addCell(rcell);
				
				if(reserveContent){
					ReservedCellContent content = ReservedCellContent.reserve(cell);
					rcell.setContent(content);
				}
				
				if(reserveStyle){
					SCellStyle style = cell.getCellStyle(true);
					rcell.setStyle(style);
				}
				
				cellProcessed.add(cell.getColumnIndex());
			}
			
			//has to reserve the style on the row/column across cell to avoid row/column style conflict on null cell
			if(reserveStyle){
				Iterator<SColumn> columns = sheet.getColumnIterator();
				while(columns.hasNext()){
					SColumn column = columns.next();
					if(cellProcessed.contains(column.getIndex())){
						continue;
					}
					if(column.getCellStyle(true)!=null){
						SCell cell = sheet.getCell(r, column.getIndex());
						ReservedCell rcell = new ReservedCell(cell.getColumnIndex());
						reservedRow.addCell(rcell);
						SCellStyle style = cell.getCellStyle(true);
						rcell.setStyle(style);
					}
				}
			}
			
			
		}
		result.setRowsInfo(reservedRows);
		
	}

	private static void reserveWholeRow(ReservedResult result,boolean reserveContent, boolean reserveStyle) {
		SSheet sheet = result.getSheet();
		
		Map<Integer,ReservedColumn> reservedColumns = new HashMap<Integer, ReservedColumn>();
		if(reserveStyle){
			for(int c=result.getColumn();c<=result.getLastColumn();c++){
				SColumn col = sheet.getColumn(c);

				ReservedColumn reservedColumn = new ReservedColumn(col.getIndex());
				reservedColumns.put(col.getIndex(), reservedColumn);
				
				SCellStyle style = col.getCellStyle(true);
				reservedColumn.setStyle(style);
				reservedColumn.setCustomWidth(col.isCustomWidth());
				reservedColumn.setWidth(col.getWidth());
			}
		}
		
		Map<Integer,ReservedRow> reservedRows = new TreeMap<Integer, ReservedRow>();
		
		Iterator<SRow> rowIter = sheet.getRowIterator();
		
		while(rowIter.hasNext()){
			SRow row = rowIter.next();
			int r = row.getIndex();
			ReservedRow reservedRow = new ReservedRow(r);
			reservedRows.put(r, reservedRow);
			
			for(int c=result.getColumn();c<=result.getLastColumn();c++){
				SCell cell = sheet.getCell(r,c);
				if(cell.isNull() && !reserveStyle){
					continue;
				}
				ReservedCell rcell = new ReservedCell(cell.getColumnIndex());
				reservedRow.addCell(rcell);
				
				if(reserveContent){
					ReservedCellContent content = ReservedCellContent.reserve(cell);
					rcell.setContent(content);
				}
				
				if(reserveStyle){
					SCellStyle style = cell.getCellStyle(true);
					rcell.setStyle(style);
				}				
			}
		}
		result.setRowsInfo(reservedRows);
		result.setColumnsInfo(reservedColumns);
	}

	private static void reserveWholeSheet(ReservedResult result,boolean reserveContent, boolean reserveStyle) {
		SSheet sheet = result.getSheet();
		
		Map<Integer,ReservedColumn> reservedColumns = new HashMap<Integer, ReservedColumn>();
		
		Iterator<SColumn> colIter = sheet.getColumnIterator();
		while(reserveStyle && colIter.hasNext()){
			SColumn col = colIter.next();
			ReservedColumn reservedColumn = new ReservedColumn(col.getIndex());
			reservedColumns.put(col.getIndex(), reservedColumn);
			
			SCellStyle style = col.getCellStyle(true);
			reservedColumn.setStyle(style);
			reservedColumn.setCustomWidth(col.isCustomWidth());
			reservedColumn.setWidth(col.getWidth());
		}
		
		Map<Integer,ReservedRow> reservedRows = new TreeMap<Integer, ReservedRow>();
		
		Iterator<SRow> rowIter = sheet.getRowIterator();
		
		while(rowIter.hasNext()){
			SRow row = rowIter.next();
			int r = row.getIndex();
			ReservedRow reservedRow = new ReservedRow(r);
			reservedRows.put(r, reservedRow);
			
			if(reserveStyle){
				SCellStyle style = row.getCellStyle(true);
				reservedRow.setStyle(style);
				reservedRow.setCustomHeight(row.isCustomHeight());
				reservedRow.setHeight(row.getHeight());
			}
			
			for(int c=result.getColumn();c<=result.getLastColumn();c++){
				SCell cell = sheet.getCell(r,c);
				if(cell.isNull() && !reserveStyle){
					continue;
				}
				ReservedCell rcell = new ReservedCell(cell.getColumnIndex());
				reservedRow.addCell(rcell);
				
				if(reserveContent){
					ReservedCellContent content = ReservedCellContent.reserve(cell);
					rcell.setContent(content);
				}
				
				if(reserveStyle){
					SCellStyle style = cell.getCellStyle(true);
					rcell.setStyle(style);
				}				
			}
		}
		result.setRowsInfo(reservedRows);
		result.setColumnsInfo(reservedColumns);
	}

	/**
	 * reserve the merge information that in the given range.
	 */
	public static CellRegion[] reserveMergeInfo(SSheet sheet, int row, int column,
			int lastRow, int lastColumn){
		ArrayList<CellRegion> array = new ArrayList<CellRegion>();
		
		CellRegion cur = new CellRegion(row,column,lastRow,lastColumn);
		array.addAll(sheet.getOverlapsMergedRegions(cur, false));
		return array.size()==0?null:array.toArray(new CellRegion[array.size()]);
	}

	public static class ReservedResult {
		private final int _reserveType;
		private final SSheet _sheet;
		private Map<Integer,ReservedRow> _rows = null;
		private Map<Integer,ReservedColumn> _columns = null;
		private CellRegion[] _mergeInfo;
		private final int _row, _column, _lastRow, _lastColumn;
		private boolean _wholeRow,_wholeColumn;
		

		public ReservedResult(SSheet sheet, int row, int column, int lastRow,
				int lastColumn, int reserveType) {
			_sheet = sheet;
			_row = row;
			_column = column;
			_lastRow = lastRow;
			_lastColumn = lastColumn;
			_reserveType = reserveType;
			_wholeRow = _row<=0 && _lastRow>=_sheet.getBook().getMaxRowIndex();
			_wholeColumn = _column<=0 && _lastColumn>=_sheet.getBook().getMaxColumnIndex(); 
		}
		
		
		public boolean isWholeSheet(){
			return isWholeRow()&&isWholeColumn();
		}

		public boolean isWholeRow() {
			return _wholeRow;
		}

		public boolean isWholeColumn() {
			return _wholeColumn;
		}		


		public SSheet getSheet() {
			return _sheet;
		}

		public int getRow() {
			return _row;
		}

		public int getColumn() {
			return _column;
		}

		public int getLastRow() {
			return _lastRow;
		}

		public int getLastColumn() {
			return _lastColumn;
		}
		
		public CellRegion[] getMergeInfo() {
			return _mergeInfo;
		}

		public void setMergeInfo(CellRegion[] mergeInfo) {
			this._mergeInfo = mergeInfo;
		}
		public void setRowsInfo(Map<Integer,ReservedRow> rows){
			_rows = rows;
		}
		public void setColumnsInfo(Map<Integer,ReservedColumn> columns){
			_columns = columns;
		}
		
		public Map<Integer,ReservedRow> getRows(){
			return _rows;
		}

		public void restore(){
			SRange tempRange;
			boolean reserveContent = false;
			boolean reserveStyle = false;
			boolean reserveMerge = false;
			if((_reserveType & RESERVE_CONTENT)!=0){
				reserveContent = true;
			}
			if((_reserveType & RESERVE_STYLE)!=0){
				reserveStyle = true;
			}
			if((_reserveType & RESERVE_MERGE)!=0){
				reserveMerge = true;
			}
			
			//clear current merge info, we will restore it later
			if(reserveMerge){
				//clear the merge first
				CellRegion[] curMergeInfo = reserveMergeInfo(_sheet,_row,_column,_lastRow,_lastColumn);
				if(curMergeInfo!=null){
					for(CellRegion rect:curMergeInfo){
						tempRange = SRanges.range(_sheet,rect);
						tempRange.unmerge();
					}
				}
			}
			
			//clear content, and we will restore it back later.
			//following, it just clear whole area , it waste time because of reservation area will fill back. 
			SRange targetRange = SRanges.range(_sheet,_row,_column,_lastRow,_lastColumn);
			if(reserveContent){
				targetRange.clearContents();
			}
			
			if(reserveStyle && (isWholeRow() || isWholeColumn())){
				targetRange.clearCellStyles();
			}
			
			//start to restore
			if(reserveStyle && isWholeRow() && _columns!=null){
				for(ReservedColumn rcol:_columns.values()){
					SColumn column = _sheet.getColumn(rcol.getIndex()); 
					column.setCellStyle(rcol.getStyle());
					column.setCustomWidth(rcol.isCustomWidth());
					column.setWidth(rcol.getWidth());
				}
			}
			
			if(_rows!=null){
				for(ReservedRow rrow:_rows.values()){
					if(reserveStyle && isWholeColumn()){
						SRow row = _sheet.getRow(rrow.getIndex()); 
						row.setCellStyle(rrow.getStyle());
						row.setCustomHeight(rrow.isCustomHegiht());
						row.setHeight(rrow.getHeight());
					}
					
					for(ReservedCell rcell:rrow.getReservedCells()){
						SCell cell = _sheet.getCell(rrow.getIndex(),rcell.getColumnIndex());
						if(reserveContent){
							tempRange = SRanges.range(_sheet,rrow.getIndex(),rcell.getColumnIndex());
							ReservedCellContent data = rcell.getContent();
							if(data!=null){
								data.apply(tempRange);
							}
						}					
						if(reserveStyle){
							SCellStyle style = rcell.getStyle();
							cell.setCellStyle(style);
						}
					}
				}
			}
			
			if(reserveStyle){
				targetRange.notifyChange();
			}
			
			//restore merge area
			if(reserveMerge){
				//restore merge 
				if(_mergeInfo!=null){				
					for(CellRegion rect:_mergeInfo){
						tempRange = SRanges.range(_sheet,rect);
						tempRange.merge(false);
					}
				}
			}

		}
	}
	
	public static class ReservedColumn {
		private int _index;
		private SCellStyle _style;
		private int _width;
		private boolean _customWidth;
		public ReservedColumn(int index){
			this._index = index;
		}
		public int getIndex(){
			return _index;
		}		
		public SCellStyle getStyle() {
			return _style;
		}
		public void setStyle(SCellStyle style) {
			this._style = style;
		}
		public int getWidth() {
			return _width;
		}
		public void setWidth(int width) {
			this._width = width;
		}
		public boolean isCustomWidth() {
			return _customWidth;
		}
		public void setCustomWidth(boolean custom) {
			this._customWidth = custom;
		}
		
	}
	
	public static class ReservedRow {
		private int _index;
		private Map<Integer,ReservedCell> cells;
		private SCellStyle _style;
		private int _height;
		private boolean _customHeight;
		public ReservedRow(int index){
			this._index = index;
		}
		
		public int getIndex(){
			return _index;
		}
		
		public void addCell(ReservedCell cell){
			if(cells==null){
				cells = new TreeMap<Integer, ReservedCell>();
			}
			cells.put(cell.getColumnIndex(), cell);
		}
		
		public Collection<ReservedCell> getReservedCells(){
			return cells==null?Collections.EMPTY_SET:cells.values();
		}
		public SCellStyle getStyle() {
			return _style;
		}
		public void setStyle(SCellStyle style) {
			this._style = style;
		}

		public int getHeight() {
			return _height;
		}

		public void setHeight(int height) {
			this._height = height;
		}

		public boolean isCustomHegiht() {
			return _customHeight;
		}

		public void setCustomHeight(boolean custom) {
			this._customHeight = custom;
		}
		
	}
	
	public static class ReservedCell {
		private SCellStyle _style;
		private ReservedCellContent _content;
		int _columnIdx;
		public ReservedCell(int columnIdx){
			this._columnIdx = columnIdx;
		}
		public int getColumnIndex(){
			return _columnIdx;
		}
		public ReservedCellContent getContent() {
			return _content;
		}
		public void setContent(ReservedCellContent content) {
			this._content = content;
		}
		public SCellStyle getStyle() {
			return _style;
		}
		public void setStyle(SCellStyle style) {
			this._style = style;
		}
		
	}
	
	public static class ReservedCellContent {

		CellType _type;
		String _editText;
		Object _value;
		SHyperlink _link;
		
		public ReservedCellContent(CellType type){
			this._type = type;
		}
		
		public void apply(SRange range){
			//range hyperlink api will change string value, so have to set before set value
			
			SCell cell = range.getSheet().getCell(range.getRow(), range.getColumn());
			if(_link!=null){
				cell.setupHyperlink(_link.getType(), _link.getAddress(), _link.getLabel());
			}
			
			//following code will notify cell (and it's dependency) change
			if(CellType.FORMULA.equals(_type)){
				try{
					range.setEditText(_editText);
				}catch(IllegalFormulaException x){};//eat in this mode
			}else{
				range.setValue(_value);
			}
			
			
		}
		
		public static ReservedCellContent reserve(SCell cell){
			CellType type = cell.getType();
			if(cell.isNull() || CellType.BLANK.equals(type)){
				return null;
			}
			
			ReservedCellContent cc = new ReservedCellContent(cell.getType());
			
			if(CellType.FORMULA.equals(type)){
				//for the formula case, we have to keep the formula string and set back to let dependency tracking work
				cc._editText = SRanges.range(cell.getSheet(),cell.getRowIndex(),cell.getColumnIndex()).getEditText();
			}else{
				cc._value = cell.getValue();
			}
			cc._link = cell.getHyperlink();
			
			//TODO handle other data someday(hyperlink, comment)
			return cc;
		}
	}
}
