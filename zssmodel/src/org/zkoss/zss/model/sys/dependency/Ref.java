package org.zkoss.zss.model.sys.dependency;

/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public interface Ref {
	/**
	 * @since 3.5.0
	 */
	public enum RefType {
		CELL, AREA, SHEET, BOOK, NAME, OBJECT, INDIRECT, TABLE, 
	}

	public RefType getType();

	public String getBookName();

	public String getSheetName();
	
	public String getLastSheetName();

	public int getRow();

	public int getColumn();

	public int getLastRow();

	public int getLastColumn();
	
	//ZSS-815
	//since 3.7.0
	public int getSheetIndex(); 
	
	//ZSS-815
	//since 3.7.0
	public int getLastSheetIndex();
}
