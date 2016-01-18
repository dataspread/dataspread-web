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

package org.zkoss.poi.ss.formula.eval;

/**
 * Common base class for implementors of {@link RefEval}
 *
 * @author Josh Micich
 */
public abstract class RefEvalBase implements RefEval {

	private final int _rowIndex;
	private final int _columnIndex;
	private final boolean _rowRel; // whether row is relative
	private final boolean _colRel; // whether column is relative

//	protected RefEvalBase(int rowIndex, int columnIndex) {
//		this(rowIndex, columnIndex, false, false);
//	}
	//ZSS-833
	protected RefEvalBase(int rowIndex, int columnIndex, boolean rowRel, boolean colRel) {
		_rowIndex = rowIndex;
		_columnIndex = columnIndex;
		_rowRel = rowRel;
		_colRel = colRel;
	}
	public final int getRow() {
		return _rowIndex;
	}
	public final int getColumn() {
		return _columnIndex;
	}
	//ZSS-833
	//@since 3.9.6
	public final boolean isRowRelative() {
		return _rowRel;
	}
	//ZSS-833
	//@since 3.9.6
	public final boolean isColRelative() {
		return _colRel;
	}
}
