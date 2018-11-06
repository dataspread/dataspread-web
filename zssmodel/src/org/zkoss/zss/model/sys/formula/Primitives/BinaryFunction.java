package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;

import java.util.List;

public abstract class BinaryFunction {
    final static public BinaryFunction PLUS = new BinaryFunction(){

        @Override
        Double evaluate(Double a, Double b) {
            return a + b;
        }

        @Override
        Double invertedEvaluate(Double a, Double b) {
            return a - b;
        }

        @Override
        Double groupEvaluate(List<Double> values) {
            double sum = 0;
            for (int i=0, iSize=values.size(); i<iSize; i++) {
                sum += values.get(i);
            }
            return sum;
        }
    };
    final static public BinaryFunction COUNTPLUS = new BinaryFunction(){

        @Override
        Double evaluate(Double a, Double b) throws OptimizationError {
            throw OptimizationError.UNSUPPORTED_CASE;
        }

        @Override
        Double invertedEvaluate(Double a, Double b) {
            return a - b;
        }

        @Override
        Double groupEvaluate(List<Double> values) {
            double count = 0;
            for (int i=0, iSize=values.size(); i<iSize; i++) {
                if (values.get(i) != null)
                    count += 1;
            }
            return count;
        }
    };
    abstract Double evaluate(Double a, Double b) throws OptimizationError;
    abstract Double invertedEvaluate(Double a, Double b);
    Double groupEvaluate(List<Double> values) throws OptimizationError {
        Double sum = values.get(0);
        for (int i=1, iSize=values.size(); i<iSize; i++)
            sum = evaluate(sum, values.get(i));
        return sum;
    }
}
