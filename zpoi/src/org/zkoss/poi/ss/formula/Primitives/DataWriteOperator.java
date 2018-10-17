package org.zkoss.poi.ss.formula.Primitives;

import org.zkoss.poi.ss.formula.ptg.AreaPtg;
import org.zkoss.zss.model.impl.AbstractCellAdv;

public class DataWriteOperator extends LogicalOperator {
    AbstractCellAdv target;
    public DataWriteOperator(AbstractCellAdv target){
        super();
        this.target = target;
    }
}
