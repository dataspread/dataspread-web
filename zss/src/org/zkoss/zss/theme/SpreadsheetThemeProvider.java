/* SpreadsheetThemeProvider.java

	Purpose:
		
	Description:
		
	History:
		Thu, Jun 26, 2014  2:45:07 PM, Created by RaymondChao

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.theme;

import java.util.Collection;
import java.util.List;

import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.util.ThemeProvider;

/**
 * 
 * @author RaymondChao
 * @since 3.5.0
 */
public class SpreadsheetThemeProvider implements ThemeProvider {
	private final ThemeProvider _themeProvider;
	public SpreadsheetThemeProvider(ThemeProvider themeProvider) {
		_themeProvider = themeProvider; 
	}

	public Collection<Object> getThemeURIs(Execution exec, List<Object> uris) {
		return _themeProvider.getThemeURIs(exec, uris);
	}

	public int getWCSCacheControl(Execution exec, String uri) {
		return _themeProvider.getWCSCacheControl(exec, uri);
	}

	public String beforeWCS(Execution exec, String uri) {
		return _themeProvider.beforeWCS(exec, uri);
	}

	public String beforeWidgetCSS(Execution exec, String uri) {
		if (uri.startsWith("~./js/zss") || uri.startsWith("~./js/zss/css")) {
			uri = SpreadsheetThemes.resolveThemeURL(uri);
		} else {
			uri = _themeProvider.beforeWidgetCSS(exec, uri);
		}
		return uri;
	}
}
