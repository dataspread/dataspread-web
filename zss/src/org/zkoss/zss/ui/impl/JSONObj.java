/* JSONObj.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jan 8, 2008 10:48:05 AM     2008, Created by Dennis.Chen
}}IS_NOTE

Copyright (C) 2007 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
	This program is distributed under GPL Version 2.0 in the hope that
	it will be useful, but WITHOUT ANY WARRANTY.
}}IS_RIGHT
*/
package org.zkoss.zss.ui.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A util class to handle JSON response.
 * @author Dennis.Chen
 *
 */
public class JSONObj {

	Map _data = new LinkedHashMap();
	
	public void setData(String name,String value){
		setDataMap(name,value);
	}
	
	public void setData(String name,Number number){
		setDataMap(name,number);
	}
	
	public void setData(String name,boolean bool){
		setDataMap(name,(bool)?Boolean.TRUE:Boolean.FALSE);
	}
	
	public void setData(String name,int number){
		setDataMap(name,Integer.valueOf(number));
	}
	public void setData(String name,long number){
		setDataMap(name,Long.valueOf(number));
	}
	
	public void setData(String name,float number){
		setDataMap(name,new Float(number));
	}
	public void setData(String name,double number){
		setDataMap(name,new Double(number));
	}
	
	public void setData(String name,JSONObj jsonobj){
		setDataMap(name,jsonobj);
	}
	public void setData(String name,List list){
		setDataMap(name,list);
	}
	public void setData(String name,Object[] list){
		ArrayList al = new ArrayList();
		for(int i=0;i<list.length;i++){
			al.add(list[i]);
		}
		setDataMap(name,al);
	}
	
	private void setDataMap(String name,Object obj){
		_data.put(name,obj);
	}
	
	private void toFormatedString(StringBuffer sb){
		sb.append("{");
		boolean first = true;
		for(Iterator iter = _data.entrySet().iterator();iter.hasNext();){
			Map.Entry entry = (Map.Entry)iter.next();
			String key = (String)entry.getKey();
			Object value = entry.getValue();
			if(first){
				first = false;
			}else{
				sb.append(",");
			}
			sb.append("\"").append(key).append("\":");
			appendObj(sb,value);
		}
		
		sb.append("}");
	}
	
	
	private void appendObj(StringBuffer sb,Object obj){
		if(obj==null) obj="";
		if(obj instanceof String){
			String str = (String)obj;
			sb.append("\"");
			char[] chars = str.toCharArray();
			for(int i=0;i<chars.length;i++){
				switch(chars[i]){
				case '"':
					sb.append("\\\"");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					break;
				default:
					sb.append(chars[i]);
				}
			}
			sb.append("\"");
		}else if(obj instanceof Number){
			sb.append(((Number)obj).toString());
		}else if(obj instanceof Boolean){
			sb.append(((Boolean)obj).booleanValue()?"true":"false");
		}else if(obj instanceof JSONObj){
			((JSONObj)obj).toFormatedString(sb);
		}else if(obj instanceof List){
			sb.append("[");
			boolean first = true;
			for(Iterator iter = ((List)obj).iterator();iter.hasNext();){
				Object value = iter.next();
				if(first){
					first = false;
				}else{
					sb.append(",");
				}
				appendObj(sb,value);
			}
			sb.append("]");
		}
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		toFormatedString(sb);
		return sb.toString();
	}
	
	
	static public void main(String args[]){

		JSONObj obj1 = new JSONObj();
		JSONObj obj2 = new JSONObj();
		JSONObj obj3 = new JSONObj();
		JSONObj obj4 = new JSONObj();
		
		obj1.setData("index",1);
		obj1.setData("txt","abc");
		obj1.setData("format","ddd");
		
		obj2.setData("index",2);
		obj2.setData("txt","def");
		obj2.setData("format","kkk");
		
		obj3.setData("type","row");
		obj3.setData("index",1);
		obj3.setData("cells",new Object[]{obj1,obj2});
		
		obj4.setData("data",new Object[]{obj3});
		
		System.out.println(obj4.toString());
		
		
		
	}
}
