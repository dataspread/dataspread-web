package org.zkoss.zss.model.impl;

import java.util.List;

/**
 * Created by Sajjadur on 11/6/2017.
 */
public class Bucket<T> {
    T minValue;
    T maxValue;
    int startPos;
    int endPos;
    int size;
    int childrenCount;
    String name;
    List<Bucket<T>> children;


    @Override
    public String toString() {
        if (minValue==null || maxValue==null)
            return null;
        return minValue.toString().equals(maxValue.toString())?minValue.toString():minValue.toString()+" to "+maxValue.toString();
    }

    public T getMaxValue(){return  maxValue;}
    public T getMinValue(){return  minValue;}
    public int getStartPos(){return  startPos;}
    public int getEndPos(){return  endPos;}
    public int getChildrenCount(){return childrenCount;}
    public int getSize(){return size;}
    public String getName(){ return this.toString(); }
    public List<Bucket<T>> getChildren(){return children;}
    public void setChildren(List<Bucket<T>> children){
        this.children=children;
        this.childrenCount = children.size();
    }
}
