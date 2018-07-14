/* FullColorExt.java

	Purpose:
		
	Description:
		
	History:
		Jan 17, 2011 09:12:40 AM     2011, Created by henrichen

Copyright (C) 2011 Potix Corporation. All Rights Reserved.
*/
package org.zkoss.poi.hssf.record;

import org.zkoss.poi.util.HexDump;
import org.zkoss.poi.util.LittleEndianOutput;

/**
 * 2.5.155 FullColorExt. ([MS-XLS].pdf, page 730.
 * @author henrichen
 *
 */
public class FullColorExt {
	private short xclrType; //2.5.279 XColorType. [MS-XLS].pdf, page 938.
	private short nTintShade;
	private int xclrValue; //0-7 red, 8-15 green, 16-23 blue, 24-31 alpha (native bits). 2.5.178 LongRGBA. [MS-XLS].pdf, page 749
	private byte[] unused = new byte[8];
	
	public FullColorExt(RecordInputStream in) {
		xclrType = in.readShort();
		nTintShade = in.readShort();
		xclrValue = in.readInt();
		in.read(unused, 0, unused.length);
	}
	
	public FullColorExt(short r, short g, short b) {
		xclrType = 2;
		nTintShade = 0;
		xclrValue = 0xff000000 //alpha 
					| (((int)b) << 16 ) & 0xff0000 //blue 
					| (((int)g) << 8 ) & 0x00ff00 //green
					| (((int)r) & 0x0000ff); //red
	}

	//ZSS-746
	public FullColorExt(short xclrType, short nTintShade, int xclrValue) {
		this.xclrType = xclrType;
		this.nTintShade = nTintShade;
		this.xclrValue = xclrValue;
	}

	public int getDataSize() {
		return 2 + 2 + 4 + 8;
	}
	public boolean isTheme() {
		return xclrType == 3;
	}
	public boolean isRGB() {
		return xclrType == 2;
	}
	public boolean isIndex() {
		return xclrType == 1;
	}
	public void appendString(StringBuffer sb, String prefix) {
		sb.append(prefix).append(".xclrType  =").append(xclrType).append("\n");
		sb.append(prefix).append(".nTintShade=").append(nTintShade).append("\n");
		sb.append(prefix).append(".xclrValue =").append(HexDump.intToHex(xclrValue)).append("\n");
	}
	public void serialize(LittleEndianOutput out) {
		out.writeShort(xclrType);
		out.writeShort(nTintShade);
		out.writeInt(xclrValue);
		out.write(unused);
	}
	
	public short getXclrType() {
		return xclrType;
	}
	
	public void setXclrType(short xclrType) {
		this.xclrType = xclrType;
	}
	
	public short getTintShade() {
		return nTintShade;
	}
	
	public void setTintShade(short nTintShade) {
		this.nTintShade = nTintShade;
	}
	
	public int getXclrValue() {
		return xclrValue;
	}
	
	public void setXclrValue(int xclrValue) {
		this.xclrValue = xclrValue;
	}
	private int BGRToRGB() {
		return (xclrValue & 0xff000000) //alpha 
			| ((xclrValue << 16 ) & 0xff0000) //red 
			| (xclrValue & 0x00ff00) //green
			| ((xclrValue >> 16 ) & 0x000000ff); //blue
	}
	public int getRGB() {
		if (isRGB()) {
			return BGRToRGB(); //20110322, henrichen@zkoss.org: BGR -> RGB
		} else if (isTheme()) {
			return DEFAULT_THEME[getXclrValue()];
		} else if (isIndex()) {
			throw new RuntimeException("XFExt with color table is not supported yet:"+ getXclrValue());
		} else {
			return -1;
		}
	}
	
	public boolean isTint() {
		return nTintShade != 0;
	}
	public double getTint() {
		return ((double) nTintShade) / Short.MAX_VALUE;
	}
	
	//MS document incorrect lt and dk should reverse their order to make lt before dk; 
	//i.e., [0]:lt1, [1]:dk1, [2]:lt2, [3]:dk2
	private static final int[] DEFAULT_THEME = new int[] {
		0xFFFFFF, //lt1
		0x000000, //dk1
		0xEEECE1, //lt2
		0x1F497D, //dk2
		0x4F81BD, //accent1
		0xC0504D, //accent2
		0x9BBB59, //accent3
		0x8064A2, //accent4
		0x4BACC6, //accent5
		0xF79646, //accent6
		0x0000FF, //hlink
		0x800080, //folHlink
	};
}
