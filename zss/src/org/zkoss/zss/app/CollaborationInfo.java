package org.zkoss.zss.app;

import org.zkoss.zss.api.model.Book;

import java.util.Set;

public interface CollaborationInfo {

	/**
	 * set relationship between username and book
	 * @param username
	 * @param book
	 */
	void setRelationship(String username, Book book);

	/**
	 * @param old_name
	 * @param new_name
	 */
	void replaceRelationship(String new_name, String old_name);

	/**
	 * remove books having relationship to username
	 *
	 * @param username
	 */
	void removeRelationship(String username);

	/**
	 * ask if username is existing
	 * @param username
	 * @return true if username has already existed, false otherwise.
	 */
	boolean isUsernameExist(String username);

	/**
	 * add username and remove old username
	 * @param username
	 * @param oldUsername
	 * @return
	 */
	boolean addUsername(String username, String oldUsername);

	/**
	 * remove username
	 * @param username
	 */
	void removeUsername(String username);

	/**
	 * return username of users which are using book "bookName"
	 * @param bookName
	 * @return a set containing usernames
	 */
	Set<String> getUsedUsernames(String bookName);

	/**
	 * return a username which doesn't duplicate with others
	 * @param originName
	 * @return unique username
	 */
	String getUsername(String originName);

	/**
	 * @return current username
	 */
	String getUsername();
	
	/**************************
	 * 
	 * Event Mechanism
	 * 
	 **************************/
	void addEvent(CollaborationEventListener listener);

	interface CollaborationEventListener {
		void onEvent(CollaborationEvent event);
	}

	class CollaborationEvent {
		private Type type;
		private Object value;
		public CollaborationEvent(Type type, Object value) {
			this.type = type;
			this.value = value;
		}

		public Type getType() {
			return type;
		}

		public void setType(Type type) {
			this.type = type;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

		public enum Type {BOOK_EMPTY}
	}


}