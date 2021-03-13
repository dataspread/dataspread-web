package org.zkoss.zss.model.impl.sys.compression;

import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.sys.dependency.Ref;

public class RefWithMeta {

    private final Ref ref;
    private final PatternType patternType;
    private final Offset startOffset;
    private final Offset endOffset;


    public RefWithMeta(Ref ref, PatternType patternType,
                       Offset startOffset, Offset endOffset) {
        this.ref = ref;
        this.patternType = patternType;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public Ref getRef() {
        return ref;
    }

    public PatternType getPatternType() {
        return patternType;
    }

    public Ref findDepUpdateRef(Ref precRange) {
        int row = -1;
        int col = -1;
        int lastRow = -1;
        int lastCol = -1;

        switch (patternType) {
            case TYPEZERO:
            case TYPETHREE: // relative start, fixed end
                row = precRange.getRow() + startOffset.getRowOffset();
                col = precRange.getColumn() + startOffset.getColOffset();
                lastRow = ref.getLastRow();
                lastCol = ref.getLastColumn();
                break;

            case TYPEONE: // relative start, relative end
                row = precRange.getRow() + startOffset.getRowOffset();
                col = precRange.getColumn() + startOffset.getColOffset();
                lastRow = precRange.getLastRow() + endOffset.getRowOffset();
                lastCol = precRange.getLastColumn() + endOffset.getColOffset();
                break;

            case TYPETWO: // fixed start, relative end
                row = ref.getRow();
                col = ref.getColumn();
                lastRow = precRange.getLastRow() + endOffset.getRowOffset();
                lastCol = precRange.getLastColumn() + endOffset.getColOffset();
                break;

            case TYPEFOUR: // fixed start, fixed end
                row = ref.getRow();
                col = ref.getColumn();
                lastRow = ref.getLastRow();
                lastCol = ref.getLastColumn();
                break;
        }

        return new RefImpl(
                ref.getBookName(),
                ref.getSheetName(),
                row, col, lastRow, lastCol);
    }
}
