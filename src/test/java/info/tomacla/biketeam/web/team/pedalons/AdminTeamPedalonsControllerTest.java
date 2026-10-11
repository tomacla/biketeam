package info.tomacla.biketeam.web.team.pedalons;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.UserService;
import info.tomacla.biketeam.service.pedalons.PedalonsMigrationException;
import info.tomacla.biketeam.service.pedalons.PedalonsMigrationService;
import info.tomacla.biketeam.web.ControllerTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AdminTeamPedalonsControllerTest {

    private static final String GRANT = "bmg_" + "Q".repeat(43);

    private PedalonsMigrationService migrationService;
    private Team team;
    private User user;
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        migrationService = mock(PedalonsMigrationService.class);

        AdminTeamPedalonsController controller = new AdminTeamPedalonsController();
        ReflectionTestUtils.setField(controller, "migrationService", migrationService);
        ReflectionTestUtils.setField(controller, "objectMapper", new ObjectMapper());
        mockMvc = ControllerTestSupport.mockMvc(controller);

        team = new Team();
        team.setId("n-peloton");
        team.setName("N'Peloton");
        TeamService teamService = mock(TeamService.class);
        when(teamService.get("n-peloton")).thenReturn(Optional.of(team));
        ReflectionTestUtils.setField(controller, "teamService", teamService);

        user = new User();
        user.setId("user-1");
        UserService userService = mock(UserService.class);
        when(userService.get("user-1")).thenReturn(Optional.of(user));
        ReflectionTestUtils.setField(controller, "userService", userService);
    }

    @Test
    public void testStartRedirectsTheBrowserToPedalons() throws Exception {
        when(migrationService.start(team, user, true, false))
                .thenReturn("https://www.pedalons.fr/migration-biketeam?request=abc.def.ghi");

        mockMvc.perform(post("/n-peloton/admin/pedalons/start")
                        .param("dryRun", "true")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("https://www.pedalons.fr/migration-biketeam?request=abc.def.ghi"));
    }

    @Test
    public void testUncheckedBoxesMeanDefinitiveWithoutReset() throws Exception {
        when(migrationService.start(any(), any(), anyBoolean(), anyBoolean())).thenReturn("https://p/migration-biketeam?request=t");

        mockMvc.perform(post("/n-peloton/admin/pedalons/start")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(status().isSeeOther());

        verify(migrationService).start(team, user, false, false);
    }

    @Test
    public void testRefusedStartComesBackWithTheReason() throws Exception {
        when(migrationService.start(any(), any(), anyBoolean(), anyBoolean()))
                .thenThrow(new PedalonsMigrationException("Une migration de ce groupe est déjà en cours."));

        mockMvc.perform(post("/n-peloton/admin/pedalons/start")
                        .param("dryRun", "true")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/n-peloton/admin/pedalons"))
                .andExpect(flash().attribute("errors", List.of("Une migration de ce groupe est déjà en cours.")));
    }

    @Test
    public void testSwitch() throws Exception {
        when(migrationService.switchTeam(team)).thenReturn("Le groupe a basculé vers Pédalons.");

        mockMvc.perform(post("/n-peloton/admin/pedalons/switch")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/n-peloton/admin/pedalons"))
                .andExpect(flash().attribute("infos", List.of("Le groupe a basculé vers Pédalons.")));
    }

    @Test
    public void testRefusedSwitchComesBackWithTheReason() throws Exception {
        when(migrationService.switchTeam(team))
                .thenThrow(new PedalonsMigrationException("Aucune migration définitive réussie n'attend d'être basculée."));

        mockMvc.perform(post("/n-peloton/admin/pedalons/switch")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/n-peloton/admin/pedalons"))
                .andExpect(flash().attribute("errors", List.of("Aucune migration définitive réussie n'attend d'être basculée.")));
    }

    @Test
    public void testCallbackTriggersAndHidesTheGrantFromReferers() throws Exception {
        when(migrationService.callback(team, user, "req-1", GRANT, null)).thenReturn("Essai de migration lancé sur Pédalons.");

        mockMvc.perform(get("/n-peloton/admin/pedalons/callback")
                        .param("request", "req-1")
                        .param("grant", GRANT)
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/n-peloton/admin/pedalons"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(flash().attribute("infos", List.of("Essai de migration lancé sur Pédalons.")));
    }

    @Test
    public void testCallbackCancelled() throws Exception {
        when(migrationService.callback(team, user, "req-1", null, "cancelled")).thenReturn("La migration a été annulée.");

        mockMvc.perform(get("/n-peloton/admin/pedalons/callback")
                        .param("request", "req-1")
                        .param("outcome", "cancelled")
                        .principal(ControllerTestSupport.authentication(user)))
                .andExpect(status().isSeeOther())
                .andExpect(flash().attribute("infos", List.of("La migration a été annulée.")));
    }

    @Test
    public void testCallbackErrorIsShownOnTheAdminPage() throws Exception {
        when(migrationService.callback(any(), any(), any(), any(), any()))
                .thenThrow(new PedalonsMigrationException("Demande de migration inconnue, expirée ou déjà traitée."));

        mockMvc.perform(get("/n-peloton/admin/pedalons/callback")
                        .param("request", "nope")
                        .param("grant", GRANT))
                .andExpect(status().isSeeOther())
                .andExpect(redirectedUrl("/n-peloton/admin/pedalons"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(flash().attribute("errors", List.of("Demande de migration inconnue, expirée ou déjà traitée.")));
    }

    @Test
    public void testStatusPage() throws Exception {
        when(migrationService.isEnabled()).thenReturn(true);
        when(migrationService.history("n-peloton")).thenReturn(List.of());

        mockMvc.perform(get("/n-peloton/admin/pedalons"))
                .andExpect(status().isOk())
                .andExpect(view().name("team_admin_pedalons"))
                .andExpect(model().attribute("running", false))
                .andExpect(model().attribute("pedalonsEnabled", true));

        verify(migrationService).summary(team);
    }

}
