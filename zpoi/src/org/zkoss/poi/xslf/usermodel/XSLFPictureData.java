/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.zkoss.poi.xslf.usermodel;

import org.zkoss.poi.POIXMLDocumentPart;
import org.zkoss.poi.POIXMLException;
import org.zkoss.poi.POIXMLRelation;
import org.zkoss.poi.openxml4j.opc.PackagePart;
import org.zkoss.poi.openxml4j.opc.PackageRelationship;
import org.zkoss.poi.util.Beta;
import org.zkoss.poi.util.IOUtils;

import java.io.IOException;

/**
 * Instantiates sub-classes of POIXMLDocumentPart depending on their relationship type
 *
 * @author Yegor Kozlov
 */
@Beta
public final class XSLFPictureData extends POIXMLDocumentPart {
    /**
     * Extended windows meta file
     */
    public static final int PICTURE_TYPE_EMF = 2;

    /**
     * Windows Meta File
     */
    public static final int PICTURE_TYPE_WMF = 3;

    /**
     * Mac PICT format
     */
    public static final int PICTURE_TYPE_PICT = 4;

    /**
     * JPEG format
     */
    public static final int PICTURE_TYPE_JPEG = 5;

    /**
     * PNG format
     */
    public static final int PICTURE_TYPE_PNG = 6;

    /**
     * Device independent bitmap
     */
    public static final int PICTURE_TYPE_DIB = 7;

    /**
     * GIF image format
     */
    public static final int PICTURE_TYPE_GIF = 8;

    /**
     * Tag Image File (.tiff)
     */
    public static final int PICTURE_TYPE_TIFF = 9;

    /**
     * Encapsulated Postscript (.eps)
     */
    public static final int PICTURE_TYPE_EPS = 10;


    /**
     * Windows Bitmap (.bmp)
     */
    public static final int PICTURE_TYPE_BMP = 11;

    /**
     * WordPerfect graphics (.wpg)
     */
    public static final int PICTURE_TYPE_WPG = 12;

    /**
     * Relationships for each known picture type
     */
    protected static final POIXMLRelation[] RELATIONS;

    static {
        RELATIONS = new POIXMLRelation[13];
        RELATIONS[PICTURE_TYPE_EMF] = XSLFRelation.IMAGE_EMF;
        RELATIONS[PICTURE_TYPE_WMF] = XSLFRelation.IMAGE_WMF;
        RELATIONS[PICTURE_TYPE_PICT] = XSLFRelation.IMAGE_PICT;
        RELATIONS[PICTURE_TYPE_JPEG] = XSLFRelation.IMAGE_JPEG;
        RELATIONS[PICTURE_TYPE_PNG] = XSLFRelation.IMAGE_PNG;
        RELATIONS[PICTURE_TYPE_DIB] = XSLFRelation.IMAGE_DIB;
        RELATIONS[PICTURE_TYPE_GIF] = XSLFRelation.IMAGE_GIF;
        RELATIONS[PICTURE_TYPE_TIFF] = XSLFRelation.IMAGE_TIFF;
        RELATIONS[PICTURE_TYPE_EPS] = XSLFRelation.IMAGE_EPS;
        RELATIONS[PICTURE_TYPE_BMP] = XSLFRelation.IMAGE_BMP;
        RELATIONS[PICTURE_TYPE_WPG] = XSLFRelation.IMAGE_WPG;
    }

    private Long checksum = null;

    /**
     * Create a new XSLFGraphicData node
     */
    protected XSLFPictureData() {
        super();
    }

    /**
     * Construct XSLFPictureData from a package part
     *
     * @param part the package part holding the drawing data,
     * @param rel  the package relationship holding this drawing,
     *             the relationship type must be http://schemas.openxmlformats.org/officeDocument/2006/relationships/image
     */
    public XSLFPictureData(PackagePart part, PackageRelationship rel) {
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
     * @return the Picture data.
     */
    public byte[] getData() {
        try {
            return IOUtils.toByteArray(getPackagePart().getInputStream());
        } catch (IOException e) {
            throw new POIXMLException(e);
        }
    }

    /**
     * Returns the file name of the image, eg image7.jpg . The original filename
     * isn't always available, but if it can be found it's likely to be in the
     * CTDrawing
     */
    public String getFileName() {
        String name = getPackagePart().getPartName().getName();
        if (name == null)
            return null;
        return name.substring(name.lastIndexOf('/') + 1);
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
     */
    public int getPictureType() {
        String contentType = getPackagePart().getContentType();
        for (int i = 0; i < RELATIONS.length; i++) {
            if (RELATIONS[i] == null) {
                continue;
            }

            if (RELATIONS[i].getContentType().equals(contentType)) {
                return i;
            }
        }
        return 0;
    }

    long getChecksum(){
        if(checksum == null){
            byte[] pictureData = getData();
            checksum = IOUtils.calculateChecksum(pictureData);
        }
        return checksum;
    }
}