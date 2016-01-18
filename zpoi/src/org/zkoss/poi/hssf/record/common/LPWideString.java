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
import org.zkoss.poi.util.HexDump;
import org.zkoss.poi.util.LittleEndianInput;
import org.zkoss.poi.util.LittleEndianOutput;
import org.zkoss.poi.util.StringUtil;

/**
 * @author henri
 * @since 3.9.5
 */
public class LPWideString {
	private short cchCharacters;
	private String rgchData;
	
	public LPWideString(RecordInputStream in) {
		cchCharacters = in.readShort();
		rgchData = StringUtil.readUnicodeLE(in, cchCharacters);
	}
	
	public LPWideString(String str) {
		setString(str);
	}
	
	public int getCharCount() {
		return ((int) cchCharacters) & 0xFFFF;
	}
	
	public String getString() {
		return rgchData;
	}
	
	public void setString(String str) {
		rgchData = str;
		cchCharacters = (short) str.length();
	}
	
    public void serialize(LittleEndianOutput out) {
    	out.writeShort(cchCharacters);
    	StringUtil.putUnicodeLE(rgchData, out);
    }
    
    public int getDataSize() {
    	return 2 + 2 * cchCharacters;
    }
    
	public void appendString(StringBuffer sb, String prefix) {
		sb.append(prefix).append("[LPWideString]").append("\n");
		sb.append(prefix).append("    .cchCharacters = ").append(Integer.toString(getCharCount())).append("\n");
		sb.append(prefix).append("    .rgchData      = ").append(getString()).append("\n");
		sb.append(prefix).append("[/LPWideString]").append("\n");
	}
}
