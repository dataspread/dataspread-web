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

import org.openxmlformats.schemas.drawingml.x2006.chart.*;
import org.zkoss.poi.ss.usermodel.Chart;
import org.zkoss.poi.ss.usermodel.charts.AbstractCategoryDataSerie;
import org.zkoss.poi.ss.usermodel.charts.CategoryData;
import org.zkoss.poi.ss.usermodel.charts.CategoryDataSerie;
import org.zkoss.poi.ss.usermodel.charts.ChartAxis;
import org.zkoss.poi.ss.usermodel.charts.ChartDataSource;
import org.zkoss.poi.ss.usermodel.charts.ChartGrouping;
import org.zkoss.poi.ss.usermodel.charts.ChartTextSource;
import org.zkoss.poi.util.Beta;
import org.zkoss.poi.xssf.usermodel.XSSFChart;

/**
 * Represents DrawingML area chart.
 *
 * @author henrichen@zkoss.org
 */
@Beta
public class XSSFAreaChartData implements CategoryData {

	private CTAreaChart ctAreaChart;
	private ChartGrouping _chartGrouping;
    /**
     * List of all data series.
     */
    private List<CategoryDataSerie> series;

    public XSSFAreaChartData() {
        series = new ArrayList<CategoryDataSerie>();
    }

    public XSSFAreaChartData(XSSFChart chart) {
    	this();
    	final CTPlotArea plotArea = chart.getCTChart().getPlotArea();
    	
    	//Area
    	@SuppressWarnings("deprecation")
		final CTAreaChart[] plotCharts = plotArea.getAreaChartArray();
    	if (plotCharts != null && plotCharts.length > 0) {
    		ctAreaChart = plotCharts[0];
    	}
    	
    	if (ctAreaChart != null) {
    		@SuppressWarnings("deprecation")
			CTAreaSer[] bsers = ctAreaChart.getSerArray();
    		for (int j = 0; j < bsers.length; ++j) {
    			final CTAreaSer ser = bsers[j];
    			CTSerTx serTx = ser.getTx();
    			ChartTextSource title = serTx == null ? null : new XSSFChartTextSource(serTx);
    			ChartDataSource<String> cats = new XSSFChartAxDataSource<String>(ser.getCat());
    			ChartDataSource<Double> vals = new  XSSFChartNumDataSource<Double>(ser.getVal());
		    	addSerie(title, cats, vals);
    		}
	    }
    }
    
    public ChartGrouping getGrouping() {
    	return XSSFChartUtil.toChartGrouping(ctAreaChart.getGrouping());
    }
    
    public void setGrouping(ChartGrouping grouping) {
    	_chartGrouping = grouping;
    	if (ctAreaChart != null){
    		CTGrouping ctGrouping = ctAreaChart.getGrouping(); 
    		if ( ctGrouping == null){
    			ctGrouping = ctAreaChart.addNewGrouping();
    		}
    		ctGrouping.setVal(XSSFChartUtil.fromChartGrouping(_chartGrouping));
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

		protected void addToChart(CTAreaChart ctAreaChart) {
            CTAreaSer areaSer = ctAreaChart.addNewSer();
            areaSer.addNewIdx().setVal(this.id);
            areaSer.addNewOrder().setVal(this.order);

            if (title != null) {
	            CTSerTx tx = areaSer.addNewTx();
	            XSSFChartUtil.buildSerTx(tx, title);
            }
            
            if (categories != null && categories.getPointCount() > 0) {
	            CTAxDataSource cats = areaSer.addNewCat();
	            XSSFChartUtil.buildAxDataSource(cats, categories);
            }

            CTNumDataSource vals = areaSer.addNewVal();
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

        if (ctAreaChart == null) {
	        XSSFChart xssfChart = (XSSFChart) chart;
	        CTPlotArea plotArea = xssfChart.getCTChart().getPlotArea();
	        ctAreaChart = plotArea.addNewAreaChart();
        
	        ctAreaChart.addNewVaryColors().setVal(true);
	        //TODO setup other properties of areaChart
	        setGrouping(_chartGrouping);
	        for (CategoryDataSerie s : series) {
	            ((Serie)s).addToChart(ctAreaChart);
	        }
        }
        
		// ZSS-358: chart element should also link to axis through ID
		// otherwise, Excel will fail to load this XLSX file
		for(ChartAxis a : axis) {
			ctAreaChart.addNewAxId().setVal(a.getId());
		}
    }

    public List<? extends CategoryDataSerie> getSeries() {
        return series;
    }
}
