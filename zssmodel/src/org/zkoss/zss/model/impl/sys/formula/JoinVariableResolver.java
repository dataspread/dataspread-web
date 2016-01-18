/* JoinVariableResolver.java

	Purpose:
		
	Description:
		
	History:
		Apr 7, 2010 12:49:09 PM, Created by henrichen

Copyright (C) 2010 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.model.impl.sys.formula;

import java.util.LinkedHashSet;

import org.zkoss.xel.VariableResolver;
import org.zkoss.xel.XelException;

/**
 * Aggregate VariableResolver that join serveral {@link VariableResolver} together. The first
 * added get called first.
 * 
 * @author henrichen
 */
public class JoinVariableResolver implements VariableResolver {
	private LinkedHashSet<VariableResolver> _resolvers;
	
	public JoinVariableResolver() {
		_resolvers = new LinkedHashSet<VariableResolver>(4);
	}
	
	/*package*/ void addVariableResolver(VariableResolver resolver) {
		_resolvers.add(resolver);
	}
	/*package*/ void removeVariableResolver(VariableResolver resolver) {
		_resolvers.remove(resolver);
	}
	
	//--VariableResolver--//
	public Object resolveVariable(String name) throws XelException {
		for (VariableResolver resolver : _resolvers) {
			final Object result = resolver.resolveVariable(name);
			if (result != null) return result;
		}
		return null;
	}
}
