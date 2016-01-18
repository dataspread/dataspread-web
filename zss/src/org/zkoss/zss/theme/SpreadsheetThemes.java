/* SpreadsheetThemes.java

	Purpose:
		
	Description:
		
	History:
		Thu, Jun 26, 2014 12:56:03 PM, Created by RaymondChao

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.theme;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zkoss.lang.Library;
import org.zkoss.lang.Strings;
import org.zkoss.web.theme.Theme;
import org.zkoss.web.theme.ThemeRegistry;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zss.theme.SpreadsheetStandardTheme.ThemeOrigin;

/**
 * Facade for accessing internal theming subsystem
 * In most cases, users need not use the underlying theme registry and
 * theme resolver directly.
 * 
 * @author sam
 * @author neillee
 * @author RaymondChao
 * @since 3.5.0
 */
public class SpreadsheetThemes {
	
	/**
	 * Sets the theme name using the current theme resolution strategy
	 * Default strategy is to use cookies
	 * 
	 * @param exe Execution
	 * @param themeName the new intended theme name
	 */
	public static void setTheme (Execution exe, String themeName) {
		SpreadsheetThemeResolver themeResolver = SpreadsheetThemeFns.getThemeResolver();
		
		themeResolver.setTheme(
			(HttpServletRequest)exe.getNativeRequest(), 
			(HttpServletResponse)exe.getNativeResponse(), 
			themeName);
	}
	
	/**
	 * Returns the theme specified using the current theme resolution strategy
	 * Default strategy is to use cookies
	 * 
	 * @param exe Execution
	 * @return the name of the theme or a fall back theme name determined by the
	 * theme resolution strategy used.
	 */
	public static String getTheme (Execution exe) {
		SpreadsheetThemeResolver themeResolver = SpreadsheetThemeFns.getThemeResolver();
		
		return themeResolver.getTheme((HttpServletRequest) exe.getNativeRequest());
	}

	/**
	 * Returns the current theme name
	 * @return the current theme name
	 */
	public static String getCurrentTheme() {
		return SpreadsheetThemeFns.getCurrentTheme();
	}
	
	/**
	 * Returns true if the theme is registered
	 * @param themeName the name of the theme
	 * @return true if the theme with the given name is registered
	 */
	public static boolean hasTheme(String themeName) {
		ThemeRegistry themeRegistry = SpreadsheetThemeFns.getThemeRegistry();
		return themeRegistry.hasTheme(themeName);
	}
	
	/**
	 * Returns an array of registered theme names
	 * @return an array of registered theme names
	 */
	public static String[] getThemes() {
		ThemeRegistry themeRegistry = SpreadsheetThemeFns.getThemeRegistry();
		Theme[] themes = themeRegistry.getThemes();
		String[] themeNames = new String[themes.length];
		for (int i = 0; i < themes.length; i++) {
			themeNames[i] = themes[i].getName();			
		}
		return themeNames;
	}
	
	/**
	 * Register the theme, so it becomes available in the theme list
	 * 
	 * @param themeName the name of the theme to be registered
	 */
	public static void register(String themeName) {
		ThemeRegistry themeRegistry = SpreadsheetThemeFns.getThemeRegistry();
		themeRegistry.register(new SpreadsheetStandardTheme(themeName));
	}
	
	/**
	 * Register the theme, and specifies its origin (e.g. from JAR or from FOLDER)
	 * Please use <code>Themes.register("custom", Themes.ThemeOrigin.FOLDER)</code> 
	 * to make your custom theme available if the theme resource is inside a folder
	 * 
	 * @param themeName theme name
	 * @param origin origin of the theme resource
	 */
	public static void register(String themeName, ThemeOrigin origin) {
		ThemeRegistry themeRegistry = SpreadsheetThemeFns.getThemeRegistry();
		themeRegistry.register(new SpreadsheetStandardTheme(themeName, origin));
	}
	
	/**
	 * Register the theme with details
	 * 
	 * @param themeName theme name
	 * @param displayName The human name of the theme
	 * @param priority Priority is higher if the value the smaller
	 */
	public static void register(String themeName, String displayName, int priority) {
		ThemeRegistry themeRegistry = SpreadsheetThemeFns.getThemeRegistry();
		themeRegistry.register(new SpreadsheetStandardTheme(themeName, displayName, priority));
	}

	/**
	 * Register the theme, its display name, its priority; and also specifies 
	 * its origin (e.g. from JAR or from FOLDER). Please use 
	 * <code>Themes.register("custom", "Custom Theme", 100, Themes.ThemeOrigin.FOLDER)</code> 
	 * to make your custom theme available if the theme resource is inside a folder.
	 * 
	 * @param themeName theme name
	 * @param displayName a more descriptive name for the theme, for display purpose
	 * @param priority priority of the theme
	 * @param origin origin of the theme resource
	 */
	public static void register(String themeName, String displayName, int priority, ThemeOrigin origin) {
		ThemeRegistry themeRegistry = SpreadsheetThemeFns.getThemeRegistry();
		themeRegistry.register(new SpreadsheetStandardTheme(themeName, displayName, priority, origin));
	}
	
	/** 
	 * Set the display name (human name) of the theme
	 * 
	 * @param themeName theme name
	 * @param displayName the new name to be displayed
	 */
	public static void setDisplayName(String themeName, String displayName) {
		ThemeRegistry themeRegistry = SpreadsheetThemeFns.getThemeRegistry();
		Theme theme = themeRegistry.getTheme(themeName);
		if (theme instanceof SpreadsheetStandardTheme) {
			((SpreadsheetStandardTheme) theme).setDisplayName(displayName);
		}
	}
	
	/**
	 * Return the display name (human name) of the theme
	 *
	 * @param themeName theme name
	 * @return the display name
	 */
	public static String getDisplayName(String themeName) {
		ThemeRegistry themeRegistry = SpreadsheetThemeFns.getThemeRegistry();
		Theme theme = themeRegistry.getTheme(themeName);
		String displayName = "";
		if (theme instanceof SpreadsheetStandardTheme) {
			displayName = ((SpreadsheetStandardTheme) theme).getDisplayName();
		}
		return Strings.isEmpty(displayName) ? capitalize(themeName) : displayName;
	}
	
	/**
	 * Set the priority of the theme.
	 * @param themeName theme name
	 * @param priority Priority is higher if the value the smaller
	 */
	public static void setPriority(String themeName, int priority) {
		ThemeRegistry themeRegistry = SpreadsheetThemeFns.getThemeRegistry();
		Theme theme = themeRegistry.getTheme(themeName);

		if (theme instanceof SpreadsheetStandardTheme) {
			((SpreadsheetStandardTheme)theme).setPriority(priority);
		}
	}
	
	/**
	 * Return the priority of the given theme
	 * 
	 * @param themeName theme name
	 * @return the priority of the given theme
	 */
	public static int getPriority(String themeName) {
		ThemeRegistry themeRegistry = SpreadsheetThemeFns.getThemeRegistry();
		Theme theme = themeRegistry.getTheme(themeName);
		
		if (theme instanceof SpreadsheetStandardTheme) {
			return ((SpreadsheetStandardTheme)theme).getPriority();
		} else {
			return Integer.MAX_VALUE;
		}
	}
	
	// helper //
	private static String capitalize(String str) {
		if(Strings.isEmpty(str))
			return str;
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}
	
	private final static String THEME_FOLDER_ROOT = "org.zkoss.zss.theme.folder.root";
	
	public static final String resolveThemeURL(String url) {
		if (url == null)
			return null;
		
		String themeName = 
			SpreadsheetThemeFns.getCurrentTheme();
		String prefix =
			Library.getProperty(THEME_FOLDER_ROOT, "theme");
		
		String resolved = null;
		
		if (Strings.isBlank(themeName) || SpreadsheetStandardTheme.DEFAULT_NAME.equals(themeName))
			resolved = url;
		else {
			Theme theme = SpreadsheetThemeFns.getThemeRegistry().getTheme(themeName);
			if (theme instanceof SpreadsheetStandardTheme) {
				if (((SpreadsheetStandardTheme)theme).getOrigin() == ThemeOrigin.JAR)
					resolved = url.replaceFirst("~./", "~./" + themeName + "/");
				else
					resolved = url.replaceFirst("~./", "/" + prefix + "/" + themeName +"/");					
			}
		}
		return resolved;
	}
	
}
