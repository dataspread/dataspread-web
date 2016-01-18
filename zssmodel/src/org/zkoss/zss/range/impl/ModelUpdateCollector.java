/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.range.impl;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.STable;
import org.zkoss.zss.model.SheetRegion;
import org.zkoss.zss.model.impl.CellAttribute;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.range.impl.ModelUpdate.UpdateType;
/**
 * 
 * @author Dennis
 * @since 3.5.0
 */
public class ModelUpdateCollector {
	static ThreadLocal<ModelUpdateCollector> _current = new ThreadLocal<ModelUpdateCollector>();

	private List<ModelUpdate> _updates;

	public ModelUpdateCollector() {
	}

	public static ModelUpdateCollector setCurrent(ModelUpdateCollector ctx) {
		ModelUpdateCollector old = _current.get();
		_current.set(ctx);
		return old;
	}

	public static ModelUpdateCollector getCurrent() {
		return _current.get();
	}

	public void addModelUpdate(ModelUpdate mu) {
		if (_updates == null) {
			_updates = new LinkedList<ModelUpdate>();
		}
		_updates.add(mu);
	}

	public List<ModelUpdate> getModelUpdates() {
		
		if(_updates==null){
			return Collections.EMPTY_LIST;
		}
		return Collections.unmodifiableList(_updates);
	}
	
	private ModelUpdate getLast(){
		return (_updates==null||_updates.size()==0)?null:_updates.get(_updates.size()-1);
	}
	private void removeLast(){
		if(_updates!=null && _updates.size()>0){
			_updates.remove(_updates.size()-1);
		}
	}

	public void addRefs(Set<Ref> dependents) {
		ModelUpdate last = getLast();
		//optimal, merge refs, add to refs if it is just previous update
		if(last!=null){
			if(last.getType()==UpdateType.REFS){
				((Set)last.getData()).addAll(dependents);
				return;
			}else if(last.getType()==UpdateType.REF){
				 Set data = new LinkedHashSet();
				 data.add((Ref)last.getData());
				 data.addAll(dependents);
				 
				 removeLast();
				 
				 addModelUpdate(new ModelUpdate(UpdateType.REFS, data, CellAttribute.ALL)); //ZSS-939
				 return;
			}
		}
		addModelUpdate(new ModelUpdate(UpdateType.REFS, new LinkedHashSet(dependents), CellAttribute.ALL)); //ZSS-939
	}

	public void addRef(Ref ref) {
		ModelUpdate last = getLast();
		//optimal, merge refs, add to refs if it is just previous update
		if(last!=null){
			if(last.getType()==UpdateType.REFS){
				((Set)last.getData()).add(ref);
				return;
			}else if(last.getType()==UpdateType.REF){
				 Set<Ref> data = new LinkedHashSet<Ref>();
				 data.add((Ref)last.getData());
				 data.add(ref);
				 
				 removeLast();
				 
				 addModelUpdate(new ModelUpdate(UpdateType.REFS, data, CellAttribute.ALL)); //ZSS-939
				 return;
			}
		}
		addModelUpdate(new ModelUpdate(UpdateType.REF, ref, CellAttribute.ALL));
	}

	@Deprecated
	public void addCellUpdate(SSheet sheet, int row, int column, int lastRow,
			int lastColumn) {
		addCellUpdate(sheet, row, column, lastRow, lastColumn, CellAttribute.ALL);
	}

	//ZSS-939
	//@since 3.8.0
	public void addCellUpdate(SSheet sheet, int row, int column, int lastRow,
			int lastColumn, CellAttribute cellAttr) {
		ModelUpdate last = getLast();
		//optimal, check if it is in previous refs, ref
		if(last!=null){
			if(last.getType()==UpdateType.REFS){
				String bookName = sheet.getBook().getBookName();
				String sheetName = sheet.getSheetName();
				if(((Set)last.getData()).contains(new RefImpl(bookName,sheetName,row,column,lastRow,lastColumn))){
					//ignore if it is in previous refs
					return;
				}
			}else if(last.getType()==UpdateType.REF){
				String bookName = sheet.getBook().getBookName();
				String sheetName = sheet.getSheetName();
				if(((Ref)last.getData()).equals(new RefImpl(bookName,sheetName,row,column,lastRow,lastColumn))){
					//ignore if it is in previous refs
					return;
				}
			}else if(last.getType()==UpdateType.CELLS && last.getCellAttr()==cellAttr){ //ZSS-939
				SheetRegion data = new SheetRegion(sheet,row,column,lastRow,lastColumn);
				((Set<SheetRegion>)last.getData()).add(data); 
				return;
			}else if(last.getType()==UpdateType.CELL && last.getCellAttr()==cellAttr){ //ZSS-939
				Set<SheetRegion> data = new LinkedHashSet<SheetRegion>();
				data.add((SheetRegion)last.getData());
				data.add(new SheetRegion(sheet,row,column,lastRow,lastColumn));
				 
				removeLast();
				
				addModelUpdate(new ModelUpdate(UpdateType.CELLS, data, cellAttr)); //ZSS-939
			}
		}
		addModelUpdate(new ModelUpdate(UpdateType.CELL, new SheetRegion(sheet,
				row, column, lastRow, lastColumn), cellAttr)); //ZSS-939
	}

	public void addMergeChange(SSheet sheet, CellRegion original,
			CellRegion changeTo) {
		addModelUpdate(new ModelUpdate(UpdateType.MERGE, new MergeUpdate(sheet,
				original, changeTo), CellAttribute.ALL)); //ZSS-939
	}

	public void addInsertDeleteUpdate(SSheet sheet, boolean inserted,
			boolean isRow, int index, int lastIndex) {
		addModelUpdate(new ModelUpdate(
				UpdateType.INSERT_DELETE,
				new InsertDeleteUpdate(sheet, inserted, isRow, index, lastIndex), CellAttribute.ALL));//ZSS-939
	}

	//ZSS-988: delete old filter, shift row/col, add new filter
	//@since 3.8.0
	public void addAutoFilterUpdate(SSheet sheet, STable table) {
		addModelUpdate(new ModelUpdate(
				UpdateType.FILTER,
				new AutoFilterUpdate(sheet, table), CellAttribute.ALL));
	}
}
