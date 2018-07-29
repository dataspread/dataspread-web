package api;

import org.model.DBHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
		SpringApplication.run(DataSpreadAPIServer.class, args);
	}

}

@RestController
class GreetingController {

    @RequestMapping("/hello/{name}")
    String hello(@PathVariable String name) {
        return "Hello, " + name + "!";
    }

    @RequestMapping("/getRows/{start}/{end}")
    ArrayList<String> hello1(@PathVariable int start,
                  @PathVariable int end) {
        ArrayList<String> out=new ArrayList<>();
        System.out.println("getRows " + start + " " + end   );
        for (int i=start;i<=end;i++)
        {
            out.add(Integer.toString(i));

        }
        return out;
    }
}