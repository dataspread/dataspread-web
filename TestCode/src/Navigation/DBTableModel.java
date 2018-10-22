package Navigation;

import com.google.common.cache.CacheBuilder;
import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import sun.security.util.Cache;


import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DBTableModel implements TableModel {
    private NavIndex navIndex;
    private String tableName;
    private Integer estimatedTableCount;
    private int columnCount;
    ArrayList<String> columnNames;
    final int cacheSize = 1000; // Keep this small as possible, to allow loading or newer data.

    com.google.common.cache.Cache<Object, Object> cache;


    public DBTableModel(NavIndex navIndex, String tableName)
    {
        this.navIndex = navIndex;
        this.tableName = tableName;
        estimatedTableCount=null;
        cache = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterWrite(2, TimeUnit.SECONDS)
                .build();

        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT OID as ID, * FROM " + tableName + " WHERE false")) {
            ResultSet rs = statement.executeQuery();
            ResultSetMetaData resultSetMetaData =  rs.getMetaData();
            columnCount = resultSetMetaData.getColumnCount();
            columnNames = new ArrayList<>(columnCount);
            for (int i=1;i<=columnCount;i++)
                columnNames.add(resultSetMetaData.getColumnName(i));
            System.out.println("Columns :" + columnNames);
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getRowCount() {
        if (navIndex.isComplete()) {
            System.out.println("Correct row count for " + tableName + " is " + navIndex.size() + " rows.");

            return navIndex.size();
        }
        else if (estimatedTableCount!=null)
            return estimatedTableCount;
        else {
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT reltuples AS approximate_row_count FROM pg_class WHERE relname = ?")) {
                statement.setString(1, tableName);
                ResultSet rs = statement.executeQuery();
                if (rs.next())
                    estimatedTableCount = rs.getInt(1);
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("Estimated row count for " + tableName + " is " + estimatedTableCount + " rows.");
            return estimatedTableCount;
        }

    }

    @Override
    public int getColumnCount() {
        return columnCount;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames.get(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    private ArrayList<String> getDataFromDB(int rowIndex)
    {
        ArrayList<String> ret = new ArrayList<>();
        List<Integer> oids = navIndex.getOids(rowIndex, rowIndex+100,
                estimatedTableCount);
            // TODO: fetch a screen full buffer.
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT oid as ID, * FROM " + tableName
                             + " WHERE OID = ?")) {
            preparedStatement.setInt(1, oids.get(0));
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                for (int i = 1; i <= columnCount; i++)
                    ret.add(resultSet.getString(i));
            resultSet.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
//            ArrayList<String> row = cache.get(rowIndex, () -> getDataFromDB(rowIndex));
//            ArrayList<String> prev_row = cache.getIfPresent(rowIndex-1);
//            if (prev_row==null  || !row.get(0).equals(prev_row.get(0)))
//                return row.get(columnIndex);
//            else
//                return "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

    }

    @Override
    public void addTableModelListener(TableModelListener l) {

    }

    @Override
    public void removeTableModelListener(TableModelListener l) {

    }
}
