/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.zkoss.poi.ss.usermodel;

import org.zkoss.poi.hssf.record.FontRecord;

/**
 * 17 Built-in Fonts starting from index 5(1-based) beside 0~3 default fonts.
 * 
 * @author henri
 * @since 3.9.5
 */
public class BuiltinFonts {
	private static final FontRecord[] _fonts = new FontRecord[17];
	
	//default 0 ~ 3, 4 not available, 5 (1-based) ...
	static {
		int j = 0;
		_fonts[j++] = new FontRecord(0x00dc, 0x000, 0x0008, 0x0190,0x0, 0x0, 0x2, 0x0, "Calibri"); // 5
		_fonts[j++] = new FontRecord(0x00dc, 0x000, 0x0009, 0x0190,0x0, 0x0, 0x2, 0x0, "Calibri"); // 6
		_fonts[j++] = new FontRecord(0x00dc, 0x000, 0x0014, 0x0190,0x0, 0x0, 0x2, 0x0, "Calibri");  
		_fonts[j++] = new FontRecord(0x00dc, 0x001, 0x0034, 0x02bc,0x0, 0x0, 0x2, 0x0, "Calibri");
		_fonts[j++] = new FontRecord(0x00dc, 0x001, 0x0009, 0x02bc,0x0, 0x0, 0x2, 0x0, "Calibri"); 
		_fonts[j++] = new FontRecord(0x00dc, 0x002, 0x0017, 0x0190,0x0, 0x0, 0x2, 0x0, "Calibri"); //10
		_fonts[j++] = new FontRecord(0x00dc, 0x000, 0x0011, 0x0190,0x0, 0x0, 0x2, 0x0, "Calibri"); 
		_fonts[j++] = new FontRecord(0x012c, 0x001, 0x0038, 0x02bc,0x0, 0x0, 0x2, 0x0, "Calibri");
		_fonts[j++] = new FontRecord(0x0104, 0x001, 0x0038, 0x02bc,0x0, 0x0, 0x2, 0x0, "Calibri");
		_fonts[j++] = new FontRecord(0x00dc, 0x001, 0x0038, 0x02bc,0x0, 0x0, 0x2, 0x0, "Calibri");
		_fonts[j++] = new FontRecord(0x00dc, 0x000, 0x003e, 0x0190,0x0, 0x0, 0x2, 0x0, "Calibri"); //15
		_fonts[j++] = new FontRecord(0x00dc, 0x000, 0x0034, 0x0190,0x0, 0x0, 0x2, 0x0, "Calibri"); 
		_fonts[j++] = new FontRecord(0x00dc, 0x000, 0x003c, 0x0190,0x0, 0x0, 0x2, 0x0, "Calibri");
		_fonts[j++] = new FontRecord(0x00dc, 0x001, 0x003f, 0x02bc,0x0, 0x0, 0x2, 0x0, "Calibri");
		_fonts[j++] = new FontRecord(0x0168, 0x001, 0x0038, 0x02bc,0x0, 0x0, 0x2, 0x0, "Cambria"); 
		_fonts[j++] = new FontRecord(0x00dc, 0x001, 0x0008, 0x02bc,0x0, 0x0, 0x2, 0x0, "Calibri"); //20
		_fonts[j++] = new FontRecord(0x00dc, 0x000, 0x000a, 0x0190,0x0, 0x0, 0x2, 0x0, "Calibri");
	}
	
	public static FontRecord getBuiltinFont(int index) {
		return _fonts[index - 5];
	}
}
