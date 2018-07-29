package api.utils;

import java.util.HashMap;
import java.util.Map;

public class ResumableInfoStorage {
    //Single instance
    private ResumableInfoStorage() {
    }

    private static ResumableInfoStorage sInstance;

    public static synchronized ResumableInfoStorage getInstance() {
        if (sInstance == null) {
            sInstance = new ResumableInfoStorage();
        }
        return sInstance;
    }

    private Map<String, ResumableInfo> mMap = new HashMap<>();

    public synchronized ResumableInfo get(int resumableChunkSize,
                                          long resumableTotalSize,
                                          String resumableIdentifier,
                                          String resumableFilename,
                                          String resumableFilePath) {

        ResumableInfo info = mMap.get(resumableIdentifier);

        if (info == null) {
            info = new ResumableInfo();

            info.resumableChunkSize = resumableChunkSize;
            info.resumableTotalSize = resumableTotalSize;
            info.resumableIdentifier = resumableIdentifier;
            info.resumableFilename = resumableFilename;
            info.resumableFilePath = resumableFilePath;

            mMap.put(resumableIdentifier, info);
        }
        return info;
    }

    /**
     * @param info
     */
    public void remove(ResumableInfo info) {
        mMap.remove(info.resumableIdentifier);
    }
}
