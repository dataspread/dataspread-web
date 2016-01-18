/* XSSFTableStyleInfo.java

	Purpose:
		
	Description:
		
	History:
		Mar 16, 2015 12:02:57 PM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.poi.xssf.usermodel;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;

/**
 * @author henri
 * @since 3.9.7
 */
public class XSSFTableStyleInfo {
	final CTTableStyleInfo ctTableStyleInfo;
	public XSSFTableStyleInfo(CTTableStyleInfo ctTableStyleInfo) {
		this.ctTableStyleInfo = ctTableStyleInfo;
	}
	
	public String getName() {
		return ctTableStyleInfo.getName();
	}
	public void setName(String name) {
		if (name == null) {
			if (ctTableStyleInfo.isSetName())
				ctTableStyleInfo.unsetName();
			return;
		}
		ctTableStyleInfo.setName(name);
	}
	
	public boolean isShowColumnStripes() {
		return ctTableStyleInfo.isSetShowColumnStripes() ? 
				ctTableStyleInfo.getShowColumnStripes() : false;
	}
	public void setShowColumnStripes(boolean b) {
		ctTableStyleInfo.setShowColumnStripes(b);
	}
	
	public boolean isShowRowStripes() {
		return ctTableStyleInfo.isSetShowRowStripes() ? 
				ctTableStyleInfo.getShowRowStripes() : false;
	}
	public void setShowRowStripes(boolean b) {
		ctTableStyleInfo.setShowRowStripes(b);
	}
	
	public boolean isShowLastColumn() {
		return ctTableStyleInfo.isSetShowLastColumn() ? 
				ctTableStyleInfo.getShowLastColumn() : false;
	}
	public void setShowLastColumn(boolean b) {
		ctTableStyleInfo.setShowLastColumn(b);
	}

	public boolean isShowFirstColumn() {
		return ctTableStyleInfo.isSetShowFirstColumn() ? 
				ctTableStyleInfo.getShowFirstColumn() : false;
	}
	public void setShowFirstColumn(boolean b) {
		ctTableStyleInfo.setShowFirstColumn(b);
	}
}
