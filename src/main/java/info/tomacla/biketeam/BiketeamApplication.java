package info.tomacla.biketeam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"info.tomacla.biketeam", "io.github.glandais"})
public class BiketeamApplication {

    public static void main(String[] args) {
        SpringApplication.run(BiketeamApplication.class, args);
    }

}
