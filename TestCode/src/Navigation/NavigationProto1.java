package Navigation;

import org.model.AutoRollbackConnection;
import org.model.DBHandler;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NavigationProto1 {
    private JTable table1;
    private JTree tree1;
    private JComboBox comboTable;
    private JComboBox comboColumns;
    private JPanel Panel1;
    private JTextField txtRecordsRead;

    private String tableName;
    private String columnName;
    private NavIndex navIndex;

    boolean keepRunning = true;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    private Runnable task = () -> {loadTable();};
    Future<?> taskStatus=null;

    final int FETCH_SIZE = 1000;


    public NavigationProto1() {
        comboTable.addItemListener(e -> {
            if (e.getStateChange()==ItemEvent.SELECTED) {
                ArrayList<String> columnNames = new ArrayList<>();
                try(AutoRollbackConnection connection = DBHandler.instance.getConnection();
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery("SELECT column_name FROM " +
                        " information_schema.columns " +
                        " WHERE table_name='"+ e.getItem() +"'"))
                {
                    while (rs.next())
                        columnNames.add(rs.getString(1));
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }

                SwingUtilities.invokeLater(() -> {comboColumns.removeAllItems();
                    for(String columnName:columnNames)
                        comboColumns.addItem(columnName);});
            }
        });

        comboColumns.addItemListener(e -> {
            if (e.getStateChange()==ItemEvent.SELECTED) {
                populateTree(comboTable.getSelectedItem().toString(),
                        comboColumns.getSelectedItem().toString());
            }
        });

    }

    private void populateTree(String tableName, String columnName)
    {
        /* Stop current thread */
        try {
            keepRunning=false;
            if (taskStatus == null || taskStatus.get()==null)
            {
                /* Restart Computation */
                this.tableName = tableName;
                this.columnName = columnName;
                keepRunning=true;
                taskStatus =  executor.submit(task);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("NavigationProto1");
        NavigationProto1 navigationProto1 = new NavigationProto1();
        frame.setContentPane(navigationProto1.Panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        navigationProto1.init();
        frame.setVisible(true);
    }


    private void init()  {
        String url = "jdbc:postgresql://127.0.0.1:5432/datasets";
        String driver = "org.postgresql.Driver";
        String userName = "postgres";
        String password = "";
        DBHandler.connectToDB(url, driver, userName, password);

        // Get tables
        try(
        AutoRollbackConnection connection = DBHandler.instance.getConnection();
        Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery("SELECT table_name FROM " +
                    " information_schema.tables" +
                    " WHERE table_schema='public'");
            while (rs.next())
                comboTable.addItem(rs.getString(1));
            rs.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /* Background thread */
    private void loadTable() {
        navIndex = new NavIndex();
        //tree1.setModel(new NavTreeModel(navIndex));
        int totalRecords = 0;
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             Statement statement = connection.createStatement()) {
            statement.setFetchSize(FETCH_SIZE);
            ResultSet rs = statement.executeQuery("SELECT oid, \"" + columnName
                    + "\" FROM " + tableName);
            ArrayList<AbstractMap.SimpleImmutableEntry<Integer, String>> recordList =
                    new ArrayList<>(10000);

            SwingUtilities.invokeLater(() -> {
                table1.setModel(new DBTableModel(navIndex, tableName));
            });

            while (rs.next())
            {
                if (!keepRunning)
                {
                    rs.close();
                    return;
                }
                recordList.add(new AbstractMap.SimpleImmutableEntry<>(rs.getInt(1),
                        rs.getString(2)));
                if (recordList.size() == FETCH_SIZE) {
                    final int recordsTillNow = totalRecords;
                    navIndex.addRecords(recordList);
                    recordList.clear();
                    SwingUtilities.invokeLater(() ->
                            txtRecordsRead.setText("Records Read:"+recordsTillNow));
                }
                totalRecords++;
                if (totalRecords==FETCH_SIZE*10)
                    SwingUtilities.invokeLater(() ->
                            tree1.setModel(new NavTreeModel(navIndex)));

            }
            if (recordList.size() > 0)
                navIndex.addRecords(recordList);

            final int recordsTillNow = totalRecords;
            SwingUtilities.invokeLater(() -> {tree1.setModel(new NavTreeModel(navIndex));
                txtRecordsRead.setText("Records Read:"+recordsTillNow);
            });

            navIndex.setComplete();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
