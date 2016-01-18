/* SpreadsheetThemeResolver.java

	Purpose:
		
	Description:
		
	History:
		Fri, Jun 27, 2014  9:40:11 AM, Created by RaymondChao

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

 */
package org.zkoss.zss.theme;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zkoss.web.theme.ThemeResolver;

/**
 * A standard implementation of ThemeResolver Retrieves and stores theme names
 * via cookie
 * 
 * @author neillee
 * @author RaymondChao
 * @since 3.5.0
 */
public class SpreadsheetThemeResolver implements ThemeResolver {

	private final static String SPREADSHEET_THEME_COOKIE_KEY = "zsstheme";

	/**
	 * Retrieves theme name from Cookie
	 * 
	 * @param request
	 * @return theme name stored in Cookie, or "" if not found
	 */
	public String getTheme(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null)
			return "";
		for (Cookie c : cookies) {
			if (SPREADSHEET_THEME_COOKIE_KEY.equals(c.getName())) {
				String themeName = c.getValue();
				if (themeName != null)
					return themeName;
			}
		}
		return "";
	}

	/**
	 * Stores theme name in Cookie
	 * 
	 * @param request
	 * @param response
	 * @param themeName
	 *            theme name to be stored in Cookie
	 */
	public void setTheme(HttpServletRequest request,
			HttpServletResponse response, String themeName) {
		Cookie cookie = new Cookie(SPREADSHEET_THEME_COOKIE_KEY, themeName);
		if (request.isSecure()) {
			cookie.setSecure(true);
		}
		cookie.setMaxAge(60 * 60 * 24 * 30); // store 30 days
		String cp = request.getContextPath();
		if (cp == null || "/".equals(cp))
			cp = "";
		// if path is empty, cookie path will be request path, which causes
		// problems
		if (cp.length() == 0)
			cp = "/";
		cookie.setPath(cp);
		response.addCookie(cookie);
	}

}
