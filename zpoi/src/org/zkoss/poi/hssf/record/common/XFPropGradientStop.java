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
 * see [MS-XLS].pdf 2.5.285 XFPropColor page 945
 * @since 3.9.5
 */
public class XFPropGradientStop {
	private double numPosition;  // 0.0 ~ 1.0
	XFPropColor color;

	public XFPropGradientStop(RecordInputStream in) {
		short unused = in.readShort();
		numPosition = in.readDouble();
		color = new XFPropColor(in);
	}
	
	public XFPropGradientStop(double numPosition, XFPropColor color) { 
		this.numPosition = numPosition;
		this.color = color;
	}
	
	public int getDataSize() {
		return 2 + 8 + color.getDataSize();
	}
	
	public void serialize(LittleEndianOutput out) {
		out.writeShort(0); //unused
		out.writeDouble(numPosition);
		color.serialize(out);
	}
	
	public void appendString(StringBuffer sb, String prefix) {
		sb.append(prefix).append("[XFPROPGRADIENTSTOP]").append("\n");
		sb.append(prefix).append("    .numPosition = ").append(numPosition).append("\n");
		sb.append(prefix).append("    .color       = \n");
		color.appendString(sb, prefix + "    ");
		sb.append(prefix).append("[/XFPROPGRADIENTSTOP]").append("\n");
	}
}
