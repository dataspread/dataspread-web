/* DummyFreezeInfoLoader.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/8/8 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.impl;

import org.zkoss.zss.ui.sys.FreezeInfoLoader;

/**
 * @author dennis
 * @since 3.0.0
 */
public class DummyFreezeInfoLoader implements FreezeInfoLoader {
	private static final long serialVersionUID = -8162740108561693443L;

	/* (non-Javadoc)
	 * @see org.zkoss.zss.ui.sys.FreezeInfoLoader#getRowFreeze(org.zkoss.zss.api.model.Sheet)
	 */
	@Override
	public int getRowFreeze(Object sheet) {
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.zkoss.zss.ui.sys.FreezeInfoLoader#getColumnFreeze(org.zkoss.zss.api.model.Sheet)
	 */
	@Override
	public int getColumnFreeze(Object sheet) {
		return -1;
	}

}
