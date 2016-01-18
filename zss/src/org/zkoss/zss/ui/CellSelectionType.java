/* CellSelectionType.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/5/20 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui;

import org.zkoss.zss.ui.event.CellSelectionEvent;
import org.zkoss.zss.ui.event.CellSelectionUpdateEvent;

/**
 * Cell selection type about {@link CellSelectionEvent} and {@link CellSelectionUpdateEvent}
 * @author dennis
 * @since 3.0.0
 */
public enum CellSelectionType {
	CELL, //cell
	ROW, //row
	COLUMN, //col
	ALL //all
}