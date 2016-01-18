/* ModelEvent.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/11 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model;

import java.util.HashMap;
import java.util.Map;

/**
 * The event will be sent when there is an operation performed on a book model. It could also contain a operation-related data.
 * @author dennis
 * @since 3.5.0
 */
public class ModelEvent {
	private String _name;
	
	private Map<String,Object> data;
	
	public ModelEvent(String name){
		this._name = name;
	}
	
	public ModelEvent(String name,Map<String,Object> data){
		this._name = name;
		this.data = new HashMap<String, Object>(data);
	}
	
	public Object getData(String key){
		return data==null?null:data.get(key);
	}

	public String getName() {
		return _name;
	}

	public SBook getBook() {
		return (SBook)getData(ModelEvents.PARAM_BOOK);
	}
	
	public SSheet getSheet() {
		return (SSheet)getData(ModelEvents.PARAM_SHEET);
	}
	
	public CellRegion getRegion() {
		return (CellRegion)getData(ModelEvents.PARAM_REGION);
	}
	
	public Object getCustomData() {
		return getData(ModelEvents.PARAM_CUSTOM_DATA);
	}
	
	public boolean isWholeRow(){
		SBook book = getBook();
		if(book == null){
			throw new IllegalStateException("can't find book");
		}
		CellRegion region = getRegion();
		if(region==null){
			return false;
		}
		return region.column<=0 && region.lastColumn>=book.getMaxColumnIndex();
	}
	
	public boolean isWholeColumn(){
		SBook book = getBook();
		if(book == null){
			throw new IllegalStateException("can't find book");
		}
		CellRegion region = getRegion();
		if(region==null){
			return false;
		}
		return region.row<=0 && region.lastRow>=book.getMaxRowIndex();
	}

	public String getObjectId() {
		return (String)getData(ModelEvents.PARAM_OBJECT_ID);
	}
}
