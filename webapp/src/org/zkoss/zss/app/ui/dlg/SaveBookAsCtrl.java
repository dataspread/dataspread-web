/* 
	Purpose:
		
	Description:
		
	History:
		2013/7/10, Created by dennis

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.ui.dlg;

import java.util.Arrays;
import java.util.Map;

import org.zkoss.lang.Strings;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.app.impl.CollaborationInfoImpl;
import org.zkoss.zss.app.BookRepository;
import org.zkoss.zss.app.repository.BookRepositoryFactory;
import org.zkoss.zss.app.BookManager;
import org.zkoss.zss.app.impl.BookManagerImpl;
import org.zkoss.zss.app.repository.impl.BookUtil;
import org.zkoss.zss.app.repository.impl.SimpleBookInfo;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * 
 * @author dennis
 *
 */
public class SaveBookAsCtrl extends DlgCtrlBase{
	private static final long serialVersionUID = 1L;
	
	public final static String ARG_NAME = "name";
	public final static String BOOK = "book";
	private static final String TITLE = "title";
	private static final String OK_BTN_NAME = "okBtnName";
	
	public static final String ON_SAVE = "onSave";
		
	@Wire
	Textbox bookName;
	
	@Wire
	Button save;
	
	private BookRepository repo = BookRepositoryFactory.getInstance().getRepository();
	private BookManager bookManager = BookManagerImpl.getInstance(repo);
	private final static String URI = "~./zssapp/dlg/saveBookAs.zul";

	private Book _book;
	
	public static void show(EventListener<DlgCallbackEvent> callback, String name, Book book) {
		Map arg = newArg(callback);
		arg.put(ARG_NAME, name);
		arg.put(BOOK, book);
		
		Window comp = (Window)Executions.createComponents(URI, null, arg);
		comp.doModal();
		return;
	}
	
	// this function provides changing title and save button's name
	public static void show(EventListener<DlgCallbackEvent> callback, String name, Book book, String title, String okBtnName) {
		Map arg = newArg(callback);
		arg.put(ARG_NAME, name);
		arg.put(BOOK, book);
		arg.put(TITLE, title);
		arg.put(OK_BTN_NAME, okBtnName);
		
		Window comp = (Window)Executions.createComponents(URI, null, arg);
		comp.doModal();
		return;
	}
	
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		
		Map<?, ?> arg = Executions.getCurrent().getArg();

		_book =  (Book) arg.get(BOOK);
		String title = (String) arg.get(TITLE);
		String okBtnName = (String)arg.get(OK_BTN_NAME);
		
		if(title != null)
			comp.setTitle(title);
		
		if(okBtnName != null)
			save.setLabel(okBtnName);
	}

	@Listen("onClick=#save; onOK=#saveAsDlg")
	public void onSave(){
		bookName.clearErrorMessage();
		if(Strings.isBlank(bookName.getValue())){
			bookName.setErrorMessage("empty name is not allowed");
			return;
		}

		if (_book.getInternalBook().setBookName(bookName.getValue()))
		{
			postCallback(ON_SAVE, newMap(newEntry(ARG_NAME, bookName.getValue())));
			detach();
		}
		else
		{
			Messagebox.show("File Name already exists.", "DataSpread",
					Messagebox.OK , Messagebox.ERROR);
		}
	}
	
	@Listen("onClick=#cancel; onCancel=#saveAsDlg")
	public void onCancel(){
		detach();
	}
}
