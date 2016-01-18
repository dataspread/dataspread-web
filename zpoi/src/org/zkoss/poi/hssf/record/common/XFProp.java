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

package org.zkoss.poi.hssf.record.common;

import org.zkoss.poi.hssf.record.FullColorExt;
import org.zkoss.poi.hssf.record.RecordInputStream;
import org.zkoss.poi.util.HexDump;
import org.zkoss.poi.util.LittleEndianOutput;

/**
 * An array of formatting properties
 * @author henri
 * see [MS-XLS].pdf 2.5.283  page 942
 */
public class XFProp {
	//xfPropType
	public static final int FILL_PATTERN = 0;       //FillPattern
	public static final int FOREGROUND_COLOR = 1;	//XFPropColor
	public static final int BACKGROUND_COLOR = 2;	//XFPropColor
	public static final int GRADIENT = 3;			//XFPropGradient
	public static final int GRADIENT_STOP = 4;  	//XFPropGradientStop
	public static final int TEXT_COLOR = 5;			//XFPropColor
	public static final int TOP_BORDER = 6;			//XFPropBorder
	public static final int BOTTOM_BORDER = 7;		//XFPropBorder
	public static final int LEFT_BORDER = 8;		//XFPropBorder
	public static final int RIGHT_BORDER = 9;		//XFPropBorder
	public static final int DIAG_BORDER = 0x0A;		//XFPropBorder
	public static final int VERT_BORDER = 0x0B;		//XFPropBorder
	public static final int HORZ_BORDER = 0x0C;		//XFPropBorder
	public static final int USE_DIAG_UP_BORDER = 0x0D;  //byte: 0 or 1
	public static final int USE_DIAG_DOWN_BORDER = 0x0E; //byte: 0 or 1
	public static final int HORZ_ALIGN = 0x0F;			//HorizAlign
	public static final int VERT_ALIGN = 0x10;			//VertAlign
	public static final int TEXT_ROTATION = 0x011;	//XFPropTextRotation
	public static final int ABS_INDENTION = 0x12;	//unsigned short, <= 15
	public static final int READING_ORDER = 0x13;	//ReadingOrder
	public static final int WRAPPED = 0x14;			//byte: 0 or 1
	public static final int JUSTIFIED = 0x15;       //byte: 0 or 1
	public static final int SHRINK_FIT = 0x16;		//byte: 0 or 1
	public static final int MERGED = 0x17;			//byte: 0 or 1
	public static final int FONT_NAME = 0x18;		//LPWideString
	public static final int BOLD = 0x19;			//Bold
	public static final int UNDERLINE = 0x1A;		//Underline
	public static final int SCRIPT = 0x1B;			//Script
	public static final int ITALIC = 0x1C;			//byte: 0 or 1
	public static final int STRIKETHRU = 0x1D;		//byte: 0 or 1
	public static final int OUTLINE = 0x1E;			//byte: 0 or 1
	public static final int SHADOW = 0x1F;			//byte: 0 or 1
	public static final int CONDENSED = 0x20;		//byte: 0 or 1
	public static final int EXTENDED = 0x21;		//byte: 0 or 1
	public static final int CHARSET = 0x22;			//byte
	public static final int FONT_FAMILY = 0x23;		//byte: <= 5
	public static final int FONT_SIZE = 0x24;		//int 20~8191 (twips)
	public static final int FONT_SCHEME = 0x25;		//FontScheme
	public static final int NUM_FORMAT = 0x26;		//stFormat of FormatRecord
	public static final int FORMAT_ID = 0x29;		//IFmt
	public static final int REL_INDENTION = 0x2A;	//short; added to previous indention: >= -15; <= 15, 255: N/A
	public static final int LOCKED = 0x2B;			//byte: 0 or 1
	public static final int HIDDEN = 0x2C;			//byte: 0 or 1
	
	final private int xfPropType;
	final private int cb;
	final private Object xfPropData;
	
	public XFProp(RecordInputStream in) {
		xfPropType = ((int)in.readShort()) & 0xffff;
		cb = ((int)in.readShort()) & 0xffff;
		switch(xfPropType) {
		case FOREGROUND_COLOR:	//XFPropColor
		case BACKGROUND_COLOR:	//XFPropColor
		case TEXT_COLOR:		//XFPropColor
			xfPropData = new XFPropColor(in);
			break;

		case GRADIENT:			//XFPropGradient
			xfPropData = new XFPropGradient(in);
			break;
			
		case GRADIENT_STOP:  	//XFPropGradientStop
			xfPropData = new XFPropGradientStop(in);
			break;
			
		case TOP_BORDER:		//XFPropBorder
		case BOTTOM_BORDER:		//XFPropBorder
		case LEFT_BORDER:		//XFPropBorder
		case RIGHT_BORDER:		//XFPropBorder
		case DIAG_BORDER:		//XFPropBorder
		case VERT_BORDER:		//XFPropBorder
		case HORZ_BORDER:		//XFPropBorder
			xfPropData = new XFPropBorder(in);
			break;
			
		case FONT_SCHEME:	//byte: FontScheme type
		case SCRIPT:		//byte: Script type
		case UNDERLINE:		//byte: Underline type
		case BOLD:			//Bold: 0x0190 or 0x02bc
		case READING_ORDER:	    //byte: ReadingOrder enumeration
		case TEXT_ROTATION:		//byte: 00 ~ 90 (countclockwise 0~90), 91 ~ 180 (clockwise 1 ~ 90), 0xff (vertical text)
		case VERT_ALIGN:			//byte: VertAlign enumeration type
		case HORZ_ALIGN:			//byte: HorizAlign enumeration type
		case FILL_PATTERN:      //byte: FillPattern enumeration type
		case USE_DIAG_UP_BORDER:   //byte: 0 or 1
		case USE_DIAG_DOWN_BORDER: //byte: 0 or 1
		case WRAPPED:			//byte: 0 or 1
		case JUSTIFIED:       	//byte: 0 or 1
		case SHRINK_FIT:		//byte: 0 or 1
		case MERGED:			//byte: 0 or 1
		case ITALIC:			//byte: 0 or 1
		case STRIKETHRU:		//byte: 0 or 1
		case OUTLINE:			//byte: 0 or 1
		case SHADOW:			//byte: 0 or 1
		case CONDENSED:        	//byte: 0 or 1
		case EXTENDED:        	//byte: 0 or 1
		case CHARSET: 			//byte
		case FONT_FAMILY:		//byte: <= 5
		case LOCKED:			//byte: 0 or 1
		case HIDDEN:			//byte: 0 or 1
			xfPropData = in.readByte();
			break;
			
		case FORMAT_ID:		//short: identifier of a number format
		case ABS_INDENTION:	//unsigned short, <= 15
		case REL_INDENTION:	//short; added to previous indention: >= -15; <= 15, 255: N/A
			xfPropData = in.readShort();
			break;
			
		case FONT_NAME:		//LPWideString
			xfPropData = new LPWideString(in);
			break;
			
		case FONT_SIZE:		//int 20~8191 (twips)
			xfPropData = in.readInt();
			break;
			
		case NUM_FORMAT:		//stFormat of FormatRecord
			xfPropData = new XLUnicodeString(in);
			break;
			
		default:
			throw new RuntimeException("Unknown xfPropType: "+ xfPropType);
		}
	}
	
	public XFProp(int type, Object data) {
		xfPropType = type;
		xfPropData = data;
		switch(xfPropType) {
		case FOREGROUND_COLOR:	//XFPropColor
		case BACKGROUND_COLOR:	//XFPropColor
		case TEXT_COLOR:		//XFPropColor
			cb = 4 + ((XFPropColor) xfPropData).getDataSize();
			break;

		case GRADIENT:			//XFPropGradient
			cb = 4 + ((XFPropGradient) xfPropData).getDataSize();
			break;
			
		case GRADIENT_STOP:  	//XFPropGradientStop
			cb = 4 + ((XFPropGradientStop) xfPropData).getDataSize();
			break;
			
		case TOP_BORDER:		//XFPropBorder
		case BOTTOM_BORDER:		//XFPropBorder
		case LEFT_BORDER:		//XFPropBorder
		case RIGHT_BORDER:		//XFPropBorder
		case DIAG_BORDER:		//XFPropBorder
		case VERT_BORDER:		//XFPropBorder
		case HORZ_BORDER:		//XFPropBorder
			cb = 4 + ((XFPropBorder) xfPropData).getDataSize();
			break;
			
		case FONT_SCHEME:	//byte: FontScheme type
		case SCRIPT:		//byte: Script type
		case UNDERLINE:		//byte: Underline type
		case BOLD:			//Bold: 0x0190 or 0x02bc
		case READING_ORDER:	    //byte: ReadingOrder enumeration
		case TEXT_ROTATION:		//byte: 00 ~ 90 (countclockwise 0~90), 91 ~ 180 (clockwise 1 ~ 90), 0xff (vertical text)
		case VERT_ALIGN:			//byte: VertAlign enumeration type
		case HORZ_ALIGN:			//byte: HorizAlign enumeration type
		case FILL_PATTERN:      //byte: FillPattern enumeration type
		case USE_DIAG_UP_BORDER:   //byte: 0 or 1
		case USE_DIAG_DOWN_BORDER: //byte: 0 or 1
		case WRAPPED:			//byte: 0 or 1
		case JUSTIFIED:       	//byte: 0 or 1
		case SHRINK_FIT:		//byte: 0 or 1
		case MERGED:			//byte: 0 or 1
		case ITALIC:			//byte: 0 or 1
		case STRIKETHRU:		//byte: 0 or 1
		case OUTLINE:			//byte: 0 or 1
		case SHADOW:			//byte: 0 or 1
		case CONDENSED:        	//byte: 0 or 1
		case EXTENDED:        	//byte: 0 or 1
		case CHARSET: 			//byte
		case FONT_FAMILY:		//byte: <= 5
		case LOCKED:			//byte: 0 or 1
		case HIDDEN:			//byte: 0 or 1
			cb = 4 + 1;
			break;
			
		case FORMAT_ID:		//short: identifier of a number format
		case ABS_INDENTION:	//unsigned short, <= 15
		case REL_INDENTION:	//short; added to previous indention: >= -15; <= 15, 255: N/A
			cb = 4 + 2;
			break;
			
		case FONT_NAME:		//LPWideString
			cb = 4 + ((LPWideString) xfPropData).getDataSize();
			break;
			
		case FONT_SIZE:		//int 20~8191 (twips)
			cb = 4 + 4;
			break;
			
		case NUM_FORMAT:		//stFormat of FormatRecord
			cb = 4 + ((XLUnicodeString) xfPropData).getDataSize();
			break;
			
		default:
			throw new RuntimeException("Unknown xfPropType: "+ xfPropType + ", xfPropData:" + xfPropData);
		}
		
	}
	public int getDataSize() {
		return cb;
	}
	
	public int getXFPropType() {
		return xfPropType;
	}
	
	public Object getXFPropData() {
		return xfPropData;
	}

	public void serialize(LittleEndianOutput out) {
		out.writeShort(xfPropType);
		out.writeShort(cb);
		switch(xfPropType) {
		case FOREGROUND_COLOR:	//XFPropColor
		case BACKGROUND_COLOR:	//XFPropColor
		case TEXT_COLOR:		//XFPropColor
			((XFPropColor) xfPropData).serialize(out);
			break;

		case GRADIENT:			//XFPropGradient
			((XFPropGradient) xfPropData).serialize(out);
			break;
			
		case GRADIENT_STOP:  	//XFPropGradientStop
			((XFPropGradientStop) xfPropData).serialize(out);
			break;
			
		case TOP_BORDER:		//XFPropBorder
		case BOTTOM_BORDER:		//XFPropBorder
		case LEFT_BORDER:		//XFPropBorder
		case RIGHT_BORDER:		//XFPropBorder
		case DIAG_BORDER:		//XFPropBorder
		case VERT_BORDER:		//XFPropBorder
		case HORZ_BORDER:		//XFPropBorder
			((XFPropBorder) xfPropData).serialize(out);
			break;
			
		case FONT_SCHEME:	//byte: FontScheme type
		case SCRIPT:		//byte: Script type
		case UNDERLINE:		//byte: Underline type
		case BOLD:			//Bold: 0x0190 or 0x02bc
		case READING_ORDER:	    //byte: ReadingOrder enumeration
		case TEXT_ROTATION:		//byte: 00 ~ 90 (countclockwise 0~90), 91 ~ 180 (clockwise 1 ~ 90), 0xff (vertical text)
		case VERT_ALIGN:			//byte: VertAlign enumeration type
		case HORZ_ALIGN:			//byte: HorizAlign enumeration type
		case FILL_PATTERN:      //byte: FillPattern enumeration type
		case USE_DIAG_UP_BORDER:   //byte: 0 or 1
		case USE_DIAG_DOWN_BORDER: //byte: 0 or 1
		case WRAPPED:			//byte: 0 or 1
		case JUSTIFIED:       	//byte: 0 or 1
		case SHRINK_FIT:		//byte: 0 or 1
		case MERGED:			//byte: 0 or 1
		case ITALIC:			//byte: 0 or 1
		case STRIKETHRU:		//byte: 0 or 1
		case OUTLINE:			//byte: 0 or 1
		case SHADOW:			//byte: 0 or 1
		case CONDENSED:        	//byte: 0 or 1
		case EXTENDED:        	//byte: 0 or 1
		case CHARSET: 			//byte
		case FONT_FAMILY:		//byte: <= 5
		case LOCKED:			//byte: 0 or 1
		case HIDDEN:			//byte: 0 or 1
			out.writeByte(((Number)xfPropData).intValue());
			break;
			
		case FORMAT_ID:		//short: identifier of a number format
		case ABS_INDENTION:	//unsigned short, <= 15
		case REL_INDENTION:	//short; added to previous indention: >= -15; <= 15, 255: N/A
			out.writeShort(((Number) xfPropData).intValue());
			break;
			
		case FONT_NAME:		//LPWideString
			((LPWideString) xfPropData).serialize(out);
			break;
			
		case FONT_SIZE:		//int 20~8191 (twips)
			out.writeInt(((Number) xfPropData).intValue());
			break;
			
		case NUM_FORMAT:		//stFormat of FormatRecord
			((XLUnicodeString) xfPropData).serialize(out);
			break;
			
		default:
			throw new RuntimeException("Unknown xfPropType: "+ xfPropType + ", xfPropData:" + xfPropData);
		}
	}
	
	public void appendString(StringBuffer sb, String prefix) {
		sb.append(prefix).append(".xfPropType = ")
		  .append(getPropTypeName())
		  .append("(").append(HexDump.shortToHex(xfPropType)).append(")\n");
		sb.append(prefix).append(".cb         = ").append(cb).append("\n");
		appendString0(sb, prefix);
	}

	private void appendString0(StringBuffer sb, String prefix) {
		switch(xfPropType) {
		case FOREGROUND_COLOR:	//XFPropColor
		case BACKGROUND_COLOR:	//XFPropColor
		case TEXT_COLOR:		//XFPropColor
			sb.append(prefix).append(".xfPropData = \n");
			((XFPropColor) xfPropData).appendString(sb, prefix + "    ");
			break;

		case GRADIENT:			//XFPropGradient
			((XFPropGradient) xfPropData).appendString(sb, prefix + "    ");
			break;
			
		case GRADIENT_STOP:  	//XFPropGradientStop
			((XFPropGradientStop) xfPropData).appendString(sb, prefix + "    ");
			break;
			
		case TOP_BORDER:		//XFPropBorder
		case BOTTOM_BORDER:		//XFPropBorder
		case LEFT_BORDER:		//XFPropBorder
		case RIGHT_BORDER:		//XFPropBorder
		case DIAG_BORDER:		//XFPropBorder
		case VERT_BORDER:		//XFPropBorder
		case HORZ_BORDER:		//XFPropBorder
			((XFPropBorder) xfPropData).appendString(sb, prefix + "    ");
			break;
			
		case FONT_SCHEME:	//byte: FontScheme type
		case SCRIPT:		//byte: Script type
		case UNDERLINE:		//byte: Underline type
		case BOLD:			//Bold: 0x0190 or 0x02bc
		case READING_ORDER:	    //byte: ReadingOrder enumeration
		case TEXT_ROTATION:		//byte: 00 ~ 90 (countclockwise 0~90), 91 ~ 180 (clockwise 1 ~ 90), 0xff (vertical text)
		case VERT_ALIGN:			//byte: VertAlign enumeration type
		case HORZ_ALIGN:			//byte: HorizAlign enumeration type
		case FILL_PATTERN:      //byte: FillPattern enumeration type
		case USE_DIAG_UP_BORDER:   //byte: 0 or 1
		case USE_DIAG_DOWN_BORDER: //byte: 0 or 1
		case WRAPPED:			//byte: 0 or 1
		case JUSTIFIED:       	//byte: 0 or 1
		case SHRINK_FIT:		//byte: 0 or 1
		case MERGED:			//byte: 0 or 1
		case ITALIC:			//byte: 0 or 1
		case STRIKETHRU:		//byte: 0 or 1
		case OUTLINE:			//byte: 0 or 1
		case SHADOW:			//byte: 0 or 1
		case CONDENSED:        	//byte: 0 or 1
		case EXTENDED:        	//byte: 0 or 1
		case CHARSET: 			//byte
		case FONT_FAMILY:		//byte: <= 5
		case LOCKED:			//byte: 0 or 1
		case HIDDEN:			//byte: 0 or 1
			sb.append(prefix).append(".xfPropData = 0x")
			  .append(Integer.toHexString(					  
					  ((int)((Number)xfPropData).byteValue()) & 0xff))
			  .append("\n");
			break;
			
		case FORMAT_ID:		//short: identifier of a number format
		case ABS_INDENTION:	//unsigned short, <= 15
		case REL_INDENTION:	//short; added to previous indention: >= -15; <= 15, 255: N/A
			sb.append(prefix).append(".xfPropData = 0x")
			  .append(Integer.toHexString(					  
					  ((int)((Number)xfPropData).shortValue()) & 0xffff))
			  .append("\n");
			break;
			
		case FONT_NAME:		//LPWideString
			((LPWideString) xfPropData).appendString(sb, prefix + "    ");
			break;
			
		case FONT_SIZE:		//int 20~8191 (twips)
			sb.append(prefix).append(".xfPropData = 0x")
			  .append(Integer.toHexString(((Number)xfPropData).intValue()))
			  .append("\n");
			break;
			
		case NUM_FORMAT:		//stFormat of FormatRecord
			((XLUnicodeString) xfPropData).appendString(sb, prefix + "    ");
			break;
			
		default:
			throw new RuntimeException("Unknown xfPropType: "+ xfPropType + ", xfPropData:" + xfPropData);
		}
		
	}
	
	private String getPropTypeName() {
		switch(xfPropType) {
		case FOREGROUND_COLOR:	//XFPropColor
			return "FOREGROUND_COLOR";
		case BACKGROUND_COLOR:	//XFPropColor
			return "BACKGROUND_COLOR";
		case TEXT_COLOR:		//XFPropColor
			return "TEXT_COLOR";
		case GRADIENT:			//XFPropGradient
			return "GRADIENT";
		case GRADIENT_STOP:  	//XFPropGradientStop
			return "GRADIENT_STOP";
		case TOP_BORDER:		//XFPropBorder
			return "TOP_BORDER";
		case BOTTOM_BORDER:		//XFPropBorder
			return "BOTTOM_BORDER";
		case LEFT_BORDER:		//XFPropBorder
			return "LEFT_BORDER";
		case RIGHT_BORDER:		//XFPropBorder
			return  "RIGHT_BORDER";
		case DIAG_BORDER:		//XFPropBorder
			return "DIAG_BORDER";
		case VERT_BORDER:		//XFPropBorder
			return "VERT_BORDER";
		case HORZ_BORDER:		//XFPropBorder
			return "HORZ_BORDER";
		case FONT_SCHEME:	//byte: FontScheme type
			return "FONT_SCHEME";
		case SCRIPT:		//byte: Script type
			return "SCRIPT";
		case UNDERLINE:		//byte: Underline type
			return "UNDERLINE";
		case BOLD:			//Bold: 0x0190 or 0x02bc
			return "BOLD";
		case READING_ORDER:	    //byte: ReadingOrder enumeration
			return "READING_ORDER";
		case TEXT_ROTATION:		//byte: 00 ~ 90 (countclockwise 0~90), 91 ~ 180 (clockwise 1 ~ 90), 0xff (vertical text)
			return "TEXT_ROTATION";
		case VERT_ALIGN:			//byte: VertAlign enumeration type
			return "VERT_ALIGN";
		case HORZ_ALIGN:			//byte: HorizAlign enumeration type
			return "HORZ_ALIGN";
		case FILL_PATTERN:      //byte: FillPattern enumeration type
			return "FILL_PATTERN";
		case USE_DIAG_UP_BORDER:   //byte: 0 or 1
			return "USE_DIAG_UP_BORDER";
		case USE_DIAG_DOWN_BORDER: //byte: 0 or 1
			return "USE_DIAG_DOWN_BORDER";
		case WRAPPED:			//byte: 0 or 1
			return "WRAPPED";
		case JUSTIFIED:       	//byte: 0 or 1
			return "JUSTIFIED";
		case SHRINK_FIT:		//byte: 0 or 1
			return "SHRINK_FIT";
		case MERGED:			//byte: 0 or 1
			return "MERGED";
		case ITALIC:			//byte: 0 or 1
			return "ITALIC";
		case STRIKETHRU:		//byte: 0 or 1
			return "STRIKETHRU";
		case OUTLINE:			//byte: 0 or 1
			return "OUTLINE";
		case SHADOW:			//byte: 0 or 1
			return "SHADOW";
		case CONDENSED:        	//byte: 0 or 1
			return "CONDENSED";
		case EXTENDED:        	//byte: 0 or 1
			return "EXTENDED";
		case CHARSET: 			//byte
			return "CHARSET";
		case FONT_FAMILY:		//byte: <= 5
			return "FONT_FAMILY";
		case LOCKED:			//byte: 0 or 1
			return "LOCKED";
		case HIDDEN:			//byte: 0 or 1
			return "HIDDEN";
		case FORMAT_ID:		//short: identifier of a number format
			return "FORMAT_ID";
		case ABS_INDENTION:	//unsigned short, <= 15
			return "ABS_INDENTION";
		case REL_INDENTION:	//short; added to previous indention: >= -15; <= 15, 255: N/A
			return "REL_INDENTION";
		case FONT_NAME:		//LPWideString
			return "FONT_NAME";
		case FONT_SIZE:		//int 20~8191 (twips)
			return "FONT_SIZE";
		case NUM_FORMAT:		//stFormat of FormatRecord
			return "NUM_FORMAT";
		default:
			throw new RuntimeException("Unknown xfPropType: "+ xfPropType + ", xfPropData:" + xfPropData);
		}
	}
}