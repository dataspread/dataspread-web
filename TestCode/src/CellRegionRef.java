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
        refType = RefType.One2One;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CellRegionRef other = (CellRegionRef) obj;

        return (super.equals(other) && this.refType == other.refType);
    }

    public enum RefType {One2One, One2Many}

}
