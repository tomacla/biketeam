package info.tomacla.biketeam;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.jdbc.JdbcIndexedSessionRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * L'ecriture d'un attribut de session doit etre un UPSERT.
 * <p>
 * Ce test existe pour une raison precise : le customizer de Spring Boot appelle setTableName(),
 * qui REINITIALISE toutes les requetes du depot. Il ne fonctionne donc que parce que le bean de
 * {@link SecurityConfig} est applique apres celui de Spring Boot (@Order(HIGHEST_PRECEDENCE)).
 * Donner un ordre a ce bean, ou faire appeler setTableName() plus tard, ramenerait l'INSERT sec
 * et les DuplicateKeyException par rafales sur SPRING_SESSION_ATTRIBUTES, sans erreur au
 * demarrage.
 */
class SessionRepositoryCustomizerConfigurationTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SessionAutoConfiguration.class))
            .withUserConfiguration(SessionInfrastructure.class)
            .withPropertyValues("spring.session.store-type=jdbc",
                    "spring.session.jdbc.initialize-schema=never");

    @Test
    void sessionAttributesAreWrittenAsUpsert() {
        runner.run(context -> {
            JdbcIndexedSessionRepository repository = context.getBean(JdbcIndexedSessionRepository.class);
            String query = (String) ReflectionTestUtils.getField(repository, "createSessionAttributeQuery");
            assertTrue(query.contains("ON CONFLICT"), query);
            assertTrue(query.contains("DO UPDATE"), query);
            // %TABLE_NAME% doit avoir ete resolu : sinon la requete est syntaxiquement invalide
            assertTrue(query.contains("SPRING_SESSION_ATTRIBUTES"), query);
        });
    }

    @TestConfiguration
    static class SessionInfrastructure {

        /**
         * Aucune connexion n'est ouverte : la construction du depot ne fait que memoriser ses
         * collaborateurs, et le test n'execute aucune requete.
         */
        @Bean
        DataSource dataSource() {
            return mock(DataSource.class);
        }

        @Bean
        JdbcOperations jdbcOperations(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        SessionRepositoryCustomizer<JdbcIndexedSessionRepository> postgreSqlSessionRepositoryCustomizer() {
            return new SecurityConfig().postgreSqlSessionRepositoryCustomizer();
        }

    }

}
