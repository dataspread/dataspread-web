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
package org.zkoss.zss.model.impl;

import org.zkoss.zss.model.*;
import org.zkoss.zss.model.sys.dependency.DependencyTable;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.formula.DirtyManager;
import org.zkoss.zss.range.impl.ModelUpdateCollector;

import java.util.Set;

/**
 * 
 * @author Dennis
 * @since 3.5.0
 */
/*package*/ class ModelUpdateUtil {
	/*package*/ static void handlePrecedentUpdate(SBookSeries bookSeries, AbstractSheetAdv sheet, Ref precedent){
		handlePrecedentUpdate(bookSeries, sheet, precedent, true);
	}
	//ZSS-1047: (side-effect of ZSS-988 and ZSS-1007 which consider setHidden() of SUBTOTAL() function)
	// see ColumnArrayImpl#setHidden()
	/*package*/ static void handlePrecedentUpdate(SBookSeries bookSeries, AbstractSheetAdv sheet, Ref precedent, boolean includePrecedent){
		//clear formula cache (that reval the unexisted sheet before
		FormulaCacheCleaner clearer = FormulaCacheCleaner.getCurrent();
		ModelUpdateCollector collector = ModelUpdateCollector.getCurrent();
		Set<Ref> dependents = null; 
		//get table when collector and clearer is not ignored (in import case, we should ignore clear cache)
		//if(collector!=null || clearer!=null || bookSeries.isAutoFormulaCacheClean()){
        DependencyTable table = ((AbstractBookSeriesAdv)bookSeries).getDependencyTable();
        dependents = table.getDependents(precedent);
		//}
		//TODO: if dependents are from different sheets, increment the trxId of those sheet.
		// Sheets Seems a nice granularity to have the trxId.
		// If we have it on a book or global level might trigger to many lookups.

		//TODO get trxid for the action
		int trxId = sheet.getNewTrxId();

		if (includePrecedent) { //ZSS-1047
			addRefUpdate(precedent);
			if(!sheet.isSyncCalc())
				DirtyManager.dirtyManagerInstance.addDirtyRegion(precedent, trxId);
		}


		if (dependents != null && dependents.size() > 0) {
			if (sheet.isSyncCalc()) {
				// Cleaner is only required for sync.
				if (clearer != null) {
					clearer.clear(dependents);
				} else if (bookSeries.isAutoFormulaCacheClean()) {
					new FormulaCacheClearHelper(bookSeries).clear(dependents);
				}
			}

			if (collector != null) {
				collector.addRefs(dependents);
			}
			if(sheet.isSyncCalc())
			{
				//TODO assuming single sheet
				dependents.forEach(v -> sheet.getCell(v.getRow(), v.getColumn()).getValueSync());
			}
			else {
				dependents.forEach(v ->
						DirtyManager.dirtyManagerInstance.addDirtyRegion(v, trxId));
			}
		}
	}

	/*package*/ static void addRefUpdate(Ref ref) {
		ModelUpdateCollector collector = ModelUpdateCollector.getCurrent();
		if(collector!=null){
			collector.addRef(ref);
		}
	}
	@Deprecated
	/*package*/ static void addCellUpdate(SSheet sheet,SCell cell){
		addCellUpdate(sheet, cell, CellAttribute.ALL);
	}
	//ZSS-939
	//@since 3.8.0
	/*package*/ static void addCellUpdate(SSheet sheet,SCell cell, CellAttribute cellAttr){
		addCellUpdate(sheet,cell.getRowIndex(),cell.getColumnIndex(),cellAttr);
	}
	
	@Deprecated
	/*package*/ static void addCellUpdate(SSheet sheet,int row,int column){
		addCellUpdate(sheet,row,column, CellAttribute.ALL);
	}
	//ZSS-939
	//@since 3.8.0
	/*package*/ static void addCellUpdate(SSheet sheet,int row,int column, CellAttribute cellAttr){
		addCellUpdate(sheet,row,column,row,column, cellAttr);
	}
	
	@Deprecated
	/*package*/ static void addCellUpdate(SSheet sheet,int row,int column, int lastRow, int lastColumn){
		addCellUpdate(sheet, row, column, lastRow, lastColumn, CellAttribute.ALL);
	}
	//ZSS-939
	//@since 3.8.0
	/*package*/ static void addCellUpdate(SSheet sheet,int row,int column, int lastRow, int lastColumn, CellAttribute cellAttr){
		ModelUpdateCollector collector = ModelUpdateCollector.getCurrent();
		if(collector!=null){
			collector.addCellUpdate(sheet,row, column,lastRow,lastColumn, cellAttr);
		}
	}
	
	/*package*/ static void addMergeUpdate(SSheet sheet,CellRegion original,CellRegion changeTo){
		ModelUpdateCollector collector = ModelUpdateCollector.getCurrent();
		if(collector!=null){
			collector.addMergeChange(sheet,original,changeTo);
		}
	}

	/*package*/static void addInsertDeleteUpdate(SSheet sheet, boolean inserted, boolean isRow, int index, int lastIndex) {
		ModelUpdateCollector collector = ModelUpdateCollector.getCurrent();
		if(collector != null) {
			collector.addInsertDeleteUpdate(sheet, inserted, isRow, index, lastIndex);
		}
	}
	
	//ZSS-988: delete old filter, shift rows/cols, add new filter
	//@since 3.8.0
	/*package*/static void addAutoFilterUpdate(SSheet sheet, STable table) {
		ModelUpdateCollector collector = ModelUpdateCollector.getCurrent();
		if(collector != null) {
			collector.addAutoFilterUpdate(sheet, table);
		}
	}
}
