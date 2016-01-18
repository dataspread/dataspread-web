package org.zkoss.poi.hssf.record.aggregates;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.poi.hssf.model.RecordStream;
import org.zkoss.poi.hssf.record.AutoFilter12Record;
import org.zkoss.poi.hssf.record.AutoFilterInfoRecord;
import org.zkoss.poi.hssf.record.AutoFilterRecord;
import org.zkoss.poi.hssf.record.ContinueFrt12Record;
import org.zkoss.poi.hssf.record.Record;
import org.zkoss.poi.ss.usermodel.FilterColumn;

/**
 * 
 * @author PeterKuo
 * @deprecated by dennischen@zkoss.org, 2013/8/13, this class doesn't implement well and cause ZSS-408 Cannot save 2003 format if the file contains auto filter configuration.
 */
public class AutoFilterInfoRecordAggregate extends RecordAggregate {

	private final List<Record> records;
	
	/**
	 * Creates an empty aggregate
	 */
	public AutoFilterInfoRecordAggregate() {
		records = new ArrayList<Record>();
	}

	
	/**
	 * 
	 * @param rs
	 */
	public AutoFilterInfoRecordAggregate(RecordStream rs) {
		this();
		if(rs.peekNextSid() == AutoFilterInfoRecord.sid){
			records.add(rs.getNext());
			
			while( rs.peekNextSid() == AutoFilterRecord.sid 
					|| rs.peekNextSid() == ContinueFrt12Record.sid
					|| rs.peekNextSid() == AutoFilter12Record.sid){
				records.add(rs.getNext());
			}
		}
	}
	
	@Override
	public void visitContainedRecords(RecordVisitor rv) {
		int nItems = records.size();
		if (nItems < 1) {
			return;
		}
		for(int i=0; i<nItems; i++) {
			rv.visitRecord(records.get(i));
		}
	}
	
	public void setNumEntries(short numEntries){
		if(records.size() == 0){
			AutoFilterInfoRecord r = new AutoFilterInfoRecord();
			r.setNumEntries(numEntries);
			records.add(r);
		}else{
			AutoFilterInfoRecord r = (AutoFilterInfoRecord) records.get(0);
			r.setNumEntries(numEntries);
		}
	}

	//TODO:
	public List<String> getValuesOfFilter(int column) {
		List<String> result = new ArrayList<String>();
		int nItems = records.size();
		int startPosition = 0;
		boolean found = false;
		for(int i=0;i<nItems;i++){
			if(records.get(i) instanceof AutoFilterRecord){
				AutoFilterRecord afRecord = (AutoFilterRecord)records.get(i);
				if(afRecord.getColEntries() == column){
					startPosition = i;				
					found = true;
					break;
				}
			}
		}
		
		if(found = false){
			return null;
		}
		
		if(records.get(startPosition+1) instanceof AutoFilter12Record){
			AutoFilter12Record af12Record = (AutoFilter12Record) records.get(startPosition+1);
			int count = af12Record.getCCriteria();
			for(int i=0;i<count;i++){
				ContinueFrt12Record cfrt12Record = (ContinueFrt12Record) records.get(startPosition+2+i);
				result.add(cfrt12Record.getStr());
			}
		}else{
			AutoFilterRecord afRecord = (AutoFilterRecord) records.get(startPosition);
			if(afRecord.getStr1() != null){
				result.add(afRecord.getStr1());
			}
			if(afRecord.getStr2() != null){
				result.add(afRecord.getStr2());
			}
		}
		
		return result;
	}
}
