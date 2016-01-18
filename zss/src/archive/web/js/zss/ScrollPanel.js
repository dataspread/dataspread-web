/* ScrollPanel.js

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
 * ScrollPanel is used to handle spreadsheet scroll moving event,  
 */
zss.ScrollPanel = zk.$extends(zk.Object, {
	/**
	 * Scroll direction
	 */
	dir: 'south',
	$init: function (sheet) {
		this.$supers('$init', []);
		var wgt = sheet._wgt,
			scrollPanel = sheet.$n('sp');
		
		this.id = scrollPanel.id;
		this.sheet = sheet;
		this.comp = scrollPanel;
		this.currentTop = this.currentLeft = this.timerCount = 0;
		this.leftWidth = 28;//left head width
		this.topHeight = 20;//top head height
		this.timerRunning = false;//is timer for scroll running.
		this.lastMove = "";//the last move of scrolling,
		scrollPanel.ctrl = this;
		
		var dtcmp = this.sheet.dp.comp,//zkSDatapanelCtrl._currcmp(self);
			sccmp = this.comp;
		this.minLeft = this._getMaxScrollLeft(dtcmp, sccmp);
		this.minTop = this._getMaxScrollTop(dtcmp, sccmp);
		this.minHeight = dtcmp.offsetHeight;
		this.minWidth = dtcmp.offsetWidth;
		wgt.domListen_(scrollPanel, 'onScroll', this.proxy(this._doScrolling))
			.domListen_(scrollPanel, 'onMouseDown', this.proxy(this._doMousedown));
		
		jq(sheet.cp.$n()).on('mousewheel', this.proxy(this._doMouseWheel));
		jq(sheet.lp.$n()).on('mousewheel', this.proxy(this._doMouseWheel));
		jq(sheet.tp.$n()).on('mousewheel', this.proxy(this._doMouseWheel));
	},
	/**
	 * Returns the direction that scroll to
	 * 
	 * <ul>
	 * 	<li>north</li>
	 * 	<li>west</li>
	 * 	<li>east</li>
	 * 	<li>south</li>
	 * </ul>
	 * 
	 * @return string direction
	 */
	getDirection: function () {
		return this.dir;
	},
	reset: function (top, left) {
		var n = this.comp;
		n.scrollLeft = left;
		n.scrollTop = top;
		this.currentTop = top;
		this.currentLeft = left;
	},
	cleanup: function () {
		var sheet = this.sheet,
			wgt = sheet._wgt,
			n = sheet.$n('sp');
		
		jq(sheet.tp.$n()).off('mousewheel', this.proxy(this._doMouseWheel));
		jq(sheet.lp.$n()).off('mousewheel', this.proxy(this._doMouseWheel));
		jq(sheet.cp.$n()).off('mousewheel', this.proxy(this._doMouseWheel));
		
		wgt.domUnlisten_(n, 'onScroll', this.proxy(this._doScrolling))
			.domUnlisten_(n, 'onMouseDown', this.proxy(this._doMousedown));
		
		this.invalid = true;
		if(this.comp) this.comp.ctrl = null;
		this.comp = this.sheet = null;
	},
	//ZSS-996
	_doMouseWheel: function (evt) {
		var event = evt.originalEvent,
			deltaX = event.wheelDeltaX,
			deltaY = event.wheelDeltaY ? event.wheelDeltaY / 120 : event.wheelDelta ? event.wheelDelta / 120 : -event.detail / 3,
			scrollPanel = jq(this.comp);
				
		if(deltaY && deltaY != 0)
			scrollPanel.scrollTop(scrollPanel.scrollTop() - (deltaY * 100));
		
		// ff & IE doesn't support horizontal mouse wheel
		if(deltaX && deltaX != 0)
			scrollPanel.scrollLeft(scrollPanel.scrollLeft() - (deltaX / 120 * 100));
	},
	_doMousedown: function (evt) {
		var cmp = this.comp;
		if(evt.domTarget != cmp) return;
		var data = zkS._getMouseData(evt, cmp);
		
		//calculate is xy in scroll bar
		var clickInHor = (data[1] < zk(cmp).offsetHeight() && data[1] > cmp.clientHeight);
			clickInVer = (data[0] < zk(cmp).offsetWidth() && data[0] > cmp.clientWidth);
		
		if( (clickInHor&&clickInVer) || (!clickInHor&&!clickInVer)) return;
		var sinfo = this.sheet.sinfo;
		sinfo.pinXY(data[0], data[1], clickInHor);
	},
	_doScrolling: function (evt) {
		var sheet = this.sheet,
			dtcmp = sheet.dp.comp,
			sccmp = this.comp,
			scleft = sccmp.scrollLeft,
			sctop = sccmp.scrollTop,
			moveHorizontal = (scleft != this.currentLeft),
			moveVertical = (sctop != this.currentTop),
			moveLeft = scleft < this.currentLeft,
			moveTop = sctop < this.currentTop;
		
		if (moveHorizontal) {
			this.dir = moveLeft ? 'west' : 'east';
		} else {
			this.dir = moveTop ? 'north' : 'south';
		}
		this.currentLeft = scleft;
		this.currentTop = sctop;
		sheet.tp._updateLeftPos(-this.currentLeft);
		sheet.lp._updateTopPos(-this.currentTop);
		if (this._visFlg) {
			//if fire by scroll to visible, don't show info 
			this._visFlg = false;
		} else {
			/*show scroll info*/
			var sinfo = sheet.sinfo;
			if (moveHorizontal) {
				if (!sinfo.visible || !sinfo.horizontal)
					sinfo.pinLocation(true);
				sinfo.showInfoOnDir(true);
			}
			
			if (moveVertical) {
				if (!sinfo.visible || sinfo.horizontal)
					sinfo.pinLocation(false);
				sinfo.showInfoOnDir(false);
			}
			if (moveVertical || moveHorizontal)
				this._trackScrolling(moveVertical);
		}
	},
	_trackScrolling: function (vertical) {
		if (vertical)
			this._fireOnVScroll();//fire scroll vertical
		else
			this._fireOnHScroll();//fire scroll horizontal
	},
	_vscrollTimeoutId: null,
	_hscrollTimeoutId: null,
	_fireOnVScroll: function (time) {
		clearTimeout(this._vscrollTimeoutId);
		
		//this._vscrollTimeoutId = setTimeout(this.proxy(this._doVScroll), time >= 0 ? time : zk.gecko ? 200 : 60);
		var self = this;
		this._vscrollTimeoutId = setTimeout(function () {
			self._doVScroll();
		}, time >= 0 ? time : 60);
	},
	_fireOnHScroll: function (time) {
		clearTimeout(this._hscrollTimeoutId);
		
		//this._hscrollTimeoutId = setTimeout(this.proxy(this._doHScroll), time >= 0 ? time : zk.gecko ? 200 : 60);
		var self = this;
		this._vscrollTimeoutId = setTimeout(function () {
			self._doHScroll();
		}, time >= 0 ? time : 60);
	},
	_doVScroll: function () {
		this._doScroll(true);
	},
	_doHScroll: function () {
		this._doScroll(false);
	},
	_doScroll: function (vertical) {
		this.sheet.activeBlock.doScroll(vertical);
	},
	_getMaxScrollLeft: function (dtcmp, sccmp) {
		return (dtcmp.offsetWidth - sccmp.offsetWidth) + zss.Spreadsheet.scrollWidth;
	},
	_getMaxScrollTop: function (dtcmp, sccmp) {
		return (dtcmp.offsetHeight - sccmp.offsetHeight) + zss.Spreadsheet.scrollWidth;
	},
	/**
	 * scroll a row, col or cell to visible
	 * @param {Object} cell cell ctrl
	 */
	scrollToVisible: function (row, col, cell, direction, pos) { // ZSS-475: add direction parameter
																// ZSS-664: add position parameter
		
		if(direction == undefined || direction == null) {
			direction = zss.SCROLL_DIR.BOTH; 
		}

		if (!pos) {
			pos = zss.SCROLL_POS.NONE;
		}
		
		//zss-219, scrollToVisible should able to not depends on cell dom instance
		
		var sheet = this.sheet;
		if (cell) {
			row = cell.r;
		    col = cell.c;
		}
		var cellLoc = this._getCellLocation(sheet,row,col);
		
		var spcmp = this.comp,
			block = sheet.activeBlock,
			w = cellLoc.width,//cellcmp.offsetWidth, // component width
			h = cellLoc.height,//cellcmp.offsetHeight, // component height
			l = cellLoc.left + sheet.leftWidth,//cellcmp.offsetLeft + block.comp.offsetLeft,//cell left in row + block left in datapanel = cell left in scroll panel 
			t = cellLoc.top + sheet.topHeight,//cellcmp.parentNode.offsetTop + block.comp.offsetTop,//Row top in block + block top in datapanel = cell top in scroll panel.
			sl = spcmp.scrollLeft,//current scroll left
			st = spcmp.scrollTop,//current scroll top
			sw = spcmp.clientWidth,//scroll panel width(no scroll bar)
			sh = spcmp.clientHeight,//scroll panel height(no scroll bar)
			th = sheet.topHeight, //sheet top height
			lw = sheet.leftWidth, //sheet left width
			lsl = sl, //final scroll left
			lst = st, //final scroll top
			dirty = false,
			fzr = sheet.frozenRow, //processing for frozen
			fzc = sheet.frozenCol,
			fh = 0,
			fw = 0;
		
		/**
		 * I wan to provide a feature, is moveFocus on frozenpanel then don't scroll datapanel 
		 * but if scroll to visible after a jump(for example, at the last column then click HOME)
		 * then the datapanel wil be recreate, if i dont scroll to the certain cell, 
		 * then i will see a blank (because the cell in this position is all cleared. 
		 */
		var moveonfr = moveonfc = false;
		 
		if (zkS.t(col) && fzc > -1) {
			if (fzc < col)
				fw = sheet.custColWidth.getStartPixel(fzc + 1);
			else
				moveonfc = true;// cell on frozen col, no need to scroll datapanel 
		}
	
		if (zkS.t(row) && fzr > -1) {
			if (fzr < row)
				fh = sheet.custRowHeight.getStartPixel(fzr + 1);
			else
				moveonfr = true;// cell on frozen col, no need to scroll datapanel
		}
		//end procesing for frozen
		
		/**
		 * if cell width large then scroll panel width
		 * or cell left small then scroll panel left
		 * then scroll left = cell-left
		 * 
		 * if scroll right < cell right
		 * then scroll left = cell right - scroll width (show cell right at the end of right side 
		 */
		if (zkS.t(col) && !moveonfc) {
			//cellcmp width large then scroll pane || cell-left  small then scroll=panel-left
			if (( (sw - lw) < (w - fw)) || (l-lw) < (sl+fw) ) {  
				lsl = l-lw - fw;
				dirty = true;
			 } else if(sl + sw < l + w) {
				lsl = l + w - sw;
				dirty = true;
			}
		}
		if (zkS.t(row) && !moveonfr) {
			if (((sh - th < h - fh || t - th < st + fh) && pos != zss.SCROLL_POS.BOTTOM) ||
				pos == zss.SCROLL_POS.TOP) { 
				lst = t-th - fh;
				dirty = true;
			} else if (st + sh < t + h || pos == zss.SCROLL_POS.BOTTOM) {//top large then scroll panel bottom
				lst = t+h - sh;
				dirty = true;
			}
		}

		if (dirty) {
			this._visFlg = true;

			// ZSS-475: consider scrolling direction accroding to argument
			switch(direction) {
			case zss.SCROLL_DIR.HORIZONTAL :
					spcmp.scrollLeft = lsl;
					break;
			case zss.SCROLL_DIR.VERTICAL :
					spcmp.scrollTop = lst;
					break;
			case zss.SCROLL_DIR.BOTH :
					spcmp.scrollLeft = lsl;
					spcmp.scrollTop = lst;
					break;
			}
			//after this , onScroll will be fired.
		}
	},
	_getCellLocation : function(sheet, row, column){
		var loc = {};
		loc.left = sheet.custColWidth.getStartPixel(column);
		loc.width = sheet.custColWidth.getSize(column);
		loc.top = sheet.custRowHeight.getStartPixel(row);
		loc.height = sheet.custRowHeight.getSize(row);
		return loc;
	}
});