/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/18 , Created by Hawk
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
 */
package org.zkoss.zss.range.impl.imexp;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorder;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFill;
import org.zkoss.poi.ss.usermodel.*;
import org.zkoss.poi.ss.util.CellRangeAddress;
import org.zkoss.poi.xssf.model.StylesTable;
import org.zkoss.poi.xssf.usermodel.XSSFFont;
import org.zkoss.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.zkoss.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.zkoss.poi.xssf.usermodel.XSSFWorkbook;
import org.zkoss.util.logging.Log;
import org.zkoss.zss.model.*;
import org.zkoss.zss.model.SCell.CellType;
import org.zkoss.zss.model.SFill.FillPattern;
import org.zkoss.zss.model.SRichText.Segment;
import org.zkoss.zss.model.impl.HeaderFooterImpl;
import org.zkoss.zss.model.impl.TableNameImpl;
import org.zkoss.zss.model.impl.sys.formula.FormulaEngineImpl;
import org.zkoss.zss.model.util.Strings;

/**
 * Common exporting behavior for both XLSX and XLS.
 * 
 * @author kuro, Hawk
 * @since 3.5.0
 */
abstract public class AbstractExcelExporter extends AbstractExporter {
	//Though there is customHeight defined in <sheetFormatPr>. However, it is 
	//  only for empty rows; not rows with cells. For rows with cells, still 
	//  have to specifies its height or it will use application's default row 
	//  height instead.
	private static final int DEFAULT_ROW_HEIGHT = 20; //ZSS-1012

	/**
	 * Exporting destination, POI book model
	 */
	protected Workbook workbook;
	/**
	 * The map stores the exported {@link CellStyle} during exporting, so that
	 * we can reuse them for exporting other cells.
	 */
	protected Map<SCellStyle, CellStyle> styleTable = new HashMap<SCellStyle, CellStyle>();
	protected Map<SFont, Font> fontTable = new HashMap<SFont, Font>();
	protected Map<SColor, Color> colorTable = new HashMap<SColor, Color>();
	//ZSS-688: SPictureData index -> poi PictureData index
	protected Map<Integer, Integer> exportedPicDataMap = new HashMap<Integer, Integer>(); 
	
	private static final Log _logger = Log.lookup(AbstractExcelExporter.class.getName());

	abstract protected void exportColumnArray(SSheet sheet, Sheet poiSheet, SColumnArray columnArr);

	abstract protected Workbook createPoiBook();

	abstract protected void exportChart(SSheet sheet, Sheet poiSheet);

	abstract protected void exportPicture(SSheet sheet, Sheet poiSheet);

	abstract protected void exportValidation(SSheet sheet, Sheet poiSheet);

	abstract protected void exportAutoFilter(SSheet sheet, Sheet poiSheet);

	abstract protected void exportPassword(SSheet sheet, Sheet poiSheet);
	
	abstract protected int exportTables(SSheet sheet, Sheet poiSheet, int tbId); //ZSS-855
	/**
	 * Export the model according to reversed depended order: book, sheet,
	 * defined name, cells, chart, pictures, validation. Because named ranges
	 * (defined names) require sheet index, they should be imported after sheets
	 * created. Besides, cells, charts, and validations may have formulas
	 * referring to named ranges, they must be imported after named ranged.
	 * Pictures depend on cells.
	 */
	@Override
	public void export(SBook book, OutputStream fos) throws IOException {
		ReadWriteLock lock = book.getBookSeries().getLock();
		lock.readLock().lock();

		try {
			// clear cache for reuse
			styleTable.clear();
			fontTable.clear();
			colorTable.clear();
			
			workbook = createPoiBook();
			//ZSS-854: export default cell styles
			workbook.clearDefaultCellStyles();
			for (SCellStyle style: book.getDefaultCellStyles()) {
				CellStyle cellStyle = toPOIDefaultCellStyle(style); // put the default cellStyle
				workbook.addDefaultCellStyle(cellStyle);
				toPOICellStyle(style); //ZSS-1027: put the default cellStyle into cellXfs
			}
			//ZSS-854: export named cell styles
			workbook.clearNamedStyles();
			for (SNamedStyle style: book.getNamedStyles()) {
				NamedStyle  poiStyle = toPOINamedStyle(style); // put the named cellStyle
				workbook.addNamedStyle(poiStyle);
			}
			int tbId = 0;
			for (int n = 0; n < book.getSheets().size(); n++) {
				SSheet sheet = book.getSheet(n);
				exportSheet(sheet);
				Sheet poiSheet = workbook.getSheetAt(n);
				tbId = exportTables(sheet, poiSheet, tbId); //ZSS-855, ZSS-1011
			}
			exportNamedRange(book);
			exportPictureData(book); //ZSS-735
			for (int n = 0; n < book.getSheets().size(); n++) {
				SSheet sheet = book.getSheet(n);
				Sheet poiSheet = workbook.getSheetAt(n);
				exportRowColumn(sheet, poiSheet);
				exportMergedRegions(sheet, poiSheet);
				exportChart(sheet, poiSheet);
				exportPicture(sheet, poiSheet);
				exportValidation(sheet, poiSheet);
				exportAutoFilter(sheet, poiSheet);
			}

			workbook.write(fos);
		} finally {
			lock.readLock().unlock();
		}
	}
	
	//ZSS-735
	protected void exportPictureData(SBook book) {
		for (SPictureData picData : book.getPicturesDatas()) {
			int poiIndex = workbook.addPicture(picData.getData(), PoiEnumConversion.toPoiPictureFormat(picData.getFormat()));
			exportedPicDataMap.put(picData.getIndex(), poiIndex);
		}
	}

	protected void exportNamedRange(SBook book) {
		for (SName name : book.getNames()) {
			if (name instanceof TableNameImpl) continue; //ZSS-855: skip Name associated with Table
			Name poiName = workbook.createName();
			try{
				String sheetName = name.getApplyToSheetName();//ZSS-699
				if(sheetName!=null){
					poiName.setSheetIndex(workbook.getSheetIndex(sheetName));
				}
				poiName.setNameName(name.getName()); //ZSS-699: execute after setSheetIndex() to avoid check duplicate name issue
				//zss-214, to tolerate the name refers to formula error (#REF!!$A$1:$I$18)
				if(!name.isFormulaParsingError()){
					poiName.setRefersToFormula(name.getRefersToFormula());
				}
			}catch (Exception e) {
				//ZSS-645 catch the exception happens when a book has a named range referring to an external book in XLS
				_logger.warning("Cannot export a name range: "+name.getName(),e);
				if (poiName.getNameName()!=null){
					workbook.removeName(poiName.getNameName());
				}
			}
			
		}
	}

	protected void exportSheet(SSheet sheet) {
		Sheet poiSheet = workbook.createSheet(sheet.getSheetName());

		// refer to BookHelper#setFreezePanel
		int freezeRow = sheet.getViewInfo().getNumOfRowFreeze();
		int freezeCol = sheet.getViewInfo().getNumOfColumnFreeze();
		poiSheet.createFreezePane(freezeCol <= 0 ? 0 : freezeCol, freezeRow <= 0 ? 0 : freezeRow);

		poiSheet.setDisplayGridlines(sheet.getViewInfo().isDisplayGridlines());

		exportSheetProtection(sheet, poiSheet); //ZSS-576
		if (sheet.isProtected()) {
			poiSheet.protectSheet(""); // without password; set hashed password directly later
			exportPassword(sheet, poiSheet);
		} else {
			//ZSS-679
			poiSheet.protectSheet(null);
		}

		poiSheet.setDefaultRowHeight((short) UnitUtil.pxToTwip(sheet.getDefaultRowHeight()));
		poiSheet.setDefaultColumnWidth((int) UnitUtil.pxToDefaultColumnWidth(sheet.getDefaultColumnWidth(), AbstractExcelImporter.CHRACTER_WIDTH));

		// Header
		Header header = poiSheet.getHeader();
		header.setLeft(sheet.getViewInfo().getHeader().getLeftText());
		header.setCenter(sheet.getViewInfo().getHeader().getCenterText());
		header.setRight(sheet.getViewInfo().getHeader().getRightText());

		// Footer
		Footer footer = poiSheet.getFooter();
		footer.setLeft(sheet.getViewInfo().getFooter().getLeftText());
		footer.setCenter(sheet.getViewInfo().getFooter().getCenterText());
		footer.setRight(sheet.getViewInfo().getFooter().getRightText());

		SPrintSetup sps = sheet.getPrintSetup();
		
		// Margin
		poiSheet.setMargin(Sheet.LeftMargin, sps.getLeftMargin());
		poiSheet.setMargin(Sheet.RightMargin, sps.getRightMargin());
		poiSheet.setMargin(Sheet.TopMargin, sps.getTopMargin());
		poiSheet.setMargin(Sheet.BottomMargin, sps.getBottomMargin());
		poiSheet.setMargin(Sheet.HeaderMargin, sps.getHeaderMargin());
		poiSheet.setMargin(Sheet.FooterMargin, sps.getFooterMargin());

		// Print Setup Information
		PrintSetup poips = poiSheet.getPrintSetup();
		if (sps.isDifferentOddEvenPage()) {
			SHeader evenHeader = sps.getEvenHeader();
			if (evenHeader != null) {
				Header poiEvenHeader = poiSheet.getEvenHeader();
				poiEvenHeader.setCenter(evenHeader.getCenterText());
				poiEvenHeader.setLeft(evenHeader.getLeftText());
				poiEvenHeader.setRight(evenHeader.getRightText());
			}
			SFooter evenFooter = sps.getEvenFooter();
			if (evenFooter != null) {
				Footer poiEvenFooter = poiSheet.getEvenFooter();
				poiEvenFooter.setCenter(evenFooter.getCenterText());
				poiEvenFooter.setLeft(evenFooter.getLeftText());
				poiEvenFooter.setRight(evenFooter.getRightText());
			}
		}
		if (sps.isDifferentFirstPage()) {
			SHeader firstHeader = sps.getFirstHeader();
			if (firstHeader != null) {
				Header poiFirstHeader = poiSheet.getFirstHeader();
				poiFirstHeader.setCenter(firstHeader.getCenterText());
				poiFirstHeader.setLeft(firstHeader.getLeftText());
				poiFirstHeader.setRight(firstHeader.getRightText());
			}
			SFooter firstFooter = sps.getFirstFooter();
			if (firstFooter != null) {
				Footer poiFirstFooter = poiSheet.getFirstFooter();
				poiFirstFooter.setCenter(firstFooter.getCenterText());
				poiFirstFooter.setLeft(firstFooter.getLeftText());
				poiFirstFooter.setRight(firstFooter.getRightText());
			}
		}

		poiSheet.setAlignMargins(sps.isAlignWithMargins());
		poips.setErrorsMode(sps.getErrorPrintMode());

		poips.setFitHeight((short) sps.getFitHeight());
		poips.setFitWidth((short) sps.getFitWidth());
		
		poiSheet.setHorizontallyCenter(sps.isHCenter());
		poiSheet.setVerticallyCenter(sps.isVCenter());
		poips.setLandscape(sps.isLandscape());
		poips.setLeftToRight(sps.isLeftToRight());

		int pageStart = sps.getPageStart();
		poips.setUsePage(pageStart > 0);
		poips.setPageStart((short) (pageStart > 0 ? pageStart : 0));
		poips.setPaperSize(PoiEnumConversion.toPoiPaperSize(sps.getPaperSize()));
		poips.setCommentsMode(sps.getCommentsMode());
		poiSheet.setPrintGridlines(sps.isPrintGridlines());
		poiSheet.setPrintHeadings(sps.isPrintHeadings());
		poips.setScale((short)sps.getScale());
		poiSheet.setScalWithDoc(sps.isScaleWithDoc());
		poiSheet.setDiffOddEven(sps.isDifferentOddEvenPage());
		poiSheet.setDiffFirst(sps.isDifferentFirstPage());
		
		int sheetIndex = workbook.getNumberOfSheets() - 1;
		String area = sps.getPrintArea();
		if (!Strings.isEmpty(area)) {
			workbook.setPrintArea(sheetIndex, area);
		}

		final CellRegion rgn = sps.getRepeatingRowsTitle();
		if (rgn != null) {
			CellRangeAddress rowrng = new CellRangeAddress(rgn.getRow(), rgn.getLastRow(), -1, -1);
			poiSheet.setRepeatingRows(rowrng);
		}
		
		final CellRegion crgn= sps.getRepeatingColumnsTitle();
		if (crgn != null) {
			CellRangeAddress colrng = new CellRangeAddress(-1, -1, crgn.getColumn(), crgn.getLastColumn());
			poiSheet.setRepeatingColumns(colrng);
		}
		
		//ZSS-832
		// export sheet visible
		int option = 0;
		switch(sheet.getSheetVisible()) {
		default:
		case VISIBLE:
			option = 0;
			break;
		case HIDDEN:
			option = 1;
			break;
		case VERY_HIDDEN:
			option = 2;
			break;
		}
	    workbook.setSheetHidden(sheetIndex, option);
	}

	protected void exportMergedRegions(SSheet sheet, Sheet poiSheet) {
		// consistent with importer, read from last merged region
		for (int i = sheet.getNumOfMergedRegion() - 1; i >= 0; i--) {
			CellRegion region = sheet.getMergedRegion(i);
			poiSheet.addMergedRegion(new CellRangeAddress(region.row, region.lastRow, region.column, region.lastColumn));
		}
	}

	protected void exportRowColumn(SSheet sheet, Sheet poiSheet) {
		// export rows
		Iterator<SRow> rowIterator = sheet.getRowIterator();
		while (rowIterator.hasNext()) {
			SRow row = rowIterator.next();
			exportRow(sheet, poiSheet, row);
		}

		// export columns
		Iterator<SColumnArray> columnArrayIterator = sheet.getColumnArrayIterator();
		while (columnArrayIterator.hasNext()) {
			SColumnArray columnArr = columnArrayIterator.next();
			exportColumnArray(sheet, poiSheet, columnArr);
		}
	}

	protected void exportRow(SSheet sheet, Sheet poiSheet, SRow row) {
		Row poiRow = poiSheet.createRow(row.getIndex());

		if (row.isHidden()) {
			// hidden, set height as 0
			poiRow.setZeroHeight(true);
		} else {
			// not hidden, calculate height
			//ZSS-1012
			// We export both customHeight and Height when customHeight is set.
			// otherwise, we may export only height value if height is not equal to DEFAULT_ROW_HEIGHT
			if (row.isCustomHeight()) {
				poiRow.setCustomHeight(true);
				poiRow.setHeight((short) UnitUtil.pxToTwip(row.getHeight()));
			} else if (row.getHeight() != DEFAULT_ROW_HEIGHT) {
				poiRow.setHeight((short) UnitUtil.pxToTwip(row.getHeight()));
			}
		}

		SCellStyle rowStyle = row.getCellStyle();
		CellStyle poiRowStyle = toPOICellStyle(rowStyle);
		poiRow.setRowStyle(poiRowStyle);

		// Export Cell
		Iterator<SCell> cellIterator = sheet.getCellIterator(row.getIndex());
		while (cellIterator.hasNext()) {
			SCell cell = cellIterator.next();
			exportCell(poiRow, cell);
		}
	}

	protected void exportCell(Row poiRow, SCell cell) {
		Cell poiCell = poiRow.createCell(cell.getColumnIndex());

		SCellStyle cellStyle = cell.getCellStyle();
		poiCell.setCellStyle(toPOICellStyle(cellStyle));

		switch (cell.getType()) {
		case BLANK:
			poiCell.setCellType(Cell.CELL_TYPE_BLANK);
			break;
		case ERROR:
			//ignore the value of this cell, excel doesn't allow it invalid formula (pasring error).
			if(cell.getErrorValue().getCode() != ErrorValue.INVALID_FORMULA){
				poiCell.setCellType(Cell.CELL_TYPE_ERROR);
				poiCell.setCellErrorValue(cell.getErrorValue().getCode());
			}
			break;
		case BOOLEAN:
			poiCell.setCellType(Cell.CELL_TYPE_BOOLEAN);
			poiCell.setCellValue(cell.getBooleanValue());
			break;
		case FORMULA:
			if(cell.getFormulaResultType()==CellType.ERROR && cell.getErrorValue().getCode() == ErrorValue.INVALID_FORMULA){//ZSS-891
				//ignore the value of this cell, excel doesn't allow it invalid formula (pasring error).
			}else{
				poiCell.setCellType(Cell.CELL_TYPE_FORMULA);
				poiCell.setCellFormula(cell.getFormulaValue());
				//ZSS-873
				if (isExportCache()) {
					switch(cell.getFormulaResultType()) {
					default:
					case BLANK:
						break;
					case BOOLEAN:
						poiCell.setCellValue(cell.getBooleanValue());
						break;
					case ERROR:
						poiCell.setCellErrorValue(cell.getErrorValue().getCode());
						break;
					case NUMBER:
						poiCell.setCellValue((Double) cell.getNumberValue());
						break;
					case STRING:
						if(cell.isRichTextValue()) {
							poiCell.setCellValue(toPOIRichText(cell.getRichTextValue()));
						} else {
							poiCell.setCellValue(cell.getStringValue());
						}
					}
				}
			}
			break;
		case NUMBER:
			poiCell.setCellType(Cell.CELL_TYPE_NUMERIC);
			poiCell.setCellValue((Double) cell.getNumberValue());
			break;
		case STRING:
			poiCell.setCellType(Cell.CELL_TYPE_STRING);
			if(cell.isRichTextValue()) {
				poiCell.setCellValue(toPOIRichText(cell.getRichTextValue()));
			} else {
				poiCell.setCellValue(cell.getStringValue());
			}
			break;
		default:
		}
		
		SHyperlink hyperlink = cell.getHyperlink();
		if (hyperlink != null) {
			CreationHelper helper = workbook.getCreationHelper();
			try{
				Hyperlink poiHyperlink = helper.createHyperlink(PoiEnumConversion.toPoiHyperlinkType(hyperlink.getType()));
				poiHyperlink.setAddress(hyperlink.getAddress());
				poiHyperlink.setLabel(hyperlink.getLabel());
				poiCell.setHyperlink(poiHyperlink);
			}catch (Exception e) {
				//ZSS-644 catch the exception happens when a hyperlink has an invalid URI in XLSX
				_logger.warning("Cannot export a hyperlink: "+hyperlink.getAddress(),e);
			}
			
		}
		
		SComment comment = cell.getComment();
		if (comment != null) {
			// Refer to the POI Official Tutorial
			// http://poi.apache.org/spreadsheet/quick-guide.html#CellComments
			CreationHelper helper = workbook.getCreationHelper();
			Drawing drawing = poiCell.getSheet().createDrawingPatriarch();
			ClientAnchor anchor = helper.createClientAnchor();
			anchor.setCol1(poiCell.getColumnIndex());
			anchor.setCol2(poiCell.getColumnIndex() + 1);
			anchor.setRow1(poiRow.getRowNum());
			anchor.setRow2(poiRow.getRowNum() + 3);
			Comment poiComment = drawing.createCellComment(anchor);
			SRichText richText = comment.getRichText();
			if (richText != null) {
				poiComment.setString(toPOIRichText(richText));
			} else {
				poiComment.setString(helper.createRichTextString(comment.getText()));
			}
			poiComment.setAuthor(comment.getAuthor());
			poiComment.setVisible(comment.isVisible());
			poiCell.setCellComment(poiComment);
		}	
	}
	
	protected RichTextString toPOIRichText(SRichText richText) {

		CreationHelper helper = workbook.getCreationHelper();

		RichTextString poiRichTextString = helper.createRichTextString(richText.getText());

		int start = 0;
		int end = 0;
		for (Segment sg : richText.getSegments()) {
			SFont font = sg.getFont();
			int len = sg.getText().length();
			end += len;
			poiRichTextString.applyFont(start, end, toPOIFont(font));
			start += len;
		}

		return poiRichTextString;
	}
	//ZSS-854
	protected NamedStyle toPOINamedStyle(SNamedStyle cellStyle) {
		return workbook.createNamedStyle(cellStyle.getName(), cellStyle.isCustomBuiltin(), cellStyle.getBuiltinId(), cellStyle.getIndex());
	}
	
	//ZSS-854
	protected CellStyle toPOIDefaultCellStyle(SCellStyle cellStyle) {
		return null;
	}

	protected CellStyle toPOICellStyle(SCellStyle cellStyle) {
		// instead of creating a new style, use old one if exist
		CellStyle poiCellStyle = styleTable.get(cellStyle);
		if (poiCellStyle != null) {
			return poiCellStyle;
		}
		poiCellStyle = workbook.createCellStyle();

		//set Border
		short bottom = PoiEnumConversion.toPoiBorderType(cellStyle.getBorderBottom());
		short left = PoiEnumConversion.toPoiBorderType(cellStyle.getBorderLeft());
		short right = PoiEnumConversion.toPoiBorderType(cellStyle.getBorderRight());
		short top = PoiEnumConversion.toPoiBorderType(cellStyle.getBorderTop());
		Color bottomColor = toPOIColor(cellStyle.getBorderBottomColor());
		Color leftColor = toPOIColor(cellStyle.getBorderLeftColor());
		Color rightColor = toPOIColor(cellStyle.getBorderRightColor());
		Color topColor = toPOIColor(cellStyle.getBorderTopColor());
		poiCellStyle.setBorder(left, leftColor, top, topColor, right, rightColor, bottom, bottomColor);

		// fill
		//ZSS-857: SOLID pattern; switch fgColor and bgColor 
		SColor fgColor = cellStyle.getFillColor();
		SColor bgColor = cellStyle.getBackColor();
		if (cellStyle.getFillPattern() == FillPattern.SOLID) {
			SColor tmp = fgColor;
			fgColor = bgColor;
			bgColor = tmp;
		}
		Color fillColor = toPOIColor(fgColor);
		Color backColor = toPOIColor(bgColor);
		short pattern = PoiEnumConversion.toPoiFillPattern(cellStyle.getFillPattern());
		poiCellStyle.setFill(fillColor, backColor, pattern);
		
		//cell Alignment
		short hAlign = PoiEnumConversion.toPoiHorizontalAlignment(cellStyle.getAlignment());
		short vAlign = PoiEnumConversion.toPoiVerticalAlignment(cellStyle.getVerticalAlignment());
		boolean wrapText = cellStyle.isWrapText();
		
		//ZSS-1020
		poiCellStyle.setCellAlignment(hAlign, vAlign, wrapText, (short) cellStyle.getRotation());
		
		//protect
		boolean locked = cellStyle.isLocked();
		boolean hidden = cellStyle.isHidden();
		poiCellStyle.setProtection(locked, hidden);

		// refer from BookHelper#setDataFormat
		DataFormat df = workbook.createDataFormat();
		short fmt = df.getFormat(cellStyle.getDataFormat());
		poiCellStyle.setDataFormat(fmt);

		// font
		poiCellStyle.setFont(toPOIFont(cellStyle.getFont()));

		// put into table
		styleTable.put(cellStyle, poiCellStyle);
		
		int indention = cellStyle.getIndention();
		if (indention > 0) 
			poiCellStyle.setIndention((short) indention);

		return poiCellStyle;

	}

	protected Color toPOIColor(SColor color) {
		Color poiColor = colorTable.get(color);

		if (poiColor != null) {
			return poiColor;
		}
		poiColor = BookHelper.HTMLToColor(workbook, color.getHtmlColor());
		colorTable.put(color, poiColor);
		return poiColor;
	}

	/**
	 * Convert ZSS Font into POI Font. Cache font in the fontTable. If font
	 * exist, don't create a new one.
	 * 
	 * @param font
	 * @return
	 */
	protected Font toPOIFont(SFont font) {

		Font poiFont = fontTable.get(font);
		if (poiFont != null) {
			return poiFont;
		}

		poiFont = workbook.createFont();
		poiFont.setBoldweight(PoiEnumConversion.toPoiBoldweight(font.getBoldweight()));
		poiFont.setStrikeout(font.isStrikeout());
		poiFont.setItalic(font.isItalic());
		BookHelper.setFontColor(workbook, poiFont, toPOIColor(font.getColor()));
		poiFont.setFontHeightInPoints((short) font.getHeightPoints());
		poiFont.setFontName(font.getName());
		poiFont.setTypeOffset(PoiEnumConversion.toPoiTypeOffset(font.getTypeOffset()));
		poiFont.setUnderline(PoiEnumConversion.toPoiUnderline(font.getUnderline()));

		// put into table
		fontTable.put(font, poiFont);

		return poiFont;
	}

	/**
	 * POI SheetProtection.
	 * @param sheet destination sheet
	 * @param poiSheet source POI sheet
	 */
	private void exportSheetProtection(SSheet sheet, Sheet poiSheet) { //ZSS-576
		SSheetProtection ssp = sheet.getSheetProtection();
		SheetProtection sp = poiSheet.getOrCreateSheetProtection();
		
	    sp.setAutoFilter(ssp.isAutoFilter());
	    sp.setDeleteColumns(ssp.isDeleteColumns());
	    sp.setDeleteRows(ssp.isDeleteRows());
	    sp.setFormatCells(ssp.isFormatCells());
	    sp.setFormatColumns(ssp.isFormatColumns());
	    sp.setFormatRows(ssp.isFormatRows());
	    sp.setInsertColumns(ssp.isInsertColumns());
	    sp.setInsertHyperlinks(ssp.isInsertHyperlinks());
	    sp.setInsertRows(ssp.isInsertRows());
	    sp.setPivotTables(ssp.isPivotTables());
	    sp.setSort(ssp.isSort());
	    sp.setObjects(ssp.isObjects());
	    sp.setScenarios(ssp.isScenarios());
	    sp.setSelectLockedCells(ssp.isSelectLockedCells());
	    sp.setSelectUnlockedCells(ssp.isSelectUnlockedCells());
	}
	
	//ZSS-873: Import formula cache result from an Excel file
	private boolean _exportCache = false;
	/**
	 * Set whether export cached value into excel file(must be called before
	 * export() is called.
	 * @param b
	 * @since 3.7.0
	 */
	public void setExportCache(boolean b) {
		_exportCache = b;
	}
	/**
	 * Returns whether export cached value into excel file.
	 * @return
	 * @since 3.7.0
	 */
	protected boolean isExportCache() {
		return _exportCache;
	}
}
