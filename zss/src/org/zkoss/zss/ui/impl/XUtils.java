///* XUtils.java
//
//{{IS_NOTE
//	Purpose:
//		
//	Description:
//		
//	History:
//		Mar 20, 2008 12:40:01 PM     2008, Created by Dennis.Chen
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
//import java.awt.font.FontRenderContext;
//import java.awt.font.TextAttribute;
//import java.awt.font.TextLayout;
//import java.text.AttributedString;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import org.zkoss.poi.ss.usermodel.Cell;
//import org.zkoss.poi.ss.usermodel.ClientAnchor;
//import org.zkoss.poi.ss.usermodel.Hyperlink;
//import org.zkoss.poi.ss.usermodel.RichTextString;
//import org.zkoss.poi.ss.usermodel.Row;
//import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.util.logging.Log;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.api.model.impl.SheetImpl;
import org.zkoss.zss.ui.Spreadsheet;
//
///**
// * Internal Use Only. Utility class for {@link Spreadsheet}.
// * @author Dennis.Chen
// *
// */
@Deprecated
public class XUtils {
//	private static final Log log = Log.lookup(XUtils.class);
//	
//	/**
//	 * Gets Cell text by given row and column
//	 */
//	static public String getCellHtmlText(XSheet sheet, int row,int column){
//		final Cell cell = XUtils.getCell(sheet, row, column);
//		String text = "";
//		if (cell != null) {
//			boolean wrap = cell.getCellStyle().getWrapText();
//			final XFormatText ft = XUtils.getFormatText(cell);
//			if (ft != null) {
//				if (ft.isRichTextString()) {
//					final RichTextString rstr = ft.getRichTextString();
//					text = rstr == null ? "" : rstr.getString();
//				} else if (ft.isCellFormatResult()) {
//					text = ft.getCellFormatResult().text;
//				}
//				text = XUtils.escapeCellText(text, wrap, true);
//			}
//		}
//		return text;
//	}
//	
//	static public String getEditText(XSheet sheet, int row,int column){
//		final Cell cell = XUtils.getCell(sheet, row, column);
//		return cell != null ? XUtils.getEditText(cell) : "";
//	}
//	
//	static public String getCellFormatText(XSheet sheet, int row,int column) {
//		final Cell cell = XUtils.getCell(sheet, row, column);
//		String text = "";
//		if (cell != null) {
//			final XFormatText ft = XUtils.getFormatText(cell);
//			if (ft != null) {
//				if (ft.isRichTextString()) {
//					final RichTextString rstr = ft.getRichTextString();
//					text = rstr == null ? "" : rstr.toString();
//				} else if (ft.isCellFormatResult()) {
//					text = ft.getCellFormatResult().text;
//				}
//			}
//		}
//		return text;
//	}
//	
//	/**
//	 * Wrap cell text with &lt;a> according to {@link Hyperlink}
//	 * @param sheet the sheet with the RichTextString 
//	 * @param hlink the Hyperlink, its address might be null
//	 * @return HTML &lt;a> with specified hyperlink or originally passed-in cell text
//	 */
//	public static String formatHyperlink(XSheet sheet, Hyperlink hlink, String cellText, boolean wrap) {
//		if (hlink == null) {
//			return cellText;
//		}
//		//ZSS-453
//		final String hyperlinkAddress = hlink.getAddress() == null?"":hlink.getAddress();
//		final String escapedAddress = XUtils.escapeCellText(hyperlinkAddress, true, false);
//		String linkText = "";
//		
//		if (!"".equals(cellText)){
//			linkText = cellText;
//		}else if (hlink.getLabel()!=null ){
//			linkText = XUtils.escapeCellText(hlink.getLabel(), wrap, false);
//		}else{
//			linkText = escapedAddress;
//		}
//		return BookHelper.formatHyperlink((XBook)sheet.getWorkbook(), hlink.getType(), escapedAddress, linkText);
//	}
//
//	/**
//	 * Escape character that has special meaning in HTML such as &lt;, &amp;, etc..
//	 * @param text the text
//	 * @param wrap whether to allow wrap
//	 * @param multiline whether to show multiple line
//	 * @return the HTML 
//	 */
//	public static String escapeCellText(String text, boolean wrap, boolean multiline) {
//		final StringBuffer out = new StringBuffer();
//		for (int j = 0, tl = text.length(); j < tl; ++j) {
//			char cc = text.charAt(j);
//			switch (cc) {
//			case '&': out.append("&amp;"); break;
//			case '<': out.append("&lt;"); break;
//			case '>': out.append("&gt;"); break;
//			case ' ': out.append(wrap?" ":"&nbsp;"); break;
//			case '\n':
//				if (wrap && multiline) {
//					out.append("<br/>");
//					break;
//				}
//			default:
//				out.append(cc);
//			}
//		}
//		return out.toString();
//	}
//	
//	//escape character that has special meaning in HTML such as &lt;, &amp;, etc..
//	//runs is a index pair to the text string that needs to be escaped
//	protected static String escapeCellText(String text,boolean wrap,boolean multiline, List<int[]> runs){
//		StringBuffer out = new StringBuffer();
//		if (text!=null){
//			int j = 0;
//			for (int[] run : runs) {
//				for (int tl = run[0]; j < tl; ++j) {
//					char cc = text.charAt(j);
//					out.append(cc);
//				}
//				for (int tl = run[1]; j < tl; ++j) {
//					char cc = text.charAt(j);
//					switch (cc) {
//					case '&': out.append("&amp;"); break;
//					case '<': out.append("&lt;"); break;
//					case '>': out.append("&gt;"); break;
//					case ' ': out.append(wrap?" ":"&nbsp;"); break;
//					case '\n':
//						if (wrap && multiline) {
//							out.append("<br/>");
//							break;
//						}
//					default:
//						out.append(cc);
//					}
//				}
//			}
//			for (int tl = text.length(); j < tl; ++j) {
//				char cc = text.charAt(j);
//				out.append(cc);
//			}
//		}
//		return out.toString();
//	}
//	
	//Used for special id generator so UI component can find the sheet easily.
	public static String nextUpdateId(){
		Execution ex = Executions.getCurrent();
		Integer count;
		synchronized (ex) {
			count = (Integer) ex.getAttribute("_zssmseq");
			if (count == null) {
				count = Integer.valueOf(0);
			} else {
				count = Integer.valueOf(count.intValue() + 1);
			}
			ex.setAttribute("_zssmseq", count);
		}
		return count.toString();
	}
//	
//	/**
//	 * Return or create if not exist the {@link Cell} per the given sheet, row index, and column index. 
//	 * @param sheet the sheet to get cell from
//	 * @param rowIndex the row index of the cell
//	 * @param colIndex the column index of the cell
//	 * @return or create if not exist the {@link Cell} per the given sheet, row index, and column index. 
//	 */
//	public static Cell getOrCreateCell(XSheet sheet,int rowIndex, int colIndex){
//		Row row = getOrCreateRow(sheet, rowIndex);
//		Cell cell = row.getCell(colIndex);
//		if (cell == null) {
//			cell = row.createCell(colIndex);
//		}
//		return cell;
//	}
//	
//	public static Row getOrCreateRow(XSheet sheet, int rowIndex) {
//		Row row = sheet.getRow(rowIndex);
//		if (row == null) {
//			row = sheet.createRow(rowIndex);
//		}
//		return row;
//	}
//	
//	/**
//	 * Return the {@link Cell} per the given sheet, row index, and column index; return null if cell not exists. 
//	 * @param sheet the sheet to get cell from
//	 * @param rowIndex the row index of the cell
//	 * @param colIndex the column index of the cell
//	 * @return the {@link Cell} per the given sheet, row index, and column index; return null if cell not exists. 
//	 */
//	public static Cell getCell(XSheet sheet,int rowIndex, int colIndex){
//		final Row row = sheet.getRow(rowIndex);
//		return row != null ? row.getCell(colIndex) : null; 
//	}
//
//	public static void setCellValue(XSheet sheet, int rowIndex, int colIndex, String value) {
//		final Cell cell = getOrCreateCell(sheet, rowIndex, colIndex);
//		cell.setCellValue(value);
//	}
//	public static void setCellValue(XSheet sheet, int rowIndex, int colIndex, double value) {
//		final Cell cell = getOrCreateCell(sheet, rowIndex, colIndex);
//		cell.setCellValue(value);
//	}
//	public static void setCellValue(XSheet sheet, int rowIndex, int colIndex, boolean value) {
//		final Cell cell = getOrCreateCell(sheet, rowIndex, colIndex);
//		cell.setCellValue(value);
//	}
//	public static void setCellValue(XSheet sheet, int rowIndex, int colIndex, Date value) {
//		final Cell cell = getOrCreateCell(sheet, rowIndex, colIndex);
//		cell.setCellValue(value);
//	}
//	public static void setCellValue(XSheet sheet, int rowIndex, int colIndex, int value) {
//		final Cell cell = getOrCreateCell(sheet, rowIndex, colIndex);
//		cell.setCellValue(value);
//	}
//	
//	/**
//	 * Returns the uuid of the specified {@link XSheet}. 
//	 * to identify a {@link XSheet})
//	 * @param sheet the sheet
//	 * @return the uuid of the specified {@link XSheet}.
//	 */
//	public static String getSheetUuid(XSheet sheet){
//		return ((SheetCtrl)sheet).getUuid();
//	}
//	
//	public static String getSheetUuid(Sheet sheet){
//		
//		return (((SheetImpl)sheet).getNative()).getId();
//	}
//	
//	/**
//	 * Returns the {@link XSheet} of the specified uuid; null if id not exists.
//	 * @param book the book the contains the {@link XSheet}
//	 * @param uuid the sheet uuid
//	 * @return the {@link XSheet} of the specified uuid; null if id not exists.
//	 */
//	public static XSheet getSheetByUuid(XBook book, String uuid) {
//		int count = book.getNumberOfSheets();
//		for(int j = 0; j < count; ++j) {
//			XSheet sheet = book.getWorksheetAt(j);
//			if (uuid.equals(getSheetUuid(sheet))) {
//				return sheet;
//			}
//		}
//		return null;
//	}
//	
//	/**
//	 * Returns the {@link XSheet} of the specified uuid; null if id not exists.
//	 * @param book the book the contains the {@link XSheet}
//	 * @param uuid the sheet uuid
//	 * @return the {@link XSheet} of the specified uuid; null if id not exists.
//	 */
//	public static Sheet getSheetByUuid(Book book, String uuid) {
//		int count = book.getNumberOfSheets();
//		for(int j = 0; j < count; ++j) {
//			Sheet sheet = book.getSheetAt(j);
//			if (uuid.equals(getSheetUuid(sheet))) {
//				return sheet;
//			}
//		}
//		return null;
//	}
//		
//	
//	
	/**
	 * Returns whether the {@link Spreadsheet} title is in index mode
	 * @param ss the {@link Spreadsheet} 
	 * @return whether the {@link Spreadsheet} title is in index mode
	 */
	public static boolean isTitleIndexMode(Spreadsheet ss){
		return "index".equals(ss.getAttribute("zss_titlemode"));
	}
//	
//	/**
//	 * Returns the associated model sheet({@link XSheet}) per the given the reference sheet({@link RefSheet}). 
//	 * @param book the model book
//	 * @param refSheet the reference sheet
//	 * @return the associated model sheet({@link XSheet}) per the given the reference sheet({@link RefSheet}).
//	 */
//	public static XSheet getSheetByRefSheet(XBook book, RefSheet refSheet) {
//		return book.getWorksheet(refSheet.getSheetName());
//	}
//
//	/**
//	 * Returns the {@link Hyperlink} to be shown on the specified cell.
//	 * @param cell the cell
//	 * @return the {@link Hyperlink} to be shown on the specified cell.
//	 */
//	public static Hyperlink getHyperlink(Cell cell) {
//		return BookHelper.getHyperlink(cell);
//	}
//
//	public static XFormatText getFormatText(Cell cell) {
//		return BookHelper.getFormatText(cell);
//	}
//	/**
//	 * Returns the text for editing on the specified cell. 
//	 * @param cell the cell
//	 * @return the text for editing on the specified cell.
//	 */
//	public static String getEditText(Cell cell) {
//		final RichTextString rstr = cell == null ? null : getRichEditText(cell);
//		return rstr != null ? rstr.getString() : "";
//	}
//	/**
//	 * Returns the {@link RichTextString} for editing on the cell.
//	 * @param cell the cell
//	 * @return the {@link RichTextString} for editing on the cell.
//	 */
//	public static RichTextString getRichEditText(Cell cell) {
//		return BookHelper.getRichEditText(cell);
//	}
//	
//	public static int getWidthInPx(XSheet zkSheet, ClientAnchor anchor, int charWidth) {
//	    final int l = anchor.getCol1();
//	    final int lfrc = anchor.getDx1();
//	    
//	    //first column
//	    final int lw = getWidthAny(zkSheet,l, charWidth);
//	    
//	    final int wFirst = lfrc >= 1024 ? 0 : (lw - lw * lfrc / 1024);  
//	    
//	    //last column
//	    final int r = anchor.getCol2();
//	    int wLast = 0;
//	    if (l != r) {
//		    final int rfrc = anchor.getDx2();
//	    	final int rw = getWidthAny(zkSheet,r, charWidth);
//	    	wLast = rw * rfrc / 1024;  
//	    }
//	    
//	    //in between
//	    int width = wFirst + wLast;
//	    for (int j = l+1; j < r; ++j) {
//	    	width += getWidthAny(zkSheet,j, charWidth);
//	    }
//	    
//	    return width;
//	}
//	
//	public static int getHeightInPx(XSheet zkSheet, ClientAnchor anchor) {
//	    final int t = anchor.getRow1();
//	    final int tfrc = anchor.getDy1();
//	    
//	    //first row
//	    final int th = getHeightAny(zkSheet,t);
//	    final int hFirst = tfrc >= 256 ? 0 : (th - th * tfrc / 256);  
//	    
//	    //last row
//	    final int b = anchor.getRow2();
//	    int hLast = 0;
//	    if (t != b) {
//		    final int bfrc = anchor.getDy2();
//	    	final int bh = getHeightAny(zkSheet,b);
//	    	hLast = bh * bfrc / 256;  
//	    }
//	    
//	    //in between
//	    int height = hFirst + hLast;
//	    for (int j = t+1; j < b; ++j) {
//	    	height += getHeightAny(zkSheet,j);
//	    }
//	    
//	    return height;
//	}
//	
//	public static int getTopFraction(XSheet zkSheet, ClientAnchor anchor) {
//	    final int t = anchor.getRow1();
//	    final int tfrc = anchor.getDy1();
//	    
//	    //first row
//	    final int th = getHeightAny(zkSheet,t);
//	    return tfrc >= 256 ? th : (th * tfrc / 256);  
//	}
//	
//	public static int getLeftFraction(XSheet zkSheet, ClientAnchor anchor, int charWidth) {
//	    final int l = anchor.getCol1();
//	    final int lfrc = anchor.getDx1();
//	    
//	    //first column
//	    final int lw = getWidthAny(zkSheet,l, charWidth);
//	    return lfrc >= 1024 ? lw : (lw * lfrc / 1024);  
//	}
//	
//	public static int getColumnWidthInPx(XSheet sheet, int col) {
//		return getWidthAny(sheet, col, ((XBook)sheet.getWorkbook()).getDefaultCharWidth());
//	}
//	
//	public static int getRowHeightInPx(XSheet sheet, int row) {
//		return getHeightAny(sheet, row);
//	}
//	
//	public static int getRowHeightInPx(XSheet sheet, Row row) {
//		final int defaultHeight = sheet.getDefaultRowHeight();
//		int h = row == null ? defaultHeight : row.getHeight();
//		if (h == 0xFF) {
//			h = defaultHeight;
//		}
//		return twipToPx(h);
//	}
//	
//	public static int getDefaultColumnWidthInPx(XSheet sheet) {
//		int columnWidth = sheet != null ? sheet.getDefaultColumnWidth() : -1;
//		return columnWidth <= 0 ? 64 : XUtils.defaultColumnWidthToPx(columnWidth, getDefaultCharWidth(sheet));
//	}
//	
//	public static int getDefaultCharWidth(XSheet sheet) {
//		return ((XBook)sheet.getWorkbook()).getDefaultCharWidth();
//	}
//	public static int getWidthAny(XSheet zkSheet,int col, int charWidth){
//		int w = zkSheet.getColumnWidth(col);
//		if (w == zkSheet.getDefaultColumnWidth() * 256) { //default column width
//			return XUtils.defaultColumnWidthToPx(w / 256, charWidth);
//		}
//		return fileChar256ToPx(w, charWidth);
//	}
//	
//	public static int getHeightAny(XSheet sheet, int row){
//		return getRowHeightInPx(sheet, sheet.getRow(row));
//	}
//	
//	//calculate the default char width in pixel per the given Font
//	public static int calcDefaultCharWidth(java.awt.Font font) {
//        /**
//         * Excel measures columns in units of 1/256th of a character width
//         * but the docs say nothing about what particular character is used.
//         * '0' looks to be a good choice. ref. http://support.microsoft.com/kb/214123
//         */
//        final String defaultString = "0";
//
//        final FontRenderContext frc = new FontRenderContext(null, true, true);
//        final AttributedString str = new AttributedString(defaultString);
//        copyAttributes(font, str, 0, defaultString.length());
//        final TextLayout layout = new TextLayout(str.getIterator(), frc);
//        //TODO, don't know how to calculate the charter width per the Font
//        final double w = layout.getAdvance();
//        final int charWidth = (int) Math.floor(w + 0.5) +  1;
//        return charWidth;
//	}
//	
//    private static void copyAttributes(java.awt.Font font, AttributedString str, int startIdx, int endIdx) {
//        str.addAttribute(TextAttribute.FAMILY, font.getFontName(), startIdx, endIdx);
//        str.addAttribute(TextAttribute.SIZE, new Float(font.getSize2D()), startIdx, endIdx);
//        if (font.isBold()) str.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, startIdx, endIdx);
//        if (font.isItalic()) str.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE, startIdx, endIdx);
//    }
//
	/** convert pixel to point */
	public static int pxToPoint(int px) {
		return px * 72 / 96; //assume 96dpi
	}
	
	/** convert point to pixel */
	public static int pointToPx(int point) {
		return point * 96 / 72; //assume 96dpi
	}
	
	/** convert pixel to twip (1/20 point) */
	public static int pxToTwip(int px) {
		return px * 72 * 20 / 96; //assume 96dpi
	}

	/** convert twip (1/20 point) to pixel */
	public static int twipToPx(int twip) {
		return twip * 96 / 72 / 20; //assume 96dpi
	}
	
	/** convert point to twip (1/20 point) */
	public static int pointToTwip(int px) {
		return px * 20; 
	}

	/** convert twip (1/20 point) to point */
	public static int twipToPoint(int twip) {
		return twip / 20; 
	}

	/** convert file 1/256 character width to pixel */
	public static int fileChar256ToPx(int char256, int charWidth) {
		final double w = (double) char256;
		return (int) Math.floor(w * charWidth / 256 + 0.5);
	}
	
	/** convert pixel to file 1/256 character width */
	public static int pxToFileChar256(int px, int charWidth) {
		final double w = (double) px;
		return (int) Math.floor(w * 256 / charWidth + 0.5);
	}
	
	/** convert 1/256 character width to pixel */
	public static int screenChar256ToPx(int char256, int charWidth) {
		final double w = (double) char256;
		return (char256 < 256) ?
			(int) Math.floor(w * (charWidth + 5) / 256 + 0.5):
			(int) Math.floor(w * charWidth  / 256 + 0.5) + 5;
	}
	
	/** Convert pixel to 1/256 character width */
	public static int pxToScreenChar256(int px, int charWidth) {
		return px < (charWidth + 5) ? 
				px * 256 / (charWidth + 5):
				(px - 5) * 256 / charWidth;
	}
	
	/** convert character width to pixel */
	public static int screenChar1ToPx(double w, int charWidth) {
		return w < 1 ?
			(int) Math.floor(w * (charWidth + 5) + 0.5):
			(int) Math.floor(w * charWidth + 0.5) + 5;
	}
	
	/** Convert pixel to character width (for application) */
	public static double pxToScreenChar1(int px, int charWidth) {
		final double w = (double) px;
		return px < (charWidth + 5) ?
			roundTo100th(w / (charWidth + 5)):
			roundTo100th((w - 5) / charWidth);
	}

	/** Convert default columns character width to pixel */ 
	public static int defaultColumnWidthToPx(int columnWidth, int charWidth) {
		final int w = columnWidth * charWidth + 5;
		final int diff = w % 8;
		return w + (diff > 0 ? (8 - diff) : 0);
	}
	
	private static double roundTo100th(double w) {
		return Math.floor(w * 100 + 0.5) / 100;
	}

//	public static String getColumnTitle(XSheet sheet, int col) {
//		return CellReference.convertNumToColString(col);
//	}
//	
//	public static String getRowTitle(XSheet sheet, int row) {
//		return Integer.toString((row+1));
//	}
}
