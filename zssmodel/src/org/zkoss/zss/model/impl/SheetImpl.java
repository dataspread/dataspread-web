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
package org.zkoss.zss.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.zkoss.lang.Library;
import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.poi.ss.util.SheetUtil;
import org.zkoss.poi.ss.util.WorkbookUtil;
import org.zkoss.util.logging.Log;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.InvalidModelOpException;
import org.zkoss.zss.model.SAutoFilter;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SChart;
import org.zkoss.zss.model.SColumn;
import org.zkoss.zss.model.SColumnArray;
import org.zkoss.zss.model.SSheetProtection;
import org.zkoss.zss.model.SDataValidation;
import org.zkoss.zss.model.SPicture;
import org.zkoss.zss.model.SPrintSetup;
import org.zkoss.zss.model.SRow;
import org.zkoss.zss.model.SSheetViewInfo;
import org.zkoss.zss.model.STable;
import org.zkoss.zss.model.ViewAnchor;
import org.zkoss.zss.model.PasteOption;
import org.zkoss.zss.model.SheetRegion;
import org.zkoss.zss.model.SAutoFilter.FilterOp;
import org.zkoss.zss.model.SAutoFilter.NFilterColumn;
import org.zkoss.zss.model.SPicture.Format;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.dependency.DependencyTable;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.dependency.ObjectRef.ObjectType;
import org.zkoss.zss.model.sys.formula.FormulaClearContext;
import org.zkoss.zss.model.util.Validations;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public class SheetImpl extends AbstractSheetAdv {
	private static final long serialVersionUID = 1L;
	private static final Log _logger = Log.lookup(SheetImpl.class);
			
	private AbstractBookAdv _book;
	private String _name;
	private final String _id;
	
	private boolean _protected; //whether this sheet is protected
	private short _password; //hashed password
	
	private SAutoFilter _autoFilter;
	
	private SSheetProtection _sheetProtection;
	private SheetVisible _visible = SheetVisible.VISIBLE; //default value
	
	//ZSS-1063
	private String _hashValue;
	private String _spinCount;
	private String _algName;
	private String _saltValue;
	
	private final IndexPool<AbstractRowAdv> _rows = new IndexPool<AbstractRowAdv>(){
		private static final long serialVersionUID = 1L;
		@Override
		void resetIndex(int newidx, AbstractRowAdv obj) {
			obj.setIndex(newidx);
		}};
//	private final BiIndexPool<ColumnAdv> columns = new BiIndexPool<ColumnAdv>();
	private final ColumnArrayPool _columnArrays = new ColumnArrayPool();
	
	
	private final List<AbstractPictureAdv> _pictures = new LinkedList<AbstractPictureAdv>();
	private final List<AbstractChartAdv> _charts = new LinkedList<AbstractChartAdv>();
	private final List<AbstractDataValidationAdv> _dataValidations = new ArrayList<AbstractDataValidationAdv>();
	
	private final List<CellRegion> _mergedRegions = new LinkedList<CellRegion>();
	
	//to store some lowpriority view info
	private final SSheetViewInfo _viewInfo = new SheetViewInfoImpl();
	
	private final SPrintSetup _printSetup = new PrintSetupImpl();
	
	private HashMap<String,Object> _attributes;
	private int _defaultColumnWidth = 64; //in pixel
	private int _defaultRowHeight = 20;//in pixel
	
	//ZSS-855
	private final List<STable> _tables = new ArrayList<STable>();
	
	public SheetImpl(AbstractBookAdv book,String id){
		this._book = book;
		this._id = id;
	}
	
	protected void checkOwnership(SPicture picture){
		if(!_pictures.contains(picture)){
			throw new IllegalStateException("doesn't has ownership "+ picture);
		}
	}
	
	protected void checkOwnership(SChart chart){
		if(!_charts.contains(chart)){
			throw new IllegalStateException("doesn't has ownership "+ chart);
		}
	}
	
	protected void checkOwnership(SDataValidation validation){
		if(!_dataValidations.contains(validation)){
			throw new IllegalStateException("doesn't has ownership "+ validation);
		}
	}
	
	public SBook getBook() {
		checkOrphan();
		return _book;
	}

	public String getSheetName() {
		return _name;
	}

	public SRow getRow(int rowIdx) {
		return getRow(rowIdx,true);
	}
	@Override
	AbstractRowAdv getRow(int rowIdx, boolean proxy) {
		AbstractRowAdv rowObj = _rows.get(rowIdx);
		if(rowObj != null){
			return rowObj;
		}
		return proxy?new RowProxy(this,rowIdx):null;
	}
	@Override
	AbstractRowAdv getOrCreateRow(int rowIdx){
		AbstractRowAdv rowObj = _rows.get(rowIdx);
		if(rowObj == null){
			checkOrphan();
			if(rowIdx > getBook().getMaxRowIndex()){
				throw new IllegalStateException("can't create the row that exceeds max row size "+getBook().getMaxRowIndex());
			}
			rowObj = new RowImpl(this,rowIdx);
			_rows.put(rowIdx, rowObj);
		}
		return rowObj;
	}

	@Override
	public SColumn getColumn(int columnIdx) {
		return getColumn(columnIdx,true);
	}
	
	SColumn getColumn(int columnIdx, boolean proxy) {
		SColumnArray array = getColumnArray(columnIdx);
		if(array==null && !proxy){
			return null;
		}
		return new ColumnProxy(this,columnIdx);
	}
	@Override
	public SColumnArray getColumnArray(int columnIdx) {
		if(_columnArrays.hasLastKey(columnIdx)){
			return null;
		}
		SortedMap<Integer, AbstractColumnArrayAdv> submap = _columnArrays.lastSubMap(columnIdx);
		
		return submap.size()>0?submap.get(submap.firstKey()):null;
	}
//	@Override
//	ColumnAdv getColumn(int columnIdx, boolean proxy) {
//		ColumnAdv colObj = columns.get(columnIdx);
//		if(colObj != null){
//			return colObj;
//		}
//		return proxy?new ColumnProxy(this,columnIdx):null;
//	}
	
	/**internal use only for developing/test state, should remove when stable*/
	private static boolean COLUMN_ARRAY_CHECK = false;
	static{
		if("true".equalsIgnoreCase(Library.getProperty("org.zkoss.zss.model.internal.CollumnArrayCheck"))){
			COLUMN_ARRAY_CHECK = true;
		}
	}
	
	private void checkColumnArrayStatus(){
		if(!COLUMN_ARRAY_CHECK) //only check in dev 
			return;
		AbstractColumnArrayAdv prev = null;
		try{
			for(AbstractColumnArrayAdv array:_columnArrays.values()){
				//check the existed data
				if(prev==null){
					if(array.getIndex()!=0){
						throw new IllegalStateException("column array doesn't not start with 0 is "+array.getIndex());
					}
				}else{
					if(prev.getLastIndex()+1!=array.getIndex()){
						throw new IllegalStateException("column array doesn't continue, "+prev.getLastIndex() +" to "+array.getIndex());
					}
				}
				prev = array;
			}
		}catch(RuntimeException x){
			_logger.error(x.getMessage(),x);
			for(AbstractColumnArrayAdv array:_columnArrays.values()){
				_logger.info("ColumnArray "+array.getIndex()+":"+array.getLastIndex());
			}
			throw x;
		}
		
	}
	

	@Override
	public SColumnArray setupColumnArray(int index, int lastIndex) {
		if(index<0 && lastIndex > index){
			throw new IllegalArgumentException(index+","+lastIndex);
		}
		int start1,end1;
		start1 = end1 = -1;
		
		AbstractColumnArrayAdv ov = _columnArrays.overlap(index,lastIndex); 
		if(ov!=null){
			throw new IllegalStateException("Can't setup an overlapped column array "+index+","+lastIndex +" overlppaed "+ov);
		}
		
		
		if(_columnArrays.size()==0){
			start1 = 0;
		}else{
			start1 = _columnArrays.lastLastKey()+1;
		}
		end1 = index-1;
		
		AbstractColumnArrayAdv array;
		if(start1<=end1 && end1>-1){
			array = new ColumnArrayImpl(this, start1, end1);
			_columnArrays.put(array);
		}
		array = new ColumnArrayImpl(this, index, lastIndex);
		_columnArrays.put(array);
		
		checkColumnArrayStatus();
		return array;
	}

	
	@Override
	AbstractColumnArrayAdv getOrSplitColumnArray(int columnIdx){
		AbstractColumnArrayAdv contains = (AbstractColumnArrayAdv)getColumnArray(columnIdx);
		if(contains!=null && contains.getIndex()==columnIdx && contains.getLastIndex()==columnIdx){
			return contains;
		}
		
		if(columnIdx > getBook().getMaxColumnIndex()){
			throw new IllegalStateException("can't create the column array that exceeds max row size "+getBook().getMaxRowIndex());
		}
		
		int start1,end1,start2,end2;
		start1 = end1 = start2 = end2 = -1;
		
		if(contains==null){
			if(_columnArrays.size()==0){//no data
				start1 = 0;
			}else{//out of existed array
				start1 = _columnArrays.lastLastKey()+1;
			}
			end1 = columnIdx-1;
		}else{
			if(contains.getIndex()==columnIdx){//for the begin
				start2 = columnIdx+1;
				end2 = contains.getLastIndex();
			}else if(contains.getLastIndex()==columnIdx){//at the end
				start1 = contains.getIndex();
				end1 = columnIdx-1;
			}else{
				start1 = contains.getIndex();
				end1 = columnIdx-1;
				end2 = contains.getLastIndex();
				start2 = columnIdx+1;
			}
		}
		AbstractColumnArrayAdv array = null;
		AbstractColumnArrayAdv prev = null;
		if(contains!=null){
			_columnArrays.remove(contains);
		}
		//
		if(start2<=end2 && end2>-1){
			prev =new ColumnArrayImpl(this, start2, end2);
			_columnArrays.put(prev);
			if(contains!=null){
				prev.setCellStyle(contains.getCellStyle());
				prev.setHidden(contains.isHidden());
				prev.setWidth(contains.getWidth());
			}
		}
		
		array = new ColumnArrayImpl(this, columnIdx, columnIdx);
		_columnArrays.put(array);
		if(contains!=null){
			array.setCellStyle(contains.getCellStyle());
			array.setHidden(contains.isHidden());
			array.setWidth(contains.getWidth());
		}
		
		if(start1<=end1 && end1>-1){
			prev =new ColumnArrayImpl(this, start1, end1);
			_columnArrays.put(prev);
			if(contains!=null){
				prev.setCellStyle(contains.getCellStyle());
				prev.setHidden(contains.isHidden());
				prev.setWidth(contains.getWidth());
			}
		}
		
		checkColumnArrayStatus();
		return array;
	}
//	@Override
//	int getColumnIndex(ColumnAdv column){
//		return columns.get(column);
//	}

	@Override
	public SCell getCell(int rowIdx, int columnIdx) {
		return getCell(rowIdx,columnIdx,true);
	}
	@Override
	public SCell getCell(String cellRef) {
		CellRegion region = new CellRegion(cellRef);
		if(!region.isSingle()){
			throw new InvalidModelOpException("not a single ref "+cellRef);
		}
		return getCell(region.getRow(),region.getColumn(),true);
	}
	
	@Override
	AbstractCellAdv getCell(int rowIdx, int columnIdx, boolean proxy) {
		AbstractRowAdv rowObj = (AbstractRowAdv) getRow(rowIdx,false);
		if(rowObj!=null){
			return rowObj.getCell(columnIdx,proxy);
		}
		return proxy?new CellProxy(this, rowIdx,columnIdx):null;
	}
	@Override
	AbstractCellAdv getOrCreateCell(int rowIdx, int columnIdx){
		AbstractRowAdv rowObj = (AbstractRowAdv)getOrCreateRow(rowIdx);
		AbstractCellAdv cell = rowObj.getOrCreateCell(columnIdx);
		return cell;
	}

	public int getStartRowIndex() {
		return _rows.firstKey();
	}

	public int getEndRowIndex() {
		return _rows.lastKey();
	}
	
	public int getStartColumnIndex() {
		return _columnArrays.size()>0?_columnArrays.firstFirstKey():-1;
	}

	public int getEndColumnIndex() {
		return _columnArrays.size()>0?_columnArrays.lastLastKey():-1;
	}

	public int getStartCellIndex(int rowIdx) {
		int idx1 = -1;
		AbstractRowAdv rowObj = (AbstractRowAdv) getRow(rowIdx,false);
		if(rowObj!=null){
			idx1 = rowObj.getStartCellIndex();
		}
		return idx1;

	}

	public int getEndCellIndex(int rowIdx) {
		int idx1 = -1;
		AbstractRowAdv rowObj = (AbstractRowAdv) getRow(rowIdx,false);
		if(rowObj!=null){
			idx1 = rowObj.getEndCellIndex();
		}
		return idx1;
	}
	
	@Override
	void setSheetName(String name) {
		checkLegalSheetName(name);
		this._name = name;
	}
	

	private void checkLegalSheetName(String name) {
		try{
			WorkbookUtil.validateSheetName(name);
		}catch(IllegalArgumentException x){
			throw new InvalidModelOpException(x.getMessage());
		}catch(Exception x){
			throw new InvalidModelOpException("The sheet name "+name+" is not allowed");
		}
	}
	
//	public void clearRow(int rowIdx, int rowIdx2) {
//		int start = Math.min(rowIdx, rowIdx2);
//		int end = Math.max(rowIdx, rowIdx2);
//		
//		//clear before move relation
//		for(RowAdv row:rows.subValues(start,end)){
//			row.destroy();
//		}		
//		rows.clear(start,end);
//		
//		//Send event?
//		
//	}

//	public void clearColumn(int columnIdx, int columnIdx2) {
//		int start = Math.min(columnIdx, columnIdx2);
//		int end = Math.max(columnIdx, columnIdx2);
//		
//		
//		for(ColumnAdv column:columns.subValues(start,end)){
//			column.destroy();
//		}
//		columns.clear(start,end);
//		
//		for(RowAdv row:rows.values()){
//			row.clearCell(start,end);
//		}
//		//Send event?
//		
//	}

	@Override
	public void clearCell(CellRegion region) {
		this.clearCell(region.getRow(), region.getColumn(), region.getLastRow(), region.getLastColumn());
	}
	
	@Override
	public void clearCell(int rowIdx, int columnIdx, int rowIdx2,
			int columnIdx2) {
		int rowStart = Math.min(rowIdx, rowIdx2);
		int rowEnd = Math.max(rowIdx, rowIdx2);
		int columnStart = Math.min(columnIdx, columnIdx2);
		int columnEnd = Math.max(columnIdx, columnIdx2);
		
		Collection<AbstractRowAdv> effected = _rows.subValues(rowStart,rowEnd);
		for(AbstractRowAdv row:effected){
			row.clearCell(columnStart, columnEnd);
		}
	}

	@Override
	public void insertRow(int rowIdx, int lastRowIdx) {
		checkOrphan();
		if(rowIdx>lastRowIdx){
			throw new IllegalArgumentException(rowIdx+">"+lastRowIdx);
		}
		int size = lastRowIdx-rowIdx+1;
		_rows.insert(rowIdx, size);

		//destroy the row that exceed the max size
		int maxSize = getBook().getMaxRowSize();
		Collection<AbstractRowAdv> exceeds = new ArrayList<AbstractRowAdv>(_rows.subValues(maxSize, Integer.MAX_VALUE));
		if(exceeds.size()>0){
			_rows.trim(maxSize);
		}
		for(AbstractRowAdv row:exceeds){
			row.destroy();
		}
		//ZSS-619, should clear formula for entire effected region
		//here we clear whole sheet because of we don't have a efficient way to get the effected cell in the effected region
		//NOTE, in current formula-engine, it clears all formula cache in non-cell case.
		EngineFactory.getInstance().createFormulaEngine().clearCache(new FormulaClearContext(this));
		
		Map<String,Object> dataBefore = shiftBeforeRowInsert(rowIdx,lastRowIdx);
		ModelUpdateUtil.addInsertDeleteUpdate(this, true, true, rowIdx, lastRowIdx);
		shiftAfterRowInsert(dataBefore,rowIdx,lastRowIdx);
	}
	
	private Map<String, Object> shiftBeforeRowInsert(int rowIdx, int lastRowIdx) {
		Map<String,Object> dataBefore = new HashMap<String, Object>();
		// handling merged regions shift
		// find merge cells in affected region and remove them
		CellRegion affectedRegion = new CellRegion(rowIdx, 0, _book.getMaxRowIndex(), _book.getMaxColumnIndex());
		List<CellRegion> toExtend = getOverlapsMergedRegions(affectedRegion, true);
		List<CellRegion> toShift = getContainsMergedRegions(affectedRegion);
		removeMergedRegion(affectedRegion, true);
		dataBefore.put("toExtend", toExtend);
		dataBefore.put("toShift", toShift);
		return dataBefore;
	}

	private void shiftAfterRowInsert(Map<String, Object> dataBefore, int rowIdx, int lastRowIdx) {
		// handling pic location shift
		int size = lastRowIdx - rowIdx+1;
		for (AbstractPictureAdv pic : _pictures) {
			ViewAnchor anchor = pic.getAnchor();
			int idx = anchor.getRowIndex();
			if (idx >= rowIdx) {
				anchor.setRowIndex(idx + size);
			}
		}
		// handling chart location shift
		for (AbstractChartAdv chart : _charts) {
			ViewAnchor anchor = chart.getAnchor();
			int idx = anchor.getRowIndex();
			if (idx >= rowIdx) {
				anchor.setRowIndex(idx + size);
			}
		}
		
		// handling merged regions shift
		List<CellRegion> toExtend = (List<CellRegion>)dataBefore.get("toExtend");
		List<CellRegion> toShift = (List<CellRegion>)dataBefore.get("toShift");
		// extend/move removed merged cells, then add them back
		for(CellRegion r : toExtend) {
			addMergedRegion(new CellRegion(r.row, r.column, r.lastRow + size, r.lastColumn));
		}
		for(CellRegion r : toShift) {
			addMergedRegion(new CellRegion(r.row + size, r.column, r.lastRow + size, r.lastColumn));
		}
		
		//shift data validation will be done inside extendFormula
		extendFormula(new CellRegion(rowIdx,0,lastRowIdx,_book.getMaxColumnIndex()), false);
		
		//shift freeze panel
		int freezeIdx = _viewInfo.getNumOfRowFreeze()-1;
		if(freezeIdx>=rowIdx){
			if(freezeIdx<lastRowIdx){
				freezeIdx += freezeIdx-rowIdx + 1;
			}else{
				freezeIdx += lastRowIdx-rowIdx + 1;
			}
			_viewInfo.setNumOfRowFreeze(freezeIdx<0?0:freezeIdx+1);
		}
	}	

	@Override
	public void deleteRow(int rowIdx, int lastRowIdx) {
		checkOrphan();
		if(rowIdx>lastRowIdx){
			throw new IllegalArgumentException(rowIdx+">"+lastRowIdx);
		}
		
		//clear before move relation
		for(AbstractRowAdv row:_rows.subValues(rowIdx,lastRowIdx)){
			row.destroy();
		}
		int size = lastRowIdx-rowIdx+1;
		_rows.delete(rowIdx, size);
		
		//ZSS-619, should clear formula for entire effected region
		EngineFactory.getInstance().createFormulaEngine().clearCache(new FormulaClearContext(this));
		
		Map<String,Object> dataBefore = shiftBeforeRowDelete(rowIdx,lastRowIdx);
		ModelUpdateUtil.addInsertDeleteUpdate(this, false, true, rowIdx, lastRowIdx);
		shiftAfterRowDelete(dataBefore,rowIdx,lastRowIdx);
	}	
	
	private Map<String, Object> shiftBeforeRowDelete(int rowIdx, int lastRowIdx) {
		Map<String,Object> dataBefore = new HashMap<String,Object>();
		CellRegion affectedRegion = new CellRegion(rowIdx, 0, _book.getMaxRowIndex(), _book.getMaxColumnIndex());
		List<CellRegion> toShrink = getOverlapsMergedRegions(affectedRegion, false);
		removeMergedRegion(affectedRegion, true);
		dataBefore.put("toShrink", toShrink);
		return dataBefore;
	}

	private void shiftAfterRowDelete(Map<String, Object> dataBefore, int rowIdx, int lastRowIdx) {
		//handling pic shift
		int size = lastRowIdx - rowIdx+1;
		for(AbstractPictureAdv pic:_pictures){
			ViewAnchor anchor = pic.getAnchor();
			int idx = anchor.getRowIndex();
			if(idx >= rowIdx+size){
				anchor.setRowIndex(idx-size);
			}else if(idx >= rowIdx){
				anchor.setRowIndex(rowIdx);//as excel's rule
				anchor.setYOffset(0);
			}
		}
		//handling pic shift
		for(AbstractChartAdv chart:_charts){
			ViewAnchor anchor = chart.getAnchor();
			int idx = anchor.getRowIndex();
			if(idx >= rowIdx+size){
				anchor.setRowIndex(idx-size);
			}else if(idx >= rowIdx){
				anchor.setRowIndex(rowIdx);//as excel's rule
				anchor.setYOffset(0);
			}
		}
		
		// handling merged regions shift
		List<CellRegion> toShrink = (List<CellRegion>)dataBefore.get("toShrink");
		// shrink/move removed merged cells, then add them back
		for(CellRegion r : toShrink) {
			CellRegion shrank = shrinkRow(r, rowIdx, lastRowIdx);
			if(shrank != null) {
				addMergedRegion(shrank);
			}
		}

		//TODO shift data validation?
		
		shrinkFormula(new CellRegion(rowIdx,0,lastRowIdx,_book.getMaxColumnIndex()), false);
		
		//shift freeze panel
		int freezeIdx = _viewInfo.getNumOfRowFreeze()-1;
		if(freezeIdx>=rowIdx){
			if(freezeIdx<lastRowIdx){
				freezeIdx = rowIdx-1;
			}else{
				freezeIdx -= lastRowIdx-rowIdx + 1;
			}
			_viewInfo.setNumOfRowFreeze(freezeIdx<0?0:freezeIdx+1);
		}		
		
	}
	
	private CellRegion shrinkRow(CellRegion region, int row, int lastRow) {
		int[] shrank = shrinkIndexes(region.row, region.lastRow, row, lastRow);
		return shrank != null ? new CellRegion(shrank[0], region.column, shrank[1], region.lastColumn) : null;
	}
	
	private CellRegion shrinkColumn(CellRegion region, int column, int lastColumn) {
		int[] shrank = shrinkIndexes(region.column, region.lastColumn, column, lastColumn);
		return shrank != null ? new CellRegion(region.row, shrank[0], region.lastRow, shrank[1]) : null;
	}
	
	/**
	 * To shrink (and shift) line ab by given line cd, a~d are indexes.
	 * @return a shrank line, or null if deleted completely.
	 */
	private int[] shrinkIndexes(int a, int b, int c, int d) {
		if(a < 0 || b < 0 || c < 0 || d < 0 || a > b || c > d) {
			throw new IllegalArgumentException("indexes must be >= 0 and ascending");
		}

		// check every point at shrinking segment or after it
		// then move to corresponding position,
		// e.g b point at shrinking segment, so move it to c-1:
		//        c       d               c       d
		// ---+---+-x-+-x-+--- >>> ---+---+-x-+-x-+---  
		//    a       b               a  b 
		int delta = d - c + 1;
		if(a > d) {
			a -= delta;
		} else if(a >= c) {
			a = c; // as (d + 1) - delta
			a = (d + 1) - delta;
		}
		if(b > d) {
			b -= delta;
		} else if(b >= c) {
			b = c - 1; //
		}
		return (b >= a) ? new int[]{a, b} : null;
	}
	
	@Override
	public void insertCell(CellRegion region,boolean horizontal){
		insertCell(region.getRow(),region.getColumn(),region.getLastRow(),region.getLastColumn(),horizontal);
	}
	@Override
	public void insertCell(int rowIdx,int columnIdx,int lastRowIdx, int lastColumnIdx,boolean horizontal){
		checkOrphan();
		
		if(rowIdx>lastRowIdx){
			throw new IllegalArgumentException(rowIdx+">"+lastRowIdx);
		}
		if(columnIdx>lastColumnIdx){
			throw new IllegalArgumentException(columnIdx+">"+lastColumnIdx);
		}
		
		int columnSize = lastColumnIdx - columnIdx+1;
		int rowSize = lastRowIdx - rowIdx +1; 
		if(horizontal){
			Collection<AbstractRowAdv> effectedRows = _rows.subValues(rowIdx,lastRowIdx);
			for(AbstractRowAdv row:effectedRows){
				row.insertCell(columnIdx,columnSize);
			}
			
			//ZSS-619, should clear formula for entire effected region
			EngineFactory.getInstance().createFormulaEngine().clearCache(new FormulaClearContext(this));
			
			// notify affected region update
			ModelUpdateUtil.addCellUpdate(this, rowIdx, columnIdx, lastRowIdx, getBook().getMaxColumnIndex(), CellAttribute.ALL); //ZSS-939
		}else{ // vertical
			int maxSize = getBook().getMaxRowSize();
			Collection<AbstractRowAdv> effectedRows = _rows.descendingSubValues(rowIdx,Integer.MAX_VALUE);
			for(AbstractRowAdv row: new ArrayList<AbstractRowAdv>(effectedRows)){//to aovid concurrent modify
				//move the cell down to target row
				int idx = row.getIndex()+rowSize;
				if(idx >= maxSize){
					//clear the cell since it out of max
					row.clearCell(columnIdx,lastColumnIdx);
				}else{
					AbstractRowAdv target = getOrCreateRow(idx);
					row.moveCellTo(target,columnIdx,lastColumnIdx,0);
				}
			}
			
			//ZSS-619, should clear formula for entire effected region
			EngineFactory.getInstance().createFormulaEngine().clearCache(new FormulaClearContext(this));
			
			// notify affected region update
			ModelUpdateUtil.addCellUpdate(this, rowIdx, columnIdx, getBook().getMaxRowIndex(), lastColumnIdx, CellAttribute.ALL); //ZSS-939
		}
		
		shiftAfterCellInsert(rowIdx, columnIdx, lastRowIdx,lastColumnIdx,horizontal);
	}

	@Override
	public void deleteCell(CellRegion region,boolean horizontal){
		deleteCell(region.getRow(),region.getColumn(),region.getLastRow(),region.getLastColumn(),horizontal);
	}
	@Override
	public void deleteCell(int rowIdx,int columnIdx,int lastRowIdx, int lastColumnIdx,boolean horizontal){
		checkOrphan();
		if(rowIdx>lastRowIdx){
			throw new IllegalArgumentException(rowIdx+">"+lastRowIdx);
		}
		if(columnIdx>lastColumnIdx){
			throw new IllegalArgumentException(columnIdx+">"+lastColumnIdx);
		}
		
		int columnSize = lastColumnIdx - columnIdx+1;
		int rowSize = lastRowIdx - rowIdx +1; 
		
		if(horizontal){
			Collection<AbstractRowAdv> effected = _rows.subValues(rowIdx,lastRowIdx);
			for(AbstractRowAdv row:effected){
				row.deleteCell(columnIdx,columnSize);
			}
			
			//ZSS-619, should clear formula for entire effected region
			EngineFactory.getInstance().createFormulaEngine().clearCache(new FormulaClearContext(this));
			
			// notify affected region update
			ModelUpdateUtil.addCellUpdate(this, rowIdx, columnIdx, lastRowIdx, getBook().getMaxColumnIndex(), CellAttribute.ALL); //ZSS-939
		}else{ // vertical
			Collection<AbstractRowAdv> effectedRows = _rows.subValues(rowIdx,lastRowIdx);
			for(AbstractRowAdv row:effectedRows){
				row.clearCell(columnIdx,lastColumnIdx);
			}
			effectedRows = _rows.subValues(rowIdx+rowSize,Integer.MAX_VALUE);
			for(AbstractRowAdv row: new ArrayList<AbstractRowAdv>(effectedRows)){//to aovid concurrent modify
				//move the cell up
				AbstractRowAdv target = getOrCreateRow(row.getIndex()-rowSize);
				row.moveCellTo(target,columnIdx,lastColumnIdx,0);
			}
			
			//ZSS-619, should clear formula for entire effected region
			EngineFactory.getInstance().createFormulaEngine().clearCache(new FormulaClearContext(this));
			
			// notify affected region update
			ModelUpdateUtil.addCellUpdate(this, rowIdx, columnIdx, getBook().getMaxRowIndex(), lastColumnIdx, CellAttribute.ALL); //ZSS-939
		}
		
		shiftAfterCellDelete(rowIdx, columnIdx, lastRowIdx,lastColumnIdx,horizontal);
	}
	
	
	private void shiftAfterCellInsert(int rowIdx, int columnIdx, int lastRowIdx,
			int lastColumnIdx, boolean horizontal) {
		
		// handle merged cells
		// move merged cells only if they are contained in affected region
		// and unmerge others if they are overlapped with affected region 
		if(horizontal) {
			// find merge cells in affected region and remove them
			int size = lastColumnIdx - columnIdx + 1;
			CellRegion affectedRegion = new CellRegion(rowIdx, columnIdx, lastRowIdx, _book.getMaxColumnIndex());
			List<CellRegion> toShift = getContainsMergedRegions(affectedRegion);
			removeMergedRegion(affectedRegion, true); // including contained and overlapped
			for(CellRegion r : toShift) { // only add contained back, to simulate shifting
				addMergedRegion(new CellRegion(r.row, r.column + size, r.lastRow, r.lastColumn + size));
			}

		} else { // vertical
			
			// find merge cells in affected region and remove them
			int size = lastRowIdx - rowIdx + 1;
			CellRegion affectedRegion = new CellRegion(rowIdx, columnIdx, _book.getMaxRowIndex(), lastColumnIdx);
			List<CellRegion> toShift = getContainsMergedRegions(affectedRegion);
			removeMergedRegion(affectedRegion, true); // including contained and overlapped
			for(CellRegion r : toShift) { // only add contained back, to simulate shifting
				addMergedRegion(new CellRegion(r.row + size, r.column, r.lastRow + size, r.lastColumn));
			}
		}
		
		extendFormula(new CellRegion(rowIdx, columnIdx, lastRowIdx, lastColumnIdx),horizontal);
	}
	
	private void shiftAfterCellDelete(int rowIdx, int columnIdx, int lastRowIdx,
			int lastColumnIdx, boolean horizontal) {
		
		// handle merged cells
		// unmerge every merged cells overlapped with delete region
		removeMergedRegion(new CellRegion(rowIdx, columnIdx, lastRowIdx, lastColumnIdx), true);
		// move merged cells only if they are contained in affected region
		// and unmerge others if they are overlapped with affected region 
		if(horizontal) {
			// find merge cells in affected region and remove them
			int size = lastColumnIdx - columnIdx + 1;
			CellRegion affectedRegion = new CellRegion(rowIdx, lastColumnIdx, lastRowIdx, _book.getMaxColumnIndex());
			List<CellRegion> toShift = getContainsMergedRegions(affectedRegion);
			removeMergedRegion(affectedRegion, true); // including contained and overlapped
			for(CellRegion r : toShift) { // only add contained back, to simulate shifting
				addMergedRegion(new CellRegion(r.row, r.column - size, r.lastRow, r.lastColumn - size));
			}

		} else { // vertical
			
			// find merge cells in affected region and remove them
			int size = lastRowIdx - rowIdx + 1;
			CellRegion affectedRegion = new CellRegion(lastRowIdx, columnIdx, _book.getMaxRowIndex(), lastColumnIdx);
			List<CellRegion> toShift = getContainsMergedRegions(affectedRegion);
			removeMergedRegion(affectedRegion, true); // including contained and overlapped
			for(CellRegion r : toShift) { // only add contained back, to simulate shifting
				addMergedRegion(new CellRegion(r.row - size, r.column, r.lastRow - size, r.lastColumn));
			}
		}
		
		shrinkFormula(new CellRegion(rowIdx, columnIdx, lastRowIdx, lastColumnIdx),horizontal);
	}
	
	@Override
	void copyTo(AbstractSheetAdv sheet) {
		if(sheet==this)
			return;
		
		checkOrphan();
		sheet.checkOrphan();
		if(!getBook().equals(sheet.getBook())){
			throw new InvalidModelOpException("the source book is different");
		}
		
		//ZSS-688
		//can only clone on the begining.
		SheetImpl tgt = (SheetImpl) sheet;
		
		//_book
		//_name
		//_id
		
		//_protected
		tgt._protected = this._protected;
		//_password
		tgt._password = this._password;
		//_mergedRegions
		for (CellRegion rgn : this._mergedRegions) {
			tgt._mergedRegions.add(new CellRegion(rgn.row, rgn.column, rgn.lastRow, rgn.lastColumn));
		}
		//_autoFilter
		if (this._autoFilter != null) {
			tgt._autoFilter = ((AutoFilterImpl)this._autoFilter).cloneAutoFilterImpl();
			tgt.addIntoDependency(tgt._autoFilter);
		}
		//_sheetProtection
		if (this._sheetProtection != null) {
			tgt._sheetProtection =  ((SheetProtectionImpl)this._sheetProtection).cloneSheetProtectionImpl();
		}
		// _rows
		for (AbstractRowAdv srcrow : this._rows.values()) {
			AbstractRowAdv tgtrow = srcrow.cloneRow(tgt); 
			tgt._rows.put(tgtrow.getIndex(), tgtrow);
		}
		//_columnArrays
		for (AbstractColumnArrayAdv ca : this._columnArrays.values()) {
			tgt._columnArrays.put(((ColumnArrayImpl)ca).cloneColumnArrayImpl(tgt));
		}
		//_pictures
		for (AbstractPictureAdv pic : this._pictures) {
			tgt._pictures.add(((PictureImpl)pic).clonePictureImpl(tgt));
		}
		//_charts
		for (AbstractChartAdv chart : this._charts) {
			tgt._charts.add(((ChartImpl)chart).cloneChartImpl(tgt));
		}
		//_dataValidations
		for (AbstractDataValidationAdv dv : this._dataValidations) {
			tgt._dataValidations.add(((DataValidationImpl)dv).cloneDataValidationImpl(tgt));
		}
		//_viewInfo
		((SheetViewInfoImpl)tgt._viewInfo).copyFrom((SheetViewInfoImpl)this._viewInfo);
		//_printSetup
		((PrintSetupImpl)tgt._printSetup).copyFrom((PrintSetupImpl)this._printSetup);

		//Do not clone runtime internal attributes map
		//Map<String, Object> _attributes;
		
		//_defualtColumnWidth
		tgt._defaultColumnWidth = this._defaultColumnWidth;
		//_defaultRowHeight
		tgt._defaultRowHeight = this._defaultRowHeight;

		//ZSS-1063
		tgt._hashValue = this._hashValue;
		tgt._saltValue = this._saltValue;
		tgt._spinCount = this._spinCount;
		tgt._algName = this._algName;
	}

	public void dump(StringBuilder builder) {
		
		builder.append("'").append(getSheetName()).append("' {\n");
		
		int endColumn = getEndColumnIndex();
		int endRow = getEndRowIndex();
		builder.append("  ==Columns==\n\t");
		for(int i=0;i<=endColumn;i++){
			builder.append(CellReference.convertNumToColString(i)).append(":").append(i).append("\t");
		}
		builder.append("\n");
		builder.append("  ==Row=={");
		for(int i=0;i<=endRow;i++){
			builder.append("\n  ").append(i).append("\t");
			if(getRow(i).isNull()){
				builder.append("-*");
				continue;
			}
			int endCell = getEndCellIndex(i);
			for(int j=0;j<=endCell;j++){
				SCell cell = getCell(i, j);
				Object cellvalue = cell.isNull()?"-":cell.getValue();
				String str = cellvalue==null?"null":cellvalue.toString();
				if(str.length()>8){
					str = str.substring(0,8);
				}else{
					str = str+"\t";
				}
				
				builder.append(str);
			}
		}
		builder.append("\n}\n");
	}

	@Override
	public void insertColumn(int columnIdx, int lastColumnIdx) {
		checkOrphan();
		if(columnIdx>lastColumnIdx){
			throw new IllegalArgumentException(columnIdx+">"+lastColumnIdx);
		}

		int size = lastColumnIdx - columnIdx + 1;
		insertAndSplitColumnArray(columnIdx,size);
		
		for(AbstractRowAdv row:_rows.values()){
			row.insertCell(columnIdx,size);
		}
		
		//ZSS-619, should clear formula for entire effected region
		EngineFactory.getInstance().createFormulaEngine().clearCache(new FormulaClearContext(this));
		
		Map<String,Object> dataBefore = shiftBeforeColumnInsert(columnIdx,lastColumnIdx);
		ModelUpdateUtil.addInsertDeleteUpdate(this, true, false, columnIdx, lastColumnIdx);
		shiftAfterColumnInsert(dataBefore,columnIdx,lastColumnIdx);
	}
	
	private Map<String, Object> shiftBeforeColumnInsert(int columnIdx,
			int lastColumnIdx) {
		Map<String,Object> dataBefore = new HashMap<String, Object>();
		// handling merged regions shift
		// find merge cells in affected region and remove them
		CellRegion affectedRegion = new CellRegion(0, columnIdx, _book.getMaxRowIndex(), _book.getMaxColumnIndex());
		List<CellRegion> toExtend = getOverlapsMergedRegions(affectedRegion, true);
		List<CellRegion> toShift = getContainsMergedRegions(affectedRegion);
		removeMergedRegion(affectedRegion, true);
		dataBefore.put("toExtend", toExtend);
		dataBefore.put("toShift", toShift);
		return dataBefore;
	}

	private void shiftAfterColumnInsert(Map<String, Object> dataBefore, int columnIdx, int lastColumnIdx) {
		int size = lastColumnIdx - columnIdx+1;
		// handling pic shift
		for (AbstractPictureAdv pic : _pictures) {
			ViewAnchor anchor = pic.getAnchor();
			int idx = anchor.getColumnIndex();
			if (idx >= columnIdx) {
				anchor.setColumnIndex(idx + size);
			}
		}
		// handling chart shift
		for (AbstractChartAdv chart : _charts) {
			ViewAnchor anchor = chart.getAnchor();
			int idx = anchor.getColumnIndex();
			if (idx >= columnIdx) {
				anchor.setColumnIndex(idx + size);
			}
		}
		
		// handling merged regions shift
		List<CellRegion> toExtend = (List<CellRegion>)dataBefore.get("toExtend");
		List<CellRegion> toShift = (List<CellRegion>)dataBefore.get("toShift");
		// extend/move removed merged cells, then add them back
		for(CellRegion r : toExtend) {
			addMergedRegion(new CellRegion(r.row, r.column, r.lastRow, r.lastColumn + size));
		}
		for(CellRegion r : toShift) {
			addMergedRegion(new CellRegion(r.row, r.column + size, r.lastRow, r.lastColumn + size));
		}

		//TODO shift data validation?
		
		extendFormula(new CellRegion(0,columnIdx,_book.getMaxRowIndex(),lastColumnIdx), true);
		
		//shift freeze panel
		int freezeIdx = _viewInfo.getNumOfColumnFreeze()-1;
		if(freezeIdx>=columnIdx){
			if(freezeIdx<lastColumnIdx){
				freezeIdx += freezeIdx-columnIdx + 1;
			}else{
				freezeIdx += lastColumnIdx-columnIdx + 1;
			}
			_viewInfo.setNumOfColumnFreeze(freezeIdx<0?0:freezeIdx+1);
		}		
	}	
	
	private void insertAndSplitColumnArray(int columnIdx,int size){
				
		AbstractColumnArrayAdv contains = null;
		
		int start1,end1,start2,end2;
		start1 = end1 = start2 = end2 = -1;
		
		if(_columnArrays.hasLastKey(columnIdx)){//no data
			return;
		}
		
		List<AbstractColumnArrayAdv> shift = new LinkedList<AbstractColumnArrayAdv>();
		
		for(AbstractColumnArrayAdv array:_columnArrays.lastSubMap(columnIdx).values()){
			if(array.getIndex()<=columnIdx && array.getLastIndex()>=columnIdx){
				contains = array;
			}
			if(array.getIndex()>columnIdx){//shift the right side array
				shift.add(0,array);//revert it to avoid overlap key replace issue
			}
		}
		for(AbstractColumnArrayAdv array:shift){
			_columnArrays.remove(array);
			
			array.setIndex(array.getIndex()+size);
			array.setLastIndex(array.getLastIndex()+size);
			
			_columnArrays.put(array);
		}
		
		if(contains==null){//doesn't need to do anything
			return;//
		}else{
			if(contains.getIndex()==columnIdx){//from the begin
				start2 = columnIdx+size;
				end2 = contains.getLastIndex()+size;
			}else{//at the end and in the middle
				start1 = contains.getIndex();
				end1 = columnIdx-1;
				start2 = columnIdx+size;
				end2 = contains.getLastIndex()+size;
			}
		}
		
		AbstractColumnArrayAdv array = null;
		AbstractColumnArrayAdv prev = null;
		
		_columnArrays.remove(contains);
		//
		if(start2<=end2 && end2>-1){
			prev =new ColumnArrayImpl(this, start2, end2);
			_columnArrays.put(prev);
			if(contains!=null){
				prev.setCellStyle(contains.getCellStyle());
				prev.setHidden(contains.isHidden());
				prev.setWidth(contains.getWidth());
			}
		}
		
		array = new ColumnArrayImpl(this, columnIdx, columnIdx+size-1);
		_columnArrays.put(array);
		//don't need to copy the property from contains to new inserted array, keep it default.
		
		if(start1<=end1 && end1>-1){
			prev =new ColumnArrayImpl(this, start1, end1);
			_columnArrays.put(prev);
			if(contains!=null){
				prev.setCellStyle(contains.getCellStyle());
				prev.setHidden(contains.isHidden());
				prev.setWidth(contains.getWidth());
			}
		}
		
		//destroy the cell that exceeds the max size
		int maxSize = getBook().getMaxColumnSize();
		Collection<AbstractColumnArrayAdv> exceeds = new ArrayList<AbstractColumnArrayAdv>(_columnArrays.firstSubValues(maxSize, Integer.MAX_VALUE));
		if(exceeds.size()>0){
			_columnArrays.trim(maxSize);
		}
		for(AbstractColumnArrayAdv ca:exceeds){
			ca.destroy();
		}
		
		
		checkColumnArrayStatus();
	}

	@Override
	public void deleteColumn(int columnIdx, int lastColumnIdx) {
		checkOrphan();
		if(columnIdx>lastColumnIdx){
			throw new IllegalArgumentException(columnIdx+">"+lastColumnIdx);
		}
		int size = lastColumnIdx - columnIdx + 1;
		deleteAndShrinkColumnArray(columnIdx,size);
		
		for(AbstractRowAdv row:_rows.values()){
			row.deleteCell(columnIdx,size);
		}
		
		//ZSS-619, should clear formula for entire effected region
		EngineFactory.getInstance().createFormulaEngine().clearCache(new FormulaClearContext(this));
		
		Map<String,Object> dataBefore = shiftBeforeColumnDelete(columnIdx,lastColumnIdx);
		ModelUpdateUtil.addInsertDeleteUpdate(this, false, false, columnIdx, lastColumnIdx);
		shiftAfterColumnDelete(dataBefore,columnIdx,lastColumnIdx);
	}
	
	private Map<String,Object> shiftBeforeColumnDelete(int columnIdx,int lastColumnIdx) {
		Map<String,Object> dataBefore = new HashMap<String,Object>();
		
		// handling merged regions shift
		// find merge cells in affected region and remove them
		CellRegion affectedRegion = new CellRegion(0, columnIdx, _book.getMaxRowIndex(), _book.getMaxColumnIndex());
		List<CellRegion> toShrink = getOverlapsMergedRegions(affectedRegion, false);
		removeMergedRegion(affectedRegion, true);
		dataBefore.put("toShrink", toShrink);
		
		return dataBefore;
	}
	private void shiftAfterColumnDelete(Map<String,Object> dataBefore, int columnIdx,int lastColumnIdx) {
		int size = lastColumnIdx - columnIdx+1;
		//handling pic shift
		for(AbstractPictureAdv pic:_pictures){
			ViewAnchor anchor = pic.getAnchor();
			int idx = anchor.getColumnIndex();
			if(idx >= columnIdx+size){
				anchor.setColumnIndex(idx-size);
			}else if(idx >= columnIdx){
				anchor.setColumnIndex(columnIdx);//as excel's rule
				anchor.setXOffset(0);
			}
		}
		//handling chart shift
		for(AbstractChartAdv chart:_charts){
			ViewAnchor anchor = chart.getAnchor();
			int idx = anchor.getColumnIndex();
			if(idx >= columnIdx+size){
				anchor.setColumnIndex(idx-size);
			}else if(idx >= columnIdx){
				anchor.setColumnIndex(columnIdx);//as excel's rule
				anchor.setXOffset(0);
			}
		}
		
		// handling merged regions shift
		List<CellRegion> toShrink = (List<CellRegion>)dataBefore.get("toShrink");
		// shrink/move removed merged cells, then add them back
		for(CellRegion r : toShrink) {
			CellRegion shrank = shrinkColumn(r, columnIdx, lastColumnIdx);
			if(shrank != null) {
				addMergedRegion(shrank);
			}
		}

		//TODO shift data validation?

		shrinkFormula(new CellRegion(0,columnIdx,_book.getMaxRowIndex(),lastColumnIdx), true);
		
		//shift freeze panel
		int freezeIdx = _viewInfo.getNumOfColumnFreeze()-1;
		if(freezeIdx>=columnIdx){
			if(freezeIdx<lastColumnIdx){
				freezeIdx = columnIdx-1;
			}else{
				freezeIdx -= lastColumnIdx-columnIdx + 1;
			}
			_viewInfo.setNumOfColumnFreeze(freezeIdx<0?0:freezeIdx+1);
		}				
	}	
	
	private void deleteAndShrinkColumnArray(int columnIdx,int size){

		if(_columnArrays.hasLastKey(columnIdx)){//no data
			return;
		}
		
		List<AbstractColumnArrayAdv> remove = new LinkedList<AbstractColumnArrayAdv>();
		List<AbstractColumnArrayAdv> contains = new LinkedList<AbstractColumnArrayAdv>();
		List<AbstractColumnArrayAdv> leftOver = new LinkedList<AbstractColumnArrayAdv>();
		List<AbstractColumnArrayAdv> rightOver = new LinkedList<AbstractColumnArrayAdv>();
		List<AbstractColumnArrayAdv> right = new LinkedList<AbstractColumnArrayAdv>();
		
		int lastColumnIdx = columnIdx+size-1;
		for(AbstractColumnArrayAdv array:_columnArrays.lastSubMap(columnIdx).values()){
			int arrIdx = array.getIndex();
			int arrLastIdx = array.getLastIndex();
			if(arrIdx<columnIdx && arrLastIdx > lastColumnIdx){//array large and contain delete column
				contains.add(array);
			}else if(arrIdx<columnIdx && arrLastIdx >= columnIdx){//overlap left side
				leftOver.add(array);
			}else if(arrIdx >= columnIdx && arrLastIdx <= lastColumnIdx){//contains
				remove.add(array);//remove entire
			}else if(arrIdx<=lastColumnIdx && arrLastIdx > lastColumnIdx){//overlap right side
				rightOver.add(array); 
			}else if(arrIdx>lastColumnIdx){//right side
				right.add(array); 
			}else{
				throw new IllegalStateException("wrong array state");
			}
			
		}
		for(AbstractColumnArrayAdv array:contains){
			_columnArrays.remove(array);
			array.setLastIndex(array.getLastIndex()-size);
			_columnArrays.put(array);
		}
		for(AbstractColumnArrayAdv array:leftOver){
			_columnArrays.remove(array);
			array.setLastIndex(columnIdx-1);//shrink trail
			_columnArrays.put(array);
		}
		for(AbstractColumnArrayAdv array:remove){
			_columnArrays.remove(array);
		}
		for(AbstractColumnArrayAdv array:rightOver){
			int arrIdx = array.getIndex();
			int arrLastIdx = array.getLastIndex();
			
			_columnArrays.remove(array);
			array.setIndex(columnIdx);//shrink head and move trail
			array.setLastIndex(columnIdx + arrLastIdx-lastColumnIdx -1); 
			_columnArrays.put(array);
		}
		for(AbstractColumnArrayAdv array:right){
			int arrIdx = array.getIndex();
			int arrLastIdx = array.getLastIndex();
			
			_columnArrays.remove(array);
			array.setIndex(arrIdx-size);//shrink head and move trail
			array.setLastIndex(arrLastIdx-size); 
			_columnArrays.put(array);
		}	

		checkColumnArrayStatus();
	}
	@Override
	public void moveCell(CellRegion region,int rowOffset, int columnOffset) {
		this.moveCell(region.getRow(), region.getColumn(), region.getLastRow(), region.getLastColumn(),rowOffset,columnOffset);
	}
	
	@Override
	public void moveCell(int rowIdx, int columnIdx,int lastRowIdx,int lastColumnIdx, int rowOffset, int columnOffset){
		if(rowOffset==0 && columnOffset==0)
			return;
		
		int maxRow = getBook().getMaxRowIndex();
		int maxCol = getBook().getMaxColumnIndex();
		
		if(rowIdx<0 || columnIdx<0 || 
				rowIdx > lastRowIdx || lastRowIdx > maxRow || columnIdx>lastColumnIdx || lastColumnIdx>maxCol){
			throw new InvalidModelOpException(new CellRegion(rowIdx,columnIdx,lastRowIdx,lastColumnIdx).getReferenceString()+" is illegal");
		}
		
		if(rowIdx+rowOffset<0 || columnIdx+columnOffset<0 || 
				lastRowIdx+rowOffset > maxRow|| lastColumnIdx+columnOffset > maxCol){
			throw new InvalidModelOpException(new CellRegion(rowIdx,columnIdx,lastRowIdx,lastColumnIdx).getReferenceString()+" can't move to offset "+rowOffset+","+columnOffset);
		}
		
		//TODO optimal for whole row, whole column
		
		//TODO zss 3.5 move to  movecellhelper
		//check merge overlaps and contains
		CellRegion srcRegion = new CellRegion(rowIdx,columnIdx,lastRowIdx,lastColumnIdx);
		Collection<CellRegion> containsMerge = getContainsMergedRegions(srcRegion);
		Collection<CellRegion> overlapsMerge = getOverlapsMergedRegions(srcRegion,true);
		if(overlapsMerge.size()>0){
			throw new InvalidModelOpException("Can't move "+srcRegion.getReferenceString()+" which overlaps merge area "+overlapsMerge.iterator().next().getReferenceString());
		}
		CellRegion targetRegion = new CellRegion(rowIdx+rowOffset,columnIdx+columnOffset,lastRowIdx+rowOffset,lastColumnIdx+columnOffset);
		//to backward compatible with old spec, we should auto ummerge the target area
//		overlapsMerge = getOverlapsMergedRegions(targetRegion,true); 
//		if(overlapsMerge.size()>0){
//			throw new InvalidateModelOpException("Can't move to "+targetRegion.getReferenceString()+" which overlaps merge area");
//		}
		this.removeMergedRegion(targetRegion, true);
		
		
		boolean reverseYDir = rowOffset>0;
		boolean reverseXDir = columnOffset>0;
		
		int rowStart = reverseYDir?lastRowIdx:rowIdx;
		int rowEnd = reverseYDir?rowIdx:lastRowIdx;
		int colStart = reverseXDir?lastColumnIdx:columnIdx;
		int colEnd = reverseXDir?columnIdx:lastColumnIdx;
		
		for(int r = rowStart; reverseYDir?r>=rowEnd:r<=rowEnd;){
			int tr = r+rowOffset;
			AbstractRowAdv row = getRow(r,false);
			for(int c = colStart; reverseXDir?c>=colEnd:c<=colEnd;){
				int tc = c+columnOffset;
				SCell cell = row==null?null:row.getCell(c, false);
				if(cell==null){ // no such cell, the clear the target cell
					clearCell(tr, tc, tr, tc);
				}else{
					AbstractRowAdv target = getOrCreateRow(tr);
					row.moveCellTo(target, c, c, columnOffset);
				}
				
				if(reverseXDir){
					c--;
				}else{
					c++;
				}				
			}
			if(reverseYDir){
				r--;
			}else{ 
				r++;
			}
		}
		
		//should use precedent update since the value might be changed and need to clear cache
		ModelUpdateUtil.handlePrecedentUpdate(getBook().getBookSeries(),
				new RefImpl(getBook().getBookName(), getSheetName(), rowIdx,
						columnIdx, lastRowIdx, lastColumnIdx));
		ModelUpdateUtil.handlePrecedentUpdate(getBook().getBookSeries(),
				new RefImpl(getBook().getBookName(), getSheetName(), rowIdx
						+ rowOffset, columnIdx + columnOffset, lastRowIdx
						+ rowOffset, lastColumnIdx + columnOffset));
		
		//shift the merge
		_mergedRegions.removeAll(containsMerge);
		for(CellRegion merge:containsMerge){
			CellRegion newMerge = new CellRegion(merge.getRow() + rowOffset,merge.getColumn()+ columnOffset,
					merge.getLastRow()+rowOffset,merge.getLastColumn()+columnOffset);
			_mergedRegions.add(newMerge);
			ModelUpdateUtil.addMergeUpdate(this,merge, newMerge);
		}
		
		shiftAfterCellMove(rowIdx, columnIdx,lastRowIdx,lastColumnIdx, rowOffset, columnOffset);
		
		//TODO validation and other stuff
		
		//TODO event
	}

	
	private void shiftAfterCellMove(int rowIdx, int columnIdx, int lastRowIdx,
			int lastColumnIdx, int rowOffset, int columnOffset) {
		moveFormula(new CellRegion(rowIdx,columnIdx,lastRowIdx,lastColumnIdx),rowOffset,columnOffset);
	}
	
	private void moveFormula(CellRegion src,int rowOffset,int columnOffset){
		SBook book = getBook();
		AbstractBookSeriesAdv bs = (AbstractBookSeriesAdv)book.getBookSeries();
		DependencyTable dt = bs.getDependencyTable();
		Set<Ref> dependents = dt.getDirectDependents(new RefImpl(book.getBookName(),getSheetName(),src.getRow(),src.getColumn(),src.getLastRow(),src.getLastColumn()));
		if(dependents.size()>0){
			FormulaTunerHelper tuner = new FormulaTunerHelper(bs);
			tuner.move(new SheetRegion(this,src),dependents,rowOffset,columnOffset);
		}
	}
	
	private void shrinkFormula(CellRegion src,boolean horizontal){
		SBook book = getBook();
		AbstractBookSeriesAdv bs = (AbstractBookSeriesAdv)book.getBookSeries();
		DependencyTable dt = bs.getDependencyTable();
		
		Ref ref = new RefImpl(book.getBookName(),getSheetName(),src.getRow(), src.getColumn(),
				horizontal?src.getLastRow():book.getMaxRowIndex(),horizontal?book.getMaxColumnIndex():src.getLastColumn());
		
		Set<Ref> dependents = dt.getDirectDependents(ref);
		if(dependents.size()>0){
			FormulaTunerHelper tuner = new FormulaTunerHelper(bs);
			tuner.shrink(new SheetRegion(this,src),dependents,horizontal);
		}
	}
	
	private void extendFormula(CellRegion src,boolean horizontal){
		SBook book = getBook();
		AbstractBookSeriesAdv bs = (AbstractBookSeriesAdv)book.getBookSeries();
		DependencyTable dt = bs.getDependencyTable();
		Ref ref = new RefImpl(book.getBookName(),getSheetName(),src.getRow(), src.getColumn(),
				horizontal?src.getLastRow():book.getMaxRowIndex(),horizontal?book.getMaxColumnIndex():src.getLastColumn());
		Set<Ref> dependents = dt.getDirectDependents(ref);
		if(dependents.size()>0){
			FormulaTunerHelper tuner = new FormulaTunerHelper(bs);
			tuner.extend(new SheetRegion(this,src),dependents,horizontal);
		}
	}

	public void checkOrphan(){
		if(_book==null){
			throw new IllegalStateException("doesn't connect to parent");
		}
	}
	@Override
	public void destroy(){
		checkOrphan();
		for(AbstractColumnArrayAdv column:_columnArrays.values()){
			column.destroy();
		}
		_columnArrays.clear();
		for(AbstractRowAdv row:_rows.values()){
			row.destroy();
		}
		_rows.clear();
		for(AbstractChartAdv chart:_charts){
			chart.destroy();
		}
		_charts.clear();
		for(AbstractPictureAdv picture:_pictures){
			picture.destroy();
		}
		_pictures.clear();
		for(AbstractDataValidationAdv validation:_dataValidations){
			validation.destroy();
		}
		_dataValidations.clear();
		
		_book = null;
		//TODO all 
		
	}

	public String getId() {
		return _id;
	}

	public SPicture addPicture(Format format, byte[] data,ViewAnchor anchor) {
		checkOrphan();
		AbstractPictureAdv pic = new PictureImpl(this,_book.nextObjId("pic"), format, data,anchor);
		_pictures.add(pic);
		return pic;
	}
	
	//ZSS-735
	//@since 3.6.0
	public SPicture addPicture(int picDataIndex, ViewAnchor anchor) {
		checkOrphan();
		AbstractPictureAdv pic = new PictureImpl(this,_book.nextObjId("pic"), picDataIndex,anchor);
		_pictures.add(pic);
		return pic;
	}

	
	public SPicture getPicture(String picid){
		for(SPicture pic:_pictures){
			if(pic.getId().equals(picid)){
				return pic;
			}
		}
		return null;
	}

	public void deletePicture(SPicture picture) {
		checkOrphan();
		checkOwnership(picture);
		((AbstractPictureAdv)picture).destroy();
		_pictures.remove(picture);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<SPicture> getPictures() {
		return Collections.unmodifiableList((List)_pictures);
	}
	
	
	@Override
	public int getNumOfPicture() {
		return _pictures.size();
	}

	@Override
	public SPicture getPicture(int idx) {
		return _pictures.get(idx);
	}
	
	@Override
	public SChart addChart(SChart.ChartType type,ViewAnchor anchor) {
		checkOrphan();
		AbstractChartAdv pic = new ChartImpl(this, _book.nextObjId("chart"), type, anchor);
		_charts.add(pic);
		return pic;
	}
	@Override
	public SChart getChart(String picid){
		for(SChart pic:_charts){
			if(pic.getId().equals(picid)){
				return pic;
			}
		}
		return null;
	}
	@Override
	public void deleteChart(SChart chart) {
		checkOrphan();
		checkOwnership(chart);
		((AbstractChartAdv)chart).destroy();
		_charts.remove(chart);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<SChart> getCharts() {
		return Collections.unmodifiableList((List)_charts);
	}

	@Override
	public int getNumOfChart() {
		return _charts.size();
	}

	@Override
	public SChart getChart(int idx) {
		return _charts.get(idx);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<CellRegion> getMergedRegions() {
		return Collections.unmodifiableList((List)_mergedRegions);
	}

	@Override
	public void removeMergedRegion(CellRegion region,boolean removeOverlaps) {
		for(CellRegion r:new ArrayList<CellRegion>(_mergedRegions)){
			if((removeOverlaps && region.overlaps(r)) || region.contains(r)){
				_mergedRegions.remove(r);
				ModelUpdateUtil.addMergeUpdate(this,r, null);
			}
		}
	}

	@Override
	public void addMergedRegion(CellRegion region) {
		Validations.argNotNull(region);
		if(region.isSingle()){
			return;
		}
		for(CellRegion r:_mergedRegions){
			if(r.overlaps(region)){
				throw new InvalidModelOpException("the region is overlapped "+r+":"+region);
			}
		}
		_mergedRegions.add(region);
		ModelUpdateUtil.addMergeUpdate(this,null, region);
	}

	@Override
	public List<CellRegion> getOverlapsMergedRegions(CellRegion region,boolean excludeContains){
		List<CellRegion> list =new LinkedList<CellRegion>(); 
		for(CellRegion r:_mergedRegions){
			if(excludeContains && region.contains(r))
				continue;
			if(r.overlaps(region)){
				list.add(r);
			}
		}
		return list;
	}	
	@Override
	public List<CellRegion> getContainsMergedRegions(CellRegion region) {
		List<CellRegion> list =new LinkedList<CellRegion>(); 
		for(CellRegion r:_mergedRegions){
			if(region.contains(r)){
				list.add(r);
			}
		}
		return list;
	}
	

	
	@Override
	public CellRegion getMergedRegion(String cellRef) {
		CellRegion region = new CellRegion(cellRef);
		if(!region.isSingle()){
			throw new InvalidModelOpException("not a single ref "+cellRef);
		}
		return getMergedRegion(region.getRow(),region.getColumn());
	}
	@Override
	public CellRegion getMergedRegion(int row, int column) {
		for(CellRegion r:_mergedRegions){
			if(r.contains(row, column)){
				return r;
			}
		}
		return null;
	}

	@Override
	public Object getAttribute(String name) {
		return _attributes==null?null:_attributes.get(name);
	}

	@Override
	public Object setAttribute(String name, Object value) {
		if(_attributes==null){
			_attributes = new HashMap<String, Object>();
		}
		return _attributes.put(name, value);
	}

	@Override
	public Map<String, Object> getAttributes() {
		return _attributes==null?Collections.EMPTY_MAP:Collections.unmodifiableMap(_attributes);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<SRow> getRowIterator() {
		return Collections.unmodifiableCollection((Collection)_rows.values()).iterator();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Iterator<SColumnArray> getColumnArrayIterator() {
		return Collections.unmodifiableCollection((Collection)_columnArrays.values()).iterator();
	}
	

	@Override
	public Iterator<SColumn> getColumnIterator() {
		return new Iterator<SColumn>(){
			int index = -1;
			@Override
			public boolean hasNext() {
				return getColumnArray(index+1)!=null;
			}

			@Override
			public SColumn next() {
				index++;
				return getColumn(index);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("readonly");
			}
		};
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Iterator<SCell> getCellIterator(int row) {
		return (Iterator)((AbstractRowAdv)getRow(row)).getCellIterator(false);
	}
	
	@Override
	public int getDefaultRowHeight() {
		return _defaultRowHeight;
	}

	@Override
	public int getDefaultColumnWidth() {
		return _defaultColumnWidth;
	}

	@Override
	public void setDefaultRowHeight(int height) {
		_defaultRowHeight = height;
	}

	@Override
	public void setDefaultColumnWidth(int width) {
		_defaultColumnWidth = width;
	}


	@Override
	public int getNumOfMergedRegion() {
		return _mergedRegions.size();
	}

	@Override
	public CellRegion getMergedRegion(int idx) {
		return _mergedRegions.get(idx);
	}

	@Override
	public boolean isProtected() {
		return _protected;
	}

	@Override
	public void setPassword(String password) {
		_protected = password != null;
		this._password = 
			password == null || password.isEmpty() ? 0 : WorkbookUtil.hashPassword(password);
		
		//ZSS-1063
		if (password != null && !password.isEmpty()) {
			this._algName = "SHA-512";
			this._saltValue = SheetUtil.base64Random16Bytes();
			this._spinCount = "100000";
			this._hashValue = SheetUtil.encryptPassword(password, this._algName, this._saltValue, Integer.parseInt(this._spinCount));
		} else {
			this._algName = null;
			this._saltValue = null;
			this._spinCount = null;
			this._hashValue = null;
		}
	}
	
	@Override
	public short getHashedPassword() {
		return this._password;
	}

	@Override
	public SSheetViewInfo getViewInfo(){
		return _viewInfo;
	}
	
	@Override
	public SPrintSetup getPrintSetup(){
		return _printSetup;
	}
	
	@Override
	public SDataValidation addDataValidation(CellRegion region) {
		return addDataValidation(region,null);
	}
	public SDataValidation addDataValidation(CellRegion region, SDataValidation src) {
		checkOrphan();
		Validations.argInstance(src, AbstractDataValidationAdv.class);
		AbstractDataValidationAdv validation = new DataValidationImpl(this, _book.nextObjId("valid"));
		_dataValidations.add(validation);
		if(src!=null){
			validation.copyFrom((AbstractDataValidationAdv)src);
		}
		if (region!= null) {
			validation.addRegion(region);
		}
		return validation;
	}
	@Override
	public SDataValidation getDataValidation(String validationid){
		for(SDataValidation validation:_dataValidations){
			if(validation.getId().equals(validationid)){
				return validation;
			}
		}
		return null;
	}
	@Override
	public void deleteDataValidation(SDataValidation validationid) {
		checkOrphan();
		checkOwnership(validationid);
		((AbstractDataValidationAdv)validationid).destroy();
		_dataValidations.remove(validationid);
	}
	
	@Override
	public void removeDataValidationRegion(CellRegion region) {
		deleteDataValidationRegion(region);
	}
	
	@Override
	public List<SDataValidation> deleteDataValidationRegion(CellRegion region) {
		List<SDataValidation> dels = new ArrayList<SDataValidation>();
		for (SDataValidation validation : getDataValidations()) {
			validation.removeRegion(region);
			if (validation.getRegions() == null) {
				dels.add(validation);
			}
		}
		for (SDataValidation validation : dels) {
			deleteDataValidation(validation);
		}
		return dels;
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<SDataValidation> getDataValidations() {
		return Collections.unmodifiableList((List)_dataValidations);
	}

	@Override
	public int getNumOfDataValidation() {
		return _dataValidations.size();
	}

	@Override
	public SDataValidation getDataValidation(int idx) {
		return _dataValidations.get(idx);
	}
	
	@Override
	public SDataValidation getDataValidation(int rowIdx,int columnIdx) {
		for(SDataValidation validation:_dataValidations){
			for (CellRegion regn : validation.getRegions()) {
				if(regn.contains(rowIdx, columnIdx)){
					return validation;
				}
			}
		}
		return null;
	}

	@Override
	public SAutoFilter getAutoFilter() {
		return _autoFilter;
	}

	@Override
	public SAutoFilter createAutoFilter(CellRegion region) {
		Validations.argNotNull(region);
		
		_autoFilter = new AutoFilterImpl(region);
		final int left = region.getColumn();
        final int top = region.getRow();
        final int right = region.getLastColumn();
        final int bottom = region.getLastRow();
        
		//refer from XSSFSheet impl
		//handle the showButton on merged cell
		for (CellRegion mrng:getMergedRegions()) {
			final int t = mrng.getRow();
	        final int b = mrng.getLastRow();
	        final int l = mrng.getColumn();
	        final int r = mrng.getLastColumn();
	        
	        if (t == top && l <= right && l >= left) { // to be add filter column to hide button
	        	for(int c = l; c < r; ++c) {
		        	final int colId = c - left; 
		        	final NFilterColumn col = _autoFilter.getFilterColumn(colId, true);
		        	col.setProperties(FilterOp.AND, null, null, false);
	        	}
	        }
		}
		
		//ZSS-555
		addIntoDependency(_autoFilter);
		
		return _autoFilter;
	}
	
	//ZSS-555: add into dependency table
	private void addIntoDependency(SAutoFilter filter) {
		SBook book = getBook();
		String bookName = book.getBookName();
		String sheetName = getSheetName();
		Ref dependent = new ObjectRefImpl(bookName, sheetName, "AUTO_FILTER", ObjectType.AUTO_FILTER);
		CellRegion rgn = filter.getRegion();
		final DependencyTable dt = 
			((AbstractBookSeriesAdv) book.getBookSeries()).getDependencyTable();
		// prepare a dummy CellRef to enforce AutoFilter reference dependency
		Ref dummy = new RefImpl(bookName, sheetName, 
				rgn.row, rgn.column, rgn.lastRow, rgn.lastColumn);
		dt.add(dependent, dummy);
		ModelUpdateUtil.addRefUpdate(dependent);
	}
	
	//ZSS-555: delete from dependency table
	private void deleteFromDependency() {
		SBook book = getBook();
		String bookName = book.getBookName();
		String sheetName = getSheetName();
		Ref dependent = new ObjectRefImpl(bookName, sheetName, "AUTO_FILTER", ObjectType.AUTO_FILTER);
		DependencyTable dt = 
			((AbstractBookSeriesAdv) book.getBookSeries()).getDependencyTable();
			
		dt.clearDependents(dependent);
	}

	@Override
	public void deleteAutoFilter() {
		deleteFromDependency();
		_autoFilter = null;
	}

	@Override
	public void clearAutoFilter() {
		_autoFilter = null;
	}


	@Override
	public CellRegion pasteCell(SheetRegion src, CellRegion dest, PasteOption option) {
		return new PasteCellHelper(this).pasteCell(src,dest,option);
	}
	
	@Override
	public SSheetProtection getSheetProtection() {
		if (_sheetProtection == null) {
			_sheetProtection = new SheetProtectionImpl();
		}
		return _sheetProtection;
	}

	@Override
	public void setHashedPassword(short hashpass) {
		_password = hashpass;
	}

	@Override
	public SheetVisible getSheetVisible() {
		return _visible;
	}

	@Override
	public void setSheetVisible(SheetVisible state) {
		_visible = state; 
	}

	//ZSS-855
	@Override
	public void addTable(STable table) {
		_tables.add(table);
	}

	//ZSS-855
	@Override
	public List<STable> getTables() {
		return Collections.unmodifiableList(_tables);
	}
	
	//ZSS-855
	@Override
	public void removeTable(String name) {
		for (Iterator<STable> it = _tables.iterator(); it.hasNext();) {
			final STable tb = it.next();
			if (name.equalsIgnoreCase(tb.getName())) {
				it.remove();
				break;
			}
		}
	}
	
	//ZSS-855
	@Override
	public STable getTableByRowCol(int rowIdx, int colIdx) {
		for (STable tb : _tables) {
			final SheetRegion srgn = tb.getAllRegion();
			final CellRegion rgn = srgn.getRegion();
			if (rgn.getColumn() <= colIdx && colIdx <= rgn.getLastColumn()
					&& rgn.getRow() <= rowIdx && rowIdx <= rgn.getLastRow()) {
				return tb; //found
			}
		}
		return null;
	}

	//ZSS-962
	@Override
	public boolean isHidden(int rowIdx, int colIdx) {
		SRow row = getRow(rowIdx);
		if (row.isHidden()) return true;
		
		SColumnArray colArray = getColumnArray(colIdx);
		//null means the column is in default width and status
		return colArray == null ? false : colArray.isHidden();
	}
	
	//ZSS-985
	@Override
	public void removeTables(Set<String> tableNames) {
		if (tableNames.isEmpty()) return;
		for (Iterator<STable> it = _tables.iterator(); it.hasNext();) {
			final STable tb = it.next();
			final String tbName = tb.getName().toUpperCase();
			if (tableNames.contains(tbName)) {
				it.remove();
				tableNames.remove(tbName);
				if (tableNames.isEmpty()) break;
			}
		}
	}
	
	//ZSS-1001
	@Override
	public void removeTable(STable table) {
		_tables.remove(table);
	}
	
	//ZSS-1001
	@Override
	public void clearTables() {
		_tables.clear();
	}

	//ZSS-1063
	@Override
	public void setHashValue(String hashValue) {
		_hashValue  = hashValue;
	}
	public String getHashValue() {
		return _hashValue;
	}

	//ZSS-1063
	@Override
	public void setSpinCount(String spinCount) {
		_spinCount = spinCount;
	}
	public String getSpinCount() {
		return  _spinCount;
	}

	//ZSS-1063
	@Override
	public void setSaltValue(String saltValue) {
		_saltValue = saltValue;
	}
	public String getSaltValue() {
		return _saltValue;
	}

	//ZSS-1063
	@Override
	public void setAlgName(String algName) {
		_algName = algName;
	}
	public String getAlgName() {
		return _algName;
	}
}
