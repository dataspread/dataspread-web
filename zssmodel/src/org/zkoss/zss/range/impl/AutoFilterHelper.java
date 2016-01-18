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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.zkoss.lang.Integers;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.InvalidModelOpException;
import org.zkoss.zss.model.SAutoFilter;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SRow;
import org.zkoss.zss.model.STable;
import org.zkoss.zss.model.SAutoFilter.FilterOp;
import org.zkoss.zss.model.SAutoFilter.NFilterColumn;
import org.zkoss.zss.model.impl.AbstractSheetAdv;
import org.zkoss.zss.range.SRange;
import org.zkoss.zss.range.SRanges;
import org.zkoss.zss.range.impl.DataRegionHelper.FilterRegionHelper;

/**
 * 
 * @author dennis
 *
 */
//these code if from XRangeImpl,XBookHelper and migrate to new model
/*package*/ class AutoFilterHelper extends RangeHelperBase{

	public AutoFilterHelper(SRange range){
		super(range);
	}
	
	public CellRegion findAutoFilterRegion() {
		return new DataRegionHelper(range).findAutoFilterDataRegion();
	}

	//ZSS-988
	public SAutoFilter enableTableFilter(STable table, final boolean enable){
		SAutoFilter filter = table.getAutoFilter();
		if(filter!=null && !enable){
			CellRegion region = filter.getRegion();
			SRange toUnhide = SRanges.range(sheet,region.getRow(),region.getColumn(),region.getLastRow(),region.getLastColumn()).getRows();
			//to show all hidden row in autofiler region when disable
			toUnhide.setHidden(false);
			table.deleteAutoFilter();
			filter = null;
		}else if(filter==null && enable){
			table.enableAutoFilter(enable);
			filter = table.getAutoFilter();
		}
		return filter;
	}
	
	//refer to #XRangeImpl#autoFilter
	public SAutoFilter enableAutoFilter(final boolean enable){
		SAutoFilter filter = sheet.getAutoFilter();
		if(filter!=null && !enable){
			CellRegion region = filter.getRegion();
			SRange toUnhide = SRanges.range(sheet,region.getRow(),region.getColumn(),region.getLastRow(),region.getLastColumn()).getRows();
			//to show all hidden row in autofiler region when disable
			toUnhide.setHidden(false);
			sheet.deleteAutoFilter();
			filter = null;
		}else if(filter==null && enable){
			CellRegion region = findAutoFilterRegion();
			if(region!=null){
				filter = sheet.createAutoFilter(region);
			}else{
				throw new InvalidModelOpException("can't find any data in range");
			}
		}
		return filter;
	}
	
	@Deprecated
	//refer to #XRangeImpl#autoFilter(int field, Object criteria1, int filterOp, Object criteria2, Boolean visibleDropDown) {
	public SAutoFilter enableAutoFilter(final int field, final FilterOp filterOp,
			final Object criteria1, final Object criteria2, final Boolean showButton) {
		STable table = ((AbstractSheetAdv)sheet).getTableByRowCol(getRow(), getColumn());
		return enableAutoFilter(table, field, filterOp, criteria1, criteria2, showButton);
	}
	
	//ZSS-988
	public SAutoFilter enableAutoFilter(STable table, final int field, final FilterOp filterOp,
			final Object criteria1, final Object criteria2, final Boolean showButton) {
		SAutoFilter filter = table == null ? sheet.getAutoFilter() : table.getAutoFilter();
		
		if(filter==null){
			//ZSS-988
			if (table != null) {
				table.enableAutoFilter(true);
				filter = table.getAutoFilter();
			} else {
				CellRegion region = new DataRegionHelper(range).findAutoFilterDataRegion();
				if(region!=null){
					filter = sheet.createAutoFilter(region);
				}else{
					throw new InvalidModelOpException("can't find any data in range");
				}
			}
		}
		enableAutoFilter0(table, filter, field, filterOp, criteria1, criteria2, showButton);
		return filter;
	}
	
	//ZSS-988
	private void enableAutoFilter0(STable table, SAutoFilter filter, final int field, final FilterOp filterOp,
			final Object criteria1, final Object criteria2, final Boolean showButton) {
		
		final NFilterColumn fc = filter.getFilterColumn(field-1,true);	
		fc.setProperties(filterOp, criteria1, criteria2, showButton);

		//update rows
		final CellRegion affectedArea = filter.getRegion();
		final int row1 = affectedArea.getRow();
		final int col1 = affectedArea.getColumn(); 
		final int col =  col1 + field - 1;
		final int row = row1 + 1;
		final int row2 = affectedArea.getLastRow();
		final Set cr1 = fc.getCriteria1();
		//ZSS-1083(refix ZSS-838): Collect affected rows first
		LinkedHashMap<Integer, Boolean> affectedRows = new LinkedHashMap<Integer, Boolean>(); 
//		final Set<Ref> all = new HashSet<Ref>();
		for (int r = row; r <= row2; ++r) {
			final SCell cell = sheet.getCell(r, col); 
			final String val = isBlank(cell) ? "=" : getFormattedText(cell); //"=" means blank!
			if (cr1 != null && !cr1.isEmpty() && !cr1.contains(val)) { //to be hidden
				final SRow rowobj = sheet.getRow(r);
				if (!rowobj.isHidden()) { //a non-hidden row
					//ZSS-1083(refix ZSS-838): Collect affected rows first 
//					SRanges.range(sheet,r,0).getRows().setHidden(true);
					affectedRows.put(r, true);
				}
			} else { //candidate to be shown (other FieldColumn might still hide this row!
				final SRow rowobj = sheet.getRow(r);
				if (rowobj.isHidden() && canUnhide(filter, fc, r, col1)) { //a hidden row and no other hidden filtering
					// ZSS-646: we don't care about the columns at all; use 0.
//					final int left = sheet.getStartCellIndex(r);
//					final int right = sheet.getEndCellIndex(r);
					//ZSS-1083(refix ZSS-838): Collect affected rows first
//					final SRange rng = SRanges.range(sheet,r,0,r,0);  
//					all.addAll(rng.getRefs());
//					rng.getRows().setHidden(false); //unhide row
					affectedRows.put(r, false);
					
//					rng.notifyChange(); //why? text overflow? ->  //BookHelper.notifyCellChanges(_sheet.getBook(), all); //unhidden row must reevaluate
				}
			}
		}
		
		//ZSS-1083(refix ZSS-838): Handle affected rows
		if (!affectedRows.isEmpty()) {
			final String key = (table == null ? sheet.getId() : table.getName())+"_ZSS_AFFECTED_ROWS"; 
			Executions.getCurrent().setAttribute("CONTAINS_"+key, true);
			int sz = affectedRows.size();
			int j  = 0;
			for (int r : affectedRows.keySet()) {
				//ZSS-838: flag only the last handled row so 
				//  Spreadsheet.java#updateAutoFilter can optimize the smartUpdate
				if (++j == sz) { 
					Executions.getCurrent().setAttribute(key, new Integer(sz));
				} else { // wait for last affected row
					Executions.getCurrent().setAttribute(key, Integers.ZERO);
				}
				SRanges.range(sheet,r,0).getRows().setHidden(affectedRows.get(r));
			}
		}
//		BookHelper.notifyCellChanges(_sheet.getBook(), all); //unhidden row must reevaluate
	}
	
	private boolean canUnhide(SAutoFilter af, NFilterColumn fc, int row, int col) {
		final Collection<NFilterColumn> fltcs = af.getFilterColumns();
		for(NFilterColumn fltc: fltcs) {
			if (fc.equals(fltc)) continue;
			if (shallHide(fltc, row, col)) { //any FilterColumn that shall hide the row
				return false;
			}
		}
		return true;
	}
	
	private boolean shallHide(NFilterColumn fc, int row, int col) {
		final SCell cell = sheet.getCell(row, col + fc.getIndex());
		final boolean blank = isBlank(cell); 
		final String val =  blank ? "=" : getFormattedText(cell); //"=" means blank!
		final Set critera1 = fc.getCriteria1();
		return critera1 != null && !critera1.isEmpty() && !critera1.contains(val);
	}

	@Deprecated
	//refer to XRangeImpl#showAllData
	public void resetAutoFilter() {
		//ZSS-988
		STable table = ((AbstractSheetAdv)sheet).getTableByRowCol(getRow(), getColumn());
		resetAutoFilter(table);
	}
	//ZSS-988: check if this filter ever filter out any rows; so it can do
	// resetAutoFilter() or reapplyAutoFilter()
	//@since 3.8.0
	private void validFiltered(SAutoFilter af) {
		if (af == null) { //no AutoFilter to apply 
			return;
		}
		final Collection<NFilterColumn> fcs = af.getFilterColumns();
		if (fcs == null)
			return;
		
		//ZSS-988: must contains filterColumn with criteria that can be cleared
		boolean hasCriteria1 = false;
		for(NFilterColumn fc : fcs) {
			final Set criteria1 = fc.getCriteria1();
			if (criteria1 != null && !criteria1.isEmpty()) {
				hasCriteria1 = true;
				break;
			}
		}
		if (!hasCriteria1) {
			throw new InvalidModelOpException("The filter is not applied any criteria"); 
		}
	}
	
	public void resetAutoFilter(STable table) {
		final SAutoFilter af = table == null ? sheet.getAutoFilter() : table.getAutoFilter();
		if (af == null) { //no AutoFilter to apply 
			return;
		}
		final CellRegion afrng = af.getRegion();
		final Collection<NFilterColumn> fcs = af.getFilterColumns();
		if (fcs == null)
			return;
		
		//ZSS-988: filterColumn been filtering with criteria that can be cleared
		validFiltered(af);
		
		for(NFilterColumn fc : fcs) {
			fc.setProperties(FilterOp.VALUES, null, null, null); //clear all filter
		}
		final int row1 = afrng.getRow();
		final int row = row1 + 1;
		final int row2 = afrng.getLastRow();
		final int col1 = afrng.getColumn();
		final int col2 = afrng.getLastColumn();
//		final Set<Ref> all = new HashSet<Ref>();
		for (int r = row; r <= row2; ++r) {
			final SRow rowobj = sheet.getRow(r);
			if (rowobj.isHidden()) { //a hidden row
				//ZSS-646, we don't care about columns, use 0.
				//final int left = sheet.getStartCellIndex(r);
				//final int right = sheet.getEndCellIndex(r);
				final SRange rng = SRanges.range(sheet,r,0,r,0); 
//				all.addAll(rng.getRefs());
				rng.getRows().setHidden(false); //unhide
			}
		}

//		BookHelper.notifyCellChanges(_sheet.getBook(), all); //unhidden row must reevaluate
		//update button
//		final XRangeImpl buttonChange = (XRangeImpl) XRanges.range(_sheet, row1, col1, row1, col2);
//		BookHelper.notifyBtnChanges(new HashSet<Ref>(buttonChange.getRefs()));
	}

	@Deprecated
	//refer to XRangeImpl#applyFilter
	public void applyAutoFilter() {
		//ZSS-988
		STable table = ((AbstractSheetAdv)sheet).getTableByRowCol(getRow(), getColumn());
		applyAutoFilter(table);
	}
	
	//ZSS-988
	//@since 3.8.0
	public void applyAutoFilter(STable table) {
		final SAutoFilter oldFilter = table == null ? sheet.getAutoFilter() : table.getAutoFilter();
		
		if (oldFilter==null) { //no criteria is applied
			return;
		}

		//ZSS-988: filterColumn been filtering with criteria that can be reapplied
		validFiltered(oldFilter);

		CellRegion region = oldFilter.getRegion();
		//copy filtering criteria
		int firstRow = region.getRow(); //first row is header
		int firstColumn = region.getColumn();
		//backup column data because getting from removed auto filter will cause XmlValueDisconnectedException
		
		//index,criteria1,op,criteria2,showVisible
		List<Object[]> originalFilteringColumns = new ArrayList<Object[]>();
		if (oldFilter.getFilterColumns() != null){ //has applied some criteria
			for (NFilterColumn filterColumn : oldFilter.getFilterColumns()){
				Object[] filterColumnData = new Object[5];
				filterColumnData[0] = filterColumn.getIndex()+1;
				filterColumnData[1] = filterColumn.getCriteria1().toArray(new String[0]);
				filterColumnData[2] = filterColumn.getOperator();
				filterColumnData[3] = filterColumn.getCriteria2();
				filterColumnData[4] = filterColumn.isShowButton();
				originalFilteringColumns.add(filterColumnData);
			}
		}
		
		SAutoFilter newFilter = null;
		//ZSS-988
		if (table != null) {
			enableTableFilter(table, false); // unhidden rows if any
			newFilter = enableTableFilter(table, true); // create a new filter
		} else {
			enableAutoFilter(false); //disable existing filter
			//re-define filtering range 
			CellRegion filteringRange = new FilterRegionHelper().findCurrentRegion(sheet, firstRow, firstColumn);
			if (filteringRange == null){ //Don't enable auto filter if there are all blank cells
				return;
			}else{
				//enable auto filter
				newFilter = sheet.createAutoFilter(filteringRange);
				//			BookHelper.notifyAutoFilterChange(getRefs().iterator().next(),true);
			}
		}
		
		//apply original criteria
		for (int nCol = 0 ; nCol < originalFilteringColumns.size(); nCol ++){
			Object[] oldFilterColumn =  originalFilteringColumns.get(nCol);
			
			int field = (Integer)oldFilterColumn[0];
			Object c1 = oldFilterColumn[1];
			FilterOp op = (FilterOp)oldFilterColumn[2];
			Object c2 = oldFilterColumn[3];
			boolean showBtn = (Boolean)oldFilterColumn[4];
			
			enableAutoFilter0(table, newFilter, field,op,c1,c2,showBtn); //ZSS-988
		}
	}

}
