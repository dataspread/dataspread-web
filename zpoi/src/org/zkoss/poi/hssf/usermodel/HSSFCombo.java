package org.zkoss.poi.hssf.usermodel;

import org.zkoss.poi.ss.usermodel.ClientAnchor;
import org.zkoss.poi.ss.usermodel.Combo;

public class HSSFCombo extends HSSFSimpleShape implements Combo{

	public HSSFCombo(HSSFShape parent, HSSFAnchor anchor) {
		super(parent, anchor);
		setShapeType(OBJECT_TYPE_COMBO_BOX);
	}

	@Override
	public ClientAnchor getClientAnchor() {
		HSSFClientAnchor anchor = (HSSFClientAnchor)getAnchor();
		return anchor;
	}
}
