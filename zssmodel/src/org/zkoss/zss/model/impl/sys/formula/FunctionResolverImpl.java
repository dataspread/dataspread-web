/* DefaultFunctionResolver.java

	Purpose:
		
	Description:
		
	History:
		Dec 26, 2013 Created by Pao Wang

Copyright (C) 2013 Potix Corporation. All Rights Reserved.
 */
package org.zkoss.zss.model.impl.sys.formula;

import org.zkoss.poi.ss.formula.DependencyTracker;
import org.zkoss.poi.ss.formula.udf.UDFFinder;
import org.zkoss.xel.FunctionMapper;
import org.zkoss.zss.model.sys.formula.FunctionResolver;

/**
 * A default function resolver.
 * @author Pao
 * @since 3.5.0
 */
public class FunctionResolverImpl implements FunctionResolver {

	private DependencyTracker _tracker = new DependencyTrackerImpl();

	public FunctionResolverImpl() {
	}

	@Override
	public UDFFinder getUDFFinder() {
		return null;
	}

	@Override
	public FunctionMapper getFunctionMapper() {
		return null;
	}

	@Override
	public DependencyTracker getDependencyTracker() {
		return _tracker;
	}

}
