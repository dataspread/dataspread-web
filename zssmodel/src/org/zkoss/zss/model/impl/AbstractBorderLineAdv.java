/* AbstractBorderLineAdv.java

	Purpose:
		
	Description:
		
	History:
		Apr 1, 2015 3:20:24 PM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model.impl;

import java.io.Serializable;

import org.zkoss.zss.model.SBorderLine;

/**
 * @author henri
 * @since 3.8.0
 */
public abstract class AbstractBorderLineAdv implements Serializable, SBorderLine {
	private static final long serialVersionUID = 1L;

	abstract String getStyleKey();
}
