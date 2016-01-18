package org.zkoss.zss.model;
/**
 * the style holder interface
 * @author Dennis
 * @since 3.5.0
 */
public interface CellStyleHolder {
	/**
	 * Get the style, if it doesn't has local style, it will possible look up it's parent's style
	 * @see #getCellStyle(boolean)
	 */
	public SCellStyle getCellStyle();
	
	/**
	 * Set the local style
	 * @param cellStyle the style to set, null to clean local style
	 */
	public void setCellStyle(SCellStyle cellStyle);
	
	/**
	 * Get the cell style locally or look up from the parent
	 * @param local true to get the local style only 
	 */
	public SCellStyle getCellStyle(boolean local);
}
