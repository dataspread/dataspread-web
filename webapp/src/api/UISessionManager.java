package api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class UISessionManager {

    private static final Logger logger = Logger.getLogger(UISessionManager.class.getName());
    private static UISessionManager instance;
    public static final int FETCH_SIZE = 100;

    //SessionID -> Session
    Map<String, UISession> uiSessionMap;

    public UISession getUISession(String sessionId) {
        return uiSessionMap.get(sessionId);
    }


    public static class UISession {
        final String sessionId;
        String bookName=null;
        String sheetName=null;
        int fetchSize = 0;
        int startRow=0;
        int endRow=0;
        Set<Integer> cachedBlocks=null;

        UISession(String sessionId)
        {
            this.sessionId = sessionId;
        }

        public String getSessionId()
        {
            return sessionId;
        }

        public String getBookName()
        {
            return bookName;
        }

        public String getSheetName()
        {
            return sheetName;
        }

        public int getFetchSize() {
            return fetchSize;
        }

        public void assignSheet(String bookName, String sheetName, int fetchSize)
        {
            this.bookName = bookName;
            this.sheetName = sheetName;
            this.fetchSize = fetchSize;
            cachedBlocks = new HashSet<>();
        }

        public void clearSheet()
        {
            bookName = null;
            sheetName = null;
            fetchSize = 0;
            cachedBlocks = null;
        }

        public void addCachedBlock(int blockNumber)
        {
            cachedBlocks.add(blockNumber);
            System.out.println("addCachedBlock " + blockNumber);
        }

        public void removeCachedBlock(int blockNo)
        {
            cachedBlocks.remove(blockNo);
            System.out.println("removeCachedBlock " + blockNo);
        }

        public void updateViewPort(int startRow, int endRow) {
            this.startRow = startRow;
            this.endRow = endRow;
            System.out.println("updateViewPort " + startRow + " " + endRow);
        }

        public int getViewPortBlockNumber()
        {
            return startRow/fetchSize;
        }

        public boolean isBlockCached(int blockNumber)
        {
            return cachedBlocks.contains(blockNumber);
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



}
