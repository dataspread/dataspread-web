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
import org.zkoss.util.Pair;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.impl.Hybrid_Model;
import org.zkoss.zss.model.impl.Model;
import org.zkoss.zss.model.impl.TOM_Model;
import org.zkoss.zss.ui.UserActionContext;
import org.zkoss.zul.Messagebox;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * @author dennis
 */
public class AppendTableColumnHandler extends AbstractHandler {
    @Override
    protected boolean processAction(UserActionContext ctx) {
        Sheet sheet = ctx.getSheet();
        AreaRef selection = ctx.getSelection();
        Hybrid_Model dataModel = (Hybrid_Model) sheet.getInternalSheet().getDataModel();
        CellRegion selectedCellRegion = new CellRegion(selection.getRow(),
                selection.getColumn(),
                selection.getLastRow(),
                selection.getLastColumn());
        if (dataModel != null) {
            // Check if there is a table with same height to left, and get table name.
            Pair<CellRegion, Model> cellRegionModelPair = dataModel.getTableModelToLeft(selectedCellRegion);
            if (cellRegionModelPair == null) {
                Messagebox.show("Select a column right of an existing table", "Add Columns",
                        Messagebox.OK, Messagebox.ERROR);
                return false;
            }

            try (Connection connection = DBHandler.instance.getConnection()) {
                DBContext dbContext = new DBContext(connection);

                CellRegion tupleRegion = new CellRegion(selectedCellRegion.getRow() + 1,
                        selectedCellRegion.getColumn(), selectedCellRegion.getLastRow(), selectedCellRegion.getLastColumn());
                Collection<AbstractCellAdv> cells = dataModel.getCells(dbContext, tupleRegion);
                dataModel.appendTableColumn(dbContext, selectedCellRegion, cellRegionModelPair.y.getTableName());
                dataModel.deleteCells(dbContext, selectedCellRegion);
                ((TOM_Model) cellRegionModelPair.y).insertColMappings(dbContext, selectedCellRegion.getLength());
                ((TOM_Model) cellRegionModelPair.y).loadColumnInfo(dbContext);
                dataModel.extendRange(dbContext,
                        cellRegionModelPair.y.getTableName(),
                        cellRegionModelPair.x, selectedCellRegion);

                dataModel.updateCells(dbContext, cells);
                connection.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            Messagebox.show("Select a column right of an existing table", "Add Columns",
                    Messagebox.OK, Messagebox.ERROR);
            return false;
        }
        return true;
    }
}
