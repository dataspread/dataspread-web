/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/01 , Created by Hawk
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.range.impl.imexp;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;

import org.zkoss.poi.ss.usermodel.*;
import org.zkoss.poi.ss.usermodel.charts.*;
import org.zkoss.poi.ss.util.*;
import org.zkoss.poi.xssf.usermodel.*;
import org.zkoss.poi.xssf.usermodel.XSSFAutoFilter.XSSFFilterColumn;
import org.zkoss.poi.xssf.usermodel.XSSFTableColumn.TotalsRowFunction;
import org.zkoss.poi.xssf.usermodel.charts.*;
import org.zkoss.poi.xssf.usermodel.extensions.XSSFCellBorder;
import org.zkoss.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.zkoss.zss.model.*;
import org.zkoss.zss.model.SAutoFilter.NFilterColumn;
import org.zkoss.zss.model.SFill.FillPattern;
import org.zkoss.zss.model.STableColumn.STotalsRowFunction;
import org.zkoss.zss.model.SDataValidation.ValidationType;
import org.zkoss.zss.model.chart.*;
import org.zkoss.zss.model.impl.AbstractDataValidationAdv;
import org.zkoss.zss.model.impl.SheetImpl;
/**
 * 
 * @author dennis, kuro, Hawk
 * @since 3.5.0
 */
public class ExcelXlsxExporter extends AbstractExcelExporter {
	private static final long serialVersionUID = 20141231175402L;

	protected void exportColumnArray(SSheet sheet, Sheet poiSheet, SColumnArray columnArr) {
		XSSFSheet xssfSheet = (XSSFSheet) poiSheet;
		
        CTWorksheet ctSheet = xssfSheet.getCTWorksheet();
    	if(xssfSheet.getCTWorksheet().sizeOfColsArray() == 0) {
    		xssfSheet.getCTWorksheet().addNewCols();
    	}
    		
    	CTCol col = ctSheet.getColsArray(0).addNewCol();
        col.setMin(columnArr.getIndex()+1);
        col.setMax(columnArr.getLastIndex()+1);
    	col.setStyle(toPOICellStyle(columnArr.getCellStyle()).getIndex());
    	col.setCustomWidth(true);
    	col.setWidth(UnitUtil.pxToCTChar(columnArr.getWidth(), AbstractExcelImporter.CHRACTER_WIDTH));
    	col.setHidden(columnArr.isHidden());
	}

	@Override
	protected Workbook createPoiBook() {
		return new XSSFWorkbook();
	}

	/**
	 * reference DrawingManagerImpl.addChartX()
	 */
	@Override
	protected void exportChart(SSheet sheet, Sheet poiSheet) {
		for (SChart chart: sheet.getCharts()){
			ChartData chartData = fillPoiChartData(chart);
			if (chartData != null){ //an unsupported chart has null chart data
				plotPoiChart(chart, chartData, sheet, poiSheet );
			}
		}
	}

	/**
	 * Reference DrawingManagerImpl.addPicture()
	 */
	@Override
	protected void exportPicture(SSheet sheet, Sheet poiSheet) {
		for (SPicture picture : sheet.getPictures()){
			int poiPictureIndex = exportedPicDataMap.get(picture.getPictureData().getIndex()); //ZSS-735
			poiSheet.createDrawingPatriarch().createPicture(toClientAnchor(picture.getAnchor(), sheet), poiPictureIndex);
		}
	}
	
	/**
	 * 
	 * @param chart
	 * @return a POI ChartData filled with Spreadsheet chart data, or null if the chart type is unsupported.   
	 */
	private ChartData fillPoiChartData(SChart chart) {
		CategoryData categoryData = null;
		ChartData chartData = null;
		switch(chart.getType()){
			case AREA:
				if (chart.isThreeD()){
					categoryData = new XSSFArea3DChartData();
					((XSSFArea3DChartData)categoryData).setGrouping(PoiEnumConversion.toPoiGrouping(chart.getGrouping()));
				}else{
					categoryData = new XSSFAreaChartData();
					((XSSFAreaChartData)categoryData).setGrouping(PoiEnumConversion.toPoiGrouping(chart.getGrouping()));
				}
				break;
			case BAR:
				if (chart.isThreeD()){
					categoryData = new XSSFBar3DChartData();				
					((XSSFBar3DChartData)categoryData).setGrouping(PoiEnumConversion.toPoiGrouping(chart.getGrouping()));
					((XSSFBar3DChartData)categoryData).setBarDirection(PoiEnumConversion.toPoiBarDirection(chart.getBarDirection()));
				}else{
					categoryData = new XSSFBarChartData();
					((XSSFBarChartData)categoryData).setGrouping(PoiEnumConversion.toPoiGrouping(chart.getGrouping()));
					((XSSFBarChartData)categoryData).setBarDirection(PoiEnumConversion.toPoiBarDirection(chart.getBarDirection()));
					((XSSFBarChartData)categoryData).setBarOverlap(chart.getBarOverlap()); //ZSS-830
				}
				break;
			case BUBBLE:
				XYZData xyzData  = new XSSFBubbleChartData();
				fillXYZData((SGeneralChartData)chart.getData(), xyzData);
				chartData = xyzData;
				break;
			case COLUMN:
				if (chart.isThreeD()){
					categoryData = new XSSFColumn3DChartData();
					((XSSFColumn3DChartData)categoryData).setGrouping(PoiEnumConversion.toPoiGrouping(chart.getGrouping()));
					((XSSFColumn3DChartData)categoryData).setBarDirection(PoiEnumConversion.toPoiBarDirection(chart.getBarDirection()));
				}else{
					categoryData = new XSSFColumnChartData();
					((XSSFColumnChartData)categoryData).setGrouping(PoiEnumConversion.toPoiGrouping(chart.getGrouping()));
					((XSSFColumnChartData)categoryData).setBarDirection(PoiEnumConversion.toPoiBarDirection(chart.getBarDirection()));
					((XSSFColumnChartData)categoryData).setBarOverlap(chart.getBarOverlap());
				}
				break;
			case DOUGHNUT:
				categoryData = new XSSFDoughnutChartData();
				break;
			case LINE:
				if (chart.isThreeD()){
					categoryData = new XSSFLine3DChartData();
				}else{
					categoryData = new XSSFLineChartData();
				}
				break;
			case PIE:
				if (chart.isThreeD()){
					categoryData = new XSSFPie3DChartData();
				}else{
					categoryData = new XSSFPieChartData();
				}
				break;
			case SCATTER:
				XYData xyData =  new XSSFScatChartData();
				fillXYData((SGeneralChartData)chart.getData(), xyData);
				chartData = xyData;
				break;
//			case STOCK: TODO XSSFStockChartData is implemented with errors.
//				categoryData = new XSSFStockChartData();
//				break;
			default:
				return chartData;
		}
		if (categoryData != null){
			fillCategoryData((SGeneralChartData)chart.getData(), categoryData);
			chartData = categoryData;
		}
		return chartData;
	}
	
	/**
	 * Create and plot a POI chart with its chart data.
	 * @param chart
	 * @param chartData
	 * @param sheet
	 * @param poiSheet the sheet where the POI chart locates
	 */
	private void plotPoiChart(SChart chart, ChartData chartData, SSheet sheet, Sheet poiSheet){
		Chart poiChart = poiSheet.createDrawingPatriarch().createChart(toClientAnchor(chart.getAnchor(),sheet));
		//TODO export a chart's title, no POI API supported
		if (chart.isThreeD()){
			//ZSS-830
			XSSFView3D view3d = (XSSFView3D) poiChart.getOrCreateView3D();
			if (chart.getRotX() != 0) view3d.setRotX(chart.getRotX());
			if (chart.getRotY() != 0) view3d.setRotY(chart.getRotY());
			if (chart.getPerspective() != 30) view3d.setPerspective(chart.getPerspective());
			if (chart.getHPercent() != 100) view3d.setHPercent(chart.getHPercent());
			if (chart.getDepthPercent() != 100) view3d.setDepthPercent(chart.getDepthPercent());
			if (!chart.isRightAngleAxes()) view3d.setRightAngleAxes(false);
		}
		if (chart.getLegendPosition() != null) {
			ChartLegend legend = poiChart.getOrCreateLegend();
			legend.setPosition(PoiEnumConversion.toPoiLegendPosition(chart.getLegendPosition()));
		}
		ChartAxis bottomAxis = null;
		switch(chart.getType()) {
			case AREA:
			case BAR:
			case COLUMN:
			case LINE:
				bottomAxis=  poiChart.getChartAxisFactory().createCategoryAxis(AxisPosition.BOTTOM);
				break;
			case BUBBLE:
			case SCATTER:
				bottomAxis = poiChart.getChartAxisFactory().createValueAxis(AxisPosition.BOTTOM);
				break;
		}
		if (bottomAxis != null) {
			poiChart.plot(chartData, bottomAxis, poiChart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT));
		} else {
			poiChart.plot(chartData);
		}
		
		poiChart.setPlotOnlyVisibleCells(chart.isPlotOnlyVisibleCells());
	}
	
	
	private ClientAnchor toClientAnchor(ViewAnchor viewAnchor, SSheet sheet){
		ViewAnchor rightBottomAnchor = viewAnchor.getRightBottomAnchor(sheet);
		
		ClientAnchor clientAnchor = new XSSFClientAnchor(UnitUtil.pxToEmu(viewAnchor.getXOffset()),UnitUtil.pxToEmu(viewAnchor.getYOffset()),
				UnitUtil.pxToEmu(rightBottomAnchor.getXOffset()),UnitUtil.pxToEmu(rightBottomAnchor.getYOffset()),
				viewAnchor.getColumnIndex(),viewAnchor.getRowIndex(),
				rightBottomAnchor.getColumnIndex(),rightBottomAnchor.getRowIndex());
		return clientAnchor;
	}
	
	/**
	 * reference ChartDataUtil.fillCategoryData()
	 * @param chart
	 * @param categoryData
	 */
	private void fillCategoryData(SGeneralChartData chartData, CategoryData categoryData){
		ChartDataSource<?> categories = createCategoryChartDataSource(chartData);
		for (int i=0 ; i < chartData.getNumOfSeries() ; i++){
			SSeries series = chartData.getSeries(i);
			ChartTextSource title = createChartTextSource(series);
			ChartDataSource<? extends Number> values = createXValueDataSource(series);
			categoryData.addSerie(title, categories, values);
		}
	}
	
	/**
	 * reference ChartDataUtil.fillXYData()
	 * @param chart
	 * @param xyData
	 */
	private void fillXYData(SGeneralChartData chartData, XYData xyData){
		for (int i=0 ; i < chartData.getNumOfSeries() ; i++){
			final SSeries series = chartData.getSeries(i);
			ChartTextSource title = createChartTextSource(series);
			ChartDataSource<? extends Number> xValues = createXValueDataSource(series);
			ChartDataSource<? extends Number> yValues = createYValueDataSource(series);
			xyData.addSerie(title, xValues, yValues);
		}
	}

	/**
	 * reference ChartDataUtil.fillXYZData()
	 */
	private void fillXYZData(SGeneralChartData chartData, XYZData xyzData){
		for (int i=0 ; i < chartData.getNumOfSeries() ; i++){
			final SSeries series = chartData.getSeries(i);
			ChartTextSource title = createChartTextSource(series);
			ChartDataSource<? extends Number> xValues = createXValueDataSource(series);
			ChartDataSource<? extends Number> yValues = createYValueDataSource(series);
			ChartDataSource<? extends Number> zValues = createZValueDataSource(series);
			xyzData.addSerie(title, xValues, yValues, zValues);
		}
	}
	
	private ChartDataSource<Number> createXValueDataSource(final SSeries series) {
		return new ChartDataSource<Number>() {

			@Override
			public int getPointCount() {
				return series.getNumOfXValue();
			}

			@Override
			public Number getPointAt(int index) {
				try{
					return Double.parseDouble(series.getXValue(index).toString());
				}catch(NumberFormatException nfe){
					return index;
				}
			}

			@Override
			public boolean isReference() {
				return true;
			}

			@Override
			public boolean isNumeric() {
				return true;
			}

			@Override
			public String getFormulaString() {
				return series.getXValuesFormula();
			}

			@Override
			public void renameSheet(String oldname, String newname) {
			}
		};
	}

	private ChartDataSource<Number> createYValueDataSource(final SSeries series) {
		return new ChartDataSource<Number>() {

			@Override
			public int getPointCount() {
				return series.getNumOfYValue();
			}

			@Override
			public Number getPointAt(int index) {
				try{
					return Double.parseDouble(series.getYValue(index).toString());
				}catch (NumberFormatException nfe) {
					return index;
				}
			}

			@Override
			public boolean isReference() {
				return true;
			}

			@Override
			public boolean isNumeric() {
				return true;
			}

			@Override
			public String getFormulaString() {
				return series.getYValuesFormula();
			}

			@Override
			public void renameSheet(String oldname, String newname) {
			}
		};
	}
	
	private ChartDataSource<Number> createZValueDataSource(final SSeries series) {
		return new ChartDataSource<Number>() {

			@Override
			public int getPointCount() {
				return series.getNumOfZValue();
			}

			@Override
			public Number getPointAt(int index) {
				try{
					return Double.parseDouble(series.getZValue(index).toString());
				}catch (NumberFormatException e) {
					return index;
				}
			}

			@Override
			public boolean isReference() {
				return true;
			}

			@Override
			public boolean isNumeric() {
				return true;
			}

			@Override
			public String getFormulaString() {
				return series.getZValuesFormula();
			}

			@Override
			public void renameSheet(String oldname, String newname) {
			}
		};
	}
	
	private ChartTextSource createChartTextSource(final SSeries series){
		return new ChartTextSource() {
			
			@Override
			public void renameSheet(String oldname, String newname) {
			}
			
			@Override
			public boolean isReference() {
				return true;
			}
			
			@Override
			public String getTextString() {
				return series.getName();
			}
			
			@Override
			public String getFormulaString() {
				return series.getNameFormula();
			}
		};
		
	}

	private ChartDataSource<?> createCategoryChartDataSource(final SGeneralChartData chartData){
		return new ChartDataSource<String>() {

			@Override
			public int getPointCount() {
				return chartData.getNumOfCategory();
			}

			@Override
			public String getPointAt(int index) {
				return chartData.getCategory(index).toString();
			}

			@Override
			public boolean isReference() {
				return true;
			}

			@Override
			public boolean isNumeric() {
				return false;
			}

			@Override
			public String getFormulaString() {
				return chartData.getCategoriesFormula();
			}

			@Override
			public void renameSheet(String oldname, String newname) {
			}
		};
	}
	
	/**
	 * According to {@link ValidationType}, FORMULA means custom validation.
	 */
	@Override
	protected void exportValidation(SSheet sheet, Sheet poiSheet) {
		for (SDataValidation validation : sheet.getDataValidations()){
			int operatorType = PoiEnumConversion.toPoiOperatorType(validation.getOperatorType());
			String formula1 = ((AbstractDataValidationAdv)validation).getEscapedFormula1();
			String formula2 = ((AbstractDataValidationAdv)validation).getEscapedFormula2();
			DataValidationConstraint constraint = null;
			switch(validation.getValidationType()){
				case TIME:
					constraint = poiSheet.getDataValidationHelper().createTimeConstraint(operatorType, formula1, formula2);
					break;
				case TEXT_LENGTH:
					constraint = poiSheet.getDataValidationHelper().createTextLengthConstraint(operatorType, formula1, formula2);
					break;
				case DATE:
					//the last argument, dateFormat, is only used in XLS. We just pass empty string here. 
					constraint = poiSheet.getDataValidationHelper().createDateConstraint(operatorType, formula1, formula2, "");
					break;
				case LIST:
					constraint = poiSheet.getDataValidationHelper().createFormulaListConstraint(formula1);
					break;
				case INTEGER:
					constraint = poiSheet.getDataValidationHelper().createIntegerConstraint(operatorType, formula1, formula2);
					break;
				case CUSTOM: // custom
					constraint = poiSheet.getDataValidationHelper().createCustomConstraint(formula1);
					break;
				case DECIMAL:
					constraint = poiSheet.getDataValidationHelper().createDecimalConstraint(operatorType, formula1, formula2);
					break;
				case ANY:
					constraint = poiSheet.getDataValidationHelper().createAnyConstraint(); //ZSS-835
					break;
				default:
					continue;
			}
			if (!validation.getRegions().isEmpty()) { // ZSS-835
				final CellRangeAddressList rgnList = new CellRangeAddressList();
				for (CellRegion rgn : validation.getRegions()) { // must prepare rgnList then create poiValidation
					rgnList.addCellRangeAddress(rgn.getRow(), rgn.getColumn(), rgn.getLastRow(), rgn.getLastColumn());
				}
				DataValidation poiValidation = 
					poiSheet.getDataValidationHelper().createValidation(constraint, rgnList);
				
				poiValidation.setEmptyCellAllowed(validation.isIgnoreBlank());
				poiValidation.setSuppressDropDownArrow(validation.isInCellDropdown());
				
				poiValidation.setErrorStyle(PoiEnumConversion.toPoiErrorStyle(validation.getAlertStyle()));
				poiValidation.createErrorBox(validation.getErrorTitle(), validation.getErrorMessage());
				poiValidation.setShowErrorBox(validation.isShowError());
				
				poiValidation.createPromptBox(validation.getInputTitle(), validation.getInputMessage());
				poiValidation.setShowPromptBox(validation.isShowInput());
				
				poiSheet.addValidationData(poiValidation);
			}
		}
	}
	
	/**
	 * See Javadoc at {@link AbstractExcelImporter} importAutoFilter().
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void exportAutoFilter(SSheet sheet, Sheet poiSheet) {
		SAutoFilter autoFilter = sheet.getAutoFilter();
		if (autoFilter != null){
			CellRegion region = autoFilter.getRegion();
			XSSFAutoFilter poiAutoFilter = (XSSFAutoFilter)poiSheet.setAutoFilter(new CellRangeAddress(region.getRow(), region.getLastRow(), region.getColumn(), region.getLastColumn()));
			int numberOfColumn = region.getLastColumn() - region.getColumn() + 1;
			exportFilterColumns(poiAutoFilter, autoFilter, numberOfColumn);
		}
	}
	
	//ZSS-1019
	private void exportFilterColumns(XSSFAutoFilter poiAutoFilter, SAutoFilter autoFilter, int numberOfColumn) {
		for( int i = 0 ; i < numberOfColumn ; i++){
			NFilterColumn srcFilterColumn = autoFilter.getFilterColumn(i, false);
			if (srcFilterColumn == null){
				continue;
			}
			XSSFFilterColumn destFilterColumn = (XSSFFilterColumn)poiAutoFilter.getOrCreateFilterColumn(i);
			Object[] criteria1 = null;
			if (srcFilterColumn.getCriteria1()!=null){
				criteria1 = srcFilterColumn.getCriteria1().toArray(new String[0]);
			}
			Object[] criteria2 = null;
			if (srcFilterColumn.getCriteria1()!=null){
				criteria2 = srcFilterColumn.getCriteria2().toArray(new String[0]);
			}
			destFilterColumn.setProperties(criteria1, PoiEnumConversion.toPoiFilterOperator(srcFilterColumn.getOperator()),
					criteria2, srcFilterColumn.isShowButton());
		}
	}
	
	/**
	 * Export hashed password directly to poiSheet.
	 */
	@Override
	protected void exportPassword(SSheet sheet, Sheet poiSheet) {
		
		//ZSS-1063
		final String hashValue = ((SheetImpl)sheet).getHashValue();
		if (hashValue != null) {
			final String saltValue = ((SheetImpl)sheet).getSaltValue();
			final String spinCount = ((SheetImpl)sheet).getSpinCount();
			final String algName = ((SheetImpl)sheet).getAlgName();
			
			((XSSFSheet)poiSheet).setHashValue(hashValue);
			((XSSFSheet)poiSheet).setSaltValue(saltValue);
			((XSSFSheet)poiSheet).setSpinCount(spinCount);
			((XSSFSheet)poiSheet).setAlgName(algName);
		} else {
			short hashpass = sheet.getHashedPassword();
			if (hashpass != 0) {
				((XSSFSheet)poiSheet).setPasswordHash(hashpass);
			}
		}
	}
	
	
	//ZSS-854 
	@Override
	protected CellStyle toPOIDefaultCellStyle(SCellStyle cellStyle) {
		//set Border
		short bottom = PoiEnumConversion.toPoiBorderType(cellStyle.getBorderBottom());
		short left = PoiEnumConversion.toPoiBorderType(cellStyle.getBorderLeft());
		short right = PoiEnumConversion.toPoiBorderType(cellStyle.getBorderRight());
		short top = PoiEnumConversion.toPoiBorderType(cellStyle.getBorderTop());
		Color bottomColor = toPOIColor(cellStyle.getBorderBottomColor());
		Color leftColor = toPOIColor(cellStyle.getBorderLeftColor());
		Color rightColor = toPOIColor(cellStyle.getBorderRightColor());
		Color topColor = toPOIColor(cellStyle.getBorderTopColor());
		CTBorder ct = CTBorder.Factory.newInstance();
		XSSFCellBorder border = new XSSFCellBorder(ct);
		border.prepareBorder(left, leftColor, top, topColor, right, rightColor, bottom, bottomColor);
		
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
		CTFill ctf = CTFill.Factory.newInstance();
		XSSFCellFill fill = new XSSFCellFill(ctf);
		fill.prepareFill(fillColor, backColor, pattern);
		
		// font
		XSSFFont font = (XSSFFont)toPOIFont(cellStyle.getFont());
		
		// refer from BookHelper#setDataFormat
		DataFormat df = workbook.createDataFormat();
		short fmt = df.getFormat(cellStyle.getDataFormat());
		
		XSSFCellStyle poiCellStyle = (XSSFCellStyle) ((XSSFWorkbook)workbook).createDefaultCellStyle(border, fill, font, fmt);

		//cell Alignment
		short hAlign = PoiEnumConversion.toPoiHorizontalAlignment(cellStyle.getAlignment());
		short vAlign = PoiEnumConversion.toPoiVerticalAlignment(cellStyle.getVerticalAlignment());
		boolean wrapText = cellStyle.isWrapText();
		poiCellStyle.setDefaultCellAlignment(hAlign, vAlign, wrapText);
		
		//protect
		boolean locked = cellStyle.isLocked();
		boolean hidden = cellStyle.isHidden();
		poiCellStyle.setDefaultProtection(locked, hidden);

		return poiCellStyle;
	}

	//ZSS-855
	@Override
	protected int exportTables(SSheet sheet, Sheet poiSheet0, int tbId) {
		final XSSFSheet poiSheet = (XSSFSheet) poiSheet0;
		for (STable table : sheet.getTables()) {
			XSSFTable poiTable = poiSheet.createTable();
			poiTable.setName(table.getName());
			poiTable.setDisplayName(table.getDisplayName());
			poiTable.setRef(table.getAllRegion().getRegion().getReferenceString());
			poiTable.setTotalsRowCount(table.getTotalsRowCount());
			poiTable.setHeaderRowCount(table.getHeaderRowCount());
			final XSSFTableStyleInfo poiInfo = poiTable.createTableStyleInfo();
			final STableStyleInfo info = table.getTableStyleInfo();
			poiInfo.setName(info.getName());
			poiInfo.setShowColumnStripes(info.isShowColumnStripes());
			poiInfo.setShowRowStripes(info.isShowRowStripes());
			poiInfo.setShowLastColumn(info.isShowLastColumn());
			poiInfo.setShowFirstColumn(info.isShowFirstColumn());
			
			final SAutoFilter filter = table.getAutoFilter();
			if (filter != null) {
				final CellRegion region = filter.getRegion();
				XSSFAutoFilter poiFilter = poiTable.createAutoFilter();
				poiFilter.setRef(region.getReferenceString());
				exportFilterColumns(poiFilter, filter, region.getColumnCount());
			} else {
				poiTable.clearAutoFilter();
			}
			
			int j = 0;
			for (STableColumn tbCol : table.getColumns()) {
				final XSSFTableColumn poiTbCol = poiTable.addTableColumn();
				poiTbCol.setName(tbCol.getName());
				poiTbCol.setId(++j);
				if (tbCol.getTotalsRowFunction() != null)
					poiTbCol.setTotalsRowFunction(TotalsRowFunction.values()[tbCol.getTotalsRowFunction().ordinal()]);
				if (tbCol.getTotalsRowFunction() == STotalsRowFunction.none && tbCol.getTotalsRowLabel() != null)
					poiTbCol.setTotalsRowLabel(tbCol.getTotalsRowLabel());
				else if (tbCol.getTotalsRowFunction() == STotalsRowFunction.custom && tbCol.getTotalsRowFormula() != null)
					poiTbCol.setTotalsRowFormula(tbCol.getTotalsRowFormula()); //ZSS-977
			}
			poiTable.setId(++tbId);
			((XSSFWorkbook)workbook).addTableName(poiTable);
		}
		
		return tbId;
	}
}
