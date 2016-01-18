/* DragHandler.js

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
 * DragHandler handle mouse dragging selection event
 */
zss.DragHandler = zk.$extends(zk.Object, {
	$init: function (sheet) {
		this.$supers('$init', arguments);
		this.sheet = sheet;
		var wgt = sheet._wgt;

		wgt.domListen_(document, "onMouseUp", '_doDragMouseUp');
		wgt.domListen_(document, "onMouseMove", '_doDragMouseDown');

		zk(document.body).disableSelection();
	},
	_isMenupopupOpen: function () {
		var sheet = this.sheet,
			p = sheet.getStyleMenupopup();
		if (p && p.isOpen()) {
			return true;
		}
		
		p = sheet.getCellMenupopup();
		if (p && p.isOpen()) {
			return true;
		}
		
		p = sheet.getColumnHeaderMenupopup();
		if (p && p.isOpen()) {
			return true;
		}
		
		p = sheet.getRowHeaderMenupopup();
		if (p && p.isOpen()) {
			return true;
		}
		return false;
	},
	cleanup: function () {
		if (this.invalid) return;//don't clean twice.
		var wgt = this.sheet._wgt;
		wgt.domUnlisten_(document, "onMouseUp", '_doDragMouseUp');
		wgt.domUnlisten_(document, "onMouseMove", '_doDragMouseDown');
		
		this.invalid = true;
		this.stopAutoScroll();
		zk(document.body).enableSelection();
		this.sheet.stopDragging();
		/* ZSS-169: set paste src only when user set selection by drag cells
		//feature #26: Support copy/paste value to local Excel
		var sheet = this.sheet,
			self = this;
		if (sheet.state != zss.SSheetCtrl.Editing && !sheet.editingFormulaInfo) {
			var focustag = sheet.dp.focustag;
			
			//Note. mouse down -> mouse up (DragHandler cleanup) -> mouse click
			//Menupopup open at mouse click event, delay focustag.focus 
			sheet.runAfterMouseClick(function () {
				setTimeout(function () {
					if (!self._isMenupopupOpen()) {
						focustag.focus();
						jq(focustag).select();
					}
				}, 0);
			});
		}
		*/
	},
	stopAutoScroll : function (){
		if (this.scrollctrl) {
			this.scrollctrl.cleanup();
			this.scrollctrl = null;
		}
	},
	doMouseup: function (evt) {
		this.cleanup();
	},
	doMousemove: function (evt) {
		//do nothing in default
	}
});


/**
 * zkSSelDrag -> zss.SelDrag
 * 
 * SelDrag handle mouse dragging selection
 */
zss.SelDrag = zk.$extends(zss.DragHandler, {
	$init: function (sheet, selType, row, col, key, merr) {
		this.$supers('$init', arguments);
		this.selType = selType;
		this.row = row;
		this.col = col;
		this.lastrow = row;
		this.lastcol = col;
		this.key = key;//right("r") or left("l") mouse key
		this.merr = merr;
	},
	/**
	 * Ends mouse dragging selection event, and sets the cell's selection range
	 * @param zk.Event
	 */
	doMouseup: function (evt) {
		var sheet = this.sheet,
			ls = sheet.getLastSelection();

		sheet._sendOnCellSelection(this.selType, ls.left, ls.top, ls.right, ls.bottom);
		this.cleanup();
	},
	/**
	 * Start mouse dragging selection event
	 * @param zk.Event
	 */ 
	doMousemove: function (evt) {
		if (this.key != "l") return;//only care left key drag
		
		var sheet = this.sheet,
			elm = evt.domTarget,
			row = -1,
			col = -1,

			mx = evt.pageX,//mouse offset, against browser body 
			my = evt.pageY,
			cmp,
			outside;
		
		if ( (cmp = zkS.parentByZSType(elm, ["SCell","SRow"], 1)) != null) {//move on cell or vertical merged cell
			var cellpos = zss.SSheetCtrl._calCellPos(sheet, mx, my, false);
			row = cellpos[0];
			col = cellpos[1];
		} else if (this.selType == zss.SEL.ROW && (cmp = zkS.parentByZSType(elm, "SLheader", 1)) != null) {//move on left header
			row = cmp.ctrl.index;
		} else if (this.selType == zss.SEL.COL && (cmp = zkS.parentByZSType(elm, "STheader", 1)) != null) {//move on top header
			col = cmp.ctrl.index;
		} else if ((cmp = zkS.parentByZSType(elm, ["SSelect", "SHighlight"], 1)) != null) {//move on select
			var cellpos = zss.SSheetCtrl._calCellPos(sheet, mx, my, false);
			row = cellpos[0];
			col = cellpos[1];
		} else if ((cmp = zkS.parentByZSType(elm, ["SScrollpanel", "SCorner", "SLheader", "STheader"], 2)) != null
			|| zkS.parentByZSType(elm, "SSheet", 5) == null ) {
			var dir = "";
			switch (jq(cmp).attr('zs.t')) {
			case 'SCorner':
				dir = "lt";
				break;
			case 'SLheader':
				dir = "l";
				break;
			case 'STheader':
				dir = "t";
				break;
			default:
				//outside or scrollpanel*/
				var sheetofs = zk(sheet.comp).revisedOffset();
				if (mx < sheetofs[0]) {
					dir = "l";
				} else if (mx >= sheetofs[0] + sheet.comp.offsetWidth - zss.Spreadsheet.scrollWidth) {
					dir = "r";
				}
				if (my < sheetofs[1]) {
					dir += "t";
				} else if (my>=sheetofs[1] + sheet.comp.offsetHeight) {
					dir += "b";
				}
			}

			if (dir != "" && (!this.scrollctrl || this.scrollctrl.getDir() == "")) { 
				var dironly = (this.selType == zss.SEL.ROW ? zss.AutoScrollCtrl.ROWONLY: this.selType == zss.SEL.COL ? zss.AutoScrollCtrl.COLONLY: "");
				this.scrollctrl = new zss.AutoScrollCtrl(sheet, dir, dironly);
			} else if (this.scrollctrl)
				this.scrollctrl.setDir(dir);

			return;
		} else {
			this.stopAutoScroll();
			return;
		}
		this.stopAutoScroll();
		this.moveSelection(row, col);
	},
	/**
	 * Move selection
	 * @param int row
	 * @param int col
	 */
	moveSelection: function (row, col) {
		var sheet = this.sheet;
		if (row >= sheet.maxRows) row = sheet.maxRows - 1;
		if (col >= sheet.maxCols) col = sheet.maxCols - 1;
		if (row < 0) row = 0;
		if (col < 0) col = 0;
		
		if (this.selType == zss.SEL.ROW) {
			if (this.lastrow != row && row>-1) {
				this.lastrow = row;
				sheet.moveRowSelection(this.row, row);
			}
		} else if (this.selType == zss.SEL.COL) {
			if (this.lastcol != col && col > -1) {
				this.lastcol = col;
				sheet.moveColumnSelection(this.col, col);
			}
		} else if (this.selType == zss.SEL.CELL) {
			var update = false;
			if (this.lastrow != row && row > -1) {
				this.lastrow = row;
				update = true;
			}
			if (this.lastcol != col && col > -1) {
				this.lastcol = col;
				update = true;
			}
			if (update) {
				var top = (this.row > row) ? row : this.row,
					bottom = (this.row < row)? row : this.row,
					left = (this.col > col) ? col : this.col,
					right = (this.col < col) ? col : this.col;
		
				sheet.moveCellSelection(left, top, right, bottom, true);
			}
		}
	}
});

/**
 * zkSSelChgDrag -> zss.SelChgDrag
 * SelChgDrag handle selection change event.
 * 
 * Selection change event occur when mouse down on current cell selection and drag to change it's range.  
 */ 
zss.SelChgDrag = zk.$extends(zss.DragHandler, {
	$init: function (sheet, selType, action, row, col) {
		this.$supers('$init', arguments);
		this.selType = selType;
		this.action = action;
		var selrang = sheet.selArea.lastRange;
		this.top = selrang.top;
		this.left = selrang.left;
		this.right = selrang.right;
		this.bottom = selrang.bottom;
		
		var pos = sheet.getLastFocus();
		this.frow = pos.row;
		this.fcol = pos.column;
		
		if (action == zss.SELDRAG.MOVE) {
			this.row = row;//start on which row
			this.col = col;//on which col
		}
		sheet.moveSelectionChange(this.left, this.top, this.right, this.bottom);
	},
	/**
	 * Ends selection change event
	 * @param  zk.Event
	 */ 
	doMouseup: function (evt) {
		var sheet = this.sheet,
			orgrange = sheet.selArea.lastRange;
			range = sheet.selChgArea.lastRange;
		if (!range)
			return;//sel change area never be updated.
		var row = this.frow,
			col = this.fcol;
			
		if (this.action == zss.SELDRAG.MOVE) {
			var voff = range.top - this.top,//offset to orginal selection
				hoff = range.left - this.left; 
			//move foucs depends on offset	
			row += voff;
			col += hoff;
		}
		
		if (col > range.right) col = range.right;
		else if (col < range.left) col = range.left;
			
		if (row > range.bottom) row = range.bottom;
		else if(row < range.top) row = range.top;
	
		if (row != this.frow || col != this.fcol) {
			sheet.dp.moveFocus(row, col, false, false);//moveFocus only move Focus, doesn't move selection
		}
			
		
		if (range.left != orgrange.left || range.top != orgrange.top || range.right != orgrange.right || range.bottom != orgrange.bottom) { 
			sheet._sendOnCellSelectionUpdate(this.selType, this.action,
				range.left, range.top, range.right, range.bottom,
				orgrange.left, orgrange.top, orgrange.right, orgrange.bottom);
		}
		sheet.hideSelectionChange();
		sheet.moveCellSelection(range.left, range.top, range.right, range.bottom);
		this.cleanup();
	},
	/**
	 * Starts selection change event
	 * @param zk.Event
	 */
	doMousemove: function (evt) {
		var sheet = this.sheet,
			elm = evt.domTarget,
			row = -1,
			col = -1,
			mx = evt.pageX,//mouse offset, against browser body 
			my = evt.pageY,
			cmp,
			outside;
		
		if ((cmp = zkS.parentByZSType(elm, ["SCell", "SSelect", "SSelChg", "SHighlight"], 1)) != null){//move on select
			cellpos = zss.SSheetCtrl._calCellPos(sheet, mx, my, false);
			row = cellpos[0];
			col = cellpos[1];
		} else if((cmp = zkS.parentByZSType(elm, ["SScrollpanel", "SCorner", "SLheader", "STheader"], 2)) != null
			|| zkS.parentByZSType(elm, "SSheet", 5) == null ){
			var dir = "";
			switch (jq(cmp).attr('zs.t')) {
			case 'SCorner':
				dir = "lt";
				break;
			case 'SLheader':
				dir = "l";
				break;
			case 'STheader':
				dir = "t";
				break;
			default:
				//outside or scrollpanel*/
				var sheetofs = zk(sheet.comp).revisedOffset();
				if (mx < sheetofs[0])
					dir = "l";
				else if(mx >= sheetofs[0] + sheet.comp.offsetWidth - zss.Spreadsheet.scrollWidth)
					dir = "r";
				
				if (my < sheetofs[1])
					dir += "t";
				else if (my >= sheetofs[1] + sheet.comp.offsetHeight)
					dir += "b";
			}

			if (dir != "" && (!this.scrollctrl || this.scrollctrl.getDir() == "")) { 
				this.scrollctrl = new zss.AutoScrollCtrl(sheet, dir, false);
			} else if (this.scrollctrl)
				this.scrollctrl.setDir(dir);
			return;
		} else {
			this.stopAutoScroll();
			return;
		}
		this.stopAutoScroll();

		if (this.action == zss.SELDRAG.MOVE)
			this._move(row, col);
		else if (this.action == zss.SELDRAG.RESIZE)
			this._modify(row, col);
	},
	_modify: function (row, col) {
		var sheet = this.sheet;
		if (row >= sheet.maxRows) row = sheet.maxRows - 1;
		if (col >= sheet.maxCols) col = sheet.maxCols - 1;
		if (row < 0) row = 0;
		if (col < 0) col = 0;

		var top = this.top,
			bottom = this.bottom,
			left = this.left,
			right = this.right,
			update = false;
		if (this.lastrow != row && row > -1) {
			this.lastrow = row;
			update = true;
		}
		if (this.lastcol != col && col > -1){
			this.lastcol = col;
			update = true;
		}
		if (update) {
			if(top > row || bottom < row || left > col || right < col) {
				//outside original selection
				//just a simple algorithm to know it move in horizontal or vertical
				var v = (Math.abs(top + (bottom - top) / 2 - row) / 2 > Math.abs(left + (right - left) / 2 - col))?true:false;
				if( (v || (left <= col && this.right >= col)) && top > row) {
					top = row;	
				} else if( (v || (left <= col && this.right >= col)) &&  bottom < row){
					bottom = row;
				} else if((!v || (top <= row && this.bottom >= row)) && left > col){
					left = col;
				} else if((!v || (top <= row && this.bottom >= row)) && right < col){
					right = col;
				}
			} else {
				//inside original selection
				var v = ((bottom - row) / 2 > (right - col)) ? true : false;
				if (v)
					bottom = row;
				else
					right = col;
			}
			sheet.moveSelectionChange(left, top, right, bottom);
		}
	},
	_move: function (row, col) {
		var sheet = this.sheet;
		if (row >= sheet.maxRows) row = sheet.maxRows - 1;
		else if (row < 0) row = 0;
		if (col >= sheet.maxCols) col = sheet.maxCols - 1;
		else if(col < 0) col = 0;

		var update = false;
		if (this.lastrow != row && row > -1) {
			this.lastrow = row;
			update = true;
		}
		if (this.lastcol != col && col>-1) {
			this.lastcol = col;
			update = true;
		}
		if (update) {
			var voff = row - this.row,
				hoff = col - this.col; 
			if(voff + this.top < 0) voff = -this.top;
			else if(voff + this.bottom >= sheet.maxRows) voff = sheet.maxRows - this.bottom - 1;
			
			if(hoff + this.left < 0) hoff = -this.left;
			else if(hoff + this.right >= sheet.maxCols) hoff = sheet.maxCols - this.right - 1;
			
			var top = this.top + voff,
				bottom = this.bottom + voff,
				left = this.left + hoff,
				right = this.right + hoff;
			sheet.moveSelectionChange(left, top, right, bottom);
		}
	}
});