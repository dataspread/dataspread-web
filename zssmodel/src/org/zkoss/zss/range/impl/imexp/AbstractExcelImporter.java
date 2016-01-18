/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/13 , Created by Hawk
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
 */
package org.zkoss.zss.range.impl.imexp;

import java.io.*;
import java.util.*;

import org.zkoss.poi.hssf.usermodel.HSSFRichTextString;
import org.zkoss.poi.ss.formula.eval.BoolEval;
import org.zkoss.poi.ss.formula.eval.ErrorEval;
import org.zkoss.poi.ss.formula.eval.NumberEval;
import org.zkoss.poi.ss.formula.eval.StringEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.poi.ss.formula.ptg.FuncVarPtg;
import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.poi.ss.usermodel.*;
import org.zkoss.poi.ss.util.CellRangeAddress;
import org.zkoss.poi.xssf.usermodel.*;
import org.zkoss.util.Locales;
import org.zkoss.zss.model.*;
import org.zkoss.zss.model.SAutoFilter.NFilterColumn;
import org.zkoss.zss.model.SFill.FillPattern;
import org.zkoss.zss.model.SPicture.Format;
import org.zkoss.zss.model.SSheet.SheetVisible;
import org.zkoss.zss.model.impl.AbstractCellStyleAdv;
import org.zkoss.zss.model.impl.BookImpl;
import org.zkoss.zss.model.impl.CellStyleImpl;
import org.zkoss.zss.model.impl.HeaderFooterImpl;
import org.zkoss.zss.model.impl.NamedStyleImpl;
import org.zkoss.zss.model.impl.AbstractBookAdv;
import org.zkoss.zss.model.impl.RichTextImpl;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.sys.formula.FormulaExpression;

/**
 * Contains common importing behavior for both XLSX and XLS. Spreadsheet
 * {@link SBook} model including following information: Book: name Sheet: name,
 * (default) column width, (default) row height, hidden row (column), row
 * (column) style, freeze, merge, protection, named range , gridline display
 * Cell: type, value, font with color and style, type offset(normal or
 * subscript), background color, border's type and color , data format,
 * alignment, wrap, locked, fill pattern
 * 
 * We use XLSX, XLS common interface (e.g. CellStyle instead of
 * {@link XSSFCellStyle}) to get content first for that codes can be easily
 * moved to parent class.
 * 
 * @author Hawk
 * @since 3.5.0
 */
abstract public class AbstractExcelImporter extends AbstractImporter {
	/**
	 * Office Open XML Part 4: Markup Language Reference 3.3.1.12 col (Column
	 * Width & Formatting) The character width 7 is based on Calibri 11. We can
	 * get correct column width under Excel 2007, but incorrect column width in
	 * 2010
	 */
	public static final int CHRACTER_WIDTH = 7;
	/**
	 * <poi CellStyle index, {@link SCellStyle} object> Keep track of imported
	 * style during importing to avoid creating duplicated style objects.
	 */
	protected Map<CellStyle, SCellStyle> importedStyle = new HashMap<CellStyle, SCellStyle>(); 	//ZSS-685
	/** <poi Font index, {@link SFont} object> **/
	protected Map<Short, SFont> importedFont = new HashMap<Short, SFont>();
	/** target book model */
	protected SBook book;
	/** source POI book */
	protected Workbook workbook;
	//ZSS-735, poiPictureData -> SPictureData index
	protected Map<PictureData, Integer> importedPictureData = new HashMap<PictureData, Integer>(); 
	
	/** book type key for book attribute **/
	protected static String BOOK_TYPE_KEY = "$ZSS.BOOKTYPE$";

	//ZSS-854
	private void importDefaultCellStyles() {
		((AbstractBookAdv)book).clearDefaultCellStyles();
		for (CellStyle poiStyle : workbook.getDefaultCellStyles()) {
			book.addDefaultCellStyle(importCellStyle(poiStyle, false));
		}
		// in case of XLS files which we have not support defaultCellStyles 
		if (book.getDefaultCellStyles().isEmpty()) {
			((AbstractBookAdv)book).initDefaultCellStyles();
		}
	}
	//ZSS-854
	private void importNamedStyles() {
		((AbstractBookAdv)book).clearNamedStyles();
		for (NamedStyle poiStyle : workbook.getNamedStyles()) {
			SNamedStyle namedStyle = 
					new NamedStyleImpl(poiStyle.getName(), poiStyle.isCustomBuiltin(), 
							poiStyle.getBuiltinId(), book, (int) poiStyle.getIndex());
			book.addNamedCellstyle(namedStyle);
		}
	}
	
	/**
	 * Import the model according to reversed dependency order among model
	 * objects: book, sheet, defined name, cells, chart, pictures, validation.
	 */
	@Override
	public SBook imports(InputStream is, String bookName) throws IOException {
		
		// clear cache for reuse
		importedStyle.clear();
		importedFont.clear();

		workbook = createPoiBook(is);
		book = SBooks.createBook(bookName);
//		book.setDefaultCellStyle(importCellStyle(workbook.getCellStyleAt((short) 0), false)); //ZSS-780
		//ZSS-854
		importDefaultCellStyles();
		importNamedStyles();
		
		setBookType(book);

		//ZSS-715: Enforce internal Locale.US Locale so formula is in consistent internal format
		Locale old = Locales.setThreadLocal(Locale.US);
		SBookSeries bookSeries = book.getBookSeries();
		boolean isCacheClean = bookSeries.isAutoFormulaCacheClean();
		try {
			bookSeries.setAutoFormulaCacheClean(false);// disable it to avoid
														// unnecessary clean up
														// during importing

			importExternalBookLinks();
			int numberOfSheet = workbook.getNumberOfSheets();
			for (int i = 0; i < numberOfSheet; i++) {
				Sheet poiSheet = workbook.getSheetAt(i);
				importSheet(poiSheet, i);
				SSheet sheet = book.getSheet(i);
				importTables(poiSheet, sheet); //ZSS-855, ZSS-1011
			}
			importNamedRange();
			for (int i = 0; i < numberOfSheet; i++) {
				SSheet sheet = book.getSheet(i);
				Sheet poiSheet = workbook.getSheetAt(i);
				for (Row poiRow : poiSheet) {
					importRow(poiRow, sheet);
				}
				importColumn(poiSheet, sheet);
				importMergedRegions(poiSheet, sheet);
				importDrawings(poiSheet, sheet);
				importValidation(poiSheet, sheet);
				importAutoFilter(poiSheet, sheet);
				importSheetProtection(poiSheet, sheet); //ZSS-576
			}
		} finally {
			book.getBookSeries().setAutoFormulaCacheClean(isCacheClean);
			Locales.setThreadLocal(old);
		}

		return book;
	}

	abstract protected Workbook createPoiBook(InputStream is) throws IOException;
	
	
	abstract protected void setBookType(SBook book);
	/**
	 * Gets the book-type information ("xls" or "xlsx"), return null if not found
	 * @param book
	 * @return
	 */
	public static String getBookType(SBook book){
		return (String)book.getAttribute(BOOK_TYPE_KEY);
	}

	/**
	 * When a column is hidden with default width, we don't import the width for it's 0. 
	 * We also don't import the width that equals to default width for optimization.
	 * 
	 * @param poiSheet
	 * @param sheet
	 */
	abstract protected void importColumn(Sheet poiSheet, SSheet sheet);

	/**
	 * If in same column: anchorWidthInFirstColumn + anchor width in
	 * inter-columns + anchorWidthInLastColumn (dx2) no in same column:
	 * anchorWidthInLastColumn - offsetInFirstColumn (dx1)
	 * 
	 */
	abstract protected int getAnchorWidthInPx(ClientAnchor anchor, Sheet poiSheet);

	abstract protected int getAnchorHeightInPx(ClientAnchor anchor, Sheet poiSheet);

	/**
	 * Name should be created after sheets created. A special defined name,
	 * _xlnm._FilterDatabase (xlsx) or _FilterDatabase (xls), stores the
	 * selected cells for auto-filter
	 */
	protected void importNamedRange() {
		for (int i = 0; i < workbook.getNumberOfNames(); i++) {
			Name definedName = workbook.getNameAt(i);
			if(skipName(definedName)){
				continue;
			}
			SName namedRange = null;
			if (definedName.getSheetIndex() == -1) {// workbook scope
				namedRange = book.createName(definedName.getNameName());
			} else {
				namedRange = book.createName(definedName.getNameName(), definedName.getSheetName());
			}
			namedRange.setRefersToFormula(definedName.getRefersToFormula());
		}
	}

	protected boolean skipName(Name definedName) {
		String namename = definedName.getNameName();
		if(namename==null){
			return true;
		}
		// ignore defined name of functions, they are macro functions that we don't support
		if (definedName.isFunctionName()){
			return true;
		}
		
		if(definedName.getRefersToFormula() == null) { // ignore defined name with null formula, don't know when will have this case
			return true;
		}
		
		return false;
	}

	/**
	 * Excel uses external book links to map external book index and name. The
	 * formula contains full external book name or index only (e.g [book2.xlsx]
	 * or [1]). We needs such table for parsing and evaluating formula when
	 * necessary.
	 */
	abstract protected void importExternalBookLinks();

	//ZSS-952
	protected void importSheetDefaultColumnWidth(Sheet poiSheet, SSheet sheet) {
		// reference XUtils.getDefaultColumnWidthInPx()
		int defaultWidth = UnitUtil.defaultColumnWidthToPx(poiSheet.getDefaultColumnWidth(), CHRACTER_WIDTH);
		sheet.setDefaultColumnWidth(defaultWidth);
	}
	
	/*
	 * import sheet scope content from POI Sheet.
	 */
	protected SSheet importSheet(Sheet poiSheet, int poiSheetIndex) {
		SSheet sheet = book.createSheet(poiSheet.getSheetName());
		sheet.setDefaultRowHeight(UnitUtil.twipToPx(poiSheet.getDefaultRowHeight()));
		//ZSS-952
		importSheetDefaultColumnWidth(poiSheet, sheet);
		// reference FreezeInfoLoaderImpl.getRowFreeze()
		sheet.getViewInfo().setNumOfRowFreeze(BookHelper.getRowFreeze(poiSheet));
		sheet.getViewInfo().setNumOfColumnFreeze(BookHelper.getColumnFreeze(poiSheet));
		sheet.getViewInfo().setDisplayGridlines(poiSheet.isDisplayGridlines()); // Note isDisplayGridlines() and isPrintGridlines() are different
		sheet.getViewInfo().setColumnBreaks(poiSheet.getColumnBreaks());
		sheet.getViewInfo().setRowBreaks(poiSheet.getRowBreaks());

		SPrintSetup sps= sheet.getPrintSetup();
		
		SHeader header = sheet.getViewInfo().getHeader();
		if (header != null) {
			header.setCenterText(poiSheet.getHeader().getCenter());
			header.setLeftText(poiSheet.getHeader().getLeft());
			header.setRightText(poiSheet.getHeader().getRight());
			sps.setHeader(header);
		}

		SFooter footer = sheet.getViewInfo().getFooter();
		if (footer != null) {
			footer.setCenterText(poiSheet.getFooter().getCenter());
			footer.setLeftText(poiSheet.getFooter().getLeft());
			footer.setRightText(poiSheet.getFooter().getRight());
			sps.setFooter(footer);
		}

		if (poiSheet.isDiffOddEven()) {
			Header poiEvenHeader = poiSheet.getEvenHeader();
			if (poiEvenHeader != null) {
				SHeader evenHeader = new HeaderFooterImpl();
				evenHeader.setCenterText(poiEvenHeader.getCenter());
				evenHeader.setLeftText(poiEvenHeader.getLeft());
				evenHeader.setRightText(poiEvenHeader.getRight());
				sps.setEvenHeader(evenHeader);
			}
			Footer poiEvenFooter = poiSheet.getEvenFooter();
			if (poiEvenFooter != null) {
				SFooter evenFooter = new HeaderFooterImpl();
				evenFooter.setCenterText(poiEvenFooter.getCenter());
				evenFooter.setLeftText(poiEvenFooter.getLeft());
				evenFooter.setRightText(poiEvenFooter.getRight());
				sps.setEvenFooter(evenFooter);
			}
		}
		
		if (poiSheet.isDiffFirst()) {
			Header poiFirstHeader = poiSheet.getFirstHeader();
			if (poiFirstHeader != null) {
				SHeader firstHeader = new HeaderFooterImpl();
				firstHeader.setCenterText(poiFirstHeader.getCenter());
				firstHeader.setLeftText(poiFirstHeader.getLeft());
				firstHeader.setRightText(poiFirstHeader.getRight());
				sps.setFirstHeader(firstHeader);
			}
			Footer poiFirstFooter = poiSheet.getFirstFooter();
			if (poiFirstFooter != null) {
				SFooter firstFooter = new HeaderFooterImpl();
				firstFooter.setCenterText(poiFirstFooter.getCenter());
				firstFooter.setLeftText(poiFirstFooter.getLeft());
				firstFooter.setRightText(poiFirstFooter.getRight());
				sps.setFirstFooter(firstFooter);
			}
		}

		PrintSetup poips = poiSheet.getPrintSetup();
		
		sps.setBottomMargin(poiSheet.getMargin(Sheet.BottomMargin));
		sps.setTopMargin(poiSheet.getMargin(Sheet.TopMargin));
		sps.setLeftMargin(poiSheet.getMargin(Sheet.LeftMargin));
		sps.setRightMargin(poiSheet.getMargin(Sheet.RightMargin));
		sps.setHeaderMargin(poiSheet.getMargin(Sheet.HeaderMargin));
		sps.setFooterMargin(poiSheet.getMargin(Sheet.FooterMargin));
		
		sps.setAlignWithMargins(poiSheet.isAlignMargins());
		sps.setErrorPrintMode(poips.getErrorsMode());
		sps.setFitHeight(poips.getFitHeight());
		sps.setFitWidth(poips.getFitWidth());
		sps.setHCenter(poiSheet.getHorizontallyCenter());
		sps.setLandscape(poips.getLandscape());
		sps.setLeftToRight(poips.getLeftToRight());
		sps.setPageStart(poips.getUsePage() ? poips.getPageStart() : 0);
		sps.setPaperSize(PoiEnumConversion.toPaperSize(poips.getPaperSize()));
		sps.setCommentsMode(poips.getCommentsMode());
		sps.setPrintGridlines(poiSheet.isPrintGridlines());
		sps.setPrintHeadings(poiSheet.isPrintHeadings());
		
		sps.setScale(poips.getScale());
		sps.setScaleWithDoc(poiSheet.isScaleWithDoc());
		sps.setDifferentOddEvenPage(poiSheet.isDiffOddEven());
		sps.setDifferentFirstPage(poiSheet.isDiffFirst());
		sps.setVCenter(poiSheet.getVerticallyCenter());
		
		Workbook poiBook = poiSheet.getWorkbook();
		String area = poiBook.getPrintArea(poiSheetIndex);
		if (area != null) {
			sps.setPrintArea(area);
		}

		CellRangeAddress rowrng = poiSheet.getRepeatingRows();
		if (rowrng != null) {
			sps.setRepeatingRowsTitle(rowrng.getFirstRow(), rowrng.getLastRow());
		}
		
		CellRangeAddress colrng = poiSheet.getRepeatingColumns();
		if (colrng != null) {
			sps.setRepeatingColumnsTitle(colrng.getFirstColumn(), colrng.getLastColumn());
		}
		
		sheet.setPassword(poiSheet.getProtect()?"":null);
		
		//import hashed password directly
		importPassword(poiSheet, sheet);
		
		//ZSS-832
		//import sheet visible
		if (poiBook.isSheetHidden(poiSheetIndex)) {
			sheet.setSheetVisible(SheetVisible.HIDDEN);
		} else if (poiBook.isSheetVeryHidden(poiSheetIndex)) {
			sheet.setSheetVisible(SheetVisible.VERY_HIDDEN);
		} else {
			sheet.setSheetVisible(SheetVisible.VISIBLE);
		}
		
		return sheet;
	}

	abstract protected void importPassword(Sheet poiSheet, SSheet sheet);
	
	protected void importMergedRegions(Sheet poiSheet, SSheet sheet) {
		// merged cells
		// reference RangeImpl.getMergeAreas()
		int nMerged = poiSheet.getNumMergedRegions();
		for (int i = nMerged - 1; i >= 0; --i) {
			final CellRangeAddress mergedRegion = poiSheet.getMergedRegion(i);
			sheet.addMergedRegion(new CellRegion(mergedRegion.getFirstRow(), mergedRegion.getFirstColumn(), mergedRegion.getLastRow(), mergedRegion.getLastColumn()));
		}
	}

	/**
	 * Drawings includes charts and pictures. 
	 */
	abstract protected void importDrawings(Sheet poiSheet, SSheet sheet);

	abstract protected void importValidation(Sheet poiSheet, SSheet sheet);

	protected SRow importRow(Row poiRow, SSheet sheet) {
		SRow row = sheet.getRow(poiRow.getRowNum());
		row.setHeight(UnitUtil.twipToPx(poiRow.getHeight()));
		row.setCustomHeight(poiRow.isCustomHeight());
		row.setHidden(poiRow.getZeroHeight());
		CellStyle rowStyle = poiRow.getRowStyle();
		if (rowStyle != null) {
			row.setCellStyle(importCellStyle(rowStyle));
		}

		for (Cell poiCell : poiRow) {
			importCell(poiCell, poiRow.getRowNum(), sheet);
		}

		return row;
	}

	protected SCell importCell(Cell poiCell, int row, SSheet sheet) {

		SCell cell = sheet.getCell(row, poiCell.getColumnIndex());
		cell.setCellStyle(importCellStyle(poiCell.getCellStyle()));

		switch (poiCell.getCellType()) {
		case Cell.CELL_TYPE_NUMERIC:
			cell.setNumberValue(poiCell.getNumericCellValue());
			break;
		case Cell.CELL_TYPE_STRING:
			RichTextString poiRichTextString = poiCell.getRichStringCellValue();
			if (poiRichTextString != null && poiRichTextString.numFormattingRuns() > 0) {
				SRichText richText = cell.setupRichTextValue();
				importRichText(poiCell, poiRichTextString, richText);
			} else {
				cell.setStringValue(poiCell.getStringCellValue());
			}
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			cell.setBooleanValue(poiCell.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_FORMULA:
			cell.setFormulaValue(poiCell.getCellFormula());
			//ZSS-873
			if (isImportCache() && !poiCell.isCalcOnLoad() && !mustCalc(cell)) {
				ValueEval val = null;
				switch(poiCell.getCachedFormulaResultType()) {
				case Cell.CELL_TYPE_NUMERIC:
					val = new NumberEval(poiCell.getNumericCellValue());
					break;
				case Cell.CELL_TYPE_STRING:
					RichTextString poiRichTextString0 = poiCell.getRichStringCellValue();
					if (poiRichTextString0 != null && poiRichTextString0.numFormattingRuns() > 0) {
						SRichText richText = new RichTextImpl();
						importRichText(poiCell, poiRichTextString0, richText);
						val = new StringEval(richText.getText());
					} else {
						val = new StringEval(poiCell.getStringCellValue());
					}
					break;
				case Cell.CELL_TYPE_BOOLEAN:
					val = BoolEval.valueOf(poiCell.getBooleanCellValue());
					break;
				case Cell.CELL_TYPE_ERROR:
					val = ErrorEval.valueOf(poiCell.getErrorCellValue());
					break;
				case Cell.CELL_TYPE_BLANK:
				default:
					// do nothing
				}
				if (val != null) {
					((AbstractCellAdv)cell).setFormulaResultValue(val);
				}
			}
			break;
		case Cell.CELL_TYPE_ERROR:
			cell.setErrorValue(PoiEnumConversion.toErrorCode(poiCell.getErrorCellValue()));
			break;
		case Cell.CELL_TYPE_BLANK:
			// do nothing because spreadsheet model auto creates blank cells
		default:
			// TODO log: leave an unknown cell type as a blank cell.
			break;

		}
		
		Hyperlink poiHyperlink = poiCell.getHyperlink();
		if (poiHyperlink != null) {
			String addr = poiHyperlink.getAddress();
			String label = poiHyperlink.getLabel();
			SHyperlink hyperlink = cell.setupHyperlink(PoiEnumConversion.toHyperlinkType(poiHyperlink.getType()),addr==null?"":addr,label==null?"":label);
			cell.setHyperlink(hyperlink);
		}
		
		Comment poiComment = poiCell.getCellComment();
		if(poiComment != null) {
			SComment comment = cell.setupComment();
			comment.setAuthor(poiComment.getAuthor());
			comment.setVisible(poiComment.isVisible());
			RichTextString poiRichTextString = poiComment.getString();
			if (poiRichTextString != null && poiRichTextString.numFormattingRuns() > 0) {			
				importRichText(poiCell, poiComment.getString(), comment.setupRichText());
			} else {
				comment.setText(poiComment.toString());
			}
		}

		return cell;
	}
	
	protected void importRichText(Cell poiCell, RichTextString poiRichTextString, SRichText richText) {
		String cellValue = poiRichTextString.getString();
		for (int i = 0; i < poiRichTextString.numFormattingRuns(); i++) {
			int nextFormattingRunIndex = (i + 1) >= poiRichTextString.numFormattingRuns() ? cellValue.length() : poiRichTextString.getIndexOfFormattingRun(i + 1);
			final String content = cellValue.substring(poiRichTextString.getIndexOfFormattingRun(i), nextFormattingRunIndex);
			richText.addSegment(content, toZssFont(getPoiFontFromRichText(workbook, poiCell, poiRichTextString, i)));
		}
	}

	/**
	 * Convert CellStyle into NCellStyle
	 * 
	 * @param poiCellStyle
	 */
	protected SCellStyle importCellStyle(CellStyle poiCellStyle) {
		return importCellStyle(poiCellStyle, true);
	}
	protected SCellStyle importCellStyle(CellStyle poiCellStyle, boolean inStyleTable) {
		SCellStyle cellStyle = null;
//		short idx = poiCellStyle.getIndex(); // ZSS-685
		if ((cellStyle = importedStyle.get(poiCellStyle)) == null) { // ZSS-685
			cellStyle = book.createCellStyle(inStyleTable);
			importedStyle.put(poiCellStyle, cellStyle); // ZSS-685
			String dataFormat = poiCellStyle.getRawDataFormatString();
			if(dataFormat==null){//just in case
				dataFormat = SCellStyle.FORMAT_GENERAL;
			}
			if(!poiCellStyle.isBuiltinDataFormat()){
				cellStyle.setDirectDataFormat(dataFormat);
			}else{
				cellStyle.setDataFormat(dataFormat);
			}
			cellStyle.setWrapText(poiCellStyle.getWrapText());
			cellStyle.setLocked(poiCellStyle.getLocked());
			cellStyle.setAlignment(PoiEnumConversion.toHorizontalAlignment(poiCellStyle.getAlignment()));
			cellStyle.setVerticalAlignment(PoiEnumConversion.toVerticalAlignment(poiCellStyle.getVerticalAlignment()));
			cellStyle.setRotation(poiCellStyle.getRotation()); //ZSS-918
			cellStyle.setIndention(poiCellStyle.getIndention()); //ZSS-915
			Color fgColor = poiCellStyle.getFillForegroundColorColor();
			Color bgColor = poiCellStyle.getFillBackgroundColorColor();
//			if (fgColor == null && bgColor != null) { //ZSS-797
//				fgColor = bgColor;
//			}
			//ZSS-857: SOLID pattern: switch fillColor and backColor 
			cellStyle.setFillPattern(PoiEnumConversion.toFillPattern(poiCellStyle.getFillPattern()));
			SColor fgSColor = book.createColor(BookHelper.colorToForegroundHTML(workbook, fgColor));
			SColor bgSColor = book.createColor(BookHelper.colorToBackgroundHTML(workbook, bgColor));
			if (cellStyle.getFillPattern() == FillPattern.SOLID) {
				SColor tmp = fgSColor;
				fgSColor = bgSColor;
				bgSColor = tmp;
			}
			cellStyle.setFillColor(fgSColor);
			cellStyle.setBackColor(bgSColor); //ZSS-780

			cellStyle.setBorderLeft(PoiEnumConversion.toBorderType(poiCellStyle.getBorderLeft()));
			cellStyle.setBorderTop(PoiEnumConversion.toBorderType(poiCellStyle.getBorderTop()));
			cellStyle.setBorderRight(PoiEnumConversion.toBorderType(poiCellStyle.getBorderRight()));
			cellStyle.setBorderBottom(PoiEnumConversion.toBorderType(poiCellStyle.getBorderBottom()));

			cellStyle.setBorderLeftColor(book.createColor(BookHelper.colorToBorderHTML(workbook, poiCellStyle.getLeftBorderColorColor())));
			cellStyle.setBorderTopColor(book.createColor(BookHelper.colorToBorderHTML(workbook, poiCellStyle.getTopBorderColorColor())));
			cellStyle.setBorderRightColor(book.createColor(BookHelper.colorToBorderHTML(workbook, poiCellStyle.getRightBorderColorColor())));
			cellStyle.setBorderBottomColor(book.createColor(BookHelper.colorToBorderHTML(workbook, poiCellStyle.getBottomBorderColorColor())));
			cellStyle.setHidden(poiCellStyle.getHidden());
			// same style always use same font
			cellStyle.setFont(importFont(poiCellStyle));
		}

		return cellStyle;
	}

	protected SFont importFont(CellStyle poiCellStyle) {
		SFont font = null;
		final short fontIndex = poiCellStyle.getFontIndex();
		if (importedFont.containsKey(fontIndex)) {
			font = importedFont.get(fontIndex);
		} else {
			Font poiFont = workbook.getFontAt(fontIndex);
			font = createZssFont(poiFont);
			importedFont.put(fontIndex, font); //ZSS-677
		}
		return font;
	}

	protected SFont toZssFont(Font poiFont) {
		SFont font = null;
		final short fontIndex = poiFont.getIndex();
		if (importedFont.containsKey(fontIndex)) {
			font = importedFont.get(fontIndex);
		} else {
			font = createZssFont(poiFont);
			importedFont.put(fontIndex, font); //ZSS-677
		}
		return font;
	}
	
	protected SFont createZssFont(Font poiFont) {
		SFont font = book.createFont(true);
		// font
		font.setName(poiFont.getFontName());
		font.setBoldweight(PoiEnumConversion.toBoldweight(poiFont.getBoldweight()));
		font.setItalic(poiFont.getItalic());
		font.setStrikeout(poiFont.getStrikeout());
		font.setUnderline(PoiEnumConversion.toUnderline(poiFont.getUnderline()));

		font.setHeightPoints(poiFont.getFontHeightInPoints());
		font.setTypeOffset(PoiEnumConversion.toTypeOffset(poiFont.getTypeOffset()));
		font.setColor(book.createColor(BookHelper.getFontHTMLColor(workbook, poiFont)));
		
		return font;
	}

	protected ViewAnchor toViewAnchor(Sheet poiSheet, ClientAnchor clientAnchor) {
		int width = getAnchorWidthInPx(clientAnchor, poiSheet);
		int height = getAnchorHeightInPx(clientAnchor, poiSheet);
		ViewAnchor viewAnchor = new ViewAnchor(clientAnchor.getRow1(), clientAnchor.getCol1(), width, height);
		viewAnchor.setXOffset(getXoffsetInPixel(clientAnchor, poiSheet));
		viewAnchor.setYOffset(getYoffsetInPixel(clientAnchor, poiSheet));
		return viewAnchor;
	}

	abstract protected int getXoffsetInPixel(ClientAnchor clientAnchor, Sheet poiSheet);

	abstract protected int getYoffsetInPixel(ClientAnchor clientAnchor, Sheet poiSheet);

	protected void importPicture(List<Picture> poiPictures, Sheet poiSheet, SSheet sheet) {
		for (Picture poiPicture : poiPictures) {
			PictureData poiPicData = poiPicture.getPictureData();
			Integer picDataIx = importedPictureData.get(poiPicData); //ZSS-735
			if (picDataIx != null) {
				sheet.addPicture(picDataIx.intValue(), toViewAnchor(poiSheet, poiPicture.getClientAnchor()));
			} else {
				Format format = Format.valueOfFileExtension(poiPicData.suggestFileExtension());
				if (format != null) {
					SPicture pic = sheet.addPicture(format, poiPicData.getData(), toViewAnchor(poiSheet, poiPicture.getClientAnchor()));
					importedPictureData.put(poiPicData, pic.getPictureData().getIndex());
				} else {
					// TODO log we ignore a picture with unsupported format
				}
			}
		}
	}

	/**
	 * POI AutoFilter.getFilterColumn(i) sometimes returns null. A POI FilterColumn object only 
	 * exists when we have set a criteria on that column. 
	 * For example, if we enable auto filter on 2 columns, but we only set criteria on 
	 * 2nd column. Thus, the size of filter column is 1. There is only one FilterColumn 
	 * object and its column id is 1. Only getFilterColumn(1) will return a FilterColumn, 
	 * other get null.
	 * 
	 * @param poiSheet source POI sheet
	 * @param sheet destination sheet
	 */
	private void importAutoFilter(Sheet poiSheet, SSheet sheet) {
		AutoFilter poiAutoFilter = poiSheet.getAutoFilter();
		if (poiAutoFilter != null) {
			CellRangeAddress filteringRange = poiAutoFilter.getRangeAddress();
			SAutoFilter autoFilter = sheet.createAutoFilter(new CellRegion(filteringRange.formatAsString()));
			int numberOfColumn = filteringRange.getLastColumn() - filteringRange.getFirstColumn() + 1;
			importAutoFilterColumns(poiAutoFilter, autoFilter, numberOfColumn); //ZSS-1019
		}
	}
	
	//ZSS-1019
	protected void importAutoFilterColumns(AutoFilter poiFilter, SAutoFilter zssFilter, int numberOfColumn) {
		for (int i = 0; i < numberOfColumn; i++) {
			FilterColumn srcColumn = poiFilter.getFilterColumn(i);
			if (srcColumn == null) {
				continue;
			}
			NFilterColumn destColumn = zssFilter.getFilterColumn(i, true);
			destColumn.setProperties(PoiEnumConversion.toFilterOperator(srcColumn.getOperator()), srcColumn.getCriteria1(), srcColumn.getCriteria2(), srcColumn.isOn());
		}
	}

	private org.zkoss.poi.ss.usermodel.Font getPoiFontFromRichText(Workbook book,
			Cell cell, RichTextString rstr, int run) {
		org.zkoss.poi.ss.usermodel.Font font = rstr instanceof HSSFRichTextString ? book.getFontAt(((HSSFRichTextString) rstr).getFontOfFormattingRun(run)) : ((XSSFRichTextString) rstr)
				.getFontOfFormattingRun((XSSFWorkbook)book, run);
		if (font == null) {
			CellStyle style = cell.getCellStyle();
			short fontIndex = style != null ? style.getFontIndex() : (short) 0;
			return book.getFontAt(fontIndex); 
		}
		return font;
	}

	/**
	 * POI SheetProtection.
	 * @param poiSheet source POI sheet
	 * @param sheet destination sheet
	 */
	abstract protected void importSheetProtection(Sheet poiSheet, SSheet sheet); //ZSS-576

	/**
	 * POI sheet tables
	 * @param poiSheet source POI sheet
	 * @param sheet destination sheet
	 * @since 3.8.0
	 */
	abstract protected void importTables(Sheet poiSheet, SSheet sheet); //ZSS-855
	
	//ZSS-873: Import formula cache result from an Excel file
	private boolean _importCache = false;
	/**
	 * Set if import Excel cached value.
	 * @since 3.7.0
	 */
	public void setImportCache(boolean b) {
		_importCache = b;
	}
	/**
	 * Returns if import file cached value.
	 * @since 3.7.0
	 */
	protected boolean isImportCache() {
		return _importCache;
	}
	
	//ZSS-873
	//Must evaluate INDIRECT() function to make the dependency table)
	//Issue845Test-checkIndirectNameRange
	private boolean mustCalc(SCell cell) {
		FormulaExpression val = ((AbstractCellAdv)cell).getFormulaExpression();
		for (Ptg ptg : val.getPtgs()) {
			if (ptg instanceof FuncVarPtg && ((FuncVarPtg)ptg).getFunctionIndex() == 148) { //148 is INDIRECT
				return true;
			}
		}
		return false;
	}
}
