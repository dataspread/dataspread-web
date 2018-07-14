
package org.zkoss.poi.hssf.record;

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

public final class AutoFilter12Record
    extends StandardRecord
{
    public final static short sid = 0x087E;

    private byte[] _frtRefHeader = new byte[12];
    private short _iEntry;
    private int _fHideArrow;
    private int _ft;
    private int _cft;
    private int _cCriteria;
	private int _cDateGroupings;
    private short _flags;
    private int _unused2;
    private int _idList;
    private byte[] _guidSview = new byte[16];
    private AF12Criteria[] _rgCriteria;
    
    //TODO: how to handle such variable length data?
    //rgb (variable)
    
    //rgCriteria (variable)
    
    //rgDateGroupings(variable)

	private static final BitField opt_A                        = new BitField(0xc000);
	private static final BitField opt_B                        = new BitField(0x2000);

	public class AF12Criteria{
		byte[] doper = new byte[10];
		String str;
		int length;
	}
	
    public AutoFilter12Record()
    {
    }

    public AutoFilter12Record(RecordInputStream in)
    {
    	in.read(_frtRefHeader, 0, 12);
    	_iEntry = in.readShort();
    	_fHideArrow = in.readInt();
    	_ft = in.readInt();
    	_cft = in.readInt();
    	_cCriteria = in.readInt();
    	_cDateGroupings = in.readInt();
    	_flags = in.readShort();
    	_unused2 = in.readInt();
    	_idList = in.readInt();
    	in.read(_guidSview, 0, 16);
    	
    	//TODO: rgb, 
    	if(_ft!=0){
    		//if _ft == 0, rgb not exist 
    	}
    	
    	//The data is actually in the following ContinueFrt12 Record
    	//Not in here
    	/*
    	//rgCriteria, 
    	if(_cCriteria != 0){
    		_rgCriteria = new AF12Criteria[_cCriteria];
    		for(int i = 0 ; i < _cCriteria; i++){
    			_rgCriteria[i] = new AF12Criteria();
    			in.read(_rgCriteria[i].doper, 0, 10);
    		  	if(_rgCriteria[i].doper[0] == 0x06){
    	    		byte length = _rgCriteria[i].doper[6];
    	    		boolean isMultibyte = (in.readByte() != 0);
    	    		_rgCriteria[i].length = 1+ length;
    	    		if(isMultibyte){
    	    			_rgCriteria[i].str = StringUtil.readUnicodeLE(in, length);
    	    		}else{
    	    			_rgCriteria[i].str = StringUtil.readCompressedUnicode(in, length);	
    	    		}
    	    	}
    		}
    	}
    	*/
    	
    	//TODO: rgDateGroupings
    	if(_cDateGroupings != 0){
    		//if _cDateGroupings == 0, rgDateGroupings not exist 
    	}
    }


    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[AUTOFILTER12]\n");
        
        buffer.append("    ._frtRefHeader          = ")
        	.append(HexDump.toHex(_frtRefHeader)).append("\n");
        buffer.append("    ._iEntry                = ")
        	.append(_iEntry).append("\n");        
        buffer.append("    ._fHideArrow            = ")
        	.append(_fHideArrow).append("\n");
        buffer.append("    ._ft                    = ")
        	.append(_ft).append("\n");
        buffer.append("    ._cft                   = ")
        	.append(_cft).append("\n");
        buffer.append("    ._cCriteria             = ")
        	.append(_cCriteria).append("\n");
        buffer.append("    ._cDateGroupings        = ")
        	.append(_cDateGroupings).append("\n");
        
        buffer.append("    ._flags                 = ")
    		.append(_flags).append("\n");        
        buffer.append("        .A                  = ").append(opt_A.getShortRawValue(_flags)).append("\n");
        buffer.append("        .B                  = ").append(opt_B.getShortRawValue(_flags)).append("\n");
        
        buffer.append("    ._idList                = ")
        	.append(_idList).append("\n");
        buffer.append("    ._guidSview             = ")
        	.append(HexDump.toHex(_guidSview)).append("\n");

        //TODO:
        /*
        buffer.append("    ._rgCriteria            = ")
        	.append(_rgCriteria).append("\n");
        for(int i=0;i<_cCriteria;i++){
            buffer.append("    .doper"+i+"          = ")
        	.append(HexDump.toHex(_rgCriteria[i].doper)).append("\n");
    	    buffer.append("        .vt          = ").append(_rgCriteria[i].doper).append("\n");
    	    buffer.append("        .grbitSign   = ").append(_rgCriteria[i].doper).append("\n");
            buffer.append("    .str                 = ")
        		.append(_rgCriteria[i].str).append("\n");
        }
        */
        
        //TODO:
        
        buffer.append("[/AUTOFILTER12]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
//        out.writeShort(_iEntry);
    }

    protected int getDataSize() {
        return 2;
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

    public int getCCriteria() {
		return _cCriteria;
	}
   
}