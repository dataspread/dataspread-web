package org.zkoss.zss.app.repository.impl;

import org.model.DBHandler;
import org.zkoss.lang.Library;
import org.zkoss.util.logging.Log;
import org.zkoss.zss.model.impl.GraphCompressor;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.Serializable;

public class ServletContextListenerImpl implements ServletContextListener, Serializable {
	private static final long serialVersionUID = 7123078891875657326L;
	private static final Log logger = Log.lookup(ServletContextListenerImpl.class.getName());
    private GraphCompressor graphCompressor;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		/* Startup the applicaction */
		DBHandler.initDBHandler();
		try {
			DBHandler.instance.cacheDS();
		} catch (Exception e) {
			System.err.println("Unable to connect to a Database");
			e.printStackTrace();
		}
		DBHandler.instance.initApplication();

        String FormulaAsyncSchedulerName = "org.zkoss.zss.model.impl.sys.formula." +
                Library.getProperty("FormulaAsyncScheduler",
                        "FormulaAsyncSchedulerThreaded");

        FormulaAsyncScheduler formulaAsyncScheduler;
        try {
            formulaAsyncScheduler = (FormulaAsyncScheduler) Class.forName(FormulaAsyncSchedulerName).newInstance();
            Thread thread = new Thread(formulaAsyncScheduler);
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        graphCompressor = new GraphCompressor();
        Thread graphCompressorThread = new Thread(graphCompressor);
        graphCompressorThread.start();

	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		//TODO save unfinished tasks
		FormulaAsyncScheduler.getScheduler().shutdown();
        graphCompressor.stopListener();
	}
}
