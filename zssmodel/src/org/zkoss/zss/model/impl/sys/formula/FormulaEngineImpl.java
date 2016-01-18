/* ZPOIEngine.java

	Purpose:
		
	Description:
		
	History:
		Dec 10, 2013 Created by Pao Wang

Copyright (C) 2013 Potix Corporation. All Rights Reserved.
 */
package org.zkoss.zss.model.impl.sys.formula;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zkoss.poi.ss.formula.CollaboratingWorkbooksEnvironment;
import org.zkoss.poi.ss.formula.DependencyTracker;
import org.zkoss.poi.ss.formula.EvaluationCell;
import org.zkoss.poi.ss.formula.EvaluationSheet;
import org.zkoss.poi.ss.formula.EvaluationWorkbook;
import org.zkoss.poi.ss.formula.EvaluationWorkbook.ExternalSheet;
import org.zkoss.poi.ss.formula.ExternSheetReferenceToken;
import org.zkoss.poi.ss.formula.FormulaParseException;
import org.zkoss.poi.ss.formula.FormulaParser;
import org.zkoss.poi.ss.formula.FormulaParsingWorkbook;
import org.zkoss.poi.ss.formula.FormulaRenderer;
import org.zkoss.poi.ss.formula.FormulaType;
import org.zkoss.poi.ss.formula.IStabilityClassifier;
import org.zkoss.poi.ss.formula.PtgShifter;
import org.zkoss.poi.ss.formula.WorkbookEvaluator;
import org.zkoss.poi.ss.formula.eval.AreaEval;
import org.zkoss.poi.ss.formula.eval.BlankEval;
import org.zkoss.poi.ss.formula.eval.BoolEval;
import org.zkoss.poi.ss.formula.eval.ErrorEval;
import org.zkoss.poi.ss.formula.eval.EvaluationException;
import org.zkoss.poi.ss.formula.eval.NotImplementedException;
import org.zkoss.poi.ss.formula.eval.NumberEval;
import org.zkoss.poi.ss.formula.eval.RefEval;
import org.zkoss.poi.ss.formula.eval.StringEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.poi.ss.formula.eval.ValuesEval;
import org.zkoss.poi.ss.formula.function.FunctionMetadataRegistry;
import org.zkoss.poi.ss.formula.ptg.AbstractFunctionPtg;
import org.zkoss.poi.ss.formula.ptg.Area3DPtg;
import org.zkoss.poi.ss.formula.ptg.AreaPtg;
import org.zkoss.poi.ss.formula.ptg.AreaPtgBase;
import org.zkoss.poi.ss.formula.ptg.FuncPtg;
import org.zkoss.poi.ss.formula.ptg.NamePtg;
import org.zkoss.poi.ss.formula.ptg.NameXPtg;
import org.zkoss.poi.ss.formula.ptg.ParenthesisPtg;
import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.poi.ss.formula.ptg.Ref3DPtg;
import org.zkoss.poi.ss.formula.ptg.RefPtg;
import org.zkoss.poi.ss.formula.ptg.RefPtgBase;
import org.zkoss.poi.ss.formula.ptg.TablePtg;
import org.zkoss.poi.ss.formula.ptg.TablePtg.Item;
import org.zkoss.poi.ss.formula.udf.UDFFinder;
import org.zkoss.poi.xssf.model.IndexedUDFFinder;
import org.zkoss.util.logging.Log;
import org.zkoss.xel.FunctionMapper;
import org.zkoss.xel.VariableResolver;
import org.zkoss.xel.XelContext;
import org.zkoss.xel.util.SimpleXelContext;
import org.zkoss.zss.model.ErrorValue;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.STable;
import org.zkoss.zss.model.SheetRegion;
import org.zkoss.zss.model.impl.AbstractBookAdv;
import org.zkoss.zss.model.impl.AbstractBookSeriesAdv;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.impl.ColumnPrecedentRefImpl;
import org.zkoss.zss.model.impl.ColumnRefImpl;
import org.zkoss.zss.model.impl.IndirectRefImpl;
import org.zkoss.zss.model.impl.NameRefImpl;
import org.zkoss.zss.model.impl.NonSerializableHolder;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.impl.TablePrecedentRefImpl;
import org.zkoss.zss.model.impl.sys.DependencyTableAdv;
import org.zkoss.zss.model.sys.dependency.ColumnPrecedentRef;
import org.zkoss.zss.model.sys.dependency.ColumnRef;
import org.zkoss.zss.model.sys.dependency.DependencyTable;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.dependency.Ref.RefType;
import org.zkoss.zss.model.sys.dependency.TablePrecedentRef;
import org.zkoss.zss.model.sys.formula.EvaluationResult;
import org.zkoss.zss.model.sys.formula.EvaluationResult.ResultType;
import org.zkoss.zss.model.sys.formula.FormulaClearContext;
import org.zkoss.zss.model.sys.formula.FormulaEngine;
import org.zkoss.zss.model.sys.formula.FormulaEvaluationContext;
import org.zkoss.zss.model.sys.formula.FormulaExpression;
import org.zkoss.zss.model.sys.formula.FormulaParseContext;
import org.zkoss.zss.model.sys.formula.FunctionResolver;
import org.zkoss.zss.model.sys.formula.FunctionResolverFactory;

/**
 * A formula engine implemented by ZPOI
 * @author Pao
 */
public class FormulaEngineImpl implements FormulaEngine {

	public final static String KEY_EVALUATORS = "$ZSS_EVALUATORS$";

	private static final Log _logger = Log.lookup(FormulaEngineImpl.class.getName());

	private Map<EvaluationWorkbook, XelContext> _xelContexts = new HashMap<EvaluationWorkbook, XelContext>();
	
	// for POI formula evaluator
	protected final static IStabilityClassifier noCacheClassifier = new IStabilityClassifier() {
		public boolean isCellFinal(int sheetIndex, int rowIndex, int columnIndex) {
			return true;
		}
	};
	
	private static Pattern _areaPattern = Pattern.compile("\\([.[^\\(\\)]]*\\)");//match (A1,B1,C1)
	private static Pattern _searchPattern = Pattern.compile("\\s*((?:(?:'[^!\\(]+'!)|(?:[^'!,\\(]+!))?(?:[$\\w]+:)?[$\\w]+)"); // for search area reference 
	
	private static boolean isMultipleAreaFormula(String formula){
		return formula.split(",").length > 1 && _areaPattern.matcher(formula).matches(); //ZSS-847
	}
	
	private String[] unwrapeAreaFormula(String formula){
		List<String> areaStrings = new ArrayList<String>();
		Matcher m = _searchPattern.matcher(formula);
		while(m.find()) {
			areaStrings.add(m.group(1));
		}
		return areaStrings.toArray(new String[0]);
	}


	private FormulaExpression parseMultipleAreaFormula(String formula, FormulaParseContext context) {
		if(!isMultipleAreaFormula(formula)){
			return null;
		}
		FormulaExpression[] result = parse0(unwrapeAreaFormula(formula),context, true);
		
		//ZSS-747
		List<Ptg> tokens = new ArrayList<Ptg>(result.length + 2);
		List<Ref> areaRefs = new LinkedList<Ref>(); 
		for(FormulaExpression expr : result){
			if(expr.hasError()){
				return new FormulaExpressionImpl(formula, null, null, true, expr.getErrorMessage(), true);
			}
			for(Ref ref:expr.getAreaRefs()){
				areaRefs.add(ref);
			}
			Ptg[] ptgs = expr.getPtgs();
			for (int k = 0; k < ptgs.length; ++k) {
				tokens.add(ptgs[k]);
			}
		}
		tokens.add(new ParenthesisPtg(tokens.size()));
		Ptg[] ptgArray = tokens.toArray(new Ptg[tokens.size()]);
		String renderedFormula = renderFormula(new ParsingBook(context.getBook()), formula, ptgArray, true);
		return new FormulaExpressionImpl(renderedFormula, ptgArray, areaRefs.toArray(new Ref[areaRefs.size()]), false, null, true);
	}
	
	//setEditText("=xxx") call into this method
	@Override
	public FormulaExpression parse(String formula, FormulaParseContext context) {
		formula = formula.trim();
		FormulaExpression expr = parseMultipleAreaFormula(formula,context);
		if(expr!=null){
			return expr;
		}
		expr = parse0(new String[]{formula}, context, false)[0];
		if (expr.hasError()) {
			return expr;
		}
		Ptg[] ptgArray = expr.getPtgs();
		Ref[] refArray = expr.getAreaRefs();
		String renderedFormula = renderFormula(new ParsingBook(context.getBook()), expr.getFormulaString(), ptgArray, true);
		return new FormulaExpressionImpl(renderedFormula, ptgArray, refArray, false, null, expr.isMultipleAreaFormula());

	}

	private FormulaExpression[] parse0(String[] formulas, FormulaParseContext context, boolean multipleArea) {
		LinkedList<FormulaExpression> result = new LinkedList<FormulaExpression>();
		Ref dependant = context.getDependent();
		LinkedList<Ref> precedents = dependant!=null?new LinkedList<Ref>():null;
		try {
			// adapt and parse
			SBook book = context.getBook();
			ParsingBook parsingBook = new ParsingBook(book);
			int sheetIndex = parsingBook.getExternalSheetIndex(null, context.getSheet().getSheetName());
			
			AbstractBookSeriesAdv series = (AbstractBookSeriesAdv)book.getBookSeries();
			DependencyTableAdv dt = (DependencyTableAdv)series.getDependencyTable();
			boolean error = false;
			
			for(String formula:formulas){
				try{
					Ptg[] tokens = parse(formula, parsingBook, sheetIndex, context); // current sheet index in parsing is always 0
					if(dependant!=null){
						for (int j = 0, len = tokens.length; j < len; ++j) {
							Ptg ptg = tokens[j];
							Ref precedent = toDependRef(context, parsingBook, ptg, j);
							if(precedent != null) {
								precedents.add(precedent);
								
								//ZSS-966
								if (precedent instanceof ColumnRef) {
									// TableName
									ColumnRef colRef = (ColumnRef) precedent;
									final String bookName = colRef.getBookName();
									final String tableName = colRef.getTableName();
									TablePrecedentRef tbPrecedent = new TablePrecedentRefImpl(bookName, tableName);
									precedents.add(tbPrecedent);
									
									//ZSS-967
									if (colRef.isWithHeaders()) {
										final String columnName1 = colRef.getColumnName1(); 
										if (columnName1 != null) {
											// columnName1
											ColumnPrecedentRef colPrecedent1 = new ColumnPrecedentRefImpl(bookName, tableName, columnName1);
											precedents.add(colPrecedent1);

											final STable table = ((AbstractBookAdv)book).getTable(tableName);
											final int rowHd = table.getHeadersRegion().getRow();
											final int col1 = colRef.getColumn();
											final String sheetName = table.getAllRegion().getSheet().getSheetName();
											final Ref cellRef1 = new RefImpl(bookName, sheetName, rowHd, col1);
											precedents.add(cellRef1);
										
											// columnName2
											final String columnName2 = colRef.getColumnName2();
											if (columnName2 != null) {
												ColumnPrecedentRef colPrecedent2 = new ColumnPrecedentRefImpl(bookName, tableName, columnName2);
												precedents.add(colPrecedent2);
												
												final int col2 = colRef.getLastColumn();
												final Ref cellRef2 = new RefImpl(bookName, sheetName, rowHd, col2);
												precedents.add(cellRef2);
											}
										}
									}
								}
							}
						}
					}
					
					// render formula, detect region and create result
					String renderedFormula = renderFormula(parsingBook, formula, tokens, true);
					Ref singleRef = tokens.length == 1 ? toDependRef(context, parsingBook, tokens[0], 0) : null;
					Ref[] refs = singleRef==null ? null :
						(singleRef.getType() == RefType.AREA || singleRef.getType() == RefType.CELL ?new Ref[]{singleRef}:null);
					result.add(new FormulaExpressionImpl(renderedFormula, tokens, refs, false, null, multipleArea));
					
					
				}catch(FormulaParseException e) {
					_logger.info(e.getMessage() + " when parsing " + formula + " at " + getReference(context));
					if (_logger.infoable()) {
						e.printStackTrace();
					}
					result.add(new FormulaExpressionImpl(formula, null, null, true, e.getMessage(), multipleArea));
					error = true;
				} catch(Exception e) {
					_logger.error(e.getMessage() + " when parsing " + formula + " at " + getReference(context), e);
					if (_logger.errorable()) {
						e.printStackTrace();
					}
					result.add(new FormulaExpressionImpl(formula, null, null, true, e.getMessage(), multipleArea));
					error = true;
				}
			}
			
			// dependency tracking if no error and necessary
			if(!error && dependant != null) {
				for(Ref precedent:precedents){
					dt.add(dependant, precedent);
				}
			}
		} catch(Exception e) {
			_logger.error(e.getMessage() + " when parsing " + Arrays.asList(formulas) + " at " + getReference(context), e);
			result.clear();
			result.add(new FormulaExpressionImpl(Arrays.asList(formulas).toString(), null, null, true,e.getMessage(), multipleArea));
		}
		return result.toArray(new FormulaExpression[result.size()]);
	}
	
	private String getReference(FormulaParseContext context) {
		return "["+context.getBook().getBookName()+"]"+context.getSheet().getSheetName()+"!"+context.getCell();
	}
	
	protected Ptg[] parse(String formula, FormulaParsingWorkbook book, int sheetIndex, FormulaParseContext context) {
		return FormulaParser.parse(formula, book, FormulaType.CELL, sheetIndex);
	}
	
	protected String renderFormula(ParsingBook parsingBook, String formula, Ptg[] tokens, boolean always) {
		return always ? 
			FormulaRenderer.toFormulaString(parsingBook, tokens) :
			FormulaRenderer.toFormulaEditText(parsingBook, tokens, formula);
	}

	protected Ref toDependRef(FormulaParseContext ctx, ParsingBook parsingBook, Ptg ptg, int ptgIndex) {
		try {
			SSheet sheet = ctx.getSheet();

			if(ptg instanceof NamePtg) {	// name range name
				NamePtg namePtg = (NamePtg)ptg;
				// use current book, we don't refer to other book's defined name
				String bookName = sheet.getBook().getBookName(); 
				String name = parsingBook.getNameText(namePtg);
				return new NameRefImpl(bookName, null, name); // assume name is book-scope
			} else if(ptg instanceof NameXPtg) { // user defined function name
				// TODO consider function-type dependency
			} else if(ptg instanceof FuncPtg) {
				// TODO consider function-type dependency
			} else if(ptg instanceof TablePtg) { //ZSS-855, ZSS-966 and ZSS-967: (side-effect of ZSS-1013) must before Area3DPtg and Ref3DPtg
				TablePtg tbPtg = (TablePtg)ptg;
				SBook book = sheet.getBook();
				String tbName = tbPtg.getTableName();
				STable tb = ((AbstractBookAdv)book).getTable(tbName);
				String columnName1 = tbPtg.getColumn1();
				String columnName2 = tbPtg.getColumn2();
				Item item1 = tbPtg.getItem1();
				Item item2 = tbPtg.getItem2();
				SSheet srcSheet = tb.getAllRegion().getSheet();
				String sheetName = srcSheet.getSheetName();
				String bookName = book.getBookName();
				return new ColumnRefImpl(bookName, sheetName, tbName, item1, item2, 
						columnName1, columnName2, tb.getHeaderRowCount() > 0,
						tbPtg.getFirstRow(), tbPtg.getFirstColumn(),
						tbPtg.getLastRow(), tbPtg.getLastColumn());
			} else if(ptg instanceof Ref3DPtg) {
				Ref3DPtg rptg = (Ref3DPtg)ptg;
				// might be internal or external book reference
				ExternalSheet es = parsingBook.getAnyExternalSheet(rptg.getExternSheetIndex());
				String bookName = es.getWorkbookName() != null ? es.getWorkbookName() : sheet.getBook().getBookName();
				String sheetName = es.getSheetName();
				String lastSheetName = es.getLastSheetName().equals(sheetName) ? null : es.getLastSheetName();
				return new RefImpl(bookName, sheetName, lastSheetName, rptg.getRow(), rptg.getColumn());
			} else if(ptg instanceof Area3DPtg) {
				Area3DPtg aptg = (Area3DPtg)ptg;
				// might be internal or external book reference
				ExternalSheet es = parsingBook.getAnyExternalSheet(aptg.getExternSheetIndex());
				String bookName = es.getWorkbookName() != null ? es.getWorkbookName() : sheet.getBook().getBookName();
				String sheetName = es.getSheetName();
				String lastSheetName = es.getLastSheetName().equals(sheetName) ? null : es.getLastSheetName();
				return new RefImpl(bookName, sheetName, lastSheetName, aptg.getFirstRow(),
						aptg.getFirstColumn(), aptg.getLastRow(), aptg.getLastColumn());
			} else if(ptg instanceof RefPtg) {
				RefPtg rptg = (RefPtg)ptg;
				String bookName = sheet.getBook().getBookName();
				String sheetName = sheet.getSheetName();
				return new RefImpl(bookName, sheetName, rptg.getRow(), rptg.getColumn());
			} else if(ptg instanceof AreaPtg) {
				AreaPtg aptg = (AreaPtg)ptg;
				String sheetName = sheet.getSheetName();
				String bookName = sheet.getBook().getBookName();
				return new RefImpl(bookName, sheetName, aptg.getFirstRow(), aptg.getFirstColumn(),
						aptg.getLastRow(), aptg.getLastColumn());
			} else if(ptg instanceof AbstractFunctionPtg //ZSS-845
				&& ((AbstractFunctionPtg)ptg).getFunctionIndex() == FunctionMetadataRegistry.FUNCTION_INDEX_INDIRECT) {
				String sheetName = sheet.getSheetName();
				String bookName = sheet.getBook().getBookName();
				return new IndirectRefImpl(bookName, sheetName, ptgIndex);
			}
		} catch(Exception e) {
			_logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	@Override
	public EvaluationResult evaluate(FormulaExpression expr, FormulaEvaluationContext context) {

		// by pass if expression is invalid format
		if(expr.hasError()) {
			return new EvaluationResultImpl(ResultType.ERROR,  ErrorValue.valueOf(ErrorValue.INVALID_FORMULA), ErrorEval.FORMULA_INVALID);
		}
		Ref dependant = context.getDependent();
		EvaluationResult result = null;
		try {

			// get evaluation context from book series
			SBook book = context.getBook();
			AbstractBookSeriesAdv bookSeries = (AbstractBookSeriesAdv)book.getBookSeries();
			DependencyTableAdv table = (DependencyTableAdv)bookSeries.getDependencyTable();	
			Map<String, EvalContext> evalCtxMap = getEvalCtxMap(bookSeries);
			
			// check again
			EvalContext ctx = evalCtxMap.get(book.getBookName());
			if(ctx == null) { // just in case
				throw new IllegalStateException("The book isn't in the book series.");
			}
			EvalBook evalBook = ctx.getBook();
			WorkbookEvaluator evaluator = ctx.getEvaluator();

			// evaluation formula
			// for resolving, temporarily replace current XEL context 
			Object oldXelCtx = getXelContext();
			XelContext xelCtx = getXelContextForResolving(context, evalBook, evaluator);
			setXelContext(xelCtx);
			try {
				result = evaluateFormula(expr, context, evalBook, evaluator);
			} finally {
				//ZSS-818: do not drop the internal cache: better performance
				//evaluator.clearAllCachedResultValues();
				setXelContext(oldXelCtx);
			}
			if(dependant!=null){
				table.setEvaluated(dependant);
			}
		} catch(NotImplementedException e) {
			_logger.info(e.getMessage() + " when eval " + expr.getFormulaString());
			result = new EvaluationResultImpl(ResultType.ERROR, new ErrorValue(ErrorValue.INVALID_NAME, e.getMessage()), ErrorEval.NAME_INVALID);
		} catch(EvaluationException e) { 
			_logger.warning(e.getMessage() + " when eval " + expr.getFormulaString());
			ErrorEval error = e.getErrorEval();
			result = new EvaluationResultImpl(ResultType.ERROR, 
					error==null?new ErrorValue(ErrorValue.INVALID_FORMULA, e.getMessage()):error, 
					error==null?ErrorEval.FORMULA_INVALID:error);
		} catch(FormulaParseException e) {
			// we skip evaluation if formula has parsing error
			// so if still occurring formula parsing exception, it should be a bug 
			_logger.error(e.getMessage() + " when eval " + expr.getFormulaString());
			result = new EvaluationResultImpl(ResultType.ERROR, new ErrorValue(ErrorValue.INVALID_FORMULA, e.getMessage()), ErrorEval.FORMULA_INVALID);
		} catch(Exception e) {
			_logger.error(e.getMessage() + " when eval " + expr.getFormulaString(), e);
			result = new EvaluationResultImpl(ResultType.ERROR, new ErrorValue(ErrorValue.INVALID_FORMULA, e.getMessage()), ErrorEval.FORMULA_INVALID);
		}
		return result;
	}

	//20140731, henrichen: must synchronize the initialization of this context 
	//map in collaboration cases 
	//see http://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
	private Map<String, EvalContext> getEvalCtxMap(AbstractBookSeriesAdv bookSeries) {
		Map<String, EvalContext> evalCtxMap = getEvalCtxMap0(bookSeries);
		
		// get evaluation context or create new one if not existed
		if(evalCtxMap == null) {
			synchronized(bookSeries) {
				evalCtxMap = getEvalCtxMap0(bookSeries);
				if (evalCtxMap == null) {
					evalCtxMap = new LinkedHashMap<String, FormulaEngineImpl.EvalContext>();
					List<String> bookNames = new ArrayList<String>();
					List<WorkbookEvaluator> evaluators = new ArrayList<WorkbookEvaluator>();
					for(SBook nb : bookSeries.getBooks()) {
						String bookName = nb.getBookName();
						EvalBook evalBook = new EvalBook(nb);
						//ZSS-818
						WorkbookEvaluator we = new WorkbookEvaluator(evalBook, noCacheClassifier, null, new WorkbookEvaluator.CacheManager() {
							@Override
							public void onUpdateCacheResult(
									EvaluationCell srcCell, ValueEval result) {
								EvalSheet evalSheet = (EvalSheet) srcCell.getSheet();
								SSheet sheet = evalSheet.getNSheet();
								final int row = srcCell.getRowIndex();
								final int col = srcCell.getColumnIndex();
								SCell cell = sheet.getCell(row, col);
								((AbstractCellAdv)cell).setFormulaResultValue(result);
							}
							
						});
						bookNames.add(bookName);
						evaluators.add(we);
						evalCtxMap.put(bookName, new EvalContext(evalBook, we));
		
						// aggregate built-in functions and user defined functions
						FunctionResolver resolver = FunctionResolverFactory.createFunctionResolver();
						UDFFinder zkUDFF = resolver.getUDFFinder(); // ZK user defined function finder
						if(zkUDFF != null) {
							IndexedUDFFinder bookUDFF = (IndexedUDFFinder)evalBook.getUDFFinder(); // book contained built-in function finder
							bookUDFF.insert(0, zkUDFF);
						}
					}
					CollaboratingWorkbooksEnvironment.setup(bookNames.toArray(new String[0]),
							evaluators.toArray(new WorkbookEvaluator[0]));
					//20140731, henrichen: NonSerializableHolder also works as a FinalWrapper so double check locking works
					final Object holder = new NonSerializableHolder<Map<String, EvalContext>>(evalCtxMap);
					bookSeries.setAttribute(KEY_EVALUATORS, holder);
				}
			}
		}
		return evalCtxMap;
	}
	private Map<String, EvalContext> getEvalCtxMap0(AbstractBookSeriesAdv bookSeries) {
		@SuppressWarnings("unchecked")
		final NonSerializableHolder<Map<String, EvalContext>> holder = 
			(NonSerializableHolder<Map<String, EvalContext>>)bookSeries.getAttribute(KEY_EVALUATORS);
		return holder == null ? null : holder.getObject();
	}
	

	protected EvaluationResult evaluateFormula(FormulaExpression expr, FormulaEvaluationContext context, EvalBook evalBook, WorkbookEvaluator evaluator) throws FormulaParseException, Exception {

		// do evaluate
		SBook book = context.getBook();
		int currentSheetIndex = book.getSheetIndex(context.getSheet());
		SCell cell = context.getCell();
		ValueEval value = null;
		boolean multipleArea = isMultipleAreaFormula(expr.getFormulaString());
		if(cell == null || cell.isNull()) {
			// evaluation formula directly
			if(multipleArea){
				String[] formulas = unwrapeAreaFormula(expr.getFormulaString());
				List<ValueEval> evals = new ArrayList<ValueEval>(formulas.length);
				for(String f:formulas){
					value = evaluator.evaluate(currentSheetIndex, f, true, context.getDependent());
					evals.add(value);
				}
				value = new ValuesEval(evals.toArray(new ValueEval[evals.size()]));
			}else{
				value = evaluateFormulaExpression(evaluator, currentSheetIndex, expr, true, context.getDependent()); //ZSS-759, ZSS-834
			}
		} else {
			if(multipleArea){//is multipleArea formula in cell, should return #VALUE!
				return new EvaluationResultImpl(ResultType.ERROR, ErrorValue.valueOf(ErrorValue.INVALID_VALUE), ErrorEval.VALUE_INVALID);
			}
			EvaluationCell evalCell = evalBook.getSheet(currentSheetIndex).getCell(cell.getRowIndex(),
					cell.getColumnIndex());
			value = evaluator.evaluate(evalCell, context.getDependent());
		}

		// convert to result
		return convertToEvaluationResult(value);
	}

	//ZSS-818
	public static EvaluationResult convertToEvaluationResult(ValueEval value) throws EvaluationException {
		// convert to result
		if(value instanceof ErrorEval) {
			int code = ((ErrorEval)value).getErrorCode();
			return new EvaluationResultImpl(ResultType.ERROR, ErrorValue.valueOf((byte)code), value);
		} else {
			try{
				final ResultValueEval resultEval = getResolvedValue(value);
				return new EvaluationResultImpl(ResultType.SUCCESS, resultEval.value, resultEval.valueEval); //ZSS-810
			}catch(EvaluationException x){
				//error when resolve value.
				if(x.getErrorEval()!=null){//ZSS-591 Get console exception after delete sheet
					return new EvaluationResultImpl(ResultType.ERROR, ErrorValue.valueOf((byte)x.getErrorEval().getErrorCode()), x.getErrorEval());
				}else{
					throw x;
				}
			}
		}
	}

	protected static ResultValueEval getResolvedValue(ValueEval value) throws EvaluationException {
		if(value instanceof StringEval) {
			return new ResultValueEval(((StringEval)value).getStringValue(), value); //ZSS-810
		} else if(value instanceof NumberEval) {
			return new ResultValueEval(((NumberEval)value).getNumberValue(), value); //ZSS-810
		} else if(value instanceof BlankEval) {
			return new ResultValueEval("", value); //ZSS-810
		} else if(value instanceof BoolEval) {
			return new ResultValueEval(((BoolEval)value).getBooleanValue(), value); //ZSS-810
		} else if(value instanceof ValuesEval) {
			ValueEval[] values = ((ValuesEval)value).getValueEvals();
			Object[] array = new Object[values.length];
			for(int i = 0; i < values.length; ++i) {
				array[i] = getResolvedValue(values[i]).value; //ZSS-810
			}
			return new ResultValueEval(array, value); //ZSS-810
		} else if(value instanceof AreaEval) {
			// covert all values into an array
			List<Object> list = new ArrayList<Object>();
			AreaEval area = (AreaEval)value;
			for(int r = 0; r < area.getHeight(); ++r) {
				for(int c = 0; c < area.getWidth(); ++c) {
					ValueEval v = area.getValue(r, c);
					list.add(getResolvedValue(v).value); //ZSS-810
				}
			}
			return new ResultValueEval(list, value); //ZSS-810
		} else if(value instanceof RefEval) {
			ValueEval ve = ((RefEval)value).getInnerValueEval();
			Object v = getResolvedValue(ve).value; //ZSS-810
			return new ResultValueEval(v, value); //ZSS-810
		} else if(value instanceof ErrorEval) {
			throw new EvaluationException((ErrorEval)value);
		} else {
			throw new EvaluationException(null, "no matched type: " + value); // FIXME
		}
	}

	protected Object getXelContext() {
		return null; // do nothing
	}

	protected void setXelContext(Object ctx) {
		// do nothing
	}

	private XelContext getXelContextForResolving(FormulaEvaluationContext context, EvaluationWorkbook evalBook, WorkbookEvaluator evaluator) {
		XelContext xelContext = _xelContexts.get(evalBook);
		if(xelContext == null) {

			// create function resolver
			FunctionResolver resolver = FunctionResolverFactory.createFunctionResolver();

			// apply POI dependency tracker for defined name resolving
			DependencyTracker tracker = resolver.getDependencyTracker();
			if(tracker != null) {
				evaluator.setDependencyTracker(tracker);
			}
			
			// collect all function mappers 
			JoinFunctionMapper functionMapper = new JoinFunctionMapper(null);
			FunctionMapper extraFunctionMapper = context.getFunctionMapper();
			if(extraFunctionMapper != null) {
				functionMapper.addFunctionMapper(extraFunctionMapper); // must before ZSS function mapper
			}
			FunctionMapper zssFuncMapper = resolver.getFunctionMapper();
			if(zssFuncMapper != null) {
				functionMapper.addFunctionMapper(zssFuncMapper);
			}
			
			// collect all variable resolvers
			JoinVariableResolver variableResolver = new JoinVariableResolver();
			VariableResolver extraVariableResolver = context.getVariableResolver();
			if(extraVariableResolver != null) {
				variableResolver.addVariableResolver(extraVariableResolver);
			}
			
			// create XEL context
			xelContext = new SimpleXelContext(variableResolver, functionMapper);
			xelContext.setAttribute("zkoss.zss.CellType", Object.class);
			_xelContexts.put(evalBook, xelContext);
		}
		return xelContext;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void clearCache(FormulaClearContext context) {
		try {
			SBook book = context.getBook();
			SSheet sheet = context.getSheet();
			SCell cell = context.getCell();

			// take evaluators from book series
			AbstractBookSeriesAdv bookSeries = (AbstractBookSeriesAdv)book.getBookSeries();
			NonSerializableHolder<Map<String, EvalContext>> holder = (NonSerializableHolder<Map<String, EvalContext>>)bookSeries
					.getAttribute(KEY_EVALUATORS);
			Map<String, EvalContext> map = holder == null?null:holder.getObject();

			// do nothing if not existed
			if(map == null) {
				return;
			}

			// clean cache and target is a cell
			if(cell != null && !cell.isNull()) {

				// do nothing if not existed
				EvalContext ctx = map.get(book.getBookName());
				if(ctx == null) {
					_logger.warning("clear a non-existed book? >> " + book.getBookName());
					return;
				}

				// notify POI formula evaluator one of cell has been updated
				String sheetName = sheet.getSheetName();
				EvalBook evalBook = ctx.getBook();
				EvaluationSheet evalSheet = evalBook.getSheet(evalBook.getSheetIndex(sheetName));
				EvaluationCell evalCell = evalSheet.getCell(cell.getRowIndex(), cell.getColumnIndex());
				WorkbookEvaluator evaluator = ctx.getEvaluator();
				evaluator.notifyUpdateCell(evalCell);
			} else {
				// no cell indicates clearing all cache
				bookSeries.setAttribute(KEY_EVALUATORS, null);
				map.clear(); // just in case
			}
		} catch(Exception e) {
			_logger.error(e.getMessage(), e);
		}
	}

	protected static class FormulaExpressionImpl implements FormulaExpression, Serializable {
		private static final long serialVersionUID = -8532826169759927711L;
		private String formula;
		private Ref[] refs;
		private boolean error;
		private String errorMessage;

		//ZSS-747
		private Ptg[] ptgs;
		private boolean multipleArea;
		
		/**
		 * @param ref resolved reference if formula has only one parsed token
		 */
		public FormulaExpressionImpl(String formula, Ptg[] ptgs, Ref[] refs) {
			this(formula, ptgs,refs,false,null, false);
		}
		public FormulaExpressionImpl(String formula, Ptg[] ptgs, Ref[] refs, boolean error, String errorMessage, boolean multipleArea) {
			this.formula = formula;
			if(refs!=null){
				for(Ref ref:refs){
					if( ref.getType() == RefType.AREA || ref.getType() == RefType.CELL){
						continue;
					}
					this.error = true;
					this.errorMessage = errorMessage==null?"wrong area reference":errorMessage;
					return;
				}
			}
			this.ptgs = ptgs;
			this.refs = refs;
			this.error = error;
			this.errorMessage = errorMessage;
			this.multipleArea = multipleArea;
			
		}
//		public FormulaExpressionImpl(String formula, Ref[] refs) {
//			this(formula,refs,false,null);
//		}
//		public FormulaExpressionImpl(String formula, Ref[] refs, boolean error, String errorMessage) {
//			this.formula = formula;
//			if(refs!=null){
//				for(Ref ref:refs){
//					if( ref.getType() == RefType.AREA || ref.getType() == RefType.CELL){
//						continue;
//					}
//					this.error = true;
//					this.errorMessage = errorMessage==null?"wrong area reference":errorMessage;
//					return;
//				}
//			}
//			this.refs = refs;
//			this.error = error;
//			this.errorMessage = errorMessage;
//			
//		}

		@Override
		public boolean hasError() {
			return error;
		}
		
		@Override
		public String getErrorMessage(){
			return errorMessage;
		}

		@Override
		public String getFormulaString() {
			return formula;
		}

		@Override
		public boolean isAreaRefs() {
			return refs != null && refs.length>0;
		}
		
		@Override
		public Ref[] getAreaRefs() {
			return refs;
		}
		
		//ZSS-747
		@Override
		public Ptg[] getPtgs() {
			return ptgs;
		}
		
		//ZSS-747
		@Override
		public boolean isMultipleAreaFormula() {
			return multipleArea;
		}
	}

	protected static class EvaluationResultImpl implements EvaluationResult {

		private ResultType type;
		private Object value;
		private ValueEval valueEval; //ZSS-810

		public EvaluationResultImpl(ResultType type, Object value, ValueEval valueEval) {
			this.type = type;
			this.value = value;
			this.valueEval = valueEval;
		}

		@Override
		public ResultType getType() {
			return type;
		}

		@Override
		public Object getValue() {
			return value;
		}
		
		//ZSS-810
		@Override
		public ValueEval getValueEval() {
			return valueEval;
		}

	}

	protected static class EvalContext {
		private EvalBook book;
		private WorkbookEvaluator evaluator;

		public EvalContext(EvalBook book, WorkbookEvaluator evaluator) {
			this.book = book;
			this.evaluator = evaluator;
		}

		public EvalBook getBook() {
			return book;
		}

		public WorkbookEvaluator getEvaluator() {
			return evaluator;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((book == null) ? 0 : book.hashCode());
			result = prime * result + ((evaluator == null) ? 0 : evaluator.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			EvalContext other = (EvalContext)obj;
			if(book == null) {
				if(other.book != null)
					return false;
			} else if(!book.equals(other.book))
				return false;
			if(evaluator == null) {
				if(other.evaluator != null)
					return false;
			} else if(!evaluator.equals(other.evaluator))
				return false;
			return true;
		}

	}
	
	private FormulaExpression adjustMultipleArea(String formula, FormulaParseContext context, FormulaAdjuster adjuster) {
		if(!isMultipleAreaFormula(formula)){
			return null;
		}
		//handle multiple area
		String[] fs = unwrapeAreaFormula(formula); 
		List<Ptg> tokens = new ArrayList<Ptg>(fs.length + 2);
		List<Ref> areaRefs = new LinkedList<Ref>();
		for(int i=0;i<fs.length;i++){
			FormulaExpression expr = adjust(fs[i], context, adjuster);
			if(expr.hasError()){
				return new FormulaExpressionImpl(formula, null, null,true,expr.getErrorMessage(), true);
			}
			if(expr.isAreaRefs()){
				for(Ref ref:expr.getAreaRefs()){
					areaRefs.add(ref);
				}
			}
			Ptg[] ptgs = expr.getPtgs();
			for (int k = 0; k < ptgs.length; ++k) {
				tokens.add(ptgs[k]);
			}
		}
		tokens.add(new ParenthesisPtg(tokens.size()));
		return new FormulaExpressionImpl(formula, tokens.toArray(new Ptg[tokens.size()]),  areaRefs.size()==0?null:areaRefs.toArray(new Ref[areaRefs.size()]));
	}
	
	/**
	 * adjust formula through specific adjuster
	 */
	private FormulaExpression adjust(String formula, FormulaParseContext context, FormulaAdjuster adjuster) {
		FormulaExpression expr = null;
		try {
			// adapt and parse
			ParsingBook parsingBook = new ParsingBook(context.getBook());
			String sheetName = context.getSheet().getSheetName();
			int sheetIndex = parsingBook.getExternalSheetIndex(null, sheetName); // create index according parsing book logic
			Ptg[] tokens = parse(formula, parsingBook, sheetIndex, context);

			// adjust formula
			boolean modified = adjuster.process(sheetIndex, tokens, parsingBook, context);
			
			// render formula, detect region and create result
			String renderedFormula = modified ? 
					renderFormula(parsingBook, formula, tokens, true) : formula;
			Ref singleRef = tokens.length == 1 ? toDependRef(context, parsingBook, tokens[0], 0) : null;
			Ref[] refs = singleRef==null ? null :
				(singleRef.getType() == RefType.AREA || singleRef.getType() == RefType.CELL ?new Ref[]{singleRef}:null);
			expr = new FormulaExpressionImpl(renderedFormula, tokens, refs);

		} catch(FormulaParseException e) {
			_logger.info(e.getMessage());
			expr = new FormulaExpressionImpl(formula, null, null, true, e.getMessage(), false);
		}
		return expr;
	}
	
	protected static interface FormulaAdjuster {
		/**
		 * @return true if formula modified, denote this formula needs re-render
		 */
		public boolean process(int sheetIndex, Ptg[] tokens, ParsingBook parsingBook, FormulaParseContext context);
	}
	
	@Override
	public FormulaExpression move(String formula, final SheetRegion region, final int rowOffset, final int columnOffset, FormulaParseContext context) {
		formula = formula.trim();
		FormulaAdjuster shiftAdjuster = getMoveAdjuster(region, rowOffset, columnOffset);
		FormulaExpression result = adjustMultipleArea(formula, context, shiftAdjuster);
		if(result!=null){
			return result;
		}
		return adjust(formula, context, shiftAdjuster);
	}
	protected FormulaAdjuster getMoveAdjuster(final SheetRegion region, final int rowOffset, final int columnOffset) {
		return new FormulaAdjuster() {
			@Override
			public boolean process(int sheetIndex, Ptg[] tokens, ParsingBook parsingBook, FormulaParseContext context) {
				// move formula, limit to bound if dest. is out; if first and last both out on bound, it will be "#REF!"
				String bookName = context.getBook().getBookName();
				String regionBookName = region.getSheet().getBook().getBookName();
				String regionSheetName = region.getSheet().getSheetName();
				int regionSheetIndex;
				if(bookName.equals(regionBookName)) { // at same book
					regionSheetIndex = parsingBook.findExternalSheetIndex(regionSheetName); // find index, DON'T create
				} else { // different books
					regionSheetIndex = parsingBook.findExternalSheetIndex(regionBookName, regionSheetName); // find index, DON'T create
				}
				PtgShifter shifter = new PtgShifter(regionSheetIndex, region.getRow(), region.getLastRow(),
						rowOffset, region.getColumn(), region.getLastColumn(), columnOffset,
						parsingBook.getSpreadsheetVersion());
				return shifter.adjustFormula(tokens, sheetIndex);
			}
		};
	}
	

	@Override
	public FormulaExpression shrink(String formula, SheetRegion srcRegion, boolean horizontal, FormulaParseContext context) {
		SSheet sheet = srcRegion.getSheet();

		// shrinking is equals to move the neighbor region
		// calculate the neighbor and offset
		int rowOffset = 0, colOffset = 0;
		SheetRegion neighbor;
		if(horizontal) {
			// adjust on column
			colOffset = -srcRegion.getColumnCount();
			int col = srcRegion.getLastColumn() + 1;
			int lastCol = sheet.getBook().getMaxColumnIndex();
			// no change on row
			int row = srcRegion.getRow();
			int lastRow = srcRegion.getLastRow();
			neighbor = new SheetRegion(sheet, row, col, lastRow, lastCol);
		} else { // vertical
			// adjust on row
			rowOffset = -srcRegion.getRowCount();
			int row = srcRegion.getLastRow() + 1;
			int lastRow = sheet.getBook().getMaxRowIndex();
			// no change on column
			int col = srcRegion.getColumn();
			int lastCol = srcRegion.getLastColumn();
			neighbor = new SheetRegion(sheet, row, col, lastRow, lastCol);
		}

		// move it
		return move(formula, neighbor, rowOffset, colOffset, context);
	}

	@Override
	public FormulaExpression extend(String formula, SheetRegion srcRegion, boolean horizontal, FormulaParseContext context) {
		SSheet sheet = srcRegion.getSheet();

		// extending is equals to move selected region and neighbor region at the same time
		// calculate the target region (combined selected and neighbor) and offset
		int rowOffset = 0, colOffset = 0;
		SheetRegion neighbor;
		if(horizontal) {
			// adjust on column
			colOffset = srcRegion.getColumnCount();
			int col = srcRegion.getColumn();
			int lastCol = sheet.getBook().getMaxColumnIndex();
			// no change on row
			int row = srcRegion.getRow();
			int lastRow = srcRegion.getLastRow();
			neighbor = new SheetRegion(sheet, row, col, lastRow, lastCol);
		} else { // vertical
			// adjust on row
			rowOffset = srcRegion.getRowCount();
			int row = srcRegion.getRow();
			int lastRow = sheet.getBook().getMaxRowIndex();
			// no change on column
			int col = srcRegion.getColumn();
			int lastCol = srcRegion.getLastColumn();
			neighbor = new SheetRegion(sheet, row, col, lastRow, lastCol);
		}

		// move it
		return move(formula, neighbor, rowOffset, colOffset, context);
	}

	@Override
	public FormulaExpression shift(String formula, final int rowOffset, final int columnOffset, FormulaParseContext context) {
		formula = formula.trim();
		FormulaAdjuster shiftAdjuster = getShiftAdjuster(rowOffset, columnOffset); 		
		FormulaExpression result = adjustMultipleArea(formula, context, shiftAdjuster);
		if(result!=null){
			return result;
		}
		return adjust(formula, context, shiftAdjuster);
	}
	protected FormulaAdjuster getShiftAdjuster(final int rowOffset, final int columnOffset) {
		return new FormulaAdjuster() {
			@Override
			public boolean process(int sheetIndex, Ptg[] tokens, ParsingBook parsingBook, FormulaParseContext context) {
				// shift formula only if necessary
				if(rowOffset != 0 || columnOffset != 0) {
					
					// simply shift every PTG and no need to consider sheet index
					SBook book = context.getBook();
					for(int i = 0; i < tokens.length; ++i) {
						Ptg ptg = tokens[i];
						if(ptg instanceof RefPtgBase) {
							RefPtgBase rptg = (RefPtgBase)ptg;
							// calculate offset
							int r = rptg.getRow() + (rptg.isRowRelative() ? rowOffset : 0);
							int c = rptg.getColumn() + (rptg.isColRelative() ? columnOffset : 0);
							// if reference is out of bounds, convert it to #REF
							if(isValidRowIndex(book, r) && isValidColumnIndex(book, c)) {
								rptg.setRow(r);
								rptg.setColumn(c);
							} else {
								tokens[i] = PtgShifter.createDeletedRef(rptg);
							}
						} else if(ptg instanceof AreaPtgBase) {
							AreaPtgBase aptg = (AreaPtgBase)ptg;
							// shift
							int r0 = aptg.getFirstRow() + (aptg.isFirstRowRelative() ? rowOffset : 0);
							int r1 = aptg.getLastRow() + (aptg.isLastRowRelative() ? rowOffset : 0);
							int c0 = aptg.getFirstColumn() + (aptg.isFirstColRelative() ? columnOffset : 0);
							int c1 = aptg.getLastColumn() + (aptg.isLastColRelative() ? columnOffset : 0);
							// if reference is out of bounds, convert it to #REF
							if(isValidRowIndex(book, r0) && isValidRowIndex(book, r1)
									&& isValidColumnIndex(book, c0) && isValidColumnIndex(book, c1)) {
								aptg.setFirstRow(Math.min(r0, r1));
								aptg.setLastRow(Math.max(r0, r1));
								aptg.setFirstColumn(Math.min(c0, c1));
								aptg.setLastColumn(Math.max(c0, c1));
							} else {
								tokens[i] = PtgShifter.createDeletedRef(aptg);
							}
						}
					}
					return true;
				} else {
					return false;
				}
			}
		};
	}
	
	private boolean isValidRowIndex(SBook book, int rowIndex) {
		return 0 <= rowIndex && rowIndex <= book.getMaxRowIndex();
	}
	
	private boolean isValidColumnIndex(SBook book, int columnIndex) {
		return 0 <= columnIndex && columnIndex <= book.getMaxColumnIndex();
	}

	@Override
	public FormulaExpression transpose(String formula, final int rowOrigin, final int columnOrigin, FormulaParseContext context) {
		formula = formula.trim();
		FormulaAdjuster shiftAdjuster = getTransposeAdjuster(rowOrigin, columnOrigin);
		FormulaExpression result = adjustMultipleArea(formula, context, shiftAdjuster);
		if(result!=null){
			return result;
		}
		return adjust(formula, context, shiftAdjuster);
	}
	protected FormulaAdjuster getTransposeAdjuster(final int rowOrigin, final int columnOrigin) {
		return new FormulaAdjuster() {
			@Override
			public boolean process(int sheetIndex, Ptg[] tokens, ParsingBook parsingBook, FormulaParseContext context) {

				// simply adjust every PTG and no need to consider sheet index
				SBook book = context.getBook();
				for(int i = 0; i < tokens.length; ++i) {
					Ptg ptg = tokens[i];
					if(ptg instanceof RefPtgBase) {
						RefPtgBase rptg = (RefPtgBase)ptg;

						// process transpose only if both directions are relative
						if(rptg.isRowRelative() && rptg.isColRelative()) {

							// every direction:
							// 1. translate origin 2. swap row and column 3. translate origin back
							int r = (rptg.getColumn() - columnOrigin) + rowOrigin;
							int c = (rptg.getRow() - rowOrigin) + columnOrigin;

							// if reference is out of bounds, convert it to #REF
							if(isValidRowIndex(book, r) && isValidColumnIndex(book, c)) {
								rptg.setRow(r);
								rptg.setColumn(c);
							} else {
								tokens[i] = PtgShifter.createDeletedRef(rptg);
							}
						}
					} else if(ptg instanceof AreaPtgBase) {
						AreaPtgBase aptg = (AreaPtgBase)ptg;

						// need transpose process if ANY pair's both directions are relative
						// if so,
						// 1. this process skip any rest absolute setting
						// 2. swap absolute setting to another direction
						if((aptg.isFirstRowRelative() && aptg.isFirstColRelative())
								|| (aptg.isLastRowRelative() && aptg.isLastColRelative())) {

							// every direction:
							// 1. translate origin 2. swap row and column 3. translate origin back
							int r0 = (aptg.getFirstColumn() - columnOrigin) + rowOrigin;
							int c0 = (aptg.getFirstRow() - rowOrigin) + columnOrigin;
							int r1 = (aptg.getLastColumn() - columnOrigin) + rowOrigin;
							int c1 = (aptg.getLastRow() - rowOrigin) + columnOrigin;

							// swap absolute setting
							boolean temp = aptg.isFirstRowRelative();
							aptg.setFirstRowRelative(aptg.isFirstColRelative());
							aptg.setFirstColRelative(temp);
							temp = aptg.isLastRowRelative();
							aptg.setLastRowRelative(aptg.isLastColRelative());
							aptg.setLastColRelative(temp);

							// if reference is out of bounds, convert it to #REF
							if(isValidRowIndex(book, r0) && isValidRowIndex(book, r1)
									&& isValidColumnIndex(book, c0) && isValidColumnIndex(book, c1)) {
								aptg.setFirstRow(Math.min(r0, r1));
								aptg.setLastRow(Math.max(r0, r1));
								aptg.setFirstColumn(Math.min(c0, c1));
								aptg.setLastColumn(Math.max(c0, c1));
							} else {
								tokens[i] = PtgShifter.createDeletedRef(aptg);
							}
						}
					}
				}
				return true;
			}
		};
	}

	@Override
	public FormulaExpression renameSheet(String formula, final SBook targetBook, final String oldSheetName, final String newSheetName, FormulaParseContext context) {
		formula = formula.trim();
		FormulaAdjuster shiftAdjuster = getRenameSheetAdjuster(targetBook, oldSheetName, newSheetName);
		FormulaExpression result = adjustMultipleArea(formula, context, shiftAdjuster);
		if(result!=null){
			return result;
		}
		return adjust(formula, context, shiftAdjuster);
	}
	protected FormulaAdjuster getRenameSheetAdjuster(final SBook targetBook, final String oldSheetName, final String newSheetName) {
		return new FormulaAdjuster() {
			@Override
			public boolean process(int formulaSheetIndex, Ptg[] tokens, ParsingBook parsingBook, FormulaParseContext context) {
				if(newSheetName != null) {
					// parsed tokens has only external sheet index, not real sheet name
					// the sheet names are kept in parsing book, so we just rename sheets in parsing book
					// finally use such parsing book to re-render formula will get a renamed formula
					parsingBook.renameSheet(targetBook.getBookName(), oldSheetName, newSheetName);
				} else { // if new sheet name is null, it indicates deleted sheet
					
					// compare every token and replace it by deleted reference if necessary
					String bookName = targetBook == context.getBook() ? null : targetBook.getBookName(); 
					for(int i = 0; i < tokens.length; ++i) {
						Ptg ptg = tokens[i];
						if(ptg instanceof ExternSheetReferenceToken) { // must be Ref3DPtg or Area3DPtg 
							ExternSheetReferenceToken t = (ExternSheetReferenceToken)ptg;
							ExternalSheet es = parsingBook.getAnyExternalSheet(t.getExternSheetIndex());
							if((bookName == null && es.getWorkbookName() == null)
									|| (bookName != null && bookName.equals(es.getWorkbookName()))) {
								// replace token if any sheet name is matched 
								if(oldSheetName.equals(es.getSheetName()) || oldSheetName.equals(es.getLastSheetName())) {
									tokens[i] = PtgShifter.createDeletedRef3d(bookName, ptg); //ZSS-759
								}
							}
						}
					}
				}
				return true;
			}
		};
	}
	// ZSS-661
	@Override
	public FormulaExpression renameName(String formula, final SBook targetBook, final String oldName, final String newName, FormulaParseContext context) {
		return renameName(formula, targetBook, -1, oldName, newName, context);
	}
	//ZSS-790
	@Override
	public FormulaExpression renameName(String formula, final SBook targetBook, final int sheetIndex, final String oldName, final String newName, FormulaParseContext context) {
		formula = formula.trim();
		FormulaAdjuster shiftAdjuster = getRenameNameAdjuster(sheetIndex, oldName, newName);
		FormulaExpression result = adjustMultipleArea(formula, context, shiftAdjuster);
		if(result!=null){
			return result;
		}
		return adjust(formula, context, shiftAdjuster);
	}
	protected FormulaAdjuster getRenameNameAdjuster(final int sheetIndex, final String oldName, final String newName) {
		return new FormulaAdjuster() {
			@Override
			public boolean process(int contextSheetIndex, Ptg[] tokens, ParsingBook parsingBook, FormulaParseContext context) {
				// NamePtg refer to Name via index; simply rename the mapping in parsingBook
				parsingBook.renameName(sheetIndex, oldName, newName);
				return true;
			}
		};
	}

	//ZSS-747
	@Override
	public FormulaExpression movePtgs(FormulaExpression fe, final SheetRegion region, final int rowOffset, final int columnOffset, FormulaParseContext context) {
		return move(fe.getFormulaString(), region, rowOffset, columnOffset, context);
	}
	
	//ZSS-747
	@Override
	public FormulaExpression shrinkPtgs(FormulaExpression fe, SheetRegion srcRegion, boolean horizontal, FormulaParseContext context) {
		return shrink(fe.getFormulaString(), srcRegion, horizontal, context);
	}

	//ZSS-747
	@Override
	public FormulaExpression extendPtgs(FormulaExpression fe, SheetRegion srcRegion, boolean horizontal, FormulaParseContext context) {
		return extend(fe.getFormulaString(), srcRegion, horizontal, context);
	}

	//ZSS-747
	@Override
	public FormulaExpression shiftPtgs(FormulaExpression fe, final int rowOffset, final int columnOffset, FormulaParseContext context) {
		return shift(fe.getFormulaString(), rowOffset, columnOffset, context);
	}

	//ZSS-747
	@Override
	public FormulaExpression transposePtgs(FormulaExpression fe, final int rowOrigin, final int columnOrigin, FormulaParseContext context) {
		return transpose(fe.getFormulaString(), rowOrigin, columnOrigin, context);
	}

	
	//ZSS-747
	@Override
	public FormulaExpression renameSheetPtgs(FormulaExpression fe, final SBook targetBook, final String oldSheetName, final String newSheetName, FormulaParseContext context) {
		return renameSheet(fe.getFormulaString(), targetBook, oldSheetName, newSheetName, context);
	}

	//ZSS-747
	@Override
	public FormulaExpression renameNamePtgs(FormulaExpression fe, final SBook targetBook, final int sheetIndex, final String oldName, final String newName, FormulaParseContext context) {
		return renameName(fe.getFormulaString(), targetBook, sheetIndex, oldName, newName, context);
	}
	
	//ZSS-747
	@Override
	public void updateDependencyTable(FormulaExpression fexpr, FormulaParseContext context) {
		final Ref dependent = context.getDependent();
		if (dependent == null) return;
		if (fexpr.hasError()) return;
		
		SBook book = context.getBook();
		AbstractBookSeriesAdv series = (AbstractBookSeriesAdv)book.getBookSeries();
		DependencyTable dt = series.getDependencyTable();
		ParsingBook parsingBook = new ParsingBook(book);
		Ptg[] ptgs = fexpr.getPtgs();
		for (int j = 0, len = ptgs.length; j < len; ++j) {
			Ptg ptg = ptgs[j];
			Ref precedent = toDependRef(context, parsingBook, ptg, j);
			if(precedent != null) {
				dt.add(dependent, precedent);
				
				//ZSS-966
				if (precedent instanceof ColumnRef) {
					final ColumnRef colRef = (ColumnRef) precedent;
					
					// tableName
					final String bookName = colRef.getBookName();
					final String tableName = colRef.getTableName();
					final TablePrecedentRef tbPrecedent = new TablePrecedentRefImpl(bookName, tableName);
					dt.add(dependent, tbPrecedent);

					// ZSS-967
					if (colRef.isWithHeaders()) {
						// columnName1
						final String colName1 = colRef.getColumnName1();
						if (colName1 != null) {
							final ColumnPrecedentRef colPrecedent1 = new ColumnPrecedentRefImpl(bookName, tableName, colName1);
							dt.add(dependent, colPrecedent1);
	
							final STable table = ((AbstractBookAdv)book).getTable(tableName);
							final int rowHd = table.getHeadersRegion().getRow();
							final int col1 = colRef.getColumn();
							final String sheetName = table.getAllRegion().getSheet().getSheetName();
							final Ref cellRef1 = new RefImpl(bookName, sheetName, rowHd, col1);
							dt.add(dependent, cellRef1);
							
							// columnName2
							final String colName2 = colRef.getColumnName2();
							if (colName2 != null) {
								final ColumnPrecedentRef colPrecedent2 = new ColumnPrecedentRefImpl(bookName, tableName, colName2);
								dt.add(dependent, colPrecedent2);
	
								final int col2 = colRef.getLastColumn();
								final Ref cellRef2 = new RefImpl(bookName, sheetName, rowHd, col2);
								dt.add(dependent, cellRef2);
							}
						}
					}
				}
			}
		}
	}
	
	//ZSS-759
	protected ValueEval evaluateFormulaExpression(WorkbookEvaluator evaluator, int sheetIndex, FormulaExpression expr, boolean ignoreDereference, Ref dependent) {
		return evaluator.evaluate(sheetIndex, expr.getFormulaString(), ignoreDereference, dependent);
	}

	//ZSS-810
	private static class ResultValueEval {
		final Object value;
		final ValueEval valueEval;
		
		ResultValueEval(Object value, ValueEval valueEval) {
			this.value = value;
			this.valueEval = valueEval;
		}
	}

	//ZSS-820
	private FormulaExpression adjustMultipleAreaPtgs(Ptg[] ptgs0, String formula, FormulaParseContext context, FormulaAdjuster adjuster) {
		if(!isMultipleAreaFormula(formula)){
			return null;
		}
		//handle multiple area
		String[] fs = unwrapeAreaFormula(formula);
		Ptg ptg0 = ptgs0[ptgs0.length-1];
		if (!(ptg0 instanceof ParenthesisPtg))
			return null;
		
		Ptg[][] ps = FormulaRenderer.unwrapPtgArrays(ptgs0);
		
		List<Ptg> tokens = new ArrayList<Ptg>(ps.length + 2);
		List<Ref> areaRefs = new ArrayList<Ref>();
		for(int i=0;i<ps.length-1;i++){
			FormulaExpression expr = adjustPtgs(ps[i], fs[i], context, adjuster);
			if(expr.hasError()){
				return new FormulaExpressionImpl(formula, null, null,true,expr.getErrorMessage(), true);
			}
			if(expr.isAreaRefs()){
				for(Ref ref:expr.getAreaRefs()){
					areaRefs.add(ref);
				}
			}
			Ptg[] ptgs = expr.getPtgs();
			for (int k = 0; k < ptgs.length; ++k) {
				tokens.add(ptgs[k]);
			}
		}
		tokens.add(new ParenthesisPtg(tokens.size()));
		return new FormulaExpressionImpl(formula, tokens.toArray(new Ptg[tokens.size()]),  areaRefs.size()==0?null:areaRefs.toArray(new Ref[areaRefs.size()]));
	}

	//ZSS-820
	/**
	 * adjust formula through specific adjuster
	 */
	private FormulaExpression adjustPtgs(Ptg[] tokens, String formula, FormulaParseContext context, FormulaAdjuster adjuster) {
		FormulaExpression expr = null;
		try {
			// adapt and parse
			ParsingBook parsingBook = new ParsingBook(context.getBook());
			String sheetName = context.getSheet().getSheetName();
			int sheetIndex = parsingBook.getExternalSheetIndex(null, sheetName); // create index according parsing book logic
//			Ptg[] tokens = parse(formula, parsingBook, sheetIndex, context);

			// adjust formula
			boolean modified = adjuster.process(sheetIndex, tokens, parsingBook, context);
			
			// render formula, detect region and create result
			String renderedFormula = modified ? 
					renderFormula(parsingBook, formula, tokens, true) : formula;
			Ref singleRef = tokens.length == 1 ? toDependRef(context, parsingBook, tokens[0], 0) : null;
			Ref[] refs = singleRef==null ? null :
				(singleRef.getType() == RefType.AREA || singleRef.getType() == RefType.CELL ?new Ref[]{singleRef}:null);
			expr = new FormulaExpressionImpl(renderedFormula, tokens, refs);

		} catch(FormulaParseException e) {
			_logger.info(e.getMessage());
			expr = new FormulaExpressionImpl(formula, null, null, true, e.getMessage(), false);
		}
		return expr;
	}

	//ZSS-820
	@Override
	public FormulaExpression reorderSheetPtgs(FormulaExpression fexpr, SBook targetBook,
			int oldIndex, int newIndex, FormulaParseContext context) {
		String formula = fexpr.getFormulaString().trim();
		FormulaAdjuster shiftAdjuster = getReorderSheetAdjuster(targetBook, oldIndex, newIndex);
		FormulaExpression result = adjustMultipleAreaPtgs(fexpr.getPtgs(), formula, context, shiftAdjuster);
		if(result!=null){
			return result;
		}
		return adjustPtgs(fexpr.getPtgs(), formula, context, shiftAdjuster);
	}
	//ZSS-820
	protected FormulaAdjuster getReorderSheetAdjuster(final SBook targetBook, final int oldIndex, final int newIndex) {
		return new FormulaAdjuster() {
			@Override
			public boolean process(int formulaSheetIndex, Ptg[] tokens, ParsingBook parsingBook, FormulaParseContext context) {
				//do nothing, we have update parsingBook in BookImpl#reorderSheetFormula
				return true;
			}
		};
	}

	//ZSS-966
	@Override
	public FormulaExpression renameTableNameTablePtgs(FormulaExpression fexpr,
			SBook book, String oldName, String newName,
			FormulaParseContext context) {
		// TODO
		return fexpr;
	}

	//ZSS-967
	@Override
	public FormulaExpression renameColumnNameTablePtgs(FormulaExpression fexpr,
			STable table, String oldName, String newName,
			FormulaParseContext context) {
		// TODO
		return fexpr;
	}
}
