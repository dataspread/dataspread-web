/* ZssContext.java

	Purpose:
		
	Description:
		
	History:
		Dec 30, 2011 12:26:28 PM, Created by henri

Copyright (C) 2011 Potix Corporation. All Rights Reserved.
*/
package org.zkoss.poi.ss.usermodel;

import java.util.Locale;
import org.zkoss.util.Locales;

/**
 * ZK Spreadsheet context for cell evaluation, etc. for Locale and Two Digit Year interpretation.
 * @author henrichen@zkoss.org
 *
 */
public class ZssContext { //ZSS-68
	final private Locale locale;
	final private int twoDigitYearUpperBound;
	/* @param locale current locale for ZK Spreadsheet context evaluation
	 * @param twoDigitYearUpperBound specify the upper bound year a two 
	 * digit year shall be interpreted. e.g. if you specify 2029 in this 
	 * parameter, it means (1930~2029) is the legal two digit year range. 
	 * Note that 1930 is the lower bound of the 100 years range( Note 
	 * 1930 = (2029 - 100 + 1)). That is, two digit year 30~99 would be 
	 * interpreted as 1930~1999 while 00~29 would be interpreted as 
	 * 2000~2029 as in the legal 100 years range. If you set this value
	 * to less than 1999, and the system will pick the range per the 
	 * current year for you automatically(20 years after this tenth year).
	 */
	public ZssContext(Locale locale, int twoDigitYearUpperBound) {
		this.locale = locale;
		this.twoDigitYearUpperBound = twoDigitYearUpperBound;
	}
	public Locale getLocale() {
		return locale;
	}
	public int getTwoDigitYearUpperBound() {
		return twoDigitYearUpperBound;
	}
	private final static
		InheritableThreadLocal<ZssContext> _thdLocale = new InheritableThreadLocal<ZssContext>();

	/** Returns the current ZK Spreadsheet Context; never null.
	 * This is the ZK Spreadsheet Context that every other objects shall use,
	 * unless they have special consideration.
	 *
	 * <p>Default: If {@link #setThreadLocal} was called with non-null,
	 * the value is returned. Otherwise, a default context is created and returned,
	 */
	public static final ZssContext getCurrent() {
		final ZssContext l = (ZssContext)_thdLocale.get();
		return l != null ? l: new ZssContext(Locales.getCurrent(), -1);
	}
	/**
	 * Sets the locale for the current thread only.
	 *
	 * <p>Each thread could have an independent ZssContext, called
	 * the thread locale.
	 *
	 * <p>When Invoking this method under a thread that serves requests,
	 * remember to clean up the setting upon completing each request.
	 *
	 * <pre><code>ZssContext old = ZssContext.setThreadLocal(newValue);
	 *try { 
	 *  ...
	 *} finally {
	 *  ZssContext.setThreadLocal(old);
	 *}</code></pre>
	 *
	 * @param ctx the thread ZssContext; null to denote no thread zssContext
	 * @return the previous thread locale
	 */
	public static final ZssContext setThreadLocal(ZssContext ctx) {
		final ZssContext old = _thdLocale.get();
		_thdLocale.set(ctx);
		return old;
	}
	/**
	 * Returns the ZK Spreadsheet Contextdefined by {@link #setThreadLocal}.
	 *
	 * @see #getCurrent
	 */
	public static final ZssContext getThreadLocal() {
		return _thdLocale.get();
	}
}
