import org.model.DBHandler;
import org.zkoss.zss.model.sys.formula.Test.TestCommandHandler;

public class XlsxLoader {

    private static void connect(){
        String url = "jdbc:postgresql://127.0.0.1:5432/dataspread";
        String driver = "org.postgresql.Driver";
        String userName = "dbuser";
        String password = "dbadmin";

        DBHandler.connectToDB(url, driver, userName, password);
    }

    public static void main(String[] args) throws Exception {
//        for (int i = 0; i < 255;i++)
//            System.out.println(i + ": " + ((char)i) );
        System.out.println((int)('°'));
        System.out.println((int)('\t'));
        for (String t : "\n1\n\n1\n".split("\n",-1)){
            System.out.println("hi:"+ t);
        }

//        connect();
//        TestCommandHandler.instance.loadXlsxSpreadsheets();
    }

}
