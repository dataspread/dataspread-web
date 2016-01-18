/* RangeImpl.java

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
package org.zkoss.zss.api.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import org.zkoss.zss.api.CellVisitor;
import org.zkoss.zss.api.IllegalFormulaException;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.RangeRunner;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.SheetAnchor;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.CellData;
import org.zkoss.zss.api.model.CellStyle;
import org.zkoss.zss.api.model.CellStyle.BorderType;
import org.zkoss.zss.api.model.Chart;
import org.zkoss.zss.api.model.Chart.Grouping;
import org.zkoss.zss.api.model.Chart.LegendPosition;
import org.zkoss.zss.api.model.Chart.Type;
import org.zkoss.zss.api.model.Hyperlink;
import org.zkoss.zss.api.model.Font.Boldweight;
import org.zkoss.zss.api.model.Font.TypeOffset;
import org.zkoss.zss.api.model.Font.Underline;
import org.zkoss.zss.api.model.Hyperlink.HyperlinkType;
import org.zkoss.zss.api.model.Picture;
import org.zkoss.zss.api.model.Picture.Format;
import org.zkoss.zss.api.model.Color;
import org.zkoss.zss.api.model.Font;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.api.model.SheetProtection;
import org.zkoss.zss.api.model.Validation;
import org.zkoss.zss.api.model.Validation.AlertStyle;
import org.zkoss.zss.api.model.Validation.OperatorType;
import org.zkoss.zss.api.model.Validation.ValidationType;
import org.zkoss.zss.api.model.impl.BookImpl;
import org.zkoss.zss.api.model.impl.CellDataImpl;
import org.zkoss.zss.api.model.impl.CellStyleImpl;
import org.zkoss.zss.api.model.impl.ChartImpl;
import org.zkoss.zss.api.model.impl.EnumUtil;
import org.zkoss.zss.api.model.impl.FontImpl;
import org.zkoss.zss.api.model.impl.HyperlinkImpl;
import org.zkoss.zss.api.model.impl.ModelRef;
import org.zkoss.zss.api.model.impl.PictureImpl;
import org.zkoss.zss.api.model.impl.SheetImpl;
import org.zkoss.zss.api.model.impl.SimpleRef;
import org.zkoss.zss.api.model.impl.SheetProtectionImpl;
import org.zkoss.zss.api.model.impl.ValidationImpl;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.InvalidFormulaException;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SCellStyle;
import org.zkoss.zss.model.SChart;
import org.zkoss.zss.model.SDataValidation;
import org.zkoss.zss.model.SFont;
import org.zkoss.zss.model.SHyperlink;
import org.zkoss.zss.model.SPicture;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SSheetProtection;
import org.zkoss.zss.model.STable;
import org.zkoss.zss.model.ViewAnchor;
import org.zkoss.zss.model.util.FontMatcher;
import org.zkoss.zss.range.SRange;
import org.zkoss.zss.range.SRanges;
import org.zkoss.zss.range.impl.imexp.BookHelper;
import org.zkoss.zss.model.impl.AbstractSheetAdv;

/**
 * 1.Range does not handle the protection issue. By calling 
 * {@link #isSheetProtected} and {@link #getSheetProtection}, you can handle
 * it easily.
 *  
 * @author dennis
 * @since 3.0.0
 */
public class RangeImpl implements Range{
	
	private SRange _range;
	
	private CellStyleHelper _cellStyleHelper;
	private CellData _cellData;
	private static int DEFAULT_CHART_WIDTH = 600;
	private static int DEFAULT_CHART_HEIGHT = 480;
	
	/**
	 * @deprecated since 3.5 it is always synchronized on book by a read write lock.
	 */
	public void setSyncLevel(SyncLevel syncLevel){
	}
	
	private SharedContext _sharedCtx;
	
	public RangeImpl(SRange range,Sheet sheet) {
		this._range = range;
		_sharedCtx = new SharedContext(sheet);
	}
	//ZSS-966
	public RangeImpl(SRange range,Book book) {
		this._range = range;
		_sharedCtx = new SharedContext(book);
	}
	private RangeImpl(SRange range,SharedContext ctx) {
		this._range = range;
		_sharedCtx = ctx;
	}
	
	public ReadWriteLock getLock(){
		return _range.getLock();
	}
	
	public CellStyleHelper getCellStyleHelper(){
		if(_cellStyleHelper==null){
			_cellStyleHelper = new CellStyleHelperImpl(getBook());
		}
		return _cellStyleHelper;
	}
	
	public CellData getCellData(){
		if(_cellData==null){
			_cellData = new CellDataImpl(this);
		}
		return _cellData;
	}
	
	public SRange getNative(){
		return _range;
	}

	
	private static class SharedContext{
		Sheet _sheet;
		Book _book; //ZSS-966
		
		private SharedContext(Sheet sheet){
			this._sheet = sheet;
		}
		//ZSS-966
		private SharedContext(Book book) {
			this._sheet = null;
			this._book = book;
		}
		public Sheet getSheet(){
			return _sheet;
		}
		
		public Book getBook(){
			return _book == null ? _sheet.getBook() : _book;
		}
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_range == null) ? 0 : _range.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RangeImpl other = (RangeImpl) obj;
		if (_range == null) {
			if (other._range != null)
				return false;
		} else if (!_range.equals(other._range))
			return false;
		return true;
	}

	@Override
	public boolean isProtected() {
		return _range.isProtected();
	}
	
	@Override
	public boolean isSheetProtected() {
		return _range.isSheetProtected();
	}	

	public boolean isAnyCellProtected(){
		return _range.isAnyCellProtected();
	}
	
	public Range paste(Range dest, boolean cut) { 
		SRange r = _range.copy(((RangeImpl)dest).getNative(), cut);
		return new RangeImpl(r, dest.getSheet());
	}

	/* short-cut for pasteSpecial, it is original Range.copy*/
	public Range paste(Range dest) { 
		SRange r = _range.copy(((RangeImpl)dest).getNative());
		return new RangeImpl(r, dest.getSheet());
	}
	
	public Range pasteSpecial(Range dest,PasteType type,PasteOperation op,boolean skipBlanks,boolean transpose) { 
		SRange r = _range.pasteSpecial(((RangeImpl)dest).getNative(), EnumUtil.toRangePasteTypeNative(type), EnumUtil.toRangePasteOpNative(op), skipBlanks, transpose);
		return new RangeImpl(r, dest.getSheet());
	}


	public void clearContents() { 
		_range.clearContents();
	}
	
	public void clearAll() { 
		_range.clearAll();
	}
	
	public Sheet getSheet(){
		return _sharedCtx.getSheet();
	}

 
	public void clearStyles() {
		_range.clearCellStyles();	
	}

	public void setCellStyle(final CellStyle nstyle) { 
		_range.setCellStyle(nstyle==null?null:((CellStyleImpl)nstyle).getNative());
	}


	public int getColumn() {
		return _range.getColumn();
	}
	public int getRow() {
		return _range.getRow();
	}
	public int getLastColumn() {
		return _range.getLastColumn();
	}
	public int getLastRow() {
		return _range.getLastRow();
	}
	
	public void sync(RangeRunner run){
		ReadWriteLock lock = _range.getLock();
		lock.writeLock().lock();
		try{
			run.run(this);
		}finally{
			lock.writeLock().unlock();
		}
		return;
	}
	/**
	 * visit all cells in this range, make sure you call this in a limited range, 
	 * don't use it for all row/column selection, it will spend much time to iterate the cell 
	 * @param visitor the visitor 
	 * @param create create cell if it doesn't exist, if it is true, it will also lock the sheet
	 */
	public void visit(final CellVisitor visitor){
		final int r=getRow();
		final int lr=getLastRow();
		final int c=getColumn();
		final int lc=getLastColumn();
		
		Runnable run = new Runnable(){
			public void run(){
				for(int i=r;i<=lr;i++){
					for(int j=c;j<=lc;j++){
						if(!visitCell(visitor,i,j))
							break;
					}
				}
			}
		};
		
		ReadWriteLock lock = _range.getLock();
		lock.writeLock().lock();
		try{
			run.run();
		}finally{
			lock.writeLock().unlock();
		}
	}
	
	private boolean visitCell(CellVisitor visitor,int r, int c){
		boolean ignore = false;
		SSheet sheet = _range.getSheet();
		SCell cell = sheet.getCell(r,c);
		if(cell.isNull()){
			if(ignore){
				return true;
			}else{
				//use less call, to just compatible with 3.0
				visitor.createIfNotExist(r, c);
			}
		}
		return visitor.visit(new RangeImpl(SRanges.range(_range.getSheet(),r,c),_sharedCtx));
	}

	public Book getBook() {
		return getSheet().getBook();
	}
	
	public void applyBordersAround(BorderType borderType,String htmlColor){
		applyBorders(ApplyBorderType.OUTLINE,borderType, htmlColor);
	}
	
	public void applyBorders(ApplyBorderType type,BorderType borderType,String htmlColor){ 
		_range.setBorders(EnumUtil.toRangeApplyBorderType(type), EnumUtil.toRangeBorderType(borderType), htmlColor);
	}

	
	public boolean hasMergedCell(){
		CellRegion curr = new CellRegion(getRow(),getColumn(),getLastRow(),getLastColumn());
		for(CellRegion ma:_range.getSheet().getMergedRegions()){
			if(curr.overlaps(ma)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isMergedCell(){
		for(CellRegion ma:_range.getSheet().getMergedRegions()){
			if(ma.equals(getRow(),getColumn(),getLastRow(),getLastColumn())){
				return true;
			}
		}
		return false;
	}
	
	
	static private class Result<T> {
		T r;
		Result(){}
		Result(T r){
			this.r = r;
		}
		
		public T get(){
			return r;
		}
		
		public void set(T r){
			this.r = r;
		}
	}

	public void merge(boolean across){ 
		_range.merge(across);
	}
	
	public void unmerge(){ 
		_range.unmerge();
	}

	
	public RangeImpl toShiftedRange(int rowOffset,int colOffset){ 
		RangeImpl offsetRange = new RangeImpl(_range.getOffset(rowOffset, colOffset),_sharedCtx);
		return offsetRange;
	}
	
	
	public RangeImpl toCellRange(int rowOffset,int colOffset){
		RangeImpl cellRange = new RangeImpl(SRanges.range(_range.getSheet(),getRow()+rowOffset,getColumn()+colOffset),_sharedCtx);
		return cellRange;
	}
	
	/** get the top-left cell range of this range**/
	public RangeImpl getLeftTop() { 
		return toCellRange(0,0);
	}
	
	/**
	 *  Return a range that represents all columns and between the first-row and last-row of this range
	 **/
	public RangeImpl toRowRange(){ 
		return new RangeImpl(_range.getRows(),_sharedCtx);
	}
	
	/**
	 *  Return a range that represents all rows and between the first-column and last-column of this range
	 **/
	public RangeImpl toColumnRange(){ 
		return new RangeImpl(_range.getColumns(),_sharedCtx);
	}
	
	/**
	 * Check if this range represents a whole column, which mean all rows are included, 
	 */
	public boolean isWholeColumn(){ 
		return _range.isWholeColumn();
	}
	/**
	 * Check if this range represents a whole row, which mean all column are included, 
	 */
	public boolean isWholeRow(){ 
		return _range.isWholeRow();
	}
	/**
	 * Check if this range represents a whole sheet, which mean all column and row are included, 
	 */
	public boolean isWholeSheet(){ 
		return _range.isWholeSheet();
	}
	
	public void insert(InsertShift shift,InsertCopyOrigin copyOrigin){
		_range.insert(EnumUtil.toRangeInsertShift(shift), EnumUtil.toRangeInsertCopyOrigin(copyOrigin));
	}
	
	public void delete(DeleteShift shift){ 
		_range.delete(EnumUtil.toRangeDeleteShift(shift));
	}
	
	public void sort(boolean desc){	
		sort(desc,false,false,false,null);
	}
	
	public void sort(boolean desc,
			boolean hasHeader, 
			boolean matchCase, 
			boolean sortByRows, 
			SortDataOption dataOption){
		Range index = null;
		int r = getRow();
		int c = getColumn();
		int lr = getLastRow();
		int lc = getLastColumn();
		
		index = Ranges.range(this.getSheet(),r,c,sortByRows?r:lr,sortByRows?lc:c);
		
		sort(index,desc,dataOption,
			null,false,null,
			null,false,null,
			hasHeader,matchCase,sortByRows);
	}
	
	public void sort(Range key1,boolean desc1,SortDataOption dataOption1,
			Range key2,boolean desc2,SortDataOption dataOption2,
			Range key3,boolean desc3,SortDataOption dataOption3,
			boolean header, 
			boolean matchCase, 
			boolean sortByRows
			/*int orderCustom, //not implement*/
			/*int sortMethod, //not implement*/){
		
		_range.sort(key1==null?null:((RangeImpl)key1).getNative(), desc1, 
				dataOption1==null?SRange.SortDataOption.NORMAL_DEFAULT:EnumUtil.toRangeSortDataOption(dataOption1),
				key2==null?null:((RangeImpl)key2).getNative(), desc2, 
				dataOption2==null?SRange.SortDataOption.NORMAL_DEFAULT:EnumUtil.toRangeSortDataOption(dataOption2), 
				key3==null?null:((RangeImpl)key3).getNative(), desc3, 
				dataOption3==null?SRange.SortDataOption.NORMAL_DEFAULT:EnumUtil.toRangeSortDataOption(dataOption3),
				header?BookHelper.SORT_HEADER_YES:BookHelper.SORT_HEADER_NO, 
				matchCase, 
				sortByRows);
		
	}
	
	/** check if auto filter is enable or not.**/
	public boolean isAutoFilterEnabled(){
		final SSheet sheet = _range.getSheet();
		//ZSS-988
		final STable table = ((AbstractSheetAdv)sheet).getTableByRowCol(getRow(), getColumn());
		return table != null ? table.getAutoFilter() != null : sheet.getAutoFilter()!=null; 
	}
	
	// ZSS-246: give an API for user checking the auto-filtering range before applying it.
	public Range findAutoFilterRange() {
		SRange r = _range.findAutoFilterRange();
		if(r != null) {
			return Ranges.range(getSheet(), r.getRow(), r.getColumn(), r.getLastRow(), r.getLastColumn());
		} else {
			return null;
		}
	}

	/** enable/disable autofilter of the sheet**/
	public void enableAutoFilter(boolean enable){
		if(isAutoFilterEnabled() == enable){
			return ;
		} 
		_range.enableAutoFilter(enable);
	}
	/** enable filter with condition **/
	//TODO have to review this after I know more detail
	public void enableAutoFilter(int field, AutoFilterOperation filterOp, Object criteria1, Object criteria2, Boolean showButton){ 
		_range.enableAutoFilter(field,EnumUtil.toRangeAutoFilterOperation(filterOp),criteria1,criteria2,showButton);
	}
	
	/** clear criteria of all filters, show all the data**/
	public void resetAutoFilter(){
		_range.resetAutoFilter();
	}
	
	/** re-apply existing criteria of filters **/
	public void applyAutoFilter(){ 
		_range.applyAutoFilter();
	}
	
	@Deprecated
	public void protectSheet(String password){ 
		_range.protectSheet(password);
	}
	
	public void autoFill(Range dest,AutoFillType fillType){ 
		_range.fill(((RangeImpl)dest).getNative(), EnumUtil.toRangeFillType(fillType));
	}
	
	public void fillDown(){ 
		_range.fillDown();
	}
	
	public void fillLeft(){ 
		_range.fillLeft();
	}
	
	public void fillUp(){ 
		_range.fillUp();
	}
	
	public void fillRight(){ 
		_range.fillRight();
	}
	
	/** shift this range with a offset row and column**/
	public void shift(int rowOffset,int colOffset){ 
		_range.move(rowOffset, colOffset);
	}
	
	public String getCellEditText(){
		String txt = _range.getEditText();
		return txt==null?"":txt;
	}
	
	public void setCellEditText(String editText){ 
		try{
			_range.setEditText(editText);
		}catch(InvalidFormulaException x){
			throw new IllegalFormulaException(x.getMessage(),x);
		}
	}
	
	public String getCellFormatText(){ 
		return _range.getCellFormatText();
	}
	
	public String getCellDataFormat(){ 
		return _range.getCellDataFormat();
	}
	
	public Object getCellValue(){ 
		return _range.getValue();
	}
	
	public void setDisplaySheetGridlines(boolean enable){ 
		_range.setDisplayGridlines(enable);
	}
	
	public boolean isDisplaySheetGridlines(){ 
		return getSheet().isDisplayGridlines();
	}
	
	public void setHidden(boolean hidden){ 
		_range.setHidden(hidden);
	}
	
	public void setCellHyperlink(HyperlinkType type,String address,String display){ 
		_range.setHyperlink(EnumUtil.toHyperlinkType(type), address, display);
	}
	
	public Hyperlink getCellHyperlink(){ 
		SHyperlink l = _range.getHyperlink();
		return l==null?null:new HyperlinkImpl(new SimpleRef<SHyperlink>(l),getCellEditText());
	}
	
	public void setSheetName(String name){ 
		_range.setSheetName(name);
	}
	
	public String getSheetName(){
		return getSheet().getSheetName();
	}
	
	public void setSheetOrder(int pos){ 
		_range.setSheetOrder(pos);
	}
	
	public int getSheetOrder(){
		return getBook().getSheetIndex(getSheet());
	}
	
	public void setCellValue(Object value){
		_range.setValue(value);
	}
	
	private ModelRef<SBook> getBookRef(){
		return ((BookImpl)getBook()).getRef();
	}
	
	private ModelRef<SSheet> getSheetRef(){
		return ((SheetImpl)getSheet()).getRef();
	}
	

	/**
	 * get the first cell style of this range
	 * 
	 * @return cell style if cell is exist, the check row style and column cell style if cell not found, if row and column style is not exist, then return default style of sheet
	 */
	public CellStyle getCellStyle() {
		SCellStyle style = _range.getCellStyle();
		return new CellStyleImpl(getBookRef(), new SimpleRef<SCellStyle>(style));		
	}

	
	public Picture addPicture(SheetAnchor anchor,byte[] image,Format format){
		SPicture picture = _range.addPicture(SheetImpl.toViewAnchor(_range.getSheet(), anchor), image, EnumUtil.toPictureFormat(format));
		return new PictureImpl(new SimpleRef<SSheet>(_range.getSheet()), new SimpleRef<SPicture>(picture));
	}
	
	public void deletePicture(Picture picture){
		_range.deletePicture(((PictureImpl)picture).getNative());
	}
	
	public void movePicture(SheetAnchor anchor,Picture picture){
		_range.movePicture(((PictureImpl)picture).getNative(), SheetImpl.toViewAnchor(_range.getSheet(), anchor));
	}
	
	public Chart addChart(SheetAnchor anchor,Type type, Grouping grouping, LegendPosition pos){
		SChart chart =  _range.addChart(new ViewAnchor(anchor.getRow(), anchor.getColumn(), DEFAULT_CHART_WIDTH, DEFAULT_CHART_HEIGHT), 
				EnumUtil.toChartType(type), EnumUtil.toChartGrouping(grouping),
				EnumUtil.toLegendPosition(pos), EnumUtil.isThreeDimentionalChart(type));
		return new ChartImpl(new SimpleRef<SSheet>(_range.getSheet()), new SimpleRef<SChart>(chart));
	}
	
	
	public void deleteChart(Chart chart){
		_range.deleteChart(((ChartImpl)chart).getNative());
	}
	
	
	public void moveChart(SheetAnchor anchor,Chart chart){
		_range.moveChart(((ChartImpl)chart).getNative(), SheetImpl.toViewAnchor(_range.getSheet(), anchor));
	}
	
	@Override
	public void updateChart(Chart chart){
		_range.updateChart(((ChartImpl)chart).getNative());
	}
	
	public Sheet createSheet(String name){
		SSheet sheet = _range.createSheet(name);
		return new SheetImpl(getBookRef(),new SimpleRef(sheet));
	}

	public Sheet cloneSheet(String name){
		SSheet sheet = _range.cloneSheet(name);
		return new SheetImpl(getBookRef(),new SimpleRef(sheet));
	}

	public void deleteSheet(){
		_range.deleteSheet();
	}
	
	
	@Override
	public void setColumnWidth(int widthPx) {
		SRange r = _range.isWholeColumn()?_range:_range.getColumns();
		r.setColumnWidth(widthPx);
	}
	@Override
	public void setRowHeight(int heightPx) {
		SRange r = _range.isWholeRow()?_range:_range.getRows();
		r.setRowHeight(heightPx);
	}
	
	@Override
	public void setRowHeight(int heightPx, boolean isCustom) {
		SRange r = _range.isWholeRow()?_range:_range.getRows();
		r.setRowHeight(heightPx, isCustom);
	}
	
	public String toString(){
		return Ranges.getAreaRefString(getSheet(), getRow(),getColumn(),getLastRow(),getLastColumn());
	}
	
	/**
	 * Notify this range has been changed.
	 */
	@Override
	public void notifyChange(){ 
		_range.notifyChange();
	}
	
	@Override
	public void notifyChange(String[] variables){
		_range.notifyChange(variables);
	}
	
	@Override
	public void setFreezePanel(int rowfreeze, int columnfreeze) { 
		_range.setFreezePanel(rowfreeze, columnfreeze);
	}
	@Override
	public int getRowCount() {
		return _range.getLastRow()-_range.getRow()+1;
	}
	@Override
	public int getColumnCount() {
		return _range.getLastColumn()-_range.getColumn()+1;
	}
	@Override
	public String asString() {
		return Ranges.getAreaRefString(getSheet(), getRow(),getColumn(),getLastRow(),getLastColumn());
	}
	@Override
	public SRange getInternalRange() {
		return _range;
	}
	@Override
	public void createName(String nameName) {
		_range.createName(nameName);
	}
	@Override
	public void protectSheet(String password,
			boolean allowSelectingLockedCells,
			boolean allowSelectingUnlockedCells, boolean allowFormattingCells,
			boolean allowFormattingColumns, boolean allowFormattingRows,
			boolean allowInsertColumns, boolean allowInsertRows,
			boolean allowInsertingHyperlinks, boolean allowDeletingColumns,
			boolean allowDeletingRows, boolean allowSorting,
			boolean allowFiltering, boolean allowUsingPivotTables,
			boolean drawingObjects, boolean scenarios) {
		_range.protectSheet(password,
				allowSelectingLockedCells,
				allowSelectingUnlockedCells, allowFormattingCells,
				allowFormattingColumns, allowFormattingRows,
				allowInsertColumns, allowInsertRows,
				allowInsertingHyperlinks, allowDeletingColumns,
				allowDeletingRows, allowSorting,
				allowFiltering, allowUsingPivotTables,
				drawingObjects, scenarios);
	}
	@Override
	public boolean unprotectSheet(String password) {
		return _range.unprotectSheet(password);
	}

	@Override
	public SheetProtection getSheetProtection() {
		SSheetProtection ssp = _range.getSheetProtection();
		return ssp == null ?
				null : new SheetProtectionImpl(new SimpleRef<SSheet>(_range.getSheet()), new SimpleRef<SSheetProtection>(ssp));
	}

	@Override
	public Validation validate(final String editText) {
		SDataValidation dv = _range.validate(editText);
		return dv == null ? 
				null : new ValidationImpl(new SimpleRef<SDataValidation>(dv));
	}

	@Override
	public void setValidation(ValidationType validationType,
			boolean ignoreBlank, OperatorType operatorType,
			boolean inCellDropDown, String formula1, String formula2,
			boolean showInput, String inputTitle, String inputMessage,
			boolean showError, AlertStyle alertStyle, String errorTitle,
			String errorMessage) {
		_range.setValidation(validationType.getNative(), 
				ignoreBlank, operatorType.getNative(),
				inCellDropDown, formula1, formula2,
				showInput, inputTitle, inputMessage,
				showError, alertStyle.getNative(), errorTitle,
				errorMessage);
	}

	@Override
	public List<Validation> getValidations() {
		List<SDataValidation> dvs = _range.getValidations();
		List<Validation> vs = new ArrayList<Validation>(dvs.size());
		for (SDataValidation dv : dvs) {
			vs.add(new ValidationImpl(new SimpleRef<SDataValidation>(dv)));
		}
		return vs;
	}
	
	@Override
	public void deleteValidation() {
		_range.deleteValidation();
	}

	@Override
	public void setCellRichText(String html) {
		_range.setRichText(html);
	}
	
	@Override
	public String getCellRichText() {
		return _range.getRichText();
	}

	@Override
	public Font getOrCreateFont(Boldweight boldweight, Color color,
			int fontHeight, String fontName, boolean italic, boolean strikeout,
			TypeOffset typeOffset, Underline underline) {
		SFont font = _range.getOrCreateFont(
				EnumUtil.toFontBoldweight(boldweight), color.getHtmlColor(), 
				fontHeight, fontName, italic, strikeout,
				EnumUtil.toFontTypeOffset(typeOffset), 
				EnumUtil.toFontUnderline(underline));
		return new FontImpl(((BookImpl) getBook()).getRef(), new SimpleRef<SFont>(font));
	}

	@Override
	public void refresh(boolean includeDependants) {
		_range.refresh(includeDependants);
	}
	
	@Override
	public boolean setAutoRefresh(boolean auto) {
		return _range.setAutoRefresh(auto);
	}
	
	//ZSS-814
	@Override
	public void refresh(boolean includeDependants, boolean clearCache, boolean enforceEval) {
		_range.refresh(includeDependants, clearCache, enforceEval);
	}
	
	//ZSS-832
	@Override
	public void setSheetVisible(SheetVisible visible) {
		SRange.SheetVisible option = null;
		switch(visible) {
		case HIDDEN:
			option = SRange.SheetVisible.HIDDEN;
			break;
		case VISIBLE:
			option = SRange.SheetVisible.VISIBLE;
			break;
		case VERY_HIDDEN:
			option = SRange.SheetVisible.VERY_HIDDEN;
			break;
		}
		_range.setSheetVisible(option);
	}

	//ZSS-848
	public String getCommentRichText() {
		return _range.getCommentRichText();
	}

	//ZSS-848
	public void setCommentRichText(String html) {
		_range.setCommentRichText(html);
	}

	//ZSS-848
	public void setCommentVisible(boolean visible) {
		_range.setCommentVisible(visible);
	}
	
	//ZSS-848
	public boolean isCommentVisible() {
		return _range.isCommentVisible();
	}

	//ZSS-939
	@Override
	public void notifyChange(CellAttribute cellAttr){ 
		_range.notifyChange(org.zkoss.zss.model.impl.CellAttribute.values()[cellAttr.ordinal()]);
	}
	
	//ZSS-966
	@Override
	public void setNameName(String namename, String newname) {
		_range.setNameName(namename, newname);
	}
	
	//ZSS-1046
	//@Since 3.8.0
	@Override
	public void setStringValue(String text) {
		_range.setStringValue(text);
	}
}
