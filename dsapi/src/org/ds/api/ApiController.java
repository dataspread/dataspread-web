package org.ds.api;

import org.springframework.web.bind.annotation.*;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.sys.BookBindings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
public class ApiController {

    @RequestMapping(value = "/getCells/{book}/{sheet}/{row1}-{row2}/{col1}-{col2}",
            method = RequestMethod.GET)
    public Collection<Cell> getCells(@PathVariable String book,
                                     @PathVariable String sheet,
                                     @PathVariable int row1,
                                     @PathVariable int row2,
                                     @PathVariable int col1,
                                     @PathVariable int col2) {
        List<Cell> returnCells = new ArrayList<>();

        SBook sbook = BookBindings.getBookByName(book);
        SSheet sSheet = sbook.getSheetByName(sheet);

        for (int row = row1; row <= row2; row++) {
            for (int col = col1; col <= col2; col++) {
                SCell sCell = sSheet.getCell(row, col);

                Cell cell = new Cell();
                cell.row = row;
                cell.col = col;
                cell.value = sCell.getStringValue();
                if (sCell.getType() == SCell.CellType.FORMULA)
                    cell.formula = sCell.getFormulaValue();
                returnCells.add(cell);
            }
        }
        return returnCells;
    }
}