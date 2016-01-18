/* AddSheetHandler.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/8/3 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.impl.ua;

import org.zkoss.lang.Strings;
import org.zkoss.util.resource.Labels;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.SheetOperationUtil;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.UserActionContext;

/**
 * @author dennis
 * @since 3.0.0
 */
public class AddSheetHandler extends AbstractBookHandler{
	private static final long serialVersionUID = -6342909797838269567L;

	@Override
	protected boolean processAction(UserActionContext ctx) {
		String prefix = Labels.getLabel("zss.newSheetPrefix","Sheet");
		if (Strings.isEmpty(prefix))
			prefix = "Sheet";
		
		Sheet sheet = ctx.getSheet();

		Range range = Ranges.range(sheet);
		SheetOperationUtil.addSheet(range,prefix);
		
		return true;
	}

}
