import org.zkoss.zss.model.CellRegion;

/**
 * Created by Mangesh on 1/25/2017.
 */
public class CellRegionRef extends CellRegion {
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

    public RefType getRefType() {
        return refType;
    }

    public void setRefType(RefType refType) {
        this.refType = refType;
    }

    public CellRegionRef getBoundingBox(CellRegionRef cellRegionRef2) {
        CellRegion boundingBox = super.getBoundingBox(cellRegionRef2);
        CellRegionRef cellRegionRef = new CellRegionRef(boundingBox.row, boundingBox.column,
                boundingBox.lastRow, boundingBox.lastColumn);
        // By default merge results in a many to many
        // Update later on if merge results in a one to one.
        cellRegionRef.refType = RefType.One2Many;
        return cellRegionRef;
    }

    public enum RefType {One2One, One2Many}
}
