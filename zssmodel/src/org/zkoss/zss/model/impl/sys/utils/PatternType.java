package org.zkoss.zss.model.impl.sys.utils;

public enum PatternType {
    TYPEZERO,  // Long chain, special case of TypeOne
    TYPEONE,   // Relative start, Relative end
    TYPETWO,   // Absolute start, Relative end
    TYPETHREE, // Relative start, Absolute end
    TYPEFOUR,  // Absolute start, Absolute end
    NOTYPE
}
