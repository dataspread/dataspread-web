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
package org.zkoss.poi.xwpf.usermodel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.zkoss.poi.POIXMLDocumentPart;
import org.zkoss.poi.POIXMLException;
import org.zkoss.poi.openxml4j.opc.PackagePart;
import org.zkoss.poi.openxml4j.opc.PackageRelationship;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHdrFtr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumbering;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.FtrDocument;
import org.zkoss.poi.POIXMLDocumentPart;
import org.zkoss.poi.openxml4j.opc.PackagePart;
import org.zkoss.poi.openxml4j.opc.PackageRelationship;

/**
 * Sketch of XWPF footer class
 */
public class XWPFFooter extends XWPFHeaderFooter {
    public XWPFFooter() {
        super();
    }

    public XWPFFooter(XWPFDocument doc, CTHdrFtr hdrFtr) throws IOException {
        super(doc, hdrFtr);
        XmlCursor cursor = headerFooter.newCursor();
        cursor.selectPath("./*");
        while (cursor.toNextSelection()) {
            XmlObject o = cursor.getObject();
            if (o instanceof CTP) {
                XWPFParagraph p = new XWPFParagraph((CTP)o, this);
                paragraphs.add(p);
                bodyElements.add(p);
            }
            if (o instanceof CTTbl) {
                XWPFTable t = new XWPFTable((CTTbl)o, this);
                tables.add(t);
                bodyElements.add(t);
            }
        }
        cursor.dispose();
    }

    public XWPFFooter(POIXMLDocumentPart parent, PackagePart part, PackageRelationship rel) throws IOException {
        super(parent, part, rel);
    }

    /**
     * save and commit footer
     */
    @Override
    protected void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTNumbering.type.getName().getNamespaceURI(), "ftr"));
        Map<String,String> map = new HashMap<String, String>();
        map.put("http://schemas.openxmlformats.org/markup-compatibility/2006", "ve");
        map.put("urn:schemas-microsoft-com:office:office", "o");
        map.put("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "r");
        map.put("http://schemas.openxmlformats.org/officeDocument/2006/math", "m");
        map.put("urn:schemas-microsoft-com:vml", "v");
        map.put("http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", "wp");
        map.put("urn:schemas-microsoft-com:office:word", "w10");
        map.put("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w");
        map.put("http://schemas.microsoft.com/office/word/2006/wordml", "wne");
        xmlOptions.setSaveSuggestedPrefixes(map);
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        super._getHdrFtr().save(out, xmlOptions);
        out.close();
    }

    @Override  
    protected void onDocumentRead() throws IOException{
        super.onDocumentRead();
        FtrDocument ftrDocument = null;
        InputStream is;
        try {
            is = getPackagePart().getInputStream();
            ftrDocument = FtrDocument.Factory.parse(is);
            headerFooter = ftrDocument.getFtr();
            // parse the document with cursor and add
            // the XmlObject to its lists
            XmlCursor cursor = headerFooter.newCursor();
            cursor.selectPath("./*");
            while (cursor.toNextSelection()) {
                XmlObject o = cursor.getObject();
                if (o instanceof CTP) {
                    XWPFParagraph p = new XWPFParagraph((CTP)o, this);
                    paragraphs.add(p);
                    bodyElements.add(p);
                }
                if (o instanceof CTTbl) {
                    XWPFTable t = new XWPFTable((CTTbl)o, this);
                    tables.add(t);
                    bodyElements.add(t);
                }
            }
            cursor.dispose();
        } catch (Exception e) {
            throw new POIXMLException(e);
        }
    }

    /**
     * get the PartType of the body
     * @see org.zkoss.poi.xwpf.usermodel.IBody#getPartType()
     */
    public BodyType getPartType() {
        return BodyType.FOOTER;
    }
}
