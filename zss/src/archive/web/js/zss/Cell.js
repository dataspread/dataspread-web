/* Cell.js

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
var WRAP_TEXT_CLASS = 'zscelltxt-wrap';
var NUM_CELL = 0,
	STR_CELL = 1,
	FORMULA_CELL = 2,
	BLANK_CELL = 3,
	BOOLEAN_CELL = 4,
	ERROR_CELL = 5,
	Cell = 
zss.Cell = zk.$extends(zk.Widget, {
	widgetName: 'Cell',
	/**
	 * Row index number
	 */
	//r
	/**
	 * Column index number
	 */
	//c
	/**
	 * Cell reference address
	 */
	ref: null,
	/**
	 * The data source of the cell
	 */
	src: null,
	/**
	 * Cell text
	 */
	text: '',
	/**
	 * Whether cell is locked or not
	 * 
	 * Default: true
	 */
	lock: true,
	/**
	 * Indicate whether should invoke process overflow on cell or not.
	 * Process overflow when cell type is string, halign is left, no wrap, no merge
	 * 
	 * Currently, supports left aligned cell only
	 */
	overflow: false,
	/**
	 * Cell type
	 * 
	 * <ul>
	 * 	<li>0: Numeric Cell type</li>
	 * 	<li>1: String Cell type</li>
	 * 	<li>2: Formula Cell type</li>
	 * 	<li>3: Blank Cell type</li>
	 * 	<li>4: Boolean Cell type</li>
	 * 	<li>5: Error Cell type</li>
	 * </ul>
	 * 
	 * Default: Blank Cell type 
	 */
	cellType: 3,
	/**
	 * Horizontal alignment for the cell
	 * 
	 * <ul>
	 * 	<li>l: align left</li>
	 * 	<li>c: align center</li>
	 * 	<li>r: align right</li>
	 * </ul>
	 * 
	 * Default: align left
	 */
	halign: "l",
	/**
	 * Vertical alignment for the cell
	 * 
	 * <ul>
	 * 	<li>t: align top</li>
	 * 	<li>c: align center</li>
	 * 	<li>b: align bottom</li>
	 * </ul>
	 * 
	 * Default: align bottom
	 */
	valign: 'b',
	/**
	 * Whether the text should be wrapped
	 * 
	 * Default: false
	 */
	wrap: false,
	/**
	 * The font size in point
	 */
	fontSize: 11,
	/**
	 * The comment
	 */
	comment: null,
	/**
	 * Whether listen onRowHeightChanged event or not 
	 * Currently, use only on IE6/IE7 for vertical align
	 * 
	 * Default: false
	 */
	//_listenRowHeightChanged: false,
	/**
	 * Whether listen sheet's onProcessOverflow event or not
	 * 
	 * Default: false
	 */
	//_listenProcessOverflow: false,
	$init: function (sheet, block, row, col, src) {
		this.$supers(zss.Cell, '$init', []);
		
		this.sheet = sheet;
		this.block = block;
		this.r = row;
		this.c = col;
		this.src = src;
		
		var	cellData = src.getRow(row).getCell(col),
			colHeader = src.columnHeaders[col],
			rowHeader = src.rowHeaders[row];
		this.text = cellData.text || '';
		this.indention = cellData.indention;
		if (colHeader && rowHeader) {
			this.ref = colHeader.t + rowHeader.t;
		}
		this.edit = cellData.editText ? cellData.editText : '';
		this.hastxt = !!this.text;
		this.zsw = src.getColumnWidthId(col);
		this.zsh = src.getRowHeightId(row);
		this.lock = cellData.lock;
		this.cellType = cellData.cellType;
		
		this.halign = cellData.halign;
		this.valign = cellData.valign;
		this.rborder = cellData.rightBorder;
		if (cellData.fontSize){
			this.fontSize = cellData.fontSize;
		}

		var mId = cellData.mergeId;
		if (mId) {
			var r = cellData.merge;
			this.merid = mId;
			this.merl = r.left;
			this.merr = r.right;
			this.mert = r.top;
			this.merb = r.bottom;
			this.mergeCls = cellData.mergeCls;
		}
		this.wrap = cellData.wrap;
		this.overflow = cellData.overflow;
		// ZSS-224: a overflow options for indicating more status in bitwise format
		// refer to Spreadsheet.java -> getCellAttr()
		this.overflowOpt = cellData.overflowOpt;  
		
		this.style = cellData.style;
		this.innerStyle = cellData.innerStyle;
		this.fontStyle = cellData.fontStyle;
		
		//ZSS-849
		this.comment = cellData.comment;
		
		//ZSS-568
		if (cellData.db_tlbr)
			this.db_tlbr = cellData.db_tlbr;
		else
			delete this.db_tlbr;

		//ZSS-901
		if (cellData.af_tlbr)
			this.af_tlbr = cellData.af_tlbr;
		else
			delete this.af_tlbr;
		
		//ZSS-944
		this.rotate = cellData.rotate;
	},
	getVerticalAlign: function () {
		switch (this.valign) {
		case 'b':
			return 'verticalAlignBottom';
		case 'c':
			return 'verticalAlignMiddle';
		case 't':
			return 'verticalAlignTop';
		}
	},
	getHorizontalAlign: function () {
		switch (this.halign) {
		case 'l':
			return 'horizontalAlignLeft';
		case 'c':
			return 'horizontalAlignCenter';
		case 'r':
			return 'horizontalAlignRight';
		}
	},
	getFontName: function () {
		var fn = jq(this.getTextNode()).css('font-family');
		if(fn){
			fn = fn.replace(/'/g,"");//replace "'" in some font that has Space
		}
		return fn;
	},
	/**
	 * Return cell font size in point
	 * 
	 * @return int font size
	 */
	getFontSize: function () {
		return this.fontSize;
	},
	isFontBold: function () {
		var b = jq(this.getTextNode()).css('font-weight');
		return b && (b == '700' || b == 'bold');
	},
	isFontItalic: function () {
		return jq(this.getTextNode()).css('font-style') == 'italic';
	},
	isFontUnderline: function () {
		// ZSS-725
		var s = jq(this.$n('real')).css('text-decoration');
		return s && s.indexOf('underline') >= 0;
	},
	isFontStrikeout: function () {
		// ZSS-725
		var s = jq(this.$n('real')).css('text-decoration');
		return s && s.indexOf('line-through') >= 0;
	},
	doClick_: function (evt) {
		//do nothing. eat the event.
	},
	doRightClick_: function (evt) {
		this.sheet._doMouserightclick(evt);
	},
	doMouseDown_: function (evt) {
		this.sheet._doMousedown(evt);
	},
	doMouseUp_: function (evt) {
		this.sheet._doMouseup(evt);
	},
	doDoubleClick_: function (evt) {
		this.sheet._doMousedblclick(evt);
	},
	/**
	 * Returns whether cell is selectable or not
	 * @return boolean
	 */
	isSelectable: function () {
		var wgt = this.sheet._wgt;
		return !wgt.isProtect() || wgt.allowSelectLockedCells || 
			(wgt.allowSelectUnlockedCells && !this.isLocked());
	},
	/**
	 * Returns whether cell locked or not
	 * @return boolean
	 */
	isLocked: function () {
		return this.lock;
	},
	/**
	 * Update cell
	 */
	update_: function () {
		var r = this.r,
			c = this.c,
			data = this.src.getRow(r).getCell(c),
			format = data.formatText,
			st = this.style = data.style,
			ist = this.innerStyle = data.innerStyle,
			fontStyleChg = this.fontStyle != data.fontStyle,
			fst = this.fontStyle = data.fontStyle,
			n = this.comp,
			overflow = data.overflow,
			cellType = data.cellType,
			txt = data.text,
			txtChd = txt != this.text,
			indention = data.indention,
			indentionChd = indention != this.indention,
			cave = this.$n('cave'),
			prevWidth = cave.style.width,
			fontSize = data.fontSize,
			real = this.$n('real');
			
		//ZSS-944: 
		var wasRotate90 = this.rotate == 90 || this.rotate == -90, //ZSS-1020
			toRotate90 = data.rotate == 90 || data.rotate == -90, //ZSS-1020
			//  when rotate, wrap must be false!
			wrap0 = !toRotate90 && data.wrap,
			wrapChanged = this.wrap != wrap0,
			valignChanged = this.valign != data.valign,
			halignChanged = this.halign != data.halign,
			rotateChanged = this.rotate != data.rotate;
		
		var fontSizeChanged = false;
		if (fontSize != undefined) {
			fontSizeChanged = this.fontSize != data.fontSize
			this.fontSize = fontSize;
		}
		this.$n().style.cssText = st;
		cave.style.cssText = ist;
		
		// ZSS-865
		var orgwidth = real && real.style ? real.style.width : null,
			orgTransOrigin = real && real.style ? real.style.transformOrigin : null, //ZSS-944
			orgTrans = real && real.style ?  real.style[zk.ie && zk.ie < 10 ? '-ms-transform' : 'transform'] : null, //ZSS-1108:IE9 use -ms-transform
			orgFamily = real && real.style ? real.style.fontFamily : null;
		
		real.style.cssText = fst;
		if (orgwidth && !indentionChd && !real.style.width) {
			jq(real).css('width', orgwidth);
		}

		//ZSS-944: invalidate the cached text width
		if (txtChd || fontSizeChanged || orgFamily != real.style.fontFamily) delete this._tw;  

		// ZSS-944
		if (toRotate90) {
			if (orgTransOrigin && !real.style.transformOrigin) {
				real.style['transform-origin'] = orgTransOrigin;
			}
			//ZSS-1108: IE9 use -ms-transform
			if (orgTrans && !real.style[zk.ie && zk.ie < 10 ? '-ms-transform' : 'transform']) {
				real.style[zk.ie && zk.ie < 10 ? '-ms-transform' : 'transform'] = orgTrans;
				//cave must be always left aligned so css3 transform can handle the text properly
				cave.style['text-align'] = ''; 
			}
		}
		
		this.lock = data.lock;
		this.wrap = data.wrap;
		this.halign = data.halign;
		this.valign = data.valign;
		this.rborder = data.rightBorder;
		this.edit = data.editText;
		this.indention = data.indention;
		
		this._updateListenOverflow(overflow);
		this.setText(txt, false, wrapChanged); //when wrap changed, shall re-process overflow
		
		//ZSS-1018
		//must after setText(); since the old a link is included in txt
		var link = jq(real).children('a')[0];
		if (link && fontStyleChg) {
			link.style.cssText = fst;
		}

		if (wrapChanged) {
			if (wrap0) {
				jq(this.getTextNode()).addClass(WRAP_TEXT_CLASS);
			} else {
				jq(this.getTextNode()).removeClass(WRAP_TEXT_CLASS);
			}
		}
		
		//ZSS-568
		if (this.db_tlbr != data.db_tlbr) {
			if (this.db_tlbr) {
				jq(this.comp).removeClass(this.db_tlbr);
			}
			if (data.db_tlbr) {
				jq(this.comp).addClass(this.db_tlbr=data.db_tlbr);
			} else {
				delete this.db_tlbr;
			}
		}
		
		//ZSS-901
		if (this.af_tlbr != data.af_tlbr) {
			if (this.af_tlbr) {
				jq(this.comp).removeClass(this.af_tlbr);
			}
			if (data.af_tlbr) {
				jq(this.comp).addClass(this.af_tlbr=data.af_tlbr);
			} else {
				delete this.af_tlbr;
			}
		}
		
		if (this.overflow != overflow // overflow changed
			|| (this.overflow && (txtChd || indentionChd))) { // already overflow and text, indention changed
			var processedOverflow = false;
			if (this.overflow && !overflow) {
				this._clearOverflow();
				processedOverflow = true;
			}
			this.overflow = overflow;
			if (!processedOverflow)
				this._processOverflow();	
		}
		
		//trigger process overflow event when empty cell <-> string cell
		//overflow cells before this cell need to re-evaluate overflow
		if (this.cellType != cellType
			&& (this.cellType == STR_CELL || this.cellType == BLANK_CELL)) {//when cell type become empty, cells that before this cell have to re-process overflow
			this.sheet.triggerOverflowColumn_(this.r, this.c);
		}
		this.cellType = cellType;

		var processWrap = wrapChanged || (wrap0 && (txtChd || fontSizeChanged));
		if (this._justCopied === true){	//zss-528, when a cell is just inserted, its status is not synchronized with server, we ignore its status difference from server's.
			processWrap = false;
			delete this._justCopied;
		}
		
		//ZSS-944
		this._updateListenRotate(toRotate90);
		if (rotateChanged || valignChanged || halignChanged || fontSizeChanged || this.redoRotate
			|| (wasRotate90 && txtChd)) { //already rotate and text changed
			var processedRotate = false;
			if (wasRotate90 && !toRotate90) {
				this._clearRotate();
				processedRotate = true;
			}
			this.rotate = data.rotate;
			if (!processedRotate)
				this._processRotate();
			delete this.redoRotate; //see CellBlockCtrl.addMergeRange and CellBlockCtrl.removeMergeRange
		}

		//ZSS-958: process auto row/cell height for all cases
		if (
			txtChd ||
			fontStyleChg ||
			fontSizeChanged || 
			rotateChanged || 
			indentionChd ||
			((this.cellType == STR_CELL || this.cellType == BLANK_CELL) && !this.merid && processWrap) //ZSS-528, for wrap case
		) { 
			var newHeight = this._getTextHeight0();
			this.parent.updateAutoHeightDirty(this._txtHgh || -1, newHeight);
			this._txtHgh = newHeight;
			this.parent.processCellAutoHeight(this);
		}
	},
	//ZSS-944
	/**
	 * Set rotate attribute and register listener or unregister onProcessRotate listener base on rotate attribute (== 90 || == -90)
	 * @param boolean 
	 * @return boolean whether reset rotate attribute or not
	 */
	_updateListenRotate: function (b) {
		var curr = !!this._listenProcessRotate;
		if (curr != b) {
			this.sheet[curr ? 'unlisten' : 'listen']({onProcessRotate: this.proxy(this._onProcessRotate)});
			this._listenProcessRotate = b;
			return true;
		}
		return false;
	},
	//ZSS-944
	_clearRotate: function () {
		var real = this.$n('real')
			$real = jq(real);
		//ZSS-1108: IE9 use -ms-transform
		$real.css(zk.ie && zk.ie < 10 ? '-ms-transform' : 'transform', ''); // clear transform
		$real.css('transform-origin', ''); // clear transform-origin
	},
	//ZSS-944
	_processRotate: function () {
		// not implement in OSE
	},
	/**
	 * Set overflow attribute and register listener or unregister onProcessOverflow listener base on overflow attribute
	 * @param boolean 
	 * @return boolean whether reset overflow attribute or not
	 */
	_updateListenOverflow: function (b) {
		var curr = !!this._listenProcessOverflow;
		if (curr != b) {
			this.sheet[curr ? 'unlisten' : 'listen']({onProcessOverflow: this.proxy(this._onProcessOverflow)});
			this._listenProcessOverflow = b;
			return true;
		}
		return false;
	},
	_updateHasTxt: function (bool) {
		this.hastxt = bool;
		
		var zIdx = this.getZIndex();
		jq(this.comp).css('z-index', bool ? "" : zIdx);
	},
	/**
	 * Sets the text of the cell
	 * @param string text
	 */
	setText: function (txt) {
		if (!txt)
			txt = "";
		var oldTxt = this.getText(),
			difTxt = txt != oldTxt;
		this._updateHasTxt(txt != "");
		this._setText(txt);
	},
	/**
	 * Returns the text node height. Shall invoke this method after CSS ready
	 * 
	 * @return int height
	 */
	getTextHeight: function () {		
		var h = this._txtHgh;
		return h != undefined ? h : this._txtHgh = this._getTextHeight0();
	},
	_getTextHeight0: function () {
		return jq(this.getTextNode()).height();
	},
	_updateVerticalAlign: zk.ie6_ || zk.ie7_ ? function () {
		var	v = this.valign,
			text = this.text,
			cv = this.$n('cave');
		if (cv.style.display == 'none' || !text)
			return;
		var	$n = jq(cv),
			$t = jq(this.getTextNode());
		
		switch (v) {
		case 't':
			$t.css({'top': "0px", 'bottom': ''});
			break;
		case 'c':
			var ch = $n.height(),
				ich = $t.height();
			if (!ch || !ich)
				return;
			var	ah = (ch - ich) / 2,
				p = Math.ceil(ah * 100 / ch);
			if (p)
				$t.css({'top': p + "%", 'bottom': ''});
			break;
		case 'b':
			$t.css({'bottom': "0px", 'top': ''});	
			break;
		}
	} : zk.$void(),
	_onRowHeightChanged: function (evt) {
		if (evt.data.row == this.r)
			this._updateVerticalAlign();
	},
	_updateListenRowHeightChanged: function (b) {
		var curr = !!this._listenRowHeightChanged;
		if (curr != b) {
			this.sheet[curr ? 'unlisten' : 'listen']({onRowHeightChanged: this.proxy(this._onRowHeightChanged)});
			this._listenRowHeightChanged = b;
		}
	},
	getText: function () {
		return this.getTextNode().innerHTML;
	},
	getPureText: function () { //feature #26: Support copy/paste value to local Excel\
		var n = this.getTextNode();
		return n.textContent || n.innerText;
	},
	_setText: function (txt) {
		if (!txt)
			txt = "";
		this.text = this.getTextNode().innerHTML = txt;
		
		if (zk.ie6_ || zk.ie7_) {
			if (txt) {
				this._updateVerticalAlign();
			}
			this._updateListenRowHeightChanged(!!txt);
		}
	},
	redraw: function (out) {
		out.push(this.getHtml());
	},
	getHtml: function () {
		var	uid = this.uuid,
			text = this.text,
			style = this.domStyle_(),
			innerStyle = this.innerStyle,
			fontStyle = this.fontStyle;
		//IE6/IE7: vertical align need position:absolute;
		return '<div id="' + uid + '" class="' + this.getZclass() + '" zs.t="SCell" '
			+ (style ? 'style="' +  style + '"' : '') + '><div id="' + uid + '-cave" class="' +
			this._getInnerClass() + '" ' + (innerStyle ? 'style="' + innerStyle + '"' : '') + 
			'>' + this.getCommentHtml(this.comment) 
			+ '<div id="' + uid + '-real" class="' + this._getRealClass() + '"' +
			// ZSS-725
			(fontStyle ? ' style="' + fontStyle + '"' : '') + '>' + text + '</div>' + '</div></div>';
	},
	getCommentHtml: function(shown) {
		// ZSS-849 implement in CML
		return '';
	},
	getZIndex: function () {
		if (zk.ie6_ || zk.ie7_)
			return this.cellType == BLANK_CELL ? -1 : 1;
		return this.text ? null : 1;
	},
	domStyle_: function () {
		var st = this.style;
		if (st) {
			return st;
		} else {
			var zIdx = this.getZIndex();
			if (zIdx){
				return 'z-index:' + zIdx + ';';
			}
		}
	},
	getTextNode: function () {
		return this.$n('real');
	},
	_clearOverflow: function () {
		jq(this.getTextNode()).css('width', '');//clear overflow
		jq(this.$n()).removeClass("zscell-overflow").removeClass("zscell-overflow-b");
	},
	_processOverflow: function () {
		// not implement in OSE
	},
	bind_: function (desktop, skipper, after) {
		this.$supers(zss.Cell, 'bind_', arguments);
		
		var n = this.comp = this.$n(),
			sheet = this.sheet;
		n.ctrl = this;
		this.cave = n.firstChild;


		if (this.cellType == BLANK_CELL) {//no need to process overflow and wrap
			return;
		}
		this._updateListenOverflow(this.overflow);
		
		if (!!this.text && (zk.ie6_ || zk.ie7_) && this.valign != 't') { // IE6 / IE7 doesn't support CSS vertical align
			this._updateListenRowHeightChanged(true);
			this._updateVerticalAlign();
		}

		// ZSS-224: skip process overflow according to the hint from server
		// it indicates that this cell's silbing isn't blank
		var skipOverflowOnBinding = (this.overflowOpt & 2) != 0; // skip overflow when initializing
		if (this.overflow && !skipOverflowOnBinding) {
			this._processOverflow(); // heavy duty
		}
		
		//ZSS-944
		var toRotate90 = this.rotate == 90 || this.rotate == -90; //ZSS-1020
		this._updateListenRotate(toRotate90);
		if (toRotate90) {
			this._processRotate(); // heavy duty
			return; // rotate imply no wrap
		}

		// ZSS-958: save all cells which aren't same as default height to calculate heighest row height
		this._saveNoneDefaultHeightCell();
		
	},
	_saveNoneDefaultHeightCell: function () {
		// not implement in OSE
	},
	unbind_: function () {
		this._updateListenOverflow(false);
		this._updateListenRotate(false); //ZSS-944
		this._updateListenRowHeightChanged(false);
		this.parent.removeAutoHeightCell(this);
		
		this.comp = this.comp.ctrl = this.cave = this.sheet = this.overlapBy = this._listenRowHeightChanged =
		this.block = this.lock = null;
		
		this.$supers(zss.Cell, 'unbind_', arguments);
	},
	//ZSS-944
	/**
	 * When this cell's rotate changed
	 */
	_onProcessRotate: function (evt) {
		data = evt.data;
		if (data) {
			var row = this.r,
				tRow = data.tRow,
				bRow = data.bRow;
			if (this.c == data.col
				&& ((tRow == undefined && bRow == undefined) || 
						(tRow && bRow && row >= tRow && row <= bRow))) {
				var rotate90 = this.rotate == 90 || this.rotate == -90; //ZSS-1020 
				if (rotate90)
					this._processRotate();
			}
		}
	},
	/**
	 * When cells after this cell changed, may effect this cell's overflow
	 */
	_onProcessOverflow: function (evt) {
		if (this.overflow) {
			var row = this.r,
				data = evt.data;
			if (data) {
				var rCol = data.col,
					tRow = data.tRow,
					bRow = data.bRow;
				if (this.c < data.col) {
					if ((tRow == undefined && bRow == undefined) || 
						(tRow && bRow && row >= tRow && row <= bRow)) {
						this._processOverflow(true);
					}
				}
			}
		}
	},
	//super//
	getZclass: function () {
		var cls = 'zscell',
			hId = this.zsh,
			wId = this.zsw,
			mCls = this.mergeCls,
			db_tlbr = this.db_tlbr, //ZSS-568
			af_tlbr = this.af_tlbr; //ZSS-901
		if (hId)
			cls += (' zshi' + hId);
		if (wId)
			cls += (' zsw' + wId);
		if (mCls)
			cls += (' ' + mCls);
		if (db_tlbr)
			cls += (' ' + db_tlbr);
		if (af_tlbr)
			cls += (' ' + af_tlbr);
		return cls;
	},
	_getInnerClass: function () {
		var cls = 'zscelltxt',
			hId = this.zsh,
			wId = this.zsw;
		if (hId)
			cls += (' zshi' + hId);
		if (wId)
			cls += (' zswi' + wId);
		return cls;
	},
	_getRealClass: function() {
		//ZSS-944: when rotate 90 degree, wrap must be false
		var rotate90 = this.rotate == 90 || this.rotate == -90, //ZSS-1020
			cls = 'zscelltxt-real ' + (this.wrap && !rotate90 ? WRAP_TEXT_CLASS:''),
			hId = this.zsh;

		if(hId) 
			cls += (' zshr' + hId);

		return cls;
	},
	/**
	 * Sets the width position index
	 * @param int zsw the width position index
	 */
	appendZSW: function (zsw) {
		if (zsw) {
			this.zsw = zsw;
			jq(this.comp).addClass("zsw" + zsw);
			jq(this.cave).addClass("zswi" + zsw);
		}
	},
	/**
	 * Sets the height position index
	 * @param int zsh the height position index
	 */
	appendZSH: function (zsh) {
		if (zsh) {
			this.zsh = zsh;
			jq(this.comp).addClass("zshi" + zsh);
			jq(this.cave).addClass("zshi" + zsh);
			jq(this.$n('real')).addClass("zshr" + zsh);
		}
	},
	/**
	 * Sets the column index of the cell
	 * @param int column index
	 */
	resetColumnIndex: function (newcol) {
		var	src = this.src;
		this.ref = src.columnHeaders[newcol].t + src.rowHeaders[this.r].t;
		this.c = newcol;
	},
	/**
	 * Sets the row index of the cell
	 * @param int row index
	 */
	resetRowIndex: function (newrow) {
		var	src = this.src;
		this.ref = src.columnHeaders[this.c].t + src.rowHeaders[newrow].t;
		this.r = newrow;
	},
	/**
	 * Returns whether the cell is in range
	 * @param int row index
	 * @param int col index
	 */
	isInRange: function (row, col) {
		return this.merid ? this.mert <= row && this.merb >= row && this.merl <= col && this.merr >= col:
			this.r == row && this.c == col;
	}
});
})();