/* SNamedTableStyle.java

	Purpose:
		
	Description:
		
	History:
		Mar 30, 2015 4:18:59 PM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model;

/**
 * Table Style.
 * @author henri
 * @since 3.8.0
 */
public interface STableStyle {
	public String getName();
	public STableStyleElem getWholeTableStyle();
	
	public STableStyleElem getColStripe1Style();
	public int getColStrip1Size();
	
	public STableStyleElem getColStripe2Style();
	public int getColStrip2Size();
	
	public STableStyleElem getRowStripe1Style();
	public int getRowStrip1Size();
	
	public STableStyleElem getRowStripe2Style();
	public int getRowStrip2Size();
	
	public STableStyleElem getLastColumnStyle();
	public STableStyleElem getFirstColumnStyle();
	
	public STableStyleElem getHeaderRowStyle();
	public STableStyleElem getTotalRowStyle();
	
	public STableStyleElem getFirstHeaderCellStyle();
	public STableStyleElem getLastHeaderCellStyle();

	public STableStyleElem getFirstTotalCellStyle();
	public STableStyleElem getLastTotalCellStyle();
}
