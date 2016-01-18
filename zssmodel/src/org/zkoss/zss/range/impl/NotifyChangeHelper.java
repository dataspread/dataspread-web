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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zkoss.util.logging.Log;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zss.model.*;
import org.zkoss.zss.model.impl.AbstractBookAdv;
import org.zkoss.zss.model.impl.CellAttribute;
/**
 * A helper class that encapsulate details of sending model events.
 * @author Dennis
 * @since 3.5.0
 */
public class NotifyChangeHelper{

	private static final Log _logger = Log.lookup(NotifyChangeHelper.class.getName());
	
	public void notifyRowColumnSizeChange(HashSet<SheetRegion> notifySet) {
		for (SheetRegion notify : notifySet) {
			notifyRowColumnSizeChange(notify);
		}
	}

	public void notifyRowColumnSizeChange(SheetRegion notify) {
		((AbstractBookAdv) notify.getSheet().getBook()).sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_ROW_COLUMN_SIZE_CHANGE,
				notify.getSheet(),
				new CellRegion(notify.getRow(),notify.getColumn(),notify.getLastRow(),notify.getLastColumn())));
	}
	
	
	public void notifySheetAutoFilterChange(SSheet sheet, STable table) { //ZSS-988
		final Map data = new HashMap(2);
		data.put("TABLE", table); //ZSS-988: carry table information along (could be null)
		//ZSS-1083(refix ZSS-838): Carry the affecedRowCount to ModelEvent handler
		//  Spreadsheet.java#updateAutoFilter from AutoFilterHelper.java#enableAutoFilter0
		//  so Spreadsheet.java#updateAutoFilter can optimize the smartUpdate
		final Execution exec = Executions.getCurrent();
		final String key = (table == null ? sheet.getId() : table.getName())+"_ZSS_AFFECTED_ROWS";
		if (exec != null) {
			if ((Boolean)exec.getAttribute("CONTAINS_"+key, false) != null) {
				Integer affectedRows = (Integer) exec.getAttribute(key, false);
				data.put(key, affectedRows);
				if (affectedRows.intValue() > 0) { // is last affected row; so clear
					exec.setAttribute("CONTAINS_"+key, null, false);
					exec.setAttribute(key, null, false);
				}
			}
		}
		((AbstractBookAdv) sheet.getBook())
				.sendModelEvent(ModelEvents.createModelEvent(
						ModelEvents.ON_AUTOFILTER_CHANGE, sheet, data));
	}

	public void notifySheetFreezeChange(SSheet sheet) {
		((AbstractBookAdv) sheet.getBook())
				.sendModelEvent(ModelEvents.createModelEvent(
						ModelEvents.ON_FREEZE_CHANGE, sheet));
	}
	
	public void notifySheetPictureAdd(SSheet sheet, String id){
		((AbstractBookAdv) sheet.getBook()).sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_PICTURE_ADD, 
				sheet, ModelEvents.createDataMap(ModelEvents.PARAM_OBJECT_ID, id)));
	}

	public void notifySheetPictureDelete(SSheet sheet, String id) {
		((AbstractBookAdv) sheet.getBook()).sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_PICTURE_DELETE, 
				sheet, ModelEvents.createDataMap(ModelEvents.PARAM_OBJECT_ID, id)));
	}

	public void notifySheetPictureMove(SSheet sheet, String id) {
		((AbstractBookAdv) sheet.getBook()).sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_PICTURE_UPDATE, 
				sheet, ModelEvents.createDataMap(ModelEvents.PARAM_OBJECT_ID, id)));
	}

	public void notifySheetChartAdd(SSheet sheet, String id){
		((AbstractBookAdv) sheet.getBook()).sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_CHART_ADD, 
				sheet, ModelEvents.createDataMap(ModelEvents.PARAM_OBJECT_ID, id)));
	}
	
	public void notifySheetChartDelete(SSheet sheet, String id){
		((AbstractBookAdv) sheet.getBook()).sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_CHART_DELETE, 
				sheet, ModelEvents.createDataMap(ModelEvents.PARAM_OBJECT_ID, id)));
	}
	
	public void notifySheetChartUpdate(SSheet sheet, String id){
		((AbstractBookAdv) sheet.getBook()).sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_CHART_UPDATE, 
				sheet, ModelEvents.createDataMap(ModelEvents.PARAM_OBJECT_ID, id)));
	}
	
	public void notifyMergeRemove(Set<SheetRegion> toRemove) {
		for(SheetRegion notify:toRemove){//remove the final remove list
			notifyMergeRemove(notify);
		}
	}
	public void notifyMergeRemove(SheetRegion notify) {
		SBook book = notify.getSheet().getBook();
		if(_logger.debugable()){
			_logger.debug("Notify remove merge "+notify.getReferenceString());
		}
		((AbstractBookAdv) book).sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_MERGE_DELETE,notify.getSheet(),
				notify.getRegion()));
	}
	
	public void notifyMergeAdd(Set<SheetRegion> toAdd) {
		for(SheetRegion notify:toAdd){
			notifyMergeAdd(notify);
		}
	}
	public void notifyMergeAdd(SheetRegion notify) {
		SBook book = notify.getSheet().getBook();
		if(_logger.debugable()){
			_logger.debug("Notify add merge "+notify.getReferenceString());
		}
		((AbstractBookAdv) book).sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_MERGE_ADD,notify.getSheet(),
				notify.getRegion()));
	}

	@Deprecated
	public void notifyCellChange(Set<SheetRegion> cellNotifySet) {
		notifyCellChange(cellNotifySet, CellAttribute.ALL);
	}
	@Deprecated
	public void notifyCellChange(SheetRegion notify) {
		notifyCellChange(notify, CellAttribute.ALL);
	}

	//ZSS-939
	//@since 3.8.0
	public void notifyCellChange(Set<SheetRegion> cellNotifySet, CellAttribute cellAttr) {
		for(SheetRegion notify:cellNotifySet){
			notifyCellChange(notify, cellAttr);
		}
	}
	//ZSS-939
	//@since 3.8.0
	public void notifyCellChange(SheetRegion notify, CellAttribute cellAttr) {
		SBook book = notify.getSheet().getBook();
		if(_logger.debugable()){
			_logger.debug("Notify cell change "+notify.getReferenceString()+" of attribute "+cellAttr);
		}
		final Map<String, Integer> attrMap = new HashMap<String, Integer>(2);
		attrMap.put("cellAttr", cellAttr.value);
		((AbstractBookAdv) book).sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_CELL_CONTENT_CHANGE,notify.getSheet(),
				notify.getRegion(), attrMap));
	}
	
	public void notifySheetDelete(SBook book,SSheet deletedSheet,int deletedIndex){
		if(_logger.debugable()){
			_logger.debug("Notify sheet delete "+deletedSheet.getSheetName()+":"+deletedIndex);
		}
		((AbstractBookAdv) book).sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_SHEET_DELETE,book,
				ModelEvents.createDataMap(ModelEvents.PARAM_SHEET,deletedSheet,ModelEvents.PARAM_INDEX,deletedIndex)));
	}
	
	public void notifySheetCreate(SSheet sheet){
		if(_logger.debugable()){
			_logger.debug("Notify sheet create "+sheet.getSheetName());
		}
		((AbstractBookAdv) sheet.getBook()).sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_SHEET_CREATE,sheet));
	}
	
	public void notifySheetNameChange(SSheet sheet,String oldName){
		if(_logger.debugable()){
			_logger.debug("Notify sheet name change "+oldName+" to "+sheet.getSheetName());
		}
		((AbstractBookAdv) sheet.getBook()).sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_SHEET_NAME_CHANGE,sheet,
				ModelEvents.createDataMap(ModelEvents.PARAM_OLD_NAME,oldName)));
	}
	
	public void notifySheetReorder(SSheet sheet,int oldIdx){
		if(_logger.debugable()){
			_logger.debug("Notify sheet reorder "+oldIdx+" to "+sheet.getBook().getSheetIndex(sheet));
		}
		((AbstractBookAdv) sheet.getBook()).sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_SHEET_ORDER_CHANGE,sheet,
				ModelEvents.createDataMap(ModelEvents.PARAM_OLD_INDEX,oldIdx)));
	}
	//ZSS-823
	public void notifySheetVisibleChange(SSheet sheet) {
		if(_logger.debugable()){
			_logger.debug("Notify sheet visibile change: " + sheet.getSheetName() +" to " + sheet.getSheetVisible());
		}
		((AbstractBookAdv) sheet.getBook()).sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_SHEET_VISIBLE_CHANGE,sheet));
	}

	public void notifyDataValidationChange(SSheet sheet, String validationId) {
		if(_logger.debugable()){
			_logger.debug("Notify data validation change"+validationId);
		}
		((AbstractBookAdv) sheet.getBook()).sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_DATA_VALIDATION_CONTENT_CHANGE,sheet,
				ModelEvents.createDataMap(ModelEvents.PARAM_OBJECT_ID,validationId)));
	}

	public void notifyChartChange(SSheet sheet, String chartId) {
		if(_logger.debugable()){
			_logger.debug("Notify chart change "+chartId);
		}
		((AbstractBookAdv) sheet.getBook()).sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_CHART_CONTENT_CHANGE,sheet,
				ModelEvents.createDataMap(ModelEvents.PARAM_OBJECT_ID,chartId)));
	}

	public void notifyCustomEvent(String customEventName, SSheet sheet,
			Object data) {
		if(_logger.debugable()){
			_logger.debug("Notify custom event "+customEventName+":"+data);
		}
		((AbstractBookAdv) sheet.getBook()).sendModelEvent(ModelEvents.createModelEvent(customEventName,sheet,
				ModelEvents.createDataMap(ModelEvents.PARAM_CUSTOM_DATA,data)));
	}

	public void notifyDisplayGridlines(SSheet sheet, boolean show) {
		if(_logger.debugable()){
			_logger.debug("Notify display gridlines "+show);
		}
		((AbstractBookAdv) sheet.getBook()).sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_DISPLAY_GRIDLINES_CHANGE,sheet,
				ModelEvents.createDataMap(ModelEvents.PARAM_ENABLED,show)));
	}
	
	public void notifyProtectSheet(SSheet sheet, boolean protect) {
		if(_logger.debugable()){
			_logger.debug("Notify protect sheet "+sheet.getSheetName()+":"+protect);
		}
		((AbstractBookAdv) sheet.getBook()).sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_PROTECT_SHEET_CHANGE,sheet,
				ModelEvents.createDataMap(ModelEvents.PARAM_ENABLED,protect)));
	}
	public void notifyInsertDelete(List<InsertDeleteUpdate> insertDeleteNofitySet) {
		// make sure they are orderly
		for(InsertDeleteUpdate update : insertDeleteNofitySet) {
			notifyInsertDelete(update);
		}
	}
	public void notifyInsertDelete(InsertDeleteUpdate update) {
		// create event, then fire it
		String eventName;
		CellRegion region;
		if(update.isRow()) {
			eventName = update.isInserted() ? ModelEvents.ON_ROW_INSERT : ModelEvents.ON_ROW_DELETE;
			region = new CellRegion(update.getIndex(), 0, update.getLastIndex(), 0);
		} else {
			eventName = update.isInserted() ? ModelEvents.ON_COLUMN_INSERT : ModelEvents.ON_COLUMN_DELETE;
			region = new CellRegion(0, update.getIndex(), 0, update.getLastIndex());
		}
		if(_logger.debugable()){
			_logger.debug("Notify InsertDelete "+eventName+":"+region.getReferenceString());
		}
		SSheet sheet = update.getSheet();
		ModelEvent event = ModelEvents.createModelEvent(eventName, sheet, region);
		((AbstractBookAdv)sheet.getBook()).sendModelEvent(event);
	}
	
	//ZSS-966: This might be useful when we do Name Manager
	public void notifyNameNameChange(SSheet sheet, SName name, String oldName){
		if(_logger.debugable()){
			_logger.debug("Notify Name's name change "+oldName+" to "+name.getName());
		}
		((AbstractBookAdv) name.getBook()).sendModelEvent(ModelEvents.createModelEvent(ModelEvents.ON_NAME_NAME_CHANGE, name.getBook(), sheet, 
				ModelEvents.createDataMap(ModelEvents.PARAM_OLD_NAME,oldName, ModelEvents.PARAM_NAME, name)));
	}
	

}
