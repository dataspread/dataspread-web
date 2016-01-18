/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.zkoss.poi.ss.usermodel.charts;

import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.poi.ss.usermodel.CellValue;
import org.zkoss.poi.ss.usermodel.FormulaEvaluator;
import org.zkoss.poi.ss.usermodel.Row;
import org.zkoss.poi.ss.usermodel.Sheet;
import org.zkoss.poi.ss.util.CellRangeAddress;
import org.zkoss.poi.util.Beta;
import org.zkoss.poi.util.POILogFactory;
import org.zkoss.poi.util.POILogger;

/**
 * Class {@code DataSources} is a factory for {@link ChartDataSource} instances.
 *
 * @author Roman Kashitsyn
 */
@Beta
public class DataSources {
	// 20130703, paowang@potix.com: (ZSS-349) add logger
	private final static POILogger logger = POILogFactory.getLogger(DataSources.class);
	
    private DataSources() {
    }

    public static <T> ChartDataSource<T> fromArray(T[] elements) {
        return new ArrayDataSource<T>(elements);
    }

    public static ChartDataSource<Number> fromNumericCellRange(Sheet sheet, CellRangeAddress cellRangeAddress) {
        return new AbstractCellRangeDataSource<Number>(sheet, cellRangeAddress) {
            public Number getPointAt(int index) {
                CellValue cellValue = getCellValueAt(index);
                if (cellValue != null && cellValue.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                    return Double.valueOf(cellValue.getNumberValue());
				} else if(cellValue != null && cellValue.getCellType() == Cell.CELL_TYPE_ERROR) { // 20130703, paowang@potix.com: (ZSS-349) in Excel, the error cell will be converted to zero and still can be drawing
					return Double.valueOf(0.0);
				} else {
                    return null;
                }
            }

            public boolean isNumeric() {
                return true;
            }
        };
    }

    public static ChartDataSource<String> fromStringCellRange(Sheet sheet, CellRangeAddress cellRangeAddress) {
        return new AbstractCellRangeDataSource<String>(sheet, cellRangeAddress) {
            public String getPointAt(int index) {
                CellValue cellValue = getCellValueAt(index);
                if (cellValue != null && cellValue.getCellType() == Cell.CELL_TYPE_STRING) {
                    return cellValue.getStringValue();
				} else if(cellValue != null && cellValue.getCellType() == Cell.CELL_TYPE_ERROR) { // 20130703, paowang@potix.com: (ZSS-349) in Excel, the error cell will be converted to error string and still can be drawing
					return cellValue.formatAsString();
				} else {
                    return null;
                }
            }

            public boolean isNumeric() {
                return false;
            }
        };
    }

    public static class ArrayDataSource<T> implements ChartDataSource<T> { //20111014, henrichen@zkoss.org: public

        private final T[] elements;

        public ArrayDataSource(T[] elements) {
            this.elements = elements;
        }

        public int getPointCount() {
            return elements.length;
        }

        public T getPointAt(int index) {
            return elements[index];
        }

        public boolean isReference() {
            return false;
        }

        public boolean isNumeric() {
            Class<?> arrayComponentType = elements.getClass().getComponentType();
            return (Number.class.isAssignableFrom(arrayComponentType));
        }

        public String getFormulaString() {
            throw new UnsupportedOperationException("Literal data source can not be expressed by reference.");
        }
        
        //20111007, henrichen@zkoss.org: sheet name change will affect reference
		@Override
		public void renameSheet(String oldname, String newname) {
			//do nothing, it is not a reference case
		}
    }

    private abstract static class AbstractCellRangeDataSource<T> implements ChartDataSource<T> {
        private final Sheet sheet;
        private final CellRangeAddress cellRangeAddress;
        private final int numOfCells;
        private FormulaEvaluator evaluator;

        protected AbstractCellRangeDataSource(Sheet sheet, CellRangeAddress cellRangeAddress) {
            this.sheet = sheet;
            // Make copy since CellRangeAddress is mutable.
            this.cellRangeAddress = cellRangeAddress.copy();
            this.numOfCells = this.cellRangeAddress.getNumberOfCells();
            this.evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        }

        public int getPointCount() {
            return numOfCells;
        }

        public boolean isReference() {
            return true;
        }

        public String getFormulaString() {
            return cellRangeAddress.formatAsString(sheet.getSheetName(), true);
        }

        protected CellValue getCellValueAt(int index) {
            if (index < 0 || index >= numOfCells) {
                throw new IndexOutOfBoundsException("Index must be between 0 and " +
                        (numOfCells - 1) + " (inclusive), given: " + index);
            }
            int firstRow = cellRangeAddress.getFirstRow();
            int firstCol = cellRangeAddress.getFirstColumn();
            int lastCol = cellRangeAddress.getLastColumn();
            int width = lastCol - firstCol + 1;
            int rowIndex = firstRow + index / width;
            int cellIndex = firstCol + index % width;
            Row row = sheet.getRow(rowIndex);
            // 20130703, paowang@potix.com: (ZSS-349) handle fail to evaluate case when cell is a formula
            if(row == null) {
            	return null;
            }
            Cell cell = row.getCell(cellIndex);
            if(cell == null) {
            	return null;
            }
			try {
				return evaluator.evaluate(cell);
			} catch(RuntimeException e) {
				logger.log(POILogger.WARN, e);
				return CellValue.getError(cell.getErrorCellValue());
			}
        }
        
        //20111007, henrichen@zkoss.org: sheet name change will affect reference
		@Override
		public void renameSheet(String oldname, String newname) {
			//Do nothing, sheet is there
		}
    }
    
    //20111007, henrichen@zkoss.org: CellRange referenced ChartTextSource
    private static class CellRangeTextSource implements ChartTextSource {
        private final Sheet sheet;
        private final CellRangeAddress cellRangeAddress;
        private final int numOfCells;
        private FormulaEvaluator evaluator;

        protected CellRangeTextSource(Sheet sheet, CellRangeAddress cellRangeAddress) {
            this.sheet = sheet;
            // Make copy since CellRangeAddress is mutable.
            this.cellRangeAddress = cellRangeAddress.copy();
            this.numOfCells = this.cellRangeAddress.getNumberOfCells();
            this.evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
        }

        public boolean isReference() {
            return true;
        }

        public String getFormulaString() {
            return cellRangeAddress.formatAsString(sheet.getSheetName(), true);
        }

        protected CellValue getCellValueAt(int index) {
            if (index < 0 || index >= numOfCells) {
                throw new IndexOutOfBoundsException("Index must be between 0 and " +
                        (numOfCells - 1) + " (inclusive), given: " + index);
            }
            int firstRow = cellRangeAddress.getFirstRow();
            int firstCol = cellRangeAddress.getFirstColumn();
            int lastCol = cellRangeAddress.getLastColumn();
            int width = lastCol - firstCol + 1;
            int rowIndex = firstRow + index / width;
            int cellIndex = firstCol + index % width;
            Row row = sheet.getRow(rowIndex);
            // 20130703, paowang@potix.com: (ZSS-349) handle fail to evaluate case when cell is a formula
            Cell cell = row.getCell(cellIndex);
            try {
				return (row == null) ? null : evaluator.evaluate(cell);
			} catch(RuntimeException e) {
				logger.log(POILogger.WARN, e);
				return CellValue.getError(cell.getErrorCellValue()); 
			}
        }
        
		@Override
		public void renameSheet(String oldname, String newname) {
			//Do nothing, sheet is there
		}

		@Override
		public String getTextString() {
			StringBuilder sb = new StringBuilder();
			for(int j = 0; j < numOfCells; ++j) {
	            CellValue cellValue = getCellValueAt(j);
	            if (cellValue != null) {
	                sb.append(cellValue.getStringValue());
	            }
			}
			return sb.length() == 0 ? null : sb.toString();
		}
    }

    //20111025, henrichen@zkoss: Literal ChartTextSource
    private static class LiteralTextSource implements ChartTextSource {

        private String element;

        public LiteralTextSource(String element) {
            this.element = element;
        }

        public boolean isReference() {
            return false;
        }

        public boolean isNumeric() {
        	return false;
        }

        public String getFormulaString() {
            throw new UnsupportedOperationException("Literal data source can not be expressed by reference.");
        }
        
		@Override
		public void renameSheet(String oldname, String newname) {
			//do nothing, it is not a reference case
		}

		@Override
		public String getTextString() {
			return element;
		}
    }
    
    //20111025, henrichen@zkoss: Literal ChartTextSource
    public static ChartTextSource fromString(String element) {
        return new LiteralTextSource(element);
    }

    //20111007, henrichen@zkoss.org: CellRange referenced ChartTextSource
    public static ChartTextSource fromCellRange(Sheet sheet, CellRangeAddress cellRangeAddress) {
        return new CellRangeTextSource(sheet, cellRangeAddress);
    }
}
