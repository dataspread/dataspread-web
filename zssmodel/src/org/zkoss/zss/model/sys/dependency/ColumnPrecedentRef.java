/* ColumnPrecedentRef.java

	Purpose:
		
	Description:
		
	History:
		Mar 25, 2015 10:35:14 AM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model.sys.dependency;

//ZSS-967
/**
 * Precedent reference to ColumnRef. (Mainly used to colllect ColumnRefs when 
 * column name is changed)
 * @author henri
 * @since 3.8.0
 */
public interface ColumnPrecedentRef extends Ref {

	String getTableName();
	String getColumnName();
}
