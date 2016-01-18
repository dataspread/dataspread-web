/* HSSFChartDecoder.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/8/30 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.range.impl.imexp;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.zkoss.poi.ddf.EscherChildAnchorRecord;
import org.zkoss.poi.ddf.EscherClientAnchorRecord;
import org.zkoss.poi.ddf.EscherClientDataRecord;
import org.zkoss.poi.ddf.EscherComplexProperty;
import org.zkoss.poi.ddf.EscherContainerRecord;
import org.zkoss.poi.ddf.EscherOptRecord;
import org.zkoss.poi.ddf.EscherProperty;
import org.zkoss.poi.ddf.EscherRecord;
import org.zkoss.poi.ddf.EscherSpRecord;
import org.zkoss.poi.hssf.record.LabelSSTRecord;
import org.zkoss.poi.hssf.record.Record;
import org.zkoss.poi.hssf.record.RecordBase;
import org.zkoss.poi.hssf.record.aggregates.BOFRecordAggregate;
import org.zkoss.poi.hssf.record.chart.AreaRecord;
import org.zkoss.poi.hssf.record.chart.BarRecord;
import org.zkoss.poi.hssf.record.chart.BeginRecord;
import org.zkoss.poi.hssf.record.chart.Chart3DRecord;
import org.zkoss.poi.hssf.record.chart.ChartRecord;
import org.zkoss.poi.hssf.record.chart.ChartTitleFormatRecord;
import org.zkoss.poi.hssf.record.chart.EndRecord;
import org.zkoss.poi.hssf.record.chart.LegendRecord;
import org.zkoss.poi.hssf.record.chart.LineRecord;
import org.zkoss.poi.hssf.record.chart.LinkedDataRecord;
import org.zkoss.poi.hssf.record.chart.PieRecord;
import org.zkoss.poi.hssf.record.chart.ScatterRecord;
import org.zkoss.poi.hssf.record.chart.SeriesIndexRecord;
import org.zkoss.poi.hssf.record.chart.SeriesRecord;
import org.zkoss.poi.hssf.record.chart.SeriesTextRecord;
import org.zkoss.poi.hssf.record.chart.TextRecord;
import org.zkoss.poi.hssf.record.chart.ValueRangeRecord;
import org.zkoss.poi.hssf.usermodel.HSSFAnchor;
import org.zkoss.poi.hssf.usermodel.HSSFChart;
import org.zkoss.poi.hssf.usermodel.HSSFChartShape;
import org.zkoss.poi.hssf.usermodel.HSSFChildAnchor;
import org.zkoss.poi.hssf.usermodel.HSSFClientAnchor;
import org.zkoss.poi.hssf.usermodel.HSSFPatriarch;
import org.zkoss.poi.hssf.usermodel.HSSFPatriarchHelper;
import org.zkoss.poi.hssf.usermodel.HSSFShape;

/**
 * copied from zssex.
 * @author dennis
 * @since 3.0.0 (ZPOI 3.9)
 */
public class HSSFChartDecoder {
	private HSSFChartShape _chart;
	HSSFPatriarchHelper _helper;
	public HSSFChartDecoder(HSSFPatriarchHelper helper,HSSFChartShape chart) {
		_helper = helper;
		_chart = chart;
	}

	//code refer form DrawingAggregateRecord
	public void decode() {
		if(_chart.getChartInfo()!=null)
			return;
		EscherContainerRecord container = _helper.getContainer((HSSFShape)_chart);
		decodeChartRecord(_helper.getPatriarch(),container);
	}
	
	
	
	private void decodeChartRecord(HSSFPatriarch patriarch,EscherContainerRecord container) {
        final List<EscherRecord> recordList = container.getChildRecords();
        final Iterator<EscherRecord> recordIter = recordList.iterator();
        EscherChildAnchorRecord childAnchorRecord = null;
        EscherClientAnchorRecord clientAnchorRecord = null;
        EscherOptRecord optRecord = null;
        EscherSpRecord spRecord = null;
        EscherClientDataRecord dataRecord= null;
        
        while(recordIter.hasNext()) {
        	EscherRecord childRecord = recordIter.next();
        	
        	switch(childRecord.getRecordId()) {
        	case EscherSpRecord.RECORD_ID: //spRecord
        		spRecord = (EscherSpRecord) childRecord;
        		break;
        	case EscherOptRecord.RECORD_ID: //optRecord
            	optRecord = (EscherOptRecord) childRecord;
        		break;
        	case EscherChildAnchorRecord.RECORD_ID: //childAnchor
        		childAnchorRecord = (EscherChildAnchorRecord) childRecord;
        		break;
        	case EscherClientAnchorRecord.RECORD_ID: //clientAnchor
            	clientAnchorRecord = (EscherClientAnchorRecord) childRecord;
            	break;
        	case EscherClientDataRecord.RECORD_ID: //dataRecord
            	dataRecord = (EscherClientDataRecord) childRecord;
            	break;
        	}
        }
        
        if (spRecord != null) {
	        //handle patriarch
			HSSFAnchor anchor = null;  
			if (childAnchorRecord != null) {
		   		final int dx1 = Math.min(1023, childAnchorRecord.getDx1());
		   		final int dy1 = Math.min(255, childAnchorRecord.getDy1());
		   		final int dx2 = Math.min(1023, childAnchorRecord.getDx2());
		   		final int dy2 = Math.min(255, childAnchorRecord.getDy2());
		   		anchor = new HSSFChildAnchor(dx1, dy1, dx2, dy2);
			} else if (clientAnchorRecord != null) {
        		//prepare client anchor
		   		final int dx1 = Math.min(1023, clientAnchorRecord.getDx1());
		   		final int dy1 = Math.min(255, clientAnchorRecord.getDy1());
		   		final int dx2 = Math.min(1023, clientAnchorRecord.getDx2());
		   		final int dy2 = Math.min(255, clientAnchorRecord.getDy2());
		   		final short col1 = clientAnchorRecord.getCol1();
		   		final int row1 = clientAnchorRecord.getRow1();
		   		final short col2 = clientAnchorRecord.getCol2();
		   		final int row2 = clientAnchorRecord.getRow2();
		   		anchor = new HSSFClientAnchor(dx1, dy1, dx2, dy2, col1, row1, col2, row2);
			}
			if (spRecord.isHaveAnchor() && anchor != null) {
				decodeChartRecord(patriarch,optRecord, anchor);
			}
		}
    }
	
	private void decodeChartRecord(HSSFPatriarch patriarch,EscherOptRecord optRecord, HSSFAnchor anchor) {
        String chartName = null;
		try {
    		for (EscherProperty pro : (List<EscherProperty>) optRecord.getEscherProperties()) {
    			switch(pro.getPropertyNumber()) {
    			//TODO more properties for Chart
    			/*
    		    propNum: 127, RAW: 0x007F, propName: protection.lockagainstgrouping, complex: false, blipId: false, value: 31785220 (0x01E50104)
    		    propNum: 191, RAW: 0x00BF, propName: text.sizetexttofitshape, complex: false, blipId: false, value: 524296 (0x00080008)
    		    propNum: 385, RAW: 0x0181, propName: fill.fillcolor, complex: false, blipId: false, value: 134217806 (0x0800004E)
    		    propNum: 447, RAW: 0x01BF, propName: fill.nofillhittest, complex: false, blipId: false, value: 1048592 (0x00100010)
    		    propNum: 959, RAW: 0x03BF, propName: groupshape.print, complex: false, blipId: false, value: 131072 (0x00020000)
    		    */
    			case 896: //chart name
					chartName = new String(((EscherComplexProperty)pro).getComplexData(), "UTF-16LE").trim();
    				break;
    			}
    		}
		} catch (UnsupportedEncodingException e) {
			//ignore
		}
		final HSSFChart chart = decodeChartRecord(_helper.getChartBOF(_chart));
		_chart.setName(chartName);
		_chart.setAnchor(anchor);
		_chart.setChart(chart);
    }
	
	private HSSFChart decodeChartRecord(BOFRecordAggregate bofagg) {; 
		List<RecordBase> bofRecords = (bofagg==null?null:bofagg.getInnerRecords());
		if(bofRecords==null){
			return null;
		}
    	Chart3DRecord chart3d = null;
		ChartRecord chart = null;
		TextRecord titleTextRecord = null;
		List<Object[]> seriesList = new ArrayList<Object[]>();
		Object[] lastSeries = null; //0: SeriesRecord; 1: optional LinkedDataRecord; 2: optional SeriesTextRecord(as series title); 3: optional categories 
		LegendRecord legend = null;
		ChartTitleFormatRecord chartTitleFormat = null;
		SeriesTextRecord chartTitleText = null;
		List<ValueRangeRecord> valueRanges = new ArrayList<ValueRangeRecord>();
		Stack<Record> stack = new Stack<Record>();
		Record chartType = null;
		Record preR = null;
		SeriesIndexRecord siIndex = null;
		//TODO handle only partial records in a Chart Stream
		for(RecordBase rb : bofRecords) {
			if(!(rb instanceof Record)){
				continue;
			}
			Record r = ((Record)rb);
			switch(r.getSid()) {
			case BeginRecord.sid:
				stack.push(preR);
				break;
			case EndRecord.sid:
				final Record popR = stack.pop();
				if (popR.getSid() == SeriesRecord.sid){ //end of a series
					lastSeries = null;
				}
				break;
			case ChartRecord.sid:
				chart = (ChartRecord) r;
				break;
			case Chart3DRecord.sid:
				chart3d = (Chart3DRecord) r;
				break;
			case LegendRecord.sid:
				legend = (LegendRecord) r;
				break;
			case SeriesRecord.sid:
				lastSeries = new Object[] {r, new ArrayList<LinkedDataRecord>(), null, null};
				seriesList.add(lastSeries);
				break;
			case ChartTitleFormatRecord.sid:
				chartTitleFormat = (ChartTitleFormatRecord)r;
				break;
			case SeriesTextRecord.sid:
				// Applies to a series, unless we've seen
				//  a legend already
				final SeriesTextRecord str = (SeriesTextRecord)r;
				if(legend == null && lastSeries != null) {
					lastSeries[2] = str;
				} else {
					chartTitleText = str;
				}
				break;
			case TextRecord.sid:
				{
					final Record peekR = stack.peek();
					if (peekR instanceof ChartRecord && preR.getSid() == 2215) { //TextRecord of CrtLayout12A: has something to do with Title
						titleTextRecord = (TextRecord) r;
					}
					break;
				}
			case LinkedDataRecord.sid:
				final LinkedDataRecord linkedDataRecord = (LinkedDataRecord) r;
				final Record peekR = stack.peek();
				switch(peekR.getSid()) {
				case SeriesRecord.sid:
					if (lastSeries != null) {
						((List<LinkedDataRecord>)lastSeries[1]).add(linkedDataRecord);
					}
					break;
				//TODO other Record type shall handle LinkedDataRecord, too.(AI, include formula)
				}
				break;
			case ValueRangeRecord.sid:
				valueRanges.add((ValueRangeRecord)r);
				break;
			case AreaRecord.sid:
			case BarRecord.sid:
			case LineRecord.sid:
			case PieRecord.sid:
			case ScatterRecord.sid:
				chartType = r;
				break;
			case SeriesIndexRecord.sid: //20110111, henrichen@zkoss.org: prefix of different type of Number records
				siIndex = (SeriesIndexRecord) r;
				break;
			case LabelSSTRecord.sid: //20110111, henrichen@zkoss.org: literal category title?
				if (siIndex != null && siIndex.getIndex() == 2) { //2: Catgory labels, [MS-XLS].pdf page 423
					//Label not within a series definition block!
					Object[] ser = lastSeries == null && seriesList.size() > 0 ?  
						seriesList.get(seriesList.size() - 1) : lastSeries;
					if (ser != null) {
						List<LabelSSTRecord> cats = (List<LabelSSTRecord>) ser[3];
						if (cats == null) {
							ser[3] = cats = new ArrayList<LabelSSTRecord>();
						}
						cats.add((LabelSSTRecord)r);
					}
				}
				break;
			}
			preR = r;
		}
		return new HSSFChart(_helper.getSheet(), chart, legend, chartTitleFormat, chartTitleText, seriesList, valueRanges, chartType, chart3d, titleTextRecord);
	}

}
