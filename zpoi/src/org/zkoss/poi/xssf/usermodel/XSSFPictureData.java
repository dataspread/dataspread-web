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

package org.zkoss.poi.xssf.usermodel;

import java.io.IOException;

import org.zkoss.poi.POIXMLDocumentPart;
import org.zkoss.poi.POIXMLException;
import org.zkoss.poi.POIXMLRelation;
import org.zkoss.poi.openxml4j.opc.PackagePart;
import org.zkoss.poi.openxml4j.opc.PackageRelationship;
import org.zkoss.poi.ss.usermodel.PictureData;
import org.zkoss.poi.ss.usermodel.Workbook;
import org.zkoss.poi.util.IOUtils;

/**
 * Raw picture data, normally attached to a SpreadsheetML Drawing.
 * As a rule, pictures are stored in the /xl/media/ part of a SpreadsheetML package.
 */
public class XSSFPictureData extends POIXMLDocumentPart implements PictureData {

    /**
     * Relationships for each known picture type
     */
    protected static final POIXMLRelation[] RELATIONS;
    static {
        RELATIONS = new POIXMLRelation[13];
        RELATIONS[Workbook.PICTURE_TYPE_EMF] = XSSFRelation.IMAGE_EMF;
        RELATIONS[Workbook.PICTURE_TYPE_WMF] = XSSFRelation.IMAGE_WMF;
        RELATIONS[Workbook.PICTURE_TYPE_PICT] = XSSFRelation.IMAGE_PICT;
        RELATIONS[Workbook.PICTURE_TYPE_JPEG] = XSSFRelation.IMAGE_JPEG;
        RELATIONS[Workbook.PICTURE_TYPE_PNG] = XSSFRelation.IMAGE_PNG;
        RELATIONS[Workbook.PICTURE_TYPE_DIB] = XSSFRelation.IMAGE_DIB;
        RELATIONS[XSSFWorkbook.PICTURE_TYPE_GIF] = XSSFRelation.IMAGE_GIF;
        RELATIONS[XSSFWorkbook.PICTURE_TYPE_TIFF] = XSSFRelation.IMAGE_TIFF;
        RELATIONS[XSSFWorkbook.PICTURE_TYPE_EPS] = XSSFRelation.IMAGE_EPS;
        RELATIONS[XSSFWorkbook.PICTURE_TYPE_BMP] = XSSFRelation.IMAGE_BMP;
        RELATIONS[XSSFWorkbook.PICTURE_TYPE_WPG] = XSSFRelation.IMAGE_WPG;
    }

    /**
     * Create a new XSSFPictureData node
     *
     * @see org.zkoss.poi.xssf.usermodel.XSSFWorkbook#addPicture(byte[], int)
     */
    protected XSSFPictureData() {
        super();
    }

    /**
     * Construct XSSFPictureData from a package part
     *
     * @param part the package part holding the drawing data,
     * @param rel  the package relationship holding this drawing,
     * the relationship type must be http://schemas.openxmlformats.org/officeDocument/2006/relationships/image
     */
    protected XSSFPictureData(PackagePart part, PackageRelationship rel) {
        super(part, rel);
    }

    /**
     * Gets the picture data as a byte array.
     * <p>
     * Note, that this call might be expensive since all the picture data is copied into a temporary byte array.
     * You can grab the picture data directly from the underlying package part as follows:
     * <br/>
     * <code>
     * InputStream is = getPackagePart().getInputStream();
     * </code>
     * </p>
     *
     * @return the picture data.
     */
    public byte[] getData() {
        try {
            return IOUtils.toByteArray(getPackagePart().getInputStream());
        } catch(IOException e) {
            throw new POIXMLException(e);
        }
    }

    /**
     * Suggests a file extension for this image.
     *
     * @return the file extension.
     */
    public String suggestFileExtension() {
        return getPackagePart().getPartName().getExtension();
    }

    /**
     * Return an integer constant that specifies type of this picture
     *
     * @return an integer constant that specifies type of this picture 
     * @see org.zkoss.poi.ss.usermodel.Workbook#PICTURE_TYPE_EMF
     * @see org.zkoss.poi.ss.usermodel.Workbook#PICTURE_TYPE_WMF
     * @see org.zkoss.poi.ss.usermodel.Workbook#PICTURE_TYPE_PICT
     * @see org.zkoss.poi.ss.usermodel.Workbook#PICTURE_TYPE_JPEG
     * @see org.zkoss.poi.ss.usermodel.Workbook#PICTURE_TYPE_PNG
     * @see org.zkoss.poi.ss.usermodel.Workbook#PICTURE_TYPE_DIB
     */
    public int getPictureType(){
        String contentType = getPackagePart().getContentType();
        for (int i = 0; i < RELATIONS.length; i++) {
            if(RELATIONS[i] == null) continue;

            if(RELATIONS[i].getContentType().equals(contentType)){
                return i;
            }
        }
        return 0;
    }

    public String getMimeType() {
        return getPackagePart().getContentType();
    }
    
    //20111110, henrichen@zkoss.org: reference count
    @Override
    public int getRelationCounter() {
    	return super.getRelationCounter();
    }
}
