package org.zkoss.zss.model.sys.formula;

import org.zkoss.zss.model.SCell;

/**
 * Created by zekun.fan@gmail.com on 7/20/17.
 * Usage: To synchronously update UI after asynchronous formula computation
 * Request handler:
 * 1. prepare, set up UI for future use, but not sure if necessary
 * 2. confirm, if the cell has a formula needed to be computed
 * 3. cancelIfNotConfirmed, as it suggests, it release the reference to the cell for future GC.
 *
 * FormulaAsyncHandler:
 * 1. updateAndRelease, after computation, update to UI if there exists an UI.
 */

public interface FormulaAsyncUIController {
    void prepare(SCell cell, Object spreadsheet);

    void confirm(SCell cell);

    void updateAndRelease(SCell cell);

    void cancelIfNotConfirmed(SCell cell);
}
