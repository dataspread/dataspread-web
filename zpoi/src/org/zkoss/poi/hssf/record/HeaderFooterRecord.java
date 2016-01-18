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

import org.zkoss.poi.hssf.record.aggregates.PageSettingsBlock;
import org.zkoss.poi.hssf.record.common.FtrHeader;
import org.zkoss.poi.hssf.record.common.UnicodeString;
import org.zkoss.poi.hssf.record.cont.ContinuableRecord;
import org.zkoss.poi.hssf.record.cont.ContinuableRecordOutput;
import org.zkoss.poi.util.BitField;
import org.zkoss.poi.util.BitFieldFactory;
import org.zkoss.poi.util.HexDump;
import org.zkoss.poi.util.LittleEndianOutput;

import java.util.Arrays;

/**
 * The HEADERFOOTER record stores information added in Office Excel 2007 for headers/footers.
 * see [MS-XLS].pdf 2.4.137 HeaderFooter page 316
 * @author Yegor Kozlov
 */
public final class HeaderFooterRecord extends ContinuableRecord {
    private static final byte[] BLANK_GUID = new byte[16];
	private static final BitField fHFDiffOddEven	= BitFieldFactory.getInstance(0x01);
	private static final BitField fHFDiffFirst		= BitFieldFactory.getInstance(0x02);
	private static final BitField fHFScaleWidthDoc	= BitFieldFactory.getInstance(0x04);
	private static final BitField fHFAlignMargins	= BitFieldFactory.getInstance(0x08);

	private FtrHeader futureHeader;
	byte[] guidSView = new byte[16];
	private short bits;
	private short cchHeaderEven;
	private short cchFooterEven;
	private short cchHeaderFirst;
	private short cchFooterFirst;
	
	private UnicodeString strHeaderEven;
	private UnicodeString strFooterEven;
	private UnicodeString strHeaderFirst;
	private UnicodeString strFooterFirst;
	
    public final static short sid = 0x089C;

    public HeaderFooterRecord() {
		futureHeader = new FtrHeader();
		futureHeader.setRecordType(sid);
    }

	/**
	 * construct a HeaderFooterRecord record.  No fields are interpreted and the record will
	 * be serialized in its original form more or less
	 * @param in the RecordInputstream to read the record from
	 */
	public HeaderFooterRecord(RecordInputStream in) {
		futureHeader = new FtrHeader(in);
		
		in.read(guidSView, 0, 16);
		bits = in.readShort();
		
		cchHeaderEven = in.readShort();
		cchFooterEven = in.readShort();
		cchHeaderFirst = in.readShort();
		cchFooterFirst = in.readShort();
		
		if (cchHeaderEven > 0)
			strHeaderEven = new UnicodeString(in);
		if (cchFooterEven > 0)
			strFooterEven = new UnicodeString(in);
		if (cchHeaderFirst > 0)
			strHeaderFirst = new UnicodeString(in);
		if (cchFooterFirst > 0)
			strFooterFirst = new UnicodeString(in);
	}

    public short getSid()
    {
        return sid;
    }

    /**
     * If this header belongs to a specific sheet view , the sheet view?s GUID will be saved here.
     * <p>
     * If it is zero, it means the current sheet. Otherwise, this field MUST match the guid field
     * of the preceding {@link UserSViewBegin} record.
     *
     * @return the sheet view?s GUID
     */
    public byte[] getGuid(){
    	return guidSView;
    }
    
    public void setGuid(byte[] guid) {
    	guidSView = Arrays.copyOf(guid, 16);
    }

    /**
     * @return whether this record belongs to the current sheet 
     */
    public boolean isCurrentSheet(){
        return Arrays.equals(getGuid(), BLANK_GUID);
    }
    
    /** 
     * Returns whether the odd and even pages use a different header and footer 
     */
    public boolean isDiffOddEven() {
    	return fHFDiffOddEven.isSet(bits);
    }
    
    /**
     * Set whether the odd and even pages use a different header and footer. It
     * has the side effect which will clear HeaderEven and FooterEven string if
     * set to false.
     * @param flag
     */
    public void setDiffOddEven(boolean flag) {
    	bits = (short) fHFDiffOddEven.setBoolean(bits, flag);
    	if (!flag) {
    		setHeaderEven(null);
    		setFooterEven(null);
    	}
    }
    
    /**
     * Returns whether the first page use a different header and footer.
     */
    public boolean isDiffFirst() {
    	return fHFDiffFirst.isSet(bits);
    }
    
    /**
     * Sets whether the first page use a different header and footer. It
     * has the side effect which will clear HeaderFirst and FooterFirst string
     * if set to false.
     * @param flag
     */
    public void setDiffFirst(boolean flag) {
    	bits = (short) fHFDiffFirst.setBoolean(bits, flag);
    	if (!flag) {
    		setHeaderFirst(null);
    		setFooterFirst(null);
    	}
    }
    
    /**
     * Returns whether the header and footer is scaled with the sheet. 
     */
    public boolean isScaleWithDoc() {
    	return fHFScaleWidthDoc.isSet(bits);
    }
    
    public void setScaleWithDoc(boolean flag) {
    	bits = (short) fHFScaleWidthDoc.setBoolean(bits, flag);
    }
    /**
     * Returns whether the left and right edges of the header and footer are
     * lined up with the left and right margins of the sheet.
     */
    public boolean isAlignMargins() {
    	return fHFAlignMargins.isSet(bits);
    }
    public void setAlignMargins(boolean flag) {
    	bits = (short) fHFAlignMargins.setBoolean(bits, flag);
    }

    private boolean isEmpty(String str) {
    	return str == null || str.length() == 0; 
    }
    
    public String getHeaderEven() {
    	return strHeaderEven == null ? "" : strHeaderEven.getString();
    }
    
    public void setHeaderEven(String str) {
    	final boolean empty = isEmpty(str);
    	strHeaderEven = empty ? null : new UnicodeString(str);
		cchHeaderEven = empty ? 0 : strHeaderEven.getCharCountShort();
    }
    
	public String getFooterEven() {
		return strFooterEven == null ? "" : strFooterEven.getString();
	}
	
	public void setFooterEven(String str) {
		final boolean empty = isEmpty(str);
		strFooterEven = empty ? null : new UnicodeString(str);
		cchFooterEven = empty ? 0 : strFooterEven.getCharCountShort();
	}
	
	public String getHeaderFirst() {
		return strHeaderFirst == null ? "" : strHeaderFirst.getString();
	}
	
	public void setHeaderFirst(String str) {
		final boolean empty = isEmpty(str);
		strHeaderFirst = empty ? null : new UnicodeString(str);
		cchHeaderFirst = empty ? 0 : strHeaderFirst.getCharCountShort();
	}
	
	public String getFooterFirst() {
		return strFooterFirst == null ? "" : strFooterFirst.getString();
	}
	
	public void setFooterFirst(String str) {
		final boolean empty = isEmpty(str);
		strFooterFirst = empty ? null : new UnicodeString(str);
		cchFooterFirst = empty ? 0 : strFooterFirst.getCharCountShort();
	}
    
    public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[HEADERFOOTER]\n");
		buffer.append(futureHeader.toString());
        buffer.append("    .guidSView  = ")
  	  		  .append(HexDump.toHex(guidSView)).append("\n");
		buffer.append("    .bits       = ")
  		  	  .append(Integer.toHexString(bits)).append("\n");
		buffer.append("       .fHFDiffOddEven      = ").append(isDiffOddEven()).append("\n");
		buffer.append("       .fHFDiffFirst        = ").append(isDiffFirst()).append("\n");
		buffer.append("       .fHFScaleWithDoc     = ").append(isScaleWithDoc()).append("\n");
		buffer.append("       .fHFAlignMargins     = ").append(isAlignMargins()).append("\n");
		buffer.append("    .cchHeaderEven  = ")
	  	  	  .append(Integer.toString(cchHeaderEven)).append("\n");
		buffer.append("    .cchFooterEven  = ")
	  	  .append(Integer.toString(cchFooterEven)).append("\n");
		buffer.append("    .cchHeaderFirst = ")
	  	  .append(Integer.toString(cchHeaderFirst)).append("\n");
		buffer.append("    .cchFooterFirst = ")
	  	  .append(Integer.toString(cchFooterFirst)).append("\n");
		buffer.append("    .strHeaderEven  = ")
	  	  .append(strHeaderEven == null ? "(n/a)" : strHeaderEven.getDebugInfo()).append("\n");
		buffer.append("    .strFooterEven  = ")
		  .append(strFooterEven == null ? "(n/a)" : strFooterEven.getDebugInfo()).append("\n");
		buffer.append("    .strHeaderFirst = ")
		  .append(strHeaderFirst == null ? "(n/a)" : strHeaderFirst.getDebugInfo()).append("\n");
		buffer.append("    .strFooterFirst = ")
		  .append(strFooterFirst == null ? "(n/a)" : strFooterFirst.getDebugInfo()).append("\n");
        buffer.append("[/HEADERFOOTER]\n");
        return buffer.toString();
    }

    //HACK: do a "cheat" clone, see Record.java for more information
    public Object clone() {
                return cloneViaReserialise();
    }

	@Override
	protected void serialize(ContinuableRecordOutput out) {
		futureHeader.serialize(out);
		out.write(guidSView);
		out.writeShort(bits);
		out.writeShort(cchHeaderEven);
		out.writeShort(cchFooterEven);
		out.writeShort(cchHeaderFirst);
		out.writeShort(cchFooterFirst);
		
		if (cchHeaderEven > 0)
			strHeaderEven.serialize(out);
		if (cchFooterEven > 0)
			strFooterEven.serialize(out);
		if (cchHeaderFirst > 0)
			strHeaderFirst.serialize(out);
		if (cchFooterFirst > 0)
			strFooterFirst.serialize(out);
	}
}