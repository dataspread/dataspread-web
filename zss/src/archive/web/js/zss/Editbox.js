/* Editbox.js

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

	var supportGetSelection =
			typeof window.getSelection == 'function',
		supportDocumentSelection =
			document.selection && document.selection.type != 'Control', // IE
		newLine = function () {
			if (supportGetSelection) {
				return function (node) {
					var selection = window.getSelection(),
						range = selection.getRangeAt(0),
						br = createBrNode();
					range.deleteContents();
					range.insertNode(br);
					range.setStartAfter(br);
					range.setEndAfter(br);
					selection.removeAllRanges();
					selection.addRange(range);
				};
			} else if (supportDocumentSelection) {
				return function (node) {
					var range = document.selection.createRange(),
						textRange = document.body.createTextRange();
					textRange.setEndPoint('StartToStart', range);
					textRange.setEndPoint('EndToEnd', range);
					// insert a span to index caret to the new line in IE8
					textRange.pasteHTML("<br><span class=\"caret-positioner\"></span>");
					textRange.moveStart('character', 1);
				};
			} else {
				return zk.$void;
			}
		}(),
		// Returns the range's text offset including br tags.
		getTextOffset = function () {
			if (supportGetSelection) {
				return function (range, countBr) {
					return range.toString().length + 
						// count br as line break
						(countBr ? range.cloneContents().querySelectorAll('br').length : 0);
				};
			} else if (supportDocumentSelection) {
				return function (range, countBr) {
					var count = 0;
					if (countBr) {
						var brs = range.htmlText.match(/<br\s*\/?>/gi);
						if (brs) {
							// count br as line break
							count = brs.length;
						}
					}
					return range.text.length + count;
				};
			} else {
				return function () {
					return 0;
				};
			}
		}(),
		/** Returns the selection range's text offset of the specified element. The offset is returned as a two-element array,
		 * where the first item is the start offset, and the second item is the end offset (excluding).
		 * <p>If an exception occurs, [0, 0] is returned.
		 * @param node the specified element which is selected
	 	 * @param countBr whether to count <br> as text "\n" (optional)
		 * @return Array a two-element array representing the selection range offset of the specified element
		 */
		getSelectionRange = function () {
			if (supportGetSelection) {
				return function (node, countBr) {
					try {
						var range = window.getSelection().getRangeAt(0),
							startRange = range.cloneRange(),
							endRange = range.cloneRange();
						startRange.selectNodeContents(node);
						endRange.selectNodeContents(node);
						startRange.setStart(node, 0);
						startRange.setEnd(range.startContainer, range.startOffset);
						endRange.setStart(node, 0);
						endRange.setEnd(range.endContainer, range.endOffset);
						//ZSS-1005
						if(range.toString() === '' && range.startOffset == 0 && range.endOffset == 0) {
							var length = node.innerText.length;
							startRange.setEnd(range.startContainer, length);
							endRange.setEnd(range.endContainer, length);
						}
						return [getTextOffset(startRange, countBr), getTextOffset(endRange, countBr)];
					} catch (e) {
						return [0, 0];
					}
				};
			} else if (supportDocumentSelection) {
				return function (node, countBr) {
					try {
						var range = document.selection.createRange(),
							startRange = document.body.createTextRange(),
							endRange = document.body.createTextRange();
						startRange.moveToElementText(node);
						startRange.setEndPoint('EndToStart', range);
						endRange.moveToElementText(node);
						endRange.setEndPoint('EndToEnd', range);
						return [getTextOffset(startRange, countBr), getTextOffset(endRange, countBr)];
					} catch (e) {
						return [0, 0];
					}
				};
			} else {
				return function () {
					return [0, 0];
				};
			}
		}(),
		// Selects all the contents in the node.
		selectText = function () {
			if (supportGetSelection) {
				return function (node) {
					var selection = window.getSelection(),
						range;
					if (selection.rangeCount > 0)
						selection.removeAllRanges();
					range = document.createRange();
					range.selectNodeContents(node);
					selection.addRange(range);
				};
			} else if (supportDocumentSelection) {
				return function (node) {
					var textRange = document.body.createTextRange();
					textRange.moveToElementText(node);
					textRange.select();
				};
			} else {
				return zk.$void;
			}
		}(),
		placeCaretAtEnd = function () {
			if (supportGetSelection) {
				return function (node) {
					var selection = window.getSelection(),
						range;
					if (selection.rangeCount > 0)
						selection.removeAllRanges();
					range = document.createRange();
					range.selectNodeContents(node);
					range.setStart(range.endContainer, range.endOffset);
					selection.addRange(range);
				};
			} else if (supportDocumentSelection) {
				return function (node) {
					var textRange = document.body.createTextRange();
					textRange.moveToElementText(node);
					textRange.moveStart('Character', getTextOffset(textRange, true));
					textRange.select();
				};
			} else {
				return zk.$void;
			}
		}(),
		complementHTML = function () {
			if (zk.webkit) { 
				return function (node) {
					node.appendChild(createBrNode());
				};
			} else if (zk.gecko) {
				return function (node) {
					var lastChild = node.lastChild;
					// 20140609, RaymondChao: firefox needs to append <br type="_moz"> to display a new empty line at the end
					if (lastChild && lastChild.nodeName.toLowerCase() == 'br' && jq(lastChild).attr('type') != '_moz') {
						var br = createBrNode();
						jq(br).attr('type', '_moz');
						node.appendChild(br);
					}
				};
			} else {
				return zk.$void;
			}
		}(),
		appendBrNode = function () {
			if (zk.webkit) { 
				return function (node) {
					var lastChild = node.lastChild;
					// 20140606, RaymondChao: in webkit, contenteditable div should insert an additional <br> in the end,
					// or it would need to press alt+enter two times to get a new line.
					if (!lastChild || lastChild.nodeName.toLowerCase() != 'br') {
						node.appendChild(createBrNode());
					}
				};
			} else {
				return zk.$void;
			}
		}(),
		stripBrNode = function () {
			if (zk.webkit) { 
				return function (node) {
					var lastChild = node.lastChild;
					// remove the redundant <br> inserted by appendBrNode()
					if (lastChild && lastChild.nodeName.toLowerCase() == 'br') {
						node.removeChild(lastChild);
					}
				};
			} else if (zk.gecko) {
				return function (node) {
					var lastChild = node.lastChild;
					// remove the redundant <br> inserted by appendBrNode()
					if (lastChild && lastChild.nodeName.toLowerCase() == 'br' && jq(lastChild).attr('type') == '_moz') {
						node.removeChild(lastChild);
					}
				};
			} else {
				return zk.$void;
			}
		}();

	// convert node to string, to replace <br> with "\n"
	function br2nl (n) {
		var duplicateNode = n.cloneNode(true),
			$duplicateNode = jq(duplicateNode);
		stripBrNode(duplicateNode);
		$duplicateNode.find('br').after("\n").remove();
		return $duplicateNode.text();
	}

	// convert string to html, and replace "\n" with <br> node.
	function nl2br (value) {
		return jq.parseHTML(value.replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\n/g, '<br>'));
	}

	function createBrNode () {
		return document.createElement('br');
	}
	
	function syncEditorHeight (n) {
		var ch = n.clientHeight,
			//cw = n.clientWidth,
			//sw = n.scrollWidth,
			sh = n.scrollHeight,
			hsb = zkS._hasScrollBar(n),//has hor scrollbar
			vsb = zkS._hasScrollBar(n, true);//has ver scrollbar
		
		if (sh > ch + 3) {//3 is border
			if (hsb && !vsb)
				jq(n).css('height', jq.px0(sh + zss.Spreadsheet.scrollWidth));// extend height
			else
				jq(n).css('height', jq.px0(sh));
		}
	}
	
	function blurEditor (wgt) {
		var sheet = wgt.sheet;
		if (sheet) {
			wgt.isFocused = false;
			// ZSS-674: stay focused if formulabar's ok/cancel btn was pressed down.
			if (sheet.shallIgnoreBlur) {
				focusWhenBlur(wgt);
				sheet.shallIgnoreBlur = false;
			} else if (sheet.isSwitchingFocus) {
				// 20140519, RaymondChao: when change focus between editors, spreadsheet could have no focus as below
				// formulabar editor blur -> ( no focus ) -> editbox focus
				// need to add isSwitchingFocus flag to determine the situation
				sheet.isSwitchingFocus = false;
			} else if (sheet.isSwitchingSheet) {
				focusWhenBlur(wgt);
			// ZSS-683: check if the editor is editing to prevent fire onStopEditing twice
			} else if (!sheet._wgt.hasFocus() && wgt.isEditing()) {
				sheet.dp.stopEditing(sheet.innerClicking > 0 ? "refocus" : "lostfocus");
			} else if (zk.ie && sheet._wgt.hasFocus() && wgt.isEditing()) {
				//ZSS-1081; ie's hasFocus() will be true while others not
				sheet.dp.stopEditing(sheet.innerClicking > 0 ? "refocus" : "lostfocus");
			}
		}
	}

	function focusWhenBlur (wgt) {
		if (!zk.gecko) {
			wgt.focus();
		} else {
			// 20140603, RaymondChao: firefox needs setTimeout to refocus when blur.
			setTimeout(function () {wgt.focus();});
		}
	}

	/**
	 * Returns cell reference
	 * 
	 * @param string src the source to extract reference
	 * @return array cell reference
	 */
	function getCellRefs(src) {
		return src.match('\\$?([A-Za-z]+)\\$?([0-9]+)');
	}
	
	/**
	 * Returns the editing formula info
	 *  
	 * @param string type the type of editor (optional)
	 * @param Object n the editor DOM element
	 * @param array p the text cursor position (optional)
	 */
	function getEditingFormulaInfo(type, node, position) {
		var p = position || getSelectionRange(node),
			tp = type || 'inlineEditing',
			start = p[0],
			end = p[1],
			v = jq(node).text(),
			firstChar = v.charAt(0);
		if (firstChar && firstChar == '=') {
			if (start != end) { //text has selection, need to replace cell reference
				return {start: start, end: end, type: tp};
			} else {
				if (!start) {
					return {start: 0, end: 0, type: tp};
				} else {
					var DELIMITERS = ['=', '+', '-', '*', '/', '!', ':', '^', '&', '(',  ',', '.'],
						i = start - 1,
						c = v.charAt(i);
					if (DELIMITERS.$contains(c)) {
						return {start: start, end: start, type: tp};
					}
				}
			}
		}
	}
	
	/**
	 * Insert cell reference to editor
	 * 
	 * @param Object sheet the ({@link zss.SSheetCtrl})
	 * @param Object n the editor DOM element
	 * @param int tRow
	 * @param int lCol
	 * @param int bRow
	 * @param int rCol
	 */
	function insertCellRef(sheet, n, tRow, lCol, bRow, rCol) {
   		var info = sheet.editingFormulaInfo,
			start = info.start,
			end = info.end,
			$n = jq(n),
			v = $n.text(),
			c1 = sheet.getCell(tRow, lCol),
			// ZSS-689: check if the selection is merged cell or range
			c2 = c1.isInRange(bRow, rCol) ? null : sheet.getCell(bRow, rCol),
			ref = c1.ref,
			editor = sheet.inlineEditor,
			editorSheetId = editor.sheetId,
			currentSheetId = sheet.serverSheetId;
		if (c2) {
			ref += (':' + c2.ref);
		}

		if (editorSheetId && editorSheetId != currentSheetId) {
			ref = getSheetName(sheet, currentSheetId) + '!' + ref;
		}

		if (!start) {
			ref = '=' + ref; 
		}
		$n.text(v.substring(0, start) + ref + v.substring(end, v.length));
		editor.setValue($n.text());
		end = start + ref.length;
		info.end = end;
		if (zk.ie && zk.ie < 11) { 
			setTimeout(function () {
				placeCaretAtEnd(n);
			});
		} else {
			placeCaretAtEnd(n);
		}
	}

	/**
	 * Get sheet name
	 * 
	 * @param Object sheet the ({@link zss.SSheetCtrl})
	 * @param string sheetId
	 */
	function getSheetName (sheet, sheetId) {
		var labels = sheet._wgt.getSheetLabels();
		for (var prop in labels) {
			if (labels[prop].id == sheetId) {
				return quoteName(labels[prop].name);
			}
		}
	}

	// returns sheet name or surrounds name with single-quote if nessarery
	function quoteName (name) {
		if (/^[a-z_]+[a-z0-9_.]*$/i.test(name)) {
			return name;
		} else {
			return "'" + name + "'";
		}
	}

zss.FormulabarEditor = zk.$extends(zul.inp.InputWidget, {
	widgetName: 'FormulabarEditor',
	_editing: false,
	row: -1,
	col: -1,
   	$init: function (wgt) {
   		this.$supers(zss.FormulabarEditor, '$init', []);
   		this._wgt = wgt;
   	},
   	bind_: function () {
   		this.$supers(zss.FormulabarEditor, 'bind_', arguments);
   		var sheet = this._wgt.sheetCtrl;
   		if (sheet) {
   			this.sheet = sheet;
   			sheet.formulabarEditor = this;
   			
   			sheet.listen({'onStartEditing': this.proxy(this._onStartEditing)})
   				.listen({'onStopEditing': this.proxy(this.stop)})
   				.listen({'onCellSelection': this.proxy(this._onCellSelection)})
   				.listen({'onContentsChanged': this.proxy(this._onContentsChanged)});
   		}
   	},
   	unbind_: function () {
   		var sheet = this.sheet;
   		if (sheet) {
   			
   			sheet.unlisten({'onStartEditing': this.proxy(this._onStartEditing)})
   				.unlisten({'onStopEditing': this.proxy(this.stop)})
   				.unlisten({'onCellSelection': this.proxy(this._onCellSelection)})
   				.unlisten({'onContentsChanged': this.proxy(this._onContentsChanged)});
   			
   			sheet.formulaEditor = null;
   		}
   		this.sheet = this._wgt = null;
   		this.$supers(zss.FormulabarEditor, 'unbind_', arguments);
   	},
   	disable: zk.$void,
   	cancel: function () {
   		var sheet = this.sheet;
   		if (sheet) {
   			sheet.inlineEditor.cancel();
   		}
   		this.clearStatus();
   	},
   	stop: function () {
   		var sheet = this.sheet;
   		if (sheet) {
   			sheet.inlineEditor.stop();
   		}
   		this.clearStatus();
   	},
	clearStatus: function () {
   		this.row = this.col = -1;
   		this._editing = false;
	},
   	/**
   	 * Sync editor top position base on text cursor position
   	 * 
   	 * @param int p the text cursor position. If doesn't specify, use current text cursor position
   	 */
   	syncEditorPosition: function (p) {
		var	sheet = this.sheet;
		if (!sheet) {
			return;
		}
		
		var n = this.$n(), // it's outer element

			// adjust target position
			// and check position is most-top or bottom
			pos = p !== undefined ? p : getSelectionRange(this.getInputNode(), true)[1];
		if (pos <= 0) { // scroll to most-top
			jq(n).scrollTop(0); // ZSS-205: scroll the parent, don't move the input
			return;
		}
		
		// ZSS-205: scroll to specific position
		var textHeight = this.getTextHeight(this.getValue().substring(0, pos)),
			sp = textHeight - this.getLineHeight();
		jq(n).scrollTop(sp);
   	},
   	/** one line height in formula editor */
   	getLineHeight: function() {
   		// calculate once and keep the result
   		if(this._lineHeight === undefined) {
   			// use one char to simulate one line and get the height
   			this._lineHeight = this.getTextHeight('a');
   		}
   		return this._lineHeight;
   	},
   	/** get specific text height in formula editor */
   	getTextHeight: function (text) {
   		var support = this.$n('support'),
   			$support = jq(support),
   			height;
   		$support.text(text);
   		// 20140609, RaymondChao: it's need an additional <br> if text ends with \n 
   		support.appendChild(createBrNode());
   		height = support.scrollHeight;
   		$support.text(''); // clear
   		return height;
   	},
   	/**
   	 * Sets the value of formula editor
   	 * 
   	 * @string string value
   	 * @param int pos the text cursor position
   	 */
   	setValue: function (value, pos) {
   		var n = this.$n(),
   			input = this.getInputNode(),
   			$input= jq(input);
   		$input.html(nl2br(value));
   		complementHTML(input);
   		if (this.isRealVisible()) {
   			// ZSS-205: reset height to parent's height before sync. editor's height with scroll height
   			$input.css('height', jq.px0(n.clientHeight - 3 /* top padding */ ));
   	   		syncEditorHeight(input);
   	   		
   	   		this.syncEditorPosition(pos !== undefined ? pos : value.length);
   		}
   	},
   	getValue: function () {
   		return br2nl(this.getInputNode());
   	},
   	newLine: function () {
   		newLine(this.getInputNode());
   	},
   	/**
   	 * Focus on editor
   	 */
   	focus: function () {
   		var input = this.getInputNode();
   		if (zk(input).isRealVisible(true))
   			input.focus();
   	},
   	doFocus_: function () {
   		var sheet = this.sheet;
   		this._editing = true;
   		if (sheet) {
   			var info = sheet.editingFormulaInfo;
   			if (info) {
   				if (!info.moveCell) {//focus back by user's mouse evt, re-eval editing formula info
   					sheet.editingFormulaInfo = getEditingFormulaInfo('formulabarEditing', this.getInputNode());
   				} else {
   					//focus back to editor while move cell, do nothing
   				}
   			} else {
   	   	   		var p = this.sheet.getLastFocus(),
				 	ls = sheet.selArea.lastRange;
				sheet.moveCellFocus(p.row, p.column);
				sheet.moveCellSelection(ls.left, ls.top, ls.right, ls.bottom, false, true);
   			}
   			this.isFocused = true;
   		}
   	},
   	doBlur_: function () {
   		blurEditor(this);
   	},
   	_onContentsChanged: function (evt) {
   		var sheet = this.sheet;
   		if (sheet && this.isRealVisible() && !sheet.editingFormulaInfo) {
   			var p = sheet.getLastFocus(),
   				c = sheet.getCell(p.row, p.column);
   			if (c) {
   				// ZSS-205: called by server updating cells
   				// should call setValue(), don't change DOM directly 
   				this.setValue(c.edit || '', 0);
   			}
   		}
   	},
   	isEditing: function () {
   	   	return this._editing;
   	},
   	_onStartEditing: function (evt) {
   		var d = evt.data;
   		this.row = d.row;
   		this.col = d.col;
   	},
   	_onCellSelection: function (evt) {
   		var sheet = this.sheet;
   		if (sheet) {
   			if (sheet.state == zss.SSheetCtrl.FOCUSED) {
   			   	var d = evt.data,
   			   		cell = sheet.getCell(d.top, d.left);
   			   	if (cell) {
   			   		// ZSS-205: should call setValue(), don't change DOM directly
   			   		this.setValue(cell.edit || '', 0);
   			   	} else {
   			   	}
   			} else if (sheet.state == zss.SSheetCtrl.EDITING) {
   				var info = sheet.editingFormulaInfo,
   					d = evt.data,
   					skipInsertCellRef = d.skipInsertCellRef;
   				if (info && 'formulabarEditing' == info.type && !skipInsertCellRef) {
   					insertCellRef(sheet, this.getInputNode(), d.top, d.left, d.bottom, d.right);
   				}
   			}
   		}
   	},
	doMouseDown_: function (evt) {
		var sheet = this.sheet;
		if (sheet) {
			if (sheet.state == zss.SSheetCtrl.FOCUSED) {
				var p = sheet.getLastFocus(),
					c = sheet.getCell(p.row, p.column);
				if (c && sheet.dp.startEditing(evt, c.edit, 'formulabarEditing')) {
					this.row = p.row;
					this.col = p.column;
				} else { //not allow edit, focus back
					sheet.dp.gainFocus(true);
				}
			} else if (sheet.state == zss.SSheetCtrl.NOFOCUS) {
				var p = sheet.getLastFocus(),
					row = p.row,
					col = p.column;
				sheet.dp.moveFocus(row == -1 ? 0 : row, col == -1 ? 0 : col);
				evt.stop();
			} else if (sheet.state == zss.SSheetCtrl.EDITING) {
				if (!this.isFocused) {
					sheet.isSwitchingFocus = true;
				}
				var info = sheet.editingFormulaInfo;
				if (info && info.moveCell) {//re-eval editing formula info
					sheet.editingFormulaInfo = getEditingFormulaInfo('formulabarEditing', this.getInputNode());
				}
			}
		}
	},
	doKeyDown_: function (evt) {
		var sheet = this.sheet;
		if (sheet) {
			sheet._doKeydown(evt);
			appendBrNode(this.getInputNode());
		}
	},
	afterKeyDown_: function (evt, simulated) {//must eat the event, otherwise cause delete key evt doesn't work correctly
	},
	doKeyUp_: function (evt) {
		var sheet = this.sheet,
			keycode = evt.keyCode;
		if (sheet && sheet.state == zss.SSheetCtrl.EDITING) {
			var input = this.getInputNode(),
				position = getSelectionRange(input),
				value = this.getValue(),
				info = sheet.editingFormulaInfo;
			syncEditorHeight(input); // ZSS-205: doesn't need to reset height here
			this.syncEditorPosition();
			sheet.inlineEditor.setValue(value);
			this._wgt.fire('onEditboxEditing', {token: '', sheetId: sheet.serverSheetId, row:this.row, col:this.col,  clienttxt: value});
			
			if (!value) {
				sheet.editingFormulaInfo = null;
				return;
			}
			
			var type = 'formulabarEditing';
			if (info) {
				info.type = type;
				if (info.moveCell) {
					if (info.end != end) { //means keyboard input new charcater
						sheet.editingFormulaInfo = getEditingFormulaInfo(type, input, position);
					} else {
						//no new charcater, remian the same editing info, do nothing.
					}
				} else {
					sheet.editingFormulaInfo = getEditingFormulaInfo(type, input, position);
				}
			} else {
				//ZSS-1071 when mouse click on a editing input, enableKeyNavigation will became false.
				//we won't give a cell reference navigation again for it till next startEditing
				if(sheet.enableKeyNavigation)
					sheet.editingFormulaInfo = getEditingFormulaInfo(type, input, position);
			}
		}
	},
	doKeyPress_: function (evt) {//eat the event
	},
   	setWidth: function (v) {
   		this.$supers(zss.FormulabarEditor, 'setWidth', arguments);
   		
   		// ZSS-205: input should be adjusted to sync. editor
   		// should subtract padding (left and right are both 3px)
   		// and scrollbar width according to browsers
   		var w = this.$n().clientWidth - 6; // 6px is padding
   		w = w - zss.Spreadsheet.scrollWidth; // scrollbar width
   		if(w < 0) {
   			w = 0;
   		}
   		jq(this.getInputNode()).css('width', jq.px(w));
   		jq(this.$n('support')).css('width', jq.px(w)); // must sync. with input
   	},
   	setHeight: function (v) {
   		this.$supers(zss.FormulabarEditor, 'setHeight', arguments);
   		
   		// ZSS-205: input should be adjusted to sync. editor
   		var input = this.getInputNode(),
   			n = this.$n();
		jq(input).css('height', jq.px0(n.clientHeight - 3 /* top padding */ )); // reset height to parent's height before sync. editor's height with scroll height
   		syncEditorHeight(input);
   		this.syncEditorPosition();
   	},
   	redrawHTML_: function () {
   		var uid = this.uuid,
   			zcls = this.getZclass();
   		// ZSS-205: add a support element for calculating scroll position 
   		return '<div id="' + uid + '" class="' + zcls + '"><div id="' + uid + '-real" class="' + zcls + '-real" contentEditable="true">' + this._areaText() + '</div><div id="' + uid + '-wrap-support" class="' + zcls + '-wrap-support"><div id="' + uid + '-support" class="' + zcls + '-support" contentEditable="true"></div></div></div>';
   	},
   	getZclass: function () {
   		return 'zsformulabar-editor';
   	}
});

/**
 * Editbox represent a edit area for cell
 */
zss.Editbox = zk.$extends(zul.inp.InputWidget, {
	widgetName: 'InlineEditor',
	_editing: false,
	_type: 'inlineEditing',
	$init: function (sheet) {
		this.$supers(zss.Editbox, '$init', []);
		this._wgt = sheet._wgt;
		
		this.sheet = sheet;
		this.row = -1;
		this.col = -1;
		this.disabled = true;
	},
	bind_: function () {
		this.$supers(zss.Editbox, 'bind_', arguments);
		this.comp = this.getInputNode();
		this.comp.ctrl = this;

		if (!zk.ie || zk.ie > 9) {
			this.domListen_(this.comp, 'onCompositionStart');
			this.domListen_(this.comp, 'onCompositionEnd');
		}
		
		this.sheet.listen({'onStartEditing': this.proxy(this._onStartEditing)})
   			.listen({'onStopEditing': this.proxy(this.stop)})
   			.listen({'onCellSelection': this.proxy(this._onCellSelection)});
	},
	unbind_: function () {
		if (!zk.ie || zk.ie > 9) {
			this.domUnlisten_(this.comp, 'onCompositionStart');
			this.domUnlisten_(this.comp, 'onCompositionEnd');
		}
		this.sheet.unlisten({'onStartEditing': this.proxy(this._onStartEditing)})
   			.unlisten({'onStopEditing': this.proxy(this.stop)})
   			.unlisten({'onCellSelection': this.proxy(this._onCellSelection)});
		
		this.sheet = this.comp.ctrl = this.comp = this._wgt = null;
		this.$supers(zss.Editbox, 'unbind_', arguments);
	},
	focus: function () {
		var n = this.comp;
   		if (zk(n).isRealVisible(true))
   			n.focus();
	},
	isEditing: function () {
		return this._editing;
	},
   	_onStartEditing: function (evt) {
   		var d = evt.data,
   			sheet = this.sheet;
   		this.row = d.row;
   		this.col = d.col;
   		this.sheetId = sheet.serverSheetId;
   		this.sheetName = getSheetName(sheet, this.sheetId);
   		this.cellRef = sheet.getCell(this.row, this.col).ref;
   		// ZSS-182: enable key navigation only when inline editing and
   		// 1. start edit in an empty cell
   		// 2. override the text in a cell
   		if (d.type == 'inlineEditing' && (!d.value || d.value.length <= 1)) {
   			sheet.enableKeyNavigation = true;
   		}
   	},
	_onCellSelection: function (evt) {
   		var sheet = this.sheet;
   		if (sheet) {
   			if (sheet.state == zss.SSheetCtrl.EDITING) {
   				var info = sheet.editingFormulaInfo,
   					d = evt.data,
   					skipInsertCellRef = d.skipInsertCellRef;
   				if (info && 'inlineEditing' == info.type && !skipInsertCellRef) {
   					var formulabarEditor = sheet.formulabarEditor;
   					insertCellRef(sheet, this.getInputNode(), d.top, d.left, d.bottom, d.right);
   					if (formulabarEditor) {
   						formulabarEditor.setValue(this.getValue());
   					}
   				}
   			}
   		}
	},
	doFocus_: function () {
		this._editing = true;
		if (this.sheet) {
			this.isFocused = true;
		}
	},
	doBlur_: function () {
		var sheet = this.sheet;
		if (sheet && sheet.enableKeyNavigation) {
			sheet.enableKeyNavigation = null;
		}
		blurEditor(this);
	},
	doMouseDown_: function (evt) {
		var sheet = this.sheet;
		if (sheet) {
			if (sheet.state == zss.SSheetCtrl.EDITING) {
				if (!this.isFocused) {
					sheet.isSwitchingFocus = true;
				}
				if (sheet.enableKeyNavigation) {
					sheet.enableKeyNavigation = null;
				}
				var info = sheet.editingFormulaInfo;
				if (info && info.moveCell) {//re-eval editing formula info
					sheet.editingFormulaInfo = getEditingFormulaInfo(this._type, this.getInputNode());
				}
			}
		}
	},
	doKeyPress_: function (evt) {
		var sheet = this.sheet;
		if (sheet.state == zss.SSheetCtrl.EDITING) {
			this.autoAdjust(true);
		} else {
			sheet._wgt.doKeyPress_(evt);
		}
	},
	doKeyDown_: function (evt) {
		var sheet = this.sheet;
		if (sheet.state != zss.SSheetCtrl.EDITING) {
			sheet._wgt.doKeyDown_(evt);
			return;
		}
		if (this.disabled) {
			evt.stop();
		} else {
			this.sheet._doKeydown(evt);
			appendBrNode(this.comp);
			var keycode = evt.keyCode;
			switch (keycode) {
			case 35: //End
				if (evt.altKey) {
					if (this.col + this.sw < this.sheet.maxCols - 1) {
						this.sw++;
						this.adjust("w");
					}
					evt.stop();
				}
				break;
			case 34: //PgDn
				if (evt.altKey) {
					if(this.row + this.sh <  this.sheet.maxRows - 1) {
						this.sh++;
						this.adjust("h");
					}
					evt.stop();
				}
				break;
			case 36: //Home
				if (evt.altKey) {
					if (this.sw > 0) {
						this.sw--;
						this.adjust("w");
					}
					evt.stop();
				}
				break;
			case 33: //PgUp
				if (evt.altKey) {
					if (this.sh > 0) {
						this.sh--;
						this.adjust("h");
					}
					evt.stop();
				}
				break;
			case 37: //Left
			case 38: //Up
			case 39: //Right
			case 40: //Dow
				//ZSS-1005: extra bug, hold arrow key to move quickly causes cursor shift incorrectly, 
				//so that cursor calculation will be wrong.
				if(sheet.enableKeyNavigation)
					evt.stop();
				break;
			}	
		}
	},
	afterKeyDown_: function (evt, simulated) { //must eat the event, otherwise cause delete key evt doesn't work correctly
		var sheet = this.sheet;
		if (sheet.state != zss.SSheetCtrl.EDITING) {
			sheet._wgt.afterKeyDown_(evt);
		}
	},
	_doCompositionStart: function (evt) {
		var sheet = this.sheet;
		if (sheet.state == zss.SSheetCtrl.FOCUSED) {
			if (zk.webkit) {
				this.prepareComposition = true;
			}
			sheet._enterEditing(evt);
		}
	},
	_doCompositionEnd: function (evt) {
		if (this.prepareComposition) {
			this.prepareComposition = null;
		}
	},
	doKeyUp_: function (evt) {
		var sheet = this.sheet;
		if (!sheet) {
			return;
		}
		if (sheet.state == zss.SSheetCtrl.EDITING) {
			var	formulabarEditor = sheet.formulabarEditor,
				value = sheet.inlineEditor.getValue(),
				keycode = evt.keyCode,
				info = sheet.editingFormulaInfo,
				input = this.comp,
				position = getSelectionRange(input);
				end = position[1];
			if (formulabarEditor) {
				formulabarEditor.setValue(value, getSelectionRange(input, true)[1]);
			}
			sheet._wgt.fire('onEditboxEditing', {token: '', sheetId: sheet.serverSheetId, row:this.row, col:this.col, clienttxt: value});
			
			if (!value) {
				sheet.editingFormulaInfo = null;
				return;
			}
			
			var type = this._type;
			if (info) {
				info.type = type;
				if (info.moveCell) {
					if (info.end != end) { //means keyboard input new charcater
						sheet.editingFormulaInfo = getEditingFormulaInfo(type, input, position);
					} else {
						//no new charcater, remian the same editing info, do nothing.
					}
				} else {
					sheet.editingFormulaInfo = getEditingFormulaInfo(type, input, position);
				}
			} else {
				//ZSS-1005 when mouse click on a editing input, enableKeyNavigation will became false.
				//we won't give a cell reference navigation again for it till next startEditing
				if(sheet.enableKeyNavigation)
					sheet.editingFormulaInfo = getEditingFormulaInfo(type, input, position);
			}
		} else {
			sheet._wgt.doKeyUp_(evt);
		}
	},
	/**
	 * Sets edit box disabled
	 * @param boolean disabled
	 */
	disable: function (disabled) {
		this.comp.style.backgroundColor = disabled ? "#DDDDDD" : "#EFECFF";
		this.disabled = disabled;
	},
	/**
	 * Sets the value of inline editor
	 * 
	 * @param string value
	 */
	setValue: function (v) {
		if (this.disabled) {
			return;
		}
		var input = this.comp;
		jq(input).html(nl2br(v));
		complementHTML(input);
		this.autoAdjust(true);
	},
	_startEditing: function (noFocus) {
		var sheet = this.sheet;
		if (sheet) {
			this._editing = !noFocus;
			var formulabarEditor = sheet.formulabarEditor,
				input = this.comp,
				value = this.getValue();
			if (formulabarEditor) {
				//20150724, henrichen: ZSS-1086: Firefox seems stop the focus 
				// event if you set value into formulabarEditor when it is 
				// gaining focus. StackTraces:
				//  formulabarEditor.doMouseDown_ -> dataPanel.startEditing -> 
				//	dataPanel._openEditbox(null, true) -> inlineEditor.edit ->
				//	inlineEditor._startEditing -> ... -> formulabarEditor.doFocus_
				if (!noFocus) { 
					formulabarEditor.setValue(value);
				}
			}
			if ('=' == value.charAt(0)) {
				var length = value.length;
				sheet.editingFormulaInfo = getEditingFormulaInfo(this._type, input, [length, length]);
			}
		}
	},
	/**
	 * 
	 * @cellcmp
	 * @row int row number (0 based)
	 * @col int column number (0 based)
	 * @param string value the edit value 
	 * @param boolean noFocus whether focus on inline editor or not
	 */
	edit: function (cellcmp, row, col, value, noFocus) {
		this.disable(false);
		this.row = row;
		this.col = col;
		this.sw = 0;//width to show
		this.sh = 0;//height to show
		var sheet = this.sheet,
			txtcmp = cellcmp.lastChild,
			fontcmp = txtcmp.lastChild,
			editorcmp = this.comp,
			$edit = jq(editorcmp);

		this.setValue(value);
		var w = cellcmp.ctrl.overflowed ? (cellcmp.firstChild.offsetWidth + this.sheet.cellPad) : (cellcmp.offsetWidth);
		var h = cellcmp.offsetHeight;
		var $cell = cellcmp.ctrl;
		var scrollPanel = sheet.sp;
		// ZSS-421: calculate editor position must consider freeze panel
		var l = sheet.custColWidth.getStartPixel($cell.c) + sheet.leftWidth - (sheet.frozenCol >= 0 && col <= sheet.frozenCol ? 0 : scrollPanel.currentLeft);
		var t = sheet.custRowHeight.getStartPixel($cell.r) + sheet.topHeight - (sheet.frozenRow >= 0 && row <= sheet.frozenRow ? 0 : scrollPanel.currentTop);
		
		t -= 1;//position adjust
		w -= 1;
		h -= 1;
		l -= 1;
		
		if (zk.safari || zk.opera)
			//the display in different browser. 
			w -= 2;

		this.editingWidth = w;
		this.editingHeight = h;
		this.editingTop = t;
		this.editingLeft = l;

		//issue 228: firefox need set display block, but IE can not set this.
		$edit.css({'min-width': jq.px0(w), 'min-height': jq.px0(h), 'width': 'auto', 'height': 'auto',
			// 20140605: limit the editbox inside the sheet
			'max-width': jq.px0(sheet.spcmp.clientWidth - l), 'max-height': jq.px0(sheet.spcmp.clientHeight - t),
			'left': jq.px(l), 'top': jq.px(t), 'line-height': sheet.lineHeight});
		//if (!zk.ie || zk.ie >= 11)
		//	$edit.css('display', 'block');
		zcss.copyStyle(txtcmp, editorcmp, ["text-align"], true);
		zcss.copyStyle(fontcmp, editorcmp, ["font-family","font-size","font-weight","font-style","color","text-decoration"], true);
		zcss.copyStyle(cellcmp, editorcmp, ["background-color"], true);

		this._startEditing(noFocus);
   			
		//move text cursor position to last
		//fun = function () {
		//	placeCaretAtEnd(editorcmp);
		//};
		if (!noFocus) {
			// ZSS-737: do not move caret's position when prepare composition
			if (this.prepareComposition) {
				$edit.focus();
			} else {
				setTimeout(function() {
					$edit.focus();
					placeCaretAtEnd(editorcmp);
				}, 25);
			}
		}

		// ZSS-683: zk.safari is not true on Chrome since ZK 7.0.1
		//if (!zk.safari && !zk.chrome && (!zk.ie /*|| zk.ie >= 11*/)) fun();//safari must run after timeout
		//setTimeout(function(){
			//issue 228: ie focus event need after show
			//if (zk.ie /*&& zk.ie < 11*/) {
			//	$edit.show();
			//}
			//if (!noFocus) {
			//	$edit.focus();
			//	placeCaretAtEnd(editorcmp);
			//	issue 230: IE cursor position is not at the text end when press F2
			//	if (zk.safari || zk.chrome || (zk.ie /*&& zk.ie < 11*/)) fun();
			//}
		//}, 25);
		this.autoAdjust(true);
	},
	cancel: function () {
		this.clearStatus();
	},
	stop: function () {
		var str = this.getValue();
		this.clearStatus();
		return str;
	},
	clearStatus: function () {
		var sheet = this.sheet;
		if (sheet) {
			sheet.editingFormulaInfo = null;
			if (sheet.enableKeyNavigation) {
				sheet.enableKeyNavigation = null;
			}
		}
		this._editing = false;
		this.disable(true);
		jq(this.comp).text('').css({'left': '-10005px', 'top': '-10005px'});
		jq(this.$n('info')).text('').css('display', 'none');
		this.row = this.col = -1;
		this.sheetId = null;
		this.sheetName = null;
		this.cellRef = null;
		if (this.prepareComposition) {
			this.prepareComposition = null;
		}
	},
	select: function() {
		selectText(this.comp);
	},
	getValue: function () {
		return br2nl(this.comp);
	},
	newLine: function () {
		newLine(this.comp);
		this.autoAdjust();
	},
	adjust: function (type) {
		var editorcmp = this.comp;

		if (type == "w") {
			var custColWidth = this.sheet.custColWidth,
				w = custColWidth.getStartPixel(this.col + this.sw + 1) - custColWidth.getStartPixel(this.col);
			if ((zk.ie && zk.ie < 11) || zk.safari || zk.opera)
				w -= 2;
			jq(editorcmp).css('width', jq.px0(w));
		} else if (type == "h") {
			var custRowHeight = this.sheet.custRowHeight,
				h = custRowHeight.getStartPixel(this.row + this.sh + 1) - custRowHeight.getStartPixel(this.row);
			jq(editorcmp).css('height', jq.px0(h));
		}
	},
	autoAdjust: function (forceadj) {
		var local = this;
		setTimeout(function() {
			var editorcmp = local.comp,
				ch = editorcmp.clientHeight,
			//	cw = editorcmp.clientWidth,
				sw = editorcmp.scrollWidth,
				sh = editorcmp.scrollHeight;
			//	hsb = zkS._hasScrollBar(editorcmp),//has hor scrollbar
			//	vsb = zkS._hasScrollBar(editorcmp, true);//has ver scrollbar
			
			//if (sh > ch + 3) {//3 is border
			//	if (hsb && !vsb)
			//		jq(editorcmp).css('height', jq.px0(sh + zss.Spreadsheet.scrollWidth));// extend height
			//	else
			//		jq(editorcmp).css('height', jq.px0(sh));
			//}
			if (sh > ch + 3 || forceadj) {
				var custColWidth = local.sheet.custColWidth,
					custRowHeight = local.sheet.custRowHeight;
				local.sw = custColWidth.getCellIndex(custColWidth.getStartPixel(local.col) + sw)[0] - local.col;
				local.sh = custRowHeight.getCellIndex(custRowHeight.getStartPixel(local.row) + sh)[0] - local.row;
			}			
		}, 0);
	},
	updateInfo: function () {
		var sheet = this.sheet,
			info = this.$n('info'),
			$info = jq(info),
			label = '',
			sheetId = this.sheetId;

		if (sheetId && sheetId != sheet.serverSheetId) {
			label = this.sheetName + '!' + this.cellRef;
			$info.css('display', 'block');
		} else {
			$info.css('display', 'none');
		}
		$info.text(label);
		$info.css({'left': this.editingLeft, 'top': jq.px(this.editingTop - info.offsetHeight)});
	},
	redrawHTML_: function () {
		return '<div id="' + this.uuid + '" class="zsedit"><div id="' + this.uuid + '-info" class="zsedit-info"></div><div id="' + this.uuid + '-real" class="zsedit-real" zs.t="SEditbox" contentEditable="true"></div></div>';
	}
});
})();