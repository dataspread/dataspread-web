package org.zkoss.zss.model.impl;

/**
 * Created by Sajjadur on 11/6/2017.
 */
public class Bucket<T> {
    T minValue;
    T maxValue;
    int startPos;
    int endPos;
    int childCount;
    int size;
    String name;


    @Override
    public String toString() {
        if (minValue==null || maxValue==null)
            return null;

        if(minValue.toString().equals(maxValue.toString()))
            return minValue.toString();
        return minValue.toString() + " to " + maxValue.toString();
    }

    public T getMaxValue(){return  maxValue;}
    public T getMinValue(){return  minValue;}
    public int getStartPos(){return  startPos;}
    public int getEndPos(){return  endPos;}
    public int getChildCount(){return childCount;}
    public int getSize(){return size;}
    public String getName(){
        return minValue.toString().equals(maxValue.toString())?minValue.toString():minValue.toString()+" to "+maxValue.toString();
    }
}
