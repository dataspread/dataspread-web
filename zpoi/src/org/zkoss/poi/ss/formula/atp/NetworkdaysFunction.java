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


package org.zkoss.poi.ss.formula.atp;

import org.zkoss.poi.ss.formula.OperationEvaluationContext;
import org.zkoss.poi.ss.formula.eval.ErrorEval;
import org.zkoss.poi.ss.formula.eval.EvaluationException;
import org.zkoss.poi.ss.formula.eval.NumberEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.poi.ss.formula.functions.FreeRefFunction;

/**
 * Implementation of Excel 'Analysis ToolPak' function NETWORKDAYS()<br/>
 * Returns the number of workdays given a starting and an ending date, considering an interval of holidays. A workday is any non
 * saturday/sunday date.
 * <p/>
 * <b>Syntax</b><br/>
 * <b>NETWORKDAYS</b>(<b>startDate</b>, <b>endDate</b>, holidays)
 * <p/>
 * 
 * @author jfaenomoto@gmail.com
 */
final class NetworkdaysFunction implements FreeRefFunction {

    public static final FreeRefFunction instance = new NetworkdaysFunction(ArgumentsEvaluator.instance);

    private ArgumentsEvaluator evaluator;

    /**
     * Constructor.
     * 
     * @param anEvaluator an injected {@link ArgumentsEvaluator}.
     */
    private NetworkdaysFunction(ArgumentsEvaluator anEvaluator) {
        // enforces singleton
        this.evaluator = anEvaluator;
    }

    /**
     * Evaluate for NETWORKDAYS. Given two dates and a optional date or interval of holidays, determines how many working days are there
     * between those dates.
     * When start date is later than end date, it returns an invalid result. (In Excel, the result is negative number.)
     * The formula treats empty cell as 0. (In Excel, empty cell is 1.)
     * @return {@link ValueEval} for the number of days between two dates.
     */
    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length < 2 || args.length > 3) {
            return ErrorEval.VALUE_INVALID;
        }

        int srcCellRow = ec.getRowIndex();
        int srcCellCol = ec.getColumnIndex();

        double start, end;
        double[] holidays;
        try {
            start = this.evaluator.evaluateDateArg(args[0], srcCellRow, srcCellCol);
            end = this.evaluator.evaluateDateArg(args[1], srcCellRow, srcCellCol);
            if (start > end) {
                return ErrorEval.VALUE_INVALID;
            }
            ValueEval holidaysCell = args.length == 3 ? args[2] : null;
            holidays = this.evaluator.evaluateDatesArg(holidaysCell, srcCellRow, srcCellCol);
            return new NumberEval(WorkdayCalculator.instance.calculateWorkdays(start, end, holidays));
        } catch (EvaluationException e) {
            return ErrorEval.VALUE_INVALID;
        }
    }
}
