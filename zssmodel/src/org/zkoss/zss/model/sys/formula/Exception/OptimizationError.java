package org.zkoss.zss.model.sys.formula.Exception;

public class OptimizationError extends Exception {
    public static OptimizationError UNSUPPORTED_FUNCTION = new OptimizationError("Unsupported functionality");
    public static OptimizationError UNSUPPORTED_CASE = new OptimizationError("Unsupported case");
    public static OptimizationError UNSUPPORTED_TYPE = new OptimizationError("Unexpected type");
    public OptimizationError(String s) {
        super(s);
    }
}
