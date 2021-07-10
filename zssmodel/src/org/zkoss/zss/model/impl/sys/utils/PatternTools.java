package org.zkoss.zss.model.impl.sys.utils;

import org.zkoss.util.Pair;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.util.Arrays;
import java.util.List;

public class PatternTools {
    private final static int SHIFT_STEP = 1;
    private final static int FIRST_ROW = 0;
    private final static int FIRST_COL = 0;

    public static boolean isCompressibleTypeOne(Ref lastCandPrec, Ref prec,
                                         Direction direction) {
        Ref shiftedRef = shiftRef(lastCandPrec, direction);
        return shiftedRef != null && shiftedRef.equals(prec);
    }

    // Only called after isCompressibleTypeOne is true
    public static boolean isCompressibleTypeZero(Ref prec, Ref dep,
                                                 Ref lastCandPrec) {
        boolean isTypeZero = false;
        for (Direction direction: Direction.values()) {
            if (direction != Direction.NODIRECTION && !isTypeZero) {
                Ref shiftedRef = shiftRef(prec, direction);
                if (shiftedRef != null && shiftedRef.equals(dep)) { // check adjacency
                    Ref lastCandDep = shiftRef(lastCandPrec, direction);
                    isTypeZero = (lastCandDep != null && lastCandDep.equals(prec)) ||
                            lastCandPrec.equals(dep);
                }
            }
        }
        return isTypeZero;
    }

    // Relative start, fixed end
    public static boolean isCompressibleTypeTwo(Ref lastCandPrec, Ref prec,
                                         Direction direction) {
        if (direction == Direction.TODOWN || direction == Direction.TORIGHT)
            return isExtendedEnd(lastCandPrec, prec, direction);
        else
            return isShrinkedEnd(lastCandPrec, prec, direction);
    }

    public static boolean isCompressibleTypeThree(Ref lastCandPrec, Ref prec,
                                           Direction direction) {
        if (direction == Direction.TODOWN || direction == Direction.TORIGHT)
            return isShrinkedStart(lastCandPrec, prec, direction);
        else
            return isExtendedStart(lastCandPrec, prec, direction);
    }

    public static boolean isExtendedEnd(Ref lastCandPrec, Ref prec,
                                 Direction direction) {
        if (direction == Direction.TODOWN) {
            return (lastCandPrec.getRow() == prec.getRow() &&
                    lastCandPrec.getColumn() == prec.getColumn() &&
                    lastCandPrec.getLastRow() + 1 == prec.getLastRow() &&
                    lastCandPrec.getLastColumn() == prec.getLastColumn());
        } else if (direction == Direction.TORIGHT) {
            return (lastCandPrec.getRow() == prec.getRow() &&
                    lastCandPrec.getColumn() == prec.getColumn() &&
                    lastCandPrec.getLastRow() == prec.getLastRow() &&
                    lastCandPrec.getLastColumn() + 1 == prec.getLastColumn());
        } else return false;
    }

    public static boolean isShrinkedEnd(Ref lastCandPrec, Ref prec,
                                 Direction direction) {
        if (direction == Direction.TOUP) {
            return (lastCandPrec.getRow() == prec.getRow() &&
                    lastCandPrec.getColumn() == prec.getColumn() &&
                    lastCandPrec.getLastRow() - 1 == prec.getLastRow() &&
                    lastCandPrec.getLastColumn() == prec.getLastColumn());
        } else if (direction == Direction.TOLEFT) {
            return (lastCandPrec.getRow() == prec.getRow() &&
                    lastCandPrec.getColumn() == prec.getColumn() &&
                    lastCandPrec.getLastRow() == prec.getLastRow() &&
                    lastCandPrec.getLastColumn() - 1 == prec.getLastColumn());
        } else return false;
    }

    public static boolean isShrinkedStart(Ref lastCandPrec, Ref prec,
                                   Direction direction) {
        if (direction == Direction.TODOWN) {
            return (lastCandPrec.getRow() - 1 == prec.getRow() &&
                    lastCandPrec.getColumn() == prec.getColumn() &&
                    lastCandPrec.getLastRow() == prec.getLastRow() &&
                    lastCandPrec.getLastColumn() == prec.getLastColumn());
        } else if (direction == Direction.TORIGHT) {
            return (lastCandPrec.getRow() == prec.getRow() &&
                    lastCandPrec.getColumn() - 1 == prec.getColumn() &&
                    lastCandPrec.getLastRow() == prec.getLastRow() &&
                    lastCandPrec.getLastColumn() == prec.getLastColumn());
        } else return false;
    }

    public static boolean isExtendedStart(Ref lastCandPrec, Ref prec,
                                 Direction direction) {
        if (direction == Direction.TOUP) {
            return (lastCandPrec.getRow() + 1 == prec.getRow() &&
                    lastCandPrec.getColumn() == prec.getColumn() &&
                    lastCandPrec.getLastRow() == prec.getLastRow() &&
                    lastCandPrec.getLastColumn() == prec.getLastColumn());
        } else if (direction == Direction.TOLEFT) {
            return (lastCandPrec.getRow() == prec.getRow() &&
                    lastCandPrec.getColumn() + 1 == prec.getColumn() &&
                    lastCandPrec.getLastRow() == prec.getLastRow() &&
                    lastCandPrec.getLastColumn() == prec.getLastColumn());
        } else return false;
    }

    public static boolean isCompressibleTypeFour(Ref lastCandPrec, Ref prec) {
        return lastCandPrec.equals(prec);
    }

    public static Ref shiftRef(Ref ref, Direction direction) {
        Ref res = null;
        switch (direction) {
            case TOLEFT:
                if (ref.getColumn() != FIRST_COL) {
                    res = new RefImpl(ref.getBookName(),
                            ref.getSheetName(),
                            ref.getRow(), ref.getColumn() - SHIFT_STEP,
                            ref.getLastRow(), ref.getLastColumn() - SHIFT_STEP);
                }
                break;
            case TORIGHT:
                res = new RefImpl(ref.getBookName(),
                        ref.getSheetName(),
                        ref.getRow(), ref.getColumn() + SHIFT_STEP,
                        ref.getLastRow(), ref.getLastColumn() + SHIFT_STEP);
                break;
            case TOUP:
                if (ref.getRow() != FIRST_ROW) {
                    res = new RefImpl(ref.getBookName(),
                            ref.getSheetName(),
                            ref.getRow() - SHIFT_STEP, ref.getColumn(),
                            ref.getLastRow() - SHIFT_STEP, ref.getLastColumn());
                }
                break;
            default: // TODOWN
                res = new RefImpl(ref.getBookName(),
                        ref.getSheetName(),
                        ref.getRow() + SHIFT_STEP, ref.getColumn(),
                        ref.getLastRow() + SHIFT_STEP, ref.getLastColumn());
        }
        return res;
    }

    // what is the adjacency direction of refA relative to refB
    public static Direction findAdjacencyDirection(Ref refA, Ref refB) {
        int adjFirstRow = refA.getRow();
        int adjFirstCol = refA.getColumn();
        int adjLastRow = refA.getLastRow();
        int adjLastCol = refA.getLastColumn();

        int firstRow = refB.getRow();
        int firstCol = refB.getColumn();
        int lastRow = refB.getLastRow();
        int lastCol = refB.getLastColumn();

        if (adjFirstRow == firstRow && adjLastRow == lastRow
                && adjLastCol + SHIFT_STEP == firstCol) { // To Left
            return Direction.TOLEFT;
        } else if (adjFirstRow == firstRow && adjLastRow == lastRow
                && lastCol + SHIFT_STEP == adjFirstCol) { // To Right
            return Direction.TORIGHT;
        } else if (adjFirstCol == firstCol && adjLastCol == lastCol
                && adjLastRow + SHIFT_STEP == firstRow)
            return Direction.TOUP;
        else if (adjFirstCol == firstCol && adjLastCol == lastCol
                && lastRow + SHIFT_STEP == adjFirstRow)
            return Direction.TODOWN;
        else
            return Direction.NODIRECTION;
    }

    public static boolean isValidAdjacency(Ref adjRef, Ref ref) {
        return findAdjacencyDirection(adjRef, ref) != Direction.NODIRECTION;
    }

    public static Ref findUpdateDepRef(Ref prec, Ref dep,
                                       EdgeMeta edgeMeta, Ref precRange) {
        int row = -1;
        int col = -1;
        int lastRow = -1;
        int lastCol = -1;

        int startRowOffset = edgeMeta.startOffset.getRowOffset();
        int startColOffset = edgeMeta.startOffset.getColOffset();
        int endRowOffset = edgeMeta.endOffset.getRowOffset();
        int endColOffset = edgeMeta.endOffset.getColOffset();

        PatternType patternType;
        if (edgeMeta.patternType != PatternType.TYPEZERO)
            patternType = edgeMeta.patternType;
        else { // TYPEZERO
            if (startRowOffset == 1 || startColOffset == 1) patternType = PatternType.TYPETWO;
            else if (startRowOffset == -1 || startColOffset == -1) patternType = PatternType.TYPETHREE;
            else throw new RuntimeException("TYPE ZERO offset (" + startRowOffset + ","
                        + startColOffset + ") wrong");
        }

        switch (patternType) {
            case TYPEONE: // relative start, relative end
                row = precRange.getRow() + startRowOffset;
                col = precRange.getColumn() + startColOffset;
                lastRow = precRange.getLastRow() + endRowOffset;
                lastCol = precRange.getLastColumn() + endColOffset;
                break;

            case TYPETWO: // relative start, fixed end
                row = precRange.getRow() + startRowOffset;
                col = precRange.getColumn() + startColOffset;
                lastRow = dep.getLastRow();
                lastCol = dep.getLastColumn();
                break;

            case TYPETHREE: // fixed start, relative end
                row = dep.getRow();
                col = dep.getColumn();
                lastRow = precRange.getLastRow() + endRowOffset;
                lastCol = precRange.getLastColumn() + endColOffset;
                break;

            default: //case TYPEFOUR: fixed start, fixed end
                     //case NOTYPE
                row = dep.getRow();
                col = dep.getColumn();
                lastRow = dep.getLastRow();
                lastCol = dep.getLastColumn();
                break;
        }

        assert edgeMeta.patternType != PatternType.NOTYPE || (row == lastRow && col == lastCol);

        return new RefImpl(
                prec.getBookName(),
                prec.getSheetName(),
                row, col, lastRow, lastCol);
    }

    public static Ref findUpdatePrecRef(Ref prec, Ref dep,
                                        EdgeMeta edgeMeta, Ref depRange,
                                        boolean isDirectPrec) {
        int row = -1;
        int col = -1;
        int lastRow = -1;
        int lastCol = -1;

        int startRowOffset = edgeMeta.startOffset.getRowOffset();
        int startColOffset = edgeMeta.startOffset.getColOffset();
        int endRowOffset = edgeMeta.endOffset.getRowOffset();
        int endColOffset = edgeMeta.endOffset.getColOffset();

        PatternType patternType;
        if (edgeMeta.patternType != PatternType.TYPEZERO)
            patternType = edgeMeta.patternType;
        else { // TYPEZERO
            if (isDirectPrec) patternType = PatternType.TYPEONE;
            else if (startRowOffset == 1 || startColOffset == 1) patternType = PatternType.TYPETWO;
            else if (startRowOffset == -1 || startColOffset == -1) patternType = PatternType.TYPETHREE;
            else throw new RuntimeException("TYPE ZERO offset (" + startRowOffset + ","
                        + startColOffset + ") wrong");
        }

        switch (patternType) {

            case TYPEONE: // relative start, relative end
                row = depRange.getRow() - endRowOffset;
                col = depRange.getColumn() - endColOffset;
                lastRow = depRange.getLastRow() - startRowOffset;
                lastCol = depRange.getLastColumn() - startColOffset;
                break;

            case TYPETWO: // relative start, fixed end
                row = prec.getRow();
                col = prec.getColumn();
                lastRow = depRange.getLastRow() - startRowOffset;
                lastCol = depRange.getLastColumn() - startColOffset;
                break;

            case TYPETHREE: // fixed start, relative end
                row = depRange.getRow() - endRowOffset;
                col = depRange.getColumn() - endColOffset;
                lastRow = prec.getLastRow();
                lastCol = prec.getLastColumn();
                break;

            default: // TYPEFOUR: fixed start, fixed end
                     // NOTYPE
                row = prec.getRow();
                col = prec.getColumn();
                lastRow = prec.getLastRow();
                lastCol = prec.getLastColumn();
                break;
        }

        return new RefImpl(
                prec.getBookName(),
                prec.getSheetName(),
                row, col, lastRow, lastCol);
    }

    public static Ref findLastPrec(Ref prec, Ref dep,
                                   EdgeMeta edgeMeta,
                                   Direction direction) {
        assert(direction != Direction.NODIRECTION);
        Ref depRange;
        if (direction == Direction.TOUP || direction == Direction.TOLEFT) {
            depRange = new RefImpl(dep.getBookName(), dep.getSheetName(),
                    dep.getRow(), dep.getColumn(), dep.getRow(), dep.getColumn());
        } else {
            depRange = new RefImpl(dep.getBookName(), dep.getSheetName(),
                    dep.getLastRow(), dep.getLastColumn(), dep.getLastRow(), dep.getLastColumn());
        }
        boolean isDirectPrec = true;
        return findUpdatePrecRef(prec, dep, edgeMeta, depRange, isDirectPrec);
    }

}
