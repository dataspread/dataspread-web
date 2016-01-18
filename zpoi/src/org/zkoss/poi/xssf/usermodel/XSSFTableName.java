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

import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.poi.ss.formula.FormulaParser;
import org.zkoss.poi.ss.formula.FormulaType;
import org.zkoss.poi.ss.usermodel.Name;
import org.zkoss.poi.ss.util.AreaReference;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDefinedName;

/**
 * Represents a table named range in a SpreadsheetML workbook.
 * @since 3.9.7
 */
public final class XSSFTableName {
    private XSSFWorkbook _workbook;
    private XSSFTable _table;

    /**
     * Creates an XSSFName object - called internally by XSSFWorkbook.
     *
     * @param name - the xml bean that holds data represenring this defined name.
     * @param workbook - the workbook object associated with the name
     * @see org.zkoss.poi.xssf.usermodel.XSSFWorkbook#createName()
     */
    protected XSSFTableName(XSSFTable table, XSSFWorkbook workbook) {
        _workbook = workbook;
        _table = table;
    }

    public XSSFTable getTable() {
    	return _table;
    }
    
    /**
     * Returns the name that will appear in the user interface for the table name.
     *
     * @return text name of this defined name
     */
    public String getTableNameName() {
        return _table.getName();
    }

    /**
     * Sets the name that will appear in the user interface for the defined name.
     * Names must begin with a letter or underscore, not contain spaces and be unique across the workbook.
     */
    public void setTableNameName(String name) {
        validateName(name);

        //Check to ensure no other anems have the same case-insensitive table name
        if (_workbook.getTableName(name.toUpperCase()) != null) {
            String msg = "The workbook already contains this name: " + name;
            throw new IllegalArgumentException(msg);
        }
        
        //Check to ensure no other names have the same case-insensitive name
        int sheetIndex = -1;
        for (int i = 0; i < _workbook.getNumberOfNames(); i++) {
            XSSFName nm = _workbook.getNameAt(i);
            if(name.equalsIgnoreCase(nm.getNameName()) && sheetIndex == nm.getSheetIndex()){
                String msg = "The "+(sheetIndex == -1 ? "workbook" : "sheet")+" already contains this name: " + name;
                throw new IllegalArgumentException(msg);
            }
        }
        
        final String oldName = getTableNameName();
        _workbook.renameTableName(oldName, name); // remove and put in workbook
    }

    @Override
    public int hashCode() {
        return _table.getName().hashCode();
    }

    /**
     * Compares this name to the specified object.
     * The result is <code>true</code> if the argument is XSSFName and the
     * underlying CTDefinedName bean equals to the CTDefinedName representing this name
     *
     * @param   o   the object to compare this <code>XSSFName</code> against.
     * @return  <code>true</code> if the <code>XSSFName </code>are equal;
     *          <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if(o == this) return true;

        if (!(o instanceof XSSFTableName)) return false;

        XSSFTableName cf = (XSSFTableName) o;
        return _table.getName().equals(cf._table.getName());
    }

    private static void validateName(String name){
        if(name.length() == 0)  throw new IllegalArgumentException("Name cannot be blank");
        char c = name.charAt(0);
        if(!(c == '_' || Character.isLetter(c)) || name.indexOf(' ') != -1) {
            throw new IllegalArgumentException("Invalid name: '"+name+"'; Names must begin with a letter or underscore and not contain spaces");
        }
    }
}
