/* TopPanel.js

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

/**
 * TopPanel represent the top rectangle area of the spreadsheet. It contains column headers of the spreadsheet
 */
zss.TopPanel = zk.$extends(zss.Panel, {
	widgetName: 'TopPanel',
	type: 'top',
	$o: zk.$void, //owner, fellows relationship no needed
	initHeaders_: function (sheet, start, end, data, isCorner) {
		var ary = [],
			headers = data.columnHeaders,
			type = zss.Header.HOR,
			topHeaders = isCorner ? sheet.tp.headers : null,
			j = 0;
		for (var i = start; i <= end; i++) {
			var h = new zss.Header(sheet, type, headers[i], topHeaders ? topHeaders[j++] : null);
			this.appendChild(h, true);
			ary.push(h);
		}
		return ary;
	},
	/**
	 * Create cells and associated headers
	 */
	create_: function (dir, colStart, colEnd, frozenRowStart, frozenRowEnd, createFrozenOnly) {
		if (!createFrozenOnly)
			this.createHeaders_(dir, colStart, colEnd);

		var createFrozen = frozenRowStart >= 0 && frozenRowEnd >= 0;
		if ('jump' == dir && createFrozen) {
			var oldBlock = this.block;
			this.block = new zss.CellBlockCtrl(this.sheet, frozenRowStart, colStart, frozenRowEnd, colEnd, this.getFrozenData_(), 'top'); // ZSS-404: fixed missed sheet
			oldBlock ? oldBlock.replaceWidget(this.block) : this.appendChild(this.block);
		} else if (this.block && createFrozen) {
			this.block.create_(dir, frozenRowStart, colStart, frozenRowEnd, colEnd, this.getFrozenData_());
		}
	},
	initFrozenBlock_: function (sheet, lCol, rCol, data) {
		var r = sheet.frozenRow;
		if (r > -1) {
			var b = new zss.CellBlockCtrl(sheet, 0, lCol, r, rCol, data, 'top');
			this.appendChild(b);
			return b;
		}
		return null;
	},
	getHeaderData_: function () {
		// ZSS-404: if this is for corner panel's header, the header data must be fetched from left freeze panel
		if(this.isCorner) {
			return this.sheet._wgt._cacheCtrl.getSelectedSheet().leftFrozen.columnHeaders;
		} else {
			return this.sheet._wgt._cacheCtrl.getSelectedSheet().columnHeaders;
		}
	},
	getFrozenData_: function () {
		var a = this.sheet._wgt._cacheCtrl.getSelectedSheet(),
			f = a.topFrozen;
		return f ? f : a;
	},
	getFrozenHeaderData_: function () {
		var topFrozen =  this.getFrozenData_();
		return topFrozen ? topFrozen.rowHeaders : null;
	},
	getHeaderType_: function () {
		return zss.Header.HOR;
	},
	isInsert_: function (dir) {
		return 'west' == dir;
	},
	bindFrozenCtrl_: function () {
		var sheet = this.sheet,
			selArea = this.$n('select'),
			selChg = selArea.nextSibling,
			focus = selChg.nextSibling,
			highlight = focus.nextSibling;
		
		this.selArea = new zss.SelAreaCtrlTop(sheet, selArea, sheet.initparm.selrange.clone());
		this.selChgArea = new zss.SelChgCtrlTop(sheet, selChg);
		this.focusMark = new zss.FocusMarkCtrlTop(sheet, focus, sheet.initparm.focus.clone());
		this.hlArea = new zss.HighlightTop(sheet, highlight, sheet.initparm.hlrange.clone(), "inner");
	},
	//ZSS-1043
	addEditorFocus_ : function(id, name, div) {
		if(!this.editorFocusMark)
			this.editorFocusMark = new Object();
		this.editorFocusMark[id] = new zss.FocusMarkCtrlTop(this.sheet, div, new zss.Pos(0, 0));
	},
	_getTopHeaderFontSize: function () {
		var head = this.$n('head'),
			col = head != null ? head.firstChild : null;
		if (col && col.getAttribute('zs.t') == 'STheader')
			return jq(col).css('font-size');
		return null;
	},
	bind_: function () {
		this.$supers(zss.TopPanel, 'bind_', arguments);
		var fontSize= this.isCorner && (zk.ie && zk.ie < 11) ? this._getTopHeaderFontSize() : null;
		if (fontSize)
			this.fontSize = fontSize;
	},
	unbind_: function () {
		this.$supers(zss.TopPanel, 'unbind_', arguments);
		this.fontSize = null;
	},
	getTypeAttr_: function () {
		return 'STopPanel';
	},
	removeChildFromStart_: function (size) {
		if (this.block) {
			this.block.removeColumnsFromStart_(size);
		}
		this.$supers(zss.TopPanel, 'removeChildFromStart_', arguments);
	},
	removeChildFromEnd_: function (size) {
		if (this.block) {
			this.block.removeColumnsFromEnd_(size);
		}
		this.$supers(zss.TopPanel, 'removeChildFromEnd_', arguments);
	},
	/**
	 *  IE need font size to display correctly in corner panel
	 *  @return string font size of the header , or null means this's not in corner panel
	 */
	_getCornerHeaderFontSize: function () {
		return this.fontSize;
	},
	_updateWidth: function (width) {
		jq(this.comp).css('width', jq.px0(width));
		this.width = width;	
		this._updateBlockWidth();
	},
	_updateLeftPos: function (pos) {
		jq(this.icomp).css('left', jq.px(pos));
		jq(this.wpcomp).css('left', jq.px(pos-this.sheet.leftWidth));
		this.leftpos = pos;
		this._updateBlockWidth();
	},
	_updateLeftPadding: function (leftpad) {
		leftpad = leftpad - this.sheet.leftWidth;
		jq(this.icomp).css('padding-left', jq.px0(leftpad));
		this.leftpad = leftpad;
		this._updateBlockWidth();
	},
	_updateBlockWidth: function () {
		if (!this.block) return;
		var width = this.width,
			leftpos = this.leftpos,
			leftpad = this.leftpad;
		width = width - (leftpos ? leftpos : 0) - (leftpad ? leftpad : 0);
		if (width < 0) width = 0;
		jq(this.block.comp).css('width', jq.px0(width));
	},
	/**
	 * Sets the selection range of the header
	 * @param int from row selection start index
	 * @param int to row selection end index
	 */
	updateSelectionCSS: function (from, to, remove) {
		var i = this.headers.length,
			header;
		while (i--) {
			header = this.headers[i];
			if (header.index >= from && header.index <= to)
				jq(header.comp)[remove ? 'removeClass' : 'addClass']("zstop-sel");
		}
	},
	/**
	 * Sets the width position index
	 * @param int col column index
	 * @param int zsw the width position index
	 */
	appendZSW: function (col, zsw) {
		if (this.block)
			this.block.appendZSW(col, zsw);

		// ZSS-515: header might be empty because of removing last row/column at freeze panels
		if(this.isEmptyHeader()) {
			return;
		}

		var left = this.headers[0].index,
			right = left + this.headers.length - 1;
		if (left > col || right < col) return;
		
		var index = col - left,
			header = this.headers[index];
		header.appendZSW(zsw);
	},
	/**
	 * Sets the height position index
	 * @param row row index
	 * @param int the height position index
	 */
	appendZSH: function (row, zsh) {
		if (this.block)
			this.block.appendZSH(row, zsh);
	},
	/**
	 * Insert new column
	 * @param int col column
	 * @param int size the size of the column
	 * @param array extnm
	 */
	insertNewColumn: function (col, size, extnm) {
		if (this.block)
			this.block.insertNewColumn(col, size);

		// ZSS-515: header might be empty because of removing last row/column at freeze panels
		if(this.isEmptyHeader()) {
			return;
		}

		var left = this.headers[0].index,
			right = left + this.headers.length - 1; 
		if (col > (right + 1) || col < left) return;
		
		var index = col - left,
			ctrl,
			colHeaders = this.getHeaderData_(),
			type = zss.Header.HOR,
			fontSize = (zk.ie && zk.ie < 11) ? this._getCornerHeaderFontSize() : null;
			
		//insert columns intersect with selection range, must remove selection CSS before insert cells
		var sheet = this.sheet,
			selRange = sheet.selArea.lastRange;
		if (sheet.state != zss.SSheetCtrl.NOFOCUS && selRange) {
			var left = selRange.left,
				right = selRange.right;
			if (col <= right && (col+size-1) >= left)
				this.updateSelectionCSS(left, right, true);
		}
			
		for (var i = 0; i < size; i++) {
			var c = col + i;
			ctrl = new zss.Header(sheet, type, colHeaders[c]);
			
//			if (fontSize) {
//				jq(ctrl.getHeaderNode()).css('font-size', fontSize);
//			}
			
			this.insertHeader_(index + i, ctrl);
		}
		extnm = extnm.slice(size, extnm.length);
		this.shiftHeaderInfo(index + size, col + size,extnm);
	},
	/**
	 * Insert new row
	 * @param int row row index
	 * @param int size the size of the row
	 */
	insertNewRow: function (row, size) {
		if (this.block)
			this.block.insertNewRow(row,size);
	},
	/**
	 * Shift header
	 * @param int index start shift index
	 * @param int newcol new column index
	 * @param array extnm
	 */
	shiftHeaderInfo: function (index, newcol, extnm) {
		var size = this.headers.length,
			j = 0;
		for (var i = index; i < size; i++) {
			if (!extnm[j]) zk.log("undefined header to assing>>"+(newrow+j),"always");
			this.headers[i].resetInfo(newcol + j, extnm[j]);
			j++;
		}
	},
	/**
	 * Remove column
	 * @param int col column index
	 * @param int size the size of the column
	 * @param array extnm
	 */
	removeColumn: function (col, size, extnm) {
		if (this.block)
			this.block.removeColumn(col,size);

		// ZSS-515: header might be empty because of removing last row/column at freeze panels
		if(this.isEmptyHeader()) {
			return;
		}

		var left = this.headers[0].index,
			right = left + this.headers.length -1; 
		if (col > (right+1) || col < left) return;
		
		var index = col - left;
		if ((col + size) > right)
			size = right - col + 1;
		this.removeHeader_(index, size);
		this.shiftHeaderInfo(index, col, extnm);
	},
	/**
	 * Remove row
	 * @param int row row index
	 * @param int size
	 * @param array extnm
	 */
	removeRow: function (row, size) {
		if (this.block)
			this.block.removeRow(row, size);
	},
	/**
	 * Sets the selection range of the header
	 * 
	 * Update CSS after header created (this.createHeaders_), ignore update selection when sheet CSS not ready
	 */
	updateSelectionCSS_: function () {
		var sheet = this.sheet,
			wgt = sheet._wgt,
			selRange = sheet.selArea.lastRange;
		if (!wgt.isSheetCSSReady())
			return;
		
		if (selRange) {
			var left = selRange.left,
				right = selRange.right;
			this.updateSelectionCSS(left, right, false || sheet.state == zss.SSheetCtrl.NOFOCUS);
		}
	},
	_fixSize: function() {
		var sheet = this.sheet,
			wgt = sheet._wgt,
			th = sheet.topHeight,
			toph = th,
			fzr = sheet.frozenRow,
			name = wgt.getSelectorPrefix(),
			sid = wgt.getSheetCSSId();
		
		if (fzr > -1)
			toph = toph + sheet.custRowHeight.getStartPixel(fzr + 1);
		
		zcss.setRule(name + " .zstop", ["height"], [(fzr > -1 ? toph - 1: toph) + "px"], true, sid);
		zcss.setRule(name + " .zstopi", ["height"], [toph + "px"], true, sid);
	}
});