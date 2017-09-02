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
import org.zkoss.util.Pair;
import org.zkoss.util.resource.Labels;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.IllegalOpArgumentException;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Range.DeleteShift;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.impl.Hybrid_Model;
import org.zkoss.zss.model.impl.Model;
import org.zkoss.zss.model.impl.TOM_Model;
import org.zkoss.zss.ui.UserActionContext;
import org.zkoss.zss.ui.impl.undo.DeleteCellAction;
import org.zkoss.zss.ui.sys.UndoableActionManager;
import org.zkoss.zul.Messagebox;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * @author dennis
 */
public class AppendTableRowHandler extends AbstractHandler {
    //private static final long serialVersionUID = -3250068182764513758L;

    /* (non-Javadoc)
     * @see org.zkoss.zss.ui.sys.ua.impl.AbstractHandler#processAction(org.zkoss.zss.ui.UserActionContext)
     */
    @Override
    protected boolean processAction(UserActionContext ctx) {
        Sheet sheet = ctx.getSheet();
        AreaRef selection = ctx.getSelection();
        Hybrid_Model dataModel = (Hybrid_Model) sheet.getInternalSheet().getDataModel();
        CellRegion cellRegion = new CellRegion(selection.getRow(),
                selection.getColumn(),
                selection.getLastRow(),
                selection.getLastColumn());
        if (dataModel != null) {


            // Check if there is a table with same dimensions above, and get table name.
            Pair<CellRegion, Model> cellRegionModelPair = dataModel.getTableModelAbove(cellRegion);
            if (cellRegionModelPair == null) {
                Messagebox.show("Select rows below an existing table", "Add Tuples",
                        Messagebox.OK, Messagebox.ERROR);
                return false;
            }

            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
                DBContext dbContext = new DBContext(connection);
                List<Integer> oidList = dataModel.appendTableRows(dbContext, cellRegion,
                        cellRegionModelPair.y.getTableName());
                ((TOM_Model) cellRegionModelPair.y).insertOIDs(dbContext, oidList);
                //TOM_Mapping.instance.getTableOrder("", cellRegionModelPair.y.getTableName())
                dataModel.extendRange(dbContext, cellRegionModelPair.y.getTableName(),
                        cellRegionModelPair.x, cellRegion);
                connection.commit();
            } catch (SQLException e) {
                e.printStackTrace();

            }


        }

        Range range = Ranges.range(sheet, selection);
        range = range.toRowRange();
        //work around for ZSS-404 JS Error after insert column when freeze
        if (checkInCornerFreezePanel(range)) {
            throw new IllegalOpArgumentException(Labels.getLabel("zss.msg.operation_not_supported_with_freeze_panel"));
        }
        UndoableActionManager uam = ctx.getSpreadsheet().getUndoableActionManager();
        uam.doAction(new DeleteCellAction(Labels.getLabel("zss.undo.deleteRow"), sheet, range.getRow(), range.getColumn(),
                range.getLastRow(), range.getLastColumn(),
                DeleteShift.UP));
        ctx.clearClipboard();
        return true;
    }

    @Override
    public boolean isEnabled(Book book, Sheet sheet) {
        return true;
        //return book != null && sheet != null && ( !sheet.isProtected() ||
        //		Ranges.range(sheet).getSheetProtection().isDeleteRowsAllowed());
    }

}
