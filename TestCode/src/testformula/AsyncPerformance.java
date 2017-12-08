package testformula;

import org.model.DBHandler;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SSheet;
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
        FormulaAsyncScheduler formulaAsyncScheduler = new FormulaAsyncSchedulerSimple();
        Thread thread = new Thread(formulaAsyncScheduler);
        thread.start();


        SBook book= BookBindings.getBookByName("testBook");
        SSheet sheet = book.getSheet(0);

        CellRegion region = new CellRegion("A1");
        System.out.println("regsion " + region);

        sheet.getCell(0,0).setValue("100");
        System.out.println("R2 " + sheet.getCell(0,0).getReferenceString());

    //    for (int i=1;i<=100;i++)
    //        sheet.getCell(i,0).setFormulaValue("A" + i + "+1");

    //    Thread.sleep(20000);
   //    for (int i=1;i<10;i++)
  //          System.out.println(sheet.getCell(i,0).getReferenceString() + " " +
  //                  sheet.getCell(i,0).getValue());

        long startTime, endTime;
       /* Time to update A1 */
     /*
       startTime = System.currentTimeMillis();
        sheet.getCell(0,0).setValue("200");
       endTime = System.currentTimeMillis();
       System.out.print("Async time to update = " + (endTime-startTime));

        Thread.sleep(20000); */
        sheet.setSyncComputation(false);
        startTime = System.currentTimeMillis();
        sheet.getCell(0,0).setValue("300");
        endTime = System.currentTimeMillis();
        System.out.print("Sync time to update = " + (endTime-startTime));
       formulaAsyncScheduler.shutdown();
    }

}
