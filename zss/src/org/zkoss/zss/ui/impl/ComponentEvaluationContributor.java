/* ComponentEvaluationContributor.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/26, modify by Dennis Chen.
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
 */
package org.zkoss.zss.ui.impl;

import java.io.Serializable;

import org.zkoss.xel.Function;
import org.zkoss.xel.FunctionMapper;
import org.zkoss.xel.VariableResolver;
import org.zkoss.xel.XelException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.sys.formula.EvaluationContributor;
/**
 * To contribute function and variable
 * @author dennis
 * @since 3.5
 */
public class ComponentEvaluationContributor implements EvaluationContributor,Serializable {
	private static final long serialVersionUID = 1L;
	private Component comp;
	public ComponentEvaluationContributor(Component comp){
		this.comp = comp;
	}
	
	private boolean isBelowDesktopScope(SBook book){
		String scope = book.getShareScope();
		return scope==null||"desktop".equals(scope);
	}
	
	private boolean checkAlive(){
		if(comp==null){
			return false;
		}else if(comp.getDesktop()==null){
			comp = null;
			return false;
		}
		return true;
	}
	
	@Override
	public FunctionMapper getFunctionMaper(SBook book) {
		if(!checkAlive() && !isBelowDesktopScope(book)){
			//should contribute any thing if the scope large then desktop
			return null;			
		}
		return new FunctionMapper() {
			@Override
			public Function resolveFunction(String prefix, String name)
					throws XelException {
				Page page = comp.getPage();
				if(page!=null){
					FunctionMapper mapper = page.getFunctionMapper();
					if(mapper!=null){
						return mapper.resolveFunction(prefix, name);
					}
				}
				return null;
			}
		};
	}

	@Override
	public VariableResolver getVariableResolver(SBook book) {
		if(!checkAlive() &&!isBelowDesktopScope(book)){
			//should contribute any thing if the scope large then desktop
			return null;			
		}
		return new VariableResolver() {
			@Override
			public Object resolveVariable(String name) throws XelException {
				
				Object result = null;
				Page page = comp.getPage();
				if (page != null) {
					result = page.getZScriptVariable(comp, name);
				}
				if (result == null) {
					result = comp.getAttributeOrFellow(name, true);
				}
				if (result == null && page != null) {
					result = page.getXelVariable(null, null, name, true);
				}
				return result;
			}
		};
	}

}
