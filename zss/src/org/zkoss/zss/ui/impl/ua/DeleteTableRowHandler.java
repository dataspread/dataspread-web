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

public class DeleteTableRowHandler extends AbstractProtectedHandler {
    @Override
    protected boolean processAction(UserActionContext ctx) {
        boolean success = false;
        Sheet sheet = ctx.getSheet();
        AreaRef selection = ctx.getSelection();
        Model dataModel = sheet.getInternalSheet().getDataModel();
        if (dataModel != null) {
            try (Connection connection = DBHandler.instance.getConnection()) {
                DBContext dbContext = new DBContext(connection);

                success = dataModel.deleteTuples(dbContext, new CellRegion(selection.getRow(),
                        selection.getColumn(),
                        selection.getLastRow(),
                        selection.getLastColumn()));
                connection.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (!success)
            Messagebox.show("Selected region does not correspond to a table", "Delete Tuples",
                    Messagebox.OK, Messagebox.ERROR);
        return success;
    }
}
