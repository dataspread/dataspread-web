package org.zkoss.zss.model.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SAutoFilter;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.sys.DependencyTableAdv;
import org.zkoss.zss.model.sys.dependency.DependencyTable;
import org.zkoss.zss.model.sys.dependency.ObjectRef.ObjectType;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.util.Validations;
/**
 * The auto fitler implement
 * @author dennis
 * @since 3.5.0
 */
public class AutoFilterImpl extends AbstractAutoFilterAdv {
	private static final long serialVersionUID = 1L;
	
	private final CellRegion _region;
	private final TreeMap<Integer,NFilterColumn> _columns;

	public AutoFilterImpl(CellRegion region){
		this._region = region;
		_columns = new TreeMap<Integer,NFilterColumn>();
	}
	
	@Override
	public CellRegion getRegion() {
		return _region;
	}

	@Override
	public Collection<NFilterColumn> getFilterColumns() {
		return Collections.unmodifiableCollection(_columns.values());
	}

	@Override
	public NFilterColumn getFilterColumn(int index, boolean create) {
		NFilterColumn col = _columns.get(index);
		if(col==null && create){
			int s = _region.getLastColumn()-_region.getColumn()+1; 
			if(index>=s){
				throw new IllegalStateException("the column index "+index+" >= "+s);
			}
			_columns.put(index, col=new FilterColumnImpl(index));
		}
		return col;
	}

	@Override
	public void clearFilterColumn(int index) {
		_columns.remove(index);
	}
	
	@Override
	public void clearFilterColumns() {
		_columns.clear();
	}	

	//ZSS-555
	@Override
	public void renameSheet(SBook book, String oldName, String newName) {
		Validations.argNotNull(oldName);
		Validations.argNotNull(newName);
		if (oldName.equals(newName)) return; // nothing change, let go
		
		final String bookName = book.getBookName();
		// remove old ObjectRef
		Ref dependent = new ObjectRefImpl(bookName, oldName, "AUTO_FILTER", ObjectType.AUTO_FILTER);

		final DependencyTable dt = 
				((AbstractBookSeriesAdv) book.getBookSeries()).getDependencyTable();
		dt.clearDependents(dependent);
		
		// Add new ObjectRef into DependencyTable so we can extend/shrink/move
		dependent = new ObjectRefImpl(bookName, newName, "AUTO_FILTER", ObjectType.AUTO_FILTER);
		
		// prepare new dummy CellRef to enforce DataValidation reference dependency
		if (this._region != null) {
			Ref dummy = new RefImpl(bookName, newName, 
				_region.row, _region.column, _region.lastRow, _region.lastColumn);
			dt.add(dependent, dummy);
		}
	}
	
	//ZSS-688
	//@since 3.6.0
	/*package*/ AutoFilterImpl cloneAutoFilterImpl() {
		final AutoFilterImpl tgt = 
				new AutoFilterImpl(new CellRegion(this._region.row, this._region.column, this._region.lastRow, this._region.lastColumn));

		for (SAutoFilter.NFilterColumn value : this._columns.values()) {
			final FilterColumnImpl srccol = (FilterColumnImpl) value;
			final FilterColumnImpl tgtcol = srccol.cloneFilterColumnImpl(); 
			_columns.put(tgtcol.getIndex(), tgtcol);
		}
		
		return tgt;
	}
}
