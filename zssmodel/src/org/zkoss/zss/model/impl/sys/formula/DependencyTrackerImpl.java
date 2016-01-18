/* DependencyTrackerImpl.java

	Purpose:
		
	Description:
		
	History:
		Dec 26, 2013 Created by Pao Wang

Copyright (C) 2013 Potix Corporation. All Rights Reserved.
 */
package org.zkoss.zss.model.impl.sys.formula;

import java.util.Set;

import org.zkoss.poi.ss.formula.DependencyTracker;
import org.zkoss.poi.ss.formula.EvaluationWorkbook;
import org.zkoss.poi.ss.formula.LazyAreaEval;
import org.zkoss.poi.ss.formula.LazyRefEval;
import org.zkoss.poi.ss.formula.NameRangeEval;
import org.zkoss.poi.ss.formula.OperationEvaluationContext;
import org.zkoss.poi.ss.formula.eval.ErrorEval;
import org.zkoss.poi.ss.formula.eval.NameEval;
import org.zkoss.poi.ss.formula.eval.ValueEval;
import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.impl.AbstractBookSeriesAdv;
import org.zkoss.zss.model.impl.AbstractNameAdv;
import org.zkoss.zss.model.impl.NameRefImpl;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.impl.sys.DependencyTableAdv;
import org.zkoss.zss.model.sys.dependency.IndirectRef;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.dependency.Ref.RefType;

/**
 * A default dependency tracker.
 * @author Pao
 */
public class DependencyTrackerImpl implements DependencyTracker {

	@Override
	public ValueEval postProcessValueEval(OperationEvaluationContext ec, ValueEval opResult, boolean eval) {
		// resolving name to be invalid 
		if(eval && opResult instanceof NameEval) {
			return ErrorEval.NAME_INVALID;
		}
		return opResult;
	}

	@Override
	public void addDependency(OperationEvaluationContext ec, Ptg[] ptgs) {
		// do nothing, we don't need POI dependency tracking
	}

	//ZSS-845, ZSS-834
	@Override
	public void clearIndirectRefPrecedent(OperationEvaluationContext ec) {
		EvaluationWorkbook book = ec.getWorkbook();
		if (book instanceof EvalBook) {
			EvalBook evalBook = (EvalBook) book;
			SBook sbook = evalBook.getNBook();
			Ref dependent = (Ref) ec.getDependent();
			if (dependent == null) {
				final String bookName = sbook.getBookName();
				final String sheetName = ec.getSheetName();
				final int row = ec.getRowIndex();
				final int col = ec.getColumnIndex();
				dependent = new RefImpl(bookName, sheetName, row, col);
			}
			if (dependent != null) {
				final AbstractBookSeriesAdv series = (AbstractBookSeriesAdv) sbook.getBookSeries();
				final DependencyTableAdv table = (DependencyTableAdv) series.getDependencyTable();
				final int ptgIndex = ec.getPtgIndex();
				final Set<Ref> precedents = table.getDirectPrecedents(dependent);
				for (Ref precedent : precedents) {
					if (precedent.getType() == RefType.INDIRECT 
							&& ((IndirectRef)precedent).getPtgIndex() == ptgIndex) {
						// clear precedents of the found IndirectRef
						table.clearDependents(precedent);
						break;
					}
				}
			}
		}
	}

	//ZSS-845, ZSS-834
	@Override
	public void setIndirectRefPrecedent(OperationEvaluationContext ec, ValueEval preRef) {
		EvaluationWorkbook book = ec.getWorkbook();
		if (book instanceof EvalBook) {
			EvalBook evalBook = (EvalBook) book;
			SBook sbook = evalBook.getNBook();
			Ref dependent = (Ref) ec.getDependent();
			if (dependent == null) {
				final String bookName = sbook.getBookName();
				final String sheetName = ec.getSheetName();
				final int row = ec.getRowIndex();
				final int col = ec.getColumnIndex();
				dependent = new RefImpl(bookName, sheetName, row, col);
			}
			if (dependent != null) {
				final AbstractBookSeriesAdv series = (AbstractBookSeriesAdv) sbook.getBookSeries();
				final DependencyTableAdv table = (DependencyTableAdv) series.getDependencyTable();
				final int ptgIndex = ec.getPtgIndex();
				final Set<Ref> precedents = table.getDirectPrecedents(dependent);
				for (Ref precedent : precedents) {
					if (precedent.getType() == RefType.INDIRECT 
							&& ((IndirectRef)precedent).getPtgIndex() == ptgIndex) {
						Ref indirectPrecedent = null;
						// add precedent of the found IndirectRef
						if (preRef instanceof NameRangeEval) {
							final AbstractNameAdv sname = (AbstractNameAdv) sbook.getName(((NameRangeEval) preRef).getNamePtg().getIndex());
							indirectPrecedent = new NameRefImpl(sname);
						} else if (preRef instanceof LazyRefEval) {
							final LazyRefEval refEval = (LazyRefEval) preRef;
							indirectPrecedent = new RefImpl(refEval.getBookName(), refEval.getSheetName(), refEval.getLastSheetName(), refEval.getRow(), refEval.getColumn());
						} else if (preRef instanceof LazyAreaEval) {
							final LazyAreaEval refEval = (LazyAreaEval) preRef;
							indirectPrecedent = new RefImpl(refEval.getBookName(), refEval.getSheetName(), refEval.getLastSheetName(), refEval.getFirstRow(), refEval.getFirstColumn(), refEval.getLastRow(), refEval.getLastColumn());
						}
						table.add(precedent, indirectPrecedent);
						break;
					}
				}
			}
		}
	}
}
