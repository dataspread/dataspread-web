/* ZssMessageLoader.java

{{IS_NOTE
 Purpose:
  
 Description:
  
 History:
  2013/7/15 Dennis
}}IS_NOTE

Copyright (C) 2011 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.impl;

import java.io.IOException;

import org.zkoss.zk.device.Devices;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.metainfo.MessageLoader;

/**
 * ZUL implementation of MessageLoader
 * @author dennis
 * @since 3.0.0
 */
public class ZssMessageLoader implements MessageLoader {
	
	public void load(StringBuffer out, Execution exec) throws IOException {
		out.append(Devices.loadJavaScript(exec, "~./js/zss/lang/msgzss*.js"));
	}	
}