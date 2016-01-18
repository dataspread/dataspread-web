/* SequenceId.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		May 21, 2008 4:07:00 PM     2008, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.impl;

import java.io.Serializable;

/**
 * @author Dennis.Chen
 *
 */
public class SequenceId implements Serializable{

	int _last;
	int _increase;
	public SequenceId(int init,int increase){
		_last  = init;
		_increase = increase;
	}
	
	synchronized public int next(){
		_last += _increase;
		return _last;
	}
	
	public int last(){
		return _last;
	}
}
