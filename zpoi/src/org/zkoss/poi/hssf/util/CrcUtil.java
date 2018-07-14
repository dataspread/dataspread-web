/*
{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Aug 13, 2014, Created by henri
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/

package org.zkoss.poi.hssf.util;

/**
 * Cacluate CRC of a byte stream.
 * @author henri
 * 
 * [MS-OSHARED].pdf 2.4.3 MsoCrc32Compute
 */
public class CrcUtil {
    
    private static int[] _cache;
    
    public static int calcCRC(int crc, byte[] bytes, int start, int len) {
    	initCRCCache();
    	for (int j = 0; j < len; ++j) {
    		final byte b = bytes[j + start];
    		crc = calcCRC(crc, b);
    	}
    	return crc;
    }
    
    public static int calcCRC(int crc, byte b) {
		int index =  crc >>> 24;
		index ^= ((int)b) & 0xff;
		crc <<= 8;
		crc ^= _cache[index];
		return crc;
    }
    
    private static void initCRCCache() {
    	if (_cache != null) return;
    	_cache = new int[256];
    	for (int j = 0; j < 256; ++j) {
    		int value = j << 24;
    		for (int bit = 0; bit < 8; ++bit) {
    			if ((value & 0x80000000) == 0x80000000) { // bit31 is set
    				value <<= 1;
    				value ^= 0xAF;
    			} else {
    				value <<= 1;
    			}
    		}
    		value &= 0xffff;
    		_cache[j] = value;
    	}
    }

}
