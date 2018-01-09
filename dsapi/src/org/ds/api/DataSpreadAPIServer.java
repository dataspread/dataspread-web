package org.ds.api;

import org.model.DBHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DataSpreadAPIServer {

	public static void main(String[] args) {
        String url = "jdbc:postgresql://127.0.0.1:5432/ibd";
        String driver = "org.postgresql.Driver";
        String userName = "mangesh";
        String password = "";
        DBHandler.connectToDB(url, driver, userName, password);
		SpringApplication.run(DataSpreadAPIServer.class, args);
	}

}
