/* Configuration.js

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
 * A Configuration is used for spreadsheet configuration setting
 */
zss.Configuration = zk.$extends(zk.Object, {
	textOverflow: true,
	prune: true,
	readonly: false,
	hideBorder: false
});