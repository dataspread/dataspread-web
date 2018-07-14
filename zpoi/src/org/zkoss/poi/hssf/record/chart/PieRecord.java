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
 * The pie record is used to define a pie chart.<p/>
 * 
 * @author henrichen@zkoss.org
 */
public final class PieRecord extends StandardRecord {
    public final static short      sid                             = 0x1019;
    private short field_1_anStart; //0 ~ 360
    private short field_2_pcDonut; //0, 10 ~ 90
    private short field_3_formatFlags;
    private static final BitField shadow		= BitFieldFactory.getInstance(0x1);
    private static final BitField leaderLines	= BitFieldFactory.getInstance(0x2);

    public PieRecord()
    {

    }

    public PieRecord(RecordInputStream in)
    {
    	field_1_anStart = in.readShort();
    	field_2_pcDonut = in.readShort();
    	field_3_formatFlags = in.readShort();
    }

    protected int getDataSize() {
        return 2 + 2 + 2;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[PIE]\n");
        buffer.append("    .anStart        = ")
        .append("0x").append(HexDump.toHex(  field_1_anStart  ))
        .append(" (").append( field_1_anStart ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .pcDonut        = ")
        .append("0x").append(HexDump.toHex(  field_2_pcDonut ))
        .append(" (").append( field_2_pcDonut ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .formatFlags    = ")
            .append("0x").append(HexDump.toHex(  field_3_formatFlags  ))
            .append(" (").append( field_3_formatFlags ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("         .shadow                   = ").append(isShadow()).append('\n'); 
        buffer.append("         .showLdrLines             = ").append(isShowLdrLines()).append('\n'); 

        buffer.append("[/PIE]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_anStart);
        out.writeShort(field_2_pcDonut);
        out.writeShort(field_3_formatFlags);
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        PieRecord rec = new PieRecord();
    
        rec.field_1_anStart = field_1_anStart;
        rec.field_2_pcDonut = field_2_pcDonut;
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
     * Get the start angle in degrees clockwise from the top of the circle.
     */
    public short getAnStart()
    {
        return field_1_anStart;
    }

    /**
     * Set the format flags field for the Area record.
     */
    public void setAnStart(short field_1_anStart)
    {
        this.field_1_anStart = field_1_anStart;
    }

    /**
     * Get the size of the center hole as a percentage of the plot area size (0 as Pie, 10 to 90 as Doughnut).
     */
    public short getPcDonut()
    {
        return field_2_pcDonut;
    }

    /**
     * Set the size of the center hole as a percentage of the plot area size (0 as Pie, 10 to 90 as Doughnut).
     */
    public void setPcDonut(short field_2_pcDonut)
    {
        this.field_2_pcDonut = field_2_pcDonut;
    }

    /**
     * Sets show leader lines for data labels.
     */
    public void setShowLdrLines(boolean value)
    {
        field_3_formatFlags = leaderLines.setShortBoolean(field_3_formatFlags, value);
    }

    /**
     * Whether show leader lines for data labels.
     * @return  Whether show leader lines for data labels.
     */
    public boolean isShowLdrLines()
    {
        return leaderLines.isSet(field_3_formatFlags);
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
