
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

import org.zkoss.poi.util.BitField;
import org.zkoss.poi.util.BitFieldFactory;
import org.zkoss.poi.util.LittleEndianOutput;

/**
 * Title:        Default Row Height Record per sheet
 * Description:  Row height for rows with undefined or not explicitly defined
 *               heights.
 * REFERENCE:  PG 301 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * [MS-XLS].pdf 2.4.87 DefaultRowHeight page 265
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @author Henri Chen (henrichen@zkoss.org)
 * @version 2.0-pre
 */

public final class DefaultRowHeightRecord  extends StandardRecord {
	private static final BitField fUnsynced		= BitFieldFactory.getInstance(0x01);
	private static final BitField fDyZero		= BitFieldFactory.getInstance(0x02);
	private static final BitField fExAsc		= BitFieldFactory.getInstance(0x04);
	private static final BitField fExDsc		= BitFieldFactory.getInstance(0x04);
	
	public final static short sid = 0x225;
    private short             bits;
    private short             miyRw; // 1 ~ 8179

    /**
     * The default row height for empty rows is 300 twips (0x12c)
     */
    public static final short DEFAULT_ROW_HEIGHT = 300; //twips
    
    public DefaultRowHeightRecord() {
        setRowHeight(DEFAULT_ROW_HEIGHT);
    }

    public DefaultRowHeightRecord(RecordInputStream in)
    {
        bits = in.readShort();
        miyRw   = in.readShort();
    }

    /**
     * get the default row height
     * @return rowheight for undefined rows/rows w/undefined height
     */
    public short getRowHeight()
    {
        return miyRw;
    }
    public void setRowHeight(short height)
    {
    	if (height < 1 || height > 8179) {
    		height = DEFAULT_ROW_HEIGHT;
    	}
        miyRw = height;
    }

    public boolean isDefaultChanged() {
    	return fUnsynced.isSet(bits);
    }
    public void setDefaultChanged(boolean flag) {
    	bits = (short) fUnsynced.setBoolean(bits, flag);
    }
    
    
    public boolean isZeroHeight() {
    	return fDyZero.isSet(bits);
    }
    public void setZeroHeight(boolean flag) {
    	bits = (short) fDyZero.setBoolean(bits, flag);
    }
    
    public boolean isThickTopBorder() {
    	return fExAsc.isSet(bits);
    }
    public void setThickTopBorder(boolean flag) {
    	bits = (short) fExAsc.setBoolean(bits, flag);
    }
    
    public boolean isThickBottomBorder() {
    	return fExDsc.isSet(bits);
    }
    public void setThickBottomBorder(boolean flag) {
    	bits = (short) fExDsc.setBoolean(bits, flag);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[DEFAULTROWHEIGHT]\n");
        buffer.append("    .bits           = ")
              .append(Integer.toHexString(bits)).append("\n");
		buffer.append("       .fUnsynced           = ").append(isDefaultChanged()).append("\n");
		buffer.append("       .fDyZero             = ").append(isZeroHeight()).append("\n");
		buffer.append("       .fExAsc              = ").append(isThickTopBorder()).append("\n");
		buffer.append("       .fExDsc              = ").append(isThickBottomBorder()).append("\n");
        buffer.append("    .rowheight      = ")
            .append(Integer.toHexString(getRowHeight())).append("\n");
        buffer.append("[/DEFAULTROWHEIGHT]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(bits);
       	out.writeShort(miyRw);
    }

    protected int getDataSize() {
        return 4;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
      DefaultRowHeightRecord rec = new DefaultRowHeightRecord();
      rec.bits = bits;
      rec.miyRw = miyRw;
      return rec;
    }
}
