package org.zkoss.zss.model;

import org.model.AutoRollbackConnection;

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
	SCellStyle getCellStyle();

	/**
	 * Set the local style
	 * @param cellStyle the style to set, null to clean local style
	 */
	void setCellStyle(SCellStyle cellStyle);

	void setCellStyle(SCellStyle cellStyle, boolean updateToDB);

	void setCellStyle(SCellStyle cellStyle, AutoRollbackConnection connection, boolean updateToDB);

	/**
	 * Get the cell style locally or look up from the parent
	 * @param local true to get the local style only 
	 */
	SCellStyle getCellStyle(boolean local);
}
