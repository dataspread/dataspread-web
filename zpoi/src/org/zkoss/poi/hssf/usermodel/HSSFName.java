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

package org.zkoss.poi.hssf.usermodel;

import org.zkoss.poi.hssf.model.HSSFFormulaParser;
import org.zkoss.poi.hssf.model.InternalWorkbook;
import org.zkoss.poi.hssf.record.NameCommentRecord;
import org.zkoss.poi.hssf.record.NameRecord;
import org.zkoss.poi.ss.formula.ptg.Ptg;
import org.zkoss.poi.ss.formula.FormulaType;
import org.zkoss.poi.ss.usermodel.Name;

/**
 * High Level Representation of a 'defined name' which could be a 'built-in' name,
 * 'named range' or name of a user defined function.
 *
 * @author Libin Roman (Vista Portal LDT. Developer)
 */
public final class HSSFName implements Name {
    private HSSFWorkbook _book;
    private NameRecord _definedNameRec;
    private NameCommentRecord _commentRec;

    /** 
     * Creates new HSSFName   - called by HSSFWorkbook to create a name from
     * scratch.
     *
     * @see org.zkoss.poi.hssf.usermodel.HSSFWorkbook#createName()
     * @param name the Name Record
     * @param book workbook object associated with the sheet.
     */
    /* package */ HSSFName(HSSFWorkbook book, NameRecord name) {
      this(book, name, null);
    }
    /** 
     * Creates new HSSFName   - called by HSSFWorkbook to create a name from
     * scratch.
     *
     * @see org.zkoss.poi.hssf.usermodel.HSSFWorkbook#createName()
     * @param name the Name Record
     * @param comment the Name Comment Record, optional.
     * @param book workbook object associated with the sheet.
     */
    /* package */ HSSFName(final HSSFWorkbook book, final NameRecord name, final NameCommentRecord comment) {
        _book = book;
        _definedNameRec = name;
        _commentRec = comment;
    }

    /** Get the sheets name which this named range is referenced to
     * @return sheet name, which this named range referred to
     */
    public String getSheetName() {
        int indexToExternSheet = _definedNameRec.getExternSheetNumber();

        return _book.getWorkbook().findSheetNameFromExternSheet(indexToExternSheet);
    }

    /**
     * @return text name of this defined name
     */
    public String getNameName(){
        return _definedNameRec.getNameText();
    }

    /**
     * Sets the name of the named range
     *
     * <p>The following is a list of syntax rules that you need to be aware of when you create and edit names.</p>
     * <ul>
     *   <li><strong>Valid characters</strong>
     *   The first character of a name must be a letter, an underscore character (_), or a backslash (\).
     *   Remaining characters in the name can be letters, numbers, periods, and underscore characters.
     *   </li>
     *   <li><strong>Cell references disallowed</strong>
     *   Names cannot be the same as a cell reference, such as Z$100 or R1C1.</li>
     *   <li><strong>Spaces are not valid</strong>
     *   Spaces are not allowed as part of a name. Use the underscore character (_) and period (.) as word separators, such as, Sales_Tax or First.Quarter.
     *   </li>
     *   <li><strong>Name length</strong>
     *    A name can contain up to 255 characters.
     *   </li>
     *   <li><strong>Case sensitivity</strong>
     *   Names can contain uppercase and lowercase letters.
     *   </li>
     * </ul>
     *
     * <p>
     * A name must always be unique within its scope. POI prevents you from defining a name that is not unique
     * within its scope. However you can use the same name in different scopes. Example:
     * <pre><blockquote>
     * //by default names are workbook-global
     * HSSFName name;
     * name = workbook.createName();
     * name.setNameName("sales_08");
     *
     * name = workbook.createName();
     * name.setNameName("sales_08"); //will throw an exception: "The workbook already contains this name (case-insensitive)"
     *
     * //create sheet-level name
     * name = workbook.createName();
     * name.setSheetIndex(0); //the scope of the name is the first sheet
     * name.setNameName("sales_08");  //ok
     *
     * name = workbook.createName();
     * name.setSheetIndex(0);
     * name.setNameName("sales_08");  //will throw an exception: "The sheet already contains this name (case-insensitive)"
     *
     * </blockquote></pre>
    * </p>
     *
     * @param nameName named range name to set
     * @throws IllegalArgumentException if the name is invalid or the name already exists (case-insensitive)
     */
    public void setNameName(String nameName){
        validateName(nameName);

        InternalWorkbook wb = _book.getWorkbook();
        _definedNameRec.setNameText(nameName);

        int sheetNumber = _definedNameRec.getSheetNumber();

        //Check to ensure no other names have the same case-insensitive name
        for ( int i = wb.getNumNames()-1; i >=0; i-- )
        {
            NameRecord rec = wb.getNameRecord(i);
            if (rec != _definedNameRec) {
                if (rec.getNameText().equalsIgnoreCase(nameName) && sheetNumber == rec.getSheetNumber()){
                    String msg = "The "+(sheetNumber == 0 ? "workbook" : "sheet")+" already contains this name: " + nameName;
                    _definedNameRec.setNameText(nameName + "(2)");
                    throw new IllegalArgumentException(msg);
                }
            }
        }
        
        // Update our comment, if there is one
        if(_commentRec != null) {
           String oldName = _commentRec.getNameText();
           _commentRec.setNameText(nameName);
           _book.getWorkbook().updateNameCommentRecordCache(_commentRec);
        }
    }

    private static void validateName(String name){
        if(name.length() == 0)  throw new IllegalArgumentException("Name cannot be blank");

        char c = name.charAt(0);
        if(!(c == '_' || Character.isLetter(c)) || name.indexOf(' ') != -1) {
            throw new IllegalArgumentException("Invalid name: '"+name+"'; Names must begin with a letter or underscore and not contain spaces");
        }
    }

    /**
     * Returns the formula that the name is defined to refer to.
     *
     * @deprecated (Nov 2008) Misleading name. Use {@link #getRefersToFormula()} instead.
     */
    public String getReference() {
        return getRefersToFormula();
    }

    /**
     * Sets the formula that the name is defined to refer to.
     *
     * @deprecated (Nov 2008) Misleading name. Use {@link #setRefersToFormula(String)} instead.
     */
    public void setReference(String ref){
        setRefersToFormula(ref);
    }

    public void setRefersToFormula(String formulaText) {
        Ptg[] ptgs = HSSFFormulaParser.parse(formulaText, _book, FormulaType.NAMEDRANGE, getSheetIndex());
        _definedNameRec.setNameDefinition(ptgs);
    }

    public String getRefersToFormula() {
        if (_definedNameRec.isFunctionName()) {
            throw new IllegalStateException("Only applicable to named ranges");
        }
        Ptg[] ptgs = _definedNameRec.getNameDefinition();
        if (ptgs.length < 1) {
            // 'refersToFormula' has not been set yet
            return null;
        }
        return HSSFFormulaParser.toFormulaString(_book, ptgs);
    }


    /**
     * Sets the NameParsedFormula structure that specifies the formula for the 
     * defined name.
     * 
     * @param ptgs the sequence of {@link Ptg}s for the formula.
     */
    void setNameDefinition(Ptg[] ptgs) {
      _definedNameRec.setNameDefinition(ptgs);
    }


    public boolean isDeleted(){
        Ptg[] ptgs = _definedNameRec.getNameDefinition();
        return Ptg.doesFormulaReferToDeletedCell(ptgs);
    }

    /**
     * Checks if this name is a function name
     *
     * @return true if this name is a function name
     */
    public boolean isFunctionName() {
        return _definedNameRec.isFunctionName();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append(getClass().getName()).append(" [");
        sb.append(_definedNameRec.getNameText());
        sb.append("]");
        return sb.toString();
    }

    /**
     * Specifies if the defined name is a local name, and if so, which sheet it is on.
     *
     * @param index if greater than 0, the defined name is a local name and the value MUST be a 0-based index
     * to the collection of sheets as they appear in the workbook.
     * @throws IllegalArgumentException if the sheet index is invalid.
     */
    public void setSheetIndex(int index){
        int lastSheetIx = _book.getNumberOfSheets() - 1;
        if (index < -1 || index > lastSheetIx) {
            throw new IllegalArgumentException("Sheet index (" + index +") is out of range" +
                    (lastSheetIx == -1 ? "" : (" (0.." +    lastSheetIx + ")")));
        }

        _definedNameRec.setSheetNumber(index + 1);
    }

    /**
     * Returns the sheet index this name applies to.
     *
     * @return the sheet index this name applies to, -1 if this name applies to the entire workbook
     */
    public int getSheetIndex(){
        return _definedNameRec.getSheetNumber() - 1;
    }

    /**
     * Returns the comment the user provided when the name was created.
     *
     * @return the user comment for this named range
     */
    public String getComment() {
        if(_commentRec != null) {
           // Prefer the comment record if it has text in it
           if(_commentRec.getCommentText() != null &&
                 _commentRec.getCommentText().length() > 0) {
              return _commentRec.getCommentText();
           }
        }
        return _definedNameRec.getDescriptionText();
    }

    /**
     * Sets the comment the user provided when the name was created.
     *
     * @param comment the user comment for this named range
     */
    public void setComment(String comment){
        // Update the main record
        _definedNameRec.setDescriptionText(comment);
        // If we have a comment record too, update that as well
        if(_commentRec != null) {
           _commentRec.setCommentText(comment);
        }
    }

    /**
     * Indicates that the defined name refers to a user-defined function.
     * This attribute is used when there is an add-in or other code project associated with the file.
     *
     * @param value <code>true</code> indicates the name refers to a function.
     */
    public void setFunction(boolean value) {
        _definedNameRec.setFunction(value);
    }
    
    //2014/3/7 dennischen@zkoss.org, to allow import skip to import 
    public boolean isBuiltInName(){
    	return _definedNameRec.isBuiltInName();
    }
}
