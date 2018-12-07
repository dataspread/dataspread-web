package org.zkoss.zss.model.sys.formula.Test;

import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.lang.Library;
import org.zkoss.poi.hslf.model.Sheet;
import org.zkoss.poi.xssf.extractor.XSSFExcelExtractor;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.impl.AbstractSheetAdv;
import org.zkoss.zss.model.sys.BookBindings;

import java.io.IOException;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

public class TestCommandHandler {

    public static TestCommandHandler instance = new TestCommandHandler();

    String lastCommand = "";

    public void handleCommand(String command, SSheet sheet, int row, int column){
        if (command.length() == 0)
            command = lastCommand;
        lastCommand = command;
        if (command.startsWith("load")){
            String[] commands = command.split(":");
            if (commands.length == 1)
                loadXlsxSpreadsheets();
            else
                loadXlsxSpreadsheets(_prefix,commands[1].split(","));
            return;
        }

        if (command.startsWith("rerun")){
            SBook book = sheet.getBook();

            int rounds = 1;

            String[] commands = command.split("-");

            if (commands.length > 1)
                rounds = Integer.valueOf(commands[1]);

            for (int i = 0; i < rounds;i++){
                try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
                    for (SSheet s : book.getSheets()) {
                        for (SCell cell : s.getCells()) {
                            if (cell.getType().equals(SCell.CellType.FORMULA)) {
                                cell.setFormulaValue(cell.getFormulaValue(), connection, false);
                            } else if (cell.getType().equals(SCell.CellType.STRING)
                                    && ((String) cell.getValue()).startsWith("=")) {
                                cell.setFormulaValue(((String) cell.getValue()).substring(1), connection, false);
                            }

                        }
                    }
//                if (Boolean.valueOf(Library.getProperty("SynchronizeFormula")))
//                    sheet.getDataModel().updateCells(new DBContext(connection), cells);
                }
                if (i > 1)
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }


            return;
        }

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


    private SBook importBook(String bookName) throws Exception {
        String query = "SELECT COUNT(*) FROM books WHERE bookname = ?";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, bookName);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                if (rs.getInt(1) > 0)
                    throw new Exception("error");
            }
        }

        SBook book = BookBindings.getBookByName(bookName);
        book.checkDBSchema();
        query = "INSERT INTO user_books VALUES (?, ?, 'owner')";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "guest");
            statement.setString(2, book.getId());
            statement.execute();
            connection.commit();
        }

        System.out.println("Imported book:" + book.getBookName());

        return book;
    }

    private SBook importBookWithMultipleTry(String originalBookName){
        String bookName = originalBookName;
        boolean success = false;
        SBook book = null;
        int id = 0;
        while (!success){
            try {
                book = importBook(bookName);
                success = true;
            }
            catch (Exception e){
                bookName = originalBookName + "(" + (++id) + ")";
            }

        }

        System.out.println("Created Book :\n" + bookName);

        return book;
    }

    private void importSheet(SBook book, String sheetName, String text) throws IOException {
        byte[] bytes = text.getBytes("UTF-8");
        text = new String(bytes,"UTF-8");
        if (book.getSheetByName(sheetName) == null)
            book.createSheet(sheetName);
        //import into sheet
        book.getSheetByName(sheetName).getDataModel()
                .importSheet(new StringReader(text),'\t',true);
        //send message
        System.out.println("Imported sheet:" + sheetName);
    }

//    cmd:load:smallFormula\CIQ_301_DCF-Student_Model_Spring_2017_DIS.xlsx

    String _prefix = "L:\\Project\\DataSpread\\We_want_your_spreadsheets__70684530474156\\" +
        "We_want_your_spreadsheets__70684530474156\\";
//    String _prefix = "/Users/yulu/Desktop/home/Project/DataSpread/We_want_your_spreadsheets__70684530474156/";
    String[] _files = {
//                "smallFormula\\CS_465_User_Evaulations.xlsx"
        "smallFormulaArbitraryOverlapping\\harvestdata.jun172015.c046.xlsx"
//        "smallFormula\\CIQ_301_DCF-Student_Model_Spring_2017_DIS.xlsx"
//            "uploads_3696634124225598172/CS465UserEvaulations.xlsx"
//                "smallFormula\\CIQ_301_DCF-Student_Model_Spring_2017_DIS.xlsx"
    };

    public void loadXlsxSpreadsheets(){
        loadXlsxSpreadsheets(_prefix,_files);
    }

    private static int countChar(String s, char ch){
        int ans = 0;
        for (int i = 0; i < s.length();i++) {
            if (s.charAt(i) == ch)
                ans ++;
        }
        return ans;
    }

    private String removeRedundantChar(String s){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length();i++)
//            if ((s.charAt(i) >=32 && s.charAt(i) <=126) || s.charAt(i) == '\t')
            if (s.charAt(i) <=126)
                sb.append(s.charAt(i));
        return sb.toString();
    }

    private String formatContent(String content){
        content = removeRedundantChar(content);
        StringBuilder sb = new StringBuilder();
        String[] rows = content.split("\n",-1);
        int maxColumn = 0;
        int count;
        for (String row:rows){
            if ((count = countChar(row,'\t')) > maxColumn)
                maxColumn = count;
        }
        for (String row:rows){
            sb.append(row);
            for (int i = countChar(row,'\t'); i < maxColumn; i++)
                sb.append("\t");
            sb.append("\n");
        }
        return sb.toString();
    }

    private void loadXlsxSpreadsheets(String prefix, String[] files){
        try {
            for (String file: files) {
                String bookName = file.split("\\\\")[1];
                XSSFExcelExtractor extractor =
                        new XSSFExcelExtractor(prefix + file);
                String[] sheets = extractor.getTextForImport()
                        .split("\n=============================================" +
                                "================================\n");
                SBook book = importBookWithMultipleTry(bookName);

                boolean containsSheet1 = false;
                int row = -1;
                int col = -1;
                for (String text:sheets){
                    String sheetName = text.substring(0,text.indexOf("\n"));
                    if (sheetName.toLowerCase().equals("sheet1"))
                        containsSheet1 = true;
                    String content = formatContent(text.substring(text.indexOf("\n") + 1));
                    if (row == -1){
                        row = 1 + countChar(content,'\n');
                        col = 1 + countChar(content,'\t') / row;
                    }
                    importSheet(book,sheetName,content);

                    System.out.println("Sheet name:\n" + sheetName);
                    System.out.println("Content: \n" + content.replaceAll("\t",","));
                }

                if (!containsSheet1) {
                    importSheet(book, "sheet1",
                            "empty sheet for dataspread in 12/2/2018");
                    try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
                        book.getSheet(0).getCell(row,col).setStringValue(
                                "empty sheet for dataspread in 12/2/2018",connection,true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
//                            );

                System.out.println("Import successful");
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
