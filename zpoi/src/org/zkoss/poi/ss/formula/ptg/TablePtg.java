/*
{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Oct 13, 2014, Created by henri
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/

package org.zkoss.poi.ss.formula.ptg;

import org.zkoss.poi.ss.formula.FormulaRenderingWorkbook;
import org.zkoss.poi.util.LittleEndianOutput;

/**
 * TODO: XSSF only
 * @author henri
 *
 * @since 3.9.7
 */
public class TablePtg extends Area3DPtg { //ZSS-1013
	private static final long serialVersionUID = -4648300991532829249L;
	private String _tableName;
	private Item[] _items;
	private String[] _columns;
	
	private boolean _inTable;

	public TablePtg(){
		/* For seralization */
	}
	public TablePtg(int extIdx, int firstRow, int lastRow, int firstColumn, int lastColumn, 
			String tableName, Item[] items, String[] columns, boolean inTable) {
		super(firstRow, lastRow, firstColumn, lastColumn, false, false, false, false, extIdx); //ZSS-1013
		_tableName = tableName;
		_items = items;
		_columns = columns;
		_inTable = inTable;
	}
	
	public String getTableName() {
		return _tableName;
	}
	
	public void setTableName(String tableName) { //ZSS-966
		_tableName = tableName;
	}

	public Item getItem1() {
		return _items.length > 0 ? _items[0] : null; 
	}
	
	public Item getItem2() {
		return _items.length > 1 ? _items[1] : null; 
	}

	public String getColumn1() {
		return _columns.length > 0 ? _columns[0] : null; 
	}
	
	public void setColumn1(String column) {
		if (_columns.length > 0) {
			_columns[0] = column;
		}
	}
	
	public String getColumn2() {
		return _columns.length > 1 ? _columns[1] : null;
	}

	public void setColumn2(String column) {
		if (_columns.length > 1) {
			_columns[1] = column;
		}
	}

	public void write(LittleEndianOutput out) {
		throw new UnsupportedOperationException();
//		out.writeByte(sid + getPtgClass());
//		out.writeShort(field_1_label_index);
//		out.writeShort(field_2_zero);
	}

	public int getSize() {
		return 0;
	}

	public static String formatAsFormulaString(String tableName, Item item1, Item item2, String column1, String column2, boolean inTable) {
		return formatAsFormulaString0(tableName, item1, item2, column1, column2, inTable, false);
	}
	
	private static String formatAsFormulaString0(String tableName, 
			Item item1, Item item2, String column1, String column2, 
			boolean inTable, boolean internal) {
		final StringBuilder sb = new StringBuilder();
		int count = 0;
		if (item1 != null) {
			++count;
			if (item2 != null)
				++count;
		}
		if (column1 != null) {
			++count;
			if (column2 != null)
				++count;
		}
		if (item1 != null) {
			if (count > 1) {
				sb.append('[');
			}
			sb.append(item1.getName());
		}
		if (item2 != null) {
			sb.append("],[").append(item2.getName());
		}
		if (column1 != null) {
			if (sb.length() > 0) {
				sb.append("],[");
			} else if (count > 1 || column1.startsWith(" ")) {
				sb.append("[");
			}
			sb.append(column1);
		}
		if (column2 != null) {
			sb.append("]:[").append(column2);
		}
		if (count > 1 || (column1 != null && column1.startsWith(" "))) {
			sb.append(']');
		}
		return (inTable ? "" : tableName) + (sb.length() == 0 && !internal ? "" : ('[' + sb.toString() + ']'));
	}

	//ZSS-1013: override Area3DPtg
	public String toFormulaString(FormulaRenderingWorkbook book) {
		return toFormulaString(_inTable);
	}
//	public String toFormulaString() {
//		return toFormulaString(_inTable);
//	}
	
	private String toFormulaString(boolean inTable) {
		Item item1 = _items.length > 0 ? _items[0] : null;
		Item item2 = _items.length > 1 ? _items[1] : null;
		String column1 = _columns.length > 0 ? _columns[0] : null;
		String column2 = _columns.length > 1 ? _columns[1] : null;
		return formatAsFormulaString(_tableName, item1, item2, column1, column2, inTable);
	}
	
	//ZSS-1013: override Area3DPtg
	public String toInternalFormulaString(FormulaRenderingWorkbook book) {
		Item item1 = _items.length > 0 ? _items[0] : null;
		Item item2 = _items.length > 1 ? _items[1] : null;
		String column1 = _columns.length > 0 ? _columns[0] : null;
		String column2 = _columns.length > 1 ? _columns[1] : null;
		return formatAsFormulaString0(_tableName, item1, item2, column1, column2, _inTable, true);
	}
	
	//ZSS-966
//	public String toInternalFormulaString() {
//		Item item1 = _items.length > 0 ? _items[0] : null;
//		Item item2 = _items.length > 1 ? _items[1] : null;
//		String column1 = _columns.length > 0 ? _columns[0] : null;
//		String column2 = _columns.length > 1 ? _columns[1] : null;
//		return formatAsFormulaString0(_tableName, item1, item2, column1, column2, _inTable, true);
//	}

	public byte getDefaultOperandClass() {
		return Ptg.CLASS_REF;
	}

	public final String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getName());
		sb.append(" [");
		sb.append(toFormulaString(false));
		sb.append(" ");
		sb.append(formatReferenceAsString());
		sb.append("]");
		return sb.toString();
	}

	//ZSS-1002
	public String toCopyFormulaString() {
		Item item1 = _items.length > 0 ? _items[0] : null;
		Item item2 = _items.length > 1 ? _items[1] : null;
		String column1 = _columns.length > 0 ? _columns[0] : null;
		String column2 = _columns.length > 1 ? _columns[1] : null;
		return formatAsFormulaString0(_tableName, item1, item2, column1, column2, false, false);
	}

	public enum Item {
		ALL("#All"), HEADERS("#Headers"), DATA("#Data"), TOTALS("#Totals"), THIS_ROW("#This Row");
		
		private String _name;
		private Item(String name) {
			_name = name;
		}
		
		public String getName() {
			return _name;
		}
		
		public static Item valueOfName(String name) {
			if ("#All".equalsIgnoreCase(name)) {
				return ALL;
			}
			if ("#Data".equalsIgnoreCase(name)) {
				return DATA;
			}
			if ("#Headers".equalsIgnoreCase(name)) {
				return HEADERS;
			}
			if ("#Totals".equalsIgnoreCase(name)) {
				return TOTALS;
			}
			if ("#This Row".equalsIgnoreCase(name)) {
				return THIS_ROW;
			}
			return null;
		}
	}
}
