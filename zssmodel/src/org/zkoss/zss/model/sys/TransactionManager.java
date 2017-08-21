package org.zkoss.zss.model.sys;

import java.util.HashMap;
import java.util.Map;
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
    private Map<Thread,Integer> workers;

    private TransactionManager() {
        xlock=new ReentrantLock();
        workers=new HashMap<>();
        xid=0;//require loading maybe
    }

    public void startTransaction(Object target){
        xlock.lock();
        //avoid nesting start messing up xid
        if (xlock.getHoldCount()==1)
            ++xid;
    }

    public void endTransaction(Object target){
        xlock.unlock();
    }

    public boolean isInTransaction(Object target){
        return xlock.isLocked();
    }

    public int getXid(Object target){
        if (xlock.isHeldByCurrentThread())
            return xid;
        else {
            Integer res=workers.get(Thread.currentThread());
            if (res==null)
                throw new RuntimeException("Undefined Xid");
            return res;
        }
    }

    public void registerWorker(int xid){
        workers.put(Thread.currentThread(),xid);
    }
}
