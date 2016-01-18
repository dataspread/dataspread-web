/* Range.js

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
 * Range represent a rectangle area of cells
 */
zss.Range = zk.$extends(zk.Object, {
	left: -1,
	top: -1,
	right: -1,
	bottom: -1,
	width: -1,
	height: -1,
	$init: function (left, top, right/*width*/, bottom/*height*/, sizemode) {
		this.$supers('$init', arguments);
		this.left = left;
		this.top = top;
		if (sizemode) {//when size mode, that is assing width and height.
			this.width = right;
			this.height = bottom;
			this.right = this.left + this.width - 1 ;
			this.bottom = this.top + this.height - 1 ;
		} else {
			this.right = right;
			this.bottom = bottom;
			this.width = this.right - this.left + 1 ;
			this.height = this.bottom - this.top + 1 ;
		}
	},
	size: function () {
		var size = this.width * this.height;
		return size >= 0 ? size : 0;
	},
	clone: function (){
		return new zss.Range(this.left, this.top, this.right, this.bottom);
	},
	toString: function(){
		return "left:" + this.left + ",top:" + this.top + ",right:" + this.right + ",bottom:" + this.bottom;
	},
	extendRight: function (size) {
		this.right += size;
		this.width += size;
	},
	extendLeft: function (size) {
		this.left -= size;
		this.width +=size;
	},
	extendBottom: function (size) {
		this.bottom += size;
		this.height += size;
	},
	extendTop: function (size) {
		this.top -= size;
		this.height += size;
	},
	shiftTop: function (size) {
		this.top -= size;
		this.bottom -= size;
	},
	shiftLeft: function (size) {
		this.left -= size;
		this.right -= size;
	},
	shiftRight: function (size) {
		this.left += size;
		this.right += size;
	},
	shiftBottom: function (size) {
		this.top += size;
		this.bottom += size;
	}
});