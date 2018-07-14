package org.zkoss.poi.xssf.usermodel;

import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.zkoss.poi.ss.usermodel.ClientAnchor;
import org.zkoss.poi.ss.usermodel.Combo;

public final class XSSFCombo extends XSSFShape implements Combo{

	private ClientAnchor clientAnchor;
	
	
	public XSSFCombo(int col, int row1){
		clientAnchor = new XSSFClientAnchor();
		clientAnchor.setCol1(col);
		clientAnchor.setRow1(row1);
	}
	
	@Override
	protected CTShapeProperties getShapeProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClientAnchor getClientAnchor() {
		return clientAnchor;
	}

}
