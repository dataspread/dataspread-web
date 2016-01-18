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
package org.zkoss.zss.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zss.model.SChartAxis;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.ViewAnchor;
import org.zkoss.zss.model.chart.SChartData;
import org.zkoss.zss.model.impl.chart.ChartDataAdv;
import org.zkoss.zss.model.impl.chart.GeneralChartDataImpl;
import org.zkoss.zss.model.impl.chart.UnsupportedChartDataImpl;
import org.zkoss.zss.model.util.Validations;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public class ChartImpl extends AbstractChartAdv {
	private static final long serialVersionUID = 1L;
	private String _id;
	private ChartType _type;
	private ViewAnchor _anchor;
	private ChartDataAdv _data;
	private String _title;
	private String _xAxisTitle;
	private String _yAxisTitle;
	private AbstractSheetAdv _sheet;
	
	private ChartLegendPosition _legendPosition;
	private ChartGrouping _grouping;
	private BarDirection _direction;
	
	private boolean _threeD;
	
	//ZSS-830
	private boolean _rAngAx = true; //default true
	private int _rotX = 0;
	private int _rotY = 0;
	private int _hPercent = 100;
	private int _depthPercent = 100;
	private int _perspective = 30;
	private int _barOverlap = 0;
	
	private boolean _plotOnlyVisibleCells = true; //default true
	
	private List<SChartAxis> _valueAxises = new ArrayList<SChartAxis>();
	private List<SChartAxis> _categoryAxises = new ArrayList<SChartAxis>();
	
	public ChartImpl(AbstractSheetAdv sheet,String id,ChartType type,ViewAnchor anchor){
		this._sheet = sheet;
		this._id = id;
		this._type = type;
		this._anchor = anchor;
		this._data = createChartData(type);
		
		Validations.argNotNull(anchor);
		
		switch(type){//default initialization
		case BAR:
			_direction = BarDirection.HORIZONTAL;
			break;
		case COLUMN:
			_direction = BarDirection.VERTICAL;
			break;
		}
	}
	@Override
	public SSheet getSheet(){
		checkOrphan();
		return _sheet;
	}
	@Override
	public String getId() {
		return _id;
	}
	@Override
	public ViewAnchor getAnchor() {
		return _anchor;
	}
	@Override
	public void setAnchor(ViewAnchor anchor) {
		Validations.argNotNull(anchor);
		this._anchor = anchor;		
	}
	@Override
	public ChartType getType(){
		return _type;
	}
	@Override
	public SChartData getData() {
		return _data;
	}
	@Override
	public String getTitle() {
		return _title;
	}
	@Override
	public void setTitle(String title) {
		this._title = title;
	}
	@Override
	public String getXAxisTitle() {
		return _xAxisTitle;
	}
	@Override
	public void setXAxisTitle(String xAxisTitle) {
		this._xAxisTitle = xAxisTitle;
	}
	@Override
	public String getYAxisTitle() {
		return _yAxisTitle;
	}
	@Override
	public void setYAxisTitle(String yAxisTitle) {
		this._yAxisTitle = yAxisTitle;
	}

	private ChartDataAdv createChartData(ChartType type){
		switch(type){
		case AREA:
		case BAR:
		case COLUMN://same as bar
		case LINE:
		case DOUGHNUT://same as pie
		case PIE:
		case SCATTER://xy , reuse category
		case BUBBLE://xyz , reuse category
		case STOCK://stock, reuse category			
			return new GeneralChartDataImpl(this,_id+"-data");
			
		//not supported	
		case OF_PIE:
		case RADAR:
		case SURFACE:
			return new UnsupportedChartDataImpl(this);
		}
		throw new UnsupportedOperationException("unsupported chart type "+type);
	}

	@Override
	public void destroy() {
		checkOrphan();
		
		((ChartDataAdv)getData()).destroy();
		
		_sheet = null;
	}
	@Override
	public void checkOrphan() {
		if (_sheet == null) {
			throw new IllegalStateException("doesn't connect to parent");
		}
	}
	@Override
	public void setLegendPosition(ChartLegendPosition pos) {
		this._legendPosition = pos;
	}
	@Override
	public ChartLegendPosition getLegendPosition() {
		return _legendPosition;
	}
	@Override
	public void setGrouping(ChartGrouping grouping) {
		this._grouping = grouping;
	}
	@Override
	public ChartGrouping getGrouping() {
		return _grouping;
	}
	@Override
	public BarDirection getBarDirection() {
		return _direction;
	}
	public void setBarDirection(BarDirection direction){
		this._direction = direction;
	}
	@Override
	public boolean isThreeD() {
		return _threeD;
	}
	@Override
	public void setThreeD(boolean threeD) {
		this._threeD = threeD;
	}

	//ZSS-688
	//@since 3.6.0
	/*package*/ ChartImpl cloneChartImpl(AbstractSheetAdv sheet) {
		ChartImpl tgt = new ChartImpl(sheet,this._id,this._type, this._anchor.cloneViewAnchor());
		if (tgt._data instanceof GeneralChartDataImpl) {
			((GeneralChartDataImpl)tgt._data).copyFrom((GeneralChartDataImpl) this._data);
		}
		
		tgt._title = this._title;
		tgt._xAxisTitle = this._xAxisTitle;
		tgt._yAxisTitle = this._yAxisTitle;
		tgt._legendPosition = this._legendPosition;
		tgt._grouping = this._grouping;
		tgt._direction = this._direction;
		tgt._threeD = this._threeD;
		for (SChartAxis axis : _valueAxises) {
			tgt.addValueAxis(((ChartAxisImpl)axis).cloneChartAxisImpl());
		}
		for (SChartAxis axis : _categoryAxises) {
			tgt.addCategoryAxis(((ChartAxisImpl)axis).cloneChartAxisImpl());
		}
		return tgt;
	}
	@Override
	public void addValueAxis(SChartAxis axis) {
		_valueAxises.add(axis);
	}
	@Override
	public void addCategoryAxis(SChartAxis axis) {
		_categoryAxises.add(axis);
	}
	
	@Override
	public List<SChartAxis> getValueAxises() {
		return _valueAxises;
	}
	@Override
	public List<SChartAxis> getCategoryAxises() {
		return _categoryAxises;
	}
	
	//ZSS-830
	@Override
	public int getRotX() {
		return _rotX;
	}
	//ZSS-830
	@Override
	public void setRotX(int rotX) {
		_rotX = rotX;
	}
	//ZSS-830
	@Override
	public int getRotY() {
		return _rotY;
	}
	//ZSS-830
	@Override
	public void setRotY(int rotY) {
		_rotY = rotY;
	}
	//ZSS-830
	@Override
	public int getPerspective() {
		return _perspective;
	}
	//ZSS-830
	@Override
	public void setPerspective(int perspective) {
		_perspective = perspective;
	}
	//ZSS-830
	@Override
	public int getHPercent() {
		return _hPercent;
	}
	//ZSS-830
	@Override
	public void setHPercent(int percent) {
		_hPercent = percent;
	}
	//ZSS-830
	@Override
	public int getDepthPercent() {
		return _depthPercent;
	}
	//ZSS-830
	@Override
	public void setDepthPercent(int percent) {
		_depthPercent = percent;
	}
	//ZSS-830
	@Override
	public boolean isRightAngleAxes() {
		return _rAngAx;
	}
	//ZSS-830
	@Override
	public void setRightAngleAxes(boolean b) {
		_rAngAx = b;
	}
	@Override
	public int getBarOverlap() {
		return _barOverlap;
	}
	@Override
	public void setBarOverlap(int overlap) {
		_barOverlap = overlap;
	}
	@Override
	public void setPlotOnlyVisibleCells(boolean plotOnlyVisibleCells) {
		_plotOnlyVisibleCells = plotOnlyVisibleCells;
		
	}
	@Override
	public boolean isPlotOnlyVisibleCells() {
		return _plotOnlyVisibleCells;
	}
}
