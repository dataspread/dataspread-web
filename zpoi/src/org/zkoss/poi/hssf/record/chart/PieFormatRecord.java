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
 * The record specifies the distance of a data point or data points in a series from the center of pie chart.<p/>
 * 
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class PieFormatRecord extends StandardRecord {
    public final static short      sid                             = 0x100B;
    private  short      field_1_pcExplode;

    public PieFormatRecord()
    {

    }

    public PieFormatRecord(RecordInputStream in)
    {

        field_1_pcExplode            = in.readShort();
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[PIEFORMAT]\n");
        buffer.append("    .pcExplode          = ")
            .append("0x").append(HexDump.toHex(  getPcExplode ()))
            .append(" (").append( getPcExplode() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("[/PIEFORMAT]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_pcExplode);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        PieFormatRecord rec = new PieFormatRecord();
    
        rec.field_1_pcExplode = field_1_pcExplode;
        return rec;
    }




    /**
     * Get the percentage distance of a data point or data pints in a series from the center of the plot area for a doughnut or pie chart (0 
     * ~ 100).
     * 0 means data point is as close to the center as possible; 100 means data point is at the edge of the chart area; 
     * > 100 will scale down data point to fit in chart area. 
     */
    public short getPcExplode()
    {
        return field_1_pcExplode;
    }

    /**
     * Set the percentage distance of a data point or data pints in a series from the center of the plot area for a doughnut or pie chart (0 
     * ~ 100).
     * 0 means data point is as close to the center as possible; 100 means data point is at the edge of the chart area; 
     * > 100 will scale down data point to fit in chart area. 
     */
    public void setPcExplode(short field_1_pcExplode)
    {
        this.field_1_pcExplode = field_1_pcExplode;
    }
}
