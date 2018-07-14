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

import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumData;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrData;
import org.zkoss.poi.ss.formula.SheetNameFormatter;
import org.zkoss.poi.ss.usermodel.charts.ChartDataSource;

/**
 * @author henrichen@zkoss.org
 *
 */
public class XSSFChartAxDataSource<T> implements ChartDataSource<T> {
	private CTAxDataSource cat;
	public XSSFChartAxDataSource(CTAxDataSource cat) {
		this.cat = cat;
	}
	
	@Override
	public int getPointCount() {
		if (cat != null) {
			if (isNumeric()) {
				if (isReference()) {
					return (int) cat.getNumRef().getNumCache().getPtCount().getVal();
				} else {
					return (int) cat.getNumLit().getPtCount().getVal();
				}
			} else {
				if (isReference()) {
					return (int) cat.getStrRef().getStrCache().getPtCount().getVal();
				} else {
					return (int) cat.getStrLit().getPtCount().getVal();
				}
			}
		}
		return 0;
	}

	@Override
	public T getPointAt(int index) {
		if (cat != null) {
			if (isNumeric()) {
				if (isReference()) {
					return (T) Double.valueOf(cat.getNumRef().getNumCache().getPtArray(index).getV());
				} else {
					return (T) Double.valueOf(cat.getNumLit().getPtArray(index).getV());
				}
			} else {
				if (isReference()) {
					return (T) cat.getStrRef().getStrCache().getPtArray(index).getV();
				} else {
					return (T) cat.getStrLit().getPtArray(index).getV();
				}
			}
		}
		return null;
	}

	@Override
	public boolean isReference() {
		return cat != null && (cat.isSetStrRef() || cat.isSetNumRef());
	}

	@Override
	public boolean isNumeric() {
		return cat != null && (cat.isSetNumRef() || cat.isSetNumLit());
	}

	@Override
	public String getFormulaString() {
		return cat != null && isNumeric() ? cat.getNumRef().getF() : cat.getStrRef().getF();
	}

	@Override
	public void renameSheet(String oldname, String newname) {
		if (isReference()) {
			if (isNumeric()) {
				final String o = SheetNameFormatter.format(oldname);
				final String n = SheetNameFormatter.format(newname);
				final String ref = cat.getNumRef().getF();
				//20131024, dennischen@zkoss.org,  ZSS-473, ZSS-482
				Pattern p = Pattern.compile(o+"!",Pattern.LITERAL);
				final String newref = p.matcher(ref).replaceAll(n+"!");
//				final String newref = ref.replaceAll(o+"!", n+"!");
				if (!newref.equals(ref)) {
					cat.getNumRef().setF(newref);
					CTNumData cache = cat.getNumRef().getNumCache();
					if (cache != null) {
						cat.getNumRef().unsetNumCache(); //invalidate the cache 	
					}
				}
			} else {
				final String o = SheetNameFormatter.format(oldname);
				final String n = SheetNameFormatter.format(newname);
				final String ref = cat.getStrRef().getF();
				//20131024, dennischen@zkoss.org, ZSS-473, ZSS-482
				Pattern p = Pattern.compile(o+"!",Pattern.LITERAL);
				final String newref = p.matcher(ref).replaceAll(n+"!");
//				final String newref = ref.replaceAll(o+"!", n+"!");
				if (!newref.equals(ref)) {
					cat.getStrRef().setF(newref);
					CTStrData cache = cat.getStrRef().getStrCache();
					if (cache != null) {
						cat.getStrRef().unsetStrCache(); //invalidate the cache 	
					}
				}
			}
		}
	}
}
