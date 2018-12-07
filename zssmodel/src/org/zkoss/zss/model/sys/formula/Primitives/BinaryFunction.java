package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;

import java.util.List;

public abstract class BinaryFunction {

    private static int numberOfBinaryFunction = 0;

    final static public BinaryFunction PLUS = new Plus(numberOfBinaryFunction++);
    private static class Plus extends BinaryFunction{

        Plus(int id) {
            super(id);
        }

        @Override
        Double evaluate(Double a, Double b) {
            return a + b;
        }

        @Override
        Double invertedEvaluate(Double a, Double b) {
            return a - b;
        }

        @Override
        Double groupEvaluate(List values) {
            double sum = 0;
            for (Object v:values)
                if (v instanceof Double){
                    sum += (Double)v;
                }
            return sum;
        }

    }
    final static public BinaryFunction COUNTPLUS = new CountPlus(numberOfBinaryFunction++);
    private static class CountPlus extends BinaryFunction{

        CountPlus(int id) {
            super(id);
        }

        @Override
        Double evaluate(Double a, Double b) throws OptimizationError {
            return a + b;
        }

        @Override
        Double invertedEvaluate(Double a, Double b) {
            return a - b;
        }

        @Override
        Double groupEvaluate(List values) {
            double count = 0;
            for (Object v:values)
                if (v instanceof Double){
                    count += 1;
                }
            return count;
        }
    }

    boolean invertable = true;

    private int id;

    BinaryFunction(int id){
        this.id = id;
    }

    abstract Double evaluate(Double a, Double b) throws OptimizationError;

    abstract Double invertedEvaluate(Double a, Double b);

    Double groupEvaluate(List values) throws OptimizationError {
        Double sum=null;
        boolean firstValue = true;
        for (Object v:values)
            if (v instanceof Double){
                Double value = (Double)v;
                if (firstValue){
                    sum = value;
                    firstValue = false;
                }
                else
                    sum = evaluate(sum, value);
            }

        return sum;
    }

    int getId(){
        return id;
    }

    public static int getMaxId(){
        return numberOfBinaryFunction;
    }

    boolean isInvertable() {
        return invertable;
    }
}
