package org.zkoss.zss.ui;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.sys.formula.FormulaAsyncUIController;
import org.zkoss.zss.ui.sys.SpreadsheetCtrl;

import java.util.HashMap;

/**
 * Created by zekun.fan@gmail.com on 7/19/17.
 */
public class FormulaAsyncUIControllerImpl implements FormulaAsyncUIController {

    private HashMap<SCell,AsyncCellInfo> cellUIBinding;

    public FormulaAsyncUIControllerImpl(){
        cellUIBinding=new HashMap<>();
    }

    @Override
    public synchronized void prepare(SCell cell, Object spreadsheet){
        if (cellUIBinding.containsKey(cell)){
            ++cellUIBinding.get(cell).refcnt;
        }else{
            cellUIBinding.put(cell,new AsyncCellInfo((Spreadsheet) spreadsheet,1));
        }
    }

    @Override
    public synchronized void confirm(SCell cell) {
        //nullptr exception if not used in pre-defined manner.
        ++cellUIBinding.get(cell).refcnt;
    }

    @Override
    public void updateAndRelease(SCell cell){
        AsyncCellInfo info;
        synchronized (this) {
            info = cellUIBinding.get(cell);
            --info.refcnt;
            if (info.refcnt==0)
                cellUIBinding.remove(cell);
        }
        /*
        final Map<String, Integer> attrMap = new HashMap<String, Integer>(2);
        attrMap.put("cellAttr", CellAttribute.TEXT.value);
        ModelEvents.createModelEvent(ModelEvents.ON_CELL_CONTENT_CHANGE,cell.getSheet(),
                new CellRegion(cell.getRowIndex(),cell.getColumnIndex()), attrMap);
        */
        //Object[] eventData={cell.getSheet(),cell.getColumnIndex(),cell.getRowIndex(),cell.getColumnIndex(),cell.getRowIndex(), SpreadsheetCtrl.CellAttribute.TEXT};
        //org.zkoss.zk.ui.event.Events.postEvent("onAsyncUpdate",info.ui,eventData);
        boolean waiting=true;
        while (waiting) {
            try {
                Executions.activate(info.ui.getDesktop());
                waiting=false;
            } catch (InterruptedException ignored) {
            }
        }
        info.ui.updateCell(cell.getColumnIndex(),cell.getRowIndex(),cell.getColumnIndex(),cell.getRowIndex(), SpreadsheetCtrl.CellAttribute.ALL);
        Executions.deactivate(info.ui.getDesktop());
    }

    @Override
    public synchronized void cancelIfNotConfirmed(SCell cell){
        if (cellUIBinding.containsKey(cell)) {
            AsyncCellInfo info=cellUIBinding.get(cell);
            --info.refcnt;
            if (info.refcnt == 0)
                cellUIBinding.remove(cell);
        }
    }

    private class AsyncCellInfo{
        Spreadsheet ui;
        int refcnt;

        AsyncCellInfo(Spreadsheet ui, int refcnt) {
            this.ui = ui;
            this.refcnt = refcnt;
        }
    }
}
