/* WholeStyleUtil.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jun 16, 2008 2:50:27 PM     2008, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.range.impl;

import java.util.HashSet;
import java.util.Iterator;

import org.zkoss.zss.model.InvalidModelOpException;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.CellStyleHolder;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SColumn;
import org.zkoss.zss.model.SFill;
import org.zkoss.zss.model.SRow;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.range.SRange;

/**
 * A utility class to help spreadsheet set style of a row, column and cell
 * @author Dennis.Chen
 * @since 3.5.0
 */
public class WholeStyleUtil {

	public interface StyleApplyer {
		public void applyStyle(CellStyleHolder holder);
	};
	
	public static void setWholeStyle(SRange range, StyleApplyer applyer){
		if(range.isWholeSheet()){
			//1. it is not possible to set style to all row or columns
			//2. we don't have the way to replace defualt style in component.
			//caller should consider to set cell style to whole column (style on column) and give column from 0 to 1000, because of 
			//in general case we have much less columns then rows. 
			 throw new InvalidModelOpException("don't allow to set style to whole sheet");
		}else if(range.isWholeRow()) {
			setWholeRowCellStyle(range,applyer);
		}else if(range.isWholeColumn()){
			setWholeColumnCellStyle(range,applyer);
		}else{
			for(int r = range.getRow(); r <= range.getLastRow(); r++){
				for (int c = range.getColumn(); c <= range.getLastColumn(); c++){
					SCell cell = range.getSheet().getCell(r,c);
					applyer.applyStyle(cell);
				}
			}
		}
	}
	
	public static void setWholeRowCellStyle(SRange range, StyleApplyer applyer){
		SSheet sheet = range.getSheet();
		for(int r = range.getRow(); r <= range.getLastRow(); r++){
			SRow row = sheet.getRow(r);
			applyer.applyStyle(row);
			
			HashSet<Integer> cellProcessed = new HashSet<Integer>();
			
			Iterator<SCell> cells = sheet.getCellIterator(r);
			while(cells.hasNext()){
				SCell cell = cells.next();
				//the case the cell or column has local style
				if(cell.getCellStyle(true)!=null ||
						sheet.getColumn(cell.getColumnIndex()).getCellStyle(true)!=null){
					applyer.applyStyle(cell);
				}
				cellProcessed.add(cell.getColumnIndex());
			}
			
			//has to force set the style on the row/column across cell to avoid row/column style conflict on null cell
			Iterator<SColumn> columns = sheet.getColumnIterator();
			while(columns.hasNext()){
				SColumn column = columns.next();
				if(cellProcessed.contains(column.getIndex())){
					continue;
				}
				if(column.getCellStyle(true)!=null){
					applyer.applyStyle(sheet.getCell(r, column.getIndex()));
				}
			}
		}
	}
	
	public static void setWholeColumnCellStyle(SRange range, StyleApplyer applyer){
		SSheet sheet = range.getSheet();
		for (int c = range.getColumn(); c <= range.getLastColumn(); c++){
			SColumn column = sheet.getColumn(c);
			applyer.applyStyle(column);
		}
		Iterator<SRow> rows = sheet.getRowIterator();
		while(rows.hasNext()){
			SRow row = rows.next();
			for (int c = range.getColumn(); c <= range.getLastColumn(); c++){
				SCell cell = sheet.getCell(row.getIndex(),c);
				//the case the cell or column has local style
				if(cell.getCellStyle(true)!=null || row.getCellStyle(true)!=null){
					applyer.applyStyle(cell);
				}
			}
		}
	}

	public static void setFillColor(final SRange wholeRange, final String htmlColor) {
		setWholeStyle(wholeRange,new StyleApplyer(){
			@Override
			public void applyStyle(CellStyleHolder holder) {
				StyleUtil.setFillColor(wholeRange.getSheet().getBook(), holder, htmlColor);
			}});
	}
	
	public static void setFillOptions(final SRange wholeRange, final String backColor, final String fillColor, final SFill.FillPattern pattern) {
		setWholeStyle(wholeRange,new StyleApplyer(){
			@Override
			public void applyStyle(CellStyleHolder holder) {
				StyleUtil.setFillOptions(wholeRange.getSheet().getBook(), holder, backColor, fillColor, pattern);
			}});
	}

	//ZSS-857
	public static void setBackColor(final SRange wholeRange, final String htmlColor) {
		setWholeStyle(wholeRange,new StyleApplyer(){
			@Override
			public void applyStyle(CellStyleHolder holder) {
				StyleUtil.setBackColor(wholeRange.getSheet().getBook(), holder, htmlColor);
			}});
	}

	public static void setTextHAlign(final SRange wholeRange,
			final org.zkoss.zss.model.SCellStyle.Alignment hAlignment) {
		setWholeStyle(wholeRange,new StyleApplyer(){
			@Override
			public void applyStyle(CellStyleHolder holder) {
				StyleUtil.setTextHAlign(wholeRange.getSheet().getBook(), holder, hAlignment);
			}});
	}

	public static void setTextVAlign(final SRange wholeRange, 
			final org.zkoss.zss.model.SCellStyle.VerticalAlignment vAlignment) {
		setWholeStyle(wholeRange,new StyleApplyer(){
			@Override
			public void applyStyle(CellStyleHolder holder) {
				StyleUtil.setTextVAlign(wholeRange.getSheet().getBook(), holder, vAlignment);
			}});
	}

	public static void setTextWrap(final SRange wholeRange, final boolean wraptext) {
		setWholeStyle(wholeRange,new StyleApplyer(){
			@Override
			public void applyStyle(CellStyleHolder holder) {
				StyleUtil.setTextWrap(wholeRange.getSheet().getBook(), holder, wraptext);
			}});
	}

	public static void setDataFormat(final SRange wholeRange, final String format) {
		setWholeStyle(wholeRange,new StyleApplyer(){
			@Override
			public void applyStyle(CellStyleHolder holder) {
				StyleUtil.setDataFormat(wholeRange.getSheet().getBook(), holder, format);
			}});
	}

	public static void setFontName(final SRange wholeRange,final String fontName) {
		setWholeStyle(wholeRange,new StyleApplyer(){
			@Override
			public void applyStyle(CellStyleHolder holder) {
				StyleUtil.setFontName(wholeRange.getSheet().getBook(), holder, fontName);
			}});
	}

	public static void setFontHeightPoints(final SRange wholeRange,
			final int fontHeightPoints) {
		setWholeStyle(wholeRange,new StyleApplyer(){
			@Override
			public void applyStyle(CellStyleHolder holder) {
				StyleUtil.setFontHeightPoints(wholeRange.getSheet().getBook(), holder, fontHeightPoints);
			}});
	}

	public static void setFontBoldWeight(final SRange wholeRange,
			final org.zkoss.zss.model.SFont.Boldweight fontBoldweight) {
		setWholeStyle(wholeRange,new StyleApplyer(){
			@Override
			public void applyStyle(CellStyleHolder holder) {
				StyleUtil.setFontBoldWeight(wholeRange.getSheet().getBook(), holder, fontBoldweight);
			}});
	}

	public static void setFontItalic(final SRange wholeRange,final boolean italic) {
		setWholeStyle(wholeRange,new StyleApplyer(){
			@Override
			public void applyStyle(CellStyleHolder holder) {
				StyleUtil.setFontItalic(wholeRange.getSheet().getBook(), holder, italic);
			}});
	}

	public static void setFontStrikethrough(final SRange wholeRange,
			final boolean strikeout) {
		setWholeStyle(wholeRange,new StyleApplyer(){
			@Override
			public void applyStyle(CellStyleHolder holder) {
				StyleUtil.setFontStrikethrough(wholeRange.getSheet().getBook(), holder, strikeout);
			}});
	}

	public static void setFontUnderline(final SRange wholeRange,
			final org.zkoss.zss.model.SFont.Underline fontUnderline) {
		setWholeStyle(wholeRange,new StyleApplyer(){
			@Override
			public void applyStyle(CellStyleHolder holder) {
				StyleUtil.setFontUnderline(wholeRange.getSheet().getBook(), holder, fontUnderline);
			}});
	}

	public static void setFontColor(final SRange wholeRange, final String htmlColor) {
		setWholeStyle(wholeRange,new StyleApplyer(){
			@Override
			public void applyStyle(CellStyleHolder holder) {
				StyleUtil.setFontColor(wholeRange.getSheet().getBook(), holder, htmlColor);
			}});
	}	

	//ZSS-748
	public static void setFontTypeOffset(final SRange wholeRange,
			final org.zkoss.zss.model.SFont.TypeOffset fontTypeOffset) {
		setWholeStyle(wholeRange,new StyleApplyer(){
			@Override
			public void applyStyle(CellStyleHolder holder) {
				StyleUtil.setFontTypeOffset(wholeRange.getSheet().getBook(), holder, fontTypeOffset);
			}});
	}

	//ZSS-918
	public static void setTextRotation(final SRange wholeRange,
			final int rotation) {
		setWholeStyle(wholeRange,new StyleApplyer(){
			@Override
			public void applyStyle(CellStyleHolder holder) {
				StyleUtil.setTextRotation(wholeRange.getSheet().getBook(), holder, rotation);
			}});
	}
	
	// ZSS-915
	public static void setTextIndentionOffset(final SRange wholeRange,
			final int offset) {
		setWholeStyle(wholeRange, new StyleApplyer() {
			@Override
			public void applyStyle(CellStyleHolder holder) {
				StyleUtil.setTextIndentionOffset(wholeRange.getSheet().getBook(), holder, offset);
			}
		});
	}
	
	// ZSS-915
	public static void setTextIndention(final SRange wholeRange,
			final int indention) {
		setWholeStyle(wholeRange, new StyleApplyer() {
			@Override
			public void applyStyle(CellStyleHolder holder) {
				StyleUtil.setTextIndention(wholeRange.getSheet().getBook(), holder, indention);
			}
		});
	}
}
