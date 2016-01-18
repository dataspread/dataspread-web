/* MergeMatrixHelper.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mar 18, 2008 1:08:11 PM     2008, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zkoss.zss.api.AreaRef;


/**
 * Each sheet has its own MergeMatrixHelper and it manages merged cells.
 * Because current implementation of spreadsheet only support horizontal merged cell,
 * So, use this helper to help Spreadsheet control merged cell in horizontal only
 * @author Dennis.Chen
 *
 */
public class MergeMatrixHelper {

	
	private Map _leftTopIndex = new HashMap(5);
	private Map _mergeByIndex = new HashMap(20);
	private List<MergedRect> _mergeRanges = new LinkedList<MergedRect>();
	
	private int _frozenRow;
	private int _frozenCol;//don't care this for now.
	private SequenceId _mergeId = new SequenceId(0,1);
	/**
	 * @param mergeRange List of merge range
	 * @param frozenRow
	 * @param frozenCol
	 */
	public MergeMatrixHelper(List mergeRange,int frozenRow,int frozenCol){
		Iterator iter = mergeRange.iterator();
		while(iter.hasNext()){
			final int[] r = (int[])iter.next();
			final int left = r[0];
			final int top = r[1];
			final int right = r[2];
			final int bottom = r[3];
			//System.out.println("Merge:"+(count++)+" l:"+left+",t:"+top+",r:"+right+",b:"+bottom);
			final MergedRect block = new MergedRect(_mergeId.next(),left,top,right,bottom);
			_mergeRanges.add(block);
		}
		
		this._frozenRow = frozenRow;
		this._frozenCol = frozenCol;
		
		rebuildIndex();
	}
	
	private void rebuildIndex(){
		_leftTopIndex = new HashMap(5);
		_mergeByIndex = new HashMap(20);
		
		Iterator iter = _mergeRanges.iterator();
		while(iter.hasNext()){
			final MergedRect block= (MergedRect)iter.next();
			final int left = block.getColumn();
			final int top = block.getRow();
			final int right = block.getLastColumn();
			final int bottom = block.getLastRow();
			_leftTopIndex.put(top+"_"+left,block);
			for(int r = top; r <= bottom; ++r) {
				for(int c = left; c <= right; ++c) {
					_mergeByIndex.put(r+"_"+c, block);
				}
			}
		}
	}
	
	
	public void update(int frozenRow,int frozenCol){
		this._frozenRow = frozenRow;
		this._frozenCol = frozenCol;
	}
	
	/**
	 * Check is (row,col) in one of merge range's left-top 
	 */
	public boolean isMergeRangeLeftTop(int row,int col){
		return _leftTopIndex.get(row+"_"+col)==null?false:true;
	}
	
	/**
	 * Get a merged range which contains cell(row,col)
	 */
	public MergedRect getMergeRange(int row,int col){
		MergedRect range = (MergedRect)_mergeByIndex.get(row+"_"+col);
		return range;
	}
	
	/**
	 * Get all merged range. 
	 * @return a list which contains all merged range
	 */
	public List getRanges(){
		return _mergeRanges;
	}
	
	/**
	 * Returns {@link MergedRect} MergedRect by id
	 * 
	 * @param id
	 * @return MergedRect
	 */
	public MergedRect getMergedRect(int id) {
		for (MergedRect r : _mergeRanges) {
			if (r.getId() == id) {
				return r;
			}
		}
		return null;
	}
	
	/**
	 * Get merged range which contains col
	 * @param col column index
	 * @return a list which contains merged range
	 */
	public Set getRangesByColumn(int col){
		Iterator iter = _mergeRanges.iterator();
		Set result = new HashSet();
		while(iter.hasNext()){
			AreaRef rect = (AreaRef)iter.next();
			int left = rect.getColumn();
			int right = rect.getLastColumn();
			if(left<=col && right>=col){
				result.add(rect);
			}
		}
		return result;
	}

	public int getRightConnectedColumn(int col, int top, int bottom) {
		int size = _mergeRanges.size();
		List result = new ArrayList();
		AreaRef rect;
		for(int i=0;i<size;i++){
			rect = (MergedRect)_mergeRanges.get(i);
			if(rect.getRow()>_frozenRow && (rect.getRow()<top || rect.getLastRow()>bottom)){
				continue;
			}
			result.add(rect);
		}
		
		boolean conti = true;
		while(conti){
			conti = false;
			size = result.size();
			for(int i=0;i<size;i++){
				rect = (MergedRect)result.get(i);
				if(rect.getLastColumn()>col && rect.getColumn()<=col){
					col = rect.getLastColumn();
					conti = true;
					result.remove(i);
					break;
				}
			}
		}
		return col;
	}

	public int getLeftConnectedColumn(int col, int top, int bottom) {
		int size = _mergeRanges.size();
		List result = new ArrayList();
		AreaRef rect;
		for(int i=0;i<size;i++){
			rect = (MergedRect)_mergeRanges.get(i);
			if(rect.getRow()>_frozenRow && (rect.getRow()<top || rect.getLastRow()>bottom)){
				continue;
			}
			result.add(rect);
		}
		
		boolean conti = true;
		while(conti){
			conti = false;
			size = result.size();
			for(int i=0;i<size;i++){
				rect = (MergedRect)result.get(i);
				if(rect.getColumn()<col && rect.getLastColumn()>=col){
					col = rect.getColumn();
					conti = true;
					result.remove(i);
					break;
				}
			}
		}
		return col;
	}

	public int getBottomConnectedRow(int row, int left, int right) {
		int size = _mergeRanges.size();
		List result = new ArrayList();
		AreaRef rect;
		for(int i=0;i<size;i++){
			rect = (MergedRect)_mergeRanges.get(i);
			if(rect.getColumn()>_frozenCol && (rect.getColumn()<left || rect.getLastColumn()>right)){
				continue;
			}
			result.add(rect);
		}
		
		boolean conti = true;
		while(conti){
			conti = false;
			size = result.size();
			for(int i=0;i<size;i++){
				rect = (MergedRect)result.get(i);
				if(rect.getLastRow()>row && rect.getRow()<=row){
					row = rect.getLastRow();
					conti = true;
					result.remove(i);
					break;
				}
			}
		}
		return row;
	}

	public int getTopConnectedRow(int row, int left, int right) {
		int size = _mergeRanges.size();
		List result = new ArrayList();
		AreaRef rect;
		for(int i=0;i<size;i++){
			rect = (MergedRect)_mergeRanges.get(i);
			if(rect.getColumn()>_frozenCol && (rect.getColumn()<left || rect.getLastColumn()>right)){
				continue;
			}
			result.add(rect);
		}
		
		boolean conti = true;
		while(conti){
			conti = false;
			size = result.size();
			for(int i=0;i<size;i++){
				rect = (MergedRect)result.get(i);
				if(rect.getRow()<row && rect.getLastRow()>=row){
					row = rect.getRow();
					conti = true;
					result.remove(i);
					break;
				}
			}
		}
		return row;
	}

	public void updateMergeRange(int oleft, int otop, int oright, int obottom, int left, int top, int right,
			int bottom, Set toadd, Set torem) {
		for(int i=otop;i<=obottom;i++){
			MergedRect mblock = getMergeRange(i,oleft);
			if(mblock!=null){
				torem.add(mblock);
				_mergeRanges.remove(mblock);
			}
		}
		
		final MergedRect mblock = new MergedRect(_mergeId.next(),left,top,right,bottom);
		toadd.add(mblock);
		_mergeRanges.add(mblock);

		rebuildIndex();
	}

	public void deleteMergeRange(int left, int top, int right, int bottom, Set torem) {
		final MergedRect mblock = getMergeRange(top, left);
		if(mblock!=null){
			torem.add(mblock);
			_mergeRanges.remove(mblock);
		}
		rebuildIndex();
	}

	public void addMergeRange(int left, int top, int right, int bottom, Set toadd, Set torem) {
		MergedRect mblock = getMergeRange(top, left);
		if(mblock!=null){
			torem.add(mblock);
			_mergeRanges.remove(mblock);
		}
		
		mblock = new MergedRect(_mergeId.next(),left,top,right,bottom);
		toadd.add(mblock);
		_mergeRanges.add(mblock);
		
		rebuildIndex();
	}

	public void deleteAffectedMergeRangeByColumn(int col,Set removed) {
		for(Iterator iter = _mergeRanges.iterator();iter.hasNext();){
			MergedRect block = (MergedRect)iter.next();
			int right = block.getLastColumn();
			if(right<col) continue;
			removed.add(block);
		}
		for(Iterator iter = removed.iterator();iter.hasNext();){
			MergedRect block = (MergedRect)iter.next();
			_mergeRanges.remove(block);
		}
		rebuildIndex();
	}

	public void deleteAffectedMergeRangeByRow(int row,Set removed) {
		for(Iterator iter = _mergeRanges.iterator();iter.hasNext();){
			MergedRect block = (MergedRect)iter.next();
			int bottom = block.getLastRow();
			if(bottom<row) continue;
			removed.add(block);
		}
		for(Iterator iter = removed.iterator();iter.hasNext();){
			MergedRect block = (MergedRect)iter.next();
			_mergeRanges.remove(block);
		}
		rebuildIndex();
	}
	
}
