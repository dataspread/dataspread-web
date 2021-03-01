package networkcompression.utils;

import networkcompression.AsyncPerformanceMain;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.range.SImporters;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.model.DBHandler;

import java.util.stream.Collectors;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 *
 * A class with utilities that may be used for all code in this package.
 */
public class Util {

    public static Collection<CellRegion> getSheetCells (SSheet sheet, CellRegion region) {
        return sheet.getCells(region)
            .stream()
            .map(SCell::getCellRegion)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static void sleep (final long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException e) { e.printStackTrace(); }
    }

    public static String getCurrentTime () {
        return (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")).format(new Date());
    }

    public static <K, V> AbstractMap.SimpleImmutableEntry<K, V> pair (final K key, final V val) {
        return new AbstractMap.SimpleImmutableEntry<>(key, val);
    }

    public static void createDirectory (final Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
        } catch (IOException e) {
            System.out.println("Error creating directory. Exiting...");
            System.exit(0);
        }
    }

    public static <K, V> LinkedHashMap<K, V> pairsToMap (final List<AbstractMap.SimpleImmutableEntry<K, V>> list) {
        LinkedHashMap<K, V> map = new LinkedHashMap<>();
        for (AbstractMap.SimpleImmutableEntry<K, V> entry : list) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static void connectToDBIfNotConnected () {
        if (DBHandler.instance == null) {
            DBHandler.connectToDB(
                AsyncPerformanceMain.URL,
                AsyncPerformanceMain.DBDRIVER,
                AsyncPerformanceMain.USERNAME,
                AsyncPerformanceMain.PASSWORD
            );
        }
    }

    // TODO: This seems to cause an illegal reflective access operation warning
    public static SBook importBook (Path path) {
        Util.connectToDBIfNotConnected();
        SBook book = null;
        try {
            book = SImporters.getImporter().imports(path.toFile(), "testBook" + System.currentTimeMillis());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return book;
    }

    public static SBook getEmptyBook () {
        Util.connectToDBIfNotConnected();
        return BookBindings.getBookByName("testBook" + System.currentTimeMillis());
    }

}
