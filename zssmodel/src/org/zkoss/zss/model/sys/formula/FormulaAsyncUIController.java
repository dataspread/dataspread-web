package org.zkoss.zss.model.sys.formula;

import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;

/**
 * Created by zekun.fan@gmail.com on 7/20/17.
 * IoC pattern of design, for such dilemma that:
 * 1. Implementation class should be aware of Spreadsheet(in zss)
 * 2. Interface class should be visible to Scheduler(in zssmodel)
 * 3. zss is dependent on zssmodel.
 *
 * <History>
 * Usage: To synchronously update UI after asynchronous formula computation
 * Request handler:
 * 1. prepare, set up UI for future use, but not sure if necessary
 * 2. confirm, if the cell has a formula needed to be computed
 * 3. cancelIfNotConfirmed, as it suggests, it release the reference to the cell for future GC.
 *
 * FormulaAsyncHandler:
 * 1. updateAndRelease, after computation, update to UI if there exists an UI.
 * </History>
 * Now: just bind book with spreadsheet(s) and update each of them.
 * No longer need delicate mechanism to track lifecycle
 */

public interface FormulaAsyncUIController {

    void bind(SBook book,Object spreadsheet);

    void unbind(SBook book,Object spreadsheet);

    void update(SSheet sheet, CellRegion region);
}
