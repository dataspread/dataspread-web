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

package org.zkoss.poi.hssf.usermodel;
import java.util.List;
import java.util.Set;

import org.zkoss.poi.ss.usermodel.AutoFilter;
import org.zkoss.poi.ss.usermodel.FilterColumn;
import org.zkoss.poi.ss.util.CellRangeAddress;

/**
 * Represents autofiltering for the specified worksheet.
 *
 * @author Yegor Kozlov
 * @author Peterkuo
 * @author dennischen , remove unnecessary implementation
 */
public final class HSSFAutoFilter implements AutoFilter {
    private HSSFSheet _sheet;
    
    HSSFAutoFilter(HSSFSheet sheet){
        _sheet = sheet;
    }

	public List<String> getValuesOfFilter(int column) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CellRangeAddress getRangeAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<FilterColumn> getFilterColumns() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public FilterColumn getFilterColumn(int colId) {
		return null;
	}
	
	public FilterColumn getOrCreateFilterColumn(int colId) {
		return null;
	}
	
	public class HSSFFilterColumn implements FilterColumn {

		@Override
		public int getColId() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public List<String> getFilters() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set getCriteria1() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set getCriteria2() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isOn() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public int getOperator() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		public void setProperties(Object criteria1, int filterOp, Object criteria2, boolean visibleDropDown) {
			// TODO 
		}
	}
}