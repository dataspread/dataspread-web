package org.zkoss.poi.ss.formula.eval;

/**
 * OverrideEval is used to mark when an operation such as AND or + needs to be evaluated differently.
 * This different evaluation is used for relational algebra operators.
 * e.g. SELECT(A1:A100, AND(Table_1.Col_1 + 10 > 0, TRUE))
 * <p>
 * Table_1.Col_1 + 10 needs to add 10 to each value in the column.
 * Similarly, AND needs to compute (columnValue + 10 > 0) && TRUE for each value.
 * <p>
 * Created by Danny on 11/1/2016.
 */
public class OverrideEval implements ValueEval {

    public OverrideEval() {

    }

}
