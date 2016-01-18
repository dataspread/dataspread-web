/* CellFetchCommandHelper.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		January 10, 2008 03:10:40 PM , Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under Lesser GPL Version 2.1 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.au.in;


import java.util.Map;

import org.zkoss.json.JSONObject;
import org.zkoss.lang.Objects;
import org.zkoss.util.logging.Log;
import org.zkoss.zk.au.AuRequest;
import org.zkoss.zk.mesg.MZk;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.impl.HeaderPositionHelper;
import org.zkoss.zss.ui.impl.JSONObj;
import org.zkoss.zss.ui.impl.MergeMatrixHelper;
import org.zkoss.zss.ui.sys.FreezeInfoLoader;
import org.zkoss.zss.ui.sys.SpreadsheetCtrl;
import org.zkoss.zss.ui.sys.SpreadsheetInCtrl;


/**
 * A Command Helper for (client to server) for fetch data back
 * @author Dennis.Chen
 *
 */
public class CellFetchCommandHelper{
	private static final Log log = Log.lookup(CellFetchCommandHelper.class);
	
	private Spreadsheet _spreadsheet;
	private SpreadsheetCtrl _ctrl;
	HeaderPositionHelper _rowHelper;
	HeaderPositionHelper _colHelper;
	private MergeMatrixHelper _mergeMatrix;
	private boolean _hidecolhead;
	private boolean _hiderowhead;
	
	private int _lastleft; 
	private int _lastright;
	private int _lasttop;
	private int _lastbottom;
	
	private int _loadedLeft;
	private int _loadedRight;
	private int _loadedTop;
	private int _loadedBottom;
	
	private void responseDataBlock(String postfix, String token, String sheetid, String result) {
		//bug 1953830 Unnecessary command was sent and break the processing
		//use smartUpdate to instead
		//_spreadsheet.response(null, new org.zkoss.zss.ui.au.out.AuDataBlock(_spreadsheet,token,sheetid,result));
		
		//to avoid response be override in smartUpdate, I use a count-postfix
		//_spreadsheet.smartUpdateValues("dblock_"+Utils.nextUpdateId(),new Object[]{token,sheetid,result});
		
		_spreadsheet.smartUpdate(postfix != null ? "dataBlockUpdate" + postfix : "dataBlockUpdate", new String[] {token, sheetid, result});
	}
	
	//-- super --//
	protected void process(AuRequest request) {
		final Component comp = request.getComponent();
		if (comp == null)
			throw new UiException(MZk.ILLEGAL_REQUEST_COMPONENT_REQUIRED, this);
		final Map data = request.getData();
		if (data == null || data.size() != 26)
			throw new UiException(MZk.ILLEGAL_REQUEST_WRONG_DATA,
				new Object[] {Objects.toString(data), this});
		
		_spreadsheet = ((Spreadsheet)comp);
		if(_spreadsheet.isInvalidated()) return;//since it is invalidate, i don't need to update
		final SSheet selSheet = _spreadsheet.getSelectedSSheet();
		final String sheetId = (String) data.get("sheetId");
		if (selSheet == null || !sheetId.equals(selSheet.getId())) { //not current selected sheet, skip.
			return;
		}
		
		_ctrl = ((SpreadsheetCtrl)_spreadsheet.getExtraCtrl());
		_hidecolhead = _spreadsheet.isHidecolumnhead();
		_hiderowhead = _spreadsheet.isHiderowhead();
		String token = (String) data.get("token");
		
		_rowHelper = _ctrl.getRowPositionHelper(sheetId);
		_colHelper = _ctrl.getColumnPositionHelper(sheetId);
		
		SSheet sheet = _spreadsheet.getSelectedSSheet();
		if(!sheet.getId().equals(sheetId)) return;
		
		_mergeMatrix = _ctrl.getMergeMatrixHelper(sheet);
		
		String type = (String) data.get("type"); 
		String direction = (String) data.get("direction");
		
		//ZSS-440, might get double in IE 10 when room
		int dpWidth = AuDataUtil.getInt(data,"dpWidth");//pixel value of data panel width
		int dpHeight = AuDataUtil.getInt(data,"dpHeight");//pixel value of data panel height
		int viewWidth = AuDataUtil.getInt(data,"viewWidth");//pixel value of view width(scrollpanel.clientWidth)
		int viewHeight = AuDataUtil.getInt(data,"viewHeight");//pixel value of value height
		
		//current rendered block range
		int blockLeft = (Integer)data.get("blockLeft");
		int blockTop = (Integer)data.get("blockTop"); 
		int blockRight = (Integer)data.get("blockRight");// + blockLeft - 1;
		int blockBottom = (Integer)data.get("blockBottom");// + blockTop - 1;
		
		int fetchLeft = (Integer)data.get("fetchLeft");
		int fetchTop = (Integer)data.get("fetchTop"); 
		int fetchWidth = (Integer)data.get("fetchWidth");
		int fetchHeight = (Integer)data.get("fetchHeight");
		
		//visible range: cells that going to render
		int visibleLeft = (Integer)data.get("rangeLeft");//visible range
		int visibleTop = (Integer)data.get("rangeTop"); 
		int visibleRight = (Integer)data.get("rangeRight");
		int visibleBottom = (Integer)data.get("rangeBottom");
		
		//active range: extra cell's data
		int cacheLeft = (Integer)data.get("arLeft");//active range
		int cacheTop = (Integer)data.get("arTop");
		int cacheRight = (Integer)data.get("arRight");
		int cacheBottom = (Integer)data.get("arBottom");
		
		int cacheRangeFetchTopHeight = (Integer)data.get("arFetchTopHeight");
		int cacheRangeFetchBtmHeight = (Integer)data.get("arFetchBtmHeight");
		
		_loadedLeft = visibleLeft;
		_loadedTop = visibleTop;
		_loadedRight = visibleRight;
		_loadedBottom = visibleBottom;
		
		try{
			if("jump".equals(type)){
				String result = null;
				if("east".equals(direction)){
					result = jump("E",(Spreadsheet)comp,sheetId,sheet,type,dpWidth,dpHeight,viewWidth,viewHeight,blockLeft,blockTop,blockRight,blockBottom,fetchLeft,fetchTop,visibleLeft,visibleTop,visibleRight,visibleBottom);
				}else if("south".equals(direction)){
					result = jump("S",(Spreadsheet)comp,sheetId,sheet,type,dpWidth,dpHeight,viewWidth,viewHeight,blockLeft,blockTop,blockRight,blockBottom,fetchLeft,fetchTop,visibleLeft,visibleTop,visibleRight,visibleBottom);
				}else if("west".equals(direction)){
					result = jump("W",(Spreadsheet)comp,sheetId,sheet,type,dpWidth,dpHeight,viewWidth,viewHeight,blockLeft,blockTop,blockRight,blockBottom,fetchLeft,fetchTop,visibleLeft,visibleTop,visibleRight,visibleBottom);
				}else if("north".equals(direction)){
					result = jump("N",(Spreadsheet)comp,sheetId,sheet,type,dpWidth,dpHeight,viewWidth,viewHeight,blockLeft,blockTop,blockRight,blockBottom,fetchLeft,fetchTop,visibleLeft,visibleTop,visibleRight,visibleBottom);
				}else if("westnorth".equals(direction)){
					result = jump("WN",(Spreadsheet)comp,sheetId,sheet,type,dpWidth,dpHeight,viewWidth,viewHeight,blockLeft,blockTop,blockRight,blockBottom,fetchLeft,fetchTop,visibleLeft,visibleTop,visibleRight,visibleBottom);
				}else if("eastnorth".equals(direction)){
					result = jump("EN",(Spreadsheet)comp,sheetId,sheet,type,dpWidth,dpHeight,viewWidth,viewHeight,blockLeft,blockTop,blockRight,blockBottom,fetchLeft,fetchTop,visibleLeft,visibleTop,visibleRight,visibleBottom);
				}else if("westsouth".equals(direction)){
					result = jump("WS",(Spreadsheet)comp,sheetId,sheet,type,dpWidth,dpHeight,viewWidth,viewHeight,blockLeft,blockTop,blockRight,blockBottom,fetchLeft,fetchTop,visibleLeft,visibleTop,visibleRight,visibleBottom);
				}else if("eastsouth".equals(direction)){
					result = jump("ES",(Spreadsheet)comp,sheetId,sheet,type,dpWidth,dpHeight,viewWidth,viewHeight,blockLeft,blockTop,blockRight,blockBottom,fetchLeft,fetchTop,visibleLeft,visibleTop,visibleRight,visibleBottom);
				}else{
					throw new UiException("Unknow direction:"+direction);
				}
				responseDataBlock("Jump", token, sheetId, result);
			} else if ("neighbor".equals(type)) {
				if("east".equals(direction)){
					
					int right = blockRight + fetchWidth ;//blockRight+ 1 + fetchWidth - 1;
					right = _mergeMatrix.getRightConnectedColumn(right,	blockTop, blockBottom);
					
					//check top for new loaded east block
					int bottom = _mergeMatrix.getBottomConnectedRow(blockBottom, blockLeft, right);
					int top = _mergeMatrix.getTopConnectedRow(blockTop, blockLeft, right);
					
					if (bottom > blockBottom) {
						LoadResult result = loadSouth(sheet, type, blockLeft, blockTop, blockRight, blockBottom, bottom - blockBottom, -1, -1, -1);
						syncLoadedRect(result);
						responseDataBlock("South", "", sheetId, result.json.toJSONString());
					}
					if (top < blockTop) {
						LoadResult result = loadNorth(sheet, type, blockLeft, blockTop, blockRight, bottom, blockTop - top, -1, -1, -1);
						syncLoadedRect(result);
						responseDataBlock("North", "", sheetId, result.json.toJSONString());
					}
					int size = right - blockRight;//right - (blockRight +1) +1

					LoadResult result = loadEast(sheet, type, blockLeft, top, blockRight, bottom, size, -1, cacheRangeFetchTopHeight, cacheRangeFetchBtmHeight);
					syncLoadedRect(result);
					responseDataBlock("East", token, sheetId, result.json.toJSONString());
				} else if ("south".equals(direction)) {
					
					int bottom = blockBottom + fetchHeight;
					bottom = _mergeMatrix.getBottomConnectedRow(bottom, blockLeft, blockRight);
					
					//check right for new load south block
					int right = _mergeMatrix.getRightConnectedColumn(blockRight, blockTop, bottom);
					int left = _mergeMatrix.getLeftConnectedColumn(blockLeft, blockTop, bottom);

					if (right > blockRight) {
						LoadResult result = loadEast(sheet, type, blockLeft, blockTop, blockRight, blockBottom, right - blockRight, -1, -1, -1);
						syncLoadedRect(result);
						responseDataBlock("East", "", sheetId, result.json.toJSONString());
					}
					if (left < blockLeft) {
						LoadResult result = loadWest(sheet, type, blockLeft, blockTop, right, blockBottom, blockLeft - left, -1, -1, -1);
						syncLoadedRect(result);
						responseDataBlock("West", "", sheetId, result.json.toJSONString());
					}

					int size = bottom - blockBottom;
					
					LoadResult result = loadSouth(sheet, type, left, blockTop, right, blockBottom, size, -1, -1, -1);
					syncLoadedRect(result);
					responseDataBlock("South", token, sheetId, result.json.toJSONString());
				} else if ("west".equals(direction)) {
					
					int left = blockLeft - fetchWidth ;//blockLeft - 1 - fetchWidth + 1;
					left = _mergeMatrix.getLeftConnectedColumn(left,blockTop,blockBottom);
					//check top-bottom for new load west block
					int bottom = _mergeMatrix.getBottomConnectedRow(blockBottom, left, blockRight);
					int top = _mergeMatrix.getTopConnectedRow(blockTop, left, blockRight);
					
					if (bottom > blockBottom) {
						LoadResult result = loadSouth(sheet, type, blockLeft, blockTop, blockRight, blockBottom, bottom - blockBottom, -1, -1, -1);
						syncLoadedRect(result);
						responseDataBlock("South", "", sheetId, result.json.toJSONString());
					}
					if (top < blockTop) {
						LoadResult result = loadNorth(sheet, type, blockLeft, blockTop, blockRight, bottom, blockTop - top, -1, -1, -1);
						syncLoadedRect(result);
						responseDataBlock("North", "", sheetId, result.json.toJSONString());
					}
					int size = blockLeft - left ;//blockLeft -1 - left + 1;
					
					LoadResult result = loadWest(sheet, type, blockLeft, blockTop,	blockRight, blockBottom, size, -1, cacheRangeFetchTopHeight, cacheRangeFetchBtmHeight);
					syncLoadedRect(result);
					responseDataBlock("West", token, sheetId, result.json.toJSONString());
				} else if("north".equals(direction)) {
					
					int top = blockTop - fetchHeight;
					top = _mergeMatrix.getTopConnectedRow(top, blockLeft, blockRight);
					//check right-left for new load north block
					int right = _mergeMatrix.getRightConnectedColumn(blockRight, top, blockBottom);
					int left = _mergeMatrix.getLeftConnectedColumn(blockLeft,top, blockBottom);
					
					if (right > blockRight) {
						LoadResult result = loadEast(sheet, type, blockLeft, blockTop, blockRight, blockBottom, right - blockRight, -1, -1, -1);
						syncLoadedRect(result);
						responseDataBlock("East", "", sheetId, result.json.toJSONString());
					}
					if (left < blockLeft) {
						LoadResult result = loadWest(sheet,type,blockLeft,blockTop,right,blockBottom,blockLeft - left, -1, -1, -1);
						syncLoadedRect(result);
						responseDataBlock("West", "", sheetId, result.json.toJSONString());
					}
					int size = blockTop - top;
					LoadResult result = loadNorth(sheet, type, left, blockTop, right, blockBottom, size, -1, -1, -1);
					syncLoadedRect(result);
					responseDataBlock("North", token, sheetId, result.json.toJSONString());
				}
			} else if("visible".equals(type)) {
				loadForVisible((Spreadsheet) comp, sheetId, sheet, type, dpWidth, dpHeight, viewWidth, viewHeight, blockLeft, blockTop, blockRight, blockBottom, visibleLeft, visibleTop, visibleRight, visibleBottom, fetchWidth, fetchHeight);
				//always ack for call back
				String ack = ackResult();
				responseDataBlock(null, token,sheetId,ack);
			} else {
				//TODO use debug warning
				log.warning("unknow type:"+type);
			}
				
		} catch(Throwable x) {
			responseDataBlock("Error", "", sheetId, ackError(x.getMessage()));
			throw new UiException(x.getMessage(), x);
		}
		
		((SpreadsheetInCtrl) _ctrl).setLoadedRect(_loadedLeft < cacheLeft ? _loadedLeft : cacheLeft, _loadedTop < cacheTop ? _loadedTop : cacheTop, _loadedRight > cacheRight ? _loadedRight : cacheRight, _loadedBottom > cacheBottom ? _loadedBottom : cacheBottom);
		((SpreadsheetInCtrl) _ctrl).setVisibleRect(_lastleft, _lasttop,	_lastright, _lastbottom);
	}
	
	private void loadForVisible(Spreadsheet spreadsheet, String sheetId, SSheet sheet, String type, int dpWidth,
			int dpHeight, int viewWidth, int viewHeight, int blockLeft, int blockTop, int blockRight, int blockBottom,
			int visibleLeft, int visibleTop, int visibleRight, int visibleBottom, int cacheRangeWidth, int cacheRangeHeight) {
		
		if (visibleRight > spreadsheet.getCurrentMaxVisibleColumns() - 1) { //ZSS-1084
			visibleRight = spreadsheet.getCurrentMaxVisibleColumns() - 1; //ZSS-1084
		}
		if (visibleBottom > spreadsheet.getCurrentMaxVisibleRows() - 1) { //ZSS-1084
			visibleBottom = spreadsheet.getCurrentMaxVisibleRows() - 1; //ZSS-1084
		}
		//calculate visible range , for merge range.
		int left = Math.min(visibleLeft, blockLeft);
		int top = Math.min(visibleTop, blockTop);
		int right = Math.max(visibleRight, blockRight);
		int bottom = Math.max(visibleBottom, blockBottom);
		
		int cacheRangeRight = -1;
		int cacheRangeLeft = -1;

		boolean loadEast = right > blockRight;
		boolean loadWest = left < blockLeft;
		boolean loadSouth = bottom > blockBottom;
		boolean loadNorth = top < blockTop;
		if (loadEast) {
			
			right = _mergeMatrix.getRightConnectedColumn(right, top, bottom);

			int width = right - blockRight;
			LoadResult result = loadEast(sheet, type, blockLeft, blockTop, blockRight, blockBottom, width, cacheRangeWidth, -1, loadSouth && cacheRangeHeight > 0 ? -1 : cacheRangeHeight);
			cacheRangeRight = result.loadedRight;
			syncLoadedRect(result);
			responseDataBlock("East", "", sheetId, result.json.toJSONString());
			blockRight += width;
		}
		if (loadWest) {

			left = _mergeMatrix.getLeftConnectedColumn(left, top, bottom);
			int size = blockLeft - left;
			LoadResult result = loadWest(sheet, type, blockLeft, blockTop, right, blockBottom, size, cacheRangeWidth, -1, loadSouth && cacheRangeHeight > 0 ? -1 : cacheRangeHeight);
			cacheRangeLeft = result.loadedLeft;
			syncLoadedRect(result);
			responseDataBlock("West", "", sheetId, result.json.toJSONString());
			blockLeft -= size;
		}
		if (loadSouth) {
			
			bottom = _mergeMatrix.getBottomConnectedRow(bottom, left, right);
			int height = bottom - blockBottom;
			LoadResult result = loadSouth(sheet, type, left, blockTop, right, blockBottom, height, cacheRangeLeft, blockRight + cacheRangeWidth - 1, cacheRangeHeight);
			visibleBottom = result.loadedBottom;
			syncLoadedRect(result);
			responseDataBlock("South", "", sheetId, result.json.toJSONString());
			blockBottom += height;
		}
		if (loadNorth) {
			
			top = _mergeMatrix.getTopConnectedRow(top, left, right);
			
			int size = blockTop - top;
			LoadResult result = loadNorth(sheet, type, left, blockTop, right, bottom, size, cacheRangeLeft, blockRight + cacheRangeWidth - 1, cacheRangeHeight);
			syncLoadedRect(result);
			responseDataBlock("North", "", sheetId, result.json.toJSONString());
			blockTop -= size;
		}
	}
	
	private void syncLoadedRect(LoadResult loadResult) {
		_loadedLeft = loadResult.loadedLeft;
		_loadedRight = loadResult.loadedRight;
		_loadedTop = loadResult.loadedTop;
		_loadedBottom = loadResult.loadedBottom;
	}
	
	private String ackResult(){
		JSONObj jresult = new JSONObj();
		jresult.setData("type", "ack");
		return jresult.toString();
	}
	
	private String ackError(String message){
		JSONObj jresult = new JSONObj();
		jresult.setData("type","error");
		jresult.setData("message",message);
		return jresult.toString();
	}
	
	private String jumpResult(SSheet sheet, int left, int top, int right, int bottom) {
		top = _mergeMatrix.getTopConnectedRow(top, left, right);
		bottom = _mergeMatrix.getBottomConnectedRow(bottom, left, right);
		right = _mergeMatrix.getRightConnectedColumn(right,top,bottom);
		left = _mergeMatrix.getLeftConnectedColumn(left,top,bottom);

		int w = right - left + 1;
		int h = bottom - top + 1;
		
		//check merge range;
		JSONObject json = new JSONObject();
		
		json.put("type", "jump");
		json.put("left", left);
		json.put("top", top);
		json.put("width", w);
		json.put("height", h);
		
		SpreadsheetCtrl.Header header = SpreadsheetCtrl.Header.NONE;
		if (!_hidecolhead && !_hiderowhead) {
			header = SpreadsheetCtrl.Header.BOTH;
		} else if (!_hidecolhead) {
			header = SpreadsheetCtrl.Header.COLUMN;
		} else if (!_hiderowhead) {
			header = SpreadsheetCtrl.Header.ROW;
		}
		
		int preloadColSize = _spreadsheet.getPreloadColumnSize();
		int preloadRowSize = _spreadsheet.getPreloadRowSize();
		
		int re = top + h;
		int ce = left + w;
		
		int rangeLeft = left;
		int rangeRight = right;
		int rangeTop = top;
		int rangeBtm = bottom;
		
		if (preloadColSize > 0 && preloadRowSize > 0) {
			//extends both
			preloadColSize = preloadColSize / 2;
			preloadRowSize = preloadRowSize / 2;
			
			int newLeft = Math.max(rangeLeft - preloadColSize, 0);
			int newTop = Math.max(rangeTop - preloadRowSize, 0);
			int newRight = Math.min(rangeRight + preloadColSize, _spreadsheet.getCurrentMaxVisibleColumns() - 1); //ZSS-1084
			int newBtm = Math.min(rangeBtm + preloadRowSize, _spreadsheet.getCurrentMaxVisibleRows() - 1); //ZSS-1084
			
			rangeTop = _mergeMatrix.getTopConnectedRow(newTop, newLeft, newRight);
			rangeBtm = _mergeMatrix.getBottomConnectedRow(newBtm, newLeft, newRight);
			rangeRight = _mergeMatrix.getRightConnectedColumn(newRight, newTop, newBtm);
			rangeLeft = _mergeMatrix.getLeftConnectedColumn(newLeft, newTop, newBtm);
			
		} else if (preloadColSize > 0) {
			//extends range left and right
			int preloadSize =  preloadColSize / 2;
			int newLeft = Math.max(rangeLeft - preloadSize, 0);
			int newRight = Math.min(rangeRight + preloadSize, _spreadsheet.getCurrentMaxVisibleColumns() - 1); //ZSS-1084
			
			rangeLeft = _mergeMatrix.getLeftConnectedColumn(newLeft, rangeTop, rangeBtm);
			rangeRight = _mergeMatrix.getRightConnectedColumn(newRight, rangeTop, rangeBtm);
		} else if (preloadRowSize > 0) {
			
			int preloadSize = preloadRowSize / 2;
			int newTop = Math.max(rangeTop - preloadSize, 0);
			int newBtm = Math.min(rangeBtm + preloadSize, _spreadsheet.getCurrentMaxVisibleRows() - 1); //ZSS-1084
			
			rangeTop = _mergeMatrix.getTopConnectedRow(newTop, rangeLeft, rangeRight);
			rangeBtm = _mergeMatrix.getBottomConnectedRow(newBtm, rangeLeft, rangeRight);
		}

		
		final SpreadsheetCtrl spreadsheetCtrl = ((SpreadsheetCtrl) _spreadsheet.getExtraCtrl());
		JSONObject mainBlock = spreadsheetCtrl.getRangeAttrs(sheet, 
				header, SpreadsheetCtrl.CellAttribute.ALL, rangeLeft, rangeTop, rangeRight, rangeBtm);
		mainBlock.put("dir", "jump");
		json.put("data", mainBlock);
		
		_lastleft = left;
		_lastright = right;
		_lasttop = top;
		_lastbottom = bottom;
		
		_loadedLeft = rangeLeft;
		_loadedRight = rangeRight;
		_loadedTop = rangeTop;
		_loadedRight = rangeBtm;
		
		FreezeInfoLoader freezeInfo = spreadsheetCtrl.getFreezeInfoLoader();
		// prepare top frozen cell
		int fzr = freezeInfo.getRowFreeze(sheet);
		if (fzr > -1) {
			mainBlock.put("topFrozen", spreadsheetCtrl.getRangeAttrs(sheet, 
					header, SpreadsheetCtrl.CellAttribute.ALL, rangeLeft, 0, rangeRight, fzr));
		}

		//prepare left frozen cell
		int fzc = freezeInfo.getColumnFreeze(sheet);
		if (fzc > -1) {
			mainBlock.put("leftFrozen", spreadsheetCtrl.getRangeAttrs(sheet, 
					header, SpreadsheetCtrl.CellAttribute.ALL, 0, rangeTop, fzc, rangeBtm));
		}
		return json.toString();
	}
	
	private String jump(String dir,Spreadsheet spreadsheet,String sheetId, SSheet sheet, String type,
			int dpWidth, int dpHeight, int viewWidth, int viewHeight,
			int blockLeft, int blockTop, int blockRight, int blockBottom,
			int col, int row, 
			int rangeLeft, int rangeTop, int rangeRight,int rangeBottom) {
		
		int left;
		int right;
		int top;
		int bottom;
		
		
		if (dir.indexOf("E") >= 0) {
			right = col + 1;
			left = _colHelper.getCellIndex(_colHelper.getStartPixel(col) - viewWidth);
			// w = col - left + 2;//load more;

			if (right > spreadsheet.getCurrentMaxVisibleColumns() - 1) { //ZSS-1084
				// w = spreadsheet.getMaxcolumn()-left;
				right = spreadsheet.getCurrentMaxVisibleColumns() - 1; //ZSS-1084
			}
		} else if (dir.indexOf("W") >= 0) {
			left = col <= 0 ? 0 : col - 1;
			right = _colHelper.getCellIndex(_colHelper.getStartPixel(col)
					+ viewWidth);// end cell index

			if (right > spreadsheet.getCurrentMaxVisibleColumns() - 1) { //ZSS-1084
				// w = spreadsheet.getMaxcolumn()-left;
				right = spreadsheet.getCurrentMaxVisibleColumns() - 1; //ZSS-1084
			}
		} else {
			left = blockLeft;// rangeLeft;
			right = blockRight;// rangeRight;
		}
		
		if (dir.indexOf("S") >= 0) {
			bottom = row + 1;
			top = _rowHelper.getCellIndex(_rowHelper.getStartPixel(row)	- viewHeight);

			if (bottom > spreadsheet.getCurrentMaxVisibleRows() - 1) { //ZSS-1084
				bottom = spreadsheet.getCurrentMaxVisibleRows() - 1; //ZSS-1084
			}
		} else if (dir.indexOf("N") >= 0) {
			top = row <= 0 ? 0 : row - 1;
			bottom = _rowHelper.getCellIndex(_rowHelper.getStartPixel(row) + viewHeight);// end cell index

			if (bottom > spreadsheet.getCurrentMaxVisibleRows() - 1) { //ZSS-1084
				bottom = spreadsheet.getCurrentMaxVisibleRows() - 1; //ZSS-1084
			}
		} else {
			top = blockTop;// rangeTop;
			bottom = blockBottom;// rangeBottom;
		}
		
		return jumpResult(sheet,left,top,right,bottom);
	}
	
	private LoadResult loadEast(SSheet sheet,String type, 
			int blockLeft,int blockTop,int blockRight, int blockBottom,
			int fetchWidth, int rangeWidth, int rangeTopHeight, int rangeBtmHeight) {

		JSONObject json = new JSONObject();
		json.put("type", "neighbor");
		json.put("width", fetchWidth);
		json.put("height", blockBottom - blockTop + 1); //the range of height to generate DOM
		
		//append row
		int cs = blockRight + 1;
		int ce = cs + fetchWidth;
		json.put("top", blockTop);
		json.put("left", cs);
		
		int rangeTop = rangeTopHeight > 0 ? blockTop - rangeTopHeight + 1 : blockTop;
		int rangeRight = rangeWidth > fetchWidth ? cs + rangeWidth - 1 : ce - 1; 
		int rangeBottom = rangeBtmHeight < 0 ? blockBottom : blockBottom + rangeBtmHeight - 1;
		final SpreadsheetCtrl spreadsheetCtrl = ((SpreadsheetCtrl) _spreadsheet.getExtraCtrl());
		//ZSS-1075: always load header info no matter hide the head or not(must cached in client side)
		JSONObject mainBlock = spreadsheetCtrl.getRangeAttrs(sheet, SpreadsheetCtrl.Header.COLUMN, 
				SpreadsheetCtrl.CellAttribute.ALL, cs, rangeTop, rangeRight, rangeBottom);
		mainBlock.put("dir", "east");
		json.put("data", mainBlock);
	
		_lastleft = blockLeft;
		_lastright = ce - 1;
		_lasttop = blockTop;
		_lastbottom = blockBottom;

		//process frozen row data
		int fzr = spreadsheetCtrl.getFreezeInfoLoader().getRowFreeze(sheet);
		if (fzr > -1) {
			//ZSS-1075: always load header info no matter hide the head or not(must cached in client side)
			mainBlock.put("topFrozen", spreadsheetCtrl.getRangeAttrs(sheet, SpreadsheetCtrl.Header.BOTH, SpreadsheetCtrl.CellAttribute.ALL, 
					cs, 0, rangeRight, fzr));
		}
		return new LoadResult(cs, rangeTop, rangeRight, rangeBottom, json);
	}
	
	private LoadResult loadWest(SSheet sheet,String type,
			int blockLeft,int blockTop,int blockRight, int blockBottom,
			int fetchWidth, int rangeWidth, int rangeTopHeight, int rangeBtmHeight) {
		
		JSONObject json = new JSONObject();
		json.put("type", "neighbor");
		json.put("width", fetchWidth);// increased cell size
		json.put("height", blockBottom - blockTop + 1);// increased cell size
		
		// append row
		int cs = blockLeft - 1;
		int ce = cs - fetchWidth;
		json.put("top", blockTop);
		json.put("left", ce + 1);
		
		int rangeTop = rangeTopHeight > 0 ? blockTop - rangeTopHeight + 1 : blockTop;
		int rangeLeft = rangeWidth > fetchWidth ? blockLeft - rangeWidth - 1: ce + 1;
		if (rangeLeft < 0)
			rangeLeft = 0;
		int rangeBottom = rangeBtmHeight < 0 ? blockBottom : blockBottom + rangeBtmHeight - 1;
		final SpreadsheetCtrl spreadsheetCtrl = ((SpreadsheetCtrl) _spreadsheet.getExtraCtrl());
		//ZSS-1075: always load header info no matter hide the head or not(must cached in client side)
		JSONObject mainBlock = spreadsheetCtrl.getRangeAttrs(sheet, SpreadsheetCtrl.Header.COLUMN, 
				SpreadsheetCtrl.CellAttribute.ALL, rangeLeft, rangeTop, cs, rangeBottom);
		mainBlock.put("dir", "west");
		json.put("data", mainBlock);
		
		_lastleft = ce+1;
		_lastright = blockRight;
		_lasttop = blockTop;
		_lastbottom = blockBottom;
		
		// process frozen row data
		int fzr = spreadsheetCtrl.getFreezeInfoLoader().getRowFreeze(sheet);
		if (fzr > -1) {
			//ZSS-1075: always load header info no matter hide the head or not(must cached in client side)
			mainBlock.put("topFrozen", spreadsheetCtrl.getRangeAttrs(sheet, SpreadsheetCtrl.Header.BOTH, SpreadsheetCtrl.CellAttribute.ALL, 
					rangeLeft, 0, cs, fzr));
		}
		return new LoadResult(rangeLeft, rangeTop, cs, rangeBottom, json);
	}
	
	private LoadResult loadSouth(SSheet sheet, String type, 
			int blockLeft,int blockTop, int blockRight, int blockBottom, int fetchHeight, int rangeLeft, int cacheRight, int cacheRangeHeight) {
		
		JSONObject json = new JSONObject();
		json.put("type", "neighbor");
		json.put("width", blockRight - blockLeft + 1);
		json.put("height", fetchHeight);

		int rs = blockBottom + 1;
		int re = rs + fetchHeight;
		json.put("top", rs);
		json.put("left", blockLeft);
		
		int rangeBottom = Math.min(cacheRangeHeight > fetchHeight ? rs + cacheRangeHeight - 1 : re - 1, _spreadsheet.getCurrentMaxVisibleRows() - 1); //ZSS-1084
		rangeLeft = rangeLeft > 0 && rangeLeft < blockLeft ? rangeLeft : blockLeft;
		cacheRight = Math.max(blockRight, cacheRight);
		final SpreadsheetCtrl spreadsheetCtrl = ((SpreadsheetCtrl) _spreadsheet.getExtraCtrl());
		//ZSS-1075: always load header info no matter hide the head or not(must cached in client side)
		JSONObject mainBlock = spreadsheetCtrl.getRangeAttrs(sheet, SpreadsheetCtrl.Header.ROW, SpreadsheetCtrl.CellAttribute.ALL, 
				rangeLeft, rs, cacheRight, rangeBottom);
		mainBlock.put("dir", "south");
		json.put("data", mainBlock);

		_lastleft = blockLeft;
		_lastright = blockRight;
		_lasttop = blockTop;
		_lastbottom = re-1;
		
		// process frozen left
		int fzc = spreadsheetCtrl.getFreezeInfoLoader().getColumnFreeze(sheet);
		if (fzc > -1) {
			//ZSS-1075: always load header info no matter hide the head or not(must cached in client side)
			mainBlock.put("leftFrozen", spreadsheetCtrl.getRangeAttrs(sheet, SpreadsheetCtrl.Header.BOTH, SpreadsheetCtrl.CellAttribute.ALL,
					0, rs, fzc, rangeBottom));
		}

		return new LoadResult(rangeLeft, rs, cacheRight, rangeBottom, json);
	}
	private LoadResult loadNorth(SSheet sheet,String type, 
			int blockLeft, int blockTop, int blockRight, int blockBottom,
			int fetchHeight, int rangeLeft, int rangeRight, int cacheRangeHeight) {

		JSONObject json = new JSONObject();
		json.put("type", "neighbor");
		json.put("width", blockRight - blockLeft + 1);
		json.put("height", fetchHeight);
		
		int rs = blockTop - 1;
		int re = rs - fetchHeight;
		json.put("top", re + 1);
		json.put("left", blockLeft);
		
		int rangeTop = cacheRangeHeight > fetchHeight ? rs - cacheRangeHeight - 1 : re + 1;
		rangeLeft = rangeLeft > 0 && rangeLeft < blockLeft ? rangeLeft : blockLeft;
//		rangeRight = Math.min(Math.max(blockRight, rangeRight), _spreadsheet.getMaxcolumns() - 1);
		rangeRight = Math.max(blockRight, rangeRight);
		final SpreadsheetCtrl spreadsheetCtrl = ((SpreadsheetCtrl) _spreadsheet.getExtraCtrl());
		//ZSS-1075: always load header info no matter hide the head or not(must cached in client side)
		JSONObject mainBlock = spreadsheetCtrl.getRangeAttrs(sheet, SpreadsheetCtrl.Header.ROW, SpreadsheetCtrl.CellAttribute.ALL, 
				rangeLeft, rangeTop, rangeRight, rs);
		mainBlock.put("dir", "north");
		json.put("data", mainBlock);
		
		_lastleft = blockLeft;
		_lastright = blockRight;
		_lasttop = re + 1;
		_lastbottom = blockBottom;
		
		// process frozen left
		int frc = spreadsheetCtrl.getFreezeInfoLoader().getColumnFreeze(sheet);
		if (frc > -1) {
			//ZSS-1075: always load header info no matter hide the head or not(must cached in client side)
			mainBlock.put("leftFrozen", spreadsheetCtrl.getRangeAttrs(sheet, SpreadsheetCtrl.Header.BOTH, SpreadsheetCtrl.CellAttribute.ALL,
					0, rangeTop, frc, rs));
		}
		return new LoadResult(rangeLeft, rangeTop, rangeRight, rs, json);
	}
	
	private class LoadResult {
		int loadedTop;
		int loadedLeft;
		int loadedBottom;
		int loadedRight;
		JSONObject json;
		
		LoadResult(int left, int top, int right, int bottom, JSONObject json) {
			loadedLeft = left;
			loadedTop = top;
			loadedRight = right;
			loadedBottom = bottom;
			this.json = json;
		}
	}
}