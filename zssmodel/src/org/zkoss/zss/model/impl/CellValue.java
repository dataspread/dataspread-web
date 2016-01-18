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

import java.io.Serializable;

import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SCell.CellType;
/**
 * 
 * @author Dennis
 * @since 3.5.0
 */
public class CellValue implements Serializable {
	private static final long serialVersionUID = 1L;
	protected CellType cellType;
	protected Object value;
	public CellValue(String value){
		this(CellType.STRING,value);
	}
	public CellValue(Double number){
		this(CellType.NUMBER,number);
	}
	public CellValue(Boolean bool){
		this(CellType.BOOLEAN,bool);
	}
	public CellValue(){
		this(CellType.BLANK,null);
	}
	
	protected CellValue(CellType type, Object value){
		this.cellType = value==null?CellType.BLANK:type;
		this.value = value;
	}
	
	public CellType getType() {
		return cellType;
	}
	public Object getValue() {
		return value;
	}
}
