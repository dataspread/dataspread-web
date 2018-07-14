/*
{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Aug 14, 2014, Created by henri
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/

package org.zkoss.poi.ss.usermodel;

import org.zkoss.poi.hssf.record.FontRecord;
import org.zkoss.poi.hssf.record.StyleExtRecord;
import org.zkoss.poi.hssf.record.StyleRecord;
import org.zkoss.poi.hssf.record.common.BuiltInStyle;
import org.zkoss.poi.hssf.record.common.XFProp;
import org.zkoss.poi.hssf.record.common.XFPropBorder;
import org.zkoss.poi.hssf.record.common.XFPropColor;
import org.zkoss.poi.hssf.record.common.XLUnicodeString;

/**
 * @author henri
 * @since 3.9.5
 */
public class BuiltinStyles {
	private static final Tuple[] _tuples = new Tuple[47];
	
	//default 0 ~ 3, 4 not available, 5 (1-based) ...
	static {
		int j = 0;
		//0x10
		{
			//style
			StyleRecord sr = new StyleRecord(0x10, new XLUnicodeString("20% - Accent1", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x4, 0x6665, 0xFFF1E5DB)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x01, 0x0, 0xFF000000)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x1E, 0xFF), "20% - Accent1", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x11
		{
			//style
			StyleRecord sr = new StyleRecord(0x11, new XLUnicodeString("20% - Accent2", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x5, 0x6665, 0xFFDCDDF2)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x01, 0x0, 0xFF000000)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x22, 0xFF), "20% - Accent2", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x12
		{
			//style
			StyleRecord sr = new StyleRecord(0x12, new XLUnicodeString("20% - Accent3", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x6, 0x6665, 0xFFDDF1EA)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x01, 0x0, 0xFF000000)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x26, 0xFF), "20% - Accent3", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x13
		{
			//style
			StyleRecord sr = new StyleRecord(0x13, new XLUnicodeString("20% - Accent4", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x7, 0x6665, 0xFFECE0E5)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x01, 0x0, 0xFF000000)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x2A, 0xFF), "20% - Accent4", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}
		
		//0x14
		{
			//style
			StyleRecord sr = new StyleRecord(0x14, new XLUnicodeString("20% - Accent5", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x8, 0x6665, 0xFFF3EEDB)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x01, 0x0, 0xFF000000)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x2E, 0xFF), "20% - Accent5", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}
		
		//0x15
		{
			//style
			StyleRecord sr = new StyleRecord(0x15, new XLUnicodeString("20% - Accent6", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x9, 0x6665, 0xFFD9E9FD)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x01, 0x0, 0xFF000000)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x32, 0xFF), "20% - Accent6", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x16
		{
			//style
			StyleRecord sr = new StyleRecord(0x16, new XLUnicodeString("40% - Accent1", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x4, 0x4CCC, 0xFFE4CCB8)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x01, 0x0, 0xFF000000)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x1F, 0xFF), "40% - Accent1", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x17
		{
			//style
			StyleRecord sr = new StyleRecord(0x17, new XLUnicodeString("40% - Accent2", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x5, 0x4CCC, 0xFFB8B9E6)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x01, 0x0, 0xFF000000)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x23, 0xFF), "40% - Accent2", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x18
		{
			//style
			StyleRecord sr = new StyleRecord(0x18, new XLUnicodeString("40% - Accent3", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x6, 0x4CCC, 0xFFBCE4D7)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x01, 0x0, 0xFF000000)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x27, 0xFF), "40% - Accent3", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}
		
		//0x19
		{
			//style
			StyleRecord sr = new StyleRecord(0x19, new XLUnicodeString("40% - Accent4", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x7, 0x4CCC, 0xFFDAC0CC)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x01, 0x0, 0xFF000000)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x2B, 0xFF), "40% - Accent4", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x1A
		{
			//style
			StyleRecord sr = new StyleRecord(0x1A, new XLUnicodeString("40% - Accent5", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x8, 0x4CCC, 0xFFE8DDB6)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x01, 0x0, 0xFF000000)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x2F, 0xFF), "40% - Accent5", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x1B
		{
			//style
			StyleRecord sr = new StyleRecord(0x1B, new XLUnicodeString("40% - Accent6", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x9, 0x4CCC, 0xFFB4D5FC)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x01, 0x0, 0xFF000000)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x33, 0xFF), "40% - Accent6", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x1C
		{
			//style
			StyleRecord sr = new StyleRecord(0x1C, new XLUnicodeString("60% - Accent1", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x4, 0x3332, 0xFFD7B395)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x00, 0x0, 0xFFFFFFFF)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x20, 0xFF), "60% - Accent1", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x1D
		{
			//style
			StyleRecord sr = new StyleRecord(0x1D, new XLUnicodeString("60% - Accent2", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x5, 0x3332, 0xFF9597D9)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x00, 0x0, 0xFFFFFFFF)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x24, 0xFF), "60% - Accent2", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}
		
		//0x1E
		{
			//style
			StyleRecord sr = new StyleRecord(0x1E, new XLUnicodeString("60% - Accent3", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x6, 0x3332, 0xFF9AD6C2)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x00, 0x0, 0xFFFFFFFF)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x28, 0xFF), "60% - Accent3", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x1F
		{
			//style
			StyleRecord sr = new StyleRecord(0x1F, new XLUnicodeString("60% - Accent4", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x7, 0x3332, 0xFFC7A1B2)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x00, 0x0, 0xFFFFFFFF)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x2C, 0xFF), "60% - Accent4", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x20
		{
			//style
			StyleRecord sr = new StyleRecord(0x20, new XLUnicodeString("60% - Accent5", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x8, 0x3332, 0xFFDDCD93)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x00, 0x0, 0xFFFFFFFF)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x30, 0xFF), "60% - Accent5", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x21
		{
			//style
			StyleRecord sr = new StyleRecord(0x21, new XLUnicodeString("60% - Accent6", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x9, 0x3332, 0xFF90C0FA)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x00, 0x0, 0xFFFFFFFF)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x34, 0xFF), "60% - Accent6", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x22
		{
			//style
			StyleRecord sr = new StyleRecord(0x22, new XLUnicodeString("Accent1", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x4, 0x0, 0xFFBD814F)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x00, 0x0, 0xFFFFFFFF)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x1D, 0xFF), "Accent1", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x23
		{
			//style
			StyleRecord sr = new StyleRecord(0x23, new XLUnicodeString("Accent2", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x5, 0x0, 0xFF4D50C0)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x00, 0x0, 0xFFFFFFFF)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x21, 0xFF), "Accent2", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x24
		{
			//style
			StyleRecord sr = new StyleRecord(0x24, new XLUnicodeString("Accent3", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x6, 0x0, 0xFF59BB9B)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x00, 0x0, 0xFFFFFFFF)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x25, 0xFF), "Accent3", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x25
		{
			//style
			StyleRecord sr = new StyleRecord(0x25, new XLUnicodeString("Accent4", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x7, 0x0, 0xFFA26480)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x00, 0x0, 0xFFFFFFFF)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x29, 0xFF), "Accent4", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x26
		{
			//style
			StyleRecord sr = new StyleRecord(0x26, new XLUnicodeString("Accent5", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x8, 0x0, 0xFFC6AC4B)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x00, 0x0, 0xFFFFFFFF)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x2D, 0xFF), "Accent5", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x27
		{
			//style
			StyleRecord sr = new StyleRecord(0x27, new XLUnicodeString("Accent6", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x7, 0x9, 0x0, 0xFF4696F7)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x00, 0x0, 0xFFFFFFFF)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 4, new BuiltInStyle(0x31, 0xFF), "Accent6", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x28
		{
			//style
			StyleRecord sr = new StyleRecord(0x28, new XLUnicodeString("Bad", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x5, 0xFF, 0x0, 0xFFCEC7FF)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x5, 0xFF, 0x0, 0xFF06009C)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2))
			};
			StyleExtRecord ser = new StyleExtRecord(1, 1, new BuiltInStyle(0x1B, 0xFF), "Bad", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x29
		{
			//style
			StyleRecord sr = new StyleRecord(0x29, new XLUnicodeString("Calculation", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x5, 0xFF, 0x0, 0xFFF2F2F2)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x5, 0xFF, 0x0, 0xFF007DFA)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2)),
				new XFProp(XFProp.TOP_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFF7F7F7F), 0x1)),
				new XFProp(XFProp.BOTTOM_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFF7F7F7F), 0x1)),
				new XFProp(XFProp.LEFT_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFF7F7F7F), 0x1)),
				new XFProp(XFProp.RIGHT_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFF7F7F7F), 0x1)),
			};
			StyleExtRecord ser = new StyleExtRecord(1, 2, new BuiltInStyle(0x16, 0xFF), "Calculation", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x2A
		{
			//style
			StyleRecord sr = new StyleRecord(0x2A, new XLUnicodeString("Check Cell", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x5, 0xFF, 0x0, 0xFFA5A5A5)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x0, 0x0, 0xFFFFFFFF)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2)),
				new XFProp(XFProp.TOP_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFF3F3F3F), 0x6)),
				new XFProp(XFProp.BOTTOM_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFF3F3F3F), 0x6)),
				new XFProp(XFProp.LEFT_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFF3F3F3F), 0x6)),
				new XFProp(XFProp.RIGHT_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFF3F3F3F), 0x6)),
			};
			StyleExtRecord ser = new StyleExtRecord(1, 2, new BuiltInStyle(0x17, 0xFF), "Check Cell", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x2B
		{
			//style
			StyleRecord sr = new StyleRecord(0x2B, new BuiltInStyle(0x03, 0xFF));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
			};
			StyleExtRecord ser = new StyleExtRecord(1, 5, new BuiltInStyle(0x03, 0xFF), "Comma", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x2C
		{
			//style
			StyleRecord sr = new StyleRecord(0x2C, new BuiltInStyle(0x06, 0xFF));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
			};
			StyleExtRecord ser = new StyleExtRecord(1, 5, new BuiltInStyle(0x06, 0xFF), "Comma [0]", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x2D
		{
			//style
			StyleRecord sr = new StyleRecord(0x2D, new BuiltInStyle(0x04, 0xFF));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
			};
			StyleExtRecord ser = new StyleExtRecord(1, 5, new BuiltInStyle(0x04, 0xFF), "Currency", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x2E
		{
			//style
			StyleRecord sr = new StyleRecord(0x2E, new BuiltInStyle(0x07, 0xFF));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
			};
			StyleExtRecord ser = new StyleExtRecord(1, 5, new BuiltInStyle(0x07, 0xFF), "Currency [0]", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}
		
		//0x2F
		{
			//style
			StyleRecord sr = new StyleRecord(0x2F, new XLUnicodeString("Explanatory Text", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
					new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x5, 0xFF, 0x0, 0xFF7F7F7F)),
					new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2)),
				};
			StyleExtRecord ser = new StyleExtRecord(1, 2, new BuiltInStyle(0x35, 0xFF), "Explanatory Text", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x30
		{
			//style
			StyleRecord sr = new StyleRecord(0x30, new XLUnicodeString("Good", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x5, 0xFF, 0x0, 0xFFCEEFC6)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x5, 0xFF, 0x0, 0xFF006100)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2)),
			};
			StyleExtRecord ser = new StyleExtRecord(1, 1, new BuiltInStyle(0x1A, 0xFF), "Good", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x31
		{
			//style
			StyleRecord sr = new StyleRecord(0x31, new XLUnicodeString("Heading 1", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x03, 0x0, 0xFF7D491F)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2)),
				new XFProp(XFProp.BOTTOM_BORDER, new XFPropBorder(new XFPropColor(0x07, 0x04, 0x0, 0xFFBD814F), 0x5)),
			};
			StyleExtRecord ser = new StyleExtRecord(1, 3, new BuiltInStyle(0x10, 0xFF), "Heading 1", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x32
		{
			//style
			StyleRecord sr = new StyleRecord(0x32, new XLUnicodeString("Heading 2", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x03, 0x0, 0xFF7D491F)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2)),
				new XFProp(XFProp.BOTTOM_BORDER, new XFPropBorder(new XFPropColor(0x07, 0x04, 0x3FFF, 0xFFDEC0A8), 0x5)),
			};
			StyleExtRecord ser = new StyleExtRecord(1, 3, new BuiltInStyle(0x11, 0xFF), "Heading 2", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x33
		{
			//style
			StyleRecord sr = new StyleRecord(0x33, new XLUnicodeString("Heading 3", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x03, 0x0, 0xFF7D491F)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2)),
				new XFProp(XFProp.BOTTOM_BORDER, new XFPropBorder(new XFPropColor(0x07, 0x04, 0x3332, 0xFFD7B395), 0x2)),
			};
			StyleExtRecord ser = new StyleExtRecord(1, 3, new BuiltInStyle(0x12, 0xFF), "Heading 3", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x34
		{
			//style
			StyleRecord sr = new StyleRecord(0x34, new XLUnicodeString("Heading 4", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x03, 0x0, 0xFF7D491F)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2)),
			};
			StyleExtRecord ser = new StyleExtRecord(1, 3, new BuiltInStyle(0x13, 0xFF), "Heading 4", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x35
		{
			//style
			StyleRecord sr = new StyleRecord(0x35, new XLUnicodeString("Input", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x5, 0xFF, 0x0, 0xFF99CCFF)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x5, 0xFF, 0x0, 0xFF763F3F)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2)),
				new XFProp(XFProp.TOP_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFF7F7F7F), 0x1)),
				new XFProp(XFProp.BOTTOM_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFF7F7F7F), 0x1)),
				new XFProp(XFProp.LEFT_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFF7F7F7F), 0x1)),
				new XFProp(XFProp.RIGHT_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFF7F7F7F), 0x1)),
			};
			StyleExtRecord ser = new StyleExtRecord(1, 2, new BuiltInStyle(0x14, 0xFF), "Input", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x36
		{
			//style
			StyleRecord sr = new StyleRecord(0x36, new XLUnicodeString("Linked Cell", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x5, 0xFF, 0x0, 0xFF007DFA)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2)),
				new XFProp(XFProp.BOTTOM_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFF0180FF), 0x6)),
			};
			StyleExtRecord ser = new StyleExtRecord(1, 2, new BuiltInStyle(0x18, 0xFF), "Linked Cell", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}
		
		//0x37
		{
			//style
			StyleRecord sr = new StyleRecord(0x37, new XLUnicodeString("Neutral", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x5, 0xFF, 0x0, 0xFF9CEBFF)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x5, 0xFF, 0x0, 0xFF00659C)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2)),
			};
			StyleExtRecord ser = new StyleExtRecord(1, 1, new BuiltInStyle(0x1C, 0xFF), "Neutral", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x00
		{
			//style
			StyleRecord sr = new StyleRecord(0x00, new BuiltInStyle(0x00, 0xFF));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x1, 0x0, 0xFF000000)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2)),
			};
			StyleExtRecord ser = new StyleExtRecord(1, 1, new BuiltInStyle(0x00, 0xFF), "Normal", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}
		
		//0x38
		{
			//style
			StyleRecord sr = new StyleRecord(0x38, new XLUnicodeString("Note", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x5, 0xFF, 0x0, 0xFFCCFFFF)),
				new XFProp(XFProp.TOP_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFFB2B2B2), 0x1)),
				new XFProp(XFProp.BOTTOM_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFFB2B2B2), 0x1)),
				new XFProp(XFProp.LEFT_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFFB2B2B2), 0x1)),
				new XFProp(XFProp.RIGHT_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFFB2B2B2), 0x1)),
			};
			StyleExtRecord ser = new StyleExtRecord(1, 2, new BuiltInStyle(0x0A, 0xFF), "Note", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}
		
		//0x39
		{
			//style
			StyleRecord sr = new StyleRecord(0x39, new XLUnicodeString("Output", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.FOREGROUND_COLOR, new XFPropColor(0x5, 0xFF, 0x0, 0xFFF2F2F2)),
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x5, 0xFF, 0x0, 0xFF3F3F3F)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2)),
				new XFProp(XFProp.TOP_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFF3F3F3F), 0x1)),
				new XFProp(XFProp.BOTTOM_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFF3F3F3F), 0x1)),
				new XFProp(XFProp.LEFT_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFF3F3F3F), 0x1)),
				new XFProp(XFProp.RIGHT_BORDER, new XFPropBorder(new XFPropColor(0x05, 0xFF, 0x0, 0xFF3F3F3F), 0x1)),
			};
			StyleExtRecord ser = new StyleExtRecord(1, 2, new BuiltInStyle(0x15, 0xFF), "Output", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x3A
		{
			//style
			StyleRecord sr = new StyleRecord(0x3A, new BuiltInStyle(0x05, 0xFF));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
			};
			StyleExtRecord ser = new StyleExtRecord(1, 5, new BuiltInStyle(0x05, 0xFF), "Percent", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x3B
		{
			//style
			StyleRecord sr = new StyleRecord(0x3B, new XLUnicodeString("Title", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x03, 0x0, 0xFF7D491F)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x1)),
			};
			StyleExtRecord ser = new StyleExtRecord(1, 3, new BuiltInStyle(0x0F, 0xFF), "Title", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x3C
		{
			//style
			StyleRecord sr = new StyleRecord(0x3C, new XLUnicodeString("Total", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x7, 0x01, 0x0, 0xFF000000)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2)),
				new XFProp(XFProp.TOP_BORDER, new XFPropBorder(new XFPropColor(0x07, 0x04, 0x0, 0xFFBD814F), 0x1)),
				new XFProp(XFProp.BOTTOM_BORDER, new XFPropBorder(new XFPropColor(0x07, 0x04, 0x0, 0xFFBD814F), 0x6)),
			};
			StyleExtRecord ser = new StyleExtRecord(1, 3, new BuiltInStyle(0x19, 0xFF), "Total", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}

		//0x3D
		{
			//style
			StyleRecord sr = new StyleRecord(0x3D, new XLUnicodeString("Warning Text", false));
			
			//styleext
			XFProp[] xfProps = new XFProp[] {
				new XFProp(XFProp.TEXT_COLOR, new XFPropColor(0x5, 0xFF, 0x0, 0xFF0000FF)),
				new XFProp(XFProp.FONT_SCHEME, Byte.valueOf((byte)0x2)),
			};
			StyleExtRecord ser = new StyleExtRecord(1, 2, new BuiltInStyle(0x0B, 0xFF), "Warning Text", xfProps);
			_tuples[j++] = new Tuple(sr, ser);
		}
		
//		System.out.println("J:"+j); // j should be 47
	}
	
	public static int getCounts() {
		return _tuples.length;
	}
	
	public static StyleRecord getBuiltinStyle(int index) {
		return _tuples[index]._style;
	}
	
	public static StyleExtRecord getBuiltinStyleExt(int index) {
		return _tuples[index]._styleExt;
	}

	private static class Tuple {
		final StyleRecord _style;
		final StyleExtRecord _styleExt;
		
		private Tuple(StyleRecord style, StyleExtRecord styleExt) {
			_style = style;
			_styleExt = styleExt;
		}
	}
}
