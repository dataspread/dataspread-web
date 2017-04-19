/* 
	Purpose:
		
	Description:
		
	History:
		2013/7/10, Created by dennis

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.repository.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 
 * @author dennis
 *
 */
public class FileUtil {

	public static String getNameExtension(String filename){
		int i = filename.lastIndexOf('.');
		if (i > 0) {
		    return filename.substring(i+1);
		}
		return "";
	}
	
	public static String getName(String filename){
		int i = filename.lastIndexOf('.');
		if (i > 0) {
		    return filename.substring(0,i);
		}
		return filename;
	}

	public static void copy(File src, File dest) throws IOException {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		byte[] buff = new byte[1024];
		try{
			fis = new FileInputStream(src);
			fos = new FileOutputStream(dest);
			int r;
			while( (r=fis.read(buff))>-1){
				fos.write(buff,0,r);
			}
		}finally{
			if(fis!=null){
				try{
					fis.close();
				}catch(Exception x){}
			}
			if(fos!=null){
				try{
					fos.close();
				}catch(Exception x){}
			}
		}
	}
	
	
}
