/* HSSFSheetHelper.java

	Purpose:
		
	Description:
		
	History:
		Jun 4, 2010 10:28:41 AM, Created by henrichen

Copyright (C) 2010 Potix Corporation. All Rights Reserved.

*/

package org.zkoss.poi.hssf.usermodel;

import org.zkoss.poi.hssf.model.InternalSheet;

/**
 * copied from zss. we should remove it after integration.
 * A helper class to make HSSFSheet package method visible.
 * @author henrichen
 *
 */
public class HSSFSheetHelper {
	private final HSSFSheet _sheet;
	public HSSFSheetHelper(HSSFSheet sheet) {
		_sheet = sheet;
	}
	public HSSFSheet getSheet() {
		return _sheet;
	}
	public InternalSheet getInternalSheet() {
		return _sheet.getSheet();
	}
}
