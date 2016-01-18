package org.zkoss.zss.ui.impl.ua;

import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.UserActionContext;
import org.zkoss.zss.ui.UserActionHandler;

/**
 * always disable and do nothing
 * @author dennis
 * @since 3.0.0
 */
public class NilHandler implements UserActionHandler{

	@Override
	public boolean isEnabled(Book book, Sheet sheet) {
		return false;
	}

	@Override
	public boolean process(UserActionContext ctx) {
		return true;
	}

}
