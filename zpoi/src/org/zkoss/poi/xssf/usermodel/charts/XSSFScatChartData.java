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
import org.zkoss.poi.ss.usermodel.charts.ChartAxis;
import org.zkoss.poi.ss.usermodel.charts.ChartDataSource;
import org.zkoss.poi.ss.usermodel.charts.ChartTextSource;
import org.zkoss.poi.ss.usermodel.charts.CategoryData;
import org.zkoss.poi.ss.usermodel.charts.CategoryDataSerie;
import org.zkoss.poi.ss.usermodel.charts.XYData;
import org.zkoss.poi.ss.usermodel.charts.XYDataSerie;
import org.zkoss.poi.util.Beta;
import org.zkoss.poi.xssf.usermodel.XSSFChart;
import org.zkoss.poi.xssf.usermodel.charts.XSSFBarChartData.Serie;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents DrawingML line chart.
 *
 * @author henrichen@zkoss.org
 */
@Beta
public class XSSFScatChartData implements XYData {

	private CTScatterChart ctScatterChart;
    /**
     * List of all data series.
     */
    private List<XYDataSerie> series;

    public XSSFScatChartData() {
        series = new ArrayList<XYDataSerie>();
    }

    public XSSFScatChartData(XSSFChart chart) {
    	this();
    	final CTPlotArea plotArea = chart.getCTChart().getPlotArea();
    	
    	//Line
    	@SuppressWarnings("deprecation")
		final CTScatterChart[] plotCharts = plotArea.getScatterChartArray();
    	if (plotCharts != null && plotCharts.length > 0) {
    		ctScatterChart = plotCharts[0];
    	}
    	
    	if (ctScatterChart != null) {
    		@SuppressWarnings("deprecation")
			CTScatterSer[] sers = ctScatterChart.getSerArray();
    		for (int j = 0; j < sers.length; ++j) {
    			final CTScatterSer ser = sers[j];
    			CTSerTx serTx = ser.getTx();
    			ChartTextSource title = serTx == null ? null : new XSSFChartTextSource(serTx);
    			ChartDataSource<Double> xs = new  XSSFChartAxDataSource<Double>(ser.getXVal());
    			ChartDataSource<Double> ys = new  XSSFChartNumDataSource<Double>(ser.getYVal());
		    	addSerie(title, xs, ys);
    		}
	    }
    }

    /**
     * Package private PieChartSerie implementation.
     */
    static class Serie extends AbstractXYDataSerie {
        protected Serie(int id, int order, ChartTextSource title,
				ChartDataSource<? extends Number> xs, ChartDataSource<? extends Number> ys) {
			super(id, order, title, xs, ys);
		}

		protected void addToChart(CTScatterChart ctScatterChart) {
            CTScatterSer scatterSer = ctScatterChart.addNewSer();
            scatterSer.addNewIdx().setVal(this.id);
            scatterSer.addNewOrder().setVal(this.order);

            if (title != null) {
	            CTSerTx tx = scatterSer.addNewTx();
	            XSSFChartUtil.buildSerTx(tx, title);
            }
            
            CTAxDataSource cats = scatterSer.addNewXVal();
            XSSFChartUtil.buildAxDataSource(cats, this.xs);

            CTNumDataSource vals = scatterSer.addNewYVal();
            XSSFChartUtil.buildNumDataSource(vals, this.ys);
        }
    }

    public void fillChart(Chart chart, ChartAxis... axis) {
        if (!(chart instanceof XSSFChart)) {
            throw new IllegalArgumentException("Chart must be instance of XSSFChart");
        }

        if (ctScatterChart == null) {
	        XSSFChart xssfChart = (XSSFChart) chart;
	        CTPlotArea plotArea = xssfChart.getCTChart().getPlotArea();
	        ctScatterChart = plotArea.addNewScatterChart();
        
	        ctScatterChart.addNewVaryColors().setVal(true);
	        //TODO setup other properties of scatterChart
	
	        for (XYDataSerie s : series) {
	            ((Serie)s).addToChart(ctScatterChart);
	        }
        }
        
		// ZSS-358: chart element should also link to axis through ID
		// otherwise, Excel will fail to load this XLSX file
		for(ChartAxis a : axis) {
			ctScatterChart.addNewAxId().setVal(a.getId());
		}
    }

    public List<? extends XYDataSerie> getSeries() {
        return series;
    }

	@Override
	public XYDataSerie addSerie(ChartTextSource title,
			ChartDataSource<? extends Number> xs,
			ChartDataSource<? extends Number> ys) {
		if (!xs.isNumeric()) {
			xs = XSSFChartUtil.buildDefaultNumDataSource(xs);
		}
		if (!ys.isNumeric()) {
			throw new IllegalArgumentException("Y Axis data source must be numeric.");
		}
		int numOfSeries = series.size();
		Serie newSerie = new Serie(numOfSeries, numOfSeries, title, xs, ys);
		series.add(newSerie);
		return newSerie;
	}
}
