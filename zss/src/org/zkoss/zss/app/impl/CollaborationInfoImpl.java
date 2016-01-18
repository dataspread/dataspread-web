package org.zkoss.zss.app.impl;

import java.util.HashSet;
import java.util.Set;

import org.zkoss.lang.Library;
import org.zkoss.util.logging.Log;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.app.CollaborationInfo;

/**
 * 
 * @author JerryChen
 *
 */

public class CollaborationInfoImpl implements CollaborationInfo {
	
	private static final Log logger = Log.lookup(CollaborationInfoImpl.class.getName());
	protected static CollaborationInfo collaborationInfo;
	
	class UserKey {
		private String sessionId;
		private String username;
		private Set<String> desktopIds = new HashSet<String>();
		public String getSessionId() {
			return sessionId;
		}
		public void setSessionId(String sessionId) {
			this.sessionId = sessionId;
		}
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public Set<String> getDesktopIds() {
			return desktopIds;
		}
		public void setDesktopIds(Set<String> desktopIds) {
			this.desktopIds = desktopIds;
		}
	}
	
	public static CollaborationInfo getInstance() {
		if(collaborationInfo == null) {
			String clz = Library.getProperty("org.zkoss.zss.app.CollaborationInfo.class");
			if(clz != null && Boolean.valueOf(Library.getProperty("zssapp.collaboration.disabled")) != Boolean.TRUE){
				try {
					collaborationInfo = (CollaborationInfo) Class.forName(clz).newInstance();
				} catch(Exception e) {
					collaborationInfo =  new CollaborationInfoImpl();
					logger.error(e.getMessage(), e);
				}			
			} else
				collaborationInfo =  new CollaborationInfoImpl();
		}
		
		return collaborationInfo;
	}

	@Override
	public void setRelationship(String username, Book book) {}

	@Override
	public void removeRelationship(String username) {}
	
	@Override
	public boolean isUsernameExist(String username) {
		return false;
	}
	
	@Override
	public boolean addUsername(String username, String oldUsername) {
		return false;
	}
	
	@Override
	public void removeUsername(String username) {}
	
	@Override
	public Set<String> getUsedUsernames(String bookName) {
		return new HashSet<String>(0);
	}
	
	@Override
	public String getUsername(String originName) {
		return "";
	}

	@Override
	public void addEvent(CollaborationEventListener listener) {}
}
