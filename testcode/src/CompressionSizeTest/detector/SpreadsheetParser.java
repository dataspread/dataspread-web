package CompressionSizeTest.detector;

import CompressionSizeTest.detector.Utils.CellArea;
import CompressionSizeTest.detector.Utils.CellLoc;
import CompressionSizeTest.detector.Utils.Pair;
import Utils.SheetNotSupportedException;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaParsingWorkbook;
import org.apache.poi.ss.formula.FormulaRenderingWorkbook;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.*;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

public class SpreadsheetParser implements Iterable<SheetData> {

    private Workbook workbook;
    private FormulaParsingWorkbook evalbook;
    private final SheetData[] sheetDataArray;
    private final boolean rowWise;
    private final String fileName;

    public SpreadsheetParser(String filePath, boolean rowWise) {
        File fileItem = new File(filePath);
        fileName = fileItem.getName();
        try {
            this.workbook = WorkbookFactory.create(fileItem);
            this.evalbook = HSSFEvaluationWorkbook.create((HSSFWorkbook) workbook);
        } catch (Exception e) {
            System.err.println("Parsing " + filePath + " failed");
            System.exit(-1);
        }

        this.rowWise = rowWise;
        sheetDataArray = new SheetData[workbook.getNumberOfSheets()];
    }

    public boolean skipParsing(int threshold) {
        int totalRows = 0;
        for(Sheet sheet: workbook) {
            totalRows += sheet.getPhysicalNumberOfRows();
        }
        return totalRows <= threshold;
    }

    public void parseSpreadsheet() {
        for (int i = 0; i < sheetDataArray.length; i++) {
            try {
                sheetDataArray[i] = parseOneSheet(workbook.getSheetAt(i));
            } catch (SheetNotSupportedException e) {
                sheetDataArray[i] = null;
            }
        }
    }

    public String getFileName() {
        return fileName;
    }

    private SheetData parseOneSheet(Sheet sheet) throws SheetNotSupportedException {
        SheetData sheetData = new SheetData(sheet.getSheetName());
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell != null && cell.getCellType() == CellType.FORMULA) {
                    parseOneCell(sheetData, cell);
                }
            }
        }
        sheetData.sortByLoc(rowWise);
        return sheetData;
    }

    private void parseOneCell(SheetData sheetData, Cell cell) throws SheetNotSupportedException {
        Ptg[] tokens = this.getTokens(cell);
        CellLoc cellLoc = new CellLoc(cell.getRowIndex(), cell.getColumnIndex());
        HashSet<CellArea> cellAreas = new HashSet<>();
        if (tokens != null) {
            for (Ptg token : tokens) {
                if (token instanceof OperandPtg) {
                    CellArea depArea = parseOneToken(cell, (OperandPtg) token);
                    if (depArea != null) cellAreas.add(depArea);
                }
            }
        }
        if (!cellAreas.isEmpty()) sheetData.addOneDep(new Pair<>(cellAreas, cellLoc));
    }

    private CellArea parseOneToken(Cell cell, OperandPtg token) throws SheetNotSupportedException {
        Sheet sheet = this.getDependentSheet(cell, token);
        if (sheet != null) {
            if (token instanceof Area2DPtgBase) {
                Area2DPtgBase ptg = (Area2DPtgBase) token;
                int rowStart = ptg.getFirstRow();
                int colStart = ptg.getFirstColumn();
                int rowEnd = ptg.getLastRow();
                int colEnd = ptg.getLastColumn();
                boolean validArea = true;
                for (int r = ptg.getFirstRow(); r <= ptg.getLastRow(); r++) {
                    for (int c = ptg.getFirstColumn(); c <= ptg.getLastColumn(); c++) {
                        Cell dep = this.getCellAt(sheet, r, c);
                        if (dep == null) validArea = false;
                    }
                }
                if (validArea) return new CellArea(rowStart, colStart, rowEnd, colEnd);
            } else if (token instanceof RefPtg) {
                RefPtg ptg = (RefPtg) token;
                int row = ptg.getRow();
                int col = ptg.getColumn();
                Cell dep = this.getCellAt(sheet, row, col);
                if (dep != null) return new CellArea(row, col, row, col);
            } else if (token instanceof Area3DPtg ||
                    token instanceof Area3DPxg ||
                    token instanceof Ref3DPtg ||
                    token instanceof Ref3DPxg) {
                throw new SheetNotSupportedException();
            }
        }

        return null;
    }

    private Sheet getDependentSheet (Cell src, OperandPtg opPtg) {
        Sheet sheet = null;
        if (opPtg instanceof RefPtg) {
            sheet = src.getSheet();
        } else if (opPtg instanceof Area2DPtgBase) {
            sheet = src.getSheet();
        } else if (opPtg instanceof Ref3DPtg) {
            sheet = this.workbook.getSheet(this.getSheetNameFrom3DRef((Ref3DPtg) opPtg));
        } else if (opPtg instanceof Area3DPtg) {
            sheet = this.workbook.getSheet(this.getSheetNameFrom3DRef((Area3DPtg) opPtg));
        }
        return sheet;
    }

    private String getSheetNameFrom3DRef (OperandPtg ptg) {
        String sheetName = null;
        if (ptg instanceof Ref3DPtg) {
            Ref3DPtg ptgRef3D = (Ref3DPtg) ptg;
            sheetName = ptgRef3D.toFormulaString((FormulaRenderingWorkbook) this.evalbook);
        } else if (ptg instanceof Area3DPtg) {
            Area3DPtg ptgArea3D = (Area3DPtg) ptg;
            sheetName = ptgArea3D.toFormulaString((FormulaRenderingWorkbook) this.evalbook);
        }
        return sheetName != null ? sheetName.substring(0, sheetName.indexOf('!')) : null;
    }

    private Cell getCellAt (Sheet sheet, int rowIdx, int colIdx) {
        Cell cell;
        try {
            cell = sheet.getRow(rowIdx).getCell(colIdx);
        } catch (NullPointerException e) {
            return null;
        }
        return cell;
    }

    private Ptg[] getTokens (Cell cell) {
        try {
            return FormulaParser.parse(
                    cell.getCellFormula(),
                    this.evalbook,
                    FormulaType.CELL,
                    this.workbook.getSheetIndex(cell.getSheet()),
                    cell.getRowIndex()
            );
        } catch (Exception e) { return null; }
    }

    public Iterator<SheetData> iterator() {
        return Arrays.stream(sheetDataArray).iterator();
    }
}
