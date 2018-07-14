/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.zkoss.poi.hssf.record;

import org.zkoss.poi.hssf.record.common.FtrHeader;
import org.zkoss.poi.util.BitField;
import org.zkoss.poi.util.BitFieldFactory;
import org.zkoss.poi.util.LittleEndianOutput;

/**
 * [MS-XLS].pdf 2.4.200 PLV page 360
 * Page Layout View for a sheet.
 * @author henrichen@zkoss.org
 */
public class PLVRecord extends StandardRecord  {
	private static final BitField pageLayoutView		= BitFieldFactory.getInstance(0x01);
	private static final BitField rulerVisible			= BitFieldFactory.getInstance(0x02);
	private static final BitField whitespaceHidden		= BitFieldFactory.getInstance(0x04);
	
	public static final short sid = 0x088b;

	private FtrHeader futureHeader;

	short wScalePLV; //0, 10 ~ 400 (%)
	short bits;
	
	public PLVRecord() {
		futureHeader = new FtrHeader();
		futureHeader.setRecordType(sid);
	}

	public PLVRecord(RecordInputStream in) {
		futureHeader = new FtrHeader(in);
		
		wScalePLV = in.readShort();
		bits = in.readShort();
		
		if (wScalePLV < 10 || wScalePLV > 400)
			wScalePLV = 0;
	}
	
	public short getSid() {
		return sid;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[PLV]\n");
		buffer.append(futureHeader.toString());
        buffer.append("    .wScalePLV  = ")
        	  .append(Integer.toString(wScalePLV)).append("\n");
		buffer.append("    .bits       = ")
  	  		  .append(Integer.toHexString(bits)).append("\n");
		buffer.append("       .fPageLayoutView     = ").append(isPageLayoutView()).append("\n");
		buffer.append("       .fRulerVisible       = ").append(isRulerVisible()).append("\n");
		buffer.append("       .fWhitespaceHidden   = ").append(isWhitespaceHidden()).append("\n");
		buffer.append("[/PLV]\n");
		return buffer.toString();
	}
	
	public int getScalePLV() {
		return wScalePLV;
	}
	
	public void setScalePLV(int scale) {
		if (scale < 10 || scale > 400) {
			scale = 0;
		}
		wScalePLV = (short) scale;
	}
	
	public boolean isPageLayoutView() {
		return pageLayoutView.isSet(bits);
	}
	
	public boolean isRulerVisible() {
		return rulerVisible.isSet(bits);
	}
	
	public boolean isWhitespaceHidden() {
		return whitespaceHidden.isSet(bits);
	}
	
	public void setPageLayoutView(boolean flag) {
		bits = (short) pageLayoutView.setBoolean(bits, flag);
	}

	public void setRulerVisible(boolean flag) {
		bits = (short) rulerVisible.setBoolean(bits, flag);
	}
	
	public void setWhitespaceHidden(boolean flag) {
		bits = (short) whitespaceHidden.setBoolean(bits, flag);
	}
	
	public void serialize(LittleEndianOutput out) {
		futureHeader.serialize(out);
		
		out.writeShort(wScalePLV);
		out.writeShort(bits);
	}
	
    public Object clone() {
        return cloneViaReserialise();
    }

	protected int getDataSize() {
		return 12 + 4;
	}
}
