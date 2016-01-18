/* Spreadsheet.js

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
	function _calScrollWidth () {
		if(zkS.t(zss.Spreadsheet.scrollWidth)) return;
	    // scroll scrolling div
		var body = document.body,
			scr = jq('<div style="position:absolute;top:0px;left:0px;width:80px;height:50px;overflow:auto;"></div>')[0],
			inn = jq('<div style="width:100px;height:100px;overflow: scroll;"></div>')[0]; //scroll content div
	    // Put the scrolli div in the scrolling div
	    scr.appendChild(inn);

	    // Append the scrolling div to the doc
	    body.appendChild(scr);

	    //calcuate the scrollWidth;	
		zss.Spreadsheet.scrollWidth = (scr.offsetWidth - scr.clientWidth);
		if (zss.Spreadsheet.scrollWidth == 0)
			zss.Spreadsheet.scrollWidth = scr.offsetHeight - scr.clientHeight;
		body.removeChild(scr);
		return;
	}
	
	function doUpdate(wgt, shtId, data, token) {
		wgt.sheetCtrl._skipMove = data.sk; //whether to skip moving the focus/selection after update
		wgt.sheetCtrl._cmdCellUpdate(shtId, data);
		if (token)
			zkS.doCallback(token);
		delete wgt.sheetCtrl._skipMove; //reset to don't skip
	}

	function doBlockUpdate(wgt, json, token) {
		var ar = wgt._cacheCtrl.getSelectedSheet(),
			tp = json.type;
		if (ar && tp != 'ack') { //fetch cell will empty return,(not thing need to fetch)
			var d = json.data,
				tRow = json.top,
				lCol = json.left,
				bRow = tRow + json.height - 1,
				rCol = lCol + json.width - 1;
			ar.update(d);
			wgt.sheetCtrl._cmdBlockUpdate(tp, d.dir, tRow, lCol, bRow, rCol);
		}
		if (token)
			zkS.doCallback(token);
	}
	
	function _doInsertRCCmd (sheet, data, token) {
		sheet._cmdInsertRC(data);
		if (token != "")
			zkS.doCallback(token);	
	}
	function _doRemoveRCCmd (sheet, data, token) {
		sheet._cmdRemoveRC(data, true);
		if (token != "")
			zkS.doCallback(token);
	}
	function _doMergeCmd (sheet, data, token) {
		sheet._cmdMerge(jq.evalJSON(data), true);
		if (token != "")
			zkS.doCallback(token);
	}
	function _doSizeCmd (sheet, data, token) {
		sheet._cmdSize(data, true);
		if (token != "")
			zkS.doCallback(token);
	}
	function _doMaxrowCmd (sheet, data) {
		sheet._cmdMaxrow(data);
	}
	
	function _doMaxcolumnCmd (sheet, data) {
		sheet._cmdMaxcolumn(data);
	}
	
	/**
	 * Returns CSS link in head
	 * 
	 * @return StyleSheet object
	 */
	function getCSS (id) {
		var head = document.getElementsByTagName("head")[0];
		for (var n = head.firstChild; n; n = n.nextSibling) {
			if (n.id == id) {
				return n;
			}
		}
		return null;
	}
	
	/**
	 * Create a StyleSheet at the end of head node
	 * @param {String} cssText the default style css text.
	 * @param {String} id the id of this style sheet, null-able, you must assign the id if you want to remove it later.
	 * @return {Object} StyleSheet object
	 */
	function createSSheet (cssText, id) {
	    var head = document.getElementsByTagName("head")[0],
    		styleObj = document.createElement("style"),
    		sheetobj;
	    jq(styleObj).attr("type", "text/css");
		
	    if (id)
	       jq(styleObj).attr("id", id);
	    if (zk.ie && zk.ie < 11) {
	        head.appendChild(styleObj);
	        sheetobj = styleObj.styleSheet;
	        sheetobj.cssText = cssText;
	    } else {
	        try {
	        	styleObj.appendChild(document.createTextNode(cssText));
	        } catch (e) {
	        	styleObj.cssText = cssText;
	        }
	        head.appendChild(styleObj);
			sheetobj = _getElementSheet(styleObj);
	    }
	    return sheetobj;
	}
	/**
	 * remove StyleSheet object by id if exist.
	 * @param {String} id the style sheet id 
	 */
	function removeSSheet (id) {

		var node = document.getElementById(id);
		if(node && node.type == "text/css"){
			// ZSS-561: force clear css, or IE 10,11 will cache old css
			if(zk.ie >= 10) {
				node.removeAttribute('href');
			}
			node.parentNode.removeChild(node);
		}
	}
	/**
	 * get stylesheet object from DOMElement
	 */
	function _getElementSheet () {
		if (zk.ie && zk.ie < 11) {
			return function (element) {
				return element.styleSheet;
			};
		} else {
			return function (element) {
				return element.sheet;
			};
		}
	}
	
//	/**
//	 * Synchronize frozen area with top panel, left panel and corner panel
//	 */
//	function syncFrozenArea (sheet) {
//		var scroll = sheet.sp.comp,
//			corner = sheet.cp.comp,
//			currentWidth =  corner.offsetWidth,
//			maxWidth = scroll.clientWidth,
//			currentHeight = corner.offsetHeight,
//			maxHeight = scroll.clientHeight;
//		
//		if (currentWidth > maxWidth) {
//			var max = jq.px0(maxWidth),
//				left = sheet.lp.comp;
//			jq(corner).css('width', max);
//			jq(left).css('width', max);
//			//jq(scroll).css('overflow-x', 'hidden');
//		}
//
//		if (currentHeight > maxHeight) {
//			var height = jq.px0(maxHeight),
//				top = sheet.tp.comp;
//			jq(corner).css('height', height);
//			jq(top).css('height', height);
//			//jq(scroll).css('overflow-y', 'hidden');
//		}
//	}

	// 20140509, RaymondChao: ZK does not handle command key in mac, workaround is
	// converting the metaKey to ctrlKey. It should be removed when ZK fixes it.
	function _convertKeyEvent (evt) {
		if (zk.mac && evt.metaKey) {
			evt.ctrlKey = true;
			evt.data.ctrlKey = true;
		}
		return evt;
	}
	
/**
 * Spreadsheet is a is a rich ZK Component to handle EXCEL like behavior
 */
var Spreadsheet = 
zss.Spreadsheet = zk.$extends(zul.wgt.Div, {
	/**
	 * Indicate Ctrl-Paste event key down status
	 */
	_ctrlPasteDown: false,
	/**
	 * Indicate whether to always open hyperlink in a separate browser tab window; default true.
	 * <p>If this value is true, Spreadsheet will always open the link in a separate browser tab window.</p>
	 * <p>If this value is false, Spreadsheet will click to open the link in the same browser tab window; or 
	 * CTRL-click to open the link in a separate browser tab window.</p>
	 * @see #linkTo
	 */
	_linkToNewTab: true, //ZSS-13: Support Open hyperlink in a separate browser tab window
	_cellPadding: 2,
	_protect: false,
	_maxRows: 20,
	_maxColumns: 10,
	_rowFreeze: -1,
	_columnFreeze: -1,
	_rowHeight: 20,
	_clientCacheDisabled: false,
	_topPanelHeight: 20,
	_leftPanelWidth: 36,
	_maxRenderedCellSize: 8000,
	_displayGridlines: true,
	_showContextMenu: false,
	_selectionRect: null,
	_focusRect: null,
	_highLightRect: null,
	_colorPickerExUsed: false,
	/**
	 * Contains spreadsheet's toolbar
	 */
	//_toolbarPanel: null
	/**
	 * Contains zss.Formulabar, zss.SSheetCtrl
	 */
	//cave: null
	$init: function () {
		this.$supers(Spreadsheet, '$init', arguments);
		
		this.appendChild(this.cave = new zul.layout.Borderlayout({
			vflex: true, sclass: 'zscave'
		}));//contains zss.SSheetCtrl
		
		this._onResponseCallback = [];
	},
	$define: {
		/**
		 * Indicate cache data at client, won't prune data while scrolling sheet
		 */
		clientCacheDisabled: null,
		/**
		 * synchronized update data
		 * @param array
		 */
		dataBlockUpdate: _dataUpdate = function (v) {
			var sheet = this.sheetCtrl;
			if (!sheet) return;
			var token = v[0],
				json = jq.evalJSON(v[2]);
			if (sheet._initiated) {
				doBlockUpdate(this, json, token);
			} else {
				sheet.addSSInitLater(doBlockUpdate, this, json, token);
			}
		},
		dataBlockUpdateJump: _dataUpdate,
		dataBlockUpdateEast: _dataUpdate,
		dataBlockUpdateWest: _dataUpdate,
		dataBlockUpdateSouth: _dataUpdate,
		dataBlockUpdateNorth: _dataUpdate,
		dataBlockUpdateError: _dataUpdate,
		/**
		 * update data
		 * @param array
		 */
		dataUpdate: _updateCell = function (v) {
			var sheet = this.sheetCtrl;
			if (!sheet)	return;
			var token = v[0],
				shtId = v[1],
				data = v[2];
			
			if (sheet._initiated) {
				doUpdate(this, shtId, data, token);
			} else {
				sheet.addSSInitLater(doUpdate, this, shtId, data, token);
			}
		},
		dataUpdateStart: _updateCell,
		dataUpdateCancel: _updateCell,
		dataUpdateStop: _updateCell,
		dataUpdateRetry: _updateCell,
		redrawWidget: function (v) {
			var	sheet = this.sheetCtrl;
			if (!sheet)	return;
			
			var serverSheetId = v[0],
				wgtUuid = v[1],
				fn = function () {
					var w = zk.Widget.$(wgtUuid);
					if (w)
						w.redrawWidgetTo(sheet);
				};
			if (!this.isSheetCSSReady()) {
				sheet.addSSInitLater(fn);
			} else {
				fn();
			}
		},
		/**
		 * Inserts new row or column
		 */
		insertRowColumn: function (v) {
			var sheet = this.sheetCtrl;
			if (!sheet) return;
			
			var token = v[0],
				data = v[2],
				sheet = this.sheetCtrl;
			if (sheet._initiated)
				_doInsertRCCmd(sheet, data, token);
			else {
				sheet.addSSInitLater(_doInsertRCCmd, sheet, data, token);
			}
		},
		/**
		 * Removes row or column
		 */
		removeRowColumn: function (v) {
			var sheet = sheet = this.sheetCtrl;
			if (!sheet) return;
			var token = v[0], 
				data = v[2];

			if (sheet._initiated)
				_doRemoveRCCmd(sheet, data, token);
			else {
				sheet.addSSInitLater(_doRemoveRCCmd, sheet, data, token);
			}
		},
		mergeCell: function (v) {
			var sheet = this.sheetCtrl;
			if (!sheet) return;
			var token = v[0], 
				data = v[2];

			if (sheet._initiated)
				_doMergeCmd(sheet, data, token);
			else {
				sheet.addSSInitLater(_doMergeCmd, sheet, data, token);
			}
		},
		columnSize:  _size = function (v) {
			var sheet = this.sheetCtrl;
			if (!sheet) return;
			
			var data = v[2];
			if (sheet._initiated)
				_doSizeCmd(sheet, data);
			else {
				sheet.addSSInitLater(_doSizeCmd, sheet, data);
			}
		},
		/**
		 * Sets sheet protection. Default is false
		 * @param boolean
		 */
		/**
		 * Returns whether protection is enabled or disabled
		 * @return boolean
		 */
		protect: function (v) {
			var sheetCtrl = this.sheetCtrl;
			if (sheetCtrl) {
				sheetCtrl.fireProtectSheet(v);
				var ls = sheetCtrl.getLastSelection();
				if (v) {
					if (!sheetCtrl.isRangeSelectable(ls.left, ls.top, ls.right, ls.bottom)) {
						sheetCtrl.hideCellFocus();
						sheetCtrl.hideCellSelection();
					}
				} else {
					var pos = sheetCtrl.getLastFocus();
					sheetCtrl.moveCellFocus(pos.row, pos.column, true);
					sheetCtrl.showCellSelection();
				}
			}
			if (this._sheetBar) {
				this._sheetBar.getSheetSelector().setProtectSheetCheckmark(v);
			}
		},
		rowSize: _size,
		preloadRowSize: null,
		preloadColumnSize: null,
		initRowSize: null,
		initColumnSize: null,
		maxRenderedCellSize: null,
		/**
		 * Sets the maximum visible number of rows of this spreadsheet. For example, if you set
		 * this parameter to 40, it will allow showing only row 0 to 39. The minimal value of max number of rows
		 * must large than 0; <br/>
		 * Default : 20.
		 * 
		 * @param maxrows  the maximum visible number of rows
		 */
		/**
		 * Returns the maximum visible number of rows of this spreadsheet. You can assign
		 * new number by calling {@link #setMaxrows(int)}.
		 * 
		 * @return the maximum visible number of rows.
		 */
		maxRows: function(maxrows) {
			// ZSS-242: update range after change
			if(this.sheetCtrl) {
				this.sheetCtrl._cmdMaxrow(maxrows);
			}
		},
		/**
		 * Sets the maximum column number of this spreadsheet.
		 * for example, if you set to 40, which means it allow column 0 to 39. 
		 * the minimal value of maxcols must large than 0;
		 * <br/>
		 * Default : 10.
		 * 
		 * @param string
		 */
		/**
		 * Returns the maximum visible number of columns of this spreadsheet.
		 * 
		 * @return the maximum visible number of columns 
		 */
		maxColumns: function(maxcols) {
			// ZSS-242: update range after change
			if(this.sheetCtrl) {
				this.sheetCtrl._cmdMaxcolumn(maxcols);
			}
		},
		/**
		 * Sets the row freeze of this spreadsheet
		 * 
		 * @param rowfreeze row index
		 */
		/**
		 * Returns the row freeze index of this spreadsheet, zero base. Default : -1
		 * 
		 * @return the row freeze of selected sheet.
		 */
		rowFreeze: null,
		/**
		 * Sets the column freeze of this spreadsheet
		 * 
		 * @param columnfreeze  column index
		 */
		/**
		 * Returns the column freeze index of this spreadsheet, zero base. Default : -1
		 * 
		 * @return the column freeze of selected sheet.
		 */
		columnFreeze: null,
		/**
		 * Sets the customized titles of column header.
		 * @param string array
		 */
		/**
		 * Gets the customized titles of column header.
		 * @return string array
		 */
		columntitle: null,
		/**
		 * Sets the customized titles of row header.
		 * @param string array
		 */
		/**
		 * Gets the customized titles of row header.
		 * @return string array
		 */
		rowtitle: null,
		/**
		 * Sets the default row height of the selected sheet
		 * @param rowHeight the row height
		 */
		/**
		 * Gets the default row height of the selected sheet
		 * @return default value depends on selected sheet
		 */
		rowHeight: null,
		/**
		 * Sets the default column width of the selected sheet
		 * @param columnWidth the default column width
		 */
		/**
		 * Gets the default column width of the selected sheet
		 * @return default value depends on selected sheet
		 */
		columnWidth: null,
		/**
		 * Sets the top head panel height, must large then 0.
		 * @param topHeight top header height
		 */
		/**
		 * Gets the top head panel height
		 * default 20
		 * @return int
		 */
		topPanelHeight: null,
		/**
		 * Sets the left head panel width, must large then 0.
		 * @param leftWidth leaf header width
		 */
		/**
		 * Gets the left head panel width
		 * @return default value is 36
		 */
		leftPanelWidth: null,
		/**
		 * cell padding of each cell and header, both on left and right side.
		 */
		cellPadding: null,
		/** 
		 * the encoded URL for the dynamic generated content, or empty
		 * 
		 * @param string href CSS link
		 */
		scss: function (href) {
			var el = this.getCSS();
			if (el && this.bindLevel >= 0) {//Chrome need to check bindLevel; if not, CSS won't update correctly
				el.href = href;
			}
		},
		/**
		 * Sets selected sheet uuid
		 * 
		 * @param string sheey uuid
		 * @param boolean fromServer
		 * @param zss.Range visible range (from client) 
		 */
		sheetId: function (id, fromServer, visRange) {
			//For isSheetCSSReady() to work correctly.
			//when during select sheet in client side, server send focus au response first (set attributes later), 
			// _sheetId will be last selected sheet, cause isSheetCSSReady() doesn't work correctly 
			this._invalidatedSheetId = false;
			
			var sheetCtrl = this.sheetCtrl,
				cacheCtrl = this._cacheCtrl,
				sheetBar = this._sheetBar,
				sheetSelector = sheetBar ? sheetBar.getSheetSelector() : null;
			if (sheetSelector)
				sheetSelector.setSelectedSheet(id);
			if (sheetCtrl && cacheCtrl && cacheCtrl.getSelectedSheet().sheetId != id) { //side effect from ZSS-1037, don't change 'sheetId' to 'id'
				if (!fromServer) {
					cacheCtrl.setSelectedSheetBy(id);	
				}
				sheetCtrl.doSheetSelected(visRange);
			}
			var loadSheetStart = this._loadSheetStart;
			if (loadSheetStart) {
				this._loadSheetStart = false;
			}
		},
		/**
		 * Sets whether display gridlines.
		 * 
		 * Default: true
		 * @param boolean show true to show the gridlines;
		 */
		/**
		 * Returns whether display gridlines. default is true
		 * 
		 * @return boolean
		 */
		displayGridlines: function (show) {
			var sheet = this.sheetCtrl;
			if (!sheet) return;

			if (this.isSheetCSSReady()) {
				sheet.setDisplayGridlines(show);
			} else {
				//set cell focus after CSS ready
				sheet.addSSInitLater(function () {
					sheet.setDisplayGridlines(show);
				});
			}
		},
		mergeRange: null,
		autoFilter: null,
		csc: null,
		csr: null,
		//override
		width: function (v) {
			var sheet = this.sheetCtrl;
			if (sheet)
				sheet.resizeTo(v, null);
		},
		//override
		height: function (v) {
			var sheet = this.sheetCtrl;
			if (sheet)
				sheet.resizeTo(null, v);
		},
		/**
		 * Sets true to hide the row head of this spread sheet.
		 * @param boolean v true to hide the row head of this spread sheet.
		 */
		/**
		 * Returns if row head is hidden
		 * @return boolean
		 */
		rowHeadHidden: null,
		/**
		 * Sets true to hide the column head of  this spread sheet.
		 * @param boolean v true to hide the row head of this spread sheet.
		 */
		/**
		 * Returns if column head is hidden
		 * @return boolean
		 */
		columnHeadHidden: null,
		/**
		 * Sets whether show toolbar or not
		 * 
		 * Default: false
		 * @param boolean show true to show toolbar
		 */
		/**
		 * Returns whther show toolbar
		 * @return boolean
		 */
		showToolbar: function (show) {
			var w = this._toolbar;
			if (!w && show) {
				var tb = this._toolbar = new zss.Toolbar(this),
					tbp = this.getToolbarPanel();
				tbp.appendChild(tb);
				tbp.setHeight(tb.getSize());
			} else if (w) {
				var v = w.isVisible();
				if (v != show) {
					w.setVisible(show);
					this.getToolbarPanel().setVisible(show);
					zUtl.fireSized(this, -1);
				}
			}
		},
		actionDisabled: function (v) {
			var tb = this._toolbar
			if (tb)
				tb.setDisabled(v);
			if (this.getShowContextMenu()) {
				var shtCtrl = this.sheetCtrl;
				if (shtCtrl) {
					shtCtrl.setActionDisabled(v);
				}
			}
			var sb = this._sheetBar;
			if (sb)
				sb.setDisabled(v);
		},
		/**
		 * Sets whether show formula bar or not
		 * @param boolean show true to show formula bar
		 */
		/**
		 * Returns whether show formula bar
		 * @return boolean
		 */
		showFormulabar: function (show) {
			var w = this._formulabar;
			if (!w && show) {
				this.cave.appendChild(this._formulabar = new zss.Formulabar(this));
			} else if (w) {
				w.setVisible(show);
			}
		},
		/**
		 * Sets whether show sheetbar or not
		 * @param boolean true if want to show sheetbar
		 */
		/**
		 * Returns whether show sheetbar
		 * @return boolean 
		 */
		showSheetbar: function (show) {
			var w = this._sheetBar;
			if (!w && show) {
				this.cave.appendChild(this._sheetBar = new zss.Sheetbar(this));
			} else if (w) {
				w.setVisible(show);
			}
		},
		/**
		 * Sets whether show ContextMenu or not. Default is false
		 * @param boolean true if want to show ContextMenu (row/column/cell)
		 * 
		 * Returns whether show ContextMenu
		 * @return boolean
		 */
		showContextMenu: null,
		/**
		 * Sets sheet's name and uuid of book
		 */
		sheetLabels: function (v) {
			var sheetBar = this._sheetBar;
			if (sheetBar) {
				sheetBar.getSheetSelector().setSheetLabels(v);
			}
		},
		copysrc: null, //flag to show whether a copy source has set
		//flag that indicate server has done paste operation, no need to do paste at client,
		//Note. this flag will clear by doKeyUp()
		doPasteFromServer: null,
		colorPickerExUsed: null,
		//ZSS-1044: Whether keep cell selection box when lost focus
		keepCellSelection: false,
		//ZSS-1082: end user be to increase max visible rows/columns
		/**
		 * Sets whether show add-max-visible-row button
		 * @param boolean true if want to show add-max-visible-row button
		 */
		/**
		 * Returns whether show add-max-visible-row button
		 * @return boolean 
		 */
		showAddRow: function (show) {
			if (this._addRow != show) {
				this._addRow = show;
				var w = this._sheetBar;
				if (w && w.isVisible()) {
					w.cave.addRowButton.setVisible(show);
				}
			}
		},
		//ZSS-1082: end user be to increase max visible rows/columns
		/**
		 * Sets whether show add-max-visible-column button
		 * @param boolean true if want to show add-max-visible-column button
		 */
		/**
		 * Returns whether show add-max-visible-column button
		 * @return boolean 
		 */
		showAddColumn: function (show) {
			if (this._addColumn != show) {
				this._addColumn = show;
				var w = this._sheetBar;
				if (w && w.isVisible()) {
					w.cave.addColButton.setVisible(show);
				}
			}
		}
	},
	// ZSS-390: the selected range should not large than max rows/columns
	// It will be significant poor performance.
	setSelectionRect : function(rect) {
		this._selectionRect = this._narrowRect(rect);
	},
	getSelectionRect : function() {
		return this._selectionRect;
	},
	setFocusRect : function(rect) {
		this._focusRect = this._narrowRect(rect);
	},
	getFocusRect : function() {
		return this._focusRect;
	},
	setHighLightRect : function(rect) {
		this._highLightRect = this._narrowRect(rect);
	},
	getHighLightRect : function() {
		return this._highLightRect;
	},
	/** narrow rectangle and make sure its range won't exceed max rows/columns. */
	_narrowRect: function (rect) {
		var r = rect.split(",");
		var left = zk.parseInt(r[0]); 
		var top= zk.parseInt(r[1]); 
		var right = zk.parseInt(r[2]); 
		var bottom = zk.parseInt(r[3]);
		if(right > this._maxColumns - 1) {
			right = this._maxColumns - 1;
		}  
		if(bottom > this._maxRows - 1) {
			bottom = this._maxRows - 1;
		}
		return left + "," + top + "," + right + "," + bottom;
	},
	clearCachedSize_: function () {
		this.getToolbarPanel().clearCachedSize_();
		this.cave.clearCachedSize_();
		this.$supers(zss.Spreadsheet, 'clearCachedSize_', arguments);
	},
	getToolbarPanel: function () {
		var tbp = this._toolbarPanel;
		if (!tbp) {
			tbp = this._toolbarPanel = new zul.layout.Borderlayout({vflex: 'min'});
			this.insertBefore(tbp, this.firstChild);
		}
		return tbp;
	},
	getSheetCSSId: function () {
		return this.uuid + '-sheet';
	},
	getSelectorPrefix: function () {
		return '#' + this.uuid;
	},
	/**
	 * Returns whether CSS loaded from server or not
	 */
	isSheetCSSReady: function () {
		if (this._invalidatedSheetId) {//set by zss.Sheetbar, indicate current sheetId is invalidated
			return false;
		}
		return !!zcss.findRule(this.getSelectorPrefix() + " .zs_indicator_" + this.getSheetId(), this.getSheetCSSId());
	},
	/**
	 * Sets active range
	 */
	setActiveRange: function (v) {
		var c = this._cacheCtrl;
		if (!c) {
			this._cacheCtrl = c = new zss.CacheCtrl(this, v);
			
			var center = new zul.layout.Center({border: 0});
			center.appendChild(this.sheetCtrl = new zss.SSheetCtrl(this));
			this.cave.appendChild(center);
		} else {
			var sheet = this.sheetCtrl,
				range;
			if (sheet) {
				c.setSelectedSheet(v);
				this._triggerContentsChanged = true;
			}
		}
	},
//	getUpload: function () {
//		return this._$upload;
//	},
//	onChildAdded_: function (child) {
//		if (child.$instanceof(zss.Upload)) {
//			this._$upload = child;
//		}
//	},
	/**
	 * Synchronize widgets position to cell
	 * @param int row row index
	 * @param int col column index
	 */
	syncWidgetPos: function (row, col) {
		var wgtPanel = this.$n('wp'),
			widgets = jq(this.$n('wp')).children('.zswidget'),
			sheet = this.sheetCtrl,
			size = widgets.length;
		while (size--) {
			var n = widgets[size],
				wgt = zk.Widget.$(n.id);
			if (wgt && ((row >= 0 && wgt.getRow() >= row) || (col >= 0 && wgt.getCol() >= col)))
				wgt.adjustLocation();
		}
	},
	/**
	 * Returns the current focus of spreadsheet to Server
	 */
	setRetrieveFocus: function (v) {
		var sheet = this.sheetCtrl;
		if (!sheet) return;
		
		var v0 = typeof v === 'string' ? jq.evalJSON(v) : v; //ZSS-1042
		if (this.isSheetCSSReady()) {
			sheet._cmdRetriveFocus(v0);
		} else {
			sheet.addSSInitLater(function () {
				sheet._cmdRetriveFocus(v0);
			});
		}
	},
	/**
	 * Sets the current focus of spreadsheet
	 */
	setCellFocus: function (v) {
		var sheet = this.sheetCtrl;
		if (!sheet || sheet.isSwitchingSheet) return;

		if (this.isSheetCSSReady()) {
			sheet._cmdCellFocus(v);
		} else {
			//set cell focus after CSS ready
			sheet.addSSInitLater(function () {
				sheet._cmdCellFocus(v);
			});
		}
	},
	/**
	 * Retrieve client side spreadsheet focus.The cell focus and selection will
	 * keep at last status. It is useful if you want get focus back to
	 * spreadsheet after do some outside processing, for example after user
	 * click a outside button or menu item.
	 * 
	 * @param boolean trigger, true will fire a focus event, false won't.
	 */
	focus: function (trigger) {
		if (zk.ie && zk.ie < 11) {
			var self = this;
			//setTimeout(function () {
				var sht = self.sheetCtrl;
				if (sht && sht._initiated)
					sht.dp.gainFocus(trigger);
			//}, 0);
		} else if (this.sheetCtrl)
			this.sheetCtrl.dp.gainFocus(trigger);
	},
	/**
	 * Returns whether child DOM Element has focus or not
	 */
	hasFocus: function () {
		return jq.isAncestor(this.$n(), document.activeElement);
	},
	/**
	 * Move the editor focus 
	 */
	moveEditorFocus: function (id, name, color, row, col) {
		if (this.sheetCtrl)
			this.sheetCtrl.moveEditorFocus(id, name, color, zk.parseInt(row), zk.parseInt(col));
	},
	/**
	 * Remove the editor focus
	 */
	removeEditorFocus: function (id) {
		if (this.sheetCtrl)
			this.sheetCtrl.removeEditorFocus(id);
	},
	/**
	 * Sets the highlight rectangle or sets a null value to hide it.
	 */
	setSelectionHighlight: function (v) {
		var sheet = this.sheetCtrl;
		if (!sheet) return;
		
		if (this.isSheetCSSReady()) {
			sheet._cmdHighlight(v);
		} else {
			//set highlight after CSS ready
			sheet.addSSInitLater(function () {
				sheet._cmdHighlight(v);
			});
		}
	},
	/**
	 * Sets the selection rectangle of the spreadsheet
	 */
	setSelection: function (v) {
		var sheet = this.sheetCtrl;
		if (!sheet || sheet.isSwitchingSheet) return;
		
		var sf = this,
			//ZSS-169
			fn = function () {
			if (sf._ctrlPasteDown) {//set selection after keyup
				sf._afterPasteCallback = function () {
					sheet._cmdSelection(v);
				};
			} else {
				sheet._cmdSelection(v);
			}
		};
		if (this.isSheetCSSReady()) {
			fn();
		} else {
			//set selection after CSS ready
			sheet.addSSInitLater(function () {
				fn();
			});
		}
	},
	/**
	 * Returns whether if the server has registers Cell click event or not
	 * @return boolean
	 */
	_isFireCellEvt: function (type) {
		var evtnm = zss.Spreadsheet.CELL_MOUSE_EVENT_NAME[type];
		if ('onCellFilter' == evtnm) { //server side prepare auto filter popup information for client side
			return true;
		}
		return evtnm && this.isListen(evtnm, {asapOnly: true});
	},
	/**
	 * Returns whether if the server has registers Header click event or not
	 * @return boolean
	 */
	_isFireHeaderEvt: function (type) {
		var evtnm = zss.Spreadsheet.HEADER_MOUSE_EVENT_NAME[type];
		return evtnm && this.isListen(evtnm, {asapOnly: true});
	},
	/**
	 * Fire Header click event
	 * <p> Fires header event to server only if the server registers Header click event
	 * <p> Fires header event at client side
	 * @param string type, the type of the header event, "lc" for left click, "rc" for right click, "dbc" for double click
	 * 
	 */
	fireHeaderEvt: function (type, shx, shy, mousemeta, row, col, mx, my) {
		var sheetId = this.getSheetId(),
			prop = {type: type, shx: shx, shy: shy, key: mousemeta, sheetId: sheetId, row: row, col: col, mx: mx, my: my};
		if (this._isFireHeaderEvt(type)) {
			//1995689 selection rectangle error when listen onCellClick, 
			//use timeout to delay mouse click after mouse up(selection)
			var self = this;
			setTimeout(function() {
				self.fire('onZSSHeaderMouse', prop, {toServer: true});
			}, 0);
		}
		var evtName = zss.Spreadsheet.HEADER_MOUSE_EVENT_NAME[type];
		if (evtName) {
			var e = new zk.Event(this, evtName, prop);
			e.auStopped = true;
			this.fireX(e);
		}
	},
	/**
	 * Fires Cell Event
	 * <p> 
	 * @param string type
	 */
	fireCellEvt: function (type, shx, shy, mousemeta, row, col, mx, my, field) {
		if ('af'==type && this.isProtect() &&
			!this.sheetCtrl._wgt.allowAutoFilter) { //forbid using filter under protection
			return;
		}
		
		var sheetId = this.getSheetId(),
			prop = {type: type, shx: shx, shy: shy, key: mousemeta, sheetId: sheetId, row: row, col: col, mx: mx, my: my};
		if (field)
			prop.field = field;
		if (this._isFireCellEvt(type)) {
			//1995689 selection rectangle error when listen onCellClick, 
			//use timeout to delay mouse click after mouse up(selection)
			var self = this;
			setTimeout(function() {
				self.fire('onZSSCellMouse',	prop, {toServer: true}, 25);
			}, 0);
		}
		var evtName = zss.Spreadsheet.CELL_MOUSE_EVENT_NAME[type];
		if (evtName) {
			var e = new zk.Event(this, evtName, prop);
			e.auStopped = true;
			this.fireX(e);
		}
	},
	/**
	 * Fire widget update event
	 * 
	 * type
	 * <ul>
	 * 	<li>move</li>
	 * 	<li>resize</li>
	 * </li>
	 * 
	 * @param string wgt the widget type
	 * @param string action the event type
	 * @param string id the id of the widget
	 * @param int dx1 the x coordinate within the first cell
	 * @param int dy1 the y coordinate within the first cell
	 * @param int dx2 the x coordinate within the second cell
	 * @param int dy2 the y coordinate within the second cell
	 * @param int col1 the column (0 based) of the first cell
	 * @param int row1 the row (0 based) of the first cell
	 * @param int col2 the column (0 based) of the second cell
	 * @param int row2 the row (0 based) of the second cell
	 */
	fireWidgetUpdatEvt: function (wgt, action, id, dx1, dy1, dx2, dy2, col1, row1, col2, row2) {
		this.fire('onWidgetUpdate', {sheetId:this.getSheetId(), wgtType: wgt, action: action, wgtId: id, dx1: dx1, dy1: dy1, 
			dx2: dx2, dy2: dy2, col1: col1, row1: row1, col2: col2, row2: row2}, {toServer: true}, 25);
	},
	fireToolbarAction: function (action, extra) {
		var data = {sheetId: this.getSheetId(), tag: 'toolbar', action: action};
		this.fire('onAuxAction', zk.copy(data, extra), {toServer: true});
	},
	fireSheetAction: function (action, extra) {
		var data = {sheetId: this.getSheetId(), tag: 'sheet', action: action};
		this.fire('onAuxAction', zk.copy(data, extra), {toServer: true});
	},
	//ZSS-1082
	fireRowAction: function (action, extra) {
		var data = {sheetId: this.getSheetId(), tag: 'row', action: action};
		this.fire('onAuxAction', zk.copy(data, extra), {toServer: true});
	},
	//ZSS-1082
	fireColAction: function (action, extra) {
		var data = {sheetId: this.getSheetId(), tag: 'column', action: action};
		this.fire('onAuxAction', zk.copy(data, extra), {toServer: true});
	},
	/**
	 * Fetch active range. Currently fetch north/south/west/south direction
	 */
	fetchActiveRange: function (top, left, right, bottom) {
		if (!this._fetchActiveRange) {
			this.sheetCtrl.activeBlock.loadstate = zss.MainBlockCtrl.LOADING;
			this.fire('onZSSFetchActiveRange', 
				{sheetId: this.getSheetId(), top: top, left: left, right: right, bottom: bottom}, {toServer: true});
			this._fetchActiveRange = true;
		}
	},
	/**
	 * Recive active range data
	 */
	setActiveRangeUpdate: function (v) {
		this._fetchActiveRange = null;
		var cacheCtrl = this._cacheCtrl;
		if (cacheCtrl) {
			this.sheetCtrl.activeBlock.loadstate = zss.MainBlockCtrl.IDLE;
			cacheCtrl.getSelectedSheet().fetchUpdate(v);
			this.sheetCtrl.sendSyncblock();
		}
	},
	_initFrozenArea: function () {
		var rowFreeze = this.getRowFreeze(),
			colFreeze = this.getColumnFreeze();
		if ((rowFreeze != undefined && rowFreeze > -1) || (colFreeze != undefined && colFreeze > -1)) { // ZSS-392: minor issue
			var sheet = this.sheetCtrl;
			if (!sheet) return;
			
//			Bug ZSS-766: the modification fix the issue. but it also causes other issue when 
//			tiny size of width and height of browser window with frozen column and row. 
//			sync size when the left/top/coner are large than scroll panel is unneccesary.  
//			zk.afterMount(function(){
//				syncFrozenArea(sheet);
//			});
		}	
	},
	/**
	 * Returns whether load CSS or not
	 * 
	 * @return boolean
	 */
	getCSS: function () {
		return getCSS(this.uuid + "-sheet");
	},
	_initControl: function () {
		if (this.getSheetId() == null) //no sheet at all
			return;
		
		var cssId = this.uuid + '-sheet';
		if (!getCSS(cssId)) { //unbind may remove css, need to check again
			zk.loadCSS(this._scss, cssId);
		}
		
		this._initFrozenArea();
	},
	bind_: function (desktop, skipper, after) {
		_calScrollWidth();
		this._initControl();
		this.$supers('bind_', arguments);
		
		var sheet = this.sheetCtrl;
		if (sheet) {
			if (zk.safari) {
				zk(sheet.$n()).redoCSS();
			}
			sheet.fireProtectSheet(this.isProtect());
			sheet.fireDisplayGridlines(this.isDisplayGridlines());
		
			//ZSS-1087: restore panel position after invalidate()
			if (desktop._tmpSnaps) {
				if (desktop._tmpSnapsTimer) {
					clearTimeout(desktop._tmpSnapsTimer);
					delete desktop._tmpSnapsTimer
				}
				sheet.restorePos_(desktop._tmpSnaps);// sheet.restoreSheet(snapshot);
				delete desktop._tmpSnaps;
			}
		}
		
		zWatch.listen({onResponse: this});
		
		// ZSS-253: listen global event to modify focus
		// can't resolve the focus from partner to non-partner issue now.
//		jq(document).bind('zmousedown', this.proxy(this._doPartnerBlur));
//		jq(document).bind('keyup', this.proxy(this._doPartnerBlur)); // zss still has focus when key down 
	},
	unbind_: function () {
		//ZSS-1087: store the panel position before invalidate()
		if (this._cacheCtrl) {
			var sheetId = this.getSheetId(),
				dt = this.desktop;
			dt._tmpSnaps = this._cacheCtrl.snap(sheetId);
			//ZSS-1087: auto delete to avoid memory leak
			dt._tmpSnapsTimer = setTimeout(function () {
				delete dt._tmpSnaps;
			}, 0);
		}
		
		zWatch.unlisten({onResponse: this});
		
		this._cacheCtrl = this._maxColumnMap = this._maxRowMap = null;
		
		removeSSheet(this.getSheetCSSId());
		this.$supers('unbind_', arguments);
		if (window.CollectGarbage)
			window.CollectGarbage();
		
		// ZSS-253
		// can't resolve the focus from partner to non-partner issue now.
//		jq(document).unbind('zmousedown');
//		jq(document).unbind('keyup'); 
	},
	onResponse: function () {
		if (this._triggerContentsChanged != undefined) {
			this.sheetCtrl.fire('onContentsChanged');
			delete this._triggerContentsChanged;
		}

		var fns = this._onResponseCallback,
			fn = null;
		while (fn = fns.shift()) {
			fn();
		}
		this._sendAu = false;
	},
	domClass_: function (no) {
		return 'zssheet';
	},
	/**
	 * handle ZSS partner component blur event and let ZSS can blur correctly (ZSS-253). 
	 */
	_doPartnerBlur: function (event) {
		var ssctrl = this.sheetCtrl;
		var w = zk.currentFocus;
		while(w) {
			// check it's ZSS related widget or not
			if(w.className.indexOf('zss.') == 0 || w.className.indexOf('zssex.') == 0) {
				return;
			} 
			// check it's ZSS partner or not
			if(w.zssPartner) { //if a wdiget has zssPartner flag and set to true, gain focus back
				ssctrl.dp.gainFocus(false); // fake focus
				return;
			}
			w = w.parent; // check parent widget
		}
		ssctrl.dp._doFocusLost(); // otherwise, let spreadsheet blur
	}, 
	_doDataPanelBlur: function (evt) {
		var sheet = this.sheetCtrl;
		if (sheet.innerClicking <= 0 && sheet.state == zss.SSheetCtrl.FOCUSED) {
			
			// ZSS-737: focus is switching to textarea for pasting.
			if (sheet.isPasteFromClipboard) {
				sheet.dp.gainFocus(false); // fake focus
				return;
			}

			// #ZSS-253: check the widget which got focus is associated with spreadsheet or not
			// also check its parent until null
			var w = zk.currentFocus;
			
			//ZSS-630 in zk 7 and IE 9, the timing of menupop is different and currentFocus of menupop is null. 
			//has to get it back by dom manually
			if(!w){
				var fdom = document.activeElement;
				if(fdom){
					w = zk.Widget.$(fdom);
				}
			}
			
			while(w) {
				if(w.zssPartner) {	//if a wdiget has zssPartner flag and set to true, gain focus back
					sheet.dp.gainFocus(false); // fake focus
					return;
				}
				w = w.parent; // check parent widget
			}
			sheet.dp._doFocusLost(); // otherwise, let spreadsheet blur
			
			// TODO: check zk.currentFocus, if child of spreadsheet, do not _doFocusLost
			
		} else if(sheet.state == zss.SSheetCtrl.FOCUSED) {
			//retrive focus back to focustag
			sheet.dp.gainFocus(false);//Note. no prepare copy (in safari, it trigger onFloatUp evt, cause menupopup close)
		}
	},
	_doDataPanelFocus: function (evt) {
		var sheet = this.sheetCtrl;
		if (sheet.state < zss.SSheetCtrl.FOCUSED)
			sheet.dp.gainFocus(false);
	},
	_doSelAreaMouseMove: function (evt) {
		var sel = this.sheetCtrl.selArea;
		if (sel)
			sel._doMouseMove(evt);
	},
	_doSelAreaMouseOut: function (evt) {
		var sel = this.sheetCtrl.selArea;
		if (sel)
			sel._doMouseOut(evt);
	},
	doClick_: function (evt) {
		if (this.sheetCtrl)
			this.sheetCtrl._doMouseleftclick(evt);
		this.$supers('doClick_', arguments);
	},
	/**
	 * override
	 * 
	 * this method will check the current focus information first.
	 * 
	 * Case 1: no focus currently, sets the focus of the spreadsheet to last focus position.
	 * Case 2: spreadsheet has focus, depends on the target, execute the relative mouse down behavior		
	 */
	doMouseDown_: function (evt) {
		if (this.sheetCtrl)
			this.sheetCtrl._doMousedown(evt);
		this.$supers('doMouseDown_', arguments);
	},
	doMouseUp_: function (evt) {
		if (this.sheetCtrl)
			this.sheetCtrl._doMouseup(evt);
		this.$supers('doMouseUp_', arguments);
	},
	doRightClick_: function (evt) {
		if (this.sheetCtrl)
			this.sheetCtrl._doMouserightclick(evt);
		this.$supers('doRightClick_', arguments);
	},
	doDoubleClick_: function (evt) {
		if (this.sheetCtrl)
			this.sheetCtrl._doMousedblclick(evt);
		this.$supers('doDoubleClick_', arguments);
	},
	_doDragMouseUp: function (evt) {
		var dragHandler = this.sheetCtrl.dragging;
		if (dragHandler)
			dragHandler.doMouseup(evt);
	},
	_doDragMouseDown: function (evt) {
		var dragHandler = this.sheetCtrl.dragging;
		if (dragHandler)
			dragHandler.doMousemove(evt);
	},
	_releaseClientCache: function (sheetId){
		if(this._cacheCtrl){
			this._cacheCtrl.releaseCache(sheetId);
		}
	},
	sendAU_: function (evt, timeout, opts) {
		if (evt.name == 'onCtrlKey') {
			//client side need to know whether server do paste action at server side or not
			//if server side doesn't perform paste action, client side will do paste at doKeyUp_()
			timeout = 0;
		}
		this._sendAu = true;//a flag that indicat au processing
		this.$supers('sendAU_', arguments);
	},
	doKeyDown_: function (evt) {
		evt = _convertKeyEvent(evt);
		var sheet = this.sheetCtrl;
		if (sheet) {
			sheet._doKeydown(evt);
			// CTRL-V: a flag that whether stop event 
			// for avoid multi-paste same clipboard content to focus textarea or not
			if (evt.ctrlKey && evt.keyCode == 86) {
				this._ctrlPasteDown = true;
			}
		}
		this.$supers('doKeyDown_', arguments);
	},
	afterKeyDown_: function (evt) {
		var sheet = this.sheetCtrl; 
		if (sheet && sheet.state != zss.SSheetCtrl.EDITING) {
			var data = evt.data,
				sel = sheet.getLastSelection();
			if (sel) {
				data.tRow = sel.top;
				data.lCol = sel.left;
				data.bRow = sel.bottom;
				data.rCol = sel.right;
			}
			data.sheetId = this.getSheetId();
			this.$supers('afterKeyDown_', arguments);
			//feature #26: Support copy/paste value to local Excel
			var keyCode = evt.keyCode;
			if (this.isListen('onCtrlKey', {any:true}) && 
				(keyCode == 67 || keyCode == 86)) { //67: ctrl-c; 86: ctrl-v
				var parsed = this._parsedCtlKeys,
					ctrlKey = evt.ctrlKey ? 1: evt.altKey ? 2: evt.shiftKey ? 3: 0;
				if (parsed && 
					parsed[ctrlKey][keyCode]) {
					//Widget.js will stop event, if onCtrlKey reg ctrl + c and ctrl + v. restart the event
					evt.domStopped = false;
				}

				// ZSS-737: prevent focus lost when focus to textarea.
				sheet.isPasteFromClipboard = true;
				sheet.dp.selectInputNode();
				var that = this;
				//do the copy on the sheet!
				// 20140509, RaymondChao: focustag's value becames empty when paste twice or more times without setTimeout.
				setTimeout(function(){ that.doPaste(); }, 0);
			}
		}
		//avoid onCtrlKey to be eat in editing mode.
	},
	doKeyPress_: function (evt) {
		evt = _convertKeyEvent(evt);
		if (this.sheetCtrl)
			this.sheetCtrl._doKeypress(evt);
		this.$supers('doKeyPress_', arguments);
	},
	doKeyUp_: function (evt) {
		this.$supers('doKeyUp_', arguments);
		this._ctrlPasteDown = false;
	},
	//feature#161: Support copy&paste from clipboard to a cell
	doPaste: function () {
		if (!this._ctrlPasteDown)
			return;

		var sl = this,
			sheet = this.sheetCtrl,
			clearFn = function () {
				sl._doPasteFromServer = false;
			};
		if (sheet && sheet.state == zss.SSheetCtrl.FOCUSED) {
			sheet.pasteToSheet();
			//ZSS-169
			var fn = this._afterPasteCallback;
			if (fn) {
				fn();
				delete this._afterPasteCallback;
			}
		}
		// #ZSS-327: only clear the flag when copy-paste (i.e "ctrl+v") 
		if (this._sendAu) {//au processing, reset _doPasteFromServer on after response
			this._onResponseCallback.push(clearFn);
		} else {
			clearFn();
		}
	},
	linkTo: function (href, type, evt) {
		//1: LINK_URL
		//2: LINK_DOCUMENT
		//3: LINK_EMAIL
		//4: LINK_FILE
		if (type == 1) {
			if (this._linkToNewTab || evt.ctrlKey) //LINK_URL, always link to new tab window or CTRL-click
				window.open(href);
			else //LINK_URL, no CTRL
				window.location.href = href;
		} else if (type == 3) { //LINK_EMAIL
			window.location.href = href;
			this._skipOnbeforeunloadOnce(); //ZSS-900
		}
//		else if (type == 4) //LINK_FILE
			//TODO LINK_FILE
//		else if (type == 2) //LINK_DOCUMENT
			//TODO LINK_DOCUMENT
	},
	//ZSS-900
	// Tricky! Skip ZK's original onbeforeunload event once and then retore back
	_skipOnbeforeunloadOnce: function() {
		var _oldBfUnload = window.onbeforeunload;
		window.onbeforeunload = function () {
			window.onbeforeunload = _oldBfUnload;
		};
	},
	//ZSS-1000
	setCtrlArrowMoveFocus: function (v) {
		var sheetId = v.sheetId,
			row = v.row,
			col = v.col;
		if (sheetId != this.getSheetId()) return;
		var ssctrl = this.sheetCtrl;
		ssctrl.dp.moveFocus(row, col, true, true);
	},
	//ZSS-1085
	setShiftPosFocus: function (v) {
		var sheetId = v.sheetId,
			row = v.row,
			col = v.col;
		if (sheetId != this.getSheetId()) return;
		var ssctrl = this.sheetCtrl;
		ssctrl.dp.moveFocus(row, col, true, true);
	}
}, {
	CELL_MOUSE_EVENT_NAME: {lc:'onCellClick', rc:'onCellRightClick', dbc:'onCellDoubleClick', af:'onCellFilter', dv:'onCellValidator'},
	HEADER_MOUSE_EVENT_NAME: {lc:'onHeaderClick', rc:'onHeaderRightClick', dbc:'onHeaderDoubleClick'},
	SRC_CMD_SET_COL_WIDTH: 'setColWidth',
	initLaterAfterCssReady: function (sheet) {
		var wgt = sheet._wgt,
			sheetId = wgt.getSheetId();
		if (wgt.isSheetCSSReady()) {
			sheet._initiated = true;
			//since some initial depends on width or height,
			//so first ss initial later must invoke after css ready,
			
			//_doSSInitLater must invoke before loadForVisible,
			//because some urgent initial must invoke first, or loadForVisible will load a wrong block
			sheet._doSSInitLater();
			if (sheet._initmove) {//#1995031
				sheet.showMask(false);
			} else if(zk(sheet._wgt).isRealVisible() &&	!sheet.activeBlock.loadForVisible()){
				//if no loadfor visible send after init, then we must sync the block size
				sheet.showMask(false);
			}
			if (zk.opera)
				//opera cannot insert rule on special index,
				//so I must create another style sheet to control style rule priority
				createSSheet("", wgt.uuid + "-sheet-opera");//heigher
			
			//force IE to update CSS
			if (zk.ie6_ || zk.ie7_)
				jq(sheet.activeBlock.comp).toggleClass('zssnosuch');
			
			//fix the IE sometime doesn't load bottom/right block after init/switch sheet/invalidate
			setTimeout(function(){
				sheet.activeBlock.loadForVisible();
				
				// ZSS-600: create style context menu after initialized
				sheet.getStyleMenupopup();
			},25);
			
		} else {
			setTimeout(function () {
				zss.Spreadsheet.initLaterAfterCssReady(sheet);
			}, 1);
		}		
	}
});


})();
