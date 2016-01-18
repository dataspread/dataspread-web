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
package org.zkoss.zss.model.sys;

import org.zkoss.lang.Library;
import org.zkoss.util.logging.Log;
import org.zkoss.zss.model.impl.sys.CalendarUtilImpl;
import org.zkoss.zss.model.impl.sys.DependencyTableImpl;
import org.zkoss.zss.model.impl.sys.FormatEngineImpl;
import org.zkoss.zss.model.impl.sys.InputEngineImpl;
import org.zkoss.zss.model.impl.sys.formula.FormulaEngineImpl;
import org.zkoss.zss.model.sys.dependency.DependencyTable;
import org.zkoss.zss.model.sys.format.FormatEngine;
import org.zkoss.zss.model.sys.formula.FormulaEngine;
import org.zkoss.zss.model.sys.input.InputEngine;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public class EngineFactory {

	private static final Log _logger = Log.lookup(EngineFactory.class.getName());
	
	static private EngineFactory _instance;
	
	static private CalendarUtil _calendarUtil = new CalendarUtilImpl();

	private EngineFactory() {
	}

	public static EngineFactory getInstance() {
		if (_instance == null) {
			synchronized (EngineFactory.class) {
				if (_instance == null) {
					// TODO from config
					_instance = new EngineFactory();
				}
			}
		}
		return _instance;
	}

	static InputEngine _inputEngine;
	static Class<?> inputEngineClazz;
	static {
		String clz = Library.getProperty("org.zkoss.zss.model.InputEngine.class");
		if(clz!=null){
			try {
				inputEngineClazz = Class.forName(clz);
			} catch(Exception e) {
				_logger.error(e.getMessage(), e);
			}			
		}
		
	}
	public InputEngine createInputEngine() {
		if (_inputEngine == null) {
			try {
				if(inputEngineClazz != null) {
					return (_inputEngine = (InputEngine)inputEngineClazz.newInstance());
				}
			} catch(Exception e) {
				_logger.error(e.getMessage(), e);
				inputEngineClazz = null;
			}
			_inputEngine = new InputEngineImpl();
		}
		return _inputEngine;
	}

	static Class<?> formulaEngineClazz;
	static {
		String clz = Library.getProperty("org.zkoss.zss.model.FormulaEngine.class");
		if(clz!=null){
			try {
				formulaEngineClazz = Class.forName(clz);
			} catch(Exception e) {
				_logger.error(e.getMessage(), e);
			}			
		}
		
	}
	
	public FormulaEngine createFormulaEngine() {
		try {
			if(formulaEngineClazz != null) {
				return (FormulaEngine)formulaEngineClazz.newInstance();
			}
		} catch(Exception e) {
			_logger.error(e.getMessage(), e);
			formulaEngineClazz = null;
		}
		return new FormulaEngineImpl();
	}

	//ZSS-815
	static Class<?> dependencyTableClazz;
	static {
		String clz = Library.getProperty("org.zkoss.zss.model.DependencyTable.class");
		if(clz!=null){
			try {
				dependencyTableClazz = Class.forName(clz);
			} catch(Exception e) {
				_logger.error(e.getMessage(), e);
			}			
		}
		
	}
	//ZSS-815
	public DependencyTable createDependencyTable() {
		try {
			if(dependencyTableClazz != null) {
				return (DependencyTable)dependencyTableClazz.newInstance();
			}
		} catch(Exception e) {
			_logger.error(e.getMessage(), e);
			dependencyTableClazz = null;
		}
		return new DependencyTableImpl();
	}
	
	static FormatEngine _formatEngine;
	static Class<?> formatEngineClazz;
	static {
		String clz = Library.getProperty("org.zkoss.zss.model.FormatEngine.class");
		if(clz!=null){
			try {
				formatEngineClazz = Class.forName(clz);
			} catch(Exception e) {
				_logger.error(e.getMessage(), e);
			}			
		}
		
	}
	public FormatEngine createFormatEngine() {
		if (_formatEngine == null) {
			try {
				if(formatEngineClazz != null) {
					return (_formatEngine = (FormatEngine)formatEngineClazz.newInstance());
				}
			} catch(Exception e) {
				_logger.error(e.getMessage(), e);
				formatEngineClazz = null;
			}
			_formatEngine = new FormatEngineImpl();
		}
		return _formatEngine;
	}
	
	public CalendarUtil getCalendarUtil(){
		return _calendarUtil;
	}

}
