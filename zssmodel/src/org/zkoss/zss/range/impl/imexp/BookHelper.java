/* BookHelper.java

	Purpose:
		
	Description:
		
	History:
		Mar 24, 2010 5:42:58 PM, Created by henrichen

Copyright (C) 2010 Potix Corporation. All Rights Reserved.

*/

package org.zkoss.zss.range.impl.imexp;

import java.util.*;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;
import org.zkoss.poi.hssf.record.FullColorExt;
import org.zkoss.poi.hssf.usermodel.*;
import org.zkoss.poi.hssf.util.*;
import org.zkoss.poi.ss.usermodel.*;
import org.zkoss.poi.xssf.model.ThemesTable;
import org.zkoss.poi.xssf.usermodel.*;

/**
 * Helper for XSSF and HSSF book handling
 * @author Hawk, Kuro
 */
//copies from ZSS project
public final class BookHelper {
	public static final String AUTO_COLOR = "AUTO_COLOR";

	public static final int SORT_NORMAL_DEFAULT = 0;
	public static final int SORT_TEXT_AS_NUMBERS = 1;
	public static final int SORT_HEADER_NO  = 0;
	public static final int SORT_HEADER_YES = 1;
	/**
	 * gets the row freeze, 1 base
	 */
	public static int getRowFreeze(Sheet sheet) {
		if (isFreezePane(sheet)) { //issue #103: Freeze row/column is not correctly interpreted
			final PaneInformation pi = sheet.getPaneInformation();
			int fr = pi != null ? pi.getHorizontalSplitPosition() : 0;
			return fr>0?fr:0;
		}else{
			return 0;
		}
		
	}
	
	/**
	 * gets the column freeze, 1 base
	 */
	public static int getColumnFreeze(Sheet sheet) {
		if (isFreezePane(sheet)) { //issue #103: Freeze row/column is not correctly interpreted
			final PaneInformation pi = sheet.getPaneInformation();
			int fc = pi != null ? pi.getVerticalSplitPosition() : 0;
			return fc>0?fc:0;
		}else{
			return 0;
		}
		
	}
	
	public static boolean isFreezePane(Sheet sheet) {
		
		if (sheet instanceof HSSFSheet){
			return new HSSFSheetHelper((HSSFSheet)sheet).getInternalSheet().getWindowTwo().getFreezePanes();
		}else{
			final CTWorksheet ctsheet = ((XSSFSheet)sheet).getCTWorksheet();
			final CTSheetViews views = ctsheet != null ? ctsheet.getSheetViews() : null;
			final List<CTSheetView> viewList = views != null ? views.getSheetViewList() : null; 
			final CTSheetView view = viewList != null && !viewList.isEmpty() ? viewList.get(0) : null;
			final CTPane pane = view != null ? view.getPane() : null;
			if (pane == null) {
				return false;
			} else {
				return pane.getState() == STPaneState.FROZEN || pane.getState() == STPaneState.FROZEN_SPLIT; //ZSS-1008 
			}
		}
	}
	
	
	public static String getFontHTMLColor(Workbook book, Font font) {
		String colorCode = null;
		if (font instanceof XSSFFont) {
			final XSSFFont f = (XSSFFont) font;
			final XSSFColor color = f.getXSSFColor();
			colorCode = BookHelper.colorToHTML(book, color);
		} else {
			//ZSS-409 Set font color doesn't work in 2003
			//api to get font color is chaos here, i remove and use the reliable one to 
			final HSSFColor color = getHSSFFontColor((HSSFWorkbook)book, (HSSFFont) font);
			colorCode = BookHelper.colorToHTML(book, color);
		}
		//reference BookHelper.getFontCSSStyle()
		if (AUTO_COLOR.equals(colorCode)){
			colorCode = "#000000";
		}
		return colorCode;
	}
	
	private static HSSFColor getHSSFFontColor(HSSFWorkbook book, HSSFFont font) {
		final short index = font.getColor() == Font.COLOR_NORMAL ? HSSFColor.AUTOMATIC.index : font.getColor();
		HSSFPalette palette = book.getCustomPalette();
		if (palette != null) {
			return palette.getColor(index);
		}
		Map<Integer, HSSFColor> indexHash = (Map<Integer, HSSFColor>) HSSFColor.getIndexHash();
		return indexHash.get(Integer.valueOf(index));
	}
	/*
	 * Returns the associated #rrggbb HTML color per the given POI Color.
	 * @return the associated #rrggbb HTML color per the given POI Color.
	 */
	public static String colorToHTML(Workbook book, Color color) {
		if (book instanceof HSSFWorkbook) {
			return HSSFColorToHTML((HSSFWorkbook) book, (HSSFColor) color);
		} else {
			return XSSFColorToHTML((XSSFWorkbook) book, (XSSFColor) color);
		}
	}
	public static String colorToBorderHTML(Workbook book, Color color) {
		String htmlColor = colorToHTML(book,color);
		if(AUTO_COLOR.equals(htmlColor)){
			return "#000000";
		}
		return htmlColor;
	}
	
	//ZSS-857: default fill color is black
	public static String colorToForegroundHTML(Workbook book, Color color) {
		String htmlColor = colorToHTML(book,color);
		if(AUTO_COLOR.equals(htmlColor)){
			return "#000000";
		}
		return htmlColor;
	}
	
	public static String colorToBackgroundHTML(Workbook book, Color color) {
		String htmlColor = colorToHTML(book,color);
		if(AUTO_COLOR.equals(htmlColor)){
			return "#ffffff";
		}
		return htmlColor;
	}
	
	private static byte[] getRgbWithTint(byte[] rgb, double tint) {
		int k = rgb.length > 3 ? 1 : 0; 
		final byte red = rgb[k++];
		final byte green = rgb[k++];
		final byte blue = rgb[k++];
		final double[] hsl = rgbToHsl(red, green, blue);
		final double hue = hsl[0];
		final double sat = hsl[1];
		final double lum = tint(hsl[2], tint);
		return hslToRgb(hue, sat, lum);
	}
	private static double[] rgbToHsl(byte red, byte green, byte blue){
		final double r = (red & 0xff) / 255d;
		final double g = (green & 0xff) / 255d;
		final double b = (blue & 0xff) / 255d;
		final double max = Math.max(Math.max(r, g), b);
		final double min = Math.min(Math.min(r, g), b);
		double h = 0d, s = 0d, l = (max + min) / 2d;
		if (max == min) {
			h = s = 0d; //gray scale
		} else {
			final double d = max - min;
			s = l > 0.5 ? d / (2d - max - min) : d / (max + min);
			if (max == r) {
				h = (g - b) / d + (g < b ? 6d : 0d);
			} else if (max == g) {
				h = (b - r) / d + 2d; 
			} else if (max == b) {
				h = (r - g) / d + 4d;
			}
			h /= 6d;
		}
		return new double[] {h, s, l};
	}
	private static byte[] hslToRgb(double hue, double sat, double lum){
		 double r, g, b;
		 if(sat == 0d){
			 r = g = b = lum; // gray scale
		 } else {
			 final double q = lum < 0.5d ? lum * (1d + sat) : lum + sat - lum * sat;
			 final double p = 2d * lum - q;
			 r = hue2rgb(p, q, hue + 1d/3d);
			 g = hue2rgb(p, q, hue);
			 b = hue2rgb(p, q, hue - 1d/3d);
		 }
		 final byte red = (byte) (r * 255d);
		 final byte green = (byte) (g * 255d);
		 final byte blue = (byte) (b * 255d);
		 return new byte[] {red, green, blue};
	}
	private static double hue2rgb(double p, double q, double t) {
		if(t < 0d) t += 1d;
		if(t > 1d) t -= 1d;
		if(t < 1d/6d) 
			return p + (q - p) * 6d * t;
		if(t < 1d/2d) 
			return q;
		if(t < 2d/3d) 
			return p + (q - p) * (2d/3d - t) * 6d;
		return p;
	}
	private static double tint(double val, double tint) {
		return tint > 0d ? val * (1d - tint) + tint : val * (1d + tint);
	}

	private static String XSSFColorToHTML(XSSFWorkbook book, XSSFColor color) {
		if (color != null) {
			final CTColor ctcolor = color.getCTColor();
			if (ctcolor.isSetIndexed()) {
				byte[] rgb = IndexedRGB.getRGB(color.getIndexed());
				if (rgb != null) {
					return "#"+ toHex(rgb[0])+ toHex(rgb[1])+ toHex(rgb[2]);
				}
			}
			if (ctcolor.isSetRgb()) {
				byte[] argb = ctcolor.isSetTint() ?
					getRgbWithTint(color.getRgb(), color.getTint())/*color.getRgbWithTint()*/ : color.getRgb();
				return argb.length > 3 ? 
					"#"+ toHex(argb[1])+ toHex(argb[2])+ toHex(argb[3])://ignore alpha
					"#"+ toHex(argb[0])+ toHex(argb[1])+ toHex(argb[2]); 
			}
			if (ctcolor.isSetTheme()) {
			    ThemesTable theme = book.getTheme();
			    if (theme != null) {
			    	XSSFColor themecolor = theme.getThemeColor(color.getTheme());
			    	if (themecolor != null) {
			    		if (ctcolor.isSetTint()) {
			    			themecolor.setTint(ctcolor.getTint());
			    		}
			    		return XSSFColorToHTML(book, themecolor); //recursive
			    	}
			    }
			}
		}
	    return AUTO_COLOR;
 	}
	
	private static String HSSFColorToHTML(HSSFWorkbook book, HSSFColor color) {
		return color == null || HSSFColor.AUTOMATIC.getInstance().equals(color) ? AUTO_COLOR : 
			color.isIndex() ? HSSFColorIndexToHTML(book, color.getIndex()) : HSSFColorToHTML((HSSFColorExt)color); 
	}
	private static String HSSFColorToHTML(HSSFColorExt color) {
		short[] triplet = color.getTriplet();
		byte[] argb = new byte[3];
		for (int j = 0; j < 3; ++j) {
			argb[j] = (byte) triplet[j];
		}
		if (color.isTint()) {
			argb = getRgbWithTint(argb, color.getTint());
		}
		return 	"#"+ toHex(argb[0])+ toHex(argb[1])+ toHex(argb[2]); 
	}
	
	private static String HSSFColorIndexToHTML(HSSFWorkbook book, int index) {
		HSSFPalette palette = book.getCustomPalette();
		HSSFColor color = null;
		if (palette != null) {
			color = palette.getColor(index);
		}
		short[] triplet = null;
		if (color != null)
			triplet =  color.getTriplet();
		else {
			final Map<Integer, HSSFColor> colors = HSSFColor.getIndexHash();
			color = colors.get(Integer.valueOf(index));
			if (color != null)
				triplet = color.getTriplet();
		}
		return triplet == null ? null : 
			HSSFColor.AUTOMATIC.getInstance().equals(color) ? AUTO_COLOR : tripletToHTML(triplet);
	}
	
	public static String toHex(int num) {
		num = num & 0xff;
		final String hex = Integer.toHexString(num);
		return hex.length() == 1 ? "0"+hex : hex; 
	}
	
	private static String tripletToHTML(short[] triplet) {
		return triplet == null ? null : "#"+ toHex(triplet[0])+ toHex(triplet[1])+ toHex(triplet[2]);
	}
	
	public static void setLeftBorderColor(CellStyle style, Color color) {
		if (style instanceof HSSFCellStyle) {
			((HSSFCellStyle)style).setLeftBorderColor(((HSSFColor)color));
		} else {
			((XSSFCellStyle)style).setLeftBorderColor((XSSFColor)color);
		}
	}
	
	public static void setRightBorderColor(CellStyle style, Color color) {
		if (style instanceof HSSFCellStyle) {
			((HSSFCellStyle)style).setRightBorderColor(((HSSFColor)color));
		} else {
			((XSSFCellStyle)style).setRightBorderColor((XSSFColor)color);
		}
	}
	public static void setTopBorderColor(CellStyle style, Color color) {
		if (style instanceof HSSFCellStyle) {
			((HSSFCellStyle)style).setTopBorderColor(((HSSFColor)color));
		} else {
			((XSSFCellStyle)style).setTopBorderColor((XSSFColor)color);
		}
	}
	public static void setBottomBorderColor(CellStyle style, Color color) {
		if (style instanceof HSSFCellStyle) {
			((HSSFCellStyle)style).setBottomBorderColor(((HSSFColor)color));
		} else {
			((XSSFCellStyle)style).setBottomBorderColor((XSSFColor)color);
		}
	}
	
	public static Color HTMLToColor(Workbook book, String color) {
		if (book instanceof HSSFWorkbook) {
			return HTMLToHSSFColor((HSSFWorkbook) book, color);
		} else {
			return HTMLToXSSFColor((XSSFWorkbook) book, color);
		}
	}
	
	private static Color HTMLToXSSFColor(XSSFWorkbook book, String color) {
		byte[] triplet = HTMLToTriplet(color);
		byte a = (byte) 0xff;
		byte r = triplet[0];
		byte g = triplet[1];
		byte b = triplet[2];
		return  new XSSFColor(new byte[] {a, r, g, b});
	}
	
	private static Color HTMLToHSSFColor(HSSFWorkbook book, String color) {
		byte[] triplet = HTMLToTriplet(color);
		byte r = triplet[0];
		byte g = triplet[1];
		byte b = triplet[2];
		short red = (short) (r & 0xff);
		short green = (short) (g & 0xff);
		short blue = (short) (b & 0xff);
		HSSFPalette palette = book.getCustomPalette();
		HSSFColor pcolor = palette != null ? palette.findColor(r, g, b) : null;
		if (pcolor != null) { //find default palette
			return pcolor;
		} else {
			final Hashtable<short[], HSSFColor> colors = HSSFColor.getRgbHash();
			HSSFColor tcolor = colors.get(new short[] {red, green, blue});
			if (tcolor != null)
				return tcolor;
			else {
				try {
					HSSFColor ncolor = palette.addColor(r, g, b);
					return ncolor;
				} catch (RuntimeException ex) {
					//try to create a fullcolor if not a built in palette color
					FullColorExt fullColor = new FullColorExt(red, green, blue);
					return new HSSFColorExt(fullColor);
				}
				
			}
		}
	}
	
	private static byte[] HTMLToTriplet(String color) {
		final int offset = color.charAt(0) == '#' ? 1 : 0;
		final short red = Short.parseShort(color.substring(offset+0,offset+2), 16); //red
		final short green = Short.parseShort(color.substring(offset+2,offset+4), 16); //green
		final short blue = Short.parseShort(color.substring(offset+4, offset+6), 16); //blue
		final byte r = (byte)(red & 0xff);
		final byte g = (byte)(green & 0xff);
		final byte b = (byte)(blue & 0xff);
		return new byte[] {r, g, b};
	}
	
	public static void setFillForegroundColor(CellStyle newCellStyle, Color xlsColor) {
		if (newCellStyle instanceof HSSFCellStyle) {
			((HSSFCellStyle)newCellStyle).setFillForegroundColor((HSSFColor)xlsColor);
		} else {
			((XSSFCellStyle)newCellStyle).setFillForegroundColor((XSSFColor)xlsColor);
		}
	}

	//ZSS-780
	//since 3.6.0
	public static void setFillBackgroundColor(CellStyle newCellStyle, Color xlsColor) {
		if (newCellStyle instanceof HSSFCellStyle) {
			((HSSFCellStyle)newCellStyle).setFillBackgroundColor((HSSFColor)xlsColor);
		} else {
			((XSSFCellStyle)newCellStyle).setFillBackgroundColor((XSSFColor)xlsColor);
		}
	}

	public static void setFontColor(Workbook book, Font font, Color color) {
		if (font instanceof HSSFFont) {
			if (color instanceof HSSFColorExt) { //not palette color
				color = ((HSSFColorExt)color).getSimilarColor(((HSSFWorkbook)book).getCustomPalette());
			}
			((HSSFFont)font).setColor(((HSSFColor)color).getIndex());
		} else {
			//20130415, dennischen, force reset, set rgb color is not able to override previous set a color with theme(color form default cell) 
			((XSSFFont)font).setColor(null);
			((XSSFFont)font).setColor((XSSFColor)color);
		}
	}
}
