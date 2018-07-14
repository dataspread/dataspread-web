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
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBar3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarDir;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarGrouping;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;
import org.zkoss.poi.ss.usermodel.Chart;
import org.zkoss.poi.ss.usermodel.charts.AbstractCategoryDataSerie;
import org.zkoss.poi.ss.usermodel.charts.CategoryData;
import org.zkoss.poi.ss.usermodel.charts.CategoryDataSerie;
import org.zkoss.poi.ss.usermodel.charts.ChartAxis;
import org.zkoss.poi.ss.usermodel.charts.ChartDataSource;
import org.zkoss.poi.ss.usermodel.charts.ChartDirection;
import org.zkoss.poi.ss.usermodel.charts.ChartGrouping;
import org.zkoss.poi.ss.usermodel.charts.ChartTextSource;
import org.zkoss.poi.util.Beta;
import org.zkoss.poi.xssf.usermodel.XSSFChart;

/**
 * Represents DrawingML bar 3D chart.
 *
 * @author henrichen@zkoss.org
 */
@Beta
public class XSSFBar3DChartData implements CategoryData {
	private ChartGrouping _chartGrouping;
	private ChartDirection _chartDirection;
	private CTBar3DChart ctBar3DChart;
    /**
     * List of all data series.
     */
    private List<CategoryDataSerie> series;

    public XSSFBar3DChartData() {
        series = new ArrayList<CategoryDataSerie>();
        setBarDirection(ChartDirection.HORIZONTAL);
    }

    public XSSFBar3DChartData(XSSFChart chart) {
    	this();
    	final CTPlotArea plotArea = chart.getCTChart().getPlotArea();
    	
    	//Bar3D
    	@SuppressWarnings("deprecation")
		final CTBar3DChart[] plotCharts = plotArea.getBar3DChartArray();
    	if (plotCharts != null && plotCharts.length > 0) {
    		ctBar3DChart = plotCharts[0];
    	}
    	
    	if (ctBar3DChart != null) {
    		@SuppressWarnings("deprecation")
			CTBarSer[] bsers = ctBar3DChart.getSerArray();
    		for (int j = 0; j < bsers.length; ++j) {
    			final CTBarSer ser = bsers[j];
    			CTSerTx serTx = ser.getTx();
    			ChartTextSource title = serTx == null ? null : new XSSFChartTextSource(serTx);
    			ChartDataSource<String> cats = new XSSFChartAxDataSource<String>(ser.getCat());
    			ChartDataSource<Double> vals = new  XSSFChartNumDataSource<Double>(ser.getVal());
		    	addSerie(title, cats, vals);
    		}
	    }
    }
    
    public ChartGrouping getGrouping() {
    	if (ctBar3DChart != null) {
    		_chartGrouping = XSSFChartUtil.toChartGroupingForBar(ctBar3DChart.getGrouping()); 
    	}
    	return _chartGrouping;
    }
    
    public void setGrouping(ChartGrouping grouping) {
    	_chartGrouping = grouping;
    	if (ctBar3DChart != null) {
    		CTBarGrouping ctgr = ctBar3DChart.getGrouping();
    		if (ctgr == null) {
    			ctgr = ctBar3DChart.addNewGrouping();
    		}
    		ctgr.setVal(XSSFChartUtil.fromChartGroupingForBar(grouping));
    	}
    }

    public ChartDirection getBarDirection() {
    	if (ctBar3DChart != null) { 
    		_chartDirection = XSSFChartUtil.toBarDirection(ctBar3DChart.getBarDir());
    	}
    	return _chartDirection;
    }
    
    public void setBarDirection(ChartDirection barDir) {
    	_chartDirection = barDir;
    	if (ctBar3DChart != null) {
	    	CTBarDir dir = ctBar3DChart.getBarDir();
	    	if (dir == null) {
	    		dir = ctBar3DChart.addNewBarDir();
	    	}
	    	dir.setVal(XSSFChartUtil.fromBarDirection(barDir));
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

		protected void addToChart(CTBar3DChart ctBar3DChart) {
            CTBarSer barSer = ctBar3DChart.addNewSer();
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
    }

    public CategoryDataSerie addSerie(ChartTextSource title, ChartDataSource<?> cats,
                                      ChartDataSource<? extends Number> vals) {
        if (!vals.isNumeric()) {
            throw new IllegalArgumentException("Pie data source must be numeric.");
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

        if (ctBar3DChart == null) {
	        XSSFChart xssfChart = (XSSFChart) chart;
	        CTPlotArea plotArea = xssfChart.getCTChart().getPlotArea();
	        ctBar3DChart = plotArea.addNewBar3DChart();
        
	        ctBar3DChart.addNewVaryColors().setVal(true);
	        setBarDirection(_chartDirection);
	        setGrouping(_chartGrouping);
	        //TODO setup other properties of bar3DChart
	
	        for (CategoryDataSerie s : series) {
	            ((Serie)s).addToChart(ctBar3DChart);
	        }
        }
        
		// ZSS-358: chart element should also link to axis through ID
		// otherwise, Excel will fail to load this XLSX file
		for(ChartAxis a : axis) {
			ctBar3DChart.addNewAxId().setVal(a.getId());
		}
    }

    public List<? extends CategoryDataSerie> getSeries() {
        return series;
    }
}
