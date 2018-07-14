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
package org.zkoss.poi.ss.usermodel;

/**
 * Repersents a picture in a SpreadsheetML document
 *
 * @author Yegor Kozlov
 */
public interface Picture {

    /**
     * Reset the image to the original size.
     *
     * <p>
     * Please note, that this method works correctly only for workbooks
     * with default font size (Arial 10pt for .xls and Calibri 11pt for .xlsx).
     * If the default font is changed the resized image can be streched vertically or horizontally.
     * </p>
     */
    void resize();

    /**
     * Reset the image to the original size.
     *
     * <p>
     * Please note, that this method works correctly only for workbooks
     * with default font size (Arial 10pt for .xls and Calibri 11pt for .xlsx).
     * If the default font is changed the resize() procedure can be 'off'.
     * </p>
     *
     * @param scale the amount by which image dimensions are multiplied relative to the original size.
     * <code>resize(1.0)</code> sets the original size, <code>resize(0.5)</code> resize to 50% of the original,
     * <code>resize(2.0)</code> resizes to 200% of the original.
     */
    void resize(double scale);

    ClientAnchor getPreferredSize();
    
    /**
     * Return picture data for this picture
     *
     * @return picture data for this picture
     */
    PictureData getPictureData();

    //20101015, henrichen@zkoss.org
    String getName();
    String getAlt();
    ClientAnchor getClientAnchor();
    String getPictureId();
    void setClientAnchor(ClientAnchor anchor);
}
