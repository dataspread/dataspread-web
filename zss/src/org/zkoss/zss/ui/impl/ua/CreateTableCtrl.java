package org.zkoss.zss.ui.impl.ua;

import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.impl.Hybrid_Model;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class CreateTableCtrl extends DlgCtrlBase {
    public static final String ON_OPEN = "onOpen";
    private static final long serialVersionUID = 1L;
    private final static String URI = "~./zssapp/dlg/createTable.zul";
    private static Spreadsheet sss;
    @Wire
    private Textbox tableName;
    @Wire
    private Window createTableDlg;
    private AreaRef selection;
    private Sheet sheet;

    private String tableNameStr;

    public static void show(EventListener<DlgCallbackEvent> callback, Spreadsheet ss) {

        sss = ss;

        Map arg = newArg(callback);
        Window comp = (Window) Executions.createComponents(URI, null, arg);
        comp.doModal();
        return;
    }


    @Override
    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);
        selection = sss.getSelection();
        sheet = sss.getSelectedSheet();

    }

    @Listen("onClick = #createButton")
    public void create() {
        boolean created = false;
        tableNameStr = tableName.getValue();
        if (tableNameStr.isEmpty()) {
            Messagebox.show("Table Name is Required", "Table Name",
                    Messagebox.OK, Messagebox.ERROR);
            return;
        }

        CellRegion region = new CellRegion(selection.getRow(), selection.getColumn(),
                selection.getLastRow(), selection.getLastColumn());
        Hybrid_Model model = (Hybrid_Model) sheet.getInternalSheet().getDataModel();
        if (model.checkOverap(region)) {
            Messagebox.show("Table Range Overlaps with Existing Table.", "Create Table",
                    Messagebox.OK, Messagebox.ERROR);
            createTableDlg.detach();
            return;
        }

        try (AutoRollbackConnection connection = DBHandler.instance.getConnection())
        {
            DBContext dbContext = new DBContext(connection);
            model.createTable(dbContext, region, tableNameStr);
            model.appendTableRows(dbContext, new CellRegion(region.getRow() + 1, region.getColumn(),
                    region.getLastRow(), region.getLastColumn()), tableNameStr);
            model.linkTable(dbContext, tableNameStr, new CellRegion(region.getRow(), region.getColumn(),
                    region.getLastRow(), region.getLastColumn()));
            connection.commit();
            sheet.getInternalSheet().clearCache(region);
            sss.updateCell(selection.getColumn(), selection.getRow(), selection.getLastColumn(),
                    selection.getLastRow());
            Messagebox.show("Table " + tableNameStr.toUpperCase() + " is Successfully Created", "Table Creation",
                    Messagebox.OK, Messagebox.INFORMATION);
            createTableDlg.detach();
            return;

        } catch (SQLException e) {
            e.printStackTrace();
            Messagebox.show("Error in Creating Table: " + e.getMessage(), "Table Creation",
                    Messagebox.OK, Messagebox.ERROR);
        }
    }

    @Listen("onClick = #cancelButton")
    public void cancel() {

        createTableDlg.detach();
    }
}
