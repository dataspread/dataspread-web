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

import org.zkoss.poi.POIXMLDocumentPart;
import org.zkoss.poi.openxml4j.opc.PackagePart;
import org.zkoss.poi.openxml4j.opc.PackageRelationship;
import org.zkoss.poi.util.Beta;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.presentationml.x2006.main.CTCommonSlideData;
import org.openxmlformats.schemas.presentationml.x2006.main.CTNotesSlide;
import org.openxmlformats.schemas.presentationml.x2006.main.NotesDocument;

import java.io.IOException;

@Beta
public final class XSLFNotes extends XSLFSheet {
   private CTNotesSlide _notes;

    /**
     * Create a new notes
     */
    XSLFNotes() {
        super();
        _notes = prototype();
        setCommonSlideData(_notes.getCSld());
    }

    /**
     * Construct a SpreadsheetML notes from a package part
     *
     * @param part the package part holding the notes data,
     * the content type must be <code>application/vnd.openxmlformats-officedocument.notes+xml</code>
     * @param rel  the package relationship holding this notes,
     * the relationship type must be http://schemas.openxmlformats.org/officeDocument/2006/relationships/notes
     */
    XSLFNotes(PackagePart part, PackageRelationship rel) throws IOException, XmlException {
        super(part, rel);

        NotesDocument doc =
            NotesDocument.Factory.parse(getPackagePart().getInputStream());
        _notes = doc.getNotes();
        setCommonSlideData(_notes.getCSld());
    }


    private static CTNotesSlide prototype(){
        CTNotesSlide ctNotes = CTNotesSlide.Factory.newInstance();
        CTCommonSlideData cSld = ctNotes.addNewCSld();

        // TODO What else is needed for a mininum notes?

        return ctNotes;
    }

    @Override
    public CTNotesSlide getXmlObject() {
       return _notes;
    }

    @Override
    protected String getRootElementName(){
        return "notes";        
    }

    @Override
    public XSLFNotesMaster getMasterSheet() {
        for (POIXMLDocumentPart p : getRelations()) {
           if (p instanceof XSLFNotesMaster){
              return (XSLFNotesMaster)p;
           }
        }
        return null;
    }
}
