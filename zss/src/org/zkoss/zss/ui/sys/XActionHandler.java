///* ToolbarActionHandler.java
//
//{{IS_NOTE
//	Purpose:
//		
//	Description:
//		
//	History:
//		Feb 24, 2012 2:08:34 PM , Created by sam
//}}IS_NOTE
//
//Copyright (C) 2012 Potix Corporation. All Rights Reserved.
//
//{{IS_RIGHT
//}}IS_RIGHT
//*/
//package org.zkoss.zss.ui.sys;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
//import org.zkoss.image.AImage;
//import org.zkoss.lang.Objects;
//import org.zkoss.lang.Strings;
//
//import org.zkoss.util.media.Media;
//import org.zkoss.zk.ui.UiException;
//import org.zkoss.zk.ui.event.Event;
//import org.zkoss.zk.ui.event.EventListener;
//import org.zkoss.zk.ui.event.UploadEvent;
//
//import org.zkoss.zss.api.CellOperationUtil;
//import org.zkoss.zss.api.Range;
//import org.zkoss.zss.api.SheetAnchor;
//import org.zkoss.zss.api.Range.ApplyBorderType;
//import org.zkoss.zss.api.Range.DeleteShift;
//import org.zkoss.zss.api.Range.InsertCopyOrigin;
//import org.zkoss.zss.api.Range.InsertShift;
//import org.zkoss.zss.api.Range.PasteOperation;
//import org.zkoss.zss.api.Range.PasteType;
//import org.zkoss.zss.api.Ranges;
//import org.zkoss.zss.api.SheetOperationUtil;
//import org.zkoss.zss.api.model.Book;
//import org.zkoss.zss.api.model.Chart;
//import org.zkoss.zss.api.model.ChartData;
//import org.zkoss.zss.api.model.CellStyle.Alignment;
//import org.zkoss.zss.api.model.CellStyle.VerticalAlignment;
//import org.zkoss.zss.api.model.Sheet;
//import org.zkoss.zss.api.model.CellStyle.BorderType;
//import org.zkoss.zss.api.model.Font.Boldweight;
//import org.zkoss.zss.api.model.Font.Underline;
//import org.zkoss.zss.ui.UserAction;
//import org.zkoss.zss.ui.Rect;
//import org.zkoss.zss.ui.Spreadsheet;
//import org.zkoss.zss.ui.Spreadsheet.HelperContainer;
//import org.zkoss.zss.ui.event.Events;
//import org.zkoss.zss.ui.event.KeyEvent;
//import org.zkoss.zss.ui.impl.HeaderPositionHelper;
//import org.zkoss.zss.ui.impl.MergeMatrixHelper;
//import org.zkoss.zss.ui.impl.MergedRect;
//import org.zkoss.zss.ui.impl.Upload;
//import org.zkoss.zss.ui.impl.Uploader;
//import org.zkoss.zss.ui.sys.XActionHandler.Clipboard.Type;
//import org.zkoss.zul.Messagebox;
//
///**
// * TODO delete this class, it is just for code reference now
// * @author sam
// * @deprecated
// */
//@Deprecated
//public abstract class XActionHandler {
//	
////	protected Spreadsheet _spreadsheet;
////	protected Uploader _insertPicture;
////	protected Upload _upload;
////	protected Rect _insertPictureSelection;
//	private Clipboard _clipboard;
//	protected Set<UserAction> toggleAction = new HashSet<UserAction>();
//	private static UserAction[] _defaultDisabledActionOnBookClosed = new UserAction[]{
//			UserAction.SAVE_BOOK,
//			UserAction.EXPORT_PDF, 
//			UserAction.PASTE,
//			UserAction.CUT,
//			UserAction.COPY,
//			UserAction.FONT_FAMILY,
//			UserAction.FONT_SIZE,
//			UserAction.FONT_BOLD,
//			UserAction.FONT_ITALIC,
//			UserAction.FONT_UNDERLINE,
//			UserAction.FONT_STRIKE,
//			UserAction.BORDER,
//			UserAction.FONT_COLOR,
//			UserAction.FILL_COLOR,
//			UserAction.VERTICAL_ALIGN,
//			UserAction.HORIZONTAL_ALIGN,
//			UserAction.WRAP_TEXT,
//			UserAction.MERGE_AND_CENTER,
//			UserAction.INSERT,
//			UserAction.DELETE,
//			UserAction.CLEAR,
//			UserAction.SORT_AND_FILTER,
//			UserAction.PROTECT_SHEET,
//			UserAction.GRIDLINES,
//			UserAction.INSERT_PICTURE,
//			UserAction.COLUMN_CHART,
//			UserAction.LINE_CHART,
//			UserAction.PIE_CHART,
//			UserAction.BAR_CHART,
//			UserAction.AREA_CHART,
//			UserAction.SCATTER_CHART,
//			UserAction.OTHER_CHART,
//			UserAction.HYPERLINK
//	};
//	//TODO the disable action information should get from worksheet, not hard coded
//	private static UserAction[] _defaultDisabledActionOnSheetProtected = new UserAction[]{
//			UserAction.FONT_FAMILY,
//			UserAction.FONT_SIZE,
//			UserAction.FONT_BOLD,
//			UserAction.FONT_ITALIC,
//			UserAction.FONT_UNDERLINE,
//			UserAction.FONT_STRIKE,
//			UserAction.BORDER,
//			UserAction.FONT_COLOR,
//			UserAction.FILL_COLOR,
//			UserAction.VERTICAL_ALIGN,
//			UserAction.HORIZONTAL_ALIGN,
//			UserAction.WRAP_TEXT,
//			UserAction.MERGE_AND_CENTER,
//			UserAction.INSERT,
//			UserAction.INSERT_SHIFT_CELL_RIGHT,
//			UserAction.INSERT_SHIFT_CELL_DOWN,
//			UserAction.INSERT_SHEET_ROW,
//			UserAction.INSERT_SHEET_COLUMN,
//			UserAction.DELETE,
//			UserAction.DELETE_SHIFT_CELL_LEFT,
//			UserAction.DELETE_SHIFT_CELL_UP,
//			UserAction.DELETE_SHEET_ROW,
//			UserAction.DELETE_SHEET_COLUMN,
//			UserAction.CLEAR,
//			UserAction.CLEAR_CONTENT,
//			UserAction.FORMAT_CELL,
//			UserAction.SORT_ASCENDING,
//			UserAction.SORT_DESCENDING,
//			UserAction.CUSTOM_SORT,
//			UserAction.FILTER,
//			UserAction.SORT_AND_FILTER,
//			UserAction.INSERT_PICTURE,
//			UserAction.COLUMN_CHART,
//			UserAction.LINE_CHART,
//			UserAction.PIE_CHART,
//			UserAction.BAR_CHART,
//			UserAction.AREA_CHART,
//			UserAction.SCATTER_CHART,
//			UserAction.OTHER_CHART,
//			UserAction.HYPERLINK
//	};
//	
//	
//	//TODO, Dennis, Why need to check this? should it a common solution in co-work mode, or update directly by ui behavior
////	protected EventListener _bookListener = new EventListener() {
////
////		@Override
////		public void onEvent(Event event) throws Exception {
////			SSDataEvent evt = (SSDataEvent)event;
////			if (evt.getName().equals(SSDataEvent.ON_BTN_CHANGE)) {
////				//TODO DENNIS, why?
////				syncAutoFilter();
////			}
////		}
////	};
//	
//	//TODO, Dennis, listener should be inside the spreadsheet component, shouldn't in action hnadler
////	protected EventListener _doSelectSheetListener = new EventListener() {
////		
////		@Override
////		public void onEvent(Event event) throws Exception {
////			doSheetSelect();
////		}
////	};
//	
////	protected EventListener _doCtrlKeyListener = new EventListener() {
////		
////		@Override
////		public void onEvent(Event event) throws Exception {
////			doCtrlKey((KeyEvent) event);
////		}
////	};
//	
////	protected EventListener _doClearClipboard = new EventListener() {
////
////		@Override
////		public void onEvent(Event event) throws Exception {
////			clearClipboard();
////		}
////	};
//	
//	public XActionHandler() {}
////	public ActionHandler(Spreadsheet spreadsheet) {
////		_spreadsheet = spreadsheet;
////		init();
////	}
//
//	private static ThreadLocal<ActionContext> _ctx = new ThreadLocal<ActionContext>();
//	
//	private static class ActionContext {
//
//		Spreadsheet spreadsheet;
//		String action;
//		Rect selection;
//		Map extraData;
//		
//		public ActionContext(Spreadsheet spreadsheet, String action,
//				Rect selection, Map extraData) {
//			this.spreadsheet = spreadsheet;
//			this.action = action;
//			this.selection = selection;
//			this.extraData = extraData;
//		}
//	}
//	
//	protected Spreadsheet getSpreadsheet(){
//		return _ctx.get().spreadsheet;
//	}
//	
//	protected Sheet getSelectedSheet(){
//		return _ctx.get().spreadsheet.getSelectedSheet();
//	}
//	
//	protected Book getBook(){
//		return getSelectedSheet().getBook();
//	}
//	
//	protected Rect getSelection(){
//		return _ctx.get().selection;
//	}
//	
//	
//	public void dispatch(Spreadsheet spreadsheet, String action, Rect selection, Map extraData) {
//		final ActionContext ctx = new ActionContext(spreadsheet, action,selection,extraData);
//		_ctx.set(ctx);
//		try{
//			dispatch0(spreadsheet,action,selection,extraData);
//		}finally{
//			_ctx.set(null);
//		}
//	}
//	
//	
//	/**
//	 * Returns the border color
//	 * @return
//	 */
//	protected String getDefaultBorderColor() {
//		return "#000000";
//	}
//	
//	
//	/**
//	 * Returns the border color
//	 * @return
//	 */
//	protected String getDefaultFontColor() {
//		return "#000000";
//	}
//	
//	
//	/**
//	 * Returns the border color
//	 * @return
//	 */
//	protected String getDefaultFillColor() {
//		return "#FFFFFF";
//	}
//	
//	private String getBorderColor(Map extraData){
//		String color = (String)extraData.get("color");
//		if (Strings.isEmpty(color)) {//CE version won't provide color
//			color = getDefaultBorderColor();
//		}
//		return color;
//	}
//	
//	private String getFontColor(Map extraData){
//		String color = (String)extraData.get("color");
//		if (Strings.isEmpty(color)) {//CE version won't provide color
//			color = getDefaultFontColor();
//		}
//		return color;
//	}
//	
//	private String getFillColor(Map extraData){
//		String color = (String)extraData.get("color");
//		if (Strings.isEmpty(color)) {//CE version won't provide color
//			color = getDefaultFillColor();
//		}
//		return color;
//	}
//	
//	private void dispatch0(Spreadsheet spreadsheet, String action, Rect selection, Map extraData) {
//		
//		if (UserAction.HOME_PANEL.toString().equals(action)) {
//			doHomePanel();
//		} else if (UserAction.INSERT_PANEL.equals(action)) {
//			doInsertPanel();
//		} else if (UserAction.FORMULA_PANEL.equals(action)) {
//			doFormulaPanel();
//		} else if (UserAction.NEW_BOOK.equals(action)) {
//			doNewBook();
//		} else if (UserAction.SAVE_BOOK.equals(action)) {
//			doSaveBook();
//		} else if (UserAction.EXPORT_PDF.equals(action)) {
//			doExportPDF(selection);
//		} else if (UserAction.PASTE.equals(action)) {
//			doPaste(selection);
//		} else if (UserAction.PASTE_FORMULA.equals(action)) {
//			doPasteFormula(selection);
//		} else if (UserAction.PASTE_VALUE.equals(action)) {
//			doPasteValue(selection);
//		} else if (UserAction.PASTE_ALL_EXPECT_BORDERS.equals(action)) {
//			doPasteAllExceptBorder(selection);
//		} else if (UserAction.PASTE_TRANSPOSE.equals(action)) {
//			doPasteTranspose(selection);
//		} else if (UserAction.PASTE_SPECIAL.equals(action)) {
//			doPasteSpecial(selection);
//		} else if (UserAction.CUT.equals(action)) {
//			doCut(selection);	
//		} else if (UserAction.COPY.equals(action)) {
//			doCopy(selection);
//		} else if (UserAction.FONT_FAMILY.equals(action)) {
//			doFontFamily((String)extraData.get("name"), selection);
//		} else if (UserAction.FONT_SIZE.equals(action)) {
//			Integer fontSize = Integer.parseInt((String)extraData.get("size"));
//			doFontSize(fontSize, selection);
//		} else if (UserAction.FONT_BOLD.equals(action)) {
//			doFontBold(selection);
//		} else if (UserAction.FONT_ITALIC.equals(action)) {
//			doFontItalic(selection);
//		} else if (UserAction.FONT_UNDERLINE.equals(action)) {
//			doFontUnderline(selection);
//		} else if (UserAction.FONT_STRIKE.equals(action)) {
//			doFontStrikeout(selection);
//		} else if (UserAction.BORDER.equals(action)) {
//			doBorder(getBorderColor(extraData), selection);
//		} else if (UserAction.BORDER_BOTTOM.equals(action)) {
//			doBorderBottom(getBorderColor(extraData), selection);
//		} else if (UserAction.BORDER_TOP.equals(action)) {
//			doBoderTop(getBorderColor(extraData), selection);
//		} else if (UserAction.BORDER_LEFT.equals(action)) {
//			doBorderLeft(getBorderColor(extraData), selection);
//		} else if (UserAction.BORDER_RIGHT.equals(action)) {
//			doBorderRight(getBorderColor(extraData), selection);
//		} else if (UserAction.BORDER_NO.equals(action)) {
//			doBorderNo(getBorderColor(extraData), selection);
//		} else if (UserAction.BORDER_ALL.equals(action)) {
//			doBorderAll(getBorderColor(extraData), selection);
//		} else if (UserAction.BORDER_OUTSIDE.equals(action)) {
//			doBorderOutside(getBorderColor(extraData), selection);
//		} else if (UserAction.BORDER_INSIDE.equals(action)) {
//			doBorderInside(getBorderColor(extraData), selection);
//		} else if (UserAction.BORDER_INSIDE_HORIZONTAL.equals(action)) {
//			doBorderInsideHorizontal(getBorderColor(extraData), selection);
//		} else if (UserAction.BORDER_INSIDE_VERTICAL.equals(action)) {
//			doBorderInsideVertical(getBorderColor(extraData), selection);
//		} else if (UserAction.FONT_COLOR.equals(action)) {
//			doFontColor(getFontColor(extraData), selection);
//		} else if (UserAction.FILL_COLOR.equals(action)) {
//			doFillColor(getFillColor(extraData), selection);
//		} else if (UserAction.VERTICAL_ALIGN_TOP.equals(action)) {
//			doVerticalAlignTop(selection);
//		} else if (UserAction.VERTICAL_ALIGN_MIDDLE.equals(action)) {
//			doVerticalAlignMiddle(selection);
//		} else if (UserAction.VERTICAL_ALIGN_BOTTOM.equals(action)) {
//			doVerticalAlignBottom(selection);
//		} else if (UserAction.HORIZONTAL_ALIGN_LEFT.equals(action)) {
//			doHorizontalAlignLeft(selection);
//		} else if (UserAction.HORIZONTAL_ALIGN_CENTER.equals(action)) {
//			doHorizontalAlignCenter(selection);
//		} else if (UserAction.HORIZONTAL_ALIGN_RIGHT.equals(action)) {
//			doHorizontalAlignRight(selection);
//		} else if (UserAction.WRAP_TEXT.equals(action)) {
//			doWrapText(selection);
//		} else if (UserAction.MERGE_AND_CENTER.equals(action)) {
//			doMergeAndCenter(selection);
//		} else if (UserAction.MERGE_ACROSS.equals(action)) {
//			doMergeAcross(selection);
//		} else if (UserAction.MERGE_CELL.equals(action)) {
//			doMergeCell(selection);
//		} else if (UserAction.UNMERGE_CELL.equals(action)) {
//			doUnmergeCell(selection);
//		} else if (UserAction.INSERT_SHIFT_CELL_RIGHT.equals(action)) {
//			doShiftCellRight(selection);
//		} else if (UserAction.INSERT_SHIFT_CELL_DOWN.equals(action)) {
//			doShiftCellDown(selection);
//		} else if (UserAction.INSERT_SHEET_ROW.equals(action)) {
//			doInsertSheetRow(selection);
//		} else if (UserAction.INSERT_SHEET_COLUMN.equals(action)) {
//			doInsertSheetColumn(selection);
//		} else if (UserAction.DELETE_SHIFT_CELL_LEFT.equals(action)) {
//			doShiftCellLeft(selection);
//		} else if (UserAction.DELETE_SHIFT_CELL_UP.equals(action)) {
//			doShiftCellUp(selection);
//		} else if (UserAction.DELETE_SHEET_ROW.equals(action)) {
//			doDeleteSheetRow(selection);
//		} else if (UserAction.DELETE_SHEET_COLUMN.equals(action)) {
//			doDeleteSheetColumn(selection);
//		} else if (UserAction.SORT_ASCENDING.equals(action)) {
//			doSortAscending(selection);
//		} else if (UserAction.SORT_DESCENDING.equals(action)) {
//			doSortDescending(selection);
//		} else if (UserAction.CUSTOM_SORT.equals(action)) {
//			doCustomSort(selection);
//		} else if (UserAction.FILTER.equals(action)) {
//			doFilter(selection);
//		} else if (UserAction.CLEAR_FILTER.equals(action)) {
//			doClearFilter();
//		} else if (UserAction.REAPPLY_FILTER.equals(action)) {
//			doReapplyFilter();
//		} else if (UserAction.CLEAR_CONTENT.equals(action)) {
//			doClearContent(selection);
//		} else if (UserAction.CLEAR_STYLE.equals(action)) {
//			doClearStyle(selection);
//		} else if (UserAction.CLEAR_ALL.equals(action)) {
//			doClearAll(selection);
//		} else if (UserAction.PROTECT_SHEET.equals(action)) {
//			doProtectSheet();
//		} else if (UserAction.GRIDLINES.equals(action)) {
//			doGridlines();
//		} else if (UserAction.COLUMN_CHART.equals(action)) {
//			doColumnChart(selection);
//		} else if (UserAction.COLUMN_CHART_3D.equals(action)) {
//			doColumnChart3D(selection);
//		} else if (UserAction.LINE_CHART.equals(action)) {
//			doLineChart(selection);
//		} else if (UserAction.LINE_CHART_3D.equals(action)) {
//			doLineChart3D(selection);
//		} else if (UserAction.PIE_CHART.equals(action)) {
//			doPieChart(selection);
//		} else if (UserAction.PIE_CHART_3D.equals(action)) {
//			doPieChart3D(selection);
//		} else if (UserAction.BAR_CHART.equals(action)) {
//			doBarChart(selection);
//		} else if (UserAction.BAR_CHART_3D.equals(action)) {
//			doBarChart3D(selection);
//		} else if (UserAction.AREA_CHART.equals(action)) {
//			doAreaChart(selection);
//		} else if (UserAction.SCATTER_CHART.equals(action)) {
//			doScatterChart(selection);
//		} else if (UserAction.DOUGHNUT_CHART.equals(action)) {
//			doDoughnutChart(selection);
//		} else if (UserAction.HYPERLINK.equals(action)) {
//			doHyperlink(selection);
//		} else if (UserAction.INSERT_PICTURE.equals(action)) {
//			//TODO 
////			doBeforeInsertPicture(selection);
//		} else if (UserAction.CLOSE_BOOK.equals(action)) {
//			doCloseBook();
//		} else if (UserAction.FORMAT_CELL.equals(action)) {
//			doFormatCell(selection);
//		} else if (UserAction.COLUMN_WIDTH.equals(action)) {
//			doColumnWidth(selection);
//		} else if (UserAction.ROW_HEIGHT.equals(action)) {
//			doRowHeight(selection);
//		} else if (UserAction.HIDE_COLUMN.equals(action)) {
//			doHideColumn(selection);
//		} else if (UserAction.UNHIDE_COLUMN.equals(action)) {
//			doUnhideColumn(selection);
//		} else if (UserAction.HIDE_ROW.equals(action)) {
//			doHideRow(selection);
//		} else if (UserAction.UNHIDE_ROW.equals(action)) {
//			doUnhideRow(selection);
//		} else if (UserAction.INSERT_FUNCTION.equals(action)) {
//			doInsertFunction(selection);
//		}
//	}
//	
//	protected abstract void doInsertFunction(Rect selection);
//	
//	protected void doHideRow(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		range = range.getRowRange();
//		CellOperationUtil.hide(range);
//	}
//
//	protected void doUnhideRow(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		range = range.getRowRange();
//		CellOperationUtil.unHide(range);
//	}
//
//	protected void doUnhideColumn(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		range = range.getColumnRange();
//		CellOperationUtil.hide(range);
//		
//	}
//
//	protected void doHideColumn(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		range = range.getColumnRange();
//		CellOperationUtil.unHide(range);
//	}
//	/**
//	 * @param selection
//	 */
//	protected abstract void doColumnWidth(Rect selection);
//	
//	/**
//	 * @param selection
//	 */
//	protected abstract void doRowHeight(Rect selection);
//	
//	/**
//	 * Initializes actions that will enable on sheet selected, and will disable on book closed
//	 */
//	private void initToggleAction() {
//		if (toggleAction.size() == 0) {
//			for (UserAction a : _defaultDisabledActionOnBookClosed) {
//				toggleAction.add(a);
//			}
//		}
//	}
//
//// TODO, redesign this, should consider to 
////	public void toggleActionOnSheetSelected() {
////		for (Action action : toggleAction) {
////			_spreadsheet.setActionDisabled(false, action);
////		}
////		
////		//TODO: read protect information from worksheet
////		boolean protect = _spreadsheet.getSelectedXSheet().getProtect();
////		for (Action action : _defaultDisabledActionOnSheetProtected) {
////			_spreadsheet.setActionDisabled(protect, action);
////		}
////	}
////	
////	public void toggleActionOnBookClosed() {
////		for (Action a : toggleAction) {
////			_spreadsheet.setActionDisabled(true, a);
////		}
////	}
//	
////	/**
////	 * Execute when user select sheet
////	 */
////	protected void doSheetSelect() {
//////		syncClipboard();
//////		syncAutoFilter();
////		
//////		toggleActionOnSheetSelected();
////	}
//	
//	protected void doCloseBook() {
//		Spreadsheet zss = getSpreadsheet();
//		if(zss.getSrc()!=null){
//			zss.setSrc(null);
//		}
//		if(zss.getBook()!=null){
//			zss.setBook(null);
//		}
//		
//		_clipboard = null;
////		_insertPictureSelection = null;
//		
////		toggleActionOnBookClosed();
//	}
//	
//	protected void doInsertPicture(UploadEvent evt) {
//		//TODO DENNIS, re-design this.
////		if (_insertPictureSelection == null) {
////			return;
////		}
////		
////		XSheet sheet = _spreadsheet.getSelectedXSheet();
////		if (sheet != null) {
////			if (!sheet.getProtect()) {
////				final Media media = evt.getMedia();
////				if (media instanceof AImage) {
////					AImage image = (AImage)media;
////					XRanges
////					.range(_spreadsheet.getSelectedXSheet())
////					.addPicture(getClientAnchor(_insertPictureSelection.getTop(), _insertPictureSelection.getLeft(), 
////							image.getWidth(), image.getHeight()), image.getByteData(), getImageFormat(image));
////				}	
////			} else {
////				showProtectMessage();
////			}
////		}
//	}
//	
////	/**
////	 * @param selection
////	 */
////	protected void doBeforeInsertPicture(Rect selection) {
////		_insertPictureSelection = selection;
////	}
//	
//
//	
////	protected void syncClipboard() {
////		//TODO, Dennis, shouldn't clipboard only care the range?
////		//why should we check this?
////		if (_clipboard != null) {
////			final Book srcBook = _clipboard.book;
////			if (!srcBook.equals(_spreadsheet.getBook())) {
////				_clipboard = null;
////			} else {
////				final Sheet srcSheet = _clipboard.sourceSheet;
////				boolean validSheet = srcBook.getSheetIndex(srcSheet) >= 0;
////				if (!validSheet) {
////					clearClipboard();
////				} else if (!srcSheet.equals(_spreadsheet.getSelectedXSheet())) {
////					_spreadsheet.setHighlight(null);
////				} else {
////					_spreadsheet.setHighlight(_clipboard.sourceRect);
////				}
////			}
////		}
////	}
//	
////	protected void syncAutoFilter() {
////		//TODO Dennis, need to check the logic here
////		final Sheet worksheet = _spreadsheet.getSelectedSheet();
////		boolean appliedFilter = false;
////		AutoFilter af = worksheet.getAutoFilter();
////		if (af != null) {
////			final CellRangeAddress afrng = af.getRangeAddress();
////			if (afrng != null) {
////				int rowIdx = afrng.getFirstRow() + 1;
////				for (int i = rowIdx; i <= afrng.getLastRow(); i++) {
////					final Row row = worksheet.getRow(i);
////					if (row != null && row.getZeroHeight()) {
////						appliedFilter = true;
////						break;
////					}
////				}	
////			}
////		}
////
////		_spreadsheet.setActionDisabled(!appliedFilter, Action.CLEAR_FILTER);
////		_spreadsheet.setActionDisabled(!appliedFilter, Action.REAPPLY_FILTER);
////		
////		if (!Objects.equals(_book, _spreadsheet.getBook())) {
////			if (_book != null) {
////				_book.unsubscribe(_bookListener);
////			}
////			_book = _spreadsheet.getXBook();
////			_book.subscribe(_bookListener);
////		}
////	}
//	
////	private void init() {
////		_spreadsheet.addEventListener(Events.ON_SHEET_SELECT, _doSelectSheetListener);
////		_spreadsheet.addEventListener(Events.ON_CTRL_KEY, _doCtrlKeyListener);
////		
////		_spreadsheet.addEventListener(Events.ON_CELL_DOUBLE_CLICK, _doClearClipboard);
////		_spreadsheet.addEventListener(Events.ON_START_EDITING, _doClearClipboard);
////		
//////		if (_upload == null) {
//////			_upload = new Upload();
//////			_upload.appendChild(_insertPicture = new Uploader());
//////			_insertPicture.addEventListener(org.zkoss.zk.ui.event.Events.ON_UPLOAD, new EventListener() {
//////				
//////				@Override
//////				public void onEvent(Event event) throws Exception {
//////					doInsertPicture((UploadEvent)event);
//////				}
//////			});
//////		}
//////		_upload.setParent(_spreadsheet);
////		
////		initToggleAction();
////	}
//	
//	/**
//	 * Execute when user press key
//	 * @param event
//	 */
//	protected void doCtrlKey(KeyEvent event) {
//		Rect selection = getSelection();
//		if (46 == event.getKeyCode()) {
//			if (event.isCtrlKey())
//				doClearStyle(selection);
//			else
//				doClearContent(selection);
//			return;
//		}
//		if (false == event.isCtrlKey())
//			return;
//		
//		char keyCode = (char) event.getKeyCode();
//		switch (keyCode) {
//		case 'X':
//			doCut(selection);
//			break;
//		case 'C':
//			doCopy(selection);
//			break;
//		case 'V':
//			if (_clipboard != null){
//				//what the god-dam implementation in user customizable side here
//				//TODO
////				_spreadsheet.smartUpdate("doPasteFromServer", true);
//			}
//			doPaste(selection);
//			break;
//		case 'D':
//			doClearContent(selection);
//			break;
//		case 'B':
//			doFontBold(selection);
//			break;
//		case 'I':
//			doFontItalic(selection);
//			break;
//		case 'U':
//			doFontUnderline(selection);
//			break;
//		}
//	}
//	
////	/**
////	 * Bind the handler's target
////	 * 
////	 * @param spreadsheet
////	 */
////	public void bind(Spreadsheet spreadsheet) {
////		if (_spreadsheet != spreadsheet) {
////			_spreadsheet = spreadsheet;
////			init();	
////		}
////	}
//	
////	/**
////	 * Unbind the handler's target
////	 */
////	public void unbind() {
////		if (_spreadsheet != null) {
//////			if (_upload.getParent() == _spreadsheet) {
//////				_spreadsheet.removeChild(_upload);
//////			}
////			
////			_spreadsheet.removeEventListener(Events.ON_SHEET_SELECT, _doSelectSheetListener);
////			_spreadsheet.removeEventListener(Events.ON_CTRL_KEY, _doCtrlKeyListener);
////		}
////	}
//	
//	/**
//	 * When user click Home pane
//	 * 
//	 * <p>
//	 * Default: do nothing
//	 */
//	protected void doHomePanel() {
//	}
//	
//	/**
//	 * Execute when user click formula panel
//	 * 
//	 * <p>
//	 * Default: do nothing
//	 */
//	protected void doFormulaPanel() {
//	}
//	
//	/**
//	 * Execute when user click insert panel
//	 * 
//	 * <p>
//	 * Default: do nothing
//	 */
//	protected void doInsertPanel() {
//	}
//
//	/**
//	 * Execute when user click new book
//	 */
//	protected abstract void doNewBook();
//	
//	/**
//	 * Execute when user click save book
//	 */
//	protected abstract void doSaveBook();
//	
//	/**
//	 * Execute when user click export PDF
//	 */
//	protected abstract void doExportPDF(Rect selection);
//	
//	protected Clipboard getClipboard() {
//		return _clipboard;
//	}
//	
//	protected void setClipboard(Clipboard clipboard){
//		_clipboard = clipboard;
//	}
//	
//	protected void clearClipboard() {
//		_clipboard = null;
//		getSpreadsheet().setHighlight(null);
//		//TODO: shall also clear client side clipboard if possible
//	}
//	
//	/**
//	 * Execute when user click copy
//	 */
//	protected void doCopy(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		setClipboard(new Clipboard(Clipboard.Type.COPY, sheet.getSheetName(), selection));
//		getSpreadsheet().setHighlight(getSelection());
//	}
//	
//	protected void doPaste(Rect selection, PasteType pasteType, PasteOperation pasteOperation, boolean skipBlank, boolean transpose) {
//		Clipboard cb = getClipboard();
//		if(cb==null)
//			return;
//		
//		Book book = getBook();
//		Sheet destSheet = getSelectedSheet();
//		Sheet srcSheet = book.getSheet(cb.sourceSheetName);
//		if(srcSheet==null){
//			//TODO message;
//			clearClipboard();
//			return;
//		}
//		Rect src = cb.sourceRect;
//		
//		
//		Range srcRange = Ranges.range(srcSheet, src.getTop(),
//				src.getLeft(), src.getBottom(),src.getRight());
//
//		Range destRange = Ranges.range(destSheet, selection.getTop(),
//				selection.getLeft(), selection.getBottom(), selection.getRight());
//		
//		if (destRange.isProtected()) {
//			showProtectMessage();
//			return;
//		} else if (cb.type == Type.CUT && srcRange.isProtected()) {
//			showProtectMessage();
//			return;
//		}
//		
//		if(cb.type==Type.CUT){
//			CellOperationUtil.cut(srcRange,destRange);
//			clearClipboard();
//		}else{
//			CellOperationUtil.pasteSpecial(srcRange, destRange, pasteType, pasteOperation, skipBlank, transpose);
//		}
//	}
//	
//	protected void showProtectMessage() {
//		Messagebox.show("The cell that you are trying to change is protected and locked.", "ZK Spreadsheet", Messagebox.OK, Messagebox.EXCLAMATION);
//	}
//	
//	protected void showWarnMessage(String message) {
//		Messagebox.show(message, "ZK Spreadsheet", Messagebox.OK, Messagebox.EXCLAMATION);
//	}
//	
//	/**
//	 * Execute when user click paste 
//	 */
//	protected void doPaste(Rect selection) {
//		doPaste(selection,PasteType.PASTE_ALL,PasteOperation.PASTEOP_NONE,false,false);
//	}
//	
//	
//	/**
//	 * Execute when user click paste formula
//	 */
//	protected void doPasteFormula(Rect selection) {
//		doPaste(selection,PasteType.PASTE_FORMULAS,PasteOperation.PASTEOP_NONE,false,false);
//	}
//	
//	/**
//	 *  Execute when user click paste value
//	 */
//	protected void doPasteValue(Rect selection) {
//		doPaste(selection,PasteType.PASTE_VALUES,PasteOperation.PASTEOP_NONE,false,false);
//	}
//	
//	/**
//	 * Execute when user click paste all except border
//	 */
//	protected void doPasteAllExceptBorder(Rect selection) {
//		doPaste(selection,PasteType.PASTE_ALL_EXCEPT_BORDERS,PasteOperation.PASTEOP_NONE,false,false);
//	}
//	
//	/**
//	 * Execute when user click paste transpose
//	 */
//	protected void doPasteTranspose(Rect selection) {
//		doPaste(selection, PasteType.PASTE_ALL, PasteOperation.PASTEOP_NONE, false, true);
//	}
//	
//	/**
//	 * Execute when user click paste special
//	 */
//	protected abstract void doPasteSpecial(Rect selection);
//	
//	/**
//	 * Execute when user click cut
//	 */
//	protected void doCut(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		setClipboard(new Clipboard(Clipboard.Type.CUT, sheet.getSheetName(), selection));
//		getSpreadsheet().setHighlight(getSelection());
//	}
//	
//	/**
//	 * Execute when user select font family
//	 * 
//	 * @param selection
//	 */
//	protected void doFontFamily(String fontFamily, Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.applyFontName(range, fontFamily);
//	}
//	
//	/**
//	 * Execute when user select font size
//	 *  
//	 * @param selection
//	 */
//	protected void doFontSize(int fontSize, Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.applyFontSize(range, (short)fontSize);
//	}
//	
//	/**
//	 * Execute when user click font bold
//	 * 
//	 * @param selection
//	 */
//	protected void doFontBold(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//
//		//toggle and apply bold of first cell to dest
//		Boldweight bw = range.getCellStyle().getFont().getBoldweight();
//		if(Boldweight.BOLD.equals(bw)){
//			bw = Boldweight.NORMAL;
//		}else{
//			bw = Boldweight.BOLD;
//		}
//		
//		CellOperationUtil.applyFontBoldweight(range, bw);
//	}
//	
//	/**
//	 * Execute when user click font italic
//	 * 
//	 * @param selection
//	 */
//	protected void doFontItalic(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//
//		//toggle and apply bold of first cell to dest
//		boolean italic = !range.getCellStyle().getFont().isItalic();
//		CellOperationUtil.applyFontItalic(range, italic);	
//	}
//	
//	/**
//	 * Execute when user click font strikethrough
//	 * 
//	 * @param selection
//	 */
//	protected void doFontStrikeout(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//
//		//toggle and apply bold of first cell to dest
//		boolean strikeout = !range.getCellStyle().getFont().isStrikeout();
//		CellOperationUtil.applyFontStrikeout(range, strikeout);
//	}
//	
//	/**
//	 * Execute when user click font underline
//	 * 
//	 * @param selection
//	 */
//	protected void doFontUnderline(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//
//		//toggle and apply bold of first cell to dest
//		Underline underline = range.getCellStyle().getFont().getUnderline();
//		if(Underline.NONE.equals(underline)){
//			underline = Underline.SINGLE;
//		}else{
//			underline = Underline.NONE;
//		}
//		
//		CellOperationUtil.applyFontUnderline(range, underline);	
//	}
//	
//	
//	protected void doBorder(Rect selection, ApplyBorderType type,BorderType borderTYpe, String color){
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.applyBorder(range,type, borderTYpe, color);
//	}
//	
//	/**
//	 * Execute when user click border
//	 * 
//	 * @param selection
//	 */
//	protected void doBorder(String color, Rect selection) {
//		doBorder(selection,ApplyBorderType.EDGE_BOTTOM,BorderType.MEDIUM,color);
//	}
//	
//	/**
//	 * Execute when user click bottom border
//	 * 
//	 * @param selection
//	 */
//	protected void doBorderBottom(String color, Rect selection) {
//		doBorder(selection,ApplyBorderType.EDGE_BOTTOM,BorderType.MEDIUM,color);
//	}
//	
//	/**
//	 * Execute when user click top border
//	 * 
//	 * @param selection
//	 */
//	protected void doBoderTop(String color, Rect selection) {
//		doBorder(selection,ApplyBorderType.EDGE_TOP,BorderType.MEDIUM,color);
//	}
//	
//	/**
//	 * Execute when user click left border
//	 * 
//	 * @param selection
//	 */
//	protected void doBorderLeft(String color, Rect selection) {
//		doBorder(selection,ApplyBorderType.EDGE_LEFT,BorderType.MEDIUM,color);
//	}
//	
//	/**
//	 * Execute when user click right border
//	 * 
//	 * @param selection
//	 */
//	protected void doBorderRight(String color, Rect selection) {
//		doBorder(selection,ApplyBorderType.EDGE_RIGHT,BorderType.MEDIUM,color);
//	}
//	
//	/**
//	 * Execute when user click no border
//	 * 
//	 * @param selection
//	 */
//	protected void doBorderNo(String color, Rect selection) {
//		doBorder(selection,ApplyBorderType.FULL,BorderType.NONE,color);
//	}
//	
//	/**
//	 * Execute when user click all border
//	 * 
//	 * @param selection
//	 */
//	protected void doBorderAll(String color, Rect selection) {
//		doBorder(selection,ApplyBorderType.FULL,BorderType.MEDIUM,color);
//	}
//	
//	/**
//	 * Execute when user click outside border
//	 * 
//	 * @param selection
//	 */
//	protected void doBorderOutside(String color, Rect selection) {
//		doBorder(selection,ApplyBorderType.OUTLINE,BorderType.MEDIUM,color);
//	}
//	
//	/**
//	 * Execute when user click inside border
//	 * 
//	 * @param selection
//	 */
//	protected void doBorderInside(String color, Rect selection) {
//		doBorder(selection,ApplyBorderType.INSIDE,BorderType.MEDIUM,color);
//	}
//	
//	/**
//	 * Execute when user click inside horizontal border
//	 * 
//	 * @param selection
//	 */
//	protected void doBorderInsideHorizontal(String color, Rect selection) {
//		doBorder(selection,ApplyBorderType.INSIDE_HORIZONTAL,BorderType.MEDIUM,color);
//	}
//	
//	/**
//	 * Execute when user click inside vertical border
//	 * 
//	 * @param selection
//	 */
//	protected void doBorderInsideVertical(String color, Rect selection) {
//		doBorder(selection,ApplyBorderType.INSIDE_VERTICAL,BorderType.MEDIUM,color);
//	}
//	
//	/**
//	 * Execute when user click font color 
//	 * 
//	 * @param color
//	 * @param selection
//	 */
//	protected void doFontColor(String color, Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.applyFontColor(range, color);	
//	}
//	
//	/**
//	 * Execute when user click fill color
//	 * 
//	 * @param color
//	 * @param selection
//	 */
//	protected void doFillColor(String color, Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.applyCellColor(range,color);
//	}
//	
//	/**
//	 * Execute when user click vertical align top
//	 * 
//	 * @param selection
//	 */
//	protected void doVerticalAlignTop(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.applyCellVerticalAlignment(range, VerticalAlignment.TOP);
//	}
//	
//	/**
//	 * Execute when user click vertical align middle
//	 * 
//	 * @param selection
//	 */
//	protected void doVerticalAlignMiddle(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.applyCellVerticalAlignment(range, VerticalAlignment.CENTER);
//	}
//
//	/**
//	 * Execute when user click vertical align bottom
//	 * 
//	 * @param selection
//	 */
//	protected void doVerticalAlignBottom(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.applyCellVerticalAlignment(range, VerticalAlignment.BOTTOM);
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doHorizontalAlignLeft(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.applyCellAlignment(range, Alignment.LEFT);
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doHorizontalAlignCenter(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.applyCellAlignment(range, Alignment.CENTER);
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doHorizontalAlignRight(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.applyCellAlignment(range, Alignment.RIGHT);
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doWrapText(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		boolean wrapped = !range.getCellStyle().isWrapText();
//		CellOperationUtil.applyCellWrapText(range, wrapped);
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doMergeAndCenter(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.toggleMergeCenter(range);
//		clearClipboard();
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doMergeAcross(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.merge(range, true);
//		clearClipboard();
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doMergeCell(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.merge(range, false);
//		clearClipboard();
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doUnmergeCell(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.unMerge(range);
//		clearClipboard();
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doShiftCellRight(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.insert(range,InsertShift.RIGHT, InsertCopyOrigin.RIGHT_BELOW);
//		clearClipboard();
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doShiftCellDown(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.insert(range,InsertShift.DOWN, InsertCopyOrigin.LEFT_ABOVE);
//		clearClipboard();
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doInsertSheetRow(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		
//		if(range.isWholeColumn()){
//			showWarnMessage("don't allow to inser row when select whole column");
//			return;
//		}
//		
//		range = range.getRowRange();
//		CellOperationUtil.insert(range,InsertShift.DOWN, InsertCopyOrigin.LEFT_ABOVE);
//		clearClipboard();
//		
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doInsertSheetColumn(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		
//		if(range.isWholeRow()){
//			showWarnMessage("don't allow to inser column when select whole row");
//			return;
//		}
//		
//		range = range.getColumnRange();
//		CellOperationUtil.insert(range,InsertShift.RIGHT, InsertCopyOrigin.RIGHT_BELOW);
//		clearClipboard();
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doShiftCellLeft(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		
//		CellOperationUtil.delete(range,DeleteShift.LEFT);
//		clearClipboard();
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doShiftCellUp(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		
//		CellOperationUtil.delete(range,DeleteShift.UP);
//		clearClipboard();
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doDeleteSheetRow(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		
//		if(range.isWholeColumn()){
//			showWarnMessage("don't allow to delete all rows");
//			return;
//		}
//		
//		range = range.getRowRange();
//		CellOperationUtil.delete(range, DeleteShift.UP);
//		clearClipboard();
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doDeleteSheetColumn(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		
//		if(range.isWholeRow()){
//			showWarnMessage("don't allow to delete all column");
//			return;
//		}
//		
//		range = range.getColumnRange();
//		CellOperationUtil.delete(range, DeleteShift.LEFT);
//		clearClipboard();
//	}
//
//	/**
//	 * Execute when user click clear style
//	 */
//	protected void doClearStyle(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.clearStyles(range);
//	}
//	
//	/**
//	 * Execute when user click clear content
//	 */
//	protected void doClearContent(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.clearContents(range);
//	}
//	
//	/**
//	 * Execute when user click clear all
//	 */
//	protected void doClearAll(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.clearAll(range);
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doSortAscending(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.sort(range,false);
//		clearClipboard();
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doSortDescending(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		CellOperationUtil.sort(range,true);
//		clearClipboard();
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected abstract void doCustomSort(Rect selection);
//	
//	/**
//	 * @param selection
//	 */
//	protected void doFilter(Rect selection) {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		SheetOperationUtil.toggleAutoFilter(range);
//		clearClipboard();
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doClearFilter() {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet);
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		SheetOperationUtil.resetAutoFilter(range);
//		clearClipboard();
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doReapplyFilter() {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet);
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		SheetOperationUtil.applyAutoFilter(range);
//		clearClipboard();
//	}
//	
//	/**
//	 * 
//	 */
//	protected void doProtectSheet() {
//		
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet);
//		
//		String newpassword = "1234";//TODO, make it meaningful
//		if(range.isProtected()){
//			SheetOperationUtil.protectSheet(range,null,null);
//		}else{
//			SheetOperationUtil.protectSheet(range,null,newpassword);
//		}
//		
//		boolean p = range.isProtected();
//		
//		//TODO re-factor action bar
////		for (Action action : _defaultDisabledActionOnSheetProtected) {
////			getSpreadsheet().setActionDisabled(p, action);
////		}
//	}
//	
//	/**
//	 * 
//	 */
//	protected void doGridlines() {
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet);
//		
//		SheetOperationUtil.displaySheetGridlines(range,!range.isDisplaySheetGridlines());
//	}
//	
//	
//	protected void doChart(Rect selection, Chart.Type type, Chart.Grouping grouping, Chart.LegendPosition pos){
//		Sheet sheet = getSelectedSheet();
//		if(sheet==null) return;
//		Range range = Ranges.range(sheet, selection.getTop(), selection.getLeft(), selection.getBottom(), selection.getRight());
//		if(range.isProtected()){
//			showProtectMessage();
//			return;
//		}
//		
//		SheetAnchor anchor = SheetOperationUtil.toChartAnchor(range);
//		
//		ChartData data = org.zkoss.zss.api.ChartDataUtil.getChartData(sheet,selection, type);
//		SheetOperationUtil.addChart(range,data,type,grouping,pos);
//		clearClipboard();
//		
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doColumnChart(Rect selection) {
//		doChart(selection,Chart.Type.Column,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
//	}
//	
//	/**
//	 * @param selection
//	 */
//	protected void doColumnChart3D(Rect selection) {
//		doChart(selection,Chart.Type.Column3D,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
//	}
//	/**
//	 * @param selection
//	 */
//	protected void doLineChart(Rect selection) {
//		doChart(selection,Chart.Type.Line,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
//	}
//	/**
//	 * @param selection
//	 */
//	protected void doLineChart3D(Rect selection) {
//		doChart(selection,Chart.Type.Line3D,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
//	}
//	/**
//	 * @param selection
//	 */
//	protected void doPieChart(Rect selection) {
//		doChart(selection,Chart.Type.Pie,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
//	}
//	/**
//	 * @param selection
//	 */
//	protected void doPieChart3D(Rect selection) {
//		doChart(selection,Chart.Type.Pie3D,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
//	}
//	/**
//	 * @param selection
//	 */
//	protected void doBarChart(Rect selection) {
//		doChart(selection,Chart.Type.Bar,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
//	}
//	/**
//	 * @param selection
//	 */
//	protected void doBarChart3D(Rect selection) {
//		doChart(selection,Chart.Type.Bar3D,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
//	}
//	/**
//	 * @param selection
//	 */
//	protected void doAreaChart(Rect selection) {
//		doChart(selection,Chart.Type.Area,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
//	}
//	/**
//	 * @param selection
//	 */
//	protected void doScatterChart(Rect selection) {
//		doChart(selection,Chart.Type.Scatter,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
//	}
//	/**
//	 * @param selection
//	 */
//	protected void doDoughnutChart(Rect selection) {
//		doChart(selection,Chart.Type.Doughnut,Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
//	}
//
//	
////	/** convert pixel to EMU */
////	private static int pxToEmu(int px) {
////		return (int) Math.round(((double)px) * 72 * 20 * 635 / 96); //assume 96dpi
////	}
//	
//	/**
//	 * 
//	 */
//	protected abstract void doHyperlink(Rect selection);
//	
//	/**
//	 * @param selection
//	 */
//	protected abstract void doFormatCell(Rect selection);
//	
//	private static <T> T checkNotNull(String message, T t) {
//		if (t == null) {
//			throw new NullPointerException(message);
//		}
//		return t;
//	}
//	
//	/**
//	 * Used for copy & paste function
//	 * 
//	 * @author sam
//	 */
//	public static class Clipboard {
//		public enum Type {
//			COPY,
//			CUT
//		}
//		
//		public final Type type;
//		public final Rect sourceRect;
//		public final String sourceSheetName;
//		
//		public Clipboard(Type type, String sourceSheetName,Rect sourceRect) {
//			this.type = checkNotNull("Clipboard's type cannot be null", type);
//			this.sourceSheetName = checkNotNull("Clipboard's sourceSheetName cannot be null", sourceSheetName);
//			this.sourceRect = checkNotNull("Clipboard's sourceRect cannot be null", sourceRect);
//		}
//	}
//}
