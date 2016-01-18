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
 * [MS-XLS].pdf 2.4.173 MTRSettings page 343
 * Multithreaded calculation settings.
 * @author henrichen@zkoss.org
 */
public class MTRSettingsRecord extends StandardRecord  {
	
	public static final short sid = 0x089A;

	private FtrHeader futureHeader;

	int fMTREnabled;
	int fUserSetThreadCount;
	int cUserThreadCount; // 1 ~ 0x400
	
	public MTRSettingsRecord() {
		futureHeader = new FtrHeader();
		futureHeader.setRecordType(sid);
	}

	public MTRSettingsRecord(RecordInputStream in) {
		futureHeader = new FtrHeader(in);

		fMTREnabled = in.readInt();
		fUserSetThreadCount = in.readInt();
		cUserThreadCount = in.readInt();
	}
	
	public short getSid() {
		return sid;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[MTRSettings]\n");
		buffer.append(futureHeader.toString());
		buffer.append("    .fMTREnabled         = ").append(isMTREnabled()).append("\n");
		buffer.append("    .fUserSetThreadCount = ").append(isUserSetThreadCount()).append("\n");
		buffer.append("    .cUserThreadCount    = ").append(getUserThreadCount()).append("\n");
		buffer.append("[/MTRSettings]\n");
		return buffer.toString();
	}
	
	/**
	 * Returns count of calculation threads. Must between 1 ~ 0x400.
	 * If either fMTREnabled or fUserSeetThreadCount is false, ignore this value 
	 * @return
	 */
	public int getUserThreadCount() {
		return cUserThreadCount;
	}
	
	public void setUserThreadCount(int count) {
		if (count < 1 || count > 0x400) {
			count = 1;
		}
		cUserThreadCount = count;
	}
	
	/**
	 * Returns whether the multi-threaded calculation is enabled.
	 * @return
	 */
	public boolean isMTREnabled() {
		return fMTREnabled != 0;
	}
	
	/**
	 * Returns whether the thread count was manually specified by the user.
	 * @return
	 */
	public boolean isUserSetThreadCount() {
		return fUserSetThreadCount != 0;
	}
	
	public void setMTREnabled(boolean flag) {
		fMTREnabled = flag ? 1 : 0;
	}

	public void setUserSetThreadCount(boolean flag) {
		fUserSetThreadCount = flag ? 1 : 0;
	}
	
	public void serialize(LittleEndianOutput out) {
		futureHeader.serialize(out);
		
		out.writeInt(fMTREnabled);
		out.writeInt(fUserSetThreadCount);
		out.writeInt(cUserThreadCount);
	}
	
    public Object clone() {
        return cloneViaReserialise();
    }

	protected int getDataSize() {
		return 12 + 4 * 3;
	}
}
