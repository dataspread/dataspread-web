/* 
	Purpose:
		
	Description:
		
	History:
		2013/7/10, Created by dennis

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.repository.impl;

import java.io.File;
import java.util.Date;

import org.zkoss.zss.app.BookInfo;
/**
 * 
 * @author dennis
 *
 */
public class SimpleBookInfo implements BookInfo{
	private static final long serialVersionUID = -7407433808475436480L;
	private String name;
	private Date lastModify;
	private File file;
	private String id;
	public SimpleBookInfo(File file,String name, Date lastModify){
		this.file = file;
		this.name = name;
		this.lastModify = lastModify;		
	}
	
	public SimpleBookInfo(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getLastModify() {
		return new Date(file.lastModified());
	}
	public File getFile() {
		return file;
	}
//	public void setFile(File file) {
//		this.file = file;
//	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleBookInfo other = (SimpleBookInfo) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		return true;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("{name:").append(name).append(", file:"+file).append("}");
		return sb.toString();
	}
}
