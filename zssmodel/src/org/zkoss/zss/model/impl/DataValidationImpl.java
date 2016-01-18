/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.zkoss.lang.Strings;
import org.zkoss.poi.ss.formula.LazyAreaEval;
import org.zkoss.poi.ss.formula.LazyRefEval;
import org.zkoss.poi.ss.formula.eval.StringEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.poi.ss.formula.ptg.BoolPtg;
import org.zkoss.poi.ss.formula.ptg.ErrPtg;
import org.zkoss.poi.ss.formula.ptg.IntPtg;
import org.zkoss.poi.ss.formula.ptg.NumberPtg;
import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.poi.ss.formula.ptg.StringPtg;
import org.zkoss.poi.ss.usermodel.ZssContext;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.ErrorValue;
import org.zkoss.zss.model.InvalidFormulaException;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SBookSeries;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.dependency.DependencyTable;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.dependency.ObjectRef.ObjectType;
import org.zkoss.zss.model.sys.formula.EvaluationResult;
import org.zkoss.zss.model.sys.formula.FormulaEngine;
import org.zkoss.zss.model.sys.formula.FormulaEvaluationContext;
import org.zkoss.zss.model.sys.formula.FormulaExpression;
import org.zkoss.zss.model.sys.formula.FormulaParseContext;
import org.zkoss.zss.model.sys.formula.EvaluationResult.ResultType;
import org.zkoss.zss.model.sys.input.InputEngine;
import org.zkoss.zss.model.sys.input.InputParseContext;
import org.zkoss.zss.model.sys.input.InputResult;
import org.zkoss.zss.model.util.Validations;
import org.zkoss.zss.model.impl.sys.DependencyTableAdv;
import org.zkoss.zss.model.impl.sys.FormatEngineImpl;
/**
 * 
 * @author Dennis
 * @since 3.5.0
 */
public class DataValidationImpl extends AbstractDataValidationAdv {

	private static final long serialVersionUID = 1L;
	private AbstractSheetAdv _sheet;
	final private String _id;
	
	private AlertStyle _alertStyle = AlertStyle.STOP;//default stop
	private boolean _ignoreBlank = true;//default true
	private boolean _showInCellDropdown;
	private boolean _showInput;
	private boolean _showError;
	private String _inputTitle;
	private String _inputMessage;
	private String _errorTitle;
	private String _errorMessage;
	private Set<CellRegion> _regions;
	private ValidationType _validationType = ValidationType.ANY;
	private OperatorType _operatorType = OperatorType.BETWEEN;
	
	
	private FormulaExpression _formula1Expr;
	private FormulaExpression _formula2Expr;
	private Object _evalValue1Result;
	private Object _evalValue2Result;
	private ValueEval _evalValue1EvalResult; //ZSS-810
	private ValueEval _evalValue2EvalResult; //ZSS-810
	
	private boolean _evaluated = false;
	
	public DataValidationImpl(AbstractSheetAdv sheet,String id){
		this._sheet = sheet;
		this._id = id;
	}
	
	public DataValidationImpl(AbstractSheetAdv sheet, AbstractDataValidationAdv copyFrom) {
		this(sheet, (String) null);
		if (copyFrom != null) {
			this.copyFrom(copyFrom);
		}
	}
	
	public String getId(){
		return _id;
	}
	
	public SSheet getSheet(){
		return _sheet;
	}
	
	@Override
	public void checkOrphan() {
		if (_sheet == null) {
			throw new IllegalStateException("doesn't connect to parent");
		}
	}
	
	@Override
	public void destroy() {
		checkOrphan();
		clearFormulaDependency(true);
		clearFormulaResultCache();
		_sheet = null;
	}
	
	@Override
	public AlertStyle getAlertStyle() {
		return _alertStyle;
	}

	@Override
	public void setAlertStyle(AlertStyle alertStyle) {
		Validations.argNotNull(alertStyle);
		this._alertStyle = alertStyle;
	}

	@Override
	public void setIgnoreBlank(boolean allowed) {
		this._ignoreBlank = allowed;
	}

	@Override
	public boolean isIgnoreBlank() {
		return _ignoreBlank;
	}

	@Override
	public void setInCellDropdown(boolean show) {
		_showInCellDropdown = show;
	}

	@Override
	public boolean isInCellDropdown() {
		return _showInCellDropdown;
	}

	@Override
	public void setShowInput(boolean show) {
		_showInput = show;
	}

	@Override
	public boolean isShowInput() {
		return _showInput;
	}

	@Override
	public void setShowError(boolean show) {
		_showError = show;
	}

	@Override
	public boolean isShowError() {
		return _showError;
	}

	@Override
	public void setInputTitle(String title) {
		_inputTitle = title;
	}
	@Override
	public void setInputMessage(String message) {
		_inputMessage = message;
	}

	@Override
	public String getInputTitle() {
		return _inputTitle;
	}

	@Override
	public String getInputMessage() {
		return _inputMessage;
	}

	@Override
	public void setErrorTitle(String title) {
		_errorTitle = title;
	}
	
	@Override
	public void setErrorMessage(String text) {
		_errorMessage = text;
	}

	@Override
	public String getErrorTitle() {
		return _errorTitle;
	}

	@Override
	public String getErrorMessage() {
		return _errorMessage;
	}

	@Override
	public Set<CellRegion> getRegions() {
		return _regions;
	}
	
	@Override
	public void addRegion(CellRegion region) { // ZSS-648
		Validations.argNotNull(region);
		if (this._regions == null) {
			this._regions = new LinkedHashSet<CellRegion>(2);
		}
		for (CellRegion regn : this._regions) {
			if (regn.contains(region)) return; // already in this DataValidation, let go
		}
		
		this._regions.add(region);
		
		// ZSS-648
		// Add new ObjectRef into DependencyTable so we can extend/shrink/move
		Ref dependent = getRef();
		SBook book = _sheet.getBook();
		final DependencyTable dt = 
				((AbstractBookSeriesAdv) book.getBookSeries()).getDependencyTable();
		// prepare a dummy CellRef to enforce DataValidation reference dependency
		dt.add(dependent, newDummyRef(region));
		
		ModelUpdateUtil.addRefUpdate(dependent);
	}
	
	@Override
	public void removeRegion(CellRegion region) { // ZSS-694
		Validations.argNotNull(region);
		if (this._regions == null || this._regions.isEmpty()) return;
		
		List<CellRegion> newRegions = new ArrayList<CellRegion>();
		List<CellRegion> delRegions = new ArrayList<CellRegion>();
		for (CellRegion regn : this._regions) {
			if (!regn.overlaps(region)) continue;
			newRegions.addAll(regn.diff(region));
			delRegions.add(regn);
		}
		
		// no overlapping at all
		if (newRegions.isEmpty() && delRegions.isEmpty()) {
			return;
		}

		Ref dependent = getRef();
		SBook book = _sheet.getBook();
		final DependencyTable dt = 
				((AbstractBookSeriesAdv) book.getBookSeries()).getDependencyTable();
		final Set<Ref> precedents = ((DependencyTableAdv)dt).getDirectPrecedents(dependent);
		dt.clearDependents(dependent);
		
		for (CellRegion regn : delRegions) {
			this._regions.remove(regn);
			precedents.remove(newDummyRef(regn));
		}
		
		// restore dependent precedents relation
		if (precedents != null) {
			for (Ref precedent: precedents) {
				dt.add(dependent, precedent);
			}
		}
		
		// add new split regions
		for (CellRegion regn : newRegions) {
			this._regions.add(regn);
			dt.add(dependent, newDummyRef(regn));
		}
		
		if (this._regions.isEmpty()) {
			this._regions = null;
		}
		
		ModelUpdateUtil.addRefUpdate(dependent);
	}
	
	@Override
	public void setRegions(Set<CellRegion> regions) {
		_regions = new HashSet<CellRegion>(regions.size() * 4 / 3 + 1);
		for (CellRegion rgn : regions) {
			addRegion(rgn);
		}
	}

	@Override
	public ValidationType getValidationType() {
		return _validationType;
	}

	@Override
	public void setValidationType(ValidationType type) {
		Validations.argNotNull(type);
		_validationType = type;
	}

	@Override
	public OperatorType getOperatorType() {
		return _operatorType;
	}

	@Override
	public void setOperatorType(OperatorType type) {
		Validations.argNotNull(type);
		_operatorType = type;
	}

	@Override
	public boolean isFormulaParsingError() {
		boolean r = false;
		if(_formula1Expr!=null){
			r |= _formula1Expr.hasError();
		}
		if(!r && _formula2Expr!=null){
			r |= _formula2Expr.hasError();
		}
		return r;
	}

	@Override
	public int getNumOfValue(){
		return getNumOfValue1();
	}
	@Override
	public Object getValue(int index) {
		return getValue1(index);
	}
	@Override
	public int getNumOfValue1(){
		evalFormula();
		return EvaluationUtil.sizeOf(_evalValue1Result);
	}
	@Override
	public Object getValue1(int index) {
		evalFormula();
		if(index>=EvaluationUtil.sizeOf(_evalValue1Result)){
			return null;
		}
		return EvaluationUtil.valueOf(_evalValue1Result,index);
	}
	
	@Override
	public int getNumOfValue2(){
		evalFormula();
		return EvaluationUtil.sizeOf(_evalValue2Result);
	}
	@Override
	public Object getValue2(int index) {
		evalFormula();
		if(index>=EvaluationUtil.sizeOf(_evalValue2Result)){
			return null;
		}
		return EvaluationUtil.valueOf(_evalValue2Result,index);
	}

	@Override
	public String getFormula1() {
		return _unescapeFromPoi(_formula1Expr); // ZSS-978, ZSS-994
	}

	@Override
	public String getFormula2() {
		return _unescapeFromPoi(_formula2Expr); // ZSS-978, ZSS-994
	}

	private void clearFormulaDependency(boolean all) { // ZSS-648
		if(_formula1Expr!=null || _formula2Expr!=null){
			Ref dependent = getRef();
			DependencyTable dt = 
			((AbstractBookSeriesAdv) _sheet.getBook().getBookSeries())
					.getDependencyTable();
			
			dt.clearDependents(dependent);
			
			// ZSS-648
			// must keep the region reference itself in DependencyTable; so add it back
			if (!all && this._regions != null) {
				for (CellRegion regn : this._regions) {
					dt.add(dependent, newDummyRef(regn));
				}
			}
		}
	}
	
	private Ref getRef(){
		return new ObjectRefImpl(this,_id);
	}
	
	// ZSS-648
	private Ref getRef(String sheetName) {
		return new ObjectRefImpl(_sheet.getBook().getBookName(), sheetName, _id, ObjectType.DATA_VALIDATION);
	}
	
	// ZSS-648
	private Ref newDummyRef(CellRegion regn) {
		return new RefImpl(_sheet.getBook().getBookName(), _sheet.getSheetName(), 
				regn.row, regn.column, regn.lastRow, regn.lastColumn);
	}
	
	// ZSS-648
	private Ref newDummyRef(String sheetName, CellRegion regn) {
		return new RefImpl(_sheet.getBook().getBookName(), sheetName, 
				regn.row, regn.column, regn.lastRow, regn.lastColumn);
	}
	
	@Override
	public void setFormula1(String formula1) {
		formula1 = _escapeToPoi(formula1);
		setEscapedFormulas(formula1, getEscapedFormula2());
	}
	
	@Override
	public void setFormula2(String formula2) {
		formula2 = _escapeToPoi(formula2);
		setEscapedFormulas(getEscapedFormula1(), formula2);
	}

	//ZSS-978
	private boolean isLiteralPtg(Ptg ptg) {
		return ptg instanceof BoolPtg
				|| ptg instanceof IntPtg 
				|| ptg instanceof NumberPtg
				|| ptg instanceof StringPtg
				|| ptg instanceof ErrPtg;
	}
	
	//ZSS-866
	//20150108, henrichen: DataValidation's formula must be unescaped from
	//    POI before used in API.
	// For LIST validation type
	// * "xyz" embraced with double quote is a literal string. Should remove 
	//   the embraced double quote(") and unescape adjacent double quote to only 
	//   one double quote;
	//   e.g. """8%"", ""9%"", ""10%""" => "8%", "9%", "10%" 
	// * xyz not embraced with double quote is deemed as a formula. Should lead
	//   the string with a equals sign as =xyz.
	private String _unescapeFromPoi(FormulaExpression expr) { //ZSS-978, ZSS-994
		if (expr == null) return null;
		String formula = expr.getFormulaString();
		Ptg[] ptgs = expr.getPtgs();
		if (Strings.isBlank(formula)) return null;
		final StringBuilder sb = new StringBuilder();
		if (!formula.startsWith("\"") && formula.length() > 1) {
			//ZSS-978
			if (ptgs.length > 1 || !isLiteralPtg(ptgs[0]))
				return sb.append("=").append(formula).toString(); //leading with '='  
		}
		if (_validationType == ValidationType.LIST) {
			// skip first double quote and last double quote
			int pre = -2;
			for (int j = 1; j < formula.length() - 1; ++j) { 
				char ch = formula.charAt(j);
				if (ch == '"') {
					if (pre == j - 1) { // adjacent double quote; skip 2nd one
						continue;
					}
					pre = j;
				}
				sb.append(ch);
			}
			return sb.toString();
		}
		//ZSS-994
		if (isNumber()) {
			if (isDateFormat()) {
				final double val = Double.parseDouble(formula);
				final Locale locale = ZssContext.getCurrent().getLocale();
				return FormatEngineImpl.getDateTimeString(val, locale);
			}
			return formula;
		}
		return null;
	}

	//ZSS-994
	private boolean isNumber() {
		return _validationType == ValidationType.DECIMAL
				|| _validationType == ValidationType.INTEGER
				|| _validationType == ValidationType.TEXT_LENGTH
				|| isDateFormat();
	}
	// ZSS-994
	private boolean isDateFormat() {
		return _validationType == ValidationType.DATE
				|| _validationType == ValidationType.TIME;
	}
	
	//ZSS-866, ZSS-994
	//20150108, henrichen: DataValidation's formula must be escaped before 
	// store into POI
	// For LIST validation type
	//  * =xyz formula, remove the equal sign and make it as xyz
	//  * xyz, embrace with double quote(") to make it as "xyz". If contents 
	//    contains double quote, should escape it by repeat one more double 
	//    quote;
	//  e.g. "8%", "9%", "10%" => """8%"", ""9%"", ""10%"""
	private String _escapeToPoi(String formula) {
		if (Strings.isBlank(formula))
			return null;
		InputResult input = parseInput(formula);
		switch (input.getType()) {
		case FORMULA:
			return formula.substring(1);
		case STRING:
			if (_validationType == ValidationType.LIST) {
				StringBuilder sb = new StringBuilder();
				sb.append('"');
				for (int j = 0; j < formula.length(); ++j) {
					char ch = formula.charAt(j);
					sb.append(ch);
					if (ch == '"') {
						sb.append('"');
					}
				}
				sb.append('"');
				return sb.toString();
			}
			return formula;
		case NUMBER:
			final Object val = input.getValue();
			double num = val instanceof Date ? EngineFactory.getInstance()
					.getCalendarUtil().dateToDoubleValue((Date) val)
					: ((Number) val).doubleValue();
			return Double.toString(num);
		default:
			return formula;
		}
	}

	// ZSS-994
	private InputResult parseInput(String formula) {
		final InputEngine ie = EngineFactory.getInstance().createInputEngine();
		return ie.parseInput(formula == null ? "" : formula,
				SCellStyle.FORMAT_GENERAL, new InputParseContext(ZssContext
						.getCurrent().getLocale()));
	}

	// ZSS-994
	private FormulaExpression parseFormula(String formula) {
		FormulaEngine fe = EngineFactory.getInstance().createFormulaEngine();
		Ref ref = getRef();
		final Locale locale = ZssContext.getCurrent().getLocale();
		final FormulaParseContext formulaCtx = new FormulaParseContext(
				_sheet.getBook(), _sheet, null/* SCell */, _sheet.getSheetName(),
				ref, locale);
		final FormulaExpression expr = fe.parse(formula, formulaCtx);
		if (expr.hasError()) {
			String msg = expr.getErrorMessage();
			throw new InvalidFormulaException(msg == null ? "The formula ="
					+ formula + " contains error" : msg);
		}
		return expr;
	}
	
	@Override
	public void setFormulas(String formula1, String formula2) {
		formula1 = _escapeToPoi(formula1);
		formula2 = _escapeToPoi(formula2);
		setEscapedFormulas(formula1, formula2);
	}

	@Override
	public void setEscapedFormulas(String formula1, String formula2) {
		checkOrphan();
		_evaluated = false;
		clearFormulaDependency(false); // will clear formula
		clearFormulaResultCache();

		if (formula1 != null) {
			_formula1Expr = parseFormula(formula1); // ZSS-994
		} else {
			_formula1Expr = null;
		}

		if (formula2 != null) {
			_formula2Expr = parseFormula(formula2); // ZSS-994
		} else {
			_formula2Expr = null;
		}
	}

	@Override
	public void clearFormulaResultCache() {
		_evaluated = false;
		_evalValue1Result = _evalValue2Result = null;
		_evalValue1EvalResult = _evalValue2EvalResult = null; //ZSS-834
	}
	
	/*package*/ void evalFormula(){
		//20140731, henrichen: when share the same book, many users might 
		//populate DataValidationImpl simultaneously; must synchronize it.
		if(_evaluated) return;
		synchronized (this) {
			if(!_evaluated){
				Ref ref = getRef();
				FormulaEngine fe = EngineFactory.getInstance().createFormulaEngine();
				if(_formula1Expr!=null){
					EvaluationResult result = fe.evaluate(_formula1Expr,new FormulaEvaluationContext(_sheet,ref));
	
					Object val = result.getValue();
					if(result.getType() == ResultType.SUCCESS){
						_evalValue1Result = val;
						_evalValue1EvalResult = result.getValueEval(); //ZSS-810
						_evalValue1Result = processCommaLiteral(_evalValue1Result, _evalValue1EvalResult ); //ZSS-809
					}else if(result.getType() == ResultType.ERROR){
						_evalValue1Result = (val instanceof ErrorValue)?val: ErrorValue.valueOf(ErrorValue.INVALID_VALUE);
					}
					
				}
				if(_formula2Expr!=null){
					EvaluationResult result = fe.evaluate(_formula2Expr,new FormulaEvaluationContext(_sheet,ref));
	
					Object val = result.getValue();
					if(result.getType() == ResultType.SUCCESS){
						_evalValue2Result = val;
						_evalValue2EvalResult = result.getValueEval(); //ZSS-810
						_evalValue2Result = processCommaLiteral(_evalValue2Result, _evalValue2EvalResult); //ZSS-809
					}else if(result.getType() == ResultType.ERROR){
						_evalValue2Result = (val instanceof ErrorValue)?val: ErrorValue.valueOf(ErrorValue.INVALID_VALUE);
					}
					
				}
				_evaluated = true;
			}
		}
	}
	
	@Override
	public List<SCell> getReferToCellList(){
		if(_formula1Expr!=null && _formula1Expr.isAreaRefs()){
			Ref areaRef = _formula1Expr.getAreaRefs()[0];
			return getReferToCellList0(areaRef);
		} else if (_evalValue1EvalResult instanceof LazyAreaEval) { //ZSS-810
			LazyAreaEval areaEval = (LazyAreaEval) _evalValue1EvalResult;
			return getReferToCellList0(areaEval.getBookName(), areaEval.getSheetName(), 
					areaEval.getFirstRow(), areaEval.getFirstColumn(), 
					areaEval.getLastRow(), areaEval.getLastColumn());
		} else if (_evalValue1EvalResult instanceof LazyRefEval) { //ZSS-810
			LazyRefEval areaEval = (LazyRefEval) _evalValue1EvalResult;
			return getReferToCellList0(areaEval.getBookName(), areaEval.getSheetName(), 
					areaEval.getRow(), areaEval.getColumn(), 
					areaEval.getRow(), areaEval.getColumn());
		}
		return Collections.emptyList();
	}
	
	private List<SCell> getReferToCellList0(Ref areaRef) {
		SBookSeries bookSeries = _sheet.getBook().getBookSeries(); 
		List<SCell> list = new LinkedList<SCell>();
		String bookName =  areaRef.getBookName();
		String sheetName = areaRef.getSheetName();
		CellRegion region = new CellRegion(areaRef.getRow(),areaRef.getColumn(),areaRef.getLastRow(),areaRef.getLastColumn());
		
		SBook book = bookSeries.getBook(bookName);
		if(book==null){
			return list;
		}
		SSheet sheet = book.getSheetByName(sheetName);
		if(sheet==null){
			return list;
		}
		for(int i = region.getRow();i<=region.getLastRow();i++){
			for(int j=region.getColumn();j<=region.getLastColumn();j++){
				list.add(sheet.getCell(i, j));
			}
		}
		return list;
	}

	@Override
	public boolean hasReferToCellList() {
		return (_formula1Expr!=null && _formula1Expr.isAreaRefs())
				|| (_evalValue1EvalResult instanceof LazyAreaEval) //ZSS-810
				|| (_evalValue1EvalResult instanceof LazyRefEval); //ZSS-810
	}

	@Override
	void copyFrom(AbstractDataValidationAdv src) {
		Validations.argInstance(src, DataValidationImpl.class);
		DataValidationImpl srcImpl = (DataValidationImpl)src;
		_alertStyle = srcImpl._alertStyle;
		_ignoreBlank = srcImpl._ignoreBlank;
		_showInCellDropdown = srcImpl._showInCellDropdown;
		_showInput = srcImpl._showInput;
		_showError = srcImpl._showError;
		_inputTitle = srcImpl._inputTitle;
		_inputMessage = srcImpl._inputMessage;
		_errorTitle = srcImpl._errorTitle;
		_errorMessage = srcImpl._errorMessage;
		_validationType = srcImpl._validationType;
		_operatorType = srcImpl._operatorType;
		
		if(srcImpl._formula1Expr!=null){
			setEscapedFormulas(srcImpl.getEscapedFormula1(), 
					srcImpl.getEscapedFormula2());
		}
	}
	
	@Override
	void renameSheet(String oldName, String newName) { //ZSS-648
		Validations.argNotNull(oldName);
		Validations.argNotNull(newName);
		if (oldName.equals(newName)) return; // nothing change, let go
		
		// remove old ObjectRef
		Ref dependent = getRef(oldName);
		SBook book = _sheet.getBook();
		final DependencyTable dt = 
				((AbstractBookSeriesAdv) book.getBookSeries()).getDependencyTable();
		final Set<Ref> precedents = ((DependencyTableAdv)dt).getDirectPrecedents(dependent);
		if (precedents != null && this._regions != null) {
			for (CellRegion regn : this._regions) {
				precedents.remove(newDummyRef(oldName, regn));
			}
		}
		dt.clearDependents(dependent);
		
		// Add new ObjectRef into DependencyTable so we can extend/shrink/move
		dependent = getRef(newName);  // new dependent because region have changed
		
		// ZSS-648
		// prepare new dummy CellRef to enforce DataValidation reference dependency
		if (this._regions != null) {
			for (CellRegion regn : this._regions) {
				dt.add(dependent, newDummyRef(newName, regn));
			}
		}
		
		// restore dependent precedents relation
		if (precedents != null) {
			for (Ref precedent: precedents) {
				dt.add(dependent, precedent);
			}
		}
	}

	//ZSS-688
	//@since 3.6.0
	/*package*/ DataValidationImpl cloneDataValidationImpl(AbstractSheetAdv sheet) {
		DataValidationImpl tgt = new DataValidationImpl(sheet, this._id); 

		tgt._alertStyle = this._alertStyle;
		tgt._ignoreBlank = this._ignoreBlank;
		tgt._showInCellDropdown = this._showInCellDropdown;
		tgt._showInput = this._showInput;
		tgt._showError = this._showError;
		tgt._inputTitle = this._inputTitle;
		tgt._inputMessage = this._inputMessage;
		tgt._errorTitle = this._errorTitle;
		tgt._errorMessage = this._errorMessage;
		tgt._validationType = this._validationType;
		tgt._operatorType = this._operatorType;

		if (this._regions != null) {
			tgt._regions = new HashSet<CellRegion>(this._regions.size() * 4 / 3);
			for (CellRegion rgn : this._regions) {
				tgt._regions.add(new CellRegion(rgn.row, rgn.column, rgn.lastRow, rgn.lastColumn));
			}
		}
		
		final String f1 = getEscapedFormula1();
		if (f1 != null) {
			final String f2 = getEscapedFormula2();
			setEscapedFormulas(f1, f2);
		}

		// Do NOT clone _evalValue1Result, _evalValue2Result, and _evaluated
		//private Object _evalValue1Result;
		//private Object _evalValue2Result;
		//private boolean _evaluated;
		
		return tgt;
	}
	
	//ZSS-747
	/**
	 * 
	 * @param fe1
	 * @param fe2
	 * @since 3.6.0
	 */
	public void setFormulas(FormulaExpression fe1, FormulaExpression fe2) {
		checkOrphan();
		_evaluated = false;
		clearFormulaDependency(false); // will clear formula
		clearFormulaResultCache();
		
		_formula1Expr = fe1;
		_formula2Expr = fe2;

		// update dependency table
		FormulaEngine fe = EngineFactory.getInstance().createFormulaEngine();
		
		Ref ref = getRef();
		FormulaParseContext context = new FormulaParseContext(_sheet,ref);
		
		if(fe1 != null) {
			fe.updateDependencyTable(fe1, context);
		}
		if(fe2 != null) {
			fe.updateDependencyTable(fe2, context);
		}
	}

	//ZSS-747
	/**
	 * 
	 * @return
	 * @since 3.6.0
	 */
	public FormulaExpression getFormulaExpression1() {
		return _formula1Expr;
	}
	//ZSS-747
	/**
	 * 
	 * @return
	 * @since 3.6.0
	 */
	public FormulaExpression getFormulaExpression2() {
		return _formula2Expr;
	}
	//ZSS-747
	/**
	 * 
	 * @param formula
	 * @since 3.6.0
	 */
	public void setFormula1(FormulaExpression formula1) {
		setFormulas(formula1, _formula2Expr);
	}
	//ZSS-747
	/**
	 * 
	 * @param formula
	 * @since 3.6.0
	 */
	public void setFormula2(FormulaExpression formula2) {
		setFormulas(_formula1Expr, formula2);
	}
	
	//ZSS-810
	public ValueEval getValueEval1() { 	
		return _evalValue1EvalResult;
	}
	
	//ZSS-810
	public ValueEval getValueEval2() {
		return _evalValue2EvalResult;
	}
	
	//ZSS-809
	private Object processCommaLiteral(Object value, ValueEval valueEval) {
		if (value != null && valueEval instanceof StringEval) {
			// YES! must be comma separated and filter out spece
			final String[] strs = ((String)value).split(","); 
			List<String> stra = new ArrayList<String>(strs.length);
			for (String s : strs) {
				s = s.trim();
				if (s.length() == 0) continue;
				stra.add(s);
			}
			return stra.size() <= 1 ? value : stra;
		}
		return value;
	}

	//ZSS-833
	@Override
	public boolean hasReferToRelativeCol() {
		evalFormula();
		if (hasReferToCellList()) {
			if (_evalValue1EvalResult instanceof LazyAreaEval) {
				return ((LazyAreaEval)_evalValue1EvalResult).isFirstColRelative()
						|| ((LazyAreaEval)_evalValue1EvalResult).isLastColRelative();
			} else if (_evalValue1EvalResult instanceof LazyRefEval) {
				return ((LazyRefEval)_evalValue1EvalResult).isColRelative(); 
			}
		}
		return false;
	}

	//ZSS-833
	@Override
	public boolean hasReferToRelativeRow() {
		evalFormula();
		if (hasReferToCellList()) {
			if (_evalValue1EvalResult instanceof LazyAreaEval) {
				return ((LazyAreaEval)_evalValue1EvalResult).isFirstRowRelative()
						|| ((LazyAreaEval)_evalValue1EvalResult).isLastRowRelative();
			} else if (_evalValue1EvalResult instanceof LazyRefEval) {
				return ((LazyRefEval)_evalValue1EvalResult).isRowRelative(); 
			}
		}
		return false;
	}

	//ZSS-833
	@Override
	public List<SCell> getReferToCellList(int row, int col) {
		if (!hasReferToRelativeCol() && !hasReferToRelativeRow())
			return getReferToCellList();
		if (_regions.isEmpty()) {
			return Collections.emptyList();
		}
		CellRegion rgn = _regions.iterator().next();
		int colDiff = col - rgn.getColumn(), rowDiff = row - rgn.getRow();
		
//		if(_formula1Expr!=null && _formula1Expr.isAreaRefs()){
//			Ref areaRef = _formula1Expr.getAreaRefs()[0];
//			return getReferToCellList0(areaRef);
//		} else 
		if (_evalValue1EvalResult instanceof LazyAreaEval) { //ZSS-810
			LazyAreaEval areaEval = (LazyAreaEval) _evalValue1EvalResult;
			final int row1 = areaEval.getFirstRow() + (areaEval.isFirstRowRelative() ? rowDiff : 0);
			final int row2 = areaEval.getLastRow() + (areaEval.isLastRowRelative() ? rowDiff : 0);
			final int col1 = areaEval.getFirstColumn() + (areaEval.isFirstColRelative() ? colDiff : 0);
			final int col2 = areaEval.getLastColumn() + (areaEval.isLastColRelative() ? colDiff : 0);
			if (row1 >= 0 && row2 >= 0 && col1 >= 0 && col2 >= 0) {
				return getReferToCellList0(areaEval.getBookName(), areaEval.getSheetName(), row1, col1, row2, col2);
			}
		} else if (_evalValue1EvalResult instanceof LazyRefEval) { //ZSS-810
			LazyRefEval areaEval = (LazyRefEval) _evalValue1EvalResult;
			final int row1 = areaEval.getRow() + (areaEval.isRowRelative() ? rowDiff : 0);
			final int col1 = areaEval.getColumn() + (areaEval.isColRelative() ? colDiff : 0);
			if (row1 >= 0 && col1 >= 0) {
				return getReferToCellList0(areaEval.getBookName(), areaEval.getSheetName(), row1, col1, row1, col1);
			}
		}
		return Collections.emptyList();
	}

	//ZSS-833
	private List<SCell> getReferToCellList0(String bookName, String sheetName, int row1, int col1, int row2, int col2) {
		SBookSeries bookSeries = _sheet.getBook().getBookSeries(); 
		List<SCell> list = new LinkedList<SCell>();
		
		SBook book = bookSeries.getBook(bookName);
		if(book==null){
			return list;
		}
		SSheet sheet = book.getSheetByName(sheetName);
		if(sheet==null){
			return list;
		}
		for(int i = row1; i<=row2; i++){
			for(int j = col1; j <= col2; ++j) {
				list.add(sheet.getCell(i, j));
			}
		}
		return list;
	}
	
	//ZSS-833
	@Override
	public void addDependency(int row, int col) {
		if (!hasReferToRelativeCol() && !hasReferToRelativeRow())
			return;
		if (_regions.isEmpty()) {
			return;
		}
		CellRegion rgn = _regions.iterator().next();
		int colDiff = col - rgn.getColumn(), rowDiff = row - rgn.getRow();
		
		if (_evalValue1EvalResult instanceof LazyAreaEval) { //ZSS-810
			LazyAreaEval areaEval = (LazyAreaEval) _evalValue1EvalResult;
			final int row1 = areaEval.getFirstRow() + (areaEval.isFirstRowRelative() ? rowDiff : 0);
			final int row2 = areaEval.getLastRow() + (areaEval.isLastRowRelative() ? rowDiff : 0);
			final int col1 = areaEval.getFirstColumn() + (areaEval.isFirstColRelative() ? colDiff : 0);
			final int col2 = areaEval.getLastColumn() + (areaEval.isLastColRelative() ? colDiff : 0);
			if (row1 >= 0 && row2 >= 0 && col1 >= 0 && col2 >= 0) {
				addDependency(areaEval.getBookName(), areaEval.getSheetName(), row1, col1, row2, col2);
			}
		} else if (_evalValue1EvalResult instanceof LazyRefEval) { //ZSS-810
			LazyRefEval areaEval = (LazyRefEval) _evalValue1EvalResult;
			final int row1 = areaEval.getRow() + (areaEval.isRowRelative() ? rowDiff : 0);
			final int col1 = areaEval.getColumn() + (areaEval.isColRelative() ? colDiff : 0);
			if (row1 >= 0 && col1 >= 0) {
				addDependency(areaEval.getBookName(), areaEval.getSheetName(), row1, col1, row1, col1);
			}
		}
	}
	
	//ZSS-833
	private void addDependency(String bookName, String sheetName, int row1, int col1, int row2, int col2) {
		SBookSeries bookSeries = _sheet.getBook().getBookSeries(); 
		SBook book = bookSeries.getBook(bookName);
		if(book==null){
			return;
		}
		SSheet sheet = book.getSheetByName(sheetName);
		if(sheet==null){
			return;
		}
		DependencyTable table = ((AbstractBookSeriesAdv)bookSeries).getDependencyTable();
		table.add(getRef(), new RefImpl(bookName, sheetName, row1, col1, row2, col2));
	}
	
	//ZSS-866
	@Override
	public String getEscapedFormula1() {
		return _formula1Expr == null ? null : _formula1Expr.getFormulaString();
	}
	
	//ZSS-866
	@Override
	public String getEscapedFormula2() {
		return _formula2Expr == null ? null : _formula2Expr.getFormulaString();
	}
}
