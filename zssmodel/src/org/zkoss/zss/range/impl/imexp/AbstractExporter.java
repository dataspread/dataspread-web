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
package org.zkoss.zss.range.impl.imexp;

import java.io.*;

import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SheetRegion;
import org.zkoss.zss.range.SExporter;

/**
 * Defines common behaviors for an exporter.
 * @author dennis
 * @since 3.5.0
 */
public abstract class AbstractExporter implements SExporter, Serializable{
	@Override
	public void export(SBook book, File file) throws IOException {
		OutputStream os = null;
		try{
			os = new FileOutputStream(file);
			export(book,os);
		}finally{
			if(os!=null){
				try{
					os.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	public void export(SSheet sheet, OutputStream fos) throws IOException{
		throw new UnsupportedOperationException("doesn't support this api");
	}

	public void export(SheetRegion sheetRegion, OutputStream fos) throws IOException{
		throw new UnsupportedOperationException("doesn't support this api");
	}
}
