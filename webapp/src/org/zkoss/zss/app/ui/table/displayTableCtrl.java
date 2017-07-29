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
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.impl.CellImpl;
import org.zkoss.zss.model.impl.Hybrid_Model;
import org.zkoss.zss.model.impl.Model;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zul.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
    private Range src;


    private String name;
    private String rangeRef;
    private String bookName;

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
        src = Ranges.range(sheet, selection.getRow(), selection.getColumn(), selection.getLastRow(), selection.getLastColumn());

        kshkool();

    }

    private ListModelList<String> getTablesList() throws SQLException {

        Connection connection = DBHandler.instance.getConnection();
        Statement stmt = connection.createStatement();

        String sql = "SELECT table_name FROM information_schema.tables  WHERE TABLE_SCHEMA='public' AND table_name NOT IN ('users', 'usertables', 'books') EXCEPT  " +
                "SELECT table_name FROM information_schema.tables, public.books WHERE table_name LIKE booktable||'%'";

        ResultSet result = stmt.executeQuery(sql);

        while (result.next()) {
            tablesList.add(result.getString(1));
        }
        connection.close();

        return tablesList;
    }

    private void getSelectedTable()
    {

    }


    @Listen("onClick = #createButton")
    public void create() {

        name = tablesBox.getSelectedItem().getLabel();

        if (!name.isEmpty()) {

            Table tableObj= new Table();
            try {
                int rowCount=selection.getLastRow()-selection.getRow()+1;
                int colCount=selection.getLastColumn()-selection.getColumn()+1;

                ArrayList<ArrayList<String>> set=tableObj.getDisplayTable(name,colCount ,rowCount);

//                if(!set.wasNull())
//                {
                colCount=set.get(0).size();
                rowCount=set.size();

                    for (int row = 0 ; row < rowCount ; row++ ){

                        for (int column  = 0 ; column < colCount ; column++){

                        Range range = Ranges.range(sheet, row+selection.getRow(), column+selection.getColumn());
                        range.setAutoRefresh(false);
                        range.getCellData().setEditText(set.get(row).get(column).toString());
                        }
                    }
                    Range x=Ranges.range(sheet, selection.getRow(), selection.getColumn(), selection.getRow()+rowCount-1, selection.getColumn()+colCount-1);
                    x.notifyChange();
                    CellOperationUtil.applyBorder(x, Range.ApplyBorderType.FULL, CellStyle.BorderType.THICK, "#000000");
                    CellOperationUtil.applyBackColor(x, "#c5f0e7");

//                }
                String rangeRef = Ranges.getAreaRefString(sheet, selection.getRow(), selection.getColumn(), selection.getRow()+rowCount-1, selection.getColumn()+colCount-1);

                tableObj.insertUserTable(name,sheet.getBook().getBookName(),rangeRef);


            } catch (SQLException e) {
                e.printStackTrace();
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





    private void kshkool() throws SQLException {


//        for (int column  = 0 ; column < 5 ; column++){
//            for (int row = 0 ; row < 6 ; row++ ){
//                Range range = Ranges.range(sheet, row, column);
//                range.setAutoRefresh(false);
//                range.getCellData().setEditText(row+", "+column);
//                CellOperationUtil.applyFontColor(range, "#0099FF");
//                CellOperationUtil.applyAlignment(range, CellStyle.Alignment.CENTER);
//            }
//        }
//        Ranges.range(sss.getSelectedSheet(), 0, 0, 5, 5).notifyChange();



        //        selection.
//
//        Connection connection = DBHandler.instance.getConnection();
//        DBContext dbContext = new DBContext(connection);
//        CellRegion cr= new CellRegion(selection.getRow(), selection.getColumn(), selection.getLastRow(), selection.getLastColumn());
//
//        cr.
//
//        Collection<AbstractCellAdv> cells=sheet.getInternalSheet().getDataModel().getCells(dbContext, cr);




        Connection connection = DBHandler.instance.getConnection();
        DBContext dbContext = new DBContext(connection);

        CellRegion region= new CellRegion(selection.getRow(), selection.getColumn(), selection.getLastRow(), selection.getLastColumn());
        Hybrid_Model model=(Hybrid_Model)sheet.getInternalSheet().getDataModel();

//        model.getCells(dbContext, region);

        model.convert(dbContext, Model.ModelType.TOM_Model,region);
////        sheet.notify();
//
//        connection.commit();
//connection.close();
//        sheet.getInternalSheet().getDataModel().getCells(dbContext,region);


//        sheet.getInternalSheet().getDataModel().getCells()



//        Connection connection = DBHandler.instance.getConnection();
//        Statement stmt = connection.createStatement();
//
//        String tableName=tablesBox.getSelectedItem().toString();
//        String sql = "SELECT * FROM "+tableName;



//        for (int column  = 0 ; column < 5 ; column++){
//            for (int row = 0 ; row < 6 ; row++ ){
//                Range range = Ranges.range(sheet, row, column);
//                range.setAutoRefresh(false);
//                range.getCellData().setEditText(row+", "+column);
//                CellOperationUtil.applyFontColor(range, "#0099FF");
//                CellOperationUtil.applyAlignment(range, CellStyle.Alignment.CENTER);
//            }
//        }
//        Ranges.range(sss.getSelectedSheet(), 0, 0, 5, 5).notifyChange();


//        Collection<AbstractCellAdv> cells = new ArrayList<>();
//
//        SSheet ssheet= sss.getSelectedSSheet();
//        SCell scell=ssheet.getCell(3,10);
//        scell.setValueParse("1991",connection,false);
//
//
//        DBContext dbContext = new DBContext(connection);
////        CellRegion cr= new CellRegion(selection.getRow(), selection.getColumn(), selection.getLastRow(), selection.getLastColumn());
//
//
//        AbstractCellAdv acell= (AbstractCellAdv) scell;
//        cells.add(acell);
//
//
//
//        //        Collection<AbstractCellAdv> cells=sheet.getInternalSheet().getDataModel().getCells(dbContext, cr);
//
//
//
//        sheet.getInternalSheet().getDataModel().updateCells(dbContext, cells);
//
//
//
////        String tableName=tablesBox.getSelectedItem().toString();
//
//
//
//
//
////        String sql = "SELECT * FROM "+tableName;
//
////        ResultSet result = stmt.executeQuery(sql);
////
//        while (result.next()) {
//
//            AbstractCellAdv cell = new CellImpl(1,1);
//            cell.setValueParse("hello",dbContext.getConnection(),false);
//        }
//        connection.commit();
//        connection.close();
//
//        sheet.notify();


    }

}
