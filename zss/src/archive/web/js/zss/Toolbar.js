/* Button.js

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

zss.ResizeableToolbar = zk.$extends(zul.wgt.Toolbar, {
	$init: function (wgt) {
		this.$supers(zss.ResizeableToolbar, '$init', []);
		this._wgt = wgt;
	},
	bind_: function () {
		this.$supers(zss.ResizeableToolbar, 'bind_', arguments);
		zWatch.listen({onSize: this});
	},
	unbind_: function () {
		zWatch.unlisten({onSize: this});
		this.$supers(zss.ResizeableToolbar, 'unbind_', arguments);
	},
	//ZSS-177
	onSize: function () {
		var prevHgh = this._hgh,
			curHgh = jq(this.$n('cave')).height();
		if (!prevHgh || prevHgh != curHgh) {
			var panel = this.parent,
				tb = panel.getTabbox(),
				zkn = jq(this.$n()).zk,
				cv = this._wgt.cave,
				h = curHgh + zkn.sumStyles('tb', jq.paddings) + zkn.sumStyles('tb', jq.borders);
			panel.setHeight(h + 'px');
			
			tb.clearCachedSize_();//clear tabbox vflex height cache
			tb.parent.clearCachedSize_();//clear north vflex height cache
			
			//1. resize top panel (toolbar)
			zWatch.fire('onFitSize', this._wgt, {reverse:true})
			
			//2. resize cave (sheet content)
			this._wgt.clearCachedSize_();//clear sheet cave size
			zFlex.onSize.apply(cv);
			cv.resize();
		}
		this._hgh = curHgh;
	}
});

zss.ToolbarTabpanel = zk.$extends(zul.tab.Tabpanel, {
	$init: function (actions, wgt) {
		this.$supers(zss.ToolbarTabpanel, '$init', []);
		this._wgt = wgt;
		this._actions = actions;//array
	},
	createButtonsIfNotExist: function () {
		var tb = this.toolbar;
		if (!tb) {
			tb = this.toolbar = new zss.ResizeableToolbar(this._wgt);
			var btns = new zss.ButtonBuilder(this._wgt, tb).addAll(this._actions).build();
			for (var i = 0, len = btns.length; i < len; i++) {
				var b = btns[i];
				if (b)
					tb.appendChild(b);
			}
			this.appendChild(tb);
			this.setDisabled(this._wgt.getActionDisabled());
		}
	},
	onShow: function () {
		this.createButtonsIfNotExist();
	},
	setDisabled: function (actions) {
		var tb = this.toolbar;
		if (tb && actions) {
			var btn = tb.firstChild;
			for (; btn; btn = btn.nextSibling) {
				if (!btn.setDisabled) {
					continue;
				}
				btn.setDisabled(actions);
			}	
		}
	},
	getSclass: function () {
		return 'zstbtabpanel';
	}
});

zss.ToolbarTabbox = zk.$extends(zul.tab.Tabbox, {
	$o: zk.$void,
	$init: function (wgt) {
		this.$supers(zss.ToolbarTabbox, '$init', []);
		this._wgt = wgt;
		this.setVflex('min');
		
		var tbs = new zul.tab.Tabs(),
			homeTab = new zul.tab.Tab({
				label: msgzss.action.homePanel,
				sclass: 'zstab-homePanel'
			}),
			insertTab = new zul.tab.Tab({
				label: msgzss.action.insertPanel,
				onClick: this.proxy(this.onClickInsertTab),
				sclass: 'zstab-insertPanel'
			}),
			//TODO: formulaTab = new zul.tab.Tab({
			//	label: msgzss.action.formulaPanel,
			//	onClick: this.proxy(this.onClickFormulaTab)
			//}),
			panels = new zul.tab.Tabpanels(),
			homePanel = this.homePanel = new zss.ToolbarTabpanel(zss.Buttons.HOME_DEFAULT, wgt),
			//TODO: formulaPanel = this.formulaPanel = new zul.tab.Tabpanel(zss.Buttons.FORMULA_DEFAULT),
			insertPanel = this.insertPanel = new zss.ToolbarTabpanel(zss.Buttons.INSERT_DEFAULT, wgt);
//			homeToolbar = new zul.wgt.Toolbar({height: '23px'});

		this.appendChild(tbs);
		this.appendChild(panels);
		
		tbs.appendChild(homeTab);
		tbs.appendChild(insertTab);
		//TODO: tbs.appendChild(formulaTab);

		homePanel.createButtonsIfNotExist();
		panels.appendChild(homePanel);
		panels.appendChild(insertPanel);
		//TODO: panels.appendChild(formulaPanel);
		
		this.setSelectedTab(homeTab);
		
		var tb = new zul.wgt.Toolbar({
				ignoreFlexSize_: function () {
					return true;
				}
			});
		tb.appendChild(new zss.Toolbarbutton({
			$action: 'closeBook',
			tooltiptext: msgzss.action.closeBook,
			image: zk.ajaxURI('/web/zss/img/gray-cross.png', {au: true}),
			onClick: function () {
				wgt.fireToolbarAction('closeBook');
			}
		}));
		this.appendChild(tb);
	},
	onClickInsertTab: function () {
		this.insertPanel.createButtonsIfNotExist();
	},
	onClickFormulaTab: function () {
		this.formulaPanel.createButtonsIfNotExist();
	}
});

zss.Toolbar = zk.$extends(zul.layout.North, {
	$o: zk.$void,
	$init: function (wgt) {
		this.$supers(zss.Toolbar, '$init', []);
		this.setBorder(0);
		this._wgt = wgt;
		this.setVflex('min');
		
		this.appendChild(this.toolbarTabbox = new zss.ToolbarTabbox(wgt));
	},
	setDisabled: function (actions) {
		var tb = this.toolbarTabbox,
			panels = tb.getTabpanels(),
			panel = panels.firstChild;
		for (; panel; panel = panel.nextSibling) {
			panel.setDisabled(actions);
		}
	},
	//	ZSS-177
	setFlexSize_: function(sz, isFlexMin) {
		var sz = this.$supers(zss.Toolbar, 'setFlexSize_', arguments),
			ss = this.$n('real').style,//to compitiable with ZK7		
			sh = ss.height,
			$cv = jq(this.$n('cave'));
		if (sh && $cv.height() != parseInt(sh)) {
			$cv.height(sh + 'px');
		}
		return sz;
	}
});
})();