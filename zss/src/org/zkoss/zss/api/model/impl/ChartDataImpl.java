/* ChartDataImpl.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/5/1 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.api.model.impl;

import org.zkoss.zss.api.model.ChartData;
import org.zkoss.zss.model.chart.SChartData;
/**
 * 
 * @author dennis
 * @since 3.0.0
 */
public class ChartDataImpl implements ChartData{

	private ModelRef<SChartData> _chartDataRef;
	
	public ChartDataImpl(ModelRef<SChartData> chartDataRef) {
		this._chartDataRef = chartDataRef;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((_chartDataRef == null) ? 0 : _chartDataRef.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChartDataImpl other = (ChartDataImpl) obj;
		if (_chartDataRef == null) {
			if (other._chartDataRef != null)
				return false;
		} else if (!_chartDataRef.equals(other._chartDataRef))
			return false;
		return true;
	}
	
	public SChartData getNative(){
		return _chartDataRef.get();
	}
	
	@Override
	public SChartData getInternalChartData(){
		return _chartDataRef.get(); 
	}
	
}
