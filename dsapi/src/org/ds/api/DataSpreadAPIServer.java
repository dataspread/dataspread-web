package org.ds.api;

import org.model.AutoRollbackConnection;
import org.model.DBContext;
import org.model.DBHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.zkoss.zss.model.impl.CountedBTree;
import org.zkoss.zss.model.impl.PosMapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

@SpringBootApplication
public class DataSpreadAPIServer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(DataSpreadAPIServer.class);
    }

	public static void main(String[] args){
        String url = "jdbc:postgresql://127.0.0.1:5432/dataspread";
        String driver = "org.postgresql.Driver";
        String userName = "dbuser";
        String password = "dbadmin";
        DBHandler.connectToDB(url, driver, userName, password);
//        try (AutoRollbackConnection connection = DBHandler.instance.getConnection()){
//            Statement stmt = connection.createStatement();
//            ResultSet rs = stmt.executeQuery("SELECT * FROM books");
//            System.out.println(rs.getMetaData().getColumnLabel(1));
//            System.out.println(rs.getMetaData().getColumnType(2));
//            System.out.println(rs.getMetaData().getColumnType(3));
//            if (rs.next()) {
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

		SpringApplication.run(DataSpreadAPIServer.class, args);
//        DBContext dbContext = new DBContext(DBHandler.instance.getConnection());
//        PosMapping mapping = new CountedBTree(dbContext, "test_btree");
//        ArrayList<Integer> ids = new ArrayList<>();
//        ids.add(666);
//        ids.add(889);
//        ids.add(110);
//        mapping.insertIDs(dbContext, 0, ids);
//        mapping.getIDs(dbContext,0,100);
//        for (Object i: mapping.getIDs(dbContext,0,3)){
//            System.out.println(i);
//        }

	}

}
