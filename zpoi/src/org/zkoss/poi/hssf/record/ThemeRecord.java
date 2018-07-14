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
import org.zkoss.poi.hssf.record.cont.ContinuableRecord;
import org.zkoss.poi.hssf.record.cont.ContinuableRecordOutput;
import org.zkoss.poi.util.HexDump;

/**
 * The theme used in this workbook.
 * see [MS-XLS].pdf 2.4.326 Theme page 573
 * @author henrichen@zkoss.org
 * @since 3.9.5
 */
public final class ThemeRecord extends ContinuableRecord {
    public static final int DEFAULT_THEME = 124226;

	private FtrHeader futureHeader;
	
	private int version;
	private byte _themedata[];
	
    public final static short sid = 0x0896;

    public ThemeRecord() {
		futureHeader = new FtrHeader();
		futureHeader.setRecordType(sid);
		
		setVersion(DEFAULT_THEME);
    }

	/**
	 * construct a HeaderFooterRecord record.  No fields are interpreted and the record will
	 * be serialized in its original form more or less
	 * @param in the RecordInputstream to read the record from
	 */
	public ThemeRecord(RecordInputStream in) {
		futureHeader = new FtrHeader(in);
		
		version = in.readInt();
		
		if (version == 0) {
			//TODO: custom theme
			_themedata = in.readRemainder();
		}
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
    public int getVersion(){
    	return version;
    }
    
    /**
     * Set to 0 if use custom theme; or DEFAULT_THEME.
     * 
     * @param version
     */
    public void setVersion(int version) {
    	this.version = version;
    }
    
    //TODO: temporary hold the theme data
    public byte[] getThemeData() {
    	return _themedata;
    }

    public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[THEME]\n");
		buffer.append(futureHeader.toString());
        buffer.append("    .version    = ")
  		  	  .append(Integer.toString(version)).append("\n");
        buffer.append("    .themeData  = ");
        if (version == 0)
  	  		buffer.append(HexDump.toHex(_themedata)).append("\n");
  	  	else
  	  		buffer.append("(n/a)\n");
        buffer.append("[/THEME]\n");
        return buffer.toString();
    }

    //HACK: do a "cheat" clone, see Record.java for more information
    public Object clone() {
                return cloneViaReserialise();
    }

	@Override
	protected void serialize(ContinuableRecordOutput out) {
		futureHeader.serialize(out);
		out.writeInt(version);
		if (_themedata != null)
			out.write(_themedata);
	}
}
