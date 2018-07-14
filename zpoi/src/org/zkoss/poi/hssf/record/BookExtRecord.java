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
 * [MS-XLS].pdf 2.4.23 BookExt page 203
 * properties of a workbook file
 * @author henrichen@zkoss.org
 */
public class BookExtRecord extends StandardRecord  {
	private static final BitField fDontAutoRecover	= BitFieldFactory.getInstance(0x01);
	private static final BitField fHidePivotList	= BitFieldFactory.getInstance(0x02);
	private static final BitField fFilterPrivacy 	= BitFieldFactory.getInstance(0x04);
	private static final BitField fEmbedFactoids    = BitFieldFactory.getInstance(0x08);
	private static final BitField mdFactoidDisplay = BitFieldFactory.getInstance(0x30);
	private static final BitField fSavedDuringRecovery = BitFieldFactory.getInstance(0x40);
	private static final BitField fCreatedViaDataRecovery = BitFieldFactory.getInstance(0x80);
	private static final BitField fOpenedViaDataRecovery = BitFieldFactory.getInstance(0x100);
	private static final BitField fOpenedViaSafeLoad = BitFieldFactory.getInstance(0x200);
	
	//grbit1
	private static final BitField fBuggedUserAboutSolution = BitFieldFactory.getInstance(0x01);
	private static final BitField fShowInkAnnotation = BitFieldFactory.getInstance(0x02);
	
	//grbit2
	private static final BitField fPublishedBookItems = BitFieldFactory.getInstance(0x02);
	private static final BitField fShowPivotChartFilter = BitFieldFactory.getInstance(0x04);
	
	
	public static final short sid = 0x0863;

	private FtrHeader futureHeader;

	private int bits;
	private int cb;
	private byte grbit1;
	private byte grbit2;
	
	public BookExtRecord() {
		futureHeader = new FtrHeader();
		futureHeader.setRecordType(sid);
		cb = 22;
	}

	public BookExtRecord(RecordInputStream in) {
		futureHeader = new FtrHeader(in);
		
		cb = in.readInt();
		bits = in.readInt();
	
		if (cb > 20) { // cb is 21 or 22
			grbit1 = in.readByte();
			if (cb > 21) { // cb is 22
				grbit2 = in.readByte();
			}
		}
	}
	
	public short getSid() {
		return sid;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[BOOKEXT]\n");
		buffer.append(futureHeader.toString());
        buffer.append("    .cb         = ")
        	  .append(Integer.toString(cb)).append("\n");
		buffer.append("    .bits       = ")
  	  		  .append(Integer.toHexString(bits)).append("\n");
		buffer.append("       .fDontAutoRecover         = ").append(isDontAutoRecover()).append("\n");
		buffer.append("       .fHidePivotList           = ").append(isHidePivotList()).append("\n");
		buffer.append("       .fFilterPrivacy           = ").append(isFilterPrivacy()).append("\n");
		buffer.append("       .fEmbedFactoids           = ").append(isEmbedFactoids()).append("\n");
		buffer.append("       .mdFactoidDisplay         = ").append(getFactoidDisplay()).append("\n");
		buffer.append("       .fSavedDuringRecovery     = ").append(isSavedDuringRecovery()).append("\n");
		buffer.append("       .fCreatedViaDataRecovery  = ").append(isCreatedViaDataRecovery()).append("\n");
		buffer.append("       .fOpenedViaDataRecovery   = ").append(isOpenedViaDataRecovery()).append("\n");
		buffer.append("       .fOpenedViaSafeLoad       = ").append(isOpenedViaSafeLoad()).append("\n");

		//grbit1
		if (cb > 20) {
			buffer.append("    .grbit1     = ")
	  		  	  .append(Integer.toHexString(grbit1)).append("\n");
			
			buffer.append("       .fBuggedUserAboutSolution = ").append(isBuggedUserAboutSolution()).append("\n");
			buffer.append("       .fShowInkAnnotation       = ").append(isShowInkAnnotation()).append("\n");
			//grbit2
			if (cb > 21) {
				buffer.append("    .grbit2     = ")
	  		  	  	  .append(Integer.toHexString(grbit2)).append("\n");
				buffer.append("       .fPublishedBookItems      = ").append(isPublishOnlySelectedBookItems()).append("\n");
				buffer.append("       .fShowPivotChartFilter    = ").append(isShowPivotChartFilter()).append("\n");
			} else {
				buffer.append("    .grbit2     = (n/a)").append("\n");
			}
		} else {
			buffer.append("    .grbit1     = (n/a)").append("\n");
			buffer.append("    .grbit2     = (n/a)").append("\n");
		}
		buffer.append("[/BOOKEXT]\n");
		return buffer.toString();
	}
	
	public boolean isDontAutoRecover() {
		return fDontAutoRecover.isSet(bits);
	}
	public void setDontAutoRecover(boolean flag) {
		bits = fDontAutoRecover.setBoolean(bits, flag);
	}

	public boolean isHidePivotList() {
		return fHidePivotList.isSet(bits);
	}
	public void setHidePivotList(boolean flag) {
		bits = fHidePivotList.setBoolean(bits, flag);
	}
	
	public boolean isFilterPrivacy() {
		return fFilterPrivacy.isSet(bits);
	}
	public void setFilterPrivacy(boolean flag) {
		bits = fFilterPrivacy.setBoolean(bits, flag);
	}

	public boolean isEmbedFactoids() {
		return fEmbedFactoids.isSet(bits);
	}
	public void setEmbedFactoids(boolean flag) {
		bits = fEmbedFactoids.setBoolean(bits, flag);
	}
	
	// 0: display both smart tag action button and indicator
	// 1; display only smart tag action button 
	// 2: display neither smart tag action button nor indicator 
	public int getFactoidDisplay() {
		return mdFactoidDisplay.getValue(bits);
	}
	public void setFactoidDisplay(int value) {
		if (value < 0 || value > 2)
			value = 2;
		mdFactoidDisplay.setValue(bits, value);
	}
	
	
	public boolean isSavedDuringRecovery() {
		return fSavedDuringRecovery.isSet(bits);
	}
	public void setSavedDuringRecovery(boolean flag) {
		bits = fSavedDuringRecovery.setBoolean(bits, flag);
	}

	public boolean isCreatedViaDataRecovery() {
		return fCreatedViaDataRecovery.isSet(bits);
	}
	public void setCreatedViaDataRecovery(boolean flag) {
		bits = fCreatedViaDataRecovery.setBoolean(bits, flag);
	}

	public boolean isOpenedViaDataRecovery() {
		return fOpenedViaDataRecovery.isSet(bits);
	}
	public void setOpenedViaDataRecovery(boolean flag) {
		bits = fOpenedViaDataRecovery.setBoolean(bits, flag);
	}

	public boolean isOpenedViaSafeLoad() {
		return fOpenedViaSafeLoad.isSet(bits);
	}
	public void setOpenedViaSafeLoad(boolean flag) {
		bits = fOpenedViaSafeLoad.setBoolean(bits, flag);
	}

	//grbit1
	public boolean isBuggedUserAboutSolution() {
		return fBuggedUserAboutSolution.isSet(grbit1);
	}
	public void setBuggedUserAboutSolution(boolean flag) {
		grbit1 = (byte) fBuggedUserAboutSolution.setBoolean(grbit1, flag);
	}

	public boolean isShowInkAnnotation() {
		return fShowInkAnnotation.isSet(grbit1);
	}
	public void setShowInkAnnotation(boolean flag) {
		grbit1 = (byte) fShowInkAnnotation.setBoolean(grbit1, flag);
	}
	
	//grbit2
	public boolean isPublishOnlySelectedBookItems() {
		return fPublishedBookItems.isSet(grbit2);
	}
	public void setPublishOnlySelectedBookItems(boolean flag) {
		grbit2 = (byte) fPublishedBookItems.setBoolean(grbit2, flag);
	}

	public boolean isShowPivotChartFilter() {
		return fShowPivotChartFilter.isSet(grbit2);
	}
	public void setShowPivotChartFilter(boolean flag) {
		grbit2 = (byte) fShowPivotChartFilter.setBoolean(grbit2, flag);
	}

	
	public void serialize(LittleEndianOutput out) {
		futureHeader.serialize(out);
		
		out.writeInt(cb);
		out.writeInt(bits);
		
//		if (cb > 20) {
//			out.writeByte(grbit1);
//			if (cb > 21) {
//				out.writeByte(grbit2);
//			} else {
//				out.writeByte(0);
//			}
//		} else {
//			out.writeByte(0);
//			out.writeByte(0);
//		}
		out.writeByte(grbit1);
		out.writeByte(grbit2);
	}
	
    public Object clone() {
        return cloneViaReserialise();
    }

	protected int getDataSize() {
//		int sz = 12 + 4 * 2;
//		if (grbit1 != 0 || grbit2 != 0)
//			sz += 1;
//		if (grbit2 != 0)
//			sz += 1;
//		return sz;
		return 12 + 4 * 2 + 2;
	}
}
