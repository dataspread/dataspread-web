package CompressionSizeTest;

import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;

import java.sql.SQLException;
import java.sql.Statement;

public class Utils {

    public static void connectToDBIfNotConnected() {
        if (DBHandler.instance == null) {
            DBHandler.connectToDB(
                    SizeTestMain.url,
                    SizeTestMain.dbDriver,
                    SizeTestMain.userName,
                    SizeTestMain.password
            );
        }
    }

    public static void cleanDB() {
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            Statement stmt = connection.createStatement();

            String cleanDepTable = "DELETE FROM " + DBHandler.dependency;
            stmt.execute(cleanDepTable);

            String cleanCompDepTable = "DELETE FROM " + DBHandler.compressDependency;
            stmt.execute(cleanCompDepTable);

            String cleanLogTable = "DELETE FROM " + DBHandler.stagedLog;
            stmt.execute(cleanLogTable);

            String cleanFullDepTable = "DELETE FROM " + DBHandler.fullDependency;
            stmt.execute(cleanFullDepTable);

            connection.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanDependencyTable(DBContext dbContext) {
        AutoRollbackConnection connection = dbContext.getConnection();
        try (Statement stmt = connection.createStatement()) {

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
