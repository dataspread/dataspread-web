import junit.framework.*;
import org.junit.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class was created to check if the developer database setup steps were
 * working properly.
 */
public class TestingDemo extends TestCase {

    protected Connection conn;

    /**
     * A helper method for connecting to the underlying database. The parameters
     * to this function depend on the way you set up dataspread and postgres.
     *
     * @param database
     * @param user
     * @param pass
     */
    private Connection establishConnection(String database, String user, String pass) {
        String url = "jdbc:postgresql://127.0.0.1:5433/" + database;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    /**
     * This method is called before every test. For now, it tries to connect to
     * the underlying database. If the connection can't be made, an error is raised.
     *
     */
    protected void setUp() {
        String db   = "dataspread";
        String user = "dataspreaduser";
        String pass = "password";
        this.conn = this.establishConnection(db,user, pass);
        assertNotNull("Connection failed!", this.conn);
    }

    @Test
    public void test1() {
        assertEquals(1, 1);
    }

    @Test
    public void test2() {
        assertEquals(1, 2);
    }
}
