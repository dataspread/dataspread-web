
package org.zkoss.poi.hssf.record;

import org.zkoss.poi.hssf.record.common.UnicodeString;
import org.zkoss.poi.util.BitField;
import org.zkoss.poi.util.HexDump;
import org.zkoss.poi.util.LittleEndianOutput;
import org.zkoss.poi.util.StringUtil;

/**
 * The AutoFilter record 
 *
 * @author PeterKuo
 * @deprecated by dennischen@zkoss.org, 2013/8/13, this class doesn't implement well and cause ZSS-408 Cannot save 2003 format if the file contains auto filter configuration.
 */

public final class AutoFilterRecord
    extends StandardRecord
{
    public final static short sid = 0x9E;

    private short   _iEntry;   // = 0;
    private short	_flags;
    private byte[] _doper1 = new byte[10];
    private byte[] _doper2 = new byte[10];
    private String _str1;
	private String _str2;
    
    private int _str1Length = 0;
    private int _str2Length = 0;
    
	private static final BitField opt_wJoin                        = new BitField(0xc000);
	private static final BitField opt_fSimple1                     = new BitField(0x2000);
	private static final BitField opt_fSimple2                     = new BitField(0x1000);
	private static final BitField opt_fTopN                        = new BitField(0x0800);
	private static final BitField opt_fTop                         = new BitField(0x0400);
	private static final BitField opt_fPercent                     = new BitField(0x0200);
	private static final BitField opt_wTopN                        = new BitField(0x01ff);   
	
    public AutoFilterRecord()
    {
    }

//    public class AFDOper{
//    	byte _vt;
//    	byte grbitSign;
//    	byte[] vtValue = new byte[8];
//    }
    
    public AutoFilterRecord(RecordInputStream in)
    {
    	_iEntry = in.readShort();
    	_flags = in.readShort();
    	in.read(_doper1, 0, 10);
    	in.read(_doper2, 0, 10);
    	
    	if(_doper1[0] == 0x06){
    		byte length = _doper1[6];
    		boolean isMultibyte = (in.readByte() != 0);
    		_str1Length =  1+ length;
    		if(isMultibyte){
    			_str1 = StringUtil.readUnicodeLE(in, length);
    		}else{
    			_str1 = StringUtil.readCompressedUnicode(in, length);	
    		}
    	}
    		
    	
    	if(_doper2[0] == 0x06){
    		byte length = _doper2[6];
    		boolean isMultibyte = (in.readByte() != 0);
    		_str2Length =  1+ length;
    		if(isMultibyte){
    			_str2 = StringUtil.readUnicodeLE(in, length);
    		}else{
    			_str2 = StringUtil.readCompressedUnicode(in, length);	
    		}
    	}
    }


    public void setColEntries(short num)
    {
    	_iEntry = num;
    }


    public short getColEntries()
    {
        return _iEntry;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[AUTOFILTER]\n");
        buffer.append("    .iEntry          = ")
            .append(_iEntry).append("\n");
        buffer.append("    .flags           = ")
        	.append(_flags).append("\n");
        
        buffer.append("        .wJoin       = ").append(opt_wJoin.getShortRawValue(_flags)).append("\n");
        buffer.append("        .fSimple1    = ").append(opt_fSimple1.getShortRawValue(_flags)).append("\n");
        buffer.append("        .fSimple2    = ").append(opt_fSimple2.getShortRawValue(_flags)).append("\n");
        buffer.append("        .fTopN       = ").append(opt_fTopN.getShortRawValue(_flags)).append("\n");
        buffer.append("        .fTop        = ").append(opt_fTop.getShortRawValue(_flags)).append("\n");
        buffer.append("        .fPercent    = ").append(opt_fPercent.getShortRawValue(_flags)).append("\n");
        buffer.append("        .wTopN       = ").append(opt_wTopN.getShortRawValue(_flags)).append("\n");
        
        buffer.append("    .doper1          = ")
        	.append(HexDump.toHex(_doper1)).append("\n");
        
        buffer.append("        .vt          = ").append(_doper1[0]).append("\n");
        buffer.append("        .grbitSign   = ").append(_doper1[1]).append("\n");
        
        buffer.append("    .doper2          = ")
    	.append(HexDump.toHex(_doper2)).append("\n");
    
	    buffer.append("        .vt          = ").append(_doper2[0]).append("\n");
	    buffer.append("        .grbitSign   = ").append(_doper2[1]).append("\n");

        
        buffer.append("    .str1            = ")
        	.append(_str1).append("\n");
        buffer.append("    .str2            = ")
			.append(_str2).append("\n");
        
        buffer.append("[/AUTOFILTER]\n");
        return buffer.toString();
    }

    //TODO:
    public void serialize(LittleEndianOutput out) {
        out.writeShort(_iEntry);
    }

    protected int getDataSize() {
    	//_iEntry+_flags+_doper1+_doper2 +??
        return 2+2+10+10+_str1Length+_str2Length;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public Object clone()
    {
    	return cloneViaReserialise();
    }
 
    public String getStr1() {
		return _str1;
	}

    public String getStr2() {
		return _str2;
	}
}