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
 * Title: FeatHdr (Feature Header) Record
 * <P>
 * This record specifies common information for Shared Features, and 
 *  specifies the beginning of a collection of records to define them. 
 * The collection of data (Globals Substream ABNF, macro sheet substream 
 *  ABNF or worksheet substream ABNF) specifies Shared Feature data.
 */
public final class FeatHdrRecord extends StandardRecord  {
	/**
	 * Specifies the enhanced protection type. Used to protect a 
	 * shared workbook by restricting access to some areas of it 
	 */
	public static final int SHAREDFEATURES_ISFPROTECTION = 0x02;
	/**
	 * Specifies that formula errors should be ignored 
	 */
	public static final int SHAREDFEATURES_ISFFEC2       = 0x03;
	/**
	 * Specifies the smart tag type. Recognises certain
	 * types of entries (proper names, dates/times etc) and
	 * flags them for action 
	 */
	public static final int SHAREDFEATURES_ISFFACTOID    = 0x04;
	/**
	 * Specifies the shared list type. Used for a table
	 * within a sheet
	 */
	public static final int SHAREDFEATURES_ISFLIST       = 0x05;
    
	public final static short sid = 0x0867;

	private FtrHeader futureHeader;
	private int isf_sharedFeatureType; // See SHAREDFEATURES_
	private byte reserved = (byte) 0x01; // ZSS-746: Should always be one!
	/** 
	 * 0x00000000 = rgbHdrData not present
	 * 0xffffffff = rgbHdrData present
	 */
	private int cbHdrData;
	/** We need a BOFRecord to make sense of this... */
	private byte[] rgbHdrData;

	/** shared object */ //20140409, henrichen: EnhancedProtection
	private Object shared;
	
	public FeatHdrRecord() {
		futureHeader = new FtrHeader();
		futureHeader.setRecordType(sid);
	}

	public short getSid() {
		return sid;
	}

	//20140409, henrichen: EnhancedProtection
	public FeatHdrRecord(int sharedFutureType) {
		this();
		isf_sharedFeatureType = sharedFutureType;
        switch(isf_sharedFeatureType) {
        case SHAREDFEATURES_ISFPROTECTION:
        	shared = new EnhancedProtection();
        	cbHdrData = 0xffffffff;
        	break;
        case SHAREDFEATURES_ISFFACTOID:
        case SHAREDFEATURES_ISFFEC2:
        case SHAREDFEATURES_ISFLIST:
        	//TODO: other SHAREDFUTUERS
        	break;
        default:
        	throw new RuntimeException("Unknown shardFutureType: " + sharedFutureType);
        }
	}
	
	public FeatHdrRecord(RecordInputStream in) {
		futureHeader = new FtrHeader(in);
		
		isf_sharedFeatureType = in.readShort();
		reserved = in.readByte();
		cbHdrData = in.readInt();
		//20140409, henrichen: Handle EnhancedProtection
        switch(isf_sharedFeatureType) {
        case SHAREDFEATURES_ISFPROTECTION:
        	shared = cbHdrData == 0xffffffff ? new EnhancedProtection(in) : new EnhancedProtection();
        	break;
        case SHAREDFEATURES_ISFFACTOID:
        case SHAREDFEATURES_ISFFEC2:
        case SHAREDFEATURES_ISFLIST:
        default:
    		rgbHdrData = in.readRemainder();
        	//TODO: other SHAREDFUTUERS
        	break;
        }
	}

	//20140409, henrichen: Handle EnhancedProtection
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[FEATURE HEADER]\n");
		buffer.append(futureHeader.toString());
		buffer.append("    .isf        = ")
        	  .append(Integer.toHexString(isf_sharedFeatureType)).append("\n");
		buffer.append("    .reserved   = ")
		      .append(HexDump.byteToHex(reserved)).append("\n");
        buffer.append("    .cbHdrData  = ")
    		  .append(Integer.toHexString(cbHdrData)).append("\n");
        switch(isf_sharedFeatureType) {
        case SHAREDFEATURES_ISFPROTECTION:
        	buffer.append(shared.toString());
        	break;
        case SHAREDFEATURES_ISFFACTOID:
        case SHAREDFEATURES_ISFFEC2:
        case SHAREDFEATURES_ISFLIST:
        default:
        	buffer.append("  rgbHdrData=").append(HexDump.toHex(rgbHdrData)).append("\n");
        }
		
		buffer.append("[/FEATURE HEADER]\n");
		return buffer.toString();
	}

	public void serialize(LittleEndianOutput out) {
		futureHeader.serialize(out);
		
		out.writeShort(isf_sharedFeatureType);
		out.writeByte(reserved);
		out.writeInt((int)cbHdrData);
		//20140409, henrichen: Handle EnhancedProtection
        switch(isf_sharedFeatureType) {
        case SHAREDFEATURES_ISFPROTECTION:
        	((EnhancedProtection)shared).serialize(out);
        	break;
        case SHAREDFEATURES_ISFFACTOID:
        case SHAREDFEATURES_ISFFEC2:
        case SHAREDFEATURES_ISFLIST:
        default:
    		out.write(rgbHdrData);
        	//TODO: other SHAREDFUTUERS
        	break;
        }
	}

	//20140409, henrichen: Handle EnhancedProtection
	protected int getDataSize() {
		int sz = 12 + 2+1+4;
        switch(isf_sharedFeatureType) {
        case SHAREDFEATURES_ISFPROTECTION:
        	sz += 4;
        	break;
        case SHAREDFEATURES_ISFFACTOID:
        case SHAREDFEATURES_ISFFEC2:
        case SHAREDFEATURES_ISFLIST:
        default:
    		sz += rgbHdrData.length;
        	//TODO: other SHAREDFUTUERS
        	break;
        }
		return sz;
	}
    
    //HACK: do a "cheat" clone, see Record.java for more information
    public Object clone() {
        return cloneViaReserialise();
    }

	//20140409, henrichen: ZSS-576, EnhancedProtection
	public boolean isEnhancedProtection() {
		return isf_sharedFeatureType == SHAREDFEATURES_ISFPROTECTION;
	}
	
	//20140409, henrichen: ZSS-576, EnhancedProtection
	public EnhancedProtection getEnhancedProtection() {
		return isEnhancedProtection() ? (EnhancedProtection) shared : null;
	}
}
