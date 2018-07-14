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
package org.zkoss.poi.xssf.model;

import java.io.IOException;

import org.zkoss.poi.POIXMLDocumentPart;
import org.zkoss.poi.openxml4j.opc.PackagePart;
import org.zkoss.poi.openxml4j.opc.PackageRelationship;
import org.zkoss.poi.xssf.usermodel.XSSFColor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColorScheme;
import org.openxmlformats.schemas.drawingml.x2006.main.CTHslColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPresetColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTScRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSystemColor;
import org.openxmlformats.schemas.drawingml.x2006.main.ThemeDocument;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColor;
import org.zkoss.poi.POIXMLDocumentPart;
import org.zkoss.poi.openxml4j.opc.PackagePart;
import org.zkoss.poi.openxml4j.opc.PackageRelationship;
import org.zkoss.poi.xssf.usermodel.XSSFColor;

/**
 * Class that represents theme of XLSX document. The theme includes specific
 * colors and fonts.
 * 
 * @author Petr Udalau(Petr.Udalau at exigenservices.com) - theme colors
 */
public class ThemesTable extends POIXMLDocumentPart {
    private ThemeDocument theme;

    public ThemesTable(PackagePart part, PackageRelationship rel) throws IOException {
        super(part, rel);
        
        try {
           theme = ThemeDocument.Factory.parse(part.getInputStream());
        } catch(XmlException e) {
           throw new IOException(e.getLocalizedMessage());
        }
    }

    public ThemesTable(ThemeDocument theme) {
        this.theme = theme;
    }

    //ZSS-1022
    private XSSFColor getRGBColor(CTColor ctColor) {
    	if (ctColor != null) {
	    	CTSystemColor sysClr = ctColor.getSysClr();
	    	if (sysClr != null) return new XSSFColor(sysClr.getLastClr());
	    	
	    	CTSRgbColor srgbClr = ctColor.getSrgbClr();
	    	if (srgbClr != null) return new XSSFColor(srgbClr.getVal());
	    	
	//    	CTScRgbColor scrgbClr = ctColor.getScrgbClr();
	//    	if (scrgbClr != null) return scrgbClr.get
	    	
	//    	CTSchemeColor schemeClr = ctColor.getSchemeClr();
	//    	if (schemeClr != null) return schemeClr.getVal();
	    	
	//    	CTPresetColor prstClr = ctColor.getPrstClr();
	//    	if (prstClr != null) return prstClr.getVal();
	    	
	//    	CTHslColor hslClr = ctColor.getHslClr();
	//    	if (hslClr != null) return hslClr.get
    	}
    	
    	return null;
    }
    public XSSFColor getThemeColor(int idx) {
        CTColorScheme colorScheme = theme.getTheme().getThemeElements().getClrScheme();
        //20101123, henrichen@zkoss.org: shall handle System Color case
    	switch(idx) {
    	case 0: //lt1
    		return getRGBColor(colorScheme.getLt1()); //ZSS-1022
    	case 1: //dk1
    		return getRGBColor(colorScheme.getDk1()); //ZSS-1022
    	case 2: //lt2
    		return getRGBColor(colorScheme.getLt2()); //ZSS-1022
    	case 3: //dk2
    		return getRGBColor(colorScheme.getDk2()); //ZSS-1022
    	default:
	        CTColor ctColor = null;
	        int cnt = 0;
	        for (XmlObject obj : colorScheme.selectPath("./*")) {
	            if (obj instanceof org.openxmlformats.schemas.drawingml.x2006.main.CTColor) {
	                if (cnt == idx) {
	                    ctColor = (org.openxmlformats.schemas.drawingml.x2006.main.CTColor) obj;
	                    return getRGBColor(ctColor); //ZSS-1022
	                }
	                cnt++;
	            }
	        }
	        return null;
    	}
    }
    
    /**
     * If the colour is based on a theme, then inherit 
     *  information (currently just colours) from it as
     *  required.
     */
    public void inheritFromThemeAsRequired(XSSFColor color) {
       if(color == null) {
          // Nothing for us to do
          return;
       }
       if(! color.getCTColor().isSetTheme()) {
          // No theme set, nothing to do
          return;
       }
       
       // Get the theme colour
       XSSFColor themeColor = getThemeColor(color.getTheme());
       // Set the raw colour, not the adjusted one
       // Do a raw set, no adjusting at the XSSFColor layer either
       color.getCTColor().setRgb(themeColor.getCTColor().getRgb());
       
       // All done
    }
}
