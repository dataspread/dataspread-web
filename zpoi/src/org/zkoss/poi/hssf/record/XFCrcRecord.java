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
import org.zkoss.poi.util.HexDump;
import org.zkoss.poi.util.LittleEndianOutput;

/**
 * Title: XFCRC Record
 */
public final class XFCrcRecord extends StandardRecord  {
	public final static short sid = 0x087C;
	
	private FtrHeader futureHeader;
	
	/**
	 * See 2.4.354 XFCRC at [MS.XLS].pdf, page 609 
	 */
	private short reserved1; // Should always be zero
	private int cxfs;
	private long crc; 
	
	public XFCrcRecord() {
		futureHeader = new FtrHeader();
		futureHeader.setRecordType(sid);
	}

	public short getSid() {
		return sid;
	}

	public XFCrcRecord(RecordInputStream in) {
		futureHeader = new FtrHeader(in);
		reserved1 = in.readShort();
		cxfs = in.readUShort();
		crc = in.readInt() & 0xffffffffL;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[XFCRC]\n");
		sb.append("    .cxfs =").append(cxfs).append("\n");
		sb.append("    .crc  =").append(HexDump.intToHex((int)crc)).append("\n");
		sb.append("[/XFCRC]\n");
		return sb.toString();
	}

	public void serialize(LittleEndianOutput out) {
		futureHeader.serialize(out);
		
		out.writeShort(reserved1);
		out.writeShort(cxfs);
		out.writeInt((int)crc);
	}

	protected int getDataSize() {
		return 12 + 2+2+4;
	}

	public int getCxfs() {
		return cxfs;
	}
	public void setCxfs(int cxfs) {
		this.cxfs = cxfs;
	}
	public long getCrc() {
		return crc;
	}
	public void setCrc(long crc) {
		this.crc = crc;
	}
    
    //HACK: do a "cheat" clone, see Record.java for more information
    public Object clone() {
        return cloneViaReserialise();
    }
}
