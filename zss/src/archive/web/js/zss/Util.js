/* Util.js

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
 * Static functions for backward compatible
 */
zss.Util = zk.$extends(zk.Object, {}, {
	_vpts: {},
	$parent: function (n) {
		var p = zss.Util._vpts[n.id];
		return p ? p : n.parentNode;
	}
});