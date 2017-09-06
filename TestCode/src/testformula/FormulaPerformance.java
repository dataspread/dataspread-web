package testformula;

import org.model.DBHandler;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SBooks;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.FormulaCacheCleaner;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerCoverFIFO;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerCoverLTF;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.TransactionManager;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

/**
 * Created by zekun.fan@gmail.com on 8/25/17.
 */

public class FormulaPerformance {
    private static final String xlsPath="D:\\csv\\100000.xlsx";

    private static final int range=100000;
    private static final int modification=1000;

    private static FileWriter log;

    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://127.0.0.1:5432/dataspread";
        String driver = "org.postgresql.Driver";
        String userName = "dataspread";
        String password = "dataspread";
        DBHandler.connectToDB(url, driver, userName, password);

        String logpath=String.format("%s_%s_r%d_m%d_%s.csv",xlsPath,"LTF",range,modification, UUID.randomUUID());
        log=new FileWriter(new File(logpath));
        FormulaAsyncScheduler.initLogWriter(log);
        FormulaAsyncScheduler.initScheduler(new FormulaAsyncSchedulerCoverLTF());

        importAndShuffleTest(xlsPath);
    }

    private static void importAndShuffleTest(String path) throws Exception{

        //System.out.printf("Importing %s @%s\n",path,LocalDateTime.now());
        //System.out.printf("Imported %s,id=%s @%s\n",path,importedId, LocalDateTime.now());
        //System.out.printf("Shuffled @%s",LocalDateTime.now());
        SBook book= XlsxOfflineImporter.loadToDB(path);
        TransactionManager.INSTANCE.startTransaction(book);
        FormulaCacheCleaner.setCurrent(new FormulaCacheCleaner(book.getBookSeries()));
        shuffle(book.getSheet(0));
        TransactionManager.INSTANCE.endTransaction(book);
    }

    private static void loadAndShuffleTest(String dbid) throws Exception{
        SBook book= SBooks.createOrGetBook(dbid);
        TransactionManager.INSTANCE.startTransaction(book);
        book.setIdAndLoad(dbid);
        shuffle(book.getSheet(0));
        TransactionManager.INSTANCE.endTransaction(book);
    }

    private static void shuffle(SSheet sheet){
        try {Thread.sleep(300000);}catch (InterruptedException ignored){return;}

        Random random=new Random();
        Connection conn=DBHandler.instance.getConnection();
        for (int i=0;i<modification;++i)
            sheet.getCell(1+random.nextInt(range),0).setNumberValue(random.nextDouble()*range,conn,false);
    }
}
