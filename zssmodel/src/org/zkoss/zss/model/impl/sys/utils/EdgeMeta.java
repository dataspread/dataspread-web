package org.zkoss.zss.model.impl.sys.utils;

import java.util.Objects;

public class EdgeMeta {

    public final PatternType patternType;
    public final Offset startOffset;
    public final Offset endOffset;

    public EdgeMeta(PatternType patternType,
                    Offset startOffset, Offset endOffset) {
        this.patternType = patternType;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EdgeMeta)) return false;
        EdgeMeta edgeMeta = (EdgeMeta) o;
        return patternType == edgeMeta.patternType &&
                Objects.equals(startOffset, edgeMeta.startOffset) &&
                Objects.equals(endOffset, edgeMeta.endOffset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patternType, startOffset, endOffset);
    }
}
