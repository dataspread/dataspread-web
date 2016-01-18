package org.zkoss.zss.model.impl;

import java.util.Iterator;
import java.util.Set;

import org.zkoss.util.logging.Log;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SBookSeries;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SChart;
import org.zkoss.zss.model.SDataValidation;
import org.zkoss.zss.model.SRow;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.sys.dependency.ObjectRef;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.dependency.ObjectRef.ObjectType;
import org.zkoss.zss.model.sys.dependency.Ref.RefType;

/**
 * 
 * @author dennis
 * @since 3.5.0
 */
/*package*/ class FormulaCacheClearHelper {
	private final SBookSeries _bookSeries;
	
	private static final Log logger = Log.lookup(FormulaCacheClearHelper.class.getName());
	
	public FormulaCacheClearHelper(SBookSeries bookSeries) {
		this._bookSeries = bookSeries;
	}

	public void clear(Set<Ref> refs) {
		// clear formula cache
		for (Ref ref : refs) {
			if(logger.debugable()){
				logger.debug("Clear Formula Cache: "+ref);
			}
			//clear the dependent's formula cache since the precedent is changed.
			if (ref.getType() == RefType.CELL || ref.getType() == RefType.AREA) {
				handleAreaRef(ref);
			} else if (ref.getType() == RefType.OBJECT) {
				if(((ObjectRef)ref).getObjectType()==ObjectType.CHART){
					handleChartRef((ObjectRef)ref);
				}else if(((ObjectRef)ref).getObjectType()==ObjectType.DATA_VALIDATION){
					handleDataValidationRef((ObjectRef)ref);
				}
			} else {// TODO another

			}
		}
	}
	private void handleChartRef(ObjectRef ref) {
		SBook book = _bookSeries.getBook(ref.getBookName());
		if(book==null) return;
		SSheet sheet = book.getSheetByName(ref.getSheetName());
		if(sheet==null) return;
		String[] ids = ref.getObjectIdPath();
		SChart chart = sheet.getChart(ids[0]);
		if(chart!=null){
			chart.getData().clearFormulaResultCache();
		}
	}
	private void handleDataValidationRef(ObjectRef ref) {
		SBook book = _bookSeries.getBook(ref.getBookName());
		if(book==null) return;
		SSheet sheet = book.getSheetByName(ref.getSheetName());
		if(sheet==null) return;
		String[] ids = ref.getObjectIdPath();
		SDataValidation validation = sheet.getDataValidation(ids[0]);
		if(validation!=null){
			validation.clearFormulaResultCache();
		}
	}

	private void handleAreaRef(Ref ref) {
		SBook book = _bookSeries.getBook(ref.getBookName());
		if(book==null) return;
		SSheet sheet = book.getSheetByName(ref.getSheetName());
		if(sheet==null) return;
		
		boolean wholeRow = ref.getColumn()==0 && ref.getLastColumn()>=book.getMaxColumnIndex();
		boolean wholeColumn = ref.getRow()==0 && ref.getLastRow()>=book.getMaxRowIndex();
		boolean wholeSheet = wholeRow && wholeColumn;
		
		if(wholeSheet){
			Iterator<SRow> rows = sheet.getRowIterator();
			while(rows.hasNext()){
				Iterator<SCell> cells = sheet.getCellIterator(rows.next().getIndex());
				while(cells.hasNext()){
					cells.next().clearFormulaResultCache();
				}
			}
		}else if(wholeRow){
			//from column 0 to max
			for(int r = ref.getRow();r<=ref.getLastRow();r++){
				Iterator<SCell> cells = sheet.getCellIterator(r);
				while(cells.hasNext()){
					cells.next().clearFormulaResultCache();
				}
			}
		}else if(wholeColumn){
			//from row 0 to max
			Iterator<SRow> rows = sheet.getRowIterator();
			while(rows.hasNext()){
				int r = rows.next().getIndex();
				for(int c = ref.getColumn();c<=ref.getLastColumn();c++){
					SCell cell = ((AbstractSheetAdv)sheet).getCell(r,c,false);
					if(cell!=null){
						cell.clearFormulaResultCache();
					}
				}
			}
		}else{
			for(int r = ref.getRow();r<=ref.getLastRow();r++){
				for(int c = ref.getColumn();c<=ref.getLastColumn();c++){
					SCell cell = ((AbstractSheetAdv)sheet).getCell(r,c,false);
					if(cell!=null){
						cell.clearFormulaResultCache();
					}
				}
			}
		}
	}
}
