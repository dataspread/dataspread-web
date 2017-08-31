package testformula;

import org.model.DBHandler;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SBooks;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.BookImpl;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerCoverFIFO;
import org.zkoss.zss.model.sys.TransactionManager;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.util.Random;

/**
 * Created by zekun.fan@gmail.com on 8/25/17.
 */

public class FormulaPerformance {
    private static final String logpath="D:\\formulaLog.csv";
    private static final String dbid="sj6sevlgf";
    private static final int range=10000;
    private static final int modification=1000;

    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://127.0.0.1:5432/dataspread";
        String driver = "org.postgresql.Driver";
        String userName = "dataspread";
        String password = "dataspread";
        DBHandler.connectToDB(url, driver, userName, password);

        FormulaAsyncScheduler.initScheduler(new FormulaAsyncSchedulerCoverFIFO());
        FormulaAsyncScheduler.initLogWriter(new FileWriter(new File(logpath)));
        SBook book= SBooks.createOrGetBook(dbid);
        TransactionManager.INSTANCE.startTransaction(book);
        book.setIdAndLoad(dbid);
        shuffle(book.getSheet(0));
        TransactionManager.INSTANCE.endTransaction(book);
        ((FormulaAsyncSchedulerCoverFIFO)FormulaAsyncScheduler.getScheduler()).shutdownGracefully();
    }

    private static void shuffle(SSheet sheet){
        Random random=new Random();
        Connection conn=DBHandler.instance.getConnection();
        for (int i=0;i<modification;++i)
            sheet.getCell(1+random.nextInt(range),0).setNumberValue(random.nextDouble()*range,conn,false);
    }
}
