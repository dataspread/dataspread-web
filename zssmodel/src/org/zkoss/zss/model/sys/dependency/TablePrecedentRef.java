/* TablePrecedentRef.java

	Purpose:
		
	Description:
		
	History:
		Mar 25, 2015 10:36:06 AM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model.sys.dependency;

//ZSS-966
/**
 * Precedent reference to ColumnRef. (Mainly used to collect ColumnRefs when 
 * table name is changed)
 * @author henri
 * @since 3.8.0
 *
 */
public interface TablePrecedentRef extends Ref {
	String getTableName();
}
