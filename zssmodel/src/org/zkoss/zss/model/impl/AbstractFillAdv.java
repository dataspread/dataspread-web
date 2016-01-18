/* AbstractFillAdv.java

	Purpose:
		
	Description:
		
	History:
		Mar 31, 2015 6:04:04 PM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model.impl;

import java.io.Serializable;

import org.zkoss.zss.model.SFill;

/**
 * @author henri
 * @since 3.8
 */
public abstract class AbstractFillAdv implements SFill, Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * gets the string key of this font, the key should combine all the style value in short string as possible
	 */
	abstract String getStyleKey();
}
