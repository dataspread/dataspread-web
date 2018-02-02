package org.zkoss.zss.ui.impl;

public class HeaderPositionInfo {
	//[0]: column/row index, [1]: width/height, [2]: column/row id
	private int index; //column/row idnex
	private int size; //width/height in pixel
	private int id; //column/row uuid
	private boolean hidden; //whether the column/row is hidden
	private boolean custom = true;

	private HeaderPositionInfo(){

	}

	public HeaderPositionInfo(int index, int size, int id, boolean hidden, boolean isCustom) {
		this.index = index;
		this.size = size;
		this.id = id;
		this.hidden = hidden;
		this.custom = isCustom;
	}

	public  int getIndex(){
		return index;
	}

	public void setIndex(int index){this.index = index;}
	public void setHidden(boolean hidden){this.hidden = hidden;}
	public  void setId(int id){this.id = id;}
	public void setSize(int size){this.size = size;}

	public  int getSize(){
		return size;
	}

	public  int getId(){
		return id;
	}

	public  boolean getHidden(){
		return hidden;
	}

	public boolean isCustom() {
		return custom;
	}

	public void setCustom(boolean custom) {
		this.custom = custom;
	}
}
