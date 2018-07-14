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
 * The line record is used to define a line chart.<p/>
 * 
 * @author henrichen@zkoss.org
 */
public final class LineRecord extends StandardRecord {
    public final static short      sid                             = 0x1018;
    private short field_1_formatFlags;
    private static final BitField stacked		= BitFieldFactory.getInstance(0x1);
    private static final BitField percent		= BitFieldFactory.getInstance(0x2);
    private static final BitField shadow		= BitFieldFactory.getInstance(0x4);

    public LineRecord()
    {

    }

    public LineRecord(RecordInputStream in)
    {
    	field_1_formatFlags = in.readShort();
    }

    protected int getDataSize() {
        return 2;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[LINE]\n");
        buffer.append("    .formatFlags    = ")
            .append("0x").append(HexDump.toHex(  field_1_formatFlags  ))
            .append(" (").append( field_1_formatFlags ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("         .stacked                  = ").append(isStacked()).append('\n'); 
        buffer.append("         .percent                  = ").append(isPercent()).append('\n'); 
        buffer.append("         .shadow                   = ").append(isShadow()).append('\n'); 

        buffer.append("[/LINE]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_formatFlags);
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        LineRecord rec = new LineRecord();
    
        rec.field_1_formatFlags = field_1_formatFlags;
        return rec;
    }


    /**
     * Get the format flags field for the Pie record.
     */
    public short getFormatFlags()
    {
        return field_1_formatFlags;
    }

    /**
     * Set the format flags field for the Pie record.
     */
    public void setFormatFlags(short field_3_formatFlags)
    {
        this.field_1_formatFlags = field_3_formatFlags;
    }

    /**
     * Sets percent field value.
     * whether to show data points as a percentage of the sum of all data point in a category.
     */
    public void setPercent(boolean value)
    {
        field_1_formatFlags = percent.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * whether to show data points as a percentage of the sum of all data point in a category.
     * @return  the percent field value.
     */
    public boolean isPercent()
    {
        return percent.isSet(field_1_formatFlags);
    }

    /**
     * Sets the stacked field value.
     * whether stack data points in a category.
     */
    public void setStacked(boolean value)
    {
        field_1_formatFlags = stacked.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * whether stack data points in a category.
     * @return  the stacked field value.
     */
    public boolean isStacked()
    {
        return stacked.isSet(field_1_formatFlags);
    }
    /**
     * Sets the shadow field value.
     * display a shadow for the chart
     */
    public void setShadow(boolean value)
    {
        field_1_formatFlags = shadow.setShortBoolean(field_1_formatFlags, value);
    }

    /**
     * display a shadow for the chart
     * @return  the shadow field value.
     */
    public boolean isShadow()
    {
        return shadow.isSet(field_1_formatFlags);
    }
}
