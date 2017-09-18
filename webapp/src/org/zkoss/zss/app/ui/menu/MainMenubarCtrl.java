/* 
	Purpose:
		
	Description:
		
	History:
		2013/7/10, Created by dennis

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.app.ui.menu;

import org.zkoss.lang.Library;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.app.BookInfo;
import org.zkoss.zss.app.ui.AppEvts;
import org.zkoss.zss.app.ui.CtrlBase;
import org.zkoss.zss.app.ui.UiUtil;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.Version;
import org.zkoss.zss.ui.sys.UndoableActionManager;
import org.zkoss.zul.Menu;
import org.zkoss.zul.Menubar;
import org.zkoss.zul.Menuitem;

import java.util.Date;
/**
 * 
 * @author dennis
 *
 */
public class MainMenubarCtrl extends CtrlBase<Menubar> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7588544342697212954L;
    private static final String ZSS_PREFIX = "DataSpread ";
    @Wire
	Menuitem newFile;
	@Wire
	Menuitem openManageFile;
	@Wire
	Menuitem saveFile;
	@Wire
	Menuitem saveFileAs;
	@Wire
	Menuitem saveFileAndClose;
	@Wire
	Menuitem closeFile;
	@Wire
	Menuitem importFile;
	@Wire
	Menuitem exportFile;
	@Wire
	Menuitem exportPdf;
	@Wire
	Menuitem about;
	@Wire
	Menuitem zssmark;
	@Wire
	Menuitem changeUsername;
	@Wire
	Menuitem shareBook;
	
	
	@Wire
	Menuitem undo;
	@Wire
	Menuitem redo;
	
	
	@Wire
	Menuitem toggleFormulaBar;
	@Wire
	Menuitem freezePanel;
	@Wire
	Menuitem unfreezePanel;
	@Wire
	Menu freezeRows;
	@Wire
	Menu freezeCols;
	@Wire
	Menu insertMenu;

	@Wire
	Menuitem createTable;
	@Wire
	Menuitem deleteTable;
	@Wire
	Menuitem displayTable;
	@Wire
	Menuitem toggleTableBar;

    public MainMenubarCtrl() {
        super(true);
    }

    @Override
	public void doAfterCompose(Menubar comp) throws Exception {
		super.doAfterCompose(comp);
	}

	protected void onAppEvent(String event,Object data){
		if(AppEvts.ON_CHANGED_SPREADSHEET.equals(event)){
			doUpdateMenu((Spreadsheet)data);
		}else if(AppEvts.ON_UPDATE_UNDO_REDO.equals(event)){
			doUpdateMenu((Spreadsheet)data);
		}else if(AppEvts.ON_AFTER_CHANGED_USERNAME.equals(event)){
			doUpdateUsername((String)data);
            pushAppEvent(AppEvts.ON_CLOSE_BOOK);
			pushAppEvent(AppEvts.ON_NEW_BOOK);
		}else if(AppEvts.ON_CHANGED_FILE_STATE.equals(event)){
			doUpdateFileState((String)data);
		}
	}

	private void doUpdateFileState(String data) {
		if (data.equals(BookInfo.STATE_EMPTY))
			setFileState(Labels.getLabel("zssapp.mainMenu.state.empty"));
		else if (data.equals(BookInfo.STATE_UNSAVED))
			setFileState(Labels.getLabel("zssapp.mainMenu.state.unsaved"));
		else if (data.equals(BookInfo.STATE_SAVED))
			updateFileSavedTime();
	}
	
	private void setFileState(String label) {
		Clients.evalJavaScript("jq('$saveMessage').zk.$().setLabel('" + label + "');");
	}
	
	private void updateFileSavedTime() {
		// we do client side operation for time zone.
		String script = "var saveMsgTime = new Date(" + new Date().getTime() + ");";
		// template "Last saved: xx:xx:xx"
		Clients.evalJavaScript(script + "jq('$saveMessage').zk.$().setLabel('" + 
				Labels.getLabel("zssapp.mainMenu.state.lastSaved") + ": ' + " +
				"saveMsgTime.getHours() + ':' + " +
				"('0' + saveMsgTime.getMinutes()).slice(-2) + ':' + " + 
				"('0' + saveMsgTime.getSeconds()).slice(-2));");
	}

	private void doUpdateMenu(Spreadsheet sparedsheet){

		boolean hasBook = sparedsheet.getBook()!=null;
		boolean isEE = "EE".equals(Version.getEdition());
		Boolean evalOnly = (Boolean) Executions.getCurrent().getDesktop().getWebApp().getAttribute("Evaluation Only");
		Boolean collabDisabled = Boolean.valueOf(Library.getProperty("zssapp.collaboration.disabled"));
		//new and open are always on
		newFile.setDisabled(false);
		openManageFile.setDisabled(false);
		boolean readonly = UiUtil.isRepositoryReadonly();
		boolean disabled = !hasBook;
//		saveFile.setDisabled(disabled || readonly);
		saveFileAs.setDisabled(disabled || readonly);
//		saveFileAndClose.setDisabled(disabled || readonly);
		closeFile.setDisabled(disabled);
		exportFile.setDisabled(!isEE || disabled);
		exportPdf.setDisabled(!isEE || disabled);
		//changeUsername.setDisabled(!isEE || collabDisabled == Boolean.TRUE);
		changeUsername.setDisabled(collabDisabled == Boolean.TRUE);
		shareBook.setDisabled(!isEE || collabDisabled == Boolean.TRUE);
		
		// set about url
        about.setHref(Library.getProperty("zssapp.menu.about.url", "http://dataspread.github.io"));

        // zss title
		if(evalOnly == null) 
			evalOnly = Boolean.FALSE;
		if(!evalOnly && Boolean.valueOf(Library.getProperty("zssapp.menu.zssmark.hidden")) == Boolean.TRUE) {
			zssmark.setVisible(false);
		} else {
			String title = ZSS_PREFIX + Version.DATASPREADUID;
			zssmark.setLabel(title);
		}
		
		Boolean shareBookHidden = Boolean.valueOf(Library.getProperty("zssapp.menu.sharebook.hidden"));
		if(shareBookHidden)
			shareBook.setVisible(false);
        shareBook.setVisible(false);
		
		Boolean usernameHidden = Boolean.valueOf(Library.getProperty("zssapp.menu.username.hidden"));
		if(usernameHidden)
			changeUsername.setVisible(false);
		
		UndoableActionManager uam = sparedsheet.getUndoableActionManager();
		
		
		String label = Labels.getLabel("zssapp.mainMenu.edit.undo");
		if(isEE && uam.isUndoable()){
			undo.setDisabled(false);
			label = label+":"+uam.getUndoLabel();	
		}else{
			undo.setDisabled(true);
		}
		undo.setLabel(label);
		
		label = Labels.getLabel("zssapp.mainMenu.edit.redo");
		if(isEE && uam.isRedoable()){
			redo.setDisabled(false);
			label = label+":"+uam.getRedoLabel();	
		}else{
			redo.setDisabled(true);
		}
		redo.setLabel(label);
				
//		toggleFormulaBar.setDisabled(disabled); //don't need to care the book load or not.
		toggleFormulaBar.setChecked(sparedsheet.isShowFormulabar());
		
		
		
		freezePanel.setDisabled(!isEE || disabled);
		Sheet sheet = sparedsheet.getSelectedSheet();
		unfreezePanel.setDisabled(!isEE || disabled || !(sheet.getRowFreeze()>0||sheet.getColumnFreeze()>0));
		
		for(Component comp:Selectors.find(freezeRows, "menuitem")){
			((Menuitem)comp).setDisabled(!isEE || disabled);
		}
		for(Component comp:Selectors.find(freezeCols, "menuitem")){
			((Menuitem)comp).setDisabled(!isEE || disabled);
		}
		for(Component comp:Selectors.find(insertMenu, "menuitem")){
			((Menuitem)comp).setDisabled(!isEE || disabled);
		}
	}
	
	private void doUpdateUsername(String username) {
		changeUsername.setLabel(username);
	}
	

	@Listen("onClick=#newFile")
	public void onNew(){
		pushAppEvent(AppEvts.ON_NEW_BOOK);
	}
	@Listen("onClick=#openManageFile")
	public void onOpen(){
		pushAppEvent(AppEvts.ON_OPEN_MANAGE_BOOK);
	}
	@Listen("onClick=#saveFile")
	public void onSave(){
		pushAppEvent(AppEvts.ON_SAVE_BOOK);
	}
	@Listen("onClick=#saveFileAs")
	public void onSaveAs(){
		pushAppEvent(AppEvts.ON_SAVE_BOOK_AS);
	}
	@Listen("onClick=#saveFileAndClose")
	public void onSaveClose(){
		pushAppEvent(AppEvts.ON_SAVE_CLOSE_BOOK);
	}
	@Listen("onClick=#closeFile")
	public void onClose(){
		pushAppEvent(AppEvts.ON_CLOSE_BOOK);
	}
	@Listen("onClick=#importFile")
	public void onImport(){
		pushAppEvent(AppEvts.ON_IMPORT_BOOK);
	}
	@Listen("onClick=#exportFile")
	public void onExport(){
		pushAppEvent(AppEvts.ON_EXPORT_BOOK);
	}
	@Listen("onClick=#exportPdf")
	public void onExportPdf(){
		pushAppEvent(AppEvts.ON_EXPORT_BOOK_PDF);
	}
	@Listen("onClick=#changeUsername")
	public void onChangeUsername(){
		pushAppEvent(AppEvts.ON_CHANGED_USERNAME);
    }

    @Listen("onClick=#logout")
    public void onLogout() {
        pushAppEvent(AppEvts.ON_LOGOUT);
    }

    @Listen("onClick=#register")
    public void onRegister() {
        pushAppEvent(AppEvts.ON_REGISTER);
    }

    @Listen("onClick=#shareBook")
    public void onShareBook() {
		pushAppEvent(AppEvts.ON_SHARE_BOOK);
	}
	
	@Listen("onToggleFormulaBar=#mainMenubar")
	public void onToggleFormulaBar(){
		pushAppEvent(AppEvts.ON_TOGGLE_FORMULA_BAR);
	}
	
	@Listen("onFreezePanel=#mainMenubar")
	public void onFreezePanel(){
		pushAppEvent(AppEvts.ON_FREEZE_PNAEL);
	}
	
	@Listen("onUnfreezePanel=#mainMenubar")
	public void onUnfreezePanel(){
		pushAppEvent(AppEvts.ON_UNFREEZE_PANEL);
	}
	
	@Listen("onViewFreezeRows=#mainMenubar")
	public void onViewFreezeRows(ForwardEvent event) {
		int index = Integer.parseInt((String) event.getData());
		pushAppEvent(AppEvts.ON_FREEZE_ROW,index);
	}
	
	@Listen("onViewFreezeCols=#mainMenubar")
	public void onViewFreezeCols(ForwardEvent event) {
		int index = Integer.parseInt((String) event.getData());
		pushAppEvent(AppEvts.ON_FREEZE_COLUMN,index);
	}
	
	@Listen("onUndo=#mainMenubar")
	public void onUndo(ForwardEvent event) {
		pushAppEvent(AppEvts.ON_UNDO);
	}
	
	@Listen("onRedo=#mainMenubar")
	public void onRedo(ForwardEvent event) {
		pushAppEvent(AppEvts.ON_REDO);
	}
	
	@Listen("onInsertPicture=#mainMenubar")
	public void onInsertPicture(ForwardEvent event) {
		pushAppEvent(AppEvts.ON_INSERT_PICTURE);
	}
	
	@Listen("onInsertChart=#mainMenubar")
	public void onInsertChart(ForwardEvent event) {
		pushAppEvent(AppEvts.ON_INSERT_CHART, event.getData());
	}
	
	@Listen("onInsertHyperlink=#mainMenubar")
	public void onInsertHyperlink(ForwardEvent event) {
		pushAppEvent(AppEvts.ON_INSERT_HYPERLINK);
	}

	@Listen("onClick=#createTable")
	public void onCreateTable() {
		pushAppEvent(AppEvts.ON_CREATE_TABLE);
	}

	@Listen("onClick=#displayTable")
	public void onDisplayTable() {
		pushAppEvent(AppEvts.ON_DISPLAY_TABLE);
	}

	@Listen("onClick=#deleteTable")
	public void onDeleteTable() {
		pushAppEvent(AppEvts.ON_DELETE_TABLE);
	}

	@Listen("onClick=#expandCols")
	public void onExpandCols() {
		pushAppEvent(AppEvts.ON_EXPAND_COLS);
	}

	@Listen("onClick=#expandRows")
	public void onExpandRows() {
		pushAppEvent(AppEvts.ON_EXPAND_ROWS);
	}

	@Listen("onClick=#expandAll")
	public void onExpandAll() {
		pushAppEvent(AppEvts.ON_EXPAND_ALL);
	}

	@Listen("onToggleTableBar=#mainMenubar")
	public void onToggleTableBar() {
		pushAppEvent(AppEvts.ON_TOGGLE_TABLE_BAR);
	}
}

