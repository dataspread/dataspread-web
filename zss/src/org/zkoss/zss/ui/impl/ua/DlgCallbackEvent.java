package org.zkoss.zss.ui.impl.ua;

import org.zkoss.zk.ui.event.Event;

import java.util.Map;

public class DlgCallbackEvent extends Event {
    private static final long serialVersionUID = 1574572906717059919L;

    public DlgCallbackEvent(String name, Map<String, Object> data) {
        super(name, null, data);
    }

    public Map<String, Object> getData() {
        return (Map<String, Object>) super.getData();
    }

    public Object getData(String name) {
        Map<String, Object> data = getData();
        if (data != null) {
            return data.get(name);
        }
        return null;
    }

}
