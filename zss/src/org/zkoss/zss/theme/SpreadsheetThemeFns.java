/* SpreadsheetThemeFns.java

	Purpose:
		
	Description:
		
	History:
		Thu, Jun 26, 2014 12:43:36 PM, Created by RaymondChao

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.theme;
import java.util.Comparator;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.zkoss.lang.Library;
import org.zkoss.web.fn.ServletFns;
import org.zkoss.web.theme.StandardTheme;
import org.zkoss.web.theme.Theme;

/**
 * Providing theme relevant functions for EL.
 * 
 * @author simonpai
 * @author jumperchen
 * @author RaymondChao
 * @since 3.5.0
 */
public class SpreadsheetThemeFns {
	
	private final static String SPREADSHEET_THEME_PREFERRED_KEY = "org.zkoss.zss.theme.preferred";
	// the current spreadsheet theme registry
	private static SpreadsheetThemeRegistry _themeRegistry = null;
	// the current spreadsheet theme resolver
	private static SpreadsheetThemeResolver _themeResolver = null;
	
	/**
	 * Change the theme registry
	 * 
	 * @param themeRegistry the new theme registry
	 */
	public static void setThemeRegistry(SpreadsheetThemeRegistry themeRegistry) {
		_themeRegistry = themeRegistry;
	}
	
	/**
	 * Returns the current theme registry
	 * 
	 * @return the current theme registry
	 */
	public static SpreadsheetThemeRegistry getThemeRegistry() {
		return _themeRegistry;
	}
	
	/**
	 * Returns the current theme resolver
	 * 
	 * @return the current theme resolver
	 */
	public static SpreadsheetThemeResolver getThemeResolver() {
		return _themeResolver;
	}
	
	/**
	 * Change the current theme resolver
	 * 
	 * @param themeResolver the new theme resolver
	 */
	public static void setThemeResolver(SpreadsheetThemeResolver themeResolver) {
		_themeResolver = themeResolver;
	}
	
	/**
	 * Returns the current theme name
	 * 
	 * @return the current theme name
	 */
	public static String getCurrentTheme() {

		// 1. cookie's key
		String t = getTheme();
		if (_themeRegistry.hasTheme(t))
			return t;
		
		// 2. library property
		t = Library.getProperty(SPREADSHEET_THEME_PREFERRED_KEY);
		if (_themeRegistry.hasTheme(t))
			return t;

		
		// 3. theme of highest priority
		Theme[] themes = _themeRegistry.getThemes();
		SpreadsheetStandardTheme highest = null;
		Comparator<SpreadsheetStandardTheme> comparator = SpreadsheetStandardTheme.getComparator();
		for (Theme theme : themes) {
			if (theme instanceof SpreadsheetStandardTheme) {
				if (comparator.compare((SpreadsheetStandardTheme)theme, highest) < 0) {
					highest = (SpreadsheetStandardTheme) theme;
				}
			}
		}		
		return (highest != null) ? highest.getName() : SpreadsheetStandardTheme.DEFAULT_NAME;
	}
	
	/**
	 * Returns the theme specified in cookies
	 * @return the name of the theme or default theme.
	 */
	private static String getTheme() {
		ServletRequest request = ServletFns.getCurrentRequest();
		
		if (!(request instanceof HttpServletRequest))
			return StandardTheme.DEFAULT_NAME;
		
		return _themeResolver.getTheme((HttpServletRequest)request);
	}
}
