/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.range.impl;

import org.zkoss.poi.ss.usermodel.ZssContext;
import org.zkoss.util.Locales;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SCell.CellType;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.format.FormatContext;
import org.zkoss.zss.model.sys.format.FormatEngine;
import org.zkoss.zss.model.sys.formula.FormulaEngine;
import org.zkoss.zss.range.SRange;
/**
 * 
 * @author Dennis
 * @since 3.5.0
 */
public class RangeHelperBase {
	protected final SRange range;
	protected final SSheet sheet;
	private FormatEngine _formatEngine;
	private FormulaEngine _formulaEngine;
	
	public RangeHelperBase(SRange range){
		this.range = range;
		this.sheet = range.getSheet();
	}

	public static boolean isBlank(SCell cell){
		return cell==null || cell.isNull()||cell.getType() == CellType.BLANK;
	}
	
	protected FormatEngine getFormatEngine(){
		if(_formatEngine==null){
			_formatEngine = EngineFactory.getInstance().createFormatEngine();
		}
		return _formatEngine;
	}
	
	public String getFormattedText(SCell cell){
		return getFormatEngine().format(cell, new FormatContext(ZssContext.getCurrent().getLocale())).getText();
	}
	
	protected FormulaEngine getFormulaEngine(){
		if (_formulaEngine == null){
			_formulaEngine = EngineFactory.getInstance().createFormulaEngine();
		}
		return _formulaEngine;
	}
	
	public int getRow() {
		return range.getRow();
	}

	public int getColumn() {
		return range.getColumn();
	}

	public int getLastRow() {
		return range.getLastRow();
	}

	public int getLastColumn() {
		return range.getLastColumn();
	}

	public boolean isWholeRow(){
		return range.isWholeRow();
	}
	
	public boolean isWholeSheet(){
		return range.isWholeSheet();
	}
	
	public boolean isWholeColumn() {
		return range.isWholeColumn();
	}
}
