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

package org.zkoss.poi.ss.usermodel;

import java.util.List;

import org.zkoss.poi.ss.usermodel.charts.ChartData;
import org.zkoss.poi.ss.usermodel.charts.ChartAxis;
import org.zkoss.poi.ss.usermodel.charts.ChartLegend;
import org.zkoss.poi.ss.usermodel.charts.ManualLayout;
import org.zkoss.poi.ss.usermodel.charts.ManuallyPositionable;
import org.zkoss.poi.ss.usermodel.charts.ChartDataFactory;
import org.zkoss.poi.ss.usermodel.charts.ChartAxisFactory;
import org.zkoss.poi.ss.usermodel.charts.View3D;

/**
 * High level representation of a chart.
 *
 * @author Roman Kashitsyn
 */
public interface Chart extends ManuallyPositionable {
	
	/**
	 * @return an appropriate ChartDataFactory implementation
	 */
	ChartDataFactory getChartDataFactory();

	/**
	 * @return an appropriate ChartAxisFactory implementation
	 */
	ChartAxisFactory getChartAxisFactory();

	/**
	 * @return chart legend instance
	 */
	ChartLegend getOrCreateLegend();

	/**
	 * Delete current chart legend.
	 */
	void deleteLegend();

	/**
	 * @return list of all chart axis
	 */
	List<? extends ChartAxis> getAxis();

	/**
	 * Plots specified data on the chart.
	 *
	 * @param data a data to plot
	 */
	void plot(ChartData data, ChartAxis... axis);
	
	/**
	 * @return chart legend instance
	 */
	View3D getOrCreateView3D();

	/**
	 * Delete current chart legend.
	 */
	void deleteView3D();

	
	//20111007, henrichen@zkoss.org: rename sheet could affect the reference
	void renameSheet(String oldname, String newname);
	
	//20111111, henrichen@zkoss.org: chart poistion anchor
    ClientAnchor getPreferredSize();

	//20111111, henrichen@zkoss.org: chart poistion anchor
	void setClientAnchor(ClientAnchor anchor);

	//20111111, henrichen@zkoss.org: chart id
	String getChartId();

	//20150417, jerrychen@potix.com: visible flag for showing hidden row
	boolean isPlotOnlyVisibleCells();

	//20150417, jerrychen@potix.com: visible flag for showing hidden row
	void setPlotOnlyVisibleCells(boolean plotVisOnly);
}
