/*

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/01 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.zss.model.util;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * Use this class to help you concurrently access book model safely. 
 * Usage: implement your model-accessing logic in invoke() method.
 * 
 * @author dennis
 * @since 3.5.0
 */
public abstract class ReadWriteTask {
	
	/**
	 * The method will be invoked after acquiring a read-write lock and release the lock after invocation. 
	 * So implement your model-accessing logic in this method.
	 * @return depends on your logic
	 */
	abstract public Object invoke();
	
	/**
	 * Call invoke() after acquiring a write lock, and then release it. 
	 */
	public Object doInWriteLock(ReadWriteLock lock){
		lock.writeLock().lock();
		try{
			return this.invoke();
		}finally{
			lock.writeLock().unlock();
		}
	}
//	
//	private static Object doInWriteLock(ReadWriteLock lock, ReadWriteTask task){
//		lock.writeLock().lock();
//		try{
//			return task.invoke();
//		}finally{
//			lock.writeLock().unlock();
//		}
//	}
	
	/**
	 * Call invoke() after acquiring a read lock, and then release it. 
	 */
	public Object doInReadLock(ReadWriteLock lock){
		lock.readLock().lock();
		try{
			return this.invoke();
		}finally{
			lock.readLock().unlock();
		}
	}
//	private static Object doInReadLock(ReadWriteLock lock, ReadWriteTask task){
//		lock.readLock().lock();
//		try{
//			return task.invoke();
//		}finally{
//			lock.readLock().unlock();
//		}
//	}
}
