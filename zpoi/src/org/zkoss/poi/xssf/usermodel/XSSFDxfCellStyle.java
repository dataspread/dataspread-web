/* XSSFDxfCellStyle.java

	Purpose:
		
	Description:
		
	History:
		Dec 11, 2014 12:42:46 PM, Created by henrichen

	Copyright (C) 2014 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.poi.xssf.usermodel;

import java.util.List;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorderPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDxf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTNumFmt;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPatternFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STBorderStyle;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STPatternType;
import org.zkoss.poi.ss.usermodel.BuiltinFormats;
import org.zkoss.poi.ss.usermodel.CellStyle;
import org.zkoss.poi.ss.usermodel.Color;
import org.zkoss.poi.ss.usermodel.DxfCellStyle;
import org.zkoss.poi.ss.usermodel.Font;
import org.zkoss.poi.ss.usermodel.HorizontalAlignment;
import org.zkoss.poi.ss.usermodel.IndexedColors;
import org.zkoss.poi.ss.usermodel.VerticalAlignment;
import org.zkoss.poi.xssf.model.StylesTable;
import org.zkoss.poi.xssf.model.ThemesTable;
import org.zkoss.poi.xssf.usermodel.extensions.XSSFCellAlignment;
import org.zkoss.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.zkoss.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.zkoss.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;

/**
 * Cell style for Dxfs
 * @author henri
 * @since 3.7.0
 */
public class XSSFDxfCellStyle implements DxfCellStyle {
	private int _dxfId;
    private StylesTable _stylesSource;
	private CTDxf _ctDxf;
    private XSSFFont _font;
    private XSSFCellAlignment _cellAlignment;
    private ThemesTable _theme;

    public XSSFDxfCellStyle(int dxfId, StylesTable stylesSource, ThemesTable theme) {
        _dxfId = dxfId;
        _stylesSource = stylesSource;
        _ctDxf = stylesSource.getDxfAt(this._dxfId);
        _theme = theme;
        
        if (_ctDxf.isSetFont()) {
        	_font = new XSSFFont(_ctDxf.getFont());
        }
        if (_ctDxf.isSetAlignment()) {
        	_cellAlignment = new XSSFCellAlignment(_ctDxf.getAlignment());
        }
    }
	
    /**
     * Creates an empty Cell Style
     */
    public XSSFDxfCellStyle(StylesTable stylesSource) {
        _stylesSource = stylesSource;
        _ctDxf = CTDxf.Factory.newInstance();
    }

    public CTDxf getDxf() {
    	return _ctDxf;
    }
    
	@Override
	public short getIndex() {
		return (short) _dxfId;
	}

	@Override
	public void setDataFormat(short fmt) {
		if (_ctDxf.isSetNumFmt()) {
			_ctDxf.unsetNumFmt();
		}
    	if (fmt >= 0) {
    		CTNumFmt numFmt = _ctDxf.addNewNumFmt();
	        numFmt.setNumFmtId(fmt);
	        numFmt.setFormatCode(new XSSFDataFormat(_stylesSource).getRawFormat(fmt));
    	}
	}

	@Override
	public short getDataFormat() {
        if(_ctDxf.isSetNumFmt()) {
            return (short)_ctDxf.getNumFmt().getNumFmtId();
        }
        return 0;
	}

	@Override
	public String getDataFormatString() {
        int idx = getDataFormat();
        return new XSSFDataFormat(_stylesSource).getFormat((short)idx);
	}

	@Override
	public String getRawDataFormatString() {
		String val = null;
        if(_ctDxf.isSetNumFmt()) {
            val = _ctDxf.getNumFmt().getFormatCode();
        }
        return val != null ? 
        	val : new XSSFDataFormat(_stylesSource).getRawFormat(getDataFormat());
	}

	@Override
	public boolean isBuiltinDataFormat() {
    	int idx = getDataFormat();
    	return _stylesSource.getNumberFormatAt(idx) == null 
    			&& BuiltinFormats.getBuiltinFormat(idx) != null;
	}

	@Override
	public void setFont(Font font) {
		if (_ctDxf.isSetFont()) {
			this._ctDxf.unsetFont();
		}
        if(font != null){
        	XSSFFont font0 = (XSSFFont) font;
            this._ctDxf.setFont((CTFont)font0.getCTFont().copy());
        }
	}
	
    /**
    * Gets the font for this style
    * @return Font - font
    */
    public XSSFFont getFont() {
        if (_font == null) {
        	CTFont font = _ctDxf.addNewFont();
            _font = new XSSFFont(font);
        }
        return _font;
    }


	@Override
	public short getFontIndex() {
		return -1;
	}

	@Override
	public void setHidden(boolean hidden) {
        if (!_ctDxf.isSetProtection()) {
            _ctDxf.addNewProtection();
        }
        _ctDxf.getProtection().setHidden(hidden);
	}

	@Override
	public boolean getHidden() {
    	if (_ctDxf.isSetProtection() && _ctDxf.getProtection().isSetHidden()) {
	        return _ctDxf.getProtection().getHidden();
    	}
        return false;
	}

	@Override
	public void setLocked(boolean locked) {
        if (!_ctDxf.isSetProtection()) {
            _ctDxf.addNewProtection();
        }
       _ctDxf.getProtection().setLocked(locked);
	}

	@Override
	public boolean getLocked() {
        if (_ctDxf.isSetProtection() && _ctDxf.getProtection().isSetLocked()) {
            return _ctDxf.getProtection().getLocked();
        }
        return true;
	}

	@Override
	public void setAlignment(short align) {
        getCellAlignment().setHorizontal(HorizontalAlignment.values()[align]);
	}

	@Override
	public short getAlignment() {
        return (short)(getAlignmentEnum().ordinal());
	}

	@Override
	public void setWrapText(boolean wrapped) {
        getCellAlignment().setWrapText(wrapped);
	}

	@Override
	public boolean getWrapText() {
    	if (_ctDxf.isSetAlignment()) {
	        CTCellAlignment align = _ctDxf.getAlignment();
	        return align != null && align.getWrapText();
    	}
	    return false;
	}

	@Override
	public void setVerticalAlignment(short align) {
        getCellAlignment().setVertical(VerticalAlignment.values()[align]);
	}

	@Override
	public short getVerticalAlignment() {
        return (short) (getVerticalAlignmentEnum().ordinal());
	}

	@Override
	public void setRotation(short rotation) {
        getCellAlignment().setTextRotation(rotation);
	}

	@Override
	public short getRotation() {
    	if (_ctDxf.isSetAlignment()) {
	        CTCellAlignment align = _ctDxf.getAlignment();
	        return (short)(align == null ? 0 : align.getTextRotation());
    	}
        return (short) 0;
	}

	@Override
	public void setIndention(short indent) {
        getCellAlignment().setIndent(indent);
	}

	@Override
	public short getIndention() {
    	if (_ctDxf.isSetAlignment()) {
	        CTCellAlignment align = _ctDxf.getAlignment();
	        return (short)(align == null ? 0 : align.getIndent());
    	}
        return (short) 0;
	}

	@Override
	public void setBorderLeft(short border) {
		if(border != BORDER_NONE && !_ctDxf.isSetBorder()) {
			_ctDxf.addNewBorder();
		}
		if (_ctDxf.isSetBorder()) {
			CTBorder ct = _ctDxf.getBorder();
			if (border == BORDER_NONE) {
				if (ct.isSetLeft()) ct.unsetLeft();
			} else {
				CTBorderPr pr = ct.isSetLeft() ? ct.getLeft() : ct.addNewLeft();
				pr.setStyle(STBorderStyle.Enum.forInt(border + 1));
			}
		}
	}

	@Override
	public short getBorderLeft() {
        if(_ctDxf.isSetBorder()) {
            CTBorder ct = _ctDxf.getBorder();
            STBorderStyle.Enum ptrn = ct.isSetLeft() ? ct.getLeft().getStyle() : null;
            return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
        }
    	return BORDER_NONE;
	}

	@Override
	public void setBorderRight(short border) {
		if(border != BORDER_NONE && !_ctDxf.isSetBorder()) {
			_ctDxf.addNewBorder();
		}
		if (_ctDxf.isSetBorder()) {
			CTBorder ct = _ctDxf.getBorder();
			if (border == BORDER_NONE) {
				if (ct.isSetRight()) ct.unsetRight();
			} else {
				CTBorderPr pr = ct.isSetRight() ? ct.getRight() : ct.addNewRight();
				pr.setStyle(STBorderStyle.Enum.forInt(border + 1));
			}
		}
	}

	@Override
	public short getBorderRight() {
        if(_ctDxf.isSetBorder()) {
            CTBorder ct = _ctDxf.getBorder();
            STBorderStyle.Enum ptrn = ct.isSetRight() ? ct.getRight().getStyle() : null;
            return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
        }
    	return BORDER_NONE;
	}

	@Override
	public void setBorderTop(short border) {
		if(border != BORDER_NONE && !_ctDxf.isSetBorder()) {
			_ctDxf.addNewBorder();
		}
		if (_ctDxf.isSetBorder()) {
			CTBorder ct = _ctDxf.getBorder();
			if (border == BORDER_NONE) {
				if (ct.isSetTop()) ct.unsetTop();
			} else {
				CTBorderPr pr = ct.isSetTop() ? ct.getTop() : ct.addNewTop();
				pr.setStyle(STBorderStyle.Enum.forInt(border + 1));
			}
		}
	}

	@Override
	public short getBorderTop() {
        if(_ctDxf.isSetBorder()) {
            CTBorder ct = _ctDxf.getBorder();
            STBorderStyle.Enum ptrn = ct.isSetTop() ? ct.getTop().getStyle() : null;
            return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
        }
    	return BORDER_NONE;
	}

	@Override
	public void setBorderBottom(short border) {
		if(border != BORDER_NONE && !_ctDxf.isSetBorder()) {
			_ctDxf.addNewBorder();
		}
		if (_ctDxf.isSetBorder()) {
			CTBorder ct = _ctDxf.getBorder();
			if (border == BORDER_NONE) {
				if (ct.isSetBottom()) ct.unsetBottom();
			} else {
				CTBorderPr pr = ct.isSetBottom() ? ct.getBottom() : ct.addNewBottom();
				pr.setStyle(STBorderStyle.Enum.forInt(border + 1));
			}
		}
	}

	@Override
	public short getBorderBottom() {
        if(_ctDxf.isSetBorder()) {
            CTBorder ct = _ctDxf.getBorder();
            STBorderStyle.Enum ptrn = ct.isSetBottom() ? ct.getBottom().getStyle() : null;
            return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
        }
    	return BORDER_NONE;
	}

	@Override
	public void setLeftBorderColor(short color) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(color);
        setLeftBorderColor(clr);
	}

	@Override
	public short getLeftBorderColor() {
        XSSFColor clr = getLeftBorderXSSFColor();
        return clr == null ? IndexedColors.BLACK.getIndex() : clr.getIndexed();
	}

	@Override
	public void setRightBorderColor(short color) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(color);
        setRightBorderColor(clr);
	}

	@Override
	public short getRightBorderColor() {
        XSSFColor clr = getRightBorderXSSFColor();
        return clr == null ? IndexedColors.BLACK.getIndex() : clr.getIndexed();
	}

	@Override
	public void setTopBorderColor(short color) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(color);
        setTopBorderColor(clr);
	}

	@Override
	public short getTopBorderColor() {
        XSSFColor clr = getTopBorderXSSFColor();
        return clr == null ? IndexedColors.BLACK.getIndex() : clr.getIndexed();
	}

	@Override
	public void setBottomBorderColor(short color) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(color);
        setBottomBorderColor(clr);
	}

	@Override
	public short getBottomBorderColor() {
        XSSFColor clr = getBottomBorderXSSFColor();
        return clr == null ? IndexedColors.BLACK.getIndex() : clr.getIndexed();
	}

	@Override
	public void setFillPattern(short fp) {
		if (!_ctDxf.isSetFill()) {
			if (fp == NO_FILL) return;
			_ctDxf.addNewFill();
		}
        CTFill ct = _ctDxf.getFill();
        CTPatternFill ptrn = ct.isSetPatternFill() ? ct.getPatternFill() : ct.addNewPatternFill();
        if(fp == NO_FILL && ptrn.isSetPatternType()) ptrn.unsetPatternType();
        else ptrn.setPatternType(STPatternType.Enum.forInt(fp + 1));
	}

	@Override
	public short getFillPattern() {
		if (_ctDxf.isSetFill()) {
			CTFill fill = _ctDxf.getFill();
			if (fill.isSetPatternFill()) {
				CTPatternFill pf = fill.getPatternFill();
				STPatternType.Enum ptrn = pf.getPatternType();
		        return ptrn == null ? CellStyle.NO_FILL : (short)(ptrn.intValue() - 1);
			}
		}
		return CellStyle.NO_FILL;
	}

	@Override
	public void setFillBackgroundColor(short bg) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(bg);
        setFillBackgroundColor(clr);
	}

	@Override
	public short getFillBackgroundColor() {
        XSSFColor clr = getFillBackgroundXSSFColor();
        return clr == null ? IndexedColors.AUTOMATIC.getIndex() : clr.getIndexed();
	}

	@Override
	public Color getFillBackgroundColorColor() {
	    return getFillBackgroundXSSFColor();
	}

	@Override
	public void setFillForegroundColor(short bg) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(bg);
        setFillForegroundColor(clr);
	}

	@Override
	public short getFillForegroundColor() {
        XSSFColor clr = getFillForegroundXSSFColor();
        return clr == null ? IndexedColors.AUTOMATIC.getIndex() : clr.getIndexed();
	}

	@Override
	public Color getFillForegroundColorColor() {
		return getFillForegroundXSSFColor();
	}

	@Override
	public void cloneStyleFrom(CellStyle source) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Color getTopBorderColorColor() {
        return getTopBorderXSSFColor();
	}

	@Override
	public Color getBottomBorderColorColor() {
        return getBottomBorderXSSFColor();
	}

	@Override
	public Color getRightBorderColorColor() {
        return getRightBorderXSSFColor();
	}

	@Override
	public Color getLeftBorderColorColor() {
        return getLeftBorderXSSFColor();
	}

	@Override
	public void setFontColorColor(Color fontColor) {
    	setFontColorColor((XSSFColor)fontColor);
	}
    private void setFontColorColor(XSSFColor color) {
    	getFont().setColor(color);
    }
    
    private void setBorderStyle(CTBorderPr bpr, short ordinal) {
    	if (bpr != null) {
    		if (ordinal != CellStyle.BORDER_NONE) {
    			bpr.setStyle(STBorderStyle.Enum.forInt(ordinal + 1));
    		} else {
    			bpr.unsetStyle();
    		}
    	}
    }
    
    private void setBorderColor(CTBorderPr bpr, XSSFColor color) {
    	if (bpr != null) {
            if (color == null) {
            	if (bpr.isSetColor()) {
            		bpr.unsetColor();
            	}
            }
            else bpr.setColor(color.getCTColor());
    	}
    }
	@Override
	public void setBorder(short left, Color leftColor, short top,
			Color topColor, short right, Color rightColor, short bottom,
			Color bottomColor) {
		if (!_ctDxf.isSetBorder()) {
			_ctDxf.addNewBorder();
		}
		CTBorder ct = _ctDxf.getBorder();
		
        // always generate <left/> ...
       	CTBorderPr lbpr = ct.isSetLeft() ? ct.getLeft() : 
       		left != CellStyle.BORDER_NONE ? ct.addNewLeft() : null;
      	setBorderStyle(lbpr, left);
      	
       	CTBorderPr tbpr = ct.isSetTop() ? ct.getTop() : 
       		top != CellStyle.BORDER_NONE ? ct.addNewTop() : null;
    	setBorderStyle(tbpr, top);
    	
       	CTBorderPr rbpr = ct.isSetRight() ? ct.getRight() : 
       		right != CellStyle.BORDER_NONE ? ct.addNewRight() : null;
    	setBorderStyle(rbpr, right);
    	
       	CTBorderPr bbpr = ct.isSetBottom() ? ct.getBottom() :
       		bottom != CellStyle.BORDER_NONE ? ct.addNewBottom() : null;
       	setBorderStyle(bbpr, bottom);
       	
        setBorderColor(lbpr, (XSSFColor)leftColor);
        setBorderColor(tbpr, (XSSFColor)topColor);
        setBorderColor(rbpr, (XSSFColor)rightColor);
        setBorderColor(bbpr, (XSSFColor)bottomColor);
	}

	@Override
	public void setFill(Color fillColor, Color backColor, short patternType) {
		if (!_ctDxf.isSetFill()) {
			_ctDxf.addNewFill();
		}
        CTFill ct = _ctDxf.getFill();
        XSSFCellFill fill = new XSSFCellFill(ct);
        XSSFColor fc = (XSSFColor) fillColor;
        XSSFColor bc = (XSSFColor) backColor;
        //ZSS-797
        String fHex = fc != null ? fc.getARGBHex() : null;
        String bHex = bc != null ? bc.getARGBHex() : null;
        boolean fcset = fHex != null && !"FFFFFFFF".equalsIgnoreCase(fHex);
        boolean bcset = bHex != null && !"FFFFFFFF".equalsIgnoreCase(bHex);
        if (bcset || fcset) {
            fill.setFillForegroundColor(fc == null ? 
            		new XSSFColor(new byte[] {(byte)0xff, (byte)0xff, (byte)0xff}) : fc);
        }
        if (bcset) {
        	fill.setFillBackgroundColor(bc == null ? 
            		new XSSFColor(new byte[] {(byte)0xff, (byte)0xff, (byte)0xff}) : bc);
        }
        fill.setPatternType(STPatternType.Enum.forInt(patternType + 1));
	}

	//ZSS-1020
	@Override
	@Deprecated
	public void setCellAlignment(short hAlign, short vAlign, boolean wrapText) {
		setCellAlignment(hAlign, vAlign, wrapText, (short)0);
	}
	
	@Override
	public void setCellAlignment(short hAlign, short vAlign, boolean wrapText, short rotation) {
    	short defaultHAlign = CellStyle.ALIGN_GENERAL;
    	short defaultVAlign = CellStyle.VERTICAL_BOTTOM;
    	boolean defaultWrapText = false;
    	short defaultRotation = 0; //ZSS-1020
    	
		if (defaultHAlign != hAlign && hAlign != CellStyle.ALIGN_GENERAL) {
			setAlignment(hAlign);
		}
		if (defaultVAlign != vAlign && vAlign != CellStyle.VERTICAL_BOTTOM) {
			setVerticalAlignment(vAlign);
		}
		if (defaultWrapText != wrapText && wrapText) {
			setWrapText(wrapText);
		}
		//ZSS-1020
		if (defaultRotation != rotation && rotation != 0) {
			setRotation(rotation);
		}
	}

	@Override
	public void setProtection(boolean locked, boolean hidden) {
    	boolean defaultLocked = true;
    	boolean defaultHidden = false;
		if (locked != defaultLocked && !locked) {
			setLocked(locked);
		}
		if (hidden != defaultHidden && !hidden) {
			setHidden(hidden);
		}
	}
	
    /**
     * get the cellAlignment object to use for manage alignment
     * @return XSSFCellAlignment - cell alignment
     */
    protected XSSFCellAlignment getCellAlignment() {
        if (this._cellAlignment == null) {
        	CTCellAlignment ctAlign = _ctDxf.addNewAlignment();
            this._cellAlignment = new XSSFCellAlignment(ctAlign);
        }
        return this._cellAlignment;
    }

    protected HorizontalAlignment getAlignmentEnum() {
    	if (_ctDxf.isSetAlignment()) {
	        CTCellAlignment align = _ctDxf.getAlignment();
	        if(align.isSetHorizontal()) {
	            return HorizontalAlignment.values()[align.getHorizontal().intValue()-1];
	        }
    	}
        return HorizontalAlignment.GENERAL;
    }

    public VerticalAlignment getVerticalAlignmentEnum() {
    	if (_ctDxf.isSetAlignment()) {
	        CTCellAlignment align = _ctDxf.getAlignment();
	        if(align.isSetVertical()) {
	            return VerticalAlignment.values()[align.getVertical().intValue()-1];
	        }
    	}
        return VerticalAlignment.BOTTOM;
    }

	@Override
	public void setBorderHorizontal(short border) {
		if(border != BORDER_NONE && !_ctDxf.isSetBorder()) {
			_ctDxf.addNewBorder();
		}
		if (_ctDxf.isSetBorder()) {
			CTBorder ct = _ctDxf.getBorder();
			if (border == BORDER_NONE) {
				if (ct.isSetHorizontal()) ct.unsetHorizontal();
			} else {
				CTBorderPr pr = ct.isSetHorizontal() ? ct.getHorizontal() : ct.addNewHorizontal();
				pr.setStyle(STBorderStyle.Enum.forInt(border + 1));
			}
		}
	}

	@Override
	public short getBorderHorizontal() {
        if(_ctDxf.isSetBorder()) {
            CTBorder ct = _ctDxf.getBorder();
            STBorderStyle.Enum ptrn = ct.isSetHorizontal() ? ct.getHorizontal().getStyle() : null;
            return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
        }
    	return BORDER_NONE;
	}

	@Override
	public Color getHorizontalBorderColorColor() {
        return getHorizontalBorderXSSFColor();
	}

	@Override
	public void setHorizontalBorderColor(short color) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(color);
        setHorizontalBorderColor(clr);
	}

	@Override
	public short getHorizontalBorderColor() {
        XSSFColor clr = getHorizontalBorderXSSFColor();
        return clr == null ? IndexedColors.BLACK.getIndex() : clr.getIndexed();
	}

	@Override
	public void setBorderVertical(short border) {
	}

	@Override
	public short getBorderVertical() {
        if(_ctDxf.isSetBorder()) {
            CTBorder ct = _ctDxf.getBorder();
            STBorderStyle.Enum ptrn = ct.isSetVertical() ? ct.getVertical().getStyle() : null;
            return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
        }
    	return BORDER_NONE;
	}

	@Override
	public Color getVerticalBorderColorColor() {
        return getVerticalBorderXSSFColor();
	}

	@Override
	public void setVerticalBorderColor(short color) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(color);
        setVerticalBorderColor(clr);
	}

	@Override
	public short getVerticalBorderColor() {
        XSSFColor clr = getVerticalBorderXSSFColor();
        return clr == null ? IndexedColors.BLACK.getIndex() : clr.getIndexed();
	}

	@Override
	public void setBorderDiagonal(short border) {
		if(border != BORDER_NONE && !_ctDxf.isSetBorder()) {
			_ctDxf.addNewBorder();
		}
		if (_ctDxf.isSetBorder()) {
			CTBorder ct = _ctDxf.getBorder();
			if (border == BORDER_NONE) {
				if (ct.isSetDiagonal()) ct.unsetDiagonal();
			} else {
				CTBorderPr pr = ct.isSetDiagonal() ? ct.getDiagonal() : ct.addNewDiagonal();
				pr.setStyle(STBorderStyle.Enum.forInt(border + 1));
			}
		}
	}

	@Override
	public short getBorderDiagonal() {
        if(_ctDxf.isSetBorder()) {
            CTBorder ct = _ctDxf.getBorder();
            STBorderStyle.Enum ptrn = ct.isSetDiagonal() ? ct.getDiagonal().getStyle() : null;
            return ptrn == null ? BORDER_NONE : (short)(ptrn.intValue() - 1);
        }
    	return BORDER_NONE;
	}

	@Override
	public Color getDiagonalBorderColorColor() {
        return getDiagonalBorderXSSFColor();
	}

	@Override
	public void setDiagonalBorderColor(short color) {
        XSSFColor clr = new XSSFColor();
        clr.setIndexed(color);
        setDiagonalBorderColor(clr);
	}

	@Override
	public short getDiagonalBorderColor() {
        XSSFColor clr = getDiagonalBorderXSSFColor();
        return clr == null ? IndexedColors.BLACK.getIndex() : clr.getIndexed();
	}

	@Override
	public boolean isShowDiagonalUpBorder() {
		if (_ctDxf.isSetBorder()) {
			CTBorder border = _ctDxf.getBorder();
			return border.getDiagonalUp();
		}
		return false;
	}

	@Override
	public boolean isShowDiagonalDownBorder() {
		if (_ctDxf.isSetBorder()) {
			CTBorder border = _ctDxf.getBorder();
			return border.getDiagonalDown();
		}
		return false;
	}

	@Override
	public void setShowDiagonalUpBorder(boolean up) {
		if (!_ctDxf.isSetBorder()) {
			_ctDxf.addNewBorder();
		}
		CTBorder border = _ctDxf.getBorder();
		border.setDiagonalUp(up);
	}

	@Override
	public void setShowDiagonalDownBorder(boolean down) {
		if (!_ctDxf.isSetBorder()) {
			_ctDxf.addNewBorder();
		}
		CTBorder border = _ctDxf.getBorder();
		border.setDiagonalDown(down);
	}

    /**
     * Set the color to use for the left border as a {@link XSSFColor} value
     *
     * @param color the color to use
     */
    public void setLeftBorderColor(XSSFColor color) {
        if(color == null && (!_ctDxf.isSetBorder() || !_ctDxf.getBorder().isSetLeft())) return;
        
    	if (!_ctDxf.isSetBorder()) {
    		_ctDxf.addNewBorder();
    	}
        CTBorder ct = _ctDxf.getBorder();
        CTBorderPr pr = ct.isSetLeft() ? ct.getLeft() : ct.addNewLeft();
        if (color != null)  pr.setColor(color.getCTColor());
        else pr.unsetColor();
    }

    /**
     * Set the color to use for the left border as a {@link XSSFColor} value
     *
     * @param color the color to use
     */
    public void setTopBorderColor(XSSFColor color) {
        if(color == null && (!_ctDxf.isSetBorder() || !_ctDxf.getBorder().isSetTop())) return;
        
    	if (!_ctDxf.isSetBorder()) {
    		_ctDxf.addNewBorder();
    	}
        CTBorder ct = _ctDxf.getBorder();
        CTBorderPr pr = ct.isSetTop() ? ct.getTop() : ct.addNewTop();
        if (color != null)  pr.setColor(color.getCTColor());
        else pr.unsetColor();
    }

    /**
     * Set the color to use for the left border as a {@link XSSFColor} value
     *
     * @param color the color to use
     */
    public void setRightBorderColor(XSSFColor color) {
        if(color == null && (!_ctDxf.isSetBorder() || !_ctDxf.getBorder().isSetRight())) return;
        
    	if (!_ctDxf.isSetBorder()) {
    		_ctDxf.addNewBorder();
    	}
        CTBorder ct = _ctDxf.getBorder();
        CTBorderPr pr = ct.isSetRight() ? ct.getRight() : ct.addNewRight();
        if (color != null)  pr.setColor(color.getCTColor());
        else pr.unsetColor();
    }

    /**
     * Set the color to use for the left border as a {@link XSSFColor} value
     *
     * @param color the color to use
     */
    public void setBottomBorderColor(XSSFColor color) {
        if(color == null && (!_ctDxf.isSetBorder() || !_ctDxf.getBorder().isSetBottom())) return;
        
    	if (!_ctDxf.isSetBorder()) {
    		_ctDxf.addNewBorder();
    	}
        CTBorder ct = _ctDxf.getBorder();
        CTBorderPr pr = ct.isSetBottom() ? ct.getBottom() : ct.addNewBottom();
        if (color != null)  pr.setColor(color.getCTColor());
        else pr.unsetColor();
    }

    /**
     * Set the color to use for the left border as a {@link XSSFColor} value
     *
     * @param color the color to use
     */
    public void setHorizontalBorderColor(XSSFColor color) {
        if(color == null && (!_ctDxf.isSetBorder() || !_ctDxf.getBorder().isSetHorizontal())) return;
        
    	if (!_ctDxf.isSetBorder()) {
    		_ctDxf.addNewBorder();
    	}
        CTBorder ct = _ctDxf.getBorder();
        CTBorderPr pr = ct.isSetHorizontal() ? ct.getHorizontal() : ct.addNewHorizontal();
        if (color != null)  pr.setColor(color.getCTColor());
        else pr.unsetColor();
    }

    /**
     * Set the color to use for the left border as a {@link XSSFColor} value
     *
     * @param color the color to use
     */
    public void setVerticalBorderColor(XSSFColor color) {
        if(color == null && (!_ctDxf.isSetBorder() || !_ctDxf.getBorder().isSetVertical())) return;
        
    	if (!_ctDxf.isSetBorder()) {
    		_ctDxf.addNewBorder();
    	}
        CTBorder ct = _ctDxf.getBorder();
        CTBorderPr pr = ct.isSetVertical() ? ct.getVertical() : ct.addNewVertical();
        if (color != null)  pr.setColor(color.getCTColor());
        else pr.unsetColor();
    }

    /**
     * Set the color to use for the left border as a {@link XSSFColor} value
     *
     * @param color the color to use
     */
    public void setDiagonalBorderColor(XSSFColor color) {
        if(color == null && (!_ctDxf.isSetBorder() || !_ctDxf.getBorder().isSetDiagonal())) return;
        
    	if (!_ctDxf.isSetBorder()) {
    		_ctDxf.addNewBorder();
    	}
        CTBorder ct = _ctDxf.getBorder();
        CTBorderPr pr = ct.isSetDiagonal() ? ct.getDiagonal() : ct.addNewDiagonal();
        if (color != null)  pr.setColor(color.getCTColor());
        else pr.unsetColor();
    }

    /**
     * Get the color to use for the left border
     *
     * @return the index of the color definition or <code>null</code> if not set
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public XSSFColor getLeftBorderXSSFColor() {
        if(_ctDxf.isSetBorder()) {
        	final CTBorder ct = _ctDxf.getBorder();
        	if (ct.isSetLeft()) {
        		final CTBorderPr pr = ct.getLeft();
        		if (pr.isSetColor()) {
        			final CTColor color = pr.getColor();
        			return new XSSFColor(color);
        		}
        	}
        }
        return null;
    }

    /**
     * Get the color to use for the top border
     *
     * @return the index of the color definition or <code>null</code> if not set
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public XSSFColor getTopBorderXSSFColor() {
        if(_ctDxf.isSetBorder()) {
        	final CTBorder ct = _ctDxf.getBorder();
        	if (ct.isSetTop()) {
        		final CTBorderPr pr = ct.getTop();
        		if (pr.isSetColor()) {
        			final CTColor color = pr.getColor();
        			return new XSSFColor(color);
        		}
        	}
        }
        return null;
    }

    /**
     * Get the color to use for the right border
     *
     * @return the index of the color definition or <code>null</code> if not set
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public XSSFColor getRightBorderXSSFColor() {
        if(_ctDxf.isSetBorder()) {
        	final CTBorder ct = _ctDxf.getBorder();
        	if (ct.isSetRight()) {
        		final CTBorderPr pr = ct.getRight();
        		if (pr.isSetColor()) {
        			final CTColor color = pr.getColor();
        			return new XSSFColor(color);
        		}
        	}
        }
        return null;
    }


    /**
     * Get the color to use for the bottom border
     *
     * @return the index of the color definition or <code>null</code> if not set
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public XSSFColor getBottomBorderXSSFColor() {
        if(_ctDxf.isSetBorder()) {
        	final CTBorder ct = _ctDxf.getBorder();
        	if (ct.isSetBottom()) {
        		final CTBorderPr pr = ct.getBottom();
        		if (pr.isSetColor()) {
        			final CTColor color = pr.getColor();
        			return new XSSFColor(color);
        		}
        	}
        }
        return null;
    }

    /**
     * Get the color to use for the horizontal border
     *
     * @return the index of the color definition or <code>null</code> if not set
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public XSSFColor getHorizontalBorderXSSFColor() {
        if(_ctDxf.isSetBorder()) {
        	final CTBorder ct = _ctDxf.getBorder();
        	if (ct.isSetHorizontal()) {
        		final CTBorderPr pr = ct.getHorizontal();
        		if (pr.isSetColor()) {
        			final CTColor color = pr.getColor();
        			return new XSSFColor(color);
        		}
        	}
        }
        return null;
    }

    /**
     * Get the color to use for the vertical border
     *
     * @return the index of the color definition or <code>null</code> if not set
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public XSSFColor getVerticalBorderXSSFColor() {
        if(_ctDxf.isSetBorder()) {
        	final CTBorder ct = _ctDxf.getBorder();
        	if (ct.isSetVertical()) {
        		final CTBorderPr pr = ct.getVertical();
        		if (pr.isSetColor()) {
        			final CTColor color = pr.getColor();
        			return new XSSFColor(color);
        		}
        	}
        }
        return null;
    }

    /**
     * Get the color to use for the diagonal border
     *
     * @return the index of the color definition or <code>null</code> if not set
     * @see org.zkoss.poi.ss.usermodel.IndexedColors
     */
    public XSSFColor getDiagonalBorderXSSFColor() {
        if(_ctDxf.isSetBorder()) {
        	final CTBorder ct = _ctDxf.getBorder();
        	if (ct.isSetDiagonal()) {
        		final CTBorderPr pr = ct.getDiagonal();
        		if (pr.isSetColor()) {
        			final CTColor color = pr.getColor();
        			return new XSSFColor(color);
        		}
        	}
        }
        return null;
    }

    /**
    * Set the foreground fill color represented as a {@link XSSFColor} value.
     * <br/>
    * <i>Note: Ensure Foreground color is set prior to background color.</i>
    * @param color the color to use
    * @see #setFillBackgroundColor(org.zkoss.poi.xssf.usermodel.XSSFColor) )
    */
    public void setFillForegroundColor(XSSFColor color) {
    	if (!_ctDxf.isSetFill()) {
    		_ctDxf.addNewFill();
    	}
    	
        CTFill ct = _ctDxf.getFill();
        CTPatternFill ptrn = ct.getPatternFill();
        if(color == null) {
            if(ptrn != null) ptrn.unsetFgColor();
        } else {
            if(ptrn == null) ptrn = ct.addNewPatternFill();
            ptrn.setFgColor(color.getCTColor());
        }
    }


    /**
     * Set the background fill color represented as a {@link XSSFColor} value.
     * <p>
     * For example:
     * <pre>
     * cs.setFillPattern(XSSFCellStyle.FINE_DOTS );
     * cs.setFillBackgroundXSSFColor(new XSSFColor(java.awt.Color.RED));
     * </pre>
     * optionally a Foreground and background fill can be applied:
     * <i>Note: Ensure Foreground color is set prior to background</i>
     * <pre>
     * cs.setFillPattern(XSSFCellStyle.FINE_DOTS );
     * cs.setFillForegroundColor(new XSSFColor(java.awt.Color.BLUE));
     * cs.setFillBackgroundColor(new XSSFColor(java.awt.Color.GREEN));
     * </pre>
     * or, for the special case of SOLID_FILL:
     * <pre>
     * cs.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND );
     * cs.setFillForegroundColor(new XSSFColor(java.awt.Color.GREEN));
     * </pre>
     * It is necessary to set the fill style in order
     * for the color to be shown in the cell.
     *
     * @param color - the color to use
     */
    public void setFillBackgroundColor(XSSFColor color) {
    	if (!_ctDxf.isSetFill()) {
    		_ctDxf.addNewFill();
    	}
        CTFill ct = _ctDxf.getFill();
        CTPatternFill ptrn = ct.getPatternFill();
        if(color == null) {
            if(ptrn != null) ptrn.unsetBgColor();
        } else {
            if(ptrn == null) ptrn = ct.addNewPatternFill();
            ptrn.setBgColor(color.getCTColor());
        }
    }

    /**
     * Get the background fill color.
     * <p>
     * Note - many cells are actually filled with a foreground
     *  fill, not a background fill - see {@link #getFillForegroundColor()}
     * </p>
     * @see org.zkoss.poi.xssf.usermodel.XSSFColor#getRgb()
     * @return XSSFColor - fill color or <code>null</code> if not set
     */
    public XSSFColor getFillBackgroundXSSFColor() {
    	if (_ctDxf.isSetFill()) {
    		CTFill fill = _ctDxf.getFill();
    		if (fill.isSetPatternFill()) {
    			CTPatternFill pf = fill.getPatternFill();
    			if (pf.isSetBgColor()) {
    				CTColor color = pf.getBgColor();
    				return new XSSFColor(color);
    			}
    		}
    	}
    	return null;
    }

    /**
     * Get the foreground fill color.
     * <p>
     * Note - many cells are actually filled with a foreground
     *  fill, not a background fill - see {@link #getFillForegroundColor()}
     * </p>
     * @see org.zkoss.poi.xssf.usermodel.XSSFColor#getRgb()
     * @return XSSFColor - fill color or <code>null</code> if not set
     */
    public XSSFColor getFillForegroundXSSFColor() {
    	if (_ctDxf.isSetFill()) {
    		final CTFill fill = _ctDxf.getFill();
    		if (fill.isSetPatternFill()) {
    			final CTPatternFill pf = fill.getPatternFill();
    			if (pf.isSetFgColor()) {
    				final CTColor color = pf.getFgColor();
    				return new XSSFColor(color);
    			}
    		}
    	}
    	return null;
    }

	@Override
	public void setBorder(short left, Color leftColor, short top,
			Color topColor, short right, Color rightColor, short bottom,
			Color bottomColor, short diagonal, Color diagonalColor,
			short horizontal, Color horizontalColor, short vertical,
			Color verticalColor, boolean diaUp, boolean diaDown) {
		if (!_ctDxf.isSetBorder()) {
			_ctDxf.addNewBorder();
		}
		CTBorder ct = _ctDxf.getBorder();
		ct.setDiagonalUp(diaUp);
		ct.setDiagonalDown(diaDown);

        // always generate <left/> ...
       	CTBorderPr lbpr = ct.isSetLeft() ? ct.getLeft() : 
       		left != CellStyle.BORDER_NONE ? ct.addNewLeft() : null;
      	setBorderStyle(lbpr, left);
      	
       	CTBorderPr tbpr = ct.isSetTop() ? ct.getTop() : 
       		top != CellStyle.BORDER_NONE ? ct.addNewTop() : null;
    	setBorderStyle(tbpr, top);
    	
       	CTBorderPr rbpr = ct.isSetRight() ? ct.getRight() : 
       		right != CellStyle.BORDER_NONE ? ct.addNewRight() : null;
    	setBorderStyle(rbpr, right);
    	
       	CTBorderPr bbpr = ct.isSetBottom() ? ct.getBottom() :
       		bottom != CellStyle.BORDER_NONE ? ct.addNewBottom() : null;
       	setBorderStyle(bbpr, bottom);

       	CTBorderPr dbpr = ct.isSetDiagonal() ? ct.getDiagonal() :
       		diagonal != CellStyle.BORDER_NONE ? ct.addNewDiagonal() : null;
       	setBorderStyle(dbpr, diagonal);

       	CTBorderPr vbpr = ct.isSetVertical() ? ct.getVertical() :
       		vertical != CellStyle.BORDER_NONE ? ct.addNewVertical() : null;
       	setBorderStyle(vbpr, vertical);

       	CTBorderPr hbpr = ct.isSetHorizontal() ? ct.getHorizontal() :
       		horizontal != CellStyle.BORDER_NONE ? ct.addNewHorizontal() : null;
       	setBorderStyle(hbpr, horizontal);

        setBorderColor(lbpr, (XSSFColor)leftColor);
        setBorderColor(tbpr, (XSSFColor)topColor);
        setBorderColor(rbpr, (XSSFColor)rightColor);
        setBorderColor(bbpr, (XSSFColor)bottomColor);
        setBorderColor(hbpr, (XSSFColor)horizontalColor);
        setBorderColor(vbpr, (XSSFColor)verticalColor);
        setBorderColor(dbpr, (XSSFColor)diagonalColor);
	}
}
