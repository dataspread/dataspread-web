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
 * The chart3d marker record.<p/>
 * 
 * @author henrichen@zkoss.org
 */
public final class Chart3DRecord extends StandardRecord {
    public final static short      sid                             = 0x103A;
    private short field_1_anRot; //0 ~ 360
    private short field_2_anElev; //-90 ~ 90
    private short field_3_pcDist; //if (fNotPieChart) then the percentage height to its width(5~500); or the thickness of the Pie chart.
    private short field_4_pcHeight;
    private short field_5_depth; //percentage to its width (1~2000)
    private short field_6_gap;
    private short field_7_formatFlags;
    private static final BitField perspective = BitFieldFactory.getInstance(0x1);
    private static final BitField cluster = BitFieldFactory.getInstance(0x2);
    private static final BitField scaling = BitFieldFactory.getInstance(0x4);
    private static final BitField notpie = BitFieldFactory.getInstance(0x10);
    private static final BitField wall2d = BitFieldFactory.getInstance(0x20);

    public Chart3DRecord()
    {

    }

    public Chart3DRecord(RecordInputStream in)
    {
    	field_1_anRot = in.readShort();
    	field_2_anElev = in.readShort();
    	field_3_pcDist = in.readShort();
    	field_4_pcHeight = in.readShort();
        field_5_depth = in.readShort();
        field_6_gap = in.readShort();
        field_7_formatFlags = in.readShort();
    }

    protected int getDataSize() {
        return 2 + 2 + 2 + 2 + 2 + 2 + 2;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[CHART3D]\n");
        buffer.append("    .anRot          = ")
        .append("0x").append(HexDump.toHex(  field_1_anRot  ))
        .append(" (").append( field_1_anRot ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .anElev         = ")
        .append("0x").append(HexDump.toHex(  field_2_anElev ))
        .append(" (").append( field_2_anElev ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .pcDist         = ")
            .append("0x").append(HexDump.toHex(  field_3_pcDist  ))
            .append(" (").append( field_3_pcDist ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .pcHeight       = ")
        .append("0x").append(HexDump.toHex(  field_4_pcHeight  ))
        .append(" (").append( field_4_pcHeight ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .depth          = ")
        .append("0x").append(HexDump.toHex(  field_5_depth  ))
        .append(" (").append( field_5_depth ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
	    buffer.append("    .gap            = ")
	    .append("0x").append(HexDump.toHex(  field_6_gap  ))
	    .append(" (").append( field_6_gap ).append(" )");
	    buffer.append(System.getProperty("line.separator")); 
        buffer.append("    .formatFlags    = ")
        .append("0x").append(HexDump.toHex(  field_7_formatFlags  ))
        .append(" (").append( field_7_formatFlags ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("         .perspective       = ").append(isPerspective()).append('\n'); 
        buffer.append("         .cluster           = ").append(isCluster()).append('\n'); 
        buffer.append("         .3dscaling         = ").append(isScaling()).append('\n'); 
        buffer.append("         .notpie            = ").append(isNotPie()).append('\n'); 
        buffer.append("         .walls2d           = ").append(isWalls2D()).append('\n'); 
        buffer.append("[/CHART3D]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_anRot);
        out.writeShort(field_2_anElev);
        out.writeShort(field_3_pcDist);
        //20120412 samchuang@zkoss.org
        out.writeShort(field_4_pcHeight);
        out.writeShort(field_5_depth);
        out.writeShort(field_6_gap);
        out.writeShort(field_7_formatFlags);
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        Chart3DRecord rec = new Chart3DRecord();
    
        rec.field_1_anRot = field_1_anRot;
        rec.field_2_anElev = field_2_anElev;
        rec.field_3_pcDist = field_3_pcDist;
        rec.field_4_pcHeight = field_4_pcHeight;
        rec.field_5_depth = field_5_depth;
        rec.field_6_gap = field_6_gap;
        rec.field_7_formatFlags = field_7_formatFlags;
        return rec;
    }


    /**
     * Get the format flags field for the Pie record.
     */
    public short getFormatFlags()
    {
        return field_7_formatFlags;
    }

    /**
     * Set the format flags field for the Pie record.
     */
    public void setFormatFlags(short field_6_formatFlags)
    {
        this.field_7_formatFlags = field_6_formatFlags;
    }

    /**
     * Get the rotation angle in degrees around a vertical line clockwise.
     */
    public short getAnRot()
    {
        return field_1_anRot;
    }

    /**
     * Get the rotation angle in degrees around a vertical line clockwise.
     */
    public void setAnRot(short field_1_anRot)
    {
        this.field_1_anRot = field_1_anRot;
    }

    /**
     * Get the rotation angle in degrees around a horizontal line clockwise.
     */
    public short getAnElev()
    {
        return field_2_anElev;
    }

    /**
     * Set the rotation angle in degrees around a horizontal line clockwise.
     */
    public void setAnElev(short field_2_anElev)
    {
        this.field_2_anElev = field_2_anElev;
    }

    /**
     * Sets view angle in degree(0 ~ 200).
     */
    public void setDist(short field_3_pcDist)
    {
        this.field_3_pcDist = field_3_pcDist;
    }
    
    /**
     * Gets view angle in degree(0 ~ 200).
     */
    public short getDist() {
    	return field_3_pcDist;
    }

    /**
     * Sets thick of the Pie chart; or height in percentage to the widht of its plot area(0 ~ 500).
     */
    public void setHeight(short field_4_pcHeight)
    {
        this.field_4_pcHeight = field_4_pcHeight;
    }
    
    /**
     * Gets thick of the Pie chart; or height in percentage to the width of its plot area(0 ~ 500).
     */
    public short getHeight() {
    	return field_4_pcHeight;
    }

    /**
     * Sets depth in percentage to the width of its plot area.(1 ~ 200).
     */
    public void setDepth(short field_5_depth)
    {
        this.field_5_depth = field_5_depth;
    }
    
    /**
     * Gets depth in percentage to the width of its plot area.(1 ~ 200).
     */
    public short getDepth() {
    	return field_5_depth;
    }

    /**
     * Sets gap in percentage to the width of its plot area.
     */
    public void setGap(short field_6_gap)
    {
        this.field_6_gap = field_6_gap;
    }
    
    /**
     * Gets gap in percentage to the width of its plot area.
     */
    public short getGap() {
    	return field_6_gap;
    }
    
    /**
     * Sets the perspective field value.
     */
    public void setPerspective(boolean value)
    {
        field_7_formatFlags = perspective.setShortBoolean(field_7_formatFlags, value);
    }

    /**
     * @return  the perspective field value.
     */
    public boolean isPerspective()
    {
        return perspective.isSet(field_7_formatFlags);
    }

    /**
     * Sets the cluster field value.
     */
    public void setCluster(boolean value)
    {
        field_7_formatFlags = cluster.setShortBoolean(field_7_formatFlags, value);
    }

    /**
     * @return  the cluster field value.
     */
    public boolean isCluster()
    {
        return cluster.isSet(field_7_formatFlags);
    }

    /**
     * Sets the 3dscaling field value.
     */
    public void setScaling(boolean value)
    {
        field_7_formatFlags = scaling.setShortBoolean(field_7_formatFlags, value);
    }

    /**
     * @return  the perspective field value.
     */
    public boolean isScaling()
    {
        return scaling.isSet(field_7_formatFlags);
    }
    
    /**
     * Sets the notpie field value.
     */
    public void setNotPie(boolean value)
    {
        field_7_formatFlags = notpie.setShortBoolean(field_7_formatFlags, value);
    }

    /**
     * @return  the notpie field value.
     */
    public boolean isNotPie()
    {
        return notpie.isSet(field_7_formatFlags);
    }
    
    /**
     * Sets the notpie field value.
     */
    public void setWalls2D(boolean value)
    {
        field_7_formatFlags = wall2d.setShortBoolean(field_7_formatFlags, value);
    }

    /**
     * @return  the notpie field value.
     */
    public boolean isWalls2D()
    {
        return wall2d.isSet(field_7_formatFlags);
    }
}
