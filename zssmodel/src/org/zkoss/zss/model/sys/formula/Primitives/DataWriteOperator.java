package org.zkoss.zss.model.sys.formula.Primitives;

import org.zkoss.zss.model.impl.AbstractCellAdv;

public class DataWriteOperator extends LogicalOperator {
    AbstractCellAdv target;
    public DataWriteOperator(AbstractCellAdv target){
        super();
        this.target = target;
    }
}
