package org.zkoss.zss.model.impl.navigation;

/**
 * Created by Sajjadur on 10/12/2017.
 */
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;

public class HierarchyController extends SelectorComposer<Component> {
    private static final long serialVersionUID = 1L;

    final StockModel stockData = new StockModel();

    public ListModel<Stock> getStocks() {
        return new ListModelList<Stock>(stockData.getStocks());
    }
}
