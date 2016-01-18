/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/01 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model;
/**
 * A picture in a sheet.
 * @author dennis
 * @since 3.5.0
 */
public interface SPicture {
	/**
	 * @since 3.5.0
	 */
	public enum Format{
		DIB,
		EMF,
		GIF,
		JPG,
		PICT,
		PNG,
		WMF;
		public String getFileExtension() {
			return name().toLowerCase();
		}
		
		/**
		 * Convert file extension to picture format
		 * @param fileExtension
		 * @return null if no corresponding format found
		 */
		public static Format valueOfFileExtension(String fileExtension) {
			if (fileExtension.equalsIgnoreCase("dib")){
				return DIB;
			}else if (fileExtension.equalsIgnoreCase("emf")){
				return EMF;
			}else if (fileExtension.equalsIgnoreCase("gif")){
				return GIF;
			}else if (fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("jpeg")){
				return JPG;
			}else if (fileExtension.equalsIgnoreCase("pct") || fileExtension.equalsIgnoreCase("pict") || fileExtension.equalsIgnoreCase("pic")){
				return PICT;
			}else if (fileExtension.equalsIgnoreCase("png")){
				return PNG;
			}else if (fileExtension.equalsIgnoreCase("wmf")){
				return WMF;
			}
			return null;
		}
		
	}
	public SSheet getSheet();
	
	public String getId();
	
	public Format getFormat();
	
	public byte[] getData();
	
	public ViewAnchor getAnchor();

	void setAnchor(ViewAnchor anchor);
	
	/**
	 * Returns the SPictureData
	 * @since 3.6.0
	 * @return
	 */
	public SPictureData getPictureData();
}
