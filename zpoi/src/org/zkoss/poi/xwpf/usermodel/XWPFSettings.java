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
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDocProtect;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSettings;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTZoom;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STDocProtect;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.SettingsDocument;
import org.zkoss.poi.POIXMLDocumentPart;
import org.zkoss.poi.openxml4j.opc.PackagePart;
import org.zkoss.poi.openxml4j.opc.PackageRelationship;

public class XWPFSettings extends POIXMLDocumentPart {

    private CTSettings ctSettings;

    public XWPFSettings(PackagePart part, PackageRelationship rel) throws IOException {
        super(part, rel);
    }

    public XWPFSettings() {
        super();
        ctSettings = CTSettings.Factory.newInstance();
    }

    @Override
    protected void onDocumentRead() throws IOException
    {
        super.onDocumentRead();
        readFrom(getPackagePart().getInputStream());
    }

    /**
     * Set zoom.<br/>
     * In the zoom tag inside settings.xml file <br/>
     * it sets the value of zoom
     * <br/>
     * sample snippet from settings.xml
     * <pre>
     *    &lt;w:zoom w:percent="50" /&gt;
     * <pre>
     * @return percentage as an integer of zoom level
     */
    public long getZoomPercent() {
       CTZoom zoom;
       if (!ctSettings.isSetZoom()) {
          zoom = ctSettings.addNewZoom();
       } else {
          zoom = ctSettings.getZoom();
       }

       return zoom.getPercent().longValue();
    }

    /**
     * Set zoom.<br/>
     * In the zoom tag inside settings.xml file <br/>
     * it sets the value of zoom
     * <br/>
     * sample snippet from settings.xml 
     * <pre>
     *    &lt;w:zoom w:percent="50" /&gt; 
     * <pre>
     */
    public void setZoomPercent(long zoomPercent) {
       if (! ctSettings.isSetZoom()) {
          ctSettings.addNewZoom();
       }
       CTZoom zoom = ctSettings.getZoom();
       zoom.setPercent(BigInteger.valueOf(zoomPercent));
    }

    /**
     * Verifies the documentProtection tag inside settings.xml file <br/>
     * if the protection is enforced (w:enforcement="1") <br/>
     * and if the kind of protection equals to passed (STDocProtect.Enum editValue) <br/>
     * 
     * <br/>
     * sample snippet from settings.xml
     * <pre>
     *     &lt;w:settings  ... &gt;
     *         &lt;w:documentProtection w:edit=&quot;readOnly&quot; w:enforcement=&quot;1&quot;/&gt;
     * </pre>
     * 
     * @return true if documentProtection is enforced with option readOnly
     */
    public boolean isEnforcedWith(STDocProtect.Enum editValue) {
        CTDocProtect ctDocProtect = ctSettings.getDocumentProtection();

        if (ctDocProtect == null) {
            return false;
        }

        return ctDocProtect.getEnforcement().equals(STOnOff.X_1) && ctDocProtect.getEdit().equals(editValue);
    }

    /**
     * Enforces the protection with the option specified by passed editValue.<br/>
     * <br/>
     * In the documentProtection tag inside settings.xml file <br/>
     * it sets the value of enforcement to "1" (w:enforcement="1") <br/>
     * and the value of edit to the passed editValue (w:edit="[passed editValue]")<br/>
     * <br/>
     * sample snippet from settings.xml
     * <pre>
     *     &lt;w:settings  ... &gt;
     *         &lt;w:documentProtection w:edit=&quot;[passed editValue]&quot; w:enforcement=&quot;1&quot;/&gt;
     * </pre>
     */
    public void setEnforcementEditValue(org.openxmlformats.schemas.wordprocessingml.x2006.main.STDocProtect.Enum editValue) {
        safeGetDocumentProtection().setEnforcement(STOnOff.X_1);
        safeGetDocumentProtection().setEdit(editValue);
    }

    /**
     * Removes protection enforcement.<br/>
     * In the documentProtection tag inside settings.xml file <br/>
     * it sets the value of enforcement to "0" (w:enforcement="0") <br/>
     */
    public void removeEnforcement() {
        safeGetDocumentProtection().setEnforcement(STOnOff.X_0);
    }

    /**
     * Enforces fields update on document open (in Word).
     * In the settings.xml file <br/>
     * sets the updateSettings value to true (w:updateSettings w:val="true")
     * 
     *  NOTICES:
     *  <ul>
     *  	<li>Causing Word to ask on open: "This document contains fields that may refer to other files. Do you want to update the fields in this document?"
     *           (if "Update automatic links at open" is enabled)</li>
     *  	<li>Flag is removed after saving with changes in Word </li>
     *  </ul> 
     */
    public void setUpdateFields() {
    	CTOnOff onOff = CTOnOff.Factory.newInstance();
    	onOff.setVal(STOnOff.TRUE);
    	ctSettings.setUpdateFields(onOff);
    }

    boolean isUpdateFields() {
        return ctSettings.isSetUpdateFields() && ctSettings.getUpdateFields().getVal() == STOnOff.TRUE;
    }

    @Override
    protected void commit() throws IOException {
        if (ctSettings == null) {
           throw new IllegalStateException("Unable to write out settings that were never read in!");
        }

        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTSettings.type.getName().getNamespaceURI(), "settings"));
        Map<String, String> map = new HashMap<String, String>();
        map.put("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w");
        xmlOptions.setSaveSuggestedPrefixes(map);

        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        ctSettings.save(out, xmlOptions);
        out.close();
    }

    private CTDocProtect safeGetDocumentProtection() {
        CTDocProtect documentProtection = ctSettings.getDocumentProtection();
        if (documentProtection == null) {
            documentProtection = CTDocProtect.Factory.newInstance();
            ctSettings.setDocumentProtection(documentProtection);
        }
        return ctSettings.getDocumentProtection();
    }

    private void readFrom(InputStream inputStream) {
        try {
            ctSettings = SettingsDocument.Factory.parse(inputStream).getSettings();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
