package org.zkoss.poi.ss.formula.Primitives;

import org.zkoss.poi.ss.formula.functions.MathX;

public abstract class BinaryFunction {
    final static public BinaryFunction PLUS = new BinaryFunction(){

        @Override
        double evluate(double a, double b) {
            return a + b;
        }

        @Override
        double Invertedevluate(double a, double b) {
            return a - b;
        }

        @Override
        double groupEvaluate(double[] values) {
            return MathX.sum(values);
        }
    };
    abstract double evluate(double a, double b);
    abstract double Invertedevluate(double a, double b);
    abstract double groupEvaluate(double[] values);
}
