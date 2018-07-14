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

package org.zkoss.poi.hssf.record;

import org.zkoss.poi.hssf.record.aggregates.PageSettingsBlock;
import org.zkoss.poi.util.HexDump;
import org.zkoss.poi.util.LittleEndianOutput;

/**
 * Title:        Unknown Record (for debugging)<p/>
 * Description:  Unknown record just tells you the sid so you can figure out
 *               what records you are missing.  Also helps us read/modify sheets we
 *               don't know all the records to.  (HSSF leaves these alone!) <p/>
 * Company:      SuperLink Software, Inc.<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class UnknownRecord extends StandardRecord {

	/*
	 * Some Record IDs used by POI as 'milestones' in the record stream
	 */
	/**
	 * seems to be part of the {@link PageSettingsBlock}. Not interpreted by POI.
	 * The name 'PRINTSIZE' was taken from OOO source.<br/>
	 * The few POI test samples with this record have data { 0x03, 0x00 }.
	 */
	public static final int PRINTSIZE_0033       = 0x0033;
	/**
	 * Environment-Specific Print Record
	 */
	public static final int PLS_004D             = 0x004D;
	public static final int SHEETPR_0081         = 0x0081;
	public static final int SORT_0090            = 0x0090;
	public static final int STANDARDWIDTH_0099   = 0x0099;
	public static final int SCL_00A0             = 0x00A0;
	public static final int BITMAP_00E9          = 0x00E9;
	public static final int PHONETICPR_00EF      = 0x00EF;
	public static final int LABELRANGES_015F     = 0x015F;
	public static final int QUICKTIP_0800        = 0x0800;
	public static final int SHEETEXT_0862        = 0x0862; // OOO calls this SHEETLAYOUT
	public static final int SHEETPROTECTION_0867 = 0x0867;
	public static final int HEADER_FOOTER_089C   = 0x089C;
    public static final int CODENAME_1BA         = 0x01BA;
    
    //20130916, hawkchen@potix.com, ZSS-272: : to identify records of conditional formatting
    //Conditional format related record
    //refer to 2.3 Record Enumeration, Excel Binary File Format (.xls) Structure
    public static final int CFEX_087B 			= 0x087B;
    public static final int CF12_087A 			= 0x087A;
    
	private int _sid;
	private byte[] _rawData;

	/**
	 * @param id	id of the record -not validated, just stored for serialization
	 * @param data  the data
	 */
	public UnknownRecord(int id, byte[] data) {
		_sid = id & 0xFFFF;
		_rawData = data;
	}


	/**
	 * construct an unknown record.  No fields are interpreted and the record will
	 * be serialized in its original form more or less
	 * @param in the RecordInputstream to read the record from
	 */
	public UnknownRecord(RecordInputStream in) {
		_sid = in.getSid();
		_rawData = in.readRemainder();
		if (false && getBiffName(_sid) == null) {
			// unknown sids in the range 0x0004-0x0013 are probably 'sub-records' of ObjectRecord
			// those sids are in a different number space.
			// TODO - put unknown OBJ sub-records in a different class
			System.out.println("Unknown record 0x" + Integer.toHexString(_sid).toUpperCase());
		}
	}

	/**
	 * spit the record out AS IS. no interpretation or identification
	 */
	public void serialize(LittleEndianOutput out) {
		out.write(_rawData);
	}

	protected int getDataSize() {
		return _rawData.length;
	}

	/**
	 * print a sort of string representation ([UNKNOWN RECORD] id = x [/UNKNOWN RECORD])
	 */
	public String toString() {
		String biffName = getBiffName(_sid);
		if (biffName == null) {
			biffName = "UNKNOWNRECORD";
		}
		StringBuffer sb = new StringBuffer();

		sb.append("[").append(biffName).append("] (0x");
		sb.append(Integer.toHexString(_sid).toUpperCase() + ")\n");
		if (_rawData.length > 0) {
			sb.append("  rawData=").append(HexDump.toHex(_rawData)).append("\n");
		}
		sb.append("[/").append(biffName).append("]\n");
		return sb.toString();
	}

	public short getSid() {
		return (short) _sid;
	}

	/**
	 * These BIFF record types are known but still uninterpreted by POI
	 *
	 * @return the documented name of this BIFF record type, <code>null</code> if unknown to POI
	 */
	public static String getBiffName(int sid) {
		// Note to POI developers:
		// Make sure you delete the corresponding entry from
		// this method any time a new Record subclass is created.
		switch (sid) {
			case PRINTSIZE_0033: return "PRINTSIZE";
			case PLS_004D: return "PLS";
			case 0x0050: return "DCON"; // Data Consolidation Information
			case 0x007F: return "IMDATA";
			case SHEETPR_0081: return "SHEETPR";
			case SORT_0090: return "SORT"; // Sorting Options
			case 0x0094: return "LHRECORD"; // .WK? File Conversion Information
			case STANDARDWIDTH_0099: return "STANDARDWIDTH"; //Standard Column Width
			case SCL_00A0: return "SCL"; // Window Zoom Magnification
			case 0x00AE: return "SCENMAN"; // Scenario Output Data

			case 0x00B2: return "SXVI";        // (pivot table) View Item
			case 0x00B4: return "SXIVD";       // (pivot table) Row/Column Field IDs
			case 0x00B5: return "SXLI";        // (pivot table) Line Item Array

			case 0x00D3: return "OBPROJ";
			case 0x00DC: return "PARAMQRY";
			case 0x00DE: return "OLESIZE";
			case BITMAP_00E9: return "BITMAP";
			case PHONETICPR_00EF: return "PHONETICPR";
			case 0x00F1: return "SXEX";        // PivotTable View Extended Information

			case LABELRANGES_015F: return "LABELRANGES";
			case 0x01BA: return "CODENAME";
			case 0x01A9: return "USERBVIEW";
			case 0x01AD: return "QSI";

//			case 0x01C0: return "EXCEL9FILE"; //ZSS-746

			case 0x0802: return "QSISXTAG";   // Pivot Table and Query Table Extensions
			case 0x0803: return "DBQUERYEXT";
			case 0x0805: return "TXTQUERY";
			case 0x0810: return "SXVIEWEX9";  // Pivot Table Extensions

			case 0x0812: return "CONTINUEFRT";
			case QUICKTIP_0800: return "QUICKTIP";
			case SHEETEXT_0862: return "SHEETEXT";
//			case 0x0863: return "BOOKEXT"; //ZSS-746
			case 0x0864: return "SXADDL";    // Pivot Table Additional Info
			case SHEETPROTECTION_0867: return "SHEETPROTECTION";
			case 0x086B: return "DATALABEXTCONTENTS";
			case 0x086C: return "CELLWATCH";
			case 0x0874: return "DROPDOWNOBJIDS";
			case 0x0876: return "DCONN";
			case 0x087B: return "CFEX";
			//20130916, hawkchen@potix.com, ZSS-272: for being printed
			case 0x087A: return "CF12";
//			case 0x087C: return "XFCRC"; //ZSS-746
//			case 0x087D: return "XFEXT"; //ZSS-746
			case 0x087F: return "CONTINUEFRT12";
//			case 0x088B: return "PLV"; //ZSS-746
//			case 0x088C: return "COMPAT12";  //ZSS-746
			case 0x088D: return "DXF";
//			case 0x0892: return "STYLEEXT"; //ZSS-746
//			case 0x0896: return "THEME";  //ZSS-746
			case 0x0897: return "GUIDTYPELIB";
//			case 0x089A: return "MTRSETTINGS"; //ZSS-746
//			case 0x089B: return "COMPRESSPICTURES"; //ZSS-746
//			case HEADER_FOOTER_089C: return "HEADERFOOTER"; //ZSS-746
			case 0x08A1: return "SHAPEPROPSSTREAM";
//			case 0x08A3: return "FORCEFULLCALCULATION"; //ZSS-746
			case 0x08A4: return "SHAPEPROPSSTREAM";
			case 0x08A5: return "TEXTPROPSSTREAM";
			case 0x08A6: return "RICHTEXTSTREAM";

//			case 0x08C8: return "PLV{Mac Excel}"; //ZSS-746


		}
		if (isObservedButUnknown(sid)) {
			return "UNKNOWN-" + Integer.toHexString(sid).toUpperCase();
		}

		return null;
	}

	/**
	 * @return <code>true</code> if the unknown record id has been observed in POI unit tests
	 */
	private static boolean isObservedButUnknown(int sid) {
		switch (sid) {
			case 0x0033:
				// contains 2 bytes of data: 0x0001 or 0x0003
			case 0x0034:
				// Seems to be written by MSAccess
				// contains text "[Microsoft JET Created Table]0021010"
				// appears after last cell value record and before WINDOW2
			case 0x01BD:
			case 0x01C2:
				// Written by Excel 2007
				// rawData is multiple of 12 bytes long
				// appears after last cell value record and before WINDOW2 or drawing records
			case 0x089D:
			case 0x089E:
			case 0x08A7:

			case 0x1001:
			case 0x1006:
			case 0x1007:
			case 0x1009:
			case 0x100A:
			case 0x100B:
			case 0x100C:
			case 0x1014:
			case 0x1017:
			case 0x1018:
			case 0x1019:
			case 0x101A:
			case 0x101B:
			case 0x101D:
			case 0x101E:
			case 0x101F:
			case 0x1020:
			case 0x1021:
			case 0x1022:
			case 0x1024:
			case 0x1025:
			case 0x1026:
			case 0x1027:
			case 0x1032:
			case 0x1033:
			case 0x1034:
			case 0x1035:
			case 0x103A:
			case 0x1041:
			case 0x1043:
			case 0x1044:
			case 0x1045:
			case 0x1046:
			case 0x104A:
			case 0x104B:
			case 0x104E:
			case 0x104F:
			case 0x1051:
			case 0x105C:
			case 0x105D:
			case 0x105F:
			case 0x1060:
			case 0x1062:
			case 0x1063:
			case 0x1064:
			case 0x1065:
			case 0x1066:
				return true;
		}
		return false;
	}

	public Object clone() {
		// immutable - OK to return this
		return this;
	}
}
