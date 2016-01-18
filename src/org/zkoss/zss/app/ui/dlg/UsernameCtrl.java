/* 
	Purpose:
		
	Description:
		
	History:
		2014/11/27, Created by JerryChen

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.ui.dlg;

import java.util.Map;

import org.zkoss.lang.Strings;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * 
 * @author JerryChen
 *
 */
public class UsernameCtrl extends DlgCtrlBase{
	private static final long serialVersionUID = 1L;
	
	public final static String ARG_NAME = "username";
	public final static String MESSAGE = "message";
	
	public static final String ON_USERNAME_CHANGE = "onUsernameChange";
	
	@Wire
	Textbox username;
	
	private final static String URI = "~./zssapp/dlg/username.zul";
	
	public static void show(EventListener<DlgCallbackEvent> callback, String username, String message) {
		Map arg = newArg(callback);
		
		arg.put(ARG_NAME, username);
		arg.put(MESSAGE, message);
		
		Window comp = (Window)Executions.createComponents(URI, null, arg);
		comp.doModal();
		return;
	}
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		Map<?, ?> args = Executions.getCurrent().getArg();
		if(args.containsKey(ARG_NAME))
			username.setValue((String) args.get(ARG_NAME));
		if(args.containsKey(MESSAGE))
			username.setErrorMessage((String) args.get(MESSAGE));
	}

	@Listen("onClick=#ok; onOK=#usernameDlg")
	public void onSave(){
		if(Strings.isBlank(username.getValue())){
			username.setErrorMessage("empty name is not allowed");
			return;
		}
		postCallback(ON_USERNAME_CHANGE, newMap(newEntry(ARG_NAME, username.getValue())));
		detach();
	}
}
