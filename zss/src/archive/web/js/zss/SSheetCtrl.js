/* SSheetCtrl.js

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mon Apr 23, 2007 17:29:18 AM , Created by sam
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/

(function () {
	_skey = [32,106,107,109,110,111,186,187,188,189,190,191,192,220,221,222,219];
	_opearKey = [42, 43, 45, 47, 61];
	function asciiChar (charcode) {
		if ((charcode != 13 && charcode != 9 && charcode < 32) || charcode > 127) return null;
		return String.fromCharCode(charcode);
	}
	function isAsciiCharkey (keycode) {
		//48-57 number, 65-90 alpha, 
		//number pad
		//0-9 96-105
		//* 106 + 107 - 109 . 110  / 111
		//special
		//;: 186  =+ 187 ,< 188  -_ 189   .> 190  /? 191  `~ 192 
		//\| 220  } 221 '" 222 [{ 219      
		var r = ((keycode >= 48 && keycode <= 57) ||
			(keycode >= 65 && keycode <= 90) || (keycode >= 96 && keycode <= 105)),
			i = _skey.length;
		if(r) return true;
		while (i--)
			if(keycode == _skey[i]) return true;
		if (zk.opera && _opearKey.$contains(keycode))
			return true;
		//firefox fire +(61) ;(59) -(173)
		if(zk.gecko && (keycode == 61 || keycode == 59 || keycode == 173)) return true;
		
		return false;
	}
	
	function _isEvtButton (evt, flag) {
		var r = false;
		if (flag.indexOf("l") != -1)
			r |= ((evt.which) && (evt.which == 1));
		if(!r && flag.indexOf("r") != -1)
			r |= ((evt.which) && (evt.which == 3));
		if(!r && flag.indexOf("m") != -1)
			r |= ((evt.which) && (evt.which == 2));
		return r;
	}
	
	function _isLeftMouseEvt (evt) {
		return (evt.which) && (evt.which == 1);
	}
	
	function _isMiddleMouseEvt (evt) {
		return (evt.which) && (evt.which == 2);
	}
	
	function _isRightMouseEvt (evt) {
		return (evt.which) && (evt.which == 3);
	}
	
	function syncAttributes(dst, src, fields) {
		for (var key in fields) {
			var f = fields[key];
			dst[f] = src[f];
		}
	}
	
	/**
	 * 1. show active block
	 * 2. cells may need to process wrap height
	 * 3. show focus/selection etc.
	 */
	function doAfterCSSReady(sheet) {
		var wgt = sheet._wgt;
		if (wgt.isSheetCSSReady()) {
			sheet.activeBlock.setVisible(true); //show cells
			sheet._doSSInitLater(); //may show focus, process wrap height
			if (!sheet.isSwitchingSheet) {
				wgt.focus();
			} else {
				sheet.inlineEditor.updateInfo();
				sheet.isSwitchingSheet = null;
			}
		} else {
			setTimeout(function () {
				doAfterCSSReady(sheet);
			}, 1);
		}
	}
	
	function newPositionArray(str) {
		var array = [];
		if (str) {
			str = str.split(",");
			var size  = str.length;
			for (var i = 0; i < size; i = i + 5)
				array.push([zk.parseInt(str[i]), zk.parseInt(str[i + 1]), zk.parseInt(str[i + 2]), 'true' == str[i+3], 'true' == str[i+4]]);
		}
		return array;
	}
	
	function newMergeMatrixArray(str) {
		var array = [];
		if (str && str != "") {
			str = str.split(";");
			var size = str.length,
				r;
			for (var i = 0; i < size; i++) {
				r = str[i].split(",");
				var range = new zss.Range(zk.parseInt(r[0]), zk.parseInt(r[1]), zk.parseInt(r[2]), zk.parseInt(r[3]));
				range.id = zk.parseInt(r[4]);
				array.push(range);
			}
		}
		return array;
	}
	
	
	function toHeaderTitleArray(headers) {
		var ts = [];
		for (var i = 0, len = headers.length; i < len; i++) {
			ts[i] = headers[i].t;
		}
		return ts;
	}

	function groupContains(group, index) {
		if (!group || group.length == 0)
			return false;
		return searchGroup(group, index, 0, group.length - 1);
	}

	// use binary search for better performance
	function searchGroup(group, key, min, max) {
		if (max < min)
			return false;
		var mid = Math.floor((min + max) / 2);
		if (group[mid].start > key) {
			return searchGroup(group, key, min, mid - 1);
		} else if (group[mid].end < key) {
			return searchGroup(group, key, mid + 1, max);
		} else {
			return true;
		}
	}

/**
 *  SSheetCtrl controls spreadsheet
 *  
 *  Sheet events
 *  <ul>
 *  	<li>onContentsChanged</li>
 *  	<li>onFocused: fire when sheet has focus (cell, widget etc...)</li>
 *  	<li>onCellSelection</li>
 *  	<li>onStartEditing</li>
 *  	<li>onStopEditing</li>
 *  </ul>
 *  
 */
zss.SSheetCtrl = zk.$extends(zk.Widget, {	
	$o: zk.$void, //no need to invoke _addIdSpaceDown, no fellows relationship
	/**
	 * Editing formula info
	 * 
	 * <ul>
	 * 	<li>start: the start index position in formula string, for insert cell address reference</li>
	 * 	<li>end: the end index position in formula string, for insert cell address reference</li>
	 * 	<li>type: either 'inlineEditiong' or 'formulabarEditing'</li>
	 *  <li>moveCell: boolean indicate whether key event shall move cell or not</li>
	 * </ul>
	 */
	editingFormulaInfo: null,
	// ZSS-514: change default line hight to normal
	lineHeight: 'normal', 
	$init: function (wgt) {
		this.$supers(zss.SSheetCtrl, '$init', []);
		this._wgt = wgt;
		this.setHflex(true);
		this.setVflex(true);
		this.pageKeySize = 100;
		this._initiated = false;
		
		this._clienttxt = '';
		
		//init function later queue, the function in this queue will be invoke in ZK InitialLater
		//I create this queue because ZK initial later doesn't support parameter.
		this._initLaterQ = [];//after init function queue
		this._initLaterQ.urgent = 0;//after init function queue
		
		this.state = zss.SSheetCtrl.NOFOCUS;
		
		//initial default size;
		this.topHeightDt = this.leftWidthDt = this.rowHeightDt = 
			this.colWidthDt = this.lineHeightDt = this.cellPadDt =  false;

		this.config = new zss.Configuration();
	},
	doSheetSelected: function (visRng) {
		if (this.bindLevel < 0) {//this method shall invoke after bind
			return;
		}
		var	wgt = this._wgt,
			sheet = this,
			cacheCtrl = wgt._cacheCtrl,
			dp = this.dp,
			sp = this.sp,
			tp = this.tp,
			lp = this.lp,
			snapshot = cacheCtrl.getSnapshot(wgt.getSheetId());
		if (snapshot) {
			syncAttributes(wgt, snapshot, 
				['_displayGridlines', '_rowFreeze', '_columnFreeze', '_rowHeight', '_columnWidth', '_protect']);
			var d = snapshot.getDataPanelSize(),
				s = snapshot.getScrollPanelPos();
			
			dp.reset(d.width, d.height);
			sp.reset(s.scrollTop, s.scrollLeft);
			lp._updateTopPos(snapshot.getLeftPanelPos());
			tp._updateLeftPos(snapshot.getTopPanelPos());
		} else { //switch to new sheet focus on [0, 0]
			sp.reset(0, 0);
			lp._updateTopPos(0);
			tp._updateLeftPos(0);
		}
		
		this.serverSheetId = wgt.getSheetId();
		this.topHeight = wgt.getTopPanelHeight(); //default top panel height 20
		this.leftWidth = wgt.getLeftPanelWidth(); //default left panel width 28
		this.rowHeight = wgt.getRowHeight(); //default row height 20
		this.colWidth = wgt.getColumnWidth(); //default column width 80
		this.frozenRow = wgt.getRowFreeze();
		this.frozenCol = wgt.getColumnFreeze();

		this.custColWidth = new zss.PositionHelper(this.colWidth, snapshot ? snapshot.getCustColWidth() : newPositionArray(wgt.getCsc()));
		this.custColWidth.ids = new zss.Id(snapshot?snapshot.getCustColLastId():0, 2);
		
		this.custRowHeight = new zss.PositionHelper(this.rowHeight, snapshot ? snapshot.getCustRowHeight() : newPositionArray(wgt.getCsr()));
		this.custRowHeight.ids = new zss.Id(snapshot?snapshot.getCustRowLastId():0, 2);
		
		//merge range
		this.mergeMatrix = new zss.MergeMatrix(snapshot ? snapshot.getMergeMatrix() : newMergeMatrixArray(wgt.getMergeRange()), this);

		var data = wgt._cacheCtrl.getSelectedSheet(),
			sheetCSSReady = wgt.isSheetCSSReady();
		visRng = visRng || zss.SSheetCtrl._getVisibleRange(this);
		if (data) {			
			var oldBlock = this.activeBlock,
				oldTopPanel = this.tp,
				oldLeftPanel = this.lp,
				oldCornerPanel = this.cp,
				rows = data.rows,
				rect = data.rect,
				tRow = visRng.top,
				bRow = visRng.bottom,
				lCol = visRng.left,
				rCol = visRng.right,
				rowHeadHidden = wgt._rowHeadHidden,
				colHeadHidden = wgt._columnHeadHidden,
				tRow0 = tRow > 0 ? this.mergeMatrix.getTopRow(tRow) : 0, //ZSS-927
				lCol0 = lCol > 0 ? this.mergeMatrix.getLeftCol(lCol) : 0; //ZSS-927
			if (bRow > rect.bottom)
				bRow = rect.bottom;
			if (rCol > rect.right)
				rCol = rect.right;
			// ZSS-392: every panel use it's owned ActiveRange data
			var activeBlock = new zss.MainBlockCtrl(sheet, tRow0, lCol0, bRow, rCol, data); //ZSS-927
			var topPanel = new zss.TopPanel(sheet, rowHeadHidden, lCol0, rCol, (data.topFrozen ? data.topFrozen : data)); //ZSS-927
			var leftPanel = new zss.LeftPanel(sheet, colHeadHidden, tRow0, bRow, (data.leftFrozen ? data.leftFrozen : data)); //ZSS-927

			if (!sheetCSSReady) {//set visible after CSS loaded
				activeBlock.setVisible(false);
			}
			if (oldTopPanel) {
				oldTopPanel.replaceWidget(this.tp = topPanel);
			} else {
				this.appendChild(this.tp = topPanel, true);
			}
			
			if (oldLeftPanel) {
				oldLeftPanel.replaceWidget(this.lp = leftPanel);
			} else {
				this.appendChild(this.lp = leftPanel, true);
			}
			
			var cornerPanel = new zss.CornerPanel(sheet, rowHeadHidden, colHeadHidden, lCol, tRow, rCol, bRow, data); // corner needs other two freeze panels' data for header
			
			if (oldCornerPanel) {
				oldCornerPanel.replaceWidget(this.cp = cornerPanel);
			} else {
				this.appendChild(this.cp = cornerPanel, true);
			}
			if (oldBlock) {
				//Note. do not use MainBlockCtrl.replaceWidget (for row/column freeze)
				activeBlock.replaceHTML(oldBlock.$n(), this.desktop, null, true);
				this.activeBlock = activeBlock;
			} else {
				this.appendChild(this.activeBlock = activeBlock, true);
			}

			this.fireProtectSheet(wgt.isProtect());
			this.fireDisplayGridlines(wgt.isDisplayGridlines());
			
			dp._fixSize(this.activeBlock);
			this._fixSize();
		}
		if (!sheetCSSReady) {
			this.addSSInitLater(function () {
				sheet._resize();
			});
		} else {
			this._resize();
		}
		doAfterCSSReady(this);
	},
	//ZSS-1087: restore panel postion 
	restorePos_: function (snapshot) {
		if (!snapshot) return; //ZSS-1095
		if (this.bindLevel < 0) {//this method shall invoke after bind
			return;
		}
		var	wgt = this._wgt,
			sheet = this,
			cacheCtrl = wgt._cacheCtrl,
			dp = this.dp,
			sp = this.sp,
			tp = this.tp,
			lp = this.lp;
//		syncAttributes(wgt, snapshot, 
//			['_displayGridlines', '_rowFreeze', '_columnFreeze', '_rowHeight', '_columnWidth', '_protect']);
		var d = snapshot.getDataPanelSize(),
			s = snapshot.getScrollPanelPos();
		
		dp.reset(d.width, d.height);
		sp.reset(s.scrollTop, s.scrollLeft);
		lp._updateTopPos(snapshot.getLeftPanelPos());
		tp._updateLeftPos(snapshot.getTopPanelPos());
	},
	afterParentChanged_: function () { //all attributes set when afterParentChanged_
		var self = this,
			wgt = this._wgt;
		
		this.sheetid = wgt.uuid;
		
		//current server sheet index
		this.serverSheetId = wgt.getSheetId();
		
		var rowHeight = wgt.getRowHeight(),
			colWidth = wgt.getColumnWidth();
	
		this.maxCols = wgt.getMaxColumns();
		this.maxRows = wgt.getMaxRows();
		this.topHeight = wgt.getTopPanelHeight(); //default top panel height 20
		this.leftWidth = wgt.getLeftPanelWidth(); //default left panel width 28
		this.cellPad = wgt.getCellPadding();
		this.rowHeight = wgt.getRowHeight(); //default row height 20
		this.colWidth = wgt.getColumnWidth(); //default column width 80
		
		//initial time parameter
		var initparm = this.initparm = {},
			fs = wgt.getFocusRect();
		fs = fs.split(",");
		initparm.focus = new zss.Pos(zk.parseInt(fs[1]), zk.parseInt(fs[0]));//[row,col]
		
		var sel = wgt.getSelectionRect();
		sel = sel.split(",");
		initparm.selrange = new zss.Range(zk.parseInt(sel[0]), zk.parseInt(sel[1]),zk.parseInt(sel[2]),zk.parseInt(sel[3]));
		
		var hl = wgt.getHighLightRect();
		if (hl) {
			hl = hl.split(",");
			initparm.hlrange = new zss.Range(zk.parseInt(hl[0]), zk.parseInt(hl[1]), zk.parseInt(hl[2]), zk.parseInt(hl[3]));
			
			this.addSSInitLater(function() {
				var range = self.initparm.hlrange;
				self.hlArea.show = true;
				self.moveHighlight(range.left, range.top, range.right, range.bottom);
			});
			
		} else {
			initparm.hlrange = new zss.Range(-1, -1, -1, -1);
		}

		this.custColWidth = new zss.PositionHelper(this.colWidth, newPositionArray(wgt.getCsc()));
		this.custColWidth.ids = new zss.Id(0, 2);
		
		this.custRowHeight = new zss.PositionHelper(this.rowHeight, newPositionArray(wgt.getCsr()));
		this.custRowHeight.ids = new zss.Id(0, 2);
		
		this.mergeMatrix = new zss.MergeMatrix(newMergeMatrixArray(wgt.getMergeRange()), this);
		
		//frozen row & column
		this.frozenRow = wgt.getRowFreeze();
		this.frozenCol = wgt.getColumnFreeze();
		
		var sheet = this,
			cacheCtrl = wgt._cacheCtrl,
			ar = cacheCtrl.getSelectedSheet();
		
		this.appendChild(this.inlineEditor = new zss.Editbox(sheet));
		if (ar) {
			var	rows = ar.rows,
				rect = ar.rect,
				tRow = rect.top,
				lCol = rect.left,
				rCol = rect.right,
				rowHeadHidden = wgt._rowHeadHidden,
				colHeadHidden = wgt._columnHeadHidden;
			//TODO: measure best init size
			bRow = tRow + 20; //load row size
			rCol = lCol + 20; //load column size
			if (bRow > rect.bottom)
				bRow = rect.bottom;
			if (rCol > rect.right)
				rCol = rect.right;
			
			// ZSS-392: every panel use it's owned ActiveRange data
			this.appendChild(this.activeBlock = new zss.MainBlockCtrl(sheet, tRow, lCol, bRow, rCol, ar), true);
			this.appendChild(this.tp = new zss.TopPanel(sheet, rowHeadHidden, lCol, rCol, (ar.topFrozen ? ar.topFrozen : ar)), true);
			this.appendChild(this.lp = new zss.LeftPanel(sheet, colHeadHidden, tRow, bRow, (ar.leftFrozen ? ar.leftFrozen : ar)), true);
			this.appendChild(this.cp = new zss.CornerPanel(sheet, rowHeadHidden, colHeadHidden, lCol, tRow, rCol, bRow, ar), true); // corner needs other two freeze panels' data for header
		}
		
		this.innerClicking = 0;// mouse down counter to check that is focus rellay lost.
	},
	setFlexSize_: function(sz, isFlexMin) {
		var r = this.$supers(zss.SSheetCtrl, 'setFlexSize_', arguments);
		if (!this._initiated) {
			zss.Spreadsheet.initLaterAfterCssReady(this);
		}
		this._resize();
		return r;
	},
	//ZSS-884
	isWatchable_: function (name, p, cache) {
		if ("onSize" == name)
			return !!this.$n();
		else
			return this.$supers(zss.SSheetCtrl, 'isWatchable_', arguments);
	},
	bind_: function (desktop, skipper, after) {
		this.$supers(zss.SSheetCtrl, 'bind_', arguments);
		//ZSS 125: already process wrap on row.bind_
		delete this._wrapRange;
		
		zss.SSheetCtrl._initInnerComp(this, this._wgt._autoFilter ? this._wgt._autoFilter.range.top : null);
		this.listen({onContentsChanged: this, onRowHeightChanged: this});
		
		var n = this.comp = this.$n();
		n.ctrl = this;
	},
	unbind_: function () { 
		this.unlisten({onContentsChanged: this, onRowHeightChanged: this});
		this.animateHighlight(false);
		this.invalid = true;

		if(this.comp) this.comp.ctrl = null;
		this.comp = this.busycmp = this.maskcmp = this.spcmp = this.topcmp = 
		this.leftcmp = this.sinfocmp = this.infocmp = this.focusmarkcmp =
		this.selareacmp = this.selchgcmp = this.hlcmp = this.wpcmp = null;
		
		if (this.dragging) {
			this.dragging.cleanup();
			this.dragging = null;
		}
		
		this.sp.cleanup();
		this.dp.cleanup();
		
		this.sinfo.cleanup();
		this.info.cleanup();
		this.focusMark.cleanup();
		this.selArea.cleanup();
		this.selChgArea.cleanup();
		this.hlArea.cleanup();
		this.sp = this.dp = this.tp = this.lp = this.cp = this.sinfo = this.info = 
		this.inlineEditor = this.focusMark = this.selArea = this.selChgArea = this.hlArea = null;
		
		this.custTHSize = this.custLHSize = this._initLaterQ =
		this._lastmdelm = this._lastmdstr = null;
		
		this.$supers(zss.SSheetCtrl, 'unbind_', arguments);
	},
	//ZSS-944
	/**
	 * Sets the rotate 90deg column
	 * 
	 * @param int row the row index
	 * @param int col the column index
	 * @param boolean run indicate whether to process rotate now or not
	 */
	triggerRotateColumn_: function (row, col, run) {
		var r = this._rotateRange;
		if (!r)
			r = this._rotateRange = {}; //tRow, bRow, and col attributes
		var rCol = r.col;
		rCol ? r.col = Math.max(rCol, col) : r.col = col;
		if (row) {
			var	tRow = r.tRow,
				bRow = r.bRow;
			tRow ? r.tRow = Math.min(tRow, row) : r.tRow = row;
			bRow ? r.bRow = Math.max(bRow, row) : r.bRow = row;
		}
		if (run) {
			this.fireProcessRotate_();
		}
	},
	//ZSS-944
	/**
	 * Fire process cell 90deg rotate event
	 */
	fireProcessRotate_: function () {
		var r = this._rotateRange;
		if (r != undefined) {
			this.fire('onProcessRotate', {col: r.col, tRow: r.tRow, bRow: r.bRow});
			delete this._rotateRange;
		}
	},
	/**
	 * Sets the overflow column, columns before need to process overflow
	 * 
	 * @param int row the row index
	 * @param int col the column index
	 * @param boolean run indicate whether to process overflow now or not
	 */
	triggerOverflowColumn_: function (row, col, run) {
		var r = this._overflowRange;
		if (!r)
			r = this._overflowRange = {}; //tRow, bRow, and col attributes
		var rCol = r.col;
		rCol ? r.col = Math.max(rCol, col) : r.col = col;
		if (row) {
			var	tRow = r.tRow,
				bRow = r.bRow;
			tRow ? r.tRow = Math.min(tRow, row) : r.tRow = row;
			bRow ? r.bRow = Math.max(bRow, row) : r.bRow = row;
		}
		if (run) {
			this.fireProcessOverflow_();
		}
	},
	triggerWrap: function (row, run) {
		var r = this._wrapRange = this._wrapRange || {};
		typeof r.tRow == 'undefined' ? r.tRow = row : r.tRow = Math.min(r.tRow, row);
		typeof r.bRow == 'undefined' ? r.bRow = row : r.bRow = Math.max(r.bRow, row);
		if (run) {
			this.fireProcessWrap_();
		}
	},
	/**
	 * Fire process cell overflow event
	 */
	fireProcessOverflow_: function () {
		var r = this._overflowRange;
		if (r != undefined) {
			this.fire('onProcessOverflow', {col: r.col, tRow: r.tRow, bRow: r.bRow});
			delete this._overflowRange;
		}
	},
	fireProcessWrap_: function () {
		if (this._wgt.isSheetCSSReady()){
			var r = this._wrapRange;
			if (r != undefined) {
				this.fire('onProcessWrap', {tRow: r.tRow, bRow: r.bRow});
				delete this._wrapRange;
			}
		}else{
			var self = this; 
			setTimeout(function(){
				self.fireProcessWrap_();
				},25);
		}
	},
	fireProcessAutoHeight_: function() {
		// not implement in OSE
	},
	//TODO: change to fire 'onSelectedSheet' evt
	fireProtectSheet: function (protect) {
		this.fire('onProtectSheet', {protect: protect});
	},
	fireDisplayGridlines: function (show) {
		this.fire('onDisplayGridlines', {show: show});
	},
	triggerSelection: function (tRow, lCol, bRow, rCol) {
		var r = this._selectionRange;
		if (!r) {
			r = this._selectionRange = {};//tRow, lCol, bRow, rCol
		}
		
		var top = r.tRow,
			left = r.lCol,
			btm = r.bRow,
			right = r.rCol;
		top ? r.tRow = Math.min(tRow, top) : r.tRow = tRow;
		left ? r.lCol = Math.min(lCol, left) : r.lCol = lCol;
		btm ? r.bRow = Math.max(bRow, btm) : r.bRow = bRow;
		right ? r.rCol = Math.max(rCol, right) : r.rCol = rCol;
	},
	onContentsChanged: function (evt) {
		this.fireProcessOverflow_();
		this.fireProcessAutoHeight_();
		
		var r = this._selectionRange;
		if (r) {
			this.deferFireCellSelection(r.lCol, r.tRow, r.rCol, r.bRow);
			this._selectionRange = null;
		}
	},
	onRowHeightChanged: function () {
		var h = this.hlArea;
		if (h.show) {
			h.relocate();
		}
	},
	_addEditorFocus : function(id, name){
		var x = this.focusmarkcmp,
			div = x.cloneNode(true);
		
		div.id = div.id + '_' + id;
		div.style.borderWidth = "3px";
		x.parentNode.appendChild(div);
		if(!this.editorFocusMark)
			this.editorFocusMark = new Object();
		this.editorFocusMark[id] = new zss.FocusMarkCtrl(this, div, new zss.Pos(0, 0));

		//ZSS-1043
		if (this.tp) {
			this.tp.addEditorFocus(id, name);
		}
		if (this.lp) {
			this.lp.addEditorFocus(id, name);
		}
		if (this.cp) {
			this.cp.addEditorFocus(id, name);
		}
	},
	removeEditorFocus : function(id){
		if (!this.editorFocusMark)
			return;
		var ctrl = this.editorFocusMark[id];
		if (ctrl) {
			ctrl.comp.parentNode.removeChild(ctrl.comp);
			ctrl.cleanup();
		}
		this.editorFocusMark[id] = null;

		//ZSS-1043
		if (this.tp) {
			this.tp.removeEditorFocus(id);
		}
		if (this.lp) {
			this.lp.removeEditorFocus(id);
		}
		if (this.cp) {
			this.cp.removeEditorFocus(id);
		}
	},
	moveEditorFocus : function(id, name, color, row, col){
		if(!this.editorFocusMark || !this.editorFocusMark[id]){
			this._addEditorFocus(id, name);
		}
		this.editorFocusMark[id].relocate(row, col);
		this.editorFocusMark[id].showMark(color, name); //new color/oldcolor with label
		
		//ZSS-1043
		if (this.tp) {
			this.tp.moveEditorFocus(id, name, color, row, col);
		}
		if (this.lp) {
			this.lp.moveEditorFocus(id, name, color, row, col);
		}
		if (this.cp) {
			this.cp.moveEditorFocus(id, name, color, row, col);
		}
	},
	_resize: function () {
		if (this.invalid) return;

		this._fixSize();
		if (!this.activeBlock.loadForVisible()) //bug#303:avoid hide to visible and the mask is still there
			this.showMask(false);
	},
	/**
	 * Returns whether is dragging or not
	 * @return boolean
	 */
	isDragging: function () {
		return this.dragging ? true : false;
	},
	/**
	 * Returns whether is stop dragging or not
	 * @return boolean
	 */
	stopDragging: function () {
		if (this.dragging) {
			this.dragging.cleanup();
			this.dragging = null;
		}
	},
	/**
	 * Sets dragging status
	 * @param boolean dragging
	 */
	setDragging: function (dragging) {
//		this.stopDragging(); //ZSS-1048: Should call stopDragging() by caller
		this.dragging = dragging;
	},
	/**
	 * Returns whether is in asynchronous state or not
	 * @return boolean
	 */
	isAsync: function(){
		return (this.state&1 == 1);
	},
	/**
	 * Add init function to queue
	 * @param fn
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	addSSInitLater: function (fn, arg0, arg1, arg2, arg3) {
		this._initLaterQ.push([fn, arg0, arg1, arg2, arg3]);
	},
	/**
	 * Insert init function to the queue
	 * @param fn
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	insertSSInitLater: function(fn, arg0, arg1, arg2, arg3){
		this._initLaterQ.unshift([fn,arg0,arg1,arg2,arg3]);
		this._initLaterQ.urgent ++;
	},
	_doSSInitLater: function () {
		if(this.invalid) return;
		var local = this,
			queu = local._initLaterQ;

		if (queu.length == 0) return;
		var urgent = queu.urgent,
			parm,
			count = 0;
		while ((parm = queu.shift())) {
			parm[0](parm[1], parm[2], parm[3], parm[4]);//fn, arg0,arg1,arg2
			if(count > urgent &&  count >= 25 ){//break a while.
				setTimeout(function(){
					local._doSSInitLater();
				}, 1);
				break;
			}
			count++;
		}
		queu.urgent = 0;
	},
	_cmdCellUpdate: function (shtId, result) {
		var type = result.type,
			row = result.r, // be careful r does not always mean "row" in certain update type.
			col = result.c, // for example, in type udcell, r means "right" and no c attribute. 
			value = result.val,
			server = result.server; //use editValue from server
		switch(type){
		case "udtext" :
			this._updateText(result);
			break;
		case "udcell":
			var wgt = this._wgt,
				data = result,
				cCtl = this._wgt._cacheCtrl,
				cacheSheet = cCtl.getSheetBy(shtId),
				selSheet = cCtl.getSelectedSheet(),
				ls = this.getLastSelection();
			if (cacheSheet) {
				cacheSheet.update(data); // update cell model 
				if (cacheSheet.id == selSheet.id) {//update current sheet
					// ZSS-392: update every panel separately 
					this.update_(data); // update cell DOM
					wgt._triggerContentsChanged = true;
				}
			}
			if (ls) {
				// ZSS-579: fire onCellUpdate to notify toolbar button to change status
				this.fire('onCellUpdate', ls);
			}
			break;
		case "startedit":
			var editType = result.et,
				dp = this.dp;
			//ZSS 171
			this._skipInsertCellRef = true;
			if ('inlineEditing' == editType) {
				if (!dp._moveFocus(row, col, true, true)) {
					//TODO, if cell not initial, i should skip or put to delay batch? 
					break;
				}
			}
			//ZSS 171
			this._skipInsertCellRef = false;
			dp._startEditing(value, server, editType);
			break;
		case "stopedit":
			this.dp._stopEditing();
			break;
		case "canceledit":
			this.dp.cancelEditing(result.et);
			break;
		case "retryedit":
			this.dp.retryEditing(value, result.r, result.c);
			break;
		}
	},
	_cmdBlockUpdate: function (type, dir, tRow, lCol, bRow, rCol) {
		switch (type) {
		case 'neighbor': //move to a neighbor block
			this.activeBlock.loadForVisible();
			if (zk.ie && zk.ie < 11) {
				//TODO: test if set display none could speedup or not when switch cache  
				//ie have some display error(cell overlap) when scroll up(neighbor north)
				//same issue when scroll right
				var dp = this.dp.comp,
					l = this.lp.comp;
				jq(dp).css('display', 'none');//for speed up
				jq(l).css('display', 'none');//for speed up
				
				zk(dp).redoCSS();
				zk(l).redoCSS();
				
				jq(dp).css('display', '');
				jq(l).css('display', '');
			}
			break;
		case 'jump'://jump to another bolck, not a neighbor
			var oldBlock = this.activeBlock,
				wgt = this._wgt,
				data = wgt._cacheCtrl.getSelectedSheet();
			oldBlock.replaceWidget(this.activeBlock = new zss.MainBlockCtrl(this, tRow, lCol, bRow, rCol, data));
			this.dp._fixSize(this.activeBlock);
			this.activeBlock.loadForVisible();
			break;
		case 'error': //fetch cell with exception
			break;
		}
		this.showMask(false);
		
		//bug 1951423 IE : row is broken when scroll down, st time to do ss initiallater
		var self = this;
		setTimeout(function(){
			self._doSSInitLater();//after creating cell need to invoke init later
		}, 0);
	},
	_cmdInsertRC: function (result) {

		// ZSS-404: should update freeze size in spreadsheet widget
		if('colfreeze' in result) {
			this._wgt.setColumnFreeze(result.colfreeze);
		}
		if('rowfreeze' in result) {
			this._wgt.setRowFreeze(result.rowfreeze);
		}
		
		if (result.type == "column") {
			var col = result.col;
			var size = result.size;
			var headers = result.hs;
			var ar = this._wgt._cacheCtrl.getSelectedSheet();
			ar.insertNewColumn(col, size, headers);	// update cache
			this._insertNewColumn(col, size, toHeaderTitleArray(headers.hs));	// update DOM
			this._removeColumn(this.maxCols, size); // ZSS-450: remove excess columns (DOM) 

			//update positionHelper
			this.custColWidth.shiftMeta(col, size);
			
			// adjust data panel size;
			this.dp.updateWidth(this.colWidth * size);
			
			//update maxCol
			this.maxCols = result.maxcol;

			// fix freeze panels' sizes
			var fzc = this.frozenCol = result.colfreeze;
			if (fzc > -1) {
				this.lp._fixSize();
				this.cp._fixSize();
			}
			
			var block = this.activeBlock;
			if (col < block.range.left)// insert before current block, then jump
				block.reloadBlock("east");
			else
				this.triggerOverflowColumn_(null, col);
			
		} else if (result.type == "row") {	//jump to another bolck, not a neighbor
			var row = result.row;
			var size = result.size;
			var headers = result.hs;
			var ar = this._wgt._cacheCtrl.getSelectedSheet();
			ar.insertNewRow(row, size, headers);	// update cache
			this._insertNewRow(row, size, toHeaderTitleArray(headers.hs)); // update DOM
			this._removeRow(this.maxRows, size); // ZSS-450: remove excess rows (DOM) 
			
			//update positionHelper
			this.custRowHeight.shiftMeta(row, size);
			
			// adjust datapanel size;
			this.dp.updateHeight(this.rowHeight * size);
			
			//update maxRow
			this.maxRows = result.maxrow;

			// fix freeze panels' sizes
			var fzr = this.frozenRow = result.rowfreeze;;
			if (fzr > -1) {
				this.tp._fixSize();
				this.cp._fixSize();
			}
			
			var block = this.activeBlock;
			if (row < block.range.top)// insert before current block, then jump
				block.reloadBlock("south");
		}
		
		this.dp._fixSize(this.activeBlock);
		this._fixSize();
		this.sendSyncblock();
		
		var sel = this.getLastSelection();
		if (sel)
			this.moveCellSelection(sel.left, sel.top, sel.right, sel.bottom);
		
		var self = this;
		setTimeout(function () {
			var h = self.hlArea;
			if (h.show) {
				h.relocate();
			}
			self._doSSInitLater();//after creating cell need to invoke init later
		},0);
	},
	_cmdRemoveRC: function (result, shfitsize) {

		// ZSS-404: should update freeze size in spreadsheet widget
		if('colfreeze' in result) {
			this._wgt.setColumnFreeze(result.colfreeze);
		}
		if('rowfreeze' in result) {
			this._wgt.setRowFreeze(result.rowfreeze);
		}
		
		var lfv = true;
		if (result.type == "column") {
			var col = result.col,
				size = result.size,
				headers = result.hs,
				ar = this._wgt._cacheCtrl.getSelectedSheet();
			ar.removeColumns(col, size, headers);
			this._removeColumn(col, size, toHeaderTitleArray(headers.hs));
			
			// adjust datapanel size;
			var dp = this.dp,
				w = this.custColWidth.getStartPixel(col);
			w = this.custColWidth.getStartPixel(col + size) - w;
			dp.updateWidth(-w);
			
			//update positionHelper
			if(shfitsize) this.custColWidth.unshiftMeta(col,size);
			
			//update maxCol
			this.maxCols = result.maxcol;

			//fix frozenCol size
			this.frozenCol = result.colfreeze;
			this.lp._fixSize();
			this.cp._fixSize();
			
			var block = this.activeBlock;
			if (col < block.range.left) {// insert before current block, then jump
				block.reloadBlock("east");
				lfv = false;
			} else
				this.triggerOverflowColumn_(null, col);// no need to invoke 
			
			this._syncColFocusAndSelection(col);
		} else if (result.type == "row") {//jump to another bolck, not a neighbor
			var row = result.row,
				size = result.size,
				headers = result.hs,
				ar = this._wgt._cacheCtrl.getSelectedSheet();
			ar.removeRows(row, size, headers);
			this._removeRow(row, size, toHeaderTitleArray(headers.hs));
			
			// adjust datapanel size;
			var dp = this.dp,
				h = this.custRowHeight.getStartPixel(row);
			h = this.custRowHeight.getStartPixel(row + size) - h;
			dp.updateHeight(-h);
			
			//update positionHelper
			if(shfitsize) this.custRowHeight.unshiftMeta(row,size);
			
			//update maxRow
			this.maxRows = result.maxrow;

			//fix frozen size
			this.frozenRow = result.rowfreeze;
			this.tp._fixSize();
			this.cp._fixSize();
			
			var block = this.activeBlock;
			if (row < block.range.top) {// insert before current block, then jump
				block.reloadBlock("south");
				lfv = false;
			}
			this._syncRowFocusAndSelection(row);
		}
		
		dp._fixSize(this.activeBlock);
		this._fixSize();		
		this.sendSyncblock();
		
		if(lfv) this.activeBlock.loadForVisible();
		
		var pos = this.getLastFocus(),
			update;
		if (pos.row >= this.maxRows) {
			pos.row = this.maxRows - 1;
			update = true;
		}
		if (pos.column >= this.maxCols) {
			pos.column = this.maxCols - 1;
			update = true;
		}
		
		if(update) dp.moveFocus(pos.row, pos.column, true, true);
		
		var self = this;
		setTimeout(function () {
			var h = self.hlArea;
			if (h.show) {
				h.relocate();
			}
			self._doSSInitLater();//after creating cell need to invoke init later
		}, 0);
	},
	_cmdMaxcolumn: function (maxcols) { // ZSS-242, modify range update function
		if(maxcols == this.maxCols) { // early return
			return;
		}

		var r, c, fpos, spos;
		var delta = maxcols - this.maxCols;
		var isIncreased = delta > 0;
		
		// update data panel's width
		var w = this.custColWidth.getStartPixel(this.maxCols);
		w = this.custColWidth.getStartPixel(maxcols) - w;
		var dp = this.dp;
		dp.updateWidth(w);
		
		// fix all panel's size
		this.maxCols = maxcols; //update max columns first
		dp._fixSize(this.activeBlock);
		this._fixSize(); // this will fix every panel's size
		
		if(isIncreased) { // increase columns
			this.activeBlock.loadForVisible();
		} else { // reduce columns
			
			// update focus and selection
			fpos = this.getLastFocus(); // {row, column}
			if(fpos && (fpos.column > maxcols - 1)) {
				this.moveCellFocus(fpos.row, maxcols - 1);	// this will send AU
			}
			spos = this.getLastSelection(); // {left, top, right, bottom}
			if(spos && (spos.right > maxcols - 1)) { // intersected or out of bound, make selection to focus
				this.moveCellSelection(maxcols - 1, fpos.row, maxcols - 1, fpos.row, true);
				// reload and send AU
				spos = this.getLastSelection();
				if (this.selType) { //ZSS-1100: fire only if not change sheet(selType would be undefined when chagne sheet)
					this._sendOnCellSelection(this.selType, spos.left, spos.top, spos.right, spos.bottom);
				}
			}
			
			// prune block, cache will auto-pruned by scrolling
			this._removeColumn(maxcols, -delta);
		}		
	},
	_cmdMaxrow: function (maxrows) { 	// ZSS-242, modify range update function
		if(maxrows == this.maxRows) { // early return
			return;
		}

		var r, c, fpos, spos;
		var delta = maxrows - this.maxRows;
		var isIncreased = delta > 0;
		
		// update data panel's height
		var h = this.custRowHeight.getStartPixel(this.maxRows);
		h = this.custRowHeight.getStartPixel(maxrows) - h;
		var dp = this.dp;
		dp.updateHeight(h);
		
		// fix all panel's size
		this.maxRows = maxrows; //update max rows first
		dp._fixSize(this.activeBlock);
		this._fixSize(); // this will fix every panel's size
		
		if(isIncreased) { // increase rows
			this.activeBlock.loadForVisible();
		} else { // reduce rows
			
			// update focus and selection
			fpos = this.getLastFocus();
			if(fpos && (fpos.row > maxrows - 1)) { // {row, column}
				this.moveCellFocus(maxrows - 1, fpos.column);	// this will send AU
			}
			spos = this.getLastSelection(); // {left, top, right, bottom}
			if(spos && (spos.bottom > maxrows - 1)) { // intersected or out of bound, make selection to focus
				this.moveCellSelection(fpos.column, maxrows - 1, fpos.column, maxrows - 1, true);
				// reload and send AU
				spos = this.getLastSelection();
				if (this.selType) {//ZSS-1100: fire only if not change sheet(selType would be undefined when chagne sheet)
					this._sendOnCellSelection(this.selType, spos.left, spos.top, spos.right, spos.bottom);
				}
			}
			
			// prune block, cache will auto-pruned by scrolling
			this._removeRow(maxrows, -delta);
		}
	},
	_cmdMerge: function (result){
		var type = result.type;
		if (type == "remove")
			this._removeMergeRange(result);
		else if(type == "add")
			this._addMergeRange(result);
	},
	_cmdSelection: function (result) {
		var type = result.type,
			left = result.left,
			top = result.top,
			right = result.right,
			bottom = result.bottom;
		if (type == "move")
			this._doCellSelection(left, top, right, bottom);
	},
	_doCellSelection: function(left, top, right, bottom) {
		this.moveCellSelection(left, top, right, bottom, true);
		var ls = this.getLastSelection();//because of merge, selection might be change, get from last
		if (ls.left != left || ls.right != right || ls.top != top || ls.bottom != bottom) {
			this.selType = zss.SEL.CELL;
			this._sendOnCellSelection(this.selType, ls.left, ls.top, ls.right, ls.bottom);
		}
	},
	_cmdCellFocus: function (result) {
		var type = result.type,
			row = result.row,
			column = result.column;
		if (type == "move") {
			this.moveCellFocus(row, column);
			var pos = this.getLastFocus();
			if (pos.row != row || pos.column != column) //update server to new focus position
				this._sendOnCellFocus(pos.row, pos.column);
		}
	},
	_cmdRetriveFocus: function (result) {
		var type = result.type,
			row = result.row,
			column = result.column;
		if (type == "moveto") {
			//sheet.dp.selectCell(row,column,true);
			this.dp.moveFocus(row, column, true, true, true);
		} else if (type == "retrive") {
			this.dp._gainFocus(true, true);
		}
	},
	_cmdSize: function (result) {
		var type = result.type;
		if (type == "column")
			this._setColumnWidth(result.column, result.width, false, true, result.hidden, result.id, true);
		else if(type=="row")
			this._setRowHeight(result.row, result.height, false, true, result.hidden, result.id, result.custom);
	},
	_cmdHighlight: function (result) {
		var type = result.type;
		if (type == "hide") {
			this.hideHighlight(true);
		} else if(type == "show") {
			this.moveHighlight(result.left, result.top, result.right, result.bottom);
		}
	},
	_cmdGridlines: function (show) {
		this.setDisplayGridlines(show);
	},
	_shiftMouseSelection: function(evt, row, col, selType) {
		if (zkS.isEvtKey(evt, "s") && _isLeftMouseEvt(evt)) {
			var fpos = this.getLastFocus(),
				frow = fpos.row,
				fcol = fpos.column,
				left = col < fcol ? col : fcol,
				right = col < fcol ? fcol : col,
				top = row < frow ? row :  frow,
				bottom = row < frow ? frow : row;
			
			this.moveCellSelection(left, top, right, bottom, true);
			var ls = this.getLastSelection();
			this.selType = selType;
			this._sendOnCellSelection(selType, ls.left, ls.top, ls.right, ls.bottom);
			return true;
		}
		return false;
	},
	_doMousedown: function (evt) {
		this.innerClicking++;
		var sheet = this;
		setTimeout(function() {
			if (sheet.innerClicking > 0) sheet.innerClicking--;
		}, 0);

		var elm = evt.domTarget;
		if (zkS.parentByZSType(elm, "SMask"))
			return;

		if (this.state == zss.SSheetCtrl.NOFOCUS) {
			this._nfdown = true;//down on no foucs
			this.dp._gainFocus(true);
			return;
		} else
			this._nfdown = false;
		if (!_isEvtButton(evt, "lr"))
			return;
		
		this._lastmdelm = elm;//last mouse down on element
		this._lastmdstr = "";//last mouse down str;
		
		var cmp, row, col, mx, my;
		if ((cmp = zkS.parentByZSType(elm, "SCell"))) {
			var cmpofs = zk(cmp).revisedOffset();
			mx = evt.pageX;
			my = evt.pageY;
			
			var cellpos = zss.SSheetCtrl._calCellPos(sheet, mx, my, false);//calculate if over the width
			
			row = cellpos[0];
			col = cellpos[1];
			var cell = this.getCell(row, col);
			if (cell != null && cell.merr) {
				row = cell.mert;
				col = cell.merl;
			}
			cell = this.getCell(row, col);
			if (!cell || !cell.isSelectable())
				return;	

			if (this._shiftMouseSelection(evt, row, col, zss.SEL.CELL))
				return;			
			sheet.dp.moveFocus(row, col, false, true, false, true);
			this._lastmdstr = "c";

			var ls = this.getLastSelection();//cause of merge, focus might be change, get form last
			this.selType = zss.SEL.CELL;
			this.stopDragging(); //ZSS-1048: should stop old dragging first then set new dragging
			this.setDragging(new zss.SelDrag(sheet, this.selType, ls.top, ls.left,
					_isLeftMouseEvt(evt) ? "l" : "r", ls.right));
			
			//start hyperlink follow up
			if(_isLeftMouseEvt(evt) && this.selArea) {
				this.selArea._startHyperlink(elm);
				
				//ZSS-1048: prevent from dragging anchor in firefox
				if(zk.ff && elm.tagName.toLowerCase() == 'a')
					evt.stop();
			}
			
		} else if ((cmp = zkS.parentByZSType(elm, "SRow"))) { //click down on vertical merged cell
			var cmpofs = zk(cmp).revisedOffset();
			mx = evt.pageX;
			my = evt.pageY;
			
			var cellpos = zss.SSheetCtrl._calCellPos(sheet, mx, my, false);//calculate if over the width
			
			row = cellpos[0];
			col = cellpos[1];
			var cell = this.getCell(row, col);
			if (cell && cell.merr) {
				row = cell.mert;
				col = cell.merl;
				elm = cell.comp;
				this._lastmdelm = elm;
			}
			cell = this.getCell(row, col);
			if (!cell || !cell.isSelectable())
				return;	

			if (this._shiftMouseSelection(evt, row, col, zss.SEL.CELL))
				return;			
			sheet.dp.moveFocus(row, col, false, true, false, true);
			this._lastmdstr = "c";

			var ls = this.getLastSelection();//cause of merge, focus might be change, get from last
			this.selType = zss.SEL.CELL;
			this.stopDragging(); //ZSS-1048: should stop old dragging first then set new dragging
			this.setDragging(new zss.SelDrag(sheet, this.selType, ls.top, ls.left,
					_isLeftMouseEvt(evt) ? "l" : "r", ls.right));
			
			//start hyperlink follow up
			if(_isLeftMouseEvt(evt) && this.selArea)
				this.selArea._startHyperlink(elm);
		} else if ((cmp = zkS.parentByZSType(elm, "SSelDot", 1)) != null) {
			//modify selection
			if(_isLeftMouseEvt(evt)) {//TODO support right mouse down
				if (!this.selType)
					this.selType = zss.SEL.CELL;
				this.stopDragging(); //ZSS-1048: should stop old dragging first then set new dragging
				this.setDragging(new zss.SelChgDrag(sheet, this.selType, zss.SELDRAG.RESIZE));
			}
		} else if ((cmp = zkS.parentByZSType(elm, ["SSelInner", "SFocus", "SHighlight"], 1)) != null) {
			//Mouse down on Selection / Focus Block
			mx = evt.pageX;
			my = evt.pageY;
			
			var cellpos = zss.SSheetCtrl._calCellPos(sheet, mx, my, false);
			row = cellpos[0];
			col = cellpos[1];
			var cell = this.getCell(row, col);
			if (cell != null && cell.merr) {
				row = cell.mert;
				col = cell.merl;
			}
			if (this._shiftMouseSelection(evt, row, col, zss.SEL.CELL))
				return;			
			this._lastmdstr = "c";

			//Check whether click on AutoFilter button if a left mouse down
			var firebtndown = false,
				btn = this.getBtn ? this.getBtn(row, col) : null;
			if (_isLeftMouseEvt(evt) && btn) {
				var rx = cellpos[2], 
					ry = cellpos[3],
					right = btn.imgleft + btn.imgwidth,
					bottom = btn.imgtop + btn.imgheight;
				firebtndown = (rx >= btn.imgleft && rx < right && ry >= btn.imgtop && ry < bottom); //click on AutoFilter button
			}
			if (firebtndown)
				this._doBtndown(evt, btn.btntype, btn.$n(), btn);
			else if (_isLeftMouseEvt(evt) || cmp.getAttribute('zs.t') == "SHighlight") {
				sheet.dp.moveFocus(row, col, false, true, false, true);
				var ls = this.getLastSelection();//cause of merge, focus might be change, get form last
				this.selType = zss.SEL.CELL;
				this.stopDragging(); //ZSS-1048: should stop old dragging first then set new dragging
				this.setDragging(new zss.SelDrag(sheet, this.selType, ls.top, ls.left,
						_isLeftMouseEvt(evt) ? "l" : "r", ls.right));
				
				//start hyperlink follow up
				if (this.selArea)
					this.selArea._startHyperlink();
			}
		} else if ((cmp = zkS.parentByZSType(elm, ["SSelect"], 1)) != null) {
			mx = evt.pageX;
			my = evt.pageY;
			var cellpos = zss.SSheetCtrl._calCellPos(sheet, mx, my, false);
			row = cellpos[0];
			col = cellpos[1];
			var cell = this.getCell(row, col);
			if (cell != null && cell.merr) {
				row = cell.mert;
				col = cell.merl;
			}
			if (this._shiftMouseSelection(evt, row, col, zss.SEL.CELL))
				return;			
			this._lastmdstr = "c";
			
			if(_isLeftMouseEvt(evt)){//TODO support right mouse down
				range = sheet.selArea.lastRange;
				
				//adjust row,col to selection range
				if (row > range.bottom)
					row = range.bottom;
				else if (row < range.top)
					row = range.top;

				if (col > range.right)
					col = range.right;
				else if(col < range.left)
					col = range.left;
			
				if (!this.selType)
					this.selType = zss.SEL.CELL;
				this.stopDragging(); //ZSS-1048: should stop old dragging first then set new dragging
				this.setDragging(new zss.SelChgDrag(sheet, this.selType, zss.SELDRAG.MOVE, row, col));
				
				//start hyperlink follow up
				if (this.selArea)
					this.selArea._startHyperlink();
			}
		} else if ((cmp = zkS.parentByZSType(elm, "SLheader")) != null 
			|| (cmp = zkS.parentByZSType(elm, "STheader")) != null) {
			var type = (cmp.getAttribute('zs.t') == "SLheader") ? zss.Header.VER : zss.Header.HOR,
				row, col, onsel,	//process select row or column
				ls = this.selArea.lastRange,
				header = evt.target,
				wgt = this._wgt;
			this._lastmdstr = "h";
			if (type == zss.Header.HOR) {
				row = -1;
				col = header.index;

				if (!this.isHeaderSelectable(col, type))
					return;

				if(col >= ls.left && col <= ls.right &&
					ls.top == 0 && ls.bottom == this.maxRows - 1) {
					onsel = true;
				}
			} else {
				row = header.index;
				col = -1;

				if (!this.isHeaderSelectable(row, type))
					return;

				if(row >= ls.top && row <= ls.bottom &&
					ls.left == 0 && ls.right == this.maxCols - 1) {
					onsel = true;
				}
			}
			if (_isLeftMouseEvt(evt) || !onsel) {
				var range = zss.SSheetCtrl._getVisibleRange(this),
					seltype;
				if (row == -1) {//column
					if (zkS.isEvtKey(evt, "s") && _isLeftMouseEvt(evt)) {
						var fpos = this.getLastFocus(),
							fcol = fpos.column;
						sheet.moveColumnSelection(col, fcol);
						var ls = this.getLastSelection();
						this.selType = zss.SEL.COL;
						this._sendOnCellSelection(this.selType, ls.left, ls.top, ls.right, ls.bottom);
						return;
					}
					var fzr = sheet.frozenRow;
					sheet.dp.moveFocus((fzr > -1 ? 0 : range.top), col, true, true, false, true);
					//sheet.dp.selectCell((fzr > -1 ? 0 : range.top), col, true);//force move to first visible cell or 0 if frozenRow
					sheet.moveColumnSelection(col);
					seltype = zss.SEL.COL;
				} else {
					if (zkS.isEvtKey(evt, "s") && _isLeftMouseEvt(evt)) {
						var fpos = this.getLastFocus(),
							frow = fpos.row;
						sheet.moveRowSelection(row, frow);
						var ls = this.getLastSelection();
						this.selType = zss.SEL.ROW;
						this._sendOnCellSelection(this.selType, ls.left, ls.top, ls.right, ls.bottom);
						return;
					}
					var fzc = sheet.frozenCol;
					sheet.dp.moveFocus(row, (fzc > -1 ? 0 : range.left), true, true, false, true);
					//sheet.dp.selectCell(row, (fzc > -1 ? 0 : range.left),true);//force move to first visible cell
					sheet.moveRowSelection(row);
					seltype = zss.SEL.ROW;
				}
				sheet.selType = seltype;
				this.stopDragging(); //ZSS-1048: should stop old dragging first then set new dragging
				this.setDragging(new zss.SelDrag(sheet, seltype, row, col, _isLeftMouseEvt(evt) ? "l" : "r"));
			}
		} else if ((cmp = zkS.parentByZSType(elm, "SCorner", 1)) != null) {
			var ls = this.getLastSelection(),
				left = 0,
				top = 0,
				right = this.maxCols - 1,
				bottom = this.maxRows - 1;
			
			if (left != ls.left || top != ls.top || right != ls.right || bottom != ls.bottom) {
				this.moveCellSelection(left, top, right, bottom);
				this.selType = zss.SEL.ALL;
				this.stopDragging(); //ZSS-1048: should stop old dragging first then set new dragging
				this.setDragging(new zss.SelDrag(sheet, this.selType, 0, 0, _isLeftMouseEvt(evt) ? "l" : "r"));
			}
		}
		this._lastmdstr = this._lastmdstr + "_" + row + "_" + col;
	},
	_doMouseup: function (evt) {
		if (this.isAsync())//wait async event, skip mouse click;
			return;
		//bug#1974069, leftkey & has last mouse down element 
		//ZSS233,348 add SHyperlink and remove the fix of zss-30 
		if (_isLeftMouseEvt(evt) && this._lastmdelm && zkS.parentByZSType(this._lastmdelm, ["SCell", "SHighlight", "SSelInner", "SHyperlink"], 1) != null) {
			this._doMouseclick(evt, "lc", this._lastmdelm);
		}
		this._lastmdelm = null;
	},
	_doMouseleftclick: function (evt) {
		if(this.isAsync())//wait async event, skip mouse click;
			return;

		this._doMouseclick(evt, "lc");
		evt.stop();
	},
	_doMouserightclick : function (evt) {
		if (this.isAsync())//wait async event, skip mouse click;
			return;
		var dataPanel = this.dp;
		// ZSS-681: focus textarea before open stylepanel
		if (typeof dataPanel.selectFocustagId == "number") {
			window.clearTimeout(dataPanel.selectFocustagId);
			delete dataPanel.selectFocustagId;
			dataPanel.selectFocustag();
		}
		this._doMouseclick(evt, "rc");
		evt.stop();//always stop right (context) click.
	},
	_doMousedblclick: function (evt) {
		if (this.isAsync())//wait async event, skip mouse click;
			return;
		this._doMouseclick(evt, "dbc");
		evt.stop();
	},
	getStyleMenupopup: function () {
		var p = this._styleMenupopup;
		if (!p) {
			var wgt = this._wgt;
			p = this._styleMenupopup = new zss.MenupopupFactory(wgt).style();
			p.setDisabled(wgt.getActionDisabled());
			this.appendChild(p);
		}
		return p;
	},
	getRowHeaderMenupopup: function () {
		var p = this._rowHeaderMenupopup;
		if (!p) {
			var wgt = this._wgt;
			p = this._rowHeaderMenupopup = new zss.MenupopupFactory(wgt).rowHeader();
			p.setDisabled(wgt.getActionDisabled());
		}
		return p;
	},
	showRowHeaderMenu: function (pageX, pageY) {
		var show = this._wgt.isShowContextMenu();
		if (show) {
			var menu = this.getRowHeaderMenupopup();
			this.openStyleMenupopup(pageX, pageY, menu);
		}
	},
	getColumnHeaderMenupopup: function () {
		var p = this._columnHeaderMenupopup;
		if (!p) {
			var wgt = this._wgt;
			p = this._columnHeaderMenupopup = new zss.MenupopupFactory(this._wgt).columnHeader();
			p.setDisabled(wgt.getActionDisabled());
		}
		return p;
	},
	showColumnHeaderMenu: function (pageX, pageY) {
		var show = this._wgt.isShowContextMenu();
		if (show) {
			var menu = this.getColumnHeaderMenupopup();
			this.openStyleMenupopup(pageX, pageY, menu);
		}
	},
	getCellMenupopup: function () {
		var p = this._cellMenupopup;
		if (!p) {
			var wgt = this._wgt;
			p = this._cellMenupopup = new zss.MenupopupFactory(wgt).cell();
			p.setDisabled(wgt.getActionDisabled());
		}
		return p;
	},
	showCellContextMenu: function (pageX, pageY) {
		var show = this._wgt.isShowContextMenu();
		if (show) {
			var menu = this.getCellMenupopup();
			menu.updateItemState.call(menu, {sheet: this, pageX: pageX, pageY: pageY});
			this.openStyleMenupopup(pageX, pageY, menu);
		}
	},
	setActionDisabled: function (actions) {
		var show = this._wgt.isShowContextMenu();
		if (show && actions) {
			var cellPopup = this.getCellMenupopup();
			if (cellPopup) {
				cellPopup.setDisabled(actions);
			}
			var rowPopup = this.getRowHeaderMenupopup();
			if (rowPopup) {
				rowPopup.setDisabled(actions);
			}
			var colPopup = this.getColumnHeaderMenupopup();
			if (colPopup) {
				colPopup.setDisabled(actions);
			}
			var stylePopup = this.getStyleMenupopup();
			if (stylePopup) {
				stylePopup.setDisabled(actions);
			}
		}
	},
	openStyleMenupopup: function (x, y, refPop) {
		x += 5;
		y -= 85;
		y = y < 0 ? 0 : y;
		
		var p = this.getStyleMenupopup(),
			wgt = this._wgt;
		var fn = function() {
			p.getMenuContainer().appendChild(refPop);

			// 20140509, RaymondChao: set style container visibility to hidden
			// so that menu container's position will be located at the pointer
			if (wgt.isProtect() && !wgt.allowFormatCells) {
				jq(p.getStyleContainer()).css('visibility', 'hidden');
			} else {
				jq(p.getStyleContainer()).css('visibility', 'visible');
			}

			// make menu visible before style menu showing
			// for calculating correct height
			jq(refPop.$n()).css('display', 'block');
			p.open(null, [x, y]);
			
			refPop.setVisible(true);
		}
		
		if(zk.ie < 11) {
			// ZSS-654: popup's close() has 50ms delay if using IE < 11 in Popup.js afterCloseAnima_()
			setTimeout(fn, 50);
		} else {
			fn();
		}
	},
	runAfterMouseClick: function (fn) {
		var fns = this._afterMouseClick;
		if (!fns) {
			fns = this._afterMouseClick = [];
		}
		fns.push(fn);
	},
	doAfterMouseClick: function () {
		var fns = this._afterMouseClick;
		if (fns) {
			var fn;
			while (fn = fns.shift()) {
				fn();
			}
			this._afterMouseClick = null;
		}
	},
	/**
	 * @param zk.Event, mouse event
	 * @param string type "lc" for left click, "rc" for right click, "dbc" for double click, "af" for autofilter
	 */
	_doMouseclick: function (evt, type, element) {
		if (this._nfdown) {
			if (this.selArea) 
				this.selArea._stopHyperlink();
			return; // don't care click if it was fired when nofocus mouse down
		}
		var sheet = this,
			wgt = sheet._wgt,
			elm = (element) ? element : evt.domTarget,
			cmp,
			mx = my = 0,//mouse offset, against body 
			shx = shy = 0,//mouse offset against sheet
			row, 
			col,
			md1 = zkS._getMouseData(evt, this.comp),
			mdstr = "";
		
		//ZSS-630 mark to hold focus
		this.innerClicking++;
		setTimeout(function() {
			if (sheet.innerClicking > 0) sheet.innerClicking--;
		}, 0);
		
		//Click on Cell
		if ((cmp = zkS.parentByZSType(elm, "SCell", 0)) != null) {
			var cellcmp = cmp,
				sheetofs = zk(sheet.comp).revisedOffset(),
			//TODO there is a bug in opera, when a cell is overflow, zk.revisedOffset can get correct component offset 
				cmpofs = zk(cellcmp).revisedOffset();
			
			mx = evt.pageX;
			my = evt.pageY;
			shx = Math.round(mx - sheetofs[0]);
			shy = Math.round(my - sheetofs[1]);
			
			var x = mx - cmpofs[0],
				cellpos = zss.SSheetCtrl._calCellPos(sheet, mx, my, false);
			row = cellpos[0];
			col = cellpos[1];
			var cell = this.getCell(row, col);
			if (cell != null && cell.merr) {
				row = cell.mert;
				col = cell.merl;
			}
			
			mdstr = "c_" + row + "_" + col;

			if (this._lastmdstr == mdstr) {
				if (type == 'rc') {
					this.showCellContextMenu(mx, my);
				}
				wgt.fireCellEvt(type, shx, shy, md1[2], row, col, mx, my);
			}
				
			if (type == 'lc' && this.selArea) {
				if (cell!=null && cell.merr) {
					var anchorA;
					// go to real merged cell
					cell = sheet.getCell(cell.mert,cell.merl);
					if(cell && (anchorA = jq(cell).find('a')) && anchorA.length > 0) {
						// detect overlap between cursor and anchor
						if(zkS.isOverlapByPoint(anchorA, mx, my))
							elm = anchorA[0];
					}
				}
				this.selArea._setHyperlinkElment(elm);
				this.selArea._tryAndEndHyperlink(row, col, evt);
			}
		} else if ((cmp = zkS.parentByZSType(elm, "SRow", 0)) != null) { //when click on vertical merged cell 
			var cellcmp = cmp, //row
				sheetofs = zk(sheet.comp).revisedOffset(),
			//TODO there is a bug in opera, when a cell is overflow, zk.revisedOffset can get correct component offset 
				cmpofs = zk(cellcmp).revisedOffset();
			
			mx = evt.pageX;
			my = evt.pageY;
			shx = Math.round(mx - sheetofs[0]);
			shy = Math.round(my - sheetofs[1]);
			
			var x = mx - cmpofs[0],
				cellpos = zss.SSheetCtrl._calCellPos(sheet, mx, my, false);
			row = cellpos[0];
			col = cellpos[1];
			var cell = this.getCell(row, col);
			if (cell != null && cell.merr) {
				row = cell.mert;
				col = cell.merl;
				elm = cell.comp;
			}
			mdstr = "c_" + row + "_" + col;

			if (this._lastmdstr == mdstr)
				wgt.fireCellEvt(type, shx, shy, md1[2], row, col, mx, my);

			if (type == 'lc' && this.selArea) {
				this.selArea._setHyperlinkElment(elm);
				this.selArea._tryAndEndHyperlink(row, col, evt);
			}
		} else if((cmp = zkS.parentByZSType(elm, "SSelDot", 1)) != null) {
		//TODO
		} else if((cmp = zkS.parentByZSType(elm, [(zk.ie8 && zk.ie < 11) ? "SSelInner" : "SSelect", "SFocus", "SHighlight"], 1)) != null ) {
			//Mouse click on Selection / Focus Block
			var sheetofs = zk(sheet.comp).revisedOffset();
			mx = evt.pageX;
			my = evt.pageY;
			shx = Math.round(mx - sheetofs[0]);
			shy = Math.round(my - sheetofs[1]);
			var cellpos = zss.SSheetCtrl._calCellPos(sheet, mx, my, false),
				cx = cellpos[4]; //x relative to cell 
			row = cellpos[0];
			col = cellpos[1];
			var cell = this.getCell(row, col);
			if (cell != null && cell.merr) {
				row = cell.mert;
				col = cell.merl;
			}
			
			//try hyperlink, ZSS-21: Right click on hyperlink should not jump to the link
			if (type == 'lc' && this.selArea) {
				this.selArea._tryAndEndHyperlink(row, col, evt);
			}
			mdstr = "c_" + row + "_" + col;			
			if (this._lastmdstr == mdstr) {
				if (type == 'rc') {
					this.showCellContextMenu(mx, my);
				} else if (type == "dbc") {
					sheet._enterEditing(null);
				}
				wgt.fireCellEvt(type, shx, shy, md1[2], row, col, mx, my);
			}
		} else if ((cmp = zkS.parentByZSType(elm, "STheader",1)) != null ||
			(cmp = zkS.parentByZSType(elm, "SLheader",1)) != null) {
			//Click on header
			var headercmp = cmp,
				sheetofs = zk(sheet.comp).revisedOffset();
			
			mx = evt.pageX;
			my = evt.pageY;
			shx = Math.round(mx - sheetofs[0]);
			shy = Math.round(my - sheetofs[1]);
			
			if (headercmp.ctrl.type == zss.Header.HOR) {
				row = -1;
				col = headercmp.ctrl.index;
			} else {
				row = headercmp.ctrl.index;
				col = -1;
			}
			mdstr = "h_" + row + "_" + col;
			if (this._lastmdstr == mdstr) {
				
				// #ZSS-292: only allow right click to pop-up context menu on row and column header
				if (type === 'rc') {
					if (headercmp.ctrl.type == 'V') {
						this.showRowHeaderMenu(mx, my);
					} else {
						this.showColumnHeaderMenu(mx, my);
					}
					// don't use this coding style !!!
					//	this['show' + (headercmp.ctrl.type == 'V' ? 'Row' : 'Column') + 'HeaderMenu'](mx, my);
				}
				
				wgt.fireHeaderEvt(type, shx, shy, md1[2], row, col, mx, my);
			}
		}
		this.doAfterMouseClick();
	},
	_sendOnCellFocus: function (row, col) {
		var wgt = this._wgt;
		wgt.fire('onCellFocus', {sheetId: this.serverSheetId, row: row, col : col});
	},
	_sendOnCellSelection: function (type, left, top, right, bottom) {
		this._wgt.fire('onCellSelection',
				{sheetId: this.serverSheetId, type: type, left: left, top: top, right: right, bottom: bottom});
	},
	_sendOnCellSelectionUpdate: function (type, action, left, top, right, bottom, orgleft, orgtop, orgright, orgbottom) {
		this._wgt.fire('onCellSelectionUpdate',
				{sheetId: this.serverSheetId, type:type, action: action, left: left,top: top, right: right, bottom: bottom, orgileft: orgleft, orgitop: orgtop, orgiright: orgright, orgibottom: orgbottom});
	},
	_sendOnCellHyperlink: function (row, col, href, type, evt) {
		var wgt = this._wgt,
			data = zk.copy(evt.data, {sheetId: this.serverSheetId, row: row, col: col, href: href, type: type});
		wgt.fire('onCellHyperlink', data, wgt.isListen('onCellHyperlink') ? {toServer: true} : null);
	},
	_timeoutId: null,
	_fireOnOpenAndEdit: function (time) { //open Editbox and start editing
		clearTimeout(this._timeoutId);
		this._timeoutId = setTimeout(this.proxy(this._onOpenAndEdit), time >= 0 ? time : 100);
	},
	_onOpenAndEdit: function () {
		this.dp._startEditing(); //open client side edit box to catch the user input
	},
	_doKeypress: function (evt) {
		if (this._skipress) //wait async event, skip
			return;
		var charcode = evt.which,
			c = asciiChar(charcode == 0 && evt.keyCode == 9 ? keyCode : charcode);
		//ascii, not editing, not special key
		if (c != null && !(evt.altKey || evt.ctrlKey) && this.state != zss.SSheetCtrl.EDITING) {
			if (this.state == zss.SSheetCtrl.START_EDIT) //startEditing but not get response from server yet
				this._clienttxt += c; //this._clienttxt is cleared in DataPanel#_startEditing()
			else if (this.state == zss.SSheetCtrl.FOCUSED) {
				if (this.dp.startEditing(evt, c)) //fire to server so user can override the result
					this._clienttxt = c;
			}
			//bug #117: Barcode Scanner data incomplete
			this._fireOnOpenAndEdit();
			evt.stop();
		}
	},
	_enterEditing: function(evt) {
		var p = this.getLastFocus();
		this.dp.startEditing(evt, this.getCell(p.row, p.column).edit);
		this.dp._openEditbox();
	},
	_doKeydown: function(evt) {
		this._skipress = false;
		//wait async event, skip
		//handle spreadsheet common keydown event
		if (this.isAsync()) return;
		
		//ctrl-paste: avoid multi-paste same clipboard content to focus textarea
		if (this._wgt._ctrlPasteDown)
			evt.stop();
		var keycode = evt.keyCode,
			ctrl;
		switch (keycode) {
		case 33: //PgUp
			this.dp.movePageup(evt);
			evt.stop();
			break;
		case 34: //PgDn
			this.dp.movePagedown(evt);
			evt.stop();
			break;
		case 35: //End
			if (this.isAllowKeyNavigation()) {
				this.dp.moveEnd(evt);
				//evt.stop(); //ZSS-181
			}
			break;
		case 36: //Home
			if (this.isAllowKeyNavigation()) {
				this.dp.moveHome(evt);
				//evt.stop(); //ZSS-181
			}
			break;
		case 37: //Left
			if (this.isAllowKeyNavigation()) {
				this.dp.moveLeft(evt);
				//evt.stop(); //ZSS-181
			}
			break;
		case 38: //Up
			if (this.isAllowKeyNavigation()) {
				this.dp.moveUp(evt);
				//evt.stop(); //ZSS-181
			}
			break;
		case 9://tab;
			if (this.state == zss.SSheetCtrl.EDITING){
				if (evt.altKey || evt.ctrlKey)
					break;
				this.dp.stopEditing(evt.shiftKey ? "moveleft" : "moveright");//invoke move right after stopEdit
				evt.stop();
				break;
			}
			if (evt.shiftKey) {
				this.dp.moveLeft();
				evt.stop();
			} else if (!(evt.altKey || evt.ctrlKey)) {
				this.dp.moveRight();
				evt.stop();
			}
			break;
		case 39: //Right
			var info = this.editingFormulaInfo;
			if (this.isAllowKeyNavigation()) {
				this.dp.moveRight(evt);
				//evt.stop(); //ZSS-181
			}
			break;
		case 40: //Down
			if (this.isAllowKeyNavigation()) {
				this.dp.moveDown(evt);
				//evt.stop(); //ZSS-181
			}
			break;
		case 229: //ZSS-378 Chinese Input keyCode is always 229 in chrome/IE(8-10) (no spec.)
			// ZSS-737: other browsers listen composition event to catch IME input.
			if (zk.ie && zk.ie < 10 && this.state == zss.SSheetCtrl.FOCUSED) { //enter editing mode only when focused
				this._enterEditing(evt);
			}
			break;
		case 113: //F2
			if(this.state == zss.SSheetCtrl.FOCUSED)
				this._enterEditing(evt);
			evt.stop();
			break;
		case 13://Enter
			if (this.state == zss.SSheetCtrl.EDITING){
				if(evt.altKey || evt.ctrlKey){
					this.dp.getEditor().newLine();
					evt.stop();
					break;
				}
				this.dp.stopEditing("movedown");//invoke move down after stopEdit
				evt.stop();
			} else if (this.state == zss.SSheetCtrl.FOCUSED) {
				if (!this._wgt._copysrc) {
					this.dp.moveDown(evt);
					evt.stop();
				}
			}
			break;
		case 27://ESC
			if (this.state == zss.SSheetCtrl.EDITING) {
				this.dp.cancelEditing();
				evt.stop();
			} else if(this.state == zss.SSheetCtrl.FOCUSED) {
				//TODO should i send onCancel here?
			}
			break;
		}
		//in my notebook,some keycode ex : LEFT(37) and RIGHT(39) will fire keypress after keydown,
		//it confuse with the ascii value "%' and ''', so add this to do some controll in key press
		if (!isAsciiCharkey(keycode)) {
			this._skipress = true;
		}
	},
	isAllowKeyNavigation: function () {
		var info = this.editingFormulaInfo;
		if ((info && !info.moveCell) || this.state == zss.SSheetCtrl.FOCUSED || this.enableKeyNavigation) {
			return true;
		}
		return false;
	},
	//feature #161: Support copy&paste from clipboard to a cell
	pasteToSheet: function () {
		var wgt = this._wgt,
			sl = this,
			//ZSS-169
			fn = function () {
				if (!wgt._doPasteFromServer) {//do paste from client when server doesn't do it
					var focustag = sl.dp.getInputNode(),
						value = jq(focustag).val(),
						pos = sl.dp._speedCopy(value);
					
					//Note. _speedCopy will fire edit cmd to server, set selection after response
					if (pos)
						wgt._onResponseCallback.push(function () {
							sl._doCellSelection(pos.left, pos.top, pos.right, pos.bottom);
						});
				}
				if (sl.isPasteFromClipboard) {
					sl.isPasteFromClipboard = null;
				}
			};
		if (wgt._sendAu) {//flag that indicate ZK send Au request. (cannot use zAu.processing(), it may be null since ZK use timeout to send request) 
			wgt._onResponseCallback.push(fn); 
		} else {
			fn();
		}
	},
	/**
	 * resize Sheet
	 * @param {String} w width string
	 * @param {String} h height string
	 */
	resizeTo: function(w , h) {
		//don't use style-class, use style, because style of sheet is the colsest style. 
		var sheetcmp = this.comp;
		if (w)
			jq(sheetcmp).css('width', w);
		if (h)
			jq(sheetcmp).css('height', h);
		
		var self = this;
		setTimeout(function(){
			self._resize();
		}, 0);
	},
	/**fix sp, tp and lp size*/
	_fixSize: function () {
		var sheetcmp = this.comp,
			spcmp = this.sp.comp,
			$n = zk(sheetcmp),
			w = $n.offsetWidth(),
			h = $n.offsetHeight();
		if (h <= 0)
			//if user doesn't set the height of style sheet set height on it's parent, 
			//then we will get a zero height, so , i assign a default height here
			h = 100;
		var barHeight = zkS._hasScrollBar(spcmp) ? zss.Spreadsheet.scrollWidth : 0,
			barWidth = zkS._hasScrollBar(spcmp, true) ? zss.Spreadsheet.scrollWidth : 0,
			zkdp = zk(this.dp.comp), //bug #61: Fronzen row/column does not comply with Spreadsheet's maxrows/maxcolumn
			rw = Math.min(zkdp.offsetWidth() - this.leftWidth, w - this.leftWidth- barWidth),
			rh = Math.min(zkdp.offsetHeight() - this.topHeight, h - this.topHeight - barHeight);
		
		this.tp._updateWidth(rw);
		this.lp._updateHeight(rh);
		this.sp._doScrolling();
	},
	/**
	 * Returns last focus position
	 * @return zss.Pos
	 */
	getLastFocus: function () {
		if (!this.focusMark)
			return null;
		return new zss.Pos(this.focusMark.row, this.focusMark.column);
	},
	/**
	 * Returns last selection range
	 * @return zss.Range
	 */
	getLastSelection: function () {
		if (!this.selArea)
			return null;
		var range = this.selArea.lastRange;
		return !range ? null : new zss.Range(range.left, range.top, range.right, range.bottom);
	},
	/**
	 * Sets column selection
	 * @param int from
	 * @param int to
	 */
	moveColumnSelection: function (from, to) {
		if (!to && to != 0)
			to = from;

		var t = from;
		if (from > to) {
			from = to;
			to = t;
		}
		this.moveCellSelection(from, 0, to, this.maxRows - 1);
	},
	/**
	 * Sets row selection
	 * @param int from 
	 * @param int to
	 */
	moveRowSelection: function (from, to) {
		if(!to && to!=0){
			to = from;
		}
		var t = from;
		if (from > to) {
			from = to;
			to = t;
		}
		this.moveCellSelection(0, from, this.maxCols - 1, to);
	},
	/**
	 * set column width
	 * @param {Object, int} col column index or header of column component
	 * @param {Object} width the new width
	 */
	setColumnWidth: function (col, width) {
		this._setColumnWidth(col, width, true, true);
	},
	_setColumnsWidth: function (left, right, width, fireevent, loadvis, hidden, metaid) {
		for(var col=left; col<=right; ++col)
			this._setColumnWidth(col, width, fireevent, loadvis, hidden, metaid);
	},
	_setColumnWidth: function (col, width, fireevent, loadvis, hidden, metaid) {
		var wgt = this._wgt,
			sheetid = this.sheetid,
			custColWidth = this.custColWidth,
			oldw = custColWidth.getSize(col);
		if (width < 0)
			width = 0;

		//update customized width
		var meta = custColWidth.getMeta(col),
			zsw;
		if (hidden === undefined) {
			hidden = (width == 0);
		}
		if (hidden)
			width = oldw;
		
		//adjust cell width, check also:_updateCustomDefaultStyle
		var cp = this.cellPad,
			cellwidth,
			celltextwidth = width - 2 * cp;
		
		//bug 1989680
		var fixpadding = false;
		if (celltextwidth < 0) {
			fixpadding = true;
			celltextwidth = width;
		}
		cellwidth = width;
		
		var name = wgt.getSelectorPrefix(),
			cssId = wgt.getSheetCSSId(),
			createbefor = ".zs_header";
		if(zk.opera) //opera bug, it cannot insert rul to special position
			createbefor = true;

		if (!meta) {
			//append style class to column header and cell
			zsw = zkS.t(metaid) ? metaid : custColWidth.ids.next();
			custColWidth.setCustomizedSize(col, width, zsw, hidden);
			this._appendZSW(col, zsw);
			this._wgt._cacheCtrl.getSelectedSheet().updateColumnWidthId(col, zsw);
		} else {
			zsw = zkS.t(metaid) ? metaid : meta[2];
			custColWidth.setCustomizedSize(col, width, zsw, hidden);
		}

		if (width <= 0 || hidden)
			zcss.setRule(name + " .zsw" + zsw, "display", "none", createbefor, cssId);
		else {
			zcss.setRule(name + " .zsw" + zsw, ["display", "width"], ["", cellwidth + "px"], createbefor, cssId);
			zcss.setRule(name + " .zswi" + zsw, "width", celltextwidth + "px", createbefor, cssId);
			//bug 1989680
			if (fixpadding)
				zcss.setRule(name + " .zsw" + zsw, "padding", "0px", createbefor, cssId);
			else
				zcss.setRule(name + " .zsw" + zsw, "padding", "", createbefor, cssId);
		}

		//set merged cell width;
		var ranges = this.mergeMatrix.getRangesByColumn(col),
			size = ranges.length,
			range;
		
		for (var i = 0; i < size; i++) {
			range = ranges[i];
			var w = custColWidth.getStartPixel(range.right + 1);
			w -= custColWidth.getStartPixel(range.left);

			celltextwidth = w - 2 * cp;
			fixpadding = false;
			if (celltextwidth < 0) {
				fixpadding = true;
				celltextwidth = w;
			}
			cellwidth = w;

			if (w <= 0) 
				zcss.setRule(name+" .zsmerge"+range.id,"display","none",true, cssId);
			else {
				// ZSS-330, ZSS-382: when column was hidden, the left-top cell of merge is also hidden by column style.  
				// But, the left-top cell must display, its position and size should be adjusted automatically
				zcss.setRule(name+" .zsmerge"+range.id,"display","inline-block",true, cssId);
				zcss.setRule(name+" .zsmerge"+range.id,"width", jq.px0(cellwidth), true, cssId);
				zcss.setRule(name+" .zsmerge"+range.id+" .zscelltxt","width", jq.px0(celltextwidth), true, cssId);
				if (fixpadding)
					zcss.setRule(name+" .zsmerge"+range.id,"padding", "0px",true, cssId);
				else
					zcss.setRule(name+" .zsmerge"+range.id,"padding", "", true, cssId);
			}
		}
		
		//adjust header
		var tp = this.tp, 
			header = tp ? tp.getHeader(col) : null;
		if (header)
			header.setColumnHeader(hidden);
		
		if (col < this.maxCols) {
			//adjust datapanel size;
			var dp = this.dp;
		
			//process text overflow when resize column
			this.triggerOverflowColumn_(null, col + 1, true);
			
			//ZSS-944: process 90deg rotate when resize column
			this.triggerRotateColumn_(null, col, true);
			
			if (this.frozenCol >= col) {
				this.lp._fixSize();
				this.cp._fixSize();
			}

			//update datapanel padding
			// ZSS-324: column/row header must be update their size after data panel updated
			if(!this._deferUS) { // _deferUS, a flag for defer ctrl.
				this._deferUS = true;
				var that = this;
				setTimeout(function(){
					delete that._deferUS;
					dp._fixSize(that.activeBlock);
					that._fixSize();
				}, 20); // before loadForVisible()
			}

			this._wgt.syncWidgetPos(-1, col);
		}

		//sync focus and selection area
		this._syncColFocusAndSelection(col);

		if (fireevent) {
			this._wgt.fire('onHeaderUpdate', 
					{sheetId: this.serverSheetId, type: "top", action: "resize", index: col, size: width, id: zsw, hidden: hidden, custom:true},
					{toServer: true, sendAhead: true}, 25);//ZSS-180
		}
		//ZSS-180
		if(col < this.maxCols && loadvis) {
			// ZSS-324: this is a heavy duty, so just call once when batch updating
			if (!this._deferL4V) { //_deferL4V, a flag for defer ctrl.
				this._deferL4V = true;
				var that = this;
				setTimeout(function() {
					delete that._deferL4V;
					that.activeBlock.loadForVisible(); // data panel update here
					that._fixSize(); // column/row header must be update their size after data panel updated
				}, 25);
			}
		}
	},
	_syncColFocusAndSelection: function(left) {
		var focPos = this.getLastFocus(),
			fCol = focPos.column,
			ls = this.getLastSelection(),
			selL = ls.left,
			selR = ls.right;
		if (left <= fCol)
			this.moveCellFocus(focPos.row, fCol);
		if (left <= selR)
			this.moveCellSelection(selL, ls.top, selR, ls.bottom);
	},
	_syncRowFocusAndSelection: function(top) {
		var focPos = this.getLastFocus(),
			fRow = focPos.row,
			ls = this.getLastSelection(),
			selT = ls.top,
			selB = ls.bottom;
		if (top <= fRow)
			this.moveCellFocus(fRow, focPos.column);
		if (top <= selB)
			this.moveCellSelection(ls.left, selT, ls.right, selB);
	},
	_appendZSW: function(col, zsw) {
		this.activeBlock.appendZSW(col, zsw);
		this.cp.appendZSW(col, zsw);
		this.tp.appendZSW(col, zsw);
		this.lp.appendZSW(col, zsw);
	},
	_appendZSH: function(row, zsh) {
		this.activeBlock.appendZSH(row, zsh);
		this.cp.appendZSH(row, zsh);
		this.tp.appendZSH(row, zsh);
		this.lp.appendZSH(row, zsh);
	},
	/**
	 * set row height
	 * @param {Object} row row index or header of row component
	 * @param {Object} height new height of row
	 */
	setRowHeight: function(row, height) {
		this._setRowHeight(row, height, true, true, false, null, true);
	},
	_setRowsHeight: function(top, bottom, height, fireevent, loadvis, hidden, metaid, isCustom) {
		for(var row=top; row<=bottom; ++row)
			this._setRowHeight(row, height, fireevent, loadvis, hidden, metaid, isCustom);
	},
	_setRowHeight: function(row, height, fireevent, loadvis, hidden, metaid, isCustom) {
		var wgt = this._wgt,
			sheetid = this.sheetid,
			custRowHeight = this.custRowHeight,
			oldh = custRowHeight.getSize(row);
		height = height <= 0 ? 0 : height;

		var name = "#" + sheetid,
			meta = custRowHeight.getMeta(row),
			zsh;
		if (hidden === undefined) {
			hidden = (height == 0);
		} 
		if (hidden)
			height = oldh;
			
		var cellheight;// = zk.revisedSize(colcmp,height,true);
		
		if((zk.ie && zk.ie < 11) || zk.safari || zk.opera)
			//1989680
			cellheight = height > 0 ? height : 0;
		else
			cellheight = height;

		if (!meta) {
			//append style class to column header and cell
			zsh = zkS.t(metaid) ? metaid : custRowHeight.ids.next();
			custRowHeight.setCustomizedSize(row, height, zsh, hidden, isCustom);
			this._appendZSH(row, zsh);
			this._wgt._cacheCtrl.getSelectedSheet().updateRowHeightId(row, zsh);
		} else {
			zsh = zkS.t(metaid) ? metaid : meta[2];
			custRowHeight.setCustomizedSize(row, height, zsh, hidden, isCustom);
		}
		
		var name = wgt.getSelectorPrefix(),
			cssId = wgt.getSheetCSSId(),
			createbefor = ".zs_header";
		if (zk.opera)//opera bug, it cannot insert rul to special position
			createbefor = true;

		if (height <= 0 || hidden) {
			// ZSS-330, ZSS-382: using "height: 0" and don't use "display: none", latter one cause merge cell to chaos
			// ZSS-500: re-overwrite overflow to hidden when row hidden 
			zcss.setRule(name + " .zsh" + zsh, "height", "0px", createbefor, cssId);
			zcss.setRule(name + " .zshi" + zsh, ["height", "border-bottom-width", "overflow"], ["0px", "0px", "hidden"], createbefor, cssId);//both zscell and zscelltxt
			zcss.setRule(name + " .zslh" + zsh, ["height", "line-height", "border-bottom-width"], ["0px", "0px", "0px"], createbefor, cssId); // header has 1px border.
			zcss.setRule(name + " .zshr" + zsh, ["max-height"], ["0px"], createbefor, cssId); // real in cell
		} else {
			zcss.setRule(name + " .zsh" + zsh, "height", height + "px", createbefor, cssId);
			zcss.setRule(name + " .zshi" + zsh, ["height", "border-bottom-width", "overflow"], [cellheight + "px", "1px", ""], createbefor, cssId);//both zscell and zscelltxt
			zcss.setRule(name + " .zslh" + zsh, ["height", "line-height", "border-bottom-width"], [height + "px", height + "px", "1px"], createbefor, cssId);
			zcss.setRule(name + " .zshr" + zsh, ["max-height"], [height + "px"], createbefor, cssId); // real in cell
		}
		
		//set merged cell height;
		var ranges = this.mergeMatrix.getRangesByRow(row),
			size = ranges.length,
			range;
		
		for (var i = 0; i < size; i++) {
			range = ranges[i];
			var h = custRowHeight.getStartPixel(range.bottom + 1);
			h -= custRowHeight.getStartPixel(range.top);

			celltextheight = h;
			cellheight = (zk.ie && zk.ie < 11) || zk.safari || zk.opera ? celltextheight : h;

			if (h <= 0) {
				zcss.setRule(name+" .zsmerge"+range.id,"display","none",true, cssId);
			} else {
				// ZSS-330, ZSS-382: when row was hidden, the left-top cell of merge is also hidden by row style.  
				// But, the left-top cell must display, its position and size should be adjusted automatically
				zcss.setRule(name+" .zsmerge"+range.id,"display","inline-block",true, cssId);
				zcss.setRule(name+" .zsmerge"+range.id,"height", jq.px0(cellheight), true, cssId);
				zcss.setRule(name+" .zsmerge"+range.id+" .zscelltxt","height", jq.px0(celltextheight),true, cssId);
				zcss.setRule(name+" .zsmerge"+range.id,"border-bottom-width","1px",true, cssId); // re-apply bottom border for grid line; Or grid line will be missed if row was hidden  
			}
		}
		
		//adjust header
		var lp = this.lp,
			header = lp ? lp.getHeader(row) : null;
		if (header)
			header.setRowHeader(hidden);
		
		if (row < this.maxRows) {
			//adjust datapanel size;
			var dp = this.dp;
		
			var fzr = this.frozenRow;
			if (fzr >= row) {
				this.tp._fixSize();
				this.cp._fixSize();
			}

			// ZSS-324: column/row header must be update their size after data panel updated
			if(!this._deferUS) { // _deferUS, a flag for defer ctrl.
				this._deferUS = true;
				var that = this;
				setTimeout(function(){
					delete that._deferUS;
					dp._fixSize(that.activeBlock);
					that._fixSize();
				}, 20); // before loadForVisible()
			}
			
			this._wgt.syncWidgetPos(row, -1);
		}
		//sync focus and selection area
		this._syncRowFocusAndSelection(row);
		this.fire('onRowHeightChanged', {row: row});

		if (fireevent) {
			this._wgt.fire('onHeaderUpdate', 
					{sheetId: this.serverSheetId, type: "left", action: "resize", index: row, size: height, id: zsh, hidden: hidden, custom:isCustom},
					{toServer: true, sendAhead: true}, 25);//ZSS-180
		}
		//ZSS-180
		if (row < this.maxRows && loadvis) {
			// ZSS-324: this is a heavy duty, so just call once when batch updating
			if (!this._deferL4V) {
				this._deferL4V = true; //_deferL4V, a flag for defer ctrl.
				var that = this;
				setTimeout(function() {
					delete that._deferL4V;
					that.activeBlock.loadForVisible(); // data panel update here
					that._fixSize(); // column/row header must be update their size after data panel updated
				}, 25);
			}
		}
	},
	_updateText: function (result) {
		var cell = this.activeBlock.getCell(result.r, result.c);
		if (cell)//update if cell exist 
			cell.setText(result.val, true);
	},
	/**
	 * Update cells
	 */
	update_: function (data) {
		var cb = this.cp.block;
		var tb = this.tp.block;
		var lb = this.lp.block;
		var d;

		// ZSS-392: update cells separately (data panel and freeze panels)
		this.activeBlock.update_(data.t, data.l, data.b, data.r);
		if (cb && data.cornerFrozen) {
			d = data.cornerFrozen;
			cb.update_(d.t, d.l, d.b, d.r);
		}
		if (tb && data.topFrozen) {
			d = data.topFrozen;
			tb.update_(d.t, d.l, d.b, d.r);
		}
		if (lb && data.leftFrozen) {
			d = data.leftFrozen;
			lb.update_(d.t, d.l, d.b, d.r);
		}
		
		/* ZSS-169: prepare client side paste src when user set selection by drag cells 
		//feature #26: Support copy/paste value to local Excel		
		var ls = this.getLastSelection();
		if (tRow >= ls.top && bRow <= ls.bottom && lCol >= ls.left && rCol <= ls.right) {
			this._prepareCopy(debug);
		}
		*/
	},
	_updateHeaderSelectionCss: function (range, remove) {
		var top = range.top,
			bottom = range.bottom,
			left = range.left,
			right = range.right;

		//hor
		this.tp.updateSelectionCSS(left, right, remove);
		this.lp.updateSelectionCSS(top, bottom, remove);
		if (this.cp.tp)
			this.cp.tp.updateSelectionCSS(left, right, remove);	
		if (this.cp.lp)
			this.cp.lp.updateSelectionCSS(top, bottom, remove);
	},
	/**
	 * Sets whether display the gridlines for this sheet.
	 */
	setDisplayGridlines: function (show) {
		var wgt = this._wgt,
			bc = show ? '':'#FFFFFF';
		zcss.setRule(wgt.getSelectorPrefix() + ' .zscell', ['border-bottom-color', 'border-right-color'],[bc, bc], true, wgt.getSheetCSSId());
		this.fireDisplayGridlines(show);
	},
	deferFireCellSelection: function (left, top, right, bottom) {
		var id = this._fireCellSelectionId,
			self = this,
			skipInsertCellRef = this._skipInsertCellRef;
		if (id) {
			clearTimeout(id);
		}
		this._fireCellSelectionId = setTimeout(function () {
			//ZSS 171
			// 20140612, RaymondChao: it should skip insert cell reference when updating cell from server, so it needs to keep
			// the flag when defer fire cell selection
			self.fire('onCellSelection', {left: left, top: top, right: right, bottom: bottom, skipInsertCellRef: skipInsertCellRef});
		}, 50);
	},
	/**
	 * Sets the cell's selection area and display it
	 * 
	 * @param int left column start index
	 * @param int top row start index
	 * @param int right column end index
	 * @param int bottom row end index
	 * @param boolean snap whether snap to merge cell border
	 * @param boolean not trigger DOM Element focus event
	 */
	moveCellSelection: function (left, top, right, bottom, snap, noTrigger) {
		var lastRange = this.selArea.lastRange;
		if (lastRange)
			this._updateHeaderSelectionCss(lastRange, true);

		var show = !(this.state == zss.SSheetCtrl.NOFOCUS);
		if (snap) {
			var maxr = right,
				minl = left;
	
			//Selection shall snap to merge area
			for (var r = bottom; r >= top; --r) {
				var cellR = this.getCell(r, maxr);
				if (cellR && cellR.merr > maxr) maxr = cellR.merr;
				var cellL = this.getCell(r, minl);
				if (cellL && cellL.merl < minl) minl = cellL.merl;
			}
			right = maxr;
			left = minl;
			
			var maxb = bottom,
				mint = top;
			for (var c = maxr; c >= minl; --c) {
				var cellB = this.getCell(maxb, c);
				if (cellB && cellB.merb > maxb) maxb = cellB.merb;
				var cellT = this.getCell(mint, c);
				if (cellT && cellT.mert < mint) mint = cellT.mert;
			}
			bottom = maxb;
			top = mint;
		} else {
			var cell = this.getCell(top, left);
			//only show merged selection when selecing on same row
			if (top == bottom && cell) {
				if (cell.merr > right)
					right = cell.merr;
				if (cell.merb > bottom)
					bottom = cell.merb;
			}
		}

		if (!this.isRangeSelectable(left, top, right, bottom))
			return;

		var selRange = new zss.Range(left, top, right, bottom);
		this.state == zss.SSheetCtrl.START_EDIT ? 
				this.fire('onCellSelection', {left: left, top: top, right: right, bottom: bottom}) : this.deferFireCellSelection(left, top, right, bottom);
		this.selArea.relocate(selRange);
		
		if (show) {
			this._updateHeaderSelectionCss(selRange,false);
			this.selArea.showArea();
		}
		
		if (this.tp.selArea) {
			this.tp.selArea.relocate(selRange);
			if(show) this.tp.selArea.showArea();
		}
		if (this.lp.selArea) {
			this.lp.selArea.relocate(selRange);
			if(show) this.lp.selArea.showArea();
		}
		if (this.cp.selArea) {
			this.cp.selArea.relocate(selRange);
			if(show) this.cp.selArea.showArea();
		}

		if (!noTrigger)
			this._prepareCopy(); //feature #26: Support copy/paste value to local Excel
	},
	/**
	 * Check if the range is selectable
	 */
	isRangeSelectable: function (left, top, right, bottom) {
		var wgt = this._wgt;
		if (!wgt.isProtect() || wgt.allowSelectLockedCells)
			return true;

		if (!wgt.allowSelectUnlockedCells)
			return false;

		for (var i = top; i <= bottom; i++)
			for (var j = left; j <= right; j++) {
			if (this.isCellLocked(i, j))
				return false;
		}
		return true;
	},
	/**
	 * Returns whether the header is selectable
	 * @param int index header index
	 * @param string type header type
	 */
	isHeaderSelectable: function (index, type) {
		var wgt = this._wgt;

		if (!wgt.isProtect() || wgt.allowSelectLockedCells)
			return true;

		if (!wgt.allowSelectUnlockedCells)
			return false;

		if (!wgt.unlockInfo)
			return false;

		if (type == zss.Header.HOR) {
			if (groupContains(wgt.unlockInfo.chs, index)) {
				return true;
			}
		} else {
			var rhs = wgt.unlockInfo.rhs;
			for (var prop in rhs) {
				if (rhs[prop] == index)
					return true;
			}
		}
		return false;
	},
	/** 
	 * Returns whether the cell is locked
	 * @param int row row index
	 * @param int col column index
	 * <p>Please note that it only works when sheet is protected and
	 *    is only allowed to select unlocked cells.</p>
	 */
	isCellLocked: function (row, col) {
		var unlockInfo = this._wgt.unlockInfo;

		if (!unlockInfo)
			return true;

		for (var prop in unlockInfo.cs) {
			if (unlockInfo.cs[prop].i == row) {
				if (groupContains(unlockInfo.cs[prop].data, col)) {
					return false;
				}
				if (groupContains(unlockInfo.cs[prop].lockData, col)) {
					return true;
				}
			}
		}

		if (groupContains(unlockInfo.chs, col))
			return false;

		for (var prop in unlockInfo.rhs) {
			if (unlockInfo.rhs[prop] == row) {
				return false;
			}
		}

		return true;
	},
	//feature #26: Support copy/paste value to local Excel
	_prepareCopy: function () {
		var range =  this._wgt._cacheCtrl.getSelectedSheet(),
			ls = this.getLastSelection(),
			top = ls.top,
			btm = Math.min(ls.bottom, ls.top + zss.clientCopy.maxRowCount - 1), // ZSS-331: limited cells for copying at once. refer to globalDef.js
			left = ls.left,
			right = Math.min(ls.right, ls.left + zss.clientCopy.maxColumnCount - 1), // ZSS-331: limited cells for copying at once. refer to globalDef.js
			result = '';
		if (range) {
			var rows = range.rows;
			for(var r = top; r <= btm; ++r) {
				var row = rows[r];
				for(var c = left; c <= right; ++c) {
					var val = '';
					if (row) {
						var cell = row.cells[c];
						if (cell) {
							val = cell.formatText;
						}
					}
					result += val;
					if (c < right)
						result+='\t';
					
				}
				result += '\n';
			}
		} else {
			for(var r = top; r <= btm; ++r) {
				for(var c = left; c <= right; ++c) {
					var cell = this.getCell(r, c),
						val = !cell ? null : cell.getPureText();
					if (val != null)
						result+=val;
					if (c < right)
						result+='\t';
				}
				result+='\n';
			}
		}
		if (this.state != zss.SSheetCtrl.FOCUSED)
			return;
		
		//ZSS-903
		var inputNode = this.dp.getInputNode(); 
		jq(inputNode).val(result);
		
		this.dp.deferSelectFocustag();
	},
	/**
	 * Hides cell's selection area 
	 */
	hideCellSelection: function () {
		this.selArea.hideArea();
		if (this.tp.selArea)
			this.tp.selArea.hideArea();

		if (this.lp.selArea)
			this.lp.selArea.hideArea();

		if (this.cp.selArea)
			this.cp.selArea.hideArea();

		var lastRange = this.selArea.lastRange;
		if (lastRange)
			this._updateHeaderSelectionCss(lastRange, true);
	},
	showCellSelection: function () {
		this.selArea.showArea();
		if (this.tp.selArea)
			this.tp.selArea.showArea();

		if (this.lp.selArea)
			this.lp.selArea.showArea();

		if (this.cp.selArea)
			this.cp.selArea.showArea();

		var lastRange = this.selArea.lastRange;
		if (lastRange)
			this._updateHeaderSelectionCss(lastRange, true);
	},
	/**
	 * Sets the cell's selection change area and display it
	 * 
	 * @param int left column start index
	 * @param int top row start index
	 * @param int right column end index
	 * @param int bottom row end index
	 */
	moveSelectionChange : function (left, top, right, bottom) {	
		var selRange = new zss.Range(left, top, right, bottom);
		this.selChgArea.relocate(selRange);
		this.selChgArea.showArea();

		if (this.tp.selChgArea) {
			this.tp.selChgArea.relocate(selRange);
			this.tp.selChgArea.showArea();
		}
		if (this.lp.selChgArea) {
			this.lp.selChgArea.relocate(selRange);
			this.lp.selChgArea.showArea();
		}
		if (this.cp.selChgArea) {
			this.cp.selChgArea.relocate(selRange);
			this.cp.selChgArea.showArea();
		}
	},
	/**
	 * Hides cell's selection change area
	 */
	hideSelectionChange: function () {
		this.selChgArea.hideArea();
		if (this.tp.selChgArea)
			this.tp.selChgArea.hideArea();

		if (this.lp.selChgArea)
			this.lp.selChgArea.hideArea();

		if (this.cp.selChgArea)
			this.cp.selChgArea.hideArea();
	},
	/**
	 * Sets cell's focus mark
	 * @param int row row index
	 * @param int col column index
	 * @param boolean noEvt indicate whether shall fire onFocused or not
	 */
	moveCellFocus: function (row, col, noEvt) {
		var show = !(this.state == zss.SSheetCtrl.NOFOCUS),
			cell = this.getCell(row, col);
		if (cell && cell.merl < col) {//check if a merged cell
			col = cell.merl;
			row = cell.mert;
		}
		this.focusMark.relocate(row, col);
		if (!noEvt) {
			this.fire('onFocused', {row: row, col: col});
		}
		if (show)
			this.focusMark.showMark();
		if (this.tp.focusMark) {
			this.tp.focusMark.relocate(row, col);
			if(show) this.tp.focusMark.showMark();
		}
		if (this.lp.focusMark) {
			this.lp.focusMark.relocate(row, col);
			if(show) this.lp.focusMark.showMark();
		}
		if (this.cp.focusMark) {
			this.cp.focusMark.relocate(row,col);
			if(show) this.cp.focusMark.showMark();
		}
	},
	/**
	 * Hides cell's focus mark 
	 */
	hideCellFocus: function () {
		this.focusMark.hideMark();
		if (this.tp.focusMark)
			this.tp.focusMark.hideMark();
		
		if (this.lp.focusMark)
			this.lp.focusMark.hideMark();

		if (this.cp.focusMark)
			this.cp.focusMark.hideMark();
	},
	_realRight: function(top, bottom, right) {
		var minl = right;
		for (var r = bottom; r >= top; --r) {
			var cellL = this.getCell(r, minl);
			if (cellL && cellL.merl < minl) minl = cellL.merl;
		}
		return minl;
	},
	_realBottom: function(left, right, bottom) {
		var mint = bottom;
		for (var c = right; c >= left; --c) {
			var cellT = this.getCell(mint, c);
			if (cellT && cellT.mert < mint) mint = cellT.mert;
		}
		return mint;
	},
	_realLeft: function(top, bottom, left) {
		var maxr = left;
		for (var r = bottom; r >= top; --r) {
			var cellR = this.getCell(r, maxr);
			if (cellR && cellR.merr > maxr) maxr = cellR.merr;
		}
		return maxr;
	},
	_realTop: function(left, right, top) {
		var maxb = top;
		for (var c = right; c >= left; --c) {
			var cellB = this.getCell(maxb, c);
			if (cellB && cellB.merb > maxb) maxb = cellB.merb;
		}
		return maxb;
	},
	/**
	 * Move selection position
	 * @param int key  
	 */
	shiftSelection: function (key) {
		var ls = this.getLastSelection(),
			pos = this.getLastFocus(),
			row = pos.row,
			col = pos.column,
			left = ls.left,
			top = ls.top,
			right = ls.right,
			bottom = ls.bottom,
			update = false,
			custRowHeight = this.custRowHeight,
			custColWidth = this.custColWidth, 
			seltype = this.selType ? this.selType : zss.SEL.CELL;
		
		switch (key) {
		case 'up':
			bottom = this._realBottom(left, right, bottom);
			if (row < bottom) {
				var newbottom = custRowHeight.getDecUnhidden(bottom - 1, 0);
				if (newbottom >= 0)
					bottom = newbottom;
			} else {
				var newtop = custRowHeight.getDecUnhidden(top - 1, 0);
				if (newtop >= 0)
					top = newtop;
			}
			break;
		case 'down':
			top = this._realTop(left, right, top);
			if (row > top) {
				var newtop = custRowHeight.getIncUnhidden(top + 1, this.maxRows - 1);
				if (newtop >= 0)
					top = newtop;
			} else {
				var newbottom = custRowHeight.getIncUnhidden(bottom + 1, this.maxRows - 1);
				if (newbottom >= 0)
					bottom = newbottom;
			}
			break;
		case 'left':
			right = this._realRight(top, bottom, right);
			if (col < right) {
				var newright = custColWidth.getDecUnhidden(right - 1, 0);
				if (newright >= 0)
					right = newright;
			} else {
				var newleft = custColWidth.getDecUnhidden(left - 1, 0);
				if (newleft >= 0)
					left = newleft;
			}
			break;
		case 'right':
			left = this._realLeft(top, bottom, left);
			if (col > left) {
				var newleft = custColWidth.getIncUnhidden(left + 1, this.maxCols - 1);
				if (newleft >= 0)
					left = newleft;
			} else {
				var newright = custColWidth.getIncUnhidden(right + 1, this.maxCols - 1);
				if (newright >= 0)
					right = newright;
			}
			break;
		case 'home':
			right = col;
			left = custColWidth.getIncUnhidden(0, right);
			if (seltype == zss.SEL.ALL)
				seltype = zss.SEL.COL;
			else if (seltype == zss.SEL.ROW)
				seltype = zss.SEL.CELL;
			break;
		case 'end':
			left = col;
			right = custColWidth.getDecUnhidden(this.maxCols - 1, left);
			if (left == 0) {
				if (seltype == zss.SEL.COL)
					seltype = zss.SEL.ALL;
				else if (seltype == zss.SEL.CELL)
					seltype = zss.SEL.ROW;
			}
			break;
		case 'pgup':
			if (row < bottom) {
				var newbottom = bottom - this.pageKeySize;
				if (newbottom < 0)
					newbottom = 0;
				var xbottom = newbottom > 0 ? 
						custRowHeight.getDecUnhidden(newbottom, 0) : //search backward
						custRowHeight.getIncUnhidden(newbottom, bottom); //search forward
				if (xbottom < 0) //fail on search backward
					xbottom = custRowHeight.getIncUnhidden(newbottom, bottom); //search forward

				bottom = xbottom;
				if (bottom < row) {
					top = bottom;
					bottom = row;
				}
			} else {
				var newtop = top - this.pageKeySize;
				if (newtop < 0)
					newtop = 0;
				var xtop = newtop > 0 ? 
						custRowHeight.getDecUnhidden(newtop, 0) : //search backward
						custRowHeight.getIncUnhidden(newtop, top); //search forward
				if (xtop < 0) //fail on search backward
					xtop = custRowHeight.getIncUnhidden(newtop, top); //search forward

				top = xtop;
			}
			break;
		case 'pgdn':
			if (row > top) {
				var newtop = top + this.pageKeySize;
				if (newtop > this.maxRows - 1)
					newtop = this.maxRows - 1;
				var xtop = newtop < this.maxRows - 1 ? 
						custRowHeight.getIncUnhidden(newtop, this.maxRows - 1): //search downward
						custRowHeight.getDecUnhidden(newtop, top); //search upward
				if (xtop < 0) //fail on search downward
					xtop = custRowHeight.getDecUnhidden(newtop, top); //search upward

				top = xtop;
				if (top > row) {
					bottom = top;
					top = row;
				}
			} else {
				var newbottom = bottom + this.pageKeySize;
				if (newbottom > this.maxRows - 1)
					newbottom = this.maxRows - 1;
				var xbottom = newbottom < this.maxRows - 1 ? 
						custRowHeight.getIncUnhidden(newbottom, this.maxRows - 1): //search downward
						custRowHeight.getDecUnhidden(newbottom, bottom); //search upward
				if (xbottom < 0) //fail on search downward
					xbottom = custRowHeight.getDecUnhidden(newbottom, bottom); //search upward

				bottom = xbottom;
			}
			break;
			
		default:
			this.selType = seltype;
			return;
		}
		
		if (left < 0) left = 0;
		if (right >= this.maxCols) right = this.maxCols-1;
		if (top < 0) top = 0;
		if (bottom >= this.maxRows) bottom = this.maxRows-1;
		
		
		//TODO , check cell merge
		
		// ZSS-475: auto scroll
		var destRow = (bottom > row) ? bottom : top; // compare selection bottom with focus row to know current row  
		var destCol = (right > col) ? right : left;
		this.sp.scrollToVisible(destRow, destCol);
		this.activeBlock.loadCell(destRow, destCol, 5); // just load one cell, it'll load a whole rectangle block. see DataPanel.moveFocus() 
		
		if (left != ls.left || top != ls.top || right != ls.right || bottom != ls.bottom) {
			this.moveCellSelection(left, top, right, bottom, true);
			var newls = this.getLastSelection();
			if (newls.left != ls.left || newls.top != ls.top ||
				newls.right != ls.right || newls.bottom != ls.bottom) {
				this.selType = seltype;
				this._sendOnCellSelection(seltype, newls.left, newls.top, newls.right, newls.bottom);
			}
		}
	},
	/**
	 * Sets the highlight area
	 * @param int left
	 * @param int top
	 * @param int right
	 * @param int bottom
	 */
	moveHighlight: function (left, top, right, bottom) {
		
		//1995691 Highlight doesn't showup after invalidate
		var show = this.hlArea.show;
		
		if (left < 0 || top < 0 || right < 0 || bottom < 0) {
			this.hideHighlight();
			return;
		}
		var hlRange = new zss.Range(left, top, right, bottom);
		this.hlArea.relocate(hlRange);
		
		this.hlArea.show = true;
		this.hlArea.showArea();

		if (this.tp.hlArea) {
			this.tp.hlArea.relocate(hlRange);
			if (show) 
				this.tp.hlArea.showArea();
		}
		if (this.lp.hlArea) {
			this.lp.hlArea.relocate(hlRange);
			if (show) 
				this.lp.hlArea.showArea();
		}
		if (this.cp.hlArea) {
			this.cp.hlArea.relocate(hlRange);
			if (show) 
				this.cp.hlArea.showArea();
		}
	},
	getLastHighlight: function () {
		var range = this.hlArea.lastRange;
		return !range ? null : new zss.Range(range.left, range.top, range.right, range.bottom);
	},
	isHighlightVisible: function () {
		return this.hlArea.isVisible();
	},
	/**
	 * Hides the highlight area
	 */
	hideHighlight: function(clear){
		if (clear) {
			this.hlArea.lastRange = new zss.Range(-1, -1, -1, -1);
		}
		this.hlArea.hideArea();
		if (this.tp.hlArea)
			this.tp.hlArea.hideArea();

		if (this.lp.hlArea)
			this.lp.hlArea.hideArea();

		if (this.cp.hlArea)
			this.cp.hlArea.hideArea();
	},
	/**
	 * Animate highlight area
	 * @param boolean start
	 */
	animateHighlight: function (start) {
		this.hlArea.doAnimation(start);
		if (this.tp.hlArea) {
			this.tp.hlArea.doAnimation(start);
		}

		if (this.lp.hlArea) {
			this.lp.hlArea.doAnimation(start);
		}

		if (this.cp.hlArea) {
			this.cp.hlArea.doAnimation(start);
		}
	},
	/**
	 * Display info
	 */
	showInfo: function (text, autohide) {
		this.info.setInfoText(text);
		this.info.showInfo(autohide);
	},
	/**
	 * Hides info
	 */
	hideInfo: function () {
		this.info.hideInfo();
	},
	/**
	 * Display mark
	 */
	showMask: function (show, txt) {
		jq(this.maskcmp).css('visibility', show ? 'visible' : 'hidden');
		if (txt)
			jq(this._wgt.$n('masktxt')).text(txt);
	},
	/**
	 * Returns focused cell
	 * @return zss.Cell
	 */
	getFocusedCell: function () {
		var pos = this.getLastFocus();
		return this.getCell(pos.row, pos.column);
	},
	/**
	 * Returns the cell
	 * @param int row row index
	 * @param int col column index
	 * @return zss.Cell
	 */
	getCell: function (row, col) {
		var fzr = this.frozenRow,
			fzc = this.frozenCol,
			cell = null;
		
		// get cell from one of 4 areas according to the gived location 
		if (row <= fzr && col <= fzc) { //corner
			var cp = this.cp;
			cell = cp && cp.block ? cp.block.getCell(row, col) : null
		} else if(fzr >= 0 && row <= fzr) { // ZSS-420: it should be index not number
			var tp = this.tp;
			cell = tp && tp.block ? tp.block.getCell(row, col) : null; //top panel
		} else if(fzc >= 0 && col <= fzc)  { // ZSS-420: it should be index not number
			var lp = this.lp;
			cell = lp && lp.block ? lp.block.getCell(row, col) : null; //left panel.
		} else {
			cell = this.activeBlock.getCell(row, col); //data panel
		} 
		return cell;
	},
	/**
	 * Returns rows of the specified row index (could be collections of rows in corner panel and top panel; or in left panel and data panel)
	 * @param int row row index
	 * @param int col column index (affect whether get row in left freeze panel)
	 * @return zss.Row[]
	 */
	getRow: function (row, col) {
		var fzr = this.frozenRow,
			fzc = this.frozenCol,
			rowobj = [];
		
		if (row <= fzr) {
			if (col <= fzc)
				rowobj.push(this.cp.block.getRow(row)); //corner
			rowobj.push(this.tp.block.getRow(row)); //top panel
		} else {
			if(col <= fzc) 
				rowobj.push(this.lp.block.getRow(row)); //left panel
			rowobj.push(this.activeBlock.getRow(row)); //data panel
		}
		return rowobj;
	},
	/**
	 * Sets block sync event to server
	 */
	sendSyncblock: function (now) {
		var spcmp = this.sp.comp,
			dp = this.dp,
			brange = this.activeBlock.range,
			rect = this._wgt._cacheCtrl.getSelectedSheet().rect;

		this._wgt.fire('onZSSSyncBlock', {
			sheetId: this.sheetid,
			dpWidth: dp.width,
			dpHeight: dp.height,
			viewWidth: spcmp.clientWidth,
			viewHeight: spcmp.clientHeight,
			blockLeft: brange.left,
			blockTop: brange.top,
			blockRight: brange.right,
			blockBottom: brange.bottom,
			fetchLeft: -1,
			fetchTop: -1,
			fetchWidth: -1,
			fetchHeight: -1,
			rangeLeft: rect.left,
			rangeTop: rect.top,
			rangeRight: rect.right,
			rangeBottom: rect.bottom
		}, now ? {toServer: true} : null, (now ? 25 : -1));
	},
	_insertNewColumn: function (col, size, extnm) {
		this.activeBlock.insertNewColumn(col,size);
		var fzc = this.frozenCol;
		if(col <= fzc){
			this.lp.insertNewColumn(col, size);
			this.cp.insertNewColumn(col, size, extnm);
		}
		this.tp.insertNewColumn(col, size, extnm);	
	},
	_insertNewRow: function (row, size, extnm) {
		this.activeBlock.insertNewRow(row, size);
		var fzr = this.frozenRow;
		if (row <= fzr) {
			this.tp.insertNewRow(row, size);
			this.cp.insertNewRow(row, size, extnm);
		}
		this.lp.insertNewRow(row, size, extnm);
			
	},
	_removeColumn: function (col, size, extnm) {
		this.activeBlock.removeColumn(col, size);
		var fzc = this.frozenCol;
		if (col <= fzc) {
			this.lp.removeColumn(col, size);
			this.cp.removeColumn(col, size, extnm);
		}
		this.tp.removeColumn(col, size, extnm);
	},
	_removeRow: function (row, size, extnm) {
		this.activeBlock.removeRow(row, size);
		var fzr = this.frozenRow;
		if (row <= fzr) {
			this.tp.removeRow(row, size);
			this.cp.removeRow(row, size, extnm);
		}
		this.lp.removeRow(row, size, extnm);
	},
	_removeMergeRange: function (result) {
		var id = result.id,
			left = result.left,
			top = result.top,
			right = result.right,
			bottom = result.bottom,
			sheetid = this.sheetid,
			cssid = sheetid + "-sheet" + ((zk.opera) ? "-opera" : ""),//opera bug, it cannot insert rul to special position
			name = "#" + sheetid,
			cBlock = this.cp.block,
			tBlock = this.tp.block,
			lBlock = this.lp.block;
		zcss.removeRule(name + " .zsmerge" + id, cssid);
		zcss.removeRule(name + " .zsmerge" + id + " .zscelltxt", cssid);
		
		this.mergeMatrix.removeMergeRange(id);
		this.activeBlock.removeMergeRange(id, left, top, right, bottom);

		if(cBlock)
			cBlock.removeMergeRange(id, left, top, right, bottom);
		if(tBlock)
			tBlock.removeMergeRange(id, left, top, right, bottom);
		if(lBlock)
			lBlock.removeMergeRange(id, left, top, right, bottom);
	},
	_addMergeRange: function (result) {
		var id = result.id,
			left = result.left,
			top = result.top,
			right = result.right,
			bottom = result.bottom,
			width = result.width,
			height = result.height,
			cp = this.cellPad,
			celltextwidth = width - 2 * cp,
			cellwidth = width,
			celltextheight = height,
			cellheight = height,
			wgt = this._wgt,
			cssId = wgt.getSheetCSSId(),
			name = wgt.getSelectorPrefix(),
			cBlock = this.cp.block,
			tBlock = this.tp.block,
			lBlock = this.lp.block;

		zcss.setRule(name + " .zsmerge" + id, "width", cellwidth + "px", true, cssId);
		zcss.setRule(name + " .zsmerge" + id + " .zscelltxt", "width", celltextwidth + "px", true, cssId);
		zcss.setRule(name + " .zsmerge" + id, "height", cellheight + "px", true, cssId);
		zcss.setRule(name + " .zsmerge" + id + " .zscelltxt", "height", celltextheight + "px", true, cssId);
		
		this.mergeMatrix.addMergeRange(id, left, top, right, bottom);	
		this.activeBlock.addMergeRange(id, left, top, right, bottom);
		
		if(cBlock)
			cBlock.addMergeRange(id, left, top, right, bottom);
		if(tBlock)
			tBlock.addMergeRange(id, left, top, right, bottom);
		if(lBlock)
			lBlock.addMergeRange(id, left, top, right, bottom);
		
		//ZSS-861: move focus only if current state is not NOFOCUS
		if (!(this.state == zss.SSheetCtrl.NOFOCUS)) {
			//ZSS-930
			this.moveCellSelection(left, top, left, top, false, true);
			this.moveCellFocus(top, left);
		}
	},
	afterKeyDown_: function (wevt) {
		var wgt = this._wgt; 
		wevt.target = wgt; //mimic as keydown directly sent to wgt
		return wgt.afterKeyDown_(wevt, true);
	},
	redraw: function (out) {
		var wgt = this._wgt,
			uuid = this.uuid,
			activeBlock = this.activeBlock,
			topPanel = this.tp,
			leftPanel = this.lp,
			cornerPanel = this.cp,
			hidecolhead = wgt.isColumnHeadHidden(),
			hiderowhead = wgt.isRowHeadHidden();
		out.push('<div ' + this.domAttrs_() + '><textarea id="', uuid, '-fo" class="zsfocus"></textarea>',
				'<div id="', uuid, '-mask" class="zssmask" zs.t="SMask"><div class="zssmask2"><div id="', uuid, '-masktxt" class="zssmasktxt" align="center"></div></div></div>', 
				'<div id="', uuid, '-sp" class="zsscroll" zs.t="SScrollpanel">',
				'<div id="', uuid, '-dp" class="zsdata" zs.t="SDatapanel">',
				'<div id="', uuid, '-datapad" class="zsdatapad"></div>');

		if (activeBlock)
			activeBlock.redraw(out);
		
		out.push(
				'<div id="', uuid, '-select" class="zsselect" zs.t="SSelect"><div id="', uuid, '-selecti" class="zsselecti" zs.t="SSelInner"></div><div class="zsseldot" zs.t="SSelDot"></div></div>',
				'<div id="', uuid, '-selchg" class="zsselchg" zs.t="SSelChg"><div id="', uuid, '-selchgi" class="zsselchgi"></div></div>',
				'<div id="', uuid, '-focmark" class="zsfocmark" zs.t="SFocus"><div id="', uuid, '-focmarki" class="zsfocmarki"></div></div>',
				'<div id="', uuid, '-highlight" class="zshighlight" zs.t="SHighlight"><div id="', uuid, '-highlighti" class="zshighlighti" zs.t="SHlInner"></div></div>',
				'</div><div id="', uuid, '-wp" class="zswidgetpanel" zs.t="SWidgetpanel"></div><div id="', uuid, '-pp" class="zspopuppanel"></div></div>');
		
		if (topPanel)
			topPanel.redraw(out);
		
		if (leftPanel)
			leftPanel.redraw(out);
		
		out.push(this.inlineEditor.redrawHTML_() + '<span id="', uuid, '-sinfo" class="zsscrollinfo"><span class="zsscrollinfoinner"></span></span>',
				'<span id="', uuid, '-info" class="zsinfo"><span class="zsinfoinner"></span></span>');
		
		if (cornerPanel)
			cornerPanel.redraw(out);
		
	    out.push('</div>');
	}
}, {
	NOFOCUS: 0,
	FOCUSED: 2,
	START_EDIT: 5, //2*2 + 1; //async state is odd
	EDITING: 6, //3*2 ,
	STOP_EDIT: 9, //4*2 + 1;//async state is odd
	_initInnerComp: function (sheet, row) {
		sheet.maskcmp = sheet.$n('mask');
		sheet.busycmp = sheet.$n('busy');
		sheet.spcmp = sheet.$n('sp');//scroll panel comp
		sheet.topcmp = sheet.$n('top');//top panel comp
		sheet.leftcmp = sheet.$n('left');//left panel comp
		sheet.wpcmp = sheet.$n('wp');//widget panel comp
		sheet.sinfocmp = sheet.$n('sinfo');
		sheet.infocmp = sheet.$n('info');

		sheet.dp = new zss.DataPanel(sheet);
		sheet.sp = new zss.ScrollPanel(sheet); //ScrollPanel depends DataPanel
		
		var dppadcmp = sheet.$n('datapad');
		
		var next = sheet.$n('select');
		if (next.getAttribute('zs.t') == "SSelect") {
			sheet.selareacmp = next;
			sheet.selchgcmp = sheet.$n('selchg');
			sheet.focusmarkcmp = sheet.$n('focmark');
			sheet.hlcmp = sheet.$n('highlight');
			sheet.editorcmp = sheet.$n('eb');
			
			sheet.focusMark = new zss.FocusMarkCtrl(sheet, sheet.focusmarkcmp, sheet.initparm.focus.clone());
			sheet.selArea = new zss.SelAreaCtrl(sheet, sheet.selareacmp, sheet.initparm.selrange.clone());
			sheet.selChgArea = new zss.SelChgCtrl(sheet, sheet.selchgcmp);
			sheet.hlArea = new zss.Highlight(sheet, sheet.hlcmp,sheet.initparm.hlrange.clone(), "inner");
			//sheet.inlineEditor = new zss.Editbox(sheet);
		} else {
			//error
			//zk.log('error to parse component');
		}
		
		
		//initial scroll info
		sheet.sinfo = new zss.ScrollInfo(sheet, sheet.sinfocmp);
		sheet.info = new zss.Info(sheet, sheet.infocmp);
	},
	_getVisibleRange: function (sheet) {
		var //scrollSize = zss.Spreadsheet.scrollWidth, 
			sp = sheet.sp,
			spcmp = sp.comp,
			scrollLeft = spcmp.scrollLeft,
			scrollTop = spcmp.scrollTop,
			custColWidth = sheet.custColWidth,
			custRowHeight = sheet.custRowHeight,
			frozenColWidth = custColWidth.getStartPixel(sheet.frozenCol + 1),
			frozenRowHeight = custRowHeight.getStartPixel(sheet.frozenRow + 1),
			viewWidth = spcmp.clientWidth -  sheet.leftWidth,
			viewHeight = spcmp.clientHeight - sheet.topHeight,	
			// ZSS-664: ignore frozen columns and rows
			left = custColWidth.getCellIndex(scrollLeft + frozenColWidth)[0],
			top = custRowHeight.getCellIndex(scrollTop + frozenRowHeight)[0],
			right = custColWidth.getCellIndex(scrollLeft + viewWidth)[0],
			bottom = custRowHeight.getCellIndex(scrollTop + viewHeight)[0];

		// ZSS-324: the left and top must not be out of range too 
		if (left > sheet.maxCols - 1) left = sheet.maxCols - 1;
		if (top > sheet.maxRows - 1) top = sheet.maxRows - 1; 
		if (right > sheet.maxCols - 1) right = sheet.maxCols - 1;
		if (bottom > sheet.maxRows - 1) bottom = sheet.maxRows - 1; 

		return new zss.Range(left, top, right, bottom);
	},
	/**
	 * get cell position(row,col) according to given client position (x,y) of browser and current UI display
	 * @param zss.SSheetCtrl
	 * @param int x page offset
	 * @param int y page offset
	 * @param boolean ignorefrezon don't care if over a frezon panel 
	 */
	_calCellPos: function (sheet, x, y, ignorefrezon) {
		var row = col = -1,
			dpofs = zk(sheet.dp.comp).revisedOffset(),
			custColWidth = sheet.custColWidth,
			custRowHeight = sheet.custRowHeight,
			rx = x,
			ry = y,
			fzr = sheet.frozenRow,
			fzc = sheet.frozenCol;
		
		if (!ignorefrezon && (fzr > -1 || fzc > -1)) {
			var sheetofs = zk(sheet.comp).revisedOffset(),
				fx = fy = -1;
			if (fzc > -1)
				fx = custColWidth.getStartPixel(fzc + 1);
			if (fzr > -1)
				fy = custRowHeight.getStartPixel(fzr + 1);

			rx = x - sheetofs[0] - sheet.leftWidth;
			ry = y - sheetofs[1] - sheet.topHeight;
			if (rx > fx && ry > fy) {
				rx = x - dpofs[0] - sheet.leftWidth;
				ry = y - dpofs[1] - sheet.topHeight;
			} else if (ry > fy)
				ry = y - dpofs[1] - sheet.topHeight;
			else if(rx > fx)
				rx = x - dpofs[0] - sheet.leftWidth;

		} else {
			rx = x - dpofs[0] - sheet.leftWidth;
			ry = y - dpofs[1] - sheet.topHeight;
		}
		
		var xcol = custColWidth.getCellIndex(rx),
			xrow = custRowHeight.getCellIndex(ry);
		return [xrow[0], xcol[0], rx, ry, xcol[1], xrow[1]];
	}
});
})();
