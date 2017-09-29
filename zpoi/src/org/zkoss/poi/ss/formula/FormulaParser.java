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

package org.zkoss.poi.ss.formula;

import org.zkoss.poi.ss.SpreadsheetVersion;
import org.zkoss.poi.ss.format.Formatters;
import org.zkoss.poi.ss.formula.constant.ErrorConstant;
import org.zkoss.poi.ss.formula.eval.FunctionEval;
import org.zkoss.poi.ss.formula.function.FunctionMetadata;
import org.zkoss.poi.ss.formula.function.FunctionMetadataRegistry;
import org.zkoss.poi.ss.formula.functions.*;
import org.zkoss.poi.ss.formula.ptg.*;
import org.zkoss.poi.ss.usermodel.ErrorConstants;
import org.zkoss.poi.ss.util.AreaReference;
import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.poi.ss.util.CellReference.NameType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * This class parses a formula string into a List of tokens in RPN order.
 * Inspired by
 *		   Lets Build a Compiler, by Jack Crenshaw
 * BNF for the formula expression is :
 * <expression> ::= <term> [<addop> <term>]*
 * <term> ::= <factor>  [ <mulop> <factor> ]*
 * <factor> ::= <number> | (<expression>) | <cellRef> | <function>
 * <function> ::= <functionName> ([expression [, expression]*])
 * <p/>
 * For POI internal use only
 * <p/>
 *
 *
 *  @author Avik Sengupta <avik at apache dot org>
 *  @author Andrew C. oliver (acoliver at apache dot org)
 *  @author Eric Ladner (eladner at goldinc dot com)
 *  @author Cameron Riley (criley at ekmail.com)
 *  @author Peter M. Murray (pete at quantrix dot com)
 *  @author Pavel Krupets (pkrupets at palmtreebusiness dot com)
 *  @author Josh Micich
 *  @author David Lewis (DLewis400 at gmail dot com)
 *  @author Henri Chen (henrichen at zkoss dot org) - Sheet1:Sheet3!xxx 3d reference
 */
public final class FormulaParser {
	private final static char TAB = '\t'; // HSSF + XSSF
	private final static char CR = '\r';  // Normally just XSSF
	private final static char LF = '\n';  // Normally just XSSF
	/**
	 * Matches a zero or one letter-runs followed by zero or one digit-runs.
	 * Either or both runs man optionally be prefixed with a single '$'.
	 * (copied+modified from {@link org.zkoss.poi.ss.util.CellReference#CELL_REF_PATTERN})
	 */
	private static final Pattern CELL_REF_PATTERN = Pattern.compile("(\\$?[A-Za-z]+)?(\\$?[0-9]+)?");
	private static final Pattern OP_FORMULA_PATTERN = Pattern.compile("((ATTR)_[A-Za-z0-9_]+)");
	private final String _formulaString;
	private final int _formulaLength;
	//ZSS-565
	//See http://office.microsoft.com/en-us/excel-help/issue-array-constants-may-unexpectedly-contain-backslash-characters-HA010222558.aspx
	final private char _decimalPoint; //default to '.'
	final private char _argComma; //default to ','
	final private char _arrayComma; //default to ','
	final private char _arraySemicolon; //default to ';'
	/** points at the next character to be read (after the {@link #look} char) */
	private int _pointer;
	private ParseNode _rootNode;
	/**
	 * Lookahead Character.
	 * gets value '\0' when the input string is exhausted
	 */
	private char look;
	private FormulaParsingWorkbook _book;
	private SpreadsheetVersion _ssVersion;
	private int _sheetIndex;
	private int _rowIdx;
	private int _colIdx;
	/**
	 * Create the formula parser, with the string that is to be
	 *  parsed against the supplied workbook.
	 * A later call the parse() method to return ptg list in
	 *  rpn order, then call the getRPNPtg() to retrieve the
	 *  parse results.
	 * This class is recommended only for single threaded use.
	 *
	 * If you only have a usermodel.HSSFWorkbook, and not a
	 *  model.Workbook, then use the convenience method on
	 *  usermodel.HSSFFormulaEvaluator
	 */
	//ZSS-855
	private FormulaParser(String formula, FormulaParsingWorkbook book, int sheetIndex, int rowIdx, int colIdx){
		_formulaString = formula;
		_pointer=0;
		_book = book;
		_ssVersion = book == null ? SpreadsheetVersion.EXCEL97 : book.getSpreadsheetVersion();
		_formulaLength = _formulaString.length();
		_sheetIndex = sheetIndex;
		
		_decimalPoint = '.';
		_argComma = ',';
		_arrayComma = ',';
		_arraySemicolon = ';';
		_rowIdx = rowIdx;
		_colIdx = colIdx;
	}
	//ZSS-855
	private FormulaParser(String formula, FormulaParsingWorkbook book, int sheetIndex, Locale locale, int rowIdx, int colIdx){
		_formulaString = formula;
		_pointer=0;
		_book = book;
		_ssVersion = book == null ? SpreadsheetVersion.EXCEL97 : book.getSpreadsheetVersion();
		_formulaLength = _formulaString.length();
		_sheetIndex = sheetIndex;
		if (locale != null) { //ZSS-565
			_decimalPoint = Formatters.getDecimalSeparator(locale);
			_argComma = _decimalPoint == ',' ? ';' : ',';
			_arrayComma = _decimalPoint == ',' ? '\\' : ',';
			_arraySemicolon = _decimalPoint == ';' ? '\\' : ';';
		} else {
			_decimalPoint = '.';
			_argComma = ',';
			_arrayComma = ',';
			_arraySemicolon = ';';
		}
		_rowIdx = rowIdx;
		_colIdx = colIdx;
	}
	/**
	 * Create the formula parser, with the string that is to be
	 *  parsed against the supplied workbook.
	 * A later call the parse() method to return ptg list in
	 *  rpn order, then call the getRPNPtg() to retrieve the
	 *  parse results.
	 * This class is recommended only for single threaded use.
	 *
	 * If you only have a usermodel.HSSFWorkbook, and not a
	 *  model.Workbook, then use the convenience method on
	 *  usermodel.HSSFFormulaEvaluator
	 */
	private FormulaParser(String formula, FormulaParsingWorkbook book, int sheetIndex){
		this(formula, book, sheetIndex, /*rowIdx*/ -1, /*rowIdx*/ -1);
	}
	private FormulaParser(String formula, FormulaParsingWorkbook book, int sheetIndex, Locale locale){
		_formulaString = formula;
		_pointer=0;
		_book = book;
		_ssVersion = book == null ? SpreadsheetVersion.EXCEL97 : book.getSpreadsheetVersion();
		_formulaLength = _formulaString.length();
		_sheetIndex = sheetIndex;
		if (locale != null) { //ZSS-565
			_decimalPoint = Formatters.getDecimalSeparator(locale);
			_argComma = _decimalPoint == ',' ? ';' : ',';
			_arrayComma = _decimalPoint == ',' ? '\\' : ',';
			_arraySemicolon = _decimalPoint == ';' ? '\\' : ';';
		} else {
			_decimalPoint = '.';
			_argComma = ',';
			_arrayComma = ',';
			_arraySemicolon = ';';
		}
	}
	
	/**
	 * Parse a formula into a array of tokens
	 *
	 * @param formula	 the formula to parse
	 * @param workbook	the parent workbook
	 * @param formulaType the type of the formula, see {@link FormulaType}
	 * @param sheetIndex  the 0-based index of the sheet this formula belongs to.
	 * The sheet index is required to resolve sheet-level names. <code>-1</code> means that
	 * the scope of the name will be ignored and  the parser will match names only by name
	 *
	 * @return array of parsed tokens
	 * @throws FormulaParseException if the formula has incorrect syntax or is otherwise invalid
	 */
	public static Ptg[] parse(String formula, FormulaParsingWorkbook workbook, int formulaType, int sheetIndex) {
		return parse(formula, workbook, formulaType, sheetIndex, /*rowIdx*/ -1, /*colIdx*/ -1);
	}

	/**
	 * Parse a formula into a array of tokens
	 *
	 * @param formula	 the formula to parse
	 * @param workbook	the parent workbook
	 * @param formulaType the type of the formula, see {@link FormulaType}
	 * @param sheetIndex  the 0-based index of the sheet this formula belongs to.
	 * The sheet index is required to resolve sheet-level names. <code>-1</code> means that
	 * the scope of the name will be ignored and  the parser will match names only by name
	 *
	 * @return array of parsed tokens
	 * @throws FormulaParseException if the formula has incorrect syntax or is otherwise invalid
	 * @since 3.9.7
	 */
	//ZSS-855
	public static Ptg[] parse(String formula, FormulaParsingWorkbook workbook, int formulaType, int sheetIndex, int rowIdx, int colIdx) {
		FormulaParser fp = new FormulaParser(formula, workbook, sheetIndex, rowIdx, colIdx);
		fp.parse();
		return fp.getRPNPtg(formulaType);
	}

	//ZSS-855
	public static Ptg[] parse(String formula, FormulaParsingWorkbook workbook, int formulaType, int sheetIndex, Locale locale, int rowIdx, int colIdx) {
		FormulaParser fp = new FormulaParser(formula, workbook, sheetIndex, locale, rowIdx, colIdx);
		fp.parse();
		return fp.getRPNPtg(formulaType);
	}
	
	//ZSS-565
	public static Ptg[] parse(String formula, FormulaParsingWorkbook workbook, int formulaType, int sheetIndex, Locale locale) {
		FormulaParser fp = new FormulaParser(formula, workbook, sheetIndex, locale);
		fp.parse();
		return fp.getRPNPtg(formulaType);
	}

	/**
	 * Recognize an Alpha Character
	 */
	private static boolean IsAlpha(char c) {
		return Character.isLetter(c) || c == '$' || c == '_';
	}

	/**
	 * Recognize a Decimal Digit
	 */
	private static boolean IsDigit(char c) {
		return Character.isDigit(c);
	}

	/** Report What Was Expected */
/*	private RuntimeException expected(String s) {
		String msg;

		if (look == '=' && _formulaString.substring(0, _pointer-1).trim().length() < 1) {
			msg = "The specified formula '" + _formulaString
				+ "' starts with an equals sign which is not allowed.";
		} else {
			msg = "Parse error near char " + (_pointer-1) + " '" + look + "'"
				+ " in specified formula '" + _formulaString + "'. Expected "
				+ s;
		}
		return new FormulaParseException(msg);
	}
*/

	/** Recognize White Space */
	private static boolean IsWhite( char c) {
		return  c ==' ' || c== TAB || c == CR || c == LF;
	}

	private static ParseNode augmentWithMemPtg(ParseNode root) {
		Ptg memPtg;
		if (needsMemFunc(root)) {
			memPtg = new MemFuncPtg(root.getEncodedSize());
		} else {
			memPtg = new MemAreaPtg(root.getEncodedSize());
		}
		return new ParseNode(memPtg, root);
	}

	/**
	 * From OOO doc: "Whenever one operand of the reference subexpression is a function,
	 *  a defined name, a 3D reference, or an external reference (and no error occurs),
	 *  a tMemFunc token is used"
	 *
	 */
	private static boolean needsMemFunc(ParseNode root) {
		Ptg token = root.getToken();
		if (token instanceof AbstractFunctionPtg) {
			return true;
		}
		if (token instanceof ExternSheetReferenceToken) { // 3D refs
			return true;
		}
		if (token instanceof NamePtg || token instanceof NameXPtg) { // 3D refs
			return true;
		}

		if (token instanceof OperationPtg || token instanceof ParenthesisPtg) {
			// expect RangePtg, but perhaps also UnionPtg, IntersectionPtg etc
			for(ParseNode child : root.getChildren()) {
				if (needsMemFunc(child)) {
					return true;
				}
			}
			return false;
		}
		if (token instanceof OperandPtg) {
			return false;
		}
		return token instanceof OperationPtg;

	}

	/**
	 * @param currentParsePosition used to format a potential error message
	 */
	private static void checkValidRangeOperand(String sideName, int currentParsePosition, ParseNode pn) {
		if (!isValidRangeOperand(pn)) {
			throw new FormulaParseException("The " + sideName
					+ " of the range operator ':' at position "
					+ currentParsePosition + " is not a proper reference.");
		}
	}

	/**
	 * @return <code>false</code> if sub-expression represented the specified ParseNode definitely
	 * cannot appear on either side of the range (':') operator
	 */
	private static boolean isValidRangeOperand(ParseNode a) {
		Ptg tkn = a.getToken();
		// Note - order is important for these instance-of checks
		if (tkn instanceof OperandPtg) {
			// notably cell refs and area refs
			return true;
		}

		// next 2 are special cases of OperationPtg
		if (tkn instanceof AbstractFunctionPtg) {
			AbstractFunctionPtg afp = (AbstractFunctionPtg) tkn;
			byte returnClass = afp.getDefaultOperandClass();
			return Ptg.CLASS_REF == returnClass;
		}
		if (tkn instanceof ValueOperatorPtg) {
			return false;
		}
		if (tkn instanceof OperationPtg) {
			return true;
		}

		// one special case of ControlPtg
		if (tkn instanceof ParenthesisPtg) {
			// parenthesis Ptg should have only one child
			return isValidRangeOperand(a.getChildren()[0]);
		}

		// one special case of ScalarConstantPtg
		if (tkn == ErrPtg.REF_INVALID) {
			return true;
		}

		// All other ControlPtgs and ScalarConstantPtgs cannot be used with ':'
		return false;
	}

	/**
	 * @return <code>true</code> if the specified character may be used in a defined name
	 */
	private static boolean isValidDefinedNameChar(char ch) {
		if (Character.isLetterOrDigit(ch)) {
			return true;
		}
		switch (ch) {
			case '.':
			case '_':
			case '?':
			case '\\': // of all things
				return true;
		}
		return false;
	}

	private static AreaReference createAreaRef(SimpleRangePart part1, SimpleRangePart part2) {
		if (!part1.isCompatibleForArea(part2)) {
			throw new FormulaParseException("has incompatible parts: '"
					+ part1.getRep() + "' and '" + part2.getRep() + "'.");
		}
		if (part1.isRow()) {
			return AreaReference.getWholeRow(part1.getRep(), part2.getRep());
		}
		if (part1.isColumn()) {
			return AreaReference.getWholeColumn(part1.getRep(), part2.getRep());
		}
		return new AreaReference(part1.getCellReference(), part2.getCellReference());
	}

	/**
	 * very similar to {@link SheetNameFormatter#isSpecialChar(char)}
	 */
	private static boolean isUnquotedSheetNameChar(char ch) {
		if (Character.isLetterOrDigit(ch)) {
			return true;
		}
		switch (ch) {
			case '.': // dot is OK
			case '_': // underscore is OK
			case ':': // colon is OK
				return true;
		}
		return false;
	}

	private static Double convertArrayNumber(Ptg ptg, boolean isPositive) {
		double value;
		if (ptg instanceof IntPtg) {
			value = ((IntPtg) ptg).getValue();
		} else if (ptg instanceof NumberPtg) {
			value = ((NumberPtg) ptg).getValue();
		} else {
			throw new RuntimeException("Unexpected ptg (" + ptg.getClass().getName() + ")");
		}
		if (!isPositive) {
			value = -value;
		}
		return new Double(value);
	}

	/**
	 * Get a PTG for an integer from its string representation.
	 * return Int or Number Ptg based on size of input
	 */
	private static Ptg getNumberPtgFromString(String number1, String number2, String exponent) {
		StringBuffer number = new StringBuffer();

		if (number2 == null) {
			number.append(number1);

			if (exponent != null) {
				number.append('E');
				number.append(exponent);
			}

			String numberStr = number.toString();
			int intVal;
			try {
				intVal = Integer.parseInt(numberStr);
			} catch (NumberFormatException e) {
				return new NumberPtg(numberStr);
			}
			if (IntPtg.isInRange(intVal)) {
				return new IntPtg(intVal);
			}
			return new NumberPtg(numberStr);
		}

		if (number1 != null) {
			number.append(number1);
		}

		number.append('.');
		number.append(number2);

		if (exponent != null) {
			number.append('E');
			number.append(exponent);
		}

		return new NumberPtg(number.toString());
	}

	/**
	 * Read New Character From Input Stream
	 */
	private void GetChar() {
		// Check to see if we've walked off the end of the string.
		if (_pointer > _formulaLength) {
			throw new FormulaParseException("string format is not correct");
		}
		if (_pointer < _formulaLength) {
			look = _formulaString.charAt(_pointer);
		} else {
			// Just return if so and reset 'look' to something to keep
			// SkipWhitespace from spinning
			look = (char) 0;
		}
		_pointer++;
		//System.out.println("Got char: "+ look);
	}

	private void resetPointer(int ptr) {
		_pointer = ptr;
		if (_pointer <= _formulaLength) {
			look = _formulaString.charAt(_pointer - 1);
		} else {
			// Just return if so and reset 'look' to something to keep
			// SkipWhitespace from spinning
			look = (char) 0;
		}
	}

	/**
	 * Skip Over Leading White Space
	 */
	private void SkipWhite() {
		while (IsWhite(look)) {
			GetChar();
		}
	}

	/**
	 * Consumes the next input character if it is equal to the one specified otherwise throws an
	 * unchecked exception. This method does <b>not</b> consume whitespace (before or after the
	 * matched character).
	 */
	private void Match(char x) {
		if (look != x) {
			throw expected("'" + x + "'");
		}
		GetChar();
	}

	/**
	 * Get a Number
	 */
	private String GetNum() {
		StringBuffer value = new StringBuffer();

		while (IsDigit(this.look)) {
			value.append(this.look);
			GetChar();
		}
		return value.length() == 0 ? null : value.toString();
	}

	private ParseNode parseRangeExpression() {
		ParseNode result = parseRangeable();
		boolean hasRange = false;
		while (look == ':') {
			int pos = _pointer;
			GetChar();
			ParseNode nextPart = parseRangeable();
			// Note - no range simplification here. An expr like "A1:B2:C3:D4:E5" should be
			// grouped into area ref pairs like: "(A1:B2):(C3:D4):E5"
			// Furthermore, Excel doesn't seem to simplify
			// expressions like "Sheet1!A1:Sheet1:B2" into "Sheet1!A1:B2"

			checkValidRangeOperand("LHS", pos, result);
			checkValidRangeOperand("RHS", pos, nextPart);

			ParseNode[] children = {result, nextPart,};
			result = new ParseNode(RangePtg.instance, children);
			hasRange = true;
		}
		if (hasRange) {
			return augmentWithMemPtg(result);
		}
		return result;
	}
	
	/**
	 * Parses area refs (things which could be the operand of ':') and simple factors
	 * Examples
	 * <pre>
	 *   A$1
	 *   $A$1 :  $B1
	 *   A1 .......	C2
	 *   Sheet1 !$A1
	 *   a..b!A1
	 *   'my sheet'!A1
	 *   .my.sheet!A1
	 *   my.named..range.
	 *   foo.bar(123.456, "abc")
	 *   123.456
	 *   "abc"
	 *   true
	 * </pre>
	 *
	 */
	private ParseNode parseRangeable() {
		SkipWhite();
		int savePointer = _pointer;
		SheetIdentifier sheetIden = parseSheetName();
		if (sheetIden == null) {
			resetPointer(savePointer);
		} else {
			SkipWhite();
			savePointer = _pointer;
		}

		SimpleRangePart part1 = parseSimpleRangePart();
		if (part1 == null) {
			if (sheetIden != null) {
                if(look == '#'){  // error ref like MySheet!#REF!
                    return new ParseNode(ErrPtg.valueOf(parseErrorLiteral()));
                } else if (!Character.isLetter(look) && look != '_' && look != '\\') {
                	// defined names may begin with a letter or underscore or backslash
                    throw new FormulaParseException("Cell reference expected after sheet name at index "
                            + _pointer + ":" + _formulaString);
                } else {
                	//ZSS-790: try to parse the Name
                	return parseNameName(sheetIden.getSheetName());
                }
			}
			//ZSS-796
			if (look == '[') { // could be a no name Table reference
				return parseTableSpecifiers(null, _sheetIndex);
			} else if (Character.isLetter(look) || look != '_' || look != '\\') {
				String tableName = parseName(); // could be a table name
				if (tableName != null && look == '[') {
					return parseTableSpecifiers(tableName, _sheetIndex);
				}
			}
			return parseNonRange(savePointer);
		}
		boolean whiteAfterPart1 = IsWhite(look);
		if (whiteAfterPart1) {
			SkipWhite();
		}

		if (look == ':') {
			int colonPos = _pointer;
			GetChar();
			SkipWhite();
			SimpleRangePart part2 = parseSimpleRangePart();
			if (part2 != null && !part1.isCompatibleForArea(part2)) {
				// second part is not compatible with an area ref e.g. S!A1:S!B2
				// where S might be a sheet name (that looks like a column name)

				part2 = null;
			}
			if (part2 == null) {
				// second part is not compatible with an area ref e.g. A1:OFFSET(B2, 1, 2)
				// reset and let caller use explicit range operator
				resetPointer(colonPos);
				if (!part1.isCell()) {
					String prefix;
					if (sheetIden == null) {
						prefix = "";
					} else {
						prefix = "'" + sheetIden.getSheetIdentifier().getName() + '!';
					}
					throw new FormulaParseException(prefix + part1.getRep() + "' is not a proper reference.");
				}
				return createAreaRefParseNode(sheetIden, part1, part2);
			}
			return createAreaRefParseNode(sheetIden, part1, part2);
		}

		if (look == '.') {
			GetChar();
			int dotCount = 1;
			while (look =='.') {
				dotCount ++;
				GetChar();
			}
			boolean whiteBeforePart2 = IsWhite(look);

			SkipWhite();
			SimpleRangePart part2 = parseSimpleRangePart();
			String part1And2 = _formulaString.substring(savePointer-1, _pointer-1);
			if (part2 == null) {
				if (sheetIden != null) {
					throw new FormulaParseException("Complete area reference expected after sheet name at index "
							+ _pointer + ".");
				}
				return parseNonRange(savePointer);
			}


			if (whiteAfterPart1 || whiteBeforePart2) {
				if (part1.isRowOrColumn() || part2.isRowOrColumn()) {
					// "A .. B" not valid syntax for "A:B"
					// and there's no other valid expression that fits this grammar
					throw new FormulaParseException("Dotted range (full row or column) expression '"
							+ part1And2 + "' must not contain whitespace.");
				}
				return createAreaRefParseNode(sheetIden, part1, part2);
			}

			if (dotCount == 1 && part1.isRow() && part2.isRow()) {
				// actually, this is looking more like a number
				return parseNonRange(savePointer);
			}

			if (part1.isRowOrColumn() || part2.isRowOrColumn()) {
				if (dotCount != 2) {
					//henrichen@zkoss.org: shall return #NAME?
					//throw new FormulaParseException("Dotted range (full row or column) expression '" + part1And2
					//		+ "' must have exactly 2 dots.");
					return parseNonRange(savePointer);
				}
			}
			
			return createAreaRefParseNode(sheetIden, part1, part2);
		}
		if (part1.isCell() && isValidCellReference(part1.getRep())) {
			return createAreaRefParseNode(sheetIden, part1, null);
		}
		if (sheetIden != null) {
			throw new FormulaParseException("Second part of cell reference expected after sheet name at index "
					+ _pointer + ".");
		}

		return parseNonRange(savePointer);
	}
	
	/**
	 * Parses simple factors that are not primitive ranges or range components
	 * i.e. '!', ':'(and equiv '...') do not appear
	 * Examples
	 * <pre>
	 *   my.named...range.
	 *   foo.bar(123.456, "abc")
	 *   123.456
	 *   "abc"
	 *   true
	 * </pre>
	 */
	private ParseNode parseNonRange(int savePointer) {
		resetPointer(savePointer);

		if (Character.isDigit(look)) {
			return new ParseNode(parseNumber());
		}
		if (look == '"') {
			return new ParseNode(new StringPtg(parseStringLiteral()));
		}
		// from now on we can only be dealing with non-quoted identifiers
		// which will either be named ranges or functions
		StringBuilder sb = new StringBuilder();

		// defined names may begin with a letter or underscore or backslash
		if (!Character.isLetter(look) && look != '_' && look != '\\') {
			throw expected("number, string, or defined name");
		}
		while (isValidDefinedNameChar(look)) {
			sb.append(look);
			GetChar();
		}
		SkipWhite();
		String name = sb.toString();
		if (look == '(') {
			return function(name);
		}
		if (name.equalsIgnoreCase("TRUE") || name.equalsIgnoreCase("FALSE")) {
			return  new ParseNode(BoolPtg.valueOf(name.equalsIgnoreCase("TRUE")));
		}
		if (OP_FORMULA_PATTERN.matcher(name).matches() && name.length() > 5) {
			String actualName = name.substring(5);
			return new ParseNode(new RelTableAttrPtg(actualName));
		}
		if (_book == null) {
			// Only test cases omit the book (expecting it not to be needed)
			throw new IllegalStateException("Need book to evaluate name '" + name + "'");
		}
		//ZSS-966: Table1 => Table1[] internally
		if (_book.getTableName(name) != null) {
			return createTableRefParseNode(name, new Object[0], _sheetIndex);
		}
		//20141012, henrichen@zkoss.org: refer to Name without sheet name must
		//  give -1 index since it could be Book scope or the _sheetIndex scope
		EvaluationName evalName = _book.getName(name, -1); 
		if (evalName == null) {
			return new ParseNode(createPtgForNonExistedName(_book, name, null));
		}
		
		return new ParseNode(evalName.createPtg()); 
	}
	
	private String parseName() {
		// from now on we can only be dealing with non-quoted identifiers
		// which will either be named ranges or functions
		StringBuilder sb = new StringBuilder();

		// defined names may begin with a letter or underscore or backslash
		if (!Character.isLetter(look) && look != '_' && look != '\\') {
			return null;
		}
		while (isValidDefinedNameChar(look)) {
			sb.append(look);
			GetChar();
		}
		SkipWhite();
		return sb.toString();
	}
	
	//ZSS-790
	private ParseNode parseNameName(String sheetName) {
		String name = parseName();
		if (look == '(') {
			throw expected("defined name");
		}
		if (name.equalsIgnoreCase("TRUE") || name.equalsIgnoreCase("FALSE")) {
			throw expected("defined name");
		}
		if (_book == null) {
			// Only test cases omit the book (expecting it not to be needed)
			throw new IllegalStateException("Need book to evaluate name '" + name + "'");
		}
		
		EvaluationName evalName = _book.getName(name, sheetName);
		if (evalName == null) {
			return new ParseNode(createPtgForNonExistedName(_book, name, sheetName));
		}
		return new ParseNode(evalName.createPtg()); 
	}

	//ZSS-796, ZSS-960
	private ParseNode parseTableSpecifiers(String tableName, int sheetIndex) {
		//ZSS-966 check if table exists
		if (tableName != null && _book.getTableName(tableName) == null) 
			throw expected("legal table name(" + tableName + ")");
		SkipWhite();
		GetChar();
		try {
			if (look == '[') { // column specifiers/ item specifiers / most 3
				Object[] specifiers = parseSpecifiers();
				return createTableRefParseNode(tableName, specifiers, sheetIndex);
			} else if (look == ']') { // ZSS-966: for internal Table1[] case
				GetChar();
				SkipWhite();
				return createTableRefParseNode(tableName, new Object[0], sheetIndex);
			} else {
				// One specifier case: must be #All, #Data, #Headers, #Totals, 
				// #This Row or simple column name
				Object specifier = parseSpecifier0();
				GetChar();
				SkipWhite();
				if (_book == null) {
					// Only test cases omit the book (expecting it not to be needed)
					throw new IllegalStateException("Need book to evaluate name '" + tableName + "'");
				}
				return createTableRefParseNode(tableName, new Object[] {specifier}, sheetIndex); 
			}
		} catch (IllegalArgumentException ex) {
			throw new IllegalStateException(ex.getMessage(), ex);
		}
	}
	
	//ZSS-960
	//see https://support.office.com/en-us/article/Using-structured-references-with-Excel-tables-f5ed2452-2337-4f71-bed3-c8ae6d2b276e
	private Object[] parseSpecifiers() {
		List<Object> specifiers = new ArrayList<Object>();
		int itemCount = 0;
		int colCount = 0;
		
		boolean preItem = false;
		boolean preColon = false;
		do {
			final Object result = parseSpecifier();
			if (result instanceof TablePtg.Item) { //#All,#Data,#Totals,#Headers,#This Row
				if (preColon)
					throw expected("a legal column name after ':' or maybe should use a ',' instead of a ':' before ["+ result + "]");
				++itemCount;
				if (itemCount > 2)
					throw expected("at most two special item specifier(e.g. #All, #Data, #Headers, #Totals, or #This Row)");
			} else {
				++colCount;
				if (colCount > 2)
					throw expected("at most two column specifiers separated with a colon(:) (e.g. [columnX]:[columnY])");
			}
			if ((colCount + itemCount) > 3) {
				throw expected("at most three specifiers in table expression");
			}
			specifiers.add(result);
			preItem = result instanceof TablePtg.Item;
			
			GetChar(); // fetch next
			SkipWhite();
			if (',' == look) {
				preColon = false;
				GetChar(); // fetch next
			} else if (':' == look) {
				if (preItem)
					throw unexpected("','", "':'");
				preColon = true;
				GetChar(); // fetch next
			} else if (']' != look) {
				if (preItem)
					throw expected("',' or ']'");
				else
					throw expected("',', ':' or ']'");
			}
			SkipWhite();
		} while (look != ']');
		GetChar();
		SkipWhite();
		return specifiers.toArray(new Object[specifiers.size()]);
	}

	//ZSS-960
	private Object parseSpecifier() {
		if (look != '[') {
			throw unexpected("'['", "'"+look+"'");
		}
		GetChar();
		return parseSpecifier0();
	}
	
	//ZSS-960
	private Object parseSpecifier0() {
		boolean leadingQuote = false;
		StringBuilder sb = new StringBuilder();
		int j = 0;
		while (true) {
			if (look == '\'') {
				if (leadingQuote) {
					sb.append("''");
				}
				leadingQuote = !leadingQuote;
			} else {
				switch(look) {
				case '[':
					if (!leadingQuote) {
						throw expected("single quote(') before '['");
					}
				case '#':
					if (!leadingQuote) {
						if (j > 0) {
							throw expected("single quote(') before '#'");
						}
					} else {
						sb.append("'");
					}
					sb.append('#');
					break;
				case ']':
					if (!leadingQuote) {
						final String result = sb.toString();
						final int sblen = result.length(); 
						if (sblen == 0)
							throw expected("#All, #Data, #Headers, #Totals, #This Row, or legal column names between [...]");
						else if (sblen == 1) {
							if ("#".equals(result))
								throw expected("#All, #Data, #Headers, #Totals, #This Row between [...] or prepend single quote(') before '#' as a legal column name");
						}
						if (result.startsWith("#")) {
							TablePtg.Item item = TablePtg.Item.valueOfName(result);
							if (item == null) {
								throw unexpected("#All, #Data, #Headers, #Totals, or #This Row", result);
							}
							return item; // Item specifier
						} else {
							return result; // Column specifier
						}
					}
					sb.append("']");
					break;
				default:
					if (leadingQuote) {
						throw expected("'[', ']', '#', or single quote(') after single quote(')");
					}
					sb.append(look);
					break;
				}
				leadingQuote = false;
			}
			GetChar(); // fetch next
			++j;
		}
	}

	//ZSS-796, ZSS-960
	private ParseNode createTableRefParseNode(String tableName, Object[] specifiers, int sheetIndex) {
		//ZSS-966 use name of that Table
		if (tableName != null) {
			String name = _book.getTableName(tableName);
			if (name != null) {
				tableName = name;
			}
		}
		TablePtg tbPtg = _book.createTablePtg(tableName, specifiers, sheetIndex,  _rowIdx, _colIdx);
		return new ParseNode(tbPtg);
	}

	// 20140305, hawkchen@potix.com, ZSS-575: HSSF cannot use DeferredNamePtg because it renders a formula string from Ptg
	//could move this method to FormulaParsingWorkbook 
	private Ptg createPtgForNonExistedName(FormulaParsingWorkbook book, String nonExistedName, String sheetName){
		// 20131227, paowang@potix.com, ZSS-533: still need a name, use another ptg for non-existed name					
		// throw new FormulaParseException("Name '" + name
		// + "' is completely unknown in the current workbook");
		if(_book.isAllowedDeferredNamePtg()){
			return new DeferredNamePtg(nonExistedName);
		} else {
			return _book.getName(nonExistedName, sheetName).createPtg();
		}
	}

	/**
	 *
	 * @param sheetIden may be <code>null</code>
	 * @param part1
	 * @param part2 may be <code>null</code>
	 */
	private ParseNode createAreaRefParseNode(SheetIdentifier sheetIden, SimpleRangePart part1,
			SimpleRangePart part2) throws FormulaParseException {

		int extIx;
		if (sheetIden == null) {
			extIx = Integer.MIN_VALUE;
		} else {
			String sName = sheetIden.getSheetIdentifier().getName();
			if (sheetIden.getBookName() == null) {
				extIx = _book.getExternalSheetIndex(sName);
			} else {
				extIx = _book.getExternalSheetIndex(sheetIden.getBookName(), sName);
			}
		}
		Ptg ptg;
		if (part2 == null) {
			CellReference cr = part1.getCellReference();
			if (sheetIden == null) {
				ptg = new RefPtg(cr);
			} else {
				ptg = new Ref3DPtg(cr, extIx);
			}
		} else {
			AreaReference areaRef = createAreaRef(part1, part2);

			if (sheetIden == null) {
				ptg = new AreaPtg(areaRef);
			} else {
				ptg = new Area3DPtg(areaRef, extIx);
			}
		}
		return new ParseNode(ptg);
	}

	/**
	 * Parses out a potential LHS or RHS of a ':' intended to produce a plain AreaRef.  Normally these are
	 * proper cell references but they could also be row or column refs like "$AC" or "10"
	 * @return <code>null</code> (and leaves {@link #_pointer} unchanged if a proper range part does not parse out
	 */
	private SimpleRangePart parseSimpleRangePart() {
		int ptr = _pointer-1; // TODO avoid StringIndexOutOfBounds
		boolean hasDigits = false;
		boolean hasLetters = false;
		boolean hasUnder = false;	//for form of Table_1 or Col_1 in rel operator formula
		while (ptr < _formulaLength) {
			char ch = _formulaString.charAt(ptr);
			if (Character.isDigit(ch)) {
				hasDigits = true;
			} else if (Character.isLetter(ch)) {
				hasLetters = true;
			} else if (ch == '_') {
				hasUnder = true;
			} else if (ch == '$') {
				//
			} else {
				break;
			}
			ptr++;
		}
		if (ptr <= _pointer - 1) {
			return null;
		}

		String rep = _formulaString.substring(_pointer - 1, ptr);

		if (hasUnder && (hasDigits || hasLetters)) {
			if (OP_FORMULA_PATTERN.matcher(rep.toUpperCase()).matches()) {
				resetPointer(ptr + 1); // stepping forward
				return new SimpleRangePart(rep, hasLetters, hasDigits, hasUnder);
			}
		}
		if (!CELL_REF_PATTERN.matcher(rep).matches()) {
			return null;
		}
		// Check range bounds against grid max
		if (hasLetters && hasDigits) {
			if (!isValidCellReference(rep)) {
				return null;
			}
		} else if (hasLetters) {
			if (!CellReference.isColumnWithnRange(rep.replace("$", ""), _ssVersion)) {
				return null;
			}
		} else if (hasDigits) {
			int i;
			try {
				i = Integer.parseInt(rep.replace("$", ""));
			} catch (NumberFormatException e) {
				return null;
			}
			if (i<1 || i>65536) {
				return null;
			}
		} else {
			// just dollars ? can this happen?
			return null;
		}


		resetPointer(ptr + 1); // stepping forward
		return new SimpleRangePart(rep, hasLetters, hasDigits, hasUnder);
	}

	/**
	 * Note - caller should reset {@link #_pointer} upon <code>null</code> result
	 * @return The sheet name as an identifier <code>null</code> if '!' is not found in the right place
	 */
	private SheetIdentifier parseSheetName() {

		String bookName;
		if (look == '[') {
			StringBuilder sb = new StringBuilder();
			GetChar();
			while (look != ']') {
				sb.append(look);
				GetChar();
			}
			GetChar();
			bookName = sb.toString();
		} else {
			bookName = null;
		}

		if (look == '\'') {
			StringBuffer sb = new StringBuffer();

			Match('\'');
			boolean done = look == '\'';
			while(!done) {
				sb.append(look);
				GetChar();
				if(look == '\'')
				{
					Match('\'');
					done = look != '\'';
				}
			}

			Identifier iden = new Identifier(sb.toString(), true);
			// quoted identifier - can't concatenate anything more
			SkipWhite();
			if (look == '!') {
				GetChar();
				return new SheetIdentifier(bookName, iden, _book);
			}
			return null;
		}

		// unquoted sheet names must start with underscore or a letter
		if (look =='_' || Character.isLetter(look)) {
			StringBuilder sb = new StringBuilder();
			// can concatenate idens with dots
			while (isUnquotedSheetNameChar(look)) {
				sb.append(look);
				GetChar();
			}
			SkipWhite();
			if (look == '!') {
				GetChar();
				return new SheetIdentifier(bookName, new Identifier(sb.toString(), false), _book);
			}
			return null;
		}
		return null;
	}

	/**
	 * @return <code>true</code> if the specified name is a valid cell reference
	 */
	private boolean isValidCellReference(String str) {
		//check range bounds against grid max
		boolean result = CellReference.classifyCellReference(str, _ssVersion) == NameType.CELL;

		if(result){
			/**
			 * Check if the argument is a function. Certain names can be either a cell reference or a function name
			 * depending on the contenxt. Compare the following examples in Excel 2007:
			 * (a) LOG10(100) + 1
			 * (b) LOG10 + 1
			 * In (a) LOG10 is a name of a built-in function. In (b) LOG10 is a cell reference
			 */
			boolean isFunc = FunctionMetadataRegistry.getFunctionByName(str.toUpperCase()) != null;
			if(isFunc){
				int savePointer = _pointer;
				resetPointer(_pointer + str.length());
				SkipWhite();
				// open bracket indicates that the argument is a function,
				// the returning value should be false, i.e. "not a valid cell reference"
				result = look != '(';
				resetPointer(savePointer);
			}
		}
		return result;
	}

	/**
	 * Note - Excel function names are 'case aware but not case sensitive'.  This method may end
	 * up creating a defined name record in the workbook if the specified name is not an internal
	 * Excel function, and has not been encountered before.
	 *
	 * @param name case preserved function name (as it was entered/appeared in the formula).
	 */
	private ParseNode function(String name) {
		Ptg nameToken = null;
		if(!AbstractFunctionPtg.isBuiltInFunctionName(name)) {
			// user defined function
			// in the token tree, the name is more or less the first argument

			if (_book == null) {
				// Only test cases omit the book (expecting it not to be needed)
				throw new IllegalStateException("Need book to evaluate name '" + name + "'");
			}
			//20101112, henrichen@zkoss.org: shall provide a temporary defined named record
			EvaluationName hName = _book.getName(name, -1); //20131227, paowang@potix.com, ZSS-533: don't create name into book, use another ptg for name
//			EvaluationName hName = _book.getOrCreateName(name, _sheetIndex);
			if (hName == null) {

				nameToken = _book.getNameXPtg(name);
				if (nameToken == null) {
					nameToken = createPtgForNonExistedName(_book, name, null);
				}
			} else {
				//20101112, henrichen@zkoss.org: unnecessary check
/*				if (!hName.isFunctionName()) {
					throw new FormulaParseException("Attempt to use name '" + name
							+ "' as a function, but defined name in workbook does not refer to a function");
				}
*/
				// calls to user-defined functions within the workbook
				// get a Name token which points to a defined name record
				nameToken = hName.createPtg();
			}
		}

		Match('(');
		ParseNode[] args = Arguments();
		Match(')');

		return getFunction(name, nameToken, args);
	}

	/**
	 * Generates the variable function ptg for the formula.
	 * <p>
	 * For IF Formulas, additional PTGs are added to the tokens
	 * @param name a {@link NamePtg} or {@link NameXPtg} or <code>null</code>
	 * @return Ptg a null is returned if we're in an IF formula, it needs extreme manipulation and is handled in this function
	 */
	private ParseNode getFunction(String name, Ptg namePtg, ParseNode[] args) {

		FunctionMetadata fm = FunctionMetadataRegistry.getFunctionByName(name.toUpperCase());
		int numArgs = args.length;
		if(fm == null) {
			if (namePtg == null) {
				throw new IllegalStateException("NamePtg must be supplied for external functions");
			}
			// must be external function
			ParseNode[] allArgs = new ParseNode[numArgs+1];
			allArgs[0] = new ParseNode(namePtg);
			System.arraycopy(args, 0, allArgs, 1, numArgs);
			return new ParseNode(FuncVarPtg.create(name, numArgs+1, false), allArgs);
		}

		if (namePtg != null) {
			throw new IllegalStateException("NamePtg no applicable to internal functions");
		}
		boolean isVarArgs = !fm.hasFixedArgsLength();
		boolean containsFilter = false;
		int funcIx = fm.getIndex();
		if (funcIx == FunctionMetadataRegistry.FUNCTION_INDEX_SUM && args.length == 1) {
			// Excel encodes the sum of a single argument as tAttrSum
			// POI does the same for consistency, but this is not critical
			return new ParseNode(AttrPtg.getSumSingle(), args);
			// The code below would encode tFuncVar(SUM) which seems to do no harm
		}
		//ZSS-852: Multiply inside SUMPRODUCT must do array calc
		if (funcIx == FunctionMetadataRegistry.FUNCTION_INDEX_SUMPRODUCT) {
			for (ParseNode node : args) {
				setMultiplyOperator(node);
			}
		}
		if (funcIx == FunctionMetadataRegistry.FUNCTION_INDEX_SELECT && args.length >= 2) {
			args[1] = new ParseNode(FilterHelperPtg.instance, args[1]);
			containsFilter = true;
		}
		if (funcIx == FunctionMetadataRegistry.FUNCTION_INDEX_JOIN && args.length >= 3) {
			args[2] = new ParseNode(FilterHelperPtg.instance, args[2]);
			containsFilter = true;
		}
		
		validateNumArgs(args.length, fm);

		AbstractFunctionPtg retval;
		if(isVarArgs) {
			retval = FuncVarPtg.create(name, numArgs, containsFilter);
		} else {
			retval = FuncPtg.create(funcIx);
		}

		return new ParseNode(retval, args);
	}

	//ZSS-852: Multiply inside SUMPRODUCT must do array calc
	private void setMultiplyOperator(ParseNode node) {
		if (node.getToken() instanceof MultiplyPtg) {
			((MultiplyPtg)node.getToken()).setOperator(true);
			if (node.getChildren() != null) {
				for (ParseNode node0 : node.getChildren()) {
					setMultiplyOperator(node0); //recursive
				}
			}
		}
	}
	
	private void validateNumArgs(int numArgs, FunctionMetadata fm) {
		if(numArgs < fm.getMinParams()) {
			String msg = "Too few arguments to function '" + fm.getName() + "'. ";
			if(fm.hasFixedArgsLength()) {
				msg += "Expected " + fm.getMinParams();
			} else {
				msg += "At least " + fm.getMinParams() + " were expected";
			}
			msg += " but got " + numArgs + ".";
			throw new FormulaParseException(msg);
		}
		//the maximum number of arguments depends on the Excel version
		int maxArgs;
		if (fm.hasUnlimitedVarags()) {
			if(_book != null) {
				maxArgs = _book.getSpreadsheetVersion().getMaxFunctionArgs();
			} else {
				//_book can be omitted by test cases
				maxArgs = fm.getMaxParams(); // just use BIFF8
			}
		} else {
			maxArgs = fm.getMaxParams();
		}

		if(numArgs > maxArgs) {
			String msg = "Too many arguments to function '" + fm.getName() + "'. ";
			if(fm.hasFixedArgsLength()) {
				msg += "Expected " + maxArgs;
			} else {
				msg += "At most " + maxArgs + " were expected";
			}
			msg += " but got " + numArgs + ".";
			throw new FormulaParseException(msg);
	   }
	}

	private boolean isArgumentDelimiter(char ch) {
		return ch == _argComma || ch == ')'; //ZSS-565
	}


	/** get arguments to a function */
	private ParseNode[] Arguments() {
		//average 2 args per function
		List<ParseNode> temp = new ArrayList<>(2);
		SkipWhite();
		if(look == ')') {
			return ParseNode.EMPTY_ARRAY;
		}

		boolean missedPrevArg = true;
		int numArgs = 0;
		while (true) {
			SkipWhite();
			if (isArgumentDelimiter(look)) {
				if (missedPrevArg) {
					temp.add(new ParseNode(MissingArgPtg.instance));
					numArgs++;
				}
				if (look == ')') {
					break;
				}
				Match(_argComma); //ZSS-565
				missedPrevArg = true;
				continue;
			}

			ParseNode children = comparisonExpression();

			temp.add(children);
			
			numArgs++;
			missedPrevArg = false;
			SkipWhite();
			if (!isArgumentDelimiter(look)) {
				//20131204 kuroridoplayer@gmail.com : ZSS-523
				// ZSS-719: show proper info instead of (char) 0
				throw unexpected("'"+_argComma+"' or ')'", look == (char) 0 ? null : String.valueOf(look));
			}
		}
		ParseNode[] result = new ParseNode[temp.size()];
		temp.toArray(result);
		return result;
	}

   /** Parse and Translate a Math Factor  */
	private ParseNode powerFactor() {
		ParseNode result = percentFactor();
		while(true) {
			SkipWhite();
			if(look != '^') {
				return result;
			}
			Match('^');
			ParseNode other = percentFactor();
			result = new ParseNode(PowerPtg.instance, result, other);
		}
	}

	private ParseNode percentFactor() {
		ParseNode result = parseSimpleFactor();
		while(true) {
			SkipWhite();
			if(look != '%') {
				return result;
			}
			Match('%');
			result = new ParseNode(PercentPtg.instance, result);
		}
	}

	/**
	 * factors (without ^ or % )
	 */
	private ParseNode parseSimpleFactor() {
		SkipWhite();
		switch(look) {
			case '#':
				return new ParseNode(ErrPtg.valueOf(parseErrorLiteral()));
			case '-':
				Match('-');
				return parseUnary(false);
			case '+':
				Match('+');
				return parseUnary(true);
			case '(':
				Match('(');
				ParseNode inside = comparisonExpression();
				Match(')');
				return new ParseNode(ParenthesisPtg.instance, inside);
			case '"':
				return new ParseNode(new StringPtg(parseStringLiteral()));
			case '{':
				Match('{');
				ParseNode arrayNode = parseArray();
				Match('}');
				return arrayNode;
		}
		if (IsAlpha(look) || Character.isDigit(look) || look == '\'' || look == '['){
			return parseRangeExpression();
		}
		if (look == _decimalPoint) { //ZSS-565
			return new ParseNode(parseNumber());
		}
		throw expected("cell reference or constant literal");
	}

	private ParseNode parseUnary(boolean isPlus) {

		boolean numberFollows = IsDigit(look) || look==_decimalPoint; //ZSS-565
		ParseNode factor = powerFactor();

		if (numberFollows) {
			// + or - directly next to a number is parsed with the number

			Ptg token = factor.getToken();
			if (token instanceof NumberPtg) {
				if (isPlus) {
					return factor;
				}
				token = new NumberPtg(-((NumberPtg)token).getValue());
				return new ParseNode(token);
			}
			if (token instanceof IntPtg) {
				if (isPlus) {
					return factor;
				}
				int intVal = ((IntPtg)token).getValue();
				// note - cannot use IntPtg for negatives
				token = new NumberPtg(-intVal);
				return new ParseNode(token);
			}
		}
		return new ParseNode(isPlus ? UnaryPlusPtg.instance : UnaryMinusPtg.instance, factor);
	}

	private ParseNode parseArray() {
		List<Object[]> rowsData = new ArrayList<Object[]>();
		while(true) {
			Object[] singleRowData = parseArrayRow();
			rowsData.add(singleRowData);
			if (look == '}') {
				break;
			}
			if (look != _arraySemicolon) { //ZSS-565
				throw expected("'}' or '" + _arraySemicolon + "'");
			}
			Match(_arraySemicolon); //ZSS-565
		}
		int nRows = rowsData.size();
		Object[][] values2d = new Object[nRows][];
		rowsData.toArray(values2d);
		int nColumns = values2d[0].length;
		checkRowLengths(values2d, nColumns);

		return new ParseNode(new ArrayPtg(values2d));
	}

	private void checkRowLengths(Object[][] values2d, int nColumns) {
		for (int i = 0; i < values2d.length; i++) {
			int rowLen = values2d[i].length;
			if (rowLen != nColumns) {
				throw new FormulaParseException("Array row " + i + " has length " + rowLen
						+ " but row 0 has length " + nColumns);
			}
		}
	}

	private Object[] parseArrayRow() {
		List<Object> temp = new ArrayList<Object>();
		while (true) {
			temp.add(parseArrayItem());
			SkipWhite();
			//ZSS-565
			if (look == '}' || look == _arraySemicolon) break;
			if (look == _arrayComma) {
				Match(_arrayComma);
				continue;
			}
			throw expected("'}', '" + _arrayComma + "', or '" + _arraySemicolon + "'");
		}

		Object[] result = new Object[temp.size()];
		temp.toArray(result);
		return result;
	}

	private Object parseArrayItem() {
		SkipWhite();
		switch(look) {
			case '"': return parseStringLiteral();
			case '#': return ErrorConstant.valueOf(parseErrorLiteral());
			case 'F': case 'f':
			case 'T': case 't':
				return parseBooleanLiteral();
			case '-':
				Match('-');
				SkipWhite();
				return convertArrayNumber(parseNumber(), false);
		}
		// else assume number
		return convertArrayNumber(parseNumber(), true);
	}

	private Boolean parseBooleanLiteral() {
		String iden = parseUnquotedIdentifier();
		if ("TRUE".equalsIgnoreCase(iden)) {
			return Boolean.TRUE;
		}
		if ("FALSE".equalsIgnoreCase(iden)) {
			return Boolean.FALSE;
		}
		throw expected("'TRUE' or 'FALSE'");
	}

	private Ptg parseNumber() {
		String number2 = null;
		String exponent = null;
		String number1 = GetNum();

		if (look == _decimalPoint) { //ZSS-565
			GetChar();
			number2 = GetNum();
		}

		if (look == 'E') {
			GetChar();

			String sign = "";
			if (look == '+') {
				GetChar();
			} else if (look == '-') {
				GetChar();
				sign = "-";
			}

			String number = GetNum();
			if (number == null) {
				throw expected("Integer");
			}
			exponent = sign + number;
		}

		if (number1 == null && number2 == null) {
			throw expected("Integer");
		}

		return getNumberPtgFromString(number1, number2, exponent);
	}

	private int parseErrorLiteral() {
		Match('#');
		String part1 = parseUnquotedIdentifier().toUpperCase();
		if (part1 == null) {
			throw expected("valid error constant literal");
		}

		switch(part1.charAt(0)) {
			case 'V':
				if(part1.equals("VALUE")) {
					Match('!');
					return ErrorConstants.ERROR_VALUE;
				}
				throw expected("#VALUE!");
			case 'R':
				if(part1.equals("REF")) {
					Match('!');
					return ErrorConstants.ERROR_REF;
				}
				throw expected("#REF!");
			case 'D':
				if(part1.equals("DIV")) {
					Match('/');
					Match('0');
					Match('!');
					return ErrorConstants.ERROR_DIV_0;
				}
				throw expected("#DIV/0!");
			case 'N':
				if(part1.equals("NAME")) {
					Match('?');  // only one that ends in '?'
					return ErrorConstants.ERROR_NAME;
				}
				if(part1.equals("NUM")) {
					Match('!');
					return ErrorConstants.ERROR_NUM;
				}
				if(part1.equals("NULL")) {
					Match('!');
					return ErrorConstants.ERROR_NULL;
				}
				if(part1.equals("N")) {
					Match('/');
					if(look != 'A' && look != 'a') {
						throw expected("#N/A");
					}
					Match(look);
					// Note - no '!' or '?' suffix
					return ErrorConstants.ERROR_NA;
				}
				throw expected("#NAME?, #NUM!, #NULL! or #N/A");

		}
		throw expected("#VALUE!, #REF!, #DIV/0!, #NAME?, #NUM!, #NULL! or #N/A");
	}

	private String parseUnquotedIdentifier() {
		if (look == '\'') {
			throw expected("unquoted identifier");
		}
		StringBuilder sb = new StringBuilder();
		while (Character.isLetterOrDigit(look) || look == '.') {
			sb.append(look);
			GetChar();
		}
		if (sb.length() < 1) {
			return null;
		}

		return sb.toString();
	}

	private String parseStringLiteral() {
		Match('"');

		StringBuffer token = new StringBuffer();
		while (true) {
			if (look == '"') {
				GetChar();
				if (look != '"') {
					break;
				}
			 }
			token.append(look);
			GetChar();
		}
		return token.toString();
	}

	/** Parse and Translate a Math Term */
	private ParseNode  Term() {
		ParseNode result = powerFactor();
		while(true) {
			SkipWhite();
			Ptg operator;
			switch(look) {
				case '*':
					Match('*');
					operator = new MultiplyPtg(); //MultiplyPtg.instance; //ZSS-852
					break;
				case '/':
					Match('/');
					operator = DividePtg.instance;
					break;
				default:
					return result; // finished with Term
			}
			ParseNode other = powerFactor();
			result = new ParseNode(operator, result, other);
		}
	}

	private ParseNode unionExpression() {
		ParseNode result = comparisonExpression();
		boolean hasUnions = false;
		while (true) {
			SkipWhite();
			switch(look) {
				case ',':
					GetChar();
					hasUnions = true;
					ParseNode other = comparisonExpression();
					result = new ParseNode(UnionPtg.instance, result, other);
					continue;
			}
			if (hasUnions) {
				return augmentWithMemPtg(result);
			}
			return result;
		}
	}

	private ParseNode comparisonExpression() {
		ParseNode result = concatExpression();
		while (true) {
			SkipWhite();
			switch(look) {
				case '=':
				case '>':
				case '<':
					Ptg comparisonToken = getComparisonToken();
					ParseNode other = concatExpression();
					result = new ParseNode(comparisonToken, result, other);
					continue;
			}
			return result; // finished with predicate expression
		}
	}

	private Ptg getComparisonToken() {
		if(look == '=') {
			Match(look);
			return EqualPtg.instance;
		}
		boolean isGreater = look == '>';
		Match(look);
		if(isGreater) {
			if(look == '=') {
				Match('=');
				return GreaterEqualPtg.instance;
			}
			return GreaterThanPtg.instance;
		}
		switch(look) {
			case '=':
				Match('=');
				return LessEqualPtg.instance;
			case '>':
				Match('>');
				return NotEqualPtg.instance;
		}
		return LessThanPtg.instance;
	}

	private ParseNode concatExpression() {
		ParseNode result = additiveExpression();
		while (true) {
			SkipWhite();
			if(look != '&') {
				break; // finished with concat expression
			}
			Match('&');
			ParseNode other = additiveExpression();
			result = new ParseNode(ConcatPtg.instance, result, other);
		}
		return result;
	}

	/** Parse and Translate an Expression */
	private ParseNode additiveExpression() {
		ParseNode result = Term();
		while (true) {
			SkipWhite();
			Ptg operator;
			switch(look) {
				case '+':
					Match('+');
					operator = AddPtg.instance;
					break;
				case '-':
					Match('-');
					operator = SubtractPtg.instance;
					break;
				default:
					return result; // finished with additive expression
			}
			ParseNode other = Term();
			result = new ParseNode(operator, result, other);
		}
	}

	/**
	 *  API call to execute the parsing of the formula
	 *
	 */
	private void parse() {
		_pointer=0;
		GetChar();
		_rootNode = unionExpression();

		if(_pointer <= _formulaLength) {
			String msg = "Unused input [" + _formulaString.substring(_pointer-1)
				+ "] after attempting to parse the formula [" + _formulaString + "]";
			throw new FormulaParseException(msg);
		}
	}

	private Ptg[] getRPNPtg(int formulaType) {
		OperandClassTransformer oct = new OperandClassTransformer(formulaType);
		// RVA is for 'operand class': 'reference', 'value', 'array'
		oct.transformFormula(_rootNode);
		return ParseNode.toTokenArray(_rootNode);
	}

	//20101214, henrichen@zkoss.org: make parse error more end user readable
	private RuntimeException expected(String s) {
		String msg;

		if (look == '=' && _formulaString.substring(0, _pointer - 1).trim().length() < 1) {
			msg = "The specified formula '" + _formulaString
					+ "' cannot starts with two equals signs.";
		} else {
			msg = "The specified formula '" + _formulaString
					+ "' contains an error. Expects " + s + ".";
		}
		return new FormulaParseException(msg);
	}

	//{--------------------------------------------------------------}
	//{ Parse and Translate an Assignment Statement }

	/**
	 procedure Assignment;
	 var Name: string[8];
	 begin
	 Name := GetName;
	 Match('=');
	 Expression;

	 end;
	 **/

	//20131204, kuroridoplayer@gmail.com: make parse error more end user readable
	private RuntimeException unexpected(String expect, String unexpect) {
		String msg = "The specified formula '" + _formulaString
				+ "' contains an error. Expects " + expect
				+ (unexpect == null ? "" : "; not " + unexpect) + ".";
		return new FormulaParseException(msg);
	}

	private static final class Identifier {
		private final String _name;
		private final boolean _isQuoted;

		public Identifier(String name, boolean isQuoted) {
			_name = name;
			_isQuoted = isQuoted;
		}

		public String getName() {
			return _name;
		}

		public boolean isQuoted() {
			return _isQuoted;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer(64);
			sb.append(getClass().getName());
			sb.append(" [");
			if (_isQuoted) {
				sb.append("'").append(_name).append("'");
			} else {
				sb.append(_name);
			}
			sb.append("]");
			return sb.toString();
		}
	}

	private static final class SheetIdentifier {


		private final String _bookName;
		private final Identifier _sheetIdentifier;

		public SheetIdentifier(String bookName, Identifier sheetIdentifier, FormulaParsingWorkbook book) {
			if (bookName == null && sheetIdentifier.isQuoted()) { //might be '[Book.xls]Sheet 1'!
				final String name = sheetIdentifier.getName();
				if (name.charAt(0) == '[') {
					int j = name.indexOf(']');
					if (j > 1) {
						bookName = name.substring(1, j);
						sheetIdentifier = new Identifier(name.substring(j + 1), true);
					}
				}
			}
			String realname = null; //20120117, henrichen@zkoss.org: ZSS-81
			if (bookName != null) {
				realname = book.getBookNameFromExternalLinkIndex(bookName);
				if (realname == null) {
					realname = bookName;
				}
			}
			_bookName = realname; //20120117, henrichen@zkoss.org: ZSS-81
			_sheetIdentifier = sheetIdentifier;
		}

		public String getBookName() {
			return _bookName;
		}

		public Identifier getSheetIdentifier() {
			return _sheetIdentifier;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer(64);
			sb.append(getClass().getName());
			sb.append(" [");
			if (_bookName != null) {
				sb.append(" [").append(_sheetIdentifier.getName()).append("]");
			}
			if (_sheetIdentifier.isQuoted()) {
				sb.append("'").append(_sheetIdentifier.getName()).append("'");
			} else {
				sb.append(_sheetIdentifier.getName());
			}
			sb.append("]");
			return sb.toString();
		}

		public String getSheetName() { //ZSS-790
			return _sheetIdentifier.getName();
		}
	}

	/**
	 * A1, $A1, A$1, $A$1, A, 1
	 */
	private static final class SimpleRangePart {
		private final Type _type;
		private final String _rep;

		public SimpleRangePart(String rep, boolean hasLetters, boolean hasNumbers, boolean hasUnder) {
			_rep = rep;
			_type = Type.get(hasLetters, hasNumbers, hasUnder);
		}

		public boolean isRef() {
			return _type == Type.REF;
		}

		public boolean isCell() {
			return _type == Type.CELL;
		}

		public boolean isRowOrColumn() {
			return _type != Type.CELL;
		}

		public CellReference getCellReference() {
			if (_type != Type.CELL) {
				throw new IllegalStateException("Not applicable to this type");
			}
			return new CellReference(_rep);
		}

		public boolean isColumn() {
			return _type == Type.COLUMN;
		}

		public boolean isRow() {
			return _type == Type.ROW;
		}

		public String getRep() {
			return _rep;
		}

		/**
		 * @return <code>true</code> if the two range parts can be combined in an
		 * {@link AreaPtg} ( Note - the explicit range operator (:) may still be valid
		 * when this method returns <code>false</code> )
		 */
		public boolean isCompatibleForArea(SimpleRangePart part2) {
			return _type == part2._type;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(64);
			sb.append(getClass().getName()).append(" [");
			sb.append(_rep);
			sb.append("]");
			return sb.toString();
		}

		private enum Type {
			CELL, ROW, COLUMN, REF;

			public static Type get(boolean hasLetters, boolean hasDigits, boolean hasUnder) {
				if (hasUnder) {
					return REF;
				}
				if (hasLetters) {
					return hasDigits ? CELL : COLUMN;
				}
				if (!hasDigits) {
					throw new IllegalArgumentException("must have either letters or numbers");
				}

				return ROW;
			}
		}
	}
}
