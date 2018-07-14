/* HSSFColorExt.java

	Purpose:
		
	Description:
		
	History:
		Jan 18, 2011 11:09:40 AM     2011, Created by henrichen

Copyright (C) 2011 Potix Corporation. All Rights Reserved.
*/
package org.zkoss.poi.hssf.util;

import org.zkoss.poi.hssf.record.FullColorExt;
import org.zkoss.poi.hssf.usermodel.HSSFPalette;
import org.zkoss.poi.util.HexDump;
/**
 * HSSFColor that wrap FullColorExt.
 * @author henrichen
 */
public final class HSSFColorExt extends HSSFColor
{
	private final FullColorExt _colorExt;

	public HSSFColorExt(FullColorExt colorExt) {
		_colorExt = colorExt;
	}
	public FullColorExt getFullColorExt() {
		return _colorExt;
	}
	
    public short getIndex()
    {
        return _colorExt.isIndex() ? (short)_colorExt.getXclrValue() : AUTOMATIC.getInstance().getIndex();
    }
    
    public HSSFColor getSimilarColor(HSSFPalette palette) {
		final short[] rgb = getTriplet();
		final short red = rgb[0];
		final short green = rgb[1];
		final short blue = rgb[2];
		//return similar color
		return palette.findSimilarColor(red, green, blue);
    }

    public boolean isIndex() {
    	return _colorExt.isIndex();
    }
    
    public boolean isTheme() {
    	return _colorExt.isTheme();
    }
    
    public boolean isRgb() {
    	return _colorExt.isRGB();
    }

    public boolean isTint() {
    	return _colorExt.isTint();
    }
    
    public double getTint() {
    	return _colorExt.getTint();
    }
    
    public short [] getTriplet()
    {
    	if (_colorExt.isRGB() || _colorExt.isTheme()) {
    		final int color = _colorExt.getRGB();
    		final short[] rgb = new short[3];
    		rgb[0] = (short) ((color >> 16) & 0xff); //r
    		rgb[1] = (short) ((color >> 8) & 0xff); //g
    		rgb[2] = (short) (color & 0xff); //b
            return rgb;
    	}
    	return AUTOMATIC.getInstance().getTriplet();
    }

    public String getHexString()
    {
    	final short[] rgb = getTriplet();
    	if (rgb != null) {
        	String r = new String(HexDump.byteToHex(rgb[0]));
        	String g = new String(HexDump.byteToHex(rgb[1]));
        	String b = new String(HexDump.byteToHex(rgb[2]));
            return (rgb[0] == 0 ? "0" : (r+r)) + ":"
            		+ (rgb[1] == 0 ? "0" : (g+g)) + ":"
            		+ (rgb[2] == 0 ? "0" : (b+b));
    	}
    	return AUTOMATIC.getInstance().getHexString();
    }
}
