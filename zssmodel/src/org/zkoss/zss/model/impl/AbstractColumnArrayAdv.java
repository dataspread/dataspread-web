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

import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SColumnArray;
/**
 * 
 * @author Dennis
 * @since 3.5.0
 */
public abstract class AbstractColumnArrayAdv implements SColumnArray,LinkedModelObject,Serializable{
	private static final long serialVersionUID = 1L;
//	/*package*/ abstract void onModelInternalEvent(ModelInternalEvent event);
	/*package*/ abstract void setIndex(int index);
	/*package*/ abstract void setLastIndex(int lastIndex);
}
