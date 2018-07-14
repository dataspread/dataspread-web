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
 * [MS-XLS].pdf 2.4.55 CompressPictures page 231
 * Recomendation for picture compression when saving.
 * @author henrichen@zkoss.org
 */
public class CompressPicturesRecord extends StandardRecord  {
	private static final BitField fAutoCompressPictures = BitFieldFactory.getInstance(0x01);
	
	public static final short sid = 0x089b;

	private FtrHeader futureHeader;

	int bits;
	
	public CompressPicturesRecord() {
		futureHeader = new FtrHeader();
		futureHeader.setRecordType(sid);
	}

	public CompressPicturesRecord(RecordInputStream in) {
		futureHeader = new FtrHeader(in);
		
		bits = in.readInt();
	}
	
	public short getSid() {
		return sid;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[COMPRESSPICTURES]\n");
		buffer.append(futureHeader.toString());
		buffer.append("    .bits       = ")
  	  		  .append(Integer.toHexString(bits)).append("\n");
		buffer.append("       .fAutoCompressPictures     = ").append(isAutoCompressPictures()).append("\n");
		buffer.append("[/COMPERSSPICTURES]\n");
		return buffer.toString();
	}
	
	public boolean isAutoCompressPictures() {
		return fAutoCompressPictures.isSet(bits);
	}
	public void setAutoCompressPictures(boolean flag) {
		bits = fAutoCompressPictures.setBoolean(bits, flag);
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
