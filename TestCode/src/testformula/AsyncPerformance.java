package testformula;

import org.model.DBHandler;
import org.model.GraphCompressor;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.FormulaCacheCleaner;
import org.zkoss.zss.model.impl.SheetImpl;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerSimple;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.sys.formula.DirtyManagerLog;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import java.util.Collection;
import java.util.stream.Collectors;

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
        SheetImpl.disablePrefetch();
        FormulaAsyncScheduler formulaAsyncScheduler = new FormulaAsyncSchedulerSimple();
        Thread thread = new Thread(formulaAsyncScheduler);
        thread.start();

        GraphCompressor  graphCompressor = new GraphCompressor();
        Thread thread2 = new Thread(formulaAsyncScheduler);

        SBook book= BookBindings.getBookByName("testBook");
        /* Cleaner for sync computation */
        FormulaCacheCleaner.setCurrent(new FormulaCacheCleaner(book.getBookSeries()));
        SSheet sheet = book.getSheet(0);

        sheet.getCell(0,0).setValue("500");


        sheet.setSyncComputation(true);
        int cellCount = 50;
        for (int i=1;i<=cellCount;i++)
            sheet.getCell(i,0).setFormulaValue("A" + i + "+1");


        Thread.sleep(2000);
       //sheet.clearCache();
        long startTime, endTime;
       /* Time to update A1 */


        sheet.clearCache();
        startTime = System.currentTimeMillis();

        sheet.getCell(0,0).setValue("300");
        System.out.println("Final Value "
                + sheet.getCell(cellCount,0).getValue());
        endTime = System.currentTimeMillis();
        System.out.println("Sync time to update = " + (endTime-startTime));


        sheet.setSyncComputation(false);
        sheet.clearCache();

        System.out.println("Starting Asyn ");
        startTime = System.currentTimeMillis();

        sheet.getCell(0,0).setValue("200");
        //System.out.println("Final Value "
        //        + sheet.getCell(cellCount,0).getValue());
       endTime = System.currentTimeMillis();
       System.out.println("Async time to update = " + (endTime-startTime));
       formulaAsyncScheduler.waitForCompletion();
       endTime = System.currentTimeMillis();
       System.out.println("Final Value "
                + sheet.getCell(cellCount,0).getValue());
       System.out.println("Async time to complete = " + (endTime-startTime));

        //Get total dirty time for all cells
        formulaAsyncScheduler.shutdown();
        thread.join();

        //Get total dirty time for all cells
        Collection<SCell> cells = sheet.getCells().stream()
                .filter(e->e.getType()== SCell.CellType.FORMULA)
                .collect(Collectors.toList());
        long totalWaitTime = cells.stream()
                .mapToLong(e-> DirtyManagerLog.instance.getDirtyTime(e.getCellRegion()))
                .sum();
        System.out.println("Total Wait time " + totalWaitTime);
        System.out.println("Avg Wait time " + totalWaitTime/cells.size());


    }

}
