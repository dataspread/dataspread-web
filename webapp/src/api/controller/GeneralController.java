package api.controller;

import api.UISessionManager;
import com.google.common.collect.ImmutableMap;
import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;
import org.zkoss.poi.ss.formula.FormulaComputationStatusManager;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.FormulaCacheCleaner;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncListener;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;
import org.zkoss.zss.range.impl.ModelUpdate;
import org.zkoss.zss.range.impl.ModelUpdateCollector;

import java.util.*;


@RestController
@EnableScheduling
public class GeneralController implements FormulaAsyncListener {
    public GeneralController() {
        FormulaAsyncScheduler.initFormulaAsyncListener(this);
    }

    @Scheduled(fixedDelay = 250)
    public void updateFormulaProgress() {
        Collection<FormulaComputationStatusManager.FormulaComputationStatus> statusSet
                = FormulaComputationStatusManager.getInstance().getCurrentStatus();
        for (FormulaComputationStatusManager.FormulaComputationStatus status : statusSet) {
            Set<UISessionManager.UISession> uiSessionSet =
                    UISessionManager.getInstance().getSessionBySheet(((SCell) status.cell).getSheet());

            for (UISessionManager.UISession uiSession : uiSessionSet) {
                simpMessagingTemplate.convertAndSendToUser(uiSession.getSessionId(),
                        "" +
                                "" +
                                "", ImmutableMap.of("message", "asyncStatus",
                                "data", new Integer[]{status.row, status.column,
                                        (status.currentCells * 100 / status.totalCells)}),
                        createHeaders(uiSession.getSessionId()));
            }
        }
    }

    @Override
    public void update(SBook book, SSheet sheet, CellRegion cellRegion, String value, String formula) {
        List<List<Object>> data = new ArrayList<>();
        List<Object> cellArr = new ArrayList<>(4);
        cellArr.add(cellRegion.getRow());
        cellArr.add(cellRegion.getColumn());
        cellArr.add(value);
        cellArr.add(formula);
        data.add(cellArr);

        //TODO: For each session check if the UI has the cells cached.
        Set<UISessionManager.UISession> uiSessionSet =
                UISessionManager.getInstance().getSessionBySheet(sheet);

        for (UISessionManager.UISession uiSession : uiSessionSet) {
            simpMessagingTemplate.convertAndSendToUser(uiSession.getSessionId(),
                    "/push/updates", ImmutableMap.of("message", "pushCells",
                            "data", data),
                    createHeaders(uiSession.getSessionId()));
        }
    }


    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

    private synchronized void refreshFormulas(UISessionManager.UISession uiSession,
                                                    ArrayList<SCell> formulaCells){
        FormulaAsyncScheduler.formulaUpdateLock.lock();
        for (SCell cell:formulaCells){
            updateCellWithNotfication(uiSession,cell.getRowIndex(),
                    cell.getColumnIndex(),"=" + cell.getFormulaValue());
        }
        FormulaAsyncScheduler.formulaUpdateLock.unlock();
    }

    public void pushCells(UISessionManager.UISession uiSession, int blockNumber) {
        //TODO: Update to directly call the data model.
        // TODO: Improve efficiency.
        SSheet sheet = uiSession.getSheet();
        int endColumn = sheet.getEndColumnIndex();

        int row1 = blockNumber * uiSession.getFetchSize();
        Map<String, Object> ret = new HashMap<>();
        List<List<String[]>> data = new ArrayList<>();
        ArrayList<SCell> formulaCells = new ArrayList<>();
        for (int row = row1; row < row1 + uiSession.getFetchSize(); row++) {
            List<String[]> cellsRow = new ArrayList<>();
            data.add(cellsRow);

            for (int col = 0; col <= endColumn; col++) {
                SCell sCell = sheet.getCell(row, col);
                if (sCell.isNull()) {
                    cellsRow.add(new String[]{""});
                } else if (sCell.getType() == SCell.CellType.FORMULA) {
                    cellsRow.add(new String[]{sCell.getValue().toString(), sCell.getFormulaValue()});
                    formulaCells.add(sCell);
                }
                else
                    cellsRow.add(new String[]{sCell.getValue().toString()});
            }
        }

        ret.put("message", "getCellsResponse");
        ret.put("blockNumber", blockNumber);
        ret.put("data", data);

        uiSession.addCachedBlock(blockNumber);

        // Single cell update
        simpMessagingTemplate.convertAndSendToUser(uiSession.getSessionId(),
                "/push/updates", ret,
                createHeaders(uiSession.getSessionId()));

        new Thread(() -> refreshFormulas(uiSession,formulaCells)).start();

    }


    @MessageMapping("/push/status")
    void clientStatus(@Payload Map<String, Object> payload,
                      SimpMessageHeaderAccessor accessor) {
        UISessionManager.UISession uiSession = UISessionManager.getInstance().getUISession(accessor.getSessionId());
        String message = (String) payload.get("message");

        if (message.equals("changeViewPort")) {
            uiSession.updateViewPort((int) payload.get("rowStartIndex"), (int) payload.get("rowStopIndex"));
            // If viewport not cached, push to FE
            int blockNumber = uiSession.getViewPortBlockNumber();
            for (int i = 0; i < 5; i++) {
                if (!uiSession.isBlockCached(uiSession.getViewPortBlockNumber() + i))
                    pushCells(uiSession, blockNumber + i);
            }
        } else if (message.equals("disposeFromLRU")) {
            uiSession.removeCachedBlock((int) payload.get("blockNumber"));
        } else if (message.equals("updateCell")) {
            int row = (int) payload.get("row");
            int column = (int) payload.get("column");
            String value = (String) payload.get("value");
            updateCellWithNotfication(uiSession, row, column, value);
        }
    }

    private void updateCellWithNotfication(UISessionManager.UISession uiSession, int row, int column, String value) {
        SSheet sheet = uiSession.getSheet();
        ModelUpdateCollector modelUpdateCollector = new ModelUpdateCollector();
        ModelUpdateCollector oldCollector = ModelUpdateCollector.setCurrent(modelUpdateCollector);
        FormulaCacheCleaner.setCurrent(new FormulaCacheCleaner(sheet.getBook().getBookSeries()));

        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            SCell cell = sheet.getCell(row, column);
            if (value.startsWith("="))
                cell.setFormulaValue(value.substring(1), connection, true);
            else
                try {
                    cell.setNumberValue(Double.parseDouble(value), connection, true);
                } catch (Exception e) {
                    cell.setStringValue(value, connection, true);
                }
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<ModelUpdate> modelUpdates = modelUpdateCollector.getModelUpdates();
        Set<Ref> refSet = new HashSet<>();
        for (ModelUpdate update : modelUpdates) {
            System.out.println("Model Update " + update);
            //TODO: refs can be regions

            switch (update.getType()) {
                case CELL:
                    break;

                case REFS:
                    refSet.addAll((Set<Ref>) update.getData());
                    break;
                case REF:
                    refSet.add((Ref) update.getData());
                    break;
            }

        }

        List<List<Object>> data = new ArrayList<>();
        for (Ref ref : refSet) {
            // TODO: These can be ranges, need to explode into cells.
            // TODO: Only push cells if the FE has them cached.

            List<Object> cellArr = new ArrayList<>(4);
            cellArr.add(ref.getRow());
            cellArr.add(ref.getColumn());
            SCell sCell = sheet.getCell(ref.getRow(), ref.getColumn());
            cellArr.add(sCell.getValue());
            if (sCell.getType() == SCell.CellType.FORMULA)
                cellArr.add(sCell.getFormulaValue());
            data.add(cellArr);
        }

        //TODO:  For each session check if the UI has the cells cached.
        Set<UISessionManager.UISession> uiSessionSet =
                UISessionManager.getInstance().getSessionBySheet(sheet);
        for (UISessionManager.UISession uiSession1 : uiSessionSet) {
            simpMessagingTemplate.convertAndSendToUser(uiSession1.getSessionId(),
                    "/push/updates", ImmutableMap.of("message", "pushCells",
                            "data", data),
                    createHeaders(uiSession1.getSessionId()));
        }

        simpMessagingTemplate.convertAndSendToUser(uiSession.getSessionId(),
                "/push/updates", ImmutableMap.of("message", "processingDone"),
                createHeaders(uiSession.getSessionId()));
        ModelUpdateCollector.setCurrent(oldCollector);
    }

    @SubscribeMapping("/user/push/updates")
    void subscribe(@Header String bookName,
                   @Header String sheetName,
                   @Header int fetchSize,
                   SimpMessageHeaderAccessor accessor) {

        SSheet sheet = BookBindings.getBookById(bookName).getSheetByName(sheetName);
        UISessionManager.getInstance()
                .getUISession(accessor.getSessionId())
                .assignSheet(sheet, fetchSize);

        simpMessagingTemplate.convertAndSendToUser(accessor.getSessionId(),
                "/push/updates", ImmutableMap.of("message", "subscribed"),
                createHeaders(accessor.getSessionId()));
    }
}