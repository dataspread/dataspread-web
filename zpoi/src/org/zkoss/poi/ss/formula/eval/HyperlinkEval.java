/* HyperlinkEval.java

	Purpose:
		
	Description:
		
	History:
		Jul 20, 2010 10:47:23 AM, Created by henrichen

Copyright (C) 2010 Potix Corporation. All Rights Reserved.

*/

package org.zkoss.poi.ss.formula.eval;

import org.zkoss.poi.ss.usermodel.Hyperlink;

/**
 * Handle associated hyperlink if this ValueEval is a label of the HYPERLINK function.
 * @author henrichen
 *
 */
public interface HyperlinkEval {
	/**
	 * Sets the associated hyperlink if this ValueEval is a label of the HYPERLINK function.
	 * @param hyperlink the associated hyperlink 
	 */
	public void setHyperlink(Hyperlink hyperlink);
	
	/**
	 * Returns the associated hyperlink if this ValueEval is a label of the HYPERLINK function.
	 * @return the associated hyperlink.
	 */
	public Hyperlink getHyperlink();
}
