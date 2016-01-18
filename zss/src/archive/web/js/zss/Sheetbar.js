/* Sheetbar.js

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
	
zss.SheetMenupopup = zk.$extends(zul.menu.Menupopup, {
	$o: zk.$void,
	$init: function (wgt) {
		this.$supers(zss.SheetMenupopup, '$init', []);
		this._wgt = wgt;
		
		var sheet = this.sheet = wgt.sheetCtrl,
			del = this.deleteSheet = new zss.Menuitem({
				$action: 'deleteSheet',
				label: msgzss.action.deleteSheet
			}, wgt),
			rename = this.renameSheet = new zss.Menuitem({
				$action: 'renameSheet',
				label: msgzss.action.renameSheet
			}, wgt),
			copy = this.copySheet = new zss.Menuitem({
				$action: 'copySheet',
				label: msgzss.action.copySheet
			}, wgt),
			protect = this.protectSheet = new zss.Menuitem({
				$action: 'protectSheet',
				label: msgzss.action.protectSheet, 
				checkmark: true
			}, wgt),
			hide = this.hide = new zss.Menuitem({
				$action: 'hideSheet',
				label: msgzss.action.hideSheet
			}, wgt),
			unhide = this.unhide = new zss.Menuitem({
				$action: 'unhideSheet',
				label: msgzss.action.unhideSheet
			}, wgt);
			moveLeft = this.moveLeft = new zss.Menuitem({
				$action: 'moveSheetLeft',
				label: msgzss.action.moveSheetLeft
			}, wgt),
			moveRight = this.moveRight = new zss.Menuitem({
				$action: 'moveSheetRight',
				label: msgzss.action.moveSheetRight
			}, wgt);
		
		this.appendChild(del);
		this.appendChild(rename);
		this.appendChild(copy);
		this.appendChild(protect);
		
		this.appendChild(new zul.menu.Menuseparator());
		this.appendChild(hide);
		this.appendChild(unhide);
		this.appendChild(moveLeft);
		this.appendChild(moveRight);
	},
	doMouseDown_: function () {
		//eat event, if propagate to Spreadsheet will cause fail to click Menuitem
	},
	doMouseUp_: function () {
		//eat event, if propagate to Spreadsheet will cause fail to click Menuitem
	},
	onClickDeleteSheet: function () {
		this._wgt.fireSheetAction('delete');
	},
	onClickRenameSheet: function () {
		var sheetBar = this._wgt._sheetBar;
		if (sheetBar) {
			var tab = sheetBar.getSheetSelector().getSelectedTab();
			if (tab)
				tab.startEditing();
		}
	},
	onClickCopySheet: function() {
		this._wgt.fireSheetAction('copy');
	},
	onClickProtectSheet: function () {
		this._wgt.fireSheetAction('protect');
	},
	onClickHideSheet: function () {
		this._wgt.fireSheetAction('hide');
	},
	onClickUnhideSheet: function () {
		this._wgt.fireSheetAction('unhide');
	},
	onClickMoveSheetLeft: function () {
		this._wgt.fireSheetAction('moveLeft');
	},
	onClickMoveSheetRight: function () {
		this._wgt.fireSheetAction('moveRight');
	},
	setProtectSheetCheckmark: function (b) {
		this.protectSheet.setChecked(b);
	},
	bind_: function () {
		this.$supers(zss.SheetMenupopup, 'bind_', arguments);
		
		// ZSS-601: listen onClick here, not just attach listener to menu item
		// ZK replace DOM every time when using IE version less than 11
		// so, it will invoke unbind_() first then bind_()
		this.moveRight.listen({'onClick': this.proxy(this.onClickMoveSheetRight)});
		this.moveLeft.listen({'onClick': this.proxy(this.onClickMoveSheetLeft)});
		this.unhide.listen({'onClick': this.proxy(this.onClickUnhideSheet)});
		this.hide.listen({'onClick': this.proxy(this.onClickHideSheet)});
		this.protectSheet.listen({'onClick': this.proxy(this.onClickProtectSheet)});
		this.copySheet.listen({'onClick': this.proxy(this.onClickCopySheet)});
		this.renameSheet.listen({'onClick': this.proxy(this.onClickRenameSheet)});
		this.deleteSheet.listen({'onClick': this.proxy(this.onClickDeleteSheet)});
	},
	unbind_: function () {
		this.deleteSheet.unlisten({'onClick': this.proxy(this.onClickDeleteSheet)});
		this.renameSheet.unlisten({'onClick': this.proxy(this.onClickRenameSheet)});
		this.copySheet.unlisten({'onClick': this.proxy(this.onClickCopySheet)});
		this.protectSheet.unlisten({'onClick': this.proxy(this.onClickProtectSheet)});
		this.hide.unlisten({'onClick': this.proxy(this.onClickHideSheet)});
		this.unhide.unlisten({'onClick': this.proxy(this.onClickUnhideSheet)});
		this.moveLeft.unlisten({'onClick': this.proxy(this.onClickMoveSheetLeft)});
		this.moveRight.unlisten({'onClick': this.proxy(this.onClickMoveSheetRight)});
		
		this.$supers(zss.SheetMenupopup, 'unbind_', arguments);
	},
	open: function (ref, offset, position, opts) {
		var wgt = this._wgt,
			sheetName = ref.getLabel(),
			sheetLabels = wgt.getSheetLabels(),
			i = 0,
			len = sheetLabels.length;
		for (; i < len; i++) {
			var obj = sheetLabels[i];
			if (obj.name == sheetName) {
				break;
			}
		}
		if (this.moveLeft.isDisabled)
		this.moveLeft.setDisabled(i == 0 || this._moveLeftDisabled);//the first sheet not allow move left, ZSS-1099
		this.moveRight.setDisabled(i == len - 1 || this._moveRightDisabled);//the last sheet not allow move right, ZSS-1099
		
		this.protectSheet.setChecked(wgt.isProtect());
		
		position = 'before_start';
		this.$supers(zss.SheetMenupopup, 'open', arguments);
	},
	setDisabled: function (actions){
		var chd = this.firstChild;
		for (;chd; chd = chd.nextSibling) {
			if (!chd.setDisabled) {//Menuseparator
				continue;
			}
			
			chd.setDisabled(actions);
		}
		//ZSS-1099
		this._moveLeftDisabled = this.moveLeft.isDisabled();
		this._moveRightDisabled = this.moveRight.isDisabled();
	}
});

zss.SheetTab = zk.$extends(zul.tab.Tab, {
	$o: zk.$void, //owner, fellows relationship no needed
	$init: function (arg, wgt) {
		this.$supers(zss.SheetTab, '$init', [arg]);
		this._wgt = wgt;
		this._selector = arg.selector;
		this.appendChild(this.textbox = new zul.inp.Textbox({
			visible: false,
			onBlur: this.proxy(this.onStopEditing), // ZSS-308: spec. changed > do rename process when blurring 
			onOK: this.proxy(this.onStopEditing), // send by afterKeyDown_(), no matter event propagation is stopped
			onCancel: this.proxy(this.onCancelEditing), // send by afterKeyDown_(), no matter event propagation is stopped
			afterKeyDown_: this.proxy(function(evt){
				// editing doesn't need to be handled by CtrlKey detection in zss.Spreadsheet
				// here we let ESC(27) ENTER(13) to be handled by Spreadsheet in order to get onOK and Cancel behavior
				if(!this.editing || evt.keyCode === 27 || evt.keyCode === 13)
					this.$supers(zul.inp.InputWidget, 'afterKeyDown_', arguments);
			}),
			sclass: 'zssheettab-rename-textbox'
		}));
	},
	$define: {
		sheetUuid: null
	},
	domContent_: function () {
		var uid = this.uuid,
			scls = this.getSclass(),
			html = '<div id="' + uid + '-text" class="' + scls + '-text">' + this.getLabel() + '</div>' +
				this.textbox.redrawHTML_();
		return html;
	},
	onStopEditing: function () {
		// ZSS-308: if it's not in editing status, don't rename.
		// this prevents rename at non-editing statues
		// e.g: if server is slow enough, press Enter make ZK fire onOK and onBlur sequentially
		if (!this.editing) {
			return;
		}
		
		var name = this.getLabel(),
			text = this.textbox.getText();
		// ZSS-609: let component determine if the sheet name is legal
		//if (!text)
		//	return;
		
		if (name != text) {
			var wgt = this._wgt;
			this._wgt.fireSheetAction('rename', {name: text});
		}
		this.stopEditing();
	},
	onCancelEditing: function () {
		this.textbox.setText(this.getLabel());
		this.stopEditing();
	},
	stopEditing: function () {
		this.textbox.setVisible(false);
		this.editing = false;
		jq(this.getTextNode()).css('display', 'block');
	},
	startEditing: function () {
		var tb = this.textbox,
			val = this.getLabel();
		jq(this.getTextNode()).css({'display': 'none'});
		tb.setValue(val);
		tb.setVisible(true);
		tb.focus_();
		tb.select(0, val.length);
		this.editing = true;
	},
	isEditing: function () {
		return !!this.editing;
	},
	doDoubleClick_: function () {
		// ZSS-908 support disabled from server command
		if(this._selector.renameDisabled)
			return;
		var editing = this.isEditing();
		if (editing) {
			var tb = this.textbox;
			tb.select(0, tb.getText().length);
		} else {
			this.startEditing();
		}
	},
	//ZSS-1080: tricky. Avoid doClick_ proceed (until formula check on server is done)
	doClick_: function (event) {
		var sheet = this._wgt.sheetCtrl;
	
		if (sheet.asyncCheckFormula) { // is checking formula on server
			sheet.asyncCheckFormula = false;
			var sheetTab = this,
				event0 = event;
			sheet.sheetTabFn = function () 
				{ sheetTab.$supers(zss.SheetTab, 'doClick_', [event0]);};
			return;
		}
	
		this.$supers(zss.SheetTab, 'doClick_', arguments);
	},
	doMouseDown_: function (event) {
		//TODO: spreadsheet shall remain focus when mouse down on SheetTab
		//eat event
		var sheet = this._wgt.sheetCtrl;
		if (sheet.editingFormulaInfo) {
			sheet.isSwitchingSheet = true;
		}
	},
	doMouseUp_: function () {
		//eat event
	},
	doKeyDown_: function () {
		//eat event, or zss.SSheetCtrl might stop the event propagation and make Textbox no input 
	},
	afterKeyDown_: function () {
		//eat event, or zss.SSheetCtrl might stop the event propagation and make Textbox no input 
	},
	doKeyUp_: function () {
		//eat event, or zss.SSheetCtrl might stop the event propagation and make Textbox no input 
	},
	doKeyPress_: function () {
		//eat event, or zss.SSheetCtrl might stop the event propagation and make Textbox no input 
	},
	getTextNode: function () {
		return this.$n('text');
	},
	getSclass: function () {
		return 'zssheettab';
	}
});

// ZSS-858
zss.Tabs = zk.$extends(zul.tab.Tabs, {
	_scrollcheck: function(way, tb) {
		this._shallCheck = false;
		var tabbox = this.getTabbox();
		if (!this.desktop || 
				(tabbox && (!tabbox.isRealVisible() || !tabbox.isTabscroll())))
			return;

		var tabs = this.$n(),
			tbx = tabbox.$n();

		if (!tabs || !tbx) 
			return;	// tabbox is delete , no need to check scroll

		if (tabbox.isVertical()) { //vertical
			var tabsOffsetHeight = tabs.offsetHeight,
				tabsScrollTop = tabs.scrollTop,
				childHeight = 0;
			
			jq(this.$n('cave')).children().each(function () {
				childHeight += this.offsetHeight;
			});

			if (tabbox._scrolling) { //already in scrolling status
				var btnsize = this._getArrowSize();
				if (tabs.offsetHeight <= btnsize)  return;
				
				var sel = tabbox.getSelectedTab(),
					node = tb ? tb.$n() : (sel ? sel.$n() : null),
					nodeOffsetTop = node ? node.offsetTop : 0,
					nodeOffsetHeight = node ? node.offsetHeight : 0;
					
				if (childHeight <= tabsOffsetHeight + btnsize) {
					tabbox._scrolling = false;
					this._showbutton(false)
					tabs.style.height = jq.px0(tbx.offsetHeight-2);
					tabs.scrollTop = 0;
				}
				switch (way) {
				case 'end':
					var d = childHeight - tabsOffsetHeight - tabsScrollTop;
					this._doScroll(d >= 0 ? 'down' : 'up', d >= 0 ? d : Math.abs(d));
					break;
				case 'init':
				case 'vsel':
					if (nodeOffsetTop < tabsScrollTop) {
						this._doScroll('up', tabsScrollTop - nodeOffsetTop);
					} else if (nodeOffsetTop + nodeOffsetHeight > tabsScrollTop + tabsOffsetHeight) {
						this._doScroll('down', nodeOffsetTop + nodeOffsetHeight - tabsScrollTop - tabsOffsetHeight);
					}
					break;
				}
			} else { // not enough tab to scroll
				if (childHeight - tabsOffsetHeight > 0) {
					tabbox._scrolling = true;
					this._showbutton(true);
					var btnsize = this._getArrowSize(),
						temp = tbx.offsetHeight - btnsize;
					tabs.style.height = temp > 0 ? temp + 'px' : '';
					if (way == 'end') {
						var d = childHeight - tabsOffsetHeight - tabsScrollTop + 2;
						if (d >= 0)
							this._doScroll(this.uuid, 'down', d);
					}
				} else {
					this._showbutton(false);
				}
			}
		} else if(!tabbox.inAccordionMold()) {
			var cave = this.$n('cave'),
			 	sel = tabbox.getSelectedTab(),
				node = tb ? tb.$n() : ( sel ? sel.$n() : null),
			 	nodeOffsetLeft = node ? node.offsetLeft : 0,
				nodeOffsetWidth = node ? node.offsetWidth : 0,
				tabsOffsetWidth = tabs.offsetWidth,
				tabsScrollLeft = tabs.scrollLeft,
				childWidth = 0,
				toolbar = tabbox.toolbar,
				toolbarWidth = 0;

			jq(cave).children().each(function () {
				childWidth += this.offsetWidth;
			});
			
			if (toolbar && toolbar.desktop)
				toolbarWidth = toolbar.$n().offsetWidth;
			
			if (tabbox._scrolling) { //already in scrolling status
				var btnsize = this._getArrowSize();
				tabbox.$n('right').style.right = toolbarWidth + 'px';
				
				if (tabs.offsetWidth <= btnsize) return;
				if (childWidth <= tabsOffsetWidth + btnsize) {
					tabbox._scrolling = false;
					this._showbutton(false);
					tabs.style.width = jq.px0(tbx.offsetWidth - toolbarWidth);
					tabs.scrollLeft = 0;
				}
				// scroll to specific position
				switch (way) {
				case 'end':
					var d = childWidth - tabsOffsetWidth - tabsScrollLeft;
					this._doScroll(d >= 0 ? 'right' : 'left', d >= 0 ? d : Math.abs(d));
					break;
				case 'init':
				case 'sel':
					if (nodeOffsetLeft == tabsScrollLeft) // nothing to do
						break;
					
					if (nodeOffsetLeft < tabsScrollLeft) {
						this._doScroll('left', tabsScrollLeft - nodeOffsetLeft);
					} else if (nodeOffsetLeft + nodeOffsetWidth > tabsScrollLeft + tabsOffsetWidth) {
						this._doScroll('right', nodeOffsetLeft + nodeOffsetWidth - tabsScrollLeft - tabsOffsetWidth);
					}
					break;
				}
			} else { // not enough tab to scroll
				if (childWidth - tabsOffsetWidth > 0) {
					tabbox._scrolling = true;
					this._showbutton(true);
					var cave = this.$n('cave'),
						btnsize = this._getArrowSize(),
						temp = tbx.offsetWidth - toolbarWidth - btnsize;//coz show button then getsize again
					cave.style.width = '33554430px'; // maximum width for chrome
					tabs.style.width = temp > 0 ? temp + 'px' : '';
					tabbox.$n('right').style.right = toolbarWidth + 'px';
					
					if (way == 'sel') {
						if (nodeOffsetLeft < tabsScrollLeft) {
							this._doScroll('left', tabsScrollLeft - nodeOffsetLeft);
						} else if (nodeOffsetLeft + nodeOffsetWidth > tabsScrollLeft + tabsOffsetWidth) {
							this._doScroll('right', nodeOffsetLeft + nodeOffsetWidth - tabsScrollLeft - tabsOffsetWidth);
						}
					}
				} else {
					this._showbutton(false);
				}
			}
		}
	}
});

zss.SheetSelector = zk.$extends(zul.tab.Tabbox, {
	$o: zk.$void,
	$init: function (wgt, menu) {
		this.$supers(zss.SheetSelector, '$init', []);
		this._wgt = wgt;
		// ZSS-601: don't register context menu on Tabbox, just on Tab
		// and _context field should be a UUID string
		this._menu = menu; 
		this.setSheetLabels(wgt.getSheetLabels());
		this._orient = 'bottom';
	},
	setSheetLabels: function (labels) {
		var wgt = this._wgt,
			tabs = this.tabs,
			menu = this._menu,
			selTab = null,
			clkFn = this.proxy(this._onSelectSheet);
		if (tabs)
			tabs.detach();
		
		tabs = new zss.Tabs();
		for (var i = 0, len = labels.length; i < len; i++) {
			var obj = labels[i],
				tab = new zss.SheetTab({'label': obj.name, 'sheetUuid': obj.id, 
				'onClick': clkFn, 'onRightClick': clkFn, selector: this}, wgt);
			tab.setContext(menu);
			tabs.appendChild(tab);
			
			if (obj.sel)
				selTab = tab;
		}
		this.appendChild(tabs);
		if (selTab)
			this.setSelectedTab(selTab);
	},
	//when select different sheet, detach current sheet's widgets
	_detachSheetWidget: function () {
		var wgt = this._wgt,
			n = wgt.sheetCtrl.$n('wp');
		jq(n).children().each(function (i, n) {
			var w = zk.Widget.$(n.id);
			if (w) {
				w.detach();
			}
		});
	},
	_onSelectSheet: function (evt) {
		var tab = evt.target;
		if (!tab.$instanceof(zss.SheetTab)) {
			return;
		}
		this.doSelectSheet(tab.getSheetUuid(), this._wgt.sheetCtrl.isSwitchingSheet);
	},
	doSelectSheet: function (sheetId, ignoreStatus) {

		var	wgt = this._wgt,
			sheet = wgt.sheetCtrl,
			currSheetId = wgt.getSheetId();
		if (sheetId != currSheetId) {
			var useCache = false,
				row = -1, col = -1,
				left = -1, top = -1, right = -1, bottom = -1,
				hleft = -1, htop = -1, hright = -1, hbottom = -1,
				frow = -1, fcol = -1;
			
			this._detachSheetWidget();
			
			//For wgt.isSheetCSSReady() to work correctly.
			//when during select sheet in client side, server send focus Au command response first (set attributes later), 
			// _sheetId will be last selected sheet, cause isSheetCSSReady() doesn't work correctly 
			wgt._invalidatedSheetId = true;
			
			this.setDisabled(true);//shall not allow user to select sheet during loading sheet
			
			var cacheCtrl = wgt._cacheCtrl;
			if (cacheCtrl) {
				cacheCtrl.snap(currSheetId);//snapshot current sheet status
				if (cacheCtrl.isCached(sheetId)) {
					
					//restore target sheet status: focus, selection etc..
					var snapshop = cacheCtrl.getSnapshot(sheetId),
						visRng = snapshop.getVisibleRange(),
						focus = snapshop.getFocus(),
						sel = snapshop.getSelection(),
						hsel = snapshop.getHighlight(),
						dv = snapshop.getDataValidations(),
						af = snapshop.getAutoFilter(),
						tbafs = snapshop.getTableFilters(), //ZSS-988
						frow = snapshop.getRowFreeze(),
						fcol = snapshop.getColumnFreeze(),
						maxRows = snapshop.getMaxRows(), //ZSS-1082
						maxColumns = snapshop.getMaxColumns(); //ZSS-1082
						
					//ZSS-1082
					wgt.setMaxRows(maxRows);
					wgt.setMaxColumns(maxColumns);
					
					if (focus && !ignoreStatus) {
						row = focus.row;
						col = focus.column;
					}
					if (sel && !ignoreStatus) {
						left = sel.left;
						top = sel.top;
						right = sel.right;
						bottom = sel.bottom;
					}
					if (hsel && !ignoreStatus) { //highlight
						hleft = hsel.left;
						htop = hsel.top;
						hright = hsel.right;
						hbottom = hsel.bottom;
					}
					if (dv && !ignoreStatus) {
						wgt.setDataValidations(dv);
					} else if (wgt.setDataValidations) {
						wgt.setDataValidations(null);
					}
					if (af && !ignoreStatus) {
						wgt.setAutoFilter(af);
					} else if (wgt.setAutoFilter) {
						wgt.setAutoFilter(null);
					}
					//ZSS-988
					if (tbafs && !ignoreStatus) {
						wgt.setTableFilters(tbafs);
					} else if (wgt.setTableFilters) {
						wgt.setTableFilters(null);
					}
					wgt.setSheetId(sheetId, false, visRng);//replace widgets: cells, headers etc..
					
					//restore sheet last focus/selection
					if (row >= 0 && col >= 0 && !ignoreStatus) {
						sheet.moveCellFocus(row, col);
						sheet.moveCellSelection(left, top, right, bottom);
					}
					useCache = true;
				}
			}
			
			sheet.hideCellFocus();
			sheet.hideCellSelection();
			if (sheet.isHighlightVisible()) {
				sheet.hideHighlight(true);
			}
			
			wgt.fire('onSheetSelect', 
				{sheetId: sheetId, cache: useCache, 
				row: row, col: col, 
				left: left, top: top, right: right, bottom: bottom,
				hleft: hleft, htop: htop, hright: hright, hbottom: hbottom,
				frow: frow, fcol: fcol}, {toServer: true});
		}
	},
	//shall invoke this at the end of process setSelectedSheet
	setSelectedSheet: function (sheetId) {
		this.setProtectSheetCheckmark(this._wgt.isProtect());
		
		var tab = this.tabs.firstChild;
		for (;tab; tab = tab.nextSibling) {
			if (sheetId == tab.getSheetUuid()) {
				var sheet = this._wgt.sheetCtrl;
				if (sheet && !sheet.isSwitchingSheet) { //ZSS-1037: side-effect of ZSS-762
					// ZSS-762: blur spreadsheet to prevent it refocuses to inline editor
					sheet.dp._doFocusLost();
				}
				this.setSelectedTab(tab);
				break;
			}
		}
		
		if (!!this._disd) {
			this.setDisabled(false);
		}
	},
	setProtectSheetCheckmark: function (b) {
		this._menu.setProtectSheetCheckmark(b);
	},
	setDisabled: function (b) {
		// var cur = !!this._disd;
		// if (cur != b) {
		// 	this._disd = b;
		// 	var tab = this.tabs.firstChild;
		// 	for (;tab; tab = tab.nextSibling) {
		// 		tab.setDisabled(b);
		// 	}
		// }

		// ZSS-908 support disabled from server command
		var renameDisabled = false;
		if (jq.isArray(b)) {
			for(var i = 0, length = b.length; i < length; i++) {
				if(b[i] === 'renameSheet') {
					renameDisabled = true;
					break;
				}
			}	
			this.renameDisabled = renameDisabled;
		}
	},
   	redrawHTML_: function () {
   		return this.$supers(zss.SheetSelector, 'redrawHTML_', arguments);
   	},
   	getSclass: function () {
   		return 'zssheetselector';
   	}
});

zss.TabPopup = zk.$extends(zul.wgt.Popup, {
	$init: function() {
		this.$supers(zss.TabPopup, '$init', arguments);
		this.setSclass('zstab-popup');
	},
	doMouseDown_: function () {
		// cancel bubble
	},
	doMouseUp_: function () {
		// cancel bubble
	},
	bind_: function () {
		this.$supers(zss.TabPopup, 'bind_', arguments);
		this.domListen_(this.$n('cave'), 'onClick', '_doClick');
	},
	unbind_: function () {
		this.domUnlisten_(this.$n('cave'), 'onClick', '_doClick');
		this.$supers(zss.TabPopup, 'unbind_', arguments);
	},
	_doClick: function(evt) {
		var target = evt.target;
		if(target.widgetName == 'TabOption') {
			var sheetpanelCave = this.parent.parent;
			sheetpanelCave.sheetSelector.doSelectSheet(target.getSheetId(), 
					sheetpanelCave._wgt.sheetCtrl.isSwitchingSheet);
			this.close();
		}
	},
	afterOpenAnima_: function (ref, offset, position, opts) {
		this.$supers(zss.TabPopup, 'afterOpenAnima_', arguments);
		if (opts && opts.tabOption) {
			opts.tabOption.scrollIntoView();
		}
	}
});

zss.TabOption = zk.$extends(zul.wgt.Button, {
	$init: function() {
		this.$supers(zss.TabOption, '$init', arguments);
		this.setZclass('zstab-option');
	},
	widgetName: 'TabOption',
	$define: {
		sheetId: function(){}
	}
});

zss.SheetpanelCave = zk.$extends(zk.Widget, {
	$o: zk.$void,
	$init: function (wgt) {
		this.$supers(zss.SheetpanelCave, '$init', []);
		this.setHflex(true);
		this.setHeight("100%");
		this._wgt = wgt;
		
		var menu = this.menu = new zss.SheetMenupopup(wgt),
			addSheetBtn = this.addSheetButton = new zss.Toolbarbutton({
				$action: 'addSheet',
				tooltiptext: msgzss.action.addSheet,
				image: zk.ajaxURI('/web/zss/img/plus.png', {au: true}),
				onClick: this.proxy(this.onClickAddSheet)
			}),
			sheetMenuBtn = this.sheetMenuButton = new zss.Toolbarbutton({
				$action: 'showSheets',
				tooltiptext: msgzss.action.showSheets,
				image: zk.ajaxURI('/web/zss/img/ui-menu.png', {au: true}),
				onClick: function() {
					var labels = wgt.getSheetLabels(),
						inner = [],
						sheetId = wgt.getSheetId(),
						selected;
					popup.clear();
					for(var i = 0, label; label = labels[i++];) {						
						var labelId = label.id,
							tabOption = new zss.TabOption({
								label: label.name, 
								sheetId: label.id,
								sclass: label.id === sheetId ? 'zstab-option-select' : ''
							});
						popup.appendChild(tabOption);
						if (label.id === sheetId) {
							selected = tabOption;
						}
					}
					//will call into zss.TabPopup.afterOpenAnima_()
					popup.open(this, null, 'before_start', {'tabOption':selected});
				}
			}),
			hlayout = this.hlayout = new zul.box.Hlayout({spacing: 0}),
			sheetSelector = this.sheetSelector = new zss.SheetSelector(wgt, menu),
			popup = new zss.TabPopup(),
			addColBtn = this.addColButton = new zss.Toolbarbutton({ //ZSS-1082
				$action: 'addCol',
				tooltiptext: msgzss.action.addCol,
				image: zk.ajaxURI('/web/zss/img/control-stop-000-small.png', {au: true}),
				onClick: this.proxy(this.onClickAddCol)
			}),
			addRowBtn = this.addRowButton = new zss.Toolbarbutton({ //ZSS-1082
				$action: 'addRow',
				tooltiptext: msgzss.action.addRow,
				image: zk.ajaxURI('/web/zss/img/control-stop-270-small.png', {au: true}),
				onClick: this.proxy(this.onClickAddRow)
			});
		
		hlayout.appendChild(addSheetBtn);
		hlayout.appendChild(sheetMenuBtn);
		hlayout.appendChild(sheetSelector);
		hlayout.appendChild(addColBtn); //ZSS-1082
		hlayout.appendChild(addRowBtn); //ZSS-1082
		hlayout.appendChild(popup);
		
		this.appendChild(hlayout);
		this.appendChild(menu);
	},
	setFlexSize_: function(sz, isFlexMin) {
		var r = this.$supers(zss.SheetpanelCave, 'setFlexSize_', arguments),
			width = sz.width,
			btnWidth = 26 * 2 + 18 * 2; //ZSS-1082: button size, TODO: rm hard-code: jq(this.hlayout.$n().firstChild).width() get wrong value;

		if (width > btnWidth)
			this.sheetSelector.setWidth((width - btnWidth) + 'px');
	},
	onClickAddSheet: function () {
		this._wgt.fireSheetAction("add");
	},
	//ZSS-1082
	onClickAddRow: function () {
		this._wgt.fireRowAction("add");
	},
	//ZSS-1082
	onClickAddCol: function () {
		this._wgt.fireColAction("add");
	},
	//ZSS-1082
	bind_: function () {
		this.addColButton.setVisible(this._wgt._addColumn);
		this.addRowButton.setVisible(this._wgt._addRow);
		this.$supers(zss.SheetpanelCave, 'bind_', arguments);
	},
	redraw: function (out) {
		var uid = this.uuid;
		out.push('<div', this.domAttrs_(), '>');
		
		var chd = this.firstChild;
		for (;chd;chd = chd.nextSibling) {
			chd.redraw(out);
		}
		out.push('</div>');
	},
	setDisabled: function (actions){
		// TODO: consider applying sheet selector's and sheet menu's disabled. 
		this.addSheetButton.setDisabled(actions);
		this.sheetSelector.setDisabled(actions);
		this.menu.setDisabled(actions);
	}
});

zss.Sheetbar = zk.$extends(zul.layout.South, {
	$o: zk.$void,
	$init: function (wgt) {
		this.$supers(zss.Sheetbar, '$init', []);
		this._wgt = wgt;
		this.setBorder(0);
		this.setSize('28px');
		
		this.appendChild(this.cave = new zss.SheetpanelCave(wgt));
		
		this.setDisabled(wgt.getActionDisabled());
	},
	getSheetSelector: function () {
		return this.cave.sheetSelector;
	},
   	redrawHTML_: function () {
   		return this.$supers(zss.Sheetbar, 'redrawHTML_', arguments);
   	},
   	getSclass: function () {
   		return 'zssheetbar';
   	},
   	setDisabled: function (actions){
   		this.cave.setDisabled(actions);
   	}
});
})();