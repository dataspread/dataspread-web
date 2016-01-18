/* TableStyleMedium9.java

	Purpose:
		
	Description:
		
	History:
		Mar 30, 2015 5:17:51 PM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model.impl;

/**
 * Builtin TableStyleNone
 * 
 * @author henri
 * @since 3.8.0
 */
public class TableStyleNone extends TableStyleImpl {
	private TableStyleNone() {
		super(
				"None",//name,
				null, 	//wholeTable,
				null, 	//colStripe1,
				1, 		//colStripe1Size,
				null, 	//colStripe2,
				1, 		//colStripe2Size,
				null, 	//rowStripe1,
				1, 		//rowStripe1Size,
				null,	//rowStripe2,
				1, 		//rowStripe2Size,
				null, 	//lastCol,
				null, 	//firstCol,
				null,	//headerRow,
				null,	//totalRow,
				null,	//firstHeaderCell,
				null,	//lastHeaderCell,
				null,	//firstTotalCell,
				null	//lastTotalCell
		);
	}
	public static final TableStyleNone instance = new TableStyleNone();
}
