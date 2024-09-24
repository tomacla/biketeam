package info.tomacla.biketeam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

@SpringBootApplication
@ComponentScan({"info.tomacla.biketeam", "io.github.glandais"})
@EnableJdbcHttpSession
public class BiketeamApplication {

    public static void main(String[] args) {
        SpringApplication.run(BiketeamApplication.class, args);
    }

}
