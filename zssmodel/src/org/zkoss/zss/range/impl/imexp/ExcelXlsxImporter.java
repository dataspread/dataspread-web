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

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.openxmlformats.schemas.drawingml.x2006.main.*;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.*;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTDrawing;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;
import org.w3c.dom.Node;
import org.zkoss.poi.POIXMLDocumentPart;
import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.poi.ss.usermodel.*;
import org.zkoss.poi.ss.usermodel.charts.*;
import org.zkoss.poi.ss.util.CellRangeAddress;
import org.zkoss.poi.xssf.model.ExternalLink;
import org.zkoss.poi.xssf.usermodel.*;
import org.zkoss.poi.xssf.usermodel.charts.*;
import org.zkoss.zss.model.*;
import org.zkoss.zss.model.SChart.ChartType;
import org.zkoss.zss.model.STableColumn.STotalsRowFunction;
import org.zkoss.zss.model.chart.*;
import org.zkoss.zss.model.impl.AbstractDataValidationAdv;
import org.zkoss.zss.model.impl.AutoFilterImpl;
import org.zkoss.zss.model.impl.ChartAxisImpl;
import org.zkoss.zss.model.impl.TableColumnImpl;
import org.zkoss.zss.model.impl.TableImpl;
import org.zkoss.zss.model.impl.TableStyleInfoImpl;
import org.zkoss.zss.model.sys.formula.FormulaEngine;
import org.zkoss.zss.model.impl.AbstractBookAdv;

/**
 * Specific importing behavior for XLSX.
 * 
 * @author Hawk
 * @since 3.5.0
 */
public class ExcelXlsxImporter extends AbstractExcelImporter{
	private static final Logger logger = Logger.getLogger(ExcelXlsxImporter.class.getName());

	@Override
	protected Workbook createPoiBook(InputStream is) throws IOException {
		return new XSSFWorkbook(is);
	}
	
	@Override
	protected void importExternalBookLinks() {
		try {
			// directly use XML to retrieve external book references and fetch external book names
			// if retrieving ExternalLink from relations list of workbook, the order might be incorrect.
			// according to spec. ISO 29500-1, 18.17.2.3, p. 2048, the order should be natural order of XML tags.
			List<String> bookNames = new ArrayList<String>();
			XSSFWorkbook xssfBook = (XSSFWorkbook)workbook;
			CTWorkbook ctBook = xssfBook.getCTWorkbook();
			CTExternalReferences externalReferences = ctBook.getExternalReferences();
			if(externalReferences != null) {
				List<CTExternalReference> exRefs = externalReferences.getExternalReferenceList();
				for(CTExternalReference exRef : exRefs) {
					ExternalLink link = (ExternalLink)xssfBook.getRelationById(exRef.getId());
					bookNames.add(link.getBookName());
				}
			}
			if(bookNames.size() > 0) {
				this.book.setAttribute(FormulaEngine.KEY_EXTERNAL_BOOK_NAMES, bookNames);
			}
		} catch(Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}


	/**
	 * [ISO/IEC 29500-1 1st Edition] 18.3.1.13 col (Column Width & Formatting)
	 * By experiments, CT_Col is always created in ascending order by min and the range specified by min & max doesn't overlap each other.<br/>
	 * For example:
	 * <x:cols xmlns:x="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
	 *   <x:col min="1" max="1" width="11.28515625" customWidth="1" />
	 *   <x:col min="2" max="4" width="11.28515625" hidden="1" customWidth="1" />
	 *   <x:col min="5" max="5" width="11.28515625" customWidth="1" />
	 * </x:cols> 
	 */
	@Override
	protected void importColumn(Sheet poiSheet, SSheet sheet) {
		CTWorksheet worksheet = ((XSSFSheet)poiSheet).getCTWorksheet();
		if(worksheet.sizeOfColsArray()<=0){
			return;
		}
		
		//ZSS-952: xssf's default column width includes the 5 pixels padding and is in charwidth unit.
//		int defaultWidth = UnitUtil.defaultColumnWidthToPx(poiSheet.getDefaultColumnWidth(), CHRACTER_WIDTH);
		int defaultWidth = UnitUtil.xssfDefaultColumnWidthToPx(((XSSFSheet)poiSheet).getXssfDefaultColumnWidth(), CHRACTER_WIDTH);
		CTCols colsArray = worksheet.getColsArray(0);
		for (int i = 0; i < colsArray.sizeOfColArray(); i++) {
			CTCol ctCol = colsArray.getColArray(i);
			//max is 16384
			
			SColumnArray columnArray = sheet.setupColumnArray((int)ctCol.getMin()-1, (int)ctCol.getMax()-1);
			
			boolean hidden = ctCol.getHidden();
			int columnIndex = (int)ctCol.getMin()-1;
			columnArray.setHidden(hidden);
			int width = ImExpUtils.getWidthAny(poiSheet, columnIndex, CHRACTER_WIDTH);
			if (!(hidden || width == defaultWidth)){
				//when CT_Col is hidden with default width, We don't import the width for it's 0.  
				columnArray.setWidth(width);
			}
			columnArray.setCustomWidth(ctCol.getCustomWidth() || width != defaultWidth); //ZSS-896

			CellStyle columnStyle = poiSheet.getColumnStyle(columnIndex);
			if (columnStyle != null){
				columnArray.setCellStyle(importCellStyle(columnStyle));
			}
		}
	}

	/**
	 * Not import X & Y axis title because {@link XSSFCategoryAxis} doesn't provide API to get title. 
	 * Reference ChartHelper.drawXSSFChart()
	 */
	private void importChart(List<ZssChartX> poiCharts, Sheet poiSheet, SSheet sheet) {
		
		for (ZssChartX zssChart : poiCharts){
			XSSFChart xssfChart = (XSSFChart)zssChart.getChart();
			ViewAnchor viewAnchor = toViewAnchor(poiSheet, xssfChart.getPreferredSize());

			SChart chart = null;
			CategoryData categoryData = null;
			//reference ChartHelper.drawXSSFChart()
			switch(xssfChart.getChartType()){
				case Area:
					chart = sheet.addChart(ChartType.AREA, viewAnchor);
					categoryData = new XSSFAreaChartData(xssfChart);
					chart.setGrouping(PoiEnumConversion.toChartGrouping(((XSSFAreaChartData)categoryData).getGrouping()));
					break;
				case Area3D:
					chart = sheet.addChart(ChartType.AREA, viewAnchor);
					categoryData = new XSSFArea3DChartData(xssfChart);
					chart.setGrouping(PoiEnumConversion.toChartGrouping(((XSSFArea3DChartData)categoryData).getGrouping()));
					break;
				case Bar:
					chart = sheet.addChart(ChartType.BAR, viewAnchor);
					categoryData = new XSSFBarChartData(xssfChart);
					chart.setBarDirection(PoiEnumConversion.toBarDirection(((XSSFBarChartData)categoryData).getBarDirection()));
					chart.setBarOverlap(xssfChart.getBarOverlap()); //ZSS-830
					chart.setGrouping(PoiEnumConversion.toChartGrouping(((XSSFBarChartData)categoryData).getGrouping()));
					break;
				case Bar3D:
					chart = sheet.addChart(ChartType.BAR, viewAnchor);
					categoryData = new XSSFBar3DChartData(xssfChart);
					chart.setBarDirection(PoiEnumConversion.toBarDirection(((XSSFBar3DChartData)categoryData).getBarDirection()));
					chart.setGrouping(PoiEnumConversion.toChartGrouping(((XSSFBar3DChartData)categoryData).getGrouping()));
					break;
				case Bubble:
					chart = sheet.addChart(ChartType.BUBBLE, viewAnchor);
					
					XYZData xyzData = new XSSFBubbleChartData(xssfChart);
					importXyzSeries(xyzData.getSeries(), (SGeneralChartData)chart.getData());
					break;
				case Column:
					chart = sheet.addChart(ChartType.COLUMN, viewAnchor);
					categoryData = new XSSFColumnChartData(xssfChart);
					chart.setBarDirection(PoiEnumConversion.toBarDirection(((XSSFColumnChartData)categoryData).getBarDirection()));
					chart.setBarOverlap(xssfChart.getBarOverlap()); //ZSS-830
					chart.setGrouping(PoiEnumConversion.toChartGrouping(((XSSFColumnChartData)categoryData).getGrouping()));
					break;
				case Column3D:
					chart = sheet.addChart(ChartType.COLUMN, viewAnchor);
					categoryData = new XSSFColumn3DChartData(xssfChart);
					chart.setBarDirection(PoiEnumConversion.toBarDirection(((XSSFColumn3DChartData)categoryData).getBarDirection()));
					chart.setGrouping(PoiEnumConversion.toChartGrouping(((XSSFColumn3DChartData)categoryData).getGrouping()));
					break;
				case Doughnut:
					chart = sheet.addChart(ChartType.DOUGHNUT, viewAnchor);
					categoryData = new XSSFDoughnutChartData(xssfChart);
					break;
				case Line:
					chart = sheet.addChart(ChartType.LINE, viewAnchor);
					categoryData = new XSSFLineChartData(xssfChart);
					chart.setGrouping(PoiEnumConversion.toChartGrouping(((XSSFLineChartData)categoryData).getGrouping())); //ZSS-828
					break;
				case Line3D:
					chart = sheet.addChart(ChartType.LINE, viewAnchor);
					categoryData = new XSSFLine3DChartData(xssfChart);
					chart.setGrouping(PoiEnumConversion.toChartGrouping(((XSSFLine3DChartData)categoryData).getGrouping())); //ZSS-828
					break;
				case Pie:
					chart = sheet.addChart(ChartType.PIE, viewAnchor);
					categoryData = new XSSFPieChartData(xssfChart);
					break;
				case Pie3D:
					chart = sheet.addChart(ChartType.PIE, viewAnchor);
					categoryData = new XSSFPie3DChartData(xssfChart);
					break;
				case Scatter:
					chart = sheet.addChart(ChartType.SCATTER, viewAnchor);
					
					XYData xyData =  new XSSFScatChartData(xssfChart);
					importXySeries(xyData.getSeries(), (SGeneralChartData)chart.getData());
					break;
//				case Stock:
//					chart = sheet.addChart(NChartType.STOCK, viewAnchor);
//					categoryData = new XSSFStockChartData(xssfChart); TODO XSSFStockChartData contains wrong implementation
//					break;
				default:
					//TODO ignore unsupported charts
					continue;
			}
			
			if (xssfChart.getTitle()!=null){
				chart.setTitle(xssfChart.getTitle().getString());
			}
			chart.setThreeD(xssfChart.isSetView3D());
			//ZSS-830
			if (chart.isThreeD()) {
				XSSFView3D view3d = xssfChart.getOrCreateView3D();
				chart.setRotX(view3d.getRotX());
				chart.setRotY(view3d.getRotY());
				chart.setPerspective(view3d.getPerspective());
				chart.setHPercent(view3d.getHPercent());
				chart.setDepthPercent(view3d.getDepthPercent());
				chart.setRightAngleAxes(view3d.isRightAngleAxes());
			}
			if (xssfChart.hasLegend()) {
				chart.setLegendPosition(PoiEnumConversion.toLengendPosition(xssfChart.getOrCreateLegend().getPosition()));
			}
			if (categoryData != null){
				importSeries(categoryData.getSeries(), (SGeneralChartData)chart.getData());
			}
			//ZSS-822
			importAxis(xssfChart, chart);
			
			chart.setPlotOnlyVisibleCells(xssfChart.isPlotOnlyVisibleCells());
		}
	}
	//ZSS-822
	private void importAxis(XSSFChart xssfChart, SChart chart) {
		List axises = (List) xssfChart.getAxis();
		if (axises != null) {
			for (Object axis0 : axises) {
				if (axis0 instanceof ValueAxis) {
					ValueAxis axis = (ValueAxis) axis0;
					String format = ((ValueAxis) axis).getNumberFormat();
					double min = axis.getMinimum();
					double max = axis.getMaximum();
					SChartAxis saxis = new ChartAxisImpl(axis.getId(), SChartAxis.SChartAxisType.VALUE, min, max, format);
					chart.addValueAxis(saxis);
				} else if (axis0 instanceof CategoryAxis) {
					CategoryAxis axis = (CategoryAxis) axis0;
					String format = null;
					double min = axis.getMinimum();
					double max = axis.getMaximum();
					SChartAxis saxis = new ChartAxisImpl(axis.getId(), SChartAxis.SChartAxisType.CATEGORY, min, max, format);
					chart.addCategoryAxis(saxis);
				}
			}
		}
	}

	/**
	 * reference ChartHepler.prepareCategoryModel() 
	 * Category normally indicates the values show on X axis.
	 * Tile indicates the name for each series.
	 * @param seriesList source chart data
	 * @param chartData destination chart data
	 */
	private void importSeries(List<? extends CategoryDataSerie> seriesList, SGeneralChartData chartData) {
		CategoryDataSerie firstSeries = null;
		if ((firstSeries = seriesList.get(0))!=null){
			chartData.setCategoriesFormula(getValueFormula(firstSeries.getCategories()));
		}
		for (int i =0 ;  i< seriesList.size() ; i++){
			CategoryDataSerie sourceSeries = seriesList.get(i);
			String nameExpression = getTitleFormula(sourceSeries.getTitle(), i);			
			String xValueExpression = getValueFormula(sourceSeries.getValues());
			SSeries series = chartData.addSeries();
			series.setFormula(nameExpression, xValueExpression);
		}
	}
	
	private void importXySeries(List<? extends XYDataSerie> seriesList, SGeneralChartData chartData) {
		for (int i =0 ;  i< seriesList.size() ; i++){
			XYDataSerie sourceSeries = seriesList.get(i);
			SSeries series = chartData.addSeries();
			series.setXYFormula(getTitleFormula(sourceSeries.getTitle(), i), 
								getValueFormula(sourceSeries.getXs()), 
								getValueFormula(sourceSeries.getYs()));
		}
	}

	/**
	 * reference ChartHepler.prepareXYZModel() 
	 * @param seriesList
	 * @param chart
	 */
	private void importXyzSeries(List<? extends XYZDataSerie> seriesList, SGeneralChartData chartData) {
		for (int i =0 ;  i< seriesList.size() ; i++){
			XYZDataSerie sourceSeries = seriesList.get(i);
			//reference to ChartHelper.prepareTitle()
			String nameExpression = getTitleFormula(sourceSeries.getTitle(), i);			
			String xValueExpression = getValueFormula(sourceSeries.getXs());
			String yValueExpression = getValueFormula(sourceSeries.getYs());
			String zValueExpression = getValueFormula(sourceSeries.getZs());	
			
			SSeries series = chartData.addSeries();
			series.setXYZFormula(nameExpression, xValueExpression, yValueExpression, zValueExpression);
		}
	}
	
	/**
	 * return a formula or generate a default title ("Series[N]")if title doesn't exist.
	 * reference ChartHelper.prepareTitle()
	 * @param textSource
	 * @return
	 */
	private String getTitleFormula(ChartTextSource textSource, int seriesIndex){
		if (textSource == null){
			return "\"Series"+seriesIndex+"\"";
		}else {
			if (textSource.isReference()){
				return textSource.getFormulaString();
			}else{
				return "\""+textSource.getTextString()+"\"";
			}
		}
	}
	
	/**
	 * reference ChartHelper.prepareValues()
	 * @param dataSource
	 * @return
	 */
	private String getValueFormula(ChartDataSource<?> dataSource){
		if (dataSource.isReference()){
			return dataSource.getFormulaString();
		}else{
			int count = dataSource.getPointCount();
			if(count<=0){
				return null;
			}
			StringBuilder expression = new StringBuilder("{");
			for (int i = 0; i < count; ++i) {
				Object value = dataSource.getPointAt(i); //Number or String
				if (value == null){
					if (dataSource.isNumeric()){
						expression.append("0");
					}else{
						expression.append("\"\"");
					}
				}else{
					//ZSS-577 it is possible a string array
					if(dataSource.isNumeric()){
						expression.append(value.toString());
					}else{
						expression.append("\"").append(value).append("\"");
					}
				}
				if (i != count-1){
					expression.append(",");
				}
			}
			expression.append("}");
			return expression.toString();
		}
	}
	
	
	/*
	 * import drawings which include charts and pictures.
	 * reference DrawingManagerImpl.initXSSFDrawings()
	 */
	@Override
	protected void importDrawings(Sheet poiSheet, SSheet sheet) {
		/**
		 * A list of POI chart wrapper loaded during import.
		 */
		List<ZssChartX> poiCharts = new ArrayList<ZssChartX>();
		List<Picture> poiPictures = new ArrayList<Picture>();
		
		XSSFDrawing patriarch = null;
		for(POIXMLDocumentPart dr : ((XSSFSheet)poiSheet).getRelations()){
			if(dr instanceof XSSFDrawing){
				patriarch = (XSSFDrawing) dr;
				break;
			}
		}
		if (patriarch != null) {			
			final CTDrawing ctdrawing = patriarch.getCTDrawing();
			for (CTTwoCellAnchor anchor: ctdrawing.getTwoCellAnchorArray()) {
				final CTMarker from = anchor.getFrom();
				final CTMarker to = anchor.getTo();
				XSSFClientAnchor clientAnchor = null;
				if (from != null && to != null){
					clientAnchor = new XSSFClientAnchor((int)from.getColOff(), (int)from.getRowOff(), (int)to.getColOff(), (int)to.getRowOff(), 
							from.getCol(), from.getRow(), to.getCol(), to.getRow());
				}
				CTPicture ctPicture = anchor.getPic();
				if (ctPicture == null){ 
					final CTGraphicalObjectFrame gfrm = anchor.getGraphicFrame();
					if (gfrm != null) {
						final XSSFChartX chartX = createXSSFChartX(patriarch, gfrm , clientAnchor);
						poiCharts.add(chartX);
					}
				}else{ 
					poiPictures.add(XSSFPictureHelper.newXSSFPicture(patriarch, clientAnchor, ctPicture));
				}
			}
		}
		importChart(poiCharts, poiSheet, sheet);
		importPicture(poiPictures, poiSheet, sheet);
	}
	
	private XSSFChartX createXSSFChartX(XSSFDrawing patriarch, CTGraphicalObjectFrame gfrm , XSSFClientAnchor xanchor) {
		//chart
		final String name = gfrm.getNvGraphicFramePr().getCNvPr().getName();
		final CTGraphicalObject gobj = gfrm.getGraphic();
		final CTGraphicalObjectData gdata = gobj.getGraphicData();
		String chartId = null;
		// ZSS-358: must skip other XML nodes such as TEXT_NODE
		Node child = gdata.getDomNode().getFirstChild();
		while(child != null) {
			if("chart".equals(child.getLocalName())) {
				chartId = child.getAttributes().getNamedItemNS("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id").getNodeValue();
				break;
			}
			child = child.getNextSibling();
		}
		final XSSFChartX chartX = new XSSFChartX(patriarch, xanchor, name, chartId);
		return chartX;
	}
	
	/**
	 * Reference DefaultBookWidgetLoader.getXSSFWidthInPx()
	 */
	@Override
	protected int getAnchorWidthInPx(ClientAnchor anchor, Sheet poiSheet) {
		final int firstColumn = anchor.getCol1();
	    final int firstColumnWidth = ImExpUtils.getWidthAny(poiSheet,firstColumn, AbstractExcelImporter.CHRACTER_WIDTH);
	    int offsetInFirstColumn = UnitUtil.emuToPx(anchor.getDx1());

	    final int anchorWidthInFirstColumn = firstColumnWidth - offsetInFirstColumn;  
		int anchorWidthInLastColumn = UnitUtil.emuToPx(anchor.getDx2());
	    
		final int lastColumn = anchor.getCol2();
	    //in between
	    int width = firstColumn == lastColumn ? anchorWidthInLastColumn - offsetInFirstColumn : anchorWidthInFirstColumn + anchorWidthInLastColumn;
	    width = Math.abs(width); // just in case
	    
	    for (int j = firstColumn+1; j < lastColumn; ++j) {
	    	width += ImExpUtils.getWidthAny(poiSheet,j, AbstractExcelImporter.CHRACTER_WIDTH);
	    }
	    
	    return width;
	}
	
	/**
	 * DefaultBookWidgetLoader.getXSSFHeightInPx()
	 */
	@Override
	protected int getAnchorHeightInPx(ClientAnchor anchor, Sheet poiSheet) {
	    final int firstRow = anchor.getRow1();
	    int offsetInFirstRow = UnitUtil.emuToPx(anchor.getDy1());
	    //first row
	    final int firstRowHeight = ImExpUtils.getHeightAny(poiSheet,firstRow);
		final int anchorHeightInFirstRow = firstRowHeight - offsetInFirstRow;  
	    
	    //last row
		final int lastRow = anchor.getRow2();
		int anchorHeightInLastRow = UnitUtil.emuToPx(anchor.getDy2());
		int height = lastRow == firstRow ? anchorHeightInLastRow - offsetInFirstRow : anchorHeightInFirstRow + anchorHeightInLastRow ;
		height = Math.abs(height); // just in case
	    
	    //add inter-row height
	    for (int row = firstRow+1; row < lastRow; ++row) {
	    	height += ImExpUtils.getHeightAny(poiSheet,row);
	    }
	    
	    return height;
	}


	@Override
	protected int getXoffsetInPixel(ClientAnchor clientAnchor, Sheet poiSheet) {
		return UnitUtil.emuToPx(clientAnchor.getDx1());
	}

	@Override
	protected int getYoffsetInPixel(ClientAnchor clientAnchor, Sheet poiSheet) {
		return UnitUtil.emuToPx(clientAnchor.getDy1());
	}

	/**
	 * Reference BookHelper.validate()
	 */
	@Override
	protected void importValidation(Sheet poiSheet, SSheet sheet) {
		for (DataValidation poiValidation : poiSheet.getDataValidations()){
			
			SDataValidation dataValidation = sheet.addDataValidation(null, null);
			CellRangeAddress[] cellRangeAddresses = poiValidation.getRegions().getCellRangeAddresses();
			DataValidationConstraint poiConstraint = poiValidation.getValidationConstraint();
			// getExplicitListValues() will be represented as formula1
			dataValidation.setOperatorType(PoiEnumConversion.toOperatorType(poiConstraint.getOperator()));
			dataValidation.setValidationType(PoiEnumConversion.toValidationType(poiConstraint.getValidationType()));
			((AbstractDataValidationAdv)dataValidation).setEscapedFormulas(poiConstraint.getFormula1(), poiConstraint.getFormula2());
			
			dataValidation.setIgnoreBlank(poiValidation.getEmptyCellAllowed());
			dataValidation.setErrorTitle(poiValidation.getErrorBoxTitle());
			dataValidation.setErrorMessage(poiValidation.getErrorBoxText());
			dataValidation.setAlertStyle(PoiEnumConversion.toErrorStyle(poiValidation.getErrorStyle()));
			dataValidation.setInputTitle(poiValidation.getPromptBoxTitle());
			dataValidation.setInputMessage(poiValidation.getPromptBoxText());
			if (poiConstraint.getValidationType() == DataValidationConstraint.ValidationType.LIST){
				/* 
				 * According to ISO/IEC 29500-1 \ 18.3.1.32  dataValidation (Data Validation) 
				 * Excel file contains reversed value against format specification . If not showing drop down, file contains 1 (true). If showing, 
				 * attribute "showDropDown" won't exist and POI get false. But POI's API reverse its value again.
				 * So we just directly accept the POI returned value.
				 */
				dataValidation.setInCellDropdown(poiValidation.getSuppressDropDownArrow());
			}
			dataValidation.setShowError(poiValidation.getShowErrorBox());
			dataValidation.setShowInput(poiValidation.getShowPromptBox());
			/*
			 * According to ISO/IEC 29500-1 \ 18.18.76  ST_Sqref (Reference Sequence) and A.2 
			 * Its XML Schema indicates it's a required attribute, so CellRangeAddresses must have at least one address. 
			 */
			Set<CellRegion> regions = new HashSet<CellRegion>();
			for(CellRangeAddress cellRangeAddr:cellRangeAddresses){
				regions.add(new CellRegion(cellRangeAddr.formatAsString()));
			}
			dataValidation.setRegions(regions);
		}
	}
	
	@Override
	protected boolean skipName(Name definedName) {
		boolean r = super.skipName(definedName);
		if(r)
			return r;
		if(((XSSFName)definedName).isBuiltInName()){
			return true;
		}
		return false;
	}
	
	@Override
	protected void setBookType(SBook book){
		book.setAttribute(BOOK_TYPE_KEY, "xlsx");
	}

	@Override
	protected void importPassword(Sheet poiSheet, SSheet sheet) {
		short hashpass = ((XSSFSheet)poiSheet).getPasswordHash(); 
		sheet.setHashedPassword(hashpass);
		
		//ZSS-1063
		String hashValue = ((XSSFSheet)poiSheet).getHashValue();
		sheet.setHashValue(hashValue);
		String spinCount = ((XSSFSheet)poiSheet).getSpinCount();
		sheet.setSpinCount(spinCount);
		String saltValue = ((XSSFSheet)poiSheet).getSaltValue();
		sheet.setSaltValue(saltValue);
		String algName = ((XSSFSheet)poiSheet).getAlgName();
		sheet.setAlgName(algName);
	}
	
	@Override
	protected void importSheetProtection(Sheet poiSheet, SSheet sheet) { //ZSS-576
		SheetProtection sp = poiSheet.getOrCreateSheetProtection();
		SSheetProtection ssp = sheet.getSheetProtection();
		
	    ssp.setAutoFilter(sp.isAutoFilter());
	    ssp.setDeleteColumns(sp.isDeleteColumns());
	    ssp.setDeleteRows(sp.isDeleteRows());
	    ssp.setFormatCells(sp.isFormatCells());
	    ssp.setFormatColumns(sp.isFormatColumns());
	    ssp.setFormatRows(sp.isFormatRows());
	    ssp.setInsertColumns(sp.isInsertColumns());
	    ssp.setInsertHyperlinks(sp.isInsertHyperlinks());
	    ssp.setInsertRows(sp.isInsertRows());
	    ssp.setPivotTables(sp.isPivotTables());
	    ssp.setSort(sp.isSort());
	    ssp.setObjects(sp.isObjects());
	    ssp.setScenarios(sp.isScenarios());
	    ssp.setSelectLockedCells(sp.isSelectLockedCells());
	    ssp.setSelectUnlockedCells(sp.isSelectUnlockedCells());
	}

	//ZSS-952
	@Override
	protected void importSheetDefaultColumnWidth(Sheet poiSheet, SSheet sheet) {
		// reference XUtils.getDefaultColumnWidthInPx()
		int defaultWidth = UnitUtil.xssfDefaultColumnWidthToPx(((XSSFSheet)poiSheet).getXssfDefaultColumnWidth(), CHRACTER_WIDTH);
		sheet.setDefaultColumnWidth(defaultWidth);
	}

	//ZSS-855
	@Override
	protected void importTables(Sheet poiSheet, SSheet sheet) {
		final XSSFSheet srcSheet = (XSSFSheet) poiSheet;
		for (XSSFTable poiTable : srcSheet.getTables()) {
			final CellReference cr1 = poiTable.getStartCellReference();
			final CellReference cr2 = poiTable.getEndCellReference();
			final CellRegion region0 = new CellRegion(cr1.getRow(), cr1.getCol(), cr2.getRow(), cr2.getCol());
			final SheetRegion region  = new SheetRegion(sheet, region0);
			final XSSFTableStyleInfo poiInfo = new XSSFTableStyleInfo(poiTable.getTableStyleInfo());
			final STableStyleInfo info = 
				new TableStyleInfoImpl(poiInfo.getName(), 
					poiInfo.isShowColumnStripes(), poiInfo.isShowRowStripes(),
					poiInfo.isShowFirstColumn(), poiInfo.isShowLastColumn());
 
			final STable table = new TableImpl((AbstractBookAdv)book,
				poiTable.getName(),
				poiTable.getDisplayName(),
				region,
				poiTable.getHeaderRowCount(),
				poiTable.getTotalsRowCount(),
				info);
			
			for (XSSFTableColumn poiTbCol : poiTable.getTableColumns()) {
				final STableColumn tbCol = new TableColumnImpl(poiTbCol.getName());
				tbCol.setTotalsRowFunction(STotalsRowFunction.values()[poiTbCol.getTotalsRowFunction().ordinal()]);
				if (tbCol.getTotalsRowFunction() == STotalsRowFunction.none)
					tbCol.setTotalsRowLabel(poiTbCol.getTotalsRowLabel());
				else if (tbCol.getTotalsRowFunction() == STotalsRowFunction.custom)
					tbCol.setTotalsRowFormula(poiTbCol.getTotalsRowFormula()); //ZSS-977
				table.addColumn(tbCol);
			}
			
			final XSSFAutoFilter poiFilter = poiTable.getAutoFilter();
			table.enableAutoFilter(poiFilter != null);
			
			sheet.addTable(table);

			//ZSS-1019
			if (poiFilter != null) {
				final int numberOfColumn = region.getColumnCount();
				final SAutoFilter zssFilter = table.getAutoFilter();
				importAutoFilterColumns(poiFilter, zssFilter, numberOfColumn);
			}
			
			importTableName(table);
			((AbstractBookAdv)book).addTable(table);
		}
	}
	
	//ZSS-855
	protected void importTableName(STable table) {
		SName namedRange = ((AbstractBookAdv)book).createTableName(table);
		SheetRegion rgn = table.getDataRegion();
		namedRange.setRefersToFormula(rgn.getReferenceString());
	}
}
 
