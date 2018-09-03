package api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class UISessionManager {

    private static final Logger logger = Logger.getLogger(UISessionManager.class.getName());
    private static UISessionManager instance;

    //SessionID -> Session
    Map<String, UISession> uiSessionMap;

    public UISession getUISession(String sessionId) {
        return uiSessionMap.get(sessionId);
    }


    public static class UISession {
        final String sessionId;
        String bookName=null;
        String sheetName=null;
        Set<Integer> cachedBlocks=null;

        UISession(String sessionId)
        {
            this.sessionId = sessionId;
        }

        public String getBookName()
        {
            return bookName;
        }

        public String getSheetName()
        {
            return sheetName;
        }
    }

    private UISessionManager()
    {
        logger.info("UISessionManager Created");
        uiSessionMap = new HashMap<>();
    }

    public static UISessionManager getInstance()
    {
        if (instance==null)
            instance=new UISessionManager();
        return instance;
    }

    public void addSession(String sessionId)
    {
        uiSessionMap.put(sessionId, new UISession(sessionId));
        System.out.println("New session:" + sessionId);
    }

    public void deleteSession(String sessionId)
    {
        uiSessionMap.remove(sessionId);
        System.out.println("Session deleted:" + sessionId);

    }


    public void unassignSheet(String sessionId)
    {
        UISession uiSession = uiSessionMap.get(sessionId);
        uiSession.bookName = null;
        uiSession.sheetName = null;
        uiSession.cachedBlocks = null;
    }

    public void assignSheet(String sessionId, String bookName, String sheetName)
    {
        UISession uiSession = uiSessionMap.get(sessionId);
        uiSession.bookName = bookName;
        uiSession.sheetName = sheetName;
        uiSession.cachedBlocks = new HashSet<>();
        logger.info("Sheet Assigned");
    }

}
