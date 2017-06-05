/* 
	Purpose:
		
	Description:
		
	History:
		2013/7/10, Created by dennis

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.ui.dlg;

import java.util.Map;

import org.zkoss.zk.ui.event.Event;
/**
 * 
 * @author dennis
 *
 */
public class DlgCallbackEvent extends Event{
	private static final long serialVersionUID = 1574572906717059919L;

	public DlgCallbackEvent(String name, Map<String,Object> data) {
		super(name, null, data);
	}
	
	public Map<String,Object> getData(){
		return (Map<String,Object>)super.getData();
	}
	
	public Object getData(String name){
		Map<String,Object> data = getData();
		if(data!=null){
			return data.get(name);
		}
		return null;
	}

}
