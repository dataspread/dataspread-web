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

package org.zkoss.poi.hssf.record.chart;

import org.zkoss.poi.hssf.record.RecordInputStream;
import org.zkoss.poi.hssf.record.StandardRecord;
import org.zkoss.poi.util.BitField;
import org.zkoss.poi.util.BitFieldFactory;
import org.zkoss.poi.util.HexDump;
import org.zkoss.poi.util.LittleEndianOutput;

/**
 * The Chart3DShape record is used to define the shape of bars in a bar/column chart.<p/>
 * 
 * @author henrichen@zkoss.org
 */
public final class Chart3DBarShapeRecord extends StandardRecord {
    public final static short sid = 0x105F;

    private  short      field_1_riser;
    private  short      field_2_taper;


    public Chart3DBarShapeRecord()
    {

    }

    public Chart3DBarShapeRecord(RecordInputStream in)
    {
        field_1_riser          = in.readByte();
        field_2_taper          = in.readByte();
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[CHART3DBARSHAPE]\n");
        buffer.append("    .riser             = ")
            .append("0x").append(HexDump.toHex(  getRiser ()))
            .append(" (").append( getRiser() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .taper             = ")
            .append("0x").append(HexDump.toHex(  getTaper ()))
            .append(" (").append( getTaper() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("[/CHART3DBARSHAPE]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeByte(field_1_riser);
        out.writeByte(field_2_taper);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        Chart3DBarShapeRecord rec = new Chart3DBarShapeRecord();
    
        rec.field_1_riser = field_1_riser;
        rec.field_2_taper = field_2_taper;
        return rec;
    }




    /**
     * Get the shape of the base data point in bar or column chart (0: rectangle; 1: ellipse).
     */
    public short getRiser()
    {
        return field_1_riser;
    }

    /**
     * Set the shape of the base data point in a bar or column chart (0: rectangle; 1: ellipse).
     */
    public void setRiser(short field_1_riser)
    {
        this.field_1_riser = field_1_riser;
    }

    /**
     * Get the shape of the tip data point in a bar or column chart (0: same shape as the base; 
     * 1: taper to the maximum value of EACH data point; 2: taper to the projected maximum value of ALL data point but clipped at the value of EACH data point).
     */
    public short getTaper()
    {
        return field_2_taper;
    }

    /**
     * Set the shape of the tip data point in a bar or column chart (0: same shape as the base; 
     * 1: taper to the maximum value of EACH data point; 2: taper to the projected maximum value of ALL data point but clipped at the value of EACH data point).
     */
    public void setTaper(short field_2_taper)
    {
        this.field_2_taper = field_2_taper;
    }
}
