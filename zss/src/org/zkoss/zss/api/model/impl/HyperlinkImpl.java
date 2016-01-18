/* HyperlinkImpl.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/5/1 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.api.model.impl;

import org.zkoss.zss.api.model.Hyperlink;
import org.zkoss.zss.model.SHyperlink;

/**
 * 
 * @author dennis
 * @since 3.0.0
 */
public class HyperlinkImpl implements Hyperlink{

	private ModelRef<SHyperlink> _linkRef;
	private String _label;

	public HyperlinkImpl(ModelRef<SHyperlink> linkRef,String label) {
		this._linkRef = linkRef;
		this._label = label;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_linkRef == null) ? 0 : _linkRef.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HyperlinkImpl other = (HyperlinkImpl) obj;
		if (_linkRef == null) {
			if (other._linkRef != null)
				return false;
		} else if (!_linkRef.equals(other._linkRef))
			return false;
		return true;
	}

	public SHyperlink getNative() {
		return _linkRef.get();
	}
	
	@Override
	public HyperlinkType getType() {
		return EnumUtil.toHyperlinkType(getNative().getType());
	}

	@Override
	public String getAddress() {
		return getNative().getAddress();
	}

	@Override
	public String getLabel() {
		return _label;
	}
}
