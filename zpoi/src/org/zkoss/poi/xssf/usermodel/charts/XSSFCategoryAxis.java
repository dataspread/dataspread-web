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

import org.zkoss.poi.ss.usermodel.charts.AxisLabelAlign;
import org.zkoss.poi.ss.usermodel.charts.AxisTickLabelPosition;
import org.zkoss.poi.ss.usermodel.charts.CategoryAxis;
import org.zkoss.poi.ss.usermodel.charts.ChartAxis;
import org.zkoss.poi.ss.usermodel.charts.ValueAxis;
import org.zkoss.poi.ss.usermodel.charts.AxisPosition;
import org.zkoss.poi.ss.usermodel.charts.AxisOrientation;
import org.zkoss.poi.ss.usermodel.charts.AxisCrossBetween;
import org.zkoss.poi.ss.usermodel.charts.AxisCrosses;

import org.zkoss.poi.util.Beta;
import org.zkoss.poi.xssf.usermodel.XSSFChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCatAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLblAlgn;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTickLblPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTValAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumFmt;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCrosses;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScaling;
import org.openxmlformats.schemas.drawingml.x2006.chart.STAxPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.STCrossBetween;
import org.openxmlformats.schemas.drawingml.x2006.chart.STCrosses;
import org.openxmlformats.schemas.drawingml.x2006.chart.STLblAlgn;
import org.openxmlformats.schemas.drawingml.x2006.chart.STTickLblPos;

/**
 * Value axis type.
 *
 * @author Roman Kashitsyn
 */
@Beta
public class XSSFCategoryAxis extends XSSFChartAxis implements CategoryAxis {

	private CTCatAx ctCatAx;

	public XSSFCategoryAxis(XSSFChart chart, long id, AxisPosition pos) {
		super(chart);
		createAxis(id, pos);
	}

	public XSSFCategoryAxis(XSSFChart chart, CTCatAx ctCatAx) {
		super(chart);
		this.ctCatAx = ctCatAx;
	}

	public long getId() {
		return ctCatAx.getAxId().getVal();
	}

	@Override
	protected CTAxPos getCTAxPos() {
		return ctCatAx.getAxPos();
	}

	@Override
	protected CTNumFmt getCTNumFmt() {
		if (ctCatAx.isSetNumFmt()) {
			return ctCatAx.getNumFmt();
		}
		return ctCatAx.addNewNumFmt();
	}

	@Override
	protected CTScaling getCTScaling() {
		return ctCatAx.getScaling();
	}

	@Override
	protected CTCrosses getCTCrosses() {
		return ctCatAx.getCrosses();
	}

	public void crossAxis(ChartAxis axis) {
		ctCatAx.getCrossAx().setVal(axis.getId());
	}

	private void createAxis(long id, AxisPosition pos) {
		ctCatAx = chart.getCTChart().getPlotArea().addNewCatAx();
		ctCatAx.addNewAxId().setVal(id);
		ctCatAx.addNewAxPos();
		ctCatAx.addNewScaling();
		ctCatAx.addNewCrosses();
		ctCatAx.addNewCrossAx();
		ctCatAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO);

		setPosition(pos);
		setOrientation(AxisOrientation.MIN_MAX);
		setCrosses(AxisCrosses.AUTO_ZERO);
	}

	//20111012, henrichen@zkoss.org: handle TickLblPos
	@Override
	protected CTTickLblPos getTickLblPos() {
		return ctCatAx.getTickLblPos();
	}

	//20111024, henrichen@zkoss.org: handle LabelAlign
	@Override
	public AxisLabelAlign getLabelAlign() {
		return toLabelAlign(ctCatAx.getLblAlgn());
	}

	//20111024, henrichen@zkoss.org: handle LabelAlign
	@Override
	public void setLabelAlign(AxisLabelAlign labelAlign) {
		ctCatAx.getLblAlgn().setVal(fromLabelAlign(labelAlign));
	}

	//20111024, henrichen@zkoss.org: handle LabelOffset
	@Override
	public int getLabelOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	//20111024, henrichen@zkoss.org: handle LabelOffset
	@Override
	public void setLabelOffset(int offset) {
		// TODO Auto-generated method stub
		
	}
	
	private static STLblAlgn.Enum fromLabelAlign(AxisLabelAlign lblAlign) {
		switch (lblAlign) {
			case LEFT: return STLblAlgn.L; //"l"
			case CENTER: return STLblAlgn.CTR; //"ctr"
			case RIGHT: return STLblAlgn.R; //"r"
			default:
				throw new IllegalArgumentException();
		}
	}

	private static AxisLabelAlign toLabelAlign(CTLblAlgn ctLblAlgn) {
		switch (ctLblAlgn.getVal().intValue()) {
			case STLblAlgn.INT_CTR: return AxisLabelAlign.CENTER;
			case STLblAlgn.INT_L: return AxisLabelAlign.LEFT;
			case STLblAlgn.INT_R: return AxisLabelAlign.RIGHT;
			default:
				throw new IllegalArgumentException();
		}
	}
}
