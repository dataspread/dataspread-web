package org.zkoss.poi.ss.formula.functions;

import org.zkoss.poi.ss.formula.eval.AreaEval;
import org.zkoss.poi.ss.formula.eval.EvaluationException;
import org.zkoss.poi.ss.formula.eval.StringEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;

/**
 * Abstract class which holds all of the relational operator functions.
 * Functions to be implemented here are:
 * Union, set difference, intersection, cartesian product, select, join, project, and rename.
 * Created by Danny on 9/22/2016.
 */
public abstract class RelationalOperatorFunction implements Function {
    
    
    public static final Function UNION = new TwoRangeFunction() {
        
        @Override
        protected ValueEval evaluate(AreaEval range1, AreaEval range2) {
            
            try {
                
                validateUnionCompatible(range1, range2);
                
            }
            catch (EvaluationException e) {
                
                return e.getErrorEval();
                
            }            
            
            return null;
        }
    };
    
    
    public static final Function DIFFERENCE = new TwoRangeFunction() {
        
        @Override
        protected ValueEval evaluate(AreaEval range1, AreaEval range2) {

            try {

                validateUnionCompatible(range1, range2);

            }
            catch (EvaluationException e) {
                
                return e.getErrorEval();

            }
            
            return null;
        }
    };
    
    
    private static void validateUnionCompatible(AreaEval range1, AreaEval range2) throws EvaluationException {
        
        if (range1.getWidth() != range2.getWidth()) {
            
            throw EvaluationException.invalidValue();
        
        }
    }
    
    
    public static final Function INTERSECTION = new TwoRangeFunction() {
        
        @Override
        protected ValueEval evaluate(AreaEval range1, AreaEval range2) {
            return null;
        }
    };
    
    
    public static final Function CROSSPRODUCT = new TwoRangeFunction() {
        
        @Override
        protected ValueEval evaluate(AreaEval range1, AreaEval range2) {
            return null;
        }
    };
    
    
    public static final Function SELECT = new SelectFunction() {
        
        //select with no conditions
        @Override
        protected ValueEval evaluate(AreaEval range) {
            return new StringEval("select");
        }

        //select with conditions
        @Override
        protected ValueEval evaluate(AreaEval range, ValueEval[] args) {
            return null;
        }
    };
    
    
    public static final Function PROJECT = new RangeSchemaFunction() {
        
        @Override
        protected ValueEval evaluate(AreaEval range, ValueEval[] args) {
            return null;
        }
    };
    
    
    public static final Function RENAME = new RangeSchemaFunction() {
        
        @Override
        protected ValueEval evaluate(AreaEval range, ValueEval[] args) {
            return null;
        }
    };   
    
    
    public static final Function JOIN = new JoinFunction() {
        
        @Override
        protected ValueEval evaluate(AreaEval range1, AreaEval range2, ValueEval[] args) {
            return null;
        }
    };
    
    
}
