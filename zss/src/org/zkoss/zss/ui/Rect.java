package org.zkoss.zss.ui;

import org.zkoss.zss.api.AreaRef;

/**
 * A class to represent a rectangle range with 4 value : left, top, right, bottom.
 * @author Dennis.Chen
 * @deprecated since 3.0.0 use {@link AreaRef}
 */
public class Rect extends AreaRef{
	private static final long serialVersionUID = -660274288545569502L;

	public Rect(){
	}
	
	public Rect(int left,int top,int right,int bottom){
		setArea(top,left,bottom,right);
	}
	
	public void set(int left,int top,int right,int bottom){
		setArea(top,left,bottom,right);
	}
	
	public int getLeft() {
		return getColumn();
	}

	public void setLeft(int left) {
		setColumn(left);
	}

	public int getTop() {
		return getRow();
	}

	public void setTop(int top) {
		setRow(top);
	}

	public int getRight() {
		return getLastColumn();
	}

	public void setRight(int right) {
		setLastColumn(right);
	}

	public int getBottom() {
		return getLastRow();
	}

	public void setBottom(int bottom) {
		setLastRow(bottom);
	}
}
