/* Info.js

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
 * Info is used for display current header index information
 */
zss.Info = zk.$extends(zk.Object, {
	$init: function (sheet, cmp) {
		this.$supers('$init', arguments);
		this.sheet = sheet;
		this.comp = cmp;
		this.txtcomp = jq(cmp).children("SPAN:first")[0];
		this.visible = false;
		this.displaytime = 2000;
		this.showcount = 0;
		this.enabled = true;
		cmp.ctrl = this;
		this.lastautohide;
	},
	cleanup: function () {
		this.invalid = true;
		this.visible = this.enabled = false;
		
		if(this.comp) this.comp.ctrl = null;
		this.sheet = this.comp = this.txtcmop = null;
	},
	/**
	 * Sets info
	 * @param boolean enabled
	 */
	setEnabled: function (enabled) {
		if(this.enabled == enabled) return;
		this.enabled = enabled;
		if(!enabled)
			this.hideInfo();
	},
	/**
	 * Display info
	 * @param boolean autohide
	 */
	showInfo: function (autohide) {
		if (!this.enabled) return ;
		this.lastautohide = autohide;
		this.visible = true;
		jq(this.comp).css('display', 'inline');
		
		if (autohide) {
			var local = this;
			this.showcount++;
			setTimeout(function () {
				local.showcount -- ;
				if (local.showcount <= 0) {
					setTimeout(function () {
						if (local.showcount > 0) return;
						if (!local.lastautohide) return;
						local.hideInfo();
					}, local.displaytime);	
				}
			},300);
		}
	},
	/**
	 * Sets info text
	 * @param string txt
	 */
	setInfoText: function (txt) {
		var txtcmp = this.txtcomp;
		txt = txt.trim();
		jq(txtcmp).text(txt);
	},
	/**
	 * Hides info
	 */
	hideInfo: function(){
		if (!this.visible) return;
		this.visible = false;
		jq(this.comp).css('display', 'none');
	}
});