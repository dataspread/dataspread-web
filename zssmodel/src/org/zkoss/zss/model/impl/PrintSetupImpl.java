/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/01 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.impl;

import java.io.Serializable;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SFooter;
import org.zkoss.zss.model.SHeader;
import org.zkoss.zss.model.SPrintSetup;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.sys.EngineFactory;
import org.zkoss.zss.model.sys.formula.FormulaParseContext;
import org.zkoss.zss.model.sys.formula.FormulaExpression;
import org.zkoss.zss.model.sys.dependency.Ref;

/**
 * 
 * @author Dennis
 * @since 3.5.0
 */
public class PrintSetupImpl implements SPrintSetup,Serializable {
	private static final long serialVersionUID = 1L;
	
	private boolean _printGridlines = false; 
	private double _headerMargin; //inches
	private double _footerMargin; //inches
	private double _leftMargin;	  //inches 	
	private double _rightMargin;  //inches	
	private double _topMargin;	  //inches
	private double _bottomMargin; //inches
	
	private boolean _landscape = false;
	private int _scale = 100;
	private PaperSize _paperSize = PaperSize.A4;

	private SHeader _header;
	private SHeader _evenHeader;
	private SHeader _firstHeader;
	
	private SFooter _footer;
	private SFooter _evenFooter;
	private SFooter _firstFooter;

	private boolean _diffOddEven;
	private boolean _diffFirst;
	private boolean _scaleWithDoc;
	private boolean _alignWithMargins;
	private boolean _hCenter;
	private boolean _vCenter;
	
	private int _pageStart;  //<= 0 means auto
	private int _fitWidth;
	private int _fitHeight;
	
	private String _printArea;
	private int _rowsTitle1 = -1; // negative value represents ignore
	private int _rowsTitle2 = -1; // negative value represents ignore
	private int _columnsTitle1 = -1; // negative value represents ignore
	private int _columnsTitle2 = -1; // negative value represents ignore
	
	private boolean _printHeadings;
	private int _errorMode;
	private int _commentMode;
	private boolean _leftToRight;
	
	@Override
	public boolean isPrintGridlines() {
		return _printGridlines;
	}

	@Override
	public void setPrintGridlines(boolean enable) {
		_printGridlines = enable;
	}

	@Override
	public double getHeaderMargin() {
		return _headerMargin;
	}

	@Override
	public void setHeaderMargin(double headerMargin) {
		this._headerMargin = headerMargin;
	}

	@Override
	public double getFooterMargin() {
		return _footerMargin;
	}

	@Override
	public void setFooterMargin(double footerMargin) {
		this._footerMargin = footerMargin;
	}

	@Override
	public double getLeftMargin() {
		return _leftMargin;
	}

	@Override
	public void setLeftMargin(double leftMargin) {
		this._leftMargin = leftMargin;
	}

	@Override
	public double getRightMargin() {
		return _rightMargin;
	}

	@Override
	public void setRightMargin(double rightMargin) {
		this._rightMargin = rightMargin;
	}

	@Override
	public double getTopMargin() {
		return _topMargin;
	}

	@Override
	public void setTopMargin(double topMargin) {
		this._topMargin = topMargin;
	}

	@Override
	public double getBottomMargin() {
		return _bottomMargin;
	}

	@Override
	public void setBottomMargin(double bottomMargin) {
		this._bottomMargin = bottomMargin;
	}

	@Override
	public void setPaperSize(PaperSize size) {
		this._paperSize = size;
	}

	@Override
	public PaperSize getPaperSize() {
		return _paperSize;
	}

	@Override
	public void setLandscape(boolean landscape) {
		this._landscape = landscape;
	}

	@Override
	public boolean isLandscape() {
		return _landscape;
	}

	@Override
	public void setScale(int scale) {
		if (scale < 10 || scale > 400) scale = 100; //ZSS-892
		this._scale = scale;
	}

	@Override
	public int getScale() {
		return _scale;
	}

	//ZSS-688
	//@since 3.6.0
	//internal use
	public void copyFrom(PrintSetupImpl src) {
		
		this._printGridlines = src._printGridlines; 
		this._headerMargin = src._headerMargin;
		this._footerMargin = src._footerMargin;
		this._leftMargin = src._leftMargin;
		this._rightMargin = src._rightMargin;
		this._topMargin = src._topMargin;
		this._bottomMargin = src._bottomMargin;
		
		this._landscape = src._landscape;
		setScale(src._scale);
		this._paperSize = src._paperSize;
		
		HeaderFooterImpl srcHF = (HeaderFooterImpl) src._header;
		if (srcHF != null) {
			this._header = srcHF.cloneHeaderFooterImpl();
		}
		srcHF = (HeaderFooterImpl) src._evenHeader;
		if (srcHF != null) {
			this._evenHeader = srcHF.cloneHeaderFooterImpl();
		}
		srcHF = (HeaderFooterImpl) src._firstHeader;
		if (srcHF != null) {
			this._firstHeader = srcHF.cloneHeaderFooterImpl();
		}
		
		srcHF = (HeaderFooterImpl) src._footer;
		if (srcHF != null) {
			this._footer = srcHF.cloneHeaderFooterImpl();
		}
		srcHF = (HeaderFooterImpl) src._evenFooter;
		if (srcHF != null) {
			this._evenFooter = ((HeaderFooterImpl)src._evenFooter).cloneHeaderFooterImpl();;
		}
		srcHF = (HeaderFooterImpl) src._firstFooter;
		if (srcHF != null) {
			this._firstFooter = ((HeaderFooterImpl)src._firstFooter).cloneHeaderFooterImpl();;
		}
		
		this._diffOddEven = src._diffOddEven;
		this._diffFirst = src._diffFirst;
		this._scaleWithDoc = src._scaleWithDoc;
		this._alignWithMargins = src._alignWithMargins;
		this._hCenter = src._hCenter;
		this._vCenter = src._vCenter;

		this._pageStart = src._pageStart;  //<= 0 means auto
		this._fitWidth = src._fitWidth;
		this._fitHeight = src._fitHeight;

		this._printArea = src._printArea;
		this._rowsTitle1 = src._rowsTitle1;
		this._rowsTitle2 = src._rowsTitle2;
		this._columnsTitle1 = src._columnsTitle1;
		this._columnsTitle2 = src._columnsTitle2;
		
		this._printHeadings = src._printHeadings;
		this._errorMode = src._errorMode;
		this._commentMode = src._commentMode;
		this._leftToRight = src._leftToRight;
	}

	@Override
	public void setHeader(SHeader header) {
		_header = header;
	}

	@Override
	public SHeader getHeader() {
		return _header;
	}

	@Override
	public void setEvenHeader(SHeader header) {
		_evenHeader = header;
	}

	@Override
	public SHeader getEvenHeader() {
		return _evenHeader;
	}

	@Override
	public void setFirstHeader(SHeader header) {
		_firstHeader = header;
	}

	@Override
	public SHeader getFirstHeader() {
		return _firstHeader;
	}

	@Override
	public void setFooter(SFooter footer) {
		_footer = footer;
	}

	@Override
	public SFooter getFooter() {
		return _footer;
	}

	@Override
	public void setEvenFooter(SFooter footer) {
		_evenFooter = footer;
	}

	@Override
	public SFooter getEvenFooter() {
		return _evenFooter;
	}

	@Override
	public void setFirstFooter(SFooter footer) {
		_firstFooter = footer;
	}

	@Override
	public SFooter getFirstFooter() {
		return _firstFooter;
	}

	@Override
	public void setScaleWithDoc(boolean flag) {
		_scaleWithDoc = flag;
	}

	@Override
	public boolean isScaleWithDoc() {
		return _scaleWithDoc;
	}

	@Override
	public void setAlignWithMargins(boolean flag) {
		_alignWithMargins = flag;
		
	}

	@Override
	public boolean isAlignWithMargins() {
		return _alignWithMargins;
	}

	@Override
	public void setHCenter(boolean center) {
		_hCenter = center;
	}

	@Override
	public boolean isHCenter() {
		return _hCenter;
	}

	@Override
	public void setVCenter(boolean center) {
		_vCenter = center;
		
	}

	@Override
	public boolean isVCenter() {
		return _vCenter;
	}

	@Override
	public void setPageStart(int start) {
		_pageStart = start;
	}

	@Override
	public int getPageStart() {
		return _pageStart;
	}

	@Override
	public void setFitWidth(int numPages) {
		_fitWidth = numPages;
	}

	@Override
	public int getFitWidth() {
		return _fitWidth;
	}

	@Override
	public void setFitHeight(int numPages) {
		_fitHeight = numPages;
		
	}

	@Override
	public int getFitHeight() {
		return _fitHeight;
	}

	@Override
	public void setPrintArea(String formula) {
		_printArea = formula;
	}

	@Override
	public String getPrintArea() {
		return _printArea;
	}

	@Override
	public void setRepeatingRowsTitle(int firstRow, int lastRow) {
		_rowsTitle1 = firstRow;
		_rowsTitle2 = lastRow;
	}

	@Override
	public CellRegion getRepeatingRowsTitle() {
		return _rowsTitle1 >= 0 && _rowsTitle2 >= _rowsTitle1 ? 
				new CellRegion(_rowsTitle1, 0, _rowsTitle2, 0) : null;
	}

	@Override
	public void setRepeatingColumnsTitle(int firstCol, int lastCol) {
		_columnsTitle1 = firstCol;
		_columnsTitle2 = lastCol;
	}

	@Override
	public CellRegion getRepeatingColumnsTitle() {
		return _columnsTitle1 >= 0 && _columnsTitle2 >= _columnsTitle1 ? 
				new CellRegion(0, _columnsTitle1, 0, _columnsTitle2) : null;
	}

	@Override
	public void setPrintHeadings(boolean flag) {
		_printHeadings = flag;
	}

	@Override
	public boolean isPrintHeadings() {
		return _printHeadings;
	}

	@Override
	public void setCommentsMode(int mode) {
		_commentMode = mode;
	}

	@Override
	public int getCommentsMode() {
		return _commentMode;
	}

	@Override
	public void setErrorPrintMode(int mode) {
		_errorMode = mode;
	}

	@Override
	public int getErrorPrintMode() {
		return _errorMode;
	}

	@Override
	public void setLeftToRight(boolean flag) {
		_leftToRight = flag;
	}

	@Override
	public boolean isLeftToRight() {
		return _leftToRight;
	}

	@Override
	public void setDifferentOddEvenPage(boolean flag) {
		_diffOddEven = flag;
	}

	@Override
	public boolean isDifferentOddEvenPage() {
		return _diffOddEven;
	}

	@Override
	public void setDifferentFirstPage(boolean flag) {
		_diffFirst = flag;
	}

	@Override
	public boolean isDifferentFirstPage() {
		return _diffFirst;
	}
}
