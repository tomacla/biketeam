package info.tomacla.biketeam.domain;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Configuration
@EnableJpaRepositories(basePackages = {"info.tomacla.biketeam.domain"})
@EntityScan("info.tomacla.biketeam.domain")
public class TestDBConfig {

}