/* Formulabar.js

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Dec 8, 2011 9:43:24 AM , Created by sam
}}IS_NOTE

Copyright (C) 2011 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
(function () {

zss.Namebox = zk.$extends(zk.Widget, {
   	//indicate whether redraw name items or not
   	_redrawNames: false,
   	$init: function (wgt) {
   		this.$supers(zss.Namebox, '$init', []);
   		this._names = [];
   		this._wgt = wgt;
   	},
   	$define: {
		/** Returns whether it is disabled.
		 * <p>Default: false.
		 * @return boolean
		 */
		/** Sets whether it is disabled.
		 * @param boolean disabled
		 */
   		disabled: function (disabled) {
   			var n = this.$n();
   			if (n) {
   				jq(n)[disabled ? 'addClass' : 'removeClass'](this.getZclass() + '-disd');
   			}
   		}
   	},
   	bind_: function () {
   		this.$supers(zss.Namebox, 'bind_', arguments);
   		this.sheet = this._wgt.sheetCtrl;
   	},
   	unbind_: function () {
   		this._wgt = this.sheet = null;
   		this.$supers(zss.Namebox, 'unbind_', arguments);
   	},
   	setValue: function (v) {
   		var inp = this.$n('inp');
   		inp.value = v;
   	},
   	setNames: function (ary) {
   		//{v: value, range: {top: 0, right: 0, bottom: 0, right: 0}}
   		this._redrawNames = false;
   		var ns = this._names = [],
   			i = ary.length;
   		while (i--) {
   			ns[i] = ary[i];
   		}
   		if (this._isOpenPopup()) {
			this._appendNames();
   		}
   	},
   	_appendNames: function () {
		var $pp = jq(this.$n('pp'));
		$pp.children().remove();
		$pp.append(this._redrawNamesHTML_());
		this._redrawNames = true;
   	},
   	_fixPopupSize: function () {
   		var size = this._names.length,
   			pp = this.$n('pp');
   		if (!size) {
   			pp.style.height = '150px';//empty popup default height
   		}
   		pp.style.width = jq(this.$n()).width() + 'px';
   	},
   	_isOpenPopup: function () {
   		return zk(this.$n('pp')).isVisible();
   	},
   	_closePopup: function () {
   		var pp = this.$n('pp');
   		zk(pp).undoVParent();
   		pp.style.display = 'none';
   	},
   	_openPopup: function () {
   		var created = this._redrawNames,
   			pp = this.$n('pp');
   		if (!created) {
   			this._appendNames();
   		}
   		zk(pp).makeVParent();
   		zk(pp).position(this.$n(), 'after_start'); //TODO: add offset
   		pp.style.display = 'block';
   		this._fixPopupSize();
   		
   		var $n = this.$n('inp');
   		setTimeout(function () {
   			$n.focus();
   			$n.select();
   		});
   	},
   	doMouseDown_: function (evt) {
   		if (this.isDisabled()) {
   			this.$n('inp').blur();
   			evt.stop();
   			return;
   		}
   		var sheet = this.sheet;
   		if (sheet) {
   			if (sheet.state == zss.SSheetCtrl.FOCUSED) {
   	   	   		var target = evt.domTarget,
		   			inp = this.$n('inp'),
		   			icon = this.$n('icon');
		   		if (inp == target) {
		   			setTimeout(function () {
		   				jq(inp).select();
		   			});
		   		} else if (icon == target) {
		   			this._openPopup();
		   		} else if (jq.isAncestor(this.$n('pp'), target)) {
		   			this._closePopup();
		   		}	
   			} else if (sheet.state == zss.SSheetCtrl.NOFOCUS) {
				var p = sheet.getLastFocus(),
					row = p.row,
					col = p.column;
				sheet.dp.moveFocus(row == -1 ? 0 : row, col == -1 ? 0 : col);
   			}
   		}
   	},
   	doMouseOver_: function (evt) {
   		var t = evt.domTarget,
   			pp = this.$n('pp'),
   			clsOver = 'zsnamebox-item-over';
   		if (pp != t && jq.isAncestor(pp, t)) {
   			jq(pp).children('.' + clsOver).removeClass(clsOver);
   			jq(t).addClass(clsOver);
   		}
   	},
   	setWidth: function (v) {
   		this.$supers(zss.Namebox, 'setWidth', arguments);
   		this._fixWidth(v);
   	},
   	_fixWidth: function (v) {
   		var n = this.$n(),
   			totalWidth = zk.parseInt(v),
   			inp = n.firstChild,
   			icon = inp.nextSibling;
   		inp.style.width = jq.px(totalWidth - icon.clientWidth);		    		
   	},
   	bind_: function () {
   		this.$supers(zss.Namebox, 'bind_', arguments);
   		var sheet = this._wgt.sheetCtrl;
   		if (sheet) {
   			this.sheet = sheet;
   			sheet.listen({'onCellSelection': this.proxy(this._onCellSelection)});
   		}
   	},
   	unbind_: function () {
   		var sheet = this.sheet;
   		if (sheet) {
   			sheet.unlisten({'onCellSelection': this.proxy(this._onCellSelection)});
   		}
   		this.sheet = this._wgt = null;
   		this.$supers(zss.Namebox, 'unbind_', arguments);
   	},
   	_onCellSelection: function (evt) {
   		var d = evt.data,
   			inp = this.$n('inp'),
			c = this.sheet.getCell(d.top, d.left);
   		if (c) {
   			inp.value = c.ref;
   		}
   		if (this._isOpenPopup()) {
   			this._closePopup();
   		}
   	},
   	_redrawNamesHTML_: function () {
   		var ns = this._names,
   			zcls = this.getZclass(),
   			html = '';
   		for (var i = 0, size = ns.length; i < size; i++) {
   			html += '<div class="' + zcls +'-item">' + ns[i].v + '</div>';
   		}
   		return html;
   	},
   	redrawHTML_: function () {
   		var uid = this.uuid,
   			zcls = this.getZclass();
   			html = '<div id="' + uid + '" class="' + zcls + '"><input id="' + 
    				uid + '-inp" class="' + zcls + '-inp"></input><div id="' + 
    				uid +'-icon" class="' + zcls + '-icon"></div><div id="' + 
    				uid + '-pp" class="' + zcls + '-pp" style="display:none;"></div></div>';
    		return html;
   	},
   	getZclass: function () {
   		return 'zsnamebox';
   	}
});
	
zss.FormulabarButton = zk.$extends(zul.wgt.Toolbarbutton, {
	setDomVisible_: function (n, visible, opts) {
		this.$supers(zss.FormulabarButton, 'setDomVisible_', [n, visible, {visibility:1}]);
	},
	domStyle_: function (no) {
		if (!this.isVisible())
			return 'visibility:hidden;';
	},
	doMouseOver_: function (evt) {
		this.$supers(zss.FormulabarButton, 'doMouseOver_', arguments);
		var body = this.$n().firstChild;
		jq(body).addClass(this.getSclass() + '-body-over');
	},
	doMouseOut_: function (evt) {
		this.$supers(zss.FormulabarButton, 'doMouseOut_', arguments);
		jq(this.$n().firstChild).removeClass(this.getSclass() + '-body-over');
	}
});

zss.FormulabarWestCave = zk.$extends(zk.Widget, {
	$o: zk.$void, //owner, fellows relationship no needed
	$init: function (wgt) {
		this.$supers(zss.FormulabarWestCave, '$init', []);
		
		this._wgt = wgt;
		//namebox and button container
		var nb = this._namebox = new zss.Namebox(wgt);
		this.appendChild(nb);
		
		var cancelBtn = this._cancelBtn = new zss.FormulabarButton({label: '✗', sclass: 'zsformulabar-cancelbtn',
				'onClick': this.proxy(this._onClickCancelBtn), 'onMouseDown': this.proxy(this._onMouseDownBtn)}),
			okBtn = this._okBtn = new zss.FormulabarButton({label: '✓', sclass: 'zsformulabar-okbtn',
				'onClick': this.proxy(this._onClickOKBtn), 'onMouseDown': this.proxy(this._onMouseDownBtn)}),
			formulaBtn = this._formulaBtn = new zss.FormulabarButton({label: 'f(x)', sclass: 'zsformulabar-insertbtn', 'onClick': this.proxy(this._onClickInsertFormulabar)});
		cancelBtn.setVisible(false);
		okBtn.setVisible(false);
		this.appendChild(cancelBtn);
		this.appendChild(okBtn);
		this.appendChild(formulaBtn); //for insert formula
	},
	bind_: function () {
		this.$supers(zss.FormulabarWestCave, 'bind_', arguments);
		var sheet = this._wgt.sheetCtrl;
		if (sheet) {
			this.sheet = sheet;
			sheet.listen({'onStartEditing': this.proxy(this._onStartEditing)});
			sheet.listen({'onStopEditing': this.proxy(this._onStopEditing)});
		} else {
			this._formulaBtn.setDisabled(true);
			this._namebox.setDisabled(true);
		}
	},
	unbind_: function () {
		var sheet = this.sheet;
		if (sheet) {
			sheet.unlisten({'onStartEditing': this.proxy(this._onStartEditing)});
			sheet.unlisten({'onStopEditing': this.proxy(this._onStopEditing)});
		}
		this._cancelBtn.unlisten({'onClick': this.proxy(this._onClickCancelBtn),
			'onMouseDown': this.proxy(this._onMouseDownBtn)});
		this._okBtn.unlisten({'onClick': this.proxy(this._onClickOKBtn),
			'onMouseDown': this.proxy(this._onMouseDownBtn)});
		this._formulaBtn.unlisten({'onClick': this.proxy(this._onClickInsertFormulabar)});
		
		this._wgt = this._cancelBtn = this._okBtn = this._formulaBtn = null; 
		this.$supers(zss.FormulabarWestCave, 'unbind_', arguments);
	},
	_onStartEditing: function () {
		this._cancelBtn.setVisible(true);
		this._okBtn.setVisible(true);
	},
	_onStopEditing: function () {
		this._cancelBtn.setVisible(false);
		this._okBtn.setVisible(false);
	},
	_onClickCancelBtn: function () {
		var sheet = this.sheet;
		if (sheet) {
			sheet.dp.cancelEditing('formulabarEditing');
			this._onStopEditing();
		}
	},
	_onClickOKBtn: function () {
		var sheet = this.sheet;
		if (sheet) {
			sheet.dp.stopEditing('refocus'); // ZSS-501
			this._onStopEditing();
		}
	},
	_onClickInsertFormulabar: function () {
		var wgt = this._wgt,
			sheet = wgt.sheetCtrl;
		if (sheet) {
			var s = sheet.getLastSelection();
			wgt.fireToolbarAction('insertFunction', {tRow: s.top, lCol: s.left, bRow: s.bottom, rCol: s.right});
		}
	},
	_onMouseDownBtn: function () {
		var sheet = this.sheet;
		if (sheet) {
			sheet.shallIgnoreBlur = true;
		}
	},
	getNamebox: function () {
		return this._namebox;
	},
	redraw: function (out) {
		var uid = this.uuid,
			html = '<div id="' + uid + '">';
		//namebox and buttons container
		html += '<div style="background:#F7F7F7;">' + this._namebox.redrawHTML_() + 
			'<div id="' + uid + '-btns" class="zsformulabar-buttons">' + this._cancelBtn.redrawHTML_() + this._okBtn.redrawHTML_() + this._formulaBtn.redrawHTML_() + '</div></div>';
		html += '<div class="zsformulabar-blank"></div></div>';
		out.push(html);
	},
	setFlexSize_: function(sz) {
		var size = this.$supers(zss.FormulabarWestCave, 'setFlexSize_', arguments),
			total = this.$n().clientWidth; //to compitiable with ZK7
		//TODO: fine tune IE6/IE7 namebox width/height
		this._namebox.setWidth(jq.px(total - this.$n('btns').clientWidth));
	}
});

zss.FormulabarWest = zk.$extends(zul.layout.West, {
	$o: zk.$void, //owner, fellows relationship no needed
   	$init: function (wgt) {
   		this.$supers(zss.FormulabarWest, '$init', []);
   		this.setFlex(true);
   		this.setBorder(0);
   		this.setSplittable(true);
   		this.setCollapsible(false);
   		this.setSclass('zsformulabar-west');
   		this.setSize('265px');
    		
   		var cave = this.cave = new zss.FormulabarWestCave(wgt);
   		this.appendChild(cave);
   	},
   	redrawHTML_: function () {
   		return this.$supers(zss.FormulabarWest, 'redrawHTML_', arguments);
   	}
});
    
zss.ExpandFormulabarButton = zk.$extends(zss.FormulabarButton, {
   	_expanded: false,
   	bind_: function () {
   		this.$supers(zss.ExpandFormulabarButton, 'bind_', arguments);
    	if (this._expanded)
    		jq(this.$n()).addClass(this.getSclass() + '-expanded');
    },
    isExpanded: function () {
    	return this._expanded;
    },
   	setExpanded: function (v) {
   		if (this._expanded != v) {
   			this._expanded = v;
   			jq(this.$n())[v ? 'addClass' : 'removeClass'](this.getSclass() + '-expanded');
   		}
   	},
   	getSclass: function () {
   		return 'zsformulabar-expandbtn';
   	}
});

zss.FormulabarCenterCave = zk.$extends(zk.Widget, {
	$o: zk.$void, //owner, fellows relationship no needed
   	$init: function (wgt) {
   		this.$supers(zss.FormulabarCenterCave, '$init', []);
   		this._wgt = wgt;
   		
   		this.appendChild(this.editor = new zss.FormulabarEditor(wgt));
   		this.appendChild(this.expandBtn = new zss.ExpandFormulabarButton());
   	},
   	bind_: function () {
   		this.$supers(zss.FormulabarCenterCave, 'bind_', arguments);
   		if (!this._wgt.sheetCtrl) {
   			this.editor.setDisabled(true);
   		}
   	},
   	redraw: function (out) {
		var uid = this.uuid,
			zcls = this.getZclass(),
			html = '<div id="' + uid + '">';
		html += (this.editor.redrawHTML_() + 
				'<div id="' + uid + '-expandbtn" class="' + zcls + '-colbtn">' + this.expandBtn.redrawHTML_() + '</div></div>');
		out.push(html);
   	},
   	getZclass: function () {
   		return 'zsformulabar-center-cave';
   	},
	setFlexSize_: function(sz) {
		var size = this.$supers(zss.FormulabarCenterCave, 'setFlexSize_', arguments),
			ss = this.$n(),//to compitiable with ZK7
			editor = this.editor,
			btn = this.$n('expandbtn');
		editor.setWidth(jq.px(ss.clientWidth - btn.clientWidth));
		editor.setHeight(jq.px(ss.clientHeight - zk(editor.$n()).sumStyles('tb', jq.borders)));
   	}
});

zss.FormulabarCenter = zk.$extends(zul.layout.Center, {
	$o: zk.$void, //owner, fellows relationship no needed
   	$init: function (wgt) {
   		this.$supers(zss.FormulabarCenter, '$init', []);
   		this.setFlex(true);
   		this.setBorder(0);
   		
   		this.appendChild(this.cave = new zss.FormulabarCenterCave(wgt));
   	},
   	getEditor: function () {
   		return this.cave.editor;
   	},
   	getExpandFormulabarBtn: function () {
   		return this.cave.expandBtn;
   	},
   	redrawHTML_: function () {
   		return this.$supers(zss.FormulabarCenter, 'redrawHTML_', arguments);
   	}
});

zss.Formulabar = zk.$extends(zul.layout.North, {
	$o: zk.$void, //owner, fellows relationship no needed
    //default expand formulabar size
   	_prevExpandedSize: 47,
   	$init: function (wgt) {
   		this.$supers(zss.Formulabar, '$init', []);
   		this._wgt = wgt;
   		this.setFlex(true);
   		this.setBorder(0);
   		this.setSize('27px');
   		this.setMinsize(27);
   		this.setSplittable(true);
   		this.setCollapsible(false);
   	},
   	afterParentChanged_: function () { 
    	var wgt = this._wgt,
   			cave = new zul.layout.Borderlayout(),
   			west = new zss.FormulabarWest(wgt),
   			center = new zss.FormulabarCenter(wgt),
   			btn = this.expandFormulabarBtn = center.getExpandFormulabarBtn();
   		this.editor = center.getEditor();
   		cave.appendChild(west);
   		cave.appendChild(center);
   		this.appendChild(cave);
   		
   		btn.listen({'onClick': this.proxy(this._onClickExpandFormulabarBtn)});
   	},
   	bind_: function () {
   		this.$supers(zss.Formulabar, 'bind_', arguments);
   		this.listen({'onSize': this});
   	},
   	unbind_: function () {
   		this.unlisten({'onSize': this});
   		this.expandFormulabarBtn.listen({'onClick': this.proxy(this._onClickExpandFormulabarBtn)});
   		this.$supers(zss.Formulabar, 'unbind_', arguments);
   	},
   	setHeight: function (v) {
   		this.$supers(zss.Formulabar, 'setHeight', arguments);
   		this._prevExpandedSize = zk.parseInt(v);
   	},
   	onSize: function (evt) {
   		var d = evt.data,
   			h = zk.parseInt(d.height),
   			fixHgh = h,
   			editor = this.editor,
   			minSize = this.getMinsize(),
   			btn = this.expandFormulabarBtn;
   		
   		var lineHgh = zk.parseInt(jq(editor.getInputNode()).css('line-height')),
   			extra = editor.$n().clientHeight - minSize;
   		if (extra > 0) {
   			var rest = extra % lineHgh;
   			fixHgh = minSize + (rest > 0 ? extra + (lineHgh - rest) : extra);
   			this.setHeight(jq.px(fixHgh));
   			editor.setHeight(jq.px(fixHgh));
   			btn.setExpanded(fixHgh > minSize);
   		} else {
   			btn.setExpanded(false);
   		}
   	},
   	_onClickExpandFormulabarBtn: function (evt) {
   		var curSize = zk.parseInt(this.getSize()),
   			minSize = this.getMinsize(),
   			btn = this.expandFormulabarBtn,
			expanded = btn.isExpanded();
   		if (expanded) {
   			this.setSize(minSize + 'px'); //min height
   		} else {
   			this.setSize(this._prevExpandedSize + 'px'); //restroe last height
   		}
   		btn.setExpanded(!expanded);
   	},
   	redrawHTML_: function () {
   		return this.$supers(zss.Formulabar, 'redrawHTML_', arguments);
   	},
   	getSclass: function () {
   		return 'zsformulabar';
   	}
});
})();