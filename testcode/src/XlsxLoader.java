import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.zkoss.poi.xssf.extractor.XSSFExcelExtractor;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.sys.BookBindings;

import java.io.IOException;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class XlsxLoader {

    private static void connect(){
        String url = "jdbc:postgresql://127.0.0.1:5432/dataspread";
        String driver = "org.postgresql.Driver";
        String userName = "dbuser";
        String password = "dbadmin";

        DBHandler.connectToDB(url, driver, userName, password);
    }

    private static SBook importBook(String bookName) throws Exception {
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

        System.out.println("Imported book" + book.getBookName());

        return book;
    }

    private static void importSheet(SBook book, String sheetName, String text) throws IOException {
        if (book.getSheetByName(sheetName) == null)
            book.createSheet(sheetName);
        //import into sheet
        book.getSheetByName(sheetName).getDataModel()
                .importSheet(new StringReader(text),'\t',true);
        //send message
        System.out.println("Imported sheet" + sheetName);
    }

    public static void main(String[] args) throws Exception {
        connect();
//        String prefix = "L:\\Project\\DataSpread\\We_want_your_spreadsheets__70684530474156\\" +
//                "We_want_your_spreadsheets__70684530474156\\";
        String prefix = "/Users/yulu/Desktop/home/Project/DataSpread/We_want_your_spreadsheets__70684530474156/";
        String[] files = {
//                "smallFormula\\CS_465_User_Evaulations.xlsx"
                "uploads_3696634124225598172/CS465UserEvaulations.xlsx"
//                "smallFormula\\CIQ_301_DCF-Student_Model_Spring_2017_DIS.xlsx"
        };

        for (String file: files) {
//            String bookName = file.split("\\\\")[1] + "1";
            XSSFExcelExtractor extractor =
                    new XSSFExcelExtractor(prefix + file);
            String[] sheets = extractor.getText().replaceAll("\t",",")
                    .split("\n=============================================" +
                    "================================\n");
//            System.out.println("Book name:\n" + bookName);
//            SBook book = importBook(bookName);
            for (String text:sheets){
                String sheetName = text.substring(0,text.indexOf("\n"));
                String content = text.substring(text.indexOf("\n") + 1);
//                importSheet(book,sheetName,content);
                System.out.println("Sheet name:\n" + sheetName);
                System.out.println("Content: \n" + content.replaceAll("\t",","));
            }

//            System.out.println(text);
        }
    }

}
