/*
{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Aug 13, 2014, Created by henri
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/

package org.zkoss.poi.hssf.record.common;

import org.zkoss.poi.hssf.record.RecordInputStream;
import org.zkoss.poi.util.BitField;
import org.zkoss.poi.util.BitFieldFactory;
import org.zkoss.poi.util.HexDump;
import org.zkoss.poi.util.LittleEndianOutput;

/**
 * @author henri
 * see [MS-XLS].pdf 2.5.285 XFPropColor page 945
 * @since 3.9.5
 */
public class XFPropColor {
	private static final BitField fValidRGBA = BitFieldFactory.getInstance(0x01);
	private static final BitField xclrType = BitFieldFactory.getInstance(0xFE);

	private byte bits;
	private byte icv;
	private int nTintShade;
	private int dwRgba;

	public XFPropColor(RecordInputStream in) {
		bits = in.readByte();
		icv = in.readByte();
		nTintShade = ((int)in.readShort()) & 0xffff;
		dwRgba = in.readInt();
	}
	
	public XFPropColor(int bits, int icv, int nTintShade, int dwRgba) {
		this.bits = (byte) bits;
		this.icv = (byte) icv;
		this.nTintShade = nTintShade;
		this.dwRgba = dwRgba;
	}
	
	public int getIcv() {
		return ((int)icv) & 0xff;
	}
	
	// xclrType == 1: 0x48, 0x01 ~ 0x3F, 
	//                default foreground 0x40, default background 0x41
	// xclrType == 3: theme color in ThemeRecord
	public void setIcv(int icv) {
		this.icv = (byte) icv;
	}
	
	public boolean isValidRGBA() {
		return fValidRGBA.isSet(bits);
	}
	
	public void setValidRGBA(boolean flag) {
		bits = fValidRGBA.setByteBoolean(bits, flag);
	}
	
	public int getTintShade() {
		return nTintShade;
	}
	
	public void setTintShade(int shade) {
		nTintShade = shade;
	}
	
	public int getXclrType() {
		return xclrType.getValue(bits);
	}
	
	public void setXclrType(int xclrType0) {
		bits = (byte) xclrType.setValue(bits, xclrType0);
	}
	
	public int getRgba() {
		return dwRgba;
	}
	
	public void setRgba(int rgba) {
		dwRgba = rgba;
	}
	
	public int getDataSize() {
		return 8;
	}
	
	public void serialize(LittleEndianOutput out) {
		out.writeByte(bits);
		out.writeByte(icv);
		out.writeShort(nTintShade);
		out.writeInt(dwRgba);
	}
	
	public void appendString(StringBuffer sb, String prefix) {
		sb.append(prefix).append("[XFPROPCOLOR]").append("\n");
		sb.append(prefix).append("    .bits        = ").append(HexDump.byteToHex(bits)).append("\n");
		sb.append(prefix).append("        .fValidaRGBA = ").append(isValidRGBA()).append("\n");
		sb.append(prefix).append("        .xclrType    = ").append(HexDump.byteToHex(getXclrType())).append("\n");
		sb.append(prefix).append("    .icv         = ").append(HexDump.byteToHex(icv)).append("\n");
		sb.append(prefix).append("    .nTintShade  = ").append(HexDump.intToHex(nTintShade)).append("\n");
		sb.append(prefix).append("    .dwRgba      = ").append(HexDump.intToHex(dwRgba)).append("\n");
		sb.append(prefix).append("[/XFPROPCOLOR]").append("\n");
	}
}
