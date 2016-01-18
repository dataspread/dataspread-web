/* ScrollInfo.js

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
 * ScrollInfo show current row/column information after scrolling
 */
zss.ScrollInfo = zk.$extends(zss.Info, {
	$init: function (sheet, cmp) {
		this.$supers('$init', arguments);
		this.horizontal = true;
		this.x = this.y = -1;
	},
	/**
	 * Show information by direction
	 * @param boolean horizontal
	 */
	showInfoOnDir: function (horizontal) {
		
		if (!this.enabled) return;
		var sheet = this.sheet,
			sp = sheet.sp,
			custColWidth = sheet.custColWidth,
			custRowHeight = sheet.custRowHeight,
			spcmp = sp.comp,
			viewLeft = spcmp.scrollLeft,
			viewTop = spcmp.scrollTop,
			viewWidth = spcmp.clientWidth,
			viewHeight = spcmp.clientHeight;
		
		if (horizontal) {
			var col = viewLeft;
			
			if (sheet.frozenCol > -1)
				col += custColWidth.getStartPixel(sheet.frozenCol + 1);
			
			col = custColWidth.getCellIndex(col)[0] + 1;
			this.setInfoText("Column : " + col);
			jq(this.comp).css('text-align', 'left');
		} else {
			var row = viewTop;
			if (sheet.frozenRow > -1)
				row += custRowHeight.getStartPixel(sheet.frozenRow + 1);

			row = custRowHeight.getCellIndex(row)[0] + 1;
			this.setInfoText("Row : " + row);
			jq(this.comp).css('text-align', 'right');
		}
		this.showInfo(true);
	},
	/** 
	 * Pin xy point and enable
	 * @param int x
	 * @param int y
	 * @param boolean horizontal
	 */
	pinXY: function(x, y, horizontal) {
		this.x = x;
		this.y = y;
		this.horizontal = horizontal;
		this.enabled = true;

		if (this.visible) {
			this.pinLocation(horizontal);
			this.showInfo(horizontal);
		}
	},
	/**
	 * Pin location
	 * @param boolean horizontal
	 */
	pinLocation: function (horizontal) {
		if (!this.enabled) return ;
		var sheet = this.sheet,
			spcmp = sheet.sp.comp;
		if (this.horizontal != horizontal) {
			this.x = -1;
			this.y = -1;
		}
		this.horizontal = horizontal;
		
		var viewWidth = spcmp.clientWidth,
			viewHeight = spcmp.clientHeight;
		
		if (!this.visible)
			jq(this.comp).css({'left': '-90000px', 'display': 'inline'});//show in non-visible for correct size
		var infox, infoy;
		
		if (horizontal) {
			infox = (this.x == -1) ? sheet.leftWidth + 1 : this.x;
			infoy = viewHeight - zk(this.comp).offsetHeight();
		} else {
			infox = viewWidth - zk(this.comp).offsetWidth();
			infoy = (this.y == -1) ? sheet.topHeight + 1 : this.y;
		}

		jq(this.comp).css({'left': jq.px(infox), 'top': jq.px(infoy)});
	}
});