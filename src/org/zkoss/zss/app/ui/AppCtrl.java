/* 
	Purpose:
		
	Description:
		
	History:
		2013/7/10, Created by dennis

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.ui;

import java.io.*;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zkoss.image.AImage;
import org.zkoss.lang.Library;
import org.zkoss.lang.Strings;
import org.zkoss.util.logging.Log;
import org.zkoss.util.media.AMedia;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.web.servlet.http.Encodes;
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.DesktopCleanup;
import org.zkoss.zss.api.*;
import org.zkoss.zss.api.model.*;
import org.zkoss.zss.api.model.Book.BookType;
import org.zkoss.zss.api.model.Hyperlink.HyperlinkType;
import org.zkoss.zss.app.BookInfo;
import org.zkoss.zss.app.BookRepository;
import org.zkoss.zss.app.CollaborationInfo;
import org.zkoss.zss.app.CollaborationInfo.CollaborationEventListener;
import org.zkoss.zss.app.impl.CollaborationInfoImpl;
import org.zkoss.zss.app.CollaborationInfo.CollaborationEvent;
import org.zkoss.zss.app.repository.*;
import org.zkoss.zss.app.BookManager;
import org.zkoss.zss.app.impl.BookManagerImpl;
import org.zkoss.zss.app.repository.impl.BookUtil;
import org.zkoss.zss.app.repository.impl.SimpleBookInfo;
import org.zkoss.zss.app.ui.dlg.*;
import org.zkoss.zss.model.ModelEvent;
import org.zkoss.zss.model.ModelEventListener;
import org.zkoss.zss.model.ModelEvents;
import org.zkoss.zss.ui.*;
import org.zkoss.zss.ui.event.Events;
import org.zkoss.zss.ui.event.SyncFriendFocusEvent;
import org.zkoss.zss.ui.impl.DefaultUserActionManagerCtrl;
import org.zkoss.zss.ui.impl.Focus;
import org.zkoss.zss.ui.sys.UndoableActionManager;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Html;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Script;

/**
 * 
 * @author dennis
 *
 */
public class AppCtrl extends CtrlBase<Component>{
	private static final Log log = Log.lookup(AppCtrl.class); 
	private static final long serialVersionUID = 1L;
	public static final String ZSS_USERNAME = "zssUsername";
	private static final String UNSAVED_MESSAGE = "Do you want to leave this book without save??";
	private static final String UTF8 = "UTF-8";
	private static final boolean DISABLE_BOOKMARK = Boolean.valueOf(Library.getProperty("zssapp.bookmark.disable", "false"));
	
	private static BookRepository repo = BookRepositoryFactory.getInstance().getRepository();
	private static CollaborationInfo collaborationInfo = CollaborationInfoImpl.getInstance();
	private static BookManager bookManager = BookManagerImpl.getInstance(repo);
	private ModelEventListener dirtyChangeEventListener;
	static {
		collaborationInfo.addEvent(new CollaborationEventListener() {
			@Override
			public void onEvent(CollaborationEvent event) {
				if(event.getType() == CollaborationEvent.Type.BOOK_EMPTY)
					try {
						BookInfo info = null;
						String bookName = (String) event.getValue();
						for (BookInfo bookInfo : repo.list()) {
							if(bookInfo.getName().equals(bookName))
								info = bookInfo;
						}
						
						if(info != null)
							bookManager.detachBook(info);
					} catch (IOException e) {
						log.error(e.getMessage());
						e.printStackTrace();
						UiUtil.showWarnMessage("Can't detach book: " + event.getValue());
					}
			}
		});
	}
	
	private String username;

	@Wire
	Spreadsheet ss;
	
	@Wire
	Script confirmMsgWorkaround;
	
	@Wire
	Script gaScript;
	
	@Wire
	Html usersPopContent; //ZSS-998
	
	BookInfo selectedBookInfo;
	Book loadedBook;
	Desktop desktop = Executions.getCurrent().getDesktop();
	
	private UnsavedAlertState isNeedUnsavedAlert = UnsavedAlertState.DISABLED;
	
	public AppCtrl() {
		super(true);
	}
	
	public void doBeforeComposeChildren(Component comp) throws Exception {
		super.doBeforeComposeChildren(comp);
		comp.setAttribute(APPCOMP, comp);
	}
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		boolean isEE = "EE".equals(Version.getEdition());
		boolean readonly = UiUtil.isRepositoryReadonly();
		UserActionManager uam = ss.getUserActionManager();
		uam.registerHandler(DefaultUserActionManagerCtrl.Category.AUXACTION.getName(), AuxAction.NEW_BOOK.getAction(), new UserActionHandler() {
			
			@Override
			public boolean process(UserActionContext ctx) {
				doOpenNewBook(true);
				return true;
			}
			
			@Override
			public boolean isEnabled(Book book, Sheet sheet) {
				return true;
			}
		});
		if(!readonly){
			uam.setHandler(DefaultUserActionManagerCtrl.Category.AUXACTION.getName(), AuxAction.SAVE_BOOK.getAction(), new UserActionHandler() {
				
				@Override
				public boolean process(UserActionContext ctx) {
					doSaveBook(false);
					return true;
				}
				
				@Override
				public boolean isEnabled(Book book, Sheet sheet) {
					return book!=null;
				}
			});
		}
		if(isEE){
			uam.setHandler(DefaultUserActionManagerCtrl.Category.AUXACTION.getName(), AuxAction.EXPORT_PDF.getAction(), new UserActionHandler() {
				
				@Override
				public boolean process(UserActionContext ctx) {
					doExportPdf();
					return true;
				}
				
				@Override
				public boolean isEnabled(Book book, Sheet sheet) {
					return book!=null;
				}
			});
			
			// ZSS-940 remove GA code for EE customer
			Object evalOnly = Executions.getCurrent().getDesktop().getWebApp().getAttribute("Evaluation Only");
			if(!(evalOnly == null ? false : (Boolean)evalOnly))
				gaScript.setParent(null);
		}
		
		//do after default
		uam.registerHandler(DefaultUserActionManagerCtrl.Category.AUXACTION.getName(), AuxAction.CLOSE_BOOK.getAction(), new UserActionHandler() {
			
			@Override
			public boolean process(UserActionContext ctx) {
				doCloseBook();
				return true;
			}
			
			@Override
			public boolean isEnabled(Book book, Sheet sheet) {
				return book!=null;
			}
		});
		
		
		ss.addEventListener(Events.ON_SHEET_SELECT, new SerializableEventListener<Event>() {
			private static final long serialVersionUID = -6317703235308792786L;

			@Override
			public void onEvent(Event event) throws Exception {
				onSheetSelect();
			}
		});
		
		ss.addEventListener(Events.ON_AFTER_UNDOABLE_MANAGER_ACTION, new SerializableEventListener<Event>() {
			private static final long serialVersionUID = -2428002022759075909L;

			@Override
			public void onEvent(Event event) throws Exception {
				onAfterUndoableManagerAction();
			}
		});
		
		//ZSS-998
		ss.addEventListener(Events.ON_SYNC_FRIEND_FOCUS, new SerializableEventListener<Event>() {
			private static final long serialVersionUID = 1870486146113521339L;

			@Override
			public void onEvent(Event event) throws Exception {
				final SyncFriendFocusEvent fe = (SyncFriendFocusEvent) event;
				onSyncFriendFocus(fe.getInBook(), fe.getInSheet());
			}
		});
		
		if(!DISABLE_BOOKMARK) {
			this.getPage().addEventListener(org.zkoss.zk.ui.event.Events.ON_BOOKMARK_CHANGE,
		         new SerializableEventListener<BookmarkEvent>() {
					private static final long serialVersionUID = 5699364737927805458L;

					@Override
					public void onEvent(BookmarkEvent event) throws Exception {
						String bookmark = null;
						try {
							bookmark = URLDecoder.decode(event.getBookmark(), UTF8);
						} catch (UnsupportedEncodingException e1) {
							log.error(e1.getMessage());
							e1.printStackTrace();
							UiUtil.showWarnMessage("Decoding URL got something wrong: " + event.getBookmark());
						}
	
						if(bookmark.isEmpty()) 
							doOpenNewBook(false);
						else {
							BookInfo bookinfo = getBookInfo(bookmark);
							if(bookinfo != null){
								doLoadBook(bookinfo, null, null, false);
							}else{
								// clean push state's path for incorrect file name
								doOpenNewBook(true);
							}
						}
					}
				}
	        );
		}
		
		// confirmMsgWorkaround is a workaround for confirm message with Cleanup event.
		// we don't need it if we don't use confirm message.
		if(Boolean.valueOf(Library.getProperty("zssapp.warning.save", "true")) == Boolean.TRUE)
			isNeedUnsavedAlert = UnsavedAlertState.STOP;
		else
			confirmMsgWorkaround.setParent(null);
		
		Executions.getCurrent().getDesktop().addListener(new DesktopCleanup(){
			@Override
			public void cleanup(Desktop desktop) throws Exception {
				doCloseBook();
				collaborationInfo.removeUsername(username);
			}
		});
		
		setupUsername(false);
		initBook();
	}
	
	private void setBookmark(String bookmark) {
		if(!DISABLE_BOOKMARK)
			desktop.setBookmark(bookmark);
	}

	private void initBook() throws UnsupportedEncodingException {
		String bookName = null;
		Execution exec = Executions.getCurrent();
		if(bookName == null) {
			bookName = (String)exec.getArg().get("book");			
		}
		if(bookName == null){
			bookName = exec.getParameter("book");
		}
		
		BookInfo bookinfo = getBookInfo(bookName);
		String sheetName = Executions.getCurrent().getParameter("sheet");
		if(bookinfo!=null){
			doLoadBook(bookinfo, null, sheetName, false);
		}else{
			doOpenNewBook(false);
		}
	}
	
	private BookInfo getBookInfo(String bookName) {
			BookInfo bookinfo = null;
			if(!Strings.isBlank(bookName)){
				bookName = bookName.trim();
				for(BookInfo info:BookRepositoryFactory.getInstance().getRepository().list()){
					if(bookName.equals(info.getName())){
						bookinfo = info;
						break;
					}
				}
			}
			return bookinfo;
	}
	
	private void shareBook() {
		if(!isBookSaved()){
			if(UiUtil.isRepositoryReadonly()){
				return;
			}
			
			if(!isBookLoaded()){
				UiUtil.showWarnMessage("Please load a book first before share it");
				return;
			}
			
			String name = loadedBook.getBookName();
			if(isBookSaved()){
				name = "Copy Of "+selectedBookInfo.getName();
			}
			name = BookUtil.appendExtension(name, loadedBook);
			name = BookUtil.suggestFileName(name, loadedBook, BookRepositoryFactory.getInstance().getRepository());
			
			SaveBookAsCtrl.show(new SerializableEventListener<DlgCallbackEvent>(){
				private static final long serialVersionUID = 5953139810992856892L;

				public void onEvent(DlgCallbackEvent event) throws Exception {
					if(SaveBookAsCtrl.ON_SAVE.equals(event.getName())){
						
						String name = (String) event.getData(SaveBookAsCtrl.ARG_NAME);

						try {
							synchronized (bookManager) {		
								if(bookManager.isBookAttached(new SimpleBookInfo(name))) {
									String users = Arrays.toString(CollaborationInfoImpl.getInstance().getUsedUsernames(name).toArray());
									UiUtil.showWarnMessage("File \"" + name + "\" is in used by " + users + ". Please try again.");
								}
								BookInfo info = bookManager.saveBook(new SimpleBookInfo(name), loadedBook);
								doLoadBook(info, null, "", true);
							}
							
							pushAppEvent(AppEvts.ON_SAVED_BOOK, loadedBook);
							updatePageInfo();
							
							ShareBookCtrl.show();
						} catch (IOException e) {
							log.error(e.getMessage(), e);
							UiUtil.showWarnMessage("Can't save the specified book: " + name);
							return;
						}
					}
				}}, name, loadedBook, "Save Book for sharing", "Next");
		} else {
			ShareBookCtrl.show();
		}
		
	}

	private void setupUsername(boolean forceAskUser) {
		setupUsername(forceAskUser, null);
	}
	
	private void setupUsername(final boolean forceAskUser, String message) {
		if (forceAskUser) {
			UsernameCtrl.show(new SerializableEventListener<DlgCallbackEvent>(){
				private static final long serialVersionUID = -6819708673820196683L;

				public void onEvent(DlgCallbackEvent event) throws Exception {
					if(UsernameCtrl.ON_USERNAME_CHANGE.equals(event.getName())){
						String name = (String)event.getData(UsernameCtrl.ARG_NAME);
						if(name.equals(username))
							return;
						
						if(!collaborationInfo.addUsername(name, username)) {
							setupUsername(true, "Conflict, should choose another one...");
							return;
						}
						
						ss.setUserName(username = name);
						saveUsername(username);
						pushAppEvent(AppEvts.ON_AFTER_CHANGED_USERNAME, username);
					}
				}}, username == null ? "" : username, message == null ? "" : message);
		} else {
			// already in cookie
			Cookie[] cookies = ((HttpServletRequest)Executions.getCurrent().getNativeRequest()).getCookies();
			if(cookies != null) {	
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals(ZSS_USERNAME)) {
						username = cookie.getValue();
						break;
					}
				}
			}
			
			String newName = collaborationInfo.getUsername(username);
			
			if(username == null) {
				saveUsername(newName);
			}
			
			username = newName;
			pushAppEvent(AppEvts.ON_AFTER_CHANGED_USERNAME, username);
			ss.setUserName(username);
		}
	}
	
	private void saveUsername(String username)	{
		Cookie cookie = new Cookie(ZSS_USERNAME, username);
		cookie.setMaxAge(Integer.MAX_VALUE);
		((HttpServletResponse) Executions.getCurrent().getNativeResponse()).addCookie(cookie);
	}
	
	private void setBook(Book book, BookInfo info) {
		this.loadedBook = book;
		this.selectedBookInfo = info;
		
		if(book == null) {
			pushAppEvent(AppEvts.ON_CHANGED_FILE_STATE, BookInfo.STATE_EMPTY);
		} else {
			if(info == null)
				pushAppEvent(AppEvts.ON_CHANGED_FILE_STATE, BookInfo.STATE_UNSAVED);
			else
				pushAppEvent(AppEvts.ON_CHANGED_FILE_STATE, BookInfo.STATE_SAVED);			
		}
		
		updateUnsavedAlert(false);
	}
	
	/*package*/ void doImportBook(){
		if(isNeedUnsavedAlert == UnsavedAlertState.ENABLED) {			
			askForUnsavedFile(new AsyncFunction() {
				@Override
				public void invoke() {
					doImportBook0();
				}
			});
		} else
			doImportBook0();
	}
	
	private void doImportBook0(){
		Fileupload.get(1,new SerializableEventListener<UploadEvent>() {
			private static final long serialVersionUID = -8173538106339815887L;

			public void onEvent(UploadEvent event) throws Exception {
				Importer importer = Importers.getImporter();
				Media[] medias = event.getMedias();
				
				if(isBookLoaded())
					doCloseBook(false);
				
				if(medias==null)
					return;
				
				Media[] ms = event.getMedias();
				if(ms.length > 0) {
					Media m = event.getMedias()[0];
					if (m.isBinary()){
						String name = m.getName();
						Book book = importer.imports(m.getStreamData(), name);
						book.setShareScope(EventQueues.APPLICATION);
						
						setBook(book, null);
						collaborationInfo.removeRelationship(username);
						ss.setBook(book);
						setBookmark("");
						initSaveNotification(loadedBook);
						pushAppEvent(AppEvts.ON_LOADED_BOOK, book);
						pushAppEvent(AppEvts.ON_CHANGED_SPREADSHEET, ss);
						updatePageInfo();
						
						return;
					}
				}

				UiUtil.showWarnMessage("Fail to import the specified file" + (ms.length > 0 ? ": " + ms[0].getName() : "."));
			}
		});		
	}
	
	/*package*/ void doOpenNewBook(final boolean renewState){
		if(isBookLoaded()) {			
			doAsyncCloseBook(false, new AsyncFunction() {
				
				@Override
				public void invoke() {
					doOpenNewBook0(renewState);
				}
			});
		} else
			doOpenNewBook0(renewState);
	}
	
	private void doOpenNewBook0(boolean renewState) {
		Importer importer = Importers.getImporter();
		try {
			Book book = importer.imports(getClass().getResourceAsStream("/web/zssapp/blank.xlsx"), "New Book.xlsx");
			book.setShareScope(EventQueues.APPLICATION);
			setBook(book, null);
		} catch (IOException e) {
			log.error(e.getMessage(),e);
			UiUtil.showWarnMessage("Can't open a new book");
			return;
		}

		if(renewState)
			setBookmark("");
		
		collaborationInfo.removeRelationship(username);
		ss.setBook(loadedBook);
		initSaveNotification(loadedBook);
		pushAppEvent(AppEvts.ON_CHANGED_FILE_STATE, BookInfo.STATE_UNSAVED);
		pushAppEvent(AppEvts.ON_LOADED_BOOK, loadedBook);
		pushAppEvent(AppEvts.ON_CHANGED_SPREADSHEET, ss);
		updatePageInfo();		
	}
	
	private void updatePageInfo(){
		String name = isBookSaved() ? selectedBookInfo.getName() : isBookLoaded() ? loadedBook.getBookName() : null;
		getPage().setTitle(name!=null?name:"");
	}
	
	private void doCloseBook() {
		doCloseBook(true);
	}
	
	private void doCloseBook(boolean isChangeBookmark){
		removeSaveNotification(loadedBook);
		ss.setBook(null);
		setBook(null, null);
		collaborationInfo.removeRelationship(username);
		if(isChangeBookmark)
			setBookmark("");
		pushAppEvent(AppEvts.ON_CLOSED_BOOK,null);
		pushAppEvent(AppEvts.ON_CHANGED_SPREADSHEET,ss);
		updatePageInfo();
	}
	
	//ZSS-697
	private void doAsyncCloseBook(final boolean isChangeBookmark, final AsyncFunction callback){
		if(isNeedUnsavedAlert == UnsavedAlertState.ENABLED) {
			askForUnsavedFile(new AsyncFunction() {
				@Override
				public void invoke() {					
					doAsyncCloseBook0(isChangeBookmark, callback);
				}
			});
		} else {
			doAsyncCloseBook0(isChangeBookmark, callback);
		}
	}
	
	private void askForUnsavedFile(final AsyncFunction yesCallback) {
		if(yesCallback == null) 
			return;
		
		Messagebox.show(UNSAVED_MESSAGE, "ZK Spreadsheet", 
				Messagebox.OK + Messagebox.CANCEL, Messagebox.INFORMATION, 
				new SerializableEventListener<Event>() {
			private static final long serialVersionUID = -7373178956047605810L;

			@Override
			public void onEvent(Event event) throws Exception {
				if(event.getData().equals(Messagebox.OK)) {
					yesCallback.invoke();
				}
			}
		});
	}
	
	private void doAsyncCloseBook0(boolean isChangeBookmark, AsyncFunction callback) {
		doCloseBook(isChangeBookmark);
		if(callback != null)
			callback.invoke();
	}
	
	/*package*/ void onSheetSelect(){
		pushAppEvent(AppEvts.ON_CHANGED_SPREADSHEET,ss);
	}
	
	/*package*/ void onAfterUndoableManagerAction(){
		pushAppEvent(AppEvts.ON_UPDATE_UNDO_REDO,ss);
	}
	
	//ZSS-998
	/*package*/ void onSyncFriendFocus(Collection<Focus> inBook, Collection<Focus> inSheet) {
		if (inBook.isEmpty()) {
			usersPopContent.setContent("<span style=\"color:#5f5f5f;font-size:12px;font-weight:400;"
					+"font-family:\"Segoe UI\", Tahoma, Thonburi, Arial, Verdana, sans-serif;\">"
						+"(empty)</span>");
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("<ul style=\"background-clip:padding-box;padding:0;"
					+ "margin:8px 10px;display:block;min-width:16px;font-size:12px;"
					+"font-family:\"Segoe UI\", Tahoma, Thonburi, Arial, Verdana, sans-serif;\">");
		for (Focus focus : inBook) {
			sb.append("<li style=\"display:list-item;list-style:none;font-size:12px;margin:2px 0 0;\">"
						+ "<div style=\"white-space:nowrap;height:18px;padding:0;font-size:12px;font-weight:400;color:#5f5f5f;\">" 
							+ "<i style=\"display:inline-block;width:4px;height:14px;float:left;background-color:" 
	                      		+ focus.getColor() + ";color:#ffffff;\"></i>" 
							+ "<div style=\"overflow:hidden;text-overflow:ellipsis;white-space:nowrap;padding-left:5px;\">"
	                      		+ focus.getName() + "</div>" 
						+ "</div>" + 
	                  "</li>");
		}
		sb.append("</ul>");
		usersPopContent.setContent(sb.toString());
	}
	
	/*package*/ void doSaveBook(boolean close){
		if(UiUtil.isRepositoryReadonly()){
			return;
		}
		if(!isBookLoaded()){
			UiUtil.showWarnMessage("Please load a book first before save it");
			return;
		}
		if(!isBookSaved()){
			doSaveBookAs(close);
			return;
		}
		
		try {
			bookManager.saveBook(selectedBookInfo, loadedBook);
			pushAppEvent(AppEvts.ON_CHANGED_FILE_STATE, BookInfo.STATE_SAVED);
			pushAppEvent(AppEvts.ON_SAVED_BOOK, loadedBook);
			if(close){
				doCloseBook();
			}else{
				updatePageInfo();
			}
		} catch (IOException e) {
			log.error(e.getMessage(),e);
			UiUtil.showWarnMessage("Can't save the specified book: " + selectedBookInfo.getName());
			return;
		}
	}

	private boolean isBookSaved() {
		return selectedBookInfo != null;
	}


	
	private void doSaveBookAs(final boolean close){
		if(UiUtil.isRepositoryReadonly()){
			return;
		}
		if(!isBookLoaded()){
			UiUtil.showWarnMessage("Please load a book first before save it");
			return;
		}
		
		String name = loadedBook.getBookName();
		if(isBookSaved()){
			name = "Copy Of "+selectedBookInfo.getName();
		}
		name = BookUtil.appendExtension(name, loadedBook);
		name = BookUtil.suggestFileName(name, loadedBook, BookRepositoryFactory.getInstance().getRepository());
		
		SaveBookAsCtrl.show(new SerializableEventListener<DlgCallbackEvent>(){
			private static final long serialVersionUID = 3378482725465871522L;

			public void onEvent(DlgCallbackEvent event) throws Exception {
				if(SaveBookAsCtrl.ON_SAVE.equals(event.getName())){
					
					String name = (String)event.getData(SaveBookAsCtrl.ARG_NAME);

					try {
						synchronized (bookManager) {		
							if(bookManager.isBookAttached(new SimpleBookInfo(name))) {
								String users = Arrays.toString(CollaborationInfoImpl.getInstance().getUsedUsernames(name).toArray());
								UiUtil.showWarnMessage("File \"" + name + "\" is in used by " + users + ". Please try again.");
							}
							BookInfo info = bookManager.saveBook(new SimpleBookInfo(name), loadedBook);
							doLoadBook(info, null, "", true);
						}
						
						pushAppEvent(AppEvts.ON_SAVED_BOOK,loadedBook);
						if(close){
							doCloseBook(false);
						}else{
							updatePageInfo();
						}
					} catch (IOException e) {
						log.error(e.getMessage(),e);
						UiUtil.showWarnMessage("Can't save the specified book: " + name);
						return;
					}
				}
			}},name, loadedBook);
	}
	
	private void doLoadBook(BookInfo info,Book book,String sheetName, boolean renewState){
		if(book==null){
			try {
				book = bookManager.readBook(info);
			}catch (IOException e) {
				log.error(e.getMessage(),e);
				UiUtil.showWarnMessage("Can't load the specified book: " + info.getName());
				return;
			}
		}
		
		if(isBookLoaded())
			doCloseBook(false);
		
		setBook(book, info);
		collaborationInfo.setRelationship(username, book);
		ss.setBook(loadedBook);
		if(!Strings.isBlank(sheetName)){
			if(loadedBook.getSheet(sheetName)!=null){
				ss.setSelectedSheet(sheetName);
			}
		}
		
		if(renewState) {
			try {
				setBookmark(Encodes.encodeURI(selectedBookInfo.getName()));
			} catch (UnsupportedEncodingException e) {
				log.error(e.getMessage());
				e.printStackTrace();
				UiUtil.showWarnMessage("Encoding URL got something wrong: " + selectedBookInfo.getName());
			}			
		}
		
		initSaveNotification(book);
		pushAppEvent(AppEvts.ON_LOADED_BOOK,loadedBook);
		pushAppEvent(AppEvts.ON_CHANGED_SPREADSHEET,ss);
		updatePageInfo();
	}
	
	private void initSaveNotification(Book book) {
		if(book == null || !"EE".equals(Version.getEdition()))
			return;

		dirtyChangeEventListener = new ModelEventListener() {
			private static final long serialVersionUID = -281657389731703778L;

			@Override
			public void onEvent(final ModelEvent event) {
				if(event.getName().equals(ModelEvents.ON_MODEL_DIRTY_CHANGE)) {
					//ZSS-970: a new thread is used to skip blocking by Executions.activate()
					new Thread(new Runnable() {
						@Override
						public void run() {
							if(Executions.getCurrent() == null) { // in case of background thread
								try {
									Executions.activate(AppCtrl.this.desktop);
									try {
										if( event.getData(ModelEvents.PARAM_CUSTOM_DATA).equals(Boolean.FALSE))
											pushAppEvent(AppEvts.ON_CHANGED_FILE_STATE, BookInfo.STATE_SAVED);
										else
											updateUnsavedAlert(true);
									} finally {
										Executions.deactivate(AppCtrl.this.desktop);
									}
								} catch (DesktopUnavailableException e) {
									throw new RuntimeException("The server push thread interrupted", e);
								} catch (InterruptedException e) {
									throw new RuntimeException("The server push thread interrupted", e);
								}
							} else {
								if( event.getData(ModelEvents.PARAM_CUSTOM_DATA).equals(Boolean.FALSE))
									pushAppEvent(AppEvts.ON_CHANGED_FILE_STATE, BookInfo.STATE_SAVED);
								else
									updateUnsavedAlert(true);
							}
						}
					}).start();
					
				}
			}
		};
		
		book.getInternalBook().addEventListener(dirtyChangeEventListener);
	}
	
	//ZSS-897
	private synchronized void updateUnsavedAlert(boolean turnOn) {
		if(isNeedUnsavedAlert == UnsavedAlertState.DISABLED)
			return;
		
		if(turnOn) {
			if(!isBookSaved() && isNeedUnsavedAlert == UnsavedAlertState.STOP) {
				Clients.confirmClose(UNSAVED_MESSAGE);
				isNeedUnsavedAlert = UnsavedAlertState.ENABLED;
			}
		} else {
			Clients.confirmClose(null);
			isNeedUnsavedAlert = UnsavedAlertState.STOP;
		}
	}
	
	private void removeSaveNotification(Book book) {
		if(book == null || !"EE".equals(Version.getEdition()))
			return;

		book.getInternalBook().removeEventListener(dirtyChangeEventListener);

		if(Executions.getCurrent() == null) { // in case of background thread
			try {
				Executions.activate(AppCtrl.this.desktop);
				try {
					pushAppEvent(AppEvts.ON_CHANGED_FILE_STATE, BookInfo.STATE_EMPTY);
				} finally {
					Executions.deactivate(AppCtrl.this.desktop);
				}
			} catch (DesktopUnavailableException e) {
				throw new RuntimeException("The server push thread interrupted", e);
			} catch (InterruptedException e) {
				throw new RuntimeException("The server push thread interrupted", e);
			}
		} else {
			pushAppEvent(AppEvts.ON_CHANGED_FILE_STATE, BookInfo.STATE_EMPTY);
		}
	}

	private void doOpenManageBook(){
		if (isNeedUnsavedAlert == UnsavedAlertState.ENABLED) {
			askForUnsavedFile(new AsyncFunction() {
				@Override
				public void invoke() {
					doOpenManageBook0();
				}
			});
		} else
			doOpenManageBook0();
	}
	
	private void doOpenManageBook0() {
		OpenManageBookCtrl.show(new SerializableEventListener<DlgCallbackEvent>(){
			private static final long serialVersionUID = 7753635062865984294L;

			public void onEvent(DlgCallbackEvent event) throws Exception {
				if(OpenManageBookCtrl.ON_OPEN.equals(event.getName())){					
					BookInfo info = (BookInfo)event.getData(OpenManageBookCtrl.ARG_BOOKINFO);
					Book book = (Book)event.getData(OpenManageBookCtrl.ARG_BOOK);
					doLoadBook(info, book, null, true);
				}
			}});
	}
	
	private void doExportBook(){
		if(!isBookLoaded()){
			UiUtil.showWarnMessage("Please load a book first before export it");
			return;
		}
		String name = BookUtil.suggestFileName(loadedBook);
		File file;
		try {
			BookType bookType = loadedBook.getType();
			file = BookUtil.saveBookToWorkingFolder(loadedBook, bookType == null ? null : bookType.toString().toLowerCase());
			//default Excel exporter exports XLSX 
			Filedownload.save(new AMedia(name, null, "application/vnd.ms-excel.12", file, true));
		} catch (IOException e) {
			log.error(e.getMessage(),e);
			UiUtil.showWarnMessage("Can't export the book");
		}
	}

	private boolean isBookLoaded() {
		return loadedBook != null;
	}
	
	/*package*/ void doExportPdf(){
		if(!isBookLoaded()){
			UiUtil.showWarnMessage("Please load a book first before export it");
			return;
		}
		String name = BookUtil.suggestPdfName(loadedBook);
		File file;
		try {
			file = BookUtil.saveBookToWorkingFolder(loadedBook,"pdf");
			Filedownload.save(new AMedia(name, null, "application/pdf", file, true));
		} catch (IOException e) {
			log.error(e.getMessage(),e);
			UiUtil.showWarnMessage("Can't export the book: " + e.getMessage());
		}
	}
	
	@Override
	protected void onAppEvent(String event,Object data){
		//menu
		if(AppEvts.ON_NEW_BOOK.equals(event)){
			doOpenNewBook(true);
		}else if(AppEvts.ON_SAVE_BOOK.equals(event)){
			doSaveBook(false);
		}else if(AppEvts.ON_SAVE_BOOK_AS.equals(event)){
			doSaveBookAs(false);
		}else if(AppEvts.ON_SAVE_CLOSE_BOOK.equals(event)){
			doSaveBook(true);
		}else if(AppEvts.ON_CLOSE_BOOK.equals(event)){
			doAsyncCloseBook(true, null);
		}else if(AppEvts.ON_OPEN_MANAGE_BOOK.equals(event)){
			doOpenManageBook();
		}else if(AppEvts.ON_IMPORT_BOOK.equals(event)){
			doImportBook();
		}else if(AppEvts.ON_EXPORT_BOOK.equals(event)){
			doExportBook();
		}else if(AppEvts.ON_EXPORT_BOOK_PDF.equals(event)){
			doExportPdf();
		}else if(AppEvts.ON_TOGGLE_FORMULA_BAR.equals(event)){
			doToggleFormulabar();
		}else if(AppEvts.ON_FREEZE_PNAEL.equals(event)){
			AreaRef sel = ss.getSelection();
			doFreeze(sel.getRow(),sel.getColumn());
		}else if(AppEvts.ON_UNFREEZE_PANEL.equals(event)){
			doFreeze(0,0);
		}else if(AppEvts.ON_FREEZE_ROW.equals(event)){
			doFreeze(((Integer)data),ss.getSelectedSheet().getColumnFreeze());
		}else if(AppEvts.ON_FREEZE_COLUMN.equals(event)){
			doFreeze(ss.getSelectedSheet().getRowFreeze(),((Integer)data));
		}else if(AppEvts.ON_UNDO.equals(event)){
			doUndo();
		}else if(AppEvts.ON_REDO.equals(event)){
			doRedo();
		} else if (AppEvts.ON_INSERT_PICTURE.equals(event)) {
			doInsertPicture();
		} else if (AppEvts.ON_INSERT_CHART.equals(event)) {
			doInsertChart((String) data);
		} else if (AppEvts.ON_INSERT_HYPERLINK.equals(event)) {
			doInsertHyperlink();
		} else if (AppEvts.ON_CHANGED_USERNAME.equals(event)) {
			setupUsername(true);
		} else if (AppEvts.ON_SHARE_BOOK.equals(event)) {
			shareBook();
		}
	}

	private void doUndo() {
		UndoableActionManager uam = ss.getUndoableActionManager();
		if(uam.isUndoable()){
			uam.undoAction();
		}
	}
	private void doRedo() {
		UndoableActionManager uam = ss.getUndoableActionManager();
		if(uam.isRedoable()){
			uam.redoAction();
		}
	} 
	
	private void doFreeze(int row, int column) {
		Ranges.range(ss.getSelectedSheet()).setFreezePanel(row, column);
		pushAppEvent(AppEvts.ON_CHANGED_SPREADSHEET,ss);
		
		//workaround before http://tracker.zkoss.org/browse/ZSS-390 fix
		AreaRef sel = ss.getSelection();
		row = row<0?sel.getRow():row;
		column = column<0?sel.getColumn():column;
		ss.setSelection(new AreaRef(row,column,row,column));
	}

	private void doToggleFormulabar() {
		ss.setShowFormulabar(!ss.isShowFormulabar());
		pushAppEvent(AppEvts.ON_CHANGED_SPREADSHEET,ss);
	}
	
	private void doInsertPicture() {
		Fileupload.get(1,new SerializableEventListener<UploadEvent>() {
			private static final long serialVersionUID = -3555918387396107106L;

			public void onEvent(UploadEvent event) throws Exception {
				Media media = event.getMedia();
				if(media == null){
					return;
				}
				if(!(media instanceof AImage) || SheetOperationUtil.getPictureFormat((AImage)media)==null){
					UiUtil.showWarnMessage(Labels.getLabel("zss.actionhandler.msg.cant_support_file", new Object[]{media==null?"":media.getName()}));
					return;
				}
				final Sheet sheet = ss.getSelectedSheet();
				final AreaRef selection = ss.getSelection();
				Range range = Ranges.range(sheet, selection.getRow(), selection.getColumn(), selection.getLastRow(), selection.getLastColumn());
				
				SheetOperationUtil.addPicture(range,(AImage)media);
				
				return;
			}
		});
	}
	
	private void doInsertChart(String type) {
		AreaRef selection = ss.getSelection();
		Range range = Ranges.range(ss.getSelectedSheet(), selection.getRow(),
				selection.getColumn(), selection.getLastRow(),
				selection.getLastColumn());
		SheetAnchor anchor = SheetOperationUtil.toChartAnchor(range);
		SheetOperationUtil.addChart(range,anchor, toChartType(type), Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
	}
	
	private Chart.Type toChartType(String type) {
		Chart.Type chartType;
		if ("ColumnChart".equals(type)) {
			chartType = Chart.Type.COLUMN;
		} else if ("ColumnChart3D".equals(type)) {
			chartType = Chart.Type.COLUMN_3D;
		} else if ("LineChart".equals(type)) {
			chartType = Chart.Type.LINE;
		} else if ("LineChart3D".equals(type)) {
			chartType = Chart.Type.LINE_3D;
		} else if ("PieChart".equals(type)) {
			chartType = Chart.Type.PIE;
		} else if ("PieChart3D".equals(type)) {
			chartType = Chart.Type.PIE_3D;
		} else if ("BarChart".equals(type)) {
			chartType = Chart.Type.BAR;
		} else if ("BarChart3D".equals(type)) {
			chartType = Chart.Type.BAR_3D;
		} else if ("AreaChart".equals(type)) {
			chartType = Chart.Type.AREA;
		} else if ("ScatterChart".equals(type)) {
			chartType = Chart.Type.SCATTER;
		} else if ("DoughnutChart".equals(type)) {
			chartType = Chart.Type.DOUGHNUT;
		} else {
			chartType = Chart.Type.LINE;
		}
		return chartType;
	}
	
	private void doInsertHyperlink() {
		final Sheet sheet = ss.getSelectedSheet();
		final AreaRef selection = ss.getSelection();
		final Range range = Ranges.range(sheet, selection);
		Hyperlink link = range.getCellHyperlink();
		String display = link == null ? range.getCellFormatText():link.getLabel();
		String address = link == null ? null:link.getAddress();
		HyperlinkCtrl.show(new SerializableEventListener<DlgCallbackEvent>(){
			private static final long serialVersionUID = -2571984995170497501L;

			public void onEvent(DlgCallbackEvent event) throws Exception {
				if(HyperlinkCtrl.ON_OK.equals(event.getName())){
					final String address = (String) event.getData(HyperlinkCtrl.ARG_ADDRESS);
					final String label = (String) event.getData(HyperlinkCtrl.ARG_DISPLAY);
					CellOperationUtil.applyHyperlink(range, HyperlinkType.URL, address, label);
				}
			}}, address, display);
	}
	
	interface AsyncFunction {
		public void invoke();
	}
	
	enum UnsavedAlertState {
		DISABLED, ENABLED, STOP
	}
}
