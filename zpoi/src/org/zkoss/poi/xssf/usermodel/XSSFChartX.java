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
package org.zkoss.poi.xssf.usermodel;

import org.zkoss.poi.POIXMLDocumentPart;
import org.zkoss.poi.ss.usermodel.Chart;
import org.zkoss.poi.ss.usermodel.ChartInfo;
import org.zkoss.poi.ss.usermodel.ClientAnchor;
import org.zkoss.poi.ss.usermodel.ZssChartX;
/**
 * 
 * @author henrichen
 *
 */
public class XSSFChartX implements ZssChartX {
	private XSSFDrawing _patriarch;
	private String _name;
	private XSSFChart _chart;
	private String _chartId;

	public XSSFChartX(XSSFDrawing patriarch, XSSFClientAnchor anchor, String name, String chartId) {
		_patriarch = patriarch;
		_chart = getXSSFChart1(chartId);//getXSSFChart0(chartId);
		_name = name;
		_chartId = chartId;
		_chart.setClientAnchor(anchor);
	}

	@Override
	public ClientAnchor getPreferredSize() {
		return _chart.getPreferredSize();
	}
	
	private XSSFChart getXSSFChart1(String chartId) {
        for(POIXMLDocumentPart part : _patriarch.getRelations()){
	        if(part instanceof XSSFChart && part.getPackageRelationship().getId().equals(chartId)){
	            return (XSSFChart)part;
	        }
        }
        return null;
	}
	
	public ChartInfo getChartInfo() {
        return null;
	}
	
	public Chart getChart() {
		return _chart;
	}
	
	public String getName() {
		return _name;
	}

	public void setName(String name) {
		this._name = name;
	}
	
	public void renameSheet(String oldname, String newname) {
		renameSheet1(oldname, newname);
	}
	
	private void renameSheet1(String oldname, String newname) {
		_chart.renameSheet(oldname, newname);
	}

	public String getChartId() {
		return _chartId;
	}

    @Override
	public void setClientAnchor(ClientAnchor newanchor) {
    	_chart.setClientAnchor(newanchor);
    }
}
