/* SpreadsheetWebAppInit.java

	Purpose:
		
	Description:
		
	History:
		Thu, Jun 26, 2014  4:54:52 PM, Created by RaymondChao

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.ui;

import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.util.Configuration;
import org.zkoss.zss.theme.SpreadsheetThemeFns;
import org.zkoss.zss.theme.SpreadsheetThemeProvider;

/**
 * 
 * @author RaymondChao
 * @since 3.5.0
 */
public class WebAppInit implements org.zkoss.zk.ui.util.WebAppInit {


	public void init(WebApp wapp) throws Exception {
		SpreadsheetThemeFns.setThemeResolver(new org.zkoss.zss.theme.SpreadsheetThemeResolver());
		SpreadsheetThemeFns.setThemeRegistry(new org.zkoss.zss.theme.SpreadsheetThemeRegistry());
		setSpreadsheetThemeProvider(wapp.getConfiguration());
	}
	
	private void setSpreadsheetThemeProvider(Configuration configuration) {
		configuration.setThemeProvider(new SpreadsheetThemeProvider(configuration.getThemeProvider()));
	}

}