/* LeftPanel.js

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

zss.Panel = zk.$extends(zk.Widget, {
	widgetName: 'Panel',
	$o: zk.$void, //owner, fellows relationship no needed
	$init: function (sheet, hide, start, end, data, isCorner) {
		this.$supers(zss.Panel, '$init', []);
		
		this.sheet = sheet;
		this.hidehead = hide;
		this.isCorner = isCorner;
		
		this.headers = this.initHeaders_(sheet, start, end, data, isCorner);
		this.block = this.initFrozenBlock_(sheet, start, end, data);
	},
	/**
	 * Returns {@link Header}
	 */
	getHeader: function (index) {
		var headers = this.headers;
		for(var j = 0, len = headers.length; j < len; ++j)
			if (headers[j].index == index)
				return headers[j];
		return null;
	},
	/**
	 * Create zss.Header child widgets, invoke on Panel widget initialization
	 */
	initHeaders_: function (sheet, start, end, data, isCorner) {
		//to be overridden
		return null;//return header widget array
	},
	isEmptyHeader: function() {
		return !(this.headers && this.headers.length > 0);
	},
	/**
	 * Create frozen block, invoke on Panel widget initialization 
	 */
	initFrozenBlock_: function (sheet, start, end, data) {
		//to be overridden
		return null;//return zss.CellBlockCtrl if there's frozen area
	},
	createHeaders_: function (dir, start, end) {
		var sheet = this.sheet,
			headers = this.getHeaderData_(),
			isInsert = this.isInsert_(dir),
			type = this.getHeaderType_(),
			html = '',
			hs = [];
		if ('jump' == dir)
			this._clearAllHeader();
		
		for (var i = start, j = 0; i <= end; i++) {
			var h = new zss.Header(sheet, type, headers[i]);
			isInsert ? this.insertHeader_(j++, h) : this.appendHeader_(h);
		}
		this.updateSelectionCSS_();
	},
	updateSelectionCSS_: function () {
		//to be overridden
	},
	_clearAllHeader: function () {
		var hs = this.headers,
			size = hs ? hs.length : 0;
		this.removeChildFromStart_(size);
	},
	/**
	 * Returns the cached active range data source
	 */
	getHeaderData_: function () {
		//to be overridden
	},
	/**
	 * Returns frozen data
	 */
	getFrozenData_: function () {
		//to be overridden
	},
	/**
	 * Returns frozen header data
	 */
	getFrozenHeaderData_: function () {
		//to be overridden
	},
	getHeaderType_: function () {
		//to be overridden
	},
	isInsert_: function (dir) {
		//to be overridden
	},
	/**
	 * Append zss.Header widget
	 * 
	 * @param zss.Header header
	 * @param string html
	 */
	appendHeader_: function (header) {
		this.appendChild(header, true);
		jq(this.hcomp).append(header.getHtml());
		header.bind_();
		this.headers.push(header);
	},
	/**
	 * Insert zss.Header widget
	 * 
	 * @param int index
	 * @param zss.Header 
	 * @param boolean ignoreDom
	 */
	insertHeader_: function (index, header) {
		var headers = this.headers,
			sibling = headers[index];
		if (!sibling) {
			this.appendHeader_(header);
		} else {
			this.insertBefore(header, sibling, true);
			headers.splice(index, 0, header);
			var $anchor = jq(header.getHtmlEpilogHalf());
			$anchor.insertBefore(sibling.$n());
			jq(header.getHtmlPrologHalf()).insertBefore($anchor);
			header.bind_();	
		}
	},
	/**
	 * Create cells and associated headers
	 */
	create_: function (dir, headerStart, headerEnd, frozenStart, frozenEnd, createFrozenOnly) {
		//to be overridden
	},
	/**
	 * Remove child from start
	 * 
	 * @param int size 
	 */
	removeChildFromStart_: function (size) {
		if (this.hidehead)
			return;
		var headers = this.headers;
		while (size--) {
			if (!headers.length)
				return;
			headers.shift().detach();
		}
	},
	/**
	 * Remove headers from end
	 * 
	 * @param int size 
	 */
	removeChildFromEnd_: function (size) {
		if (this.hidehead) return;
		
		var headers = this.headers;
		while (size--) {
			if (!headers.length)
				return;
			headers.pop().detach();
		}
	},
	removeHeader_: function (index, size) {
		var ctrl,
			headers = this.headers;
		
		if (index > headers.length) return;

		// ZSS-488: these code can cover every situation, we don't need to consider "first index" situation separately
		var rem = headers.slice(index, index + size);
		var tail = headers.slice(index + size, headers.length);
		headers.length = index;
		headers.push.apply(headers, tail);
		
		var header = rem.pop();
		for (; header; header = rem.pop()) {
			header.detach();
		}
	},
	bind_: function () {
		this.$supers(zss.Panel, 'bind_', arguments);
		
		var n = this.comp = this.$n();
		n.ctrl = this;
		zk(n).disableSelection();//disable selectable

		this.padcomp = this.$n('pad'),
		this.icomp = this.$n('real'),
		this.hcomp = this.icomp.firstChild,
		this.wpcomp = this.$n(this.type+'wp');
		
		if (this.block) {
			this.bindFrozenCtrl_();
			
			//ZSS-1043
			var sheet = this.sheet;
			if(sheet.editorFocusMark) {
				var dummy = "";
				for (var id in sheet.editorFocusMark) {
					var focusMark = sheet.editorFocusMark[id];
					this.addEditorFocus(id, dummy);
				}
			}
		}
	},
	bindFrozenCtrl_: function () {
		//to be overridden
	},
	unbindFrozenCtrl_: function () {
		if (this.selArea) {
			this.selArea.cleanup();
			this.selArea = null;
		}
		if (this.selChgArea) {
			this.selChgArea.cleanup();
			this.selChgArea = null;
		}
		if (this.focusMark) {
			this.focusMark.cleanup();
			this.focusMark = null;
		}
		if (this.hlArea) {
			this.hlArea.cleanup();
			this.hlArea = null;
		}
	},
	unbind_: function () {
		
		if (this.headers) {
			this.headers.splice(0, this.headers.length);
		}
		
		//ZSS-1043
		if (this.editorFocusMark) {
			for (var id in this.editorFocusMark) {
				var focusMark = this.editorFocusMark[id];
				this._removeEditorFocusMark(focusMark);
			}
			this.editorFocusMark = null;
		}
		
		this.unbindFrozenCtrl_();
		this.comp = this.comp.ctrl = this.padcomp = this.wpcomp = this.icomp = 
		this.hcomp = this.headers = this.block = this.sheet = null;
		this.$supers(zss.Panel, 'unbind_', arguments);
	},
	getTypeAttr_: function () {
		//to be overridden
	},
	hasPad_: function () {
		return false;
	},
	doTooltipOver_: zk.$void,
	doTooltipOut_: zk.$void,
	doClick_: zk.$void,
	doMouseDown_: function (evt) {
		this.sheet._doMousedown(evt);
	},
	doMouseUp_: function (evt) {
		this.sheet._doMouseup(evt);
	},
	doRightClick_: function (evt) {
		this.sheet._doMouserightclick(evt);
	},
	doDoubleClick_: function (evt) {
		this.sheet._doMousedblclick(evt);
	},
	doMouseOver_: function (evt) {
		var n = evt.domTarget;
		if (n.getAttribute('zs.t') == "SBoun")
			n.parentNode.ctrlref._processDrag(true, false);
		if (n.getAttribute('zs.t') == "SBoun2")
			n.parentNode.ctrlref._processDrag(true, true);
	},
	doMouseOut_: function (evt) {
		var n = evt.domTarget;
		if (n.getAttribute('zs.t') == "SBoun")
			n.parentNode.ctrlref._processDrag(false, false);
		if (n.getAttribute('zs.t') == "SBoun2")
			n.parentNode.ctrlref._processDrag(false, true);
	},
	redraw: function (out) {
		var type = this.type,
			type = this.isCorner ? 'corner' + type : type,
			uid = this.uuid;
		out.push('<div id="', uid, '" class="zs', type, ' zsfz', type, '" zs.t="',
				this.getTypeAttr_(), '">', 
				(this.hasPad_() ? '<div id="' + uid + '-pad" class="zs' + type + 'pad"></div>' : ''),
				'<div id="', uid, '-real" class="zs', type, 
				'i"><div id="', uid, '-head" class="zs', type, 'head">');

		// ZSS-937 output DOM however. simply hiding headers for hidden state is enough. 
		var hs = this.headers,
			size = hs.length;
		for (var i = 0; i < size; i++) {
			out.push(hs[i].getHtml());
		}

		out.push('</div>'); //head div end
		if (this.block) { //frozen block
			this.block.redraw(out);
			out.push('<div id="', uid, '-select" class="zsselect" zs.t="SSelect">',
					'<div class="zsselecti" zs.t="SSelInner"></div><div class="zsseldot" zs.t="SSelDot"></div></div>',
					'<div id="', uid, '-selchg" class="zsselchg" zs.t="SSelChg"><div class="zsselchgi"></div></div>',
					'<div id="', uid, '-focmark" class="zsfocmark" zs.t="SFocus"><div class="zsfocmarki"></div></div>',
					'<div id="', uid, '-highlight" class="zshighlight" zs.t="SHighlight"><div class="zshighlighti"></div></div>');
					
		}
		out.push('</div>');
		out.push('<div id="', uid, '-',type,'wp" class="zswidgetpanel" zs.t="SWidgetpanel"></div>');
		out.push('</div>');
	},
	//ZSS-1043
	addEditorFocus : function(id, name){
		if (this.editorFocusMark && this.editorFocusMark[id])
			return;
		if (this.block) {
			var sheet = this.sheet,
				focusmarkcmp = this.$n('focmark'), 
				x = focusmarkcmp,
				div = x.cloneNode(true);
			
			div.id = div.id + '_' + id;
			div.style.borderWidth = "3px";
			x.parentNode.appendChild(div);
			this.addEditorFocus_(id, name, div);
		}
	},
	//ZSS-1043
	addEditorFocus_ : function(id, name, div) {
		//to be overridden
	},
	//ZSS-1043
	removeEditorFocus : function(id){
		if (this.block) {
			if (!this.editorFocusMark)
				return;
			var ctrl = this.editorFocusMark[id];
			this._removeEditorFocusMark(ctrl);
			this.editorFocusMark[id] = null;
		}
	},
	//ZSS-1043
	_removeEditorFocusMark : function(ctrl) {
		if (ctrl) {
			ctrl.comp.parentNode.removeChild(ctrl.comp);
			ctrl.cleanup();
		}
	},
	//ZSS-1043
	moveEditorFocus : function(id, name, color, row, col){
		if (this.block) {
			if(!this.editorFocusMark || !this.editorFocusMark[id]){
				this.addEditorFocus(id, name);
			}
			this.editorFocusMark[id].relocate(row, col);
			this.editorFocusMark[id].showMark(color, name); //new color/oldcolor with label
		}
	}
});
/**
 * LeftPanel represent the left area of the spreadsheet. It contains row headers of the spreadsheet
 */
zss.LeftPanel = zk.$extends(zss.Panel, {
	widgetName: 'LeftPanel',
	type: 'left',
	initHeaders_: function (sheet, start, end, data, isCorner) {		
		var ary = [],
			headers = data.rowHeaders,
			type = zss.Header.VER,
			leftHeaders = isCorner ? sheet.lp.headers : null,
			j = 0;
		for (var i = start; i <= end; i++) {
			var h = new zss.Header(sheet, type, headers[i], leftHeaders ? leftHeaders[j++] : null);
			this.appendChild(h, true);
			ary.push(h);
		}
		return ary;
	},
	/**
	 * Create cells and associated headers
	 */
	create_: function (dir, rowStart, rowEnd, frozenColStart, forzenColEnd, createFrozenOnly) {
		if (!createFrozenOnly)
			this.createHeaders_(dir, rowStart, rowEnd);
		
		var createFrozen = frozenColStart >= 0 && forzenColEnd >= 0;
		if ('jump' == dir && createFrozen) {
			var oldBlock = this.block;
			this.block = new zss.CellBlockCtrl(this.sheet, rowStart, frozenColStart, rowEnd, forzenColEnd, this.getFrozenData_(), 'left'); // ZSS-404: fixed missed sheet
			oldBlock ? oldBlock.replaceWidget(this.block) : this.appendChild(this.block);
		} else if (this.block && createFrozen) {
			this.block.create_(dir, rowStart, frozenColStart, rowEnd, forzenColEnd, this.getFrozenData_());
		}
	},
	initFrozenBlock_: function (sheet, tRow, bRow, data) {
		var c = sheet.frozenCol;
		if (c > -1) {
			var b = new zss.CellBlockCtrl(sheet, tRow, 0, bRow, c, data, 'left');
			this.appendChild(b, true);
			return b;
		}
		return null;
	},
	getHeaderData_: function () {
		// ZSS-404: if this is for corner panel's header, the header data must be fetched from top freeze panel
		if(this.isCorner) {
			return this.sheet._wgt._cacheCtrl.getSelectedSheet().topFrozen.rowHeaders;
		} else {
			return this.sheet._wgt._cacheCtrl.getSelectedSheet().rowHeaders;
		}
	},
	getFrozenData_: function () {
		var a = this.sheet._wgt._cacheCtrl.getSelectedSheet(),
			f = a.leftFrozen;
		return f ? f : a;
	},
	getFrozenHeaderData_: function () {
		var leftFrozen = this.getFrozenData_();
		return leftFrozen ? leftFrozen.columnHeaders : null;
	},
	getHeaderType_: function () {
		return zss.Header.VER;
	},
	isInsert_: function (dir) {
		return 'north' == dir; 
	},
	bindFrozenCtrl_: function () {
		var sheet = this.sheet,
			selArea = this.$n('select'),
			selChg = selArea.nextSibling,
			focus = selChg.nextSibling,
			highlight = focus.nextSibling;
		
		this.selArea = new zss.SelAreaCtrlLeft(sheet, selArea, sheet.initparm.selrange.clone());
		this.selChgArea = new zss.SelChgCtrlLeft(sheet, selChg);
		this.focusMark = new zss.FocusMarkCtrlLeft(sheet, focus, sheet.initparm.focus.clone());
		this.hlArea = new zss.HighlightLeft(sheet, highlight, sheet.initparm.hlrange.clone(), "inner");
	},
	//ZSS-1043
	addEditorFocus_ : function(id, name, div) {
		if(!this.editorFocusMark)
			this.editorFocusMark = new Object();
		this.editorFocusMark[id] = new zss.FocusMarkCtrlLeft(this.sheet, div, new zss.Pos(0, 0));
	},
	getTypeAttr_: function () {
		return 'SLeftPanel';
	},
	hasPad_: function () {
		return true;
	},
	removeChildFromStart_: function (size) {
		if (this.block) {
			this.block.removeRowsFromStart_(size);
		}
		this.$supers(zss.LeftPanel, 'removeChildFromStart_', arguments);
	},
	removeChildFromEnd_: function (size) {
		if (this.block) {
			this.block.removeRowsFromEnd_(size);
		}
		this.$supers(zss.LeftPanel, 'removeChildFromEnd_', arguments);
	},
	_updateHeight: function (height) {
		jq(this.comp).css('height', jq.px0(height));
		this.height = height;
		this._updateBlockHeight();
	},	
	_updateTopPos: function (toppos) {
		jq(this.icomp).css('top', jq.px(toppos));
		jq(this.wpcomp).css('top', jq.px(toppos-this.sheet.topHeight));
		this.toppos = toppos;
		this._updateBlockHeight();
	},
	_updateTopPadding: function (toppad) {
		if (this.toppad == toppad) return;
		jq(this.padcomp).css('height', jq.px0(toppad));
		this.toppad = toppad;	
		
		if (this.selArea)
			this.selArea.relocate();

		if (this.selChgArea)
			this.selChgArea.relocate();

		if (this.focusMark) {
			var pos = this.sheet.getLastFocus();
			this.focusMark.relocate(pos.row, pos.column);
		}
		this._updateBlockHeight();
	},
	_updateBlockHeight: function () {
		if (!this.block) return;
		var height = this.height,
			toppos = this.toppos,
			toppad = this.toppad;
		height = height - (toppos ? toppos : 0) - (toppad ? toppad : 0);
		if (height < 0) height = 0;
		jq(this.block.comp).css('height', jq.px0(height));
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
				jq(header.comp)[remove ? 'removeClass' : 'addClass']("zsleft-sel");
		}
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
			var top = selRange.top,
				bottom = selRange.bottom;
			this.updateSelectionCSS(top, bottom, false || sheet.state == zss.SSheetCtrl.NOFOCUS);
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
	},
	/**
	 * Sets the height position index
	 * @param row row index
	 * @param int the height position index
	 */
	appendZSH: function (row, zsh) {
		if (this.block)
			this.block.appendZSH(row, zsh);
		
		// ZSS-515: header might be empty because of removing last row/column at freeze panels
		if(this.isEmptyHeader()) {
			return;
		}
		
		var top = this.headers[0].index,
			bottom = top + this.headers.length - 1; 
		if (top > row || bottom < row) return;
		
		var index = row - top,
			header = this.headers[index];
		header.appendZSH(zsh);
	},
	/**
	 * Insert new column
	 * @param int col column
	 * @param int size the size of the column
	 */
	insertNewColumn: function (col, size) {
		if (this.block)
			this.block.insertNewColumn(col, size);
	},
	/**
	 * Insert new row
	 * @param int row row index
	 * @param int size the size of the row
	 * @param array extnm
	 */
	insertNewRow: function (row, size, extnm) {
		if (this.block)
			this.block.insertNewRow(row, size);

		// ZSS-515: header might be empty because of removing last row/column at freeze panels
		if(this.isEmptyHeader()) {
			return;
		}

		var top = this.headers[0].index,
			bottom = top + this.headers.length - 1; 
		if (row > (bottom + 1) || row < top) return;
		
		var index = row - top,
			ctrl,
			rowHeaders = this.getHeaderData_(),
			type = zss.Header.VER;
		
		//insert row intersect with selection range, must remove selection CSS before insert cells
		var sheet = this.sheet,
			selRange = sheet.selArea.lastRange;
		if (sheet.state != zss.SSheetCtrl.NOFOCUS && selRange) {
			var top = selRange.top,
				bottom = selRange.bottom;
			if (row <= bottom && (row+size-1) >= top)
				this.updateSelectionCSS(top, bottom, true);
		}
			
		for (var i = 0; i < size; i++) {
			var r = row + i;
			ctrl = new zss.Header(sheet, type, rowHeaders[r]);
			this.insertHeader_(index + i, ctrl);
		}
		extnm = extnm.slice(size, extnm.length);
		this.shiftHeaderInfo(index + size, row + size, extnm);
	},
	/**
	 * Shift header
	 * @param int index start shift index
	 * @param array extnm
	 */
	shiftHeaderInfo: function (index, newrow, extnm) {
		var size = this.headers.length,
			j = 0;
		for (var i = index; i < size; i++) {
			if(!extnm[j]) zk.log("undefined header to assing>>"+(newrow+j),"always");
			this.headers[i].resetInfo(newrow + j, extnm[j]);
			j++;
		}
	},
	/**
	 * Remove column
	 * @param int col column index
	 * @param int size the size of the column
	 */
	removeColumn: function (col, size) {
		if (this.block)
			this.block.removeColumn(col, size);
	},
	/**
	 * Remove row
	 * @param int row row index
	 * @param int size
	 * @param array extnm
	 */
	removeRow: function (row, size, extnm) {
		if (this.block)
			this.block.removeRow(row, size);

		// ZSS-515: header might be empty because of removing last row/column at freeze panels
		if(this.isEmptyHeader()) {
			return;
		}

		var top = this.headers[0].index,
			bottom = top + this.headers.length - 1; 
		if(row > (bottom+1) || row < top) return;
		
		var index = row - top;
		if ((row + size) > bottom)
			size = bottom - row + 1;

		this.removeHeader_(index, size);
		this.shiftHeaderInfo(index, row, extnm);
	},
	_fixSize: function() {
		var sheet = this.sheet,
			wgt = sheet._wgt,
			lw = sheet.leftWidth,
			leftw = lw-1,
			fzc = sheet.frozenCol,
			name = wgt.getSelectorPrefix(),
			sid = wgt.getSheetCSSId();
		
		if (fzc > -1)
			leftw = leftw + sheet.custColWidth.getStartPixel(fzc + 1);

		zcss.setRule(name + " .zsleft", ["width"], [(fzc > -1 ? leftw - 1 : leftw) + "px"], true, sid);
		zcss.setRule(name + " .zslefti", ["width"], [leftw + "px"], true, sid);
	}
});
})();