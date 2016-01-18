package org.zkoss.zss.model;

import org.zkoss.zss.model.util.Validations;

/**
 * This class contains various paste options used for {@link SSheet#pasteCell(SheetRegion, CellRegion, PasteOption)}.
 * @since 3.5.0
 */
public class PasteOption {

	public enum PasteType{
		ALL,/*BookHelper.INNERPASTE_FORMATS + BookHelper.INNERPASTE_VALUES_AND_FORMULAS + BookHelper.INNERPASTE_COMMENTS + BookHelper.INNERPASTE_VALIDATION;*/ 
		ALL_EXCEPT_BORDERS,/*PASTE_ALL - BookHelper.INNERPASTE_BORDERS;*/
		COLUMN_WIDTHS,/* = BookHelper.INNERPASTE_COLUMN_WIDTHS;*/
		COMMENTS,/* = BookHelper.INNERPASTE_COMMENTS;*/
		FORMATS,/* = BookHelper.INNERPASTE_FORMATS; //all formats*/
		FORMULAS,/* = BookHelper.INNERPASTE_VALUES_AND_FORMULAS; //include values and formulas*/
		FORMULAS_AND_NUMBER_FORMATS,/* = PASTE_FORMULAS + BookHelper.INNERPASTE_NUMBER_FORMATS;*/
		VALIDATAION,/* = BookHelper.INNERPASTE_VALIDATION;*/
		VALUES,/* = BookHelper.INNERPASTE_VALUES;*/
		VALUES_AND_NUMBER_FORMATS/* = PASTE_VALUES + BookHelper.INNERPASTE_NUMBER_FORMATS;*/
	}
	
	public enum PasteOperation{
		ADD,/* = BookHelper.PASTEOP_ADD;*/
		SUB,/* = BookHelper.PASTEOP_SUB;*/
		MUL,/* = BookHelper.PASTEOP_MUL;*/
		DIV,/* = BookHelper.PASTEOP_DIV;*/
		NONE/* = BookHelper.PASTEOP_NONE;*/
	}
	
	private boolean _skipBlank = false;
	private boolean _cut = false;
	private boolean _transpose = false;
	
	private PasteType _pasteType = PasteType.ALL;
	private PasteOperation _pasteOperation = PasteOperation.NONE;
	
	public boolean isSkipBlank() {
		return _skipBlank;
	}
	public void setSkipBlank(boolean skipBlank) {
		this._skipBlank = skipBlank;
	}
	public PasteType getPasteType() {
		return _pasteType;
	}
	public void setPasteType(PasteType pasteType) {
		Validations.argNotNull(pasteType);
		this._pasteType = pasteType;
	}
	public PasteOperation getPasteOperation() {
		return _pasteOperation;
	}
	public void setPasteOperation(PasteOperation pasteOperation) {
		Validations.argNotNull(pasteOperation);
		this._pasteOperation = pasteOperation;
	}
	
	/** 
	 * Shall cut the source region after paste
	 * @return
	 */
	public boolean isCut() {
		return _cut;
	}
	/**
	 * Set true to enable cutting source region after paste
	 * @param cut
	 */
	public void setCut(boolean cut) {
		this._cut = cut;
	}
	public boolean isTranspose() {
		return _transpose;
	}
	public void setTranspose(boolean transpose) {
		this._transpose = transpose;
	}
	
	
	
}
