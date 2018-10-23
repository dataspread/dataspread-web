package testformula;

import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.zkoss.zss.model.CellRegion;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.SCell;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zss.model.impl.AbstractCellAdv;
import org.zkoss.zss.model.sys.BookBindings;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;

public class NavTypeConversion {


    public static void main(String[] args)
    {
        String url = "jdbc:postgresql://127.0.0.1:5432/postgres";
        String driver = "org.postgresql.Driver";
        String userName = "postgres";
        String password = "";
        DBHandler.connectToDB(url, driver, userName, password);
        String sheetNames = "Sheet1";
        String []  bookIds = {"ljjriowqt","sjnco4pr7","ejnjfatup","ujnclsiq7","tjnckyyok"};
        int [][] colIndices = {
                {8,9,10,12,13,14},
                {4,5,6,7,8,9},
                {3,4,5,6,7,8},
                {3,4,5,7,8,9},
                {0}
        };

        String tableName = "type_converted_books";
        try(AutoRollbackConnection connection = DBHandler.instance.getConnection()) {
            for (int i = 0; i < bookIds.length; i++) {
                SBook book = BookBindings.getBookById(bookIds[i]);
                SSheet sheetByName = book.getSheetByName(sheetNames);
                String col_list = "";
                StringBuffer sbSS = new StringBuffer();
                PreparedStatement pstSS = null;
                for (int j = 0; j < colIndices[i].length; j++) {
                    if(j==0)
                        col_list += colIndices[i][j];
                    else
                        col_list += "-"+colIndices[i][j];

                    CellRegion tableRegion = new CellRegion(0, colIndices[i][j], sheetByName.getEndRowIndex() - 1, colIndices[i][j]);
                    ArrayList<SCell> result = (ArrayList<SCell>) sheetByName.getCells(tableRegion);
                    result.forEach(x -> x.updateCellTypeFromString(connection, false));
                    Collection<AbstractCellAdv> castCells = new ArrayList<>();
                    result.forEach(x -> castCells.add((AbstractCellAdv) x));
                    sheetByName.getDataModel().updateCells(new DBContext(connection), castCells);
                    connection.commit();
                }

                sbSS.append("INSERT into "+tableName+" (bookid, sheetname, cols) values(\'"+bookIds[i]+"\',\'"+sheetNames+"\',\'"+col_list+"\')");

                pstSS = connection.prepareStatement(sbSS.toString());

                pstSS.executeUpdate();
                connection.commit();

            }
        }catch (Exception e)
        {

        }

    }
}
