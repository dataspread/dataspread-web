/* DependencyTableAdv.java

	Purpose:
		
	Description:
		
	History:
		Dec 12, 2013 Created by Pao Wang

Copyright (C) 2013 Potix Corporation. All Rights Reserved.
 */
package org.zkoss.zss.model.impl.sys;

import java.io.Serializable;
import java.util.Set;

import org.zkoss.zss.model.SBookSeries;
import org.zkoss.zss.model.sys.dependency.DependencyTable;
import org.zkoss.zss.model.sys.dependency.Ref;

/**
 * @author Pao
 * @since 3.5.0
 */
public abstract class DependencyTableAdv implements DependencyTable, Serializable {
	private static final long serialVersionUID = 1L;
	
	abstract public void setBookSeries(SBookSeries series);

	abstract public void merge(DependencyTableAdv dependencyTable);
	
	abstract public Set<Ref> getDirectPrecedents(Ref dependent);
	
	abstract public void adjustSheetIndex(String bookName, int index, int size); //ZSS-815

	abstract public void moveSheetIndex(String bookName, int oldIndex, int newIndex); //ZSS-820
}
