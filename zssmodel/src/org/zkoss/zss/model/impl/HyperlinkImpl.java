/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/01 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.impl;

import org.zkoss.zss.model.util.Validations;

/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public class HyperlinkImpl extends AbstractHyperlinkAdv {

	private static final long serialVersionUID = 1L;
	
	private HyperlinkType _type;
	private String _address;
	private String _label;
	
	public HyperlinkImpl(HyperlinkType type,String address, String label){
		Validations.argNotNull(type,address);
		this._type = type;
		this._address = address;
		this._label = label;
	}
	
	public HyperlinkType getType() {
		return _type;
	}
	public void setType(HyperlinkType type) {
		Validations.argNotNull(type);
		this._type = type;
	}
	public String getAddress() {
		return _address;
	}
	public void setAddress(String address) {
		Validations.argNotNull(address);
		this._address = address;
	}
	public String getLabel() {
		return _label;
	}
	public void setLabel(String label) {
		this._label = label;
	}
	@Override
	public AbstractHyperlinkAdv clone() {
		return new HyperlinkImpl(_type,_address,_label);
	}
}
