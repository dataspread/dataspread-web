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
package org.zkoss.zss.range.impl.imexp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SBooks;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SChart;
import org.zkoss.zss.model.SDataValidation;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.ViewAnchor;
import org.zkoss.zss.model.SCellStyle.Alignment;
import org.zkoss.zss.model.SBorder.BorderType;
import org.zkoss.zss.model.SFill.FillPattern;
import org.zkoss.zss.model.SCellStyle.VerticalAlignment;
import org.zkoss.zss.model.SChart.ChartLegendPosition;
import org.zkoss.zss.model.SChart.ChartType;
import org.zkoss.zss.model.SDataValidation.ValidationType;
import org.zkoss.zss.model.SPicture.Format;
import org.zkoss.zss.model.chart.SGeneralChartData;
import org.zkoss.zss.model.chart.SSeries;
import org.zkoss.zss.model.impl.CellValue;
import org.zkoss.zss.range.SImporterFactory;
import org.zkoss.zss.range.SImporter;
import org.zkoss.zss.range.SRanges;
import org.zkoss.zss.range.impl.StyleUtil;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public class TestImporterFactory implements SImporterFactory{

	static SBook book;//test test share
	
	@Override
	public SImporter createImporter() {
		return new AbstractImporter() {
			
			@Override
			public SBook imports(InputStream is, String bookName) throws IOException {
//				if(book!=null){
//					return book;
//				}
//				book = NBooks.createBook(bookName);
//				book.setShareScope("application");
				
				SBook book = SBooks.createBook(bookName);
				
//				buildTest(book);
//				if(true){
//					return book;
//				}
				
				buildInsertDeleteTest(book);
				
				buildSheetRename(book);
				
				buildCopyPaste(book);
				
				buildMove(book);
				
				buildMerge(book);
				
				buildAutoFilter(book);
				
				buildValidation(book);
				
				buildChartSheet(book);
				
				buildNormalSheet(book);

				buildFreeze(book);
				
				return book;
			}
			
			private void buildTest(SBook book) {
				SSheet sheet1 = book.createSheet("Sheet1");
				SChart chart = sheet1.addChart(ChartType.LINE, new ViewAnchor(0, 9, 600, 400));
				chart.setLegendPosition(ChartLegendPosition.RIGHT);
				SGeneralChartData data = (SGeneralChartData)chart.getData();
				sheet1.getCell("A1").setValue("A");
				sheet1.getCell("B1").setValue("B");
				sheet1.getCell("C1").setValue("C");
				sheet1.getCell("A2").setValue("KK");
				sheet1.getCell("A3").setValue(1);
				sheet1.getCell("B3").setValue(2);
				sheet1.getCell("C3").setValue(3);
				data.setCategoriesFormula("A1:C1");
				data.addSeries().setFormula("A2", "Sheet1!A3:C3");
			}
			private void buildInsertDeleteTest(SBook book) {
				SSheet sheet1 = book.createSheet("Insert Delete");
				sheet1.getCell("C10").setValue(1);
				sheet1.getCell("D10").setValue(2);
				sheet1.getCell("E10").setValue(3);
				sheet1.getCell("F10").setValue(4);
				sheet1.getCell("G10").setValue(5);
				
				sheet1.getCell("H10").setValue("=SUM(C10:G10)");
			}
			private void buildSheetRename(SBook book) {
				SSheet sheet1 = book.createSheet("SheetRename1");
				sheet1.getCell("A1").setValue(1);
				sheet1.getCell("B1").setValue(2);
				sheet1.getCell("C1").setValue("=SUM(A1:B1)");
				sheet1.getCell("D1").setValue("=SUM(SheetRename1!A1:B1)");
				SSheet sheet2 = book.createSheet("SheetRename2");
				sheet2.getCell("A1").setValue(3);
				sheet2.getCell("B1").setValue(4);
				sheet2.getCell("C1").setValue("=SUM(A1:B1)");
				sheet2.getCell("D1").setValue("=SUM(SheetRename1!A1:B1)");
				sheet2.getCell("E1").setValue("=SUM(SheetRename2!A1:B1)");
			}
			
			private void buildCopyPaste(SBook book) {
				SSheet sheet1 = book.createSheet("CopyPaste");
				
				SCellStyle totalStyle = book.createCellStyle(true);
				totalStyle.setDataFormat("#,000.0");
				totalStyle.setBorderBottom(BorderType.MEDIUM,book.createColor("#FF0000"));
				
				SCellStyle headerStyle = book.createCellStyle(true);
				headerStyle.setBackColor(book.createColor("#AAAAAA"));
				headerStyle.setFillPattern(FillPattern.SOLID);
				headerStyle.setAlignment(Alignment.CENTER);
				
				sheet1.getColumn(3).setWidth(150);
				
				
				sheet1.getCell("D4").setValue("Sales by Region");
				sheet1.getCell("D4").setCellStyle(headerStyle);
				
				sheet1.getCell("D5").setValue("Q1");
				sheet1.getCell("D6").setValue("Q2");
				sheet1.getCell("D7").setValue("Q3");
				sheet1.getCell("D8").setValue("Q4");
				sheet1.getCell("E4").setValue("Eurpoe");
				sheet1.getCell("E4").setCellStyle(headerStyle);
				sheet1.getCell("E5").setValue(10);
				StyleUtil.setDataFormat(book,sheet1.getCell(4, 4), "##.0");
				sheet1.getCell("E6").setValue(30);
				StyleUtil.setDataFormat(book,sheet1.getCell(5, 4), "##.0");
				sheet1.getCell("E7").setValue(50);
				StyleUtil.setDataFormat(book,sheet1.getCell(6, 4), "##.0");
				sheet1.getCell("E8").setValue(70);
				StyleUtil.setDataFormat(book,sheet1.getCell(7, 4), "##.0");
				sheet1.getCell("F4").setValue("Asia");
				sheet1.getCell("F4").setCellStyle(headerStyle);
				sheet1.getCell("F5").setValue(20);
				StyleUtil.setDataFormat(book,sheet1.getCell(4, 5), "##.0");
				sheet1.getCell("F6").setValue(40);
				StyleUtil.setDataFormat(book,sheet1.getCell(5, 5), "##.0");
				sheet1.getCell("F7").setValue(60);
				StyleUtil.setDataFormat(book,sheet1.getCell(6, 5), "##.0");
				sheet1.getCell("F8").setValue(80);
				StyleUtil.setDataFormat(book,sheet1.getCell(7, 5), "##.0");
				sheet1.getCell("G4").setValue("Total");
				sheet1.getCell("G4").setCellStyle(headerStyle);
				
				sheet1.getCell("G5").setValue("=SUM(E5:F5)");
				sheet1.getCell("G5").setCellStyle(totalStyle);
				
				sheet1.getCell("G6").setValue("=SUM(E6:F6)");
				sheet1.getCell("G6").setCellStyle(totalStyle);
				
				sheet1.getCell("G7").setValue("=SUM(E7:F7)");
				sheet1.getCell("G7").setCellStyle(totalStyle);
				
				sheet1.getCell("G8").setValue("=SUM(E8:F8)");
				sheet1.getCell("G8").setCellStyle(totalStyle);

				
				SChart chart = sheet1.addChart(ChartType.LINE, new ViewAnchor(0, 9, 600, 400));
				chart.setLegendPosition(ChartLegendPosition.RIGHT);
				SGeneralChartData data = (SGeneralChartData)chart.getData();
				data.setCategoriesFormula("D5:D8");
				data.addSeries().setFormula("E4", "E5:E8");
				data.addSeries().setFormula("F4", "F5:F8");
				data.addSeries().setFormula("G4", "G5:G8");
				
			}
			private void buildMove(SBook book) {
				SSheet sheet = book.createSheet("Move");
				
				sheet.getCell("A1").setValue(3);
				
				sheet.getCell("C3").setValue("=A1");
				
				sheet.getCell("A10").setValue("=C3");
				sheet.getCell("G1").setValue("=C3");
				sheet.getCell("G10").setValue("=C3");
			}
			private void buildMerge(SBook book) {
				SSheet sheet = book.createSheet("Merge");
				
				sheet.getCell("A1").setValue("ABC");
				
				sheet.getCell("C3").setValue("C3");
				sheet.getCell("C4").setValue("C4");
				sheet.getCell("C5").setValue("C5");
				sheet.getCell("C6").setValue("C6");
				
				sheet.getCell("D3").setValue("D3");
				sheet.getCell("D4").setValue("=A1");
				sheet.getCell("D6").setValue("C6");
				
				sheet.getCell("E3").setValue("E3");
				sheet.getCell("E6").setValue("E6");
				
				sheet.getCell("F3").setValue("F3");
				sheet.getCell("F6").setValue("F6");
				
				sheet.getCell("G3").setValue("G3");
				sheet.getCell("G4").setValue("G4");
				sheet.getCell("G5").setValue("G5");
				sheet.getCell("G6").setValue("G6");
				
				sheet.addMergedRegion(new CellRegion("D4:F5"));
				
				SCellStyle style = book.createCellStyle(true);
				style.setFillPattern(FillPattern.SOLID);
				style.setBackColor(book.createColor("#FF0000"));
				sheet.getCell("D4").setCellStyle(style);
				
				
				sheet.addMergedRegion(new CellRegion("I4:K5"));
				
				style = book.createCellStyle(true);
				style.setFillPattern(FillPattern.SOLID);
				style.setBackColor(book.createColor("#FFFF00"));
				sheet.getCell("I4").setCellStyle(style);
				
				
				
				style = book.createCellStyle(true);
				style.setFillPattern(FillPattern.SOLID);
				style.setBackColor(book.createColor("#FF00FF"));
				sheet.getCell("D10").setCellStyle(style);
				sheet.getCell("D10").setValue("D10");
				sheet.getCell("D11").setValue("D11");
				
			}
			private void buildAutoFilter(SBook book) {
				SSheet sheet = book.createSheet("AutoFilter");
				sheet.getCell("D7").setValue("A");
				sheet.getCell("D8").setValue("B");
				sheet.getCell("D9").setValue("C");
				sheet.getCell("D10").setValue("B");
				sheet.getCell("D11").setValue("A");
				sheet.getCell("D12").setValue("K");
				
				sheet.getCell("E7").setValue(1);
				sheet.getCell("E8").setValue(2);
				sheet.getCell("E9").setValue(3);
				sheet.getCell("E10").setValue(1);
				sheet.getCell("E11").setValue(3);
				
				sheet.getCell("F7").setValue(4);
				sheet.getCell("F8").setValue(5);
				sheet.getCell("F9").setValue(6);
				sheet.getCell("F10").setValue(5);
				sheet.getCell("F11").setValue(5);
				
				sheet.getCell("G7").setValue("=SUM(E7:F7)");
				sheet.getCell("G8").setValue("=SUM(E8:F8)");
				sheet.getCell("G9").setValue("=SUM(E9:F9)");
				sheet.getCell("G10").setValue("=SUM(E7:F7)");
				sheet.getCell("G11").setValue("=SUM(E9:F9)");
				
				
				
				SRanges.range(sheet,"H7").setEditText("2013/1/1");
				SRanges.range(sheet,"H8").setEditText("2013/1/2");
				SRanges.range(sheet,"H9").setEditText("2013/1/3");
				SRanges.range(sheet,"H10").setEditText("2013/1/1");
				SRanges.range(sheet,"H11").setEditText("2013/1/2");
				
				
				SRanges.range(sheet,"D7").enableAutoFilter(true);

			}
			private void buildValidation(SBook book) {
				SSheet sheet1 = book.createSheet("Data Validtaion");
				
				SRanges.range(sheet1,"A1").setEditText("A");
				SRanges.range(sheet1,"B1").setEditText("B");
				SRanges.range(sheet1,"C1").setEditText("C");
				SRanges.range(sheet1,"A2").setEditText("1");
				SRanges.range(sheet1,"B2").setEditText("2");
				SRanges.range(sheet1,"C2").setEditText("3");
				SRanges.range(sheet1,"A3").setEditText("2013/1/1");
				SRanges.range(sheet1,"B3").setEditText("2013/1/2");
				SRanges.range(sheet1,"C3").setEditText("2013/1/3");
				
				SDataValidation dv0 = sheet1.addDataValidation(new CellRegion("D1"));
				sheet1.getCell("E1").setValue("<A1:C1");
				dv0.setValidationType(ValidationType.LIST);
				dv0.setShowInput(true);
				dv0.setInputTitle("select form A1:C1");
				dv0.setInputMessage("you should select the value in A1:C1");
				dv0.setShowError(true);
				dv0.setErrorTitle("Not in the list");
				dv0.setErrorMessage("The value must in the list");
				dv0.setInCellDropdown(true);
				dv0.setFormula1("=A1:C1");
				
				sheet1.addDataValidation(new CellRegion("F1:K1"),dv0);//test multiple place)
				sheet1.getCell("L1").setValue("<F1:K1 by A1:C1");
				
				SDataValidation dv1 = sheet1.addDataValidation(new CellRegion("D2"));
				sheet1.getCell("E2").setValue("<A2:C2");
				dv1.setValidationType(ValidationType.LIST);
				dv1.setFormula1("=A2:C2");
				dv1.setShowError(true);
				dv1.setErrorTitle("Not in the list");
				dv1.setErrorMessage("The value must in the list A2:C2");
				dv1.setInCellDropdown(true);
				
				
				SDataValidation dv2 = sheet1.addDataValidation(new CellRegion("D3"));
				sheet1.getCell("E3").setValue("<A3:C3");
				dv2.setValidationType(ValidationType.LIST);
				dv2.setFormula1("=A3:C3");
				dv2.setInCellDropdown(true);
				dv2.setShowError(true);
				
				SDataValidation dv3 = sheet1.addDataValidation(new CellRegion("D4"));
				sheet1.getCell("E4").setValue("<A1:C3");
				dv3.setValidationType(ValidationType.LIST);
				dv3.setFormula1("=A1:C3");
				dv3.setInCellDropdown(true);
				dv3.setShowError(true);
				
			}

			private void buildFreeze(SBook book) {
				SSheet sheet = book.createSheet("Freeze");
				
				sheet.getViewInfo().setNumOfColumnFreeze(5);
				sheet.getViewInfo().setNumOfRowFreeze(7);
//				sheet.addPicture(Format.JPG, getTestImageData(), new NViewAnchor(3, 3, 600, 300));
			}

			private void buildChartSheet(SBook book) {
				SSheet sheet = book.createSheet("Chart");
				
				sheet.getViewInfo().setNumOfRowFreeze(6);
				
				sheet.getCell(0, 0).setValue("My Series");
				sheet.getCell(0, 1).setValue("Volumn");
				sheet.getCell(0, 2).setValue("Open");
				sheet.getCell(0, 3).setValue("High");
				sheet.getCell(0, 4).setValue("Low");
				sheet.getCell(0, 5).setValue("Close");
				
				
				sheet.getCell(1, 0).setValue("A");
				sheet.getCell(2, 0).setValue("B");
				sheet.getCell(3, 0).setValue("C");
				sheet.getCell(4, 0).setValue("D");
				sheet.getCell(5, 0).setValue("E");
				sheet.getCell(6, 0).setValue("F");
				
				SCellStyle style = book.createCellStyle(true);
				style.setDataFormat("yyyy/m/d");
				sheet.getCell(1, 7).setValue(newDate("2013/1/1"));
				sheet.getCell(1, 7).setCellStyle(style);
				sheet.getCell(2, 7).setValue(newDate("2013/1/2"));
				sheet.getCell(2, 7).setCellStyle(style);
				sheet.getCell(3, 7).setValue(newDate("2013/1/3"));
				sheet.getCell(3, 7).setCellStyle(style);
				sheet.getCell(4, 7).setValue(newDate("2013/1/4"));
				sheet.getCell(4, 7).setCellStyle(style);
				sheet.getCell(5, 7).setValue(newDate("2013/1/5"));
				sheet.getCell(5, 7).setCellStyle(style);
				sheet.getCell(6, 7).setValue(newDate("2013/1/6"));
				sheet.getCell(6, 7).setCellStyle(style);
				
				sheet.getCell(1, 1).setValue(1);
				sheet.getCell(2, 1).setValue(2);
				sheet.getCell(3, 1).setValue(3);
				sheet.getCell(4, 1).setValue(1);
				sheet.getCell(5, 1).setValue(2);
				sheet.getCell(6, 1).setValue(3);
				
				sheet.getCell(1, 2).setValue(4);
				sheet.getCell(2, 2).setValue(5);
				sheet.getCell(3, 2).setValue(6);
				sheet.getCell(4, 2).setValue(1);
				sheet.getCell(5, 2).setValue(2);
				sheet.getCell(6, 2).setValue(3);
				
				sheet.getCell(1, 3).setValue(7);
				sheet.getCell(2, 3).setValue(8);
				sheet.getCell(3, 3).setValue(9);
				sheet.getCell(4, 3).setValue(2);
				sheet.getCell(5, 3).setValue(2);
				sheet.getCell(6, 3).setValue(3);
				
				sheet.getCell(1, 4).setValue(1);
				sheet.getCell(2, 4).setValue(3);
				sheet.getCell(3, 4).setValue(5);
				sheet.getCell(4, 4).setValue(2);
				sheet.getCell(5, 4).setValue(2);
				sheet.getCell(6, 4).setValue(3);
				
				sheet.getCell(1, 5).setValue(2);
				sheet.getCell(2, 5).setValue(6);
				sheet.getCell(3, 5).setValue(9);
				sheet.getCell(4, 5).setValue(3);
				sheet.getCell(5, 5).setValue(2);
				sheet.getCell(6, 5).setValue(3);
				
				sheet.getCell(1, 6).setValue(1);
				sheet.getCell(2, 6).setValue(4);
				sheet.getCell(3, 6).setValue(8);
				sheet.getCell(4, 6).setValue(3);
				sheet.getCell(5, 6).setValue(2);
				sheet.getCell(6, 6).setValue(3);
				
				
				SChart chart = sheet.addChart(ChartType.PIE, new ViewAnchor(1, 12, 300, 200));
				buildChartData(chart);
				chart.setLegendPosition(ChartLegendPosition.RIGHT);
				
				chart = sheet.addChart(ChartType.PIE, new ViewAnchor(1, 18, 300, 200));
				buildChartData(chart);
				chart.setTitle("Another Title");
				chart.setThreeD(true);
				chart.setRotX(30);
				chart.setPerspective(30);
				
				chart = sheet.addChart(ChartType.BAR, new ViewAnchor(12, 0, 300, 200));
				buildChartData(chart);
				chart.setLegendPosition(ChartLegendPosition.BOTTOM);
				
				chart = sheet.addChart(ChartType.BAR, new ViewAnchor(12, 6, 300, 200));
				buildChartData(chart);
				chart.setThreeD(true);
				chart.setRightAngleAxes(true);
				
				chart = sheet.addChart(ChartType.COLUMN, new ViewAnchor(12, 12, 300, 200));
				buildChartData(chart);
				chart.setLegendPosition(ChartLegendPosition.LEFT);
				
				chart = sheet.addChart(ChartType.COLUMN, new ViewAnchor(12, 18, 300, 200));
				buildChartData(chart);
				chart.setThreeD(true);
				chart.setRightAngleAxes(true);
				
				chart = sheet.addChart(ChartType.DOUGHNUT, new ViewAnchor(12, 18, 300, 200));
				buildChartData(chart);
				chart.setLegendPosition(ChartLegendPosition.RIGHT);
				
				
				chart = sheet.addChart(ChartType.LINE, new ViewAnchor(24, 0, 300, 200));
				buildChartData(chart);
				chart.setLegendPosition(ChartLegendPosition.TOP);
				
				chart = sheet.addChart(ChartType.LINE, new ViewAnchor(24, 6, 300, 200));
				buildChartData(chart);
				chart.setThreeD(true);
				chart.setPerspective(30);
				
				chart = sheet.addChart(ChartType.AREA, new ViewAnchor(24, 12, 300, 200));
				buildChartData(chart);
				
				chart = sheet.addChart(ChartType.SCATTER, new ViewAnchor(36, 0, 300, 200));
				buildScatterChartData(chart);
				chart.setLegendPosition(ChartLegendPosition.RIGHT);
				
				chart = sheet.addChart(ChartType.BUBBLE, new ViewAnchor(36, 6, 300, 200));
				buildBubbleChartData(chart);
				chart.setLegendPosition(ChartLegendPosition.RIGHT);
//				
				chart = sheet.addChart(ChartType.STOCK, new ViewAnchor(36, 12, 600, 200));
				buildStockChartData(chart);
				
				
			}
			
			private Date newDate(String string) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
				try {
					return sdf.parse(string);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				return null;
			}

			private void buildStockChartData(SChart chart){//X,Y
				SGeneralChartData data = (SGeneralChartData)chart.getData();
				data.setCategoriesFormula("H2:H7");
				SSeries series = data.addSeries();//volumn
				series.setFormula("B1", "B2:B7");
				series = data.addSeries();
				series.setFormula("C1", "C2:C7");//open
				series = data.addSeries();
				series.setFormula("D1", "D2:D7");//high
				series = data.addSeries();
				series.setFormula("E1", "E2:E7");//low
				series = data.addSeries();
				series.setFormula("F1", "F2:F7");//close
			}
			
			private void buildBubbleChartData(SChart chart){//X,Y
				SGeneralChartData data = (SGeneralChartData)chart.getData();
				SSeries series = data.addSeries();
				series.setXYZFormula("A3", "B2:G2", "B3:G3", "B5:G5");
				series = data.addSeries();
				series.setXYZFormula("A4", "B2:G2", "B4:G4", "B5:G5");
			}
			
			private void buildScatterChartData(SChart chart){//X,Y
				SGeneralChartData data = (SGeneralChartData)chart.getData();
				SSeries series = data.addSeries();
				series.setXYFormula("A3", "B2:G2", "B3:G3");
				series = data.addSeries();
				series.setXYFormula("A4", "B2:G2", "B4:G4");
			}			
			
			private void buildChartData(SChart chart){
				SGeneralChartData data = (SGeneralChartData)chart.getData();
				data.setCategoriesFormula("A2:A4");//A,B,C
				SSeries series = data.addSeries();
				series.setXYFormula("A1", "B2:B4", null);
				series = data.addSeries();
				series.setXYFormula("\"Series 2\"", "C2:C4", null);
				series = data.addSeries();
				series.setXYFormula(null, "D2:D4", null);
			}

			private void buildNormalSheet(SBook book) {
				SSheet sheet = book.createSheet("Sheet 1");
				sheet.getColumn(0).setWidth(120);
				sheet.getColumn(1).setWidth(120);
				sheet.getColumn(2).setWidth(120);
				sheet.getColumn(3).setWidth(120);
				sheet.getColumn(4).setWidth(120);
				sheet.getColumn(5).setWidth(120);
				sheet.getColumn(6).setWidth(120);
				
				sheet.getCell(0,11).setStringValue("Column M,O is hidden");
				sheet.getColumn(12).setHidden(true);
				sheet.getColumn(14).setHidden(true);
				
				sheet.getCell(15,0).setStringValue("Row 17,19 is hidden");
				sheet.getRow(16).setHidden(true);
				sheet.getRow(18).setHidden(true);
				
				SCellStyle style;
				SCell cell;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				Date now = new Date();
				Date dayonly = null;
				try {
					dayonly = sdf.parse(sdf.format(now));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				(cell = sheet.getCell(0, 0)).setValue("Values:");
				(cell = sheet.getCell(0, 1)).setValue(123.45);
				(cell = sheet.getCell(0, 2)).setValue(now);
				(cell = sheet.getCell(0, 3)).setValue(Boolean.TRUE);
				
				(cell = sheet.getCell(1, 0)).setValue("Number Format:");
				(cell = sheet.getCell(1, 1)).setValue(33);
				style = book.createCellStyle(true);
				style.setDataFormat("0.00");
				cell.setCellStyle(style);

				(cell = sheet.getCell(1, 2)).setValue(44.55);
				style = book.createCellStyle(true);
				style.setDataFormat("$#,##0.0");
				cell.setCellStyle(style);
				
				
				(cell = sheet.getCell(1, 3)).setValue(77.88);
				style = book.createCellStyle(true);
				style.setDataFormat("0.00;[Red]0.00");
				cell.setCellStyle(style);
				
				(cell = sheet.getCell(1, 4)).setValue(-77.88);
				style = book.createCellStyle(true);
				style.setDataFormat("0.00;[Red]0.00");
				cell.setCellStyle(style);
				
				
				(cell = sheet.getCell(2, 0)).setValue("Date Format:");
				(cell = sheet.getCell(2, 1)).setValue(dayonly);
				style = book.createCellStyle(true);
				style.setDataFormat("yyyy/m/d");
				cell.setCellStyle(style);
				
				(cell = sheet.getCell(2, 2)).setValue(dayonly);
				style = book.createCellStyle(true);
				style.setDataFormat("m/d/yyy");
				cell.setCellStyle(style);
				
				(cell = sheet.getCell(2, 3)).setValue(now);
				style = book.createCellStyle(true);
				style.setDataFormat("m/d/yy h:mm;@");
				cell.setCellStyle(style);
				
				(cell = sheet.getCell(2, 4)).setValue(now);
				style = book.createCellStyle(true);
				style.setDataFormat("h:mm AM/PM;@");
				cell.setCellStyle(style);
				
				//
				(cell = sheet.getCell(3, 0)).setValue("Formula:");
				(cell = sheet.getCell(3, 1)).setNumberValue(1D);
				(cell = sheet.getCell(3, 2)).setNumberValue(2D);
				(cell = sheet.getCell(3, 3)).setNumberValue(3D);
				(cell = sheet.getCell(3, 4)).setFormulaValue("SUM(B4:D4)");
				
				(cell = sheet.getCell(4, 0)).setStringValue("this is a long long long long long string");
				
				(cell = sheet.getCell(5, 0)).setStringValue("merege A6:C6");
				sheet.addMergedRegion(new CellRegion(5,0,5,2));
				(cell = sheet.getCell(5, 3)).setStringValue("merege D6:E7");
				sheet.addMergedRegion(new CellRegion(5,3,6,4));
				
				
				cell = sheet.getCell(9, 6);
				cell.setStringValue("G9");
				style = book.createCellStyle(true);
				style.setFont(book.createFont(true));
				style.getFont().setColor(book.createColor("#FF0000"));
				style.getFont().setHeightPoints(16);
				style.setFillPattern(FillPattern.SOLID);
				style.setBackColor(book.createColor("#AAAAAA"));
				
				
				sheet.getColumn(6).setWidth(150);
				sheet.getRow(9).setHeight(100);
				
				style.setAlignment(Alignment.RIGHT);
				style.setVerticalAlignment(VerticalAlignment.CENTER);
				
				style.setBorderTop(BorderType.THIN);
				style.setBorderBottom(BorderType.THIN);
				style.setBorderLeft(BorderType.THIN);
				style.setBorderRight(BorderType.THIN);
				style.setBorderTopColor(book.createColor("#FF0000"));
				style.setBorderBottomColor(book.createColor("#FFFF00"));
				style.setBorderLeftColor(book.createColor("#FF00FF"));
				style.setBorderRightColor(book.createColor("#00FFFF"));
				
				cell.setCellStyle(style);
				
				//row/column style
				
				style = book.createCellStyle(true);
				style.setFillPattern(FillPattern.SOLID);
				style.setBackColor(book.createColor("#FFAAAA"));
				sheet.getRow(17).setCellStyle(style);
				sheet.getCell(17, 0).setStringValue("row style");
				style = book.createCellStyle(true);
				style.setFillPattern(FillPattern.SOLID);
				style.setBackColor(book.createColor("#AAFFAA"));
				sheet.getColumn(17).setCellStyle(style);
				sheet.getColumn(17).setWidth(100);
				sheet.getCell(0, 17).setStringValue("column style");
				
				
				
//				sheet.addPicture(Format.JPG, getTestImageData(), new NViewAnchor(12, 3, 30, 5, 600, 300));
				
				
				sheet = book.createSheet("Sheet 2");
				sheet.getCell(0, 0).setValue("=SUM('Sheet 1'!B4:D4)");
			}


			private byte[] getTestImageData() {
				InputStream is = null;
				try{
					is = getClass().getResourceAsStream("test.jpg");
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					byte[] b = new byte[1024];
					int r;
					while( (r = is.read(b)) !=-1){
						os.write(b,0,r);
					}
					return os.toByteArray();
				} catch (IOException e) {
					throw new RuntimeException(e.getMessage(),e);
				}finally{
					if(is!=null){
						try {
							is.close();
						} catch (IOException e) {}
					}
				}
			}
		};
	}

}
