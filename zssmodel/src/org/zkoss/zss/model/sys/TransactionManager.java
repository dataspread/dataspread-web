package org.zkoss.zss.model.sys;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by zekun.fan@gmail.com on 8/16/17.
 */
// TODO- Remove this
public enum TransactionManager {
    INSTANCE;

    private Map<Thread,Integer> workers;
    private Map<Object,TransactionInfo> xinfo;

    private TransactionManager() {
        workers=new ConcurrentHashMap<>();
        xinfo=new ConcurrentHashMap<>();
    }

    public void startTransaction(Object target){
        TransactionInfo info=xinfo.computeIfAbsent(target, v->new TransactionInfo());
        info.xlock.lock();
        //avoid nesting start messing up xid
        if (info.xlock.getHoldCount()==1)
            ++info.xid;
    }

    public void endTransaction(Object target){
        TransactionInfo info=xinfo.get(target);
        if (info!=null)
            info.xlock.unlock();
    }

    public boolean isInTransaction(Object target){
        TransactionInfo info=xinfo.get(target);
        return info!=null && info.xlock.isHeldByCurrentThread();
    }

    public int getXid(Object target){
        TransactionInfo info=xinfo.get(target);
        if (info!=null && info.xlock.isHeldByCurrentThread())
            return info.xid;
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

    private class TransactionInfo{
        public ReentrantLock xlock;
        public int xid;

        public TransactionInfo() {
            xlock=new ReentrantLock();
            xid=0;
        }
    }
}
