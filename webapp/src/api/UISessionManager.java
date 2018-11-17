package api;

import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.sys.formula.FormulaAsyncScheduler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class UISessionManager  {

    private static final Logger logger = Logger.getLogger(UISessionManager.class.getName());
    private static UISessionManager instance;


    //SessionID -> Session
    Map<String, UISession> uiSessionMap;
    Map<SSheet, Set<UISession>> uiSheetSessionMap;
    // Visible Map for formula prioritization.
    // Sheet->{session->visibleRange}
    Map<Object, Map<String, int[]>> uiVisibleMap;


    public Set<UISession> getSessionBySheet(SSheet sheet) {
        return uiSheetSessionMap.get(sheet);
    }

    public UISession getUISession(String sessionId) {
        return uiSessionMap.get(sessionId);
    }

    // UI session per sheet */
    public static class UISession {
        final String sessionId;
        int fetchSize = 0;
        int startRow=0;
        int endRow=0;
        SSheet sheet = null;
        Set<Integer> cachedBlocks=null;

        UISession(String sessionId)
        {
            this.sessionId = sessionId;
        }

        public String getSessionId()
        {
            return sessionId;
        }

        public SSheet getSheet()
        {
            return sheet;
        }

        public int getFetchSize() {
            return fetchSize;
        }

        public void assignSheet(SSheet sheet, int fetchSize)
        {
            this.fetchSize = fetchSize;
            // Change this to per sheet.
            cachedBlocks = new HashSet<>();
            this.sheet = sheet;
            UISessionManager.getInstance().uiSheetSessionMap
                    .computeIfAbsent(sheet, x -> new HashSet<>()).add(this);
        }

        public void clearSheet()
        {
            UISessionManager.getInstance().uiSheetSessionMap.get(sheet).remove(this);
            sheet = null;
            fetchSize = 0;
            cachedBlocks = null;
        }

        public void addCachedBlock(int blockNumber)
        {
            cachedBlocks.add(blockNumber);
        }

        public void removeCachedBlock(int blockNo)
        {
            cachedBlocks.remove(blockNo);
        }

        public void updateViewPort(int startRow, int endRow) {
            this.startRow = startRow;
            this.endRow = endRow;
            UISessionManager.getInstance().uiVisibleMap
                    .computeIfAbsent(sheet, x -> new HashMap<>())
                    .put(this.sessionId,
                            new int[]{startRow, endRow});
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
        uiSessionMap = new HashMap<>();
        uiSheetSessionMap = new HashMap<>();
        uiVisibleMap = new HashMap<>();
        FormulaAsyncScheduler.updateVisibleMap(uiVisibleMap);
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
    }

    public void deleteSession(String sessionId)
    {
        UISession session = uiSessionMap.remove(sessionId);
        if (session!=null && session.sheet != null) {
            uiSheetSessionMap.get(session.sheet).remove(session);
            Map uiVisibleSession = uiVisibleMap.get(session.getSheet());
            if (uiVisibleSession!=null) {
                uiVisibleSession.remove(sessionId);
                if (uiVisibleSession.isEmpty())
                    uiVisibleMap.remove(session.getSheet());
            }
        }
    }
}
