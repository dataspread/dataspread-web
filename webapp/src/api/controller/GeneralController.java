package api.controller;

import api.Authorization;
import api.Cell;
import api.JsonWrapper;
import api.UISessionManager;
import com.google.common.collect.ImmutableMap;
import org.model.AutoRollbackConnection;
import org.model.DBContext;
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
import org.springframework.web.bind.annotation.*;
import org.zkoss.json.JSONArray;
import org.zkoss.json.JSONObject;
import org.zkoss.poi.ss.formula.FormulaComputationStatusManager;
import org.zkoss.util.Pair;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.model.CellStyle;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.api.model.impl.SheetImpl;
import org.zkoss.zss.api.model.impl.SimpleRef;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.FormulaCacheCleaner;
import org.zkoss.zss.model.impl.sys.TableMonitor;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncListener;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerSimple;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;
import org.zkoss.zss.range.impl.ModelUpdate;
import org.zkoss.zss.range.impl.ModelUpdateCollector;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;


@RestController
@EnableScheduling
public class GeneralController implements FormulaAsyncListener {

    public GeneralController() {
        System.out.println("GeneralController");
        FormulaAsyncScheduler.initFormulaAsyncScheduler(new FormulaAsyncSchedulerSimple());
        FormulaAsyncScheduler.initFormulaAsyncListener(this);
    }

    private static final Logger logger = Logger.getLogger(GeneralController.class.getName());


    @Scheduled(fixedDelay = 250)
    public void updateFormulaProgress() {
        FormulaComputationStatusManager.FormulaComputationStatus status
                = FormulaComputationStatusManager.getInstance().getCurrentStatus();
        if (status.cell != null) {
            Set<UISessionManager.UISession> uiSessionSet =
                    UISessionManager.getInstance().getSessionBySheet(((SCell) status.cell).getSheet());

            for (UISessionManager.UISession uiSession : uiSessionSet) {
                simpMessagingTemplate.convertAndSendToUser(uiSession.getSessionId(),
                        "/push/updates", ImmutableMap.of("message", "asyncStatus",
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

        //TODO: Push to other sessions viewing the same book.
        // For each session check if the UI has the cells cached.
        Set<UISessionManager.UISession> uiSessionSet =
                UISessionManager.getInstance().getSessionBySheet(sheet);

        for (UISessionManager.UISession uiSession : uiSessionSet) {
            simpMessagingTemplate.convertAndSendToUser(uiSession.getSessionId(),
                    "/push/updates", ImmutableMap.of("message", "pushCells",
                            "data", data),
                    createHeaders(uiSession.getSessionId()));
        }
    }

    public enum FormatAction {
        FONT_FAMILY,
        FONT_SIZE,
        FONT_BOLD,
        FONT_ITALIC,
        FONT_UNDERLINE,
        FONT_TYPEOFFSET,
        FONT_STRIKE,
        BORDER,
        BORDER_BOTTOM,
        BORDER_TOP,
        BORDER_LEFT,
        BORDER_RIGHT,
        BORDER_NO,
        BORDER_ALL,
        BORDER_OUTSIDE,
        BORDER_INSIDE,
        BORDER_INSIDE_HORIZONTAL,
        BORDER_INSIDE_VERTICAL,
        FILL_COLOR,
        BACK_COLOR,
        FONT_COLOR,
        VERTICAL_ALIGN_TOP,
        VERTICAL_ALIGN_MIDDLE,
        VERTICAL_ALIGN_BOTTOM,
        HORIZONTAL_ALIGN_LEFT,
        HORIZONTAL_ALIGN_CENTER,
        HORIZONTAL_ALIGN_RIGHT,
        WRAP_TEXT,
        TEXT_INDENT_INCREASE,
        TEXT_INDENT_DECREASE
    }

    // General API
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public static String getCallbackPath(String bookId, String sheetName) {
        return new StringBuilder()
                .append("updateCells/")
                .append(bookId)
                .append("/")
                .append(sheetName)
                .toString();
    }

    //TODO formatAPIs

    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

    public void pushCells(UISessionManager.UISession uiSession, int blockNumber) {
        //TODO: Update to directly call the data model.
        // TODO: Improve efficiency.
        SSheet sheet = uiSession.getSheet();
        int endColumn = sheet.getEndColumnIndex();

        int row1 = blockNumber * uiSession.getFetchSize();
        Map<String, Object> ret = new HashMap<>();
        List<List<String[]>> data = new ArrayList<>();
        for (int row = row1; row < row1 + uiSession.getFetchSize(); row++) {
            List<String[]> cellsRow = new ArrayList<>();
            data.add(cellsRow);

            for (int col = 0; col <= endColumn; col++) {
                SCell sCell = sheet.getCell(row, col);
                if (sCell.isNull()) {
                    cellsRow.add(new String[]{""});
                } else if (sCell.getType() == SCell.CellType.FORMULA)
                    cellsRow.add(new String[]{sCell.getValue().toString(), sCell.getFormulaValue()});
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


        Map<String, Object> ret = new HashMap<>();
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

        ret.put("message", "pushCells");
        ret.put("data", data);

        //TODO: Push to other sessions viewing the same book.
        // For each session check if the UI has the cells cached.
        simpMessagingTemplate.convertAndSendToUser(uiSession.getSessionId(),
                "/push/updates", ret,
                createHeaders(uiSession.getSessionId()));

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
        UISessionManager.getInstance()
                .getUISession(accessor.getSessionId())
                .assignSheet(bookName, sheetName, fetchSize);
        simpMessagingTemplate.convertAndSendToUser(accessor.getSessionId(),
                "/push/updates", ImmutableMap.of("message", "subscribed"),
                createHeaders(accessor.getSessionId()));
    }


    @RequestMapping(value = "/api/getCells/{bookId}/{sheetName}/{row1}/{col1}/{row2}/{col2}",
            method = RequestMethod.GET)
    public HashMap<String, Object> getCells(@PathVariable String bookId,
                                            @PathVariable String sheetName,
                                            @PathVariable int row1,
                                            @PathVariable int col1,
                                            @PathVariable int row2,
                                            @PathVariable int col2) {
        List<Cell> returnCells = new ArrayList<>();
        SBook book = BookBindings.getBookById(bookId);
        SSheet sheet = book.getSheetByName(sheetName);
        CellRegion range = new CellRegion(row1, col1, row2, col2);
        HashMap<String, Object> data = new HashMap<>();
        DBContext dbContext = new DBContext(DBHandler.instance.getConnection());
        JSONArray tableInfo = null;
        try {
            tableInfo = TableMonitor.getMonitor().getTableInformation(dbContext, range, sheetName, bookId);
        } catch (Exception e) {
            e.printStackTrace();
            return JsonWrapper.generateError(e.getMessage());
        }
        HashSet<Pair<Integer, Integer>> checked = new HashSet<Pair<Integer, Integer>>();
        if (tableInfo != null) {
            for (int i = 0; i < tableInfo.size(); i++) {
                JSONObject table = (JSONObject) tableInfo.get(i);
                JSONObject tableRange = (JSONObject) table.get("range");
                int tableRow1 = Integer.parseInt(String.valueOf(tableRange.get("row1")));
                int tableRow2 = Integer.parseInt(String.valueOf(tableRange.get("row2")));
                int tableCol1 = Integer.parseInt(String.valueOf(tableRange.get("col1")));
                int tableCol2 = Integer.parseInt(String.valueOf(tableRange.get("col2")));
                ArrayList<Object> rows = new ArrayList<>();
                for (int row = tableRow1; row <= tableRow2; row++) {
                    ArrayList<Object> cols = new ArrayList<>();
                    for (int col = tableCol1; col <= tableCol2; col++) {
                        SCell sCell = sheet.getCell(row, col);
                        cols.add(sCell.getValue());
                        checked.add(new Pair<>(row, col));
                    }
                    rows.add(cols);
                }
                table.put("cells", rows);
            }
        }
        for (int row = row1; row <= row2; row++) {
            for (int col = col1; col <= col2; col++) {
                Pair<Integer, Integer> pair = new Pair<>(row, col);
                if (!checked.contains(pair)) {
                    SCell sCell = sheet.getCell(row, col);

                    Cell cell = new Cell();
                    cell.row = row;
                    cell.col = col;
                    cell.value = String.valueOf(sCell.getValue());
                    if (sCell.getType() == SCell.CellType.FORMULA)
                        cell.formula = sCell.getFormulaValue();
                    returnCells.add(cell);
                }
            }
        }
        data.put("cells", returnCells);
        data.put("tables", tableInfo);
        return JsonWrapper.generateJson(data);
    }


    @RequestMapping(value = "/api/getCellsV2/{bookId}/{sheetName}/{row1}/{row2}",
            method = RequestMethod.GET)
    public Map<String, List<List<String>>> getCellsV2(@PathVariable String bookId,
                                                      @PathVariable String sheetName,
                                                      @PathVariable int row1,
                                                      @PathVariable int row2) {
        //TODO: Update to directly call the data model.
        // TODO: Improve efficiency.
        List<List<String>> returnValues = new ArrayList<>();
        List<List<String>> returnFormulae = new ArrayList<>();

        SBook book = BookBindings.getBookById(bookId);
        SSheet sheet = book.getSheetByName(sheetName);
        int endColumn = sheet.getEndColumnIndex();

        for (int row = row1; row <= row2; row++) {
            List<String> valuesRow = new ArrayList<>();
            List<String> formulaRow = new ArrayList<>();
            returnValues.add(valuesRow);
            returnFormulae.add(formulaRow);
            for (int col = 0; col <= endColumn; col++) {
                //TODO: Change to range get
                SCell sCell = sheet.getCell(row, col);
                valuesRow.add(String.valueOf(sCell.getValue()));
                if (sCell.getType() == SCell.CellType.FORMULA)
                    formulaRow.add(sCell.getFormulaValue());
                else
                    formulaRow.add(null);
            }
        }
        Map<String, List<List<String>>> ret = new HashMap<>();
        ret.put("values", returnValues);
        ret.put("formulae", returnFormulae);
        return ret;
    }


    @RequestMapping(value = "/api/putCellsV2",
            method = RequestMethod.PUT)
    public void putCells(@RequestBody String json) {
        org.json.JSONObject obj = new org.json.JSONObject(json);
        String bookId = obj.getString("bookId");
        String sheetName = obj.getString("sheetName");
        SBook book = BookBindings.getBookById(bookId);
        SSheet sheet = book.getSheetByName(sheetName);
        org.json.JSONArray cells = obj.getJSONArray("cells");
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            for (Object cell : cells) {
                int row = ((org.json.JSONObject) cell).getInt("row");
                int col = ((org.json.JSONObject) cell).getInt("col");
                String type = ((org.json.JSONObject) cell).getString("type");
                String formula = ((org.json.JSONObject) cell).getString("formula");
                Object value = getValue((org.json.JSONObject) cell, type);
                if (!formula.equals("")) {
                    sheet.getCell(row, col).setFormulaValue(formula, connection, true);
                } else {
                    sheet.getCell(row, col).setValue(value, connection, true);
                }
            }
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @RequestMapping(value = "/api/putCells",
            method = RequestMethod.PUT)
    public HashMap<String, Object> putCells(@RequestHeader("auth-token") String authToken,
                                            @RequestBody String json) {
        org.json.JSONObject obj = new org.json.JSONObject(json);
        String bookId = obj.getString("bookId");
        String sheetName = obj.getString("sheetName");
        if (!Authorization.authorizeBook(bookId, authToken)) {
            JsonWrapper.generateError("Permission denied for accessing this book");
        }
        SBook book = BookBindings.getBookById(bookId);
        SSheet sheet = book.getSheetByName(sheetName);
        org.json.JSONArray cells = obj.getJSONArray("cells");
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            for (Object cell : cells) {
                int row = ((org.json.JSONObject) cell).getInt("row");
                int col = ((org.json.JSONObject) cell).getInt("col");
                String type = ((org.json.JSONObject) cell).getString("type");
                String formula = ((org.json.JSONObject) cell).getString("formula");
                Object value = getValue((org.json.JSONObject) cell, type);
                if (!formula.equals("")) {
                    sheet.getCell(row, col).setFormulaValue(formula, connection, true);
                } else {
                    sheet.getCell(row, col).setValue(value, connection, true);
                }
                String format = ((org.json.JSONObject) cell).getString("format");
            }
            connection.commit();
            simpMessagingTemplate.convertAndSend(getCallbackPath(bookId, sheetName), "");
            return JsonWrapper.generateJson(null);
        } catch (Exception e) {
            e.printStackTrace();
            return JsonWrapper.generateError(e.getMessage());
        }
    }

    @RequestMapping(value = "/api/insertRows",
            method = RequestMethod.PUT)
    public HashMap<String, Object> insertRows(@RequestHeader("auth-token") String authToken,
                                              @RequestBody String json) {
        org.json.JSONObject obj = new org.json.JSONObject(json);
        String sheetName = obj.getString("SheetName");
        String bookId = obj.getString("bookId");
        int rowIdx = obj.getInt("startRow");
        int lastRowIdx = obj.getInt("endRow");
        if (!Authorization.authorizeBook(bookId, authToken)) {
            JsonWrapper.generateError("Permission denied for accessing this book");
        }
        SBook book = BookBindings.getBookById(bookId);
        SSheet sheet = book.getSheetByName(sheetName);
        sheet.insertRow(rowIdx, lastRowIdx);
        simpMessagingTemplate.convertAndSend(GeneralController.getCallbackPath(bookId, sheetName), "");
        return JsonWrapper.generateJson(null);
    }

    @RequestMapping(value = "/api/deleteRows",
            method = RequestMethod.DELETE)
    public HashMap<String, Object> deleteRows(@RequestHeader("auth-token") String authToken,
                                              @RequestBody String json) {
        org.json.JSONObject obj = new org.json.JSONObject(json);
        String sheetName = obj.getString("SheetName");
        String bookId = obj.getString("bookId");
        int rowIdx = obj.getInt("startRow");
        int lastRowIdx = obj.getInt("endRow");
        if (!Authorization.authorizeBook(bookId, authToken)) {
            JsonWrapper.generateError("Permission denied for accessing this book");
        }
        SBook book = BookBindings.getBookById(bookId);
        SSheet sheet = book.getSheetByName(sheetName);
        sheet.deleteRow(rowIdx, lastRowIdx);
        simpMessagingTemplate.convertAndSend(GeneralController.getCallbackPath(bookId, sheetName), "");

        return JsonWrapper.generateJson(null);
    }

    @RequestMapping(value = "/api/insertCols",
            method = RequestMethod.PUT)
    public HashMap<String, Object> insertCols(@RequestHeader("auth-token") String authToken,
                                              @RequestBody String json) {
        org.json.JSONObject obj = new org.json.JSONObject(json);
        String sheetName = obj.getString("SheetName");
        String bookId = obj.getString("bookId");
        int colIdx = obj.getInt("startCol");
        int lastColIdx = obj.getInt("endCol");
        if (!Authorization.authorizeBook(bookId, authToken)) {
            JsonWrapper.generateError("Permission denied for accessing this book");
        }
        SBook book = BookBindings.getBookById(bookId);
        SSheet sheet = book.getSheetByName(sheetName);
        sheet.insertColumn(colIdx, lastColIdx);
        simpMessagingTemplate.convertAndSend(getCallbackPath(bookId, sheetName), "");
        return JsonWrapper.generateJson(null);
    }

    @RequestMapping(value = "/api/deleteCols",
            method = RequestMethod.DELETE)
    public HashMap<String, Object> deleteCols(@RequestHeader("auth-token") String authToken,
                                              @RequestBody String json) {
        org.json.JSONObject obj = new org.json.JSONObject(json);
        String sheetName = obj.getString("SheetName");
        String bookId = obj.getString("bookId");
        int colIdx = obj.getInt("startCol");
        int lastColIdx = obj.getInt("endCol");
        if (!Authorization.authorizeBook(bookId, authToken)) {
            JsonWrapper.generateError("Permission denied for accessing this book");
        }
        SBook book = BookBindings.getBookById(bookId);
        SSheet sheet = book.getSheetByName(sheetName);
        sheet.deleteColumn(colIdx, lastColIdx);
        simpMessagingTemplate.convertAndSend(getCallbackPath(bookId, sheetName), "");
        return JsonWrapper.generateJson(null);
    }

    @RequestMapping(value = "/api/changeFormat",
            method = RequestMethod.PUT)
    public HashMap<String, Object> changeFormat(@RequestHeader("auth-token") String authToken,
                                                @RequestBody String json) {
        org.json.JSONObject obj = new org.json.JSONObject(json);
        String sheetName = obj.getString("SheetName");
        String bookId = obj.getString("bookId");

        if (!Authorization.authorizeBook(bookId, authToken)) {
            JsonWrapper.generateError("Permission denied for accessing this book");
        }
        SBook book = BookBindings.getBookById(bookId);
        SSheet ssheet = book.getSheetByName(sheetName);
        String type = obj.getString("type");
        FormatAction event = FormatAction.valueOf(obj.getString("event"));
        org.json.JSONObject data = obj.getJSONObject("data");
        Sheet sheet = new SheetImpl(new SimpleRef<SBook>(ssheet.getBook()), new SimpleRef<SSheet>(ssheet));
        int row1 = obj.getInt("row1");
        int col1 = obj.getInt("col1");
        int row2 = obj.getInt("row2");
        int col2 = obj.getInt("col2");
        Range range = Ranges.range(sheet, row1, col1, row2, col2);
        AreaRef selection = new AreaRef(range.getRow(), range.getColumn(), range.getLastRow(), range.getLastColumn());

        switch (type) {
            case "font":
                doFontChange(event, data, sheet, range, selection);
                break;
            case "border":
                doBorderChange(event, data, sheet, range, selection);
                break;
            case "color":
                doColorChange(event, data, sheet, range, selection);
                break;
            case "align":
                doAlignChange(event, data, sheet, range, selection);
                break;
            case "text":
                doTextChange(event, data, sheet, range, selection);
                break;
            default:
                break;
        }

        return JsonWrapper.generateJson(null);
    }

    private void doFontChange(FormatAction event, org.json.JSONObject data, Sheet sheet, Range range, AreaRef selection) {
        /*
        CellOperationUtil.CellStyleApplier applier = null;
        CellOperationUtil.CellStyleApplier richApplier = null;
        switch (event){
            case FONT_FAMILY:
                applier = CellOperationUtil.getFontNameApplier(data.getString("name"));
                richApplier = CellOperationUtil.getRichTextFontNameApplier(data.getString("name"));
                break;
            case FONT_SIZE:
                applier = CellOperationUtil.getFontHeightPointsApplier(data.getInt("size"));
                richApplier = CellOperationUtil.getRichTextFontHeightPointsApplier(data.getInt("size"));
                break;
            case FONT_BOLD:
                Font.Boldweight bw = range.getCellStyle().getFont().getBoldweight();
                if (Font.Boldweight.BOLD.equals(bw)){
                    bw = Font.Boldweight.NORMAL;
                } else {
                    bw = Font.Boldweight.BOLD;
                }
                applier = CellOperationUtil.getFontBoldweightApplier(bw);
                richApplier = CellOperationUtil.getRichTextFontBoldweightApplier(bw);
                break;
            case FONT_ITALIC:
                boolean italic = !range.getCellStyle().getFont().isItalic();
                applier = CellOperationUtil.getFontItalicApplier(italic);
                richApplier = CellOperationUtil.getRichTextFontItalicApplier(italic);
                break;
            case FONT_UNDERLINE:
                Font.Underline underline = range.getCellStyle().getFont().getUnderline();
                if (Font.Underline.NONE.equals(underline)){
                    underline = Font.Underline.SINGLE;
                } else {
                    underline = Font.Underline.NONE;
                }
                applier = CellOperationUtil.getFontUnderlineApplier(underline);
                richApplier = CellOperationUtil.getRichTextFontUnderlineApplier(underline);
                break;
            case FONT_TYPEOFFSET:
                String offstr = data.getString("typeOffset");
                Font.TypeOffset offset =
                        "SUPER".equals(offstr) ? Font.TypeOffset.SUPER :
                                "SUB".equals(offstr) ? Font.TypeOffset.SUB : Font.TypeOffset.NONE;
                applier = CellOperationUtil.getFontTypeOffsetApplier(offset);
                richApplier = CellOperationUtil.getRichTextFontTypeOffsetApplier(offset);
                break;
            case FONT_STRIKE:
                boolean strikeout = !range.getCellStyle().getFont().isStrikeout();
                applier = CellOperationUtil.getFontStrikeoutApplier(strikeout);
                richApplier = CellOperationUtil.getRichTextFontStrikeoutApplier(strikeout);
                break;
            case FONT_COLOR:
                Color color = range.getCellStyleHelper().createColorFromHtmlColor(data.getString("color"));
                applier = CellOperationUtil.getFontColorApplier(color);
                richApplier = CellOperationUtil.getRichTextFontColorApplier(color);
                break;
        }

        List<UndoableAction> actions = new ArrayList<UndoableAction>();
        actions.add(new FontStyleAction("", sheet, selection.getRow(), selection.getColumn(),
                selection.getLastRow(), selection.getLastColumn(), applier));
        ActionHelper.collectRichTextStyleActions(range, richApplier, actions);
        AggregatedAction action = new AggregatedAction(Labels.getLabel("zss.undo.fontStyle"), actions.toArray(new UndoableAction[actions.size()]));
        action.doAction();
        */
    }

    private void doBorderChange(FormatAction event, org.json.JSONObject data, Sheet sheet, Range range, AreaRef selection) {
        Range.ApplyBorderType applyType = Range.ApplyBorderType.FULL;
        CellStyle.BorderType borderType = CellStyle.BorderType.THIN;

        switch (event) {
            case BORDER:
                applyType = Range.ApplyBorderType.EDGE_BOTTOM;
                break;
            case BORDER_BOTTOM:
                applyType = Range.ApplyBorderType.EDGE_BOTTOM;
                break;
            case BORDER_TOP:
                applyType = Range.ApplyBorderType.EDGE_TOP;
                break;
            case BORDER_LEFT:
                applyType = Range.ApplyBorderType.EDGE_LEFT;
                break;
            case BORDER_RIGHT:
                applyType = Range.ApplyBorderType.EDGE_RIGHT;
                break;
            case BORDER_NO:
                applyType = Range.ApplyBorderType.FULL;
                borderType = CellStyle.BorderType.NONE;
                break;
            case BORDER_ALL:
                applyType = Range.ApplyBorderType.FULL;
                break;
            case BORDER_OUTSIDE:
                applyType = Range.ApplyBorderType.OUTLINE;
                break;
            case BORDER_INSIDE:
                applyType = Range.ApplyBorderType.INSIDE;
                break;
            case BORDER_INSIDE_HORIZONTAL:
                applyType = Range.ApplyBorderType.INSIDE_HORIZONTAL;
                break;
            case BORDER_INSIDE_VERTICAL:
                applyType = Range.ApplyBorderType.INSIDE_VERTICAL;
                break;
        }
        /*
        String color = data.getString("color");
        UndoableAction action = new CellBorderAction(Labels.getLabel("zss.undo.cellBorder"),sheet, selection.getRow(), selection.getColumn(),
                selection.getLastRow(), selection.getLastColumn(),
                applyType, borderType, color);
        action.doAction();
        */
    }

    private void doColorChange(FormatAction event, org.json.JSONObject data, Sheet sheet, Range range, AreaRef selection) {
        /*
        Color color = range.getCellStyleHelper().createColorFromHtmlColor(data.getString("color"));
        CellOperationUtil.CellStyleApplier applier = null;

        switch (event){
            case FILL_COLOR:
                applier = CellOperationUtil.getFillColorApplier(color);
                break;
            case BACK_COLOR:
                applier = CellOperationUtil.getBackColorApplier(color);
                break;
        }

        UndoableAction action = new CellStyleAction(Labels.getLabel("zss.undo.cellStyle"),sheet, selection.getRow(), selection.getColumn(),
                selection.getLastRow(), selection.getLastColumn(), applier);
        action.doAction();
        */
    }

    private void doAlignChange(FormatAction event, org.json.JSONObject data, Sheet sheet, Range range, AreaRef selection) {
        /*
        CellOperationUtil.CellStyleApplier applier = null;
        switch (event){
            case VERTICAL_ALIGN_TOP:
                applier = CellOperationUtil.getVerticalAligmentApplier(CellStyle.VerticalAlignment.TOP.TOP);
                break;
            case VERTICAL_ALIGN_MIDDLE:
                applier = CellOperationUtil.getVerticalAligmentApplier(CellStyle.VerticalAlignment.CENTER);
                break;
            case VERTICAL_ALIGN_BOTTOM:
                applier = CellOperationUtil.getVerticalAligmentApplier(CellStyle.VerticalAlignment.BOTTOM);
                break;
            case HORIZONTAL_ALIGN_LEFT:
                applier = CellOperationUtil.getAligmentApplier(CellStyle.Alignment.LEFT);
                break;
            case HORIZONTAL_ALIGN_CENTER:
                applier = CellOperationUtil.getAligmentApplier(CellStyle.Alignment.CENTER);
                break;
            case HORIZONTAL_ALIGN_RIGHT:
                applier = CellOperationUtil.getAligmentApplier(CellStyle.Alignment.RIGHT);
                break;
        }

        UndoableAction action = new CellStyleAction(Labels.getLabel("zss.undo.cellStyle"),sheet, selection.getRow(), selection.getColumn(),
                selection.getLastRow(), selection.getLastColumn(), applier);
        action.doAction();
        */
    }

    private void doTextChange(FormatAction event, org.json.JSONObject data, Sheet sheet, Range range, AreaRef selection) {
       /*
        CellOperationUtil.CellStyleApplier applier = null;
        switch (event){
            case WRAP_TEXT:
                boolean wrapped = !range.getCellStyle().isWrapText();
                applier = CellOperationUtil.getWrapTextApplier(wrapped);
                break;
            case TEXT_INDENT_INCREASE:
                applier = CellOperationUtil.getIndentionApplier(1);
                break;
            case TEXT_INDENT_DECREASE:
                applier = CellOperationUtil.getIndentionApplier(-1);
                break;
        }

        UndoableAction action = new CellStyleAction(Labels.getLabel("zss.undo.cellStyle"),sheet, selection.getRow(), selection.getColumn(),
                selection.getLastRow(), selection.getLastColumn(), applier);
        action.doAction();
        */
    }

    private Object getValue(org.json.JSONObject cell, String type) {
        switch (type.toUpperCase()) {
            case "INTEGER":
                return cell.getInt("value");
            case "REAL":
            case "FLOAT":
                return cell.getDouble("value");
            case "DATE":
                try {
                    return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(cell.getString("value"));
                } catch (Exception ex) {
                    return cell.getString("value");
                }
            case "BOOLEAN":
                return cell.getBoolean("value");
            case "TEXT":
            default:
                return cell.getString("value");
        }
    }

    public static String encode(final String clearText) {
        try {
            return new String(
                    Base64.getEncoder().encode(MessageDigest.getInstance("SHA-256").digest(clearText.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            return clearText;
        }
    }
}