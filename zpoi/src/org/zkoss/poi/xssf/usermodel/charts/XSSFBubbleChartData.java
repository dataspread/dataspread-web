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

import org.zkoss.poi.ss.usermodel.Chart;
import org.zkoss.poi.ss.usermodel.charts.AbstractCategoryDataSerie;
import org.zkoss.poi.ss.usermodel.charts.AbstractXYDataSerie;
import org.zkoss.poi.ss.usermodel.charts.AbstractXYZDataSerie;
import org.zkoss.poi.ss.usermodel.charts.ChartAxis;
import org.zkoss.poi.ss.usermodel.charts.ChartDataSource;
import org.zkoss.poi.ss.usermodel.charts.ChartTextSource;
import org.zkoss.poi.ss.usermodel.charts.CategoryData;
import org.zkoss.poi.ss.usermodel.charts.CategoryDataSerie;
import org.zkoss.poi.ss.usermodel.charts.XYData;
import org.zkoss.poi.ss.usermodel.charts.XYDataSerie;
import org.zkoss.poi.ss.usermodel.charts.XYZData;
import org.zkoss.poi.ss.usermodel.charts.XYZDataSerie;
import org.zkoss.poi.util.Beta;
import org.zkoss.poi.xssf.usermodel.XSSFChart;
import org.zkoss.poi.xssf.usermodel.charts.XSSFScatChartData.Serie;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents DrawingML bubble chart.
 *
 * @author henrichen@zkoss.org
 */
@Beta
public class XSSFBubbleChartData implements XYZData {

	private CTBubbleChart ctBubbleChart;
    /**
     * List of all data series.
     */
    private List<XYZDataSerie> series;

    public XSSFBubbleChartData() {
        series = new ArrayList<XYZDataSerie>();
    }

    public XSSFBubbleChartData(XSSFChart chart) {
    	this();
    	final CTPlotArea plotArea = chart.getCTChart().getPlotArea();
    	
    	//Bubble
    	@SuppressWarnings("deprecation")
		final CTBubbleChart[] plotCharts = plotArea.getBubbleChartArray();
    	if (plotCharts != null && plotCharts.length > 0) {
    		ctBubbleChart = plotCharts[0];
    	}
    	
    	if (ctBubbleChart != null) {
    		@SuppressWarnings("deprecation")
			CTBubbleSer[] sers = ctBubbleChart.getSerArray();
    		for (int j = 0; j < sers.length; ++j) {
    			final CTBubbleSer ser = sers[j];
    			CTSerTx serTx = ser.getTx();
    			ChartTextSource title = serTx == null ? null : new XSSFChartTextSource(serTx);
    			ChartDataSource<Double> xs = new  XSSFChartAxDataSource<Double>(ser.getXVal());
    			ChartDataSource<Double> ys = new  XSSFChartNumDataSource<Double>(ser.getYVal());
    			ChartDataSource<Double> zs = new  XSSFChartNumDataSource<Double>(ser.getBubbleSize());
		    	addSerie(title, xs, ys, zs);
    		}
	    }
    }

    /**
     * Package private PieChartSerie implementation.
     */
    static class Serie extends AbstractXYZDataSerie {
        protected Serie(int id, int order, ChartTextSource title,
				ChartDataSource<? extends Number> xs, 
				ChartDataSource<? extends Number> ys,
				ChartDataSource<? extends Number> zs) {
			super(id, order, title, xs, ys, zs);
		}

		protected void addToChart(CTBubbleChart ctBubbleChart) {
            CTBubbleSer bubbleSer = ctBubbleChart.addNewSer();
            bubbleSer.addNewIdx().setVal(this.id);
            bubbleSer.addNewOrder().setVal(this.order);

            if (title != null) {
	            CTSerTx tx = bubbleSer.addNewTx();
	            XSSFChartUtil.buildSerTx(tx, title);
            }
            
            CTAxDataSource xs = bubbleSer.addNewXVal();
            XSSFChartUtil.buildAxDataSource(xs, this.xs);

            CTNumDataSource ys = bubbleSer.addNewYVal();
            XSSFChartUtil.buildNumDataSource(ys, this.ys);

            CTNumDataSource zs = bubbleSer.addNewBubbleSize();
            XSSFChartUtil.buildNumDataSource(zs, this.zs);
		}
    }

    public void fillChart(Chart chart, ChartAxis... axis) {
        if (!(chart instanceof XSSFChart)) {
            throw new IllegalArgumentException("Chart must be instance of XSSFChart");
        }

        if (ctBubbleChart == null) {
	        XSSFChart xssfChart = (XSSFChart) chart;
	        CTPlotArea plotArea = xssfChart.getCTChart().getPlotArea();
	        ctBubbleChart = plotArea.addNewBubbleChart();
        
	        ctBubbleChart.addNewVaryColors().setVal(true);
	        //TODO setup other properties of bubbleChart
	
	        for (XYZDataSerie s : series) {
	            ((Serie)s).addToChart(ctBubbleChart);
	        }
        }
        
        for(ChartAxis a : axis) {
        	ctBubbleChart.addNewAxId().setVal(a.getId());
        }
    }
    public List<? extends XYZDataSerie> getSeries() {
        return series;
    }

	@Override
	public XYZDataSerie addSerie(ChartTextSource title,
			ChartDataSource<? extends Number> xs,
			ChartDataSource<? extends Number> ys,
			ChartDataSource<? extends Number> zs) {
		if (!xs.isNumeric()) {
			// ZSS-257: convert to default number as Excel does
			xs = XSSFChartUtil.buildDefaultNumDataSource(xs);
		}
		if (!ys.isNumeric()) {
			throw new IllegalArgumentException("Y Axis data source must be numeric.");
		}
		if (!zs.isNumeric()) {
			throw new IllegalArgumentException("Bubble size data source must be numeric.");
		}
		int numOfSeries = series.size();
		Serie newSerie = new Serie(numOfSeries, numOfSeries, title, xs, ys, zs);
		series.add(newSerie);
		return newSerie;
	}
}
