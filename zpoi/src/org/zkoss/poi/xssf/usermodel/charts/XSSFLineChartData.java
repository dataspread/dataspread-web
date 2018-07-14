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
import org.zkoss.poi.ss.usermodel.charts.ChartAxis;
import org.zkoss.poi.ss.usermodel.charts.ChartDataSource;
import org.zkoss.poi.ss.usermodel.charts.ChartGrouping;
import org.zkoss.poi.ss.usermodel.charts.ChartTextSource;
import org.zkoss.poi.ss.usermodel.charts.CategoryData;
import org.zkoss.poi.ss.usermodel.charts.CategoryDataSerie;
import org.zkoss.poi.util.Beta;
import org.zkoss.poi.xssf.usermodel.XSSFChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents DrawingML line chart.
 *
 * @author henrichen@zkoss.org
 */
@Beta
public class XSSFLineChartData implements CategoryData {
	private ChartGrouping _chartGrouping; //ZSS-828
	private CTLineChart ctLineChart;
    /**
     * List of all data series.
     */
    private List<CategoryDataSerie> series;

    public XSSFLineChartData() {
        series = new ArrayList<CategoryDataSerie>();
    }

    public XSSFLineChartData(XSSFChart chart) {
    	this();
    	final CTPlotArea plotArea = chart.getCTChart().getPlotArea();
    	
    	//Line
    	@SuppressWarnings("deprecation")
		final CTLineChart[] plotCharts = plotArea.getLineChartArray();
    	if (plotCharts != null && plotCharts.length > 0) {
    		ctLineChart = plotCharts[0];
    	}
    	
    	if (ctLineChart != null) {
    		@SuppressWarnings("deprecation")
			CTLineSer[] bsers = ctLineChart.getSerArray();
    		for (int j = 0; j < bsers.length; ++j) {
    			final CTLineSer ser = bsers[j];
    			CTSerTx serTx = ser.getTx();
    			ChartTextSource title = serTx == null ? null : new XSSFChartTextSource(serTx);
    			ChartDataSource<String> cats = new XSSFChartAxDataSource<String>(ser.getCat());
    			ChartDataSource<Double> vals = new  XSSFChartNumDataSource<Double>(ser.getVal());
		    	addSerie(title, cats, vals);
    		}
	    }
    }

    /**
     * Package private PieChartSerie implementation.
     */
    static class Serie extends AbstractCategoryDataSerie {
        protected Serie(int id, int order, ChartTextSource title,
				ChartDataSource<?> cats, ChartDataSource<? extends Number> vals) {
			super(id, order, title, cats, vals);
		}

		protected void addToChart(CTLineChart ctLineChart) {
            CTLineSer lineSer = ctLineChart.addNewSer();
            lineSer.addNewIdx().setVal(this.id);
            lineSer.addNewOrder().setVal(this.order);
            
            if (title != null) {
	            CTSerTx tx = lineSer.addNewTx();
	            XSSFChartUtil.buildSerTx(tx, title);
            }
            
            if (categories != null && categories.getPointCount() > 0) {
	            CTAxDataSource cats = lineSer.addNewCat();
	            XSSFChartUtil.buildAxDataSource(cats, categories);
            }

            CTNumDataSource vals = lineSer.addNewVal();
            XSSFChartUtil.buildNumDataSource(vals, values);
        }
    }

    public CategoryDataSerie addSerie(ChartTextSource title, ChartDataSource<?> cats,
                                      ChartDataSource<? extends Number> vals) {
        if (!vals.isNumeric()) {
            throw new IllegalArgumentException("Line data source must be numeric.");
        }
        int numOfSeries = series.size();
        Serie newSerie = new Serie(numOfSeries, numOfSeries, title, cats, vals);
        series.add(newSerie);
        return newSerie;
    }

    public void fillChart(Chart chart, ChartAxis... axis) {
        if (!(chart instanceof XSSFChart)) {
            throw new IllegalArgumentException("Chart must be instance of XSSFChart");
        }

        if (ctLineChart == null) {
	        XSSFChart xssfChart = (XSSFChart) chart;
	        CTPlotArea plotArea = xssfChart.getCTChart().getPlotArea();
	        ctLineChart = plotArea.addNewLineChart();
        
	        ctLineChart.addNewVaryColors().setVal(true);
	        //TODO setup other properties of lineChart
	
	        for (CategoryDataSerie s : series) {
	            ((Serie)s).addToChart(ctLineChart);
	        }
        }
        
    	// ZSS-358: chart element should also link to axis through ID
    	// otherwise, Excel will fail to load this XLSX file
        for(ChartAxis a : axis) {
        	ctLineChart.addNewAxId().setVal(a.getId());
        }
    }

    public List<? extends CategoryDataSerie> getSeries() {
        return series;
    }

    //ZSS-828
    public ChartGrouping getGrouping() {
    	if (ctLineChart != null) {
    		_chartGrouping = XSSFChartUtil.toChartGrouping(ctLineChart.getGrouping()); 
    	}
    	return _chartGrouping;
    }
    
    //ZSS-828
    public void setGrouping(ChartGrouping grouping) {
    	_chartGrouping = grouping;
    	if (ctLineChart != null) {
    		CTGrouping ctgr = ctLineChart.getGrouping();
    		if (ctgr == null) {
    			ctgr = ctLineChart.addNewGrouping();
    		}
    		ctgr.setVal(XSSFChartUtil.fromChartGrouping(grouping));
    	}
    }

}
