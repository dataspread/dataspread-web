package CompressionSizeTest.detector.Utils;

public class DirectedEdge {
    private final CellLoc from;
    private final CellLoc to;

    public DirectedEdge(CellLoc from, CellLoc to) {
        this.from = from;
        this.to = to;
    }

    public CellLoc getFrom() {
        return from;
    }

    public CellLoc getTo() {
        return to;
    }

    @Override
    public boolean equals (Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true;  }
        if (!(obj instanceof DirectedEdge)) {
            return false;
        } else {
            DirectedEdge other = (DirectedEdge) obj;
            return  this.from.equals(other.from) &&
                    this.to.equals(other.to);
        }
    }

    @Override
    public String toString () {
        return "(From " + this.from + "to " + this.to + ")";
    }

}
