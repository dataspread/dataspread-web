package org.zkoss.zss.model.impl.sys.utils;

import org.zkoss.zss.model.sys.dependency.Ref;

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

}
