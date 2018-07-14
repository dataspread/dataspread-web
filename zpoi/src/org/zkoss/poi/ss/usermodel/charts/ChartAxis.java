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
 * High level representation of chart axis.
 *
 * @author Roman Kashitsyn
 */
@Beta
public interface ChartAxis {
	
	/**
	 * @return axis id
	 */
	long getId();

	/**
	 * @return axis position
	 */
	AxisPosition getPosition();

	/**
	 * @param position new axis position
	 */
	void setPosition(AxisPosition position);

	/**
	 * @return axis number format
	 */
//	String getNumberFormat(); //moved to ValueAxis

	/**
	 * @param format axis number format
	 */
//	void setNumberFormat(String format); //moved to ValueAxis

	/**
	 * @return true if log base is defined, false otherwise
	 */
	boolean isSetLogBase();

	/**
	 * @param logBase a number between 2 and 1000 (inclusive)
	 * @throws IllegalArgumentException if log base not within allowed range
	 */
	void setLogBase(double logBase);

	/**
	 * @return axis log base or 0.0 if not set
	 */
	double getLogBase();

	/**
	 * @return true if minimum value is defined, false otherwise
	 */
	boolean isSetMinimum();

	/**
	 * @return axis minimum or 0.0 if not set
	 */
	double getMinimum();

	/**
	 * @param min axis minimum
	 */
	void setMinimum(double min);

	/**
	 * @return true if maximum value is defined, false otherwise
	 */
	boolean isSetMaximum();

	/**
	 * @return axis maximum or 0.0 if not set
	 */
	double getMaximum();

	/**
	 * @param max axis maximum
	 */
	void setMaximum(double max);

	/**
	 * @return axis orientation
	 */
	AxisOrientation getOrientation();

	/**
	 * @param orientation axis orientation
	 */
	void setOrientation(AxisOrientation orientation);

	/**
	 * @param crosses axis cross type
	 */
	void setCrosses(AxisCrosses crosses);

	/**
	 * @return axis cross type
	 */
	AxisCrosses getCrosses();

	/**
	 * Declare this axis cross another axis.
	 * @param axis that this axis should cross
	 */
	void crossAxis(ChartAxis axis);

	
	//20111012, henrichen@zkoss.org: handle tick label position
	/**
	 * Returns tick label position.
	 * @return tick label position.
	 */
	AxisTickLabelPosition getTickLabelPosition();
	
	/**
	 * Sets tick label position.
	 * @param tickLblPos the tick label position
	 */
	void setTickLabelPosition(AxisTickLabelPosition tickLblPos);
}
