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
import org.zkoss.zss.model.impl.Hybrid_Model;
import org.zkoss.zss.model.impl.Model;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Albatool on 4/11/2017.
 */
public class createTableCtrl extends DlgCtrlBase {
    private static final long serialVersionUID = 1L;

    private final static String URI = "~./zssapp/dlg/createTable.zul";

    public static final String ON_OPEN = "onOpen";

    @Wire
    private Textbox tableName;
    @Wire
    private Window createTableDlg;

    private static Spreadsheet sss;
    private AreaRef selection;
    private Sheet sheet;

    private Table tableObj = new Table();

    private String name;
    private String rangeRef;
    private String bookName;

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

        bookName=sss.getBook().getBookName();
        rangeRef = Ranges.getAreaRefString(sheet, selection.getRow(), selection.getColumn(), selection.getLastRow(), selection.getLastColumn());

    }

    @Listen("onClick = #createButton")
    public void create() {
        boolean created=false;
        name = tableName.getValue();
        try {
            if (name.isEmpty()) {
                Messagebox.show("Table Name is Required", "Table Name",
                        Messagebox.OK, Messagebox.ERROR);
                return;
            }

            CellRegion region = new CellRegion(selection.getRow(), selection.getColumn(),
                    selection.getLastRow(), selection.getLastColumn());
            Hybrid_Model model = (Hybrid_Model) sheet.getInternalSheet().getDataModel();
            if (model.checkOverap(region))
            {
                Messagebox.show("Table Range Overlaps with Existing Table.", "Create Table",
                        Messagebox.OK, Messagebox.ERROR);
                createTableDlg.detach();
                return;
            }

            Connection connection = DBHandler.instance.getConnection();
            DBContext dbContext = new DBContext(connection);
            created=model.convert(dbContext, Model.ModelType.TOM_Model, region,name);
            connection.commit();
            sheet.getInternalSheet().clearCache(region);
            sss.updateCell(selection.getColumn(), selection.getRow(), selection.getLastColumn(),
                    selection.getLastRow());


        } catch (SQLException e) {
            e.printStackTrace();

        }
        createTableDlg.detach();
        if(created)
        {
            Messagebox.show("Table " + name.toUpperCase() + " is Successfully Created", "Table Creation",
                    Messagebox.OK, Messagebox.INFORMATION);
        }
        else
        {
            Messagebox.show("Error in Creating Table", "Table Creation",
                    Messagebox.OK, Messagebox.ERROR);
        }


    }

    @Listen("onClick = #cancelButton")
    public void cancel() {

        createTableDlg.detach();
    }


}
