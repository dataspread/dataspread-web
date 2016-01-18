package org.zkoss.zss.model.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * to lo/high (firstIdx/lastIdx) index the column array
 * @author dennis
 * @since 3.5.0
 */
/*package*/ class ColumnArrayPool implements Serializable{

	private static final long serialVersionUID = 1L;
	private final TreeMap<Integer,AbstractColumnArrayAdv> _columnArrayFirst = new TreeMap<Integer,AbstractColumnArrayAdv>();
	private final TreeMap<Integer,AbstractColumnArrayAdv> _columnArrayLast = new TreeMap<Integer,AbstractColumnArrayAdv>();
	
	public ColumnArrayPool(){
		
	}

	public boolean hasLastKey(int columnIdx) {
		return _columnArrayLast.size()<=0 || columnIdx>_columnArrayLast.lastKey();
	}

	public SortedMap<Integer, AbstractColumnArrayAdv> lastSubMap(int columnIdx) {
		return _columnArrayLast.subMap(columnIdx, true, _columnArrayLast.lastKey(),true);
	}
	
	public Collection<AbstractColumnArrayAdv> firstSubValues(int start, int end){
		return _columnArrayFirst.subMap(start, true, end, true).values();
	}

	public Collection<AbstractColumnArrayAdv> values() {
		return _columnArrayLast.values();
	}

	public AbstractColumnArrayAdv overlap(int index, int lastIndex) {
		SortedMap<Integer,AbstractColumnArrayAdv> overlap = _columnArrayFirst.size()==0?null:_columnArrayFirst.subMap(index, true, lastIndex,true); 
		if(overlap!=null && overlap.size()>0){
			return overlap.get(overlap.firstKey());
		}
		overlap = _columnArrayLast.size()==0?null:_columnArrayLast.subMap(index, true, lastIndex, true); 
		if(overlap!=null && overlap.size()>0){
			return overlap.get(overlap.firstKey());
		}
		return null;
		
	}

	public int size() {
		return _columnArrayLast.size();
	}

	public int lastLastKey() {
		return _columnArrayLast.lastKey();
	}

	public void put(AbstractColumnArrayAdv array) {
		AbstractColumnArrayAdv old;
		if((old = _columnArrayFirst.put(array.getIndex(),array))!=null){
			throw new IllegalStateException("try to replace a column array in first map "+old +", new "+array);
		}
		if((old = _columnArrayLast.put(array.getLastIndex(),array))!=null){
			throw new IllegalStateException("try to replace a column array in last map"+old +", new "+array);
		}
	}

	public void remove(AbstractColumnArrayAdv array) {
		_columnArrayFirst.remove(array.getIndex());
		_columnArrayLast.remove(array.getLastIndex());
	}

	public int firstFirstKey() {
		return _columnArrayFirst.firstKey();
	}

	public void clear() {
		_columnArrayFirst.clear();
		_columnArrayLast.clear();
	}
	
	public Collection<AbstractColumnArrayAdv> trim(int start){
		Collection<AbstractColumnArrayAdv> effected = _columnArrayFirst.tailMap(start,true).values();
		LinkedList<AbstractColumnArrayAdv> remove = new LinkedList<AbstractColumnArrayAdv>(); 
		for(AbstractColumnArrayAdv array:new ArrayList<AbstractColumnArrayAdv>(effected)){
			remove(array);
			remove.add(array);
		}
		return remove;
	}
}
