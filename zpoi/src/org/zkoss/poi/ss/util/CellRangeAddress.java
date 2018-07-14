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

package org.zkoss.poi.ss.util;

import org.zkoss.poi.ss.formula.SheetNameFormatter;
import org.zkoss.poi.hssf.record.RecordInputStream;
import org.zkoss.poi.hssf.record.SelectionRecord;
import org.zkoss.poi.util.LittleEndianByteArrayOutputStream;
import org.zkoss.poi.util.LittleEndianOutput;

/**
 * See OOO documentation: excelfileformat.pdf sec 2.5.14 - 'Cell Range Address'<p/>
 * 
 * <p>In the Microsoft documentation, this is also known as a 
 *  Ref8U - see page 831 of version 1.0 of the documentation.
 *
 * Note - {@link SelectionRecord} uses the BIFF5 version of this structure
 * @author Dragos Buleandra (dragos.buleandra@trade2b.ro)
 */
public class CellRangeAddress extends CellRangeAddressBase {
	/*
	 * TODO - replace  org.zkoss.poi.hssf.util.Region
	 */
	public static final int ENCODED_SIZE = 8;

	public CellRangeAddress(int firstRow, int lastRow, int firstCol, int lastCol) {
		super(firstRow, lastRow, firstCol, lastCol);
	}

	/**
	 * @deprecated use {@link #serialize(LittleEndianOutput)}
	 */
	public int serialize(int offset, byte[] data) {
		serialize(new LittleEndianByteArrayOutputStream(data, offset, ENCODED_SIZE));
		return ENCODED_SIZE;
	}
	public void serialize(LittleEndianOutput out) {
		out.writeShort(getFirstRow());
		out.writeShort(getLastRow());
		out.writeShort(getFirstColumn());
		out.writeShort(getLastColumn());
	}

	public CellRangeAddress(RecordInputStream in) {
		super(readUShortAndCheck(in), in.readUShort(), in.readUShort(), in.readUShort());
	}

	private static int readUShortAndCheck(RecordInputStream in) {
		if (in.remaining() < ENCODED_SIZE) {
			// Ran out of data
			throw new RuntimeException("Ran out of data reading CellRangeAddress");
		}
		return in.readUShort();
	}

	public CellRangeAddress copy() {
		return new CellRangeAddress(getFirstRow(), getLastRow(), getFirstColumn(), getLastColumn());
	}

	public static int getEncodedSize(int numberOfItems) {
		return numberOfItems * ENCODED_SIZE;
	}

    /**
     * @return the text format of this range.  Single cell ranges are formatted
     *         like single cell references (e.g. 'A1' instead of 'A1:A1').
     */
    public String formatAsString() {
        return formatAsString(null, false);
    }

    /**
     * @return the text format of this range using specified sheet name.
     */
    public String formatAsString(String sheetName, boolean useAbsoluteAddress) {
        StringBuffer sb = new StringBuffer();
        if (sheetName != null) {
            sb.append(SheetNameFormatter.format(sheetName));
            sb.append("!");
        }
        CellReference cellRefFrom = new CellReference(getFirstRow(), getFirstColumn(),
                useAbsoluteAddress, useAbsoluteAddress);
        CellReference cellRefTo = new CellReference(getLastRow(), getLastColumn(),
                useAbsoluteAddress, useAbsoluteAddress);
        sb.append(cellRefFrom.formatAsString());

        //for a single-cell reference return A1 instead of A1:A1
        //for full-column ranges or full-row ranges return A:A instead of A,
        //and 1:1 instead of 1         
        if(!cellRefFrom.equals(cellRefTo)
            || isFullColumnRange() || isFullRowRange()){
            sb.append(':');
            sb.append(cellRefTo.formatAsString());
        }
        return sb.toString();
    }

    /**
     * Creates a CellRangeAddress from a cell range reference string.
     *  
     * @param ref usually a standard area ref (e.g. "B1:D8").  May be a single 
     *            cell ref (e.g. "B5") in which case the result is a 1 x 1 cell 
     *            range. May also be a whole row range (e.g. "3:5"), or a whole 
     *            column range (e.g. "C:F")
     */
    public static CellRangeAddress valueOf(String ref) {
        int sep = ref.indexOf(":");
        CellReference a;
        CellReference b;
        if (sep == -1) {
            a = new CellReference(ref);
            b = a;
        } else {
            a = new CellReference(ref.substring(0, sep));
            b = new CellReference(ref.substring(sep + 1));
        }
        return new CellRangeAddress(a.getRow(), b.getRow(), a.getCol(), b.getCol());
    }

}
