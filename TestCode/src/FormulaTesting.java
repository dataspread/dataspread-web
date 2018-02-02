import org.model.DBHandler;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.AbstractBookAdv;
import org.zkoss.zss.model.impl.BookImpl;
import org.zkoss.zss.model.sys.BookBindings;

import java.util.stream.IntStream;

public class FormulaTesting {

    public static void main(String[] args) {
        String url = "jdbc:postgresql://127.0.0.1:5432/ibd";
        String driver = "org.postgresql.Driver";
        String userName = "mangesh";
        String password = "";
        DBHandler.connectToDB(url, driver, userName, password);

        SBook book = BookBindings.getBookByName("Test Book");

        System.out.println("Sheets " + book.getNumOfSheet());
        SSheet sheet = book.getSheet(0);
        long startTime, endTime;

        startTime = System.currentTimeMillis();
        sheet.getCell("C3").setValue(60.0);
        endTime = System.currentTimeMillis();
        System.out.println("Time taken " + (endTime - startTime));

        IntStream.rangeClosed(sheet.getStartRowIndex(),
                sheet.getEndRowIndex()).forEach(
                row ->
                        IntStream.rangeClosed(sheet.getStartColumnIndex(),
                                sheet.getEndColumnIndex()).mapToObj(
                                col -> sheet.getCell(row, col))
                                .forEach(e -> System.out.println("Cell " + e.getReferenceString() + " " + e.getValue()))
        );
    }

}
