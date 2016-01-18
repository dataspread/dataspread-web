/* CellBlockCtrl.js

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

/**
 * CellBlockCtrl is used for controlling cells, include load cell, creating cell, merge cell and cell position index
 */
zss.CellBlockCtrl = zk.$extends(zk.Widget, {
	widgetName: 'CellBlock',
	range: null,
	_lastDir: null,
	$o: zk.$void, //owner, fellows relationship no needed
	$init: function (sheet, tRow, lCol, bRow, rCol, data, type) {
		this.$supers(zss.CellBlockCtrl, '$init', []); //DO NOT pass "arguments" or all fields will be copied into this Object. 
		
		this.sheet = sheet;
		this.type = type;
		this.rows = [];
		this.range = new zss.Range(lCol, tRow, rCol, bRow);
		
		var rows = data.rows,
			block = this;
		for (var r = tRow; r <= bRow; r++) {
			var row = new zss.Row(sheet, block, r, data);
			for (var c = lCol;  c <= rCol; c++) {
				row.appendCell(new zss.Cell(sheet, block, r, c, data));
			}
			this.appendRow(row);
		}
	},
	//override
	setVisible: function (visible) {
		if (this._visible != visible) {
			this._visible = visible;
			var n = this.$n();
			if (n)
				n.style.visibility = visible ? 'visible' : 'hidden';
		}
	},
	redraw: function (out) {
		var rows = this.rows,
			vis = this.isVisible() ? 'visible' : 'hidden',
			size = rows.length;
		out.push('<div id="', this.uuid, '" class="', this.getZclass(), '" style="visibility:' + vis +';">');
		for (var i = 0; i < size; i++) {
			rows[i].redraw(out);
		}
		out.push('</div>');
	},
	//super//
	getZclass: function () {
		var t = this.type;
		return t ? 'zs' + t + 'block' : 'zsblock';
	},
	bind_: function () {
		this.$supers(zss.CellBlockCtrl, 'bind_', arguments);
		
		var n = this.comp = this.$n();
//		zk(n).disableSelection(); //disableSelection()
	},
	unbind_: function () {
		this.$supers(zss.CellBlockCtrl, 'unbind_', arguments);
		
		delete this.rows;
		this.sheet = this.comp = this.rows = this.range = null;
	},
	doMouseDown_: function (evt) {
		this.sheet._doMousedown(evt);
	},
	doMouseUp_: function (evt) {
		this.sheet._doMouseup(evt);
	},
	/**
	 * Returns the row of the spreadsheet
	 * @param int row 
	 */
	getRow: function (row) {
		var range = this.range;
		if(row < range.top || row > range.bottom)
			return null;
		return this.rows[row - range.top];
	},
	/**
	 * Returns the cell of the spreadsheet
	 * @param int row
	 * @param int col
	 */
	getCell: function (row, col) {
		var range = this.range;
		if(!range || row < range.top || row > range.bottom || col < range.left || col > range.right)
			return null;
		return this.rows[row - range.top].getCellAt(col - range.left);
	},
	/**
	 * Sets the merge range of the cell
	 * @param id
	 * @param int left column start index
	 * @param int top row start index
	 * @param int right column end index
	 * @param int bottom row end index
	 * @param int width
	 */
	addMergeRange: function (id, left, top, right, bottom, width) {
		var cell = ctrl = this.getCell(top, left);
		if (!cell) return;
		var comp;
		for (var r = top; r <= bottom; ++r) {
			for (var i = left; i <= right; i++) {
				if (i == left && r == top) {
					comp = cell.comp;
					jq(comp).addClass("zsmerge" + id);
				} else {
					cell = this.getCell(r, i);
					if (!cell) break;//in top / left /corner , they might not have such cell
					comp = cell.comp;
					jq(comp).addClass(r == top ? "zsmergee" : "zsmergeeu");
				}
	
				cell.merid = id;
				cell.merr = right;
				cell.merl = left;
				cell.mert = top;
				cell.merb = bottom;
			}
		}
		//ZSS-944
		ctrl.redoRotate = true;
		this.update_(top, left, top, left);
	},
	/**
	 * Remove the merge range of the cell
	 * @param id
	 * @param int left column start index
	 * @param int top row start index
	 * @param int right column end index
	 * @param int bottom row end index
	 * @param int width
	 */
	removeMergeRange: function (id, left, top, right, bottom, width) {
		var cell = ctrl = this.getCell(top, left);
		if (!cell) return;
		var merl = cell.merl,
			merr = cell.merr,
			mert = cell.mert,
			merb = cell.merb,
			merid = cell.merid;
		
		if (id != merid)
			return;

		var ud,
			comp;
		for (var r = mert; r <= merb; ++r) {
			for (var i = merl; i <= merr; i++) {
				if (i == merl && r == mert) {
					comp = cell.comp;
					jq(comp).removeClass("zsmerge" + id);
				} else {
					cell = this.getCell(r, i);
					if (!cell) break;//in top / left /corner , they might not have such cell
					comp = cell.comp;
					jq(comp).removeClass(r == mert ? "zsmergee" : "zsmergeeu");
				}
				cell.merid = cell.merr = cell.merl = cell.mert = cell.merb = ud;
			}
		}
		ctrl.redoOverflow = true;
		//ZSS-944
		ctrl.redoRotate = true;
		this.update_(top, left, top, left);
	},

	/**
	 * Update cells
	 */
	update_: function (tRow, lCol, bRow, rCol) {
		for (var r = tRow; r <= bRow; r++) {
			for (var c = lCol; c <= rCol; c++) {
				var cell = this.getCell(r, c);
				if (cell)
					cell.update_();
			}
		}
	},
	/**
	 * Dynamic create cells
	 * 
	 * @param dir the dir to create cells
	 * @param tRow
	 * @param lCol
	 * @param bRow
	 * @param rCol
	 */
	create_: function (dir, tRow, lCol, bRow, rCol, data) {
		var sheet = this.sheet,
			data = data || sheet._wgt._cacheCtrl.getSelectedSheet(),
			block = this,
			cr = this.range,
			rs = this.rows,
			isNewRow = false,
			isTop = 'north' == dir,
			isBtm = 'south' == dir,
			isJump = 'jump' == dir,
			isVer = isTop || isBtm || isJump,
			isLeft = 'west' == dir,
			isRight = 'east' == dir;
		for (var r = tRow, j = 0; r <= bRow; r++) {
			var row = isVer ? new zss.Row(sheet, block, r, data) : this.getRow(r),
				html = isVer ? row.getHtmlPrologHalf() : '';
			for (var c = lCol, i = 0;  c <= rCol; c++) {
				var cell = new zss.Cell(sheet, block, r, c, data);
				if (isLeft) {
					row.insertCell(i++, cell); //row has been attach to DOM tree
				} else if (isRight) {
					row.appendCell(cell); //row has been attach to DOM tree
				} else if (isVer) {
					row.appendCell(cell, true); //not attach to DOM tree yet
					html += cell.getHtml();
				}
			}
			if (isVer)
				html += row.getHtmlEpilogHalf();
			else if (row._prepareAutoFilterBtns)
				row._prepareAutoFilterBtns(); //ZSS-953
			
			if (isBtm)
				this.appendRow(row, html, true);
			else if (isTop) {
				this.insertRow(j++, row, html, true);
			}
		}
		//ZSS 125: wrap text processed on row.bind_, within appendRow / insertRow
		delete sheet._wrapRange;
		
		var r = this.range,
			width = rCol - lCol + 1;
		if (isLeft)
			r.extendLeft(width);
		else if (isRight)
			r.extendRight(width);
		else if (isJump) {
			var oldRange = r;
			this.range = new zss.Range(oldRange.left, oldRange.top, width, height, true);
		}
	},
	/**
	 * Sets rows's width position index
	 * @param int col the column
	 * @param int zsw the column's position index
	 */
	appendZSW: function (col, zsw) {
		if (col > this.range.right || col < this.range.left) return;
		rows = this.rows;
		var rowsize = rows.length;
		for (var i = 0; i < rowsize; i++)
			rows[i].appendZSW(col - this.range.left, zsw);
	},
	/**
	 * Sets rows's height position index
	 * @param int row the row
	 * @param int zsh the row's height position index
	 */
	appendZSH: function (row, zsh) {
		if (row > this.range.bottom || row < this.range.top) return;
		this.rows[row - this.range.top].appendZSH(zsh);
	},
	/**
	 * Insert new column
	 * @param int col the column to insert
	 * @param int size the size of the cell
	 */
	insertNewColumn: function (col, size) {
		if (col > (this.range.right + 1) || col < this.range.left) return;
		var index = col - this.range.left;
		
		rows = this.rows;
		var rowsize = rows.length;
		for (var i = 0; i < rowsize; i++)
			rows[i].insertNewCell(index, size);

		this.range.extendRight(size);
	},
	/**
	 * Insert new row
	 * @param int row the row to insert
	 * @param int size the size of the row  
	 */
	insertNewRow: function (row, size, data) {
		if (row > (this.range.bottom + 1) || row < this.range.top) return;
		var index = row - this.range.top,
			ctrl,
			rows = this.rows,
			temprow = (index >= rows.length) ? rows[rows.length - 1] : rows[index],
			sheet = this.sheet,
			data = sheet._wgt._cacheCtrl.getSelectedSheet(),
			block = this;
		for (var i = 0; i < size; i++) {
			var r = row + i,
				html = '';
			
			ctrl = new zss.Row(sheet, block, r, data);
			//ZSS-120: use default row height first, row's height will update by server if needed 
			ctrl.zsh = null;
			html += ctrl.getHtmlPrologHalf();
			html += zss.Row.copyCells(temprow, ctrl);
			this.insertRow(index + i, ctrl, html); // ZSS-450, ZSS-485: it should be row array index not row number, see also insertNewColumn()
		}
		this.shiftRowInfo(index + size, row + size);
		this.range.extendBottom(size);
	},
	/**
	 * Shift row info
	 */
	shiftRowInfo: function(index, newrow) {
		var rows = this.rows,
			size = this.rows.length,
			j = 0;
		for(var i = index; i < size; i++)
			rows[i].resetRowIndex(newrow +(j++));
	},
	/**
	 * Remove column
	 * @param int col the column to remove
	 * @param int size
	 */
	removeColumn: function (col, size) {
		if (col > (this.range.right + 1) || col < this.range.left) return;
		var index = col - this.range.left;
		if ((col + size) > this.range.right)
			size = this.range.right - col + 1;
		
		var rows = this.rows;
		var rowsize = rows.length;
		for (var i = 0; i < rowsize; i++)
			rows[i].removeCell(index, size);
		this.range.extendRight(-size);
	},
	/**
	 * Remove row
	 * @param int row the row to remove
	 * @param int size
	 */
	removeRow: function (row, size) {
		if (row > (this.range.bottom + 1) || row < this.range.top) return;
		var index = row - this.range.top;
		if ((row + size) > this.range.bottom)
			size = this.range.bottom - row + 1;
		
		var ctrl,
			rows = this.rows,
			rem = rows.slice(index, index + size),
			tail = rows.slice(index + size, rows.length);
		rows.length = index;
		rows.push.apply(rows, tail);

		var ctrl = rem.pop();

		for (; ctrl; ctrl = rem.pop()) {
			ctrl.detach();
		}
		this.range.extendBottom(-size);
		this.shiftRowInfo(index, row);
	},
	/**
	 * Insert zss.Row widget at start with html content [optional]
	 * 
	 * @param int index
	 * @param zss.Row rowwidget
	 * @param string htmlContent
	 * @param boolean extendRange whether shall extend block range or not
	 */
	insertRow: function (index, row, htmlContent, extendRange) {
		var ignoeChildDom = htmlContent === undefined || !!htmlContent,
			rows = this.rows,
			sibling = rows[index];
		if (!sibling) {
			this.appendRow(row, htmlContent, extendRange);
		} else {
			this.insertBefore(row, sibling, ignoeChildDom);
			rows.splice(index, 0, row);
			if (extendRange) {
				this.range.extendTop(1);
			}
			if (htmlContent) {
				jq(htmlContent).insertBefore(sibling.$n());
				row.bind();
			}	
		}
	},
	/**
	 * Append zss.Row widget to the end with html content [optional]
	 * 
	 * @param row zss.Row widget
	 * @param string htmlContent
	 * @param boolean extendRange whether shall extend block range or not
	 */
	appendRow: function (row, htmlContent, extendRange) {
		var ignoeChildDom = htmlContent === undefined || !!htmlContent;
		this.appendChild(row, ignoeChildDom);
		this.rows.push(row);
		if (extendRange) {
			this.range.extendBottom(1);
		}
		if (htmlContent) {
			jq(this.comp).append(htmlContent);
			row.bind();
		}
	},
	/**
	 * Sets the row by index
	 * @param zss.Row
	 * @param int index
	 */
	/** insert cell, TODO cellctrls for better performance **/
	pushRowI: function (rowctrl, index) {
		var rows = this.rows,
			size = rows.length;
		if (index > size)
			throw('index out of bound:' + index + ' > ' + size );

		if (index == 0)
			this.pushRowS(rowctrl);
		else if (index == size)
			this.pushRowE(rowctrl);
		else {
			var tail = rows.slice(index, size);
			rows.length = index;
			rows.push(rowctrl);
			rows.push.apply(rows, tail);
			this.comp.insertBefore(rowctrl.comp, tail[0].comp);	
		}
	},
	/**
	 * Hide the cell
	 */
	hide: function () {
		jq(this.comp).css('display', 'none');
	},
	/**
	 * Show the cell
	 */
	show : function () {
		jq(this.comp).css('display', 'block');
	},
	removeColumnsFromStart_: function (size) {
		var rows = this.rows,
			rowSize = this.rows.length,
			i = 0;
		for(; i < rowSize; i++)
			this.rows[i].removeLeftCell(size);
		this.range.extendLeft(-size);
	},
	removeColumnsFromEnd_: function (size) {
		var rows = this.rows,
			rowSize = this.rows.length,
			i = 0;
		for(; i < rowSize; i++)
			rows[i].removeRightCell(size);
		this.range.extendRight(-size);
	},
	removeRowsFromStart_: function (size) {
		var rows = this.rows,
			i = size,
			rm = 0;
		while (i--) {
			if (!rows.length) {
				break;
			}
			rows.shift().detach();
			rm++;
		}
		this.range.extendTop(-rm);
		return rm;
	},
	removeRowsFromEnd_: function (size) {
		var rows = this.rows,
			i = size,
			rm = 0;
		while (i--) {
			if (!rows.length) {
				break;
			}
			rows.pop().detach();
			rm++;
		}
		this.range.extendBottom(-rm);
	}
});
})();