/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.zkoss.poi.xssf.usermodel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.zkoss.poi.POIXMLDocumentPart;
import org.zkoss.poi.openxml4j.opc.PackagePart;
import org.zkoss.poi.openxml4j.opc.PackageRelationship;
import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.poi.xssf.usermodel.helpers.XSSFXmlColumnPr;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAutoFilter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.TableDocument;

/**
 * 
 * This class implements the Table Part (Open Office XML Part 4:
 * chapter 3.5.1)
 * 
 * This implementation works under the assumption that a table contains mappings to a subtree of an XML.
 * The root element of this subtree an occur multiple times (one for each row of the table). The child nodes
 * of the root element can be only attributes or element with maxOccurs=1 property set
 * 
 *
 * @author Roberto Manicardi
 */
public class XSSFTable extends POIXMLDocumentPart {
	
	private CTTable ctTable;
	private List<XSSFXmlColumnPr> xmlColumnPr;
	private CellReference startCellReference;
	private CellReference endCellReference;	
	private String commonXPath;
	private List<XSSFTableColumn> _columns;
	private XSSFAutoFilter _autoFilter; //cache; ZSS-1019
	
	
	public XSSFTable() {
		super();
		ctTable = CTTable.Factory.newInstance();
		initAutoFilter(); //ZSS-1019
	}

	public XSSFTable(PackagePart part, PackageRelationship rel)
			throws IOException {
		super(part, rel);
		readFrom(part.getInputStream());
		initAutoFilter(); //ZSS-1019
	}

	public void readFrom(InputStream is) throws IOException {
		try {
			TableDocument doc = TableDocument.Factory.parse(is);
			ctTable = doc.getTable();
		} catch (XmlException e) {
			throw new IOException(e.getLocalizedMessage());
		}
	}
	
	public XSSFSheet getXSSFSheet(){
		return (XSSFSheet) getParent();
	}

	public void writeTo(OutputStream out) throws IOException {
		TableDocument doc = TableDocument.Factory.newInstance();
		doc.setTable(ctTable);
		doc.save(out, DEFAULT_XML_OPTIONS);
	}

	@Override
	protected void commit() throws IOException {
		PackagePart part = getPackagePart();
		OutputStream out = part.getOutputStream();
		writeTo(out);
		out.close();
	}
	
	public CTTable getCTTable(){
		return ctTable;
	}
	
	/**
	 * Checks if this Table element contains even a single mapping to the map identified by id
	 * @param id the XSSFMap ID
	 * @return true if the Table element contain mappings
	 */
	public boolean mapsTo(long id){
		boolean maps =false;
		
		List<XSSFXmlColumnPr> pointers = getXmlColumnPrs();
		
		for(XSSFXmlColumnPr pointer: pointers){
			if(pointer.getMapId()==id){
				maps=true;
				break;
			}
		}
		
		return maps;
	}

	
	/**
	 * 
	 * Calculates the xpath of the root element for the table. This will be the common part
	 * of all the mapping's xpaths
	 * 
	 * @return the xpath of the table's root element
	 */
	public String getCommonXpath() {
		
		if(commonXPath == null){
		
		String[] commonTokens ={};
		
		for(CTTableColumn column :ctTable.getTableColumns().getTableColumnList()){
			if(column.getXmlColumnPr()!=null){
				String xpath = column.getXmlColumnPr().getXpath();
				String[] tokens =  xpath.split("/");
				if(commonTokens.length==0){
					commonTokens = tokens;
					
				}else{
					int maxLenght = commonTokens.length>tokens.length? tokens.length:commonTokens.length;
					for(int i =0; i<maxLenght;i++){
						if(!commonTokens[i].equals(tokens[i])){
						 List<String> subCommonTokens = Arrays.asList(commonTokens).subList(0, i);
						 
						 String[] container = {};
						 
						 commonTokens = subCommonTokens.toArray(container);
						 break;
						 
						 
						}
					}
				}
				
			}
		}
		
		
		commonXPath ="";
		
		for(int i = 1 ; i< commonTokens.length;i++){
			commonXPath +="/"+commonTokens[i];
		
		}
		}
		
		return commonXPath;
	}

	
	public List<XSSFXmlColumnPr> getXmlColumnPrs() {
		
		if(xmlColumnPr==null){
			xmlColumnPr = new Vector<XSSFXmlColumnPr>();
			for(CTTableColumn column:ctTable.getTableColumns().getTableColumnList()){
				if(column.getXmlColumnPr()!=null){
					XSSFXmlColumnPr columnPr = new XSSFXmlColumnPr(this,column,column.getXmlColumnPr());
					xmlColumnPr.add(columnPr);
				}
			}
		}
		return xmlColumnPr;
	}
	
	/**
	 * @return the name of the Table, if set
	 */
	public String getName() {
	   return ctTable.getName();
	}
	
	/**
	 * Changes the name of the Table
	 */
	public void setName(String name) {
	   if(name == null) {
	      ctTable.unsetName();
	      return;
	   }
	   ctTable.setName(name);
	}

   /**
    * @return the display name of the Table, if set
    */
   public String getDisplayName() {
      return ctTable.getDisplayName();
   }

   /**
    * Changes the display name of the Table
    */
   public void setDisplayName(String name) {
      ctTable.setDisplayName(name);
   }

	/**
	 * @return  the number of mapped table columns (see Open Office XML Part 4: chapter 3.5.1.4)
	 */
	public long getNumerOfMappedColumns(){
		return ctTable.getTableColumns().getCount();
	}
	
	
	/**
	 * @return The reference for the cell in the top-left part of the table
	 * (see Open Office XML Part 4: chapter 3.5.1.2, attribute ref) 
	 *
	 */
	public CellReference getStartCellReference() {
		
		if(startCellReference==null){			
				String ref = ctTable.getRef();
				String[] boundaries = ref.split(":");
				String from = boundaries[0];
				startCellReference = new CellReference(from);
		}
		return startCellReference;
	}
	
	/**
	 * @return The reference for the cell in the bottom-right part of the table
	 * (see Open Office XML Part 4: chapter 3.5.1.2, attribute ref)
	 *
	 */
	public CellReference getEndCellReference() {
		
		if(endCellReference==null){
			
				String ref = ctTable.getRef();
				String[] boundaries = ref.split(":");
				String from = boundaries[1];
				endCellReference = new CellReference(from);
		}
		return endCellReference;
	}
	
	
	/**
	 *  @return the total number of rows in the selection. (Note: in this version autofiltering is ignored)
	 *
	 */
	public int getRowCount(){
		
		
		CellReference from = getStartCellReference();
		CellReference to = getEndCellReference();
		
		int rowCount = -1;
		if (from!=null && to!=null){
		 rowCount = to.getRow()-from.getRow();
		}
		return rowCount;
	}
	
	//ZSS-855
	public int getHeaderRowCount() {
		return ctTable.isSetHeaderRowCount() ? (int) ctTable.getHeaderRowCount() : 1; //default to 1
	}
	
	//ZSS-855
	public int getTotalsRowCount() {
		return ctTable.isSetTotalsRowCount() ? (int) ctTable.getTotalsRowCount() : 0; //default to 0
	}
	
	//ZSS-855
	public CTTableStyleInfo getTableStyleInfo() {
		return ctTable.getTableStyleInfo();
	}

	//ZSS-855
	public List<XSSFTableColumn> getTableColumns() {
		if (_columns == null) {
			_columns = new ArrayList<XSSFTableColumn>();
			for (CTTableColumn ctTbCol : ctTable.getTableColumns().getTableColumnArray()) {
				_columns.add(new XSSFTableColumn(ctTbCol));
			}
		}
		return _columns;
	}
	
	//ZSS-855
	public XSSFTableColumn addTableColumn() {
		if (_columns == null) {
			_columns = new ArrayList<XSSFTableColumn>();
		}
		if (ctTable.getTableColumns() == null) {
			ctTable.addNewTableColumns();
		}
		final CTTableColumn ctTbCol = ctTable.getTableColumns().addNewTableColumn();
		XSSFTableColumn col = new XSSFTableColumn(ctTbCol);
		_columns.add(col);
		ctTable.getTableColumns().setCount(_columns.size());
		return col;
	}
	
    //ZSS-1019
    private void initAutoFilter(){
		_autoFilter = ctTable.isSetAutoFilter() ? 
				new XSSFAutoFilter(ctTable.getAutoFilter()) : null;
    }

	//ZSS-855
	public XSSFAutoFilter getAutoFilter() {
		return _autoFilter; //ZSS-1019
	}
	
	//ZSS-855
	public void setRef(String ref) {
		ctTable.setRef(ref);
	}
	
	//ZSS-855
	public void setTotalsRowCount(int count) {
		if (count == 0) {//default is 0
			if (ctTable.isSetTotalsRowCount())
				ctTable.unsetTotalsRowCount();
		} else
			ctTable.setTotalsRowCount(count);
	}
	
	//ZSS-855
	public void setHeaderRowCount(int count) {
		if (count != 0) {
			if (ctTable.isSetHeaderRowCount()) 
				ctTable.unsetHeaderRowCount(); //default is 1
		} else {
			ctTable.setHeaderRowCount(count);
		}
	}
	
	//ZSS-855
	public XSSFTableStyleInfo createTableStyleInfo() {
		final CTTableStyleInfo ctInfo = ctTable.addNewTableStyleInfo();
		return new XSSFTableStyleInfo(ctInfo);
	}
	
	//ZSS-855
	public void clearAutoFilter() {
		_autoFilter = null;  //ZSS-1019
		if (ctTable.isSetAutoFilter())
			ctTable.unsetAutoFilter();
	}
	
	//ZSS-855
	public XSSFAutoFilter createAutoFilter() {
		// no header row => no filter
		// default is 1
		if (ctTable.isSetHeaderRowCount() && ctTable.getHeaderRowCount() == 0) {
			_autoFilter = null; //ZSS-1019
			return null;
		}
		
		if (!ctTable.isSetAutoFilter()) {
			ctTable.addNewAutoFilter();
			initAutoFilter(); //ZSS-1019
		}
		return _autoFilter;
	}
	
	//ZSS-855
	public void setId(int id) {
		ctTable.setId(id);
	}
}
