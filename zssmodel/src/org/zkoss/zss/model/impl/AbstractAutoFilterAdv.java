/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.zkoss.zss.model.SAutoFilter;
import org.zkoss.zss.model.SBook;

/**
 * 
 * @author Dennis
 * @since 3.5.0
 */
public abstract class AbstractAutoFilterAdv implements SAutoFilter,Serializable{
	private static final long serialVersionUID = 1L;
	
	public abstract void renameSheet(SBook book, String oldName, String newName); //ZSS-555
	
	/**
	 * @since 3.5.0
	 */
	public static class FilterColumnImpl implements NFilterColumn, Serializable{
		private static final long serialVersionUID = 1L;
		private int _index;
		private List<String> _filters;
		private Set _criteria1;
		private Set _criteria2;
		private Boolean _showButton;
		private FilterOp _op = FilterOp.AND;
		
		public FilterColumnImpl(int index){
			this._index = index;
		}
		
		@Override
		public int getIndex() {
			return _index;
		}

		@Override
		public List<String> getFilters() {
			return _filters==null?Collections.EMPTY_LIST:Collections.unmodifiableList(_filters);
		}

		@Override
		public Set getCriteria1() {
			return _criteria1==null?Collections.EMPTY_SET:Collections.unmodifiableSet(_criteria1);
		}

		@Override
		public Set getCriteria2() {
			return _criteria2==null?Collections.EMPTY_SET:Collections.unmodifiableSet(_criteria2);
		}

		@Override
		public boolean isShowButton() {
			return _showButton==null?true:_showButton.booleanValue();
		}

		@Override
		public FilterOp getOperator() {
			return _op;
		}

		private Set getCriteriaSet(Object criteria) {
			Set set = new HashSet();
			if (criteria instanceof String[]) {
				String[] strings = (String[]) criteria;
				for(int j = 0; j < strings.length; ++j) {
					set.add(strings[j]);
				}
			}else if (criteria instanceof Set){
				set = (Set)criteria;
			}
			return set;
		}
		
		@Override
		public void setProperties(FilterOp filterOp, Object criteria1,
				Object criteria2, Boolean showButton) {
			this._op = filterOp;
			this._criteria1 = getCriteriaSet(criteria1);
			this._criteria2 = getCriteriaSet(criteria2);
			boolean blank1 = this._criteria1.contains("=");
			if(showButton!=null){
				_showButton = showButton; // ZSS-705
			}
			
			
			
			if (criteria1 == null) { //remove filtering
				_filters = null;
				return;
			}
			
			//TODO, more filtering operation
			switch(filterOp) {
			case VALUES:
				
				_filters =  new LinkedList<String>();
				
				for(Object obj:this._criteria1){
					if(obj instanceof String){
						_filters.add((String)obj);
					}
				}
				if(_filters.size()==0){
					_filters = null;
				}
			}
		}
		
		//ZSS-688
		//@since 3.6.0
		/*package*/ FilterColumnImpl cloneFilterColumnImpl() {
			FilterColumnImpl tgt = new FilterColumnImpl(this._index);
			if (this._filters != null) {
				tgt._filters = new LinkedList<String>();
				for (String f : this._filters) {
					tgt._filters.add(f);
				}
			}
			if (this._criteria1 != null) {
				tgt._criteria1 = new HashSet();
				for (Object c : this._criteria1) {
					tgt._criteria1.add(c);
				}
			}
			if (this._criteria2 != null) {
				tgt._criteria2 = new HashSet();
				for (Object c : this._criteria2) {
					tgt._criteria2.add(c);
				}
			}
			tgt._showButton = this._showButton;
			tgt._op = this._op;
			
			return tgt;
		}
	}
}
