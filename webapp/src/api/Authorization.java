package api;

import org.model.AutoRollbackConnection;
import org.model.DBHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Authorization {

    public static boolean authorizeBook(String bookId, String authToken){
        String query = "SELECT COUNT(*) FROM user_books WHERE authtoken = ? and booktable = ?";
        return validQuery(bookId, authToken, query);

    }

    public static boolean ownerBook(String bookId, String authToken){
        String query = "SELECT COUNT(*) FROM user_books WHERE authtoken = ? and booktable = ? and role='owner";
        return validQuery(bookId, authToken, query);

    }

    private static boolean validQuery(String bookId, String authToken, String query) {
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, authToken);
            statement.setString(2, bookId);
            ResultSet rs = statement.executeQuery();
            if (!rs.next()){
                return false;
            } else return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean authorizeTable(String bookId, String authToken){
        return true;
    }
}
