package org.zkoss.poi.ss.formula.functions;

import org.zkoss.poi.ss.formula.SheetRefEvaluator;
import org.zkoss.poi.ss.formula.TwoDEval;
import org.zkoss.poi.ss.formula.eval.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.zkoss.poi.ss.formula.functions.CountUtils.I_MatchPredicate;

/**
 * Abstract class which holds all of the relational operator functions.
 * Functions to be implemented here are:
 * Union, set difference, intersection, cartesian product, select, join, project, and rename.
 * Created by Danny on 9/22/2016.
 */
public abstract class RelationalOperatorFunction implements Function {


    /**
     * Implementation of the relational algebra operator UNION
     */
    public static final Function UNION = new TwoRangeFunction() {
        
        @Override
        protected ValueEval evaluate(ArrayEval range1, ArrayEval range2, int srcRowIndex, int srcColumnIndex) {
            
            try {
                
                validateUnionCompatible(range1, range2);

                List<Row> rows1 = Row.getRowsFromArea(range1);
                List<Row> rows2 = Row.getRowsFromArea(range2);

                Row[] combinedRows = new Row[rows1.size() + rows2.size()];
                
                int insertIndex = 0;
                
                //Add all rows from rows1
                for (int r = 0; r < rows1.size(); r++) {
                    combinedRows[insertIndex++] = rows1.get(r);
                }
                    
                //Add all rows from rows2
                for (int r = 0; r < rows2.size(); r++) {
                    combinedRows[insertIndex++] = rows2.get(r);
                }

                boolean[] indicesToKeep = getDistinctIndices(combinedRows);
                List<Row> resultRows = getRowsToKeep(indicesToKeep, combinedRows);

                //return evalHelper(resultRows);
                return Row.getArrayEval(resultRows, srcRowIndex, srcColumnIndex, range1.getRefEvaluator());
                
            }
            catch (EvaluationException e) {

                return e.getErrorEval();                

            }
        }


        /**
         * Helper method for UNION to determine which indices to keep (distinct rows)
         * @param rows
         * @return
         */
        private boolean[] getDistinctIndices(Row[] rows) {

            boolean[] indicesToKeep = new boolean[rows.length];
            Arrays.fill(indicesToKeep, true);

            for (int r1 = 0; r1 < rows.length; r1++) {
                for (int r2 = r1 + 1; r2 < rows.length; r2++) {

                    Row row1 = rows[r1];
                    Row row2 = rows[r2];

                    if (row1.matches(row2)) {

                        indicesToKeep[r2] = false;

                    }

                }//end r2 for loop
            }//end r1 for loop

            return indicesToKeep;

        }
    };


    /**
     * Implementation of the relational algebra operator SET DIFFERENCE
     */
    public static final Function DIFFERENCE = new TwoRangeFunction() {
        
        @Override
        protected ValueEval evaluate(ArrayEval range1, ArrayEval range2, int srcRowIndex, int srcColumnIndex) {

            try {

                validateUnionCompatible(range1, range2);

                List<Row> list1 = Row.getRowsFromArea(range1);
                List<Row> list2 = Row.getRowsFromArea(range2);
                Row[] rows1 = list1.toArray(new Row[list1.size()]);
                Row[] rows2 = list1.toArray(new Row[list2.size()]);
                
                boolean[] indicesToKeep = getIndicesNotInRows2(rows1, rows2);
                List<Row> resultRows = getRowsToKeep(indicesToKeep, rows1);

                //return evalHelper(resultRows);                
                return Row.getArrayEval(resultRows, srcRowIndex, srcColumnIndex, range1.getRefEvaluator());                

            }
            catch (EvaluationException e) {
                
                return e.getErrorEval();

            }
        }


        /**
         * Helper function for getRows1DiffRows2 to find the indices in rows1 that don't have
         * matching rows in rows2.
         * @param rows1
         * @param rows2
         * @return
         */
        private boolean[] getIndicesNotInRows2(Row[] rows1, Row[] rows2) {

            boolean[] indicesToKeep = new boolean[rows1.length];
            Arrays.fill(indicesToKeep, true);

            for (int r1 = 0; r1 < rows1.length; r1++) {
                for (int r2 = 0; r2 < rows1.length; r2++) {

                    Row row1 = rows1[r1];
                    Row row2 = rows2[r2];

                    //this row exists in the other range, so don't include it
                    if (row1.matches(row2)) {

                        indicesToKeep[r1] = false;
                        break;

                    }
                }//end r2 for loop
            }//end r1 for loop

            return indicesToKeep;

        }
    };


    /**
     * Implementation of relational algebra operator INTERSECTION
     */
    public static final Function INTERSECTION = new TwoRangeFunction() {
        
        @Override
        protected ValueEval evaluate(ArrayEval range1, ArrayEval range2, int srcRowIndex, int srcColumnIndex) {

            try {

                validateUnionCompatible(range1, range2);

                List<Row> list1 = Row.getRowsFromArea(range1);
                List<Row> list2 = Row.getRowsFromArea(range2);
                Row[] rows1 = list1.toArray(new Row[list1.size()]);
                Row[] rows2 = list1.toArray(new Row[list2.size()]);

                boolean[] indicesToKeep = getMatchingIndices(rows1, rows2);
                List<Row> resultRows = getRowsToKeep(indicesToKeep, rows1);

                //return evalHelper(resultRows);
                return Row.getArrayEval(resultRows, srcRowIndex, srcColumnIndex, range1.getRefEvaluator());
                

            }
            catch (EvaluationException e) {

                return e.getErrorEval();

            }
        }


        /**
         * Helper method for INTERSECTION to find the indices in rows1 that have matching rows in rows2
         * @param rows1
         * @param rows2
         * @return
         */
        private boolean[] getMatchingIndices(Row[] rows1, Row[] rows2) {

            boolean[] indicesToKeep = new boolean[rows1.length];
            Arrays.fill(indicesToKeep, false);

            for (int r1 = 0; r1 < rows1.length; r1++) {
                for (int r2 = 0; r2 < rows2.length; r2++) {

                    Row row1 = rows1[r1];
                    Row row2 = rows2[r2];

                    if (row1.matches(row2)) {
                        indicesToKeep[r1] = true;
                        break;
                    }

                }
            }

            return indicesToKeep;

        }
    };
    
    
    public static final Function CROSSPRODUCT = new TwoRangeFunction() {
        
        @Override
        protected ValueEval evaluate(ArrayEval range1, ArrayEval range2, int srcRowIndex, int srcColumnIndex) {

            try {

                validateUnionCompatible(range1, range2);

                List<Row> list1 = Row.getRowsFromArea(range1);
                List<Row> list2 = Row.getRowsFromArea(range2);
                Row[] rows1 = list1.toArray(new Row[list1.size()]);
                Row[] rows2 = list1.toArray(new Row[list2.size()]);

//                Row[] resultRows = new Row[ rows1.length * rows2.length ];
                List<Row> resultRows = new ArrayList<>();
                int insertIndex = 0;
                
                for (int r1 = 0; r1 <rows1.length; r1++) {
                    for (int r2 = 0; r2 < rows2.length; r2++) {

                        resultRows.add(Row.combineRows(rows1[r1], rows2[r2]));
                        
                    }
                }

                //return evalHelper(resultRows);
                return Row.getArrayEval(resultRows, srcRowIndex, srcColumnIndex, range1.getRefEvaluator());
                

            }
            catch (EvaluationException e) {

                return e.getErrorEval();

            }
        }
    };
    
    
    public static final Function SELECT = new SelectFunction() {
        
        //select with no conditions
        @Override
        protected ValueEval evaluate(AreaEval range, int srcRowIndex, int srcColumnIndex) {

            List<Row> rows = Row.getRowsFromArea(range);

            return Row.getArrayEval(rows, srcRowIndex, srcColumnIndex, range.getRefEvaluator());

//            return evalHelper(rows);
            
        }

        //select with conditions
        @Override
        protected ValueEval evaluate(AreaEval range, ValueEval condition, int srcRowIndex, int srcColumnIndex) {
            /** TODO
             *  how to reuse the evaluateFormula function in WorkbookEvaluator class
             *  just need to replace the value for the conditionPtg
             */
            List<Row> rows = Row.getRowsFromArea(range, condition);

            return Row.getArrayEval(rows, srcRowIndex, srcColumnIndex, range.getRefEvaluator());
//            return evalHelper(rows);
        }
    };
    
    
    public static final Function PROJECT = new RangeSchemaFunction() {
        
        @Override
        protected ValueEval evaluate(AreaEval range, String[] attributes, int srcRowIndex, int srcColumnIndex) {
            return new StringEval("not implemented");
        }
    };
    
    
    public static final Function RENAME = new RangeSchemaFunction() {
        
        @Override
        protected ValueEval evaluate(AreaEval range, String[] attributes, int srcRowIndex, int srcColumnIndex) {

            range.setAttributeNames(attributes);
            return range;

        }
    };   
    
    
    public static final Function JOIN = new JoinFunction() {
        
        @Override
        protected ValueEval evaluate(AreaEval range1, AreaEval range2, ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
            return new StringEval("not implemented");
        }
    };


    /**
     * Method to check that the two ranges are union compatible. 
     * i.e. they have same number of columns and same schema
     * @param range1
     * @param range2
     * @throws EvaluationException
     */
    private static void validateUnionCompatible(AreaEval range1, AreaEval range2) throws EvaluationException {

        //TODO: Compare schemas
        if (range1.getWidth() != range2.getWidth()) {

            throw EvaluationException.invalidValue();

        }
    }


    /**
     * Helper method for getRowsToKeep to find how many rows are going to be in the result
     * @param indicesToKeep
     * @return
     */
    private static int getNumToKeep(boolean[] indicesToKeep) {

        int numToKeep = 0;

        for (int i = 0; i < indicesToKeep.length; i++) {
            if (indicesToKeep[i]) {
                numToKeep++;
            }
        }

        return numToKeep;

    }


    /**
     * Helper method for relational operator functions to get the resulting set of rows
     * @param indicesToKeep
     * @param rows
     * @return
     */
    private static List<Row> getRowsToKeep(boolean[] indicesToKeep, Row[] rows) {

        List<Row> rowsToKeep = new ArrayList<>();

        for (int r = 0; r < indicesToKeep.length; r++) {

            if (indicesToKeep[r]) {
                rowsToKeep.add(rows[r]);
            }
        }

        return rowsToKeep;

    }


    /**
     * Method to convert the array of rows to a StringEval
     * @param rows
     * @return
     */
    private static ValueEval evalHelper(List<Row> rows) {
        
        String evalString = "";

        for (int i = 0; i < rows.size(); i++) {

            Row row = rows.get(i);
            if (row != null) {
                for (int col = 0; col < row.getLength(); col++) {
                    
                    evalString += getStringVal(row.getValue(col));
                    evalString += " ";

                }
                
                evalString += " | ";
            }            
        }
        
        return new StringEval(evalString);
    }

    
    /**
     * Helper method for evalHelper that gets a string of the value
     * @param eval
     * @return
     */
    private static String getStringVal (ValueEval eval) {
        
        if (eval instanceof NumberEval) {
            return ((NumberEval) eval).getStringValue();
        }
        else if (eval instanceof StringEval) {
            return ((StringEval) eval).getStringValue();
        }
        
        //TODO: Check other eval instances
        
        return null;
        
    }


    /**
     * Private class used to represent a row (array of ValueEval's)
     */
    private static class Row {
        
        private ValueEval[] values;
        
        public Row(ValueEval[] values) {
            
            this.values = values;
            
        }

        /**
         * Static method to combine row1 (a1, ..., an) and row2 (b1, ... , bn) into row3 (a1, ... , an, b1, ... , bn)
         * @param row1
         * @param row2
         * @return
         */
        public static Row combineRows(Row row1, Row row2) {
            
            ValueEval[] combinedRow = new ValueEval[ row1.getLength() + row2.getLength() ];
            
            int insertIndex = 0;
            
            for (int c = 0; c < row1.getLength(); c++) {
                combinedRow[insertIndex++] = row1.getValue(c);
            }
            
            for (int c = 0; c < row2.getLength(); c++) {
                combinedRow[insertIndex++] = row2.getValue(c);
            }
            
            return new Row(combinedRow);

        }


        public static ArrayEval getArrayEval(List<Row> rows, int firstRow, int firstColumn, SheetRefEvaluator evaluator) {

            int height = rows.size();

            if (height == 0) {
                ValueEval[][] values = new ValueEval[][]{{BlankEval.instance}};
                return new ArrayEval(values, firstRow, firstColumn, firstRow, firstColumn, evaluator);
            }

            ValueEval[][] values = new ValueEval[height][];


            for (int r = 0; r < height; r++) {

                Row row = rows.get(r);

                int width = row.getLength();
                values[r] = new ValueEval[width];

                for (int c = 0; c < width; c++) {
                    values[r][c] = row.getValue(c);
                }
            }

            return new ArrayEval(values, firstRow, firstColumn, firstRow + values.length - 1, firstColumn + values[0].length - 1, evaluator);
            
        }


        /**
         * Static method to create an array of Row's from an AreaEval
         * @param range
         * @return
         */
        public static List<Row> getRowsFromArea(AreaEval range) {
            List<Row> rows = new ArrayList<>();
            for (int r = 0; r < range.getHeight(); r++) {
                //get all of the ValueEval's for this row
                TwoDEval row = range.getRow(r);
                ValueEval[] rowEvals = new ValueEval[row.getWidth()];
                for (int c = 0; c < row.getWidth(); c++) {
                    rowEvals[c] = row.getValue(0, c);
                }
                rows.add(r, new Row(rowEvals));
            }
            return rows;
        }


        /**
         * Static method to create an array of Row's from an AreaEval
         *
         * @param range,conditions
         * @return
         */
        public static List<Row> getRowsFromArea(AreaEval range, ValueEval conditions) {
            List<Row> rows = new ArrayList<>();
            if (conditions instanceof BoolEval) {
                if (!((BoolEval) conditions).getBooleanValue()) return rows;
                else return getRowsFromArea(range);
            } else if (conditions instanceof ArrayEval) {
                ArrayEval cds = (ArrayEval) conditions;
                for (int r = 0; r < range.getHeight(); r++) {
                    ValueEval select = cds.getValue(r, 0);
                    if (select instanceof BoolEval) {
                        if (((BoolEval) select).getBooleanValue()) {
                            //get all of the ValueEval's for this row
                            TwoDEval row = range.getRow(r);
                            ValueEval[] rowEvals = new ValueEval[row.getWidth()];
                            for (int c = 0; c < row.getWidth(); c++) {
                                rowEvals[c] = row.getValue(0, c);
                            }
                            rows.add(new Row(rowEvals));
                        }
                    }
                }
            }
            return rows;
        }
        
        public ValueEval getValue(int col) {
            
            return values[col];
                        
        }
        
        public int getLength() {
            
            return values.length;
            
        }
        
        public boolean matches(Row row2) {

            Row row1 = this;
            
            if (row1.getLength() != row2.getLength()) {
                return false;
            }

            int width = row1.getLength();
            boolean matches = true;

            //Iterate over columns
            for (int c = 0; c < width; c++) {

                ValueEval value1 = row1.getValue(c);
                ValueEval value2 = row2.getValue(c);

                //arguments for srcCellRow and srcCellCol not used in createCriteriaPredicate, so just use 0, 0
                I_MatchPredicate matchPredicate = Countif.createCriteriaPredicate(value1, 0, 0);

                if (!matchPredicate.matches(value2)) {
                    matches = false;
                    break;
                }
            }

            return matches;
            
        }
        
    }//end Row class
    
    
}
