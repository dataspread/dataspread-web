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
import org.zkoss.poi.hssf.record.common.UnicodeString.FormatRun;
import org.zkoss.poi.hssf.record.cont.ContinuableRecordOutput;
import org.zkoss.poi.util.BitField;
import org.zkoss.poi.util.BitFieldFactory;
import org.zkoss.poi.util.HexDump;
import org.zkoss.poi.util.LittleEndianInput;
import org.zkoss.poi.util.LittleEndianOutput;
import org.zkoss.poi.util.StringUtil;

/**
 * @author henri
 * @since 3.9.5
 * see [MS-XLS].pdf 2.5.294 XLUnicodeString page 952
 */
public class XLUnicodeString {
	private static final BitField fHighByte = BitFieldFactory.getInstance(0x01);

	private short cchCharacters;
	private byte bits;
	private String data;
	
	public XLUnicodeString(RecordInputStream in) {
		cchCharacters = in.readShort();
		bits = in.readByte();
			
        if (isHighByte()) {
        	data = in.readUnicodeLEString(getCharCount());
        } else {
        	data = in.readCompressedUnicode(getCharCount());
        }
	}
	
	public XLUnicodeString(String str, boolean highByte) {
		setString(str);
		setHighByte(highByte);
	}
	
	public boolean isHighByte() {
		return fHighByte.isSet(bits);
	}
	
	public void setHighByte(boolean flag) {
		bits = fHighByte.setByteBoolean(bits, flag);
	}
	
	public int getCharCount() {
		return ((int) cchCharacters) & 0xFFFF;
	}
	
	public String getString() {
		return data;
	}
	
	public void setString(String str) {
		data = str;
		cchCharacters = (short) str.length();
	}
	
    public void serialize(LittleEndianOutput out) {
    	out.writeShort(cchCharacters);
    	out.writeByte(bits);
        if (isHighByte()) {
            StringUtil.putUnicodeLE(data, out);
        }  else {
            StringUtil.putCompressedUnicode(data, out);
        }
    }
    
    public int getDataSize() {
    	return 2 + 1 + (isHighByte() ? 2 * getCharCount() : getCharCount());
    }

	public void appendString(StringBuffer sb, String prefix) {
		sb.append(prefix).append("[XLUnitcodeString]").append("\n");
		sb.append(prefix).append("    .cchCharacters = ").append(Integer.toString(getCharCount())).append("\n");
		sb.append(prefix).append("    .bits          = ").append(HexDump.byteToHex(bits)).append("\n");
		sb.append(prefix).append("        .fHighByte     = ").append(isHighByte()).append("\n");
		sb.append(prefix).append("    .rgchData      = ").append(getString()).append("\n");
		sb.append(prefix).append("[/XLUnitcodeString]").append("\n");
	}
}

