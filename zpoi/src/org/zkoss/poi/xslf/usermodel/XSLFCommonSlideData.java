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

package org.zkoss.poi.xslf.usermodel;

import org.zkoss.poi.POIXMLException;
import org.zkoss.poi.util.Beta;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.values.XmlAnyTypeImpl;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObjectData;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTable;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.presentationml.x2006.main.CTApplicationNonVisualDrawingProps;
import org.openxmlformats.schemas.presentationml.x2006.main.CTCommonSlideData;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Beta
public class XSLFCommonSlideData {
    private final CTCommonSlideData data;

    public XSLFCommonSlideData(CTCommonSlideData data) {
        this.data = data;
    }
    
    public List<DrawingTextBody> getDrawingText() {
        CTGroupShape gs = data.getSpTree();

        List<DrawingTextBody> out = new ArrayList<DrawingTextBody>();

        processShape(gs, out);

        for (CTGroupShape shape : gs.getGrpSpList()) {
            processShape(shape, out);
        }

        for (CTGraphicalObjectFrame frame: gs.getGraphicFrameList()) {
            CTGraphicalObjectData data = frame.getGraphic().getGraphicData();
            XmlCursor c = data.newCursor();
            c.selectPath("declare namespace pic='"+CTTable.type.getName().getNamespaceURI()+"' .//pic:tbl");

            while (c.toNextSelection()) {
                XmlObject o = c.getObject();

                if (o instanceof XmlAnyTypeImpl) {
                    // Pesky XmlBeans bug - see Bugzilla #49934
                    try {
                        o = CTTable.Factory.parse(o.toString());
                    } catch (XmlException e) {
                        throw new POIXMLException(e);
                    }
                }

                if (o instanceof CTTable) {
                    DrawingTable table = new DrawingTable((CTTable) o);

                    for (DrawingTableRow row : table.getRows()) {
                        for (DrawingTableCell cell : row.getCells()) {
                            DrawingTextBody textBody = cell.getTextBody();
                            out.add(textBody);
                        }
                    }
                }
            }

            c.dispose();
        }

        return out;
    }
    public List<DrawingParagraph> getText() {
       List<DrawingParagraph> paragraphs = new ArrayList<DrawingParagraph>();
       for(DrawingTextBody textBody : getDrawingText()) {
          paragraphs.addAll(Arrays.asList(textBody.getParagraphs()));
       }
       return paragraphs;
    }

    private void processShape(CTGroupShape gs, List<DrawingTextBody> out) {
        List<CTShape> shapes = gs.getSpList();
        for (CTShape shape : shapes) {
            CTTextBody ctTextBody = shape.getTxBody();
            if (ctTextBody==null) {
                continue;
            }
            
            DrawingTextBody textBody;
            CTApplicationNonVisualDrawingProps nvpr = shape.getNvSpPr().getNvPr(); 
            if(nvpr.isSetPh()) {
               textBody = new DrawingTextPlaceholder(ctTextBody, nvpr.getPh());
            } else {
               textBody = new DrawingTextBody(ctTextBody);
            }

            out.add(textBody);
        }
    }
}
