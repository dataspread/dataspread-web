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

package org.zkoss.poi.hslf.model;

import org.zkoss.poi.ddf.*;
import org.zkoss.poi.hslf.record.*;
import org.zkoss.poi.hslf.usermodel.PictureData;
import org.zkoss.poi.hslf.usermodel.SlideShow;
import org.zkoss.poi.util.POILogFactory;
import org.zkoss.poi.util.POILogger;
import java.util.List;

import java.awt.*;

/**
 * Represents functionality provided by the 'Fill Effects' dialog in PowerPoint.
 *
 * @author Yegor Kozlov
 */
public final class Fill {
    // For logging
    protected POILogger logger = POILogFactory.getLogger(this.getClass());

    /**
     *  Fill with a solid color
     */
    public static final int FILL_SOLID = 0;

    /**
     *  Fill with a pattern (bitmap)
     */
    public static final int FILL_PATTERN = 1;

    /**
     *  A texture (pattern with its own color map)
     */
    public static final int FILL_TEXTURE = 2;

    /**
     *  Center a picture in the shape
     */
    public static final int FILL_PICTURE = 3;

    /**
     *  Shade from start to end points
     */
    public static final int FILL_SHADE = 4;

    /**
     *  Shade from bounding rectangle to end point
     */
    public static final int FILL_SHADE_CENTER = 5;

    /**
     *  Shade from shape outline to end point
     */
    public static final int FILL_SHADE_SHAPE = 6;

    /**
     *  Similar to FILL_SHADE, but the fill angle
     *  is additionally scaled by the aspect ratio of
     *  the shape. If shape is square, it is the same as FILL_SHADE
     */
    public static final int FILL_SHADE_SCALE = 7;

    /**
     *  shade to title
     */
    public static final int FILL_SHADE_TITLE = 8;

    /**
     *  Use the background fill color/pattern
     */
    public static final int FILL_BACKGROUND = 9;



    /**
     * The shape this background applies to
     */
    protected Shape shape;

    /**
     * Construct a <code>Fill</code> object for a shape.
     * Fill information will be read from shape's escher properties.
     *
     * @param shape the shape this background applies to
     */
    public Fill(Shape shape){
        this.shape = shape;
    }

    /**
     * Returns fill type.
     * Must be one of the <code>FILL_*</code> constants defined in this class.
     *
     * @return type of fill
     */
    public int getFillType(){
        EscherOptRecord opt = (EscherOptRecord)Shape.getEscherChild(shape.getSpContainer(), EscherOptRecord.RECORD_ID);
        EscherSimpleProperty prop = (EscherSimpleProperty)Shape.getEscherProperty(opt, EscherProperties.FILL__FILLTYPE);
        return prop == null ? FILL_SOLID : prop.getPropertyValue();
    }

    /**
     */
    protected void afterInsert(Sheet sh){
        EscherOptRecord opt = (EscherOptRecord)Shape.getEscherChild(shape.getSpContainer(), EscherOptRecord.RECORD_ID);
        EscherSimpleProperty p = (EscherSimpleProperty)Shape.getEscherProperty(opt, EscherProperties.FILL__PATTERNTEXTURE);
        if(p != null) {
            int idx = p.getPropertyValue();
            EscherBSERecord bse = getEscherBSERecord(idx);
            bse.setRef(bse.getRef() + 1);
        }
    }

    protected EscherBSERecord getEscherBSERecord(int idx){
        Sheet sheet = shape.getSheet();
        if(sheet == null) {
            logger.log(POILogger.DEBUG, "Fill has not yet been assigned to a sheet");
            return null;
        }
        SlideShow ppt = sheet.getSlideShow();
        Document doc = ppt.getDocumentRecord();
        EscherContainerRecord dggContainer = doc.getPPDrawingGroup().getDggContainer();
        EscherContainerRecord bstore = (EscherContainerRecord)Shape.getEscherChild(dggContainer, EscherContainerRecord.BSTORE_CONTAINER);
        if(bstore == null) {
            logger.log(POILogger.DEBUG, "EscherContainerRecord.BSTORE_CONTAINER was not found ");
            return null;
        }
        List lst = bstore.getChildRecords();
        return (EscherBSERecord)lst.get(idx-1);
    }

    /**
     * Sets fill type.
     * Must be one of the <code>FILL_*</code> constants defined in this class.
     *
     * @param type type of the fill
     */
    public void setFillType(int type){
        EscherOptRecord opt = (EscherOptRecord)Shape.getEscherChild(shape.getSpContainer(), EscherOptRecord.RECORD_ID);
        Shape.setEscherProperty(opt, EscherProperties.FILL__FILLTYPE, type);
    }

    /**
     * Foreground color
     */
    public Color getForegroundColor(){
        EscherOptRecord opt = (EscherOptRecord)Shape.getEscherChild(shape.getSpContainer(), EscherOptRecord.RECORD_ID);
        EscherSimpleProperty p = (EscherSimpleProperty)Shape.getEscherProperty(opt, EscherProperties.FILL__NOFILLHITTEST);

        if(p != null && (p.getPropertyValue() & 0x10) == 0) return null;

        return shape.getColor(EscherProperties.FILL__FILLCOLOR, EscherProperties.FILL__FILLOPACITY, -1);

    }

    /**
     * Foreground color
     */
    public void setForegroundColor(Color color){
        EscherOptRecord opt = (EscherOptRecord)Shape.getEscherChild(shape.getSpContainer(), EscherOptRecord.RECORD_ID);
        if (color == null) {
            Shape.setEscherProperty(opt, EscherProperties.FILL__NOFILLHITTEST, 0x150000);
        }
        else {
            int rgb = new Color(color.getBlue(), color.getGreen(), color.getRed(), 0).getRGB();
            Shape.setEscherProperty(opt, EscherProperties.FILL__FILLCOLOR, rgb);
            Shape.setEscherProperty(opt, EscherProperties.FILL__NOFILLHITTEST, 0x150011);
        }
    }

    /**
     * Background color
     */
    public Color getBackgroundColor(){
        EscherOptRecord opt = (EscherOptRecord)Shape.getEscherChild(shape.getSpContainer(), EscherOptRecord.RECORD_ID);
        EscherSimpleProperty p = (EscherSimpleProperty)Shape.getEscherProperty(opt, EscherProperties.FILL__NOFILLHITTEST);

        if(p != null && (p.getPropertyValue() & 0x10) == 0) return null;

        return shape.getColor(EscherProperties.FILL__FILLBACKCOLOR, EscherProperties.FILL__FILLOPACITY, -1);
    }

    /**
     * Background color
     */
    public void setBackgroundColor(Color color){
        EscherOptRecord opt = (EscherOptRecord)Shape.getEscherChild(shape.getSpContainer(), EscherOptRecord.RECORD_ID);
        if (color == null) {
            Shape.setEscherProperty(opt, EscherProperties.FILL__FILLBACKCOLOR, -1);
        }
        else {
            int rgb = new Color(color.getBlue(), color.getGreen(), color.getRed(), 0).getRGB();
            Shape.setEscherProperty(opt, EscherProperties.FILL__FILLBACKCOLOR, rgb);
        }
    }

    /**
     * <code>PictureData</code> object used in a texture, pattern of picture fill.
     */
    public PictureData getPictureData(){
        EscherOptRecord opt = (EscherOptRecord)Shape.getEscherChild(shape.getSpContainer(), EscherOptRecord.RECORD_ID);
        EscherSimpleProperty p = (EscherSimpleProperty)Shape.getEscherProperty(opt, EscherProperties.FILL__PATTERNTEXTURE);
        if (p == null) return null;

        SlideShow ppt = shape.getSheet().getSlideShow();
        PictureData[] pict = ppt.getPictureData();
        Document doc = ppt.getDocumentRecord();

        EscherContainerRecord dggContainer = doc.getPPDrawingGroup().getDggContainer();
        EscherContainerRecord bstore = (EscherContainerRecord)Shape.getEscherChild(dggContainer, EscherContainerRecord.BSTORE_CONTAINER);

        java.util.List<EscherRecord> lst = bstore.getChildRecords();
        int idx = p.getPropertyValue();
        if (idx == 0){
            logger.log(POILogger.WARN, "no reference to picture data found ");
        } else {
            EscherBSERecord bse = (EscherBSERecord)lst.get(idx - 1);
            for ( int i = 0; i < pict.length; i++ ) {
                if (pict[i].getOffset() ==  bse.getOffset()){
                    return pict[i];
                }
            }
        }

        return null;
    }

    /**
     * Assign picture used to fill the underlying shape.
     *
     * @param idx 0-based index of the picture added to this ppt by <code>SlideShow.addPicture</code> method.
     */
    public void setPictureData(int idx){
        EscherOptRecord opt = (EscherOptRecord)Shape.getEscherChild(shape.getSpContainer(), EscherOptRecord.RECORD_ID);
        Shape.setEscherProperty(opt, (short)(EscherProperties.FILL__PATTERNTEXTURE + 0x4000), idx);
        if( idx != 0 ) {
            if( shape.getSheet() != null ) {
                EscherBSERecord bse = getEscherBSERecord(idx);
                bse.setRef(bse.getRef() + 1);
            }
        }
    }

}
