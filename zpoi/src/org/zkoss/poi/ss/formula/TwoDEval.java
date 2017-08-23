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

package org.zkoss.poi.ss.formula;

import org.zkoss.poi.ss.formula.eval.AreaEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;

/**
 * Common interface of {@link AreaEval} and {@link org.zkoss.poi.ss.formula.eval.AreaEvalBase}
 *
 * @author Josh Micich
 */
public interface TwoDEval extends ValueEval {

	/**
	 * @param rowIndex relative row index (zero based)
	 * @param columnIndex relative column index (zero based)
	 * @return element at the specified row and column position
	 */
	ValueEval getValue(int rowIndex, int columnIndex);

	int getWidth();
	int getHeight();

	/**
	 * @return <code>true</code> if the area has just a single row, this also includes
	 * the trivial case when the area has just a single cell.
	 */
	boolean isRow();

	/**
	 * @return <code>true</code> if the area has just a single column, this also includes
	 * the trivial case when the area has just a single cell.
	 */
	boolean isColumn();

	/**
	 * @param rowIndex relative row index (zero based)
	 * @return a single row {@link TwoDEval}
	 */
	TwoDEval getRow(int rowIndex);
	/**
	 * @param columnIndex relative column index (zero based)
	 * @return a single column {@link TwoDEval}
	 */
	TwoDEval getColumn(int columnIndex);

	/**
	 * @param attributeName name of column
	 * @return
	 */
	TwoDEval getColumnByAttribute(String attributeName);


    /**
     * @return true if the  cell at row and col is a subtotal
     */
    boolean isSubTotal(int rowIndex, int columnIndex);

    /**
     * @return true if the  cell at row and col is hidden
     * @since 3.9.7
     */
    //ZSS-962
    boolean isHidden(int rowIndex, int columnIndex);
}
