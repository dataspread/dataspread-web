package org.zkoss.zss.model.sys;

import org.zkoss.zss.model.SBook;

import java.util.HashMap;

public class BookBindings {
    static private HashMap<String, SBook> _bindings = new HashMap<>();

    private BookBindings() {
    }

    static public void put(String key, SBook value) {
        _bindings.put(key, value);
    }

    static public SBook get(String key) {
        return _bindings.get(key);
    }

    static public SBook remove(String key) {
        return _bindings.remove(key);
    }
}
