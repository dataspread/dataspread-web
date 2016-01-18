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

package org.zkoss.poi.ss.formula.functions;

import org.zkoss.poi.ss.formula.eval.ArrayEval;
import org.zkoss.poi.ss.formula.eval.BlankEval;
import org.zkoss.poi.ss.formula.eval.BoolEval;
import org.zkoss.poi.ss.formula.eval.ErrorEval;
import org.zkoss.poi.ss.formula.eval.EvaluationException;
import org.zkoss.poi.ss.formula.eval.NumberEval;
import org.zkoss.poi.ss.formula.eval.OperandResolver;
import org.zkoss.poi.ss.formula.eval.RefEval;
import org.zkoss.poi.ss.formula.eval.StringEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.poi.ss.formula.eval.ValuesEval;
import org.zkoss.poi.ss.formula.TwoDEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * This is the super class for all excel function evaluator
 * classes that take variable number of operands, and
 * where the order of operands does not matter
 */
public abstract class MultiOperandNumericFunction implements Function {

	private final boolean _isReferenceBoolCounted;
	private final boolean _isBlankCounted;

    protected MultiOperandNumericFunction(boolean isReferenceBoolCounted, boolean isBlankCounted) {
        _isReferenceBoolCounted = isReferenceBoolCounted;
        _isBlankCounted = isBlankCounted;
    }

	static final double[] EMPTY_DOUBLE_ARRAY = { };

	private static class DoubleList {
		private double[] _array;
		private int _count;

		public DoubleList() {
			_array = new double[8];
			_count = 0;
		}

		public double[] toArray() {
			if(_count < 1) {
				return EMPTY_DOUBLE_ARRAY;
			}
			double[] result = new double[_count];
			System.arraycopy(_array, 0, result, 0, _count);
			return result;
		}

		private void ensureCapacity(int reqSize) {
			if(reqSize > _array.length) {
				int newSize = reqSize * 3 / 2; // grow with 50% extra
				double[] newArr = new double[newSize];
				System.arraycopy(_array, 0, newArr, 0, _count);
				_array = newArr;
			}
		}

		public void add(double value) {
			ensureCapacity(_count + 1);
			_array[_count] = value;
			_count++;
		}
	}

	private static final int DEFAULT_MAX_NUM_OPERANDS = 30;

	public final ValueEval evaluate(ValueEval[] args, int srcCellRow, int srcCellCol) {

		double d;
		try {
			double[] values = getNumberArray(args);
			d = evaluate(values);
		} catch (EvaluationException e) {
			return e.getErrorEval();
		}

		if (Double.isNaN(d) || Double.isInfinite(d))
			return ErrorEval.NUM_ERROR;

		return new NumberEval(d);
	}

	protected abstract double evaluate(double[] values) throws EvaluationException;

	/**
	 * Maximum number of operands accepted by this function.
	 * Subclasses may override to change default value.
	 */
	protected int getMaxNumOperands() {
		return DEFAULT_MAX_NUM_OPERANDS;
	}

	/**
	 * Returns a double array that contains values for the numeric cells
	 * from among the list of operands. Blanks and Blank equivalent cells
	 * are ignored. Error operands or cells containing operands of type
	 * that are considered invalid and would result in #VALUE! error in
	 * excel cause this function to return <code>null</code>.
	 *
	 * @return never <code>null</code>
	 */
	protected final double[] getNumberArray(ValueEval[] operands) throws EvaluationException {
		if (operands.length > getMaxNumOperands()) {
			throw EvaluationException.invalidValue();
		}
		DoubleList retval = new DoubleList();

		for (int i=0, iSize=operands.length; i<iSize; i++) {
			collectValues(operands[i], retval);
		}
		return retval.toArray();
	}

    /**
     *  Whether to count nested subtotals.
     */
    public boolean isSubtotalCounted(){
        return true;
    }

	/**
	 * Collects values from a single argument
	 */
	private void collectValues(ValueEval operand, DoubleList temp) throws EvaluationException {

		//ZSS-933
		if (operand instanceof ValuesEval) {
			ValueEval[] ves= ((ValuesEval)operand).getValueEvals();
			for (ValueEval ve : ves) {
				collectValues(ve, temp);
			}
			return;
		}
		
		if (operand instanceof TwoDEval) {
			TwoDEval ae = (TwoDEval) operand;
			int width = ae.getWidth();
			int height = ae.getHeight();
			for (int rrIx=0; rrIx<height; rrIx++) {
				for (int rcIx=0; rcIx<width; rcIx++) {
					ValueEval ve = ae.getValue(rrIx, rcIx);
                    if(!isSubtotalCounted() && ae.isSubTotal(rrIx, rcIx)) continue;
                    if(!isHiddenCounted() && ae.isHidden(rrIx, rcIx)) continue; //ZSS-962
                    collectValue(ve, true, temp);
				}
			}
			return;
		}
		if (operand instanceof RefEval) {
			RefEval re = (RefEval) operand;
			if(!isHiddenCounted() && re.isHidden()) return; //ZSS-962
			collectValue(re.getInnerValueEval(), true, temp);
			return;
		}
		collectValue(operand, false, temp);
	}
	private void collectValue(ValueEval ve, boolean isViaReference, DoubleList temp)  throws EvaluationException {
		if (ve == null) {
			throw new IllegalArgumentException("ve must not be null");
		}
		if (ve instanceof NumberEval) {
			NumberEval ne = (NumberEval) ve;
			temp.add(ne.getNumberValue());
			return;
		}
		if (ve instanceof ErrorEval) {
			throw new EvaluationException((ErrorEval) ve);
		}
		if (ve instanceof StringEval) {
			if (isViaReference) {
				// ignore all ref strings
				return;
			}
			String s = ((StringEval) ve).getStringValue();
			Double d = OperandResolver.parseDouble(s);
			if(d == null) {
				throw new EvaluationException(ErrorEval.VALUE_INVALID);
			}
			temp.add(d.doubleValue());
			return;
		}
		if (ve instanceof BoolEval) {
			if (!isViaReference || _isReferenceBoolCounted) {
				BoolEval boolEval = (BoolEval) ve;
				temp.add(boolEval.getNumberValue());
			}
			return;
		}
		if (ve == BlankEval.instance) {
			if (_isBlankCounted) {
				temp.add(0.0);
			}
			return;
		}
		//henrichen@zkoss.org: handle multiple ValueEval from 3d area references
		if (ve instanceof ValuesEval) {
			ValueEval[] ves = ((ValuesEval) ve).getValueEvals();
			for(ValueEval xve : ves) {
				collectValue(xve, isViaReference, temp); //recursive
			}
			return;
		}
		//20111128, henrichen@zkoss.org: handle 2d evaluation
		if (ve instanceof ArrayEval) {
			ArrayEval ae = (ArrayEval) ve;
			final int rows = ae.getHeight();
			final int cols = ae.getWidth();
			for (int r = 0; r < rows; ++r) {
				for (int c = 0; c < cols; ++c) {
                    if(!isHiddenCounted() && ae.isHidden(r, c)) continue; //ZSS-962
					collectValue(ae.getValue(r, c), isViaReference, temp); //recursive
				}
			}
			return;
		}
		throw new RuntimeException("Invalid ValueEval type passed for conversion: ("
				+ ve.getClass() + ")");
	}

    /**
     *  Whether to count hidden reference
     *  @since 3.9.7
     */
	//ZSS-962
    public boolean isHiddenCounted(){
        return true;
    }
}
