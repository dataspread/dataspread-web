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
import org.zkoss.poi.util.HexDump;
import org.zkoss.poi.util.LittleEndianInput;
import org.zkoss.poi.util.LittleEndianOutput;

/**
 * ftCf (0x0007)<p/>
 * The clipboard picture format.<p/>
 * 
 * @author henrichen@zkoss.rog
 */
public final class FtCfSubRecord extends SubRecord {
    public final static short sid = 0x0007;

    private  short      field_1_cf;
    public final static short FORMAT_EMF = 0x0002;
    public final static short FORMAT_BMP = 0x0009;
    public final static short FORMAT_UNKNOWN = (short) 0xFFFF;

    public FtCfSubRecord()
    {

    }

    public FtCfSubRecord(LittleEndianInput in, int size) {
        if (size != 2) {
            throw new RecordFormatException("Expected size 2 but got (" + size + ")");
        }
        field_1_cf             = in.readShort();
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[ftCf]\n");
        buffer.append("    .cf           = ")
            .append("0x").append(HexDump.toHex(  getClipboardPictureFormat ()))
            .append(" (").append( getClipboardPictureFormat() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("[/ftCf]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {

        out.writeShort(sid);
        out.writeShort(getDataSize());

        out.writeShort(field_1_cf);
    }

	protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        FtCfSubRecord rec = new FtCfSubRecord();
    
        rec.field_1_cf = field_1_cf;
        return rec;
    }


    /**
     * Get the clipboard picture format.
     *
     * @return  FORMAT_EMF
     *          FORMAT_BMP 
     *          FORMAT_UNKNOWN
     */
    public short getClipboardPictureFormat()
    {
        return field_1_cf;
    }

    /**
     * Set the clipboard picture format.
     *
     * @param field_1_cf
     *        One of 
     *        FORMAT_EMF
     *        FORMAT_BMP
     *        FORMAT_UNKNOWN
     */
    public void setClipboardPictureFormat(short field_1_cf)
    {
        this.field_1_cf = field_1_cf;
    }
}
