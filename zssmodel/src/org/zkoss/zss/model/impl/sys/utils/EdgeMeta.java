package org.zkoss.zss.model.impl.sys.utils;

import org.zkoss.zss.model.sys.dependency.Ref;

public class EdgeMeta {

    public final PatternType patternType;
    public final Offset startOffset;
    public final Offset endOffset;
    public final Ref lastPrecRef;

    public EdgeMeta(PatternType patternType,
                    Offset startOffset, Offset endOffset,
                    Ref lastPrecRef) {
        this.patternType = patternType;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.lastPrecRef = lastPrecRef;
    }

}
