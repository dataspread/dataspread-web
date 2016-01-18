/* UnitUtil.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/5/1 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.range.impl.imexp;


/**
 * Various unit conversion methods.
 * @author Hawk
 * @since 3.5.0
 */
//copied from ZSS.
public class UnitUtil {

	private static final int DPI = 96;

	/** convert pixel to point */
	public static int pxToPoint(int px) {
		return px * 72 / DPI; //assume 96dpi
	}
	
	/** convert point to pixel */
	public static int pointToPx(int point) {
		return point * DPI / 72; //assume 96dpi
	}
	
	
	/** convert pixel to EMU */
	public static int pxToEmu(int px) {
		//refer form ActionHandler,ChartHelper
		return (int) Math.round(((double)px) * 72 * 20 * 635 / DPI); //assume 96dpi
	}
	
	/** convert EMU to pixel, 1 twip == 635 emu */
	public static int emuToPx(int emu) {
		//refer form ChartHelper
		return (int) Math.round(((double)emu) * DPI / 72 / 20 / 635); //assume 96dpi
	}

	/** convert twip (1/20 point) to pixel */
	public static int twipToPx(int twip) {
		return twip * DPI / 72 / 20; //assume 96dpi
	}
	
	/** convert pixel to twip (1/20 point) */
	public static int pxToTwip(int px) {
		return px * 72 * 20 / DPI; //assume 96dpi
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

	/** 
	 * Convert default columns width (in character) to pixel.
	 * 5 pixels are margin padding.(There are 4 pixels of margin padding (two on each side), plus 1 pixel padding for the gridlines.)
	 * Description of how column widths are determined in Excel (http://support.microsoft.com/kb/214123)
	 * @param columnWidth number of character
	 * @param charWidth Using the Calibri font, the maximum digit width of 11 point font size is 7 pixels (at 96 dpi).
	 * @return width in pixel  Rounds this number up to the nearest multiple of 8 pixels.
	 */ 
	public static int defaultColumnWidthToPx(int columnWidth, int charWidth) {
		final int w = columnWidth * charWidth + 5;
		final int diff = w % 8;
		return w + (diff > 0 ? (8 - diff) : 0);
	}

	/**
	 * Convert default column width in pixel to number of character by reverse defaultColumnWidthToPx() and ignore the mod(%) operation.
	 * @param px default column width in pixel
	 * @param charWidth  one character width in pixel of normal style's font 
	 * @return default column width in character
	 */
	public static int pxToDefaultColumnWidth(int px, int charWidth) {
		return (px - 5) / charWidth;
	}

	// Formula: Pixels = Inches * DPI
	public static int incheToPx(double inches) {
		return (int) (inches * DPI);
	}

	// Formula: Inches = Pixels / DPI
	public static double pxToInche(int px) {
		return px / (DPI * 1.0);
	}

	public static double pxToCTChar(int px, int charWidth) {
		return (double) pxToFileChar256(px, charWidth) / 256;
	}
	
	public static double cmToInche(double cm) {
		return cm / 2.54;
	}
	
	public static double incheToCm(double inches) {
		return inches * 2.54;
	}
	
	public static int cmToPx(double cm) {
		return incheToPx(cmToInche(cm));
	}
	
	public static double pxToCm(int px) {
		return incheToCm(pxToInche(px));
	}

	//ZSS-952
	/** 
	 * Convert XSSF default columns width (in character) to pixel. In XSSF the
	 * value is a double value / charWidth and includes the 5 pixels margin padding.
	 * 
	 * 5 pixels are margin padding.(There are 4 pixels of margin padding (two on each side), plus 1 pixel padding for the gridlines.)
	 * Description of how column widths are determined in Excel (http://support.microsoft.com/kb/214123)
	 * @param columnWidth number of character
	 * @param charWidth Using the Calibri font, the maximum digit width of 11 point font size is 7 pixels (at 96 dpi).
	 * @return width in pixel
	 */ 
	public static int xssfDefaultColumnWidthToPx(double xssfDefaultColumnWidth, int charWidth) {
		return (int) Math.round(xssfDefaultColumnWidth * charWidth);
	}


}
