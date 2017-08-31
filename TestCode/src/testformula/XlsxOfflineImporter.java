package testformula;

import org.model.DBHandler;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.impl.BookImpl;
import org.zkoss.zss.model.sys.TransactionManager;
import org.zkoss.zss.range.SImporter;
import org.zkoss.zss.range.SImporters;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Created by zekun.fan@gmail.com on 8/25/17.
 */

public class XlsxOfflineImporter {
    public static void loadToDB(String path) throws IOException{
        SImporter importer=SImporters.getImporter();
        File inputf=new File(path);
        SBook book = importer.imports(inputf, UUID.randomUUID().toString());
        book.setBookName(book.getId());
        System.out.printf("Loaded %s, id=%s\n",path,book.getId());
    }
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://127.0.0.1:5432/dataspread";
        String driver = "org.postgresql.Driver";
        String userName = "dataspread";
        String password = "dataspread";
        DBHandler.connectToDB(url, driver, userName, password);
        System.out.println(LocalDateTime.now());
        loadToDB("D:\\csv\\1000.xlsx");
        System.out.println(LocalDateTime.now());
    }
}
