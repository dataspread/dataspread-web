package org.zkoss.zss.range.impl;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import org.zkoss.zss.model.*;
import org.zkoss.zss.model.SAutoFilter.FilterOp;
import org.zkoss.zss.model.SBorder.BorderType;
import org.zkoss.zss.model.SChart.ChartGrouping;
import org.zkoss.zss.model.SChart.ChartLegendPosition;
import org.zkoss.zss.model.SChart.ChartType;
import org.zkoss.zss.model.SDataValidation.AlertStyle;
import org.zkoss.zss.model.SDataValidation.OperatorType;
import org.zkoss.zss.model.SDataValidation.ValidationType;
import org.zkoss.zss.model.SFont.Boldweight;
import org.zkoss.zss.model.SFont.TypeOffset;
import org.zkoss.zss.model.SFont.Underline;
import org.zkoss.zss.model.SHyperlink.HyperlinkType;
import org.zkoss.zss.model.SPicture.Format;
import org.zkoss.zss.model.impl.CellAttribute;
import org.zkoss.zss.range.*;

/**
 * the empty range implementation that do nothing
 * @author Dennis
 * @since 3.5.0
 */
/*package*/ class EmptyNRange implements SRange {

	@Override
	public ReadWriteLock getLock() {
		
		return null;
	}

	@Override
	public SHyperlink getHyperlink() {
		
		return null;
	}

	@Override
	public String getEditText() {
		
		return null;
	}

	@Override
	public void setEditText(String txt) {
		

	}

	@Override
	public SRange copy(SRange dstRange, boolean cut) {
		
		return null;
	}

	@Override
	public SRange copy(SRange dstRange) {
		
		return null;
	}

	@Override
	public SRange pasteSpecial(SRange dstRange, PasteType pasteType,
			PasteOperation pasteOp, boolean skipBlanks, boolean transpose) {
		
		return null;
	}

	@Override
	public void insert(InsertShift shift, InsertCopyOrigin copyOrigin) {
		

	}

	@Override
	public void delete(DeleteShift shift) {
		

	}

	@Override
	public void merge(boolean across) {
		

	}

	@Override
	public void unmerge() {
		

	}

	@Override
	public void setBorders(ApplyBorderType borderIndex, BorderType lineStyle,
			String color) {
		

	}

	@Override
	public void move(int nRow, int nCol) {
		

	}

	@Override
	public void setColumnWidth(int widthPx) {
		

	}

	@Override
	public void setRowHeight(int heightPx) {
		

	}

	@Override
	public void setColumnWidth(int widthPx, boolean custom) {
		

	}

	@Override
	public void setRowHeight(int heightPx, boolean custom) {
		

	}

	@Override
	public SSheet getSheet() {
		
		return null;
	}

	@Override
	public void setCellStyle(SCellStyle style) {
		

	}

	@Override
	public void fill(SRange dstRange, FillType fillType) {
		

	}

	@Override
	public void clearContents() {
		

	}

	@Override
	public void fillDown() {
		

	}

	@Override
	public void fillLeft() {
		

	}

	@Override
	public void fillRight() {
		

	}

	@Override
	public void fillUp() {
		

	}

	@Override
	public SRange findAutoFilterRange() {
		
		return null;
	}

	@Override
	public SAutoFilter enableAutoFilter(int field, FilterOp filterOp,
			Object criteria1, Object criteria2, Boolean showButton) {
		
		return null;
	}

	@Override
	public SAutoFilter enableAutoFilter(boolean enable) {
		
		return null;
	}

	@Override
	public void resetAutoFilter() {
		

	}

	@Override
	public void applyAutoFilter() {
		

	}

	@Override
	public void setHidden(boolean hidden) {
		

	}

	@Override
	public void setDisplayGridlines(boolean show) {
		

	}

	@Override
	public void protectSheet(String password) {
		

	}

	@Override
	public void setHyperlink(HyperlinkType linkType, String address,
			String display) {
		

	}

	@Override
	public SRange getColumns() {
		
		return this;
	}

	@Override
	public SRange getRows() {
		
		return this;
	}

	@Override
	public int getRow() {
		
		return -1;
	}

	@Override
	public int getColumn() {
		
		return -1;
	}

	@Override
	public int getLastRow() {
		
		return -1;
	}

	@Override
	public int getLastColumn() {
		
		return -1;
	}

	@Override
	public void setValue(Object value) {
		

	}

	@Override
	public Object getValue() {
		
		return null;
	}

	@Override
	public SRange getOffset(int rowOffset, int colOffset) {
		
		return this;
	}

	@Override
	public SPicture addPicture(ViewAnchor anchor, byte[] image, Format format) {
		
		return null;
	}

	@Override
	public void deletePicture(SPicture picture) {
		

	}

	@Override
	public void movePicture(SPicture picture, ViewAnchor anchor) {
		

	}

	@Override
	public SDataValidation validate(String txt) {
		
		return null;
	}

	@Override
	public boolean isAnyCellProtected() {
		
		return false;
	}

	@Override
	public void notifyCustomEvent(String customEventName, Object data,
			boolean writeLock) {
		

	}

	@Override
	public void deleteSheet() {
		

	}

	@Override
	public SSheet createSheet(String name) {
		
		return null;
	}

	@Override
	public void setSheetName(String name) {
		

	}

	@Override
	public void setSheetOrder(int pos) {
		

	}

	@Override
	public boolean isWholeRow() {
		
		return false;
	}

	@Override
	public boolean isWholeColumn() {
		
		return false;
	}

	@Override
	public boolean isWholeSheet() {
		
		return false;
	}

	@Override
	public void notifyChange() {

	}
	
	@Override
	public void notifyChange(String[] variables){
		
	}

	@Override
	public void setFreezePanel(int numOfRow, int numOfColumn) {
		

	}

	@Override
	public String getCellFormatText() {
		
		return null;
	}

	@Override
	public SCellStyle getCellStyle() {
		
		return null;
	}

	@Override
	public boolean isProtected() {
		return false;
	}
	
	@Override
	public boolean isSheetProtected() {
		
		return false;
	}

	@Override
	public String getCellDataFormat() {
		return null;
	}
	
	public SChart addChart(ViewAnchor anchor, ChartType type, ChartGrouping grouping, ChartLegendPosition pos, boolean isThreeD) {
		return null;
	};
	
	@Override
	public void moveChart(SChart chart, ViewAnchor anchor) {
		
	}

	@Override
	public void deleteChart(SChart chart) {
		
	}

	@Override
	public void updateChart(SChart chart) {
		
	}

	@Override
	public void sort(SRange key1, boolean descending1, SortDataOption dataOption1, SRange key2, boolean descending2,
			SortDataOption dataOption2, SRange key3, boolean descending3, SortDataOption dataOption3, int hasHeader,
			boolean matchCase, boolean sortByRows) {
		
	}

	@Override
	public void clearAll() {
			
	}

	@Override
	public void clearCellStyles() {		
	}

	@Override
	public void createName(String nameName) {
		
	}
	
	@Override
	public void protectSheet(String password,  
			boolean allowSelectingLockedCells, boolean allowSelectingUnlockedCells,  
			boolean allowFormattingCells, boolean allowFormattingColumns, boolean allowFormattingRows, 
			boolean allowInsertColumns, boolean allowInsertRows, boolean allowInsertingHyperlinks,
			boolean allowDeletingColumns, boolean allowDeletingRows, 
			boolean allowSorting, boolean allowFiltering, boolean allowUsingPivotTables, 
			boolean drawingObjects, boolean scenarios) {
	}
	
	@Override
	public boolean unprotectSheet(String password) {
		return false;
	}
	
	@Override
	public SSheetProtection getSheetProtection() {
		return null;
	}

	@Override
	public void setValidation(ValidationType validationType,
			boolean ignoreBlank, OperatorType operatorType,
			boolean inCellDropDown, String formula1, String formula2,
			boolean showInput, String inputTitle, String inputMessage,
			boolean showError, AlertStyle alertStyle, String errorTitle,
			String errorMessage) {
		// do nothing
	}

	@Override
	public List<SDataValidation> getValidations() {
		// do nothing
		return Collections.emptyList();
	}
	
	@Override
	public void deleteValidation() {
		// do nothing
	}

	@Override
	public SSheet cloneSheet(String name) {
		return null;
	}

	@Override
	public void setRichText(String html) {
		// do nothing
	}

	@Override
	public String getRichText() {
		return null;
	}
	
	@Override
	public SFont getOrCreateFont(Boldweight boldweight, String htmlColor,
			int fontHeight, String fontName, boolean italic, boolean strikeout,
			TypeOffset typeOffset, Underline underline) {
		return null;
	}

	@Override
	public void refresh(boolean includeDependants) {
		// do nothing
		
	}

	@Override
	public boolean setAutoRefresh(boolean auto) {
		// always return true; and no way to change it
		return true;
	}

	@Override
	public void refresh(boolean includeDependents, boolean clearCache, boolean enforceEval) {
		// do nothing
	}

	@Override
	public void setSheetVisible(SheetVisible visible) {
		// do nothing
	}

	@Override
	public String getCommentRichText() {
		return null;
	}

	@Override
	public void setCommentRichText(String html) {
		// do nothing
		
	}

	@Override
	public void setCommentVisible(boolean visible) {
		// do nothing
	}

	@Override
	public boolean isCommentVisible() {
		return false;
	}

	//ZSS-939
	@Override
	public void notifyChange(CellAttribute cellAttr) {
		// do nothing
		
	}

	//ZSS-966
	@Override
	public void setNameName(String namename, String newname) {
		// do nothing
	}

	//ZSS-1046
	@Override
	public void setStringValue(String value) {
		// do nothing
	}
}
