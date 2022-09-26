package info.tomacla.biketeam.domain;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

@ActiveProfiles("test")
@Configuration
@EnableJpaRepositories(basePackages = {"info.tomacla.biketeam.domain"})
@EntityScan("info.tomacla.biketeam.domain")
public class TestDBConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public SpringLiquibase liquibase() {

        try {

            SpringLiquibase liquibase = new SpringLiquibase();
            liquibase.setShouldRun(true);
            liquibase.setChangeLog("classpath:liquibase/liquibase-master.xml");
            liquibase.setDataSource(dataSource);
            liquibase.setDefaultSchema("public");
            liquibase.setLiquibaseSchema("public");
            liquibase.setResourceLoader(new DefaultResourceLoader());
            return liquibase;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}