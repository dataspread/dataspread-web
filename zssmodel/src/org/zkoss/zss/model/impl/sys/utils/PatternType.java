package org.zkoss.zss.model.impl.sys.utils;

public enum PatternType {
    TYPEZERO,  // Long chain, special case of TypeOne
    TYPEONE,   // Relative start, Relative end
    TYPETWO,   // Relative start, Absolute end
    TYPETHREE, // Absolute start, Relative end
    TYPEFOUR,  // Absolute start, Absolute end
    NOTYPE
}
