/* SBorderLine.java

	Purpose:
		
	Description:
		
	History:
		Apr 1, 2015 3:15:21 PM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model;

import org.zkoss.zss.model.SBorder.BorderType;

/**
 * @author henri
 * @since 3.8.0
 */
public interface SBorderLine {
	BorderType getBorderType();
	void setBorderType(BorderType type);
	SColor getColor();
	void setColor(SColor color);
	boolean isShowDiagonalUpBorder();
	void setShowDiagonalUpBorder(boolean show);
	boolean isShowDiagonalDownBorder();
	void setShowDiagonalDownBorder(boolean show);
}
