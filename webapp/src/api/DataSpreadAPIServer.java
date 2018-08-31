package api;

import org.model.DBHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.web.bind.annotation.RestController;

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
		SpringApplication.run(DataSpreadAPIServer.class, args);
	}

}

@RestController
class GreetingController  {

    @MessageMapping("/hello")
    @SendTo("/push/sheet1/updates")
    String hello3() {

        System.out.println("hello");

        return "Hello";
    }

    @SubscribeMapping("/push/{bookName}/updates")
    void subscribe(@DestinationVariable String bookName) {
        System.out.println("subscribe to " + bookName);
    }


}