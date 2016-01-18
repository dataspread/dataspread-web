/* ChartAxisImpl.java

	Purpose:
		
	Description:
		
	History:
		Nov 11, 2014 10:53:47 AM, Created by henrichen

	Copyright (C) 2014 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model.impl;

import java.io.Serializable;

import org.zkoss.zss.model.SChartAxis;

/**
 * @author henri
 *
 */
public class ChartAxisImpl implements SChartAxis, Serializable {
	private static final long serialVersionUID = 2598887754686809214L;
	final private long id;
	final private SChartAxisType type;
	private Double min;
	private Double max;
	private String format;
	
	public ChartAxisImpl(long id, SChartAxisType type, Double min, Double max, String format) {
		this.id = id;
		this.type = type;
		this.min = min;
		this.max = max;
		this.format = format;
	}
	
	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public SChartAxisType getType() {
		return type;
	}

	@Override
	public Double getMax() {
		return max;
	}

	@Override
	public void setMax(Double max) {
		this.max = max;
	}

	@Override
	public Double getMin() {
		return min;
	}

	@Override
	public void setMin(Double min) {
		this.min = min;
	}

	@Override
	public String getFormat() {
		return format;
	}

	@Override
	public void setFormat(String format) {
		this.format = format;
	}
	
	SChartAxis cloneChartAxisImpl() {
		return new ChartAxisImpl(this.id, this.type, this.min, this.max, this.format);
	}
}
