/* FillImpl.java

	Purpose:
		
	Description:
		
	History:
		Mar 31, 2015 5:41:21 PM, Created by henrichen

	Copyright (C) 2015 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.zss.model.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.zkoss.lang.Objects;
import org.zkoss.zss.model.SColor;
import org.zkoss.zss.model.SFill;

/**
 * @author henri
 * @since 3.8.0
 */
public class FillImpl extends AbstractFillAdv {
	private static final long serialVersionUID = 1L;
	
	private SColor _fillColor = ColorImpl.BLACK; // ZSS-857: default fillColor is black
	private SColor _backColor = ColorImpl.WHITE;
	private FillPattern _fillPattern = FillPattern.NONE;
	private String _patternHtml = null; //clear cache

	public FillImpl(){}
	public FillImpl(FillPattern pattern, String fgColor, String bgColor) {
		this._fillPattern = pattern;
		this._fillColor = new ColorImpl(fgColor);
		this._backColor = new ColorImpl(bgColor);
	}
	@Override
	public void setFillColor(SColor fillColor) {
		_fillColor = fillColor;
		_patternHtml = null; //clear cache
}

	@Override
	public void setBackColor(SColor backColor) {
		_backColor = backColor;
		_patternHtml = null; //clear cache
	}

	@Override
	public void setFillPattern(FillPattern fillPattern) {
		_fillPattern = fillPattern;
		_patternHtml = null; //clear cache
	}

	@Override
	public SColor getFillColor() {
		return _fillColor;
	}

	@Override
	public SColor getBackColor() {
		return _backColor;
	}

	@Override
	public FillPattern getFillPattern() {
		return _fillPattern;
	}

	//--Object--//
	public int hashCode() {
		int hash = (_fillColor == null ? 0 : _fillColor.hashCode());
		hash = hash * 31 + (_backColor == null ? 0 : _backColor.hashCode());
		hash = hash * 31 + (_fillPattern == null ? 0 : _fillPattern.hashCode());
		return hash;
	}
	
	public boolean equals(Object other) {
		if (other == this) return true;
		if (!(other instanceof FillImpl)) return false;
		FillImpl o = (FillImpl) other;
		return Objects.equals(this._fillColor, o._fillColor)
				&& Objects.equals(this._backColor, o._backColor)
				&& Objects.equals(this._fillPattern, o._fillPattern);
	}

	public String getFillPatternHtml() {
		final FillPattern pattern = getFillPattern();
		if (pattern == FillPattern.NONE || pattern == FillPattern.SOLID) {
			return "";
		}
		if (_patternHtml == null) {
			_patternHtml = getFillPatternHtml(this);
		}
		return _patternHtml;
	}

	//ZSS-841
	private static String getFillPatternHtml(SFill style) {
		byte[] rawData = getFillPatternBytes(style, 0, 0, 8, 4);
		StringBuilder sb = new StringBuilder();
		sb.append("background-image:url(data:image/png;base64,");
		String base64 = Base64.encodeBase64String(rawData);
		sb.append(base64).append(");");
		return sb.toString();
	}
	
	//ZSS-841
	private static byte[][] _PATTERN_BYTES = {
		null, //NO_FILL
		
		// 00000000
		// 00000000
		// 00000000
		// 00000000
		new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00}, //SOLID, //SOLID_FOREGROUND
		
		// 01010101
		// 10101010
		// 01010101
		// 10101010
		new byte[] {(byte) 0x55, (byte) 0xaa, (byte) 0x55, (byte) 0xaa}, //MEDIUM_GRAY, //FINE_DOTS
		
		// 11101110
		// 10111011
		// 11101110
		// 10111011
		new byte[] {(byte) 0xee, (byte) 0xbb, (byte) 0xee, (byte) 0xbb}, //DARK_GRAY, //ALT_BARS
		
		// 10001000
		// 00100010
		// 10001000
		// 00100010
		new byte[] {(byte) 0x88, (byte) 0x22, (byte) 0x88, (byte) 0x22}, //LIGHT_GRAY, //SPARSE_DOTS
		
		// 11111111
		// 00000000
		// 00000000
		// 11111111
		new byte[] {(byte) 0xff, (byte) 0x00, (byte) 0x00, (byte) 0xff}, //DARK_HORIZONTAL, //THICK_HORZ_BANDS
		
		// 00110011
		// 00110011
		// 00110011
		// 00110011
		new byte[] {(byte) 0x33, (byte) 0x33, (byte) 0x33, (byte) 0x33}, //DARK_VERTICAL, //THICK_VERT_BANDS
		
		// 10011001
		// 11001100
		// 01100110
		// 00110011
		new byte[] {(byte) 0x99, (byte) 0xcc, (byte) 0x66, (byte) 0x33}, //DARK_DOWN, //THICK_BACKWARD_DIAG
		
		// 10011001
		// 00110011
		// 01100110
		// 11001100
		new byte[] {(byte) 0x99, (byte) 0x33, (byte) 0x66, (byte) 0xcc}, //DARK_UP, //THICK_FORWARD_DIAG
		
		// 10011001
		// 10011001
		// 01100110
		// 01100110
		new byte[] {(byte) 0x99, (byte) 0x99, (byte) 0x66, (byte) 0x66}, //DARK_GRID, //BIG_SPOTS
		
		// 10011001
		// 11111111
		// 01100110
		// 11111111
		new byte[] {(byte) 0x99, (byte) 0xff, (byte) 0x66, (byte) 0xff}, //DARK_TRELLIS, //BRICKS
		
		// 00000000
		// 00000000
		// 00000000
		// 11111111
		new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff}, //LIGHT_HORIZONTAL, //THIN_HORZ_BANDS
		
		// 00100010
		// 00100010
		// 00100010
		// 00100010
		new byte[] {(byte) 0x22, (byte) 0x22, (byte) 0x22, (byte) 0x22}, //LIGHT_VERTICAL, //THIN_VERT_BANDS
		
		// 00010001
		// 10001000
		// 01000100
		// 00100010
		new byte[] {(byte) 0x11, (byte) 0x88, (byte) 0x44, (byte) 0x22}, //LIGHT_DOWN, //THIN_BACKWARD_DIAG
		
		// 10001000
		// 00010001
		// 00100010
		// 01000100
		new byte[] {(byte) 0x88, (byte) 0x11, (byte) 0x22, (byte) 0x44}, //LIGHT_UP, //THIN_FORWARD_DIAG
		
		// 00100010
		// 00100010
		// 00100010
		// 11111111 
		new byte[] {(byte) 0x22, (byte) 0x22, (byte) 0x22, (byte) 0xff}, //LIGHT_GRID, //SQUARES
		
		// 01010101
		// 10001000
		// 01010101
		// 00100010
		new byte[] {(byte) 0x55, (byte) 0x88, (byte) 0x55, (byte) 0x22}, //LIGHT_TRELLIS, //DIAMONDS
		
		// 00000000
		// 10001000
		// 00000000
		// 00100010
		new byte[] {(byte) 0x00, (byte) 0x88, (byte) 0x00, (byte) 0x22}, //GRAY125, //LESS_DOTS 
		
		// 00000000
		// 00100000
		// 00000000
		// 00000010
		new byte[] {(byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x02}, //GRAY0625 //LEAST_DOTS
	};

	@Override
	String getStyleKey() {
		return new StringBuilder()
			.append(_fillPattern.ordinal()) 
			.append(".").append(_fillColor.getHtmlColor())
			.append(".").append(_backColor.getHtmlColor()).toString();
	}

	//ZSS-974
	//@since 3.8.0
	public static byte[] getFillPatternBytes(SFill style, int xOffset, int yOffset, int width, int height) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		// background color
		byte[] rgb = style.getBackColor().getRGB();
		g2.setColor(new Color(((int)rgb[0]) & 0xff , ((int)rgb[1]) & 0xff, ((int)rgb[2]) & 0xff));
		g2.fillRect(0, 0, width, height);
		// foreground color
		rgb = style.getFillColor().getRGB();
		g2.setColor(new Color(((int)rgb[0]) & 0xff , ((int)rgb[1]) & 0xff, ((int)rgb[2]) & 0xff));
		byte[] patb = _PATTERN_BYTES[style.getFillPattern().ordinal()];
		for (int y = 0; y < height; ++y) {
			final int y0 = (y + yOffset) % 4;
			byte b = patb[y0];
			if (b == 0) continue; // all zero case
			if (b == 0xff) {
				g2.drawLine(0, y, width-1, y);
				continue;
			}
			int mask = 0x80 >>> (xOffset % 8);
			for (int x = 0; x < width; ++x) {
				if ((b & mask) != 0) {
					g2.drawLine(x, y, x, y);
				}
				mask >>>= 1;
				if (mask == 0) mask = 0x80;
			}
		}
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", os);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return os.toByteArray();
	}
}
