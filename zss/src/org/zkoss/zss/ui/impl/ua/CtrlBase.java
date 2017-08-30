/* 
	Purpose:
		
	Description:
		
	History:
		2013/7/10, Created by dennis

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

*/
package org.zkoss.zss.ui.impl.ua;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zk.ui.select.SelectorComposer;

import java.util.HashMap;
import java.util.Map;

/**
 * @param <T>
 * @author dennis
 */
public class CtrlBase<T extends Component> extends SelectorComposer<T> {

    protected static final String APPCOMP = "$ZSSAPP$";
    private static final long serialVersionUID = 1L;
    EventListener<Event> desktopEventDispatcher;
    private boolean listenZssAppEvent;

    public CtrlBase(boolean listenZssAppEvent) {
        this.listenZssAppEvent = listenZssAppEvent;
    }

    static protected Entry newEntry(String name, Object value) {
        Entry arg = new Entry(name, value);
        return arg;
    }

    static protected Map<String, Object> newMap(Entry... args) {
        Map<String, Object> argm = new HashMap<String, Object>();
        for (Entry arg : args) {
            argm.put(arg.name, arg.value);
        }
        return argm;
    }

    public void doAfterCompose(T comp) throws Exception {
        super.doAfterCompose(comp);

        if (listenZssAppEvent) {
            desktopEventDispatcher = new SerializableEventListener<Event>() {
                private static final long serialVersionUID = 2403679013740201939L;

                public void onEvent(Event event) throws Exception {
                    AppEvent appevt = (AppEvent) event.getData();
                    if (appevt.ignoreSelf && appevt.from == CtrlBase.this) {
                        return;
                    }
                    onAppEvent(appevt.getName(), appevt.getData());
                }
            };

            getAppComp().addEventListener("onAppEvent", desktopEventDispatcher);
        }
    }

    protected void pushAppEvent(String event) {
        pushAppEvent(event, true, null);
    }

    protected void pushAppEvent(String event, Object data) {
        pushAppEvent(event, true, data);
    }

    protected void pushAppEvent(String event, boolean ignoreSelf, Object data) {

        Component comp = getAppComp();
        Event evt = new Event("onAppEvent", comp, new AppEvent(event, this, ignoreSelf, data));
        Events.postEvent(evt);
    }

    protected Component getAppComp() {
        T self = getSelf();
        if (self != null) {
            Component comp = (Component) getSelf().getAttribute(APPCOMP, true);
            if (comp == null) {
                throw new NullPointerException("zssapp component not found");
            }
            return comp;
        }
        return null;
    }

    //subclass should override this if it cares desktop event
    protected void onAppEvent(String event, Object data) {
        //default do nothing.
    }

    static class AppEvent extends Event {
        private static final long serialVersionUID = 1L;
        Object from;
        boolean ignoreSelf;

        public AppEvent(String name, Object from, boolean ignoreSelf, Object data) {
            super(name, null, data);
            this.from = from;
            this.ignoreSelf = ignoreSelf;
        }
    }

    static protected class Entry {
        final String name;
        final Object value;

        public Entry(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }
}
