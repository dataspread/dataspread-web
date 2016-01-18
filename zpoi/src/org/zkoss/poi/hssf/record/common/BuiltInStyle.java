/*
{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Aug 14, 2014, Created by henri
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/

package org.zkoss.poi.hssf.record.common;

import org.zkoss.poi.hssf.record.RecordInputStream;
import org.zkoss.poi.util.HexDump;
import org.zkoss.poi.util.LittleEndianOutput;

/**
 * @author henri
 * @since 3.9.5
 * [MS-XLS].pdf 2.5.16 BuiltInStyle  page 622
 */
public class BuiltInStyle {
	private byte istyBuiltIn;
	private byte iLevel;
	
	public BuiltInStyle(RecordInputStream in) {
		istyBuiltIn = in.readByte();
		iLevel = in.readByte();
	}
	
	public BuiltInStyle(int type, int level) {
		istyBuiltIn = (byte) type;
		iLevel = (byte) level;
	}
	
    public int getBuiltInType() {
    	return ((int)istyBuiltIn) & 0xff;
    }
    
    public void setBuiltInType(int type) {
    	istyBuiltIn = (byte) type;
    }
    
    public int getOutlineLevel() {
    	return ((int) iLevel) & 0xff;
    }

    public void setOutlineLevel(int outlineLevel) {
    	iLevel = (byte) outlineLevel;
    }
    
	
    public void serialize(LittleEndianOutput out) {
    	out.writeByte(istyBuiltIn);
    	out.writeByte(iLevel);
    }
    
    public int getDataSize() {
    	return 2;
    }
    
	public void appendString(StringBuffer sb, String prefix) {
		sb.append(prefix).append("[BUILTINSTYLE]").append("\n");
		sb.append(prefix).append("    .istyBuiltIn = ").append(HexDump.byteToHex(istyBuiltIn)).append("\n");
		sb.append(prefix).append("    .iLevel      = ").append(HexDump.byteToHex(iLevel)).append("\n");
		sb.append(prefix).append("[/BUILTINSTYLE]").append("\n");
	}
}
