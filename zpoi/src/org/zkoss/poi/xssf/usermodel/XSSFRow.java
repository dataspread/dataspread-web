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

import java.util.Iterator;
import java.util.TreeMap;

import org.zkoss.poi.ss.SpreadsheetVersion;
import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.poi.ss.usermodel.CellStyle;
import org.zkoss.poi.ss.usermodel.Row;
import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.poi.util.Internal;
import org.zkoss.poi.util.POILogFactory;
import org.zkoss.poi.util.POILogger;
import org.zkoss.poi.xssf.model.CalculationChain;
import org.zkoss.poi.xssf.model.StylesTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRow;
import org.zkoss.poi.ss.SpreadsheetVersion;
import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.poi.ss.usermodel.Row;
import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.poi.util.Internal;
import org.zkoss.poi.util.POILogFactory;
import org.zkoss.poi.util.POILogger;
import org.zkoss.poi.xssf.model.CalculationChain;

/**
 * High level representation of a row of a spreadsheet.
 */
public class XSSFRow implements Row, Comparable<XSSFRow> {
    private static final POILogger _logger = POILogFactory.getLogger(XSSFRow.class);

    /**
     * the xml bean containing all cell definitions for this row
     */
    private final CTRow _row;

    /**
     * Cells of this row keyed by their column indexes.
     * The TreeMap ensures that the cells are ordered by columnIndex in the ascending order.
     */
    private final TreeMap<Integer, XSSFCell> _cells;

    /**
     * the parent sheet
     */
    private final XSSFSheet _sheet;

    /**
     * Construct a XSSFRow.
     *
     * @param row the xml bean containing all cell definitions for this row.
     * @param sheet the parent sheet.
     */
    @SuppressWarnings("deprecation") //YK: getXYZArray() array accessors are deprecated in xmlbeans with JDK 1.5 support
    protected XSSFRow(CTRow row, XSSFSheet sheet) {
        _row = row;
        _sheet = sheet;
        _cells = new TreeMap<Integer, XSSFCell>();
        for (CTCell c : row.getCArray()) {
            XSSFCell cell = new XSSFCell(this, c);
            _cells.put(cell.getColumnIndex(), cell);
            sheet.onReadCell(cell);
        }
    }

    /**
     * Returns the XSSFSheet this row belongs to
     *
     * @return the XSSFSheet that owns this row
     */
    public XSSFSheet getSheet() {
        return this._sheet;
    }

    /**
     * Cell iterator over the physically defined cells:
     * <blockquote><pre>
     * for (Iterator<Cell> it = row.cellIterator(); it.hasNext(); ) {
     *     Cell cell = it.next();
     *     ...
     * }
     * </pre></blockquote>
     *
     * @return an iterator over cells in this row.
     */
    public Iterator<Cell> cellIterator() {
        return (Iterator<Cell>)(Iterator<? extends Cell>)_cells.values().iterator();
    }

    /**
     * Alias for {@link #cellIterator()} to allow  foreach loops:
     * <blockquote><pre>
     * for(Cell cell : row){
     *     ...
     * }
     * </pre></blockquote>
     *
     * @return an iterator over cells in this row.
     */
    public Iterator<Cell> iterator() {
    	return cellIterator();
    }

    /**
     * Compares two <code>XSSFRow</code> objects.  Two rows are equal if they belong to the same worksheet and
     * their row indexes are equal.
     *
     * @param   row   the <code>XSSFRow</code> to be compared.
     * @return	the value <code>0</code> if the row number of this <code>XSSFRow</code> is
     * 		equal to the row number of the argument <code>XSSFRow</code>; a value less than
     * 		<code>0</code> if the row number of this this <code>XSSFRow</code> is numerically less
     * 		than the row number of the argument <code>XSSFRow</code>; and a value greater
     * 		than <code>0</code> if the row number of this this <code>XSSFRow</code> is numerically
     * 		 greater than the row number of the argument <code>XSSFRow</code>.
     * @throws IllegalArgumentException if the argument row belongs to a different worksheet
     */
    public int compareTo(XSSFRow row) {
        int thisVal = this.getRowNum();
        if(row.getSheet() != getSheet()) throw new IllegalArgumentException("The compared rows must belong to the same XSSFSheet");

        int anotherVal = row.getRowNum();
        return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
    }

    /**
     * Use this to create new cells within the row and return it.
     * <p>
     * The cell that is returned is a {@link Cell#CELL_TYPE_BLANK}. The type can be changed
     * either through calling <code>setCellValue</code> or <code>setCellType</code>.
     * </p>
     * @param columnIndex - the column number this cell represents
     * @return Cell a high level representation of the created cell.
     * @throws IllegalArgumentException if columnIndex < 0 or greater than 16384,
     *   the maximum number of columns supported by the SpreadsheetML format (.xlsx)
     */
    public XSSFCell createCell(int columnIndex) {
    	return createCell(columnIndex, Cell.CELL_TYPE_BLANK);
    }

    /**
     * Use this to create new cells within the row and return it.
     *
     * @param columnIndex - the column number this cell represents
     * @param type - the cell's data type
     * @return XSSFCell a high level representation of the created cell.
     * @throws IllegalArgumentException if the specified cell type is invalid, columnIndex < 0
     *   or greater than 16384, the maximum number of columns supported by the SpreadsheetML format (.xlsx)
     * @see Cell#CELL_TYPE_BLANK
     * @see Cell#CELL_TYPE_BOOLEAN
     * @see Cell#CELL_TYPE_ERROR
     * @see Cell#CELL_TYPE_FORMULA
     * @see Cell#CELL_TYPE_NUMERIC
     * @see Cell#CELL_TYPE_STRING
     */
    public XSSFCell createCell(int columnIndex, int type) {
        CTCell ctCell;
        XSSFCell prev = _cells.get(columnIndex);
        if(prev != null){
            ctCell = prev.getCTCell();
            ctCell.set(CTCell.Factory.newInstance());
        } else {
            ctCell = _row.addNewC();
        }
        XSSFCell xcell = new XSSFCell(this, ctCell);
        xcell.setCellNum(columnIndex);
        if (type != Cell.CELL_TYPE_BLANK) {
        	xcell.setCellType(type);
        }
        _cells.put(columnIndex, xcell);
        return xcell;
    }

    /**
     * Returns the cell at the given (0 based) index,
     *  with the {@link org.zkoss.poi.ss.usermodel.Row.MissingCellPolicy} from the parent Workbook.
     *
     * @return the cell at the given (0 based) index
     */
    public XSSFCell getCell(int cellnum) {
    	return getCell(cellnum, _sheet.getWorkbook().getMissingCellPolicy());
    }

    /**
     * Returns the cell at the given (0 based) index, with the specified {@link org.zkoss.poi.ss.usermodel.Row.MissingCellPolicy}
     *
     * @return the cell at the given (0 based) index
     * @throws IllegalArgumentException if cellnum < 0 or the specified MissingCellPolicy is invalid
     * @see Row#RETURN_NULL_AND_BLANK
     * @see Row#RETURN_BLANK_AS_NULL
     * @see Row#CREATE_NULL_AS_BLANK
     */
    public XSSFCell getCell(int cellnum, MissingCellPolicy policy) {
    	if(cellnum < 0) throw new IllegalArgumentException("Cell index must be >= 0");

        XSSFCell cell = (XSSFCell)_cells.get(cellnum);
    	if(policy == RETURN_NULL_AND_BLANK) {
    		return cell;
    	}
    	if(policy == RETURN_BLANK_AS_NULL) {
    		if(cell == null) return cell;
    		if(cell.getCellType() == Cell.CELL_TYPE_BLANK) {
    			return null;
    		}
    		return cell;
    	}
    	if(policy == CREATE_NULL_AS_BLANK) {
    		if(cell == null) {
    			return createCell((short)cellnum, Cell.CELL_TYPE_BLANK);
    		}
    		return cell;
    	}
    	throw new IllegalArgumentException("Illegal policy " + policy + " (" + policy.id + ")");
    }

    /**
     * Get the number of the first cell contained in this row.
     *
     * @return short representing the first logical cell in the row,
     *  or -1 if the row does not contain any cells.
     */
    public short getFirstCellNum() {
    	return (short)(_cells.size() == 0 ? -1 : _cells.firstKey());
    }

    /**
     * Gets the index of the last cell contained in this row <b>PLUS ONE</b>. The result also
     * happens to be the 1-based column number of the last cell.  This value can be used as a
     * standard upper bound when iterating over cells:
     * <pre>
     * short minColIx = row.getFirstCellNum();
     * short maxColIx = row.getLastCellNum();
     * for(short colIx=minColIx; colIx&lt;maxColIx; colIx++) {
     *   XSSFCell cell = row.getCell(colIx);
     *   if(cell == null) {
     *     continue;
     *   }
     *   //... do something with cell
     * }
     * </pre>
     *
     * @return short representing the last logical cell in the row <b>PLUS ONE</b>,
     *   or -1 if the row does not contain any cells.
     */
    public short getLastCellNum() {
    	return (short)(_cells.size() == 0 ? -1 : (_cells.lastKey() + 1));
    }

    /**
     * Get the row's height measured in twips (1/20th of a point). If the height is not set, the default worksheet value is returned,
     * See {@link org.zkoss.poi.xssf.usermodel.XSSFSheet#getDefaultRowHeightInPoints()}
     *
     * @return row height measured in twips (1/20th of a point)
     */
    public short getHeight() {
        return (short)(getHeightInPoints()*20);
    }

    /**
     * Returns row height measured in point size. If the height is not set, the default worksheet value is returned,
     * See {@link org.zkoss.poi.xssf.usermodel.XSSFSheet#getDefaultRowHeightInPoints()}
     *
     * @return row height measured in point size
     * @see org.zkoss.poi.xssf.usermodel.XSSFSheet#getDefaultRowHeightInPoints()
     */
    public float getHeightInPoints() {
        if (this._row.isSetHt()) {
            return (float) this._row.getHt();
        }
        return _sheet.getDefaultRowHeightInPoints();
    }

    /**
     *  Set the height in "twips" or  1/20th of a point.
     *
     * @param height the height in "twips" or  1/20th of a point. <code>-1</code>  resets to the default height
     */
    public void setHeight(short height) {
        if (height == -1) {
            if (_row.isSetHt()) _row.unsetHt();
            if (_row.isSetCustomHeight()) _row.unsetCustomHeight();
        } else {
            _row.setHt((double) height / 20);
        }
    }

    /**
     * Set the row's height in points.
     *
     * @param height the height in points. <code>-1</code>  resets to the default height
     */
    public void setHeightInPoints(float height) {
	    setHeight((short)(height == -1 ? -1 : (height*20)));
    }

    /**
     * Gets the number of defined cells (NOT number of cells in the actual row!).
     * That is to say if only columns 0,4,5 have values then there would be 3.
     *
     * @return int representing the number of defined cells in the row.
     */
    public int getPhysicalNumberOfCells() {
    	return _cells.size();
    }

    /**
     * Get row number this row represents
     *
     * @return the row number (0 based)
     */
    public int getRowNum() {
        return (int) (_row.getR() - 1);
    }

    /**
     * Set the row number of this row.
     *
     * @param rowIndex  the row number (0-based)
     * @throws IllegalArgumentException if rowNum < 0 or greater than 1048575
     */
    public void setRowNum(int rowIndex) {
        int maxrow = SpreadsheetVersion.EXCEL2007.getLastRowIndex();
        if (rowIndex < 0 || rowIndex > maxrow) {
            throw new IllegalArgumentException("Invalid row number (" + rowIndex
                    + ") outside allowable range (0.." + maxrow + ")");
        }
        _row.setR(rowIndex + 1);
    }

    /**
     * Get whether or not to display this row with 0 height
     *
     * @return - height is zero or not.
     */
    public boolean getZeroHeight() {
    	return this._row.getHidden();
    }

    /**
     * Set whether or not to display this row with 0 height
     *
     * @param height  height is zero or not.
     */
    public void setZeroHeight(boolean height) {
    	this._row.setHidden(height);

    }

    /**
     * Is this row formatted? Most aren't, but some rows
     *  do have whole-row styles. For those that do, you
     *  can get the formatting from {@link #getRowStyle()}
     */
    public boolean isFormatted() {
        return _row.isSetS();
    }
    /**
     * Returns the whole-row cell style. Most rows won't
     *  have one of these, so will return null. Call
     *  {@link #isFormatted()} to check first.
     */
    public XSSFCellStyle getRowStyle() {
       if(!isFormatted()) return null;
       
       StylesTable stylesSource = getSheet().getWorkbook().getStylesSource();
       if(stylesSource.getNumCellStyles() > 0) {
           return stylesSource.getStyleAt((int)_row.getS());
       } else {
          return null;
       }
    }
    
    /**
     * Applies a whole-row cell styling to the row.
     * If the value is null then the style information is removed,
     *  causing the cell to used the default workbook style.
     */
    public void setRowStyle(CellStyle style) {
        if(style == null) {
           if(_row.isSetS()) {
              _row.unsetS();
              _row.unsetCustomFormat();
           }
        } else {
            StylesTable styleSource = getSheet().getWorkbook().getStylesSource();
            
            XSSFCellStyle xStyle = (XSSFCellStyle)style;
            xStyle.verifyBelongsToStylesSource(styleSource);

            long idx = styleSource.putStyle(xStyle);
            if (idx > 0) { //ZSS-1016
	            _row.setS(idx);
	            _row.setCustomFormat(true);
            }
        }
    }
    
    /**
     * Remove the Cell from this row.
     *
     * @param cell the cell to remove
     */
    public void removeCell(Cell cell) {
        if (cell.getRow() != this) {
            throw new IllegalArgumentException("Specified cell does not belong to this row");
        }

        XSSFCell xcell = (XSSFCell)cell;
        if(xcell.isPartOfArrayFormulaGroup()) {
            xcell.notifyArrayFormulaChanging();
        }
        if(cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
           _sheet.getWorkbook().onDeleteFormula(xcell);
        }
        _cells.remove(cell.getColumnIndex());
    }

    /**
     * Returns the underlying CTRow xml bean containing all cell definitions in this row
     *
     * @return the underlying CTRow xml bean
     */
    @Internal
    public CTRow getCTRow(){
    	return _row;
    }

    /**
     * Fired when the document is written to an output stream.
     *
     * @see org.zkoss.poi.xssf.usermodel.XSSFSheet#write(java.io.OutputStream) ()
     */
    protected void onDocumentWrite(){
        // check if cells in the CTRow are ordered
        boolean isOrdered = true;
        if(_row.sizeOfCArray() != _cells.size()) isOrdered = false;
        else {
            int i = 0;
            for (XSSFCell cell : _cells.values()) {
                CTCell c1 = cell.getCTCell();
                CTCell c2 = _row.getCArray(i++); 

                String r1 = c1.getR();
                String r2 = c2.getR();
                if (!(r1==null ? r2==null : r1.equals(r2))){
                    isOrdered = false;
                    break;
                }
            }
        }

        if(!isOrdered){
            CTCell[] cArray = new CTCell[_cells.size()];
            int i = 0;
            for (XSSFCell c : _cells.values()) {
                cArray[i++] = c.getCTCell();
            }
            _row.setCArray(cArray);
            
            // 20130820, paowang@potix.com, ZSS-179: after _row.setCArray(), _row creates new instances of CTCell 
            // But some XSSFCell still keep old instances of CTCell, this causes problems when accessing cell's data
            // So, re-build the cells mapping. 
			_cells.clear();
			for(CTCell c : _row.getCList()) {
				XSSFCell cell = new XSSFCell(this, c);
				_cells.put(cell.getColumnIndex(), cell);
			}
        }
    }

    /**
     * @return formatted xml representation of this row
     */
    @Override
    public String toString(){
        return _row.toString();
    }

    /**
     * update cell references when shifting rows
     *
     * @param n the number of rows to move
     */
    protected void shift(int n) {
        int rownum = getRowNum() + n;
        CalculationChain calcChain = _sheet.getWorkbook().getCalculationChain();
        int sheetId = (int)_sheet.sheet.getSheetId();
        String msg = "Row[rownum="+getRowNum()+"] contains cell(s) included in a multi-cell array formula. " +
                "You cannot change part of an array.";
        for(Cell c : this){
            XSSFCell cell = (XSSFCell)c;
            if(cell.isPartOfArrayFormulaGroup()){
                cell.notifyArrayFormulaChanging(msg);
            }

            //remove the reference in the calculation chain
            if(calcChain != null) calcChain.removeItem(sheetId, cell.getReference());

            CTCell ctCell = cell.getCTCell();
            String r = new CellReference(rownum, cell.getColumnIndex()).formatAsString();
            ctCell.setR(r);
        }
        setRowNum(rownum);
    }
    
    //20100915, henrichen@zkoss.org: remove all cells
    public void removeAllCells() {
    	_cells.clear();
    }
    //20100915, henrichen@zkoss.org: return cells TreeMap
    public TreeMap<Integer, XSSFCell> getCells() {
    	return _cells;
    }

    //20120103, henrichen@zkoss.org: return whether the row height is set by the user, compare to wrap text setting
	@Override
	public boolean isCustomHeight() {
		return _row.getCustomHeight();
	}

	@Override
	public void setCustomHeight(boolean custom) {
		_row.setCustomHeight(custom);
	}
}
