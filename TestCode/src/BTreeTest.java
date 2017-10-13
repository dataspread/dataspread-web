import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.zss.model.impl.BTree;

import java.util.Arrays;
import java.util.stream.IntStream;

public class BTreeTest {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://127.0.0.1:5432/ibd";
        String driver = "org.postgresql.Driver";
        String userName = "mangesh";
        String password = "";
        DBHandler.connectToDB(url, driver, userName, password);
        DBContext dbContext = new DBContext(DBHandler.instance.getConnection());
        BTree btree = new BTree(dbContext, "Test1");

        IntStream.range(0,10).forEach(e->btree.addByCount(dbContext, e, e*10));

        Arrays.stream(btree.getIDsByCount(dbContext, 1, 5))
                .forEach(e->System.out.println(e));

        dbContext.getConnection().close();


    }
}
