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
package org.zkoss.zss.model;

/**
 * Store various setting for printing.
 * @author dennis
 * @since 3.5.0
 */
public interface SPrintSetup {
	/**
	 * @since 3.5.0
	 */
	public enum PaperSize {
	    /** US Letter 8 1/2 x 11 in */
	    LETTER,
	    /** US Letter Small 8 1/2 x 11 in */
	    LETTER_SMALL,
	    /** US Tabloid 11 x 17 in */
	    TABLOID,
	    /** US Ledger 17 x 11 in */
	    LEDGER,
	    /** US Legal 8 1/2 x 14 in */
	    LEGAL,
	    /** US Statement 5 1/2 x 8 1/2 in */
	    STATEMENT,
	    /** US Executive 7 1/4 x 10 1/2 in */
	    EXECUTIVE,
	    /** A3 - 297x420 mm */
	    A3,
	    /** A4 - 210x297 mm */
	    A4,
	    /** A4 Small - 210x297 mm */
	    A4_SMALL,
	    /** A5 - 148x210 mm */
	    A5,
	    /** B4 (JIS) 250x354 mm */
	    B4,
	    /** B5 (JIS) 182x257 mm */
	    B5,
	    /** Folio 8 1/2 x 13 in */
	    FOLIO8,
	    /** Quarto 215x275 mm */
	    QUARTO,
	    /** 10 x 14 in */
	    TEN_BY_FOURTEEN,
	    /** 11 x 17 in */
	    ELEVEN_BY_SEVENTEEN,
	    /** US Note 8 1/2 x 11 in */
	    NOTE8,
	    /** US Envelope #9 3 7/8 x 8 7/8 */
	    ENVELOPE_9,
	    /** US Envelope #10 4 1/8 x 9 1/2 */
	    ENVELOPE_10,
	    /** Envelope DL 110x220 mm */
	    ENVELOPE_DL,
	    /** Envelope C5 162x229 mm */
	    ENVELOPE_CS,
	    ENVELOPE_C5,
	    /** Envelope C3 324x458 mm */
	    ENVELOPE_C3,
	    /** Envelope C4 229x324 mm */
	    ENVELOPE_C4,
	    /** Envelope C6 114x162 mm */
	    ENVELOPE_C6,

	    ENVELOPE_MONARCH,
	    /** A4 Extra - 9.27 x 12.69 in */
	    A4_EXTRA,
	    /** A4 Transverse - 210x297 mm */
	    A4_TRANSVERSE,
	    /** A4 Plus - 210x330 mm */
	    A4_PLUS,
	    /** US Letter Rotated 11 x 8 1/2 in */
	    LETTER_ROTATED,
	    /** A4 Rotated - 297x210 mm */
	    A4_ROTATED
	}
	
	public boolean isPrintGridlines();
	public void setPrintGridlines(boolean enable);
	
	public double getHeaderMargin();
	public void setHeaderMargin(double inches);
	public double getFooterMargin();
	public void setFooterMargin(double inches);
	public double getLeftMargin();
	public void setLeftMargin(double inches);
	public double getRightMargin();
	public void setRightMargin(double inches);
	public double getTopMargin();
	public void setTopMargin(double inches);
	public double getBottomMargin();
	public void setBottomMargin(double inches);
	
	public void setPaperSize(PaperSize size);
	public PaperSize getPaperSize();
	
	/**
	 * Set true to print in landscape orientation. 
	 * @param landscape
	 */
	public void setLandscape(boolean landscape);
	public boolean isLandscape();
	
	/**
	 * 
	 * @param scale must be between 10 ~ 400 (inclusive).
	 * @since 3.6.0
	 */
	public void setScale(int scale); // x100; e.g. 20 => 20%, 400 => 400%
	public int getScale();

	/**
	 * Set general Header or odd page header.
	 * @param header
	 * @since 3.6.0
	 */
	public void setHeader(SHeader header);
	public SHeader getHeader();

	/**
	 * Set even page footer; valid only if
	 * {@link #isDifferentOddEvenPage()} is true.
	 * 
	 * @param header
	 * @since 3.6.0
	 */
	public void setEvenHeader(SHeader header);
	public SHeader getEvenHeader();

	/**
	 * Set first page footer; valid only if
	 * {@link #isDifferentFirstPage()} is true.
	 * 
	 * @param header
	 * @since 3.6.0
	 */
	public void setFirstHeader(SHeader header);
	public SHeader getFirstHeader();
	
	/**
	 * Set general footer or odd page footer. 
	 * @param footer
	 * @since 3.6.0
	 */
	public void setFooter(SFooter footer);
	public SFooter getFooter();

	/**
	 * Set even page footer; valid only if
	 * {@link #isDifferentOddEvenPage()} is true.
	 * 
	 * @param footer
	 * @since 3.6.0
	 */
	public void setEvenFooter(SFooter footer);
	public SFooter getEvenFooter();

	/**
	 * Set first page footer; valid only if 
	 * {@link #isDifferentFirstPage()} is true.
	 * 
	 * @param footer
	 * @since 3.6.0
	 */
	public void setFirstFooter(SFooter footer);
	public SFooter getFirstFooter();
	
	/**
	 * Set true to print even page with special header and footer for even
	 * page; {@see #getEvenHeader()} and {@see #getEvenFooter()}.
	 * @param flag
	 * @since 3.6.0
	 */
	public void setDifferentOddEvenPage(boolean flag);
	public boolean isDifferentOddEvenPage();
	
	/**
	 * Set true to print first page with special header and footer for first
	 * page; {@see #getFirstHeader()} and {@see #getFirstFooter()}.
	 * 
	 * @param flag
	 * @since 3.6.0
	 */
	public void setDifferentFirstPage(boolean flag);
	public boolean isDifferentFirstPage();
	
		
	/**
	 * Set true to scale header/footer with document.
	 * @param flag
	 * @since 3.6.0
	 */
	public void setScaleWithDoc(boolean flag);
	public boolean isScaleWithDoc();
	
	/**
	 * Set true to align header/footer with page margins.
	 * @param flag
	 * @since 3.6.0
	 */
	public void setAlignWithMargins(boolean flag);
	public boolean isAlignWithMargins();
	
	/**
	 * Set true to print sheet center horizontally on page.
	 * @param center
	 * @since 3.6.0
	 */
	public void setHCenter(boolean center);
	public boolean isHCenter();

	/**
	 * Set true to print sheet center vertically on page.
	 * @param vcenter
	 * @since 3.6.0
	 */
	public void setVCenter(boolean vcenter);
	public boolean isVCenter();
	
	/**
	 * 
	 * @param start
	 * @since 3.6.0
	 */
	public void setPageStart(int start); // set starting page number
	public int getPageStart();
	
	/**
	 * Set the number of pages the sheet width 
	 * is fit to. MUST be less than or equal to 32767. The value 0 means use 
	 * as many pages as necessary to print the columns in the sheet.
	 * @since 3.6.0
	 */ 
	public void setFitWidth(int numPages); 
	public int getFitWidth();
	
	/**
	 * Set the number of pages the sheet height 
	 * is fit to. MUST be less than or equal to 32767. The value 0 means use 
	 * as many pages as necessary to print the rows of the sheet.
	 * @param pages
	 * @since 3.6.0
	 */
	public void setFitHeight(int numPages); // fit sheet in how many pages of print page height;
	public int getFitHeight();

	/**
	 * Set the print area as an area formula; e.g. A1:B2
	 * @param formula
	 * @since 3.6.0
	 */
	public void setPrintArea(String formula);
	public String getPrintArea();
	
	/**
	 * Set the first row of the repeat title rows for each top-to-bottom pages.
	 * 
	 * @param formula
	 * @since 3.6.0
	 */
	public void setRepeatingRowsTitle(int firstRow, int lastRow);
	public CellRegion getRepeatingRowsTitle();


	/**
	 * Set the first column of the repeat title columns for each left-to-right 
	 * pages.
	 * 
	 * @param formula
	 * @since 3.6.0
	 */
	public void setRepeatingColumnsTitle(int firstCol, int lastCol);
	public CellRegion getRepeatingColumnsTitle();
	
	/**
	 * Set true to also out row and column headings.
	 * @param flag
	 * @since 3.6.0
	 */
	public void setPrintHeadings(boolean flag);
	public boolean isPrintHeadings();

	/**
	 * Set how to print comments:
	 * 0: none
	 * 1: at end of sheet
	 * 2: as displayed on the sheet
	 * @param mode
	 * @since 3.6.0
	 */
	public void setCommentsMode(int mode);
	public int getCommentsMode();
	
	/**
	 * Set how to handle errors in the cell data;
	 * 0: print errors as displayed on the sheet
	 * 1: print errors as blank
	 * 2: print errors as dashes ("--")
	 * 3: print errors as "#N/A".
	 * 
	 * @param mode
	 * @since 3.6.0
	 */
	public void setErrorPrintMode(int mode);
	public int getErrorPrintMode();
	
	/**
	 * Set true to output multiple pages in the order of left-to-right first 
	 * and then top-to-bottom; false in the order of top-to-bottom first and
	 * then left-to-right.
	 * @param flag
	 * @since 3.6.0
	 */
	public void setLeftToRight(boolean flag);
	public boolean isLeftToRight();
}
