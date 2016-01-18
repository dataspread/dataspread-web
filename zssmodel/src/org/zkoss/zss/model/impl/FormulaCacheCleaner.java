package org.zkoss.zss.model.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import org.zkoss.zss.model.SBookSeries;
import org.zkoss.zss.model.sys.dependency.DependencyTable;
import org.zkoss.zss.model.sys.dependency.Ref;

/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public class FormulaCacheCleaner {

	static private ThreadLocal<FormulaCacheCleaner>  _current = new ThreadLocal<FormulaCacheCleaner>();
	
	final private SBookSeries _bookSeries;
	
	public FormulaCacheCleaner(SBookSeries bookSeries){
		this._bookSeries = bookSeries;
	}

	public static FormulaCacheCleaner setCurrent(FormulaCacheCleaner ctx){
		FormulaCacheCleaner old = _current.get();
		_current.set(ctx);
		return old;
	}
	
	public static FormulaCacheCleaner getCurrent(){
		return _current.get();
	}
	
	public void clear(Set<Ref> dependents){
		new FormulaCacheClearHelper(_bookSeries).clear(dependents);
	}

	public void clearByPrecedent(Ref precedent) {
		DependencyTable table = ((AbstractBookSeriesAdv)_bookSeries).getDependencyTable();
		Set<Ref> dependents = new LinkedHashSet<Ref>();
		dependents.add(precedent);
		dependents.addAll(table.getEvaluatedDependents(precedent));
		clear(dependents);
	}
}
