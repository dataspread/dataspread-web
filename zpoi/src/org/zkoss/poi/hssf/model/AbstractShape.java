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

package org.zkoss.poi.hssf.model;

import org.zkoss.poi.ddf.EscherBoolProperty;
import org.zkoss.poi.ddf.EscherContainerRecord;
import org.zkoss.poi.ddf.EscherOptRecord;
import org.zkoss.poi.ddf.EscherProperties;
import org.zkoss.poi.ddf.EscherRGBProperty;
import org.zkoss.poi.ddf.EscherRecord;
import org.zkoss.poi.ddf.EscherSimpleProperty;
import org.zkoss.poi.ddf.EscherSpRecord;
import org.zkoss.poi.hssf.record.ObjRecord;
import org.zkoss.poi.hssf.usermodel.HSSFAnchor;
import org.zkoss.poi.hssf.usermodel.HSSFComment;
import org.zkoss.poi.hssf.usermodel.HSSFPolygon;
import org.zkoss.poi.hssf.usermodel.HSSFShape;
import org.zkoss.poi.hssf.usermodel.HSSFSimpleShape;
import org.zkoss.poi.hssf.usermodel.HSSFTextbox;

/**
 * An abstract shape is the lowlevel model for a shape.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
@Deprecated
public abstract class AbstractShape
{
    /**
     * Create a new shape object used to create the escher records.
     *
     * @param hssfShape     The simple shape this is based on.
     */
    public static AbstractShape createShape( HSSFShape hssfShape, int shapeId )
    {
        AbstractShape shape;
        if (hssfShape instanceof HSSFComment)
        {
            shape = new CommentShape( (HSSFComment)hssfShape, shapeId );
        }
        else if (hssfShape instanceof HSSFTextbox)
        {
            shape = new TextboxShape( (HSSFTextbox)hssfShape, shapeId );
        }
        else if (hssfShape instanceof HSSFPolygon)
        {
            shape = new PolygonShape( (HSSFPolygon) hssfShape, shapeId );
        }
        else if (hssfShape instanceof HSSFSimpleShape)
        {
            HSSFSimpleShape simpleShape = (HSSFSimpleShape) hssfShape;
            switch ( simpleShape.getShapeType() )
            {
                case HSSFSimpleShape.OBJECT_TYPE_PICTURE:
                    shape = new PictureShape( simpleShape, shapeId );
                    break;
                case HSSFSimpleShape.OBJECT_TYPE_LINE:
                    shape = new LineShape( simpleShape, shapeId );
                    break;
                case HSSFSimpleShape.OBJECT_TYPE_OVAL:
                case HSSFSimpleShape.OBJECT_TYPE_RECTANGLE:
                    shape = new SimpleFilledShape( simpleShape, shapeId );
                    break;
                case HSSFSimpleShape.OBJECT_TYPE_COMBO_BOX:
                    shape = new ComboboxShape( simpleShape, shapeId );
                    break;
//                //20120412: samchuang@zkoss.org: POI not support export Chart yet
//                case HSSFSimpleShape.OBJECT_TYPE_CHART:
//                	break;
                default:
                    throw new IllegalArgumentException("Do not know how to handle this type of shape");
            }
        }
        else
        {
            throw new IllegalArgumentException("Unknown shape type");
        }
        EscherSpRecord sp = shape.getSpContainer().getChildById(EscherSpRecord.RECORD_ID);
        if (hssfShape.getParent() != null)
            sp.setFlags(sp.getFlags() | EscherSpRecord.FLAG_CHILD);
        return shape;
    }

    protected AbstractShape()
    {
    }

    /**
     * @return  The shape container and it's children that can represent this
     *          shape.
     */
    public abstract EscherContainerRecord getSpContainer();

    /**
     * @return  The object record that is associated with this shape.
     */
    public abstract ObjRecord getObjRecord();

    /**
     * Creates an escher anchor record from a HSSFAnchor.
     *
     * @param userAnchor    The high level anchor to convert.
     * @return  An escher anchor record.
     */
    protected EscherRecord createAnchor( HSSFAnchor userAnchor )
    {
        return ConvertAnchor.createAnchor(userAnchor);
    }

    /**
     * Add standard properties to the opt record.  These properties effect
     * all records.
     *
     * @param shape     The user model shape.
     * @param opt       The opt record to add the properties to.
     * @return          The number of options added.
     */
    protected int addStandardOptions( HSSFShape shape, EscherOptRecord opt )
    {
        opt.addEscherProperty( new EscherBoolProperty( EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 0x080000 ) );
//        opt.addEscherProperty( new EscherBoolProperty( EscherProperties.TEXT__SIZE_TEXT_TO_FIT_SHAPE, 0x080008 ) );
        if ( shape.isNoFill() )
        {
            // Wonderful... none of the spec's give any clue as to what these constants mean.
            opt.addEscherProperty( new EscherBoolProperty( EscherProperties.FILL__NOFILLHITTEST, 0x00110000 ) );
        }
        else
        {
            opt.addEscherProperty( new EscherBoolProperty( EscherProperties.FILL__NOFILLHITTEST, 0x00010000 ) );
        }
        opt.addEscherProperty( new EscherRGBProperty( EscherProperties.FILL__FILLCOLOR, shape.getFillColor() ) );
        opt.addEscherProperty( new EscherBoolProperty( EscherProperties.GROUPSHAPE__PRINT, 0x080000 ) );
        opt.addEscherProperty( new EscherRGBProperty( EscherProperties.LINESTYLE__COLOR, shape.getLineStyleColor() ) );
        int options = 5;
        if (shape.getLineWidth() != HSSFShape.LINEWIDTH_DEFAULT)
        {
            opt.addEscherProperty( new EscherSimpleProperty( EscherProperties.LINESTYLE__LINEWIDTH, shape.getLineWidth()));
            options++;
        }
        if (shape.getLineStyle() != HSSFShape.LINESTYLE_SOLID)
        {
            opt.addEscherProperty( new EscherSimpleProperty( EscherProperties.LINESTYLE__LINEDASHING, shape.getLineStyle()));
            opt.addEscherProperty( new EscherSimpleProperty( EscherProperties.LINESTYLE__LINEENDCAPSTYLE, 0));
            if (shape.getLineStyle() == HSSFShape.LINESTYLE_NONE)
                opt.addEscherProperty( new EscherBoolProperty( EscherProperties.LINESTYLE__NOLINEDRAWDASH, 0x00080000));
            else
                opt.addEscherProperty( new EscherBoolProperty( EscherProperties.LINESTYLE__NOLINEDRAWDASH, 0x00080008));
            options += 3;
        }
        opt.sortProperties();
        return options;   // # options added
    }

    /**
     * Generate id for the CommonObjectDataSubRecord that stands behind this shape
     *
     * <p>
     *     Typically objectId starts with 1, is unique among all Obj record within the worksheet stream
     *     and increments by 1 for every new shape.
     *     For most shapes there is a straight relationship between shapeId (generated by DDF) and objectId:
     * </p>
     * <p>
     *     shapeId  is unique and starts with 1024, hence objectId can be derived as <code>shapeId-1024</code>.
     * </p>
     * <p>
     *     An exception from this rule is the CellComment shape whose objectId start with 1024.
     *      See {@link CommentShape#getCmoObjectId(int)}
     * </p>
     *
     *
     *
     * @param  shapeId   shape id as generated by drawing manager
     * @return objectId  object id that will be assigned to the Obj record
     */
    int getCmoObjectId(int shapeId){
        return shapeId - 1024;
    }
}
