/* SpreadsheetStandardTheme.java

	Purpose:
		
	Description:
		
	History:
		Thu, Jun 26, 2014 12:52:33 PM, Created by RaymondChao

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

 */
package org.zkoss.zss.theme;

import java.util.Comparator;

import org.zkoss.web.theme.Theme;

/**
 * A standard implementation of Theme.
 * 
 * @author neillee
 * @author RaymondChao
 * @since 3.5.0
 */

public final class SpreadsheetStandardTheme extends Theme {

	/**
	 * Used to specify the origin of theme resources (e.g. CSS, Images)
	 * 
	 */
	public static enum ThemeOrigin {
		JAR, FOLDER
	}

	/**
	 * Name used to identify the default theme
	 */
	public final static String DEFAULT_NAME = "default";
	/**
	 * Name used to display the default theme
	 */
	public final static String DEFAULT_DISPLAY = "Default";
	/**
	 * Priority of the default theme
	 */
	public final static int DEFAULT_PRIORITY = 500;
	
	/**
	 * Origin of the default theme, it is inside zss.jar
	 */
	public final static ThemeOrigin DEFAULT_ORIGIN = ThemeOrigin.JAR;

	// Name used to display the theme
	private String _displayName;
	// Used in theme selection process
	private int _priority;
	// Location to retrieve theme resources such as CSS files and images
	private ThemeOrigin _origin;

	/**
	 * Instantiate a default theme
	 */
	public SpreadsheetStandardTheme() {
		super(DEFAULT_NAME);
	}

	/**
	 * Instantiate a standard theme
	 * 
	 * @param themeName
	 *            name used to identify the theme
	 */
	public SpreadsheetStandardTheme(String themeName) {
		this(themeName, themeName, DEFAULT_PRIORITY, DEFAULT_ORIGIN);
	}

	/**
	 * Instantiate a standard theme
	 * 
	 * @param themeName
	 *            name used to identify the theme
	 * @param origin
	 *            where the theme resource is located, jar or folder
	 */
	public SpreadsheetStandardTheme(String themeName, ThemeOrigin origin) {
		this(themeName, themeName, DEFAULT_PRIORITY, origin);
	}

	/**
	 * Instantiate a standard theme
	 * 
	 * @param themeName
	 *            name used to identify the theme
	 * @param displayName
	 *            name used to display the theme
	 * @param priority
	 *            used to choose which theme to use, if no theme is specified,
	 *            and no preferred theme is set. Higher priority themes are
	 *            chosen over the lower priority themes. Lower the priority
	 *            value, higher the priority.
	 */
	public SpreadsheetStandardTheme(String themeName, String displayName, int priority) {
		this(themeName, displayName, priority, DEFAULT_ORIGIN);
	}

	/**
	 * Instantiate a standard theme
	 * 
	 * @param themeName
	 *            name used to identify the theme
	 * @param displayName
	 *            name used to display the theme
	 * @param priority
	 *            used to choose which theme to use, if no theme is specified,
	 *            and no preferred theme is set. Higher priority themes are
	 *            chosen over the lower priority themes. Lower the priority
	 *            value, higher the priority.
	 * @param origin
	 *            where the theme resources (i.e. CSS and images) are located
	 */
	public SpreadsheetStandardTheme(String themeName, String displayName, int priority,
			ThemeOrigin origin) {
		if (!"".equals(themeName)) {
			super.setName(themeName);
			_displayName = displayName;
			_priority = priority;
			_origin = origin;
		} else
			throw new IllegalArgumentException(
					"Standard themes should not have blank names");
	}

	/**
	 * @return the name used to display the theme
	 */
	public String getDisplayName() {
		return _displayName;
	}

	/**
	 * Rename the display name
	 * 
	 * @param displayName
	 *            the new name used to display the theme
	 */
	public void setDisplayName(String displayName) {
		_displayName = displayName;
	}

	/**
	 * @return the priority of the theme
	 */
	public int getPriority() {
		return _priority;
	}

	/**
	 * Adjust the priority of the theme
	 * 
	 * @param priority
	 *            the new priority of the theme
	 */
	public void setPriority(int priority) {
		_priority = priority;
	}

	/**
	 * @return the origin of the theme resource
	 */
	public ThemeOrigin getOrigin() {
		return _origin;
	}

	private static final Comparator<SpreadsheetStandardTheme> _COMPARATOR = new Comparator<SpreadsheetStandardTheme>() {

		public int compare(SpreadsheetStandardTheme t1, SpreadsheetStandardTheme t2) {
			if (t1 == null) {
				if (t2 == null)
					return 0;
				else
					return 1;
			} else if (t2 == null) {
				return -1;
			} else
				return t1._priority - t2._priority;
		}

	};

	/**
	 * Defines the ordering of standard themes. Higher priority themes are
	 * ordered before the lower priority themes.
	 * 
	 * @return the comparator based on theme's priority values
	 */
	public static Comparator<SpreadsheetStandardTheme> getComparator() {
		return _COMPARATOR;
	}

}
