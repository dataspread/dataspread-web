package org.zkoss.zss.app;

import java.io.IOException;

import org.zkoss.zss.api.model.Book;

public interface BookManager {
	Book readBook(BookInfo info) throws IOException;
	BookInfo updateBook(BookInfo info, Book book) throws IOException;
	BookInfo saveBook(BookInfo info, Book book) throws IOException;
	void deleteBook(BookInfo info) throws IOException;
	void saveAll() throws IOException;
	
	void detachBook(BookInfo info) throws IOException;
	boolean isBookAttached(BookInfo info);
	
	void shutdownAutoFileSaving();
}