/* InserCellRightHandler.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/8/5 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.impl.ua;


import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zss.ui.UserActionContext;

public class CreateTableHandler extends AbstractHandler implements EventListener<DlgCallbackEvent> {

    @Override
    protected boolean processAction(UserActionContext ctx) {
        CreateTableCtrl.show(this, ctx.getSpreadsheet());
        return true;
    }

    @Override
    public void onEvent(DlgCallbackEvent event) throws Exception {

    }
}
