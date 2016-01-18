package org.zkoss.zss.app;

import java.util.Set;

import org.zkoss.zss.api.model.Book;

public interface CollaborationInfo {

	/**
	 * set relationship between username and book
	 * @param username
	 * @param book
	 */
	public void setRelationship(String username, Book book);

	/**
	 * remove books having relationship to username
	 * @param username
	 */
	public void removeRelationship(String username);

	/**
	 * ask if username is existing
	 * @param username
	 * @return true if username has already existed, false otherwise.
	 */
	public boolean isUsernameExist(String username);

	/**
	 * add username and remove old username
	 * @param username
	 * @param oldUsername
	 * @return
	 */
	public boolean addUsername(String username, String oldUsername);

	/**
	 * remove username
	 * @param username
	 */
	public void removeUsername(String username);

	/**
	 * return username of users which are using book "bookName"
	 * @param bookName
	 * @return a set containing usernames
	 */
	public Set<String> getUsedUsernames(String bookName);

	/**
	 * return a username which doesn't duplicate with others
	 * @param originName
	 * @return unique username
	 */
	public String getUsername(String originName);
	
	/**************************
	 * 
	 * Event Mechanism
	 * 
	 **************************/
	public void addEvent(CollaborationEventListener listener);
	
	public interface CollaborationEventListener {
		public void onEvent(CollaborationEvent event);
	}
	
	public class CollaborationEvent {
		public enum Type {BOOK_EMPTY}
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
	}
	
	
}