package org.zkoss.zss.model.impl.sys.utils;

public enum PatternType {
    TYPEZERO("RR-Chain"),  // Long chain, special case of TypeOne
    TYPEONE("RR"),   // Relative start, Relative end
    TYPETWO("RF"),   // Relative start, Absolute end
    TYPETHREE("FR"), // Absolute start, Relative end
    TYPEFOUR("FF"),  // Absolute start, Absolute end
    TYPEFIVE("RRGapOne"),
    TYPESIX("RRGapTwo"),
    TYPESEVEN("RRGapThree"),
    TYPEEIGHT("RRGapFour"),
    TYPENINE("RRGapFive"),
    TYPETEN("RRGapSix"),
    TYPEELEVEN("RRGapSeven"),
    NOTYPE("NoComp");

    public final String label;

    private PatternType(String label) {
        this.label = label;
    }
}
