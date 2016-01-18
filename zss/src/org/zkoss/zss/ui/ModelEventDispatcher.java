/* ModelEventDispatcher.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		2013/12/11 , Created by dennis
}}IS_NOTE

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/

package org.zkoss.zss.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

import org.zkoss.zss.model.EventQueueModelEventListener;
import org.zkoss.zss.model.ModelEvent;
import org.zkoss.zss.model.ModelEventListener;

/**
 * To dispatch model event
 * @author Dennis
 * @since 3.5.0
 */
public class ModelEventDispatcher implements EventQueueModelEventListener {
	private static final long serialVersionUID = 1L;
	private Map<String, List<ModelEventListener>> _listeners = new HashMap<String, List<ModelEventListener>>(8);

	//--ModelEventListener--//
	@Override
	public void onEvent(ModelEvent event) {
		ReadWriteLock lock = event.getBook().getBookSeries().getLock();
		lock.writeLock().lock();//have to use write lock because of formula evaluation is not thread safe
		try{
			final String name = event.getName();
			final List<ModelEventListener> list = _listeners.get(name);
			if (list != null) {
				for(ModelEventListener listener : list) {
					listener.onEvent(event);
				}
			}
		}finally{
			lock.writeLock().unlock();
		}
	}

	public boolean addEventListener(String name, ModelEventListener listener) {
		List<ModelEventListener> list = _listeners.get(name);
		if (list == null) {
			list = new ArrayList<ModelEventListener>(4);
			_listeners.put(name, list);
		}
		return list.add(listener);
	}

	public boolean removeEventListener(String name, ModelEventListener listener) {
		final List<ModelEventListener> list = _listeners.get(name);
		return list != null ? list.remove(listener) : true;
	}
}
