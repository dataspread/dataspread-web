package DependencyTablePgImplCaching.Tests;

import static org.junit.Assert.*;

import org.zkoss.zss.model.impl.sys.DependencyTablePGImplCache;
import org.zkoss.zss.model.sys.dependency.Ref;

import java.util.function.Consumer;
import java.util.function.Function;
import org.zkoss.zss.model.SSheet;
import java.lang.reflect.Field;
import org.model.LruCache;
import java.util.Set;

public abstract class BaseTest {

    protected static Field  depToPrcCache;
    protected static Field  prcToDepCache;
    protected static Field  CACHE_SIZE;

    static {
        try {
            depToPrcCache = DependencyTablePGImplCache.class.getDeclaredField("depToPrcCache");
            prcToDepCache = DependencyTablePGImplCache.class.getDeclaredField("prcToDepCache");
            CACHE_SIZE = DependencyTablePGImplCache.class.getDeclaredField("CACHE_SIZE");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public abstract SSheet createSheet();

    public static void makeCacheFieldsPublic() {
        depToPrcCache.setAccessible(true);
        prcToDepCache.setAccessible(true);
        CACHE_SIZE.setAccessible(true);
    }

    private static LruCache<Ref, Set<Ref>> getCache (SSheet sheet, Field cache) {
        try {
            return (LruCache<Ref, Set<Ref>>) cache.get(sheet.getDependencyTable());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void cacheAll(SSheet sheet, int rows, int cols) {
        DependencyTablePGImplCache dt = (DependencyTablePGImplCache) sheet.getDependencyTable();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                dt.getDependents(sheet.getCell(r, c).getRef());
                dt.getActualDependents(sheet.getCell(r, c).getRef());
                dt.getDirectDependents(sheet.getCell(r, c).getRef());
                dt.getDirectPrecedents(sheet.getCell(r, c).getRef());
            }
        }
    }

    public void runTest(Function<SSheet, Function<LruCache<Ref, Set<Ref>>, Consumer<LruCache<Ref, Set<Ref>>>>> onSuccess) {
        SSheet sheet = createSheet();
        if (sheet != null) {
            LruCache<Ref, Set<Ref>> prcToDepRef = getCache(sheet, prcToDepCache);
            if (prcToDepRef != null) {
                LruCache<Ref, Set<Ref>> depToPrcRef = getCache(sheet, depToPrcCache);
                if (depToPrcRef != null) {
                    onSuccess.apply(sheet).apply(prcToDepRef).accept(depToPrcRef);
                } else {
                    fail("Could not access depToPrcCache.");
                }
            } else {
                fail("Could not access prcToDepCache.");
            }
        } else {
            fail("Could not create sheet.");
        }
    }

}
