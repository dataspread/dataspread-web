package org.zkoss.zss.model.impl.sys.utils;

import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.util.HashSet;
import java.util.Set;

public class PatternTools {
    public final static int DEFAULT_SHIFT_STEP = 1;
    private final static int FIRST_ROW = 0;
    private final static int FIRST_COL = 0;

    public static boolean isCompressibleTypeOne(Ref lastCandPrec, Ref prec,
                                         Direction direction) {
        Ref shiftedRef = shiftRef(lastCandPrec, direction, DEFAULT_SHIFT_STEP);
        return shiftedRef != null && shiftedRef.equals(prec);
    }

    // Only called after isCompressibleTypeOne is true
    public static boolean isCompressibleTypeZero(Ref prec, Ref dep,
                                                 Ref lastCandPrec) {
        boolean isTypeZero = false;
        for (Direction direction: Direction.values()) {
            if (direction != Direction.NODIRECTION && !isTypeZero) {
                Ref shiftedRef = shiftRef(prec, direction, DEFAULT_SHIFT_STEP);
                if (shiftedRef != null && shiftedRef.equals(dep)) { // check adjacency
                    Ref lastCandDep = shiftRef(lastCandPrec, direction, DEFAULT_SHIFT_STEP);
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
            return isShrinkedStart(lastCandPrec, prec, direction);
        else
            return isExtendedStart(lastCandPrec, prec, direction);
    }

    public static boolean isCompressibleTypeThree(Ref lastCandPrec, Ref prec,
                                                  Direction direction) {
        if (direction == Direction.TODOWN || direction == Direction.TORIGHT)
            return isExtendedEnd(lastCandPrec, prec, direction);
        else
            return isShrinkedEnd(lastCandPrec, prec, direction);
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
            return (lastCandPrec.getRow() + 1 == prec.getRow() &&
                    lastCandPrec.getColumn() == prec.getColumn() &&
                    lastCandPrec.getLastRow() == prec.getLastRow() &&
                    lastCandPrec.getLastColumn() == prec.getLastColumn());
        } else if (direction == Direction.TORIGHT) {
            return (lastCandPrec.getRow() == prec.getRow() &&
                    lastCandPrec.getColumn() + 1 == prec.getColumn() &&
                    lastCandPrec.getLastRow() == prec.getLastRow() &&
                    lastCandPrec.getLastColumn() == prec.getLastColumn());
        } else return false;
    }

    public static boolean isExtendedStart(Ref lastCandPrec, Ref prec,
                                          Direction direction) {
        if (direction == Direction.TOUP) {
            return (lastCandPrec.getRow() - 1 == prec.getRow() &&
                    lastCandPrec.getColumn() == prec.getColumn() &&
                    lastCandPrec.getLastRow() == prec.getLastRow() &&
                    lastCandPrec.getLastColumn() == prec.getLastColumn());
        } else if (direction == Direction.TOLEFT) {
            return (lastCandPrec.getRow() == prec.getRow() &&
                    lastCandPrec.getColumn() - 1 == prec.getColumn() &&
                    lastCandPrec.getLastRow() == prec.getLastRow() &&
                    lastCandPrec.getLastColumn() == prec.getLastColumn());
        } else return false;
    }

    public static boolean isCompressibleTypeFour(Ref lastCandPrec, Ref prec) {
        return lastCandPrec.equals(prec);
    }

    public static Ref shiftRef(Ref ref, Direction direction, int shift_step) {
        Ref res = null;
        switch (direction) {
            case TOLEFT:
                if (ref.getColumn() != FIRST_COL) {
                    res = new RefImpl(ref.getBookName(),
                            ref.getSheetName(),
                            ref.getRow(), ref.getColumn() - shift_step,
                            ref.getLastRow(), ref.getLastColumn() - shift_step);
                }
                break;
            case TORIGHT:
                res = new RefImpl(ref.getBookName(),
                        ref.getSheetName(),
                        ref.getRow(), ref.getColumn() + shift_step,
                        ref.getLastRow(), ref.getLastColumn() + shift_step);
                break;
            case TOUP:
                if (ref.getRow() != FIRST_ROW) {
                    res = new RefImpl(ref.getBookName(),
                            ref.getSheetName(),
                            ref.getRow() - shift_step, ref.getColumn(),
                            ref.getLastRow() - shift_step, ref.getLastColumn());
                }
                break;
            default: // TODOWN
                res = new RefImpl(ref.getBookName(),
                        ref.getSheetName(),
                        ref.getRow() + shift_step, ref.getColumn(),
                        ref.getLastRow() + shift_step, ref.getLastColumn());
        }
        return res;
    }

    // what is the adjacency direction of refA relative to refB
    public static Direction findAdjacencyDirection(Ref refA, Ref refB, int shiftStep) {
        int adjFirstRow = refA.getRow();
        int adjFirstCol = refA.getColumn();
        int adjLastRow = refA.getLastRow();
        int adjLastCol = refA.getLastColumn();

        int firstRow = refB.getRow();
        int firstCol = refB.getColumn();
        int lastRow = refB.getLastRow();
        int lastCol = refB.getLastColumn();

        if (adjFirstRow == firstRow && adjLastRow == lastRow
                && adjLastCol + shiftStep == firstCol) { // To Left
            return Direction.TOLEFT;
        } else if (adjFirstRow == firstRow && adjLastRow == lastRow
                && lastCol + shiftStep == adjFirstCol) { // To Right
            return Direction.TORIGHT;
        } else if (adjFirstCol == firstCol && adjLastCol == lastCol
                && adjLastRow + shiftStep == firstRow)
            return Direction.TOUP;
        else if (adjFirstCol == firstCol && adjLastCol == lastCol
                && lastRow + shiftStep == adjFirstRow)
            return Direction.TODOWN;
        else
            return Direction.NODIRECTION;
    }

    public static boolean isValidAdjacency(Ref adjRef, Ref ref, int shiftStep) {
        return findAdjacencyDirection(adjRef, ref, shiftStep) != Direction.NODIRECTION;
    }

    public static Set<Ref> findUpdateDepRef(Ref prec, Ref dep,
                                            EdgeMeta edgeMeta, Ref precRange) {
        Set<Ref> retSet = new HashSet<>();
        int row = -1;
        int col = -1;
        int lastRow = -1;
        int lastCol = -1;

        int startRowOffset = edgeMeta.startOffset.getRowOffset();
        int startColOffset = edgeMeta.startOffset.getColOffset();
        int endRowOffset = edgeMeta.endOffset.getRowOffset();
        int endColOffset = edgeMeta.endOffset.getColOffset();

        int inputRow = precRange.getRow();
        int inputCol = precRange.getColumn();
        int inputLastRow = precRange.getLastRow();
        int inputLastCol = precRange.getLastColumn();

        if (dep.getColumn() == dep.getLastColumn()) { // Column-wise
            inputCol = prec.getColumn();
            inputLastCol = prec.getLastColumn();
        } else { // Row-wise
            inputRow = prec.getRow();
            inputLastRow = prec.getLastRow();
        }

        PatternType patternType;
        if (edgeMeta.patternType != PatternType.TYPEZERO)
            patternType = edgeMeta.patternType;
        else { // TYPEZERO
            if (startRowOffset == 1 || startColOffset == 1) patternType = PatternType.TYPETHREE;
            else if (startRowOffset == -1 || startColOffset == -1) patternType = PatternType.TYPETWO;
            else throw new RuntimeException("TYPE ZERO offset (" + startRowOffset + ","
                        + startColOffset + ") wrong");
        }

        switch (patternType) {
            case TYPEONE: // relative start, relative end
            case TYPEFIVE:
            case TYPESIX:
            case TYPESEVEN:
            case TYPEEIGHT:
            case TYPENINE:
            case TYPETEN:
            case TYPEELEVEN:
                row = inputRow + endRowOffset;
                col = inputCol + endColOffset;
                lastRow = inputLastRow + startRowOffset;
                lastCol = inputLastCol + startColOffset;
                if (patternType != PatternType.TYPEONE) {
                    int gapSize = patternType.ordinal() - PatternType.TYPEFIVE.ordinal() + 1;
                    retSet = findRefSetForGapType(prec.getBookName(), prec.getSheetName(),
                            Math.max(row, dep.getRow()),
                            Math.max(col, dep.getColumn()),
                            Math.min(lastRow, dep.getLastRow()),
                            Math.min(lastCol, dep.getLastColumn()),
                            dep.getRow(), dep.getColumn(),
                            dep.getLastRow(), dep.getLastColumn(),
                            gapSize);
                    return retSet;
                }
                break;

            case TYPETWO: // relative start, fixed end
                row = dep.getRow();
                col = dep.getColumn();
                lastRow = inputLastRow + startRowOffset;
                lastCol = inputLastCol + startColOffset;
                break;

            case TYPETHREE: // fixed start, relative end
                row = inputRow + endRowOffset;
                col = inputCol + endColOffset;
                lastRow = dep.getLastRow();
                lastCol = dep.getLastColumn();
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

        retSet.add(new RefImpl(
                prec.getBookName(),
                prec.getSheetName(),
                row, col, lastRow, lastCol).getOverlap(dep));

        return retSet;
    }

    public static Set<Ref> findRefSetForGapType(String bookName,
                                                String sheetName,
                                                int iRow, int iCol,
                                                int iLastRow, int iLastCol,
                                                int row, int col,
                                                int lastRow, int lastCol,
                                                int gapSize) {
        Set<Ref> refSet = new HashSet<>();
        if (row == lastRow) {
            for (int colVar = col; colVar <= lastCol; colVar += (gapSize + 1)) {
                if (colVar >= iCol && colVar <= iLastCol)
                    refSet.add(new RefImpl(bookName, sheetName, row, colVar));
            }
        } else if (col == lastCol) {
            for (int rowVar = row; rowVar <= lastRow; rowVar += (gapSize + 1)) {
                if (rowVar >= iRow && rowVar <= iLastRow)
                    refSet.add(new RefImpl(bookName, sheetName, rowVar, col));
            }
        } else {
            assert(false);
        }
        return refSet;
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
            else if (startRowOffset == 1 || startColOffset == 1) patternType = PatternType.TYPETHREE;
            else if (startRowOffset == -1 || startColOffset == -1) patternType = PatternType.TYPETWO;
            else throw new RuntimeException("TYPE ZERO offset (" + startRowOffset + ","
                        + startColOffset + ") wrong");
        }

        switch (patternType) {

            case TYPEONE: // relative start, relative end
                row = depRange.getRow() - startRowOffset;
                col = depRange.getColumn() - startColOffset;
                lastRow = depRange.getLastRow() - endRowOffset;
                lastCol = depRange.getLastColumn() - endColOffset;
                break;

            case TYPETWO: // relative start, fixed end
                row = depRange.getRow() - startRowOffset;
                col = depRange.getColumn() - startColOffset;
                lastRow = prec.getLastRow();
                lastCol = prec.getLastColumn();
                break;

            case TYPETHREE: // fixed start, relative end
                row = prec.getRow();
                col = prec.getColumn();
                lastRow = depRange.getLastRow() - endRowOffset;
                lastCol = depRange.getLastColumn() - endColOffset;
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

    public static Ref findValidGapRef(Ref ref, Ref subRef, int gapSize) {
        int newRow = -1, newCol = -1, newLastRow = -1, newLastCol = -1;
        if (ref.getRow() == ref.getLastRow()) {
            newRow = newLastRow = ref.getRow();
            newCol = findFirstMatch(ref.getColumn(), subRef.getColumn(), gapSize);
            newLastCol = findLastMatch(ref.getLastColumn(), subRef.getLastColumn(), gapSize);
            if (newCol > newLastCol) newCol = -1;
        } else if (ref.getColumn() == ref.getLastColumn()) {
            newCol = newLastCol = ref.getColumn();
            newRow = findFirstMatch(ref.getRow(), subRef.getRow(), gapSize);
            newLastRow = findLastMatch(ref.getLastRow(), subRef.getLastRow(), gapSize);
            if (newRow > newLastRow) newRow = -1;
        } else {
            assert false;
        }
        if (newRow == -1 || newCol == -1 || newLastRow == -1 || newLastCol == -1) {
            return null;
        } else {
            return new RefImpl(ref.getBookName(), ref.getSheetName(),
                    newRow, newCol, newLastRow, newLastCol);
        }
    }

    private static int findFirstMatch(int start, int subStart, int gapSize) {
        int div = (subStart - start + gapSize - 1)/gapSize;
        return start + div * gapSize;
    }

    private static int findLastMatch(int end, int subEnd, int gapSize) {
        int div = (end - subEnd + gapSize - 1)/gapSize;
        return end - div * gapSize;
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
