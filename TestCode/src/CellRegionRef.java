import org.zkoss.zss.model.CellRegion;

/**
 * Created by Mangesh on 1/25/2017.
 */
public class CellRegionRef extends CellRegion {
    public enum RefType {One2One, One2Many}

    public RefType refType;

    public CellRegionRef(int row, int column) {
        super(row, column);
    }

    public CellRegionRef(String areaReference) {
        super(areaReference);
    }

    public CellRegionRef(int row, int column, int lastRow, int lastColumn) {
        super(row, column, lastRow, lastColumn);
    }

    public CellRegionRef getBoundingBox(CellRegionRef cellRegionRef2) {
        CellRegion boundingBox = super.getBoundingBox(cellRegionRef2);
        CellRegionRef cellRegionRef = new CellRegionRef(boundingBox.row, boundingBox.column,
                boundingBox.lastRow, boundingBox.lastColumn);
        cellRegionRef.refType = refType == RefType.One2One
                && cellRegionRef2.refType == RefType.One2One
                ? RefType.One2One : RefType.One2Many;
        return cellRegionRef;
    }
}
