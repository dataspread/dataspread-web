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
package org.zkoss.zss.model.impl;

import org.zkoss.zss.model.SPictureData;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.ViewAnchor;
/**
 * 
 * @author dennis
 * @since 3.5.0
 */
public class PictureImpl extends AbstractPictureAdv {
	private static final long serialVersionUID = -8176040020483451498L;


	private String _id;
	
	
	private SPictureData _picData; //since 3.6.0
	private ViewAnchor _anchor;
	private AbstractSheetAdv _sheet;

	/** This constructor will create a {@link SPictureData} internally */
	public PictureImpl(AbstractSheetAdv sheet, String id, Format format,
			byte[] data, ViewAnchor anchor) {
		this._sheet = sheet;
		this._id = id;
		this._anchor = anchor;
		
		_picData = _sheet.getBook().addPictureData(format, data);
	}
	
	//ZSS-735
	//since 3.6.0
	/** This constructor use the existing picData in the {@SBook}. */
	public PictureImpl(AbstractSheetAdv sheet, String id, int picDataIndex,
			ViewAnchor anchor) {
		this._sheet = sheet;
		this._id = id;
		this._anchor = anchor;
		
		final SPictureData picData = sheet.getBook().getPictureData(picDataIndex);
		if (picData == null)
			throw new IllegalStateException("Inexisting picture data index: "+picDataIndex);
		_picData = picData;
	}
	
	@Override
	public SSheet getSheet(){
		checkOrphan();
		return _sheet;
	}
	@Override
	public String getId() {
		return _id;
	}
	@Override
	public Format getFormat() {
		return _picData.getFormat();
	}
	@Override
	public ViewAnchor getAnchor() {
		return _anchor;
	}

	@Override
	public void setAnchor(ViewAnchor anchor){
		this._anchor = anchor;
	}
	
	@Override
	public byte[] getData() {
		return _picData.getData();
	}

	@Override
	public void destroy() {
		checkOrphan();
		_sheet = null;
	}

	@Override
	public void checkOrphan() {
		if (_sheet == null) {
			throw new IllegalStateException("doesn't connect to parent");
		}
	}
	
	@Override
	public SPictureData getPictureData() {
		return _picData;
	}
	
	//ZSS-688
	//@since 3.6.0
	/*package*/ PictureImpl clonePictureImpl(AbstractSheetAdv sheet) {
		return new PictureImpl(sheet, this._id, this._picData.getIndex(),
				this._anchor.cloneViewAnchor());
	}
}
