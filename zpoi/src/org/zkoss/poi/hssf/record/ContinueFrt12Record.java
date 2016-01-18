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

import org.zkoss.poi.util.HexDump;
import org.zkoss.poi.util.LittleEndianOutput;
import org.zkoss.poi.util.StringUtil;

/**
 * 
 * @author PeterKuo
 * @deprecated by dennischen@zkoss.org, 2013/8/13, this class doesn't implement well and cause ZSS-408 Cannot save 2003 format if the file contains auto filter configuration.
 */
public final class ContinueFrt12Record extends StandardRecord {
    public final static short sid = 0x087F;
    
    //TODO: currently, only for AutoFilter12 Record
    private byte[] _frtRefHeader = new byte[12];
    private byte[] _doper = new byte[10];
    private String _str;
    

	public void serialize(LittleEndianOutput out) {
    }


    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[CONTINUEFRT12]\n");
        
        buffer.append("    ._frtRefHeader   = ")
    	.append(HexDump.toHex(_frtRefHeader)).append("\n");
    

        buffer.append("[CONTINUEFRT12 RECORD]\n");
        buffer.append("    .doper           = ")
        	.append(HexDump.toHex(_doper)).append("\n");
    
        buffer.append("    .str            = ")
    		.append(_str).append("\n");
        
        
        buffer.append("[/CONTINUEFRT12]\n");
        return buffer.toString();
    }

    public short getSid() {
        return sid;
    }

    public ContinueFrt12Record(RecordInputStream in) {
    	in.read(_frtRefHeader, 0, 12);
    	in.read(_doper, 0, 10);
    	byte length = _doper[2];
    	boolean isMultibyte = (in.readByte() != 0);
		if(isMultibyte){
			_str = StringUtil.readUnicodeLE(in, length);
		}else{
			_str = StringUtil.readCompressedUnicode(in, length);	
		}

    }

    @Override
    public Object clone()
    {
    	return cloneViaReserialise();
    }
    
	@Override
	protected int getDataSize() {
		// TODO Auto-generated method stub
		return 0;
	}
	
    public String getStr() {
		return _str;
	}

	
}
