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

        Connection connection = DBHandler.instance.getConnection();
        Statement stmt = connection.createStatement();

        String sql = "SELECT table_name FROM information_schema.tables  WHERE TABLE_SCHEMA='public' AND table_name NOT IN ('users', 'usertables', 'books') AND table_name NOT LIKE '%_idx' " +
                "EXCEPT SELECT table_name FROM information_schema.tables, public.books WHERE table_name LIKE booktable||'%'";

        ResultSet result = stmt.executeQuery(sql);

        while (result.next()) {
            tablesList.add(result.getString(1));
        }
        connection.close();

        return tablesList;
    }

    @Listen("onClick = #okButton")
    public void display() throws SQLException {
        String tableName =  tablesBox.getSelectedItem().getLabel();
        if (tableName!=null && !tableName.isEmpty()) {
            try(Connection connection = DBHandler.instance.getConnection())
            {
                DBContext dbContext = new DBContext(connection);
                CellRegion region = new CellRegion(selection.getRow(), selection.getColumn(), selection.getLastRow(), selection.getLastColumn());
                // Make sure the sheet is saved
                sheet.getBook().getInternalBook().checkDBSchema();
                Hybrid_Model model = (Hybrid_Model) sheet.getInternalSheet().getDataModel();


                CellRegion range=model.linkTable(dbContext, tableName, region);
                String rangeRef = Ranges.getAreaRefString(sheet, range.getRow(), range.getColumn(), range.getLastRow(), range.getLastColumn());
                //tableObj.insertUserTable(tableName,sheet.getBook().getBookName(),rangeRef);

                connection.commit();

                sheet.getInternalSheet().clearCache(region);
                sss.updateCell(selection.getColumn(), selection.getRow(), selection.getLastColumn(), selection.getLastRow());

//                Range src=Ranges.range(sheet,rangeRef);
//                CellOperationUtil.applyBorder(src, Range.ApplyBorderType.FULL, CellStyle.BorderType.THICK, "#000000");
//                CellOperationUtil.applyBackColor(src, "#c5f0e7");
            }
        } else {
            Messagebox.show("Table Name is Required", "Table Name",
                    Messagebox.OK, Messagebox.ERROR);

        }
        displayTableDlg.detach();
    }

    @Listen("onClick = #cancelButton")
    public void cancel() {

        displayTableDlg.detach();
    }
}