/* Id.js

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
 *  Id
 */
zss.Id = zk.$extends(zk.Object, {
	last: 0,
	increase: 1,
	$init: function (init, increase) {
		this.$supers('$init', arguments);
		this.last = init;
		this.increase = increase;
	},
	next: function () {
		this.last += this.increase;
		return this.last;
	}
});