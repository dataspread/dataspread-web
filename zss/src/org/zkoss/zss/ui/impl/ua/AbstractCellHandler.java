/* AbstractCellFormatHandler.java

	Purpose:
		
	Description:
		
	History:
		Wed, Apr 30, 2014 10:23:05 AM, Created by RaymondChao

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.ui.impl.ua;

import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Sheet;

/**
 * 
 * @author RaymondChao
 * @since 3.5.0
 */
public abstract class AbstractCellHandler extends AbstractHandler {
	private static final long serialVersionUID = -1020093806000599051L;

	@Override
	public boolean isEnabled(Book book, Sheet sheet) {
		return book != null && sheet != null && ( !sheet.isProtected() ||
				Ranges.range(sheet).getSheetProtection().isFormatCellsAllowed());
	}
}
