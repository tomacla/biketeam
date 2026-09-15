package info.tomacla.biketeam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

import javax.imageio.ImageIO;

@SpringBootApplication
@ComponentScan({"info.tomacla.biketeam", "io.github.glandais"})
@EnableJdbcHttpSession
@EnableScheduling
public class BiketeamApplication {

    public static void main(String[] args) {
        // Enregistre les plugins ImageIO (dont webp-imageio) depuis le thread main :
        // le class loader des threads du ForkJoinPool ne voit pas BOOT-INF/lib.
        ImageIO.scanForPlugins();
        SpringApplication.run(BiketeamApplication.class, args);
    }

}
