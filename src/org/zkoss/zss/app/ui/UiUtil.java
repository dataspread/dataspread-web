/* 
	Purpose:
		
	Description:
		
	History:
		2013/7/10, Created by dennis

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.ui;

import org.zkoss.lang.Library;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ext.Selectable;
/**
 * 
 * @author dennis
 *
 */
public class UiUtil {

	public static Object getSingleSelection(Selectable selection){
		if(selection!=null && selection.getSelection().size()>0){
			return selection.getSelection().iterator().next();
		}
		return null;
	}
	
	public static void showInfoMessage(String message,int time) {
		Clients.showNotification(message, Clients.NOTIFICATION_TYPE_INFO, null, null, time, true);
	}
	public static void showInfoMessage(String message) {
		showInfoMessage(message, 5000);
	}
	
	public static void showWarnMessage(String message,int time) {
		Clients.showNotification(message, Clients.NOTIFICATION_TYPE_WARNING, null, null, time, true);
	}
	public static void showWarnMessage(String message) {
		showWarnMessage(message, 5000);
	}
	
	public static boolean isRepositoryReadonly(){
		String readonly = Library.getProperty("zssapp.bookrepostory.readonly","false").toLowerCase();
		return "true".equals(readonly);
	}
}
