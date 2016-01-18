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

import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDepthPercent;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTHPercent;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPerspective;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTRotX;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTRotY;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTView3D;
import org.zkoss.poi.ss.usermodel.charts.View3D;
import org.zkoss.poi.util.Beta;
import org.zkoss.poi.xssf.usermodel.XSSFChart;

/**
 * Represents a SpreadsheetML chart 3D view information.
 * 
 * @author henrichen@zkoss.org
 */
@Beta
public class XSSFView3D implements View3D {
	/**
	 * Underlying CTView3D bean
	 */
	private CTView3D view3d;

	/**
	 * Create a new SpreadsheetML chart legend
	 */
	public XSSFView3D(XSSFChart chart) {
		CTChart ctChart = chart.getCTChart();
		this.view3d = (ctChart.isSetView3D()) ?
			ctChart.getView3D() :
			ctChart.addNewView3D();
	}
	
	//ZSS-830
	//-90 ~ 90; default: 0
	public int getRotX() {
		return view3d.isSetRotX() ? view3d.getRotX().getVal() : 0; 
	}
	public void setRotX(int rotX) {
		CTRotX ctRotX = !view3d.isSetRotX() ?
			view3d.addNewRotX() : view3d.getRotX();
		ctRotX.setVal((byte)rotX);
	}
	
	// 0 ~ 360; default: 0
	public int getRotY() {
		return view3d.isSetRotY() ? ((int)view3d.getRotY().getVal()) & 0xff : 0; 
	}
	public void setRotY(int rotY) {
		CTRotY ctRotY = !view3d.isSetRotY() ?
				view3d.addNewRotY() : view3d.getRotY();
		ctRotY.setVal((byte)rotY);

	}
	
	// 0 ~ 240; default: 30
	public int getPerspective() {
		return view3d.isSetPerspective() ? 
				((int)view3d.getPerspective().getVal()) & 0xffff : 30;
	}
	public void setPerspective(int perspective) {
		CTPerspective pers = !view3d.isSetPerspective() ?
				view3d.addNewPerspective() : view3d.getPerspective();
		pers.setVal((short) perspective);
	}
	
	// 5 ~ 500; default: 100
	public int getHPercent() {
		return view3d.isSetHPercent() ? view3d.getHPercent().getVal() : 100;
	}
	public void setHPercent(int percent) {
		CTHPercent perc = !view3d.isSetHPercent() ?
				view3d.addNewHPercent() : view3d.getHPercent();
		perc.setVal(percent);
	}
	
	// 20 ~ 2000; default: 100
	public int getDepthPercent() {
		return view3d.isSetDepthPercent() ? 
				view3d.getDepthPercent().getVal() : 100;
	}
	public void setDepthPercent(int percent) {
		CTDepthPercent dper = !view3d.isSetDepthPercent() ?
				view3d.addNewDepthPercent() : view3d.getDepthPercent();
		dper.setVal(percent);
	}
	
	// default: true
	public boolean isRightAngleAxes() {
		return view3d.isSetRAngAx() ? view3d.getRAngAx().getVal() : true; 
	}
	public void setRightAngleAxes(boolean b) {
		CTBoolean rAng = !view3d.isSetRAngAx() ? 
				view3d.addNewRAngAx() : view3d.getRAngAx();
		rAng.setVal(b);
	}
}
