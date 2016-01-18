/*  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ==================================================================== */
package org.zkoss.poi.ss.usermodel;

import java.util.List;

import org.zkoss.poi.ss.util.AreaReference;
import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.poi.ss.util.ItemInfo;

/**
 * @author sam
 *
 */
public interface PivotTable {
	/**
	 * Sets the name of PivotTable.
	 * 
	 * @param name
	 */
	public void setName(String name);
	
	/**
	 * Returns the name of the PivotTable.
	 * 
	 * @return name
	 */
	public String getName();
	
	/**
	 * Returns the cache id of the PivotTable.
	 * 
	 * @return cache id
	 */
	public long getCacheId();
	
	/**
	 * Sets the grand total caption.
	 * 
	 * @param caption
	 */
	public void setGrandTotalCaption(String caption);
	
	/**
	 * Returns the grand total caption.
	 * 
	 * @return caption
	 */
	public String getGrandTotalCaption();
	
	/**
	 * Sets the data caption.
	 * 
	 * @param caption
	 */
	public void setDataCaption(String caption);
	
	/**
	 * Returns the data caption.
	 * 
	 * @return
	 */
	public String getDataCaption();
	
	/**
	 * Sets the row header caption.
	 * 
	 * @param caption
	 */
	public void setRowHeaderCaption(String caption);
	
	/**
	 * Returns the row header caption.
	 * 
	 * @return
	 */
	public String getRowHeaderCaption();
	
	/**
	 * Sets the first header row.
	 * 
	 * @param row
	 */
	public void setFirstHeaderRow(int row);
	
	/**
	 * Sets the first data row/column index. 
	 * 
	 * @param row
	 * @param col
	 */
	public void setFirstData(int row, int col);
	
	/**
	 * Returns the first data {@link CellReference}
	 * 
	 * @return
	 */
	public CellReference getFirstDataRef();
	
	/**
	 * Sets the location range of the PivotTable. 
	 * 
	 * @param ref
	 */
	public void setLocationRef(AreaReference ref);
	
	/**
	 * Returns the location range of the PivotTable.
	 * 
	 * @return
	 */
	public AreaReference getLocationRef();
	
	/**
	 * Returns the {@link PivotCache} of the PivotTable.
	 * 
	 * @return
	 */
	public PivotCache getPivotCache();

	/**
	 * Returns the {@link PivotField}.
	 * 
	 * @param name
	 * @return
	 */
	public PivotField getPivotField(String name);
	
	/**
	 * Returns all fields of the PivotTable.
	 * 
	 * @return
	 */
	public List<PivotField> getPivotFields();
	
	/**
	 * Sets row type of a pivot field.
	 * 
	 * @param field
	 */
	public void setRowField(PivotField field);
	
	/**
	 * Returns all row fields.
	 * 
	 * @return
	 */
	public List<PivotField> getRowFields();
	
	/**
	 * Sets row items.
	 * 
	 * @param items
	 */
	public void setRowItems(List<List<ItemInfo>> items);
	
	/**
	 * Sets column type of a pivot field.
	 * 
	 * @param field
	 */
	public void setColumnField(PivotField field);
	
	/**
	 * Returns all column fields. 
	 * 
	 * @return
	 */
	public List<PivotField> getColumnFields();
	
	/**
	 * Serts column items
	 * 
	 * @param items
	 */
	public void setColumnItems(List<List<ItemInfo>> items);
	
	/**
	 * Sets data field
	 * 
	 * @param field
	 * @param name
	 * @param subtotal
	 */
	public void setDataField(PivotField field, String name, Calculation subtotal);
	
	/**
	 * Returns all data fields.
	 * 
	 * @return
	 */
	public List<DataField> getDataFields();
	
	/**
	 * Sets data on rows
	 * 
	 * @param dataOnRows
	 */
	public void setDataOnRows(boolean dataOnRows);
	
	/**
	 * Returns whether data on rows or not.
	 * 
	 * @return
	 */
	public boolean getDataOnRows();
	
	public void setOutline(boolean outline);
	
	public boolean getOutline();
	
	public void setOutlineData(boolean outlineData);
	
	public boolean getOutlineData();
}