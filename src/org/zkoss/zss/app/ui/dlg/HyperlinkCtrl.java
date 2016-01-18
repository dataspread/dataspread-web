/* HyperlinkCtrl.java

	Purpose:
		
	Description:
		
	History:
		Tue, Jul 01, 2014  2:32:35 PM, Created by RaymondChao

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.ui.dlg;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.lang.Strings;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zss.api.model.Hyperlink;
import org.zkoss.zss.api.model.Hyperlink.HyperlinkType;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * 
 * @author RaymondChao
 */
public class HyperlinkCtrl extends DlgCtrlBase {
	private static final long serialVersionUID = 1L;
	
	public final static String ARG_ADDRESS = "address";
	public final static String ARG_DISPLAY = "display";
	
	public static final String ON_OK = "onOk";
	
	private String linkAddress;
	private String linkDisplay;
	
	@Wire
	private Textbox displayBox;
	
	@Wire
	private Textbox addressBox;
	
	private final static String URI = "~./zssapp/dlg/hyperlink.zul";
	
	public static void show(EventListener<DlgCallbackEvent> callback, String address, String display) {
		Map arg = newArg(callback);
		arg.put(ARG_DISPLAY, display);
		arg.put(ARG_ADDRESS, address);
		Window comp = (Window)Executions.createComponents(URI, null, arg);
		comp.doModal();
		return;
	}
	
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		
		Map arg = Executions.getCurrent().getArg();
		
		linkDisplay = (String) arg.get(ARG_DISPLAY);
		linkAddress = (String)arg.get(ARG_ADDRESS);
		
		if(!Strings.isBlank(linkDisplay)){
			displayBox.setValue(linkDisplay);
		}
		
		if(!Strings.isBlank(linkAddress)){
			addressBox.setValue(linkAddress);
		}
	}
	
	@Listen("onClick=#ok; onOK=#hyperlinkDlg")
	public void onOk(){
		final String address = addressBox.getValue();
		final String display = displayBox.getValue();
		if (Strings.isBlank(address)){
			addressBox.setErrorMessage("empty name is not allowed");
			return;
		}
		postCallback(ON_OK, newMap(newEntry(ARG_ADDRESS, address), newEntry(ARG_DISPLAY,
				Strings.isBlank(display) ? address : display)));
		detach();
	}
	
	@Listen("onClick=#cancel; onOK=#hyperlinkDlg")
	public void onCancel(){
		detach();
	}
}