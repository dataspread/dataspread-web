package org.zkoss.zss.model.sys.formula.Test;

import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.lang.Library;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.AbstractCellAdv;

import java.util.ArrayList;
import java.util.Collection;

public class TestCommandHandler {

    public static TestCommandHandler instance = new TestCommandHandler();

    public void handleCommand(String command, SSheet sheet, int row, int column){
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            int updateNumber = 1;

            boolean updateToDB = true;

            if (command.endsWith("*")){
                updateToDB = false;
                command = command.substring(0,command.length() - 1);
            }

            if (command.contains("@") && command.substring(command.indexOf('@') + 1).matches("\\d+")) {
                updateNumber = Integer.valueOf(command.substring(command.indexOf('@') + 1));
                command = command.substring(0, command.indexOf('@'));

            }

            Collection<AbstractCellAdv> cells = new ArrayList<>();
            sheet.getCells(new CellRegion(row,column,row + updateNumber - 1,column))
                    .forEach((c)->cells.add((AbstractCellAdv)c));

            int i = 0;

            if (command.startsWith("=")) {
                command = command.substring(1);
                for (SCell cell : cells) {
                    i++;
                    cell.setFormulaValue(command.replaceAll("\\?", String.valueOf(i)), connection, false);
                }
            }else
                for (SCell cell:cells)
                    try {
                        cell.setNumberValue(Double.parseDouble(command), connection, false);
                    } catch (Exception e) {
                        cell.setStringValue(command, connection, false);
                    }
            if (Boolean.valueOf(Library.getProperty("SynchronizeFormula")) && updateToDB && updateNumber <= 10000)
                sheet.getDataModel().updateCells(new DBContext(connection), cells);
            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
