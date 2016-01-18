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
import java.net.URL;

import org.zkoss.zss.model.SBook;
import org.zkoss.zss.range.SImporter;

/**
 * 
 * @author Hawk
 * @since 3.5.0 
 */
public abstract class AbstractImporter implements SImporter{

	@Override
	public SBook imports(File file, String bookName) throws IOException {
		InputStream is = null;
		try{
			is = new BufferedInputStream(new FileInputStream(file));
			return imports(is,bookName);
		}finally{
			if(is!=null){
				try{
					is.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public SBook imports(URL url, String bookName) throws IOException {
		InputStream is = null;
		try {
			is = url.openStream();
			return imports(is, bookName);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
