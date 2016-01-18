package org.zkoss.zss.ui.impl;

import java.io.Serializable;

import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.Widget;
import org.zkoss.zss.ui.sys.WidgetHandler;

/**
 * Default widget implementation, don't provide any function.
 */
public class VoidWidgetHandler implements WidgetHandler, Serializable {

	Spreadsheet spreadsheet;

	public VoidWidgetHandler() {
	}

	public boolean addWidget(Widget widget) {
		return false;
	}

	public Spreadsheet getSpreadsheet() {
		return spreadsheet;
	};

	public void invaliate() {
	}

	public void onLoadOnDemand(SSheet sheet, int left, int top, int right,
			int bottom) {
	}

	public boolean removeWidget(Widget widget) {
		return false;
	}

	public void init(Spreadsheet spreadsheet) {
		this.spreadsheet = spreadsheet;
	}

	public void updateWidgets(SSheet sheet, int left, int top, int right,
			int bottom) {
	}

	public void updateWidget(SSheet sheet, String widgetId) {
	}
}