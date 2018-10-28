package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.sys.formula.Primitives.Datastructure.DataWrapper;

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
        Double groupEvaluate(DataWrapper<Double> values) {
            double sum = 0;
            for (int i=0, iSize=values.size(); i<iSize; i++) {
                sum += values.get(i);
            }
            return sum;
        }
    };
    abstract Double evluate(Double a, Double b);
    abstract Double Invertedevluate(Double a, Double b);
    Double groupEvaluate(DataWrapper<Double> values){
        Double sum = values.get(0);
        for (int i=1, iSize=values.size(); i<iSize; i++)
            sum = evluate(sum, values.get(i));
        return sum;
    }
}
