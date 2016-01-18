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

import java.util.HashMap;
import java.util.Map;


/**
 * A collection of factory method that help you create {@link ModelEvent}.
 * @author dennis
 * @since 3.5.0
 */
public class ModelEvents {
	
	/*
	 * Common model event
	 */
	public static final String ON_CELL_CONTENT_CHANGE = "onCellChange";
	public static final String ON_CHART_CONTENT_CHANGE = "onChartContentChange";
	public static final String ON_DATA_VALIDATION_CONTENT_CHANGE = "onDataValidationContentChange";
	
	public static final String ON_ROW_COLUMN_SIZE_CHANGE = "onRowColumnSizeChange";
	public static final String ON_AUTOFILTER_CHANGE = "onAutoFilterChange";
	public static final String ON_FREEZE_CHANGE = "onFreezeChange";
	
	public static final String ON_SHEET_CREATE = "onSheetCreate";
	public static final String ON_SHEET_NAME_CHANGE = "onSheetNameChange";
	public static final String ON_SHEET_ORDER_CHANGE = "onSheetOrderChange";
	public static final String ON_SHEET_DELETE = "onSheetDelete";
	public static final String ON_SHEET_VISIBLE_CHANGE = "onSheetVisibleChange"; //ZSS-832
	
	public static final String ON_PICTURE_ADD = "onPictureAdd";
	public static final String ON_PICTURE_UPDATE = "onPictureUpdate";
	public static final String ON_PICTURE_DELETE = "onPictureDelete";
	
	public static final String ON_CHART_ADD = "onChartAdd";
	public static final String ON_CHART_UPDATE = "onChartUpdate";
	public static final String ON_CHART_DELETE = "onChartDelete";
	
	public static final String ON_MERGE_ADD = "onMergeAdd";
	public static final String ON_MERGE_DELETE = "onMergeDelete";
	
	public static final String ON_DISPLAY_GRIDLINES_CHANGE = "onDisplayGridlinesChange";
	public static final String ON_PROTECT_SHEET_CHANGE = "onProtectSheetChange";
	
	public static final String ON_ROW_INSERT = "onRowInsert";
	public static final String ON_ROW_DELETE = "onRowDelete";
	public static final String ON_COLUMN_INSERT= "onColumnInsert";
	public static final String ON_COLUMN_DELETE = "onColumnDelete";
	
	//ZSS-966
	public static final String ON_NAME_NAME_CHANGE = "onNameNameChange";
	/*
	 * Custom model event
	 */
	public static final String ON_MODEL_FRIEND_FOCUS_DELETE = "onFriendFocusDelete";
	public static final String ON_MODEL_FRIEND_FOCUS_MOVE = "onFriendFocusMove";
	public static final String ON_MODEL_DIRTY_CHANGE = "onDirtyChange";
	
	/**
	 * the effected book
	 */
	public static final String PARAM_BOOK = "book";
	
	/**
	 * the effected sheet
	 */
	public static final String PARAM_SHEET = "sheet";
	
	/**
	 * the effected region
	 */
	public static final String PARAM_REGION = "region";
	
	/**
	 * the object, e.g. notify chart, picture, validation change
	 */
	public static final String PARAM_OBJECT_ID = "objid";
	
	/**
	 * the custom data, e.g. notify custom event
	 */
	public static final String PARAM_CUSTOM_DATA = "customData";
	/**
	 * the index, e.g the deleted sheet index
	 */
	public static final String PARAM_INDEX = "index";
	/**
	 * the old name, e.g old sheet name when rename the sheet
	 */
	public static final String PARAM_OLD_NAME = "oldName";
	
	/**
	 * the old index, e.g old sheet index when reorder the sheet
	 */
	public static final String PARAM_OLD_INDEX = "oldIndex";
	
	/**
	 * the enable state, e.g enable display gridlines 
	 */
	public static final String PARAM_ENABLED = "enabled";
	
	/**
	 * The Name in the table or sheet.
	 * @since 3.8.0
	 */
	public static final String PARAM_NAME = "name";
	
	public static ModelEvent createModelEvent(String name, SBook book){
		return createModelEvent0(name,book,null,null,null);
	}
	
	/**
	 * Create a model event.
	 * @param name event name, can be one constant of {@link ModelEvents}
	 * @param book the book where the event happens
	 * @param data event-related data
	 * @return a model event
	 */
	public static ModelEvent createModelEvent(String name, SBook book,Map data){
		return createModelEvent0(name,book,null,null,data);
	}
	
	/**
	 * @see #createModelEvent(String, SBook, Map) 
	 */
	public static ModelEvent createModelEvent(String name, SSheet sheet){
		return createModelEvent0(name,sheet.getBook(),sheet,null,null);
	}
	
	/**
	 * @see #createModelEvent(String, SBook, Map) 
	 */
	public static ModelEvent createModelEvent(String name, SSheet sheet,Map data){
		return createModelEvent0(name,sheet.getBook(),sheet,null,data);
	}
	
	/**
	 * @see #createModelEvent(String, SBook, Map) 
	 */
	public static ModelEvent createModelEvent(String name, SSheet sheet,CellRegion region){
		return createModelEvent0(name,sheet.getBook(),sheet,region,null);
	}
	
	//ZSS-966
	public static ModelEvent createModelEvent(String name, SBook book, SSheet sheet, Map data) {
		return createModelEvent0(name,book,sheet,null,data);
	}
	
	/**
	 * @see #createModelEvent(String, SBook, Map) 
	 */
	public static ModelEvent createModelEvent(String name, SSheet sheet,CellRegion region,Map data){
		return createModelEvent0(name,sheet.getBook(),sheet,region,data);
	}
	
	/**
	 * A utility method that help you create data map, e.g. createDataMap(ModelEvents.PARAM_CUSTOM_DATA, data)
	 * @param data
	 * @return a map that contains the data you pass in
	 */
	public static Map createDataMap(Object... data){
		if(data!=null){
			if(data.length%2 != 0){
				throw new IllegalArgumentException("event data must be key,value pair");
			}
			Map<String,Object> datamap = new HashMap<String,Object>();
			for(int i=0;i<data.length;i+=2){
				if(!(data[i] instanceof String)){
					throw new IllegalArgumentException("event data key must be string");
				}
				datamap.put((String)data[i],data[i+1]);
			}
			return datamap;
		}
		return null;
	}
	
	private static ModelEvent createModelEvent0(String name, SBook book, SSheet sheet,CellRegion region,Map data){
		Map<String,Object> datamap = new HashMap<String,Object>();
		if(book!=null){
			datamap.put(ModelEvents.PARAM_BOOK, book);
		}
		if(sheet!=null){
			datamap.put(ModelEvents.PARAM_SHEET, sheet);
		}
		if(region!=null){
			datamap.put(ModelEvents.PARAM_REGION, region);
		}
		if(data!=null){
			datamap.putAll(data);
		}
		ModelEvent event = new ModelEvent(name, datamap);
		return event;
	}

	// ZSS-936
	public static boolean isCustomEvent(ModelEvent event) {
		return event.getName().equals(ON_MODEL_FRIEND_FOCUS_MOVE) || 
			event.getName().equals(ON_MODEL_FRIEND_FOCUS_DELETE) ||
			event.getName().equals(ON_MODEL_DIRTY_CHANGE); // ZSS-942
	}
	
	
}
