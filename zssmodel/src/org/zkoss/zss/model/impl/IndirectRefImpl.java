/* IndirectRefImpl.java

	Purpose:
		
	Description:
		
	History:
		Nov 21, 2014 11:40:51 AM, Created by henrichen

	Copyright (C) 2014 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model.impl;

import org.zkoss.zss.model.sys.dependency.IndirectRef;
import org.zkoss.zss.model.sys.dependency.Ref;

/**
 * @author henri
 * @since 3.7.0
 */
public class IndirectRefImpl extends RefImpl implements IndirectRef {
	private static final long serialVersionUID = -5188384456353088778L;
	final int _ptgIndex;
	
	public IndirectRefImpl(String bookName, String sheetName, int ptgIndex) {
		super(RefType.INDIRECT, bookName, sheetName, null, -1, -1,-1,-1);
		_ptgIndex = ptgIndex;
	}
	
	//--IndirectRef--//
	@Override
	public int getPtgIndex() {
		return _ptgIndex;
	}

	// We don't override hashCode() and equals(); each instance is unique.
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		if(sheetName!=null){
			sb.append(sheetName).append(":");
		}
		sb.append("INDIRECT@").append(hashCode());
		return sb.toString();
	}
}
