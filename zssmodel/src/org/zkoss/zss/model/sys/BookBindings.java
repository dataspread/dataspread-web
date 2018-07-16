package org.zkoss.zss.model.sys;

import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.BookImpl;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BookBindings {
    private BookBindings() {
    }

    /* Book Name -> Book object */
    static private Map<String, SBook> _bindings = new ConcurrentHashMap<>();

    static public void put(String key, SBook value) {
        _bindings.put(key, value);
    }

    static public SBook get(String key) {
        return _bindings.get(key);
    }

    static public SBook remove(String key) {
        return _bindings.remove(key);
    }

    static public boolean contains(String key){ return _bindings.containsKey(key); }

    static public SBook getBookByName(String bookName) {
        return _bindings.computeIfAbsent(bookName, e->
                {
                    SBook book = new BookImpl(e);
                    if (!book.setNameAndLoad(e, book.getId())) {
                        book.createSheet("Sheet1");
                        book.createSheet("Sheet2");
                        book.checkDBSchema();
                    }
                    return book;
                }
        );
    }

    static public SBook getBookByNameDontLoad(String bookName) {
        return _bindings.computeIfAbsent(bookName, e->
                {
                    SBook book = new BookImpl(e);
                    if (!book.setNameAndLoad(e, book.getId()))
                        return null;
                    else
                        return book;
                }
        );
    }

    static public SBook getBookById(String bookId) {
        SBook book = BookImpl.getBookById(bookId);
        return book;
    }

    static public SSheet getSheetByRef(Ref ref) {
        SBook book = getBookByName(ref.getBookName());
        return book.getSheetByName(ref.getSheetName());
    }
}
