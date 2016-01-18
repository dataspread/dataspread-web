/* Defer.js

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jan 12, 2012 7:07:27 PM , Created by sam
}}IS_NOTE

Copyright (C) 2012 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
(function () {

/**
 * Preload extra cells data from server
 */
zss.DeferLoader = zk.$extends(zk.Object, {
	_deferTime: 500,
	/**
	 * Assume visible column size is 50
	 * 
	 * When 
	 * Scroll once: may scroll down about 30 rows
	 * Scroll twice: may scroll down about 40 rows
	 * Scroll thrice: may scroll down about 60 rows
	 * 
	 * Extra size: 60 (rows) * 50 (columns)
	 */
	_extraSize: 3000,
	/**
	 * Default load row size if Spreadsheet's preload row size not defined
	 */
	_loadRow: 60,
	/**
	 * Default load column size if Spreadsheet's preload column size not defined
	 */
	_loadCol: 40,
	$init: function (sheet) {
		this.sheet = sheet;
		this._wgt = sheet._wgt;
	},
	asyncRun: function () {
		if (this._timeoutId) {
			clearTimeout(this._timeoutId);
			this._timeoutId = null;
		}

		var sf = this;
		if (this.shallLoad()) {
			if (this.shallLoadNow()) {
				this._timeoutId = null;
				var wgt = sf._wgt,
					maxRow = wgt.getMaxRows(),
					maxCol = wgt.getMaxColumns(),
					preloadRow = wgt.getPreloadRowSize(),
					preloadCol = wgt.getPreloadColumnSize(),
					ar = wgt._cacheCtrl.getSelectedSheet(),
					rect = ar.rect,
					dir = this.sheet.sp.getDirection(),
					top = -1,
					left = -1,
					right = -1,
					bottom = -1;
				if (dir == 'south') {
					left = rect.left;
					right = rect.right;
					top = rect.bottom + 1;
					if (top >= maxRow) //hit boundary, no need to fetch data
						return;
					bottom = top + (preloadRow > 0 ? preloadRow : this._loadRow);
					if (bottom >= maxRow)
						bottom = maxRow - 1;
				} else if (dir == 'north') {
					left = rect.left;
					right = rect.right;
					bottom = rect.top - 1;
					if (bottom < 0)
						return;
					top = bottom - (preloadRow > 0 ? preloadRow : this._loadRow);
					if (top < 0)
						top = 0;
				} else if (dir == 'east') {
					top = rect.top;
					bottom = rect.bottom;
					left = rect.right + 1;
					if (left >= maxCol)
						return;
					right = left + (preloadCol > 0 ? preloadCol : this._loadCol);
					if (right >= maxCol)
						right = maxCol - 1;
				} else if (dir == 'west') {
					top = rect.top;
					bottom = rect.bottom;
					right = rect.left - 1;
					if (right < 0)
						return;
					left = right - (preloadCol > 0 ? preloadCol : this._loadCol);
					if (left < 0)
						left = 0;
				}
				if (top >= 0 && left >= 0 && right >= 0 && bottom >=0)
					wgt.fetchActiveRange(top, left, right, bottom);
			} else {
				this._timeoutId = setTimeout(function () {
					sf.asyncRun();
				}, this._deferTime)
			}
		}
	},
	/**
	 * Decide whether shall load extra data or not
	 * 
	 * @return boolean
	 */
	shallLoad: function (dir) {
		var wgt = this._wgt,
			sheet = this.sheet,
			activeBlock = sheet.activeBlock;
		if (activeBlock.loadstate == zss.MainBlockCtrl.LOADING) {
			return;
		}
		dir = dir || this.sheet.sp.getDirection();
		var vrng = activeBlock.range,
			maxRow = wgt.getMaxRows() - 1,
			maxCol = wgt.getMaxColumns() - 1,
			ar = wgt._cacheCtrl.getSelectedSheet(),
			rect = ar.rect,
			size = ((rect.right - rect.left + 1) * (rect.bottom - vrng.bottom + 1));
		switch (dir) {
		case 'south':
			var rbtm = rect.bottom;
			if (rbtm >= maxRow)
				return false;
			var width = rect.right - rect.left + 1,
				size = width * (rbtm - vrng.bottom + 1);
			return size < this._extraSize;
		case 'east':
			var rright = rect.right;
			if (rright >= maxCol)
				return false;
			var height = rect.bottom - rect.top + 1,
				size = (height * (rect.right - vrng.right + 1));
			return size < this._extraSize;
		case 'north':
			var rtop = rect.top
			if (!rtop)
				return false;
			var	width = rect.right - rect.left + 1,
				size = width * (vrng.top - rtop + 1);
			return size < this._extraSize;
		case 'west':
			var rleft = rect.left;
			if (!rleft)
				return false;
			var hgh = rect.bottom - rect.top + 1,
				size = hgh * (vrng.left - rleft + 1);
			return size < this._extraSize;
		}
	},
	/**
	 * Decide whether shall load extra data now or not
	 * 
	 * @return boolean
	 */
	shallLoadNow: function () {
		return this.sheet.activeBlock.loadstate == zss.MainBlockCtrl.IDLE;
		/*
		if (zAu.processing())
			return false;
		
		var sheet = this.sheet,
			activeBlock = sheet.activeBlock,
			shtState = sheet.state,
			notEditing = shtState == zss.SSheetCtrl.FOCUSED || shtState == zss.SSheetCtrl.NOFOCUS;
		return notEditing && activeBlock.loadstate == zss.MainBlockCtrl.IDLE;
		*/
	}
});
})();