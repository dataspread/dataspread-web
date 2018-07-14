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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyles;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.StylesDocument;
import org.zkoss.poi.POIXMLDocumentPart;
import org.zkoss.poi.POIXMLException;
import org.zkoss.poi.openxml4j.exceptions.OpenXML4JException;
import org.zkoss.poi.openxml4j.opc.PackagePart;
import org.zkoss.poi.openxml4j.opc.PackageRelationship;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPrDefault;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTLanguage;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFonts;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDocDefaults;
/**
 * @author Philipp Epp
 *
 */
public class XWPFStyles extends POIXMLDocumentPart{
    
    private List<XWPFStyle> listStyle = new ArrayList<XWPFStyle>();
    private CTStyles ctStyles;
    XWPFLatentStyles latentStyles;

    /**
     * Construct XWPFStyles from a package part
     *
     * @param part the package part holding the data of the styles,
     * @param rel  the package relationship of type "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles"
     */

	public XWPFStyles(PackagePart part, PackageRelationship rel) throws IOException, OpenXML4JException{
		super(part, rel);
	}

	/**
	 * Construct XWPFStyles from scratch for a new document.
	 */
	public XWPFStyles() {
	}

   /**
    * Read document
    */
   @Override
   protected void onDocumentRead() throws IOException{
      StylesDocument stylesDoc;
      try {
         InputStream is = getPackagePart().getInputStream();
         stylesDoc = StylesDocument.Factory.parse(is);
         ctStyles = stylesDoc.getStyles();
         latentStyles = new XWPFLatentStyles(ctStyles.getLatentStyles(), this);
      } catch (XmlException e) {
         throw new POIXMLException("Unable to read styles", e);
      }
      
      // Build up all the style objects
      for(CTStyle style : ctStyles.getStyleList()) {
         listStyle.add(new XWPFStyle(style, this));
      }
   }
	
   @Override
   protected void commit() throws IOException {
      if (ctStyles == null) {
         throw new IllegalStateException("Unable to write out styles that were never read in!");
      }
      
      XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
      xmlOptions.setSaveSyntheticDocumentElement(new QName(CTStyles.type.getName().getNamespaceURI(), "styles"));
      Map<String,String> map = new HashMap<String,String>();
      map.put("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "r");
      map.put("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w");
      xmlOptions.setSaveSuggestedPrefixes(map);
      PackagePart part = getPackagePart();
      OutputStream out = part.getOutputStream();
      ctStyles.save(out, xmlOptions);
      out.close();
   }
	
    /**
     * Sets the ctStyles
     * @param styles
     */
    public void setStyles(CTStyles styles) {
       ctStyles = styles;
    }
	
	 /**
	  * checks whether style with styleID exist
	  * @param styleID		styleID of the Style in the style-Document
	  * @return				true if style exist, false if style not exist
	  */
	public boolean styleExist(String styleID){
		for (XWPFStyle style : listStyle) {
			if (style.getStyleId().equals(styleID))
				return true;
		}
		return false;
	}
	/**
	 * add a style to the document
	 * @param style				
	 * @throws IOException		 
	 */
	public void addStyle(XWPFStyle style){
		listStyle.add(style);
		ctStyles.addNewStyle();
		int pos = (ctStyles.getStyleList().size()) - 1;
		ctStyles.setStyleArray(pos, style.getCTStyle());
	}
	/**
	 *get style by a styleID 
	 * @param styleID	styleID of the searched style
	 * @return style
	 */
	public XWPFStyle getStyle(String styleID){
		for (XWPFStyle style : listStyle) {
			if(style.getStyleId().equals(styleID))
				return style;		
		}
		return null;
	}

	/**
	 * get the styles which are related to the parameter style and their relatives
	 * this method can be used to copy all styles from one document to another document 
	 * @param style
	 * @return a list of all styles which were used by this method 
	 */
	public List<XWPFStyle> getUsedStyleList(XWPFStyle style){
		List<XWPFStyle> usedStyleList = new ArrayList<XWPFStyle>();
		usedStyleList.add(style);
		return getUsedStyleList(style, usedStyleList);
	}
	
	/** 
	 * get the styles which are related to parameter style
	 * @param style
	 * @return all Styles of the parameterList
	 */
	private List<XWPFStyle> getUsedStyleList(XWPFStyle style, List<XWPFStyle> usedStyleList){
		String basisStyleID  = style.getBasisStyleID();
		XWPFStyle basisStyle = getStyle(basisStyleID);
		if((basisStyle!=null)&&(!usedStyleList.contains(basisStyle))){
			usedStyleList.add(basisStyle);
			getUsedStyleList(basisStyle, usedStyleList);
		}		
		String linkStyleID = style.getLinkStyleID();
		XWPFStyle linkStyle = getStyle(linkStyleID);
		if((linkStyle!=null)&&(!usedStyleList.contains(linkStyle))){
			usedStyleList.add(linkStyle);
			getUsedStyleList(linkStyle, usedStyleList);
		}
		
		String nextStyleID = style.getNextStyleID();
		XWPFStyle nextStyle = getStyle(nextStyleID);
		if((nextStyle!=null)&&(!usedStyleList.contains(nextStyle))){
			usedStyleList.add(linkStyle);
			getUsedStyleList(linkStyle, usedStyleList);
		}		
		return usedStyleList;
	}
	
	/**
	 * Sets the default spelling language on ctStyles DocDefaults parameter
	 * @param strSpellingLanguage
	 */
	public void setSpellingLanguage(String strSpellingLanguage) {
		CTDocDefaults docDefaults = null;
		CTRPr runProps = null;
		CTLanguage lang = null;

		// Just making sure we use the members that have already been defined
		if(ctStyles.isSetDocDefaults()) {
			docDefaults = ctStyles.getDocDefaults();
			if(docDefaults.isSetRPrDefault()) {
				CTRPrDefault RPrDefault = docDefaults.getRPrDefault();
				if(RPrDefault.isSetRPr()) {
					runProps = RPrDefault.getRPr();
					if(runProps.isSetLang())
						lang = runProps.getLang();
				}
			}
		}

		if(docDefaults == null)
			docDefaults = ctStyles.addNewDocDefaults();
		if(runProps == null)
			runProps = docDefaults.addNewRPrDefault().addNewRPr();
		if(lang == null)
			lang = runProps.addNewLang();

		lang.setVal(strSpellingLanguage);
		lang.setBidi(strSpellingLanguage);
	}

	/**
	 * Sets the default East Asia spelling language on ctStyles DocDefaults parameter
	 * @param strEastAsia
	 */
	public void setEastAsia(String strEastAsia) {
		CTDocDefaults docDefaults = null;
		CTRPr runProps = null;
		CTLanguage lang = null;

		// Just making sure we use the members that have already been defined
		if(ctStyles.isSetDocDefaults()) {
			docDefaults = ctStyles.getDocDefaults();
			if(docDefaults.isSetRPrDefault()) {
				CTRPrDefault RPrDefault = docDefaults.getRPrDefault();
				if(RPrDefault.isSetRPr()) {
					runProps = RPrDefault.getRPr();
					if(runProps.isSetLang())
						lang = runProps.getLang();
				}
			}
		}

		if(docDefaults == null)
			docDefaults = ctStyles.addNewDocDefaults();
		if(runProps == null)
			runProps = docDefaults.addNewRPrDefault().addNewRPr();
		if(lang == null)
			lang = runProps.addNewLang();

		lang.setEastAsia(strEastAsia);
	}

	/**
	 * Sets the default font on ctStyles DocDefaults parameter
	 * @param fonts
	 */
	public void setDefaultFonts(CTFonts fonts) {
		CTDocDefaults docDefaults = null;
		CTRPr runProps = null;

		// Just making sure we use the members that have already been defined
		if(ctStyles.isSetDocDefaults()) {
			docDefaults = ctStyles.getDocDefaults();
			if(docDefaults.isSetRPrDefault()) {
				CTRPrDefault RPrDefault = docDefaults.getRPrDefault();
				if(RPrDefault.isSetRPr()) {
					runProps = RPrDefault.getRPr();
				}
			}
		}

		if(docDefaults == null)
			docDefaults = ctStyles.addNewDocDefaults();
		if(runProps == null)
			runProps = docDefaults.addNewRPrDefault().addNewRPr();

		runProps.setRFonts(fonts);
	}
	
	
	/**
	 * get latentstyles
	 */
	public XWPFLatentStyles getLatentStyles() {
		return latentStyles;
	}
	
	/**
	 * get the style with the same name
	 * if this style is not existing, return null
	 */
	public XWPFStyle getStyleWithSameName(XWPFStyle style){
		for (XWPFStyle ownStyle : listStyle) {
			if(ownStyle.hasSameName(style)){
				return ownStyle;
			}	
		}
		return null;
		
	}
}//end class
