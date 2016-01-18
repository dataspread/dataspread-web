/*


{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/01 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.impl;

import java.io.Serializable;
import java.util.Iterator;
import java.util.TreeSet;

import org.zkoss.zss.model.SFooter;
import org.zkoss.zss.model.SHeader;
import org.zkoss.zss.model.SSheetViewInfo;
/**
 * 
 * @author Dennis
 * @since 3.5.0
 */
public class SheetViewInfoImpl implements SSheetViewInfo, Serializable {
	private static final long serialVersionUID = 1L;
	private boolean _displayGridlines = true;
	
	private int _rowFreeze = 0;
	private int _columnFreeze = 0;

	private SHeader _header;
	
	private SFooter _footer;
	
	private TreeSet<Integer> _rowBreaks;
	private TreeSet<Integer> _columnBreaks;
	
	
	@Override
	public boolean isDisplayGridlines() {
		return _displayGridlines;
	}

	@Override
	public void setDisplayGridlines(boolean enable) {
		_displayGridlines = enable;
	}

	@Override
	public int getNumOfRowFreeze() {
		return _rowFreeze;
	}

	@Override
	public int getNumOfColumnFreeze() {
		return _columnFreeze;
	}

	@Override
	public void setNumOfRowFreeze(int num) {
		_rowFreeze = num;
	}

	@Override
	public void setNumOfColumnFreeze(int num) {
		_columnFreeze = num;
	}

	@Override
	public SHeader getHeader() {
		if(_header==null){
			_header = new HeaderFooterImpl();
		}
		return _header;
	}

	@Override
	public SFooter getFooter() {
		if(_footer==null){
			_footer = new HeaderFooterImpl();
		}
		return _footer;
	}

	@Override
	public int[] getRowBreaks() {
		if(_rowBreaks==null){
			return new int[0];
		}
		int[] arr = new int[_rowBreaks.size()];
		Iterator<Integer> iter = _rowBreaks.iterator();
		for(int i=0;i<arr.length;i++){
			arr[i] = iter.next();
		}
		return arr;
	}

	@Override
	public void setRowBreaks(int[] breaks) {
		if(_rowBreaks!=null)
			_rowBreaks.clear();
		else
			_rowBreaks = new TreeSet<Integer>();
		if(breaks!=null){
			for(int i:breaks){
				_rowBreaks.add(i);
			}
		}
	}
	
	public void addRowBreak(int row){
		if(_rowBreaks==null)
			_rowBreaks = new TreeSet<Integer>();
		
		_rowBreaks.add(row);
	}
	
	public void addColumnBreak(int column){
		if(_columnBreaks==null)
			_columnBreaks = new TreeSet<Integer>();
		
		_columnBreaks.add(column);
	}

	@Override
	public int[] getColumnBreaks() {
		if(_columnBreaks==null){
			return new int[0];
		}
		int[] arr = new int[_columnBreaks.size()];
		Iterator<Integer> iter = _columnBreaks.iterator();
		for(int i=0;i<arr.length;i++){
			arr[i] = iter.next();
		}
		return arr;
	}

	@Override
	public void setColumnBreaks(int[] breaks) {
		if(_rowBreaks!=null)
			_columnBreaks.clear();
		else
			_columnBreaks = new TreeSet<Integer>();
		if(breaks!=null){
			for(int i:breaks){
				_columnBreaks.add(i);
			}
		}
	}

	//ZSS-688
	//@since 3.6.0
	/*package*/ void copyFrom(SheetViewInfoImpl src) {
		this._displayGridlines = src._displayGridlines;
		this._rowFreeze = src._rowFreeze;
		this._columnFreeze = src._columnFreeze;

		if (src._header != null) {
			this._header = ((HeaderFooterImpl)src._header).cloneHeaderFooterImpl();
		}
		if (src._footer != null) {
			this._footer = ((HeaderFooterImpl)src._footer).cloneHeaderFooterImpl();
		}
		
		if (src._rowBreaks != null) {
			this._rowBreaks = new TreeSet<Integer>(src._rowBreaks);
		}
		if (src._columnBreaks != null) {
			this._columnBreaks = new TreeSet<Integer>(src._columnBreaks);
		}
	}
}
