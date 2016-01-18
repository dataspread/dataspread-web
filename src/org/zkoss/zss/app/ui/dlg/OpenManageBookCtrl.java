/* 
	Purpose:
		
	Description:
		
	History:
		2013/7/10, Created by dennis

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.ui.dlg;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import org.zkoss.zss.app.impl.CollaborationInfoImpl;
import org.zkoss.zss.app.BookInfo;
import org.zkoss.zss.app.BookRepository;
import org.zkoss.zss.app.CollaborationInfo;
import org.zkoss.zss.app.repository.BookRepositoryFactory;
import org.zkoss.zss.app.BookManager;
import org.zkoss.zss.app.impl.BookManagerImpl;
import org.zkoss.zss.app.repository.impl.BookUtil;
import org.zkoss.zss.app.ui.UiUtil;
import org.zkoss.zul.Button;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 * 
 * @author dennis
 *
 */
public class OpenManageBookCtrl extends DlgCtrlBase{
	private static final long serialVersionUID = 1L;
	
	public final static String ARG_BOOKINFO = "bookinfo";
	public final static String ARG_BOOK = "book";
	
	private final static String URI = "~./zssapp/dlg/openManageBook.zul";
	
	public static final String ON_OPEN = "onOpen";
	
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
		bookListModel = new ListModelList<Map<String,Object>>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		for(BookInfo info : repo.list()){
			Map<String,Object> data = new HashMap<String,Object>();
			data.put("name", info.getName());
			data.put("lastmodify", dateFormat.format(info.getLastModify()));
			data.put("bookinfo", info);
			bookListModel.add(data);
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
		BookInfo bookInfo = (BookInfo) selection.get("bookinfo");
		postCallback(ON_OPEN, newMap(newEntry(ARG_BOOKINFO, bookInfo), newEntry(ARG_BOOK, loadBook(bookInfo))));
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
		
		final BookInfo bookinfo = (BookInfo)selection.get("bookinfo");
		
		synchronized (bookManager) {
			String bookName = bookinfo.getName();
			if(bookManager.isBookAttached(bookinfo)) {
				String users = Arrays.toString(collaborationInfo.getUsedUsernames(bookName).toArray());
				UiUtil.showInfoMessage("Book \"" + bookinfo.getName() + "\" is in used by " + users + ".");
				return;
			}
				
			Messagebox.show("want to delete file \"" + bookName + "\" ?", "ZK Spreadsheet", 
					Messagebox.OK + Messagebox.CANCEL, Messagebox.INFORMATION, new SerializableEventListener<Event>() {
				private static final long serialVersionUID = 4698610847862970542L;

				@Override
				public void onEvent(Event event) throws Exception {
					if(event.getData().equals(Messagebox.OK)) {
						bookManager.deleteBook(bookinfo);
						reloadBookModel();
					}
				}
			});
		}
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
	
	private Book loadBook(BookInfo bookInfo) {
		Book book = null;
		try {
			book = bookManager.readBook(bookInfo);
		} catch (IOException e) {
			log.error(e.getMessage(),e);
			UiUtil.showWarnMessage("Can't load the specified book:" + bookInfo.getName());
		}
		return book;
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
	
}
