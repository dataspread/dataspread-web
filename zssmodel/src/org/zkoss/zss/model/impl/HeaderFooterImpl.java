/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.impl;

import java.io.Serializable;

import org.zkoss.zss.model.SFooter;
import org.zkoss.zss.model.SHeader;


/**
 * 
 * @author Dennis
 * @since 3.5.0
 */
public class HeaderFooterImpl implements SHeader,SFooter,Serializable{

	private static final long serialVersionUID = 1L;
	
	private String _leftText = "";
	private String _rightText = "";
	private String _centerText = "";
	
	public String getLeftText() {
		return _leftText;
	}
	public void setLeftText(String leftText) {
		this._leftText = leftText;
	}
	public String getRightText() {
		return _rightText;
	}
	public void setRightText(String rightText) {
		this._rightText = rightText;
	}
	public String getCenterText() {
		return _centerText;
	}
	public void setCenterText(String centerText) {
		this._centerText = centerText;
	}
	
	//ZSS-688
	//@since 3.6.0
	/*package*/ HeaderFooterImpl cloneHeaderFooterImpl() {
		final HeaderFooterImpl tgt = new HeaderFooterImpl();
		tgt._leftText = this._leftText;
		tgt._rightText = this._rightText;
		tgt._centerText = this._centerText;
		
		return tgt;
	}
}
