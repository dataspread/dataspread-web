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
public class XFPropGradient {
	private int type;
	private double numDegree;
	private double numFillToLeft;  // 0.0 ~ 1.0
	private double numFillToRight; // 0.0 ~ 1.0
	private double numFillToTop;   // 0.0 ~ 1.0
	private double numFillToBottom;// 0.0 ~ 1.0 

	public XFPropGradient(RecordInputStream in) {
		type = in.readInt();
		numDegree = in.readDouble();
		numFillToLeft = in.readDouble();  // 0.0 ~ 1.0
		numFillToRight = in.readDouble(); // 0.0 ~ 1.0
		numFillToTop = in.readDouble();   // 0.0 ~ 1.0
		numFillToBottom = in.readDouble();// 0.0 ~ 1.0 
	}
	
	public XFPropGradient(int type, double numDegree, double numFillToLeft, 
			double numFillToRight, double numFillToTop, double numFillToBottom) {
		this.type = type;
		this.numDegree = numDegree;
		this.numFillToLeft = numFillToLeft;
		this.numFillToRight = numFillToRight;
		this.numFillToTop = numFillToTop;
		this.numFillToBottom = numFillToBottom; 
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public int getDataSize() {
		return 4 + 8 * 5;
	}
	
	public void serialize(LittleEndianOutput out) {
		out.writeInt(type);
		out.writeDouble(numDegree);
		out.writeDouble(numFillToLeft);
		out.writeDouble(numFillToRight);
		out.writeDouble(numFillToTop);
		out.writeDouble(numFillToBottom);
	}

	public void appendString(StringBuffer sb, String prefix) {
		sb.append(prefix).append("[XFPROPGRADIENT]").append("\n");
		sb.append(prefix).append("    .type           = ").append(HexDump.intToHex(type)).append("\n");
		sb.append(prefix).append("    .numDegree      = ").append(numDegree).append("\n");
		sb.append(prefix).append("    .numFillToLeft  = ").append(numFillToLeft).append("\n");
		sb.append(prefix).append("    .numFillToRight = ").append(numFillToRight).append("\n");
		sb.append(prefix).append("    .numFillToToop  = ").append(numFillToTop).append("\n");
		sb.append(prefix).append("    .numFillToBottom= ").append(numFillToBottom).append("\n");
		sb.append(prefix).append("[/XFPROPGRADIENT]").append("\n");
	}
}
