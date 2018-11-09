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
        Double groupEvaluate(List<Double> values) {
            double sum = 0;
            for (int i=0, iSize=values.size(); i<iSize; i++) {
                sum += values.get(i);
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
        Double groupEvaluate(List<Double> values) {
            double count = 0;
            for (int i=0, iSize=values.size(); i<iSize; i++) {
                if (values.get(i) != null)
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

    Double groupEvaluate(List<Double> values) throws OptimizationError {
        Double sum = values.get(0);
        for (int i=1, iSize=values.size(); i<iSize; i++)
            sum = evaluate(sum, values.get(i));
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
