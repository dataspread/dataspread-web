package org.zkoss.zss.ui;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.sys.formula.FormulaAsyncUIController;
import org.zkoss.zss.ui.sys.SpreadsheetCtrl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class FormulaAsyncUIControllerImpl implements FormulaAsyncUIController {

    private Map<SBook, Set<Spreadsheet>> dataUIBinding;

    public FormulaAsyncUIControllerImpl(){
        dataUIBinding =new HashMap<>();
    }

    @Override
    public void bind(SBook book, Object spreadsheet) {
        Set<Spreadsheet> records=dataUIBinding.computeIfAbsent(book,k->new CopyOnWriteArraySet<>());
        records.add((Spreadsheet)spreadsheet);
    }

    @Override
    public void unbind(SBook book, Object spreadsheet) {
        Set<Spreadsheet> records=dataUIBinding.get(book);
        if (records!=null)
            records.remove(spreadsheet);
    }

    @Override
    public void update(SSheet sheet, CellRegion region){
        /* Method 1 - Not working
        final Map<String, Integer> attrMap = new HashMap<String, Integer>(2);
        attrMap.put("cellAttr", CellAttribute.TEXT.value);
        ModelEvents.createModelEvent(ModelEvents.ON_CELL_CONTENT_CHANGE,cell.getSheet(),
                new CellRegion(cell.getRowIndex(),cell.getColumnIndex()), attrMap);
        */
        //Object[] eventData={cell.getSheet(),cell.getColumnIndex(),cell.getRowIndex(),cell.getColumnIndex(),cell.getRowIndex(), SpreadsheetCtrl.CellAttribute.TEXT};
        //org.zkoss.zk.ui.event.Events.postEvent("onAsyncUpdate",info.ui,eventData);

        /* Method 2 - Working but with problems
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
        */

        Set<Spreadsheet> records=dataUIBinding.get(sheet.getBook());
        if (records!=null) {
            for (Spreadsheet ui : records) {
                //Method 3
                Executions.schedule(ui.getDesktop(), AsyncUIUpdateEventListener,
                        new AsyncUIUpdateEvent(ui, sheet, region));
            }
        }
    }

    private static EventListener<AsyncUIUpdateEvent> AsyncUIUpdateEventListener=new EventListener<AsyncUIUpdateEvent>() {
        @Override
        public void onEvent(AsyncUIUpdateEvent event){
            Object[] data=(Object[]) event.getData();
            CellRegion region=(CellRegion) data[2];
            //System.out.printf("AsyncUIUpdateEvent %s\n",region.getReferenceString());
            ((Spreadsheet)data[0]).updateCell((SSheet)data[1],region.column,region.row,region.lastColumn,region.lastRow, SpreadsheetCtrl.CellAttribute.ALL);
        }
    };

    private class AsyncUIUpdateEvent extends Event{
        AsyncUIUpdateEvent(Spreadsheet ui, SSheet sheet, CellRegion region) {
            super("AsyncUIUpdateEvent",null,new Object[] {ui,sheet,region});
        }
    }
}
