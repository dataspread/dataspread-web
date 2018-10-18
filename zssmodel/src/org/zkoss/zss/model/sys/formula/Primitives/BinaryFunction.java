package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.poi.ss.formula.functions.MathX;

public abstract class BinaryFunction {
    final static public BinaryFunction PLUS = new BinaryFunction(){

        @Override
        Double evluate(Double a, Double b) {
            return a + b;
        }

        @Override
        Double Invertedevluate(Double a, Double b) {
            return a - b;
        }

        @Override
        Double groupEvaluate(Double[] values) {
            double sum = 0;
            for (int i=0, iSize=values.length; i<iSize; i++) {
                sum += values[i];
            }
            return sum;
        }
    };
    abstract Double evluate(Double a, Double b);
    abstract Double Invertedevluate(Double a, Double b);
    abstract Double groupEvaluate(Double[] values);
}
