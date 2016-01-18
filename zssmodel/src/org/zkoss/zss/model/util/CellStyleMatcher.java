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

package org.zkoss.zss.model.util;

import java.util.*;
import java.util.Map.Entry;

import org.zkoss.zss.model.*;


/**
 * This class can be used to match a {@link SCellStyle} with a criteria.
 * @author dennis
 * @since 3.5.0
 */
public class CellStyleMatcher {

	
	Map<Property,Object> _criteria = new LinkedHashMap<Property, Object>();
	
	private enum Property {
		Alignment,
		VerticalAlignment,
		
		BorderBottom,
		BorderRight,
		BorderTop,
		BorderLeft,
		BorderBottomColor,
		BorderRightColor,
		BorderTopColor,
		BorderLeftColor,
		
		DataFormat,
		
		FillColor,
		BackColor, //ZSS-857
//		FillForegroundColor,
		FillPattern,
		
		FontName,
		FontColor,
		FontBoldweight,
		FontHeightPoints,
		FontItalic,
		FontStrikeout,
		FontTypeOffset,
		FontUnderline,
		
		Hidden,
		Indention,
		Locked,
		Rotation,

		WrapText;
	}

	
	public CellStyleMatcher(){}
	/**
	 * Create a style matcher with an existing cell style as the criteria 
	 */
	public CellStyleMatcher(SCellStyle criteria){
		setAlignment(criteria.getAlignment());
		setVerticalAlignment(criteria.getVerticalAlignment());
		SBorder.BorderType btype;
		setBorderBottom(btype=criteria.getBorderBottom());
		if(btype!=SBorder.BorderType.NONE){//only compare color when the border is not none
			setBorderBottomColor(criteria.getBorderBottomColor().getHtmlColor());
		}
		setBorderLeft(btype=criteria.getBorderLeft());
		if(btype!=SBorder.BorderType.NONE){
			setBorderLeftColor(criteria.getBorderLeftColor().getHtmlColor());
		}
		setBorderRight(btype=criteria.getBorderRight());
		if(btype!=SBorder.BorderType.NONE){
			setBorderRightColor(criteria.getBorderRightColor().getHtmlColor());
		}
		setBorderTop(btype=criteria.getBorderTop());
		if(btype!=SBorder.BorderType.NONE){
			setBorderTopColor(criteria.getBorderTopColor().getHtmlColor());
		}
		
		setDataFormat(criteria.getDataFormat());
		
		setFillColor(criteria.getFillColor().getHtmlColor());
		setBackColor(criteria.getBackColor().getHtmlColor()); //ZSS-857
//		setFillForegroundColor(BookHelper.colorToForegroundHTML(book,criteria.getFillForegroundColorColor()));
		setFillPattern(criteria.getFillPattern());
		
		setFont(criteria.getFont());
		
		
		setHidden(criteria.isHidden());
		setIndention(criteria.getIndention());
		
		setLocked(criteria.isLocked());
		
		setRotation(criteria.getRotation());
		
		setWrapText(criteria.isWrapText());
	}
	
	public void setDataFormat(String fmt) {
		_criteria.put(Property.DataFormat, fmt);
	}

	public void setFontColor(String color) {
		_criteria.put(Property.FontColor, color);
	}
	
	public void setFontName(String name) {
		_criteria.put(Property.FontName, name);
	}
	
	public void setFontBoldweight(SFont.Boldweight boldweight) {
		_criteria.put(Property.FontBoldweight, boldweight);
	}
	
	public void setFontHeightPoints(int height) {
		_criteria.put(Property.FontHeightPoints, height);
	}
	
	public void setFontItalic(boolean italic) {
		_criteria.put(Property.FontItalic, italic);
	}
	
	public void setFontStrikeout(boolean strikeout) {
		_criteria.put(Property.FontStrikeout, strikeout);
	}
	
	public void setFontTypeOffset(SFont.TypeOffset typeOffset) {
		_criteria.put(Property.FontTypeOffset, typeOffset);
	}
	
	public void setFontUnderline(SFont.Underline underline) {
		_criteria.put(Property.FontUnderline, underline);
	}
	
	public void setFont(SFont font) {
		setFontColor(font.getColor().getHtmlColor());
		setFontName(font.getName());
		setFontBoldweight(font.getBoldweight());
		setFontHeightPoints(font.getHeightPoints());
		setFontItalic(font.isItalic());
		setFontStrikeout(font.isStrikeout());
		setFontTypeOffset(font.getTypeOffset());
		setFontUnderline(font.getUnderline());
	}
	public void removeFont() {
		removeFontColor();
		removeFontName();
		removeFontBoldweight();
		removeFontHeightPoints();
		removeFontItalic();
		removeFontStrikeout();
		removeFontTypeOffset();
		removeFontUnderline();
	}

	public void setHidden(boolean hidden) {
		_criteria.put(Property.Hidden, hidden);
	}

	public void setLocked(boolean locked) {
		_criteria.put(Property.Locked, locked);
	}

	public void setAlignment(SCellStyle.Alignment align) {
		_criteria.put(Property.Alignment, align);
	}

	public void setWrapText(boolean wrapped) {
		_criteria.put(Property.WrapText, wrapped);
	}

	public void setVerticalAlignment(SCellStyle.VerticalAlignment align) {
		_criteria.put(Property.VerticalAlignment, align);
	}

	public void setRotation(int rotation) {
		_criteria.put(Property.Rotation, rotation );
	}

	public void setIndention(int indent) {
		_criteria.put(Property.Indention, indent);
	}

	public void setBorderRight(SBorder.BorderType border) {
		_criteria.put(Property.BorderRight, border);
	}

	public void setBorderTop(SBorder.BorderType border) {
		_criteria.put(Property.BorderTop,border );
	}

	public void setBorderBottom(SBorder.BorderType border) {
		_criteria.put(Property.BorderBottom, border);
	}
	public void setBorderLeft(SBorder.BorderType border) {
		_criteria.put(Property.BorderLeft, border);
	}

	public void setBorderLeftColor(String htmlcolor) {
		_criteria.put(Property.BorderLeftColor,htmlcolor );
	}

	public void setBorderRightColor(String htmlcolor) {
		_criteria.put(Property.BorderRightColor, htmlcolor);
	}

	public void setBorderTopColor(String htmlcolor) {
		_criteria.put(Property.BorderTopColor,htmlcolor);
	}

	public void setBorderBottomColor(String htmlcolor) {
		_criteria.put(Property.BorderBottomColor,htmlcolor );
	}

	public void setFillPattern(SFill.FillPattern fp) {
		_criteria.put(Property.FillPattern,fp );
	}

	public void setFillColor(String htmlcolor) {
		_criteria.put(Property.FillColor, htmlcolor);
	}
	
	//ZSS-857
	public void setBackColor(String htmlcolor) {
		_criteria.put(Property.BackColor, htmlcolor);
	}

	//remove api
	public void removeDataFormat() {
		_criteria.remove(Property.DataFormat);
	}

	public void removeFontColor() {
		_criteria.remove(Property.FontColor);
	}
	public void removeFontName() {
		_criteria.remove(Property.FontName);
	}
	public void removeFontBoldweight() {
		_criteria.remove(Property.FontBoldweight);
	}
	public void removeFontHeightPoints() {
		_criteria.remove(Property.FontHeightPoints);
	}
	public void removeFontItalic() {
		_criteria.remove(Property.FontItalic);
	}
	public void removeFontStrikeout() {
		_criteria.remove(Property.FontStrikeout);
	}
	public void removeFontTypeOffset() {
		_criteria.remove(Property.FontTypeOffset);
	}
	public void removeFontUnderline() {
		_criteria.remove(Property.FontUnderline);
	}

	public void removeHidden() {
		_criteria.remove(Property.Hidden);
	}

	public void removeLocked(){
		_criteria.remove(Property.Locked);
	}

	public void removeAlignment(){
		_criteria.remove(Property.Alignment);
	}

	public void removeWrapText(){
		_criteria.remove(Property.WrapText);
	}

	public void removeVerticalAlignment(){
		_criteria.remove(Property.VerticalAlignment);
	}

	public void removeRotation(){
		_criteria.remove(Property.Rotation);
	}

	public void removeIndention(){
		_criteria.remove(Property.Indention);
	}

	public void removeBorderRight(){
		_criteria.remove(Property.BorderRight);
	}

	public void removeBorderTop(){
		_criteria.remove(Property.BorderTop);
	}

	public void removeBorderBottom(){
		_criteria.remove(Property.BorderBottom);
	}
	public void removeBorderLeft(){
		_criteria.remove(Property.BorderLeft);
	}

	public void removeBorderLeftColor(){
		_criteria.remove(Property.BorderLeftColor);
	}

	public void removeBorderRightColor(){
		_criteria.remove(Property.BorderRightColor);
	}

	public void removeBorderTopColor(){
		_criteria.remove(Property.BorderTopColor);
	}

	public void removeBorderBottomColor(){
		_criteria.remove(Property.BorderBottomColor);
	}

	public void removeFillPattern(){
		_criteria.remove(Property.FillPattern);
	}

	public void removeFillColor(){
		_criteria.remove(Property.FillColor);
	}

	//ZSS-857
	public void removeBackColor(){
		_criteria.remove(Property.BackColor);
	}

//	public void removeFillForegroundColor(){
//		criteria.remove(Property.FillForegroundColor);
//	}
	
	/**
	 * 
	 * @return returns TRUE if specified cell style matches the matcher's criteria
	 */
	public boolean match(SCellStyle style){
		for(Entry<Property,Object> e:_criteria.entrySet()){
			switch(e.getKey()){
			case Alignment:
				if(!equals(e.getValue(),style.getAlignment())){
					return false;
				}
				break;
			case VerticalAlignment:
				if(!equals(e.getValue(),style.getVerticalAlignment())){
					return false;
				}
				break;
			case BorderBottom:
				if(!equals(e.getValue(),style.getBorderBottom())){
					return false;
				}
				break;
			case BorderRight:
				if(!equals(e.getValue(),style.getBorderRight())){
					return false;
				}
				break;
			case BorderTop:
				if(!equals(e.getValue(),style.getBorderTop())){
					return false;
				}
				break;
			case BorderLeft:
				if(!equals(e.getValue(),style.getBorderLeft())){
					return false;
				}
				break;
			case BorderBottomColor:
				if(!htmlColorEuqlas(e.getValue(),style.getBorderBottomColor().getHtmlColor())){
					return false;
				}
				break;
			case BorderRightColor:
				if(!htmlColorEuqlas(e.getValue(),style.getBorderRightColor().getHtmlColor())){
					return false;
				}
				break;
			case BorderTopColor:
				if(!htmlColorEuqlas(e.getValue(),style.getBorderTopColor().getHtmlColor())){
					return false;
				}
				break;
			case BorderLeftColor:
				if(!htmlColorEuqlas(e.getValue(),style.getBorderLeftColor().getHtmlColor())){
					return false;
				}
				break;
			case DataFormat:
				if(!equals(e.getValue(),style.getDataFormat())){
					return false;
				}
				break;
			case FillColor:
				if(!htmlColorEuqlas(e.getValue(),style.getFillColor().getHtmlColor())){
					return false;
				}
				break;
			case BackColor: //ZSS-857
				if(!htmlColorEuqlas(e.getValue(),style.getBackColor().getHtmlColor())){
					return false;
				}
				break;
//			case FillForegroundColor:
//				if(!equals(e.getValue(),BookHelper.colorToForegroundHTML(book, style.getFillForegroundColorColor()))){
//					return false;
//				}
//				break;
			case FillPattern:
				if(!equals(e.getValue(),style.getFillPattern())){
					return false;
				}
				break;			
			case FontColor:
				if(!htmlColorEuqlas(e.getValue(),style.getFont().getColor().getHtmlColor())){
					return false;
				}
				break;
			case FontName:
				if(!equals(e.getValue(),style.getFont().getName())){
					return false;
				}
				break;
			case FontBoldweight:
				if(!equals(e.getValue(),style.getFont().getBoldweight())){
					return false;
				}
				break;
			case FontHeightPoints:
				if(!equals(e.getValue(),style.getFont().getHeightPoints())){
					return false;
				}
				break;
			case FontItalic:
				if(!equals(e.getValue(),style.getFont().isItalic())){
					return false;
				}
				break;
			case FontStrikeout:
				if(!equals(e.getValue(),style.getFont().isStrikeout())){
					return false;
				}
				break;
			case FontTypeOffset:
				if(!equals(e.getValue(),style.getFont().getTypeOffset())){
					return false;
				}
				break;
			case FontUnderline:
				if(!equals(e.getValue(),style.getFont().getUnderline())){
					return false;
				}
				break;
				
			case Hidden:
				if(!equals(e.getValue(),style.isHidden())){
					return false;
				}
				break;
			case Indention:
				if(!equals(e.getValue(),style.getIndention())){
					return false;
				}
				break;
			case Locked:
				if(!equals(e.getValue(),style.isLocked())){
					return false;
				}
				break;
			case Rotation:
				if(!equals(e.getValue(),style.getRotation())){
					return false;
				}
				break;
			case WrapText:
				if(!equals(e.getValue(),style.isWrapText())){
					return false;
				}
				break;
			}
		}
		return true;
	}
	
	public boolean htmlColorEuqlas(Object o1, Object o2){
		if(o1==o2)
			return true;
		if(o1 instanceof String && o2 instanceof String){
			return ((String)o1).equalsIgnoreCase((String)o2);
		}
		return false;
	}
	
	public boolean equals(Object o1,Object o2){
		if(o1==o2)
			return true;
		return o1!=null?o1.equals(o2):false;
	}
}
