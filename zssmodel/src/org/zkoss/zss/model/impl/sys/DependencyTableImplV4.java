package org.zkoss.zss.model.impl.sys;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.geometry.internal.RectangleFloat;
import org.zkoss.util.logging.Log;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SBookSeries;
import org.zkoss.zss.model.impl.sys.utils.RefUtils;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.dependency.Ref.RefType;

import java.util.*;
import java.util.Map.Entry;

/* DependencyTableImpl.java
 Purpose:
 Description:
 History:
 Nov 22, 2013 Created by Pao Wang
 Copyright (C) 2013 Potix Corporation. All Rights Reserved.
 */

/**
 * Single cell optimization.
 */
public class DependencyTableImplV4 extends DependencyTableAdv {
	private static final long serialVersionUID = 1L;
	private static final Log _logger = Log.lookup(DependencyTableImplV4.class.getName());
	protected static final EnumSet<RefType> _regionTypes = EnumSet.of(RefType.BOOK, RefType.SHEET, RefType.AREA,
			RefType.CELL, RefType.TABLE);

	/** Map<dependant, precedent> */
	protected Map<Ref, Set<Ref>> _map = new LinkedHashMap<Ref, Set<Ref>>();

	private RTree<Ref, Rectangle> depGraph  = RTree.create();

	/* Single cell dependencies - , precedent, set<dependant> */
	private Map<Ref, Set<Ref>> singleDepMap = new HashMap<>();

	/** Map<precedent, Set<dependant>> */
	protected Map<Ref, Set<Ref>> _mapCompressed = new LinkedHashMap<Ref, Set<Ref>>();

	protected SBookSeries _books;

	public DependencyTableImplV4() {
	}

	@Override
	public void setBookSeries(SBookSeries series) {
		this._books = series;
	}

	@Override
	public void add(Ref dependant, Ref precedent) {
		if (precedent.getType()==RefType.CELL) {
			Set<Ref> dependants = singleDepMap.get(precedent);
			if (dependants==null)
			{
				dependants = new HashSet<>();
				singleDepMap.put(precedent, dependants);
			}
			dependants.add(dependant);
		}
		else if (precedent.getType()==RefType.AREA)
			depGraph = depGraph.add(dependant, RefUtils.refToRect(precedent));
		Set<Ref> precedents = _map.get(dependant);
		if(precedents == null) {
			precedents = new LinkedHashSet<>();
			_map.put(dependant, precedents);
		}
		precedents.add(precedent);
	}

	@Override
	public void addPreDep(Ref precedent, Set<Ref> dependent) {
		_mapCompressed.put(precedent, dependent);
	}

	public void clear() {
		_map.clear();
	}

	@Override
	public void clearDependents(Ref dependant) {
		throw new RuntimeException("Not implemented");
	}

    @Override
    public long getLastLookupTime() {
        return 0;
    }

	@Override
	public Set<Ref> getDependents(Ref precedent) {
		Set<Ref> dependantSet = _mapCompressed.get(precedent);
		if (dependantSet!=null) {
			return dependantSet;
		}
		else {
			return getDependentsInternal(precedent);
		}
	}

	@Override
	public Set<Ref> getActualDependents(Ref precedent) {
		throw new RuntimeException("unsuported");
	}


	private Set<Ref> getDependentsInternal(Ref precedent) {
		// ZSS-818
		if (_regionTypes.contains(precedent.getType())) {
			SBook book = _books.getBook(precedent.getBookName());
			if (book == null) { // no such book
				return Collections.emptySet();
			}
			int[] aSheetIndexes = getSheetIndex(book, precedent);
			if (aSheetIndexes[0] < 0) { // no such sheet
				return Collections.emptySet();
			}
		}

		// search dependents and their dependents recursively
		Set<Ref> visited = new HashSet<>();
		Set<Ref> result = new LinkedHashSet<>();
		Queue<Ref> queue = new LinkedList<>();
		queue.add(precedent);
		visited.add(precedent);
		while(!queue.isEmpty()) {
			Ref p = queue.remove();
			Set<Ref> dependents = getDirectDependents(p);
			if (dependents!=null)
			{
				for (Ref r : dependents) {
					if (!visited.contains(r)) {
						visited.add(r);
						queue.add(r);
						result.add(r);
					}
				}
			}
		}
		return result;
	}

	@Override
	public Set<Ref> getDirectDependents(Ref precedent) {
		Set<Ref> result = new LinkedHashSet<>();
		if (precedent.getType() == RefType.CELL) {
			Set<Ref> dep = singleDepMap.get(precedent);
			if (dep != null) {
				result.addAll(dep);
			}
		}
		depGraph.search(RefUtils.refToRect(precedent))
				.toBlocking().toIterable()
				.forEach(e->result.add(e.value()));
		return result;
	}

	private boolean isMatched(Ref a, Ref b) {
		if(_regionTypes.contains(a.getType()) && _regionTypes.contains(b.getType())) {
			return isIntersected(a, b);
		} else {
			return a.equals(b);
		}
	}

	/**
	 * @return true if b overlaps a.
	 */
	private boolean isIntersected(Ref a, Ref b) {

		// check book is the same or not
		if(!a.getBookName().equals(b.getBookName())) {
			return false;
		}

		// anyone is a book, matched immediately
		if(a.getType() == RefType.BOOK || b.getType() == RefType.BOOK) {
			return isBookIntersected(a,b);
		}

		// check sheets are intersected or not
		// just assume 3D reference
		SBook book = _books.getBook(a.getBookName());
		int[] aSheetIndexes = getSheetIndex(book, a);
		int[] bSheetIndexes = getSheetIndex(book, b);
		if(!a.getSheetName().equals(b.getSheetName()) &&
				(isBothNotExist(aSheetIndexes, bSheetIndexes) || !isIntersected(aSheetIndexes[0], aSheetIndexes[1], bSheetIndexes[0], bSheetIndexes[1]))) {
			return false;
		}

		// anyone is a sheet, matched immediately
		if(a.getType() == RefType.SHEET || b.getType() == RefType.SHEET) {
			return isSheetIntersected(a,b);
		}

		// Okay, they only can be area or cell now!
		// check overlapped or not
		return isIntersected(a.getColumn(), a.getRow(), a.getLastColumn(), a.getLastRow(), b.getColumn(),
				b.getRow(), b.getLastColumn(), b.getLastRow());
	}

	private boolean isSheetIntersected(Ref a, Ref b) {
		if(a.getType()==RefType.SHEET){
			return a.getSheetName().equals(b.getSheetName());
		}else if(b.getType()==RefType.SHEET){
			return b.getSheetName().equals(a.getSheetName());
		}
		return false;
	}

	private boolean isBookIntersected(Ref a, Ref b) {
		if(a.getType()==RefType.BOOK){
			return a.getBookName().equals(b.getBookName());
		}else if(b.getType()==RefType.BOOK){
			return b.getBookName().equals(a.getBookName());
		}
		return false;
	}

	private boolean isBothNotExist(int[] aSheetIndexes, int[] bSheetIndexes) {
		for(int i:aSheetIndexes){
			if(i<0) return true;
		}
		for(int i:bSheetIndexes){
			if(i<0) return true;
		}
		return false;
	}

	private int[] getSheetIndex(SBook book, Ref ref) {
		String sn = ref.getSheetName();
		String lsn = ref.getLastSheetName();
		int a = book.getSheetIndex(sn);
		int b = (lsn==null||lsn.equals(sn))?a:book.getSheetIndex(lsn);
		return new int[]{a, b}; // Excel always adjust 3D formula to ascending, we assume this too.
	}

	/** point in line */
	private final boolean isIntersected(int a1, int a2, int b) {
		return (a1 <= b && b <= a2);
	}

	/** line overlaps line */
	private final boolean isIntersected(int a1, int a2, int b1, int b2) {
		return isIntersected(a1, a2, b1) || isIntersected(a1, a2, b2) || isIntersected(b1, b2, a1) || isIntersected(
				b1, b2, a2);
	}

	/** rect. overlaps rect. */
	private final boolean isIntersected(int ax1, int ay1, int ax2, int ay2, int bx1, int by1, int bx2, int by2) {
		return (isIntersected(ax1, ax2, bx1, bx2) && isIntersected(ay1, ay2, by1, by2));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Entry<Ref, Set<Ref>> entry : _map.entrySet()) {
			Ref target = entry.getKey();
			sb.append(target).append('\n');
			for(Ref pre : entry.getValue()) {
				sb.append('\t').append(pre).append('\n');
			}
		}
		return sb.toString();
	}

	@Override
	public void merge(DependencyTableAdv dependencyTable) {
		if (!(dependencyTable instanceof DependencyTableImplV4)) {
			// just in case
			_logger.error("can't merge different type of dependency table: " + dependencyTable.getClass().getName());
			return;
		}

		// simply, just put everything in
		DependencyTableImplV4 another = (DependencyTableImplV4) dependencyTable;
		_map.putAll(another._map);
	}

	@Override
	public Set<Ref> searchPrecedents(RefFilter filter){
		Set<Ref> precedents = new LinkedHashSet<Ref>();
		for(Entry<Ref, Set<Ref>> entry : _map.entrySet()) {
			for(Ref pre : entry.getValue()) {
				if(filter.accept(pre)) {
					precedents.add(pre);
				}
			}
		}
		return precedents;
	}

	public void dump(){
		for(Entry<Ref, Set<Ref>> entry : _map.entrySet()) {
			System.out.println("["+entry.getKey()+"] depends on");
			for(Ref ref:entry.getValue()){
				System.out.println("\t+["+ref+"]");
			}
		}
	}

	//ZSS-648
	@Override
	public Set<Ref> getDirectPrecedents(Ref dependent) {
		return _map.get(dependent);
	}

	//ZSS-815
	@Override
	public void adjustSheetIndex(String bookName, int index, int size) {
		// do nothing
	}

	//ZSS-820
	@Override
	public void moveSheetIndex(String bookName, int oldIndex, int newIndex) {
		// do nothing
	}
}
