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
import java.util.Set;


/**
 * @author sam
 *
 */
public interface PivotField {
	
	public enum Type {
		ROW,
		COLUMN,
		DATA;
	}
	
	public enum SortType {
		ASCENDING,
		DESCENDING,
		MANUAL;
	}
	
	public void setItems(List<Object> items);
	
	public List<Item> getItems();
	
	public FieldGroup getFieldGroup();
	
	public void setType(Type type);
	
	public Type getType();
	
	public void setName(String name);
	
	public String getName();
	
	public void setDefaultSubtotal(boolean defaultSubtotal);
	
	public boolean getDefaultSubtotal();
	
	public void setSubtotals(Set<Calculation> subtotals);
	
	public Set<Calculation> getSubtotals();
	
	public void setSortType(SortType type);
	
	public SortType getSortType();
	
	public boolean getDatabaseField();
	
	public void setOutline(boolean outline);
	
	public boolean getOutline();
	
	public interface Item {
		
		public enum Type {
			AVERAGE,
			BLANK,
			COUNT_NUMS,
			COUNT,
			DATA,
			DEFAULT,
			GRAND,
			MAX,
			MIN,
			PRODUCT,
			STD_DEV,
			STD_DEV_P,
			SUM,
			VARIANCE,
			VARIANCE_P;
		}
		
		/**
		 * Sets hide.
		 * 
		 * @param hide
		 */
		public void setHide(boolean hide);
		
		/**
		 * Returns whether is hide or not.
		 * 
		 * @return
		 */
		public boolean getHide();
		
		public Object getValue();
		
		/**
		 * Sets whether show detail or not
		 * 
		 * @param showDetail
		 */
		public void setShowDetail(boolean showDetail);
		
		/**
		 * Returns whether show detail or not
		 * Default true show detail 
		 * 
		 * @return boolean
		 */
		public boolean getShowDetail();
		
		/**
		 * Sets the type of item
		 * 
		 * @param type
		 */
		public void setType(Type type);
		
		/**
		 * Returns the type
		 * 
		 * @return
		 */
		public Type getType();
	}
	
	public interface FieldGroup {
		
		public Item getItem();
		
		public List<Object> getItems();
		
		public Set<Object> getGroup();
		
		public PivotField getBase();
		
		public FieldGroup getParent();
	}
}
