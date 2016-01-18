/* Row.js

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
 * 
 * Row represent a row of the spreadsheet, it also be container that contains cells of the row
 */
zss.Row = zk.$extends(zk.Widget, {
	widgetName: 'Row',
	$o: zk.$void, //owner, fellows relationship no needed
	$init: function (sheet, block, row, src) {
		this.$supers(zss.Row, '$init', []); //DO NOT pass "arguments" or all fields will be copied into this Object. 
		
		this.sheet = sheet; //an object of zss.SSheetCtrl
		this.block = block;
		this.src = src;
		this.r = row;
		
		this.zsh = src.getRowHeightId(row); // an ID to retrieve the row's custom height from the pool (this.sheet.custRowHeight)
		this.cells = [];
		this.wrapedCells = [];
		
	},
	bind_: function (desktop, skipper, after) {
		this.$supers(zss.Row, 'bind_', arguments);//after bind cells, may need to process wrap height
	},
	unbind_: function () {
		delete this.cells;
		this.r = this.zsh = null;
		this.$supers(zss.Row, 'unbind_', arguments);
	},
	updateAutoHeightDirty: function (oldCellHeight, newCellHeight) {
		// not implement in OSE
	},
	processCellAutoHeight: function (cell) {
		// not implement in OSE
	},
	addAutoHeightCell: function (cell) {
		// not implement in OSE
	},
	removeAutoHeightCell: function (cell) {
		// not implement in OSE
	},
	/**
	 * Append zss.Cell at the end of the row
	 * 
	 * @param zss.Cell
	 * @param boolean ignoreDom
	 */
	appendCell: function (cell, ignoreDom) {
		this.appendChild(cell, ignoreDom);
		this.cells.push(cell);
	},
	/**
	 * Insert zss.Cell
	 * 
	 * @param int index
	 * @param zss.Cell
	 * @param boolean ignoreDom
	 */
	insertCell: function (index, cell, ignoreDom) {
		var cells = this.cells,
			sibling = cells[index];
		this.insertBefore(cell, sibling, ignoreDom);
		cells.splice(index, 0, cell);
	},
	redraw: function (out) {
		out.push(this.getHtmlPrologHalf())
		var cells = this.cells,
			size = cells.length;
		for (var i = 0; i < size; i++) {
			var cell = cells[i];
			cell.redraw(out);
		}
		out.push(this.getHtmlEpilogHalf());
	},
	getHtmlPrologHalf: function () {
		return '<div id="' + this.uuid + '" class="' + this.getZclass() + '" zs.t="SRow">';
	},
	getHtmlEpilogHalf: function () {
		return '</div>';
	},
	//super//
	getZclass: function () {
		var cls = 'zsrow',
			hId = this.zsh;
		return hId ? cls + ' zsh' + hId : 'zsrow';
	},
	/**
	 * Returns the {@link zss.Cell}
	 * @param int col column
	 * @return zss.Cell
	 */
	getCell: function (col) {
		var size = this.cells.length,
			i = 0;
		//TODO use binary search
		for (i = 0; i < size; i++) {
			if (this.cells[i].c == col) return this.cells[i];
		}
	},
	/**
	 * Returns the {@link zss.Cell}
	 * @param int index column index
	 * @return zss.Cell
	 */
	getCellAt: function (index) {
		return this.cells[index];
	},
	/**
	 * Remove cell
	 * @param int size
	 */
	removeLeftCell: function (size) {
		var cells = this.cells;
		var beforeSize = cells.length;
		while (size--) {
			if (!cells.length)
				return;
			cells.shift().detach();
		}
	},
	/**
	 * Remove right cell
	 * @param int size
	 */
	removeRightCell: function (size) {
		var cells = this.cells;
		while (size--) {
			if (!cells.length)
				return;
			cells.pop().detach();
		}
	},
	/**
	 * Sets the width position index
	 * @param int index cell index
	 * @param int zsw the width position index
	 */
	appendZSW: function (index, zsw) {
		var cell = this.cells[index];
		cell.appendZSW(zsw);
	},
	/**
	 * Sets the height position index
	 * @param int zsh the height position index
	 */
	appendZSH: function (zsh) {
		if (zsh) {
			this.zsh = zsh;
			jq(this.$n()).addClass("zsh" + zsh);
			var size = this.cells.length;
			for (var i = 0; i < size; i++)
				this.cells[i].appendZSH(zsh);	
		}
	},
	/**
	 * Insert new cell
	 * @param int index cell index
	 * @param int size
	 */
	insertNewCell: function (index, size) {
		var sheet = this.sheet,
			ctrl,
			cells = this.cells,
			col;
		
		if (index > cells.length) return;
		
		//there is a pentional BUG, if index==0 , not template to copy previous format
		//however, for now, the only templat need to copy is overflow and merge cell, 
		//and it is never be care if previous not beend loaded in client.
		var tempcell = index == 0 ? null : cells[index - 1];
		col = index == 0 ? cells[0].c :(tempcell.c + 1);
		
		var block = this.block,
			src = this.src,
			r = this.r;
		
		for (var i = 0; i < size; i++) {
			var c = col + i;
			
			//don't care merge property, it will be sync by removeMergeRange and addMergeRange.
			//don't care the sytle, since cell should be updated by continus updatecell event.
			ctrl = new zss.Cell(sheet, block, r, c, src);
			ctrl._justCopied = true; 
			//because of overflow logic, we must maintain overflow link from overhead
			//copy over flow attrbute overby and overhead,
			if (tempcell) {
				if (tempcell.overflowed) ctrl.overlapBy = tempcell;
				else if(tempcell.overlapBy) ctrl.overlapBy = tempcell.overlapBy;
			}
			this.insertCell(index + i, ctrl);
		}
		this.shiftCellInfo(index + size, col + size);
	},
	/**
	 * Shift cell's info
	 * @param int index the start index of the cell
	 * @param int newcol new column index
	 */
	shiftCellInfo: function (index, newcol) {
		var cells = this.cells,
			size = cells.length,
			j = 0;
		for(var i = index; i < size; i++)
			cells[i].resetColumnIndex(newcol+(j++));
	},
	/**
	 * Sets the row index
	 * @param int newrow new row index
	 */
	resetRowIndex: function (newrow) {
		this.r = newrow;
		var cells = this.cells,
			i = cells.length;
		while (i--)
			cells[i].resetRowIndex(newrow);
	},
	/**
	 * Remove the cell of the row
	 * @param int index cell index
	 * @param int size
	 */
	removeCell: function (index, size) {
		var ctrl,
			cells = this.cells,
			col;
		
		if (index > cells.length) return;
		if (index == cells.length)
			col = cells[index - 1].c + 1 ;
		else
			col = cells[index].c;

		var rem = cells.slice(index, index + size),
			tail = cells.slice(index + size, cells.length);
		cells.length = index;
		cells.push.apply(cells, tail);
		
		var cell = rem.pop();
		for (;cell ; cell = rem.pop()) {
			cell.detach();
		}
			
		this.shiftCellInfo(index, col);
	},
	getHeight: function(){
		return this.sheet.custRowHeight._getCustomizedSize(this.r);
	},
	doMouseMove_: function(evt) {
		var sheet = this.sheet,
			mx = evt.pageX,
			my = evt.pageY,
			position = zss.SSheetCtrl._calCellPos(sheet, mx, my),
			row = position[0],
			column = position[1],
			cell = sheet.getCell(row, column);

		//ZSS-454 Cannot click on hyperlink in the merge cell.
		if (cell!=null && cell.merr) {
			cell = sheet.getCell(cell.mert,cell.merl);
		}

		if (cell) {
			var $anchor = jq(cell.comp).find('a');
			if ($anchor.length > 0) {
				var anchor = $anchor[0];
				if(zkS.isOverlapByPoint(anchor, mx, my)) {
					this._cursor = 'pointer';
					jq(this.$n()).css('cursor', 'pointer');
					return;
				}
			} 
		}
		
		if (this._cursor) {
			this._cursor = null;
			jq(this.$n()).css('cursor', '');
		}
	}
}, {
	/**
	 * Returns a row that copy cells from target
	 * @param zss.Row tmprow a target row to copy it's cells
	 * @return string zss.Cell's html content
	 */
	copyCells: function (srcRow, destRow) {
		var cells = srcRow.cells;
			size = cells.length,
			r = destRow.r,
			html = '';
		for (var i = 0; i < size; i++) {
			var srcCell = cells[i],
				c = srcCell.c,
				src = srcCell.src,
				block = srcCell.block,
				sht = srcCell.sheet,
				newCell = new zss.Cell(sht, block, r, c, src);
			newCell._justCopied = true;
			html += newCell.getHtml();
			destRow.appendCell(newCell);
		}
		return html;
	}
});
