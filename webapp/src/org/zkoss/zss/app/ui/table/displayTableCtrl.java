package org.zkoss.zss.app.ui.table;

import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.CellOperationUtil;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.model.CellStyle;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.app.ui.dlg.DlgCallbackEvent;
import org.zkoss.zss.app.ui.dlg.DlgCtrlBase;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.impl.*;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zul.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * Created by Albatool on 4/11/2017.
 */
public class displayTableCtrl extends DlgCtrlBase {
    private static final long serialVersionUID = 1L;

    private final static String URI = "~./zssapp/dlg/displayTable.zul";

    public static final String ON_OPEN = "onOpen";

    @Wire
    private Combobox tablesBox;
    @Wire
    private Window displayTableDlg;

    private static Spreadsheet sss;
    private AreaRef selection;
    private Sheet sheet;

    //private Table tableObj = new Table();


    private ListModelList<String> tablesList = new ListModelList<String>();

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
        tablesBox.setModel(getTablesList());
        selection = sss.getSelection();
        sheet = sss.getSelectedSheet();

    }

    private ListModelList<String> getTablesList() throws SQLException {
        try (
        Connection connection = DBHandler.instance.getConnection();
        Statement stmt = connection.createStatement();) {

            String sql = "SELECT table_name FROM information_schema.tables  " +
                    "WHERE TABLE_SCHEMA='public' AND " +
                    "table_name NOT IN ('users', 'usertables', 'books') AND table_name NOT LIKE '%_idx' " +
                    "EXCEPT SELECT table_name FROM information_schema.tables, public.books " +
                    "  WHERE table_name LIKE booktable||'%'";

            ResultSet result = stmt.executeQuery(sql);

            while (result.next()) {
                tablesList.add(result.getString(1));
            }
        }
        return tablesList;
    }

    @Listen("onClick = #okButton")
    public void display() throws SQLException {
        if (tablesBox.getSelectedItem()==null)
        {
            Messagebox.show("Table Name is Required", "Table Name",
                    Messagebox.OK, Messagebox.ERROR);
            return;
        }
        String tableName =  tablesBox.getSelectedItem().getLabel();
        if (tableName!=null && !tableName.isEmpty()) {
            try(Connection connection = DBHandler.instance.getConnection())
            {
                DBContext dbContext = new DBContext(connection);
                CellRegion region = new CellRegion(selection.getRow(), selection.getColumn(), selection.getLastRow(), selection.getLastColumn());
                // Make sure the sheet is saved
                sheet.getBook().getInternalBook().checkDBSchema();
                Hybrid_Model model = (Hybrid_Model) sheet.getInternalSheet().getDataModel();


                model.linkTable(dbContext, tableName, region);
                connection.commit();

                sheet.getInternalSheet().clearCache(region);
                sss.updateCell(selection.getColumn(), selection.getRow(), selection.getLastColumn(), selection.getLastRow());
                displayTableDlg.detach();
            }
        } else {
            Messagebox.show("Table Name is Required", "Table Name",
                    Messagebox.OK, Messagebox.ERROR);
        }
    }

    @Listen("onClick = #cancelButton")
    public void cancel() {
        displayTableDlg.detach();
    }
}