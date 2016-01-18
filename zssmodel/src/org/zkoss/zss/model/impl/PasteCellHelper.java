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
package org.zkoss.zss.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.zkoss.poi.ss.formula.FormulaRenderer;
import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.InvalidModelOpException;
import org.zkoss.zss.model.PasteOption;
import org.zkoss.zss.model.PasteOption.PasteType;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SCell.CellType;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SDataValidation;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SheetRegion;
import org.zkoss.zss.model.impl.sys.formula.ParsingBook;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.formula.FormulaEngine;
import org.zkoss.zss.model.sys.formula.FormulaExpression;
import org.zkoss.zss.model.sys.formula.FormulaParseContext;
import org.zkoss.zss.model.util.Validations;
import org.zkoss.zss.range.impl.StyleUtil;
/**
 * 
 * @author Dennis
 * @since 3.5.0
 */
public class PasteCellHelper { //ZSS-693: promote visibility

	private final SSheet _destSheet;
	private final SBook _book;
	private final SCellStyle _defaultStyle;
	public PasteCellHelper(SSheet destSheet){
		this._destSheet = destSheet;
		this._book = destSheet.getBook();
		_defaultStyle = _book.getDefaultCellStyle();
	}
	
	public CellRegion pasteCell(SheetRegion src, CellRegion dest, PasteOption option) {
		Validations.argNotNull(src);
		Validations.argNotNull(dest);
		SSheet srcSheet = src.getSheet();
		boolean sameSheet = srcSheet == _destSheet;
		if(!sameSheet && srcSheet.getBook()!= _book){
			throw new IllegalArgumentException("the src sheet must be in the same book");
		}
		if(option==null){
			option = new PasteOption();
		}
		
		int rowOffset = dest.getRow() - src.getRow();
		int columnOffset = dest.getColumn() - src.getColumn();
	
		int destColCount = dest.getColumnCount();
		int destRowCount = dest.getRowCount();
		
		if(option.getPasteType()==PasteType.COLUMN_WIDTHS){
			if(option.isCut()){
				throw new InvalidModelOpException("can't do cut when copying column width");
			}else if(option.isTranspose()){
				throw new InvalidModelOpException("can't do transport when copying column width");
			}
			int[] widthBuffer = prepareColumnWidth(src);
			int srcColCount = widthBuffer.length;
			boolean wrongColMultiple = (destColCount>1 && destColCount%srcColCount!=0);
			int colMultiple = destColCount<=1||wrongColMultiple?1:destColCount/srcColCount;
			for(int j=0;j<colMultiple;j++){
					int colMultipleOffset = j*srcColCount;
					CellRegion destRegion = new CellRegion(dest.getRow(),dest.getColumn()+colMultipleOffset,
							dest.getRow(),dest.getColumn()+srcColCount -1 + colMultipleOffset);
					pasteColumnWidth(widthBuffer,destRegion);
			}
			return new CellRegion(0,dest.getColumn(),_destSheet.getBook().getMaxRowIndex(),dest.getColumn()+srcColCount*colMultiple-1);
		}
		PasteType pasteType = option.getPasteType();
		boolean handleMerge = shouldHandleMerge(pasteType);
		
		CellRegion srcRegion = src.getRegion();
		if(handleMerge){
			Collection<CellRegion> srcOverlaps = srcSheet.getOverlapsMergedRegions(srcRegion,true);
			//zss-401 , allow overlap when cuting, it will unmerge the overlaps
			if(srcOverlaps.size()>0 && !option.isCut()){
				throw new InvalidModelOpException("Can't copy "+srcRegion.getReferenceString()+" which overlaps merge area "+srcOverlaps.iterator().next().getReferenceString());
			}
		}
		
		//the buffer might be transported
		CellBuffer[][] srcBuffer = prepareCellBuffer(src,option);
		Collection<CellRegion> mergeBuffer = null;
		if(handleMerge){
			mergeBuffer = prepareMergeRegionBuffer(src,option);
		}
		
		//the ValidationBuffer might be transported
		List<ValidationBuffer> srcVBuffer = prepareValidationBuffer(src, option);
		
		SheetRegion cutFrom = null;
		if(option.isCut()){
			//clear the src's value and merge
			clearCell(src);
			clearMergeRegion(src);
			cutFrom = src;
			srcSheet.deleteDataValidationRegion(srcRegion);
		}
		
		// ZSS-608: Special Case - Copy a single cell to a merge cell
		if(srcRegion.isSingle()) {
			for (CellRegion mergedRegion : _destSheet.getMergedRegions()) {
				if (mergedRegion.contains(dest)) {
					CellRegion destRegion = new CellRegion(dest.getRow(),dest.getColumn(),dest.getRow(),dest.getColumn());
					pasteCells(srcBuffer,destRegion,cutFrom,option,rowOffset,columnOffset);
					if (option.isCut()) { //ZSS-696: if cut and paste, must unmerge
						pasteDataValidations(srcVBuffer, src, dest, option); // ZSS-694: only when CUT and paste
						_destSheet.removeMergedRegion(mergedRegion, true); // ZSS-696: should unmerge when cut and paste
					}
					return dest;
				}
			}
		}
		
		int srcColCount = srcBuffer[0].length;
		int srcRowCount = srcBuffer.length;
		
		boolean wrongRowMultiple = (destRowCount>1 && destRowCount%srcRowCount!=0);
		boolean wrongColMultiple = (destColCount>1 && destColCount%srcColCount!=0);

		
		boolean wrongMultiple = wrongRowMultiple||wrongColMultiple;
		int rowMultiple = destRowCount<=1||wrongMultiple?1:destRowCount/srcRowCount;
		int colMultiple = destColCount<=1||wrongMultiple?1:destColCount/srcColCount;

		for(int i=0;i<rowMultiple;i++){
			for(int j=0;j<colMultiple;j++){
				int rowMultpleOffset = i*srcRowCount;
				int colMultipleOffset = j*srcColCount;
				CellRegion destRegion = new CellRegion(dest.getRow()+rowMultpleOffset,dest.getColumn()+colMultipleOffset,
						dest.getRow()+srcRowCount+ -1 + rowMultpleOffset,
						dest.getColumn()+srcColCount -1 + colMultipleOffset);
				pasteCells(srcBuffer,destRegion,cutFrom,option,rowOffset+rowMultpleOffset,columnOffset+colMultipleOffset);
				pasteDataValidations(srcVBuffer, src, destRegion, option); // ZSS-694
				
				if(mergeBuffer!=null && mergeBuffer.size()>0){
					pasteMergeRegion(mergeBuffer,rowOffset+rowMultpleOffset,columnOffset+colMultipleOffset);
				}
			}
		}
		
		return new CellRegion(dest.getRow(),dest.getColumn(),
				dest.getRow()+srcRowCount*rowMultiple-1,
				dest.getColumn()+srcColCount*colMultiple-1);
	}
	
	// ZSS-694
	List<ValidationBuffer> prepareValidationBuffer(SheetRegion src, PasteOption option) {
		final PasteType type = option.getPasteType();
		if (type != PasteType.ALL && type != PasteType.ALL_EXCEPT_BORDERS && type != PasteType.VALIDATAION)
			return Collections.emptyList();
		
		boolean transpose = option.isTranspose();
		SSheet srcSheet = src.getSheet();
		CellRegion srcRegion = src.getRegion();
		List<ValidationBuffer> vbs = new ArrayList<ValidationBuffer>();
		for (SDataValidation dv : srcSheet.getDataValidations()) {
			final Set<CellRegion> overlaps = new HashSet<CellRegion>();
			for (CellRegion rgn : dv.getRegions()) {
				CellRegion overlap = srcRegion.getOverlap(rgn);
				if (overlap != null) {
					overlaps.add(transpose ? 
							new CellRegion(overlap.getColumn(), overlap.getRow(),
									overlap.getLastColumn(), overlap.getLastRow()) : overlap);
				}
			}
			if (overlaps.isEmpty()) continue;
			vbs.add(new ValidationBuffer(dv, overlaps));
		}
		return vbs;
	}
	
	// ZSS-694
	private void pasteDataValidations(List<ValidationBuffer> vbs, 
			SheetRegion src, CellRegion dst, PasteOption option) {
		final PasteType type = option.getPasteType();
		if (type != PasteType.ALL && type != PasteType.ALL_EXCEPT_BORDERS && type != PasteType.VALIDATAION)
			return;
		
		if (vbs.isEmpty()) return;
		
		// paste to destination
		final SSheet srcSheet = src.getSheet();
		final int rowOffset = dst.getRow() - src.getRegion().getRow();
		final int colOffset = dst.getColumn() - src.getRegion().getColumn();
		for (ValidationBuffer vb : vbs) {
			Set<CellRegion> destRegions = convertRegions(vb.regions, rowOffset, colOffset);
			// clear Validation at destRegions 
			for (CellRegion rgn : destRegions) {
				_destSheet.deleteDataValidationRegion(rgn);
			}
			
			// past
			SDataValidation dv = vb.validation;
			// different sheets or the validation has been cut away from its owner sheet, make a copy
			if (!srcSheet.equals(_destSheet) || dv.getSheet() == null) { 
				dv = _destSheet.addDataValidation(null, dv);
				((AbstractDataValidationAdv)dv).setRegions(destRegions);
			} else { // same sheet, adjust regions
				AbstractDataValidationAdv adv = ((AbstractDataValidationAdv)dv); 
				for (CellRegion regn : destRegions) {
					adv.addRegion(regn);
				}
			}
		}
	}
	
	// ZSS-694
	private Set<CellRegion> convertRegions(Set<CellRegion> srcRegions, int rowOffset, int colOffset) {
		Set<CellRegion> dstRegions = new HashSet<CellRegion>();
		for (CellRegion rgn : srcRegions) {
			final int row1 = rgn.getRow() + rowOffset;
			final int col1 = rgn.getColumn() + colOffset;
			final int row2 = rgn.getLastRow() + rowOffset;
			final int col2 = rgn.getLastColumn() + colOffset;
			dstRegions.add(new CellRegion(row1, col1, row2, col2));
		}
		return dstRegions;
	}
	
	// ZSS-694
	private final static class ValidationBuffer {
		final SDataValidation validation;
		final Set<CellRegion> regions;
		ValidationBuffer(SDataValidation validation, Set<CellRegion> regions) {
			this.validation  = validation;
			this.regions = regions;
		}
	}
	
	FormulaEngine formulaEngine;
	private FormulaEngine getFormulaEngine() {
		if(formulaEngine == null){
			formulaEngine = EngineFactory.getInstance().createFormulaEngine();
		}
		return formulaEngine;
	}


	private boolean shouldHandleMerge(PasteType pasteType) {
		return pasteType == PasteType.ALL ||
				pasteType == PasteType.ALL_EXCEPT_BORDERS ||
				pasteType == PasteType.FORMATS;
	}


	private int[] prepareColumnWidth(SheetRegion src){
		int column = src.getColumn();
		int lastColumn = src.getLastColumn();
		int srcColCount = src.getColumnCount();
		int[] widthBuffer = new int[srcColCount];
		SSheet srcSheet = src.getSheet();
		for(int c = column; c <= lastColumn;c++){
			widthBuffer[c-column] = srcSheet.getColumn(c).getWidth();
		}
		return widthBuffer;
	}


	private List<CellRegion> prepareMergeRegionBuffer(SheetRegion src,
			PasteOption option) {
		List<CellRegion> mergeBuffer = new LinkedList<CellRegion>();
		boolean transpose = option.isTranspose();
		for(CellRegion region:src.getSheet().getContainsMergedRegions(src.getRegion())){
			if(transpose){
				int rowAnchor = src.getRow();
				int columnAnchor = src.getColumn();
				int r = rowAnchor + region.getColumn()-columnAnchor;
				int c = columnAnchor + region.getRow()-rowAnchor;
				int lr = r + region.getColumnCount()-1;
				int lc = c + region.getRowCount()-1;
				region = new CellRegion(r, c, lr, lc);
			}
			mergeBuffer.add(region);
		}
		return mergeBuffer;
	}

	private static Set<PasteType> handleStylePasteType = new HashSet<PasteType>(); 
	static{
		handleStylePasteType.add(PasteType.ALL);
		handleStylePasteType.add(PasteType.ALL_EXCEPT_BORDERS);
		handleStylePasteType.add(PasteType.FORMATS);
		handleStylePasteType.add(PasteType.FORMULAS);
		handleStylePasteType.add(PasteType.FORMULAS_AND_NUMBER_FORMATS);
		handleStylePasteType.add(PasteType.VALUES);
		handleStylePasteType.add(PasteType.VALUES_AND_NUMBER_FORMATS);
	}

	private CellBuffer[][] prepareCellBuffer(SheetRegion src, PasteOption option) {
		int row = src.getRow();
		int column = src.getColumn();
		int lastRow = src.getLastRow();
		int lastColumn = src.getLastColumn();
		
		boolean transpose = option.isTranspose();
		int srcRowCount = transpose?src.getColumnCount():src.getRowCount();
		int srcColCount = transpose?src.getRowCount():src.getColumnCount();
		final SSheet sheet = src.getSheet();
		
		CellBuffer[][] srcBuffer = new CellBuffer[srcRowCount][srcColCount];
		SSheet srcSheet = src.getSheet();
		for(int r = row; r <= lastRow;r++){
			for(int c = column; c <= lastColumn;c++){
				SCell srcCell = srcSheet.getCell(r,c);
				PasteType pt = option.getPasteType();

				boolean handleStyle = handleStylePasteType.contains(pt)
						&& (srcSheet.getRow(r).getCellStyle(true) != null
						|| srcSheet.getColumn(c).getCellStyle(true) != null)
						|| ((AbstractSheetAdv)sheet).getTableByRowCol(r, c) != null; //ZSS-1002
				
				if(srcCell.isNull() && !handleStyle) //to avoid unnecessary create
					continue;
				
				CellBuffer buffer = srcBuffer[transpose?c-column:r-row][transpose?r-row:c-column] = new CellBuffer();
				
				buffer.setType(srcCell.getType());
				
				switch(pt){
				case ALL:
				case ALL_EXCEPT_BORDERS:
					prepareValue(buffer,srcCell,true);
					buffer.setStyle(StyleUtil.prepareStyle(srcCell)); //ZSS-1002
					buffer.setHyperlink(srcCell.getHyperlink());
					buffer.setComment(srcCell.getComment());
					buffer.setValidation(srcSheet.getDataValidation(r, c));
					break;
				case COMMENTS:
					buffer.setComment(srcCell.getComment());
					break;
				case FORMATS:
					buffer.setStyle(srcCell.getCellStyle());
					break;
				case FORMULAS_AND_NUMBER_FORMATS:
					buffer.setStyle(srcCell.getCellStyle());
				case FORMULAS:
					prepareValue(buffer,srcCell,true);
					break;
				case VALIDATAION:
					//TODO what can we do to keep validation to paste, the structure is not directly assign to cell currently
					buffer.setValidation(srcSheet.getDataValidation(r, c));
				case VALUES_AND_NUMBER_FORMATS:
					buffer.setStyle(srcCell.getCellStyle());
				case VALUES:
					prepareValue(buffer,srcCell,false);
					break;
				case COLUMN_WIDTHS:
					break;
				}
			}
		}
		return srcBuffer;
	}


	private void prepareValue(CellBuffer buffer, SCell srcCell, boolean copyFormula) {
		if(copyFormula && srcCell.getType() == CellType.FORMULA){
			//ZSS-1002
			final String formula = srcCell.getFormulaValue();
			final ParsingBook parsingBook = new ParsingBook(srcCell.getSheet().getBook());
			final Ptg[] tokens = ((AbstractCellAdv)srcCell).getFormulaExpression().getPtgs();
			final String result = FormulaRenderer.toFormulaCopyText(parsingBook, tokens, formula);
			buffer.setFormula(result);
		}else{
			buffer.setValue(srcCell.getValue());
		}
	}
	
	private void clearMergeRegion(SheetRegion src) {
		src.getSheet().removeMergedRegion(src.getRegion(), true);
	}


	private void clearCell(SheetRegion src) {
		int row = src.getRow();
		int column = src.getColumn();
		int lastRow = src.getLastRow();
		int lastColumn = src.getLastColumn();
		SSheet srcSheet = src.getSheet();
		for(int r = row ; r<=lastRow;r++){
			for(int c= column; c<=lastColumn;c++){
				SCell cell = srcSheet.getCell(r,c);
				if(!cell.isNull()){
					srcSheet.clearCell(new CellRegion(r,c));
				}
			}
		}
	}


	private void pasteMergeRegion(Collection<CellRegion> mergeBuffer, int rowOffset,int columnOffset) {
		for(CellRegion merge:mergeBuffer){
			CellRegion newMerge = new CellRegion(merge.getRow()+rowOffset,merge.getColumn()+columnOffset,
					merge.getLastRow()+rowOffset,merge.getLastColumn()+columnOffset);
			
//			for(CellRegion overlap:destSheet.getOverlapsMergedRegions(newMerge,false)){
//				//unmerge the overlaps
//				destSheet.removeMergedRegion(overlap);
//			}
			_destSheet.removeMergedRegion(newMerge, true);
			
			_destSheet.addMergedRegion(newMerge);
		}
	}


	private void pasteColumnWidth(int[] widthBuffer, CellRegion dest) {
		int col = dest.getColumn();
		int lastColumn = dest.getLastColumn();
		for (int c = col; c <= lastColumn;c++){
			_destSheet.getColumn(c).setWidth(widthBuffer[c-col]);
		}
	}
	
	private void pasteCells(CellBuffer[][] srcBuffer, CellRegion destRegion,SheetRegion cutFrom,PasteOption option, int rowOffset,int columnOffset) {
		int row = destRegion.getRow();
		int col = destRegion.getColumn();
		int lastRow = destRegion.getLastRow();
		int lastColumn = destRegion.getLastColumn();
		boolean transpose = option.isTranspose();
		for(int r = row; r <= lastRow; r++){
			for (int c = col; c <= lastColumn;c++){
				CellBuffer buffer = srcBuffer[r-row][c-col];
				if((buffer==null || buffer.getType()==CellType.BLANK) && option.isSkipBlank()){
					continue;
				}
				
				//unmerge region if it is overlaps and not at first cell
				CellRegion region = _destSheet.getMergedRegion(r, c);
				if(region!=null && (region.getRow()!=r || region.getColumn()!=c)){
					_destSheet.removeMergedRegion(region, true);
				}
				
				SCell destCell = _destSheet.getCell(r,c);
				if(buffer==null){
					if(!destCell.isNull()){
						_destSheet.clearCell(destCell.getRowIndex(), destCell.getColumnIndex(), destCell.getRowIndex(), destCell.getColumnIndex());
						continue;
					}
					continue;
				}
				
				switch(option.getPasteType()){
				case ALL:
					pasteValue(buffer,destCell,cutFrom,true,rowOffset,columnOffset,transpose,row,col);
					pasteStyle(buffer,destCell,true);//border,comment
					buffer.applyHyperlink(destCell);
					buffer.applyComment(destCell);
					break;
				case ALL_EXCEPT_BORDERS:
					pasteValue(buffer,destCell,cutFrom,true,rowOffset,columnOffset,transpose,row,col);
					pasteStyle(buffer,destCell,false);//border,comment
					buffer.applyHyperlink(destCell);
					buffer.applyComment(destCell);
					break;
				case COMMENTS:
					buffer.applyComment(destCell);
					break;
				case FORMATS:
					//paste format should paste all style
					pasteStyle(buffer,destCell,true);
					break;
				case FORMULAS_AND_NUMBER_FORMATS:
					pasteFormat(buffer,destCell);
				case FORMULAS:
					pasteValue(buffer,destCell,cutFrom,true,rowOffset,columnOffset,transpose,row,col);
					break;
				case VALIDATAION:
				case VALUES_AND_NUMBER_FORMATS:
					pasteFormat(buffer,destCell);
				case VALUES:
					pasteValue(buffer,destCell,cutFrom);
					break;
				case COLUMN_WIDTHS:
					break;				
				}

			}
		}
	}

	private void pasteFormat(CellBuffer buffer, SCell destCell) {
		String srcFormat = buffer.getStyle().getDataFormat();
		SCellStyle destStyle = destCell.getCellStyle();
		String destFormat = destStyle.getDataFormat();
		if(!destFormat.equals(srcFormat)){
			StyleUtil.setDataFormat(_destSheet.getBook(), destCell, srcFormat);
		}
	}

	private void pasteStyle(CellBuffer buffer, SCell destCell, boolean pasteBorder) {
		if(destCell.getCellStyle()==_defaultStyle && buffer.getStyle()==_defaultStyle){
			return;
		}
		if(pasteBorder){
			destCell.setCellStyle(buffer.getStyle());
		}else{
			SCellStyle newStyle = _book.createCellStyle(buffer.getStyle(), true);
			SCellStyle destStyle = destCell.getCellStyle();
			//keep original border
			newStyle.setBorderBottom(destStyle.getBorderBottom());
			newStyle.setBorderBottomColor(destStyle.getBorderBottomColor());
			newStyle.setBorderTop(destStyle.getBorderTop());
			newStyle.setBorderTopColor(destStyle.getBorderTopColor());
			newStyle.setBorderRight(destStyle.getBorderRight());
			newStyle.setBorderRightColor(destStyle.getBorderRightColor());
			newStyle.setBorderLeft(destStyle.getBorderLeft());
			newStyle.setBorderLeftColor(destStyle.getBorderLeftColor());
			destCell.setCellStyle(newStyle);
		}
	}

	private void pasteValue(CellBuffer buffer, SCell destCell,SheetRegion cutFrom) {
		pasteValue(buffer,destCell,cutFrom,false,-1,-1,false,-1,-1);
	}
	//ZSS-693: promote to public so SortHelper can use this
	public void pasteValue(CellBuffer buffer, SCell destCell,SheetRegion cutFrom, boolean pasteFormula, int rowOffset,int columnOffset, boolean transpose, int rowOrigin, int columnOrigin) {
		if(pasteFormula){
			String formula = buffer.getFormula();
			if(formula!=null){
				FormulaEngine engine = getFormulaEngine();

				FormulaParseContext context = new FormulaParseContext(destCell, null); //nodependency, //ZSS-1002
				FormulaExpression expr; 
				FormulaExpression fexpr = engine.parse(formula, context);
				if(cutFrom!=null){
					expr = engine.movePtgs(fexpr,cutFrom,rowOffset, columnOffset, context);//no dependency
				}else{
					expr = engine.shiftPtgs(fexpr,rowOffset, columnOffset, context);//no dependency
				}
				if(!expr.hasError() && transpose){
					expr = engine.transposePtgs(expr, rowOrigin, columnOrigin, context);
				}
				
				if(!expr.hasError()){
					destCell.setValue(expr);
				}//ignore if get parsing error
				return;
			}
		}
		destCell.setValue(buffer.getValue());
	}
}
