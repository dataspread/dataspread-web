package org.zkoss.zss.ui.impl.ua;

import org.zkoss.util.resource.Labels;
import org.zkoss.zss.api.CellOperationUtil;
import org.zkoss.zss.api.IllegalOpArgumentException;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.api.model.SheetProtection;
import org.zkoss.zss.ui.UserActionContext;
import org.zkoss.zss.ui.impl.undo.HideHeaderAction;
import org.zkoss.zss.ui.impl.undo.HideHeaderAction.Type;
import org.zkoss.zss.ui.sys.UndoableActionManager;

public class HideHeaderHandler extends AbstractHandler {
	private static final long serialVersionUID = 9120677511231533029L;
	final HideHeaderAction.Type _type;
	final boolean _hide;
	
	public HideHeaderHandler(Type type, boolean hide) {
		this._type = type;
		this._hide = hide;
	}



	@Override
	protected boolean processAction(UserActionContext ctx) {
		Sheet sheet = ctx.getSheet();
		AreaRef selection = ctx.getSelection();
		Range range = Ranges.range(sheet, selection);
		//ZSS-576
		if(range.isProtected()) {
			switch(_type) {
			case COLUMN:
				if (!range.getSheetProtection().isFormatColumnsAllowed()) {
					showProtectMessage();
					return true;
				}
				break;
			case ROW:
				if (!range.getSheetProtection().isFormatRowsAllowed()) {
					showProtectMessage();
					return true;
				}
			}
		}
		
		//ZSS-504, to prevent user's operation 
		if(_hide && _type == HideHeaderAction.Type.ROW && checkSelectAllVisibleRow(ctx)){
			throw new IllegalOpArgumentException(Labels.getLabel("zss.msg.operation_not_supported_with_all_row"));
		}
		if(_hide && _type == HideHeaderAction.Type.COLUMN && checkSelectAllVisibleColumn(ctx)){
			throw new IllegalOpArgumentException(Labels.getLabel("zss.msg.operation_not_supported_with_all_column"));
		}
		
		
		String label = null;
		switch(_type){
		case COLUMN:
			label = _hide?Labels.getLabel("zss.undo.hideColumn"):Labels.getLabel("zss.undo.unhideColumn");
			break;
		case ROW:
			label = _hide?Labels.getLabel("zss.undo.hideRow"):Labels.getLabel("zss.undo.unhideRow");
			break;
		}
		
		UndoableActionManager uam = ctx.getSpreadsheet().getUndoableActionManager();
		uam.doAction(new HideHeaderAction(label,sheet, selection.getRow(), selection.getColumn(), 
			selection.getLastRow(), selection.getLastColumn(), 
			_type,_hide));
		
		return true;
	}

	@Override
	public boolean isEnabled(Book book, Sheet sheet) {
		if (book == null || sheet == null) {
			return false;
		}
		if (!sheet.isProtected()) {
			return true;
		}
		final SheetProtection sheetProtection = Ranges.range(sheet).getSheetProtection();
		boolean allowed = false;
		switch(_type){
		case COLUMN:
			allowed = sheetProtection.isFormatColumnsAllowed();
			break;
		case ROW:
			allowed = sheetProtection.isFormatRowsAllowed();
			break;
		}
		return allowed;
	}
}
