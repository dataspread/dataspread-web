package org.zkoss.poi.ss.formula.functions;

import org.zkoss.poi.ss.formula.eval.ValueEval;

/**
 * Abstract class to be inherited by join function in RelationalOperatorFunction.
 * Created by Danny on 9/22/2016.
 */
public abstract class JoinFunction implements Function {

    public final ValueEval evaluate (ValueEval[] args, int srcCellRow, int srcCellCol) {
        
        return null;
        
    }
    
}
