/* StyleUtil.java

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

import org.zkoss.zss.model.*;
import org.zkoss.zss.model.SBorder.BorderType;
import org.zkoss.zss.model.SFill.FillPattern;
import org.zkoss.zss.model.impl.AbstractSheetAdv;
import org.zkoss.zss.model.impl.AbstractTableAdv;
import org.zkoss.zss.model.impl.RichTextImpl;
import org.zkoss.zss.model.util.CellStyleMatcher;
import org.zkoss.zss.model.util.FontMatcher;
/**
 * A utility class to help spreadsheet set style of a cell
 * @author Dennis.Chen
 * @since 3.5.0
 */
public class StyleUtil {
//	private static final Log log = Log.lookup(NStyles.class);

	public static final short BORDER_EDGE_BOTTOM = 0x01;
	public static final short BORDER_EDGE_RIGHT = 0x02;
	public static final short BORDER_EDGE_TOP = 0x04;
	public static final short BORDER_EDGE_LEFT = 0x08;
	public static final short BORDER_EDGE_ALL = BORDER_EDGE_BOTTOM | BORDER_EDGE_RIGHT | BORDER_EDGE_TOP | BORDER_EDGE_LEFT;

	public static SCellStyle cloneCellStyle(SCell cell) {
		final SCellStyle destination = cell.getSheet().getBook().createCellStyle(cell.getCellStyle(), true);
		return destination;
	}

	public static SCellStyle cloneCellStyle(SBook book,SCellStyle style) {
		final SCellStyle destination = book.createCellStyle(style, true);
		return destination;
	}
	
	public static void setFontColor(SBook book,CellStyleHolder holder, String color/*,HashMap<Integer,NCellStyle> cache*/){
		final SCellStyle orgStyle = holder.getCellStyle();
		SFont orgFont = orgStyle.getFont();
		final SColor orgColor = orgFont.getColor();
		final SColor newColor = book.createColor(color);
		if (orgColor == newColor || orgColor != null && orgColor.equals(newColor)) {
			return;
		}

//		NCellStyle hitStyle = cache==null?null:cache.get((int)orgStyle.getIndex());
//		if(hitStyle!=null){
//			cell.setCellStyle(hitStyle);
//			return;
//		}

		FontMatcher fontmatcher = new FontMatcher(orgFont);
		fontmatcher.setColor(color);

		SFont font = book.searchFont(fontmatcher);


		SCellStyle style = null;
		if(font!=null){//search it since we have existed font
			CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
			matcher.setFont(font);
			style = book.searchCellStyle(matcher);
		}else{
			font = book.createFont(orgFont,true);
			font.setColor(newColor);
		}

		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setFont(font);
		}
		holder.setCellStyle(style);

//		if(cache!=null){
//			cache.put((int)orgStyle.getIndex(), style);
//		}
	}

	public static void setFillColor(SBook book,CellStyleHolder holder, String htmlColor){
		final SCellStyle orgStyle = holder.getCellStyle();
		final SColor orgColor = orgStyle.getFillColor();
		final SColor newColor = book.createColor(htmlColor);
		if (orgColor == newColor || orgColor != null  && orgColor.equals(newColor)) { //no change, skip
			return;
		}

		CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
		matcher.setBackColor(htmlColor);

		SCellStyle style = book.searchCellStyle(matcher);
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setFillColor(newColor);
		}
		holder.setCellStyle(style);

	}
	
	//ZSS-857
	public static void setBackColor(SBook book,CellStyleHolder holder, String htmlColor){
		final SCellStyle orgStyle = holder.getCellStyle();
		final SColor orgColor = orgStyle.getBackColor();
		final SColor newColor = book.createColor(htmlColor);
		if (orgColor == newColor || orgColor != null  && orgColor.equals(newColor)) { //no change, skip
			return;
		}

		SFill.FillPattern pattern = orgStyle.getFillPattern();
		if (pattern == FillPattern.NONE && htmlColor != null) {
			pattern = FillPattern.SOLID;
		}

		CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
		matcher.setBackColor(htmlColor);
		matcher.setFillPattern(pattern);

		SCellStyle style = book.searchCellStyle(matcher);
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setBackColor(newColor);
			style.setFillPattern(pattern);
		}
		holder.setCellStyle(style);
	}

	public static void setFillOptions(SBook book,CellStyleHolder holder, String bgColor, String fillColor, FillPattern pattern){
		final SCellStyle orgStyle = holder.getCellStyle();
		final SColor orgBackColor = orgStyle.getBackColor();
		final SColor newBackColor = book.createColor(bgColor);
		final SColor orgFillColor = orgStyle.getFillColor();
		final SColor newFillColor = book.createColor(fillColor);
		final FillPattern orgPattern = orgStyle.getFillPattern();

		if ((orgBackColor == newBackColor || (orgBackColor != null && orgBackColor.equals(newBackColor))) &&
				(orgFillColor == newFillColor || (orgFillColor != null  && orgFillColor.equals(newFillColor))) &&
					orgPattern == pattern) {
			return;
		}

		CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
		matcher.setBackColor(bgColor);
		matcher.setFillColor(fillColor);
		matcher.setFillPattern(pattern);

		SCellStyle style = book.searchCellStyle(matcher);
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setBackColor(newBackColor);
			style.setFillColor(newFillColor);
			style.setFillPattern(pattern);
		}
		holder.setCellStyle(style);
	}

	public static void setTextWrap(SBook book,CellStyleHolder holder,boolean wrap){
		final SCellStyle orgStyle = holder.getCellStyle();
		final boolean textWrap = orgStyle.isWrapText();
		if (wrap == textWrap) { //no change, skip
			return;
		}

		CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
		matcher.setWrapText(wrap);
		SCellStyle style = book.searchCellStyle(matcher);
		if(style==null){
			style  = cloneCellStyle(book,orgStyle);
			style.setWrapText(wrap);
		}
		holder.setCellStyle(style);
	}

	public static void setFontHeightPoints(SBook book,CellStyleHolder holder,int fontHeightPoints){
		final SCellStyle orgStyle = holder.getCellStyle();
		SFont orgFont = orgStyle.getFont();

		final int orgSize = orgFont.getHeightPoints();
		if (orgSize == fontHeightPoints) { //no change, skip
			return;
		}

		FontMatcher fontmatcher = new FontMatcher(orgFont);
		fontmatcher.setHeightPoints(fontHeightPoints);

		SFont font = book.searchFont(fontmatcher);

		SCellStyle style = null;
		if(font!=null){//search it since we have existed font
			CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
			matcher.setFont(font);
			style = book.searchCellStyle(matcher);
		}else{
			font = book.createFont(orgFont,true);
			font.setHeightPoints(fontHeightPoints);
		}

		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setFont(font);
		}
		holder.setCellStyle(style);
	}

	public static void setFontStrikethrough(SBook book,CellStyleHolder holder, boolean strikeout){
		final SCellStyle orgStyle = holder.getCellStyle();
		SFont orgFont = orgStyle.getFont();

		final boolean orgStrikeout = orgFont.isStrikeout();
		if (orgStrikeout == strikeout) { //no change, skip
			return;
		}

		FontMatcher fontmatcher = new FontMatcher(orgFont);
		fontmatcher.setStrikeout(strikeout);

		SFont font = book.searchFont(fontmatcher);

		SCellStyle style = null;
		if(font!=null){//search it since we have existed font
			CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
			matcher.setFont(font);
			style = book.searchCellStyle(matcher);
		}else{
			font = book.createFont(orgFont,true);
			font.setStrikeout(strikeout);
		}

		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setFont(font);
		}
		holder.setCellStyle(style);

	}

	public static void setFontName(SBook book,CellStyleHolder holder,String name){
		final SCellStyle orgStyle = holder.getCellStyle();
		SFont orgFont = orgStyle.getFont();

		final String orgName = orgFont.getName();
		if (orgName.equals(name)) { //no change, skip
			return;
		}

		FontMatcher fontmatcher = new FontMatcher(orgFont);
		fontmatcher.setName(name);

		SFont font = book.searchFont(fontmatcher);

		SCellStyle style = null;
		if(font!=null){//search it since we have existed font
			CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
			matcher.setFont(font);
			style = book.searchCellStyle(matcher);
		}else{
			font = book.createFont(orgFont,true);
			font.setName(name);
		}

		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setFont(font);
		}
		holder.setCellStyle(style);

	}
	
	public static void setBorder(SBook book,CellStyleHolder holder, String color, SBorder.BorderType linestyle){
		setBorder(book,holder, color, linestyle, BORDER_EDGE_ALL);
	}
	
	public static void setBorderTop(SBook book,CellStyleHolder holder,String color, SBorder.BorderType linestyle){
		setBorder(book,holder, color, linestyle, BORDER_EDGE_TOP);
	}
	public static void setBorderLeft(SBook book,CellStyleHolder holder,String color, SBorder.BorderType linestyle){
		setBorder(book,holder, color, linestyle, BORDER_EDGE_LEFT);
	}
	public static void setBorderBottom(SBook book,CellStyleHolder holder,String color, SBorder.BorderType linestyle){
		setBorder(book,holder, color, linestyle, BORDER_EDGE_BOTTOM);
	}
	public static void setBorderRight(SBook book,CellStyleHolder holder,String color, SBorder.BorderType linestyle){
		setBorder(book,holder, color, linestyle, BORDER_EDGE_RIGHT);
	}
	
	public static void setBorder(SBook book,CellStyleHolder holder, String htmlColor, SBorder.BorderType lineStyle, short at){
		
		final SCellStyle orgStyle = holder.getCellStyle();
		//ZSS-464 try to search existed matched style
		SCellStyle style = null;
		final SColor color = book.createColor(htmlColor);
		boolean hasBorder = lineStyle != SBorder.BorderType.NONE;
		if(htmlColor!=null){
			CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
			if((at & BORDER_EDGE_LEFT)!=0) {
				if(hasBorder)
					matcher.setBorderLeftColor(htmlColor);
				else
					matcher.removeBorderLeftColor();
				
				matcher.setBorderLeft(lineStyle);
			}
			if((at & BORDER_EDGE_TOP)!=0){
				if(hasBorder) 
					matcher.setBorderTopColor(htmlColor);
				else
					matcher.removeBorderTopColor();
				
				matcher.setBorderTop(lineStyle);
			}
			if((at & BORDER_EDGE_RIGHT)!=0){
				if(hasBorder)
					matcher.setBorderRightColor(htmlColor);
				else
					matcher.removeBorderRightColor();
				
				matcher.setBorderRight(lineStyle);
			}
			if((at & BORDER_EDGE_BOTTOM)!=0){
				if(hasBorder)
					matcher.setBorderBottomColor(htmlColor);
				else
					matcher.removeBorderBottomColor();
				
				matcher.setBorderBottom(lineStyle);
			}
			style = book.searchCellStyle(matcher);
		}
		
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			if((at & BORDER_EDGE_LEFT)!=0) {
				if(hasBorder)
					style.setBorderLeftColor(color);
				style.setBorderLeft(lineStyle);
			}
			if((at & BORDER_EDGE_TOP)!=0){
				if(hasBorder)
					style.setBorderTopColor(color);
				style.setBorderTop(lineStyle);
			}
			if((at & BORDER_EDGE_RIGHT)!=0){
				if(hasBorder)
					style.setBorderRightColor(color);
				style.setBorderRight(lineStyle);
			}
			if((at & BORDER_EDGE_BOTTOM)!=0){
				if(hasBorder)
					style.setBorderBottomColor(color);
				style.setBorderBottom(lineStyle);
			}
		}
		
		holder.setCellStyle(style);
	}
	
//	private static void debugStyle(String msg,int row, int col, Workbook book, NCellStyle style){
//		StringBuilder sb = new StringBuilder(msg);
//		sb.append("[").append(Ranges.getCellRefString(row, col)).append("]");
//		sb.append("Top:[").append(style.getBorderTop()).append(":").append(BookHelper.colorToBorderHTML(book,style.getTopBorderColorColor())).append("]");
//		sb.append("Left:[").append(style.getBorderLeft()).append(":").append(BookHelper.colorToBorderHTML(book,style.getLeftBorderColorColor())).append("]");
//		sb.append("Bottom:[").append(style.getBorderBottom()).append(":").append(BookHelper.colorToBorderHTML(book,style.getBottomBorderColorColor())).append("]");
//		sb.append("Right:[").append(style.getBorderRight()).append(":").append(BookHelper.colorToBorderHTML(book,style.getRightBorderColorColor())).append("]");
//		System.out.println(">>"+sb.toString());
//	}
	
	public static void setFontBoldWeight(SBook book,CellStyleHolder holder,SFont.Boldweight boldWeight){
		final SCellStyle orgStyle = holder.getCellStyle();
		SFont orgFont = orgStyle.getFont();
		
		final SFont.Boldweight orgBoldWeight = orgFont.getBoldweight();
		if (orgBoldWeight.equals(boldWeight)) { //no change, skip
			return;
		}
		
		FontMatcher fontmatcher = new FontMatcher(orgFont);
		fontmatcher.setBoldweight(boldWeight);
		
		SFont font = book.searchFont(fontmatcher);
		
		SCellStyle style = null;
		if(font!=null){//search it since we have existed font
			CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
			matcher.setFont(font);
			style = book.searchCellStyle(matcher);
		}else{
			font = book.createFont(orgFont,true);
			font.setBoldweight(boldWeight);
		}
		
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setFont(font);
		}
		holder.setCellStyle(style);
	}
	
	public static void setFontItalic(SBook book,CellStyleHolder holder, boolean italic) {
		final SCellStyle orgStyle = holder.getCellStyle();
		SFont orgFont = orgStyle.getFont();
		
		final boolean orgItalic = orgFont.isItalic();
		if (orgItalic == italic) { //no change, skip
			return;
		}

		FontMatcher fontmatcher = new FontMatcher(orgFont);
		fontmatcher.setItalic(italic);
		
		SFont font = book.searchFont(fontmatcher);
		
		SCellStyle style = null;
		if(font!=null){//search it since we have existed font
			CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
			matcher.setFont(font);
			style = book.searchCellStyle(matcher);
		}else{
			font = book.createFont(orgFont,true);
			font.setItalic(italic);
		}
		
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setFont(font);
		}
		holder.setCellStyle(style);
		
	}
	
	public static void setFontUnderline(SBook book,CellStyleHolder holder, SFont.Underline underline){
		final SCellStyle orgStyle = holder.getCellStyle();
		SFont orgFont = orgStyle.getFont();
		
		final SFont.Underline orgUnderline = orgFont.getUnderline();
		if (orgUnderline.equals(underline)) { //no change, skip
			return;
		}
		
		FontMatcher fontmatcher = new FontMatcher(orgFont);
		fontmatcher.setUnderline(underline);
		
		SFont font = book.searchFont(fontmatcher);
		
		SCellStyle style = null;
		if(font!=null){//search it since we have existed font
			CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
			matcher.setFont(font);
			style = book.searchCellStyle(matcher);
		}else{
			font = book.createFont(orgFont,true);
			font.setUnderline(underline);
		}
		
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setFont(font);
		}
		holder.setCellStyle(style);
	}
	
	public static void setTextHAlign(SBook book,CellStyleHolder holder, SCellStyle.Alignment align){
		final SCellStyle orgStyle = holder.getCellStyle();
		final SCellStyle.Alignment orgAlign = orgStyle.getAlignment();
		if (align.equals(orgAlign)) { //no change, skip
			return;
		}
		
		CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
		matcher.setAlignment(align);
		SCellStyle style = book.searchCellStyle(matcher);
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setAlignment(align);
		}
		holder.setCellStyle(style);
	}


	public static void setLocked(SBook book, CellStyleHolder holder, boolean locked) {
		final SCellStyle orgStyle = holder.getCellStyle();
		final boolean orgStyleLocked = orgStyle.isLocked();
		if (locked == orgStyleLocked) { //no change, skip
			return;
		}

		CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
		matcher.setLocked(locked);
		SCellStyle style = book.searchCellStyle(matcher);
		if (style == null) {
			style = cloneCellStyle(book, orgStyle);
			style.setLocked(locked);
		}
		holder.setCellStyle(style);
	}

	public static void setTextVAlign(SBook book,CellStyleHolder holder, SCellStyle.VerticalAlignment valign){
		final SCellStyle orgStyle = holder.getCellStyle();
		final SCellStyle.VerticalAlignment orgValign = orgStyle.getVerticalAlignment();
		if (valign.equals(orgValign)) { //no change, skip
			return;
		}

		CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
		matcher.setVerticalAlignment(valign);
		SCellStyle style = book.searchCellStyle(matcher);
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setVerticalAlignment(valign);
		}
		holder.setCellStyle(style);

	}
	
	public static void setDataFormat(SBook book,CellStyleHolder holder, String format) {
		final SCellStyle orgStyle = holder.getCellStyle();
		final String orgFormat = orgStyle.getDataFormat();
		if (format == orgFormat || (format!=null && format.equals(orgFormat))) { //no change, skip
			return;
		}

		CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);

		matcher.setDataFormat(format);
		SCellStyle style = book.searchCellStyle(matcher);
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setDataFormat(format);
		}
		holder.setCellStyle(style);
	}

	//ZSS-748
	public static void setFontTypeOffset(SBook book,CellStyleHolder holder, SFont.TypeOffset offset){
		final SCellStyle orgStyle = holder.getCellStyle();
		SFont orgFont = orgStyle.getFont();
		
		final SFont.TypeOffset orgOffset = orgFont.getTypeOffset();
		if (orgOffset.equals(offset)) { //no change, skip
			return;
		}
		
		FontMatcher fontmatcher = new FontMatcher(orgFont);
		fontmatcher.setTypeOffset(offset);
		
		SFont font = book.searchFont(fontmatcher);
		
		SCellStyle style = null;
		if(font!=null){//search it since we have existed font
			CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
			matcher.setFont(font);
			style = book.searchCellStyle(matcher);
		}else{
			font = book.createFont(orgFont,true);
			font.setTypeOffset(offset);
		}
		
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setFont(font);
		}
		holder.setCellStyle(style);
	}
	
	//ZSS-752
	public static boolean setRichTextFontTypeOffset(SBook book, SCell cell, SFont.TypeOffset offset) {
		final Object value = cell.isRichTextValue() ? cell.getValue() : null;
		if (!(value instanceof SRichText)) return false;
		
		final SRichText text = (SRichText) value;
		SRichText newText = new RichTextImpl();
		boolean modified = false;
		for (SRichText.Segment seg : text.getSegments()) {
			SFont font = seg.getFont();
			
			final SFont.TypeOffset orgOffset = font.getTypeOffset();
			if (!orgOffset.equals(offset)) { //changed, find the font
				
				// locate proper font
				FontMatcher fontmatcher = new FontMatcher(font);
				fontmatcher.setTypeOffset(offset);
				
				font = book.searchFont(fontmatcher);
				if (font == null) {
					font = book.createFont(font,true);
					font.setTypeOffset(offset);
				}
				modified = true;
			}
			
			newText.addSegment(seg.getText(), font);
		}
		
		if (modified) {
			cell.setValue(newText, null, true);
			return true;
		}
		
		return false;
	}
	
	//ZSS-752
	public static boolean setRichTextFontBoldweight(SBook book, SCell cell, SFont.Boldweight bold) {
		final Object value = cell.isRichTextValue() ? cell.getValue() : null;
		if (!(value instanceof SRichText)) return false;
		
		final SRichText text = (SRichText) value;
		SRichText newText = new RichTextImpl();
		boolean modified = false;
		for (SRichText.Segment seg : text.getSegments()) {
			SFont font = seg.getFont();
			
			final SFont.Boldweight orgBold = font.getBoldweight();
			if (!orgBold.equals(bold)) { //changed, find the font
				
				// locate proper font
				FontMatcher fontmatcher = new FontMatcher(font);
				fontmatcher.setBoldweight(bold);
				
				font = book.searchFont(fontmatcher);
				if (font == null) {
					font = book.createFont(font,true);
					font.setBoldweight(bold);
				}
				modified = true;
			}
			
			newText.addSegment(seg.getText(), font);
		}
		
		if (modified) {
			cell.setValue(newText, null, true);
			return true;
		}
		
		return false;
	}
	
	//ZSS-752
	public static boolean setRichTextFontItalic(SBook book, SCell cell, boolean italic) {
		final Object value = cell.isRichTextValue() ? cell.getValue() : null;
		if (!(value instanceof SRichText)) return false;
		
		final SRichText text = (SRichText) value;
		SRichText newText = new RichTextImpl();
		boolean modified = false;
		for (SRichText.Segment seg : text.getSegments()) {
			SFont font = seg.getFont();
			
			final boolean orgItalic = font.isItalic();
			if (orgItalic != italic) { //changed find the font

				// locate proper font
				FontMatcher fontmatcher = new FontMatcher(font);
				fontmatcher.setItalic(italic);
				
				font = book.searchFont(fontmatcher);
				if (font == null) {
					font = book.createFont(font,true);
					font.setItalic(italic);
				}
				modified = true;
			}
			
			newText.addSegment(seg.getText(), font);
		}
		
		if (modified) {
			cell.setValue(newText, null, true);
			return true;
		}
		
		return false;
	}

	//ZSS-752
	public static boolean setRichTextFontUnderline(SBook book, SCell cell, SFont.Underline underline) {
		final Object value = cell.isRichTextValue() ? cell.getValue() : null;
		if (!(value instanceof SRichText)) return false;
		
		final SRichText text = (SRichText) value;
		SRichText newText = new RichTextImpl();
		boolean modified = false;
		for (SRichText.Segment seg : text.getSegments()) {
			SFont font = seg.getFont();
			
			final SFont.Underline orgUnderline = font.getUnderline();
			if (!orgUnderline.equals(underline)) { //changed, find the font

				// locate proper font
				FontMatcher fontmatcher = new FontMatcher(font);
				fontmatcher.setUnderline(underline);
				
				font = book.searchFont(fontmatcher);
				if (font == null) {
					font = book.createFont(font,true);
					font.setUnderline(underline);
				}
				modified = true;
			}
				
			newText.addSegment(seg.getText(), font);
		}
		
		if (modified) {
			cell.setValue(newText, null, true);
			return true;
		}
		
		return false;
	}

	//ZSS-752
	public static boolean setRichTextFontName(SBook book, SCell cell, String name) {
		final Object value = cell.isRichTextValue() ? cell.getValue() : null;
		if (!(value instanceof SRichText)) return false;
		
		final SRichText text = (SRichText) value;
		SRichText newText = new RichTextImpl();
		boolean modified = false;
		for (SRichText.Segment seg : text.getSegments()) {
			SFont font = seg.getFont();
			
			final String orgName = font.getName();
			if (!orgName.equals(name)) { //changed, find the font

				// locate proper font
				FontMatcher fontmatcher = new FontMatcher(font);
				fontmatcher.setName(name);
				
				font = book.searchFont(fontmatcher);
				if (font == null) {
					font = book.createFont(font,true);
					font.setName(name);
				}
				modified = true;
			}
			
			newText.addSegment(seg.getText(), font);
		}
		
		if (modified) {
			cell.setValue(newText, null, true);
			return true;
		}
		
		return false;
	}

	//ZSS-752
	public static boolean setRichTextFontHeightPoints(SBook book, SCell cell, int heightPoints) {
		final Object value = cell.isRichTextValue() ? cell.getValue() : null;
		if (!(value instanceof SRichText)) return false;
		
		final SRichText text = (SRichText) value;
		SRichText newText = new RichTextImpl();
		boolean modified = false;
		for (SRichText.Segment seg : text.getSegments()) {
			SFont font = seg.getFont();
			
			final int orgHeightPoints = font.getHeightPoints();
			if (orgHeightPoints != heightPoints) { //changed, find the font

				// locate proper font
				FontMatcher fontmatcher = new FontMatcher(font);
				fontmatcher.setHeightPoints(heightPoints);
				
				font = book.searchFont(fontmatcher);
				if (font == null) {
					font = book.createFont(font,true);
					font.setHeightPoints(heightPoints);
				}
				modified = true;
			}
			
			newText.addSegment(seg.getText(), font);
		}
		
		if (modified) {
			cell.setValue(newText, null, true);
			return true;
		}
		
		return false;
	}

	//ZSS-752
	public static boolean setRichTextFontStrikeout(SBook book, SCell cell, boolean strike) {
		final Object value = cell.isRichTextValue() ? cell.getValue() : null;
		if (!(value instanceof SRichText)) return false;
		
		final SRichText text = (SRichText) value;
		SRichText newText = new RichTextImpl();
		boolean modified = false;
		for (SRichText.Segment seg : text.getSegments()) {
			SFont font = seg.getFont();
			
			final boolean orgStrike = font.isStrikeout();
			if (orgStrike != strike) { //change, find the font

				// locate proper font
				FontMatcher fontmatcher = new FontMatcher(font);
				fontmatcher.setStrikeout(strike);
				
				font = book.searchFont(fontmatcher);
				if (font == null) {
					font = book.createFont(font,true);
					font.setStrikeout(strike);
				}
				modified = true;
			}
			
			newText.addSegment(seg.getText(), font);
		}
		
		if (modified) {
			cell.setValue(newText, null, true);
			return true;
		}
		
		return false;
	}

	//ZSS-752
	public static boolean setRichTextFontColor(SBook book, SCell cell, String htmlColor) {
		final Object value = cell.isRichTextValue() ? cell.getValue() : null;
		if (!(value instanceof SRichText)) return false;
		
		final SColor newColor = book.createColor(htmlColor);
		final SRichText text = (SRichText) value;
		SRichText newText = new RichTextImpl();
		boolean modified = false;
		for (SRichText.Segment seg : text.getSegments()) {
			SFont font = seg.getFont();
			
			final SColor orgColor = font.getColor();
			if (orgColor != newColor && (orgColor == null || !orgColor.equals(newColor))) { //changed, find the font
				// locate proper font
				FontMatcher fontmatcher = new FontMatcher(font);
				fontmatcher.setColor(htmlColor);
				
				font = book.searchFont(fontmatcher);
				if (font == null) {
					font = book.createFont(font,true);
					font.setColor(newColor);
				}
				modified = true;
			}
			
			newText.addSegment(seg.getText(), font);
		}
		
		if (modified) {
			cell.setValue(newText, null, true);
			return true;
		}
		
		return false;
	}
	
	//ZSS-918
	public static void setTextRotation(SBook book,CellStyleHolder holder, int rotation){
		final SCellStyle orgStyle = holder.getCellStyle();
		final int rot = orgStyle.getRotation();
		if (rot == rotation) { //no change, skip
			return;
		}
		
		CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
		matcher.setRotation(rotation);
		SCellStyle style = book.searchCellStyle(matcher);
		if(style==null){
			style = cloneCellStyle(book,orgStyle);
			style.setRotation(rotation);
		}
		holder.setCellStyle(style);
	}
	
	//ZSS-915
	public static void setTextIndentionOffset(SBook book,CellStyleHolder holder, int offset){
		if (offset == 0) { //no change, skip
			return;
		}
		setTextIndention(book, holder, holder.getCellStyle().getIndention() + offset);
	}

	//ZSS-915
	public static void setTextIndention(SBook book,CellStyleHolder holder, int indent) {
		final SCellStyle orgStyle = holder.getCellStyle();
		int ind = orgStyle.getIndention();
		if(indent < 0)
			indent = 0;
		
		if(ind == indent)
			return;
		
		CellStyleMatcher matcher = new CellStyleMatcher(orgStyle);
		matcher.setIndention(indent);
		SCellStyle style = book.searchCellStyle(matcher);
		if(style==null){
			style = cloneCellStyle(book, orgStyle);
			style.setIndention(indent);
		}
		holder.setCellStyle(style);
	}

	
	//ZSS-977
	//@since 3.8.0
	public static SFont getFontStyle(SBook book, SCellStyle cellStyle, SCellStyle tbCellStyle) {
		SFont font = cellStyle.getFont();
		
		if (tbCellStyle != null && book.getDefaultFont().equals(font)) {
			final SFont font0 = tbCellStyle.getFont();
			if (font0 != null) {
				font = font0;
			}
		}
		
		return font;
	}
	
	//ZSS-977
	//@since 3.8.0
	public static SCellStyle getFillStyle(SCellStyle cellStyle, SCellStyle tbStyle) {
		return cellStyle.getFillPattern() != SFill.FillPattern.NONE ?
			cellStyle : tbStyle;
	}
	
	//ZSS-977
	//@since 3.8.0
	public static SCellStyle getLeftStyle(SCellStyle cellStyle, SCellStyle tbStyle) {
		return tbStyle == null || cellStyle.getBorderLeft() != BorderType.NONE ? cellStyle : tbStyle; 
	}
	//ZSS-977
	//@since 3.8.0
	public static SCellStyle getTopStyle(SCellStyle cellStyle, SCellStyle tbStyle) {
		return tbStyle == null || cellStyle.getBorderTop() != BorderType.NONE ? cellStyle : tbStyle;
	}
	//ZSS-977
	//@since 3.8.0
	public static SCellStyle getRightStyle(SCellStyle cellStyle, SCellStyle tbStyle) {
		return tbStyle == null || cellStyle.getBorderRight() != BorderType.NONE ? cellStyle : tbStyle;
	}
	//ZSS-977
	//@since 3.8.0
	public static SCellStyle getBottomStyle(SCellStyle cellStyle, SCellStyle tbStyle) {
		return tbStyle == null || cellStyle.getBorderBottom() != BorderType.NONE ? cellStyle : tbStyle;
	}

	//ZSS-1002
	//@since 3.8.0
	public static SCellStyle prepareStyle(SCell srcCell) {
		final int row = srcCell.getRowIndex();
		final int col = srcCell.getColumnIndex();
		SSheet sheet = srcCell.getSheet();
		STable table = ((AbstractSheetAdv)sheet).getTableByRowCol(row, col);
		SCellStyle tbStyle = table != null ? ((AbstractTableAdv)table).getCellStyle(row, col) : null;
		SCellStyle cellStyle = srcCell.getCellStyle();
		if (tbStyle == null) return cellStyle;

		SBook book = sheet.getBook();
		SFont font = StyleUtil.getFontStyle(book, cellStyle, tbStyle);
		SCellStyle fillStyle = getFillStyle(cellStyle, tbStyle);
		SCellStyle leftStyle = getLeftStyle(cellStyle, tbStyle);
		SCellStyle topStyle = getTopStyle(cellStyle, tbStyle);
		SCellStyle rightStyle = getRightStyle(cellStyle, tbStyle);
		SCellStyle bottomStyle = getBottomStyle(cellStyle, tbStyle);
		
		CellStyleMatcher matcher = new CellStyleMatcher(cellStyle);
		matcher.setBackColor(fillStyle.getBackColor().getHtmlColor());
		matcher.setFillColor(fillStyle.getFillColor().getHtmlColor());
		matcher.setFillPattern(fillStyle.getFillPattern());
		matcher.setFont(font);
		matcher.setBorderBottom(bottomStyle.getBorderBottom());
		matcher.setBorderBottomColor(bottomStyle.getBorderBottomColor().getHtmlColor());
		matcher.setBorderTop(topStyle.getBorderTop());
		matcher.setBorderTopColor(topStyle.getBorderTopColor().getHtmlColor());
		matcher.setBorderLeft(leftStyle.getBorderLeft());
		matcher.setBorderLeftColor(leftStyle.getBorderLeftColor().getHtmlColor());
		matcher.setBorderRight(rightStyle.getBorderRight());
		matcher.setBorderRightColor(rightStyle.getBorderRightColor().getHtmlColor());
	
		SCellStyle style = book.searchCellStyle(matcher);
		if(style==null){
			style = cloneCellStyle(book, cellStyle);
			style.setBackColor(fillStyle.getBackColor());
			style.setFillColor(fillStyle.getFillColor());
			style.setFillPattern(fillStyle.getFillPattern());
			style.setFont(font);
			style.setBorderBottom(bottomStyle.getBorderBottom());
			style.setBorderBottomColor(bottomStyle.getBorderBottomColor());
			style.setBorderTop(topStyle.getBorderTop());
			style.setBorderTopColor(topStyle.getBorderTopColor());
			style.setBorderLeft(leftStyle.getBorderLeft());
			style.setBorderLeftColor(leftStyle.getBorderLeftColor());
			style.setBorderRight(rightStyle.getBorderRight());
			style.setBorderRightColor(rightStyle.getBorderRightColor());
		}

		return style;
	}
}
