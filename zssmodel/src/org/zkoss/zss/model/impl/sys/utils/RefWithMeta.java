package org.zkoss.zss.model.impl.sys.utils;

import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.sys.dependency.Ref;

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
}
