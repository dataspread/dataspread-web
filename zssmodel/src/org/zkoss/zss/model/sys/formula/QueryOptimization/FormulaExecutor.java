package org.zkoss.zss.model.sys.formula.QueryOptimization;

import javafx.util.Pair;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.impl.RefImpl;
import org.zkoss.zss.model.sys.dependency.Ref;
import org.zkoss.zss.model.sys.formula.Exception.OptimizationError;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;
import org.zkoss.zss.model.sys.formula.Primitives.DataOperator;
import org.zkoss.zss.model.sys.formula.Primitives.LogicalOperator;
import org.zkoss.zss.model.sys.formula.Primitives.PhysicalOperator;
import org.zkoss.zss.model.sys.formula.Primitives.SingleTransformOperator;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

import static org.zkoss.zss.model.sys.formula.Test.Timer.time;

public class FormulaExecutor {
    private static FormulaExecutor uniqueExecutor = new FormulaExecutor();
    private FormulaAsyncScheduler scheduler = null;
    private LinkedBlockingQueue<Pair<SSheet, AbstractCellAdv>> frontEndUpdateQueue;
//    private LinkedBlockingQueue<Object[]> dataBaseUpdateQueue;
    private FormulaExecutor(){
        frontEndUpdateQueue = new LinkedBlockingQueue<>();
        Thread frontEndUpdateThread = new Thread(() -> {
            try {
                while (true) {
                    Pair<SSheet, AbstractCellAdv> parameter = frontEndUpdateQueue.take();
                    update(parameter.getKey(), parameter.getValue());
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        frontEndUpdateThread.start();
//        Thread dataBaseThread = new Thread(() -> {
//            try {
//                while (true) {
//                    Pair<SSheet, AbstractCellAdv> parameter = frontEndUpdateQueue.take();
//                    update(parameter.getKey(), parameter.getValue());
//                }
//
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
//        dataBaseThread.start();
    }
    public static FormulaExecutor getExecutor(){
        return uniqueExecutor;
    }
    public void execute(QueryPlanGraph graph, FormulaAsyncScheduler scheduler) throws OptimizationError {
        this.scheduler = scheduler;
        Stack<PhysicalOperator> executionStack = new Stack<>();
        executionStack.addAll(graph.getConstants());
        for (DataOperator data:graph.dataNodes)
            if (data.readyToEvaluate())
                executionStack.push(data);
        while (!executionStack.empty()){
            PhysicalOperator p = executionStack.pop();
            if (!p.readyToEvaluate())
                continue;
            evaluate(p);
            p.forEachOutVertex((lo)->{
                PhysicalOperator po = (PhysicalOperator)lo;
                if (po.readyToEvaluate())
                    executionStack.push(po);
            });
        }
    }

    private void evaluate(PhysicalOperator operator){
        time(operator.getClass().getSimpleName(), ()-> {
            try {
                operator.evaluate(this);
            } catch (OptimizationError optimizationError) {
                optimizationError.printStackTrace();
            }
        });
    }

    public void addToUpdateQueue(SSheet sheet, AbstractCellAdv sCell){
        frontEndUpdateQueue.offer(new Pair<>(sheet,sCell));
    }

    private void update(SSheet sheet, AbstractCellAdv sCell){
        Map<String, int[]> visibleRange = FormulaAsyncScheduler.getVisibleMap().get(sheet);
        boolean hasOverlap = false;
        if (visibleRange != null && !visibleRange.isEmpty()) {
            for (int[] rows : visibleRange.values()) {
                Ref overlap = sCell.getRef()
                        .getOverlap(new RefImpl(null, null,
                                rows[0], 0, rows[1], Integer.MAX_VALUE));
                if (overlap != null){
                    hasOverlap = true;
                    break;
                }
            }
        }
        if (!hasOverlap)
            return;
        scheduler.update(sheet.getBook(), sheet, sCell.getCellRegion(),
                sCell.getValue(true, false).toString(),
                sCell.getFormulaValue());
    }

}
