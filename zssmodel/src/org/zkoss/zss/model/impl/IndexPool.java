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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
/*package*/ abstract class IndexPool<T> implements Serializable{
	private static final long serialVersionUID = 1L;
	private TreeMap<Integer,T> _objs = new TreeMap<Integer,T>();
	
	public T get(int idx) {
		return _objs.get(idx);
	}
	
	public T put(int idx, T obj){
		T old = _objs.put(idx, obj);
		return old;
	}
	
	public int firstKey(){
		return _objs.size()<=0?-1:_objs.firstKey();
	}
	
	public int lastKey(){
		return _objs.size()<=0?-1:_objs.lastKey();
	}
	
	public Collection<T> clear(int start,int end){
		
		NavigableMap<Integer, T> effected = _objs.subMap(start,true,end,true);
		LinkedList<T> remove = new LinkedList<T>(); 
		Iterator<Entry<Integer,T>> iter = effected.entrySet().iterator();
		while(iter.hasNext()){
			Entry<Integer,T> entry = iter.next();
			T obj = entry.getValue();
			remove.add(obj);
			iter.remove();
		}
		return remove;
	}
	
	public void insert(int start, int size) {
		if(size<=0) return;

		//get last, reversed cell
		SortedMap<Integer,T> effected = _objs.descendingMap().headMap(start,true);
		
		//shift from the end
		for(Entry<Integer,T> entry:new ArrayList<Entry<Integer,T>>(effected.entrySet())){
			int idx = entry.getKey();
			int newidx = idx+size;
			T obj = entry.getValue();
			
			_objs.remove(idx);
			
			resetIndex(newidx,obj);
			
			_objs.put(newidx, obj);
			
		}
	}
	
	abstract void resetIndex(int newidx, T obj);

	public Collection<T> delete(int start, int size) {
		if(size<=0) return Collections.EMPTY_LIST;
		//get last,
		SortedMap<Integer,T> effected = _objs.tailMap(start,true);
		LinkedList<T> remove = new LinkedList<T>(); 
		//shift
		for(Entry<Integer,T> entry:new ArrayList<Entry<Integer,T>>(effected.entrySet())){
			int idx = entry.getKey();
			int newidx = idx-size;
			T obj = entry.getValue();
			_objs.remove(idx);
			if(newidx>=start){
				resetIndex(newidx,obj);
				_objs.put(newidx, obj);
			}else{
				remove.add(obj);
			}
		}
		return remove;
	}
	
	public Collection<T> trim(int start) {
		//get last,
		SortedMap<Integer,T> effected = _objs.tailMap(start,true);
		LinkedList<T> remove = new LinkedList<T>(); 
		//shift
		for(Entry<Integer,T> entry:new ArrayList<Entry<Integer,T>>(effected.entrySet())){
			int idx = entry.getKey();
			_objs.remove(idx);
			remove.add(entry.getValue());
		}
		return remove;
	}
	
	public Set<Integer> keySet(){
		return _objs.keySet();
	}

	public Collection<T> values() {
		return _objs.values();
	}
	public Collection<T> descendingValues() {
		return _objs.descendingMap().values();
	}
	
	public Collection<T> subValues(int start,int end){
		return _objs.subMap(start, true, end, true).values();
	}
	public Collection<T> descendingSubValues(int start,int end){
		return _objs.subMap(start, true, end, true).descendingMap().values();
	}

	public void clear() {
		_objs.clear();
	}
	
	
}
