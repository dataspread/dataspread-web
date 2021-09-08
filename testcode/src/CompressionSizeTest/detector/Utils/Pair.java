package CompressionSizeTest.detector.Utils;

public class Pair<T, E> {

    public T first;
    public E second;

    public Pair(T first, E second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals (Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true;  }
        if (!(obj instanceof Pair<?, ?>)) {
            return false;
        } else {
            Pair<?, ?> other = (Pair<?, ?>) obj;
            return  this.first.equals(other.first) &&
                    this.second.equals(other.second);
        }
    }

    @Override
    public int hashCode () {
        return (31 * this.first.hashCode()) + this.second.hashCode();
    }

    @Override
    public String toString () {
        return "(" + this.first.toString() + ", " + this.second.toString() + ")";
    }
}
