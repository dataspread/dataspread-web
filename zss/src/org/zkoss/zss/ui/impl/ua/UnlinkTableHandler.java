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

import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.impl.Hybrid_Model;
import org.zkoss.zss.model.impl.SheetImpl;
import org.zkoss.zss.ui.UserActionContext;

import java.sql.Connection;
import java.sql.SQLException;

public class UnlinkTableHandler extends AbstractHandler {

    @Override
    protected boolean processAction(UserActionContext ctx) {
        Sheet sheet = ctx.getSheet();
        AreaRef selection = ctx.getSelection();
        Hybrid_Model dataModel = (Hybrid_Model) sheet.getInternalSheet().getDataModel();
        CellRegion cellRegion = new CellRegion(selection.getRow(),
                selection.getColumn(),
                selection.getLastRow(),
                selection.getLastColumn());

        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            DBContext dbContext = new DBContext(connection);
            dataModel.unlinkTable(dbContext, cellRegion);
            dbContext.getConnection().commit();
            ((SheetImpl) sheet.getInternalSheet()).clearCache();
            ((SheetImpl) sheet.getInternalSheet()).fullRefresh();
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return true;

    }
}
