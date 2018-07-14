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
 * [MS-XLS].pdf 2.4.54 Compat12 page 231
 * whether to check for compatibility with earlier application versions when 
 * saving the workbook.
 * 
 * @author henrichen@zkoss.org
 */
public class Compat12Record extends StandardRecord  {
	private static final BitField fNoCompatChk = BitFieldFactory.getInstance(0x01);
	
	public static final short sid = 0x088C;

	private FtrHeader futureHeader;

	int bits;
	
	public Compat12Record() {
		futureHeader = new FtrHeader();
		futureHeader.setRecordType(sid);
	}

	public Compat12Record(RecordInputStream in) {
		futureHeader = new FtrHeader(in);
		
		bits = in.readInt();
	}
	
	public short getSid() {
		return sid;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[COMPAT12]\n");
		buffer.append(futureHeader.toString());
		buffer.append("    .bits       = ")
  	  		  .append(Integer.toHexString(bits)).append("\n");
		buffer.append("       .fNoCompatChk     = ").append(isNoCompatibleCheck()).append("\n");
		buffer.append("[/COMPAT12]\n");
		return buffer.toString();
	}
	
	public boolean isNoCompatibleCheck() {
		return fNoCompatChk.isSet(bits);
	}
	public void setNoCompatibleCheck(boolean flag) {
		bits = fNoCompatChk.setBoolean(bits, flag);
	}
	
	public void serialize(LittleEndianOutput out) {
		futureHeader.serialize(out);
		
		out.writeInt(bits);
	}
	
    public Object clone() {
        return cloneViaReserialise();
    }

	protected int getDataSize() {
		return 12 + 4;
	}
}
