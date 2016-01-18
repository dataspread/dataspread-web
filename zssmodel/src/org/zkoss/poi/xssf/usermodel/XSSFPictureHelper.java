/* XSSFPictureHelper.java

	Purpose:
		
	Description:
		
	History:
		Oct 18, 2010 11:27:39 AM, Created by henrichen

Copyright (C) 2010 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.poi.xssf.usermodel;

import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTPicture;

/**
 * copied from zpoiex. We should remove it after integration.
 * raise protection.
 * @author henrichen
 *
 */
public class XSSFPictureHelper {
	public static XSSFPicture newXSSFPicture(XSSFDrawing drawing, XSSFClientAnchor anchor, CTPicture picture) {
		XSSFPicture pic = new XSSFPicture(drawing, picture);
		pic.anchor = anchor;
		return pic;
	}
}
