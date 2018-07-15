package org.zkoss.zss.app.repository.impl;

import org.model.DBHandler;
import org.zkoss.util.logging.Log;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zss.app.BookManager;
import org.zkoss.zss.app.impl.BookManagerImpl;
import org.zkoss.zss.model.impl.sys.formula.FormulaAsyncSchedulerSimple;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.io.Serializable;

public class ServletContextListenerImpl implements ServletContextListener, Serializable {
	private static final long serialVersionUID = 7123078891875657326L;
	private static final Log logger = Log.lookup(ServletContextListenerImpl.class.getName());

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

		FormulaAsyncScheduler formulaAsyncScheduler = new FormulaAsyncSchedulerSimple();
		Thread thread = new Thread(formulaAsyncScheduler);
		thread.start();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		//TODO save unfinished tasks
		FormulaAsyncScheduler.getScheduler().shutdown();
	}
}
