/* Pos.js

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
 * Pos represent cell's position
 */
zss.Pos = zk.$extends(zk.Object, {
	row: -1,
	column: -1,
	$init: function (row, column) {
		this.$supers('$init', arguments);
		this.row = row;
		this.column = column;
	},
	toString : function(){
		return "row:" + this.row + ", column:" + this.column;
	},
	clone : function(){
		return new zss.Pos(this.row, this.column);
	}
});