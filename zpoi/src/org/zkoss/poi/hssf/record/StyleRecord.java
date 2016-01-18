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

import org.zkoss.poi.hssf.record.common.BuiltInStyle;
import org.zkoss.poi.hssf.record.common.XLUnicodeString;
import org.zkoss.poi.util.BitField;
import org.zkoss.poi.util.BitFieldFactory;
import org.zkoss.poi.util.HexDump;
import org.zkoss.poi.util.LittleEndianOutput;
import org.zkoss.poi.util.StringUtil;

/**
 * Title:        Style Record (0x0293)<p/>
 * Description:  Describes a builtin to the gui or user defined style<P>
 * REFERENCE:  PG 390 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author aviks : string fixes for UserDefined Style
 * see [MS-XLS].pdf  2.4.269 Style  page 436
 */
public final class StyleRecord extends StandardRecord {
	public final static short sid = 0x0293;

	private static final BitField styleIndexMask = BitFieldFactory.getInstance(0x0FFF);
	private static final BitField isBuiltinFlag  = BitFieldFactory.getInstance(0x8000);

	/** shared by both user defined and built-in styles */
	private int field_1_xf_index;

	// only for built in styles (optional)
	private BuiltInStyle builtInData;

	// only for user defined styles
	private XLUnicodeString user;

	/**
	 * creates a new style record, initially set to 'built-in'
	 */
	public StyleRecord() {
		field_1_xf_index = isBuiltinFlag.set(field_1_xf_index);
	}

	public StyleRecord(int index, XLUnicodeString user) {
		setXFIndex(index);
		setBuiltin(false);
		this.user = user;
	}
	
	public StyleRecord(int index, BuiltInStyle data) {
		setXFIndex(index);
		setBuiltin(true);
		this.builtInData = data;
	}

	public StyleRecord(RecordInputStream in) {
		field_1_xf_index = ((int)in.readShort()) & 0xffff;
		if (isBuiltin()) {
			builtInData = new BuiltInStyle(in);
		} else {
			user = new XLUnicodeString(in);
		}
	}

	/**
	 * set the actual index of the style extended format record
	 * @param xfIndex of the xf record
	 */
	public void setXFIndex(int xfIndex) {
		field_1_xf_index = styleIndexMask.setValue(field_1_xf_index, xfIndex);
	}

	/**
	 * get the actual index of the style extended format record
	 * @see #getXFIndex() 
	 * @return index of the xf record
	 */
	public int getXFIndex() {
		return styleIndexMask.getValue(field_1_xf_index);
	}

	/**
	 * set the style's name
	 * @param name of the style
	 */
	public void setName(String name) {
		user = new XLUnicodeString(name, true);
		field_1_xf_index = isBuiltinFlag.clear(field_1_xf_index);
		builtInData = null;
	}

	/**
	 * if this is a builtin style set the number of the built in style
	 * @param  builtinStyleId style number (0-7)
	 *
	 */
	public void setBuiltinStyle(int builtinStyleId) {
		field_1_xf_index = isBuiltinFlag.set(field_1_xf_index);
		if (builtInData == null) {
			builtInData = new BuiltInStyle(builtinStyleId, -1);
		} else {
			builtInData.setBuiltInType(builtinStyleId);
		}
		user = null;
	}

	/**
	 * set the row or column level of the style (if builtin 1||2)
	 */
	public void setOutlineStyleLevel(int level) {
		field_1_xf_index = isBuiltinFlag.set(field_1_xf_index);
		if (builtInData == null) {
			builtInData = new BuiltInStyle(0, -1);
		} else {
			builtInData.setOutlineLevel(level);
		}
		user = null;
	}

	public boolean isBuiltin(){
		return isBuiltinFlag.isSet(field_1_xf_index);
	}
	
	public void setBuiltin(boolean flag) {
		field_1_xf_index = isBuiltinFlag.setBoolean(field_1_xf_index, flag) & 0xffff;
	}

	/**
	 * get the style's name
	 * @return name of the style
	 */
	public String getName() {
		return isBuiltin() ? "" : user.getString();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("[STYLE]\n");
		sb.append("    .xf_index_raw      = ").append(HexDump.shortToHex(field_1_xf_index)).append("\n");
		sb.append("        .type          = ").append(isBuiltin() ? "built-in" : "user-defined").append("\n");
		sb.append("        .xf_index      = ").append(HexDump.shortToHex(getXFIndex())).append("\n");
		if (isBuiltin()){
			sb.append("        .builtInData = \n");
			builtInData.appendString(sb, "            ");
		} else {
			sb.append("    .name          = \n");
			user.appendString(sb, "            ");
		}
		sb.append("[/STYLE]\n");
		return sb.toString();
	}

	
	protected int getDataSize() {
		if (isBuiltin()) {
			return 4; // short, byte, byte
		}
		return 2 // short xf index 
			+ user.getDataSize();
	}

	public void serialize(LittleEndianOutput out) {
		out.writeShort(field_1_xf_index);
		if (isBuiltin()) {
			builtInData.serialize(out);
		} else {
			user.serialize(out);
		}
	}

	public short getSid() {
		return sid;
	}
}
