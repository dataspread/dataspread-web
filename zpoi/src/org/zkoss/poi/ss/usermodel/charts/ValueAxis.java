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

import org.zkoss.poi.util.Beta;

/**
 * @author Roman Kashitsyn
 */
@Beta
public interface ValueAxis extends ChartAxis {

	/**
	 * @return cross between type
	 */
	AxisCrossBetween getCrossBetween();

	/**
	 * @param crossBetween cross between type
	 */
	void setCrossBetween(AxisCrossBetween crossBetween);

	//20111012, henrichen@zkoss.org: move from ChartAxis to here
	/**
	 * @return axis number format
	 */
	String getNumberFormat();

	/**
	 * @param format axis number format
	 */
	void setNumberFormat(String format);
}
