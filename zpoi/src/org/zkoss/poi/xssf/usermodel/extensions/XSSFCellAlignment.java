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
package org.zkoss.poi.xssf.usermodel.extensions;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STHorizontalAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STVerticalAlignment;
import org.zkoss.poi.ss.usermodel.HorizontalAlignment;
import org.zkoss.poi.ss.usermodel.VerticalAlignment;
import org.zkoss.poi.util.Internal;


/**
 * Cell settings avaiable in the Format/Alignment tab
 */
public class XSSFCellAlignment {

    private CTCellAlignment cellAlignement;

    /**
     * Creates a Cell Alignment from the supplied XML definition
     *
     * @param cellAlignment
     */
    public XSSFCellAlignment(CTCellAlignment cellAlignment) {
        this.cellAlignement = cellAlignment;
    }

    /**
     * Get the type of vertical alignment for the cell
     *
     * @return the type of aligment
     * @see VerticalAlignment
     */
    public VerticalAlignment getVertical() {
        STVerticalAlignment.Enum align = cellAlignement.getVertical();
        if (align == null) align = STVerticalAlignment.BOTTOM;

        return VerticalAlignment.values()[align.intValue() - 1];
    }

    /**
     * Set the type of vertical alignment for the cell
     *
     * @param align - the type of alignment
     * @see VerticalAlignment
     */
    public void setVertical(VerticalAlignment align) {
        cellAlignement.setVertical(STVerticalAlignment.Enum.forInt(align.ordinal() + 1));
    }

    /**
     * Get the type of horizontal alignment for the cell
     *
     * @return the type of aligment
     * @see HorizontalAlignment
     */
    public HorizontalAlignment getHorizontal() {
        STHorizontalAlignment.Enum align = cellAlignement.getHorizontal();
        if (align == null) align = STHorizontalAlignment.GENERAL;

        return HorizontalAlignment.values()[align.intValue() - 1];
    }

    /**
     * Set the type of horizontal alignment for the cell
     *
     * @param align - the type of alignment
     * @see HorizontalAlignment
     */
    public void setHorizontal(HorizontalAlignment align) {
        cellAlignement.setHorizontal(STHorizontalAlignment.Enum.forInt(align.ordinal() + 1));
    }

    /**
     * Get the number of spaces to indent the text in the cell
     *
     * @return indent - number of spaces
     */
    public long getIndent() {
        return cellAlignement.getIndent();
    }

    /**
     * Set the number of spaces to indent the text in the cell
     *
     * @param indent - number of spaces
     */
    public void setIndent(long indent) {
        cellAlignement.setIndent(indent);
    }

    /**
     * Get the degree of rotation for the text in the cell
     * <p/>
     * Expressed in degrees. Values range from 0 to 180. The first letter of
     * the text is considered the center-point of the arc.
     * <br/>
     * For 0 - 90, the value represents degrees above horizon. For 91-180 the degrees below the
     * horizon is calculated as:
     * <br/>
     * <code>[degrees below horizon] = 90 - textRotation.</code>
     * </p>
     *
     * @return rotation degrees (between -90 and 90 degrees)
     */
    public long getTextRotation() {
    	//ZSS-1020
    	long rotation = cellAlignement.getTextRotation();
        if (rotation == 0xff) {
            // Vertical aligned special case
            return rotation;
         }
         if (rotation > 90) {
           //This is actually the 4th quadrant
           rotation = 90-rotation;
         }
         return rotation;
    }

    /**
     * Set the degree of rotation for the text in the cell
     * <p/>
     * Expressed in degrees. Values range from 0 to 180. The first letter of
     * the text is considered the center-point of the arc.
     * <br/>
     * For 0 - 90, the value represents degrees above horizon. For 91-180 the degrees below the
     * horizon is calculated as:
     * <br/>
     * <code>[degrees below horizon] = 90 - textRotation.</code>
     * </p>
     *
     * @param rotation - the rotation degrees (between -90 and 90 degrees)
     */
    public void setTextRotation(long rotation) {
    	//ZSS-1020
        if (rotation == 0xff) {
            // Special cases for vertically aligned text
        } 
        else if ((rotation < 0)&&(rotation >= -90)) {
          //Take care of the funny 4th quadrant issue
          //The 4th quadrant (-1 to -90) is stored as (91 to 180)
          rotation = (short)(90 - rotation);
        }
        else if ((rotation < -90)  ||(rotation > 90)) {
          //Do not allow an incorrect rotation to be set
          rotation = (short) 0; //throw new IllegalArgumentException("The rotation must be between -90 and 90 degrees, or 0xff");
        }
    	cellAlignement.setTextRotation(rotation);
    }

    /**
     * Whether the text should be wrapped
     *
     * @return a boolean value indicating if the text in a cell should be line-wrapped within the cell.
     */
    public boolean getWrapText() {
        return cellAlignement.getWrapText();
    }

    /**
     * Set whether the text should be wrapped
     *
     * @param wrapped a boolean value indicating if the text in a cell should be line-wrapped within the cell.
     */
    public void setWrapText(boolean wrapped) {
        cellAlignement.setWrapText(wrapped);
    }

    /**
     * Access to low-level data
     */
    @Internal
    public CTCellAlignment getCTCellAlignment() {
        return cellAlignement;
    }
}
