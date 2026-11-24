package info.tomacla.biketeam.web.team.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.security.passkey.PasskeySuggestionService;
import info.tomacla.biketeam.service.NotificationService;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.export.TeamArchiveExporter;
import info.tomacla.biketeam.service.export.TeamExportSummary;
import info.tomacla.biketeam.service.export.TeamSnapshot;
import info.tomacla.biketeam.service.export.TeamSnapshotBuilder;
import info.tomacla.biketeam.service.url.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerView;

import java.io.ByteArrayInputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Page d'export (rendu reel du template et de l'entree de menu) et telechargement de l'archive.
 */
public class AdminTeamExportControllerTest {

    private TeamSnapshotBuilder snapshotBuilder;
    private Team team;
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() throws Exception {
        snapshotBuilder = mock(TeamSnapshotBuilder.class);
        when(snapshotBuilder.summary(any())).thenReturn(new TeamExportSummary(12, 2585, 665, 3, 19, 124, 40, true, false));

        AdminTeamExportController controller = new AdminTeamExportController();
        controller.setClock(Clock.fixed(Instant.parse("2026-11-24T10:00:00Z"), ZoneOffset.UTC));
        ReflectionTestUtils.setField(controller, "snapshotBuilder", snapshotBuilder);
        ReflectionTestUtils.setField(controller, "archiveExporter", new TeamArchiveExporter(snapshotBuilder, new ObjectMapper()));
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
        when(teamService.get("unknown")).thenReturn(Optional.empty());
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

    @Test
    public void testPageShowsContentAndDownloadLink() throws Exception {
        mockMvc.perform(get("/n-peloton/admin/export"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("585 parcours, avec leurs traces GPX")))
                .andExpect(content().string(containsString("/n-peloton/admin/export/download")))
                .andExpect(content().string(containsString("list-group-item-secondary\">Exporter les données</a>")));
    }

    @Test
    public void testDownloadIsAZipAttachment() throws Exception {
        TeamSnapshot.Snapshot snapshot = new TeamSnapshot.Snapshot(1, "2026-11-24T10:00:00Z",
                new TeamSnapshot.SnapshotTeam("n-peloton", "N'Peloton", "Nantes", "FR", "2019-03-02", "PUBLIC",
                        "Europe/Paris", null, null, null),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        when(snapshotBuilder.build(team, TeamArchiveExporter.ZIP_PATHS))
                .thenReturn(new TeamSnapshotBuilder.TeamExport(snapshot, List.of()));

        MvcResult result = mockMvc.perform(get("/n-peloton/admin/export/download"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/zip"))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"biketeam-n-peloton-2026-11-24.zip\""))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andReturn();

        List<String> names = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                names.add(entry.getName());
            }
        }
        assertEquals(List.of("README.txt", "team.json"), names);
    }

    @Test
    public void testUnknownTeam() throws Exception {
        mockMvc.perform(get("/unknown/admin/export/download"))
                .andExpect(status().isNotFound());
        verify(snapshotBuilder, never()).build(any(), any());
    }

}
