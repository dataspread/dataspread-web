package org.zkoss.zss.model.sys;

import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.BookImpl;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BookBindings {
    private BookBindings(){}

    /* Book Name -> Book object */
    static private Map<String,SBook> _bindings=new ConcurrentHashMap<>();

    static public void put(String key,SBook value)
    {
        _bindings.put(key, value);
    }

    static public SBook get(String key)
    {
        return _bindings.get(key);
    }

    static public SBook remove(String key)
    {
        return _bindings.remove(key);
    }

    static public SBook getBookByName(String bookName,boolean load)
    {
        SBook book=_bindings.get(bookName);
        if (book==null && load) {
            book=new BookImpl(bookName);
            if (!book.setNameAndLoad(bookName))
            {
                book.createSheet("Sheet1");
                book.createSheet("Sheet2");
            }
            _bindings.put(bookName,book);
        }
        return book;
    }

    static public SSheet getSheetByRef(Ref ref, boolean load)
    {
        SBook book=getBookByName(ref.getBookName(), load);
        return book.getSheetByName(ref.getSheetName());
    }
}
