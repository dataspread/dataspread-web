/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.zkoss.poi.ss.usermodel;

import java.util.List;

import org.zkoss.poi.ss.util.CellRangeAddress;

/**
 * Represents autofiltering for the specified worksheet.
 *
 * <p>
 * Filtering data is a quick and easy way to find and work with a subset of data in a range of cells or table.
 * For example, you can filter to see only the values that you specify, filter to see the top or bottom values,
 * or filter to quickly see duplicate values.
 * </p>
 *
 * TODO YK: For now (Aug 2010) POI only supports setting a basic autofilter on a range of cells.
 * In future, when we support more auto-filter functions like custom criteria, sort, etc. we will add
 * corresponding methods to this interface.
 */
public interface AutoFilter {
    /**
     * Apply a custom filter
     *
     * <p>
     * A custom AutoFilter specifies an operator and a value.
     * There can be at most two customFilters specified, and in that case the parent element
     * specifies whether the two conditions are joined by 'and' or 'or'. For any cells whose
     * values do not meet the specified criteria, the corresponding rows shall be hidden from
     * view when the filter is applied.
     * </p>
     *
     * <p>
     * Example:
     * <blockquote><pre>
     *  AutoFilter filter = sheet.setAutoFilter(CellRangeAddress.valueOf("A1:F200"));
     *  filter.applyFilter(0, FilterOperator.GreaterThanOrEqual", "0.2");
     *  filter.applyFilter(1, FilterOperator.LessThanOrEqual"", "0.5");
     * </pre></blockquote>
     * </p>
     *
     * @param columnIndex 0-based column index
     * @param operator the operator to apply
     * @param criteria top or bottom value used in the filter criteria.
     *
     * TODO YK: think how to combine AutoFilter with with DataValidationConstraint, they are really close relatives
     * void applyFilter(int columnIndex, FilterOperator operator, String criteria);
     */


    /**
     * Apply a filter against a list of values
     *
     * <p>
     * Example:
     * <blockquote><pre>
     *  AutoFilter filter = sheet.setAutoFilter(CellRangeAddress.valueOf("A1:F200"));
     *  filter.applyFilter(0, "apache", "poi", "java", "api");
     * </pre></blockquote>
     * </p>
     *
     * @param columnIndex 0-based column index
     * @param values the filter values
     *
     * void applyFilter(int columnIndex, String ... values);
     */

	//20110510, henrichen@zkoss.org
	//inner filterOp for #filter
	public final static int FILTEROP_AND = 0x01;
	public final static int FILTEROP_BOTTOM10 = 0x02;
	public final static int FILTEROP_BOTOOM10PERCENT = 0x03;
	public final static int FILTEROP_OR = 0x04;
	public final static int FILTEROP_TOP10 = 0x05;
	public final static int FILTEROP_TOP10PERCENT = 0x06;
	public final static int FILTEROP_VALUES = 0x07;

	/**
	 * Returns the filtered Range.
	 */
	CellRangeAddress getRangeAddress();
	
	/**
	 * Return filter setting of each filtered column.
	 */
	List<FilterColumn> getFilterColumns();
	
	/**
	 * Returns the column filter information of the specified column; null if the column is not filtered.
	 * @param col the nth column (1st column in the filter range is 0)
	 * @return the column filter information of the specified column; null if the column is not filtered.
	 */
	FilterColumn getFilterColumn(int col);
}
