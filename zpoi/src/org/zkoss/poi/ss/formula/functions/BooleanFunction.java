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

import org.zkoss.poi.ss.formula.TwoDEval;
import org.zkoss.poi.ss.formula.eval.*;

import java.util.Arrays;

/**
 * Here are the general rules concerning Boolean functions:
 * <ol>
 * <li> Blanks are ignored (not either true or false) </li>
 * <li> Strings are ignored if part of an area ref or cell ref, otherwise they must be 'true' or 'false'</li>
 * <li> Numbers: 0 is false. Any other number is TRUE </li>
 * <li> Areas: *all* cells in area are evaluated according to the above rules</li>
 * </ol>
 *
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
public abstract class BooleanFunction implements Function {

    public static final Function AND = new BooleanFunction() {
        protected boolean getInitialResultValue() {
            return true;
        }

        protected boolean partialEvaluate(boolean cumulativeResult, boolean currentValue) {
            return cumulativeResult && currentValue;
        }
    };
    public static final Function OR = new BooleanFunction() {
        protected boolean getInitialResultValue() {
            return false;
        }

        protected boolean partialEvaluate(boolean cumulativeResult, boolean currentValue) {
            return cumulativeResult || currentValue;
        }
    };
    public static final Function FALSE = new Fixed0ArgFunction() {
        public ValueEval evaluate(int srcRowIndex, int srcColumnIndex) {
            return BoolEval.FALSE;
        }
    };
    public static final Function TRUE = new Fixed0ArgFunction() {
        public ValueEval evaluate(int srcRowIndex, int srcColumnIndex) {
            return BoolEval.TRUE;
        }
    };
    public static final Function NOT = new BooleanFunction.OneArg() {
        public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
            boolean boolArgVal;
            try {
                ValueEval ve = OperandResolver.getSingleValue(arg0, srcRowIndex, srcColumnIndex);
                Boolean b = OperandResolver.coerceValueToBoolean(ve, false);
                boolArgVal = b != null && b.booleanValue();
            } catch (EvaluationException e) {
                return e.getErrorEval();
            }

            return BoolEval.valueOf(!boolArgVal);
        }
    };

	public final ValueEval evaluate(ValueEval[] args, int srcRow, int srcCol) {
		if (args.length < 1) {
			return ErrorEval.VALUE_INVALID;
		}

        // if arg[0] is OverrideEval, we have to use override_calculate
        if (args[0] instanceof OverrideEval) {

            boolean[][] result;
            AreaEval ae;

            try {
                args = Arrays.copyOfRange(args, 1, args.length);
                ae = validateAreas(args);
                result = override_calculate(args);

            } catch (EvaluationException e) {
                return e.getErrorEval();
            }

            //if just a single boolean
            if (result.length == 1 && result[0].length == 1) {
                return BoolEval.valueOf(result[0][0]);
            }

            ValueEval[][] result_evals = new ValueEval[result.length][];

            for (int r = 0; r < result.length; r++) {

                result_evals[r] = new ValueEval[result[r].length];

                for (int c = 0; c < result[r].length; c++) {
                    result_evals[r][c] = BoolEval.valueOf(result[r][c]);
                }
            }

            return new ArrayEval(result_evals, ae.getFirstRow(), ae.getFirstColumn(), ae.getLastRow(), ae.getLastColumn(), ae.getRefEvaluator());

        }

        boolean boolResult;
        try {

            boolResult = calculate(args);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
        return BoolEval.valueOf(boolResult);
    }

    /**
     * If there are areas as arguments to the overridden function, their dimensions need to match
     *
     * @param args
     * @throws EvaluationException
     */
    private AreaEval validateAreas(ValueEval[] args) throws EvaluationException {

        AreaEval prevArea = null;

        for (ValueEval arg : args) {
            if (arg instanceof AreaEval) {
                AreaEval ae = (AreaEval) arg;
                if (prevArea != null) {
                    if (prevArea.getHeight() != ae.getHeight() || prevArea.getWidth() != ae.getWidth()) {
                        throw new EvaluationException(ErrorEval.VALUE_INVALID);
                    }
                }
                prevArea = ae;
            }
        }

        return prevArea;
    }

    /**
     * args have already been validated, so just find an area's dimensions if there is an area.
     *
     * @param args
     * @return
     */
    private int[] getWidthHeight(ValueEval[] args) {

        for (ValueEval arg : args) {
            if (arg instanceof TwoDEval) {
                TwoDEval ae = (TwoDEval) arg;
                return new int[]{ae.getWidth(), ae.getHeight()};
            }
        }

        return new int[]{1, 1};

    }

    /**
     * Override for boolean functions that compares the corresponding values of multiple areas.
     *
     * @param args
     * @return
     * @throws EvaluationException
     */
    private boolean[][] override_calculate(ValueEval[] args) throws EvaluationException {

        int[] widthHeight = getWidthHeight(args);
        int cols = widthHeight[0];
        int rows = widthHeight[1];

        boolean[][] result = new boolean[rows][cols];
        boolean atleastOneNonBlank = false;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                boolean row_col_result = getInitialResultValue();

                for (ValueEval arg : args) {

                    if (arg instanceof TwoDEval) {

                        TwoDEval ae = (TwoDEval) arg;
                        ValueEval ve = ae.getValue(r, c);
                        Boolean tempVe = OperandResolver.coerceValueToBoolean(ve, true);
                        if (tempVe != null) {
                            row_col_result = partialEvaluate(tempVe, row_col_result);
                            atleastOneNonBlank = true;
                        }

                        continue;
                    }

                    Boolean tempVe;
                    if (arg instanceof RefEval) {
                        ValueEval ve = ((RefEval) arg).getInnerValueEval();
                        tempVe = OperandResolver.coerceValueToBoolean(ve, true);
                    } else if (arg == MissingArgEval.instance) {
                        tempVe = null;        // you can leave out parameters, they are simply ignored
                    } else {
                        tempVe = OperandResolver.coerceValueToBoolean(arg, false);
                    }

                    if (tempVe != null) {
                        row_col_result = partialEvaluate(row_col_result, tempVe);
                        atleastOneNonBlank = true;
                    }

                } //end for arg

                result[r][c] = row_col_result;

            } //end for column
        } //end for row

        if (!atleastOneNonBlank) {
            throw new EvaluationException(ErrorEval.VALUE_INVALID);
        }

        return result;

    }

	private boolean calculate(ValueEval[] args) throws EvaluationException {

		boolean result = getInitialResultValue();
		boolean atleastOneNonBlank = false;

		/*
		 * Note: no short-circuit boolean loop exit because any ErrorEvals will override the result
		 */
		for (int i=0, iSize=args.length; i<iSize; i++) {
			ValueEval arg = args[i];
			if (arg instanceof TwoDEval) {
				TwoDEval ae = (TwoDEval) arg;
				int height = ae.getHeight();
				int width = ae.getWidth();
				for (int rrIx=0; rrIx<height; rrIx++) {
					for (int rcIx=0; rcIx<width; rcIx++) {
						ValueEval ve = ae.getValue(rrIx, rcIx);
						Boolean tempVe = OperandResolver.coerceValueToBoolean(ve, true);
						if (tempVe != null) {
							result = partialEvaluate(result, tempVe.booleanValue());
							atleastOneNonBlank = true;
						}
					}
				}
				continue;
			}
			Boolean tempVe;
			if (arg instanceof RefEval) {
				ValueEval ve = ((RefEval) arg).getInnerValueEval();
				tempVe = OperandResolver.coerceValueToBoolean(ve, true);
			} else if (arg == MissingArgEval.instance) {
				tempVe = null;		// you can leave out parameters, they are simply ignored
			} else {
				tempVe = OperandResolver.coerceValueToBoolean(arg, false);
			}


			if (tempVe != null) {
				result = partialEvaluate(result, tempVe.booleanValue());
				atleastOneNonBlank = true;
			}
		}

		if (!atleastOneNonBlank) {
			throw new EvaluationException(ErrorEval.VALUE_INVALID);
		}
		return result;
	}

	protected abstract boolean getInitialResultValue();

    protected abstract boolean partialEvaluate(boolean cumulativeResult, boolean currentValue);

    public static abstract class OneArg extends Fixed1ArgFunction implements Operator {
        protected OneArg() {
            // no fields to initialise
		}
	}
}
