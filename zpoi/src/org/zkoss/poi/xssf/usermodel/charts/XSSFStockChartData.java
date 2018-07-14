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

import java.util.ArrayList;
import java.util.List;

import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStockChart;
import org.zkoss.poi.ss.usermodel.Chart;
import org.zkoss.poi.ss.usermodel.charts.AbstractCategoryDataSerie;
import org.zkoss.poi.ss.usermodel.charts.CategoryData;
import org.zkoss.poi.ss.usermodel.charts.CategoryDataSerie;
import org.zkoss.poi.ss.usermodel.charts.ChartAxis;
import org.zkoss.poi.ss.usermodel.charts.ChartDataSource;
import org.zkoss.poi.ss.usermodel.charts.ChartTextSource;
import org.zkoss.poi.util.Beta;
import org.zkoss.poi.xssf.usermodel.XSSFChart;

/**
 * Represents DrawingML stock chart.
 *
 * @author henrichen@zkoss.org
 */
@Beta
public class XSSFStockChartData implements CategoryData {
	private CTBarChart ctBarChart;
	private CTStockChart ctStockChart;
    /**
     * List of all data series.
     */
    private List<CategoryDataSerie> barSeries;
    private List<CategoryDataSerie> stockSeries;

    public XSSFStockChartData() {
        barSeries = new ArrayList<CategoryDataSerie>();
        stockSeries = new ArrayList<CategoryDataSerie>();
    }

    public XSSFStockChartData(XSSFChart chart) {
    	this();
    	final CTPlotArea plotArea = chart.getCTChart().getPlotArea();
    	
    	//Bar
    	@SuppressWarnings("deprecation")
		final CTBarChart[] barCharts = plotArea.getBarChartArray();
    	if (barCharts != null && barCharts.length > 0) {
    		ctBarChart = barCharts[0];
    	}
    	
    	if (ctBarChart != null) {
    		@SuppressWarnings("deprecation")
			CTBarSer[] bsers = ctBarChart.getSerArray();
    		for (int j = 0; j < bsers.length; ++j) {
    			final CTBarSer ser = bsers[j];
    			CTSerTx serTx = ser.getTx();
    			ChartTextSource title = serTx == null ? null : new XSSFChartTextSource(serTx);
    			ChartDataSource<String> cats = new XSSFChartAxDataSource<String>(ser.getCat());
    			ChartDataSource<Double> vals = new  XSSFChartNumDataSource<Double>(ser.getVal());
		    	addBarSerie(title, cats, vals);
    		}
	    }
    	
    	//Stock
    	@SuppressWarnings("deprecation")
		final CTStockChart[] plotCharts = plotArea.getStockChartArray();
    	if (plotCharts != null && plotCharts.length > 0) {
    		ctStockChart = plotCharts[0];
    	}

    	if (ctStockChart != null) {
    		@SuppressWarnings("deprecation")
			CTLineSer[] bsers = ctStockChart.getSerArray();
    		for (int j = 0; j < bsers.length; ++j) {
    			final CTLineSer ser = bsers[j];
    			ChartTextSource title = new XSSFChartTextSource(ser.getTx());
    			ChartDataSource<String> cats = new XSSFChartAxDataSource<String>(ser.getCat());
    			ChartDataSource<Double> vals = new  XSSFChartNumDataSource<Double>(ser.getVal());
		    	addStockSerie(title, cats, vals);
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

		protected void addToChart(CTBarChart ctBarChart) {
            CTBarSer barSer = ctBarChart.addNewSer();
            barSer.addNewIdx().setVal(this.id);
            barSer.addNewOrder().setVal(this.order);

            if (title != null) {
	            CTSerTx tx = barSer.addNewTx();
	            XSSFChartUtil.buildSerTx(tx, title);
            }
            
            if (categories != null && categories.getPointCount() > 0) {
	            CTAxDataSource cats = barSer.addNewCat();
	            XSSFChartUtil.buildAxDataSource(cats, categories);
            }

            CTNumDataSource vals = barSer.addNewVal();
            XSSFChartUtil.buildNumDataSource(vals, values);
        }
		
		protected void addToChart(CTStockChart ctStockChart) {
            CTLineSer lineSer = ctStockChart.addNewSer();
            lineSer.addNewIdx().setVal(this.id);
            lineSer.addNewOrder().setVal(this.order);

            CTSerTx tx = lineSer.addNewTx();
            XSSFChartUtil.buildSerTx(tx, title);
            
            CTAxDataSource cats = lineSer.addNewCat();
            XSSFChartUtil.buildAxDataSource(cats, categories);

            CTNumDataSource vals = lineSer.addNewVal();
            XSSFChartUtil.buildNumDataSource(vals, values);
        }
    }

     public void fillChart(Chart chart, ChartAxis... axis) {
        if (!(chart instanceof XSSFChart)) {
            throw new IllegalArgumentException("Chart must be instance of XSSFChart");
        }

        if (ctBarChart == null) {
	        XSSFChart xssfChart = (XSSFChart) chart;
	        CTPlotArea plotArea = xssfChart.getCTChart().getPlotArea();
	        ctBarChart = plotArea.addNewBarChart();
        
	        ctBarChart.addNewVaryColors().setVal(true);
	        //TODO setup other properties of barChart
	
	        for (CategoryDataSerie s : barSeries) {
	            ((Serie)s).addToChart(ctBarChart);
	        }
        }

        if (ctStockChart == null) {
	        XSSFChart xssfChart = (XSSFChart) chart;
	        CTPlotArea plotArea = xssfChart.getCTChart().getPlotArea();
	        ctStockChart = plotArea.addNewStockChart();
        
	        //TODO setup other properties of stockChart

	        for (CategoryDataSerie s : stockSeries) {
	            ((Serie)s).addToChart(ctStockChart);
	        }
        }
    }
    
    private CategoryDataSerie addBarSerie(ChartTextSource title, ChartDataSource<?> cats,
            ChartDataSource<? extends Number> vals) {
    	return addSerie0(title, cats, vals, barSeries);
    }
    
    private CategoryDataSerie addStockSerie(ChartTextSource title, ChartDataSource<?> cats,
            ChartDataSource<? extends Number> vals) {
    	return addSerie0(title, cats, vals, stockSeries);
    }
 
    private CategoryDataSerie addSerie0(ChartTextSource title, ChartDataSource<?> cats,
            ChartDataSource<? extends Number> vals, List<CategoryDataSerie> series) {
		if (!vals.isNumeric()) {
			throw new IllegalArgumentException("Bar data source must be numeric.");
		}
		int numOfSeries = series.size();
		Serie newSerie = new Serie(numOfSeries, numOfSeries, title, cats, vals);
		series.add(newSerie);
		return newSerie;
	}
    
    @Override
    public List<? extends CategoryDataSerie> getSeries() {
    	List<CategoryDataSerie> series = new ArrayList<CategoryDataSerie>();
    	series.addAll(barSeries);
    	series.addAll(stockSeries);
        return series;
    }
    
    @Override
    public CategoryDataSerie addSerie(ChartTextSource title, ChartDataSource<?> cats,
            ChartDataSource<? extends Number> vals) {
		if (!vals.isNumeric()) {
			throw new IllegalArgumentException("data source must be numeric.");
		}
		int numOfSeries = barSeries.size() + stockSeries.size();
		Serie newSerie = new Serie(numOfSeries, numOfSeries, title, cats, vals);
		if (numOfSeries > 0) {
			stockSeries.add(newSerie);
		} else {
			barSeries.add(newSerie);
		}
		return newSerie;
    }
}
