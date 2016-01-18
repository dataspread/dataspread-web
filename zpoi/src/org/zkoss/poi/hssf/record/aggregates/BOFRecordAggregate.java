/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.zkoss.poi.hssf.record.aggregates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.zkoss.poi.hssf.record.BOFRecord;
import org.zkoss.poi.hssf.record.EOFRecord;
import org.zkoss.poi.hssf.record.Record;
import org.zkoss.poi.hssf.record.RecordBase;

/**
 * Aggregation of bof
 * @author dennischen@zkoss.org
 *
 */
public class BOFRecordAggregate extends RecordAggregate {
	
	final BOFRecord bofRecord;
	final EOFRecord eofRecord;
	final List<RecordBase> innerRecords;
	
	public BOFRecordAggregate(BOFRecord bofRecord,EOFRecord eofRecord,List<RecordBase> innerRecords){
		this.bofRecord = bofRecord;
		this.eofRecord = eofRecord;
		this.innerRecords = new ArrayList<RecordBase>(innerRecords);
	}
	
	public List<RecordBase> getInnerRecords(){
		return Collections.unmodifiableList(innerRecords);
	}

	@Override
	public void visitContainedRecords(RecordVisitor rv) {
		rv.visitRecord(bofRecord);
		for(RecordBase r:innerRecords){
			if(r instanceof Record){
				rv.visitRecord((Record)r);
			}else if(r instanceof RecordAggregate){
				((RecordAggregate)r).visitContainedRecords(rv);
			}
		}
		rv.visitRecord(eofRecord);
	}
}
