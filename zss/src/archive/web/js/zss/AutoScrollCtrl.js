/* AutoScrollCtrl.js

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
	var ROWINC = 40,
		COLINC = 60,
		TIMEOUT = 100;
	/**
	 * Sets scroll panel scroll position
	 */
	function _autoScroll (sheet, ctrl) {
		var dir = ctrl.dir;
		if (dir && dir.length > 0) {
			var fn = function () {
				_autoScroll(sheet, ctrl);
			};
			setTimeout(fn, zss.AutoScrollCtrl.TIMEOUT);
		} else
			return;
		//scroll 
		var spcmp = sheet.sp.comp,
			rowoff = coloff = 0;
		if (dir.indexOf("t") > -1)
			rowoff = -ROWINC;
		else if (dir.indexOf("b") > -1)
			rowoff = ROWINC;
	
		if (dir.indexOf("l") > -1)
			coloff = -COLINC;
		else if(dir.indexOf("r") > -1)
			coloff = COLINC;
		
		if (! (ctrl.dironly == zss.AutoScrollCtrl.COLONLY))
			spcmp.scrollTop += rowoff;
				
		if (! (ctrl.dironly == zss.AutoScrollCtrl.ROWONLY) && rowoff == 0 ){
			//don't move both direction(rowoff!=0), 
			//because Load On Deman will prune cell when direction change.
			//in this case , if i move left when roowof!=0, that is V -> H -> V -> H 
			spcmp.scrollLeft += coloff;
	}
}
/**
 * AutoScrollCtrl is used to scrolling scroll panel when user do select dragging
 */
zss.AutoScrollCtrl = zk.$extends(zk.Object, {
	$init: function (sheet, dir, dironly) {
		this.$supers('$init', arguments);
		this.sheet = sheet;
		this.dir = dir;
		this.dironly = dironly;
		_autoScroll(sheet, this);
	},
	cleanup: function () {
		this.dir = "";
	},
	setDir: function(dir){
		this.dir = dir;
	},
	getDir: function(){
		return this.dir;
	}
}, {
	ROWONLY: 1,
	COLONLY: 2
});
})();