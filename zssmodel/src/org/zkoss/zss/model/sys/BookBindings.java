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
        SBook result=_bindings.get(bookName);
        if (result==null && load) {
            result=new BookImpl(bookName);
            result.setNameAndLoad(bookName);
            _bindings.put(bookName,result);
        }
        return result;
    }

    static public SSheet getSheetByRef(Ref ref, boolean load)
    {
        SBook book=getBookByName(ref.getBookName()  , load);
        return book.getSheetByName(ref.getSheetName());
    }
}
