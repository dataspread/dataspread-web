package org.zkoss.zss.ui.sys;

import java.util.Set;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.event.Events;

/**
 * Action Handler for user's action
 * 
 * @author dennis
 * 
 */
public interface UserActionManagerCtrl extends SerializableEventListener<Event>{

	/**
	 * Returns the interested events of the spreadsheet.  
	 * @return event name list if you have any interested event of spreadsheet.
	 * @see Events
	 */
	Set<String> getInterestedEvents();
	
	
	/**
	 * Returns the interested ctrlKeys of the spreadsheet
	 * @return ctrlKeys that you want to set to spreadsheet, or null to set nothing to spreadsheet.
	 * @see Spreadsheet#setCtrlKeys(String)
	 */
	String getCtrlKeys();
	
	
	/**
	 * Returns the supported user action that should be disabled 
	 * @param sheet the sheet for cheeking
	 * @return a disabled user action array
	 */
	Set<String> getSupportedUserAction(Sheet sheet);
	
	
	/**
	 * Will be called when a handler assign to spreadsheet. 
	 * @param sparedsheet
	 */
	void bind(Spreadsheet sparedsheet);
	
	
	/**
	 * Will be called when a book set to sparedshet
	 * @param book the book to load or null if close a book
	 */
	void doAfterLoadBook(Book book);
	
//	/**
//	 * the i18n label keys for client side
//	 * @return
//	 */
//	String[] getLabelKeys();
}
