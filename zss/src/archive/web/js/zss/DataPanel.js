/* DataPanel.js

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

/**
 * A DataPanel represent the spreadsheet cells 
 */
zss.DataPanel = zk.$extends(zk.Object, {
	cacheMap: {}, //handle element cache, not implement yet
	$init: function (sheet) {
		this.$supers(zss.DataPanel, '$init', []);
		var wgt = this._wgt = sheet._wgt,
			dataPanel = sheet.$n('dp'),
			//focus  = this.focustag = sheet.$n('fo');
			// ZSS-737: now we focus on inline editor's directly.
			// sheet.$n('fo') are only used when do paste
			focus = this.focustag = sheet.inlineEditor.getInputNode();

		this.id = dataPanel.id;
		this.sheet = sheet;
		this.comp = dataPanel;
		this.comp.ctrl = this;
		this.padcomp =  sheet.$n('datapad');

		wgt.domListen_(focus, 'onBlur', '_doDataPanelBlur');
		wgt.domListen_(focus, 'onFocus', '_doDataPanelFocus');

		this.width = sheet.custColWidth.getStartPixel(wgt.getMaxColumns());
		this.height = sheet.custRowHeight.getStartPixel(wgt.getMaxRows());

		this.paddingl = this.paddingt = 0;
		var pdl = this.paddingl + sheet.leftWidth,
			pdt = sheet.topHeight;//this.paddingt + sheet.topHeight;
//		jq(dataPanel).css({'padding-left': jq.px0(pdl), 'padding-top': jq.px0(pdt), 'width': jq.px0(this.width), 'height': jq.px0(this.height)});
		jq(dataPanel).css({'width': jq.px0(this.width), 'height': jq.px0(this.height)});
	},
	cleanup: function() {
		var focus = this.focustag,
			wgt = this.sheet._wgt;
		wgt.domUnlisten_(focus, 'onBlur', '_doDataPanelBlur');
		wgt.domUnlisten_(focus, 'onFocus', '_doDataPanelFocus');
		
		this.invalid = true;
		this.cacheMap = this.focustag = null;

		if (this.comp) this.comp.ctrl = null;
		this._wgt = this.comp = this.padcomp = this.sheet = null;
	},
	reset: function (width, height) {
		var wgt = this._wgt,
			l = this.paddingl = wgt.getLeftPanelWidth(),
			t = this.paddingt = wgt.getTopPanelHeight() + 1; //ZSS-948: +1 to avoid heading's bottom border
		this.width = width;
		this.height = height;
		jq(this.comp).css({'padding-left': jq.px0(l), 'padding-top': jq.px0(t),'width': jq.px0(width), 'height': jq.px0(height)});
	},
	/**
	 * Sets the height
	 * @param int diff
	 */
	updateHeight: function (diff) {
		this.height += diff;
		jq(this.comp).css('height', jq.px0(this.height));
	},
	/**
	 * Sets the width
	 * @param int diff
	 */
	updateWidth: function (diff) {
		this.width += diff;
		jq(this.comp).css('width', jq.px0(this.width));
	},
	_fixSize: function (block) {
		var self = this,
			wgt = this._wgt,
			sheet = this.sheet,
			custColWidth = sheet.custColWidth,
			custRowHeight = sheet.custRowHeight;

		// ZSS-324: remove timeout, function should be executed immediately
		// column/row header must be update their size after data panel updated 
		self.width = sheet.custColWidth.getStartPixel(wgt.getMaxColumns());
		self.height = sheet.custRowHeight.getStartPixel(wgt.getMaxRows());
		self.paddingl = custColWidth.getStartPixel(block.range.left);
		self.paddingt = custRowHeight.getStartPixel(block.range.top);
		
		var width = self.width - self.paddingl,
			height = self.height,
			pdl = self.paddingl + sheet.leftWidth,
			pdt = sheet.topHeight + 1; //ZSS-948: +1 to avoid heading's bottom border
		
		jq(self.comp).css({'padding-left': jq.px0(pdl), 'padding-top': jq.px0(pdt), 'width': jq.px0(width)});
		jq(self.comp).css({'width': jq.px0(width)});
		jq(self.comp).css({'height': jq.px0(height)}); // ZSS-324: height must be adjusted too
		jq(self.padcomp).css('height', jq.px0(self.paddingt));
		sheet.tp._updateLeftPadding(pdl);
		sheet.lp._updateTopPadding(self.paddingt);
	},
	/**
	 * Returns whether is focus on frozen area or not.
	 * @return boolean
	 */
	isFocusOnFrozen: function () {
		var sheet = this.sheet,
			fzr = sheet.frozenRow,
			fzc = sheet.frozenCol,
			pos = sheet.getLastFocus();

		return fzc > -1 && pos.column <= fzc
			|| fzr > -1 && pos.row <= fzr;
	},
	/**
	 * Start editing on cell
	 * 
	 * @param evt
	 * @param val
	 * @param type editing type. Either 'inlineEditing' or 'formulabarEditing'
	 * @param pos optional, specify the row & column index of a cell to start editing 
	 */
	startEditing: function (evt, val, type, pos) {
		var sheet = this.sheet;
		if (sheet.config.readonly) 
			return false;
		
		var row, col;
		if (typeof pos != 'undefined'){
			row = pos.row;
			col = pos.col;
		}else{ 
			row = sheet.getLastFocus().row,
			col = sheet.getLastFocus().column;
		}
		var type = type || 'inlineEditing',
			cell = sheet.getCell(row, col);
		if (this._wgt.isProtect() && cell && cell.isLocked()) {
			sheet.showInfo(msgzss.cannotEditProtected, true);
			return false;
		}
		
		/* ZSS-145: allow edit on frozen cell
		if (this.isFocusOnFrozen()) {
			sheet.showInfo("Can not edit on a frozen cell.", true);
			return false;	
		}
		 */
		if (!val) val = null;
		sheet.state = zss.SSheetCtrl.START_EDIT;
		
		//bug, if I use loadCell and didn't add a call back function to scroll to loaded block
		//when after loadCell, it always invoke loadForVisible , then UI will invoke a loadForVisible 
		//depends on current UI range. so UI moveFocus instead.
		//sheet.activeBlock.loadCell(row,col,5,null);
		if ('inlineEditing' == type) {
			sheet.dp.moveFocus(row, col, true, true);
		} else { //open inline editor without focus	
			this._openEditbox(null, true); 
		}
		
		sheet.fire('onStartEditing', {row: row, col: col, value: val, type: type});
		sheet._wgt.fire('onStartEditing',
				{token: "", sheetId: sheet.serverSheetId, row: row, col: col, clienttxt: val, type: type}, null, 25);
		return true;
	},
	_isProtect: function (tRow, lCol, bRow, rCol) {
		var shtProtect = this._wgt.isProtect(),
			data = this._wgt._cacheCtrl.getSelectedSheet();
		for (var r = tRow; r <= bRow; r++) {
			for (var c = lCol; c <= rCol; c++) {
				var cell = data.getRow(r).getCell(c);
				if (shtProtect && cell && cell.lock) {
					return true;
				}
			}
		}
		return false;
	},
	//feature#161: Support copy&paste from clipboard to a cell
	_speedCopy: function (value, type) { 
		var sheet = this.sheet,
			pos = sheet.getLastFocus(),
			top = pos.row,
			left = pos.column,
			ci = left,
			right = left,
			rows = value.split('\n'),
			rlen = rows.length,
			bottom = top + rlen - 1,
			vals = [],
			val = null,
			type = type || 'inlineEditing';
		for(var r = 0; r < rlen; ++r) {
			var row = rows[r],
				cols = row.split('\t'),
				clen = cols.length,
				rmax = left + clen - 1;
			if (right < rmax)
				right = rmax;
			vals.push(cols);
		}
		var lastcols = vals[rlen-1]; 
		if (lastcols.length == 1 && lastcols[0].length == 0) { //skip the last empty row
			--rlen;
			if (rlen > 0)
				--bottom;
		}
		if (this._isProtect(top, left, bottom, right)) {
			sheet.showInfo(msgzss.cannotEditProtected, true);
			sheet._doCellSelection(left, top, right, bottom);
			return;
		}
		var clenmax = right - left + 1;
		for(var r = 0; r < rlen; ++r) {
			var cols = vals[r],
				clen = cols.length,
				ri = top+r;
			for(var c = 0; c < clenmax; ++c) {
				ci = left + c;
				val = c >= clen ? null : cols[c];
				if (sheet.state != zss.SSheetCtrl.START_EDIT) {
					sheet.state = zss.SSheetCtrl.START_EDIT;
					sheet._wgt.fire('onStartEditing',
							{token: "", sheetId: sheet.serverSheetId, row: ri, col: ci, clienttxt: val, type: type}, null, 25);
				}
				sheet.state = zss.SSheetCtrl.FOCUSED;
				sheet._wgt.fire('onStopEditing',
						{token: "", sheetId: sheet.serverSheetId, row: ri, col: ci, value: val, type: type}, {toServer: true}, 25);
			}
		}
		return {left:left,top:top,right:right,bottom:bottom};
	},
	//bug #117 Barcode Scanner data incomplete
	_speedInput: function (value, type) {
		var sheet = this.sheet,
			pos = sheet.getLastFocus(),
			top = pos.row,
			left = pos.column,
			ri = top,
			ci = left,
			rows = value.split(zkS._enterChar),
			val = '',
			type = type || 'inlineEditing';
		for(var r = 0, rlen = rows.length; r < rlen; ++r) {
			var row = rows[r],
				cols = row.split('\t');
			ri = top+r;
			ci = left;
			for(var c = 0, clen = cols.length; c < clen; ++c) {
				ci = left + c;
				val = cols[c];
				if (r == (rlen-1) && c == (clen-1)) //skip the last one
					break;
				if (sheet.state != zss.SSheetCtrl.START_EDIT) {
					sheet.state = zss.SSheetCtrl.START_EDIT;
					sheet._wgt.fire('onStartEditing',
							{token: "", sheetId: sheet.serverSheetId, row: ri, col: ci, clienttxt: val, type: type}, null, 25);
				}
				sheet.state = zss.SSheetCtrl.FOCUSED;
				sheet._wgt.fire('onStopEditing',
						{token: "", sheetId: sheet.serverSheetId, row: ri, col: ci, value: val, type: type}, {toServer: true}, 25);
			}
		}
		if (left != ci || top != ri) //shall move focus
			this.moveFocus(ri, ci, true, true);
		if (ci != left || (val && val.length > 0)) {
			if (sheet.state != zss.SSheetCtrl.START_EDIT) {
				sheet.state = zss.SSheetCtrl.START_EDIT;
				// ZSS-520 not enough argument, missing type.
				sheet._wgt.fire('onStartEditing',
						{token: "", sheetId: sheet.serverSheetId, row: ri, col: ci, clienttxt: val != null ? val : '', type: type}, null, 25);
			}
			this._openEditbox(val);
		}
	},
	/**
	 * Start editing using inline editor
	 * 
	 * @param value to start editing
	 * @param server boolean whether the value come from server
	 */
	_startEditing: function (value, server) {
		var sheet = this.sheet,
			cell = sheet.getFocusedCell();

		//bug #117 Barcode Scanner data incomplete
		if (cell != null) {
			var editor = sheet.inlineEditor;
			if(sheet.state == zss.SSheetCtrl.START_EDIT) {
				if (!server)
					value = sheet._clienttxt;
				sheet._clienttxt = '';
				this._speedInput(value);
				editor._startEditing();
			} else if (server && sheet.state == zss.SSheetCtrl.EDITING && editor.comp.value != value) {
				var p = sheet.getLastFocus();
				editor.edit(cell.comp, p.row, p.column, value);
			}	
		}
	},
	/**
	 * Retry editing using inline editor
	 */
	retryEditing: function (val, row, column) {
		this.startEditing(null, val, 'inlineEditing', {row:row, col:column});
		this._openEditbox(val);
	},
	/**
	 * Open inline editor
	 * 
	 * @param string initval the value of editor
	 * @param boolean noFocus whether focus on inline editor or not
	 */
	_openEditbox: function (initval, noFocus) {
		var sheet = this.sheet,
			cell = sheet.getFocusedCell();
			
		if (cell != null) {
			if( sheet.state == zss.SSheetCtrl.START_EDIT) {
				var editor = sheet.inlineEditor,
					pos = sheet.getLastFocus(),
					value = initval || cell.edit;
				editor.edit(cell.comp, pos.row, pos.column, value ? value : '', noFocus);
				sheet.state = zss.SSheetCtrl.EDITING;
			}
		}
	},
	/**
	 * Returns the editor. Default returns {@link zss.Editbox} 
	 * 
	 * @param string editing type. Either 'inlineEditing' or 'formulabarEditing'.
	 */
	getEditor: function (type) {
		var sheet = this.sheet;
		if (type) {
			return type == 'formulabarEditing' ? sheet.formulabarEditor : sheet.inlineEditor;
		} else {
			var inlineEditor = sheet.inlineEditor;
			if (inlineEditor.isEditing() || !sheet.formulabarEditor) {
				return inlineEditor;
			}
			return sheet.formulabarEditor;
		}
	},
	/**
	 * Cancel editing on cell
	 */
	cancelEditing: function (type) {
		var sheet = this.sheet;
		if (sheet.state >= zss.SSheetCtrl.FOCUSED) {
			this.getEditor(type).cancel();
			sheet.fire('onStopEditing'); //hide formulabar's button
			sheet.state = zss.SSheetCtrl.FOCUSED;
			this.reFocus(true);
		}
	},
	/**
	 * Stop editing on cell
	 * 
	 * @param string type the action type (move cell focus, lost cell focus etc..) after stop editing
	 */
	stopEditing: function (type) {
		var sheet = this.sheet,
			inlineEditing = sheet.inlineEditor.isEditing(),
			editingType = inlineEditing ? 'inlineEditing' : 'formulabarEditing';
		if (sheet.state == zss.SSheetCtrl.EDITING) {

			sheet.state = zss.SSheetCtrl.STOP_EDIT;

			var data = this._wgt._cacheCtrl.getSelectedSheet(),
				editor = inlineEditing ? this.sheet.inlineEditor : this.sheet.formulabarEditor,
				value = editor.getValue(),
				sheetId = editor.sheetId,
				row = editor.row, 
				col = editor.col, 
				cell = sheet.getCell(row, col),
				edit = cell ? cell.edit : null;

			if (sheetId && sheetId != sheet.serverSheetId) {
				var sheetBar = sheet._wgt._sheetBar,
					sheetSelector = sheetBar ? sheetBar.getSheetSelector(): null;
				if (sheetSelector) {
					sheetSelector.doSelectSheet(sheetId);
				}
			}

			//ZSS-1046: enforce formula check if a String but looks like a formula
			if (edit != value || (edit && edit.charAt(0) == '=' && cell && cell.cellType == 1 && edit.length > 1)) { //has to send back to server
				var token = "";
				if (type == "movedown") {//move down after aysnchronized call back
					token = zkS.addCallback(function(){
						if (sheet.invalid) return;
						sheet.state = zss.SSheetCtrl.FOCUSED;
						if (!sheet._skipMove) //@see Spreadsheet.js#doUpdate()
							sheet.dp.moveDown(null, row, col);
					});
				} else if (type == "moveright") {//move right after aysnchronized call back
					token = zkS.addCallback(function(){
						if (sheet.invalid) return;
						sheet.state = zss.SSheetCtrl.FOCUSED;
						if (!sheet._skipMove) //@see Spreadsheet.js#doUpdate()
							sheet.dp.moveRight(null, row, col);
					});
				} else if (type == "moveleft") {//move right after aysnchronized call back
					token = zkS.addCallback (function(){
						if (sheet.invalid) return;
						sheet.state = zss.SSheetCtrl.FOCUSED;
						if (!sheet._skipMove) //@see Spreadsheet.js#doUpdate()
							sheet.dp.moveLeft(null, row, col);
					});
				} else if (type == "refocus") {//refocuse after aysnchronized call back
					token = zkS.addCallback(function(){
						if (sheet.invalid) return;
						sheet.state = zss.SSheetCtrl.FOCUSED;
						sheet.dp.reFocus(true);
					});
				} else if (type == "lostfocus") {//lost focus after aysnchronized call back
					//ZSS-1080, 20150717, henrichen: in Firefox when click another sheet tab
					// after entering a half-baked formula, Firefox will continue to trigger 
					// SheetTab.doClick_() while Chrome will NOT(an unknown myth; maybe
					// it is done in ZK?). However, it caused the issue of ZSS-1080.
					// What we do is to prohibit Firefox from calling doClick_() in SheetTab
					// and avoid sheet tab selection being changed; then after server done
					// the formula checking and then call the doClick_() accordingly.
					sheet.asyncCheckFormula = true; //ZSS-1080: do formula checking on Server after lost focus
					token = zkS.addCallback(function(){
						sheet.asyncCheckFormula = false; //ZSS-1080: done the formula checking
						if (!sheet.invalid) {
							var osheetState = sheet.state; //ZSS-1080
							sheet.dp._doFocusLost();
							
							//ZSS-1080: until after formula check is done; then 
							// call back to doClick_ on SheetTab(@see Sheetbar.js)
							// if it is a valid formula
							if (sheet.sheetTabFn && osheetState == zss.SSheetCtrl.STOP_EDIT) {
								sheet.sheetTabFn();
							}
						}
						sheet.sheetTabFn = null; //ZSS-1080: clean up sheetTabFn
					});
				}
				
				sheet.fire('onStopEditing', {row: row, col: col, value: value});
				sheet._wgt.fire('onStopEditing', 
						{token: token, sheetId: sheet.serverSheetId, row: row, col: col, value: value, type: editingType}, {toServer: true}, 25);
			} else {
				sheet.fire('onStopEditing', {row: row, col: col, value: value});
				this._stopEditing();
				if (type == "movedown") {//move down
					if (sheet.invalid) return;
					sheet.state = zss.SSheetCtrl.FOCUSED;
					this.moveDown();
				} else if (type == "moveright") {//move right
					if (sheet.invalid) return;
					sheet.state = zss.SSheetCtrl.FOCUSED;
					this.moveRight();
				} else if (type == "moveleft") {//move right
					if (sheet.invalid) return;
					sheet.state = zss.SSheetCtrl.FOCUSED;
					this.moveLeft();
				} else if (type == "refocus") {//refocuse
					if (sheet.invalid) return;
					sheet.state = zss.SSheetCtrl.FOCUSED;
					this.reFocus(true);
				} else if (type == "lostfocus") {//lost focus
					if (sheet.invalid) return;
					this._doFocusLost();
				}
			}
		}
	},
	/**
	 * Stop inline editing
	 */
	_stopEditing: function () {
		var sheet = this.sheet;
		if (sheet.state == zss.SSheetCtrl.STOP_EDIT) {
			sheet.inlineEditor.stop();
		}
	},
	/* move focus to a cell, this method will do - 
	* 1. stop editing, 
	* 2. check if the cell loaded already or not
	* 3. if not loaded, it will cause a asynchronized loading. after loading then do 4.
	* 4. if loaded, then invoke _moveFocus to move to loaded cell
	* 
	* @param {int} row
	* @param {int} col
	* @param {bollean} scroll scroll the cell in view after loading
	* @param {boolean} selection move selection to focus cell, (the selection will change)
	* @param {boolean} noevt don't send onCellFocus Event to server side
	* @param {boolean} noslevt don't send onCellSelection Event to server side
	* @param {boolean} forceStopEditing force stop editing mode
	*/
	moveFocus: function(row, col, scroll, selection, noevt, noslevt, forceStopEditing) { //ZSS-1098
		var sheet = this.sheet,
			lastFocus = sheet.getLastFocus();

		if (sheet && !sheet.editingFormulaInfo) {
			// ZSS-370: don't stop editing if target position is same as current focus cell 
			if(lastFocus.row != row || lastFocus.column != col || forceStopEditing) { //ZSS-1098
				this.stopEditing("refocus");
			}
		} else { //when editing formula, always set focus back to editor
			
			//var sf = this;
			//setTimeout(function () {
				var info = sheet.editingFormulaInfo;
				if (info) {
					info.moveCell = true;
					//sf.getEditor(info.type).focus();
					// ZSS-674: use flag instead of setTimeout
					sheet.shallIgnoreBlur = true;
				}
			//});
		}
		var fzr = sheet.frozenRow,
			fzc = sheet.frozenCol,
			local = this,
			fn = function () {
				//zss-219, if the cell dom is not exsited, scroll to it first, and then focus it. 
				if(!sheet.getCell(row, col)){
					sheet.sp.scrollToVisible(row,col);
					setTimeout(function(){
						local._moveFocus(row, col, scroll, selection, noevt, noslevt);	
					},25);
				}else{
					local._moveFocus(row, col, scroll, selection, noevt, noslevt);
				}
			},
			block = sheet.activeBlock,
		//if target cell is on frozon row and col
		//then modify the load target to a loaded cell
			lr = (row <= fzr ? block.range.top : row),
			lc = (col <= fzc ? block.range.left : col),
			r = block.loadCell(lr, lc, 5, fn);
		if(r)
			fn();
	},
	/* move focus to new cell , it will check cell is initialed or not(delay init)*/
	_moveFocus: function (row, col, scroll, selection, noevt, noslevt) {
		var sheet = this.sheet,
			cell = sheet.getCell(row, col);
		
		if (!cell || !cell.isSelectable())
			return false;	

		var cellcmp = cell.comp,
			ml = cell.merl,
			mt = cell.mert;//this cell is merged by a left cell. cellcmp.ctrl.merl;
		if (zkS.t(ml) && (ml != col || mt != row))
			return this._moveFocus(mt, ml, scroll, selection, noevt, noslevt);
				
		sheet.moveCellFocus(row, col);
		if (!noevt) sheet._sendOnCellFocus(row, col); 
		if (selection) {
			sheet.moveCellSelection(col, row, col, row);
			var ls = sheet.getLastSelection(); 
			if (!noslevt) sheet._sendOnCellSelection(zss.SEL.CELL, ls.left, ls.top, ls.right, ls.bottom);
		}
		
		var sheet = this.sheet;
		this._gainFocus(true, noevt, sheet.state < zss.SSheetCtrl.FOCUSED ? false : true);
		
		if (scroll)
			sheet.sp.scrollToVisible(null, null, cell);	
		return true;
	},
	/* re-focus current cell , and stop editing*/
	reFocus: function (scroll) {
		var pos = this.sheet.getLastFocus();
		this.moveFocus(pos.row, pos.column, scroll, true);
	},
	/** gain focus to the focus tag , and stop editing
	 *  @param trigger should fire focustag.focus (if genFocus is call by a focustag listener for focus, then it is false 
	 */
	gainFocus: function (trigger) {
		if (!this.sheet.isSwitchingSheet) {
			this.stopEditing("refocus");
		}
		this._gainFocus(trigger);
	},
	/* gain focus to the focus cell*/
	_gainFocus: function (trigger, noevt, noslloc) {
		var sheet = this.sheet,
			focustag = this.focustag,
			pos = sheet.getLastFocus(),
			row = pos.row,
			col = pos.column;

		if (sheet.state == zss.SSheetCtrl.NOFOCUS && !noevt)
			sheet._sendOnCellFocus(row, col); 
		
		if (sheet.state < zss.SSheetCtrl.FOCUSED)
			sheet.state = zss.SSheetCtrl.FOCUSED;
		
		if (!noslloc) {
			sheet.moveCellFocus(row, col);
			var ls = sheet.selArea.lastRange;
			sheet.moveCellSelection(ls.left, ls.top, ls.right, ls.bottom, null, !trigger);
		}
		
		var lhl = sheet.hlArea.lastRange;
		sheet.moveHighlight(lhl.left, lhl.top, lhl.right, lhl.bottom);
		sheet.animateHighlight(true);

		if (trigger && sheet.state == zss.SSheetCtrl.FOCUSED){ 
			this.deferSelectFocustag();
		}
	},
	// ZSS-681: defer focus textarea, it's necessary if on mouse down event
	deferSelectFocustag: function () {
		var self = this;
		if (this.selectFocustagId) {
			return;
		}
		this.selectFocustagId =
			setTimeout(function () {
				self.selectFocustag();
				delete self.selectFocustagId;
			}, 0);
	},
	selectFocustag: function () {
		var inlineEditor = this.sheet.inlineEditor;
		inlineEditor.getInputNode().focus();
		//jq(this.focustag).select();
		inlineEditor.select();
	},
	// ZSS-737: it's used for pasting from clipboard as plain text.
	getInputNode: function () {
		return this.sheet.$n('fo');
	},
	selectInputNode: function () {
		var inputNode = this.getInputNode();
		inputNode.focus();
		inputNode.select();
	},
	/* process focus lost */
	_doFocusLost: function() {
		var sheet = this.sheet;
		sheet.state = zss.SSheetCtrl.NOFOCUS;
		sheet.hideCellFocus();
		if (!sheet._wgt._keepCellSelection) sheet.hideCellSelection(); //ZSS-1044
		sheet.animateHighlight(false);
		sheet._wgt.fire('onBlur');
	},
	/**
	 * Select cell
	 * @param int row the row
	 * @param int col the column
	 */
	selectCell: function(row, col, forcemove) {
		var sheet = this.sheet;
		if(sheet.maxRows <= row || sheet.maxCols <= col) return;

		if ((forcemove || sheet.state >= zss.SSheetCtrl.FOCUSED) && row >-1 && col > -1) {
			//sheet has focus move, load cell to client,and scroll the such cell
			this.moveFocus(row, col, true, true);
		} else {//user click back to the sheet, don't move the scroll
			//gain focus and reallocate mark , then show it, 
			//don't move the focus because the cell maybe doens't exist in block anymore.
			sheet.moveCellFocus(row, col);
			sheet.moveCellSelection(col, row, col, row);			
			this._gainFocus(true, false, true);
		}
	},
	/**
	 * Move page up
	 */
	movePageup: function (evt) {
		var sheet = this.sheet;
		if (zkS.isEvtKey(evt, "s")) {
			sheet.shiftSelection("pgup");
			return;
		}
		var pos = sheet.getLastFocus(),
			row = pos.row,
			col = pos.column;
		if (row < 0 || col < 0) return;
		if (row >= 0) {
			var oldTop = zss.SSheetCtrl._getVisibleRange(sheet).top,
				activeBlock = sheet.activeBlock,
				frozenRow = sheet.frozenRow,
				rowOffset = row > frozenRow ? row - oldTop: 0,
				prevrow,
				newTop;
			// ZSS-664: scroll to previous page
			sheet.sp.scrollToVisible(oldTop, -1, null, zss.SCROLL_DIR.VERTICAL, zss.SCROLL_POS.BOTTOM);
			activeBlock.loadForVisible();

			newTop = zss.SSheetCtrl._getVisibleRange(sheet).top;
			prevrow = newTop + rowOffset;
			
			// ZSS-664: align the first row to the top after scrolled to previous page
			sheet.sp.scrollToVisible(newTop, -1, null, zss.SCROLL_DIR.VERTICAL, zss.SCROLL_POS.TOP);

			// ZSS-664: do not move focus if not allow to select locked cells
			if (sheet._wgt.isProtect() && !sheet._wgt.allowSelectLockedCells) {
				return;
			}

			if (prevrow < 0)
				prevrow = 0;
			var custRowHeight = sheet.custRowHeight,
				newrow = prevrow > 0 ? 
						custRowHeight.getDecUnhidden(prevrow, 0) : //search upward
						custRowHeight.getIncUnhidden(prevrow, row); //search downward
			if (newrow < 0 && prevrow > 0) //search upward and still fail
				newrow = custRowHeight.getIncUnhidden(prevrow, row); //search downward
			row = newrow;
		}
		this.moveFocus(row, col, true, true, null, null, true); //ZSS-1098
	},
	/**
	 * Move page down
	 */
	movePagedown: function (evt) {
		var sheet = this.sheet;
		if (zkS.isEvtKey(evt,"s")){
			sheet.shiftSelection("pgdn");
			return;
		}
		var	pos = sheet.getLastFocus(),
			row = pos.row,
			col = pos.column;
		if(row < 0 || col < 0) return;
		
		if (row < sheet.maxRows - 1) {
			var oldRange = zss.SSheetCtrl._getVisibleRange(sheet),
				activeBlock = sheet.activeBlock,
				rowOffset = row > sheet.frozenRow ? row - oldRange.top : 0,
				nextrow;

			// ZSS-664: scroll to next page
			sheet.sp.scrollToVisible(oldRange.bottom, -1, null, zss.SCROLL_DIR.VERTICAL, zss.SCROLL_POS.TOP);
			activeBlock.loadForVisible();

			// ZSS-664: do not move focus if not allow to select locked cells
			if (sheet._wgt.isProtect() && !sheet._wgt.allowSelectLockedCells) {
				return;
			}
			
			nextrow = zss.SSheetCtrl._getVisibleRange(sheet).top + rowOffset;

			if (nextrow > sheet.maxRows - 1) {
				return;
			}
			var custRowHeight = sheet.custRowHeight,
				newrow = nextrow < (sheet.maxRows - 1) ? 
					custRowHeight.getIncUnhidden(nextrow, sheet.maxRows - 1): //search downward
					custRowHeight.getDecUnhidden(nextrow, row); //search upward
			if (newrow < 0 && nextrow < (sheet.maxRows -1)) //search downward and still fail
				newrow = custRowHeight.getDecUnhidden(nextrow, row); //search upward
			row = newrow;
		}
		this.moveFocus(row, col, true, true, null, null, true); //ZSS-1098
	},
	/**
	 * Move to the end column
	 */
	moveEnd: function (evt) {
		var sheet = this.sheet;
		if (zkS.isEvtKey(evt, "s")) {
			sheet.shiftSelection("end");
			return;
		}
		var	pos = sheet.getLastFocus(),
			row = pos.row,
			col = pos.column;
		if (row < 0 || col < 0) return;

		if (!sheet._wgt.isProtect() || sheet._wgt.allowSelectLockedCells) {
			var nextcol = sheet.maxCols - 1,
				custColWidth = sheet.custColWidth;
			col = custColWidth.getDecUnhidden(nextcol, nextcol > col ? col : 0); //search backward
		} else {
			var newpos = this._getShiftPos(row, col, 'end');
			//ZSS-1085: wait for server callback
			if (newpos == null) { 
				return;
			}
			row = newpos.row;
			col = newpos.col;
		}

		this.moveFocus(row, col, true, true, null, null, true); //ZSS-1098
	},
	/**
	 * Move to the first column 
	 */
	moveHome: function (evt) {
		var sheet = this.sheet;
		if (zkS.isEvtKey(evt, "s")) {
			sheet.shiftSelection("home");
			return;
		}
		var pos = sheet.getLastFocus(),
			row = pos.row,
			col = pos.column;
		if (row < 0 || col < 0) return;
		
		if (!sheet._wgt.isProtect() || sheet._wgt.allowSelectLockedCells) {
			var prevcol = 0,
				custColWidth = sheet.custColWidth;
			col = custColWidth.getIncUnhidden(prevcol, col < (sheet.maxCols - 1) ? col : (sheet.maxCols - 1)); //search forward
		} else {
			var newpos = this._getShiftPos(row, col, 'home');
			//ZSS-1085: wait for server callback
			if (newpos == null) {
				return;
			}
			row = newpos.row;
			col = newpos.col;
		}
		this.moveFocus(row, col, true, true, null, null, true); //ZSS-1098
	},
	/**
	 * Move up
	 */
	moveUp: function (evt) {
		var sheet = this.sheet,
			ctrlkey = evt && !evt.altKey && evt.ctrlKey; //ZSS-1000, ZSS-1069;
		if (zkS.isEvtKey(evt,"s")) {
			sheet.shiftSelection("up");
			return;
		}
		var pos = sheet.getLastFocus(),
			row = pos.row,
			col = pos.column;
		if (row < 0 || col < 0) return;
		
		//ZSS-1000
		if (ctrlkey) {
			this._fireCtrlArrow(sheet, row, col, "up");
			return;
		}
		if (!sheet._wgt.isProtect() || sheet._wgt.allowSelectLockedCells) {
			if (row > 0) {
				var prevrow = row - 1,
					custRowHeight = sheet.custRowHeight,
					newrow = prevrow > 0 ? 
							custRowHeight.getDecUnhidden(prevrow, 0) : //search upward
							custRowHeight.getIncUnhidden(prevrow, row); //search downward
				if (newrow >= 0)
					row = newrow;
			}
		} else {
			var newpos = this._getShiftPos(row, col, 'up');
			//ZSS-1085: wait for server callback
			if (newpos == null) { 
				return;
			}
			row = newpos.row;
			col = newpos.col;
		}
		
		this.moveFocus(row, col, true, true, null, null, true); //ZSS-1098
	},
	/**
	 * Move down
	 */
	moveDown: function(evt, r, c) {
		var sheet = this.sheet,
			ctrlkey = evt && !evt.altKey && evt.ctrlKey; //ZSS-1000, ZSS-1069;
		if (zkS.isEvtKey(evt, "s")) {
			sheet.shiftSelection("down");
			return;
		}
		var pos = sheet.getLastFocus(),
			row = r ? r : pos.row,
			col = c ? c : pos.column;
		if (row < 0 || col < 0) return;
		
		var cell = sheet.getCell(row, col);
		if (cell) {
			var mb = cell.merb;
			if (zkS.t(mb))
				row = mb;
		}

		//ZSS-1000
		if (ctrlkey) {
			this._fireCtrlArrow(sheet, row, col, "down");
			return;
		}
		if (!sheet._wgt.isProtect() || sheet._wgt.allowSelectLockedCells) {
			if (row < sheet.maxRows - 1) {
				var nextrow = row + 1,
					custRowHeight = sheet.custRowHeight,
					newrow = nextrow < (sheet.maxRows - 1) ? 
						custRowHeight.getIncUnhidden(nextrow, sheet.maxRows - 1): //search downward
						custRowHeight.getDecUnhidden(nextrow, row); //search upward
				if (newrow >= 0)
					row = newrow;
			}
		} else {
			var newpos = this._getShiftPos(row, col, 'down');
			//ZSS-1085: wait for server callback
			if (newpos == null) { 
				return;
			}
			row = newpos.row;
			col = newpos.col;
		}

		this.moveFocus(row, col, true, true, null, null, true); //ZSS-1098
	},
	/**
	 * Move to the left column
	 */
	moveLeft: function(evt, r, c) {
		var sheet = this.sheet,
			ctrlkey = evt && !evt.altKey && evt.ctrlKey; //ZSS-1000, ZSS-1069;
		if (zkS.isEvtKey(evt,"s")) {
			sheet.shiftSelection("left");
			return;
		}
		var pos = sheet.getLastFocus(),
			row = r ? r : pos.row,
			col = c ? c : pos.column;
		if (row < 0 || col < 0) return;
		//ZSS-1000
		if (ctrlkey) {
			this._fireCtrlArrow(sheet, row, col, "left");
			return;
		}
		if (!sheet._wgt.isProtect() || sheet._wgt.allowSelectLockedCells) {
			if (col > 0) {
				var prevcol = col - 1,
				custColWidth = sheet.custColWidth,
				newcol = prevcol > 0 ? 
						custColWidth.getDecUnhidden(prevcol, 0) : //search backward
						custColWidth.getIncUnhidden(prevcol, col); //search foreward
				if (newcol >= 0)
					col = newcol;
			}
		} else {
			var newpos = this._getShiftPos(row, col, 'left');
			//ZSS-1085: wait for server callback
			if (newpos == null) { 
				return;
			}
			row = newpos.row;
			col = newpos.col;
		}
		this.moveFocus(row, col, true, true, null, null, true); //ZSS-1098
	},
	/**
	 * Move to the right column 
	 */
	moveRight: function(evt, r, c) {
		var sheet = this.sheet,
			ctrlkey = evt && !evt.altKey && evt.ctrlKey; //ZSS-1000, ZSS-1069;
		if (zkS.isEvtKey(evt, "s")) {
			sheet.shiftSelection("right");
			return;
		}
		var pos = sheet.getLastFocus(),
			row = r ? r : pos.row,
			col = c ? c : pos.column;
		if(row < 0 || col < 0) return;

		var cell = sheet.getCell(row, col);
		if (cell) {
			var mr = cell.merr;
			if(zkS.t(mr))
				col = mr;
		}
		
		//ZSS-1000
		if (ctrlkey) {
			this._fireCtrlArrow(sheet, row, col, "right");
			return;
		}
		if (!sheet._wgt.isProtect() || sheet._wgt.allowSelectLockedCells) {
			if (col < sheet.maxCols - 1) {
				var nextcol = col + 1,
					custColWidth = sheet.custColWidth,
					newcol = nextcol < (sheet.maxCols - 1) ? 
						custColWidth.getIncUnhidden(nextcol, sheet.maxCols - 1): //search foreward
						custColWidth.getDecUnhidden(nextcol, col); //search backward
				if (newcol >= 0)
					col = newcol;
			}
		} else {
			var newpos = this._getShiftPos(row, col, 'right');
			//ZSS-1085: wait for server callback
			if (newpos == null) { 
				return;
			}
			row = newpos.row;
			col = newpos.col;
		}
		this.moveFocus(row, col, true, true, null, null, true); //ZSS-1098
	},
	// Get the unlock and unhidden cell's position when in sheet protection
	_getShiftPos: function(row, col, key) {
		var sheet = this.sheet,
			newPos = {row: row, col: col},
			fn = function() {return newPos};
		if (key == 'up') {
			fn = this._shiftUp;
		} else if (key == 'down') {
			fn = this._shiftDown;
		} else if (key == 'left') {
			fn = this._shiftLeft;
		} else if (key == 'right') {
			fn = this._shiftRight;
		} else if (key == 'home') {
			// 20140509, RaymondChao: move to the first column, shift right until matched.
			newPos = {row: 0, col: -1};
			fn = this._shiftRight;
		} else if (key == 'end') {
			// 20140509, RaymondChao: move to the end of the maximum column, shift left until matched.
			fn = this._shiftLeft;
			newPos = {row: sheet.maxRows - 1, col: sheet.maxCols};
		}
		var count = 1000; //ZSS-1085: client side count of trial
		do {
			//ZSS-1085: try in client side for most [count] times; then use server solution
			if (--count == 0) {
				this._fireShiftPos(sheet, row, col, key);
				return null;
			}
			newPos = fn.call(this, newPos);
		} while (this.sheet.isCellLocked(newPos.row, newPos.col) || 
			sheet.custRowHeight.isHidden(newPos.row) || sheet.custColWidth.isHidden(newPos.col));

		return newPos;
	},
	/*
	 *      ^  ^
	 *      |  |
	 *   ^  |  |
	 *   |  |  |
	 *   |  | [X]
     *   |  | 
     *   |  | 
	 */
	_shiftUp: function (pos) {
		var newpos = {row: pos.row - 1, col: pos.col},
			sheet = this.sheet;
		if (newpos.row < 0) {
			newpos.row = sheet.maxRows - 1;
			newpos.col = newpos.col > 0 ? newpos.col - 1 : sheet.maxCols - 1;
		}
		return newpos;
	},
	/*
	 *       |  |
	 *       |  |
	 *   [X] |  |
	 *    |  |  |
     *    |  |  V
     *    |  |
     *    V  V 
	 */
	_shiftDown: function (pos) {
		var newpos = {row: pos.row + 1, col: pos.col},
			sheet = this.sheet;
		if (newpos.row >= sheet.maxRows) {
			newpos.row = 0;
			newpos.col = newpos.col < sheet.maxCols - 1 ? newpos.col + 1 : 0;
		}
		return newpos;
	},
	/*
     *       <-------
	 *   <-----------
	 *   <-----[X]
	 */
	_shiftLeft: function (pos) {
		var newpos = {row: pos.row, col: pos.col - 1},
			sheet = this.sheet;
		if (newpos.col < 0) {
			newpos.col = sheet.maxCols - 1;
			newpos.row = newpos.row > 0 ? newpos.row - 1 : sheet.maxRows - 1;
		}
		return newpos;
	},
	/*
	 *      [X]----->
	 *   ----------->
	 *   ------->
	 */
	_shiftRight: function (pos) {
		var newpos = {row: pos.row, col: pos.col + 1},
			sheet = this.sheet;
		if (newpos.col >= sheet.maxCols) {
			newpos.col = 0;
			newpos.row = newpos.row < sheet.maxRows - 1 ? newpos.row + 1 : 0;
		}
		return newpos;
	},
	//ZSS-1000
	_fireCtrlArrow: function (sheet, row, col, dir) {
		sheet._wgt.fire('onZSSCtrlArrow',
				{sheetId: sheet.serverSheetId, row: row, col: col, dir: dir}, {toServer: true});
	},
	//ZSS-1085
	_fireShiftPos: function (sheet, row, col, dir) {
		sheet._wgt.fire('onZSSShiftPos',
				{sheetId: sheet.serverSheetId, row: row, col: col, dir: dir}, {toServer: true});
	}		
});
})();