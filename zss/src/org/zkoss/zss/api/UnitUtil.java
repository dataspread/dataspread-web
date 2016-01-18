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

import org.zkoss.zss.ui.impl.XUtils;

/**
 * Utility for transferring unit
 * @author dennis
 * @since 3.0.0
 */
public class UnitUtil {

	public static int pxToPoint(int px){
		return XUtils.pxToPoint(px);
	}
	
	public static int pointToPx(int point){
		return XUtils.pointToPx(point);
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
		return XUtils.fileChar256ToPx(width256,charWidthPx);
	}
	
	/**
	 * convert row height(twip) to pixel
	 * @return the height in pixel
	 */
	public static int twipToPx(int twip) {
		return XUtils.twipToPx(twip);
	}
	
	public static int pxToTwip(int px) {
		return XUtils.pxToTwip(px);
	}
	
	/**
	 * convert row height(twip) to point(font size)
	 * @return the height in pixel
	 */
	public static int twipToPoint(int twip) {
		return XUtils.twipToPoint(twip);
	}
	
	public static int pointToTwip(int point) {
		return XUtils.pointToTwip(point);
	}
}
