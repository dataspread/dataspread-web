/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/01 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model;

import java.util.List;

import org.zkoss.zss.model.chart.SChartData;
/**
 * Represents a chart in a sheet.
 * @author dennis
 * @since 3.5.0
 */
public interface SChart {

	/**
	 * @since 3.5.0
	 */
	public enum ChartType{
		AREA,
		BAR,
		BUBBLE,
		COLUMN,
		DOUGHNUT,
		LINE,
		OF_PIE,
		PIE,
		RADAR,
		SCATTER,
		STOCK,
		SURFACE
	}
	
	/**
	 * @since 3.5.0
	 */
	public enum ChartGrouping {
		STANDARD,
		STACKED,
		PERCENT_STACKED,
		CLUSTERED; //bar only
	}
	
	/**
	 * @since 3.5.0
	 */
	public enum ChartLegendPosition {
		BOTTOM,
		LEFT,
		RIGHT,
		TOP,
		TOP_RIGHT
	}
	
	/**
	 * @since 3.5.0
	 */
	public enum BarDirection {
		HORIZONTAL, //horizontal, bar chart
		VERTICAL; //vertical, column chart
	}

	
	public String getId();
	
	public SSheet getSheet();
	
	public ChartType getType();
	
	public SChartData getData();
	
	public ViewAnchor getAnchor();
	
	public void setAnchor(ViewAnchor anchor);
	
	public String getTitle();
	
	public String getXAxisTitle();
	
	public String getYAxisTitle();

	void setTitle(String title);

	void setXAxisTitle(String xAxisTitle);

	void setYAxisTitle(String yAxisTitle);
	
	public void setLegendPosition(ChartLegendPosition pos);
	
	public ChartLegendPosition getLegendPosition();
	
	public void setGrouping(ChartGrouping grouping);
	
	public ChartGrouping getGrouping();
	
	public BarDirection getBarDirection();
	public void setBarDirection(BarDirection direction);
	
	public boolean isThreeD();
	
	public void setThreeD(boolean threeD);
	
	//ZSS-822
	public List<SChartAxis> getValueAxises();
	public List<SChartAxis> getCategoryAxises();
	
	//ZSS-822
	public void addValueAxis(SChartAxis axis);
	public void addCategoryAxis(SChartAxis axis);
	
	//ZSS-830
	// -90 ~ 90; default: 0
	public int getRotX();
	public void setRotX(int rotX);
	
	//ZSS-830
	// 0 ~ 360; default: 0
	public int getRotY();
	public void setRotY(int rotY);
	
	//ZSS-830
	// 0 ~ 240; default: 30
	public int getPerspective();
	public void setPerspective(int perspective);
	
	//ZSS-830
	// 5 ~ 500; default: 100
	public int getHPercent();
	public void setHPercent(int percent);
	
	//ZSS-830
	// 20 ~ 2000; default: 100
	public int getDepthPercent();
	public void setDepthPercent(int percent);

	//ZSS-830
	// default: true
	public boolean isRightAngleAxes();
	public void setRightAngleAxes(boolean b);

	//ZSS-830
	// -100 ~ +100; default: 0
	public int getBarOverlap();
	public void setBarOverlap(int overlap);

	//ZSS-982
	// default: true
	public void setPlotOnlyVisibleCells(boolean plotOnlyVisibleCells);
	public boolean isPlotOnlyVisibleCells();
}
