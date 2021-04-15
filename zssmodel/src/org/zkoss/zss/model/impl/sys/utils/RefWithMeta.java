package org.zkoss.zss.model.impl.sys.utils;

import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.sys.dependency.Ref;

public class RefWithMeta {

    private final Ref ref;
    private final EdgeMeta edgeMeta;

    public RefWithMeta(Ref ref, EdgeMeta edgeMeta) {
        this.ref = ref;
        this.edgeMeta = edgeMeta;
    }

    public Ref getRef() {
        return ref;
    }

    public PatternType getPatternType() {
        return edgeMeta.patternType;
    }

    public Ref findDepUpdateRef(Ref precRange) {
        int row = -1;
        int col = -1;
        int lastRow = -1;
        int lastCol = -1;

        int startRowOffset = edgeMeta.startOffset.getRowOffset();
        int startColOffset = edgeMeta.startOffset.getColOffset();
        int endRowOffset = edgeMeta.endOffset.getRowOffset();
        int endColOffset = edgeMeta.endOffset.getColOffset();

        switch (edgeMeta.patternType) {
            case TYPEZERO:
            case TYPETHREE: // relative start, fixed end
                row = precRange.getRow() + startRowOffset;
                col = precRange.getColumn() + startColOffset;
                lastRow = ref.getLastRow();
                lastCol = ref.getLastColumn();
                break;

            case TYPEONE: // relative start, relative end
                row = precRange.getRow() + startRowOffset;
                col = precRange.getColumn() + startColOffset;
                lastRow = precRange.getLastRow() + endRowOffset;
                lastCol = precRange.getLastColumn() + endColOffset;
                break;

            case TYPETWO: // fixed start, relative end
                row = ref.getRow();
                col = ref.getColumn();
                lastRow = precRange.getLastRow() + endRowOffset;
                lastCol = precRange.getLastColumn() + endColOffset;
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
