package org.zkoss.zss.model.impl.sys.utils;

import org.zkoss.zss.model.sys.dependency.Ref;

import java.util.Objects;

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

    public EdgeMeta getEdgeMeta() {
        return edgeMeta;
    }

    public PatternType getPatternType() {
        return edgeMeta.patternType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RefWithMeta)) return false;
        RefWithMeta that = (RefWithMeta) o;
        return Objects.equals(ref, that.ref) &&
                Objects.equals(edgeMeta, that.edgeMeta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ref, edgeMeta);
    }
}
