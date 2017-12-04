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

import org.zkoss.zss.model.SColumn;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.STable;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Set;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public abstract class AbstractSheetAdv implements SSheet,LinkedModelObject,Serializable{
	private static final long serialVersionUID = 1L;
	
	/*package*/ abstract AbstractRowAdv getRow(int rowIdx, boolean proxy);
	/*package*/ abstract AbstractRowAdv getOrCreateRow(int rowIdx);
//	/*package*/ abstract int getRowIndex(AbstractRowAdv row);
	
	/*package*/ abstract SColumn getColumn(int columnIdx, boolean proxy);
	/*package*/ abstract AbstractColumnArrayAdv getOrSplitColumnArray(int index);
	
//	/*package*/ abstract ColumnAdv getOrCreateColumn(int columnIdx);
//	/*package*/ abstract int getColumnIndex(ColumnAdv column);
	
	/*package*/ abstract AbstractCellAdv getCell(int rowIdx, int columnIdx, boolean proxy);

    /*package*/ public
    abstract AbstractCellAdv createCell(int rowIdx, int columnIdx);


    /*package*/ abstract void copyTo(AbstractSheetAdv sheet, Connection connection, boolean updateToDB);

	/*package*/
	abstract void setSheetName(String name, boolean updateToDB);

//	/*package*/ abstract void onModelInternalEvent(ModelInternalEvent event);
	
	//ZSS-855
	abstract public STable getTableByRowCol(int rowIdx, int colIdx);
	
	//ZSS-962
	abstract public boolean isHidden(int rowIdx, int colIdx);
	
	//ZSS-985
	abstract public void removeTables(Set<String> tableNames);
	
	//ZSS-1001
	abstract public void removeTable(STable table);
	
	//ZSS-1001
	abstract public void clearTables();

    abstract public int getTrxId();

	abstract public int getNewTrxId();

	abstract public boolean isSyncCalc();

	abstract public void setSyncComputation(boolean syncComputation);

}
