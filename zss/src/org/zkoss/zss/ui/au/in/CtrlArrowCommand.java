/* CtrlArrowCommand.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jun 15, 2015 12:35:10 PM , Created by henrichen
}}IS_NOTE

Copyright (C) 2015 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.ui.au.in;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.zkoss.json.JSONObject;
import org.zkoss.lang.Objects;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.mesg.MZk;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zss.model.SCell.CellType;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SRow;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheetProtection;
import org.zkoss.zss.model.impl.AbstractRowAdv;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.impl.HeaderPositionHelper;
import org.zkoss.zss.ui.impl.NonBlankCellsHelper;
import org.zkoss.zss.ui.impl.NonBlankCellsHelper.NonBlankCellInfo;
import org.zkoss.zss.ui.sys.SpreadsheetCtrl;

/**
 * Handle the user operation when Ctrl-arrow key is pressed.
 * @author henrichen
 * @since 3.8.1
 */
//ZSS-1000
public class CtrlArrowCommand extends AbstractCommand implements Command {

	@Override
	public void process(AuRequest request) {
		final Component comp = request.getComponent();
		if (comp == null)
			throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED,
					CtrlArrowCommand.class);
		
		final Map data = (Map) request.getData();
		if (data == null || data.size() != 4)
			throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA, new Object[] {Objects.toString(data), CtrlArrowCommand.class });
		
		Spreadsheet spreadsheet = ((Spreadsheet) comp);
		String sheetId = (String) data.get("sheetId");
		SSheet sheet = spreadsheet.getSelectedSSheet();
		
		if (sheetId.equals(sheet.getId())) {
			int index = nonBlankIndex(spreadsheet, sheet, data);
			if (index >= 0) {
				String dir = (String) data.get("dir");
				int rowIdx = (Integer) data.get("row");
				int colIdx = (Integer) data.get("col");
				if ("up".equals(dir) || "down".equals(dir)) {
					rowIdx = index;
				} else {
					colIdx = index;
				}
				if (sheet.isProtected()) {
					SSheetProtection prot = sheet.getSheetProtection();
					if (prot != null) {
						if (prot.isSelectUnlockedCells()) {
							final SCellStyle style  = sheet.getCell(rowIdx, colIdx).getCellStyle();
							if (style.isLocked()) {
								return;
							}
						} else {
							return;
						}
					}
				}
				JSONObject param = new JSONObject();
				param.put("sheetId", sheetId);
				param.put("row", rowIdx);
				param.put("col", colIdx);
				spreadsheet.smartUpdate("ctrlArrowMoveFocus", param);
			}
		}
	}
	
	private int nonBlankIndex(Spreadsheet spreadsheet, SSheet sheet, Map data) {
		final SpreadsheetCtrl spreadsheetCtrl = ((SpreadsheetCtrl) spreadsheet.getExtraCtrl());
		String sheetId = (String) data.get("sheetId");
		int rowIdx = (Integer) data.get("row");
		int colIdx = (Integer) data.get("col");
		String dir = (String) data.get("dir");
		
		if ("left".equals(dir)) {
			NonBlankCellsHelper helper = createRowNonBlankCellsHelper(sheet, rowIdx);
			HeaderPositionHelper phelper = spreadsheetCtrl.getColumnPositionHelper(sheetId);
			if (helper == null) { //no nonblank cell at all!
				return phelper.getNextNonHidden(-1);
			}
			int prev = colIdx;
			while (true) {
				prev = helper.getPrevNonBlank(prev);
				if (prev >= 0) {
					if (!phelper.isHidden(prev)) {
						return prev;
					}
				} else {
					return phelper.getNextNonHidden(-1);
				}
			}
		} else if ("right".equals(dir)) {
			NonBlankCellsHelper helper = createRowNonBlankCellsHelper(sheet, rowIdx);
			HeaderPositionHelper phelper = spreadsheetCtrl.getColumnPositionHelper(sheetId);
			if (helper == null) { //no nonblank cell at all!
				return phelper.getPrevNonHidden(spreadsheet.getCurrentMaxVisibleColumns()); //ZSS-1084
			}
			int next = colIdx;
			while (true) {
				next = helper.getNextNonBlank(next);
				if (next >= 0) {
					if (!phelper.isHidden(next)) {
						if (next >= spreadsheet.getCurrentMaxVisibleColumns()) { //ZSS-1084
							return phelper.getPrevNonHidden(spreadsheet.getCurrentMaxVisibleColumns()); //ZSS-1084
						}
						return next;
					}
				} else {
					return phelper.getPrevNonHidden(spreadsheet.getCurrentMaxVisibleColumns()); //ZSS-1084
				}
			}
		} else if ("up".equals(dir)) {
			NonBlankCellsHelper helper = createColumnNonBlankCellsHelper(sheet, colIdx);
			HeaderPositionHelper phelper = spreadsheetCtrl.getRowPositionHelper(sheetId);
			if (helper == null) { //no nonblank cell at all!
				return phelper.getNextNonHidden(-1);
			}
			int prev = rowIdx;
			while (true) {
				prev = helper.getPrevNonBlank(prev);
				if (prev >= 0) {
					if (!phelper.isHidden(prev)) {
						return prev;
					}
				} else {
					return phelper.getNextNonHidden(-1);
				}
			}
		} else if ("down".equals(dir)) {
			NonBlankCellsHelper helper = createColumnNonBlankCellsHelper(sheet, colIdx);
			HeaderPositionHelper phelper = spreadsheetCtrl.getRowPositionHelper(sheetId);
			if (helper == null) { //no nonblank cell at all!
				return phelper.getPrevNonHidden(spreadsheet.getCurrentMaxVisibleRows()); //ZSS-1084
			}
			int next = rowIdx;
			while (true) {
				next = helper.getNextNonBlank(next);
				if (next >= 0) {
					if (!phelper.isHidden(next)) {
						if (next >= spreadsheet.getCurrentMaxVisibleRows()) { //ZSS-1084
							return phelper.getPrevNonHidden(spreadsheet.getCurrentMaxVisibleRows()); //ZSS-1084
						}
						return next;
					}
				} else {
					return phelper.getPrevNonHidden(spreadsheet.getCurrentMaxVisibleRows()); //ZSS-1084
				}
			}
		}
		throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA, new Object[] {Objects.toString(data), CtrlArrowCommand.class }); 
	}
	
	private NonBlankCellsHelper createRowNonBlankCellsHelper(SSheet sheet, int rowIdx) {
		int start = -1;
		int end = -1;
		SRow row = sheet.getRow(rowIdx);
		if (row == null) {
			return null;
		}
		
		List<NonBlankCellInfo> infos = new ArrayList<NonBlankCellInfo>();
		for (Iterator<SCell> it = row.getCellIterator(); it.hasNext();) { 
			SCell cell = it.next();
			
			// blank cell
			if (cell == null || cell.isNull() || cell.getType() == CellType.BLANK) {
				if (start < 0) { // blank segement
					continue;
				} else {
					infos.add(new NonBlankCellInfo(start, end));
					start = -1;
					end = -1;
				}
			} else { //no blank cell
				final int colIdx = cell.getColumnIndex();
				if (start < 0) {
					start = end = colIdx;
				} else if (end + 1 == colIdx) { //continue
					end = colIdx;
				} else { //discontinued
					infos.add(new NonBlankCellInfo(start, end));
					start = end = colIdx;
				}
			}
		}
		if (start >= 0) {
			infos.add(new NonBlankCellInfo(start, end));
		}
		return infos.isEmpty() ? null : new NonBlankCellsHelper(infos);
	}
	
	private NonBlankCellsHelper createColumnNonBlankCellsHelper(SSheet sheet, int columnIdx) {
		int start = -1;
		int end = -1;
		List<NonBlankCellInfo> infos = new ArrayList<NonBlankCellInfo>();
		for (Iterator<SRow> it = sheet.getRowIterator(); it.hasNext();) {
			final SRow row = it.next();
			final SCell cell = ((AbstractRowAdv)row).getCell(columnIdx, false);
			
			// blank cell
			if (cell == null || cell.isNull() || cell.getType() == CellType.BLANK) {
				if (start < 0) { // blank segement
					continue;
				} else {
					infos.add(new NonBlankCellInfo(start, end));
					start = -1;
					end = -1;
				}
			} else { //no blank cell
				final int rowIdx = row.getIndex();
				if (start < 0) {
					start = end = rowIdx;
				} else if (end + 1 == rowIdx) { //continue
					end = rowIdx;
				} else { //discontinued
					infos.add(new NonBlankCellInfo(start, end));
					start = end = rowIdx;
				}
			}
		}
		if (start >= 0) {
			infos.add(new NonBlankCellInfo(start, end));
		}
		return infos.isEmpty() ? null : new NonBlankCellsHelper(infos);
	}
}
