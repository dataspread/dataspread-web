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

import org.zkoss.poi.ss.formula.EvaluationWorkbook.ExternalSheet;
import org.zkoss.poi.ss.formula.ptg.*;
import org.zkoss.poi.ss.usermodel.ZssContext;

import java.util.Locale;
import java.util.Stack;

/**
 * Common logic for rendering formulas.<br/>
 *
 * For POI internal use only
 *
 * @author Josh Micich
 */
public class FormulaRenderer {

    /**
     * Static method to convert an array of {@link Ptg}s in RPN order
     * to a human readable string format in infix mode.
     * @param book  used for defined names and 3D references
     * @param ptgs  must not be <code>null</code>
     * @return a human readable String
     */
    public static String toFormulaString(FormulaRenderingWorkbook book, Ptg[] ptgs) {
        if (ptgs == null || ptgs.length == 0) {
            throw new IllegalArgumentException("ptgs must not be null");
        }
        Stack<String> stack = new Stack<String>();

        for (int i=0 ; i < ptgs.length; i++) {
            Ptg ptg = ptgs[i];
            // TODO - what about MemNoMemPtg?
            if(ptg instanceof MemAreaPtg || ptg instanceof MemFuncPtg || ptg instanceof MemErrPtg) {
                // marks the start of a list of area expressions which will be naturally combined
                // by their trailing operators (e.g. UnionPtg)
                // TODO - put comment and throw exception in toFormulaString() of these classes
                continue;
            }
            if (ptg instanceof FilterHelperPtg) {
                continue;
            }
            if (ptg instanceof ParenthesisPtg) {
            	//ZSS-747
            	//20140827, henrichen: (A1, B2, Sheet2!A1:B2) for chart formula
            	ParenthesisPtg parenPtg = (ParenthesisPtg) ptg;
                String[] operands = getOperands(stack, parenPtg.getNumberOfOperands());
                stack.push(parenPtg.toFormulaString(operands));
                continue;
            }
            if (ptg instanceof AttrPtg) {
                AttrPtg attrPtg = ((AttrPtg) ptg);
                if (attrPtg.isOptimizedIf() || attrPtg.isOptimizedChoose() || attrPtg.isSkip()) {
                    continue;
                }
                if (attrPtg.isSpace()) {
                    // POI currently doesn't render spaces in formulas
                    continue;
                    // but if it ever did, care must be taken:
                    // tAttrSpace comes *before* the operand it applies to, which may be consistent
                    // with how the formula text appears but is against the RPN ordering assumed here
                }
                if (attrPtg.isSemiVolatile()) {
                    // similar to tAttrSpace - RPN is violated
                    continue;
                }
                if (attrPtg.isSum()) {
                    String[] operands = getOperands(stack, attrPtg.getNumberOfOperands());
                    stack.push(attrPtg.toFormulaString(operands));
                    continue;
                }
                throw new RuntimeException("Unexpected tAttr: " + attrPtg.toString());
            }

            if (ptg instanceof WorkbookDependentFormula) {
                WorkbookDependentFormula optg = (WorkbookDependentFormula) ptg;
                stack.push(optg.toFormulaString(book));
                continue;
            }
            if (! (ptg instanceof OperationPtg)) {
                stack.push(ptg.toFormulaString());
                continue;
            }

            OperationPtg o = (OperationPtg) ptg;
            String[] operands = getOperands(stack, o.getNumberOfOperands());
            stack.push(o.toFormulaString(operands));
        }
        if(stack.isEmpty()) {
            // inspection of the code above reveals that every stack.pop() is followed by a
            // stack.push(). So this is either an internal error or impossible.
            throw new IllegalStateException("Stack underflow");
        }
        String result = stack.pop();
        if(!stack.isEmpty()) {
            // Might be caused by some tokens like AttrPtg and Mem*Ptg, which really shouldn't
            // put anything on the stack
            throw new IllegalStateException("too much stuff left on the stack");
        }
        return result;
    }

    private static String[] getOperands(Stack<String> stack, int nOperands) {
        String[] operands = new String[nOperands];

        for (int j = nOperands-1; j >= 0; j--) { // reverse iteration because args were pushed in-order
            if(stack.isEmpty()) {
               String msg = "Too few arguments supplied to operation. Expected (" + nOperands
                    + ") operands but got (" + (nOperands - j - 1) + ")";
                throw new IllegalStateException(msg);
            }
            operands[j] = stack.pop();
        }
        return operands;
    }


    //20120117, henrichen@zkoss.org: generate formula string to be stored in file
    //ZSS-81 Cannot input formula with proper external book name
    /**
     * Static method to convert an array of {@link Ptg}s in RPN order
     * to internal string format for storing into file.
     * @param book  used for defined names and 3D references
     * @param ptgs  must not be <code>null</code>
     * @return a human readable String
     */
    public static String toInternalFormulaString(FormulaRenderingWorkbook book, Ptg[] ptgs) {
        if (ptgs == null || ptgs.length == 0) {
            throw new IllegalArgumentException("ptgs must not be null");
        }
        Stack<String> stack = new Stack<String>();

        for (int i=0 ; i < ptgs.length; i++) {
            Ptg ptg = ptgs[i];
            // TODO - what about MemNoMemPtg?
            if(ptg instanceof MemAreaPtg || ptg instanceof MemFuncPtg || ptg instanceof MemErrPtg) {
                // marks the start of a list of area expressions which will be naturally combined
                // by their trailing operators (e.g. UnionPtg)
                // TODO - put comment and throw exception in toFormulaString() of these classes
                continue;
            }
            if (ptg instanceof ParenthesisPtg) {
                String contents = stack.pop();
                stack.push ("(" + contents + ")");
                continue;
            }
            if (ptg instanceof AttrPtg) {
                AttrPtg attrPtg = ((AttrPtg) ptg);
                if (attrPtg.isOptimizedIf() || attrPtg.isOptimizedChoose() || attrPtg.isSkip()) {
                    continue;
                }
                if (attrPtg.isSpace()) {
                    // POI currently doesn't render spaces in formulas
                    continue;
                    // but if it ever did, care must be taken:
                    // tAttrSpace comes *before* the operand it applies to, which may be consistent
                    // with how the formula text appears but is against the RPN ordering assumed here
                }
                if (attrPtg.isSemiVolatile()) {
                    // similar to tAttrSpace - RPN is violated
                    continue;
                }
                if (attrPtg.isSum()) {
                    String[] operands = getOperands(stack, attrPtg.getNumberOfOperands());
                    stack.push(attrPtg.toFormulaString(operands));
                    continue;
                }
                throw new RuntimeException("Unexpected tAttr: " + attrPtg.toString());
            }

            if (ptg instanceof WorkbookDependentFormula) {
                WorkbookDependentFormula optg = (WorkbookDependentFormula) ptg;
                stack.push(optg.toInternalFormulaString(book));
                continue;
            }
            if (ptg instanceof NumberPtg) { //ZSS-670
            	stack.push(((NumberPtg)ptg).toInternalFormulaString());
            	continue;
            }
            if (ptg instanceof ArrayPtg) { //ZSS-565
                stack.push(((ArrayPtg)ptg).toInternalFormulaString());
                continue;
            }
//            if (ptg instanceof TablePtg) { //ZSS-966: Table1 -> Table1[]
//            	stack.push(((TablePtg)ptg).toInternalFormulaString());
//            	continue;
//            }
            if (! (ptg instanceof OperationPtg)) {
                stack.push(ptg.toFormulaString());
                continue;
            }

            OperationPtg o = (OperationPtg) ptg;
            String[] operands = getOperands(stack, o.getNumberOfOperands());
            stack.push(o.toInternalFormulaString(operands)); //ZSS-565
        }
        if(stack.isEmpty()) {
            // inspection of the code above reveals that every stack.pop() is followed by a
            // stack.push(). So this is either an internal error or impossible.
            throw new IllegalStateException("Stack underflow");
        }
        String result = stack.pop();
        if(!stack.isEmpty()) {
            // Might be caused by some tokens like AttrPtg and Mem*Ptg, which really shouldn't
            // put anything on the stack
            throw new IllegalStateException("too much stuff left on the stack");
        }
        return result;
    }
    
    // ZSS-565: Support input with Swedish locale into formula
    // Render to formula string that can be display on formula bar (per current locale) 
	public static String toFormulaEditText(FormulaRenderingWorkbook book, Ptg[] tokens, String formula) {
		boolean hit = false; // only render if necessary
		for(Ptg token : tokens) {
			// check it is a external book reference or not
			if(token instanceof ExternSheetReferenceToken) {
				ExternSheetReferenceToken externalRef = (ExternSheetReferenceToken)token;
				ExternalSheet externalSheet = book.getExternalSheet(externalRef.getExternSheetIndex());
				if(externalSheet != null) {
					hit = true;
					break;
				}
			}
			//ZSS-565: Support input with Swedish locale into Formula
			if (token instanceof NumberPtg || token instanceof AbstractFunctionPtg || token instanceof ArrayPtg) {
				hit = true;
				break;
			}
		}
		return hit ? toFormulaString(book, tokens) : formula;
	}
	
	//ZSS-820
    /**
     * Static method to convert an RPN ptgs in the form of (areaPtg1, areaPtg2, ...) to Ptg[][].
     * @param book  used for defined names and 3D references
     * @param ptgs  must not be <code>null</code>
     * @return a human readable String
     * @since 3.9.6
     */
    public static Ptg[][] unwrapPtgArrays(Ptg[] ptgs) {
        if (ptgs == null || ptgs.length == 0) {
            throw new IllegalArgumentException("ptgs must not be null");
        }
        Stack<Object> stack = new Stack<Object>();

        for (int i=0 ; i < ptgs.length; i++) {
            Ptg ptg = ptgs[i];
            // TODO - what about MemNoMemPtg?
            if(ptg instanceof MemAreaPtg || ptg instanceof MemFuncPtg || ptg instanceof MemErrPtg) {
                // marks the start of a list of area expressions which will be naturally combined
                // by their trailing operators (e.g. UnionPtg)
                // TODO - put comment and throw exception in toFormulaString() of these classes
                continue;
            }
            if (ptg instanceof ParenthesisPtg) {
            	//ZSS-747
            	//20140827, henrichen: (A1, B2, Sheet2!A1:B2) for chart formula
            	ParenthesisPtg parenPtg = (ParenthesisPtg) ptg;
            	stack.push(getPtgs(stack, parenPtg, parenPtg.getNumberOfOperands()));
            	continue;
            }
            if (ptg instanceof AttrPtg) {
                AttrPtg attrPtg = ((AttrPtg) ptg);
                if (attrPtg.isOptimizedIf() || attrPtg.isOptimizedChoose() || attrPtg.isSkip()) {
                    continue;
                }
                if (attrPtg.isSpace()) {
                    // POI currently doesn't render spaces in formulas
                    continue;
                    // but if it ever did, care must be taken:
                    // tAttrSpace comes *before* the operand it applies to, which may be consistent
                    // with how the formula text appears but is against the RPN ordering assumed here
                }
                if (attrPtg.isSemiVolatile()) {
                    // similar to tAttrSpace - RPN is violated
                    continue;
                }
                if (attrPtg.isSum()) {
                    Ptg[] operands = getPtgs(stack, attrPtg, attrPtg.getNumberOfOperands());
                    stack.push(operands);
                    continue;
                }
                throw new RuntimeException("Unexpected tAttr: " + attrPtg.toString());
            }

            if (ptg instanceof WorkbookDependentFormula) {
                WorkbookDependentFormula optg = (WorkbookDependentFormula) ptg;
                stack.push(optg);
                continue;
            }
            if (! (ptg instanceof OperationPtg)) {
                stack.push(ptg);
                continue;
            }

            OperationPtg o = (OperationPtg) ptg;
            Ptg[] operands = getPtgs(stack, o, o.getNumberOfOperands());
            stack.push(operands);
        }
        if(stack.isEmpty()) {
            // inspection of the code above reveals that every stack.pop() is followed by a
            // stack.push(). So this is either an internal error or impossible.
            throw new IllegalStateException("Stack underflow");
        }
        Object result = stack.pop();
        if(!stack.isEmpty()) {
            // Might be caused by some tokens like AttrPtg and Mem*Ptg, which really shouldn't
            // put anything on the stack
            throw new IllegalStateException("too much stuff left on the stack");
        }
        return (Ptg[][]) result;
    }

    private static Ptg[] getPtgs(Stack<Object> stack, Ptg ptg, int nOperands) {
        Ptg[] operands = new Ptg[nOperands + 1];

        for (int j = nOperands-1; j >= 0; j--) { // reverse iteration because args were pushed in-order
            if(stack.isEmpty()) {
               String msg = "Too few arguments supplied to operation. Expected (" + nOperands
                    + ") operands but got (" + (nOperands - j - 1) + ")";
                throw new IllegalStateException(msg);
            }
            operands[j] = (Ptg) stack.pop();
        }
        operands[nOperands] = ptg;
        return operands;
    }

    // ZSS-1002: Support copy table related formula form inside table to outside table and vice versa
    // Render to formula string that can be used as the copy source(always in internal Locale)
    // @since 3.9.7
	public static String toFormulaCopyText(FormulaRenderingWorkbook book, Ptg[] tokens, String formula) {
		boolean hit = false; // only render if necessary
		for(Ptg token : tokens) {
			// check it is a external book reference or not
			if(token instanceof ExternSheetReferenceToken) {
				ExternSheetReferenceToken externalRef = (ExternSheetReferenceToken)token;
				ExternalSheet externalSheet = book.getExternalSheet(externalRef.getExternSheetIndex());
				if(externalSheet != null) {
					hit = true;
					break;
				}
			}
			//ZSS-1002
			if (token instanceof TablePtg) {
				hit = true;
				break;
			}
		}
		return hit ? toCopyFormulaString(book, tokens) : formula;
	}

	//ZSS-1002
    /**
     * Static method to convert an array of {@link Ptg}s in RPN order
     * to a human readable string format in infix mode for copy source
     * @param book  used for defined names and 3D references
     * @param ptgs  must not be <code>null</code>
     * @return a human readable String
     * @since 3.9.7
     */
    private static String toCopyFormulaString(FormulaRenderingWorkbook book, Ptg[] ptgs) {
		ZssContext old = ZssContext.getThreadLocal();
		try {
			ZssContext zssContext = old == null ? 
					new ZssContext(Locale.US,-1) : 
						new ZssContext(Locale.US, old.getTwoDigitYearUpperBound());
			ZssContext.setThreadLocal(zssContext);
    		return toCopyFormulaString0(book, ptgs);
    	} finally {
			ZssContext.setThreadLocal(old);
    	}
    }
    //ZSS-1002
    private static String toCopyFormulaString0(FormulaRenderingWorkbook book, Ptg[] ptgs) {
        if (ptgs == null || ptgs.length == 0) {
            throw new IllegalArgumentException("ptgs must not be null");
        }
        Stack<String> stack = new Stack<String>();

        for (int i=0 ; i < ptgs.length; i++) {
            Ptg ptg = ptgs[i];
            // TODO - what about MemNoMemPtg?
            if(ptg instanceof MemAreaPtg || ptg instanceof MemFuncPtg || ptg instanceof MemErrPtg) {
                // marks the start of a list of area expressions which will be naturally combined
                // by their trailing operators (e.g. UnionPtg)
                // TODO - put comment and throw exception in toFormulaString() of these classes
                continue;
            }
            if (ptg instanceof ParenthesisPtg) {
            	//ZSS-747
            	//20140827, henrichen: (A1, B2, Sheet2!A1:B2) for chart formula
            	ParenthesisPtg parenPtg = (ParenthesisPtg) ptg;
                String[] operands = getOperands(stack, parenPtg.getNumberOfOperands());
                stack.push(parenPtg.toFormulaString(operands));
                continue;
            }
            if (ptg instanceof AttrPtg) {
                AttrPtg attrPtg = ((AttrPtg) ptg);
                if (attrPtg.isOptimizedIf() || attrPtg.isOptimizedChoose() || attrPtg.isSkip()) {
                    continue;
                }
                if (attrPtg.isSpace()) {
                    // POI currently doesn't render spaces in formulas
                    continue;
                    // but if it ever did, care must be taken:
                    // tAttrSpace comes *before* the operand it applies to, which may be consistent
                    // with how the formula text appears but is against the RPN ordering assumed here
                }
                if (attrPtg.isSemiVolatile()) {
                    // similar to tAttrSpace - RPN is violated
                    continue;
                }
                if (attrPtg.isSum()) {
                    String[] operands = getOperands(stack, attrPtg.getNumberOfOperands());
                    stack.push(attrPtg.toFormulaString(operands));
                    continue;
                }
                throw new RuntimeException("Unexpected tAttr: " + attrPtg.toString());
            }
            if (ptg instanceof TablePtg) { //ZSS-1002: must before WorkbookDependentFormula (Area3DPtg)
            	stack.push(((TablePtg)ptg).toCopyFormulaString());
            	continue;
            }

            if (ptg instanceof WorkbookDependentFormula) {
                WorkbookDependentFormula optg = (WorkbookDependentFormula) ptg;
                stack.push(optg.toFormulaString(book));
                continue;
            }
            if (! (ptg instanceof OperationPtg)) {
                stack.push(ptg.toFormulaString());
                continue;
            }

            OperationPtg o = (OperationPtg) ptg;
            String[] operands = getOperands(stack, o.getNumberOfOperands());
            stack.push(o.toFormulaString(operands));
        }
        if(stack.isEmpty()) {
            // inspection of the code above reveals that every stack.pop() is followed by a
            // stack.push(). So this is either an internal error or impossible.
            throw new IllegalStateException("Stack underflow");
        }
        String result = stack.pop();
        if(!stack.isEmpty()) {
            // Might be caused by some tokens like AttrPtg and Mem*Ptg, which really shouldn't
            // put anything on the stack
            throw new IllegalStateException("too much stuff left on the stack");
        }
        return result;
    }
}
