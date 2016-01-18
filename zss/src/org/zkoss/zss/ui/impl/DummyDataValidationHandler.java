/* DummyDataValidationHandler.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/8/7 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.impl;

import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.sys.DataValidationHandler;

public class DummyDataValidationHandler implements DataValidationHandler {
	private static final long serialVersionUID = -5474895962009092256L;

	@Override
	public List<Map<String, Object>> loadDataValidtionJASON(Sheet sheet) {
		return null;
	}

	@Override
	public boolean validate(Sheet sheet, int row, int col, String editText,
			EventListener callback) {
		return true;//always true,
	}

}
