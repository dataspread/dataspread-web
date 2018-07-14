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

package org.zkoss.poi.ss.usermodel.charts;

import java.util.List;

/**
 * @author henrichen@zkoss.org
 */
public interface XYData extends ChartData {
	/**
	 * @param title text source to be used for chart serie title
	 * @param xs axis data source to be used for xy data serie
	 * @param ys axis data source to be used for xy data serie
	 * @return a new xy serie
	 */
	XYDataSerie addSerie(ChartTextSource title, ChartDataSource<? extends Number> xs, ChartDataSource<? extends Number> ys);

	/**
	 * @return list of all series
	 */
	List<? extends XYDataSerie> getSeries();
}
