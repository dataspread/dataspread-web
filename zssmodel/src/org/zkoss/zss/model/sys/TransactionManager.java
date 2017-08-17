package org.zkoss.zss.model.sys;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zekun.fan@gmail.com on 8/16/17.
 * This implementation limits parallelism.
 * Shall be implemented with fine-grained lock
 * The question is - to which level? Book?
 */

public enum TransactionManager {
    INSTANCE;

    private ReentrantLock xlock;
    private int xid;

    private TransactionManager() {
        xlock=new ReentrantLock();
        xid=0;//require loading maybe
    }

    public void startTransaction(Object target){
        xlock.lock();
        ++xid;
    }

    public void endTransaction(Object target){
        xlock.unlock();
    }

    public boolean isInTransaction(Object target){
        return xlock.isLocked();
    }

    public int getXid(Object target){
        return xid;
    }
}
