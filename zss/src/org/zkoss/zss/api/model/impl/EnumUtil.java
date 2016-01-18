/* EnumUtil.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/5/1 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.api.model.impl;

import org.zkoss.poi.common.usermodel.Hyperlink;
import org.zkoss.zss.api.Range.ApplyBorderType;
import org.zkoss.zss.api.Range.AutoFillType;
import org.zkoss.zss.api.Range.AutoFilterOperation;
import org.zkoss.zss.api.Range.DeleteShift;
import org.zkoss.zss.api.Range.InsertCopyOrigin;
import org.zkoss.zss.api.Range.InsertShift;
import org.zkoss.zss.api.Range.PasteOperation;
import org.zkoss.zss.api.Range.PasteType;
import org.zkoss.zss.api.Range.SortDataOption;
import org.zkoss.zss.api.model.*;
import org.zkoss.zss.api.model.CellStyle.Alignment;
import org.zkoss.zss.api.model.CellStyle.BorderType;
import org.zkoss.zss.api.model.CellStyle.FillPattern;
import org.zkoss.zss.api.model.CellStyle.VerticalAlignment;
import org.zkoss.zss.api.model.Chart.Grouping;
import org.zkoss.zss.api.model.Chart.LegendPosition;
import org.zkoss.zss.api.model.Chart.Type;
import org.zkoss.zss.api.model.Font.Boldweight;
import org.zkoss.zss.api.model.Font.TypeOffset;
import org.zkoss.zss.api.model.Font.Underline;
import org.zkoss.zss.api.model.Hyperlink.HyperlinkType;
import org.zkoss.zss.api.model.Picture.Format;
import org.zkoss.zss.model.*;
import org.zkoss.zss.model.SAutoFilter.FilterOp;
import org.zkoss.zss.model.SChart.ChartGrouping;
import org.zkoss.zss.model.SChart.ChartLegendPosition;
import org.zkoss.zss.model.SChart.ChartType;
import org.zkoss.zss.range.SRange;
import org.zkoss.zss.range.SRange.FillType;
/**
 * 
 * @author dennis
 * @since 3.0.0
 */
public class EnumUtil {
	
	private static <T> void assertArgNotNull(T obj,String name){
		if(obj == null){
			throw new IllegalArgumentException("argument "+name==null?"":name+" is null");
		}
	}
	
	public static SRange.PasteOperation toRangePasteOpNative(PasteOperation op) {
		assertArgNotNull(op,"paste operation");
		switch(op){
		case ADD:
			return SRange.PasteOperation.ADD;
		case SUB:
			return SRange.PasteOperation.SUB;
		case MUL:
			return SRange.PasteOperation.MUL;
		case DIV:
			return SRange.PasteOperation.DIV;
		case NONE:
			return SRange.PasteOperation.NONE;
		}
		throw new IllegalArgumentException("unknow paste operation "+op);
	}


	public static SRange.PasteType toRangePasteTypeNative(PasteType type) {
		assertArgNotNull(type,"paste type");
		switch(type){
		case ALL:
			return SRange.PasteType.ALL;
		case ALL_EXCEPT_BORDERS:
			return SRange.PasteType.ALL_EXCEPT_BORDERS;
		case COLUMN_WIDTHS:
			return SRange.PasteType.COLUMN_WIDTHS;
		case COMMENTS:
			return SRange.PasteType.COMMENTS;
		case FORMATS:
			return SRange.PasteType.FORMATS;
		case FORMULAS:
			return SRange.PasteType.FORMULAS;
		case FORMULAS_AND_NUMBER_FORMATS:
			return SRange.PasteType.FORMULAS_AND_NUMBER_FORMATS;
		case VALIDATAION:
			return SRange.PasteType.VALIDATAION;
		case VALUES:
			return SRange.PasteType.VALUES;
		case VALUES_AND_NUMBER_FORMATS:
			return SRange.PasteType.VALUES_AND_NUMBER_FORMATS;
		}
		throw new IllegalArgumentException("unknow paste operation "+type);
	}
	
	public static TypeOffset toFontTypeOffset(SFont.TypeOffset typeOffset){
		switch(typeOffset){
		case NONE:
			return Font.TypeOffset.NONE;
		case SUB:
			return Font.TypeOffset.SUB;
		case SUPER:
			return Font.TypeOffset.SUPER;
		}
		throw new IllegalArgumentException("unknow font type offset "+typeOffset);
	}

	public static SFont.TypeOffset toFontTypeOffset(TypeOffset typeOffset) {
		assertArgNotNull(typeOffset,"typeOffset");
		switch(typeOffset){
		case NONE:
			return SFont.TypeOffset.NONE;
		case SUB:
			return SFont.TypeOffset.SUB;
		case SUPER:
			return SFont.TypeOffset.SUPER;
		}
		throw new IllegalArgumentException("unknow font type offset "+typeOffset);
	}

	public static Underline toFontUnderline(SFont.Underline underline) {
		switch(underline){
		case NONE:
			return Font.Underline.NONE;
		case SINGLE:
			return Font.Underline.SINGLE;
		case SINGLE_ACCOUNTING:
			return Font.Underline.SINGLE_ACCOUNTING;
		case DOUBLE:
			return Font.Underline.DOUBLE;
		case DOUBLE_ACCOUNTING:
			return Font.Underline.DOUBLE_ACCOUNTING;
		}
		throw new IllegalArgumentException("unknow font underline "+underline);
	}


	public static SFont.Underline toFontUnderline(Underline underline) {
		assertArgNotNull(underline,"underline");
		switch(underline){
		case NONE:
			return SFont.Underline.NONE;
		case SINGLE:
			return SFont.Underline.SINGLE;
		case SINGLE_ACCOUNTING:
			return SFont.Underline.SINGLE_ACCOUNTING;
		case DOUBLE:
			return SFont.Underline.DOUBLE;
		case DOUBLE_ACCOUNTING:
			return SFont.Underline.DOUBLE_ACCOUNTING;
		}
		throw new IllegalArgumentException("unknow font underline "+underline);
	}

	public static Boldweight toFontBoldweight(org.zkoss.zss.model.SFont.Boldweight boldweight) {
		switch(boldweight){
		case BOLD:
			return Font.Boldweight.BOLD;
		case NORMAL:
			return Font.Boldweight.NORMAL;
		}
		throw new IllegalArgumentException("unknow font boldweight "+boldweight);
	}
	
	public static SFont.Boldweight toFontBoldweight(Boldweight boldweight) {
		switch(boldweight){
		case BOLD:
			return SFont.Boldweight.BOLD;
		case NORMAL:
			return SFont.Boldweight.NORMAL;
		}
		throw new IllegalArgumentException("unknow font boldweight "+boldweight);
	}

	public static FillPattern toStyleFillPattern(SFill.FillPattern pattern) {
		switch(pattern){
		case NONE:
			return CellStyle.FillPattern.NONE;
		case SOLID:
			return CellStyle.FillPattern.SOLID;
		case MEDIUM_GRAY:
			return CellStyle.FillPattern.MEDIUM_GRAY;
		case DARK_GRAY:
			return CellStyle.FillPattern.DARK_GRAY;
		case LIGHT_GRAY:
			return CellStyle.FillPattern.LIGHT_GRAY;
		case DARK_HORIZONTAL:
			return CellStyle.FillPattern.DARK_HORIZONTAL;
		case DARK_VERTICAL:
			return CellStyle.FillPattern.DARK_VERTICAL;
		case DARK_DOWN:
			return CellStyle.FillPattern.DARK_DOWN;
		case DARK_UP:
			return CellStyle.FillPattern.DARK_UP;
		case DARK_GRID:
			return CellStyle.FillPattern.DARK_GRID;
		case DARK_TRELLIS:
			return CellStyle.FillPattern.DARK_TRELLIS;
		case LIGHT_HORIZONTAL:
			return CellStyle.FillPattern.LIGHT_HORIZONTAL;
		case LIGHT_VERTICAL:
			return CellStyle.FillPattern.LIGHT_VERTICAL;
		case LIGHT_DOWN:
			return CellStyle.FillPattern.LIGHT_DOWN;
		case LIGHT_UP:
			return CellStyle.FillPattern.LIGHT_UP;
		case LIGHT_GRID:
			return CellStyle.FillPattern.LIGHT_GRID;
		case LIGHT_TRELLIS:
			return CellStyle.FillPattern.LIGHT_TRELLIS;
		case GRAY125:
			return CellStyle.FillPattern.GRAY125;
		case GRAY0625:
			return CellStyle.FillPattern.GRAY0625;
		}
		throw new IllegalArgumentException("unknow pattern type "+pattern);	
	}
	
	public static SFill.FillPattern toStyleFillPattern(FillPattern pattern) {
		switch(pattern){
		case NONE:
			return SFill.FillPattern.NONE;
		case SOLID:
			return SFill.FillPattern.SOLID;
		case MEDIUM_GRAY:
			return SFill.FillPattern.MEDIUM_GRAY;
		case DARK_GRAY:
			return SFill.FillPattern.DARK_GRAY;
		case LIGHT_GRAY:
			return SFill.FillPattern.LIGHT_GRAY;
		case DARK_HORIZONTAL:
			return SFill.FillPattern.DARK_HORIZONTAL;
		case DARK_VERTICAL:
			return SFill.FillPattern.DARK_VERTICAL;
		case DARK_DOWN:
			return SFill.FillPattern.DARK_DOWN;
		case DARK_UP:
			return SFill.FillPattern.DARK_UP;
		case DARK_GRID:
			return SFill.FillPattern.DARK_GRID;
		case DARK_TRELLIS:
			return SFill.FillPattern.DARK_TRELLIS;
		case LIGHT_HORIZONTAL:
			return SFill.FillPattern.LIGHT_HORIZONTAL;
		case LIGHT_VERTICAL:
			return SFill.FillPattern.LIGHT_VERTICAL;
		case LIGHT_DOWN:
			return SFill.FillPattern.LIGHT_DOWN;
		case LIGHT_UP:
			return SFill.FillPattern.LIGHT_UP;
		case LIGHT_GRID:
			return SFill.FillPattern.LIGHT_GRID;
		case LIGHT_TRELLIS:
			return SFill.FillPattern.LIGHT_TRELLIS;
		case GRAY125:
			return SFill.FillPattern.GRAY125;
		case GRAY0625:
			return SFill.FillPattern.GRAY0625;
		}
		throw new IllegalArgumentException("unknow pattern type "+pattern);
	}

	public static SCellStyle.Alignment toStyleAlignemnt(Alignment alignment) {
		switch(alignment){
		case GENERAL:
			return SCellStyle.Alignment.GENERAL;
		case LEFT:
			return SCellStyle.Alignment.LEFT;
		case CENTER:
			return SCellStyle.Alignment.CENTER;
		case RIGHT:
			return SCellStyle.Alignment.RIGHT;
		case FILL:
			return SCellStyle.Alignment.FILL;
		case JUSTIFY:
			return SCellStyle.Alignment.JUSTIFY;
		case CENTER_SELECTION:
			return SCellStyle.Alignment.CENTER_SELECTION;
		}
		throw new IllegalArgumentException("unknow cell alignment "+alignment);
	}
	public static Alignment toStyleAlignemnt(SCellStyle.Alignment alignment) {
		switch(alignment){
		case GENERAL:
			return Alignment.GENERAL;
		case LEFT:
			return Alignment.LEFT;
		case CENTER:
			return Alignment.CENTER;
		case RIGHT:
			return Alignment.RIGHT;
		case FILL:
			return Alignment.FILL;
		case JUSTIFY:
			return Alignment.JUSTIFY;
		case CENTER_SELECTION:
			return Alignment.CENTER_SELECTION;
		}
		throw new IllegalArgumentException("unknow cell alignment "+alignment);
	}
	public static SCellStyle.VerticalAlignment toStyleVerticalAlignemnt(VerticalAlignment alignment) {
		switch(alignment){
		case TOP:
			return SCellStyle.VerticalAlignment.TOP;
		case CENTER:
			return SCellStyle.VerticalAlignment.CENTER;
		case BOTTOM:
			return SCellStyle.VerticalAlignment.BOTTOM;
		case JUSTIFY:
			return SCellStyle.VerticalAlignment.JUSTIFY;
		}
		throw new IllegalArgumentException("unknow cell vertical alignment "+alignment);
	}
	public static VerticalAlignment toStyleVerticalAlignemnt(SCellStyle.VerticalAlignment alignment) {
		switch(alignment){
		case TOP:
			return VerticalAlignment.TOP;
		case CENTER:
			return VerticalAlignment.CENTER;
		case BOTTOM:
			return VerticalAlignment.BOTTOM;
		case JUSTIFY:
			return VerticalAlignment.JUSTIFY;
		}
		throw new IllegalArgumentException("unknow cell vertical alignment "+alignment);
	}

	public static SRange.ApplyBorderType toRangeApplyBorderType(ApplyBorderType type) {
		switch(type){
		case FULL:
			return SRange.ApplyBorderType.FULL;
		case EDGE_BOTTOM:
			return SRange.ApplyBorderType.EDGE_BOTTOM;
		case EDGE_RIGHT:
			return SRange.ApplyBorderType.EDGE_RIGHT;
		case EDGE_TOP:
			return SRange.ApplyBorderType.EDGE_TOP;
		case EDGE_LEFT:
			return SRange.ApplyBorderType.EDGE_LEFT;
		case OUTLINE:
			return SRange.ApplyBorderType.OUTLINE;
		case INSIDE:
			return SRange.ApplyBorderType.INSIDE;
		case INSIDE_HORIZONTAL:
			return SRange.ApplyBorderType.INSIDE_HORIZONTAL;
		case INSIDE_VERTICAL:
			return SRange.ApplyBorderType.INSIDE_VERTICAL;
		case DIAGONAL:
			return SRange.ApplyBorderType.DIAGONAL;
		case DIAGONAL_DOWN:
			return SRange.ApplyBorderType.DIAGONAL_DOWN;
		case DIAGONAL_UP:
			return SRange.ApplyBorderType.DIAGONAL_UP;
		}
		throw new IllegalArgumentException("unknow cell border apply type "+type);
	}

	public static SBorder.BorderType toStyleBorderType(BorderType borderType) {
		switch(borderType){
		case NONE:
			return SBorder.BorderType.NONE;
		case THIN:
			return SBorder.BorderType.THIN;
		case MEDIUM:
			return SBorder.BorderType.MEDIUM;
		case DASHED:
			return SBorder.BorderType.DASHED;
		case HAIR:
			return SBorder.BorderType.HAIR;
		case THICK:
			return SBorder.BorderType.THICK;
		case DOUBLE:
			return SBorder.BorderType.DOUBLE;
		case DOTTED:
			return SBorder.BorderType.DOTTED;
		case MEDIUM_DASHED:
			return SBorder.BorderType.MEDIUM_DASHED;
		case DASH_DOT:
			return SBorder.BorderType.DASH_DOT;
		case MEDIUM_DASH_DOT:
			return SBorder.BorderType.MEDIUM_DASH_DOT;
		case DASH_DOT_DOT:
			return SBorder.BorderType.DASH_DOT_DOT;
		case MEDIUM_DASH_DOT_DOT:
			return SBorder.BorderType.MEDIUM_DASH_DOT_DOT;
		case SLANTED_DASH_DOT:
			return SBorder.BorderType.SLANTED_DASH_DOT;
		}
		throw new IllegalArgumentException("unknow style border type "+borderType);
	}
	
	public static BorderType toStyleBorderType(SBorder.BorderType borderType) {
		switch(borderType){
		case NONE:
			return BorderType.NONE;
		case THIN:
			return BorderType.THIN;
		case MEDIUM:
			return BorderType.MEDIUM;
		case DASHED:
			return BorderType.DASHED;
		case HAIR:
			return BorderType.HAIR;
		case THICK:
			return BorderType.THICK;
		case DOUBLE:
			return BorderType.DOUBLE;
		case DOTTED:
			return BorderType.DOTTED;
		case MEDIUM_DASHED:
			return BorderType.MEDIUM_DASHED;
		case DASH_DOT:
			return BorderType.DASH_DOT;
		case MEDIUM_DASH_DOT:
			return BorderType.MEDIUM_DASH_DOT;
		case DASH_DOT_DOT:
			return BorderType.DASH_DOT_DOT;
		case MEDIUM_DASH_DOT_DOT:
			return BorderType.MEDIUM_DASH_DOT_DOT;
		case SLANTED_DASH_DOT:
			return BorderType.SLANTED_DASH_DOT;
		}
		throw new IllegalArgumentException("unknow style border type "+borderType);
	}
	
	public static SBorder.BorderType toRangeBorderType(BorderType lineStyle) {
		switch(lineStyle){
		case NONE:
			return SBorder.BorderType.NONE;
		case THIN:
			return SBorder.BorderType.THIN;
		case MEDIUM:
			return SBorder.BorderType.MEDIUM;
		case DASHED:
			return SBorder.BorderType.DASHED;
		case HAIR:
			return SBorder.BorderType.HAIR;
		case THICK:
			return SBorder.BorderType.THICK;
		case DOUBLE:
			return SBorder.BorderType.DOUBLE;
		case DOTTED:
			return SBorder.BorderType.DOTTED;
		case MEDIUM_DASHED:
			return SBorder.BorderType.MEDIUM_DASHED;
		case DASH_DOT:
			return SBorder.BorderType.DASH_DOT;
		case MEDIUM_DASH_DOT:
			return SBorder.BorderType.MEDIUM_DASH_DOT;
		case DASH_DOT_DOT:
			return SBorder.BorderType.DASH_DOT_DOT;
		case MEDIUM_DASH_DOT_DOT:
			return SBorder.BorderType.MEDIUM_DASH_DOT_DOT;
		case SLANTED_DASH_DOT:
			return SBorder.BorderType.SLANTED_DASH_DOT;
		}
		throw new IllegalArgumentException("unknow cell border line style "+lineStyle);
	}

	public static SRange.InsertShift toRangeInsertShift(InsertShift shift) {
		switch(shift){
		case DEFAULT:
			return SRange.InsertShift.DEFAULT;
		case DOWN:
			return SRange.InsertShift.DOWN;
		case RIGHT:
			return SRange.InsertShift.RIGHT;
		}
		throw new IllegalArgumentException("unknow range insert shift "+shift);
	}

	public static SRange.InsertCopyOrigin toRangeInsertCopyOrigin(InsertCopyOrigin copyOrigin) {
		switch(copyOrigin){
		case FORMAT_NONE:
			return SRange.InsertCopyOrigin.FORMAT_NONE;
		case FORMAT_LEFT_ABOVE:
			return SRange.InsertCopyOrigin.FORMAT_LEFT_ABOVE;
		case FORMAT_RIGHT_BELOW:
			return SRange.InsertCopyOrigin.FORMAT_RIGHT_BELOW;
		}
		throw new IllegalArgumentException("unknow range insert copy origin "+copyOrigin);
	}
	
	public static SRange.DeleteShift toRangeDeleteShift(DeleteShift shift) {
		switch(shift){
		case DEFAULT:
			return SRange.DeleteShift.DEFAULT;
		case UP:
			return SRange.DeleteShift.UP;
		case LEFT:
			return SRange.DeleteShift.LEFT;
		}
		throw new IllegalArgumentException("unknow range delete shift "+shift);
	}

	public static SRange.SortDataOption toRangeSortDataOption(SortDataOption dataOption) {
		switch(dataOption){
		case TEXT_AS_NUMBERS:
			return SRange.SortDataOption.TEXT_AS_NUMBERS;
		case NORMAL_DEFAULT:
			return SRange.SortDataOption.NORMAL_DEFAULT;
		}
		throw new IllegalArgumentException("unknow sort data option "+dataOption);
	}

	public static FilterOp toRangeAutoFilterOperation(AutoFilterOperation filterOp) {
		switch(filterOp){
		case AND:
			return FilterOp.AND;
		case OR:
			return FilterOp.OR;
		case TOP10:
			return FilterOp.TOP10;
		case TOP10PERCENT:
			return FilterOp.TOP10_PERCENT;
		case BOTTOM10:
			return FilterOp.BOTTOM10;
		case BOTOOM10PERCENT:
			return FilterOp.BOTOOM10_PERCENT;
		case VALUES:
			return FilterOp.VALUES;
		}
		throw new IllegalArgumentException("unknow autofilter operation "+filterOp);
	}

	public static FillType toRangeFillType(AutoFillType fillType) {
		switch(fillType){
		case COPY:
			return FillType.COPY;
		case DAYS:
			return FillType.DAYS;
		case DEFAULT:
			return FillType.DEFAULT;
		case FORMATS:
			return FillType.FORMATS;
		case MONTHS:
			return FillType.MONTHS;
		case SERIES:
			return FillType.SERIES;
		case VALUES:
			return FillType.VALUES;
		case WEEKDAYS:
			return FillType.WEEKDAYS;
		case YEARS:
			return FillType.YEARS;
		case GROWTH_TREND:
			return FillType.GROWTH_TREND;
		case LINER_TREND:
			return FillType.LINER_TREND;
		}
		throw new IllegalArgumentException("unknow fill type "+fillType);
	}

	public static SHyperlink.HyperlinkType toHyperlinkType(HyperlinkType type) {
		switch(type){
		case URL:
			return SHyperlink.HyperlinkType.URL;
		case DOCUMENT:
			return SHyperlink.HyperlinkType.DOCUMENT;
		case EMAIL:
			return SHyperlink.HyperlinkType.EMAIL;
		case FILE:
			return SHyperlink.HyperlinkType.FILE;
		}
		throw new IllegalArgumentException("unknow hyperlink type "+type);
	}
	public static HyperlinkType toHyperlinkType(int type) {
		switch(type){
		case Hyperlink.LINK_URL:
			return HyperlinkType.URL;
		case Hyperlink.LINK_DOCUMENT:
			return HyperlinkType.DOCUMENT;
		case Hyperlink.LINK_EMAIL:
			return HyperlinkType.EMAIL;
		case Hyperlink.LINK_FILE:
			return HyperlinkType.FILE;
		}
		throw new IllegalArgumentException("unknow hyperlink type "+type);
	}
	public static HyperlinkType toHyperlinkType(SHyperlink.HyperlinkType type) {
		switch(type){
		case URL:
			return HyperlinkType.URL;
		case DOCUMENT:
			return HyperlinkType.DOCUMENT;
		case EMAIL:
			return HyperlinkType.EMAIL;
		case FILE:
			return HyperlinkType.FILE;
		}
		throw new IllegalArgumentException("unknow hyperlink type "+type);
	}

	public static SPicture.Format toPictureFormat(Format format) {
		switch(format){
		case EMF:
			return SPicture.Format.EMF;
		case WMF:
			return SPicture.Format.WMF;
		case PICT:
			return SPicture.Format.PICT;
		case JPEG:
			return SPicture.Format.JPG;
		case PNG:
			return SPicture.Format.PNG;
		case DIB:
			return SPicture.Format.DIB;
		}
		throw new IllegalArgumentException("unknow pciture format "+format);
	}

	public static ChartType toChartType(Type type) {
		switch(type){
		case AREA_3D:
		case AREA:
			return ChartType.AREA;
		case BAR_3D:
		case BAR:
			return ChartType.BAR;
		case BUBBLE:
			return ChartType.BUBBLE;
		case COLUMN:
		case COLUMN_3D:
			return ChartType.COLUMN;
		case DOUGHNUT:
			return ChartType.DOUGHNUT;
		case LINE_3D:
		case LINE:
			return ChartType.LINE;
		case OF_PIE:
			return ChartType.OF_PIE;
		case PIE_3D:
		case PIE:
			return ChartType.PIE;
		case RADAR:
			return ChartType.RADAR;
		case SCATTER:
			return ChartType.SCATTER;
		case STOCK:
			return ChartType.STOCK;
		case SURFACE_3D:
		case SURFACE:
			return ChartType.SURFACE;
		}
		throw new IllegalArgumentException("unknow chart type "+type);
	}
	
	public static boolean isThreeDimentionalChart(Type type) {
		switch(type){
		case AREA_3D:
		case BAR_3D:
		case COLUMN_3D:
		case LINE_3D:
		case PIE_3D:
		case SURFACE_3D:
			return true;
		default:
			return false;
		}
	}

	public static ChartGrouping toChartGrouping(Grouping grouping) {
		switch(grouping){
		case STANDARD:
			return ChartGrouping.STANDARD;
		case STACKED:
			return ChartGrouping.STACKED;
		case PERCENT_STACKED:
			return ChartGrouping.PERCENT_STACKED;
		case CLUSTERED:
			return ChartGrouping.CLUSTERED;//bar only
		}
		throw new IllegalArgumentException("unknow grouping "+grouping);
	}

	public static ChartLegendPosition toLegendPosition(LegendPosition pos) {
		switch(pos){
		case BOTTOM:
			return ChartLegendPosition.BOTTOM;
		case LEFT:
			return ChartLegendPosition.LEFT;
		case RIGHT:
			return ChartLegendPosition.RIGHT;
		case TOP:
			return ChartLegendPosition.TOP;
		case TOP_RIGHT:
			return ChartLegendPosition.TOP_RIGHT;
		}
		throw new IllegalArgumentException("unknow legend position "+pos);
	}
}
