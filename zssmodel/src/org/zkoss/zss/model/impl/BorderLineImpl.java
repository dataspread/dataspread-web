/* BorderLine.java

	Purpose:
		
	Description:
		
	History:
		Mar 31, 2015 7:13:20 PM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model.impl;

import org.zkoss.zss.model.SBorder.BorderType;
import org.zkoss.zss.model.SBorderLine;
import org.zkoss.zss.model.SColor;

/**
 * A border line.
 * @author henri
 * @since 3.8.0
 */
public class BorderLineImpl extends AbstractBorderLineAdv implements SBorderLine {
	private static final long serialVersionUID = -541335216584161785L;
	
	private BorderType type;
	private SColor color;
	private boolean showUp;
	private boolean showDown;
	
	//ZSS-977
	public BorderLineImpl(BorderType type, String htmlColor) {
		this(type, htmlColor == null ? ColorImpl.BLACK : new ColorImpl(htmlColor), false, false);
	}
	//ZSS-977
	public BorderLineImpl(BorderType type, SColor color) {
		this(type, color, false, false);
	}

	public BorderLineImpl(BorderType type, SColor color, boolean showUp, boolean showDown) {
		this.type = type;
		this.color = color;
		this.showUp = showUp;
		this.showDown = showDown;
	}

	public BorderType getBorderType() {
		return type;
	}
	
	public void setBorderType(BorderType type) {
		this.type = type;
	}
	
	public SColor getColor() {
		return color;
	}
	
	public void setColor(SColor color) {
		this.color = color;
	}
	
	public boolean isShowDiagonalUpBorder() {
		return showUp;
	}
	
	public void setShowDiagonalUpBorder(boolean show) {
		showUp = show;
	}
	
	public boolean isShowDiagonalDownBorder() {
		return showDown;
	}
	
	public void setShowDiagonalDownBorder(boolean show) {
		showDown = show;
	}
	
	/*package*/ String getStyleKey() {
		return new StringBuilder()
			.append(type == null ? "" : type.ordinal())
			.append(".").append(color == null ? "" : color.getHtmlColor())
			.append(".").append(showUp ? "1" : "0")
			.append(".").append(showDown ? "1" : "0").toString();
	}
}
