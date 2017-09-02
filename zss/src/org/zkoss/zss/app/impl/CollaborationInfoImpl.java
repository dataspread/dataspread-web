package org.zkoss.zss.app.impl;

import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.lang.Library;
import org.zkoss.util.logging.Log;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.app.CollaborationInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author JerryChen
 *
 */

public class CollaborationInfoImpl implements CollaborationInfo {
	
	private static final Log logger = Log.lookup(CollaborationInfoImpl.class.getName());
	protected static CollaborationInfo collaborationInfo;
    private HashSet<String> user_list;
    private HashMap<String, HashSet<String>> book_list;
    private HashMap<String, HashSet<String>> reverse_book_list;

    private boolean schemaPresent;
    private String _name;

    public CollaborationInfoImpl() {
        user_list = new HashSet<>();
        book_list = new HashMap<>();
        reverse_book_list = new HashMap<>();
        checkDBSchema();
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

    public void checkDBSchema() {
        if (schemaPresent)
            return;
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            //get all users
            String getUser = "SELECT * FROM users";
            PreparedStatement getUserStmt = connection.prepareStatement(getUser);
            ResultSet rs = getUserStmt.executeQuery();
            while (rs.next()) {
                String user_name = rs.getString(1);
                if (!isUsernameExist(user_name)) {
                    user_list.add(user_name);
                    book_list.put(user_name, new HashSet<>());
                }
                String booktable = rs.getString(2);
                book_list.get(user_name).add(booktable);
                if (!reverse_book_list.containsKey(booktable)) reverse_book_list.put(booktable, new HashSet<>());
                reverse_book_list.get(booktable).add(user_name);

            }
            getUserStmt.close();

            connection.commit();
            schemaPresent = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setRelationship(String username, Book book) {
        String booktable = book.getInternalBook().getId();
        if (!reverse_book_list.containsKey(booktable)) reverse_book_list.put(booktable, new HashSet<>());
        reverse_book_list.get(booktable).add(username);
        if (!isUsernameExist(username)) addUsername(username, username);
        book_list.get(username).add(booktable);
        if (schemaPresent) {
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {

                //get all users
                String add = "INSERT INTO users VALUES (?,?)";
                PreparedStatement addStmt = connection.prepareStatement(add);
                addStmt.setString(1, username);
                addStmt.setString(2, booktable);
                addStmt.execute();

                addStmt.close();

                connection.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void replaceRelationship(String new_name, String old_name) {
        HashSet<String> users = reverse_book_list.get(old_name);
        reverse_book_list.remove(old_name);
        reverse_book_list.put(new_name, users);
        for (String user : users) {
            book_list.get(user).remove(old_name);
            book_list.get(user).add(new_name);
        }
        if (schemaPresent) {
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
                //get all users
                String update = "UPDATE users SET booktable = ? WHERE booktable = ?;";
                PreparedStatement updateStmt = connection.prepareStatement(update);
                updateStmt.setString(1, new_name);
                updateStmt.setString(2, old_name);
                updateStmt.execute();
                updateStmt.close();

                connection.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

	@Override
	public void removeRelationship(String username) {}

	@Override
	public boolean isUsernameExist(String username) {
        return user_list.contains(username);
    }
	
	@Override
	public boolean addUsername(String username, String oldUsername) {
        if (!isUsernameExist(username)) {
            user_list.add(username);
            book_list.put(username, new HashSet<>());
        }
        _name = username;
        return true;
    }
	
	@Override
    public void removeUsername(String username) {
        //TODO remove username
    }

    @Override
	public Set<String> getUsedUsernames(String bookName) {
		return new HashSet<String>(0);
	}
	
	@Override
	public String getUsername(String originName) {
        return isUsernameExist(originName) ? originName : "guest";
    }

    @Override
    public String getUsername() {
        return _name;
    }

	@Override
	public void addEvent(CollaborationEventListener listener) {}

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
}
