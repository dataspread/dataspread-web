package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.poi.ss.formula.functions.Countif.CmpOp;
import org.zkoss.poi.ss.formula.ptg.*;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;

public abstract class FilterOperator extends PhysicalOperator {

    private static final int CRITERIA = 0;
    private static final int FILTERRANGE = 1;
    private static final int RETURNRANGE = 2;

    public static FilterOperator buildSingleFilter(LogicalOperator[] ops){

        if (!(ops[CRITERIA] instanceof SingleTransformOperator))
            throw OptimizationError.UNSUPPORTED_CASE;

        SingleTransformOperator criteria = (SingleTransformOperator) ops[CRITERIA];

        Ptg[] ptgs = criteria.ptgs;

        checkCriteriaPtg(ptgs);

        FilterOperator ret;

        String predicate = ((StringPtg)ptgs[0]).getValue();

        CmpOp op = CmpOp.getOperator(predicate);

        if (op == CmpOp.OP_EQ){
            if (criteria.inDegree() > 0){
                ret = new SingleEqualFilterOperator(
                        ((DataOperator)criteria.getFirstInEdge().getInVertex()).getRegion().getReferenceString());
                ret.transferInEdge(criteria.getFirstInEdge());
            }
            else {
                ret = new SingleEqualFilterOperator(predicate.substring(op.getLength()));
            }
        }
        else
            throw OptimizationError.UNSUPPORTED_CASE;



        return ret;

    }

    private static void checkCriteriaPtg(Ptg[] ptgs){
        if (ptgs.length > 3)
            throw OptimizationError.UNSUPPORTED_CASE;
        if (ptgs.length > 1 && !(ptgs[2] instanceof ConcatPtg && ptgs[1] instanceof RefPtgBase))
            throw OptimizationError.UNSUPPORTED_CASE;
        if (!(ptgs[0] instanceof StringPtg))
            throw OptimizationError.UNSUPPORTED_CASE;
    }


//    private static final class CmpOp {
//        public static final int NONE = 0;
//        public static final int EQ = 1;
//        public static final int NE = 2;
//        public static final int LE = 3;
//        public static final int LT = 4;
//        public static final int GT = 5;
//        public static final int GE = 6;
//
//        public static final CmpOp OP_NONE = op("", NONE);
//        public static final CmpOp OP_EQ = op("=", EQ);
//        public static final CmpOp OP_NE = op("<>", NE);
//        public static final CmpOp OP_LE = op("<=", LE);
//        public static final CmpOp OP_LT = op("<", LT);
//        public static final CmpOp OP_GT = op(">", GT);
//        public static final CmpOp OP_GE = op(">=", GE);
//        private final String _representation;
//        private final int _code;
//
//        private static CmpOp op(String rep, int code) {
//            return new CmpOp(rep, code);
//        }
//        private CmpOp(String representation, int code) {
//            _representation = representation;
//            _code = code;
//        }
//        /**
//         * @return number of characters used to represent this operator
//         */
//        public int getLength() {
//            return _representation.length();
//        }
//        public int getCode() {
//            return _code;
//        }
//        public static CmpOp getOperator(String value) {
//            int len = value.length();
//            if (len < 1) {
//                return OP_NONE;
//            }
//
//            char firstChar = value.charAt(0);
//
//            switch(firstChar) {
//                case '=':
//                    return OP_EQ;
//                case '>':
//                    if (len > 1) {
//                        switch(value.charAt(1)) {
//                            case '=':
//                                return OP_GE;
//                        }
//                    }
//                    return OP_GT;
//                case '<':
//                    if (len > 1) {
//                        switch(value.charAt(1)) {
//                            case '=':
//                                return OP_LE;
//                            case '>':
//                                return OP_NE;
//                        }
//                    }
//                    return OP_LT;
//            }
//            return OP_NONE;
//        }
//        public boolean evaluate(boolean cmpResult) {
//            switch (_code) {
//                case NONE:
//                case EQ:
//                    return cmpResult;
//                case NE:
//                    return !cmpResult;
//            }
//            throw new RuntimeException("Cannot call boolean evaluate on non-equality operator '"
//                    + _representation + "'");
//        }
//        public boolean evaluate(int cmpResult) {
//            switch (_code) {
//                case NONE:
//                case EQ:
//                    return cmpResult == 0;
//                case NE: return cmpResult != 0;
//                case LT: return cmpResult <  0;
//                case LE: return cmpResult <= 0;
//                case GT: return cmpResult >  0;
//                case GE: return cmpResult >= 0;
//            }
//            throw new RuntimeException("Cannot call boolean evaluate on non-equality operator '"
//                    + _representation + "'");
//        }
//        public String toString() {
//            StringBuffer sb = new StringBuffer(64);
//            sb.append(getClass().getName());
//            sb.append(" [").append(_representation).append("]");
//            return sb.toString();
//        }
//        public String getRepresentation() {
//            return _representation;
//        }
//    }

}
