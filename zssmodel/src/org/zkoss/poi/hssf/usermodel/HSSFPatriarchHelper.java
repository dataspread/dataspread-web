/* HSSFPatriarchHelper.java

	Purpose:
		
	Description:
		
	History:
		Oct 14, 2010 5:13:07 PM, Created by henrichen

Copyright (C) 2010 Potix Corporation. All Rights Reserved.
*/

package org.zkoss.poi.hssf.usermodel;

import org.zkoss.poi.ddf.EscherContainerRecord;
import org.zkoss.poi.hssf.record.EscherAggregate;
import org.zkoss.poi.hssf.record.aggregates.BOFRecordAggregate;

/**
 * Copied from zpoiex. We should remove it after integration.
 * A helper class to make HSSFPatriarch package method visible.
 * @author henrichen
 * @author dennischen
 */
public class HSSFPatriarchHelper {
	private HSSFPatriarch _patriarch;
	
	public HSSFPatriarchHelper(HSSFPatriarch patriarch) {
		_patriarch = patriarch;
	}
	
	public EscherAggregate getBoundAggregate() {
		return (EscherAggregate) _patriarch._getBoundAggregate();
	}
	
	public HSSFPatriarch getPatriarch(){
		return _patriarch;
	}
	
	public HSSFSheet getSheet() {
		return _patriarch.getSheet();
	}

	public EscherContainerRecord getContainer(HSSFShape shape) {
		return shape.getEscherContainer();
	}
	
	public BOFRecordAggregate getChartBOF(HSSFChartShape chart) {
		return chart.getBOFRecordAggregate();
	}
}
