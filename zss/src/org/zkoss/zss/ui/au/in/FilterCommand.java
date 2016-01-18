/* FilterCommand.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 5, 2011 9:44:00 AM , Created by sam
}}IS_NOTE

Copyright (C) 2011 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.au.in;

import java.util.Map;

import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.mesg.MZk;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zss.ui.Spreadsheet;

/**
 * @author sam
 *
 */
public class FilterCommand extends AbstractCommand implements Command {

	@Override
	public void process(AuRequest request) {
		final Component comp = request.getComponent();
		if (comp == null)
			throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED, this);
		
		final Map data = request.getData();
		String type = (String) data.get("type");
		if ("apply".equals(type)) {
			final boolean selectAll = (Boolean) data.get("all");
			final String cellRangeAddr = (String) data.get("range");
			final int field = (Integer) data.get("field");
			final Object criteria = data.get("criteria");
			new AutoFilterDefaultHandler().applyFilter(((Spreadsheet) comp),((Spreadsheet) comp).getSelectedSheet(),cellRangeAddr,selectAll,field,criteria);
		}
	}

}
