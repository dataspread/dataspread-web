package org.zkoss.zss.model.impl.sys.utils;

import com.github.davidmoten.rtree.geometry.Rectangle;
import com.github.davidmoten.rtree.geometry.internal.RectangleFloat;
import org.postgresql.geometric.PGbox;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.sys.dependency.Ref;

public class RefUtils {
    public static boolean isValidRef(Ref ref) {
        return (ref.getRow() >= 0 &&
                ref.getColumn() >=0 &&
                ref.getRow() <= ref.getLastRow() &&
                ref.getColumn() <= ref.getLastColumn());
    }

    public static Rectangle boxToRect(PGbox box) {
        return RectangleFloat.create((float) box.point[1].x, (float) box.point[1].y,
                (float) (0.5 + box.point[0].x), (float) (0.5 + box.point[0].y));
    }

    public static Rectangle refToRect(Ref ref)
    {
        return RectangleFloat.create(ref.getRow(),ref.getColumn(),
                (float) 0.5 + ref.getLastRow(), (float) 0.5 + ref.getLastColumn());
    }

    public static Ref coordToRef(Ref ref, int firstRow, int firstCol,
                           int lastRow, int lastCol) {
        return new RefImpl(ref.getBookName(), ref.getSheetName(),
                firstRow, firstCol, lastRow, lastCol);
    }

    public static PGbox refToPGBox(Ref ref) {
        return new PGbox(ref.getRow(), ref.getColumn(),
                ref.getLastRow(), ref.getLastColumn());
    }

    public static Ref boxToRef(PGbox range, String bookName, String sheetName) {
        return new RefImpl(bookName, sheetName,
                (int) range.point[1].x,
                (int) range.point[1].y,
                (int) range.point[0].x,
                (int) range.point[0].y);
    }

    public static PGbox offsetToPGBox(Offset startOffset,
                                Offset endOffset) {
        return new PGbox(startOffset.getRowOffset(),
                startOffset.getColOffset(),
                endOffset.getRowOffset(),
                endOffset.getColOffset());
    }

    public static Offset refToOffset(Ref prec, Ref dep, boolean isStart) {
        if (isStart) {
            return new Offset(dep.getLastRow() - prec.getLastRow(),
                    dep.getLastColumn() - prec.getLastColumn());
        } else {
            return new Offset(dep.getRow() - prec.getRow(),
                    dep.getColumn() - prec.getColumn());
        }
    }
}
