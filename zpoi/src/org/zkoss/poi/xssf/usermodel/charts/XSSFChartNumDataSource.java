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
package org.zkoss.poi.xssf.usermodel.charts;

import java.util.regex.Pattern;

import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.zkoss.poi.ss.formula.SheetNameFormatter;
import org.zkoss.poi.ss.usermodel.charts.ChartDataSource;

/**
 * @author henrichen@zkoss.org
 *
 */
public class XSSFChartNumDataSource<T> implements ChartDataSource<T> {
	private CTNumDataSource val;
	public XSSFChartNumDataSource(CTNumDataSource vat) {
		this.val = vat;
	}
	
	@Override
	public int getPointCount() {
		if (val != null) {
			if (isReference()) {
				return (int) val.getNumRef().getNumCache().getPtCount().getVal();
			} else {
				return (int) val.getNumLit().getPtCount().getVal();
			}
		}
		return 0;
	}

	@Override
	public T getPointAt(int index) {
		if (val != null) {
			if (isReference()) {
				return (T) Double.valueOf(val.getNumRef().getNumCache().getPtArray(index).getV());
			} else {
				return (T) Double.valueOf(val.getNumLit().getPtArray(index).getV());
			}
		}
		return null;
	}

	@Override
	public boolean isReference() {
		return val != null && val.isSetNumRef();
	}

	@Override
	public boolean isNumeric() {
		return val != null && (val.isSetNumRef() || val.isSetNumLit());
	}

	@Override
	public String getFormulaString() {
		return val != null ? val.getNumRef().getF() : null;
	}

	@Override
	public void renameSheet(String oldname, String newname) {
		if (isReference()) {
			final String o = SheetNameFormatter.format(oldname);
			final String n = SheetNameFormatter.format(newname);
			final String ref = val.getNumRef().getF();
			//20131024, dennischen@zkoss.org,  ZSS-473, ZSS-482
			Pattern p = Pattern.compile(o+"!",Pattern.LITERAL);
			final String newref = p.matcher(ref).replaceAll(n+"!");
//			final String newref = ref.replaceAll(o+"!", n+"!");
			if (!newref.equals(ref)) {
				val.getNumRef().setF(newref);
				CTNumData cache = val.getNumRef().getNumCache();
				if (cache != null) {
					val.getNumRef().unsetNumCache(); //invalidate the cache 
				}
			}
		}
	}
}
