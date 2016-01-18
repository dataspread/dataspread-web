/* IndexedRGB.java

	Purpose:
		
	Description:
		
	History:
		Sep 21, 2010 3:32:05 PM, Created by henrichen

Copyright (C) 2010 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.range.impl.imexp;

import java.util.HashMap;
import java.util.Map;

/**
 * copied from zss project. We should remove it after integration.
 * Excel standard color palette.
 * @author henrichen
 *
 */
/*package*/ class IndexedRGB {
	private static final Map<Integer, IndexedRGB> IndexToRGB = new HashMap<Integer, IndexedRGB>();
	private static final Map<IndexedRGB, Integer> RGBToIndex = new HashMap<IndexedRGB, Integer>();
	
	private final byte _r;
	private final byte _g;
	private final byte _b;
	private IndexedRGB(int index, int r, int g, int b) {
		this(r, g, b);
		final Integer idx = Integer.valueOf(index);
		IndexToRGB.put(idx, this);
		if (!RGBToIndex.containsKey(this)) {
			RGBToIndex.put(this, idx);
		}
	}
	
	private IndexedRGB(int r, int g, int b) {
		_r = (byte)(r & 0xff);
		_g = (byte)(g & 0xff);
		_b = (byte)(b & 0xff);
	}
	
	@Override
	public int hashCode() {
		return _r ^ _g ^ _b;
	}
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof IndexedRGB)) {
			return false;
		}
		final IndexedRGB o = (IndexedRGB) other;
		return _r == o._r && _g == o._g && _b == o._b;
	}
	
	public Integer getIndex() {
		return RGBToIndex.get(this);
	}
	
	public byte[] getRGB() {
		return new byte[] {_r, _g, _b};
	}
	
	/** per the color index, return the associated RGB
	 * @param index color index
	 */
	public static byte[] getRGB(int index) {
		IndexedRGB rgb = IndexToRGB.get(Integer.valueOf(index));
		return rgb == null ? null : rgb.getRGB();
	}
	public static Integer getIndex(byte[] rgb) {
		return RGBToIndex.get(new IndexedRGB(rgb[0], rgb[1], rgb[2]));
	}

	//ORDER is IMPORTANT, do NOT change it! 
	/*package*/ static IndexedRGB BLACK = new IndexedRGB(8, 0, 0, 0);
	/*package*/ static IndexedRGB WHITE = new IndexedRGB(9, 255, 255, 255);
	/*package*/ static IndexedRGB RED = new IndexedRGB(10, 255, 0, 0);
	/*package*/ static IndexedRGB BRIGHT_GREEN = new IndexedRGB(11, 0, 255, 0);
	/*package*/ static IndexedRGB BLUE = new IndexedRGB(12, 0, 0, 255);
	/*package*/ static IndexedRGB YELLOW = new IndexedRGB(13, 255, 255, 0);
	/*package*/ static IndexedRGB PINK = new IndexedRGB(14, 255, 0, 255);
	/*package*/ static IndexedRGB TURQUOISE = new IndexedRGB(15, 0, 255, 255);
	/*package*/ static IndexedRGB DARK_RED = new IndexedRGB(16, 128, 0, 0); 
	/*package*/ static IndexedRGB GREEN = new IndexedRGB(17, 0, 128, 0);
	/*package*/ static IndexedRGB DARK_BLUE = new IndexedRGB(18, 0, 0, 128);
	/*package*/ static IndexedRGB DARK_YELLOW = new IndexedRGB(19, 128, 128, 0);
	/*package*/ static IndexedRGB VIOLET = new IndexedRGB(20, 128, 0, 128);
	/*package*/ static IndexedRGB TEAL = new IndexedRGB(21, 0, 128, 128);
	/*package*/ static IndexedRGB GREY_25_PERCENT = new IndexedRGB(22, 192, 192, 192);
	/*package*/ static IndexedRGB GREY_50_PERCENT = new IndexedRGB(23, 128, 128, 128);
	/*package*/ static IndexedRGB CORNFLOWER_BLUE = new IndexedRGB(24, 153, 153, 255);
	/*package*/ static IndexedRGB LEMON_CHIFFON = new IndexedRGB(26, 255, 255, 204);
	
	/*package*/ static IndexedRGB ORCHID = new IndexedRGB(28, 102, 0, 102);
	/*package*/ static IndexedRGB CORAL = new IndexedRGB(29, 255, 128 ,128);
	/*package*/ static IndexedRGB ROYAL_BLUE = new IndexedRGB(30, 0, 102, 204);
	/*package*/ static IndexedRGB LIGHT_CORNFLOWER_BLUE = new IndexedRGB(31, 204, 204, 255);
	
	/*package*/ static IndexedRGB SKY_BLUE = new IndexedRGB(40, 0, 204, 255);
	/*package*/ static IndexedRGB LIGHT_TURQUOISE = new IndexedRGB(41, 204, 255, 255);
	/*package*/ static IndexedRGB LIGHT_GREEN = new IndexedRGB(42, 204, 255, 204);
	/*package*/ static IndexedRGB LIGHT_YELLOW = new IndexedRGB(43, 255, 255, 153);
	/*package*/ static IndexedRGB PALE_BLUE = new IndexedRGB(44, 153, 204, 255);
	/*package*/ static IndexedRGB ROSE = new IndexedRGB(45, 255, 153, 204);
	/*package*/ static IndexedRGB LAVENDER = new IndexedRGB(46, 204, 153, 255);
	/*package*/ static IndexedRGB TAN = new IndexedRGB(47, 255, 204, 153);
	/*package*/ static IndexedRGB LIGHT_BLUE = new IndexedRGB(48, 51, 102, 255);
	/*package*/ static IndexedRGB AQUA = new IndexedRGB(49, 51, 204, 204);
	/*package*/ static IndexedRGB LIME = new IndexedRGB(50, 153, 204, 0);
	/*package*/ static IndexedRGB GOLD = new IndexedRGB(51, 255, 204, 0);
	/*package*/ static IndexedRGB LIGHT_ORANGE = new IndexedRGB(52, 255, 153, 0);
	/*package*/ static IndexedRGB ORANGE = new IndexedRGB(53, 255, 102, 0);
	/*package*/ static IndexedRGB BLUE_GREY = new IndexedRGB(54, 102, 102, 153);
	/*package*/ static IndexedRGB GREY_40_PERCENT = new IndexedRGB(55, 150, 150, 150);
	/*package*/ static IndexedRGB DARK_TEAL = new IndexedRGB(56, 0, 51, 102);
	/*package*/ static IndexedRGB SEA_GREEN = new IndexedRGB(57, 51, 153, 102);
	/*package*/ static IndexedRGB DARK_GREEN = new IndexedRGB(58, 0, 51, 0);
	/*package*/ static IndexedRGB OLIVE_GREEN = new IndexedRGB(59, 51, 51, 0);
	/*package*/ static IndexedRGB BROWN = new IndexedRGB(60, 153, 51, 0);
	/*package*/ static IndexedRGB PLUM = new IndexedRGB(61, 153, 51, 102);
	/*package*/ static IndexedRGB INDIGO = new IndexedRGB(62, 51, 51, 153);
	/*package*/ static IndexedRGB GREY_80_PERCENT = new IndexedRGB(63, 51, 51, 51);
	/*package*/ static IndexedRGB AUTOMATIC = new IndexedRGB(64, 0, 0, 0);

	//Duplicate RGB
	/*package*/ static IndexedRGB MAROON = new IndexedRGB(25, 153, 51, 102); //PLUM
	/*package*/ static IndexedRGB LIGHT_TURQUOISE2 = new IndexedRGB(27, 204, 255, 255);
	/*package*/ static IndexedRGB DARK_BLUE2 = new IndexedRGB(32, 0, 0, 128);
	/*package*/ static IndexedRGB PINK2 = new IndexedRGB(33, 255, 0, 255);
	/*package*/ static IndexedRGB YELLOW2 = new IndexedRGB(34, 255, 255, 0);
	/*package*/ static IndexedRGB TURQUOISE2 = new IndexedRGB(35, 0, 255, 255);
	/*package*/ static IndexedRGB VIOLET2 = new IndexedRGB(36, 128, 0, 128);
	/*package*/ static IndexedRGB DARK_RED2 = new IndexedRGB(37, 128, 0, 0); 
	/*package*/ static IndexedRGB TEAL2 = new IndexedRGB(38, 0, 128, 128);
	/*package*/ static IndexedRGB BLUE2 = new IndexedRGB(39, 0, 0, 255);
}