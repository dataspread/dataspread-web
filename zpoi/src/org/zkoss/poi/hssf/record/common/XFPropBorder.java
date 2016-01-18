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
import org.zkoss.poi.util.HexDump;
import org.zkoss.poi.util.LittleEndianOutput;

/**
 * @author henri
 * see [MS-XLS].pdf 2.5.284 XFPropBordeer page 944
 * @since 3.9.5
 */
public class XFPropBorder {
	private XFPropColor color;
	private short borderStyle;

	public XFPropBorder(RecordInputStream in) {
		color = new XFPropColor(in);
		borderStyle = in.readShort();
	}
	
	public XFPropBorder(XFPropColor color, int borderStyle) {
		this.color = color;
		this.borderStyle = (short) borderStyle;
	}
	
	public int getDataSize() {
		return color.getDataSize() + 2;
	}
	
	public void serialize(LittleEndianOutput out) {
		color.serialize(out);
		out.writeShort(borderStyle);
	}
	
	public void appendString(StringBuffer sb, String prefix) {
		sb.append(prefix).append("[XFPROPBORDER]").append("\n");
		sb.append(prefix).append("    .color       = \n");
		color.appendString(sb, prefix + "    ");
		sb.append(prefix).append("    .borderStyle = ").append(HexDump.shortToHex(borderStyle)).append("\n");
		sb.append(prefix).append("[/XFPROPBORDER]").append("\n");
	}
}
