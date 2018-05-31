package org.ds.api;

public class Authorization {

    public static boolean authorizeBook(String bookId, String authToken){
        return true;
    }

    public static boolean authorizeTable(String bookId, String authToken){
        return true;
    }
}
