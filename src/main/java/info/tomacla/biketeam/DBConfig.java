package info.tomacla.biketeam;

import liquibase.Scope;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.ui.LoggerUIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

import javax.sql.DataSource;
import java.util.Map;

@EnableJdbcHttpSession
public class DBConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public SpringLiquibase liquibase() {

        try {

            // override console output
            Scope.enter(Map.of(Scope.Attr.ui.name(), new LoggerUIService()));
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
