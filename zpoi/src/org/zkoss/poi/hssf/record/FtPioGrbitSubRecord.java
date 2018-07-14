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
 * Properties for picture obj.
 * 
 * @author henrichen@zkoss.org
 */
public final class FtPioGrbitSubRecord extends SubRecord {
    public final static short sid = 0x0008;

    private static final BitField autopict    = BitFieldFactory.getInstance(0x0001);
    private static final BitField dde  = BitFieldFactory.getInstance(0x0002);
    private static final BitField printcalc = BitFieldFactory.getInstance(0x0004);
    private static final BitField icon = BitFieldFactory.getInstance(0x0008);  
    private static final BitField ctl = BitFieldFactory.getInstance(0x0010);
    private static final BitField prstm  = BitFieldFactory.getInstance(0x0020);
    private static final BitField camera = BitFieldFactory.getInstance(0x0080);
    private static final BitField defaultsize = BitFieldFactory.getInstance(0x0100);
    private static final BitField autoload = BitFieldFactory.getInstance(0x0200);
    
    private  short      field_1_option;

    public FtPioGrbitSubRecord()
    {

    }

    public FtPioGrbitSubRecord(LittleEndianInput in, int size) {
        if (size != 2) {
            throw new RecordFormatException("Expected size 2 but got (" + size + ")");
        }
        field_1_option                 = in.readShort();
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[ftPioGrbit]\n");
        buffer.append("    .option               = ")
            .append("0x").append(HexDump.toHex(  getOption ()))
            .append(" (").append( getOption() ).append(" )");
        buffer.append(System.getProperty("line.separator")); 
        buffer.append("         .autopict                 = ").append(isAutoPict()).append('\n');
        buffer.append("         .dde                      = ").append(isDde()).append('\n');
        buffer.append("         .printcalc                = ").append(isPrintCalc()).append('\n');
        buffer.append("         .icon                     = ").append(isIcon()).append('\n'); 
        buffer.append("         .ctl                      = ").append(isCtl()).append('\n');
        buffer.append("         .prstm                    = ").append(isPrstm()).append('\n');
        buffer.append("         .camera                   = ").append(isCamera()).append('\n');
        buffer.append("         .defaultsize              = ").append(isDefaultSize()).append('\n');
        buffer.append("         .autoload                 = ").append(isAutoLoad()).append('\n'); 
        buffer.append("[/ftPioGrbit]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {

        out.writeShort(sid);
        out.writeShort(getDataSize());

        out.writeShort(field_1_option);
    }

	protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        FtPioGrbitSubRecord rec = new FtPioGrbitSubRecord();
    
        rec.field_1_option = field_1_option;
        return rec;
    }

    /**
     * Get the option field for the CommonObjectData record.
     */
    public short getOption()
    {
        return field_1_option;
    }

    /**
     * Set the option field for the CommonObjectData record.
     */
    public void setOption(short field_1_option)
    {
        this.field_1_option = field_1_option;
    }

    /**
     * Sets the autopict field value.
     * whether keep picture aspect ratio for other views
     */
    public void setAutoPict(boolean value)
    {
        field_1_option = autopict.setShortBoolean(field_1_option, value);
    }

    /**
     * whether keep picture aspect ratio for other views
     * @return  the locked field value.
     */
    public boolean isAutoPict()
    {
        return autopict.isSet(field_1_option);
    }

    /**
     * Sets the icon field value.
     * whether show picture as an icon
     */
    public void setIcon(boolean value)
    {
        field_1_option = icon.setShortBoolean(field_1_option, value);
    }

    /**
     * whether show picture as an icon
     * @return  the icon field value.
     */
    public boolean isIcon()
    {
        return icon.isSet(field_1_option);
    }

    /**
     * Sets the autoload field value.
     * whether OLE sever is called to load the object's data on book loading.
     */
    public void setAutoLoad(boolean value)
    {
        field_1_option = autoload.setShortBoolean(field_1_option, value);
    }

    /**
     * whether OLE sever is called to load the object's data on book loading.
     * @return  the autoload field value.
     */
    public boolean isAutoLoad()
    {
        return autoload.isSet(field_1_option);
    }

    /**
     * Sets the camera field value.
     * whether a camera picture.
     */
    public void setCamera(boolean value)
    {
        field_1_option = camera.setShortBoolean(field_1_option, value);
    }

    /**
     * whether a camera picture.
     * @return  the uiobj field value.
     */
    public boolean isCamera()
    {
        return camera.isSet(field_1_option);
    }

    /**
     * Sets the defaultsize field value.
     * true if this picture size is NOT explicit specified.
     */
    public void setDefaultSize(boolean value)
    {
        field_1_option = defaultsize.setShortBoolean(field_1_option, value);
    }

    /**
     * true if this picture size is NOT explicit specified.
     * @return  the defaultize field value.
     */
    public boolean isDefaultSize()
    {
        return defaultsize.isSet(field_1_option);
    }

    /**
     * Sets the prstm field value.
     * whether the picture data is in the control stream(true) or in embedded storage(false)
     */
    public void setPrstm(boolean value)
    {
        field_1_option = prstm.setShortBoolean(field_1_option, value);
    }

    /**
     * whether the picture data is in the control stream(true) or in embedded storage(false)
     * @return  the prstm field value.
     */
    public boolean isPrstm()
    {
        return prstm.isSet(field_1_option);
    }

    /**
     * Sets the ctl field value.
     * whether this object is an ActiveX control.
     */
    public void setCtl(boolean value)
    {
        field_1_option = ctl.setShortBoolean(field_1_option, value);
        if (value) {
        	setDde(false);
        }
    }

    /**
     * whether this object is an ActiveX control. (Note {@link #isDde} and this cannot be both true.
     * @return  the disabled field value.
     */
    public boolean isCtl()
    {
        return ctl.isSet(field_1_option);
    }

    /**
     * Sets the printcalc field value.
     * whether update this object on print
     */
    public void setPrintCalc(boolean value)
    {
        field_1_option = printcalc.setShortBoolean(field_1_option, value);
    }

    /**
     * whether update this object on print
     * @return  the printcalc field value.
     */
    public boolean isPrintCalc()
    {
        return printcalc.isSet(field_1_option);
    }

    /**
     * Set dde field value.
     * whether the picFmla field of this object is a DDE reference. 
     */
    public void setDde(boolean value)
    {
        field_1_option = dde.setShortBoolean(field_1_option, value);
        if (value) {
        	setCtl(false);
        }
    }

    /** 
     * whether the picFmla field of this object is a DDE reference. 
     * @return  the dde field value.
     */
    public boolean isDde()
    {
        return dde.isSet(field_1_option);
    }
}
