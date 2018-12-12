package org.zkoss.zss.model.impl.sys.formula;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SSheet;

public interface FormulaAsyncListener {
    void update(SBook book, SSheet sheet, CellRegion cellRegion, Object value, String formula);
}
