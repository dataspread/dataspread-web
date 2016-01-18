/* CellFetchCommand.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		January 10, 2008 03:10:40 PM , Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under Lesser GPL Version 2.1 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.au.in;


import org.zkoss.zk.au.AuRequest;

/**
 * A Command (client to server) for fetch data back
 * @author Dennis.Chen
 *
 */
public class CellFetchCommand extends AbstractCommand implements Command {
	public final static String Command = "onZSSCellFetch";

	//-- super --//
	public void process(AuRequest request) {
		new CellFetchCommandHelper().process(request);
	}
}