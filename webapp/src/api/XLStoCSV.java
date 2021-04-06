package api;
import java.util.*;
import com.opencsv.CSVWriter;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.*;
import org.apache.poi.ss.formula.ptg.*;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import java.io.*;

public class XLStoCSV {
    private final Map<Integer, Integer> sheetOffsets;
    private final Workbook workbook;
    private final FormulaParsingWorkbook parsingWorkbook;
    public XLStoCSV(File excelFile) throws IOException {
        workbook = WorkbookFactory.create(excelFile);
        String fileExt = getFileExt(excelFile.getName());
        if (fileExt.equals("xls")) {
            parsingWorkbook = HSSFEvaluationWorkbook.create((HSSFWorkbook) workbook);
        } else if (fileExt.equals("xlsx")){
            parsingWorkbook = XSSFEvaluationWorkbook.create((XSSFWorkbook) workbook);
        } else throw new IllegalArgumentException("File must have either \".xls\" or \".xlsx\" as its extension");
        sheetOffsets = new HashMap<Integer, Integer>();
    }

    /**
     *
     * @param csv
     *
     * Converts excel file to csv file
     */
    public void fullConvert(File csv) {
        try {
            CSVWriter out = new CSVWriter(new FileWriter(csv));
            assert workbook != null;
            int longestRow = setUp();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (int r = 0, rEnd = sheet.getLastRowNum(); r <= rEnd; r++) {
                    Row row = sheet.getRow(r);
                    String[] line = new String[longestRow + 1];
                    if (row != null) {
                        for (int c = 0; c <= longestRow; c++) {
                            org.apache.poi.ss.usermodel.Cell cell = row.getCell(c,
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
     *
     * @param excel Excel file to be read from
     * @param csv CSV file to be written to
     *
     * Converts Excel .xls or .xlsx file to a CSV file
     */
    static public void basicFullConvert(File excel, File csv) {
        try {
            CSVWriter out = new CSVWriter(new FileWriter(csv));
            Workbook wb = WorkbookFactory.create(excel);
            assert wb != null;
            int longestRow = getLongestRowInWB(wb);

            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                //for (int i = 0; i < 1; i++) { // if converting all sheets, replace this line with line above
                Sheet sheet = wb.getSheetAt(i);
                for (int r = 0, rEnd = sheet.getLastRowNum(); r <= rEnd; r++) {
                    Row row = sheet.getRow(r);
                    String[] line = new String[longestRow + 1];
                    if (row != null) {
                        for (int c = 0; c <= longestRow; c++) {
                            org.apache.poi.ss.usermodel.Cell cell = row.getCell(c,
                                    Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                            if (cell != null) {
                                line[c] = getCellText(cell);
                            }
                        }
                    }
                    out.writeNext(line);
                }
            }
            out.close();
            wb.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return longest row in the Excel wb
     *
     * adds sheet number to row offset maps to sheetOffsets
     */
    private int setUp() {
        int longestRow = 0;
        int offset = 0;
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            sheetOffsets.put(i, offset);
            System.out.println("Added sheet " + i + ": " + "\t(Offset: " + offset + ")\t" + sheet.getSheetName());
            offset += sheet.getLastRowNum() + 1;
            for (int r = 0, rEnd = sheet.getLastRowNum(); r <= rEnd; r++) {
                Row row = sheet.getRow(r);
                if (row != null) {
                    //offset++;
                    if (row.getLastCellNum() > longestRow) {
                        longestRow = row.getLastCellNum();
                    }
                }
            }
        }
        return longestRow;
    }

    /**
     *
     * @param cell
     * @return the string representaion of the cell with translated cell coordinates
     */
    private String getFormulaWithOffset(org.apache.poi.ss.usermodel.Cell cell) {
        assert(cell.getCellType().equals(CellType.FORMULA));
        Ptg[] tokens = FormulaParser.parse(
                cell.getCellFormula(),
                parsingWorkbook,
                FormulaType.CELL,
                workbook.getSheetIndex(cell.getSheet()),
                cell.getRowIndex());
        for (int i = 0; i < tokens.length; i++) {
            Ptg token = tokens[i];
            if (token instanceof Area3DPtg) {
                Area3DPtg token3D = (Area3DPtg) token;
                // TODO: fix ptg sheet numbering
                int sheetIndex = token3D.getExternSheetIndex();
                String refString = token3D.toFormulaString((FormulaRenderingWorkbook) parsingWorkbook);
                String sheetName = refString.substring(0, refString.indexOf('!'));
                int offset = sheetOffsets.get(workbook.getSheetIndex(sheetName));
                tokens[i] = new AreaPtg(token3D.getFirstRow() + offset, token3D.getLastRow() + offset,
                        token3D.getFirstColumn(), token3D.getLastColumn(),
                        token3D.isFirstRowRelative(), token3D.isLastRowRelative(),
                        token3D.isFirstColRelative(), token3D.isLastColRelative());
            } else if (token instanceof AreaPtg) {
                AreaPtg tokenArea = (AreaPtg) token;
                int offset = sheetOffsets.get(workbook.getSheetIndex(cell.getSheet().getSheetName()));
                tokens[i] = new AreaPtg(tokenArea.getFirstRow() + offset, tokenArea.getLastRow() + offset,
                        tokenArea.getFirstColumn(), tokenArea.getLastColumn(),
                        tokenArea.isFirstRowRelative(), tokenArea.isLastRowRelative(),
                        tokenArea.isFirstColRelative(), tokenArea.isLastColRelative());
            } else if (token instanceof RefPtg) {
                RefPtg tokenRef = (RefPtg) token;
                int offset = sheetOffsets.get(workbook.getSheetIndex(cell.getSheet().getSheetName()));
                tokens[i] = new RefPtg(tokenRef.getRow() + offset, tokenRef.getColumn(),
                        tokenRef.isRowRelative(), tokenRef.isColRelative());
            } else if (token instanceof Ref3DPtg) {
                Ref3DPtg token3D = (Ref3DPtg) token;
                // TODO: fix ptg sheet numbering
                int sheetIndex = token3D.getExternSheetIndex();
                String refString = token3D.toFormulaString((FormulaRenderingWorkbook) parsingWorkbook);
                String sheetName = refString.substring(0, refString.indexOf('!'));
                int offset = sheetOffsets.get(workbook.getSheetIndex(sheetName));
                tokens[i] = new RefPtg(token3D.getRow() + offset, token3D.getColumn(),
                        token3D.isRowRelative(), token3D.isColRelative());
            }
        }
        return FormulaRenderer.toFormulaString((FormulaRenderingWorkbook) parsingWorkbook, tokens);
    }

    /**
     *
     * @param wb
     * @return returns length of longest row in the wb
     */
    static private int getLongestRowInWB(Workbook wb) {
        int longestRow = 0;
        for (int i = 0; i < wb.getNumberOfSheets(); i++) {
            Sheet sheet = wb.getSheetAt(i);
            // Find longest row -- CSV reader requires all rows to have same number of delimiters
            for (int r = 0, rEnd = sheet.getLastRowNum(); r <= rEnd; r++) {
                Row row = sheet.getRow(r);
                if (row != null && row.getLastCellNum() > longestRow) {
                    longestRow = row.getLastCellNum();
                }
            }
        }
        return longestRow;
    }

    /**
     *
     * @param fileName
     * @return file extension of fileName
     */
    private String getFileExt(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i + 1);
        } else return "";
    }

    /**
     *
     * @param cell A cell in the Workbook
     * @return A string representation of the cell.
     */
    static private String getCellText(org.apache.poi.ss.usermodel.Cell cell) {
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
                    value = "=" + cell.getCellFormula();
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
            return "";
        }
    }
    /**
     *
     * @param cell A cell in the Workbook
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
                    value = "=" + getFormulaWithOffset(cell);
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
            return e.getMessage();
        }
    }
    /**
     *
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

    public static void main(String[] args) throws IOException {
        //File xls = new File("F:\\Program Files\\ApacheTomcat\\apache-tomcat-9.0.41\\temp\\test.xls,test.xls.temp");
        File xls = new File("F:\\Code\\dataspread\\xmlParser\\dataset-formula-comp-master\\SinglethreadedParser\\testDS\\test_xssf.xlsx");
        //File xls = new File("F:\\Code\\dataspread\\xmlParser\\dataset-formula-comp-master\\SinglethreadedParser\\testDS\\test_hssf.xls");
        //File xls = new File("F:\\Code\\dataspread\\xmlParser\\dataset-formula-comp-master\\SinglethreadedParser\\testDS\\albert_meyers_000_1_1.pst.0.xls");
        //File xls = new File("F:\\Code\\dataspread\\xmlParser\\dataset-formula-comp-master\\SinglethreadedParser\\testDS\\cooper_richey_000_1_1.pst.256.xls");
        //File xls = new File("F:\\Code\\dataspread\\xmlParser\\dataset-formula-comp-master\\SinglethreadedParser\\testDS\\craig_dean_000_1_1.pst.3.xls");
        //File xls = new File("F:\\Code\\dataspread\\xmlParser\\dataset-formula-comp-master\\SinglethreadedParser\\testDS\\andrea_ring_000_1_1.pst.1.xls");

        File csv = new File("F:\\Code\\dataspread\\xmlParser\\dataset-formula-comp-master\\SinglethreadedParser\\testDS\\result.csv");

        //basicFullConvert(xls, csv);

        XLStoCSV tester = new XLStoCSV(xls);
        tester.fullConvert(csv);

    }
}
