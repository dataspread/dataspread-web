/* Created by kindalu 2009/3/2
 * */


package org.zkoss.zss.ui.impl;

import java.io.Serializable;
import java.lang.ref.WeakReference;

import org.zkoss.zss.ui.Spreadsheet;

public class Focus implements Serializable {
	private static final long serialVersionUID = 2401696322103952998L;
	private final String id;
	private String name;
	private String color;
	private int row,col;
	private String sheetId;
	final transient private WeakReference<Spreadsheet> ss;
	
	public Focus(String id, String name, String color, String sheetId, int row, int col, Spreadsheet ss) {
		this.id=id;
		this.name=name;
		this.color=color;
		this.row=row;
		this.col=col;
		this.sheetId = sheetId;
		this.ss= new WeakReference<Spreadsheet>(ss);
	}
	public String getId() {
		return this.id;
	}
	public String getSheetId(){
		return this.sheetId;
	}
	public String getName(){
		return name;
	}
	public int getRow(){
		return row;
	}
	public int getColumn(){
		return col;
	}
	public void setPosition(int row, int col){
		this.row = row;
		this.col = col;
	}
	public void setSheetId(String sheetId){
		this.sheetId = sheetId;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getColor() {
		return color;
	}
	public boolean isDetached() {
		try{
			return ss == null || ss.get() == null || ss.get().getDesktop() == null || !ss.get().getDesktop().isAlive();
		}catch(Exception x){
			return true;
		}
	}
	public int hashCode() {
		return id == null ? 0 : id.hashCode();
	}
	
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (!(other instanceof Focus)) {
			return false;
		}
		
		return  id.equals(((Focus)other).id);
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Focus[id:").append(id)
				.append(",name:").append(name)
				.append(",color:").append(color)
				.append(",sheetId:").append(sheetId)
				.append(",row:").append(row)
				.append(",column:").append(col)
				.append("]");
		
		return sb.toString();
	}
	public void setColor(String color) {
		this.color = color;
	}
}
