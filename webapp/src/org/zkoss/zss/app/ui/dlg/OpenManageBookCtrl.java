/* 
	Purpose:
		
	Description:
		
	History:
		2013/7/10, Created by dennis

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.ui.dlg;

import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.zkoss.util.logging.Log;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zss.api.Importer;
import org.zkoss.zss.api.Importers;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.app.BookInfo;
import org.zkoss.zss.app.BookManager;
import org.zkoss.zss.app.BookRepository;
import org.zkoss.zss.app.CollaborationInfo;
import org.zkoss.zss.app.impl.BookManagerImpl;
import org.zkoss.zss.app.impl.CollaborationInfoImpl;
import org.zkoss.zss.app.repository.BookRepositoryFactory;
import org.zkoss.zss.app.repository.impl.BookUtil;
import org.zkoss.zss.app.ui.UiUtil;
import org.zkoss.zss.model.impl.BookImpl;
import org.zkoss.zul.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * @author dennis
 *
 */
public class OpenManageBookCtrl extends DlgCtrlBase{
    public static final String ZSS_USERNAME = "zssUsername";
    public final static String ARG_BOOKINFO = "bookinfo";
	public final static String ARG_BOOK = "book";
	public static final String ON_OPEN = "onOpen";
	private static final long serialVersionUID = 1L;
	private final static String URI = "~./zssapp/dlg/openManageBook.zul";
	private static final Log log = Log.lookup(OpenManageBookCtrl.class);
	@Wire
	Listbox bookList;
	@Wire
	Button open;
	@Wire
	Button delete;
	@Wire
	Button upload;
	
	BookRepository repo = BookRepositoryFactory.getInstance().getRepository();
	BookManager bookManager = BookManagerImpl.getInstance(repo);
	CollaborationInfo collaborationInfo = CollaborationInfoImpl.getInstance();
	ListModelList<Map<String,Object>> bookListModel = new ListModelList<Map<String,Object>>();
	
	public static void show(EventListener<DlgCallbackEvent> callback) {
		Map arg = newArg(callback);
		
		Window comp = (Window)Executions.createComponents(URI, null, arg);
		comp.doModal();
		return;
	}
	
	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		reloadBookModel();
	}
	
	private void reloadBookModel(){
	    String username = "guest";
        Cookie[] cookies = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(ZSS_USERNAME)) {
                    username = cookie.getValue();
                    break;
                }
            }
        }

		bookListModel = new ListModelList<Map<String,Object>>();

		String bookListQuery = "SELECT booktable FROM users WHERE username = ?;";
		String fetchBookQuery = "SELECT * FROM books WHERE booktable = ANY(?);";
		try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement getstmt = connection.prepareStatement(bookListQuery);
             PreparedStatement stmt = connection.prepareStatement(fetchBookQuery))
		{
			ArrayList<String> booklist = new ArrayList<>();
			getstmt.setString(1, username);
			ResultSet resultSet = getstmt.executeQuery();
			while (resultSet.next()) {
				booklist.add(resultSet.getString(1));
			}
			String[] list = new String[booklist.size()];
			int i = 0;
			for (String name : booklist) {
				list[i] = name;
				i++;
			}
			Array array = connection.createArrayOf("VARCHAR", list);
			stmt.setArray(1, array);
			ResultSet rs = stmt.executeQuery();

			while (rs.next())
			{
				Map<String,Object> data = new HashMap<String,Object>();
				data.put("name", rs.getString("bookname"));
				data.put("booktable", rs.getString("booktable"));
				data.put("lastopened",
						new SimpleDateFormat("MM/dd/yy HH:mm")
								.format(rs.getTimestamp("lastopened")));
				bookListModel.add(data);
			}
			getstmt.close();
			stmt.close();
			connection.commit();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		bookList.setModel(bookListModel);
		updateButtonState();
	}
	
	@Listen("onClick=#open; onDoubleClick=#bookList")
	public void onOpen(){
		Map<String,Object> selection = (Map<String,Object>)UiUtil.getSingleSelection(bookListModel);
		if(selection==null){
			UiUtil.showWarnMessage("Please select a book first");
			return;
		}
		final  String bookName = (String) selection.get("name");
		final  String bookTable = (String) selection.get("booktable");
		BookInfo bookInfo = (BookInfo) selection.get("bookinfo");
		postCallback(ON_OPEN, newMap(newEntry(ARG_BOOK, loadBook(bookName, bookTable))));
		detach();
	}
	
	@Listen("onSelect=#bookList")
	public void onSelect(){
		updateButtonState();
	}
	
	private void updateButtonState(){
		boolean selected = bookListModel.getSelection().size()>0;
		
		boolean readonly = UiUtil.isRepositoryReadonly();

		open.setDisabled(!selected);
		delete.setDisabled(!selected || readonly);
		upload.setDisabled(readonly);
	}
	
	@Listen("onClick=#delete")
	public void onDelete(){
		if(UiUtil.isRepositoryReadonly()){
			return;
		}
		Map<String,Object> selection = (Map<String,Object>)UiUtil.getSingleSelection(bookListModel);
		if(selection==null){
			UiUtil.showWarnMessage("Please select a book first");
			return;
		}
		
		final  String bookName = (String) selection.get("name");
		final  String bookTable = (String) selection.get("booktable");
		
		Messagebox.show("Want to delete \"" + bookName + "\" ?", "DataSpread",
				Messagebox.OK + Messagebox.CANCEL, Messagebox.INFORMATION, new SerializableEventListener<Event>() {
			private static final long serialVersionUID = 4698610847862970542L;

			@Override
			public void onEvent(Event event) {
				if(event.getData().equals(Messagebox.OK)) {
					BookImpl.deleteBook(bookName, bookTable);
					reloadBookModel();
				}
			}
		});
	}
	
	@Listen("onClick=#cancel;onCancel=#openBookDlg")
	public void onCancel(){
		detach();
	}
	
	@Listen("onClick=#upload")
	public void onUpload(){
		if(UiUtil.isRepositoryReadonly()){
			return;
		}
		Fileupload.get(5,new SerializableEventListener<UploadEvent>() {
			private static final long serialVersionUID = -7772499154678293597L;

			public void onEvent(UploadEvent event) throws Exception {
				BookInfo bookInfo = null;
				int count = 0;
				Importer importer = Importers.getImporter();
				Media[] medias = event.getMedias();
				String finalName = null;
				if(medias==null)
					return;
				for(Media m:event.getMedias()){
					if(m.isBinary()){
						InputStream is = null;
						try{
							is = m.getStreamData();
							String name = m.getName();
							Book book = importer.imports(is, name);
							name = BookUtil.appendExtension(name, book);
							name = BookUtil.suggestFileName(name, book, repo);
							bookInfo = repo.saveAs(name, book);
							finalName = bookInfo.getName();
							count++;
						}catch(Exception x){
							log.debug(x);
							log.warning("exception when handling user upload file", x);
						}finally{
							if(is!=null){
								is.close();
							}
						}
					}
				}

				if(count>0){
					reloadBookModel();
					Iterator<Map<String, Object>> iter = bookListModel.iterator();
					while(iter.hasNext()) {
						Map<String, Object> data = iter.next();
						if(data.get("name").equals(finalName)) {
							bookListModel.addToSelection(data);
							updateButtonState();
							break;
						}
					}
				}else{
					UiUtil.showWarnMessage("Can't upload/import the specified file" + (medias.length > 0 ? ": " + medias[0].getName() : "."));
				}
			}
		});
	}
	
	private Book loadBook(String bookName, String bookTable) {
		Book book = new org.zkoss.zss.api.model.impl.BookImpl(bookName);
		return book;
	}

	public Comparator<Map<String, Object>> getBookNameDescComparator() {
		return new MapAttrComparator(false, "name");
	}

	public Comparator<Map<String, Object>> getBookNameAscComparator() {
		return new MapAttrComparator(true, "name");
	}

	public Comparator<Map<String, Object>> getBookDateDescComparator() {
		return new MapAttrComparator(false, "lastmodify");
	}

	public Comparator<Map<String, Object>> getBookDateAscComparator() {
		return new MapAttrComparator(true, "lastmodify");
	}

	static private class MapAttrComparator implements Comparator<Map<String, Object>>, Serializable {
		private static final long serialVersionUID = -2889285276678010655L;
		private final boolean _asc;
		private final String _attr;

		public MapAttrComparator(boolean asc,String attr) {
			_asc = asc;
			_attr = attr;
		}

		@Override
		public int compare(Map<String, Object> o1, Map<String, Object> o2) {
			if(_asc){
				return o1.get(_attr).toString().compareTo(o2.get(_attr).toString());
			}else{
				return o2.get(_attr).toString().compareTo(o1.get(_attr).toString());
			}
		}
	}
	
}
