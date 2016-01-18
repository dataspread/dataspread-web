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
package org.zkoss.zss.model.sys.formula;

import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.STable;
import org.zkoss.zss.model.SheetRegion;
import org.zkoss.zss.model.sys.dependency.Ref;

/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public interface FormulaEngine {
	
	String KEY_EXTERNAL_BOOK_NAMES = "$ZSS_EXTERNAL_BOOK_NAMES$";
	String KEY_SHEET_INDEXES = "$ZSS_SHEET_INDEXES$";

	public FormulaExpression parse(String formula, FormulaParseContext context);
	
	/**
	 * Shift the formula base on the offset
	 * @param formula
	 * @param rowOffset
	 * @param columnOffset
	 * @param context
	 * @return
	 */
	public FormulaExpression shift(String formula, int rowOffset,int columnOffset, FormulaParseContext context);
	
	/**
	 * Transpose the formula base one the origin
	 * @param formula
	 * @param rowOrigin
	 * @param columnOrigin
	 * @param context
	 * @return
	 */
	public FormulaExpression transpose(String formula, int rowOrigin,int columnOrigin, FormulaParseContext context);
	/**
	 * Shift the formula that care on sheet and region.
	 * @param formula
	 * @param srcRegion
	 * @param rowOffset
	 * @param columnOffset
	 * @param context
	 * @return
	 */
	public FormulaExpression move(String formula, SheetRegion srcRegion, int rowOffset,int columnOffset,
			FormulaParseContext context);
	
	public FormulaExpression shrink(String formula, SheetRegion srcRegion, boolean hrizontal,
			FormulaParseContext context);
	
	public FormulaExpression extend(String formula, SheetRegion srcRegion, boolean hrizontal,
			FormulaParseContext context);
	
	public FormulaExpression renameSheet(String formula, SBook book, String oldName,String newName,
			FormulaParseContext context);
	
	public FormulaExpression renameName(String formula, SBook book, String oldName,String newName,
			FormulaParseContext context);
	
	public EvaluationResult evaluate(FormulaExpression expr, FormulaEvaluationContext context);

	public void clearCache(FormulaClearContext context);

	//ZSS-747
	/**
	 * Shift the formula base on the offset
	 * @param formula
	 * @param rowOffset
	 * @param columnOffset
	 * @param context
	 * @return
	 * @since 3.6.0
	 */
	public FormulaExpression shiftPtgs(FormulaExpression fexpr, int rowOffset,int columnOffset, FormulaParseContext context);
	
	//ZSS-747
	/**
	 * Transpose the formula base one the origin
	 * @param formula
	 * @param rowOrigin
	 * @param columnOrigin
	 * @param context
	 * @return
	 * @since 3.6.0
	 */
	public FormulaExpression transposePtgs(FormulaExpression fexpr, int rowOrigin,int columnOrigin, FormulaParseContext context);
	
	//ZSS-747
	/**
	 * Shift the formula that care on sheet and region.
	 * @param formula
	 * @param srcRegion
	 * @param rowOffset
	 * @param columnOffset
	 * @param context
	 * @return
	 * @since 3.6.0
	 */
	public FormulaExpression movePtgs(FormulaExpression fexpr, SheetRegion srcRegion, int rowOffset,int columnOffset,
			FormulaParseContext context);
	
	//ZSS-747
	/**
	 * 
	 * @param fexpr
	 * @param srcRegion
	 * @param hrizontal
	 * @param context
	 * @return
	 * @since 3.6.0
	 */
	public FormulaExpression shrinkPtgs(FormulaExpression fexpr, SheetRegion srcRegion, boolean hrizontal,
			FormulaParseContext context);
	
	//ZSS-747
	/**
	 * 
	 * @param fexpr
	 * @param srcRegion
	 * @param hrizontal
	 * @param context
	 * @return
	 * @since 3.6.0
	 */
	public FormulaExpression extendPtgs(FormulaExpression fexpr, SheetRegion srcRegion, boolean hrizontal,
			FormulaParseContext context);
	
	//ZSS-747
	/**
	 * 
	 * @param fexpr
	 * @param book
	 * @param oldName
	 * @param newName
	 * @param context
	 * @return
	 * @since 3.6.0
	 */
	public FormulaExpression renameSheetPtgs(FormulaExpression fexpr, SBook book, String oldName,String newName,
			FormulaParseContext context);
	
	//ZSS-747
	/**
	 * 
	 * @param fexpr
	 * @param book
	 * @param oldName
	 * @param newName
	 * @param context
	 * @return
	 * @since 3.6.0
	 */
	public FormulaExpression renameNamePtgs(FormulaExpression fexpr, SBook book, int sheetIndex, String oldName,String newName,
			FormulaParseContext context);
	//ZSS-790
	/**
	 * 
	 * @param formula
	 * @param book
	 * @param sheetIndex
	 * @param oldName
	 * @param newName
	 * @param context
	 * @return
	 * @since 3.6.0
	 */
	public FormulaExpression renameName(String formula, SBook book, int sheetIndex, String oldName,String newName,
			FormulaParseContext context);


	//ZSS-747
	/**
	 * 
	 * @param fexpr
	 * @param context
	 * @since 3.6.0
	 */
	public void updateDependencyTable(FormulaExpression fexpr, FormulaParseContext context);
	
	//ZSS-820
	/**
	 * 
	 * @param fexpr
	 * @param book
	 * @param oldName
	 * @param newName
	 * @param context
	 * @since 3.7.0
	 */
	public FormulaExpression reorderSheetPtgs(FormulaExpression fexpr, SBook book, 
			int oldIndex, int newIndex, FormulaParseContext context);

	//ZSS-966
	/**
	 * 
	 * @param fexpr
	 * @param oldName
	 * @param newName
	 * @param context
	 * @return
	 * @since 3.8.0
	 */
	public FormulaExpression renameTableNameTablePtgs(FormulaExpression fexpr, SBook book, String oldName,String newName,
			FormulaParseContext context);


	//ZSS-967
	/**
	 * 
	 * @param fexpr
	 * @param tableName
	 * @param oldName
	 * @param newName
	 * @param context
	 * @return
	 * @since 3.8.0
	 */
	public FormulaExpression renameColumnNameTablePtgs(FormulaExpression fexpr, STable table, String oldName,String newName,
			FormulaParseContext context);
}
