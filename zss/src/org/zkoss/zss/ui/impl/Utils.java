/* Utils.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Mar 20, 2008 12:40:01 PM     2008, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.impl;

import org.zkoss.util.logging.Log;
//
///**
// * Internal Use Only. Utility class for {@link Spreadsheet}.
// * @author Dennis.Chen
// *
// */
@Deprecated
public class Utils {
	private static final Log log = Log.lookup(Utils.class);
//
//	public static void autoFilter(XSheet sheet, Rect selection) {
//		int left = selection.getLeft();
//		int right = selection.getRight();
//		int top = selection.getTop();
//		int btm = selection.getBottom();
//		
//		XRange rng = Utils.getRange(sheet, top, left, btm, right);
//				
//		rng.autoFilter();
//	}
//	
//	/**
//	 * Sort in selection range
//	 * @param sheet the sheet to sort 
//	 * @param selection sort range
//	 * @param index keys for sorting
//	 * <p> default use the first index in selection
//	 * @param order order for sorting, true to do descending sort; false to do ascending sort
//	 * <p> default ascending sort
//	 * @param header whether sort range includes header
//	 * @param matchCase true to match the string cases; false to ignore string cases
//	 * @param sortByRows false indicate sort by column (sort from top to bottom); true indicate sort by row (sort from left to right) 
//	 */
//	public static void sort(XSheet sheet, Rect selection, int[] index, boolean[] order, int[] dataOption, boolean header, boolean matchCase, boolean sortByRows) {
//		int left = selection.getLeft();
//		int right = selection.getRight();
//		int top = selection.getTop();
//		int btm = selection.getBottom();
//		
//		XRange rng = Utils.getRange(sheet, top, left, btm, right);
//		XRange rng1 = null;
//		XRange rng2 = null;
//		XRange rng3 = null;
//		boolean desc1 = false;
//		boolean desc2 = false;
//		boolean desc3 = false;
//		int dataOption1 = 0;
//		int dataOption2 = 0;
//		int dataOption3 = 0;
//		if (index == null || index.length == 0) {
//			rng1 = Utils.getRange(sheet, 
//									top, 
//									left, 
//									sortByRows ? top : btm, 
//									sortByRows ? right : left);
//			
//		} else if (index.length > 0) {
//			switch (index.length) {
//			case 3:
//				if (sortByRows)
//					rng3 = Utils.getRange(sheet, index[2], left, index[2], right);
//				else
//					rng3 = Utils.getRange(sheet, top, index[2], btm, index[2]);
//				desc3 = order[2];
//			case 2:
//				if (sortByRows)
//					rng2 = Utils.getRange(sheet, index[1], left, index[1], right);
//				else
//					rng2 = Utils.getRange(sheet, top, index[1], btm, index[1]);
//				desc2 = order[1];
//			case 1:
//				if (sortByRows) 
//					rng1 = Utils.getRange(sheet, index[0], left, index[0], right); //sort by rows, sort from left to right
//				else
//					rng1 = Utils.getRange(sheet, top, index[0], btm, index[0]); // sort by columns, sort from top to bottom
//				desc1 = order[0];
//			}
//		}
//		if (order != null && order.length > 0) {
//			switch (order.length) {
//			case 3:
//				desc3 = order[2];
//			case 2:
//				desc2 = order[1];
//			case 1:
//				desc1 = order[0];
//			}
//		}
//		if (dataOption != null && dataOption.length > 0) {
//			switch (dataOption.length) {
//			case 3:
//				dataOption3 = dataOption[2];
//			case 2:
//				dataOption2 = dataOption[1];
//			case 1:
//				dataOption1 = dataOption[0];
//			}
//		}
//		rng.sort(rng1, desc1, rng2, 0, desc2, rng3, desc3, !header ? BookHelper.SORT_HEADER_NO : BookHelper.SORT_HEADER_YES, 0, matchCase, sortByRows, 0, dataOption1, dataOption2, dataOption3);
//	}
//
//	/**
//	 * Pastes to a destination from source {@link Rect}
//	 * @param srcSheet the source sheet 
//	 * @param srcRect paste source range
//	 * @param dstSheet paste to the sheet
//	 * @param tRow destination top row index
//	 * @param lCol destination left column index
//	 * @param bRow destination bottom row index
//	 * @param rCol destination right column index
//	 * @param pasteType the part of the range to be pasted
//	 * @param pasteOp the paste operation
//	 * @param skipBlanks true to not have blank cells in the ranage on the Clipboard pasted into this range; default false
//	 * @param transpose true to transpose rows and columns when pasting to this range; default false
//	 * @return the real pasted range
//	 */
//	public static XRange pasteSpecial(XSheet srcSheet, Rect srcRect, XSheet dstSheet, int tRow, int lCol, int bRow, int rCol, int pasteType, int pasteOp, boolean skipBlanks, boolean transpose) {
//		XRange rng = Utils.getRange(srcSheet, srcRect.getTop(), srcRect.getLeft(), srcRect.getBottom(), srcRect.getRight());
//		XRange dstRange = Utils.getRange(dstSheet, tRow, lCol, bRow, rCol);
//		XRange pasteRange = rng.pasteSpecial(dstRange, pasteType, pasteOp, skipBlanks, transpose);
//		if (pasteRange == null) {
//			showProtectMessage();
//		}
//		return pasteRange;
//	}
//	
//	private static void showProtectMessage() {
//		Messagebox.show("The cell that you are trying to change is protected and locked.", "ZK Spreadsheet", Messagebox.OK, Messagebox.EXCLAMATION);
//	}
//
//	/**
//	 * Set hyperlink in cell
//	 * @param sheet
//	 * @param rowIndex
//	 * @param colIndex
//	 * @param linkType
//	 * @param address
//	 * @param display
//	 */
//	public static void setHyperlink(XSheet sheet, int rowIndex, int colIndex, int linkType, String address, String display) {
//		Utils.getRange(sheet, rowIndex, colIndex).setHyperlink(linkType, address, display);
//	}
//	/**
//	 * Set font in selection range. 
//	 * <p> Copy original cell style, and set new font.
//	 * @param sheet
//	 * @param rect selection range
//	 * @param boldWeight 
//	 * @param color color to use
//	 * @param fontHeight the font height in unit's of 1/20th of a point
//	 * @param fontName the name for the font
//	 * @param italic use italics or not
//	 * @param strikeout strikeout or not
//	 * @param typeOffset offset type to use
//	 * @param underline underline type
//	 */
//	public static void setFont(XSheet sheet, Rect rect, final short boldWeight, final Color color, final short fontHeight, final String fontName, 
//			final boolean italic, final boolean strikeout, final short typeOffset, final byte underline) {
//
//		visitCells(sheet, rect, new CellVisitor(){
//
//			@Override
//			public void handle(CellVisitorContext context) {
//				Font srcFont = context.getFont();
//				Font font = context.getOrCreateFont(boldWeight, color,
//						fontHeight, fontName, italic, strikeout, typeOffset, underline);
//				if (!srcFont.equals(font)) {
//					CellStyle newStyle = context.cloneCellStyle();
//					newStyle.setFont(font);
//					context.getRange().setStyle(newStyle);
//				}
//			}});
//	}
//	
//	/**
//	 * Sets font color in selection range
//	 * @param sheet
//	 * @param rect
//	 * @param color color (in string as #RRGGBB)
//	 */
//	public static void setFontColor(final XSheet sheet, Rect rect, final String color) {
//		visitCells(sheet, rect, new CellVisitor() {
//
//			@Override
//			public void handle(CellVisitorContext context) {
//				String srcColor = context.getFontColor();
//				if (!srcColor.equalsIgnoreCase(color)) {
//					final Workbook book = sheet.getWorkbook();
//					Font srcFont = context.getFont();
//
//					Color fontColor = BookHelper.HTMLToColor(book, color);
//					Font newFont = context.getOrCreateFont(srcFont.getBoldweight(), fontColor, srcFont.getFontHeight(), srcFont.getFontName(), 
//							srcFont.getItalic(), srcFont.getStrikeout(), srcFont.getTypeOffset(), srcFont.getUnderline());
//					CellStyle newStyle = context.cloneCellStyle();
//					newStyle.setFont(newFont);
//					newStyle.setFontColorColor(fontColor);
//					context.getRange().setStyle(newStyle);
//				}
//			}});
//	}
//	
//	/**
//	 * Set font family in selection range
//	 * <p> Copy the original cell style, font style, and set new font family
//	 * @param sheet 
//	 * @param rect selection range
//	 * @param fontName font name
//	 */
//	public static void setFontFamily(final XSheet sheet, Rect rect, final String fontName) {
//
//		visitCells(sheet, rect, new CellVisitor(){
//
//			@Override
//			public void handle(CellVisitorContext context) {
//				String srcFontName = context.getFontFamily();
//
//				if (srcFontName != fontName) {
//					final Workbook book = sheet.getWorkbook();
//					Font srcFont = context.getFont();
//					Font newFont = context.getOrCreateFont(srcFont.getBoldweight(), BookHelper.getFontColor(book, srcFont), srcFont.getFontHeight(), fontName, 
//							srcFont.getItalic(), srcFont.getStrikeout(), srcFont.getTypeOffset(), srcFont.getUnderline());
//					CellStyle newStyle = context.cloneCellStyle();
//					newStyle.setFont(newFont);
//					context.getRange().setStyle(newStyle);
//				}
//			}
//		});
//	}
//	
//	/**
//	 * Set the font height in unit's of 1/20th of a point
//	 * <p> Copy the original cell style, font style, and set new font family
//	 * @param sheet 
//	 * @param rect selection range
//	 * @param fontHeight font height in 1/20ths of a point
//	 */
//	public static void setFontHeight(final XSheet sheet, Rect rect, final short fontHeight) {
//
//		visitCells(sheet, rect, new CellVisitor(){
//
//			@Override
//			public void handle(CellVisitorContext context) {
//				short srcFontHgh = context.getFontHeight();
//				if (srcFontHgh != fontHeight) {
//					final Workbook book = sheet.getWorkbook();
//					Font srcFont = context.getFont();
//					Font newFont = context.getOrCreateFont(srcFont.getBoldweight(), BookHelper.getFontColor(book, srcFont), fontHeight, srcFont.getFontName(), 
//						srcFont.getItalic(), srcFont.getStrikeout(), srcFont.getTypeOffset(), srcFont.getUnderline());
//					CellStyle newStyle = context.cloneCellStyle();
//					newStyle.setFont(newFont);
//					context.getRange().setStyle(newStyle);
//				}
//			}});
//	}
//	
//	/**
//	 * Set the font bold.
//	 * <p> Copy the original cell style, font style, and set bold.
//	 * @param sheet
//	 * @param rect
//	 * @param isBold
//	 */
//	public static void setFontBold(final XSheet sheet, Rect rect, final boolean isBold) {
//
//		visitCells(sheet, rect, new CellVisitor(){
//
//			@Override
//			public void handle(CellVisitorContext context) {
//				boolean srcBold = context.isBold(); 
//				if (srcBold != isBold) {
//					final Workbook book = sheet.getWorkbook();
//					Font srcFont = context.getFont();
//					Font newFont = context.getOrCreateFont(isBold ? Font.BOLDWEIGHT_BOLD : Font.BOLDWEIGHT_NORMAL, BookHelper.getFontColor(book, srcFont), srcFont.getFontHeight(), srcFont.getFontName(), 
//							srcFont.getItalic(), srcFont.getStrikeout(), srcFont.getTypeOffset(), srcFont.getUnderline());
//					CellStyle newStyle = context.cloneCellStyle();
//					newStyle.setFont(newFont);
//					context.getRange().setStyle(newStyle);
//				}
//			}});
//	}
//	
//	/**
//	 * Set the font Italic.
//	 * <p> Copy the original cell style, font style, and set italic.
//	 * @param sheet
//	 * @param rect
//	 * @param isItalic
//	 */
//	public static void setFontItalic(final XSheet sheet, Rect rect, final boolean isItalic) {
//
//		visitCells(sheet, rect, new CellVisitor(){
//
//			@Override
//			public void handle(CellVisitorContext context) {
//				Font srcFont = context.getFont();
//				boolean srcItalic = context.isItalic();
//
//				if (srcItalic != isItalic) {
//					final Workbook book = sheet.getWorkbook();
//					Font newFont = context.getOrCreateFont(srcFont.getBoldweight(), BookHelper.getFontColor(book, srcFont), srcFont.getFontHeight(), srcFont.getFontName(), 
//							isItalic, srcFont.getStrikeout(), srcFont.getTypeOffset(), srcFont.getUnderline());
//					CellStyle newStyle = context.cloneCellStyle();
//					newStyle.setFont(newFont);
//					context.getRange().setStyle(newStyle);
//				}
//			}});
//	}
//	
//	/**
//	 * Set the font underline.
//	 * <p> Copy the original cell style, font style, and set underline.
//	 * @param sheet
//	 * @param rect
//	 * @param underline
//	 */
//	public static void setFontUnderline(final XSheet sheet, Rect rect, final byte underline) {
//		
//		visitCells(sheet, rect, new CellVisitor(){
//
//			public void handle(CellVisitorContext context) {
//				Font srcFont = context.getFont();
//				byte srcUnderline = srcFont.getUnderline();
//
//				if (srcUnderline != underline) {
//					final Workbook book = sheet.getWorkbook();
//					Font newFont = context.getOrCreateFont(srcFont.getBoldweight(), BookHelper.getFontColor(book, srcFont), 
//						srcFont.getFontHeight(), srcFont.getFontName(), 
//						srcFont.getItalic(), srcFont.getStrikeout(), srcFont.getTypeOffset(), underline);
//					CellStyle newStyle = context.cloneCellStyle();
//					newStyle.setFont(newFont);
//					context.getRange().setStyle(newStyle);
//				}
//			}	
//		});
//	}
//	
//	/**
//	 * Set the font strikeout.
//	 * <p> Copy the original cell style, font style, and set strikeout.
//	 * @param sheet
//	 * @param rect
//	 * @param isStrikeout
//	 */
//	public static void setFontStrikeout(final XSheet sheet, Rect rect, final boolean isStrikeout) {
//		visitCells(sheet, rect, new CellVisitor(){
//
//			public void handle(CellVisitorContext context) {
//				Font srcFont = context.getFont();
//				boolean srcStrikeout = srcFont.getStrikeout();
//				
//				if (srcStrikeout != isStrikeout) {
//					final Workbook book = sheet.getWorkbook();
//					Font newFont = context.getOrCreateFont(srcFont.getBoldweight(), 
//							BookHelper.getFontColor(book, srcFont), srcFont.getFontHeight(), srcFont.getFontName(), 
//							srcFont.getItalic(), isStrikeout, srcFont.getTypeOffset(), srcFont.getUnderline());
//					CellStyle cellStyle = context.cloneCellStyle();
//					cellStyle.setFont(newFont);
//					context.getRange().setStyle(cellStyle);
//				}
//			}});
//	}
//	
//	/**
//	 * Set alignment in selection range
//	 * @param sheet
//	 * @param rect
//	 * @param alignment
//	 */
//	public static void setAlignment(XSheet sheet, Rect rect, final short alignment) {
//		visitCells(sheet, rect, new CellVisitor(){
//			@Override
//			public void handle(CellVisitorContext context) {
//				final short srcAlign = context.getAlignment();
//
//				if (srcAlign != alignment) {
//					CellStyle newStyle = context.cloneCellStyle();
//					newStyle.setAlignment(alignment);
//					context.getRange().setStyle(newStyle);
//				}
//			}});
//	}
//	
//	/**
//	 * Set wrap text in selection range
//	 * @param sheet
//	 * @param rect
//	 * @param wrapped
//	 */
//	public static void setWrapText(XSheet sheet, Rect rect, final boolean wrapped) {
//		visitCells(sheet, rect, new CellVisitor(){
//			@Override
//			public void handle(CellVisitorContext context) {
//				final boolean srcWrapText = context.isWrapText();
//
//				if (srcWrapText != wrapped) {
//					CellStyle newStyle = context.cloneCellStyle();
//					newStyle.setWrapText(wrapped);
//					context.getRange().setStyle(newStyle);
//				}
//			}});
//	}
//	
//	/**
//	 * Sets cell locked
//	 * @param sheet
//	 * @param rect
//	 * @param locked
//	 */
//	public static void setLocked(XSheet sheet, Rect rect, final boolean locked) {
//		visitCells(sheet, rect, new CellVisitor(){
//			@Override
//			public void handle(CellVisitorContext context) {
//				final boolean srcLocked = context.getLocked();
//
//				if (srcLocked != locked) {
//					CellStyle newStyle = context.cloneCellStyle();
//					newStyle.setLocked(locked);
//					context.getRange().setStyle(newStyle);
//				}
//			}});
//	}
//	
//	/**
//	 * Set format in selection range
//	 * @param sheet
//	 * @param rect
//	 * @param format
//	 */
//	public static void setDataFormat(XSheet sheet, Rect rect, final String format) {
//		visitCells(sheet, rect, new CellVisitor(){
//			@Override
//			public void handle(CellVisitorContext context) {
//				
//				short srcFormat = context.getFormatIndex();
//				
//				DataFormat dataFormat = context.getBook().createDataFormat();
//				short dstFormat = dataFormat.getFormat(format);
//				
//				if (srcFormat != dstFormat) {
//					CellStyle newStyle = context.cloneCellStyle();
//					newStyle.setDataFormat(dstFormat);
//					context.getRange().setStyle(newStyle);
//				}
//			}
//		});
//	}
//	
//	/**
//	 * Visit each cell in the {@link Rect}
//	 * @param sheet
//	 * @param rect
//	 * @param vistor
//	 */
//	public static void visitCells(XSheet sheet, Rect rect, CellVisitor vistor) {
//		new CellSelector().doVisit(sheet, rect, vistor);
//	}
//
//	/**
//	 * Visit each worksheet in the {@link XBook}.
//	 * @param book
//	 * @param visitor
//	 */
//	public static void visitSheets(XBook book, SheetVisitor visitor) {
//		new SheetSelector().doVisit(book, visitor);
//	}
//	
////	/**
////	 * 
////	 * @param sheet
////	 * @param rect
////	 * @param vistor
////	 * @param filters
////	 */
////	public static void visitCells(Worksheet sheet, Rect rect, CellVisitor vistor, CellFilter... filters) {
////		CellSelector selector = new CellSelector();
////		for(CellFilter cellFilter : filters){
////			selector.addFilter(cellFilter);
////		}
////		selector.doVisit(sheet, rect, vistor);
////	}
//	
//	
//	
//	/**
//	 * Set background color in selection range. 
//	 * <p> Copy original cell style, and set new background color
//	 * @param sheet sheet to set background color
//	 * @param rect selection range
//	 * @param color color to use
//	 */
//	public static void setBackgroundColor(XSheet sheet, Rect rect, String color) {
//		final XBook book  = (XBook) sheet.getWorkbook();
//		final Color bsColor = BookHelper.HTMLToColor(book, color);
//		for (int row = rect.getTop(); row <= rect.getBottom(); row++)
//			for (int col = rect.getLeft(); col <= rect.getRight(); col++) {
//				Cell cell = Utils.getOrCreateCell(sheet, row, col);
//				CellStyle cs = cell.getCellStyle();
//				final Color srcColor = cs.getFillForegroundColorColor();
//				if (Objects.equals(srcColor, bsColor)) {
//					continue;
//				}
//				CellStyle newCellStyle = book.createCellStyle();
//				newCellStyle.cloneStyleFrom(cs);
//				
//				//bug#ZSS-34: cell background color does not show in excel
//				//20110819, henrichen@zkoss.org: set color to a cell shall change its fillPattern to "solid" automatically
//				final short patternType = cs.getFillPattern();
//				if (patternType == CellStyle.NO_FILL) {
//					newCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
//				}
//				BookHelper.setFillForegroundColor(newCellStyle, bsColor);
//				XRange rng = Utils.getRange(sheet, row, col);
//				rng.setStyle(newCellStyle);
//			}
//	}
//	
//	/**
//	 * Sets the border in selection range
//	 * @param sheet sheet to set border
//	 * @param rect selection range
//	 * @param borderType border type
//	 * @param lineStyle border line style
//	 * @param color border color
//	 */
//	public static void setBorder(XSheet sheet, Rect rect, short borderType, BorderStyle lineStyle, String color) {
//		int lCol = rect.getLeft();
//		int rCol = rect.getRight();
//		int tRow = rect.getTop();
//		int bRow = rect.getBottom();
//
//		XRange rng = XRanges.range(sheet, tRow, lCol, bRow, rCol);
//		rng.setBorders(borderType, lineStyle, color);
//	}
//	
//	/**
//	 * Format and escape a {@link Hyperlink} to HTML &lt;a> string.
//	 * @param sheet the sheet with the RichTextString 
//	 * @param hlink the Hyperlink
//	 * @return the HTML &lt;a> format string
//	 */
//	public static String formatHyperlink(XSheet sheet, Hyperlink hlink, String cellText, boolean wrap) {
//		if (hlink == null) {
//			return cellText;
//		}
//		final String address = Utils.escapeCellText(hlink.getAddress(), true, false);
//		final String linkLabel = hlink.getLabel();
//		final String label = !"".equals(cellText) ? cellText :  
//				Utils.escapeCellText(linkLabel == null ? hlink.getAddress() : linkLabel, wrap, false);
//		return BookHelper.formatHyperlink((XBook)sheet.getWorkbook(), hlink.getType(), address, label);
//	}
//	/**
//	 * Format and escape a {@link RichTextString} to HTML &lt;span> string.
//	 * @param sheet the sheet with the RichTextString 
//	 * @param rstr the RichTextString
//	 * @param wrap whether wrap the string if see "\n".
//	 * @return the HTML &lt;span> format string
//	 */
//	public static String formatRichTextString(XSheet sheet, RichTextString rstr, boolean wrap) {
//		final List<int[]> indexes = new ArrayList<int[]>(rstr.numFormattingRuns()+1);
//		String text = BookHelper.formatRichText((XBook)sheet.getWorkbook(), rstr, indexes);
//		return Utils.escapeCellText(text, wrap, true, indexes);
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
//	private static String escapeCellText(String text,boolean wrap,boolean multiline, List<int[]> runs){
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
//	//Used for special id generator so UI component can find the sheet easily.
//	public static String nextUpdateId(){
//		Execution ex = Executions.getCurrent();
//		Integer count;
//		synchronized (ex) {
//			count = (Integer) ex.getAttribute("_zssmseq");
//			if (count == null) {
//				count = Integer.valueOf(0);
//			} else {
//				count = Integer.valueOf(count.intValue() + 1);
//			}
//			ex.setAttribute("_zssmseq", count);
//		}
//		return count.toString();
//	}
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
//	public static void copyCell(XSheet srcSheet, int srcRow, int srcCol, XSheet dstSheet, int dstRow, int dstCol) {
//		final XRange srcRange = getRange(srcSheet, srcRow, srcCol);
//		final XRange dstRange = getRange(dstSheet, dstRow, dstCol);
//		final XRange pasteRange = srcRange.copy(dstRange);
//		if(pasteRange == null) {
//			showProtectMessage();
//		}
//	}
//	
//	public static void copyCell(Cell cell, XSheet dstSheet, int dstRow, int dstCol) {
//		copyCell((XSheet)cell.getSheet(), cell.getRowIndex(), cell.getColumnIndex(), dstSheet, dstRow, dstCol);
//	}
//	
//	public static void insertRows(XSheet sheet, int startRow, int endRow) {
//		final XBook book = (XBook)sheet.getWorkbook();
//		final XRange rng = getRange(sheet, startRow, 0, endRow, book.getSpreadsheetVersion().getLastColumnIndex());
//		rng.insert(XRange.SHIFT_DEFAULT, XRange.FORMAT_LEFTABOVE);
//	}
//
//	public static void deleteRows(XSheet sheet, int startRow, int endRow) {
//		final XBook book = (XBook)sheet.getWorkbook();
//		final XRange rng = getRange(sheet, startRow, 0, endRow, book.getSpreadsheetVersion().getLastColumnIndex());
//		rng.delete(XRange.SHIFT_DEFAULT);
//	}
//
//	public static void insertColumns(XSheet sheet, int startCol, int endCol) {
//		final XBook book = (XBook)sheet.getWorkbook();
//		final XRange rng = getRange(sheet, 0, startCol, book.getSpreadsheetVersion().getLastRowIndex(), endCol);
//		rng.insert(XRange.SHIFT_DEFAULT, XRange.FORMAT_LEFTABOVE);
//	}
//
//	public static void deleteColumns(XSheet sheet, int startCol, int endCol) {
//		final XBook book = (XBook)sheet.getWorkbook();
//		final XRange rng = getRange(sheet, 0, startCol, book.getSpreadsheetVersion().getLastRowIndex(), endCol);
//		rng.delete(XRange.SHIFT_DEFAULT);
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
//	 * Return a cell {@link XRange} per the given sheet, row index, and column index.
//	 * @param sheet the sheet to get the {@link XRange} from. 
//	 * @param row the row index of the cell
//	 * @param col the column index of the cell
//	 * @return a cell {@link XRange} per the given sheet, row index, and column index.
//	 */
//	public static XRange getRange(XSheet sheet, int row, int col) {
//		return XRanges.range(sheet, row, col);
//	}
//
//	/**
//	 * Returns a area {@link XRange} per the given sheet, top row index, left column index, 
//	 * bottom row index, and right column index.
//	 * @param sheet the sheet to get the {@link XRange} from. 
//	 * @param tRow the top row index of the area
//	 * @param lCol the left column index of the area
//	 * @param bRow the bottom row index of the area
//	 * @param rCol the right column index of the area
//	 * @return a area {@link XRange} per the given sheet, top row index, left column index, 
//	 * bottom row index, and right column index.
//	 */
//	public static XRange getRange(XSheet sheet, int tRow, int lCol, int bRow, int rCol) {
//		return XRanges.range(sheet, tRow, lCol, bRow, rCol);
//	}
//	
//	/**
//	 * Return a 3D cell {@link XRange} per the given sheet, row index, and column index.
//	 * @param firstSheet the sheet to get the {@link XRange} from. 
//	 * @param lastSheet the sheet to get the {@link XRange} from. 
//	 * @param row the row index of the cell
//	 * @param col the column index of the cell
//	 * @return a cell {@link XRange} per the given sheet, row index, and column index.
//	 */
//	public static XRange getRange(XSheet firstSheet, XSheet lastSheet, int row, int col) {
//		return XRanges.range(firstSheet, lastSheet, row, col);
//	}
//	
//	/**
//	 * Return a 3D area {@link XRange} per the given sheet, top row index, left column index, 
//	 * bottom row index, and right column index.
//	 * @param firstSheet the sheet to get the {@link XRange} from. 
//	 * @param lastSheet the sheet to get the {@link XRange} from. 
//	 * @param tRow the top row index of the area
//	 * @param lCol the left column index of the area
//	 * @param bRow the bottom row index of the area
//	 * @param rCol the right column index of the area
//	 * @return a 3D area {@link XRange} per the given sheet, top row index, left column index, 
//	 * bottom row index, and right column index.
//	 */
//	public static XRange getRange(XSheet firstSheet, XSheet lastSheet, int tRow, int lCol, int bRow, int rCol) {
//		return XRanges.range(firstSheet, lastSheet, tRow, lCol, bRow, rCol);
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
//	 * Returns whether the {@link Spreadsheet} title is in index mode
//	 * @param ss the {@link Spreadsheet} 
//	 * @return whether the {@link Spreadsheet} title is in index mode
//	 */
//	public static boolean isTitleIndexMode(Spreadsheet ss){
//		return "index".equals(ss.getAttribute("zss_titlemode"));
//	}
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
//		XRange range = getRange((XSheet)cell.getSheet(), cell.getRowIndex(), cell.getColumnIndex());
//		return range.getHyperlink();
//	}
//	/**
//	 * Returns the {@link RichTextString} to be shown on the specified cell. 
//	 * @param cell the cell
//	 * @return the {@link RichTextString} to be shown on the specified cell.
//	 */
//	public static RichTextString getText(Cell cell) {
//		XRange range = getRange((XSheet)cell.getSheet(), cell.getRowIndex(), cell.getColumnIndex());
//		return range.getText();
//	}
//	
//	public static XFormatText getFormatText(Cell cell) {
//		//TODO, shall cache the FormatText inside cell. value/style/evaluate then invalidate the cache 
//		XRange range = getRange((XSheet)cell.getSheet(), cell.getRowIndex(), cell.getColumnIndex());
//		return range.getFormatText();
//	}
//	
//	public static String getCellText(XSheet sheet, Cell cell) {
//		CellStyle style = (cell == null) ? null : cell.getCellStyle();
//		boolean wrap = style != null && style.getWrapText();
//		Hyperlink hlink = cell == null ? null : Utils.getHyperlink(cell);
//		final XFormatText ft = (cell == null) ? null : Utils.getFormatText(cell);
//		
//		final RichTextString rstr = ft != null && ft.isRichTextString() ? ft.getRichTextString() : null; 
//		String text = rstr != null ? Utils.formatRichTextString(sheet, rstr, wrap) : ft != null ? Utils.escapeCellText(ft.getCellFormatResult().text, wrap, true) : "";
//		if (hlink != null) {
//			text = Utils.formatHyperlink(sheet, hlink, text, wrap);
//		}
//		return text;
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
//	
//	/**
//	 * Set user input text into the cell at specified sheet, row index, and column index.
//	 * @param sheet the sheet where the cell resides
//	 * @param row the row index
//	 * @param col the column index
//	 * @param value the user input text
//	 */
//	public static void setEditText(XSheet sheet, int row, int col, String value) {
//		final Cell cell = getOrCreateCell(sheet, row, col);
//		setEditText(cell, value);
//	}
//	
//	/**
//	 * Sets the user input text into the cell.
//	 * @param cell the cell.
//	 * @param txt the user input text.
//	 */
//	public static void setEditText(Cell cell, String txt) {
//		final XRange range = getRange((XSheet)cell.getSheet(), cell.getRowIndex(), cell.getColumnIndex());
//		range.setEditText(txt);
//	}
//
//	/**
//	 * Returns the {@link RichTextString} for editing on the cell.
//	 * @param cell the cell
//	 * @return the {@link RichTextString} for editing on the cell.
//	 */
//	public static RichTextString getRichEditText(Cell cell) {
//		final XRange range = getRange((XSheet)cell.getSheet(), cell.getRowIndex(), cell.getColumnIndex());
//		return range.getRichEditText();
//	}
//	
//	/**
//	 * Sets the user input {@link RichTextString} into the cell.
//	 * @param cell the cell
//	 * @param value the user input {@link RichTextString} to be set into the cell.
//	 */
//	public static void setRichEditText(Cell cell, RichTextString value) {
//		final XRange range = getRange((XSheet)cell.getSheet(), cell.getRowIndex(), cell.getColumnIndex());
//		range.setRichEditText(value);
//	}
//	
//	public static void mergeCells(XSheet sheet, int tRow, int lCol, int bRow, int rCol, boolean across) {
//		XRange rng = getRange(sheet, tRow, lCol, bRow, rCol);
//		rng.merge(across);
//	}
//	
//	public static void unmergeCells(XSheet sheet, int tRow, int lCol, int bRow, int rCol) {
//		XRange rng = getRange(sheet, tRow, lCol, bRow, rCol);
//		rng.unMerge();
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
//		return columnWidth <= 0 ? 64 : Utils.defaultColumnWidthToPx(columnWidth, getDefaultCharWidth(sheet));
//	}
//	
//	public static int getDefaultCharWidth(XSheet sheet) {
//		return ((XBook)sheet.getWorkbook()).getDefaultCharWidth();
//	}
//	public static int getWidthAny(XSheet zkSheet,int col, int charWidth){
//		int w = zkSheet.getColumnWidth(col);
//		if (w == zkSheet.getDefaultColumnWidth() * 256) { //default column width
//			return Utils.defaultColumnWidthToPx(w / 256, charWidth);
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
//	/** convert pixel to point */
//	public static int pxToPoint(int px) {
//		return px * 72 / 96; //assume 96dpi
//	}
//	
//	/** convert pixel to twip (1/20 point) */
//	public static int pxToTwip(int px) {
//		return px * 72 * 20 / 96; //assume 96dpi
//	}
//
//	/** convert twip (1/20 point) to pixel */
//	public static int twipToPx(int twip) {
//		return twip * 96 / 72 / 20; //assume 96dpi
//	}
//
//	/** convert file 1/256 character width to pixel */
//	public static int fileChar256ToPx(int char256, int charWidth) {
//		final double w = (double) char256;
//		return (int) Math.floor(w * charWidth / 256 + 0.5);
//	}
//	
//	/** convert pixel to file 1/256 character width */
//	public static int pxToFileChar256(int px, int charWidth) {
//		final double w = (double) px;
//		return (int) Math.floor(w * 256 / charWidth + 0.5);
//	}
//	
//	/** convert 1/256 character width to pixel */
//	public static int screenChar256ToPx(int char256, int charWidth) {
//		final double w = (double) char256;
//		return (char256 < 256) ?
//			(int) Math.floor(w * (charWidth + 5) / 256 + 0.5):
//			(int) Math.floor(w * charWidth  / 256 + 0.5) + 5;
//	}
//	
//	/** Convert pixel to 1/256 character width */
//	public static int pxToScreenChar256(int px, int charWidth) {
//		return px < (charWidth + 5) ? 
//				px * 256 / (charWidth + 5):
//				(px - 5) * 256 / charWidth;
//	}
//	
//	/** convert character width to pixel */
//	public static int screenChar1ToPx(double w, int charWidth) {
//		return w < 1 ?
//			(int) Math.floor(w * (charWidth + 5) + 0.5):
//			(int) Math.floor(w * charWidth + 0.5) + 5;
//	}
//	
//	/** Convert pixel to character width (for application) */
//	public static double pxToScreenChar1(int px, int charWidth) {
//		final double w = (double) px;
//		return px < (charWidth + 5) ?
//			roundTo100th(w / (charWidth + 5)):
//			roundTo100th((w - 5) / charWidth);
//	}
//
//	/** Convert default columns character width to pixel */ 
//	public static int defaultColumnWidthToPx(int columnWidth, int charWidth) {
//		final int w = columnWidth * charWidth + 5;
//		final int diff = w % 8;
//		return w + (diff > 0 ? (8 - diff) : 0);
//	}
//	
//	private static double roundTo100th(double w) {
//		return Math.floor(w * 100 + 0.5) / 100;
//	}
//
//	public static String getColumnTitle(XSheet sheet, int col) {
//		return CellReference.convertNumToColString(col);
//	}
//	
//	public static String getRowTitle(XSheet sheet, int row) {
//		return ""+(row+1);
//	}
//
//	/**
//	 * Sets column Width in pixel.
//	 * @param sheet
//	 * @param col
//	 * @param px column width in pixel
//	 */
//	public static void setColumnWidth(XSheet sheet, int col, int px) {
//		final int char256 = Utils.pxToFileChar256(px, ((XBook)sheet.getWorkbook()).getDefaultCharWidth());
//		final XRange rng = Utils.getRange(sheet, -1 , col);
//		rng.setColumnWidth(char256);
//	}
//	
//	/**
//	 * Sets Row Height in pixel. 
//	 * @param sheet
//	 * @param row
//	 * @param px row height in pixel 
//	 */
//	public static void setRowHeight(XSheet sheet, int row, int px) {
//		final int points = Utils.pxToPoint(px);
//		final XRange rng = Utils.getRange(sheet, row, -1);
//		rng.setRowHeight(points);
//	}
//	
//	public static void moveRows(XSheet sheet, int tRow, int bRow, int nRow) {
//		final int maxcol = ((XBook)sheet.getWorkbook()).getSpreadsheetVersion().getLastColumnIndex();
//		final XRange rng = Utils.getRange(sheet, tRow, 0, bRow, maxcol);
//		rng.move(nRow, 0);
//	}
//
//	public static void moveColumns(XSheet sheet, int lCol, int rCol, int nCol) {
//		final int maxrow = ((XBook)sheet.getWorkbook()).getSpreadsheetVersion().getLastRowIndex();
//		final XRange rng = Utils.getRange(sheet, 0, lCol, maxrow, rCol);
//		rng.move(0, nCol);
//	}
//	
//	public static void moveCells(XSheet sheet, int tRow, int lCol, int bRow, int rCol, int nRow, int nCol) {
//		final XRange rng = Utils.getRange(sheet, tRow, lCol, bRow, rCol);
//		rng.move(nRow, nCol);
//	}
//	
//	public static void fillRows(XSheet sheet, int srctRow, int srcbRow, int dsttRow, int dstbRow) {
//		final int maxcol = ((XBook)sheet.getWorkbook()).getSpreadsheetVersion().getLastColumnIndex();
//		fillRows(sheet, srctRow, 0, srcbRow, maxcol, dsttRow, dstbRow);
//	}
//	
//	private static void fillRows(XSheet sheet, int srctRow, int srclCol, int srcbRow, int srcrCol, int dsttRow, int dstbRow) {
//		if (srctRow == dsttRow && srcbRow > dstbRow) { //if remove bottom
//			final XRange rng = Utils.getRange(sheet, dstbRow+1, srclCol, srcbRow, srcrCol);
//			rng.clearContents();
//		} else { //fill
//			final XRange srcRange = Utils.getRange(sheet, srctRow, srclCol, srcbRow, srcrCol);
//			final XRange dstRange = Utils.getRange(sheet, dsttRow, srclCol, dstbRow, srcrCol); 
//			srcRange.autoFill(dstRange, XRange.FILL_DEFAULT);
//		}
//	}
//	
//	public static void fillColumns(XSheet sheet, int srclCol, int srcrCol, int dstlCol, int dstrCol) {
//		final int maxrow = ((XBook)sheet.getWorkbook()).getSpreadsheetVersion().getLastRowIndex();
//		fillColumns(sheet, 0, srclCol, maxrow, srcrCol, dstlCol, dstrCol);
//	}
//	
//	private static void fillColumns(XSheet sheet, int srctRow, int srclCol, int srcbRow, int srcrCol, int dstlCol, int dstrCol) {
//		if (srclCol == dstlCol && srcrCol > dstrCol) { //if remove right
//			final XRange rng = Utils.getRange(sheet, srctRow, dstrCol+1, srcbRow, srcrCol);
//			rng.clearContents();
//		} else { //fill
//			final XRange srcRange = Utils.getRange(sheet, srctRow, srclCol, srcbRow, srcrCol);
//			final XRange dstRange = Utils.getRange(sheet, srctRow, dstlCol, srcbRow, dstrCol); 
//			srcRange.autoFill(dstRange, XRange.FILL_DEFAULT);
//		}
//	}
//	
//	public static void fillCells(XSheet sheet, int srctRow, int srclCol, int srcbRow, int srcrCol, int dsttRow, int dstlCol, int dstbRow, int dstrCol) {
//		if (srctRow == dsttRow && srcbRow == dstbRow) { //fillColumns
//			fillColumns(sheet, srctRow, srclCol, srcbRow, srcrCol, dstlCol, dstrCol);
//		} else if (srclCol == dstlCol && srcrCol == dstrCol) { //fillRows
//			fillRows(sheet, srctRow, srclCol, srcbRow, srcrCol, dsttRow, dstbRow);
//		}
//	}
//
//	public static boolean setEditTextWithValidation(Spreadsheet ss, XSheet sheet, int row, int col, String txt, EventListener callback) {
//		ValidationHelper helper = new ValidationHelper(ss);
//		if (helper.validate(sheet, row, col, txt, callback)) {
//			setEditText(sheet, row, col, txt);
//			return true;
//		}
//		return false;
//	}
}
