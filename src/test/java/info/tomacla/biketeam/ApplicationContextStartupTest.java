package info.tomacla.biketeam;

import info.tomacla.biketeam.domain.TestDBContainer;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamRepository;
import info.tomacla.biketeam.service.amqp.BrokerService;
import info.tomacla.biketeam.service.pedalons.PedalonsRedirectCache;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Demarre le contexte Spring COMPLET, avec un vrai conteneur de servlets, comme en production.
 * <p>
 * Aucun autre test ne charge SecurityConfig en entier : un cycle de beans (interdit par defaut
 * depuis Spring Boot 2.6) n'y apparait donc qu'au demarrage reel de l'application. C'est arrive :
 * SecurityConfig -> UserDetailsService -> UserService, dont l'init() encode le mot de passe du
 * compte admin.email avec un PasswordEncoder alors declare par SecurityConfig, encore en cours de
 * creation (d'ou PasswordEncoderConfig). admin.email / admin.password sont donc renseignes expres,
 * pour un compte absent de la base : c'est sa creation au demarrage qui fermait le cycle.
 * <p>
 * Le test verifie aussi que le filtre Pedalons, enregistre dans Tomcat avant la chaine de
 * securite, redirige bien une equipe basculee.
 * <p>
 * Seul le reseau est neutralise : RabbitMQ (declarations de files, consommateurs, envois des
 * taches planifiees). Ne jamais activer spring.main.allow-circular-references ici.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "site.url=http://localhost:8080",
        "site.name=Biketeam",
        "contact.email=contact@example.com",
        "no.reply.email=no-reply@example.com",
        "rememberme.key=test-remember-me-key",
        // fournis en local par le .env, absent en CI
        "file.repository=${java.io.tmpdir}/biketeam-startup-test/file",
        "archive.directory=${java.io.tmpdir}/biketeam-startup-test/archive",
        "gpx.data.cache=${java.io.tmpdir}/biketeam-startup-test/cache",
        "mapbox.api-key=test",
        "facebook.client-id=test",
        "facebook.client-secret=test",
        "google.client-id=test",
        "google.client-secret=test",
        "strava.client-id=test",
        "strava.client-secret=test",
        "garmin.client.key=test",
        "garmin.client.secret=test",
        "admin.strava-id=1",
        "admin.email=startup-admin@example.com",
        "admin.password=startup-admin-password",
        "smtp.host=localhost",
        "smtp.port=1025",
        "smtp.protocol=smtp",
        "smtp.username=test",
        "smtp.password=test",
        "rabbitmq.host=localhost",
        "rabbitmq.username=test",
        "rabbitmq.password=test",
        "rabbitmq.vhost=test",
        "rabbitmq.autostartup=false",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        // fonction Pedalons eteinte (le .env local ne doit pas s'en meler) : le filtre, lui, reste
        // enregistre et redirige les equipes basculees
        "pedalons.url=",
        "pedalons.internal-url=",
        "pedalons.request-key=",
        "pedalons.trigger-secret=",
        "pedalons.export-secret=",
        "pedalons.redirect-status=302",
})
@ContextConfiguration(initializers = TestDBContainer.Initializer.class)
@ActiveProfiles("test")
public class ApplicationContextStartupTest {

    @MockitoBean
    private AmqpAdmin amqpAdmin;

    @MockitoBean
    private BrokerService brokerService;

    @LocalServerPort
    private int port;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PedalonsRedirectCache pedalonsRedirectCache;

    @Test
    public void contextStartsAndPedalonsFilterRedirectsMigratedTeam() throws Exception {

        final Team team = new Team();
        team.setId("startup-migrated");
        team.setName("Startup migrated");
        team.setCity("Rennes");
        team.markAsNew();
        teamRepository.save(team);
        try {
            // colonnes Pedalons en lecture seule pour Hibernate
            jdbcTemplate.update("UPDATE team SET pedalons_state = 'MIGRATED', pedalons_url = ? WHERE id = ?",
                    "https://pedalons.example/startup-migrated", "startup-migrated");
            pedalonsRedirectCache.invalidate();

            final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
            final HttpResponse<Void> response = client.send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/startup-migrated")).GET().build(),
                    HttpResponse.BodyHandlers.discarding());

            assertEquals(302, response.statusCode());
            assertEquals("https://pedalons.example/startup-migrated", response.headers().firstValue("Location").orElse(null));
        } finally {
            // base partagee avec les autres tests (TestDBContainer)
            teamRepository.deleteById("startup-migrated");
            pedalonsRedirectCache.invalidate();
        }
    }

}
