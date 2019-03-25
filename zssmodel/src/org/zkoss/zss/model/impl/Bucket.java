package org.zkoss.zss.model.impl;

import java.io.Serializable;
import java.util.*;

/**
 * Created by Sajjadur on 11/6/2017.
 */
public class Bucket<T> implements Serializable {
    T minValue;
    T maxValue;
    LinkedHashSet<T> leaves;
    int startPos;
    int endPos;
    int size;
    int childrenCount;
    String name;
    String id;
    String summary;
    ArrayList<Bucket> children;
    Map<String, Object> aggMem;

    Bucket() {
        aggMem = new HashMap<>();
    }

    @Override
    public String toString() {
        if (minValue == null || maxValue == null)
            return null;
        return minValue.toString().equals(maxValue.toString()) ? minValue.toString() : minValue.toString() + " - " + maxValue.toString();
    }

    public T getMaxValue() {
        return maxValue;
    }

    public T getMinValue() {
        return minValue;
    }

    public int getStartPos() {
        return startPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public int getChildrenCount() {
        return childrenCount;
    }

    public int getSize() {
        return size;
    }

    public LinkedHashSet<T> getLeaves() {
        return leaves;
    }

    public void setLeaves(LinkedHashSet<T> leaves)
    {
        this.leaves = leaves;
    }
    public void setName(boolean isUniform) {
        if (isUniform)
            name = (this.startPos + 2) + "_" + (this.endPos + 2);
        else
            name = this.toString();
    }

    public String getName() {
        if (name.contains("_"))
            return "Rows:" + name.replaceAll("_", "-");
        return name;
    }

    public String getId() {
        return this.id;
    }

    public ArrayList<Bucket> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<Bucket> children) {
        this.children = children;
        this.childrenCount = children.size();
        this.size = this.endPos - this.startPos + 1;
    }

    public boolean isSingleton() {
        return maxValue.equals(minValue);
    }

    public void setId() {

        this.id = this.name.replaceAll(" ", "_");
        //this.id = this.name.replaceAll(" ","_")+this.getSaltString();
    }

    public String getSummary() {
        summary = "Name: " + this.getName() + "\n";
        summary += "Sub-categories: " + this.childrenCount + "\n";
        summary += "[Start,End]: [" + (this.startPos + 2) + "," + (this.endPos + 2) + "]\n";
        summary += "Rows: " + this.size;
        return summary;
    }

    private String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 10) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

}
