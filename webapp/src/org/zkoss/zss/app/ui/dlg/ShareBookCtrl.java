/* 
	Purpose:
		
	Description:
		
	History:
		2015/1/12, Created by JerryChen

Copyright (C) 2015 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.ui.dlg;

import java.util.Map;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zss.app.ui.CtrlBase;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * 
 * @author JerryChen
 *
 */
public class ShareBookCtrl extends CtrlBase<Window>{
	
	@Wire
	Textbox url;
	
	public ShareBookCtrl() {
		super(false);
	}

	private static final long serialVersionUID = 1L;
	
	private final static String SHARED_URL = "sharedUrl";
	private final static String URI = "~./zssapp/dlg/shareBook.zul";
	
	public static void show() {
		Execution execution = Executions.getCurrent();
		Desktop desktop = execution.getDesktop();
		String bookmark = desktop.getBookmark();
		String port = (execution.getServerPort() == 80) ? "" : (":" + execution.getServerPort());
		String url = execution.getScheme() + "://" + execution.getServerName() + port + 
				execution.getContextPath() + "#" + bookmark;
		
		Map<String, Object> arg = newMap(newEntry(SHARED_URL, url));
		Window comp = (Window) Executions.createComponents(URI, null, arg);
		comp.doModal();
		return;
	}
	
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		
		Map<?, ?> arg = Executions.getCurrent().getArg();
		
		String sharedUrl = (String) arg.get(SHARED_URL);
		url.setValue(sharedUrl);
		url.select();
	}
	
	@Listen("onClick=#url")
	public void onUrlClick() {
		url.select();
	}

	@Listen("onClick=#done; onOK=#shareBookDlg; onCancel=#shareBookDlg")
	public void onDone() {
		getSelf().detach();
	}
}
