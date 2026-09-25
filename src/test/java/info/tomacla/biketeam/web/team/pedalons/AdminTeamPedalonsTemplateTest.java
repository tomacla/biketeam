package info.tomacla.biketeam.web.team.pedalons;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.tomacla.biketeam.domain.pedalons.PedalonsMigration;
import info.tomacla.biketeam.domain.pedalons.PedalonsMigrationStatus;
import info.tomacla.biketeam.domain.team.PedalonsState;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.security.passkey.PasskeySuggestionService;
import info.tomacla.biketeam.service.NotificationService;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.pedalons.PedalonsMigrationService;
import info.tomacla.biketeam.service.pedalons.PedalonsSummary;
import info.tomacla.biketeam.service.url.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Rendu reel du template team_admin_pedalons.ftlh (et de l'entree de menu), pour chaque etat de
 * la page : une erreur FreeMarker ne se voit sinon qu'en production.
 */
public class AdminTeamPedalonsTemplateTest {

    private PedalonsMigrationService migrationService;
    private Team team;
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() throws Exception {
        migrationService = mock(PedalonsMigrationService.class);
        when(migrationService.isEnabled()).thenReturn(true);
        when(migrationService.isMigratable(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        when(migrationService.summary(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PedalonsSummary(12, 2585, 665, 3, 19, 124, 40, true, false));

        AdminTeamPedalonsController controller = new AdminTeamPedalonsController();
        ReflectionTestUtils.setField(controller, "migrationService", migrationService);
        ReflectionTestUtils.setField(controller, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(controller, "notificationService", mock(NotificationService.class));
        ReflectionTestUtils.setField(controller, "passkeySuggestionService", mock(PasskeySuggestionService.class));
        ReflectionTestUtils.setField(controller, "siteName", "BikeTeam");
        UrlService urlService = mock(UrlService.class);
        when(urlService.getSiteUrl()).thenReturn("http://localhost:8080");
        ReflectionTestUtils.setField(controller, "urlService", urlService);

        team = new Team();
        team.setId("n-peloton");
        team.setName("N'Peloton");
        team.setCity("Nantes");
        team.getDescription().setDescription("Description");
        TeamService teamService = mock(TeamService.class);
        when(teamService.get("n-peloton")).thenReturn(Optional.of(team));
        ReflectionTestUtils.setField(controller, "teamService", teamService);

        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
        configurer.setTemplateLoaderPath("classpath:/templates/");
        configurer.setDefaultEncoding("UTF-8");
        configurer.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers((viewName, locale) -> {
                    FreeMarkerView view = new FreeMarkerView();
                    view.setUrl(viewName + ".ftlh");
                    view.setConfiguration(configurer.getConfiguration());
                    view.setContentType("text/html;charset=UTF-8");
                    view.setExposeSpringMacroHelpers(true);
                    view.setApplicationContext(new org.springframework.web.context.support.StaticWebApplicationContext() {{
                        setServletContext(new org.springframework.mock.web.MockServletContext());
                        refresh();
                    }});
                    return view;
                })
                .build();
    }

    private PedalonsMigration migration(String id, boolean dryRun, PedalonsMigrationStatus status, String resultJson) {
        PedalonsMigration m = new PedalonsMigration(id, "n-peloton", "user-1", dryRun, false, Instant.parse("2026-09-22T10:00:00Z"));
        m.setStatus(status, Instant.parse("2026-09-22T10:05:00Z"));
        m.setResultJson(resultJson);
        return m;
    }

    @Test
    public void testFormWithoutHistory() throws Exception {
        when(migrationService.history("n-peloton")).thenReturn(List.of());

        mockMvc.perform(get("/n-peloton/admin/pedalons"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Essai (recommandé)")))
                .andExpect(content().string(containsString("585 parcours")))
                .andExpect(content().string(containsString("/n-peloton/admin/pedalons/start")))
                .andExpect(content().string(containsString("Migrer vers Pédalons")));
    }

    @Test
    public void testRunningMigration() throws Exception {
        when(migrationService.history("n-peloton")).thenReturn(List.of(migration("m1", false, PedalonsMigrationStatus.RUNNING,
                "{\"status\":\"RUNNING\",\"attempt\":2,\"progress\":{\"phase\":\"ROUTES\",\"done\":120,\"total\":2601}}")));
        team.setPedalonsState(PedalonsState.MIGRATING);

        mockMvc.perform(get("/n-peloton/admin/pedalons"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("http-equiv=\"refresh\"")))
                .andExpect(content().string(containsString("ROUTES")))
                .andExpect(content().string(containsString("width: 5%")))
                .andExpect(content().string(not(containsString("/admin/pedalons/start"))));
    }

    @Test
    public void testRunningMigrationWithoutLastAttemptError() throws Exception {
        // Pedalons plus ancien (champ absent) ou champ a null : rien sur l'essai precedent
        when(migrationService.history("n-peloton")).thenReturn(List.of(
                migration("m1", false, PedalonsMigrationStatus.RUNNING,
                        "{\"status\":\"RUNNING\",\"attempt\":1,\"progress\":{\"phase\":\"ROUTES\",\"done\":1,\"total\":2}}"),
                migration("m0", true, PedalonsMigrationStatus.RUNNING,
                        "{\"status\":\"RUNNING\",\"attempt\":1,\"lastAttemptError\":null,\"progress\":{\"phase\":\"ROUTES\",\"done\":1,\"total\":2}}")));
        team.setPedalonsState(PedalonsState.MIGRATING);

        mockMvc.perform(get("/n-peloton/admin/pedalons"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("échouée"))));
    }

    @Test
    public void testQueuedRetryShowsTheFailedAttemptReason() throws Exception {
        when(migrationService.history("n-peloton")).thenReturn(List.of(migration("m1", false, PedalonsMigrationStatus.RUNNING,
                "{\"status\":\"QUEUED\",\"attempt\":1,\"progress\":{\"phase\":\"QUEUED\",\"done\":0,\"total\":0},"
                        + "\"lastAttemptError\":{\"code\":\"EXPORT_UNAVAILABLE\",\"message\":\"HTTP 502 <b>proxy</b>\"},\"error\":null}")));
        team.setPedalonsState(PedalonsState.MIGRATING);

        mockMvc.perform(get("/n-peloton/admin/pedalons"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tentative 1 échouée : HTTP 502 &lt;b&gt;proxy&lt;/b&gt; — nouvel essai automatique")))
                .andExpect(content().string(not(containsString("<b>proxy"))));
    }

    @Test
    public void testRunningRetryShowsThePreviousAttemptReason() throws Exception {
        // deuxieme tentative commencee : c'est la premiere qui a echoue ; sans message, le code
        when(migrationService.history("n-peloton")).thenReturn(List.of(migration("m1", true, PedalonsMigrationStatus.RUNNING,
                "{\"status\":\"RUNNING\",\"attempt\":2,\"progress\":{\"phase\":\"ROUTES\",\"done\":3,\"total\":10},"
                        + "\"lastAttemptError\":{\"code\":\"WORKER_LOST\",\"message\":null}}")));

        mockMvc.perform(get("/n-peloton/admin/pedalons"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("(tentative 2)")))
                .andExpect(content().string(containsString("Tentative 1 échouée : WORKER_LOST — nouvel essai automatique")));
    }

    @Test
    public void testFinalFailureDoesNotShowTheRetryNotice() throws Exception {
        PedalonsMigration failed = migration("m1", true, PedalonsMigrationStatus.FAILED,
                "{\"status\":\"FAILED\",\"attempt\":3,\"lastAttemptError\":{\"code\":\"EXPORT_UNAVAILABLE\",\"message\":\"HTTP 502\"},"
                        + "\"error\":{\"code\":\"EXPORT_UNAVAILABLE\",\"message\":\"HTTP 502\"}}");
        failed.setError("EXPORT_UNAVAILABLE", "HTTP 502");
        when(migrationService.history("n-peloton")).thenReturn(List.of(failed));

        mockMvc.perform(get("/n-peloton/admin/pedalons"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Erreur : EXPORT_UNAVAILABLE — HTTP 502")))
                .andExpect(content().string(not(containsString("nouvel essai automatique"))));
    }

    @Test
    public void testReadyToSwitch() throws Exception {
        PedalonsMigration succeeded = migration("m2", false, PedalonsMigrationStatus.SUCCEEDED,
                "{\"status\":\"SUCCEEDED\",\"counts\":{\"routes\":{\"total\":2601,\"migrated\":2560,\"skipped\":16,\"failed\":25}}}");
        succeeded.setPedalonsTeamUrl("https://www.pedalons.fr/equipes/n-peloton");
        when(migrationService.history("n-peloton")).thenReturn(List.of(succeeded));
        team.setPedalonsState(PedalonsState.READY);
        team.setPedalonsUrl("https://www.pedalons.fr/equipes/n-peloton");

        mockMvc.perform(get("/n-peloton/admin/pedalons"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("prêt à basculer")))
                .andExpect(content().string(containsString("/n-peloton/admin/pedalons/switch")))
                .andExpect(content().string(containsString("Basculer vers Pédalons")))
                .andExpect(content().string(containsString("Basculer vers Pédalons : toutes les adresses")))
                // compteurs du bilan (sans le separateur de milliers, qui depend de la locale)
                .andExpect(content().string(containsString("560")))
                // une nouvelle migration reste possible avant la bascule
                .andExpect(content().string(containsString("/n-peloton/admin/pedalons/start")))
                .andExpect(content().string(not(containsString("http-equiv=\"refresh\""))));
    }

    @Test
    public void testFinishedMigrations() throws Exception {
        PedalonsMigration succeeded = migration("m2", false, PedalonsMigrationStatus.SUCCEEDED,
                "{\"status\":\"SUCCEEDED\",\"targetTeam\":{\"url\":\"https://www.pedalons.fr/equipes/n-peloton\"},"
                        + "\"counts\":{\"routes\":{\"total\":2601,\"migrated\":2560,\"skipped\":16,\"failed\":25}},"
                        + "\"warnings\":[{\"entityType\":\"ROUTE\",\"biketeamId\":\"8c0\",\"code\":\"GPX_MISSING\",\"message\":\"No GPX <file>\"}],"
                        + "\"warningsTruncated\":true}");
        succeeded.setPedalonsTeamUrl("https://www.pedalons.fr/equipes/n-peloton");
        PedalonsMigration failed = migration("m1", true, PedalonsMigrationStatus.FAILED, null);
        failed.setError("BIKETEAM_SLUG_CONFLICT", "Slug taken");
        when(migrationService.history("n-peloton")).thenReturn(List.of(succeeded, failed));
        team.setPedalonsState(PedalonsState.MIGRATED);
        team.setPedalonsUrl("https://www.pedalons.fr/equipes/n-peloton");

        mockMvc.perform(get("/n-peloton/admin/pedalons"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ce groupe a été migré vers Pédalons")))
                .andExpect(content().string(containsString("GPX_MISSING")))
                .andExpect(content().string(containsString("No GPX &lt;file&gt;")))
                .andExpect(content().string(containsString("BIKETEAM_SLUG_CONFLICT")))
                .andExpect(content().string(containsString("liste tronquée")))
                .andExpect(content().string(not(containsString("http-equiv=\"refresh\""))))
                .andExpect(content().string(not(containsString("/admin/pedalons/start"))))
                .andExpect(content().string(not(containsString("/admin/pedalons/switch"))));
    }

}
