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
 * The scatter record is used to define a scatter chart.<p/>
 * 
 * @author henrichen@zkoss.org
 */
public final class ScatterRecord extends StandardRecord {
    public final static short      sid                             = 0x101B;
    private short field_1_ratio; //0 ~ 360
    private short field_2_size; //0, 10 ~ 90
    private short field_3_formatFlags;
    private static final BitField bubbles		= BitFieldFactory.getInstance(0x1);
    private static final BitField negbubbles	= BitFieldFactory.getInstance(0x2);
    private static final BitField shadow		= BitFieldFactory.getInstance(0x4);

    public ScatterRecord()
    {

    }

    public ScatterRecord(RecordInputStream in)
    {
    	field_1_ratio = in.readShort();
    	field_2_size = in.readShort();
    	field_3_formatFlags = in.readShort();
    }

    protected int getDataSize() {
        return 2 + 2 + 2;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[SCATTER]\n");
        buffer.append("    .ratio          = ")
        .append("0x").append(HexDump.toHex(  field_1_ratio  ))
        .append(" (").append( field_1_ratio ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .size           = ")
        .append("0x").append(HexDump.toHex(  field_2_size ))
        .append(" (").append( field_2_size ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .formatFlags    = ")
            .append("0x").append(HexDump.toHex(  field_3_formatFlags  ))
            .append(" (").append( field_3_formatFlags ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("         .bubbles                  = ").append(isBubbles()).append('\n'); 
        buffer.append("         .negbubbles               = ").append(isNegBubbles()).append('\n'); 

        buffer.append("[/SCATTER]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_ratio);
        out.writeShort(field_2_size);
        out.writeShort(field_3_formatFlags);
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        ScatterRecord rec = new ScatterRecord();
    
        rec.field_1_ratio = field_1_ratio;
        rec.field_2_size = field_2_size;
        rec.field_3_formatFlags = field_3_formatFlags;
        return rec;
    }


    /**
     * Get the format flags field for the Pie record.
     */
    public short getFormatFlags()
    {
        return field_3_formatFlags;
    }

    /**
     * Set the format flags field for the Pie record.
     */
    public void setFormatFlags(short field_3_formatFlags)
    {
        this.field_3_formatFlags = field_3_formatFlags;
    }

    /**
     * Get the ratio to show the bubble to their default size.
     */
    public short getRatio()
    {
        return field_1_ratio;
    }

    /**
     * Set the ratio to show the bubble to their default size.
     */
    public void setRatio(short field_1_ratio)
    {
        this.field_1_ratio = field_1_ratio;
    }

    /**
     * Get how the default size of the data points represents the value:
     * 1 means the data point represents the value; 2 means width of the data point represent the value.
     */
    public short getSize()
    {
        return field_2_size;
    }

    /**
     * Set how the default size of the data points represents the value:
     * 1 means the data point represents the value; 2 means width of the data point represent the value.
     */
    public void setSize(short field_2_size)
    {
        this.field_2_size = field_2_size;
    }

    /**
     * True to show negative data points bubble.
     */
    public void setNegBubbles(boolean value)
    {
        field_3_formatFlags = negbubbles.setShortBoolean(field_3_formatFlags, value);
    }

    /**
     * Whether show negative data points bubble.
     * @return  Whether show negative data points bubble.
     */
    public boolean isNegBubbles()
    {
        return negbubbles.isSet(field_3_formatFlags);
    }

    /**
     * Sets the bubbles field value.
     * whether this is a scatter chart(false) or bubble chart(true).
     */
    public void setBubbles(boolean value)
    {
        field_3_formatFlags = bubbles.setShortBoolean(field_3_formatFlags, value);
    }

    /**
     * whether this is a scatter chart(false) or bubble chart(true).
     * @return  the bubbles field value.
     */
    public boolean isBubbles()
    {
        return bubbles.isSet(field_3_formatFlags);
    }

    /**
     * Sets the shadow field value.
     * display a shadow for the chart
     */
    public void setShadow(boolean value)
    {
        field_3_formatFlags = shadow.setShortBoolean(field_3_formatFlags, value);
    }

    /**
     * display a shadow for the chart
     * @return  the shadow field value.
     */
    public boolean isShadow()
    {
        return shadow.isSet(field_3_formatFlags);
    }
}
