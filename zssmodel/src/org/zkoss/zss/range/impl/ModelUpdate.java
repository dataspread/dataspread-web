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

import org.zkoss.zss.model.impl.CellAttribute;

/**
 * 
 * @author Dennis
 * @since 3.5.0
 */
public class ModelUpdate {
	/**
	 * @since 3.5.0
	 */
	public static enum UpdateType{
		REF, REFS, CELL, CELLS, MERGE, INSERT_DELETE, FILTER /*ZSS-988*/,
	}
	
	final UpdateType type;
	final Object data;
	final CellAttribute cellAttr; //ZSS-939
	
	@Deprecated
	public ModelUpdate(UpdateType type,Object data){
		this(type, data, CellAttribute.ALL);
	}
	
	//ZSS-939
	//@since 3.8.0
	public ModelUpdate(UpdateType type,Object data,CellAttribute cellAttr){
		this.type = type;
		this.data = data;
		this.cellAttr = cellAttr; //ZSS-939
	}
	
	public UpdateType getType(){
		return type;
	}
	
	public Object getData(){
		return data;
	}

	//ZSS-939
	//@since 3.8.0
	public CellAttribute getCellAttr() {
		return cellAttr;
	}
	
	@Override
	public String toString() {
		return "ModelUpdate [type=" + type + ", data=" + data + "]";
	}
	
	
}
