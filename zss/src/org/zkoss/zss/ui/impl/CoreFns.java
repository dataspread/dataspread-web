/* CoreFns.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/7/24 Dennis
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
 */
package org.zkoss.zss.ui.impl;

import org.zkoss.zss.ui.Version;

/**
 * core taglig function for zss
 * @author dennis
 * @since 3.0.0
 */
public class CoreFns {

	/** Returns the string encoded with ZSS.
	 * @since 3.0.0
	 */
	public static String encodeWithZSS(String name) {
		if (name.startsWith("zss"))
			return "zss" + Version.getEdition().toLowerCase(java.util.Locale.ENGLISH) + name.substring(3);
		return "zss" + Version.getEdition().toLowerCase(java.util.Locale.ENGLISH) + name;
	}
}
