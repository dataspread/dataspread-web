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
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SRow;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheetProtection;
import org.zkoss.zss.model.impl.AbstractRowAdv;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.impl.HeaderPositionHelper;
import org.zkoss.zss.ui.impl.UnlockedCellsHelper;
import org.zkoss.zss.ui.impl.UnlockedCellsHelper.UnlockedCellInfo;
import org.zkoss.zss.ui.sys.SpreadsheetCtrl;

/**
 * Handle the user operation when arrow key is pressed to jump around unlocked
 * cells on a protected sheet. 
 * !isCellLocked(row, col) && !row.isHidden() && !col.isHidden();
 * @author henrichen
 * @since 3.8.1
 */
//ZSS-1085
public class ShiftPosCommand extends AbstractCommand implements Command {

	@Override
	public void process(AuRequest request) {
		final Component comp = request.getComponent();
		if (comp == null)
			throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED,
					ShiftPosCommand.class);
		
		final Map data = (Map) request.getData();
		if (data == null || data.size() != 4)
			throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA, new Object[] {Objects.toString(data), ShiftPosCommand.class });
		
		Spreadsheet spreadsheet = ((Spreadsheet) comp);
		String sheetId = (String) data.get("sheetId");
		SSheet sheet = spreadsheet.getSelectedSSheet();
		
		if (!sheetId.equals(sheet.getId())) return;
		
		int[] index = unlockedIndex(spreadsheet, sheet, data);
		if (index != null) {
			int rowIdx = index[0];
			int colIdx = index[1];
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
	
	private int[] unlockedIndex(Spreadsheet spreadsheet, SSheet sheet, Map data) {
		String sheetId = (String) data.get("sheetId");
		int rowIdx = (Integer) data.get("row");
		int colIdx = (Integer) data.get("col");
		String dir = (String) data.get("dir");
		if ("home".equals(dir)) {
			return unlockedIndex0(spreadsheet, sheet, data, sheetId, 0, -1, "right");
		} else if ("end".equals(dir)) {
			return unlockedIndex0(spreadsheet, sheet, data, sheetId, spreadsheet.getCurrentMaxVisibleRows()-1, spreadsheet.getCurrentMaxVisibleColumns(), "left");
		} else {
			return unlockedIndex0(spreadsheet, sheet, data, sheetId, rowIdx, colIdx, dir);
		}
	}
	
	private int[] unlockedIndex0(Spreadsheet spreadsheet, SSheet sheet, Map data, String sheetId, int rowIdx, int colIdx, String dir) {
		final int rowIdx0 = rowIdx;
		final int colIdx0 = colIdx;
		final SpreadsheetCtrl spreadsheetCtrl = ((SpreadsheetCtrl) spreadsheet.getExtraCtrl());
		final HeaderPositionHelper rhHelper = spreadsheetCtrl.getRowPositionHelper(sheetId);
		final HeaderPositionHelper chHelper = spreadsheetCtrl.getColumnPositionHelper(sheetId);
		if ("left".equals(dir)) {
			/*
		     *       <-------
			 *   <-----------
			 *   <-----[X]
			 */
			boolean cycle = false;
			while (!cycle || rowIdx > rowIdx0 || (rowIdx == rowIdx0 && colIdx > colIdx0)) {
				UnlockedCellsHelper helper = createColumnUnlockedCellsHelper(sheet, rowIdx);
				if (helper == null) { //no Unlocked cell at all!
					rowIdx = rhHelper.getPrevNonHidden(rowIdx); //previous unhidden row
					colIdx = spreadsheet.getCurrentMaxVisibleColumns();
					if (rowIdx < 0) {
						rowIdx = spreadsheet.getCurrentMaxVisibleRows() - 1;
						cycle = true;
					}
					continue;
				}
				int prev = colIdx;
				while (true) {
					prev = helper.getPrevUnlocked(prev);
					if (prev >= 0) { // found unlocked
						if (!chHelper.isHidden(prev)) { // !hidden
							int[] result = new int[2];
							result[0] = rowIdx;
							result[1] = prev;
							return result; // found
						}
					} else { // not found unlocked in this row
						rowIdx = rhHelper.getPrevNonHidden(rowIdx); //previous unhidden row
						colIdx = spreadsheet.getCurrentMaxVisibleColumns();
						if (rowIdx < 0) {
							rowIdx = spreadsheet.getCurrentMaxVisibleRows() - 1;
							cycle = true;
						}
						break;
					}
				}
			}
			return null;
		} else if ("right".equals(dir)) {
			/*
			 *      [X]----->
			 *   ----------->
			 *   ------->
			 */
			boolean cycle = false;
			while (!cycle || rowIdx < rowIdx0 || (rowIdx == rowIdx0 && colIdx < colIdx0)) {
				UnlockedCellsHelper helper = createColumnUnlockedCellsHelper(sheet, rowIdx);
				if (helper == null) { //no Unlocked cell at all!
					rowIdx = rhHelper.getNextNonHidden(rowIdx); //next unhidden row
					colIdx = -1;
					if (rowIdx < 0 || rowIdx >= spreadsheet.getCurrentMaxVisibleRows()) {
						rowIdx = 0;
						cycle = true;
					}
					continue;
				}
				int next = colIdx;
				while (true) {
					next = helper.getNextUnlocked(next);
					if (next >= 0) {
						if (!chHelper.isHidden(next)) {
							if (next >= spreadsheet.getCurrentMaxVisibleColumns()) { // found but outside max visible columns
								rowIdx = rhHelper.getNextNonHidden(rowIdx); //next unhidden row
								colIdx = -1;
								if (rowIdx < 0 || rowIdx >= spreadsheet.getCurrentMaxVisibleRows()) {
									rowIdx = 0;
									cycle = true;
								}
								break;
							}
							int[] result = new int[2];
							result[0] = rowIdx;
							result[1] = next;
							return result; // found
						}
					} else { // not found unlocked in this row
						rowIdx = rhHelper.getNextNonHidden(rowIdx); //next unhidden row
						colIdx = -1;
						if (rowIdx < 0 || rowIdx >= spreadsheet.getCurrentMaxVisibleRows()) {
							rowIdx = 0;
							cycle = true;
						}
						break;
					}
				}
			}
			return null;
		} else if ("up".equals(dir)) {
			/*
			 *      ^  ^
			 *      |  |
			 *   ^  |  |
			 *   |  |  |
			 *   |  | [X]
		     *   |  | 
		     *   |  | 
			 */
			boolean cycle = false;
			while (!cycle || colIdx > colIdx0 || (colIdx == colIdx0 && rowIdx > rowIdx0)) {
				UnlockedCellsHelper helper = createRowUnlockedCellsHelper(sheet, colIdx);
				if (helper == null) { //no Unlocked cell at all!
					colIdx = chHelper.getPrevNonHidden(colIdx); //previous unhidden column
					rowIdx = spreadsheet.getCurrentMaxVisibleRows();
					if (colIdx < 0) {
						colIdx = spreadsheet.getCurrentMaxVisibleColumns() - 1;
						cycle = true;
					}
					continue;
				}
				int prev = rowIdx;
				while (true) {
					prev = helper.getPrevUnlocked(prev);
					if (prev >= 0) {
						if (!rhHelper.isHidden(prev)) { // !hidden
							int[] result = new int[2];
							result[0] = prev;
							result[1] = colIdx;
							return result; // found
						}
					} else {
						colIdx = chHelper.getPrevNonHidden(colIdx); //previous unhidden column
						rowIdx = spreadsheet.getCurrentMaxVisibleRows();
						if (colIdx < 0) {
							colIdx = spreadsheet.getCurrentMaxVisibleColumns() - 1;
							cycle = true;
						}
						break;
					}
				}
			}
			return null;
		} else if ("down".equals(dir)) {
			/*
			 *       |  |
			 *       |  |
			 *   [X] |  |
			 *    |  |  |
		     *    |  |  V
		     *    |  |
		     *    V  V 
			 */
			boolean cycle = false;
			while (!cycle || colIdx < colIdx0 || (colIdx == colIdx0 && rowIdx < rowIdx0)) {
				UnlockedCellsHelper helper = createRowUnlockedCellsHelper(sheet, colIdx);
				if (helper == null) { //no Unlocked cell at all!
					colIdx = chHelper.getNextNonHidden(colIdx); //next unhidden column
					rowIdx = -1;
					if (colIdx < 0 || colIdx >= spreadsheet.getCurrentMaxVisibleColumns()) {
						colIdx = 0;
						cycle = true;
					}
					continue;
				}
				int next = rowIdx;
				while (true) {
					next = helper.getNextUnlocked(next);
					if (next >= 0) { // found unlocked
						if (!rhHelper.isHidden(next)) {
							if (next >= spreadsheet.getCurrentMaxVisibleRows()) {
								colIdx = chHelper.getNextNonHidden(colIdx); //next unhidden column
								rowIdx = -1;
								if (colIdx < 0 || colIdx >= spreadsheet.getCurrentMaxVisibleColumns()) {
									colIdx = 0;
									cycle = true;
								}
								break;
							}
							int[] result = new int[2];
							result[0] = next;
							result[1] = colIdx;
							return result; // found
						}
					} else { // no unlocked in this column
						colIdx = chHelper.getNextNonHidden(colIdx); //next unhidden column
						rowIdx = -1;
						if (colIdx < 0 || colIdx >= spreadsheet.getCurrentMaxVisibleColumns()) {
							colIdx = 0;
							cycle = true;
						}
						break;
					}
				}
			}
			return null;
		}
		throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA, new Object[] {Objects.toString(data), ShiftPosCommand.class }); 
	}
	
	private UnlockedCellsHelper createColumnUnlockedCellsHelper(SSheet sheet, int rowIdx) {
		int start = -1;
		int end = -1;
		SRow row = sheet.getRow(rowIdx);
		if (row == null) {
			return null;
		}
		
		List<UnlockedCellInfo> infos = new ArrayList<UnlockedCellInfo>();
		for (Iterator<SCell> it = row.getCellIterator(); it.hasNext();) { 
			SCell cell = it.next();
			
			// blank cell
			if (cell == null || cell.isNull() || cell.getCellStyle().isLocked()) {
				if (start < 0) { // locked segement
					continue;
				} else {
					infos.add(new UnlockedCellInfo(start, end));
					start = -1;
					end = -1;
				}
			} else { //unlocked cell
				final int colIdx = cell.getColumnIndex();
				if (start < 0) {
					start = end = colIdx;
				} else if (end + 1 == colIdx) { //continue
					end = colIdx;
				} else { //discontinued
					infos.add(new UnlockedCellInfo(start, end));
					start = end = colIdx;
				}
			}
		}
		if (start >= 0) {
			infos.add(new UnlockedCellInfo(start, end));
		}
		return infos.isEmpty() ? null : new UnlockedCellsHelper(infos);
	}
	
	private UnlockedCellsHelper createRowUnlockedCellsHelper(SSheet sheet, int columnIdx) {
		int start = -1;
		int end = -1;
		List<UnlockedCellInfo> infos = new ArrayList<UnlockedCellInfo>();
		for (Iterator<SRow> it = sheet.getRowIterator(); it.hasNext();) {
			final SRow row = it.next();
			final SCell cell = ((AbstractRowAdv)row).getCell(columnIdx, false);
			
			// blank cell
			if (cell == null || cell.isNull() || cell.getCellStyle().isLocked()) {
				if (start < 0) { // locked segement
					continue;
				} else {
					infos.add(new UnlockedCellInfo(start, end));
					start = -1;
					end = -1;
				}
			} else { //unlocked cell
				final int rowIdx = row.getIndex();
				if (start < 0) {
					start = end = rowIdx;
				} else if (end + 1 == rowIdx) { //continue
					end = rowIdx;
				} else { //discontinued
					infos.add(new UnlockedCellInfo(start, end));
					start = end = rowIdx;
				}
			}
		}
		if (start >= 0) {
			infos.add(new UnlockedCellInfo(start, end));
		}
		return infos.isEmpty() ? null : new UnlockedCellsHelper(infos);
	}
}
