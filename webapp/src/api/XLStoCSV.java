package api;
import java.util.*;
import com.opencsv.CSVWriter;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.*;
import org.apache.poi.ss.formula.ptg.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.*;

public class XLStoCSV {
    private int[] sheetOffsets;
    private int[] sheetLengths;
    private final Workbook workbook;
    private final FormulaParsingWorkbook parsingWorkbook;
    private final int longestRow;
    public XLStoCSV(File excelFile) throws IOException {
        workbook = WorkbookFactory.create(excelFile);
        String fileExt = getFileExt(excelFile.getName());
        if (fileExt.equals("xls")) {
            parsingWorkbook = HSSFEvaluationWorkbook.create((HSSFWorkbook) workbook);
        } else if (fileExt.equals("xlsx")){
            parsingWorkbook = XSSFEvaluationWorkbook.create((XSSFWorkbook) workbook);
        } else throw new IllegalArgumentException("File must have either \".xls\" or \".xlsx\" as its extension");
        longestRow = setUp();
    }

    /**
     * @param csv: CSV file to be written to
     * Converts excel file to csv file.
     */
    public void fullConvert(File csv) {
        try {
            CSVWriter out = new CSVWriter(new FileWriter(csv));
            assert workbook != null;
            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);
                for (int r = 0; r < sheetLengths[s]; r++) {
                    Row row = sheet.getRow(r);
                    String[] line = new String[longestRow + 1];
                    if (row != null) {
                        for (int c = 0; c <= longestRow; c++) {
                            Cell cell = row.getCell(c,
                                    Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                            if (cell != null) {
                                line[c] = getCellTextWithOffset(cell);
                            }
                        }
                    }
                    out.writeNext(line);
                }
            }
            out.close();
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return longest row in the Excel workbook
     * fills in sheetOffsets and sheetLengths with appropriate values.
     */
    private int setUp() {
        int longestRow = 0;
        int offset = 0;
        int[] lastCellRefs = new int[workbook.getNumberOfSheets() + 1];
        sheetOffsets = new int[workbook.getNumberOfSheets() + 1];
        sheetLengths = new int[workbook.getNumberOfSheets()];
        for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
            Sheet sheet = workbook.getSheetAt(s);
            sheetOffsets[s] = offset;
            sheetLengths[s] = sheet.getLastRowNum() + 1;
            offset += sheet.getLastRowNum() + 1;
            for (int r = 0, rEnd = sheet.getLastRowNum(); r <= rEnd; r++) {
                Row row = sheet.getRow(r);
                if (row != null) {
                    longestRow = Math.max(longestRow, row.getLastCellNum());
                    for (int c = 0; c < row.getLastCellNum(); c++) {
                        Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        if (cell != null && cell.getCellType().equals(CellType.FORMULA)) {
                            try {
                                Ptg[] tokens = FormulaParser.parse(
                                        cell.getCellFormula(),
                                        parsingWorkbook,
                                        FormulaType.CELL,
                                        workbook.getSheetIndex(cell.getSheet()),
                                        cell.getRowIndex());
                                for (Ptg token : tokens) {
                                    int lastCellRef = -1;
                                    int sheetNum = s;
                                    if (token instanceof Ref3DPtg || token instanceof Area3DPtg) {
                                        WorkbookDependentFormula ptg3D = (WorkbookDependentFormula) token;
                                        String refString = ptg3D.toFormulaString((FormulaRenderingWorkbook) parsingWorkbook);
                                        String sheetName = refString.substring(0, refString.indexOf('!'));
                                        sheetNum = workbook.getSheetIndex(sheetName);
                                    } else if (token instanceof Pxg3D) {
                                        Pxg3D pxg3D = (Pxg3D) token;
                                        String refString = pxg3D.toFormulaString();
                                        String sheetName = refString.substring(0, refString.indexOf('!'));
                                        sheetNum = workbook.getSheetIndex(sheetName);
                                    }
                                    if (token instanceof AreaPtgBase) {
                                        AreaPtgBase areaPtgBase = (AreaPtgBase) token;
                                        lastCellRef = areaPtgBase.getLastRow() + 1;
                                    } else if (token instanceof Ref3DPtg || token instanceof  Ref3DPxg || token instanceof RefPtg) {
                                        RefPtgBase refPtgBase = (RefPtgBase) token;
                                        lastCellRef = refPtgBase.getRow() + 1;
                                    }
                                    if (lastCellRef >= 0) {
                                        lastCellRefs[sheetNum] = Math.max(lastCellRefs[sheetNum], lastCellRef);
                                    }
                                }
                            } catch(Exception ignored) {}
                        }
                    }
                }
            }
        }
        sheetOffsets[sheetOffsets.length - 1] = offset;
        for (int i = 0; i < lastCellRefs.length - 1; i++) {
            if (lastCellRefs[i] > sheetLengths[i]) {
                int adjust = lastCellRefs[i] - sheetLengths[i];
                sheetLengths[i] = lastCellRefs[i];
                for (int j = i + 1; j < sheetOffsets.length; j++) {
                    sheetOffsets[j] += adjust;
                }
            }
            // For debugging
            // System.out.println("Sheet added to Excel to CSV converter:\t | Offset: " + sheetOffsets[i] +
            //        "\t | Sheet length: " + sheetLengths[i] + "\t | Sheet name: " + workbook.getSheetName(i));
        }
        return longestRow;
    }

    /**
     * @param cell: Cell to get formula from
     * @return the string representaion of the cell with translated cell coordinates
     */
    private String getFormulaWithOffset(Cell cell) {
        assert(cell.getCellType().equals(CellType.FORMULA));
        try {
            Ptg[] tokens = FormulaParser.parse(
                    cell.getCellFormula(),
                    parsingWorkbook,
                    FormulaType.CELL,
                    workbook.getSheetIndex(cell.getSheet()),
                    cell.getRowIndex());
            for (int i = 0; i < tokens.length; i++) {
                Ptg token = tokens[i];
                int offset = -1;
                if (token instanceof Ref3DPtg || token instanceof Area3DPtg) {
                    WorkbookDependentFormula ptg3D = (WorkbookDependentFormula) token;
                    String refString = ptg3D.toFormulaString((FormulaRenderingWorkbook) parsingWorkbook);
                    String sheetName = refString.substring(0, refString.indexOf('!'));
                    offset = sheetOffsets[workbook.getSheetIndex(sheetName)];
                } else if (token instanceof Pxg3D) {
                    Pxg3D pxg3D = (Pxg3D) token;
                    String refString = pxg3D.toFormulaString();
                    String sheetName = refString.substring(0, refString.indexOf('!'));
                    offset = sheetOffsets[workbook.getSheetIndex(sheetName)];
                } else if (token instanceof RefPtg || token instanceof AreaPtg) {
                    offset = sheetOffsets[workbook.getSheetIndex(cell.getSheet().getSheetName())];
                }

                if (token instanceof Area3DPxg || token instanceof Area3DPtg || token instanceof AreaPtg) {
                    AreaPtgBase areaPtgBase = (AreaPtgBase) token;
                    tokens[i] = new AreaPtg(areaPtgBase.getFirstRow() + offset, areaPtgBase.getLastRow() + offset,
                            areaPtgBase.getFirstColumn(), areaPtgBase.getLastColumn(),
                            areaPtgBase.isFirstRowRelative(), areaPtgBase.isLastRowRelative(),
                            areaPtgBase.isFirstColRelative(), areaPtgBase.isLastColRelative());
                } else if (token instanceof RefPtg || token instanceof Ref3DPxg || token instanceof Ref3DPtg) {
                    RefPtgBase refPtgBase = (RefPtgBase) token;
                    tokens[i] = new RefPtg(refPtgBase.getRow() + offset, refPtgBase.getColumn(),
                            refPtgBase.isRowRelative(), refPtgBase.isColRelative());
                }
            }
            return "=" + FormulaRenderer.toFormulaString((FormulaRenderingWorkbook) parsingWorkbook, tokens);
        } catch(Exception e) {
            return "Cell formula is either malformed or uses too many nested functions.";
        }

    }

    /**
     * @param fileName: File to get extension from.
     * @return file extension of fileName.
     */
    private String getFileExt(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i + 1);
        } else return "";
    }

    /**
     * @param cell: Cell to get text from.
     * @return A string representation of the cell. Replaces cell coordinates based on sheet offsets.
     */
    private String getCellTextWithOffset(org.apache.poi.ss.usermodel.Cell cell) {
        try {
            String value;
            org.apache.poi.ss.usermodel.CellType cellType = cell.getCellType();
            switch (cellType) {
                case NUMERIC:
                    value = getNumericCellValue(cell);
                    break;
                case STRING:
                    value = cell.getStringCellValue();
                    break;
                case FORMULA:
                    value = getFormulaWithOffset(cell);
                    break;
                case BOOLEAN:
                    value = cell.getBooleanCellValue() ? "true" : "false";
                    break;
                case BLANK:
                case ERROR:
                default:
                    value = "";
                    break;
            }
            return value;
        } catch (Exception e) {
            return "Error in reading cell: " + e.getMessage();
        }
    }
    /**
     * @param cell A cell from the Workbook
     * @return A String representation of the numeric cell
     */
    static private String getNumericCellValue(org.apache.poi.ss.usermodel.Cell cell) {
        double numericValue = cell.getNumericCellValue();
        DataFormatter dataFormatter = new DataFormatter();
        try {
            CellStyle cellStyle = cell.getCellStyle();
            return dataFormatter.formatRawCellContents(numericValue
                    , cellStyle.getDataFormat()
                    , cellStyle.getDataFormatString()
            );
        } catch (Exception e) {
            return Double.toString(numericValue);
        }
    }
}
