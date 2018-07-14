/* PivotTableHelper.java

	Purpose:
		
	Description:
		
	History:
		May 17, 2012 12:42:30 PM, Created by henri

Copyright (C) 2012 Potix Corporation. All Rights Reserved.
*/
package org.zkoss.poi.ss.usermodel;

import java.util.List;

import org.zkoss.poi.ss.util.AreaReference;
import org.zkoss.poi.ss.util.CellReference;

/**
 * @author henri
 *
 */
public interface PivotTableHelper {
	static final String CLASS = "org.zkoss.poi.ss.usermodel.PivotTableHelper.class";
	List<PivotCache> initPivotCaches(Workbook book);
	PivotCache createPivotCache(AreaReference sourceRef, Workbook book);
	List<PivotTable> initPivotTables(Sheet sheet);
	PivotTable createPivotTable(CellReference destination, String name, PivotCache pivotCache, Sheet sheet);
}
