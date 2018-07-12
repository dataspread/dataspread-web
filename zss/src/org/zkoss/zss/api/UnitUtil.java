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
package org.zkoss.zss.api;

/**
 * Utility for transferring unit
 * @author dennis
 * @since 3.0.0
 */
public class UnitUtil {

	public static int pxToPoint(int px){
		return 0;
	}
	
	public static int pointToPx(int point){
		return 0;
	}
	
	
	
	/** convert pixel to EMU */
	public static int pxToEmu(int px) {
		//refer form ActionHandler,ChartHelper
		return (int) Math.round(((double)px) * 72 * 20 * 635 / 96); //assume 96dpi
	}
	
	/** convert EMU to pixel, 1 twip == 635 emu */
	public static int emuToPx(int emu) {
		//refer form ChartHelper
		return (int) Math.round(((double)emu) * 96 / 72 / 20 / 635); //assume 96dpi
	}

	/**
	 * convert column width(char 256) to pixel
	 * @return the width in pixel
	 */
	public static int char256ToPx(int width256, int charWidthPx) {
		return 0;
	}
	
	/**
	 * convert row height(twip) to pixel
	 * @return the height in pixel
	 */
	public static int twipToPx(int twip) {
		return 0;
	}
	
	public static int pxToTwip(int px) {
		return 0;
	}
	
	/**
	 * convert row height(twip) to point(font size)
	 * @return the height in pixel
	 */
	public static int twipToPoint(int twip) {
		return 0;
	}
	
	public static int pointToTwip(int point) {
		return 0;
	}
}
