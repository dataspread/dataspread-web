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

//ZSS-746
/**
 * [MS-XLS].pdf 2.4.125 ForceFullCalculation page 298
 * Value of the forced calculation mode for this workbook.
 * @author henrichen@zkoss.org
 * @since 3.9.5
 */
public class ForceFullCalculationRecord extends StandardRecord  {
	
	public static final short sid = 0x08A3;

	private FtrHeader futureHeader;

	int fNoDeps;
	
	public ForceFullCalculationRecord() {
		futureHeader = new FtrHeader();
		futureHeader.setRecordType(sid);
	}

	public ForceFullCalculationRecord(RecordInputStream in) {
		futureHeader = new FtrHeader(in);

		fNoDeps = in.readInt();
	}
	
	public short getSid() {
		return sid;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[FORCEFULLCALCULATION]\n");
		buffer.append(futureHeader.toString());
		buffer.append("    .fNoDeps         = ").append(isNoDeps()).append("\n");
		buffer.append("[/FORCEFULLCALCULATION]\n");
		return buffer.toString();
	}
	
	/**
	 * Returns whether all cells in the workbook are calcuated or not.
	 * * true if dependencies are ignored and all cell formulas in this workbook
	 * fully calcuated every time a calculation is triggered
	 * * false if dependencies are respected and only formulas that depend on
	 * cells that changed in the workbook are calculated.
	 * @return
	 */
	public boolean isNoDeps() {
		return fNoDeps != 0;
	}
	
	public void setNoDeps(boolean flag) {
		fNoDeps = flag ? 1 : 0;
	}
	
	public void serialize(LittleEndianOutput out) {
		futureHeader.serialize(out);
		
		out.writeInt(fNoDeps);
	}
	
    public Object clone() {
        return cloneViaReserialise();
    }

	protected int getDataSize() {
		return 12 + 4;
	}
}
