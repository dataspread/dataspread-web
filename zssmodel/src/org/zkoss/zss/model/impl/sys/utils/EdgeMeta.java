package org.zkoss.zss.model.impl.sys.utils;

import java.util.Objects;

public class EdgeMeta {

    public final PatternType patternType;
    public final Offset startOffset;
    public final Offset endOffset;
    public int gapLength;

    public EdgeMeta(PatternType patternType,
                    Offset startOffset, Offset endOffset) {
        this.patternType = patternType;
        this.gapLength = 0;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public EdgeMeta(PatternType patternType, int gapLength,
                    Offset startOffset, Offset endOffset) {
        this.patternType = patternType;
        this.gapLength = gapLength;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EdgeMeta)) return false;
        EdgeMeta edgeMeta = (EdgeMeta) o;
        return patternType == edgeMeta.patternType &&
                Objects.equals(this.startOffset, edgeMeta.startOffset) &&
                Objects.equals(this.endOffset, edgeMeta.endOffset) &&
                Objects.equals(this.gapLength, edgeMeta.gapLength);
    }

    public void setGapLength(int gapLength) {
        this.gapLength = gapLength;
    }

    @Override
    public int hashCode() {
        return Objects.hash(patternType, startOffset, endOffset);
    }
}
