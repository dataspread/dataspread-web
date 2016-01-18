/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2014/1/24 , Created by Kuro
}}IS_NOTE

Copyright (C) 2014 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/

package org.zkoss.zss.range.impl.imexp;

import org.zkoss.poi.common.usermodel.Hyperlink;
import org.zkoss.poi.ss.usermodel.*;
import org.zkoss.poi.ss.usermodel.charts.*;
import org.zkoss.poi.xssf.usermodel.XSSFWorkbook;
import org.zkoss.zss.model.SAutoFilter.FilterOp;
import org.zkoss.zss.model.SCellStyle.Alignment;
import org.zkoss.zss.model.SBorder.BorderType;
import org.zkoss.zss.model.SFill.FillPattern;
import org.zkoss.zss.model.SCellStyle.VerticalAlignment;
import org.zkoss.zss.model.SChart.BarDirection;
import org.zkoss.zss.model.SChart.ChartLegendPosition;
import org.zkoss.zss.model.SChart.ChartGrouping;
import org.zkoss.zss.model.SDataValidation.AlertStyle;
import org.zkoss.zss.model.SDataValidation.OperatorType;
import org.zkoss.zss.model.SDataValidation.ValidationType;
import org.zkoss.zss.model.SFont.Boldweight;
import org.zkoss.zss.model.SFont.TypeOffset;
import org.zkoss.zss.model.SFont.Underline;
import org.zkoss.zss.model.*;
import org.zkoss.zss.model.SHyperlink.HyperlinkType;
import org.zkoss.zss.model.SPicture.Format;

/**
 * An utility to convert enumeration constant between ZSS model and POI model.
 * This utility helps developers who want to write their own importer or exporter based on POI.
 * 
 * @author kuro, Hawk
 * @since 3.5.0
 */
public class PoiEnumConversion {
	
	public static int toPoiHyperlinkType(SHyperlink.HyperlinkType type) {
		switch(type) {
		case DOCUMENT:
			return Hyperlink.LINK_DOCUMENT;
		case EMAIL:
			return Hyperlink.LINK_EMAIL;
		case FILE:
			return Hyperlink.LINK_FILE;
		case URL:
			default:
			return Hyperlink.LINK_URL;
		}
	}
	
	public static SHyperlink.HyperlinkType toHyperlinkType(int type) {
		switch (type) {
		case Hyperlink.LINK_DOCUMENT:
			return HyperlinkType.DOCUMENT;
		case Hyperlink.LINK_EMAIL:
			return HyperlinkType.EMAIL;
		case Hyperlink.LINK_FILE:
			return HyperlinkType.FILE;
		case Hyperlink.LINK_URL:
		default:
			return HyperlinkType.URL;
		}
	}

	public static int toPoiFilterOperator(FilterOp operator){
		switch(operator){
			case AND:
				return AutoFilter.FILTEROP_AND;
			case BOTTOM10:
				return AutoFilter.FILTEROP_BOTTOM10;
			case BOTOOM10_PERCENT:
				return AutoFilter.FILTEROP_BOTOOM10PERCENT;
			case OR:
				return AutoFilter.FILTEROP_OR;
			case TOP10:
				return AutoFilter.FILTEROP_TOP10;
			case TOP10_PERCENT:
				return AutoFilter.FILTEROP_TOP10PERCENT;
			case VALUES:
			default:
				return AutoFilter.FILTEROP_VALUES;
		}
	}

	public static FilterOp toFilterOperator(int operator) {
		switch (operator) {
		case AutoFilter.FILTEROP_AND:
			return FilterOp.AND;
		case AutoFilter.FILTEROP_BOTTOM10:
			return FilterOp.BOTTOM10;
		case AutoFilter.FILTEROP_BOTOOM10PERCENT:
			return FilterOp.BOTOOM10_PERCENT;
		case AutoFilter.FILTEROP_OR:
			return FilterOp.OR;
		case AutoFilter.FILTEROP_TOP10:
			return FilterOp.TOP10;
		case AutoFilter.FILTEROP_TOP10PERCENT:
			return FilterOp.TOP10_PERCENT;
		case AutoFilter.FILTEROP_VALUES:
		default:
			return FilterOp.VALUES;
		}
	}

	public static short toPoiPaperSize(SPrintSetup.PaperSize paperSize) {
		switch(paperSize) {
		case A3:
			return PrintSetup.A3_PAPERSIZE;
		case A4_EXTRA:
			return PrintSetup.A4_EXTRA_PAPERSIZE;
		case A4_PLUS:
			return PrintSetup.A4_PLUS_PAPERSIZE;
		case A4_ROTATED:
			return PrintSetup.A4_ROTATED_PAPERSIZE;
		case A4_SMALL:
			return PrintSetup.A4_SMALL_PAPERSIZE;
		case A4:
			return PrintSetup.A4_PAPERSIZE;
		case A4_TRANSVERSE:
			return PrintSetup.A4_TRANSVERSE_PAPERSIZE;
		case A5:
			return PrintSetup.A5_PAPERSIZE;
		case B4:
			return PrintSetup.B4_PAPERSIZE;
		case B5:
			return PrintSetup.B5_PAPERSIZE;
		case ELEVEN_BY_SEVENTEEN:
			return PrintSetup.ELEVEN_BY_SEVENTEEN_PAPERSIZE;
		case ENVELOPE_10:
			return PrintSetup.ENVELOPE_10_PAPERSIZE;
		case ENVELOPE_9:
			return PrintSetup.ENVELOPE_9_PAPERSIZE;
		case ENVELOPE_C3:
			return PrintSetup.ENVELOPE_C3_PAPERSIZE;
		case ENVELOPE_C4:
			return PrintSetup.ENVELOPE_C4_PAPERSIZE;
		case ENVELOPE_C5:
			return PrintSetup.ENVELOPE_C5_PAPERSIZE;
		case ENVELOPE_C6:
			return PrintSetup.ENVELOPE_C6_PAPERSIZE;
		case ENVELOPE_CS:
			return PrintSetup.ENVELOPE_CS_PAPERSIZE;
		case ENVELOPE_DL:
			return PrintSetup.ENVELOPE_DL_PAPERSIZE;
		case ENVELOPE_MONARCH:
			return PrintSetup.ENVELOPE_MONARCH_PAPERSIZE;
		case EXECUTIVE:
			return PrintSetup.EXECUTIVE_PAPERSIZE;
		case FOLIO8:
			return PrintSetup.FOLIO8_PAPERSIZE;
		case LEDGER:
			return PrintSetup.LEDGER_PAPERSIZE;
		case LEGAL:
			return PrintSetup.LEGAL_PAPERSIZE;
		case LETTER:
			return PrintSetup.LETTER_PAPERSIZE;
		case LETTER_ROTATED:
			return PrintSetup.LETTER_ROTATED_PAPERSIZE;
		case LETTER_SMALL:
			return PrintSetup.LETTER_SMALL_PAGESIZE;
		case NOTE8:
			return PrintSetup.NOTE8_PAPERSIZE;
		case QUARTO:
			return PrintSetup.QUARTO_PAPERSIZE;
		case STATEMENT:
			return PrintSetup.STATEMENT_PAPERSIZE;
		case TABLOID:
			return PrintSetup.TABLOID_PAPERSIZE;
		case TEN_BY_FOURTEEN:
			return PrintSetup.TEN_BY_FOURTEEN_PAPERSIZE;
			default:
				return PrintSetup.A4_PAPERSIZE;
		}
	}
	
	public static SPrintSetup.PaperSize toPaperSize(short paperSize) {
		switch (paperSize) {
		case PrintSetup.A3_PAPERSIZE:
			return SPrintSetup.PaperSize.A3;
		case PrintSetup.A4_EXTRA_PAPERSIZE:
			return SPrintSetup.PaperSize.A4_EXTRA;
		case PrintSetup.A4_PAPERSIZE:
			return SPrintSetup.PaperSize.A4;
		case PrintSetup.A4_PLUS_PAPERSIZE:
			return SPrintSetup.PaperSize.A4_PLUS;
		case PrintSetup.A4_ROTATED_PAPERSIZE:
			return SPrintSetup.PaperSize.A4_ROTATED;
		case PrintSetup.A4_SMALL_PAPERSIZE:
			return SPrintSetup.PaperSize.A4_SMALL;
		case PrintSetup.A4_TRANSVERSE_PAPERSIZE:
			return SPrintSetup.PaperSize.A4_TRANSVERSE;
		case PrintSetup.A5_PAPERSIZE:
			return SPrintSetup.PaperSize.A5;
		case PrintSetup.B4_PAPERSIZE:
			return SPrintSetup.PaperSize.B4;
		case PrintSetup.B5_PAPERSIZE:
			return SPrintSetup.PaperSize.B5;
		case PrintSetup.ELEVEN_BY_SEVENTEEN_PAPERSIZE:
			return SPrintSetup.PaperSize.ELEVEN_BY_SEVENTEEN;
		case PrintSetup.ENVELOPE_10_PAPERSIZE:
			return SPrintSetup.PaperSize.ENVELOPE_10;
		case PrintSetup.ENVELOPE_9_PAPERSIZE:
			return SPrintSetup.PaperSize.ENVELOPE_9;
		case PrintSetup.ENVELOPE_C3_PAPERSIZE:
			return SPrintSetup.PaperSize.ENVELOPE_C3;
		case PrintSetup.ENVELOPE_C4_PAPERSIZE:
			return SPrintSetup.PaperSize.ENVELOPE_C4;
		case PrintSetup.ENVELOPE_C5_PAPERSIZE:
			return SPrintSetup.PaperSize.ENVELOPE_C5;
		case PrintSetup.ENVELOPE_C6_PAPERSIZE:
			return SPrintSetup.PaperSize.ENVELOPE_C6;
		case PrintSetup.ENVELOPE_DL_PAPERSIZE:
			return SPrintSetup.PaperSize.ENVELOPE_DL;
		case PrintSetup.ENVELOPE_MONARCH_PAPERSIZE:
			return SPrintSetup.PaperSize.ENVELOPE_MONARCH;
		case PrintSetup.EXECUTIVE_PAPERSIZE:
			return SPrintSetup.PaperSize.EXECUTIVE;
		case PrintSetup.FOLIO8_PAPERSIZE:
			return SPrintSetup.PaperSize.FOLIO8;
		case PrintSetup.LEDGER_PAPERSIZE:
			return SPrintSetup.PaperSize.LEDGER;
		case PrintSetup.LETTER_PAPERSIZE:
			return SPrintSetup.PaperSize.LETTER;
		case PrintSetup.LETTER_ROTATED_PAPERSIZE:
			return SPrintSetup.PaperSize.LETTER_ROTATED;
		case PrintSetup.LETTER_SMALL_PAGESIZE:
			return SPrintSetup.PaperSize.LETTER_SMALL;
		case PrintSetup.NOTE8_PAPERSIZE:
			return SPrintSetup.PaperSize.NOTE8;
		case PrintSetup.QUARTO_PAPERSIZE:
			return SPrintSetup.PaperSize.QUARTO;
		case PrintSetup.STATEMENT_PAPERSIZE:
			return SPrintSetup.PaperSize.STATEMENT;
		case PrintSetup.TABLOID_PAPERSIZE:
			return SPrintSetup.PaperSize.TABLOID;
		case PrintSetup.TEN_BY_FOURTEEN_PAPERSIZE:
			return SPrintSetup.PaperSize.TEN_BY_FOURTEEN;
		default:
			return SPrintSetup.PaperSize.A4;
		}
	}

	public static short toPoiVerticalAlignment(VerticalAlignment vAlignment) {
		switch(vAlignment) {
			case BOTTOM:
				return CellStyle.VERTICAL_BOTTOM;
			case CENTER:
				return CellStyle.VERTICAL_CENTER;
			case JUSTIFY:
				return CellStyle.VERTICAL_JUSTIFY;
			case TOP:
			default:
				return CellStyle.VERTICAL_TOP;
		}
	}
	
	public static VerticalAlignment toVerticalAlignment(short poiVerticalAlignment) {
		switch (poiVerticalAlignment) {
		case CellStyle.VERTICAL_TOP:
			return VerticalAlignment.TOP;
		case CellStyle.VERTICAL_CENTER:
			return VerticalAlignment.CENTER;
		case CellStyle.VERTICAL_JUSTIFY:
			return VerticalAlignment.JUSTIFY;
		case CellStyle.VERTICAL_BOTTOM:
		default:
			return VerticalAlignment.BOTTOM;
		}
	}

	public static short toPoiFillPattern(FillPattern fillPattern) {
		switch(fillPattern) {
		case DARK_GRAY:
			return CellStyle.ALT_BARS;
		case DARK_GRID:
			return CellStyle.BIG_SPOTS;
		case DARK_TRELLIS:
			return CellStyle.BRICKS;
		case LIGHT_TRELLIS:
			return CellStyle.DIAMONDS;
		case MEDIUM_GRAY:
			return CellStyle.FINE_DOTS;
		case GRAY0625:
			return CellStyle.LEAST_DOTS;
		case GRAY125:
			return CellStyle.LESS_DOTS;
		case SOLID:
			return CellStyle.SOLID_FOREGROUND;
		case LIGHT_GRAY:
			return CellStyle.SPARSE_DOTS;
		case LIGHT_GRID:
			return CellStyle.SQUARES;
		case DARK_DOWN:
			return CellStyle.THICK_BACKWARD_DIAG;
		case DARK_UP:
			return CellStyle.THICK_FORWARD_DIAG;
		case DARK_HORIZONTAL:
			return CellStyle.THICK_HORZ_BANDS;
		case DARK_VERTICAL:
			return CellStyle.THICK_VERT_BANDS;
		case LIGHT_DOWN:
			return CellStyle.THIN_BACKWARD_DIAG;
		case LIGHT_UP:
			return CellStyle.THIN_FORWARD_DIAG;
		case LIGHT_HORIZONTAL:
			return CellStyle.THIN_HORZ_BANDS;
		case LIGHT_VERTICAL:
			return CellStyle.THIN_VERT_BANDS;
		case NONE:
		default:
			return CellStyle.NO_FILL;
		}
	}
	
	public static FillPattern toFillPattern(short poiFillPattern) {
		switch (poiFillPattern) {
		case CellStyle.SOLID_FOREGROUND:
			return FillPattern.SOLID;
		case CellStyle.FINE_DOTS:
			return FillPattern.MEDIUM_GRAY;
		case CellStyle.ALT_BARS:
			return FillPattern.DARK_GRAY;
		case CellStyle.SPARSE_DOTS:
			return FillPattern.LIGHT_GRAY;
		case CellStyle.THICK_HORZ_BANDS:
			return FillPattern.DARK_HORIZONTAL;
		case CellStyle.THICK_VERT_BANDS:
			return FillPattern.DARK_VERTICAL;
		case CellStyle.THICK_BACKWARD_DIAG:
			return FillPattern.DARK_DOWN;
		case CellStyle.THICK_FORWARD_DIAG:
			return FillPattern.DARK_UP;
		case CellStyle.BIG_SPOTS:
			return FillPattern.DARK_GRID;
		case CellStyle.BRICKS:
			return FillPattern.DARK_TRELLIS;
		case CellStyle.THIN_HORZ_BANDS:
			return FillPattern.LIGHT_HORIZONTAL;
		case CellStyle.THIN_VERT_BANDS:
			return FillPattern.LIGHT_VERTICAL;
		case CellStyle.THIN_BACKWARD_DIAG:
			return FillPattern.LIGHT_DOWN;
		case CellStyle.THIN_FORWARD_DIAG:
			return FillPattern.LIGHT_UP;
		case CellStyle.SQUARES:
			return FillPattern.LIGHT_GRID;
		case CellStyle.DIAMONDS:
			return FillPattern.LIGHT_TRELLIS;
		case CellStyle.LESS_DOTS:
			return FillPattern.GRAY125;
		case CellStyle.LEAST_DOTS:
			return FillPattern.GRAY0625;
		case CellStyle.NO_FILL:
		default:
			return FillPattern.NONE;
		}
	}

	public static short toPoiBorderType(BorderType borderType) {
		switch(borderType) {
		case DASH_DOT:
			return CellStyle.BORDER_DASH_DOT;
		case DASHED:
			return CellStyle.BORDER_DASHED;
		case DOTTED:
			return CellStyle.BORDER_DOTTED;
		case DOUBLE:
			return CellStyle.BORDER_DOUBLE;
		case HAIR:
			return CellStyle.BORDER_HAIR;
		case MEDIUM:
			return CellStyle.BORDER_MEDIUM;
		case MEDIUM_DASH_DOT:
			return CellStyle.BORDER_DASH_DOT;
		case MEDIUM_DASH_DOT_DOT:
			return CellStyle.BORDER_DASH_DOT_DOT;
		case MEDIUM_DASHED:
			return CellStyle.BORDER_MEDIUM_DASHED;
		case SLANTED_DASH_DOT:
			return CellStyle.BORDER_SLANTED_DASH_DOT;
		case THICK:
			return CellStyle.BORDER_THICK;
		case THIN:
			return CellStyle.BORDER_THIN;
		case DASH_DOT_DOT:
			return CellStyle.BORDER_DASH_DOT_DOT;
		case NONE:
		default:
			return CellStyle.BORDER_NONE;
		}
	}
	
	public static BorderType toBorderType(short poiBorder) {
		switch (poiBorder) {
		case CellStyle.BORDER_THIN:
			return BorderType.THIN;
		case CellStyle.BORDER_MEDIUM:
			return BorderType.MEDIUM;
		case CellStyle.BORDER_DASHED:
			return BorderType.DASHED;
		case CellStyle.BORDER_HAIR:
			return BorderType.HAIR;
		case CellStyle.BORDER_THICK:
			return BorderType.THICK;
		case CellStyle.BORDER_DOUBLE:
			return BorderType.DOUBLE;
		case CellStyle.BORDER_DOTTED:
			return BorderType.DOTTED;
		case CellStyle.BORDER_MEDIUM_DASHED:
			return BorderType.MEDIUM_DASHED;
		case CellStyle.BORDER_DASH_DOT:
			return BorderType.DASH_DOT;
		case CellStyle.BORDER_MEDIUM_DASH_DOT:
			return BorderType.MEDIUM_DASH_DOT;
		case CellStyle.BORDER_DASH_DOT_DOT:
			return BorderType.DASH_DOT_DOT;
		case CellStyle.BORDER_MEDIUM_DASH_DOT_DOT:
			return BorderType.MEDIUM_DASH_DOT_DOT;
		case CellStyle.BORDER_SLANTED_DASH_DOT:
			return BorderType.SLANTED_DASH_DOT;
		case CellStyle.BORDER_NONE:
		default:
			return BorderType.NONE;
		}
	}

	public static short toPoiHorizontalAlignment(Alignment alignment) {
		switch(alignment) {
		case CENTER:
			return CellStyle.ALIGN_CENTER;
		case FILL:
			return CellStyle.ALIGN_FILL;
		case JUSTIFY:
			return CellStyle.ALIGN_JUSTIFY;
		case RIGHT:
			return CellStyle.ALIGN_RIGHT;
		case LEFT:
			return CellStyle.ALIGN_LEFT;
		case CENTER_SELECTION:
			return CellStyle.ALIGN_CENTER_SELECTION;
		case GENERAL:
			default:
			return CellStyle.ALIGN_GENERAL;
		}
	}
	
	public static Alignment toHorizontalAlignment(short poiHorizontalAlignment) {
		switch (poiHorizontalAlignment) {
		case CellStyle.ALIGN_LEFT:
			return Alignment.LEFT;
		case CellStyle.ALIGN_RIGHT:
			return Alignment.RIGHT;
		case CellStyle.ALIGN_CENTER:
			return Alignment.CENTER;
		case CellStyle.ALIGN_CENTER_SELECTION: //ZSS-779
			return Alignment.CENTER_SELECTION;
		case CellStyle.ALIGN_FILL:
			return Alignment.FILL;
		case CellStyle.ALIGN_JUSTIFY:
			return Alignment.JUSTIFY;
		case CellStyle.ALIGN_GENERAL:
		default:
			return Alignment.GENERAL;
		}
	}

	public static short toPoiBoldweight(Boldweight bold) {
		switch(bold) {
			case BOLD:
				return Font.BOLDWEIGHT_BOLD;
			case NORMAL:
			default:
				return Font.BOLDWEIGHT_NORMAL;
		}
	}
	
	public static Boldweight toBoldweight(short bold) {
		switch(bold) {
			case Font.BOLDWEIGHT_BOLD:
				return Boldweight.BOLD;
			case Font.BOLDWEIGHT_NORMAL:
			default:
				return Boldweight.NORMAL;
		}
	}
	
	public static short toPoiTypeOffset(TypeOffset typeOffset) {
		switch(typeOffset) {
			case SUPER:
				return Font.SS_SUPER;
			case SUB:
				return Font.SS_SUB;
			case NONE:
			default:
				return Font.SS_NONE;
		}
	}
	
	public static TypeOffset toTypeOffset(short typeOffset) {
		switch (typeOffset) {
		case Font.SS_SUB:
			return TypeOffset.SUB;
		case Font.SS_SUPER:
			return TypeOffset.SUPER;
		case Font.SS_NONE:
		default:
			return TypeOffset.NONE;
		}
	}

	public static byte toPoiUnderline(Underline underline) {
		switch(underline) {
			case SINGLE:
				return Font.U_SINGLE;
			case DOUBLE:
				return Font.U_DOUBLE;
			case DOUBLE_ACCOUNTING:
				return Font.U_DOUBLE_ACCOUNTING;
			case SINGLE_ACCOUNTING:
				return Font.U_SINGLE_ACCOUNTING;
			case NONE:
			default:
				return Font.U_NONE;
		}
	}

	/*
	 * reference BookHelper.getFontCSSStyle()
	 */
	public static Underline toUnderline(byte underline) {
		switch (underline) {
		case Font.U_SINGLE:
			return SFont.Underline.SINGLE;
		case Font.U_DOUBLE:
			return SFont.Underline.DOUBLE;
		case Font.U_SINGLE_ACCOUNTING:
			return SFont.Underline.SINGLE_ACCOUNTING;
		case Font.U_DOUBLE_ACCOUNTING:
			return SFont.Underline.DOUBLE_ACCOUNTING;
		case Font.U_NONE:
		default:
			return SFont.Underline.NONE;
		}
	}

	public static int toPoiPictureFormat(Format format){
		switch(format){
		case GIF:
			return XSSFWorkbook.PICTURE_TYPE_GIF;
		case JPG:
			return Workbook.PICTURE_TYPE_JPEG;
		case PNG:
		default:
			return Workbook.PICTURE_TYPE_PNG;
		}
	}
	//There is no format information on POI's Picture class

	public static org.zkoss.poi.ss.usermodel.charts.ChartGrouping toPoiGrouping(ChartGrouping grouping){
		if (grouping == null){ //no API to read grouping from XLS, so chart might have null grouping to export
			return org.zkoss.poi.ss.usermodel.charts.ChartGrouping.STANDARD;
		}
		switch(grouping){
			case CLUSTERED:
				return org.zkoss.poi.ss.usermodel.charts.ChartGrouping.CLUSTERED;
			case PERCENT_STACKED:
				return org.zkoss.poi.ss.usermodel.charts.ChartGrouping.PERCENT_STACKED;
			case STACKED:
				return org.zkoss.poi.ss.usermodel.charts.ChartGrouping.STACKED;
			case STANDARD:
			default:
				return org.zkoss.poi.ss.usermodel.charts.ChartGrouping.STANDARD;
		}
	}

	public static org.zkoss.zss.model.SChart.ChartGrouping 
		toChartGrouping(org.zkoss.poi.ss.usermodel.charts.ChartGrouping grouping){
		if (grouping == null) {
			return org.zkoss.zss.model.SChart.ChartGrouping.STANDARD;
		}
		switch(grouping){
		case STACKED:
			return org.zkoss.zss.model.SChart.ChartGrouping.STACKED;
		case PERCENT_STACKED:
			return org.zkoss.zss.model.SChart.ChartGrouping.PERCENT_STACKED;
		case CLUSTERED:
			return org.zkoss.zss.model.SChart.ChartGrouping.CLUSTERED;
		case STANDARD:
		default:
			return org.zkoss.zss.model.SChart.ChartGrouping.STANDARD;
		}
	}

	public static ChartDirection toPoiBarDirection(BarDirection direction){
		switch(direction){
			case VERTICAL:
				return ChartDirection.VERTICAL;
			case HORIZONTAL:
			default:
				return ChartDirection.HORIZONTAL;
		}
		
	}

	public static BarDirection toBarDirection(ChartDirection direction){
		switch(direction){
		case VERTICAL:
			return BarDirection.VERTICAL;
		case HORIZONTAL:
		default:
			return BarDirection.HORIZONTAL;
		}
	}

	public static LegendPosition toPoiLegendPosition(ChartLegendPosition position){
		switch(position){
			case BOTTOM:
				return LegendPosition.BOTTOM;
			case TOP:
				return LegendPosition.TOP;
			case TOP_RIGHT:
				return LegendPosition.TOP_RIGHT;
			case LEFT:
				return LegendPosition.LEFT;
			case RIGHT:
			default:
				return LegendPosition.RIGHT;
			
		}
	}

	public static ChartLegendPosition toLengendPosition(LegendPosition position){
		switch(position){
		case BOTTOM:
			return ChartLegendPosition.BOTTOM;
		case LEFT:
			return ChartLegendPosition.LEFT;
		case TOP:
			return ChartLegendPosition.TOP;
		case TOP_RIGHT:
			return ChartLegendPosition.TOP_RIGHT;
		case RIGHT:
		default:
			return ChartLegendPosition.RIGHT;
		}
		
	}

	public static int toPoiOperatorType(OperatorType type){
		switch (type) {
			case NOT_EQUAL:
				return DataValidationConstraint.OperatorType.NOT_EQUAL;
			case NOT_BETWEEN:	
				return DataValidationConstraint.OperatorType.NOT_BETWEEN;
			case LESS_THAN:
				return DataValidationConstraint.OperatorType.LESS_THAN;
			case LESS_OR_EQUAL:
				return DataValidationConstraint.OperatorType.LESS_OR_EQUAL;
			case GREATER_THAN:
				return DataValidationConstraint.OperatorType.GREATER_THAN;
			case GREATER_OR_EQUAL:
				return DataValidationConstraint.OperatorType.GREATER_OR_EQUAL;
			case EQUAL:
				return DataValidationConstraint.OperatorType.EQUAL;
			case BETWEEN:
			default:
				return DataValidationConstraint.OperatorType.BETWEEN;
		}
	}

	public static OperatorType toOperatorType(int poiOperator){
		switch(poiOperator){
			case DataValidationConstraint.OperatorType.EQUAL:
				return OperatorType.EQUAL;
			case DataValidationConstraint.OperatorType.GREATER_OR_EQUAL:
				return OperatorType.GREATER_OR_EQUAL;
			case DataValidationConstraint.OperatorType.GREATER_THAN:
				return OperatorType.GREATER_THAN;
			case DataValidationConstraint.OperatorType.LESS_OR_EQUAL:
				return OperatorType.LESS_OR_EQUAL;
			case DataValidationConstraint.OperatorType.LESS_THAN:
				return OperatorType.LESS_THAN;
			case DataValidationConstraint.OperatorType.NOT_BETWEEN:
				return OperatorType.NOT_BETWEEN;
			case DataValidationConstraint.OperatorType.NOT_EQUAL:
				return OperatorType.NOT_EQUAL;
			case DataValidationConstraint.OperatorType.BETWEEN:
			default:
				return OperatorType.BETWEEN;
		}
	}

	public static int toPoiErrorStyle(AlertStyle style){
		switch(style){
		case INFO:
			return DataValidation.ErrorStyle.INFO;
		case WARNING:
			return DataValidation.ErrorStyle.WARNING;
		case STOP:
		default:
			return DataValidation.ErrorStyle.STOP;
		}
	}
	
	public static AlertStyle toErrorStyle(int errorStyle){
		switch(errorStyle){
			case DataValidation.ErrorStyle.INFO:
				return AlertStyle.INFO;
			case DataValidation.ErrorStyle.WARNING:
				return AlertStyle.WARNING;
			case DataValidation.ErrorStyle.STOP:
			default:
					return AlertStyle.STOP;
		}
	}


	public static ValidationType toValidationType(int validationType){
		switch(validationType){
			case DataValidationConstraint.ValidationType.TIME:
				return ValidationType.TIME;
			case DataValidationConstraint.ValidationType.TEXT_LENGTH:
				return ValidationType.TEXT_LENGTH;
			case DataValidationConstraint.ValidationType.LIST:
				return ValidationType.LIST;
			case DataValidationConstraint.ValidationType.INTEGER:
				return ValidationType.INTEGER;
			case DataValidationConstraint.ValidationType.FORMULA:
				return ValidationType.CUSTOM;
			case DataValidationConstraint.ValidationType.DECIMAL:
				return ValidationType.DECIMAL;
			case DataValidationConstraint.ValidationType.DATE:
				return ValidationType.DATE;
			case DataValidationConstraint.ValidationType.ANY:
			default:
				return ValidationType.ANY;
		}
	}
	//POI has no way to set validation type

	public static ErrorValue toErrorCode(byte errorCellValue) {
		//ZSS-672
		return ErrorValue.valueOf(errorCellValue);
	}
}
