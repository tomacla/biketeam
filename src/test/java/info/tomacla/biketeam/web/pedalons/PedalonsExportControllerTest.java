package info.tomacla.biketeam.web.pedalons;

import info.tomacla.biketeam.domain.pedalons.PedalonsMigrationRepository;
import info.tomacla.biketeam.domain.pedalons.PedalonsMigrationStatus;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.pedalons.PedalonsProperties;
import info.tomacla.biketeam.service.pedalons.export.PedalonsExportFiles;
import info.tomacla.biketeam.service.pedalons.export.PedalonsFileKind;
import info.tomacla.biketeam.service.pedalons.export.TeamSnapshot;
import info.tomacla.biketeam.service.pedalons.export.TeamSnapshotBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import info.tomacla.biketeam.web.WebExceptionHandler;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API d'export M2M (section 6 du plan) : fonction eteinte, secret, garde de migration active.
 */
public class PedalonsExportControllerTest {

    private static final String KEY = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=";
    private static final String SECRET = "export-secret";

    @TempDir
    Path tmp;

    private TeamService teamService;
    private PedalonsMigrationRepository migrationRepository;
    private TeamSnapshotBuilder snapshotBuilder;
    private PedalonsExportFiles files;
    private Team team;

    @BeforeEach
    public void setUp() {
        teamService = mock(TeamService.class);
        migrationRepository = mock(PedalonsMigrationRepository.class);
        snapshotBuilder = mock(TeamSnapshotBuilder.class);
        files = mock(PedalonsExportFiles.class);

        team = new Team();
        team.setId("n-peloton");
        when(teamService.get("n-peloton")).thenReturn(Optional.of(team));
        when(teamService.get("unknown")).thenReturn(Optional.empty());
        when(migrationRepository.existsByTeamIdAndStatusIn("n-peloton", PedalonsMigrationStatus.ACTIVE)).thenReturn(true);
    }

    private MockMvc mockMvc(boolean enabled) {
        PedalonsProperties properties = enabled
                ? new PedalonsProperties("https://p", "https://i", KEY, "trigger", SECRET, "https://b")
                : new PedalonsProperties("", "", "", "", "", "https://b");
        return MockMvcBuilders.standaloneSetup(
                new PedalonsExportController(properties, teamService, migrationRepository, snapshotBuilder, files)).build();
    }

    @Test
    public void testDisabledFeatureAnswers404WithoutBody() throws Exception {
        mockMvc(false).perform(get("/internal/pedalons/teams/n-peloton/snapshot")
                        .header(PedalonsExportController.SECRET_HEADER, SECRET))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
        verifyNoInteractions(snapshotBuilder);
    }

    @Test
    public void testMissingOrWrongSecretIsUnauthorized() throws Exception {
        MockMvc mockMvc = mockMvc(true);
        mockMvc.perform(get("/internal/pedalons/teams/n-peloton/snapshot"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mockMvc.perform(get("/internal/pedalons/teams/n-peloton/snapshot")
                        .header(PedalonsExportController.SECRET_HEADER, "trigger"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(snapshotBuilder);
    }

    @Test
    public void testUnknownOrDeletedTeam() throws Exception {
        MockMvc mockMvc = mockMvc(true);
        mockMvc.perform(get("/internal/pedalons/teams/unknown/snapshot")
                        .header(PedalonsExportController.SECRET_HEADER, SECRET))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TEAM_NOT_FOUND"));

        team.setDeletion(true);
        mockMvc.perform(get("/internal/pedalons/teams/n-peloton/snapshot")
                        .header(PedalonsExportController.SECRET_HEADER, SECRET))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TEAM_NOT_FOUND"));
    }

    @Test
    public void testTeamWithoutActiveMigrationIsNotExported() throws Exception {
        when(migrationRepository.existsByTeamIdAndStatusIn("n-peloton", PedalonsMigrationStatus.ACTIVE)).thenReturn(false);
        mockMvc(true).perform(get("/internal/pedalons/teams/n-peloton/snapshot")
                        .header(PedalonsExportController.SECRET_HEADER, SECRET))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("NO_ACTIVE_MIGRATION"));
        verifyNoInteractions(snapshotBuilder);
    }

    @Test
    public void testSnapshotIsServedAsJson() throws Exception {
        TeamSnapshot.SnapshotTeam snapshotTeam = new TeamSnapshot.SnapshotTeam("n-peloton", "N'Peloton", "Nantes", "FR",
                "2019-03-02", "PUBLIC", "Europe/Paris", null, null, null);
        when(snapshotBuilder.build(team)).thenReturn(new TeamSnapshot.Snapshot(1, "2026-09-22T10:16:11.123Z", snapshotTeam,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of()));

        mockMvc(true).perform(get("/internal/pedalons/teams/n-peloton/snapshot")
                        .header(PedalonsExportController.SECRET_HEADER, SECRET))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.schemaVersion").value(1))
                .andExpect(jsonPath("$.team.id").value("n-peloton"))
                .andExpect(jsonPath("$.team.logo").hasJsonPath())
                .andExpect(jsonPath("$.team.logo").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.maps").isArray());
    }

    @Test
    public void testFileIsStreamedWithEtag() throws Exception {
        Path gpx = tmp.resolve("map-1.gpx");
        Files.writeString(gpx, "<gpx/>");
        PedalonsExportFiles.ExportFile file = new PedalonsExportFiles.ExportFile(gpx, "map-1.gpx", "application/gpx+xml");
        when(files.resolve(PedalonsFileKind.GPX, "n-peloton", "map-1")).thenReturn(Optional.of(file));
        when(files.md5(file)).thenReturn("9e107d9d372bb6826bd81d3542a419d6");

        mockMvc(true).perform(get("/internal/pedalons/teams/n-peloton/files/gpx/map-1")
                        .header(PedalonsExportController.SECRET_HEADER, SECRET))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/gpx+xml"))
                .andExpect(header().string("Content-Length", "6"))
                .andExpect(header().string("ETag", "\"9e107d9d372bb6826bd81d3542a419d6\""))
                .andExpect(content().string("<gpx/>"));
    }

    @Test
    public void testFileNotFound() throws Exception {
        when(files.resolve(any(), eq("n-peloton"), any())).thenReturn(Optional.empty());
        MockMvc mockMvc = mockMvc(true);

        mockMvc.perform(get("/internal/pedalons/teams/n-peloton/files/gpx/map-of-another-team")
                        .header(PedalonsExportController.SECRET_HEADER, SECRET))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FILE_NOT_FOUND"));

        // type inconnu : jamais interprete comme un chemin
        mockMvc.perform(get("/internal/pedalons/teams/n-peloton/files/user-images/u1")
                        .header(PedalonsExportController.SECRET_HEADER, SECRET))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FILE_NOT_FOUND"));
    }

    @Test
    public void testUnexpectedErrorIsA500NotARedirect() throws Exception {
        when(snapshotBuilder.build(team)).thenThrow(new UncheckedIOException(new IOException("disk")));
        when(files.resolve(any(), eq("n-peloton"), any())).thenThrow(new IllegalStateException("db down"));
        // le gestionnaire global de biketeam redirige vers l'accueil : il ne doit pas voir l'export
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                        new PedalonsExportController(new PedalonsProperties("https://p", "https://i", KEY, "trigger", SECRET, "https://b"),
                                teamService, migrationRepository, snapshotBuilder, files))
                .setControllerAdvice(new WebExceptionHandler())
                .build();

        mockMvc.perform(get("/internal/pedalons/teams/n-peloton/snapshot")
                        .header(PedalonsExportController.SECRET_HEADER, SECRET))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
        mockMvc.perform(get("/internal/pedalons/teams/n-peloton/files/gpx/map-1")
                        .header(PedalonsExportController.SECRET_HEADER, SECRET))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
        // les erreurs metier gardent leur reponse
        mockMvc.perform(get("/internal/pedalons/teams/unknown/snapshot")
                        .header(PedalonsExportController.SECRET_HEADER, SECRET))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TEAM_NOT_FOUND"));
    }

}
