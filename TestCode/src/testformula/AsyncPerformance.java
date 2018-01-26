package testformula;

import org.model.DBHandler;
import org.model.GraphCompressor;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.SheetImpl;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerSimple;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

public class AsyncPerformance {
    private static final int range=100000;
    private static final int modification=1000;

    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://127.0.0.1:5432/ibd";
        String driver = "org.postgresql.Driver";
        String userName = "mangesh";
        String password = "mangesh";
        DBHandler.connectToDB(url, driver, userName, password);

        SheetImpl.simpleModel = true;
        FormulaAsyncScheduler formulaAsyncScheduler = new FormulaAsyncSchedulerSimple();
        Thread thread = new Thread(formulaAsyncScheduler);
        thread.start();

        GraphCompressor  graphCompressor = new GraphCompressor();
        Thread thread2 = new Thread(formulaAsyncScheduler);

        SBook book= BookBindings.getBookByName("testBook");
        SSheet sheet = book.getSheet(0);


        sheet.getCell(0,0).setValue("500");

        for (int i=1;i<=100;i++)
            sheet.getCell(i,0).setFormulaValue("A" + i + "+1");


        Thread.sleep(5000);
        sheet.setSyncComputation(true);
       //sheet.clearCache();
        long startTime, endTime;
       /* Time to update A1 */


        sheet.clearCache();
        startTime = System.currentTimeMillis();
        sheet.getCell(0,0).setValue("300");
        System.out.println("Final Value "
                + sheet.getCell(100,0).getValue());
        endTime = System.currentTimeMillis();
        System.out.println("Sync time to update = " + (endTime-startTime));


        sheet.setSyncComputation(false);
        sheet.clearCache();
        startTime = System.currentTimeMillis();
        sheet.getCell(0,0).setValue("200");
        System.out.println("Final Value "
                + sheet.getCell(100,0).getValue());
       endTime = System.currentTimeMillis();
       System.out.println("Async time to update = " + (endTime-startTime));
       formulaAsyncScheduler.waitForCompletion();
        endTime = System.currentTimeMillis();
        System.out.println("Final Value "
                + sheet.getCell(100,0).getValue());
        System.out.println("Async time to complete = " + (endTime-startTime));

        formulaAsyncScheduler.shutdown();
        thread.join();
    }

}
