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

package org.zkoss.poi.xssf.usermodel;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAutoFilter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFilter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFilterColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFilters;
import org.zkoss.poi.POIXMLDocumentPart;
import org.zkoss.poi.openxml4j.opc.PackagePart;
import org.zkoss.poi.openxml4j.opc.PackageRelationship;
import org.zkoss.poi.ss.usermodel.AutoFilter;
import org.zkoss.poi.ss.usermodel.FilterColumn;
import org.zkoss.poi.ss.util.CellRangeAddress;
import org.zkoss.poi.xssf.usermodel.XSSFSheet;

/**
 * Represents autofiltering for the specified worksheet.
 *
 * @author Yegor Kozlov
 * @author peterkuo@zkoss.org
 * @author henrichen@zkoss.org
 */
public final class XSSFAutoFilter extends POIXMLDocumentPart implements AutoFilter {

    XSSFAutoFilter(XSSFSheet sheet){
        this((CTAutoFilter)null); //ZSS-1019
    }

	private CTAutoFilter _autofilter;
	private List<FilterColumn> filterColumns;

	@Deprecated
	public XSSFAutoFilter(XSSFSheet sheet, CTAutoFilter ctaf) {
		this(ctaf);
	}

	//ZSS-1019
	public XSSFAutoFilter(CTAutoFilter ctaf) {
		super();
		_autofilter = ctaf != null ? ctaf : CTAutoFilter.Factory.newInstance();
		fillInAutoFilter();
	}

    //20110930, henrichen@zkoss.org
    /**
     * Parse from CTAutoFilter to fill the user model autoFilter
     * @param af
     */
    private void fillInAutoFilter() {
		CTFilterColumn[] fcList = _autofilter.getFilterColumnArray();
		if(fcList == null || fcList.length == 0)
			return;
		
		for(CTFilterColumn fc: fcList){
			this.addFilterColumn(fc);
		}
	}

	public class XSSFFilterColumn implements org.zkoss.poi.ss.usermodel.FilterColumn {
		private CTFilterColumn _ctfc;

		private Set _criteria1;
		private Set _criteria2;
		private int _operator;

		private Set getCriteriaSet(Object criteria) {
			final Set set = new HashSet();
			if (criteria instanceof String[]) {
				String[] strings = (String[]) criteria;
				for(int j = 0; j < strings.length; ++j) {
					set.add(strings[j]);
				}
			}
			return set;
		}
		
		/*package*/ void init() {
			List<String> filters = getFilters();
			if (filters != null) {
				_criteria1 = getCriteriaSet(filters.toArray(new String[filters.size()]));
				if (isShowBlank()) {
					_criteria1.add("=");
				}
				_operator = AutoFilter.FILTEROP_VALUES;
			}
		}
		
		public void setProperties(Object criteria1, int filterOp, Object criteria2, Boolean visibleDropDown) {
			_operator = filterOp;
			_criteria1 = getCriteriaSet(criteria1);
			_criteria2 = getCriteriaSet(criteria2);
			boolean blank1 = _criteria1.contains("=");
			
			if (visibleDropDown != null) {
				if (visibleDropDown.booleanValue()) {
					if (_ctfc.isSetHiddenButton()) { //ZSS-1019
						_ctfc.unsetHiddenButton();
					}
				} else {
					_ctfc.setHiddenButton(true); //ZSS-1019
				}
			}
			
			if (criteria1 == null) { //remove filtering
				if (_ctfc.isSetFilters()) {
					_ctfc.unsetFilters();
				}
				return;
			}
			
			//TODO, more filtering operation
			switch(_operator) {
			case FILTEROP_VALUES:
				final String[] filters = (String[]) criteria1;
				//remove old
				if (_ctfc.isSetFilters()) {
					_ctfc.unsetFilters();
				}
				final CTFilters cflts = _ctfc.addNewFilters();
				if (blank1) {
					cflts.setBlank(blank1);
				}
				for(int j = 0; j < filters.length; ++j) {
					final CTFilter cflt = cflts.addNewFilter();
					cflt.setVal(filters[j]);
				}
			}
		}
		
		@Override
		public int getColId() {
			return (int)_ctfc.getColId();
		}
		
		@Override
		public List<String> getFilters() {
			final CTFilters fts = _ctfc.getFilters();
			if (fts != null) {
				final CTFilter[] ftary = fts.getFilterArray();
				final List<String> result = new ArrayList<String>(ftary.length);
				for(int j = 0; j < ftary.length; ++j) {
					result.add(ftary[j].getVal());
				}
				return result;
			}
			return null;
		}
		
		private boolean isShowBlank() {
			final CTFilters fts = _ctfc.getFilters();
			return fts == null || (fts.isSetBlank() && fts.getBlank());
		}

		private XSSFFilterColumn(CTFilterColumn ctfc) {
			_ctfc = ctfc;
		}

		@Override
		public Set getCriteria1() {
			return _criteria1;
		}

		@Override
		public Set getCriteria2() {
			return _criteria2;
		}

		@Override
		public boolean isOn() {
			return !_ctfc.isSetHiddenButton() || !_ctfc.getHiddenButton(); //ZSS-1019
		}

		@Override
		public int getOperator() {
			return _operator;
		}
	}
	
	public FilterColumn getOrCreateFilterColumn(int colId) {
		//check if in range
		final CellRangeAddress rng = getRangeAddress();
		final int sz = rng.getLastColumn() - rng.getFirstColumn() + 1;
		if (colId >= sz) {
			throw new RuntimeException("Column not in filter range: "+colId);
		}

		FilterColumn fc = null;
		int j = 0;
		if (filterColumns != null) {
			for (FilterColumn fc0 : filterColumns) {
				final int fc0Id = fc0.getColId(); 
				if (fc0Id == colId) { //found
					fc = fc0;
					break;
				} 
				//20131021, hawkchen@potix.com, ZSS-471, filter columns are not sorted by id so we should search all
//				else if (fc0Id > colId) { //pass over
//					break;
//				}
				
				++j;
			}
		}
		
		if (fc == null) {
			final CTFilterColumn ctFC = _autofilter.insertNewFilterColumn(j);
			ctFC.setColId(colId);
			fc = addFilterColumn(ctFC);
		}
		return fc;
	}
	
	/*package*/ XSSFFilterColumn addFilterColumn(CTFilterColumn ctFC){
		if(filterColumns == null)
			filterColumns = new ArrayList<FilterColumn>();

		XSSFFilterColumn fc = new XSSFFilterColumn(ctFC);
		
		filterColumns.add(fc);
		fc.init(); //ZSS-1019
		return fc;
	}
	
    public XSSFAutoFilter(PackagePart part, PackageRelationship rel) throws IOException {
        super(part, rel);
        readFrom(part.getInputStream());
    }
	
    public void readFrom(InputStream is) throws IOException {
        try {
        	_autofilter = CTAutoFilter.Factory.parse(is);
        } catch (XmlException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }

    public void writeTo(OutputStream out) throws IOException {
    	_autofilter.save(out, DEFAULT_XML_OPTIONS);
    }

    @Override
    protected void commit() throws IOException {
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        writeTo(out);
        out.close();
    }

	@Override
	public CellRangeAddress getRangeAddress() {
		final String ref = _autofilter.getRef();
		return CellRangeAddress.valueOf(ref);
	}

	@Override
	public List<org.zkoss.poi.ss.usermodel.FilterColumn> getFilterColumns() {
		return filterColumns;
	}
	
	@Override
	public FilterColumn getFilterColumn(int col) {
		if (filterColumns != null) {
			for (FilterColumn fc : filterColumns) {
				if (fc.getColId() == col) {
					return fc;
				}
			}
		}
		return null;
	}
	
	//ZSS-855
	public void setRef(String ref) {
		_autofilter.setRef(ref);
	}
}