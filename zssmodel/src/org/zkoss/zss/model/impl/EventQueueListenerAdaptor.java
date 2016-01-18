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
package org.zkoss.zss.model.impl;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zss.model.ModelEvent;
import org.zkoss.zss.model.ModelEventListener;
/**
 * To adapt model event -> queue -> zk listener -> model listener
 * @author dennis
 * @since 3.5.0
 */
public class EventQueueListenerAdaptor implements EventListenerAdaptor,Serializable {

	
	private static final long serialVersionUID = 1L;
	
	private String queueName;
	private String queueScope;

	private final List<WrappedListener> listeners = new LinkedList<WrappedListener>();
	
	public EventQueueListenerAdaptor(String scope,String bookId){
		this.queueScope = scope;
		this.queueName = "zssModel_"+bookId;
	}

	@Override
	public void addEventListener(ModelEventListener listener) {
		WrappedListener l = new WrappedListener(listener);
		if(!listeners.contains(l)){
			EventQueue<Event> queue = EventQueues.lookup(queueName, queueScope,true);
			queue.subscribe(l);
			listeners.add(l);
		}
	}

	@Override
	public void removeEventListener(ModelEventListener listener) {
		WrappedListener l = new WrappedListener(listener);
		if(listeners.contains(l)){
			EventQueue<Event> queue = EventQueues.lookup(queueName, queueScope,false);
			if(queue!=null){
				queue.unsubscribe(l);
			}
			listeners.remove(l);
		}
	}

	@Override
	public void sendModelEvent(ModelEvent event) {	
		EventQueue<Event> queue = EventQueues.lookup(queueName, queueScope,false);
		if(queue==null)
			return;
		queue.publish(new Event("onModelEvent",null,event));
	}

	@Override
	public void clear() {
		EventQueue<Event> queue = EventQueues.lookup(queueName, queueScope,false);
		if(queue!=null){
			for(WrappedListener l:listeners){
				queue.unsubscribe(l);
			}
		}
		listeners.clear();
	}

	@Override
	public int size() {
		return listeners.size();
	}

	
	static class WrappedListener implements SerializableEventListener<Event>{
		private static final long serialVersionUID = 1874923168106607228L;
		ModelEventListener listener;
		public WrappedListener(ModelEventListener l){
			this.listener = l;
		}
		@Override
		public void onEvent(Event event) throws Exception {
			//in zk lifecycle
			ModelEvent me = (ModelEvent)event.getData();
			listener.onEvent(me);
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((listener == null) ? 0 : listener.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			WrappedListener other = (WrappedListener) obj;
			if (listener == null) {
				if (other.listener != null)
					return false;
			} else if (!listener.equals(other.listener))
				return false;
			return true;
		}
	}

}
