package org.zkoss.zss.model.sys.dependency;

/**
 * 
 * @author henrichen
 * @since 3.8.0
 */
public interface ColumnRef extends Ref{
	
	String getTableName();
	
	String getColumnName1();
	
	String getColumnName2();
	
	boolean isWithHeaders(); //ZSS-967
}
