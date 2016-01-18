///* StyleUtil.java
//
//{{IS_NOTE
//	Purpose:
//		
//	Description:
//		
//	History:
//		Jun 16, 2008 2:50:27 PM     2008, Created by Dennis.Chen
//}}IS_NOTE
//
//Copyright (C) 2007 Potix Corporation. All Rights Reserved.
//
//{{IS_RIGHT
//	This program is distributed under GPL Version 2.0 in the hope that
//	it will be useful, but WITHOUT ANY WARRANTY.
//}}IS_RIGHT
//*/
package org.zkoss.zss.ui.impl;
//
//import java.util.HashMap;
//
//import org.zkoss.poi.ss.usermodel.Cell;
//import org.zkoss.poi.ss.usermodel.CellStyle;
//import org.zkoss.poi.ss.usermodel.Color;
//import org.zkoss.poi.ss.usermodel.Font;
//import org.zkoss.poi.ss.usermodel.Row;
//import org.zkoss.poi.ss.usermodel.Sheet;
//import org.zkoss.poi.ss.usermodel.Workbook;
//import org.zkoss.util.logging.Log;
//import org.zkoss.zss.api.Ranges;
//import org.zkoss.zss.model.sys.XBook;
//import org.zkoss.zss.model.sys.XSheet;
//import org.zkoss.zss.model.sys.impl.BookHelper;
//import org.zkoss.zss.model.sys.impl.CellStyleMatcher;
///**
// * A utility class to help spreadsheet set style of a cell
// * @author Dennis.Chen
// *
// */
@Deprecated
public class Styles {
//	private static final Log log = Log.lookup(Styles.class);
//	
//	public static CellStyle cloneCellStyle(Cell cell) {
//		final CellStyle destination = cell.getSheet().getWorkbook().createCellStyle();
//		destination.cloneStyleFrom(Styles.getCellStyle(cell));
//		return destination;
//	}
//	
//	public static void setFontColor(XSheet sheet, int row, int col, String color,HashMap<Integer,CellStyle> cache){
//		final Cell cell = XUtils.getOrCreateCell(sheet,row,col);
//		final XBook book = (XBook) sheet.getWorkbook();
//		final CellStyle orgStyle = Styles.getCellStyle(cell);
//		final short fontIdx = orgStyle.getFontIndex();
//		final Font font = book.getFontAt(fontIdx);
//		final Color orgColor = BookHelper.getFontColor(book, font);
//		final Color newColor = BookHelper.HTMLToColor(book, color);
//		if (orgColor == newColor || orgColor != null && orgColor.equals(newColor)) {
//			return;
//		}
//		
//		CellStyle hitStyle = cache==null?null:cache.get((int)orgStyle.getIndex());
//		if(hitStyle!=null){
//			cell.setCellStyle(hitStyle);
//			return;
//		}
//		
//		final short boldWeight = font.getBoldweight();
//		final short fontHeight = font.getFontHeight();
//		final String name = font.getFontName();
//		final boolean italic = font.getItalic();
//		final boolean strikeout = font.getStrikeout();
//		final short typeOffset = font.getTypeOffset();
//		final byte underline = font.getUnderline();
//		final Object[] newFont = BookHelper.findOrCreateFont(book, boldWeight, newColor, fontHeight, name, italic, strikeout, typeOffset, underline);
//		
//		CellStyle style = null;
//		if(Boolean.FALSE.equals(newFont[1])){//search it since we have existed font
//			CellStyleMatcher matcher = new CellStyleMatcher(sheet.getBook(),Styles.getCellStyle(cell));
//			matcher.setFontIndex(((Font)newFont[0]).getIndex());
//			style = findStyle(sheet.getBook(), matcher);
//		}
//		
//		if(style==null){
//			style = cloneCellStyle(cell);
//			style.setFont(((Font)newFont[0]));
//		}
//		cell.setCellStyle(style);
//		
//		if(cache!=null){
//			cache.put((int)orgStyle.getIndex(), style);
//		}
//	}
//	
//	/**
//	 * 
//	 * @param cache the cache of <original style id, new style to replace>
//	 */
//	public static void setFillColor(XSheet sheet, int row, int col, String htmlColor,HashMap<Integer,CellStyle> cache){
//		final Cell cell = XUtils.getOrCreateCell(sheet,row,col);
//		final XBook book = (XBook) sheet.getWorkbook();
//		final CellStyle orgStyle = Styles.getCellStyle(cell);
//		final Color orgColor = orgStyle.getFillForegroundColorColor();
//		final Color newColor = BookHelper.HTMLToColor(book, htmlColor);
//		if (orgColor == newColor || orgColor != null  && orgColor.equals(newColor)) { //no change, skip
//			return;
//		}
//		
//		CellStyle hitStyle = cache==null?null:cache.get((int)orgStyle.getIndex());
//		if(hitStyle!=null){
//			cell.setCellStyle(hitStyle);
//			return;
//		}
//		
//		CellStyleMatcher matcher = new CellStyleMatcher(sheet.getBook(),Styles.getCellStyle(cell));
//		matcher.setFillForegroundColor(htmlColor);
//		matcher.setFillPattern(CellStyle.SOLID_FOREGROUND);
//		CellStyle style = findStyle(sheet.getBook(), matcher);
//		if(style==null){
//			style  = cloneCellStyle(cell);
//			style.setFillPattern(CellStyle.SOLID_FOREGROUND);
//			BookHelper.setFillForegroundColor(style, newColor);
//		}
//		cell.setCellStyle(style);
//		
//		if(cache!=null){
//			cache.put((int)orgStyle.getIndex(), style);
//		}
//		
//	}
//	
//	public static void setTextWrap(XSheet sheet,int row,int col,boolean wrap,HashMap<Integer,CellStyle> cache){
//		final Cell cell = XUtils.getOrCreateCell(sheet,row,col);
//		final CellStyle orgStyle = Styles.getCellStyle(cell);
//		final boolean textWrap = orgStyle.getWrapText();
//		if (wrap == textWrap) { //no change, skip
//			return;
//		}
//		CellStyle hitStyle = cache==null?null:cache.get((int)orgStyle.getIndex());
//		if(hitStyle!=null){
//			cell.setCellStyle(hitStyle);
//			return;
//		}
//		
//		CellStyleMatcher matcher = new CellStyleMatcher(sheet.getBook(),Styles.getCellStyle(cell));
//		matcher.setWrapText(wrap);
//		CellStyle style = findStyle(sheet.getBook(), matcher);
//		if(style==null){
//			style  = cloneCellStyle(cell);
//			style.setWrapText(wrap);
//		}
//		cell.setCellStyle(style);
//		
//		if(cache!=null){
//			cache.put((int)orgStyle.getIndex(), style);
//		}
//	}
//	
//	public static void setFontHeight(XSheet sheet,int row,int col,int fontHeight,HashMap<Integer,CellStyle> cache){
//		final Cell cell = XUtils.getOrCreateCell(sheet,row,col);
//		final XBook book = (XBook) sheet.getWorkbook();
//		final CellStyle orgStyle = Styles.getCellStyle(cell);
//		final short fontIdx = orgStyle.getFontIndex();
//		final Font font = book.getFontAt(fontIdx);
//		final short orgSize = font.getFontHeight();
//		if (orgSize == fontHeight) { //no change, skip
//			return;
//		}
//		
//		CellStyle hitStyle = cache==null?null:cache.get((int)orgStyle.getIndex());
//		if(hitStyle!=null){
//			cell.setCellStyle(hitStyle);
//			return;
//		}
//		
//		final short boldWeight = font.getBoldweight();
//		final Color color = BookHelper.getFontColor(book, font);
//		final String name = font.getFontName();
//		final boolean italic = font.getItalic();
//		final boolean strikeout = font.getStrikeout();
//		final short typeOffset = font.getTypeOffset();
//		final byte underline = font.getUnderline();
//		final Object[] newFont = BookHelper.findOrCreateFont(book, boldWeight, color, (short)fontHeight, name, italic, strikeout, typeOffset, underline);
//		
//		CellStyle style = null;
//		if(Boolean.FALSE.equals(newFont[1])){//search it since we have existed font
//			CellStyleMatcher matcher = new CellStyleMatcher(sheet.getBook(),Styles.getCellStyle(cell));
//			matcher.setFontIndex(((Font)newFont[0]).getIndex());
//			style = findStyle(sheet.getBook(), matcher);
//		}
//		
//		if(style==null){
//			style = cloneCellStyle(cell);
//			style.setFont(((Font)newFont[0]));
//		}
//		cell.setCellStyle(style);
//		
//		if(cache!=null){
//			cache.put((int)orgStyle.getIndex(), style);
//		}
//	}
//	
//	public static void setFontStrikethrough(XSheet sheet,int row,int col, boolean strikeout,HashMap<Integer,CellStyle> cache){
//		final Cell cell = XUtils.getOrCreateCell(sheet,row,col);
//		final XBook book = (XBook) sheet.getWorkbook();
//		final CellStyle orgStyle = Styles.getCellStyle(cell);
//		final short fontIdx = orgStyle.getFontIndex();
//		final Font font = book.getFontAt(fontIdx);
//		final boolean orgStrikeout = font.getStrikeout();
//		if (orgStrikeout == strikeout) { //no change, skip
//			return;
//		}
//		CellStyle hitStyle = cache==null?null:cache.get((int)orgStyle.getIndex());
//		if(hitStyle!=null){
//			cell.setCellStyle(hitStyle);
//			return;
//		}
//		final short boldWeight = font.getBoldweight();
//		final Color color = BookHelper.getFontColor(book, font);
//		final short fontHeight = font.getFontHeight();
//		final String name = font.getFontName();
//		final boolean italic = font.getItalic();
//		final short typeOffset = font.getTypeOffset();
//		final byte underline = font.getUnderline();
//		final Object[] newFont = BookHelper.findOrCreateFont(book, boldWeight, color, fontHeight, name, italic, strikeout, typeOffset, underline);
//		
//		CellStyle style = null;
//		if(Boolean.FALSE.equals(newFont[1])){//search it since we have existed font
//			CellStyleMatcher matcher = new CellStyleMatcher(sheet.getBook(),Styles.getCellStyle(cell));
//			matcher.setFontIndex(((Font)newFont[0]).getIndex());
//			style = findStyle(sheet.getBook(), matcher);
//		}
//		
//		if(style==null){
//			style = cloneCellStyle(cell);
//			style.setFont(((Font)newFont[0]));
//		}
//		cell.setCellStyle(style);
//		
//		if(cache!=null){
//			cache.put((int)orgStyle.getIndex(), style);
//		}
//	}
//	
//	public static void setFontName(XSheet sheet,int row,int col,String name,HashMap<Integer,CellStyle> cache){
//		final Cell cell = XUtils.getOrCreateCell(sheet,row,col);
//		final XBook book = (XBook) sheet.getWorkbook();
//		final CellStyle orgStyle = Styles.getCellStyle(cell);
//		final short fontIdx = orgStyle.getFontIndex();
//		final Font font = book.getFontAt(fontIdx);
//		final String orgName = font.getFontName();
//		if (orgName.equals(name)) { //no change, skip
//			return;
//		}
//		CellStyle hitStyle = cache==null?null:cache.get((int)orgStyle.getIndex());
//		if(hitStyle!=null){
//			cell.setCellStyle(hitStyle);
//			return;
//		}
//		
//		final short boldWeight = font.getBoldweight();
//		final Color color = BookHelper.getFontColor(book, font);
//		final short fontHeight = font.getFontHeight();
//		final boolean italic = font.getItalic();
//		final boolean strikeout = font.getStrikeout();
//		final short typeOffset = font.getTypeOffset();
//		final byte underline = font.getUnderline();
//		final Object[] newFont = BookHelper.findOrCreateFont(book, boldWeight, color, fontHeight, name, italic, strikeout, typeOffset, underline);
//
//		CellStyle style = null;
//		if(Boolean.FALSE.equals(newFont[1])){//search it since we have existed font
//			CellStyleMatcher matcher = new CellStyleMatcher(sheet.getBook(),Styles.getCellStyle(cell));
//			matcher.setFontIndex(((Font)newFont[0]).getIndex());
//			style = findStyle(sheet.getBook(), matcher);
//		}
//		
//		if(style==null){
//			style = cloneCellStyle(cell);
//			style.setFont(((Font)newFont[0]));
//		}
//		cell.setCellStyle(style);
//		
//		if(cache!=null){
//			cache.put((int)orgStyle.getIndex(), style);
//		}
//	}
//	
//	public static void setBorder(XSheet sheet,int row,int col, String color, short linestyle){
//		setBorder(sheet,row,col, BookHelper.HTMLToColor(sheet.getWorkbook(), color), linestyle, 0xF);
//	}
//	public static void setBorderTop(XSheet sheet,int row,int col,String color, short linestyle){
//		setBorder(sheet,row,col, BookHelper.HTMLToColor(sheet.getWorkbook(), color), linestyle, 0x4);
//	}
//	public static void setBorderLeft(XSheet sheet,int row,int col,String color, short linestyle){
//		setBorder(sheet,row,col, BookHelper.HTMLToColor(sheet.getWorkbook(), color), linestyle, 0x8);
//	}
//	public static void setBorderBottom(XSheet sheet,int row,int col,String color, short linestyle){
//		setBorder(sheet,row,col, BookHelper.HTMLToColor(sheet.getWorkbook(), color), linestyle, 0x1);
//	}
//	public static void setBorderRight(XSheet sheet,int row,int col,String color, short linestyle){
//		setBorder(sheet,row,col, BookHelper.HTMLToColor(sheet.getWorkbook(), color), linestyle, 0x2);
//	}
//	
//	@Deprecated
//	public static void setBorder(XSheet sheet,int row,int col, short color, short lineStyle, int at){
//		final Cell cell = XUtils.getOrCreateCell(sheet,row,col);
//		final CellStyle style = cloneCellStyle(cell);
//		if((at & BookHelper.BORDER_EDGE_LEFT)!=0) {
//			style.setBorderLeft(lineStyle);
//		}
//		if((at & BookHelper.BORDER_EDGE_TOP)!=0){
//			style.setTopBorderColor(color);
//			style.setBorderTop(lineStyle);
//		}
//		if((at & BookHelper.BORDER_EDGE_RIGHT)!=0){
//			style.setRightBorderColor(color);
//			style.setBorderRight(lineStyle);
//		}
//		if((at & BookHelper.BORDER_EDGE_BOTTOM)!=0){
//			style.setBottomBorderColor(color);
//			style.setBorderBottom(lineStyle);
//		}
//		cell.setCellStyle(style);
//	}
//	
//	public static void setBorder(XSheet sheet,int row,int col, Color color, short lineStyle, int at){
//		final Cell cell = XUtils.getOrCreateCell(sheet,row,col);
//		final XBook book = sheet.getBook();
//		//ZSS-464 try to search existed matched style
//		String colorHtml = BookHelper.colorToBorderHTML(book, color);
//		CellStyle style = null;
//		boolean hasBorder = lineStyle != CellStyle.BORDER_NONE;
//		if(colorHtml!=null){
//			final CellStyle oldstyle = Styles.getCellStyle(cell);
//			CellStyleMatcher matcher = new CellStyleMatcher(sheet.getBook(),oldstyle);
//			if((at & BookHelper.BORDER_EDGE_LEFT)!=0) {
//				if(hasBorder)
//					matcher.setLeftBorderColor(colorHtml);
//				else
//					matcher.removeLeftBorderColor();
//				
//				matcher.setBorderLeft(lineStyle);
//			}
//			if((at & BookHelper.BORDER_EDGE_TOP)!=0){
//				if(hasBorder) 
//					matcher.setTopBorderColor(colorHtml);
//				else
//					matcher.removeTopBorderColor();
//				
//				matcher.setBorderTop(lineStyle);
//			}
//			if((at & BookHelper.BORDER_EDGE_RIGHT)!=0){
//				if(hasBorder)
//					matcher.setRightBorderColor(colorHtml);
//				else
//					matcher.removeRightBorderColor();
//				
//				matcher.setBorderRight(lineStyle);
//			}
//			if((at & BookHelper.BORDER_EDGE_BOTTOM)!=0){
//				if(hasBorder)
//					matcher.setBottomBorderColor(colorHtml);
//				else
//					matcher.removeBottomBorderColor();
//				
//				matcher.setBorderBottom(lineStyle);
//			}
//			style = findStyle(book, matcher);
//		}
//		
//		if(style==null){
//			style = cloneCellStyle(cell);
//			if((at & BookHelper.BORDER_EDGE_LEFT)!=0) {
//				if(hasBorder)
//					BookHelper.setLeftBorderColor(style, color);
//				style.setBorderLeft(lineStyle);
//			}
//			if((at & BookHelper.BORDER_EDGE_TOP)!=0){
//				if(hasBorder)
//					BookHelper.setTopBorderColor(style, color);
//				style.setBorderTop(lineStyle);
//			}
//			if((at & BookHelper.BORDER_EDGE_RIGHT)!=0){
//				if(hasBorder)
//					BookHelper.setRightBorderColor(style, color);
//				style.setBorderRight(lineStyle);
//			}
//			if((at & BookHelper.BORDER_EDGE_BOTTOM)!=0){
//				if(hasBorder)
//					BookHelper.setBottomBorderColor(style, color);
//				style.setBorderBottom(lineStyle);
//			}
//		}
//		
//		cell.setCellStyle(style);
//	}
//	
//	private static void debugStyle(String msg,int row, int col, Workbook book, CellStyle style){
//		StringBuilder sb = new StringBuilder(msg);
//		sb.append("[").append(Ranges.getCellRefString(row, col)).append("]");
//		sb.append("Top:[").append(style.getBorderTop()).append(":").append(BookHelper.colorToBorderHTML(book,style.getTopBorderColorColor())).append("]");
//		sb.append("Left:[").append(style.getBorderLeft()).append(":").append(BookHelper.colorToBorderHTML(book,style.getLeftBorderColorColor())).append("]");
//		sb.append("Bottom:[").append(style.getBorderBottom()).append(":").append(BookHelper.colorToBorderHTML(book,style.getBottomBorderColorColor())).append("]");
//		sb.append("Right:[").append(style.getBorderRight()).append(":").append(BookHelper.colorToBorderHTML(book,style.getRightBorderColorColor())).append("]");
//		System.out.println(">>"+sb.toString());
//	}
//	
//	
//	public static CellStyle findStyle(Workbook book,CellStyleMatcher matcher){
//    	short size = book.getNumCellStyles();
//    	for(short i=0; i<size;i++){
//    		CellStyle style = book.getCellStyleAt(i);
//    		if(matcher.match(book,style)){
//    			return style;
//    		}
//    	}
//    	return null;
//    }
//	
//	public static void setFontBoldWeight(XSheet sheet,int row,int col,short boldWeight,HashMap<Integer,CellStyle> cache){
//		final Cell cell = XUtils.getOrCreateCell(sheet,row,col);
//		final XBook book = (XBook) sheet.getWorkbook();
//		final CellStyle orgStyle = Styles.getCellStyle(cell);
//		final short fontIdx = orgStyle.getFontIndex();
//		final Font font = book.getFontAt(fontIdx);
//		final short orgBoldWeight = font.getBoldweight();
//		if (orgBoldWeight == boldWeight) { //no change, skip
//			return;
//		}
//		CellStyle hitStyle = cache==null?null:cache.get((int)orgStyle.getIndex());
//		if(hitStyle!=null){
//			cell.setCellStyle(hitStyle);
//			return;
//		}
//		final Color color = BookHelper.getFontColor(book, font);
//		final short fontHeight = font.getFontHeight();
//		final String name = font.getFontName();
//		final boolean italic = font.getItalic();
//		final boolean strikeout = font.getStrikeout();
//		final short typeOffset = font.getTypeOffset();
//		final byte underline = font.getUnderline();
//		final Object[] newFont = BookHelper.findOrCreateFont(book, boldWeight, color, fontHeight, name, italic, strikeout, typeOffset, underline);
//		
//		CellStyle style = null;
//		if(Boolean.FALSE.equals(newFont[1])){//search it since we have existed font
//			CellStyleMatcher matcher = new CellStyleMatcher(sheet.getBook(),Styles.getCellStyle(cell));
//			matcher.setFontIndex(((Font)newFont[0]).getIndex());
//			style = findStyle(sheet.getBook(), matcher);
//		}
//		
//		if(style==null){
//			style = cloneCellStyle(cell);
//			style.setFont(((Font)newFont[0]));
//		}
//		cell.setCellStyle(style);
//		if(cache!=null){
//			cache.put((int)orgStyle.getIndex(), style);
//		}
//	}
//	
//	public static void setFontItalic(XSheet sheet, int row, int col, boolean italic,HashMap<Integer,CellStyle> cache) {
//		final Cell cell = XUtils.getOrCreateCell(sheet,row,col);
//		final XBook book = (XBook) sheet.getWorkbook();
//		final CellStyle orgStyle = Styles.getCellStyle(cell);
//		final short fontIdx = orgStyle.getFontIndex();
//		final Font font = book.getFontAt(fontIdx);
//		final boolean orgItalic = font.getItalic();
//		if (orgItalic == italic) { //no change, skip
//			return;
//		}
//		CellStyle hitStyle = cache==null?null:cache.get((int)orgStyle.getIndex());
//		if(hitStyle!=null){
//			cell.setCellStyle(hitStyle);
//			return;
//		}
//		final short boldWeight = font.getBoldweight();
//		final Color color = BookHelper.getFontColor(book, font);
//		final short fontHeight = font.getFontHeight();
//		final String name = font.getFontName();
//		final boolean strikeout = font.getStrikeout();
//		final short typeOffset = font.getTypeOffset();
//		final byte underline = font.getUnderline();
//		final Object[] newFont = BookHelper.findOrCreateFont(book, boldWeight, color, fontHeight, name, italic, strikeout, typeOffset, underline);
//		
//		CellStyle style = null;
//		if(Boolean.FALSE.equals(newFont[1])){//search it since we have existed font
//			CellStyleMatcher matcher = new CellStyleMatcher(sheet.getBook(),Styles.getCellStyle(cell));
//			matcher.setFontIndex(((Font)newFont[0]).getIndex());
//			style = findStyle(sheet.getBook(), matcher);
//		}
//		
//		if(style==null){
//			style = cloneCellStyle(cell);
//			style.setFont(((Font)newFont[0]));
//		}
//		cell.setCellStyle(style);
//		
//		if(cache!=null){
//			cache.put((int)orgStyle.getIndex(), style);
//		}
//	}
//	
//	public static void setFontUnderline(XSheet sheet,int row,int col, byte underline,HashMap<Integer,CellStyle> cache){
//		final Cell cell = XUtils.getOrCreateCell(sheet,row,col);
//		final XBook book = (XBook) sheet.getWorkbook();
//		final CellStyle orgStyle = Styles.getCellStyle(cell);
//		final short fontIdx = orgStyle.getFontIndex();
//		final Font font = book.getFontAt(fontIdx);
//		final byte orgUnderline = font.getUnderline();
//		if (orgUnderline == underline) { //no change, skip
//			return;
//		}
//		CellStyle hitStyle = cache==null?null:cache.get((int)orgStyle.getIndex());
//		if(hitStyle!=null){
//			cell.setCellStyle(hitStyle);
//			return;
//		}
//		final short boldWeight = font.getBoldweight();
//		final Color color = BookHelper.getFontColor(book, font);
//		final short fontHeight = font.getFontHeight();
//		final String name = font.getFontName();
//		final boolean italic = font.getItalic();
//		final boolean strikeout = font.getStrikeout();
//		final short typeOffset = font.getTypeOffset();
//		final Object[] newFont = BookHelper.findOrCreateFont(book, boldWeight, color, fontHeight, name, italic, strikeout, typeOffset, underline);
//		
//		CellStyle style = null;
//		if(Boolean.FALSE.equals(newFont[1])){//search it since we have existed font
//			CellStyleMatcher matcher = new CellStyleMatcher(sheet.getBook(),Styles.getCellStyle(cell));
//			matcher.setFontIndex(((Font)newFont[0]).getIndex());
//			style = findStyle(sheet.getBook(), matcher);
//		}
//		
//		if(style==null){
//			style = cloneCellStyle(cell);
//			style.setFont(((Font)newFont[0]));
//		}
//		cell.setCellStyle(style);
//		if(cache!=null){
//			cache.put((int)orgStyle.getIndex(), style);
//		}
//	}
//	
//	public static void setTextHAlign(XSheet sheet,int row,int col, short align,HashMap<Integer,CellStyle> cache){
//		final Cell cell = XUtils.getOrCreateCell(sheet,row,col);
//		final CellStyle orgStyle = Styles.getCellStyle(cell);
//		final short orgAlign = orgStyle.getAlignment();
//		if (align == orgAlign) { //no change, skip
//			return;
//		}
//		CellStyle hitStyle = cache==null?null:cache.get((int)orgStyle.getIndex());
//		if(hitStyle!=null){
//			cell.setCellStyle(hitStyle);
//			return;
//		}
//		CellStyleMatcher matcher = new CellStyleMatcher(sheet.getBook(),Styles.getCellStyle(cell));
//		matcher.setAlignment(align);
//		CellStyle style = findStyle(sheet.getBook(), matcher);
//		if(style==null){
//			style = cloneCellStyle(cell);
//			style.setAlignment(align);
//		}
//		cell.setCellStyle(style);
//		if(cache!=null){
//			cache.put((int)orgStyle.getIndex(), style);
//		}
//	}
//	
//	public static void setTextVAlign(XSheet sheet,int row,int col, short valign,HashMap<Integer,CellStyle> cache){
//		final Cell cell = XUtils.getOrCreateCell(sheet,row,col);
//		final CellStyle orgStyle = Styles.getCellStyle(cell);
//		final short orgValign = orgStyle.getVerticalAlignment();
//		if (valign == orgValign) { //no change, skip
//			return;
//		}
//		CellStyle hitStyle = cache==null?null:cache.get((int)orgStyle.getIndex());
//		if(hitStyle!=null){
//			cell.setCellStyle(hitStyle);
//			return;
//		}
//		CellStyleMatcher matcher = new CellStyleMatcher(sheet.getBook(),Styles.getCellStyle(cell));
//		matcher.setVerticalAlignment(valign);
//		CellStyle style = findStyle(sheet.getBook(), matcher);
//		if(style==null){
//			style = cloneCellStyle(cell);
//			style.setVerticalAlignment(valign);
//		}
//		cell.setCellStyle(style);
//		if(cache!=null){
//			cache.put((int)orgStyle.getIndex(), style);
//		}
//	}
//
//	public static void setDataFormat(XSheet sheet, int row, int col, String format,HashMap<Integer,CellStyle> cache) {
//		final Cell cell = XUtils.getOrCreateCell(sheet,row,col);
//		final CellStyle orgStyle = Styles.getCellStyle(cell);
//		final String orgFormat = orgStyle.getDataFormatString();
//		if (format == orgFormat || (format!=null && format.equals(orgFormat))) { //no change, skip
//			return;
//		}
//		CellStyle hitStyle = cache==null?null:cache.get((int)orgStyle.getIndex());
//		if(hitStyle!=null){
//			cell.setCellStyle(hitStyle);
//			return;
//		}
//		
//		// ZSS-510, when formatStr is null or empty, it should be assigned as "General" format.
//		short idx = sheet.getBook().createDataFormat().getFormat(format == null || format.equals("") ? "General" : format);
//		
//		CellStyleMatcher matcher = new CellStyleMatcher(sheet.getBook(),Styles.getCellStyle(cell));
//		matcher.setDataFormat(idx);
//		CellStyle style = findStyle(sheet.getBook(), matcher);
//		if(style==null){
//			style = cloneCellStyle(cell);
//			style.setDataFormat(idx);
//		}
//		cell.setCellStyle(style);
//		if(cache!=null){
//			cache.put((int)orgStyle.getIndex(), style);
//		}
//		
//	}
//	
//	public static boolean isDefaultStyle(Sheet sheet, CellStyle style){
//		
////		CellStyle defaultStyle = sheet.getBook().getCellStyleAt((short)0);
////		return defaultStyle.equals(style);
//		
//		//in xssf, it use toString.equals() which is performance is bad
//		//Instated, I just check it's index is 0
//		return style!=null && style.getIndex() == 0;
//	}
//	
//	public static CellStyle getCellStyle(Cell cell){
//		XSheet sheet = (XSheet)cell.getSheet();
//		//current poi implementation always return default style if the cell doesn't has style
//		CellStyle cellStyle = cell.getCellStyle();
//		
//		if(cellStyle==null || isDefaultStyle(sheet,cellStyle)){
//			cellStyle = cell.getRow().getRowStyle();
//		}
//		if(cellStyle==null || isDefaultStyle(sheet,cellStyle)){
//			cellStyle = sheet.getColumnStyle(cell.getColumnIndex());
//		}
//		return cellStyle!=null?cellStyle: sheet.getBook().getCellStyleAt((short)0);
//	}
//	public static CellStyle getCellStyle(Sheet sheet, int row, int column){
//		Row rowObj = sheet.getRow(row);
//		CellStyle cellStyle = null;
//		if(rowObj!=null){
//			Cell cell = rowObj.getCell(column);
//			if(cell!=null){
//				cellStyle = cell.getCellStyle(); 
//			}
//			if(cellStyle==null || isDefaultStyle(sheet,cellStyle)){
//				cellStyle = rowObj.getRowStyle();
//			}
//		}
//		if(cellStyle==null || isDefaultStyle(sheet,cellStyle)){
//			cellStyle = sheet.getColumnStyle(column);
//		}
//		
//		return cellStyle!=null?cellStyle: sheet.getWorkbook().getCellStyleAt((short)0);
//	}
//
//	public static CellStyle getDefaultCellStyle(Sheet sheet) {
//		return sheet.getWorkbook().getCellStyleAt((short)0);
//	}
}
