/* 
	Purpose:
		
	Description:
		
	History:
		2013/7/10, Created by dennis

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.ui;

import org.ngi.zhighcharts.SimpleExtXYModel;
import org.ngi.zhighcharts.ZHighCharts;
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
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.DesktopCleanup;
import org.zkoss.zss.api.*;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Book.BookType;
import org.zkoss.zss.api.model.Chart;
import org.zkoss.zss.api.model.Hyperlink;
import org.zkoss.zss.api.model.Hyperlink.HyperlinkType;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.app.BookInfo;
import org.zkoss.zss.app.BookManager;
import org.zkoss.zss.app.BookRepository;
import org.zkoss.zss.app.CollaborationInfo;
import org.zkoss.zss.app.CollaborationInfo.CollaborationEvent;
import org.zkoss.zss.app.CollaborationInfo.CollaborationEventListener;
import org.zkoss.zss.app.impl.BookManagerImpl;
import org.zkoss.zss.app.impl.CollaborationInfoImpl;
import org.zkoss.zss.app.repository.BookRepositoryFactory;
import org.zkoss.zss.app.repository.impl.BookUtil;
import org.zkoss.zss.app.repository.impl.SimpleBookInfo;
import org.zkoss.zss.app.ui.dlg.*;
import org.zkoss.zss.model.*;
import org.zkoss.zss.model.impl.AbstractBookAdv;
import org.zkoss.zss.model.impl.AbstractSheetAdv;
import org.zkoss.zss.model.impl.Bucket;
import org.zkoss.zss.model.impl.SheetImpl;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.ui.*;
import org.zkoss.zss.ui.Version;
import org.zkoss.zss.ui.event.Events;
import org.zkoss.zss.ui.event.SheetSelectEvent;
import org.zkoss.zss.ui.event.SyncFriendFocusEvent;
import org.zkoss.zss.ui.impl.DefaultUserActionManagerCtrl;
import org.zkoss.zss.ui.impl.Focus;
import org.zkoss.zss.ui.sys.UndoableActionManager;
import org.zkoss.zul.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author dennis
 *
 */
public class AppCtrl extends CtrlBase<Component> {
    public static final String ZSS_USERNAME = "zssUsername";
    private static final Log log = Log.lookup(AppCtrl.class);
    private static final long serialVersionUID = 1L;
    private static final String UNSAVED_MESSAGE = "Do you want to leave this book without save??";
    private static final String UTF8 = "UTF-8";
    private static final boolean DISABLE_BOOKMARK = Boolean.valueOf(Library.getProperty("zssapp.bookmark.disable", "false"));


    private static BookRepository repo = BookRepositoryFactory.getInstance().getRepository();
    private static CollaborationInfo collaborationInfo = CollaborationInfoImpl.getInstance();
    private static BookManager bookManager = BookManagerImpl.getInstance(repo);

    static {
        collaborationInfo.addEvent(new CollaborationEventListener() {
            @Override
            public void onEvent(CollaborationEvent event) {
                if (event.getType() == CollaborationEvent.Type.BOOK_EMPTY)
                    try {
                        BookInfo info = null;
                        String bookName = (String) event.getValue();
                        for (BookInfo bookInfo : repo.list()) {
                            if (bookInfo.getName().equals(bookName))
                                info = bookInfo;
                        }

                        if (info != null)
                            bookManager.detachBook(info);
                    } catch (IOException e) {
                        log.error(e.getMessage());
                        e.printStackTrace();
                        UiUtil.showWarnMessage("Can't detach book: " + event.getValue());
                    }
            }
        });
    }

    @Wire
    Spreadsheet ss;
    @Wire
    Script confirmMsgWorkaround;
    @Wire
    Script gaScript;
    @Wire
    Html usersPopContent; //ZSS-998
    @Wire
    Window mainWin;

    BookInfo selectedBookInfo;
    Book loadedBook;
    Desktop desktop = Executions.getCurrent().getDesktop();
    private ModelEventListener dirtyChangeEventListener;
    private String username;
    private UnsavedAlertState isNeedUnsavedAlert = UnsavedAlertState.DISABLED;

    // Basic column

    private ZHighCharts chartComp25;

    private SimpleExtXYModel dataChartModel25;

    private Map<String,Bucket<String>> navSBucketMap = new HashMap<String,Bucket<String>>();

    private Map<String,Integer> navSBucketLevel = new HashMap<String,Integer>();

    // Stacked and grouped column

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    @Wire
    private Tree treeBucket;

    @Wire
    private Selectbox colSelectbox;

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
        if (!readonly) {
            uam.setHandler(DefaultUserActionManagerCtrl.Category.AUXACTION.getName(), AuxAction.SAVE_BOOK.getAction(), new UserActionHandler() {

                @Override
                public boolean process(UserActionContext ctx) {
                    doSaveBook(false);
                    return true;
                }

                @Override
                public boolean isEnabled(Book book, Sheet sheet) {
                    return book != null;
                }
            });
        }
        if (isEE) {
            uam.setHandler(DefaultUserActionManagerCtrl.Category.AUXACTION.getName(), AuxAction.EXPORT_PDF.getAction(), new UserActionHandler() {

                @Override
                public boolean process(UserActionContext ctx) {
                    doExportPdf();
                    return true;
                }

                @Override
                public boolean isEnabled(Book book, Sheet sheet) {
                    return book != null;
                }
            });

            // ZSS-940 remove GA code for EE customer
            Object evalOnly = Executions.getCurrent().getDesktop().getWebApp().getAttribute("Evaluation Only");
            if (!(evalOnly == null ? false : (Boolean) evalOnly))
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
                return book != null;
            }
        });


        ss.addEventListener(Events.ON_SHEET_SELECT, new SerializableEventListener<Event>() {
            private static final long serialVersionUID = -6317703235308792786L;

            @Override
            public void onEvent(Event event) {
                onSheetSelect();

                createNavS((SheetImpl) ((SheetSelectEvent) event).getSheet().getInternalSheet());
            }
        });

        ss.addEventListener(Events.ON_AFTER_UNDOABLE_MANAGER_ACTION, new SerializableEventListener<Event>() {
            private static final long serialVersionUID = -2428002022759075909L;

            @Override
            public void onEvent(Event event) {
                onAfterUndoableManagerAction();
            }
        });

        //ZSS-998
        ss.addEventListener(Events.ON_SYNC_FRIEND_FOCUS, new SerializableEventListener<Event>() {
            private static final long serialVersionUID = 1870486146113521339L;

            @Override
            public void onEvent(Event event) {
                final SyncFriendFocusEvent fe = (SyncFriendFocusEvent) event;
                onSyncFriendFocus(fe.getInBook(), fe.getInSheet());
            }
        });

        if (!DISABLE_BOOKMARK) {
            this.getPage().addEventListener(org.zkoss.zk.ui.event.Events.ON_BOOKMARK_CHANGE,
                    new SerializableEventListener<BookmarkEvent>() {
                        private static final long serialVersionUID = 5699364737927805458L;

                        @Override
                        public void onEvent(BookmarkEvent event) {
                            String bookmark = null;
                            try {
                                bookmark = URLDecoder.decode(event.getBookmark(), UTF8);
                            } catch (UnsupportedEncodingException e1) {
                                log.error(e1.getMessage());
                                e1.printStackTrace();
                                UiUtil.showWarnMessage("Decoding URL got something wrong: " + event.getBookmark());
                            }

                            if (bookmark.isEmpty())
                                doOpenNewBook(false);
                            else {
                                BookInfo bookinfo = getBookInfo(bookmark);
                                if (bookinfo != null) {
                                    doLoadBook(bookinfo, null, null, false);
                                } else {
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
        if (Boolean.valueOf(Library.getProperty("zssapp.warning.save", "true")) == Boolean.TRUE)
            isNeedUnsavedAlert = UnsavedAlertState.STOP;
        else
            confirmMsgWorkaround.setParent(null);

        Executions.getCurrent().getDesktop().addListener(new DesktopCleanup() {
            @Override
            public void cleanup(Desktop desktop) {
                doCloseBook();
                collaborationInfo.removeUsername(username);
            }
        });

        setupUsername(false);
        initBook();
    }

    private void createNavS(SheetImpl currentSheet) {
        if(currentSheet.getEndRowIndex() > 1000000)
            return;

        try {
            if(currentSheet.getEndRowIndex()<1) {
                treeBucket.setModel(new DefaultTreeModel<Bucket<String>>(new BucketTreeNode<Bucket<String>>(null,new BucketTreeNodeCollection<Bucket<String>>())));
                return;
            }


            ss.setNavSBuckets(currentSheet.getDataModel().createNavS(currentSheet, 0, 0));
            createNavSTree(ss.getNavSBuckets());
            updateColModel(currentSheet);

            currentSheet.fullRefresh();


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void setBookmark(String bookmark) {
        if (!DISABLE_BOOKMARK)
            desktop.setBookmark(bookmark);
    }

    private void initBook() {
        String bookName = null;
        Execution exec = Executions.getCurrent();
        if (bookName == null) {
            bookName = (String) exec.getArg().get("book");
        }
        if (bookName == null) {
            bookName = exec.getParameter("book");
        }

        BookInfo bookinfo = getBookInfo(bookName);
        String sheetName = Executions.getCurrent().getParameter("sheet");

		Cookie[] cookies = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(ZSS_USERNAME)) {
					username = cookie.getValue();
					break;
				}
			}
		}
        if (bookinfo != null) {
            doLoadBook(bookinfo, null, sheetName, false);
        } else {
            doOpenNewBook(false);
        }
    }

    private BookInfo getBookInfo(String bookName) {
        BookInfo bookinfo = null;
        if (!Strings.isBlank(bookName)) {
            bookName = bookName.trim();
            for (BookInfo info : BookRepositoryFactory.getInstance().getRepository().list()) {
                if (bookName.equals(info.getName())) {
                    bookinfo = info;
                    break;
                }
            }
        }
        return bookinfo;
    }

    private void shareBook() {
        if (!isBookSaved()) {
            if (UiUtil.isRepositoryReadonly()) {
                return;
            }

            if (!isBookLoaded()) {
                UiUtil.showWarnMessage("Please load a book first before share it");
                return;
            }

            String name = loadedBook.getBookName();
            if (isBookSaved()) {
                name = "Copy Of " + selectedBookInfo.getName();
            }
            name = BookUtil.appendExtension(name, loadedBook);
            name = BookUtil.suggestFileName(name, loadedBook, BookRepositoryFactory.getInstance().getRepository());

            SaveBookAsCtrl.show(new SerializableEventListener<DlgCallbackEvent>() {
                private static final long serialVersionUID = 5953139810992856892L;

                public void onEvent(DlgCallbackEvent event) {
                    if (SaveBookAsCtrl.ON_SAVE.equals(event.getName())) {

                        String name = (String) event.getData(SaveBookAsCtrl.ARG_NAME);

                        try {
                            synchronized (bookManager) {
                                if (bookManager.isBookAttached(new SimpleBookInfo(name))) {
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
                }
            }, name, loadedBook, "Save Book for sharing", "Next");
        } else {
            ShareBookCtrl.show();
        }

    }

    private void logout() {
        String name = "guest";
        if (name.equals(username))
            return;

        if (!collaborationInfo.addUsername(name, username)) {
            setupUsername(true, "Conflict, should choose another one...");
            return;
        }

        ss.setUserName(username = name);
        saveUsername(username);
        pushAppEvent(AppEvts.ON_AFTER_CHANGED_USERNAME, username);
    }

    private void register() {
        String message = null;
        RegisterCtrl.show(new SerializableEventListener<DlgCallbackEvent>() {
            private static final long serialVersionUID = -6819708673820196683L;

            public void onEvent(DlgCallbackEvent event) {
                if (RegisterCtrl.ON_USERNAME_CHANGE.equals(event.getName())) {
                    String name = (String) event.getData(UsernameCtrl.ARG_NAME);
                    if (name.equals(username))
                        return;

                    if (!collaborationInfo.addUsername(name, username)) {
                        setupUsername(true, "Conflict, should choose another one...");
                        return;
                    }

                    ss.setUserName(username = name);
                    saveUsername(username);
                    pushAppEvent(AppEvts.ON_AFTER_CHANGED_USERNAME, username);
                }
            }
        }, username == null ? "guest" : username, message == null ? "" : message);
    }

    private void setupUsername(boolean forceAskUser) {
        setupUsername(forceAskUser, null);
    }

    private void setupUsername(final boolean forceAskUser, String message) {
        if (forceAskUser) {
            UsernameCtrl.show(new SerializableEventListener<DlgCallbackEvent>() {
                private static final long serialVersionUID = -6819708673820196683L;

                public void onEvent(DlgCallbackEvent event) {
                    if (UsernameCtrl.ON_USERNAME_CHANGE.equals(event.getName())) {
                        String name = (String) event.getData(UsernameCtrl.ARG_NAME);
                        if (name.equals(username))
                            return;

                        if (!collaborationInfo.addUsername(name, username)) {
                            setupUsername(true, "Conflict, should choose another one...");
                            return;
                        }

                        ss.setUserName(username = name);
                        saveUsername(username);
                        pushAppEvent(AppEvts.ON_AFTER_CHANGED_USERNAME, username);
                    }
                }
            }, username == null ? "guest" : username, message == null ? "" : message);
        } else {
            // already in cookie
            Cookie[] cookies = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(ZSS_USERNAME)) {
                        username = cookie.getValue();
                        collaborationInfo.addUsername(username, username);
                        saveUsername(username);
                        pushAppEvent(AppEvts.ON_AFTER_CHANGED_USERNAME, username);
                        ss.setUserName(username);
                        break;
                    }
                }
            } else {
                String newName = collaborationInfo.getUsername(username);
                collaborationInfo.addUsername(newName, newName);
                if (username == null) {
                    saveUsername(newName);
                }

                username = newName;
                pushAppEvent(AppEvts.ON_AFTER_CHANGED_USERNAME, username);
                ss.setUserName(username);
            }

        }
    }

    private void saveUsername(String username) {
        Cookie cookie = new Cookie(ZSS_USERNAME, username);
        cookie.setMaxAge(Integer.MAX_VALUE);
        ((HttpServletResponse) Executions.getCurrent().getNativeResponse()).addCookie(cookie);
    }

    private void setBook(Book book, BookInfo info) {
        this.loadedBook = book;
        this.selectedBookInfo = info;

        if (book == null) {
            pushAppEvent(AppEvts.ON_CHANGED_FILE_STATE, BookInfo.STATE_EMPTY);
        } else {
            if (info == null)
                pushAppEvent(AppEvts.ON_CHANGED_FILE_STATE, BookInfo.STATE_UNSAVED);
            else
                pushAppEvent(AppEvts.ON_CHANGED_FILE_STATE, BookInfo.STATE_SAVED);
        }

        updateUnsavedAlert(false);
    }

    /*package*/ void doImportBook() {
        if (isNeedUnsavedAlert == UnsavedAlertState.ENABLED) {
            askForUnsavedFile(new AsyncFunction() {
                @Override
                public void invoke() {
                    doImportBook0();
                }
            });
        } else
            doImportBook0();
    }

    private void doImportBook0() {
        Fileupload.get(1, new SerializableEventListener<UploadEvent>() {
            private static final long serialVersionUID = -8173538106339815887L;

            public void onEvent(UploadEvent event) throws Exception {
                Importer importer = Importers.getImporter();
                Media[] medias = event.getMedias();

                if (medias == null)
                    return;

                Media[] ms = event.getMedias();
                if (ms.length > 0) {
                    Media m = event.getMedias()[0];
                    String name = m.getName();
                    if (name.endsWith(".csv") || name.endsWith(".tsv") || name.endsWith(".ssv")) {
                        if (!isBookLoaded())
                            doOpenNewBook0(false);
                        String sheetName = name.substring(0, name.lastIndexOf('.'));
                        char delimiter = name.endsWith(".csv") ? ',' : name.endsWith(".ssv") ? '\t' : ' ';

                        // Create a new Sheet and import file
                        loadedBook.getInternalBook().checkDBSchema();
                        SSheet newSheet = loadedBook.getInternalBook().createSheet(sheetName);

                        newSheet.getDataModel().importSheet(
                                m.isBinary() ? new BufferedReader(new InputStreamReader(m.getStreamData())) :
                                        m.getReaderData(), delimiter, (((SheetImpl) newSheet).getEndRowIndex() > 100000));

                        /*ss.setNavSBuckets(newSheet.getDataModel().navSbuckets);
                        createNavSTree(newSheet.getDataModel().navSbuckets);
                        updateColModel(newSheet);*/
                        createNavS((SheetImpl) newSheet);
                        Messagebox.show("File imported", "DataSpread",
                                Messagebox.OK, Messagebox.INFORMATION, null);

                        initSaveNotification(loadedBook);
                        pushAppEvent(AppEvts.ON_CHANGED_SPREADSHEET, ss);
                        updatePageInfo();
                        ss.setSelectedSheet(sheetName);
                        updateColModel(newSheet);
                        return;
                    }

                    // Create a unique name

                    Date dNow = new Date();
                    SimpleDateFormat ft =
                            new SimpleDateFormat("_yyyyMMdd_hhmmss_S");
                    Book book = importer.imports(m.getStreamData(), name.substring(0, name.lastIndexOf('.'))
                            .concat(ft.format(dNow)));
                    book.setShareScope(EventQueues.APPLICATION);

                    setBook(book, null);
                    collaborationInfo.setRelationship(username, loadedBook);
                    ss.setBook(book);
                    setBookmark("");
                    initSaveNotification(loadedBook);
                    pushAppEvent(AppEvts.ON_LOADED_BOOK, book);
                    pushAppEvent(AppEvts.ON_CHANGED_SPREADSHEET, ss);
                    updatePageInfo();

                    return;
                    //}
                }

                UiUtil.showWarnMessage("Fail to import the specified file" + (ms.length > 0 ? ": " + ms[0].getName() : "."));
            }
        });
    }

    /*package*/ void doOpenNewBook(final boolean renewState) {
        if (isBookLoaded()) {
            doAsyncCloseBook(false, new AsyncFunction() {

                @Override
                public void invoke() {
                    doOpenNewBook0(renewState);
                }
            });
        } else
            doOpenNewBook0(renewState);
    }

    private void doOpenExistingBook(Book book) {
        book.setShareScope(EventQueues.APPLICATION);
        setBook(book, null);
        setBookmark("");
        collaborationInfo.removeRelationship(username);
        ss.setBook(loadedBook);
        initSaveNotification(loadedBook);

        SSheet currentsheet = ss.getSelectedSSheet();

        if(currentsheet.getEndRowIndex()<1)
            treeBucket.setModel(new DefaultTreeModel<Bucket<String>>(new BucketTreeNode<Bucket<String>>(null,new BucketTreeNodeCollection<Bucket<String>>())));

        pushAppEvent(AppEvts.ON_CHANGED_FILE_STATE, BookInfo.STATE_UNSAVED);
        pushAppEvent(AppEvts.ON_LOADED_BOOK, loadedBook);
        pushAppEvent(AppEvts.ON_CHANGED_SPREADSHEET, ss);
        updatePageInfo();


    }

    private void doOpenNewBook0(boolean renewState) {
        try {
            Date dNow = new Date();
            SimpleDateFormat ft =
                    new SimpleDateFormat("yyyyMMdd_hhmmss_S");
            Book book = new org.zkoss.zss.api.model.impl.BookImpl("New Book_".concat(ft.format(dNow)));

            book.setShareScope(EventQueues.APPLICATION);
            setBook(book, null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            UiUtil.showWarnMessage("Can't open a new book");
            return;
        }

        if (renewState)
            setBookmark("");

		collaborationInfo.setRelationship(username, loadedBook);
        ss.setBook(loadedBook);
        initSaveNotification(loadedBook);
        pushAppEvent(AppEvts.ON_CHANGED_FILE_STATE, BookInfo.STATE_UNSAVED);
        pushAppEvent(AppEvts.ON_LOADED_BOOK, loadedBook);
        pushAppEvent(AppEvts.ON_CHANGED_SPREADSHEET, ss);
        updatePageInfo();
    }

    private void updatePageInfo() {
        String name = isBookLoaded() ? loadedBook.getBookName() : null;
        getPage().setTitle(name != null ? name : "");
    }

    private void doCloseBook() {
        doCloseBook(true);
    }

    private void doCloseBook(boolean isChangeBookmark) {
        removeSaveNotification(loadedBook);
        ss.setBook(null);
        setBook(null, null);
        collaborationInfo.removeRelationship(username);
        if (isChangeBookmark)
            setBookmark("");
        pushAppEvent(AppEvts.ON_CLOSED_BOOK, null);
        pushAppEvent(AppEvts.ON_CHANGED_SPREADSHEET, ss);
        updatePageInfo();
    }

    //ZSS-697
    private void doAsyncCloseBook(final boolean isChangeBookmark, final AsyncFunction callback) {
        if (isNeedUnsavedAlert == UnsavedAlertState.ENABLED) {
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
        if (yesCallback == null)
            return;

        Messagebox.show(UNSAVED_MESSAGE, "ZK Spreadsheet",
                Messagebox.OK + Messagebox.CANCEL, Messagebox.INFORMATION,
                new SerializableEventListener<Event>() {
                    private static final long serialVersionUID = -7373178956047605810L;

                    @Override
                    public void onEvent(Event event) {
                        if (event.getData().equals(Messagebox.OK)) {
                            yesCallback.invoke();
                        }
                    }
                });
    }

    private void doAsyncCloseBook0(boolean isChangeBookmark, AsyncFunction callback) {
        doCloseBook(isChangeBookmark);
        if (callback != null)
            callback.invoke();
    }

    /*package*/ void onSheetSelect() {
        pushAppEvent(AppEvts.ON_CHANGED_SPREADSHEET, ss);
    }

    /*package*/ void onAfterUndoableManagerAction() {
        pushAppEvent(AppEvts.ON_UPDATE_UNDO_REDO, ss);
    }

    //ZSS-998
    /*package*/ void onSyncFriendFocus(Collection<Focus> inBook, Collection<Focus> inSheet) {
        if (inBook.isEmpty()) {
            usersPopContent.setContent("<span style=\"color:#5f5f5f;font-size:12px;font-weight:400;"
                    + "font-family:\"Segoe UI\", Tahoma, Thonburi, Arial, Verdana, sans-serif;\">"
                    + "(empty)</span>");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<ul style=\"background-clip:padding-box;padding:0;"
                + "margin:8px 10px;display:block;min-width:16px;font-size:12px;"
                + "font-family:\"Segoe UI\", Tahoma, Thonburi, Arial, Verdana, sans-serif;\">");
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

    /*package*/ void doSaveBook(boolean close) {
        if (UiUtil.isRepositoryReadonly()) {
            return;
        }
        if (!isBookLoaded()) {
            UiUtil.showWarnMessage("Please load a book first before save it");
            return;
        }
        if (!isBookSaved()) {
            doSaveBookAs(close);
            return;
        }

        try {
            bookManager.saveBook(selectedBookInfo, loadedBook);
            pushAppEvent(AppEvts.ON_CHANGED_FILE_STATE, BookInfo.STATE_SAVED);
            pushAppEvent(AppEvts.ON_SAVED_BOOK, loadedBook);
            if (close) {
                doCloseBook();
            } else {
                updatePageInfo();
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            UiUtil.showWarnMessage("Can't save the specified book: " + selectedBookInfo.getName());
            return;
        }
    }

    private boolean isBookSaved() {
        return true;
    }


    private void doSaveBookAs(final boolean close) {
        if (UiUtil.isRepositoryReadonly()) {
            return;
        }
        if (!isBookLoaded()) {
            UiUtil.showWarnMessage("Please load a book first before save it");
            return;
        }

        String name = loadedBook.getBookName();

        SaveBookAsCtrl.show(new SerializableEventListener<DlgCallbackEvent>() {
            private static final long serialVersionUID = 3378482725465871522L;

            public void onEvent(DlgCallbackEvent event) {
                if (SaveBookAsCtrl.ON_SAVE.equals(event.getName())) {
                    updatePageInfo();
                }
            }
        }, name, loadedBook);
    }

    private void doLoadBook(BookInfo info, Book book, String sheetName, boolean renewState) {
        if (book == null) {
            try {
                book = bookManager.readBook(info);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                UiUtil.showWarnMessage("Can't load the specified book: " + info.getName());
                return;
            }
        }

        if (isBookLoaded())
            doCloseBook(false);

        setBook(book, info);
        collaborationInfo.setRelationship(username, book);
        ss.setBook(loadedBook);
        if (!Strings.isBlank(sheetName)) {
            if (loadedBook.getSheet(sheetName) != null) {
                ss.setSelectedSheet(sheetName);
            }
        }

        if (renewState) {
            try {
                setBookmark(Encodes.encodeURI(selectedBookInfo.getName()));
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage());
                e.printStackTrace();
                UiUtil.showWarnMessage("Encoding URL got something wrong: " + selectedBookInfo.getName());
            }
        }

        initSaveNotification(book);
        pushAppEvent(AppEvts.ON_LOADED_BOOK, loadedBook);
        pushAppEvent(AppEvts.ON_CHANGED_SPREADSHEET, ss);
        updatePageInfo();
    }

    private void initSaveNotification(Book book) {
        if (book == null || !"EE".equals(Version.getEdition()))
            return;

        dirtyChangeEventListener = new ModelEventListener() {
            private static final long serialVersionUID = -281657389731703778L;

            @Override
            public void onEvent(final ModelEvent event) {
                if (event.getName().equals(ModelEvents.ON_MODEL_DIRTY_CHANGE)) {
                    //ZSS-970: a new thread is used to skip blocking by Executions.activate()
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (Executions.getCurrent() == null) { // in case of background thread
                                try {
                                    Executions.activate(AppCtrl.this.desktop);
                                    try {
                                        if (event.getData(ModelEvents.PARAM_CUSTOM_DATA).equals(Boolean.FALSE))
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
                                if (event.getData(ModelEvents.PARAM_CUSTOM_DATA).equals(Boolean.FALSE))
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
        if (isNeedUnsavedAlert == UnsavedAlertState.DISABLED)
            return;

        if (turnOn) {
            if (!isBookSaved() && isNeedUnsavedAlert == UnsavedAlertState.STOP) {
                Clients.confirmClose(UNSAVED_MESSAGE);
                isNeedUnsavedAlert = UnsavedAlertState.ENABLED;
            }
        } else {
            Clients.confirmClose(null);
            isNeedUnsavedAlert = UnsavedAlertState.STOP;
        }
    }

    private void removeSaveNotification(Book book) {
        if (book == null || !"EE".equals(Version.getEdition()))
            return;

        book.getInternalBook().removeEventListener(dirtyChangeEventListener);

        if (Executions.getCurrent() == null) { // in case of background thread
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

    private void doOpenManageBook() {
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
        OpenManageBookCtrl.show(new SerializableEventListener<DlgCallbackEvent>() {
            private static final long serialVersionUID = 7753635062865984294L;

            public void onEvent(DlgCallbackEvent event) {
                if (OpenManageBookCtrl.ON_OPEN.equals(event.getName())) {
                    Book book = (Book) event.getData(OpenManageBookCtrl.ARG_BOOK);
                    doOpenExistingBook(book);
                }
            }
        });
    }

    private void doExportBook() {
        if (!isBookLoaded()) {
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
            log.error(e.getMessage(), e);
            UiUtil.showWarnMessage("Can't export the book");
        }
    }

    private boolean isBookLoaded() {
        return loadedBook != null;
    }

    /*package*/ void doExportPdf() {
        if (!isBookLoaded()) {
            UiUtil.showWarnMessage("Please load a book first before export it");
            return;
        }
        String name = BookUtil.suggestPdfName(loadedBook);
        File file;
        try {
            file = BookUtil.saveBookToWorkingFolder(loadedBook, "pdf");
            Filedownload.save(new AMedia(name, null, "application/pdf", file, true));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            UiUtil.showWarnMessage("Can't export the book: " + e.getMessage());
        }
    }

    @Override
    protected void onAppEvent(String event, Object data) {
        //menu
        if (AppEvts.ON_NEW_BOOK.equals(event)) {
            doOpenNewBook(true);
        } else if (AppEvts.ON_SAVE_BOOK.equals(event)) {
            doSaveBook(false);
        } else if (AppEvts.ON_SAVE_BOOK_AS.equals(event)) {
            doSaveBookAs(false);
        } else if (AppEvts.ON_SAVE_CLOSE_BOOK.equals(event)) {
            doSaveBook(true);
        } else if (AppEvts.ON_CLOSE_BOOK.equals(event)) {
            doAsyncCloseBook(true, null);
        } else if (AppEvts.ON_OPEN_MANAGE_BOOK.equals(event)) {
            doOpenManageBook();
        } else if (AppEvts.ON_IMPORT_BOOK.equals(event)) {
            doImportBook();
        } else if (AppEvts.ON_EXPORT_BOOK.equals(event)) {
            doExportBook();
        } else if (AppEvts.ON_EXPORT_BOOK_PDF.equals(event)) {
            doExportPdf();
        } else if (AppEvts.ON_TOGGLE_FORMULA_BAR.equals(event)) {
            doToggleFormulabar();
        } else if (AppEvts.ON_FREEZE_PNAEL.equals(event)) {
            AreaRef sel = ss.getSelection();
            doFreeze(sel.getRow(), sel.getColumn());
        } else if (AppEvts.ON_UNFREEZE_PANEL.equals(event)) {
            doFreeze(0, 0);
        } else if (AppEvts.ON_FREEZE_ROW.equals(event)) {
            doFreeze(((Integer) data), ss.getSelectedSheet().getColumnFreeze());
        } else if (AppEvts.ON_FREEZE_COLUMN.equals(event)) {
            doFreeze(ss.getSelectedSheet().getRowFreeze(), ((Integer) data));
        } else if (AppEvts.ON_UNDO.equals(event)) {
            doUndo();
        } else if (AppEvts.ON_REDO.equals(event)) {
            doRedo();
        } else if (AppEvts.ON_INSERT_PICTURE.equals(event)) {
            doInsertPicture();
        } else if (AppEvts.ON_INSERT_CHART.equals(event)) {
            doInsertChart((String) data);
        } else if (AppEvts.ON_INSERT_HYPERLINK.equals(event)) {
            doInsertHyperlink();
        } else if (AppEvts.ON_CHANGED_USERNAME.equals(event)) {
            setupUsername(true);
        } else if (AppEvts.ON_LOGOUT.equals(event)) {
            logout();
        } else if (AppEvts.ON_REGISTER.equals(event)) {
            register();
        } else if (AppEvts.ON_SHARE_BOOK.equals(event)) {
            shareBook();
        }
    }

    private void doUndo() {
        UndoableActionManager uam = ss.getUndoableActionManager();
        if (uam.isUndoable()) {
            uam.undoAction();
        }
    }

    private void doRedo() {
        UndoableActionManager uam = ss.getUndoableActionManager();
        if (uam.isRedoable()) {
            uam.redoAction();
        }
    }

    private void doFreeze(int row, int column) {
        Ranges.range(ss.getSelectedSheet()).setFreezePanel(row, column);
        pushAppEvent(AppEvts.ON_CHANGED_SPREADSHEET, ss);

        //workaround before http://tracker.zkoss.org/browse/ZSS-390 fix
        AreaRef sel = ss.getSelection();
        row = row < 0 ? sel.getRow() : row;
        column = column < 0 ? sel.getColumn() : column;
        ss.setSelection(new AreaRef(row, column, row, column));
    }

    private void doToggleFormulabar() {
        ss.setShowFormulabar(!ss.isShowFormulabar());
        pushAppEvent(AppEvts.ON_CHANGED_SPREADSHEET, ss);
    }

    private void doInsertPicture() {
        Fileupload.get(1, new SerializableEventListener<UploadEvent>() {
            private static final long serialVersionUID = -3555918387396107106L;

            public void onEvent(UploadEvent event) {
                Media media = event.getMedia();
                if (media == null) {
                    return;
                }
                if (!(media instanceof AImage) || SheetOperationUtil.getPictureFormat((AImage) media) == null) {
                    UiUtil.showWarnMessage(Labels.getLabel("zss.actionhandler.msg.cant_support_file", new Object[]{media == null ? "" : media.getName()}));
                    return;
                }
                final Sheet sheet = ss.getSelectedSheet();
                final AreaRef selection = ss.getSelection();
                Range range = Ranges.range(sheet, selection.getRow(), selection.getColumn(), selection.getLastRow(), selection.getLastColumn());

                SheetOperationUtil.addPicture(range, (AImage) media);

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
        SheetOperationUtil.addChart(range, anchor, toChartType(type), Chart.Grouping.STANDARD, Chart.LegendPosition.RIGHT);
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
        String display = link == null ? range.getCellFormatText() : link.getLabel();
        String address = link == null ? null : link.getAddress();
        HyperlinkCtrl.show(new SerializableEventListener<DlgCallbackEvent>() {
            private static final long serialVersionUID = -2571984995170497501L;

            public void onEvent(DlgCallbackEvent event) {
                if (HyperlinkCtrl.ON_OK.equals(event.getName())) {
                    final String address = (String) event.getData(HyperlinkCtrl.ARG_ADDRESS);
                    final String label = (String) event.getData(HyperlinkCtrl.ARG_DISPLAY);
                    CellOperationUtil.applyHyperlink(range, HyperlinkType.URL, address, label);
                }
            }
        }, address, display);
    }

    enum UnsavedAlertState {
        DISABLED, ENABLED, STOP
    }

    interface AsyncFunction {
        void invoke();
    }

    private BucketTreeNodeCollection<Bucket<String>> childrenBuckets(ArrayList<Bucket<String>> bucketList, int level) {
        BucketTreeNodeCollection<Bucket<String>> dtnc = new BucketTreeNodeCollection<Bucket<String>>();

        for(int i=0;i<bucketList.size();i++) {
            BucketTreeNodeCollection<Bucket<String>> btnc_ = new BucketTreeNodeCollection<Bucket<String>>();
            if(!navSBucketMap.containsKey("ch"+bucketList.get(i).getId())) {
                navSBucketMap.put("ch" + bucketList.get(i).getId(), bucketList.get(i));
                navSBucketLevel.put("ch" + bucketList.get(i).getId(), level);
            }
            if(bucketList.get(i).getChildrenCount()>0) {
                btnc_ = childrenBuckets(bucketList.get(i).getChildren(),level+1);
                dtnc.add(new DefaultTreeNode<Bucket<String>>(bucketList.get(i),btnc_));
            }
            else
            {
                dtnc.add(new DefaultTreeNode<Bucket<String>>(bucketList.get(i)));
            }
        }

        return dtnc;
    }

   private void createNavSTree(ArrayList<Bucket<String>> bucketList) {

        //treeBucket.setAutopaging(true);
        BucketTreeNodeCollection<Bucket<String>> btnc = new BucketTreeNodeCollection<Bucket<String>>();
        navSBucketMap.clear();
        navSBucketLevel.clear();
        btnc = childrenBuckets(bucketList,0);

        treeBucket.setModel(new DefaultTreeModel<Bucket<String>>(new BucketTreeNode<Bucket<String>>(null,btnc)));

        /*for(int i=0;i<bucketList.size();i++)
        {
            System.out.println("Bucket "+(i+1));
            System.out.println("Max: "+bucketList.get(i).getMaxValue());
            System.out.println("Min: "+bucketList.get(i).getMinValue());
            System.out.println("start: "+bucketList.get(i).getStartPos());
            System.out.println("end: "+bucketList.get(i).getEndPos());
            System.out.println("Size: "+bucketList.get(i).getSize());
            System.out.println("children: "+bucketList.get(i).getChildrenCount());
        }*/
    }



    public void onChartsCreate(ZHighCharts chartComp25) {

        //================================================================================

        // Basic column

        //================================================================================

        Bucket<String> currentBucket = navSBucketMap.get(chartComp25.getId());

        String [] colors = {"#F6546A","#C998FD","#FF8247","#B9E4F1","#A99A91","#382755"};
        chartComp25.setType("column");
        //chartComp25.setOptions("{margin:[-30,0,50,30]}");
        //chartComp25.setTitle(currentBucket.getName());
        String xAxisLabels = "";

        chartComp25.setHeight("200px");


        if(currentBucket.getChildrenCount() > 0)
            xAxisLabels = "{categories: ['"+currentBucket.getChildren().get(0).getName()+"'";
        else
            xAxisLabels = "{categories: ['"+currentBucket.getName()+"'";

        for(int i=1;i<currentBucket.getChildrenCount();i++)
            xAxisLabels += ",'"+currentBucket.getChildren().get(i).getName()+"'";

        chartComp25.setxAxisOptions(xAxisLabels+"],"+
                    "labels: {"+
                    "rotation: -45,"+
                    "align: 'right',"+
                    "style: {"+
                    "fontSize: '8px',"+
                    "fontFamily: 'Verdana, sans-serif'"+
                    "}"+
                    "}" +
                "}");

        chartComp25.setyAxisOptions("{ " +
                "min:0" +
                "}");

        //chartComp25.setXAxisTitle("Sub-Categories");
        chartComp25.setYAxisTitle("#Rows");
        chartComp25.setTooltipOptions("{followPointer:true}");
        chartComp25.setTooltipFormatter("function formatTooltip(obj){ " +
                "return '<b>'+obj.x +'</b> has <b>'+ obj.y+'</b> rows';" +
                "}");

        chartComp25.setLegend("{enabled:false}");
        /*
        * "{" +
                "layout: 'vertical'," +
                "backgroundColor: '#FFFFFF'," +
                "align: 'left'," +
                "verticalAlign: 'top'," +
                "x: 100," +
                "y: 70," +
                "floating: true," +
                "shadow: true" +

                "}"
                */
        chartComp25.setPlotOptions(//"["+
                "{" +
                    "column: {" +
                        "color: \'"+colors[navSBucketLevel.get(chartComp25.getId())]+"\',"+
                        "pointPadding: 0.2," +
                        "borderWidth: 0," +
                        "point: {"+
                            "events: {"+
                                "click: function() {"+
                                    "zk.Widget.$('$mainWin').fire('onFocusByChartColumn');"+
                                    "zAu.send(new zk.Event(zk.Widget.$('$mainWin'), 'onFocusByChartColumn',this.category,{toServer:true}));"+
                                "}"+
                            "}"+
                        "}"+
                    "}"+
                "}"
                );

        dataChartModel25 = new SimpleExtXYModel();
        chartComp25.setModel(dataChartModel25);

        for(int i = 0; i < currentBucket.getChildrenCount(); i++)
            dataChartModel25.addValue(currentBucket.getName(), i, currentBucket.getChildren().get(i).getSize());

        if(currentBucket.getChildrenCount()==0)
            dataChartModel25.addValue(currentBucket.getName(),0,currentBucket.getSize());

    }

    private long getDateTime(String date) throws Exception {
        return sdf.parse(date).getTime();

    }

    @Listen("onSelect = #treeBucket")
    public void nodeSelected() {
        DefaultTreeNode<Bucket<String>> selectedNode = treeBucket.getSelectedItem().getValue();

        System.out.println("Name: "+selectedNode.getData().getName());

        int start = selectedNode.getData().getStartPos();
        int end = selectedNode.getData().getEndPos();
        String bucketName = selectedNode.getData().getName();
        ss.focusTo(start+1,0);
    }

    @Listen("onFocusByChartColumn = #mainWin")
    public void onFocusByChartColumn(Event evt)
    {
        System.out.println("Client Screen Size:" + evt.getData());

        String eventData = evt.getData().toString();
        String bucketName = "ch";

        if(eventData.contains("Rows:"))
            bucketName += eventData.split("Rows:")[1].replaceAll("-","_");
        else
            bucketName += eventData.replaceAll(" ","_");

        ss.focusTo(navSBucketMap.get(bucketName).getStartPos()+1,0);


    }


    private void updateColModel(SSheet currentSheet) {

        try {

            CellRegion tableRegion =  new CellRegion(0, 0,//100000,20);
                    0,currentSheet.getEndColumnIndex());

            ArrayList<SCell> result = (ArrayList<SCell>) currentSheet.getCells(tableRegion);

            ArrayList<String> headers = new ArrayList<String>();

            for(int i=0;i<result.size();i++){
                headers.add(result.get(i).getStringValue());
            }
            ListModelList<String> colModel = new ListModelList<String>(headers);
            colSelectbox.setModel(colModel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Listen("onSelect = #colSelectbox")
    public void sort() {
        int index = colSelectbox.getSelectedIndex()+1;

        SSheet currentSheet = ss.getSelectedSSheet();
        try {
            currentSheet.getDataModel().setIndexString("col_"+index);
            currentSheet.clearCache();
            ss.setNavSBuckets(currentSheet.getDataModel().createNavS(currentSheet,0,0));
            createNavSTree(ss.getNavSBuckets());
            ((SheetImpl) currentSheet).fullRefresh();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
