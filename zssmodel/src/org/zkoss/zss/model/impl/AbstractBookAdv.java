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


import org.zkoss.zss.model.*;
import org.zkoss.zss.model.sys.formula.EvaluationContributorContainer;
import java.io.Serializable;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public abstract class AbstractBookAdv implements SBook,EvaluationContributorContainer,Serializable{
	private static final long serialVersionUID = 1L;
	
	public abstract void sendModelEvent(ModelEvent event);
	
	/*package*/ abstract String nextObjId(String type);

	/*package*/ abstract void setBookSeries(SBookSeries bookSeries);

	public abstract String getId();
	public abstract boolean setNameAndLoad(String bookName);
	
	//ZSS-854
	public abstract void clearDefaultCellStyles();
	
	//ZSS-854
	public abstract void clearNamedStyles();

	//ZSS-854
	public abstract void initDefaultCellStyles();
	
	//ZSS-855
	public abstract SName createTableName(STable table);
	
	//ZSS-855
	public abstract void addTable(STable table);

	//ZSS-855
	public abstract STable getTable(String name);
	
	//ZSS-855
	public abstract STable removeTable(String name);
	
	//ZSS-967
	//return null if newName is not duplicated in the Table
	public abstract String setTableColumnName(STable table, String oldName, String newName);

}
