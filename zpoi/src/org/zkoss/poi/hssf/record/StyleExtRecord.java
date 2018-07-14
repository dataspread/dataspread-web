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

import org.zkoss.poi.hssf.record.common.BuiltInStyle;
import org.zkoss.poi.hssf.record.common.FtrHeader;
import org.zkoss.poi.hssf.record.common.LPWideString;
import org.zkoss.poi.hssf.record.common.XFProp;
import org.zkoss.poi.hssf.record.cont.ContinuableRecord;
import org.zkoss.poi.hssf.record.cont.ContinuableRecordOutput;
import org.zkoss.poi.util.BitField;
import org.zkoss.poi.util.BitFieldFactory;
import org.zkoss.poi.util.HexDump;

import java.util.LinkedHashMap;

/**
 * The StyleExt record follow the Style record
 * @author henrichen
 * see [MS-XLS].pdf 2.4.270 StyleExt page 437
 */
public final class StyleExtRecord extends ContinuableRecord {
	private static final BitField fBuiltIn	= BitFieldFactory.getInstance(0x01);
	private static final BitField fHidden   = BitFieldFactory.getInstance(0x02);
	private static final BitField fCustom   = BitFieldFactory.getInstance(0x04);
	
	private FtrHeader futureHeader;
	private byte bits;
	private byte iCategory;
	
	public static final byte CUSTOM  = 0;
	public static final byte NEUTRAL = 1;
	public static final byte DATA_MODEL = 2;
	public static final byte TITLE = 3;
	public static final byte THEMED_CELL = 4;
	public static final byte NUMBER_FORMAT = 5;
	
	// [MS-XLS].pdf 2.5.16 BuiltInStyle page 622
	private BuiltInStyle builtInData;
	
	private LPWideString stName;
	private LinkedHashMap<Short, XFProp> xfProps;
	
    public final static short sid = 0x0892;

    public StyleExtRecord(int bits, int iCategory, BuiltInStyle builtInData, String stName, XFProp[] xfProps) {
		futureHeader = new FtrHeader();
		futureHeader.setRecordType(sid);
		
		this.bits = (byte) bits;
		this.iCategory = (byte) iCategory;
		this.builtInData = builtInData;
		this.stName = new LPWideString(stName);
		this.xfProps = new LinkedHashMap<Short, XFProp>(xfProps.length * 4 / 3);
		for (int j = 0; j < xfProps.length; ++j) {
			final XFProp prop = xfProps[j]; 
			this.xfProps.put(Short.valueOf((short)prop.getXFPropType()), prop);
		}
    }

	/**
	 * construct a HeaderFooterRecord record.  No fields are interpreted and the record will
	 * be serialized in its original form more or less
	 * @param in the RecordInputstream to read the record from
	 */
	public StyleExtRecord(RecordInputStream in) {
		futureHeader = new FtrHeader(in);
		
		bits = in.readByte();
		iCategory = in.readByte();
		builtInData = new BuiltInStyle(in);
		
		stName = new LPWideString(in);
		in.readShort(); //reserved
		int cprops = in.readShort();
		
		xfProps = new LinkedHashMap<Short, XFProp>(cprops * 4 / 3);
		for (int j = 0; j < cprops; ++j) {
			try {
				XFProp prop = new XFProp(in);
				xfProps.put(Short.valueOf((short)prop.getXFPropType()), prop);
			} catch(RuntimeException e) {
				System.out.println(e);
				throw e;
			}
		}
	}

    public short getSid()
    {
        return sid;
    }

    public XFProp getProperty(int xfPropType) {
    	return xfProps.get(Short.valueOf((short)xfPropType));
    }
    
    
    public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[STYLEEXT]\n");
		sb.append(futureHeader.toString());
		sb.append("    .bits       = ")
  		  	  .append(HexDump.intToHex(bits)).append("\n");
		sb.append("       .fBuiltIn            = ").append(isBuiltIn()).append("\n");
		sb.append("       .fHidden             = ").append(isHidden()).append("\n");
		sb.append("       .fCustom             = ").append(isCustom()).append("\n");
		sb.append("    .iCategory  = ")
	  	  	  .append(HexDump.byteToHex(getCategory())).append("\n");
		sb.append("    .builtInData= \n");
		builtInData.appendString(sb, "        ");
		sb.append("    .stName                 = \n");
	  	stName.appendString(sb, "        ");
		for(XFProp prop : xfProps.values()) {
			sb.append("    .XFProp\n");
			prop.appendString(sb, "    ");
		}
        sb.append("[/STYLEEXT]\n");
        return sb.toString();
    }

    public boolean isBuiltIn() {
    	return fBuiltIn.isSet(bits);
    }
    
    public void setBuiltIn(boolean flag) {
    	bits = fBuiltIn.setByteBoolean(bits, flag);
    }
    
    public boolean isHidden() {
    	return fHidden.isSet(bits);
    }
    
    public void setHidden(boolean flag) {
    	bits = fHidden.setByteBoolean(bits, flag);
    }
    
    public boolean isCustom() {
    	return fCustom.isSet(bits);
    }
    
    public void setCustom(boolean flag) {
    	bits = fCustom.setByteBoolean(bits, flag);
    }
    
    public int getCategory() {
    	return ((int)iCategory) & 0xff;
    }
    
    public void setCategory(int category) {
    	iCategory = (byte) category;
    }
    
    public BuiltInStyle getBuiltInData() {
    	return builtInData;
    }
    
    public void setBuiltInData(BuiltInStyle data) {
    	builtInData = data;
    }
    
    public String getName() {
    	return stName.getString();
    }
    
    //HACK: do a "cheat" clone, see Record.java for more information
    public Object clone() {
                return cloneViaReserialise();
    }

	protected int getDataSize() {
		int result = 12 + 4;
		result += stName.getDataSize();
		for (XFProp prop : xfProps.values()) {
			result += prop.getDataSize();
		}
		return result;
	}

    
	@Override
	protected void serialize(ContinuableRecordOutput out) {
		futureHeader.serialize(out);
		out.writeByte(bits);
		out.writeByte(iCategory);
		builtInData.serialize(out);
		stName.serialize(out);
		int cprops = xfProps.size();
		out.writeShort(0); //reserved
		out.writeShort(cprops);
		for (XFProp prop : xfProps.values()) {
			prop.serialize(out);
		}
	}
}