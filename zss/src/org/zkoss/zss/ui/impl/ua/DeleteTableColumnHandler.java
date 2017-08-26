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

import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.impl.Model;
import org.zkoss.zss.ui.UserActionContext;
import org.zkoss.zul.Messagebox;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author dennis
 */
public class DeleteTableColumnHandler extends AbstractHandler {
    //private static final long serialVersionUID = -3250068182764513758L;

    /* (non-Javadoc)
     * @see org.zkoss.zss.ui.sys.ua.impl.AbstractHandler#processAction(org.zkoss.zss.ui.UserActionContext)
     */
    @Override
    protected boolean processAction(UserActionContext ctx) {
        Sheet sheet = ctx.getSheet();
        AreaRef selection = ctx.getSelection();
        Model dataModel = sheet.getInternalSheet().getDataModel();
        if (dataModel != null) {
            try (Connection connection = DBHandler.instance.getConnection()) {
                DBContext dbContext = new DBContext(connection);

                boolean ret = dataModel.deleteTableColumns(dbContext, new CellRegion(selection.getRow(),
                        selection.getColumn(),
                        selection.getLastRow(),
                        selection.getLastColumn()));
                connection.commit();
                if (!ret)
                    Messagebox.show("Selected region does not correspond to a table", "Delete Columns",
                            Messagebox.OK, Messagebox.ERROR);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}
