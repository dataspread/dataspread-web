/* 
	Purpose:
		
	Description:
		
	History:
		2013/7/10, Created by dennis

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.ui.dlg;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zss.app.ui.CtrlBase;
import org.zkoss.zul.Window;
/**
 * 
 * @author dennis
 *
 */
public class DlgCtrlBase extends CtrlBase<Window>{
	private static final long serialVersionUID = -20175256603400237L;
	private final static String ARG_CALLBACK = "callback";
	protected EventListener callback;
	
	public DlgCtrlBase() {
		super(false);
	}
	
	protected static Map newArg(EventListener<DlgCallbackEvent> callback) {
		Map arg = new HashMap();
		arg.put(ARG_CALLBACK, callback);
		return arg;
	}
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		
		callback = (EventListener)Executions.getCurrent().getArg().get(ARG_CALLBACK);
		if(callback==null){
			throw new UiException("callback for dialog not found");
		}
		
		comp.addEventListener("onCallback", new SerializableEventListener<Event>() {
			private static final long serialVersionUID = 9121671747666631895L;

			public void onEvent(Event event) throws Exception {
				Object[] data = (Object[])event.getData();
				DlgCallbackEvent evt = new DlgCallbackEvent((String)data[0],(Map<String,Object>)data[1]);
				callback.onEvent(evt);
			}
		});
	}
	
	protected void postCallback(String eventName,Map<String,Object> data){
		Events.postEvent("onCallback",getSelf(),new Object[]{eventName,data});
		
	}
	protected void sendCallback(String eventName,Map<String,Object> data){
		Events.sendEvent("onCallback",getSelf(),new Object[]{eventName,data});
	}
	
	protected void detach(){
		getSelf().detach();
	}
}
