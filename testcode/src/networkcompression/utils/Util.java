package networkcompression.utils;

import networkcompression.AsyncPerformanceMain;
import org.model.DBHandler;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.sys.BookBindings;
import org.zkoss.zss.range.SImporters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A class with utilities that may be used for all code in this package.
 */
public class Util {

    public static String getCurrentTime() {
        return (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")).format(new Date());
    }

    public static <K, V> AbstractMap.SimpleImmutableEntry<K, V> pair(final K key, final V val) {
        return new AbstractMap.SimpleImmutableEntry<>(key, val);
    }

    public static <T> List<T> addAndReturn(List<T> lst, T item) {
        if (lst != null) {
            lst.add(item);
        }
        return lst;
    }

    public static Collection<CellRegion> getSheetCells(SSheet sheet, CellRegion region) {
        return sheet.getCells(region)
                .stream()
                .map(SCell::getCellRegion)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void createDirectory(final Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
        } catch (IOException e) {
            System.out.println("Error creating directory. Exiting...");
            System.exit(0);
        }
    }

    public static Path joinPaths(Path... paths) {
        if (paths.length != 0) {
            String first = paths[0].toString();
            String[] rest = new String[paths.length - 1];
            for (int i = 1; i < paths.length; i++) {
                rest[i - 1] = paths[i].toString();
            }
            return Paths.get(first, rest);
        }
        return Paths.get("");
    }

    public static <K, V> LinkedHashMap<K, V> pairsToMap(final List<AbstractMap.SimpleImmutableEntry<K, V>> list) {
        LinkedHashMap<K, V> map = new LinkedHashMap<>();
        for (AbstractMap.SimpleImmutableEntry<K, V> entry : list) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static void connectToDBIfNotConnected() {
        if (DBHandler.instance == null) {
            String[] configData = readConfig();
            DBHandler.connectToDB(
                    configData[0], configData[1], configData[2], configData[3]
            );
        }
    }

    public static String[] readConfig() {
        String[] configData = new String[4];
        try {
            File configFile = AsyncPerformanceMain.CONFIG_PATH.toFile();
            Scanner sc = new Scanner(configFile);
            for (int i = 0; i < 4; i++) {
                configData[i] = sc.nextLine();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Config file not found. Make a file named config in ../REPORTS formatted as follows:" +
                    "\n[PostgreSQL URL]" +
                    "\n[DB Driver]" +
                    "\n[PostgreSQL Username]" +
                    "\n[PostgreSQL Password]" +
                    "\n" +
                    "\nAttempting to use default PostgreSQL login.");
            configData = new String[] {
                    "jdbc:postgresql://127.0.0.1:5433/dataspread",
                    "org.postgresql.Driver",
                    "dataspreaduser",
                    "password"
            };
        }

        return configData;
    }

    // This method works correctly, but seems to cause an illegal reflective
    // access operation warning. This warning appears once and its source is
    // pretty deep in the codebase:
    //
    // SImporters
    //      -> ExcelImportFactory
    //          -> AbstractImporter
    //              -> ExcelImportAdapter
    //                  -> AbstractExcelImporter
    //                      -> ExcelXlsxImporter
    //                          -> XSSFWorkbook
    //                              -> ...
    public static SBook importBook(final Path path) {
        Util.connectToDBIfNotConnected();
        SBook book = null;
        try {
            book = SImporters.getImporter().imports(path.toFile(), "testBook" + System.currentTimeMillis());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return book;
    }

    public static SBook createEmptyBook() {
        Util.connectToDBIfNotConnected();
        return BookBindings.getBookByName("testBook" + System.currentTimeMillis());
    }

}
