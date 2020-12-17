package info.tomacla.biketeam;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

import javax.sql.DataSource;

@EnableJdbcHttpSession
public class DBConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public SpringLiquibase liquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setShouldRun(true);
        liquibase.setChangeLog("classpath:liquibase/liquibase-master.xml");
        liquibase.setDataSource(dataSource);
        liquibase.setDefaultSchema("public");
        liquibase.setLiquibaseSchema("public");
        liquibase.setResourceLoader(new DefaultResourceLoader());
        return liquibase;
    }

}
