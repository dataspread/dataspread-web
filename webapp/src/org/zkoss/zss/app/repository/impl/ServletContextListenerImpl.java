package org.zkoss.zss.app.repository.impl;

import org.zkoss.util.logging.Log;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zss.app.BookManager;
import org.zkoss.zss.app.impl.BookManagerImpl;
import org.zkoss.zss.app.repository.BookRepositoryFactory;
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
		FormulaAsyncScheduler formulaAsyncScheduler = new FormulaAsyncSchedulerSimple();
		Thread thread = new Thread(formulaAsyncScheduler);
		thread.start();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		//ZSS-1052: skip this case, repository doesn't loaded before means there is nothing unsaved file waiting for saving.
		if(WebApps.getCurrent() != null) {
			BookManager manager = BookManagerImpl.getInstance(BookRepositoryFactory.getInstance().getRepository());
			manager.shutdownAutoFileSaving();
			try {
				manager.saveAll();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("Saving all files causes error: " + e.getMessage());
			}
		}
		//TODO save unfinished tasks
		FormulaAsyncScheduler.getScheduler().shutdown();
	}
}
