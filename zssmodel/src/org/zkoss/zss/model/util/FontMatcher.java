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

import org.zkoss.zss.model.SFont;


/**
 * This class can be used to match a {@link SFont} with a criteria.
 * @author dennis
 * @since 3.5.0
 */
public class FontMatcher {

	
	Map<Property,Object> _criteria = new LinkedHashMap<Property, Object>();
	
	private enum Property {
		
		Name,
		Color,
		Boldweight,
		HeightPoints,
		Italic,
		Strikeout,
		TypeOffset,
		Underline,
	}

	
	public FontMatcher(){}
	/**
	 * Create a font matcher with a specified font as the criteria 
	 */
	public FontMatcher(SFont criteria){
		setColor(criteria.getColor().getHtmlColor());
		setName(criteria.getName());
		setBoldweight(criteria.getBoldweight());
		setHeightPoints(criteria.getHeightPoints());
		setItalic(criteria.isItalic());
		setStrikeout(criteria.isStrikeout());
		setTypeOffset(criteria.getTypeOffset());
		setUnderline(criteria.getUnderline());
		
	}
	
	public void setColor(String color) {
		_criteria.put(Property.Color, color);
	}
	
	public void setName(String name) {
		_criteria.put(Property.Name, name);
	}
	
	public void setBoldweight(SFont.Boldweight boldweight) {
		_criteria.put(Property.Boldweight, boldweight);
	}
	
	public void setHeightPoints(int height) {
		_criteria.put(Property.HeightPoints, height);
	}
	
	public void setItalic(boolean italic) {
		_criteria.put(Property.Italic, italic);
	}
	
	public void setStrikeout(boolean strikeout) {
		_criteria.put(Property.Strikeout, strikeout);
	}
	
	public void setTypeOffset(SFont.TypeOffset typeOffset) {
		_criteria.put(Property.TypeOffset, typeOffset);
	}
	
	public void setUnderline(SFont.Underline underline) {
		_criteria.put(Property.Underline, underline);
	}

	//remove api

	public void removeColor() {
		_criteria.remove(Property.Color);
	}
	public void removeName() {
		_criteria.remove(Property.Name);
	}
	public void removeBoldweight() {
		_criteria.remove(Property.Boldweight);
	}
	public void removeHeightPoints() {
		_criteria.remove(Property.HeightPoints);
	}
	public void removeItalic() {
		_criteria.remove(Property.Italic);
	}
	public void removeStrikeout() {
		_criteria.remove(Property.Strikeout);
	}
	public void removeTypeOffset() {
		_criteria.remove(Property.TypeOffset);
	}
	public void removeUnderline() {
		_criteria.remove(Property.Underline);
	}

	
	
	public boolean match(SFont style){
		for(Entry<Property,Object> e:_criteria.entrySet()){
			switch(e.getKey()){
			case Color:
				if(!htmlColorEuqlas(e.getValue(),style.getColor().getHtmlColor())){
					return false;
				}
				break;
			case Name:
				if(!equals(e.getValue(),style.getName())){
					return false;
				}
				break;
			case Boldweight:
				if(!equals(e.getValue(),style.getBoldweight())){
					return false;
				}
				break;
			case HeightPoints:
				if(!equals(e.getValue(),style.getHeightPoints())){
					return false;
				}
				break;
			case Italic:
				if(!equals(e.getValue(),style.isItalic())){
					return false;
				}
				break;
			case Strikeout:
				if(!equals(e.getValue(),style.isStrikeout())){
					return false;
				}
				break;
			case TypeOffset:
				if(!equals(e.getValue(),style.getTypeOffset())){
					return false;
				}
				break;
			case Underline:
				if(!equals(e.getValue(),style.getUnderline())){
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
