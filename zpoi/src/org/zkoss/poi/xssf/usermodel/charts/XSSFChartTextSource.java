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

import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;
import org.zkoss.poi.ss.formula.SheetNameFormatter;
import org.zkoss.poi.ss.usermodel.charts.ChartTextSource;

/**
 * @author henrichen@zkoss.org
 *
 */
public class XSSFChartTextSource implements ChartTextSource {
	private final CTSerTx tx;
	
	public XSSFChartTextSource(CTSerTx tx) {
		this.tx = tx;
	}
	
	@Override
	public boolean isReference() {
		return tx == null ? false : tx.isSetStrRef();
	}

	@Override
	public String getTextString() {
		return tx == null ? "" : tx.getV();
	}

	@Override
	public String getFormulaString() {
		return tx == null ? null : tx.getStrRef().getF();
	}

	@Override
	public void renameSheet(String oldname, String newname) {
		if (isReference()) {
			final String o = SheetNameFormatter.format(oldname);
			final String n = SheetNameFormatter.format(newname);
			final String ref = tx.getStrRef().getF();
			
			//20131024, dennischen@zkoss.org, ZSS-473, ZSS-482
			Pattern p = Pattern.compile(o+"!",Pattern.LITERAL);
			final String newref = p.matcher(ref).replaceAll(n+"!");
//			final String newref = ref.replaceAll(o+"!", n+"!");
			if (!newref.equals(ref)) {
				tx.getStrRef().setF(newref);
				//20131024, dennischen@zkoss.org, ZSS-482
				if(tx.getStrRef().getStrCache()!=null){
					tx.getStrRef().unsetStrCache(); //invalidate the cache
				}
			}
		}
	}
}
