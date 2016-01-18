/* Position.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jun 4, 2008 3:51:41 PM     2008, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui;

import org.zkoss.zss.api.CellRef;

/**
 * @author dennis
 * @deprecated since 3.0.0, use {@link CellRef}
 */
public class Position extends CellRef{

	public Position() {
		super();
	}

	public Position(int row, int column) {
		super(row, column);
	}
}
