package org.zkoss.zss.ui.event;

import java.util.Collection;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.impl.Focus;

/**
 * The event to notify friend focus updated
 * @author henrichen
 * @see Events#ON_SYNC_FRIEND_FOCUS
 */
public class SyncFriendFocusEvent extends Event{
	private static final long serialVersionUID = 1L;
	private Sheet _sheet;
	private Collection<Focus> inBook;
	private Collection<Focus> inSheet;
	
	public SyncFriendFocusEvent(String name, Component target, Sheet sheet, 
			Collection<Focus> inBook, Collection<Focus> inSheet) {
		super(name, target, sheet);
		_sheet = sheet;
		this.inBook = inBook;
		this.inSheet = inSheet;
	}
	
	public Sheet getSheet(){
		return _sheet;
	}
	
	public String getSheetName(){
		return _sheet.getSheetName();
	}
	
	public Collection<Focus> getInBook() {
		return inBook;
	}
	
	public Collection<Focus> getInSheet() {
		return inSheet;
	}
}
