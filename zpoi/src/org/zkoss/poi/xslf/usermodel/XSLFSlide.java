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
import org.openxmlformats.schemas.drawingml.x2006.main.CTGroupShapeProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGroupTransform2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBlip;
import org.openxmlformats.schemas.presentationml.x2006.main.CTCommonSlideData;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGroupShapeNonVisual;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlide;
import org.openxmlformats.schemas.presentationml.x2006.main.SldDocument;
import org.openxmlformats.schemas.presentationml.x2006.main.CTBackground;

import java.awt.Graphics2D;
import java.io.IOException;

@Beta
public final class XSLFSlide extends XSLFSheet {
   private final CTSlide _slide;
   private XSLFSlideLayout _layout;
   private XSLFComments _comments;
   private XSLFNotes _notes;

    /**
     * Create a new slide
     */
    XSLFSlide() {
        super();
        _slide = prototype();
        setCommonSlideData(_slide.getCSld());
    }

    /**
     * Construct a SpreadsheetML slide from a package part
     *
     * @param part the package part holding the slide data,
     * the content type must be <code>application/vnd.openxmlformats-officedocument.slide+xml</code>
     * @param rel  the package relationship holding this slide,
     * the relationship type must be http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide
     */
    XSLFSlide(PackagePart part, PackageRelationship rel) throws IOException, XmlException {
        super(part, rel);

        SldDocument doc =
            SldDocument.Factory.parse(getPackagePart().getInputStream());
        _slide = doc.getSld();
        setCommonSlideData(_slide.getCSld());
    }


    private static CTSlide prototype(){
        CTSlide ctSlide = CTSlide.Factory.newInstance();
        CTCommonSlideData cSld = ctSlide.addNewCSld();
        CTGroupShape spTree = cSld.addNewSpTree();

        CTGroupShapeNonVisual nvGrpSpPr = spTree.addNewNvGrpSpPr();
        CTNonVisualDrawingProps cnvPr = nvGrpSpPr.addNewCNvPr();
        cnvPr.setId(1);
        cnvPr.setName("");
        nvGrpSpPr.addNewCNvGrpSpPr();
        nvGrpSpPr.addNewNvPr();

        CTGroupShapeProperties grpSpr = spTree.addNewGrpSpPr();
        CTGroupTransform2D xfrm = grpSpr.addNewXfrm();
        CTPoint2D off = xfrm.addNewOff();
        off.setX(0);
        off.setY(0);
        CTPositiveSize2D ext = xfrm.addNewExt();
        ext.setCx(0);
        ext.setCy(0);
        CTPoint2D choff = xfrm.addNewChOff();
        choff.setX(0);
        choff.setY(0);
        CTPositiveSize2D chExt = xfrm.addNewChExt();
        chExt.setCx(0);
        chExt.setCy(0);
        ctSlide.addNewClrMapOvr().addNewMasterClrMapping();
        return ctSlide;
    }

    @Override
	public CTSlide getXmlObject() {
		return _slide;
	}

    @Override
    protected String getRootElementName(){
        return "sld";        
    }

    @Override
    public XSLFSlideLayout getMasterSheet(){
        return getSlideLayout();
    }

    public XSLFSlideLayout getSlideLayout(){
        if(_layout == null){
             for (POIXMLDocumentPart p : getRelations()) {
                if (p instanceof XSLFSlideLayout){
                   _layout = (XSLFSlideLayout)p;
                }
            }
        }
        if(_layout == null) {
            throw new IllegalArgumentException("SlideLayout was not found for " + this.toString());
        }
        return _layout;
    }

    public XSLFSlideMaster getSlideMaster(){
        return getSlideLayout().getSlideMaster();
    }

    public XSLFComments getComments() {
       if(_comments == null) {
          for (POIXMLDocumentPart p : getRelations()) {
             if (p instanceof XSLFComments) {
                _comments = (XSLFComments)p;
             }
          }
       }
       if(_comments == null) {
          // This slide lacks comments
          // Not all have them, sorry...
          return null;
       }
       return _comments;
    }

    public XSLFNotes getNotes() {
       if(_notes == null) {
          for (POIXMLDocumentPart p : getRelations()) {
             if (p instanceof XSLFNotes){
                _notes = (XSLFNotes)p;
             }
          }
       }
       if(_notes == null) {
          // This slide lacks notes
          // Not all have them, sorry...
          return null;
       }
       return _notes;
    }

    /**
     *
     * @return title of this slide or empty string if title is not set
     */
    public String getTitle(){
        XSLFTextShape txt = getTextShapeByType(Placeholder.TITLE);
        return txt == null ? "" : txt.getText();
    }
    
    @Override
    public XSLFTheme getTheme(){
    	return getSlideLayout().getSlideMaster().getTheme();
    }

    /**
     *
     * @return the information about background appearance of this slide
     */
    @Override
    public XSLFBackground getBackground() {
        CTBackground bg = _slide.getCSld().getBg();
        if(bg != null) {
            return new XSLFBackground(bg, this);
        } else {
            return getMasterSheet().getBackground();
        }
    }

    /**
     *
     * @return whether shapes on the master slide should be shown  or not.
     */
    public boolean getFollowMasterGraphics(){
        return !_slide.isSetShowMasterSp() || _slide.getShowMasterSp();
    }

    /**
     *
     * @param value whether shapes on the master slide should be shown or not.
     */
    public void setFollowMasterGraphics(boolean value){
        _slide.setShowMasterSp(value);
    }


    @Override
    public void draw(Graphics2D graphics){

        XSLFBackground bg = getBackground();
        if(bg != null) bg.draw(graphics);

        super.draw(graphics);
    }


    @Override
    public XSLFSlide importContent(XSLFSheet src){
        super.importContent(src);

        XSLFBackground bgShape = getBackground();
        if(bgShape != null) {
            CTBackground bg = (CTBackground)bgShape.getXmlObject();
            if(bg.isSetBgPr() && bg.getBgPr().isSetBlipFill()){
                CTBlip blip = bg.getBgPr().getBlipFill().getBlip();
                String blipId = blip.getEmbed();

                String relId = importBlip(blipId, src.getPackagePart());
                blip.setEmbed(relId);
            }
        }
        return this;
    }

}
