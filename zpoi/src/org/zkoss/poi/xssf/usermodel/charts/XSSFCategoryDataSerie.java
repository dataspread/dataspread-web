/* XSSFCategoryDataSerie.java

	Purpose:
		
	Description:
		
	History:
		Oct 7, 2011 2:41:13 PM, Created by henri

Copyright (C) 2011 Potix Corporation. All Rights Reserved.
*/
package org.zkoss.poi.xssf.usermodel.charts;

import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPie3DChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPieSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;
import org.zkoss.poi.ss.usermodel.charts.AbstractCategoryDataSerie;
import org.zkoss.poi.ss.usermodel.charts.ChartDataSource;
import org.zkoss.poi.ss.usermodel.charts.ChartTextSource;

/**
 * @author henrichen@zkoss.org
 *
 */
public class XSSFCategoryDataSerie extends AbstractCategoryDataSerie {
	protected XSSFCategoryDataSerie(int id, int order, ChartTextSource title,
			ChartDataSource<?> xs, ChartDataSource<? extends Number> ys) {
		super(id, order, title, xs, ys);
	}

    protected void addToChart(CTPie3DChart ctPie3DChart) {
        CTPieSer pieSer = ctPie3DChart.addNewSer();
        pieSer.addNewIdx().setVal(this.id);
        pieSer.addNewOrder().setVal(this.order);

        CTSerTx tx = pieSer.addNewTx();
        XSSFChartUtil.buildSerTx(tx, title);
        
        CTAxDataSource cats = pieSer.addNewCat();
        XSSFChartUtil.buildAxDataSource(cats, categories);

        CTNumDataSource vals = pieSer.addNewVal();
        XSSFChartUtil.buildNumDataSource(vals, values);
    }
}
