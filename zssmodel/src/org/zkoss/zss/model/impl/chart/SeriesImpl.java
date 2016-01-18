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
package org.zkoss.zss.model.impl.chart;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.zkoss.zss.model.ErrorValue;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.chart.SSeries;
import org.zkoss.zss.model.impl.AbstractBookSeriesAdv;
import org.zkoss.zss.model.impl.AbstractChartAdv;
import org.zkoss.zss.model.impl.AbstractSeriesAdv;
import org.zkoss.zss.model.impl.EvaluationUtil;
import org.zkoss.zss.model.impl.LinkedModelObject;
import org.zkoss.zss.model.impl.ObjectRefImpl;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.formula.EvaluationResult;
import org.zkoss.zss.model.sys.formula.FormulaEngine;
import org.zkoss.zss.model.sys.formula.FormulaEvaluationContext;
import org.zkoss.zss.model.sys.formula.FormulaExpression;
import org.zkoss.zss.model.sys.formula.FormulaParseContext;
import org.zkoss.zss.model.sys.formula.EvaluationResult.ResultType;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public class SeriesImpl extends AbstractSeriesAdv implements SSeries,Serializable,LinkedModelObject{
	private static final long serialVersionUID = 1L;
	private FormulaExpression _nameExpr;
	private FormulaExpression _valueExpr;
	private FormulaExpression _yValueExpr;
	private FormulaExpression _zValueExpr;
	
	private AbstractChartAdv _chart;
	private final String _id;
	
	private Object _evalNameResult;
	private Object _evalValuesResult;
	private Object _evalYValuesResult;
	private Object _evalZValuesResult;

	private boolean[] _hiddenNameInfo;
	private boolean[] _hiddenValuesInfo;
	private boolean[] _hiddenYValuesInfo;
	private boolean[] _hiddenZValuesInfo;
	
	private boolean _evaluated = false;
	private boolean _visibleEvaluated = false;
	
	
	/*package*/ void evalFormula(){
		//ZSS-740
		//20140730, henrichen: when share the same book, many users might 
		//populate SeriesImpl simultaneously; must synchronize it.
		if(_evaluated) return;
		synchronized (this) {
			if(!_evaluated){
				FormulaEngine fe = EngineFactory.getInstance().createFormulaEngine();
				SSheet sheet = _chart.getSheet();
				Ref ref = getRef();
				if(_nameExpr!=null){
					EvaluationResult result = fe.evaluate(_nameExpr,new FormulaEvaluationContext(sheet,ref));
	
					Object val = result.getValue();
					if(result.getType() == ResultType.SUCCESS){
						_evalNameResult = val;
					}else if(result.getType() == ResultType.ERROR){
						_evalNameResult = (val instanceof ErrorValue)?val: ErrorValue.valueOf(ErrorValue.INVALID_VALUE);
					}
					
				}
				if(_valueExpr!=null){
					EvaluationResult result = fe.evaluate(_valueExpr,new FormulaEvaluationContext(sheet,ref));
					Object val = result.getValue();
					if(result.getType() == ResultType.SUCCESS){
						_evalValuesResult = val;
					}else if(result.getType() == ResultType.ERROR){
						_evalValuesResult = (val instanceof ErrorValue)?val: ErrorValue.valueOf(ErrorValue.INVALID_VALUE);
					}
				}
				if(_yValueExpr!=null){
					EvaluationResult result = fe.evaluate(_yValueExpr,new FormulaEvaluationContext(sheet,ref));
					Object val = result.getValue();
					if(result.getType() == ResultType.SUCCESS){
						_evalYValuesResult = val;
					}else if(result.getType() == ResultType.ERROR){
						_evalYValuesResult = (val instanceof ErrorValue)?val: ErrorValue.valueOf(ErrorValue.INVALID_VALUE);
					}
				}
				if(_zValueExpr!=null){
					EvaluationResult result = fe.evaluate(_zValueExpr,new FormulaEvaluationContext(sheet,ref));
					Object val = result.getValue();
					if(result.getType() == ResultType.SUCCESS){
						_evalZValuesResult = val;
					}else if(result.getType() == ResultType.ERROR){
						_evalZValuesResult = (val instanceof ErrorValue)?val: ErrorValue.valueOf(ErrorValue.INVALID_VALUE);
					}
				}
				_evaluated = true;
			}
		}
	}
	
	/*package*/ void evalVisibleInfo() {
		if(_visibleEvaluated)
			return;
		
		synchronized (this) {
			if(!_visibleEvaluated) {
				Map<Integer, Boolean> cachedRowValues = new HashMap<Integer, Boolean>(16);
				Map<Integer, Boolean> cachedColumnValues = new HashMap<Integer, Boolean>(16);
				
				if(_nameExpr != null) {
					_hiddenNameInfo = new boolean[getNumOfValue()];
					ChartUtil.evalVisibleInfo(_chart, _nameExpr, _hiddenNameInfo, cachedRowValues, cachedColumnValues);
				}
				
				if(_valueExpr != null) {
					_hiddenValuesInfo = new boolean[getNumOfValue()];
					ChartUtil.evalVisibleInfo(_chart, _valueExpr, _hiddenValuesInfo, cachedRowValues, cachedColumnValues);
				}
				
				if(_yValueExpr != null) {
					_hiddenYValuesInfo = new boolean[getNumOfValue()];
					ChartUtil.evalVisibleInfo(_chart, _yValueExpr, _hiddenYValuesInfo, cachedRowValues, cachedColumnValues);		
				}
		
				if(_zValueExpr != null) {
					_hiddenZValuesInfo = new boolean[getNumOfValue()];
					ChartUtil.evalVisibleInfo(_chart, _zValueExpr, _hiddenZValuesInfo, cachedRowValues, cachedColumnValues);
				}
				
				_visibleEvaluated = true;
			}
		}
	}
	
	public SeriesImpl(AbstractChartAdv chart,String id){
		this._chart = chart;
		this._id = id;
	}
	
	@Override
	public String getId(){
		return _id;
	}
	
	@Override
	public String getName() {
		evalFormula();
		return _evalNameResult==null?null:(_evalNameResult instanceof ErrorValue)?((ErrorValue)_evalNameResult).getErrorString():_evalNameResult.toString();
	}
	@Override
	public int getNumOfValue(){
		evalFormula();
		return EvaluationUtil.sizeOf(_evalValuesResult);
	}
	@Override
	public Object getValue(int index) {
		evalFormula();
		if(index>=EvaluationUtil.sizeOf(_evalValuesResult)){
			return null;
		}
		return EvaluationUtil.valueOf(_evalValuesResult,index);
	}
	@Override
	public int getNumOfYValue(){
		evalFormula();
		return EvaluationUtil.sizeOf(_evalYValuesResult);
	}
	@Override
	public Object getYValue(int index) {
		evalFormula();
		if(index>=EvaluationUtil.sizeOf(_evalYValuesResult)){
			return null;
		}
		return EvaluationUtil.valueOf(_evalYValuesResult,index);
	}
	public int getNumOfZValue(){
		evalFormula();
		return EvaluationUtil.sizeOf(_evalZValuesResult);
	}
	@Override
	public Object getZValue(int index) {
		evalFormula();
		if(index>=EvaluationUtil.sizeOf(_evalZValuesResult)){
			return null;
		}
		return EvaluationUtil.valueOf(_evalZValuesResult,index);
	}
	
	@Override
	public void setFormula(String nameExpression,String valueExpression){
		setXYZFormula(nameExpression,valueExpression,null,null);
	}
	@Override
	public void setXYFormula(String nameExpression,String xValueExpression, String yValueExpression){
		setXYZFormula(nameExpression,xValueExpression,yValueExpression,null);
	}
	@Override
	public void setXYZFormula(String nameExpression,String xValueExpression, String yValueExpression,String zValueExpression){
		checkOrphan();
		_evaluated = false;
		_visibleEvaluated = false;
		clearFormulaDependency();
		
		FormulaEngine fe = EngineFactory.getInstance().createFormulaEngine();
		SSheet sheet = _chart.getSheet();
		Ref ref = getRef();
		if(nameExpression!=null){
			_nameExpr = fe.parse(nameExpression, new FormulaParseContext(sheet,ref));
		}else{
			_nameExpr = null;
		}
		if(xValueExpression!=null){
			_valueExpr = fe.parse(xValueExpression, new FormulaParseContext(sheet,ref));
		}else{
			_valueExpr = null;
		}
		if(yValueExpression!=null){
			_yValueExpr = fe.parse(yValueExpression, new FormulaParseContext(sheet,ref));
		}else{
			_yValueExpr = null;
		}
		if(zValueExpression!=null){
			_zValueExpr = fe.parse(zValueExpression, new FormulaParseContext(sheet,ref));
		}else{
			_zValueExpr = null;
		}
	}
	
	@Override
	public boolean isFormulaParsingError() {
		boolean r = false;
		if(_nameExpr!=null){
			r |= _nameExpr.hasError();
		}
		if(!r && _valueExpr!=null){
			r |= _valueExpr.hasError();
		}
		if(!r && _yValueExpr!=null){
			r |= _yValueExpr.hasError();
		}
		if(!r && _zValueExpr!=null){
			r |= _zValueExpr.hasError();
		}
		
		return r;
	}

	@Override
	public String getNameFormula() {
		return _nameExpr==null?null:_nameExpr.getFormulaString();
	}

	@Override
	public String getValuesFormula() {
		return _valueExpr==null?null:_valueExpr.getFormulaString();
	}

	@Override
	public String getYValuesFormula() {
		return _yValueExpr==null?null:_yValueExpr.getFormulaString();
	}
	
	@Override
	public String getZValuesFormula() {
		return _zValueExpr==null?null:_zValueExpr.getFormulaString();
	}

	@Override
	public void clearFormulaResultCache() {
		_evaluated = false;
		_visibleEvaluated = false;
		_evalNameResult = _evalValuesResult = _evalYValuesResult = _evalZValuesResult = null;
		_hiddenNameInfo = _hiddenValuesInfo = _hiddenYValuesInfo = _hiddenZValuesInfo = null;
	}
	
	private void clearFormulaDependency() {
		if(_nameExpr!=null || _valueExpr!=null || _yValueExpr!=null || _zValueExpr!=null){
			((AbstractBookSeriesAdv) _chart.getSheet().getBook().getBookSeries())
					.getDependencyTable().clearDependents(getRef());
		}
	}
	
	private Ref getRef(){
		return new ObjectRefImpl(_chart,new String[]{_chart.getId(),_id});
	}
	
	@Override
	public void destroy() {
		checkOrphan();
		clearFormulaDependency();
		clearFormulaResultCache();
		_chart = null;
	}

	@Override
	public void checkOrphan() {
		if(_chart==null){
			throw new IllegalStateException("doesn't connect to parent");
		}
	}

	@Override
	public int getNumOfXValue() {
		return getNumOfValue();
	}

	@Override
	public Object getXValue(int index) {
		return getValue(index);
	}

	@Override
	public String getXValuesFormula() {
		return getValuesFormula();
	}

	//ZSS-688
	//@since 3.6.0
	public SeriesImpl cloneSeriesImpl(AbstractChartAdv chart) {
		SeriesImpl tgt = new SeriesImpl(chart, this._id);

		final String nexpr = this.getNameFormula();
		final String vexpr = this.getValuesFormula();
		final String yexpr = this.getYValuesFormula();
		final String zexpr = this.getZValuesFormula();

		tgt.setXYZFormula(nexpr, vexpr, yexpr, zexpr);

		// do not clone _evalNameResult, _evalValueResult, _evalYValueRsult, _evalZValueResult, _evaluated
		//private Object _evalNameResult;
		//private Object _evalValuesResult;
		//private Object _evalYValuesResult;
		//private Object _evalZValuesResult;
		//private boolean _evaluated = false;
		
		return tgt;
	}
	//ZSS-747
	@Override
	public FormulaExpression getNameFormulaExpression() {
		return this._nameExpr;
	}
	//ZSS-747
	@Override
	public FormulaExpression getValuesFormulaExpression() {
		return this._valueExpr;
	}
	//ZSS-747
	@Override
	public FormulaExpression getXValuesFormulaExpression() {
		return getValuesFormulaExpression();
	}
	//ZSS-747
	@Override
	public FormulaExpression getYValuesFormulaExpression() {
		return this._yValueExpr;
	}
	//ZSS-747
	@Override
	public FormulaExpression getZValuesFormulaExpression() {
		return this._zValueExpr;
	}
	//ZSS-747
	public void setXYZFormula(FormulaExpression nameExpr, FormulaExpression xValueExpr, FormulaExpression yValueExpr, FormulaExpression zValueExpr) {
		checkOrphan();
		_evaluated = false;
		_visibleEvaluated = false;
		clearFormulaDependency();
		
		this._nameExpr = nameExpr;
		this._valueExpr = xValueExpr;
		this._yValueExpr = yValueExpr;
		this._zValueExpr = zValueExpr;

		// Update dependency table
		FormulaEngine fe = EngineFactory.getInstance().createFormulaEngine();
		SSheet sheet = _chart.getSheet();
		Ref ref = getRef();
		FormulaParseContext context = new FormulaParseContext(sheet, ref); 
		if(nameExpr != null) {
			fe.updateDependencyTable(nameExpr, context);
		}
		if(xValueExpr != null){
			fe.updateDependencyTable(xValueExpr, context);
		}
		if(yValueExpr != null){
			fe.updateDependencyTable(yValueExpr, context);
		}
		if(zValueExpr !=null){
			fe.updateDependencyTable(zValueExpr, context);
		}
	}

	@Override
	public boolean isNameFomulaHidden(int index) {
		evalVisibleInfo();
		return _hiddenNameInfo == null ? false : _hiddenNameInfo[index];
	}
	
	@Override
	public boolean isXValueFomulaHidden(int index) {
		evalVisibleInfo();
		return _hiddenValuesInfo == null ? false : _hiddenValuesInfo[index];
	}

	@Override
	public boolean isYValueFomulaHidden(int index) {
		evalVisibleInfo();
		return _hiddenYValuesInfo == null ? false : _hiddenYValuesInfo[index];
	}

	@Override
	public boolean isZValueFomulaHidden(int index) {
		evalVisibleInfo();
		return _hiddenZValuesInfo == null ? false : _hiddenZValuesInfo[index];
	}

}
