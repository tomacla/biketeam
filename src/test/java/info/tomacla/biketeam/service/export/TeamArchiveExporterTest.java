package info.tomacla.biketeam.service.export;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.tomacla.biketeam.domain.team.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Archive ZIP de l'export des administrateurs : entrees, chemins coherents avec team.json,
 * elements supprimes ecartes.
 */
public class TeamArchiveExporterTest {

    @TempDir
    Path tmp;

    private TeamSnapshotBuilder builder;
    private TeamArchiveExporter exporter;
    private Team team;

    @BeforeEach
    public void setUp() {
        builder = mock(TeamSnapshotBuilder.class);
        exporter = new TeamArchiveExporter(builder, new ObjectMapper());
        team = new Team();
        team.setId("n-peloton");
    }

    private TeamSnapshot.FileRef ref(TeamFileKind kind, TeamExportFiles.ExportFile file) {
        return new TeamSnapshot.FileRef(kind.name(), TeamArchiveExporter.ZIP_PATHS.path(kind, "n-peloton", "x", file),
                file.fileName(), file.contentType(), 4, "md5");
    }

    private TeamExportFiles.ExportFile file(String name, String contentType, String content) throws IOException {
        Path path = tmp.resolve(name);
        Files.writeString(path, content);
        return new TeamExportFiles.ExportFile(path, name, contentType);
    }

    private static TeamSnapshot.SnapshotMap map(String id, boolean deleted, TeamSnapshot.FileRef gpx) {
        return new TeamSnapshot.SnapshotMap(id, id, "Parcours " + id, 42.0, "ROAD", 100, 100, null, null, null, null,
                List.of(), deleted, gpx);
    }

    private static TeamSnapshot.SnapshotRide ride(String id, boolean deleted, String mapId, TeamSnapshot.FileRef image) {
        return new TeamSnapshot.SnapshotRide(id, id, "2026-11-24", "Ride " + id, null, "REGULAR", "PUBLISHED", null,
                null, null, true, deleted, image,
                List.of(new TeamSnapshot.SnapshotRideGroup("g-" + id, "A", 28.0, "08:30:00", mapId)));
    }

    private static TeamSnapshot.SnapshotTeam team(TeamSnapshot.FileRef logo) {
        return new TeamSnapshot.SnapshotTeam("n-peloton", "N'Peloton", "Nantes", "FR", "2019-03-02", "PUBLIC",
                "Europe/Paris", null, null, logo);
    }

    private static Map<String, byte[]> unzip(byte[] zip) throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                assertNull(entries.put(entry.getName(), zis.readAllBytes()), "duplicate entry " + entry.getName());
            }
        }
        return entries;
    }

    private byte[] export() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        exporter.write(exporter.prepare(team), out);
        return out.toByteArray();
    }

    @Test
    public void testArchiveContainsSnapshotReadmeAndReferencedFiles() throws Exception {
        TeamExportFiles.ExportFile gpx = file("map-1.gpx", TeamExportFiles.GPX_CONTENT_TYPE, "<gpx/>");
        TeamExportFiles.ExportFile image = file("ride-1.jpg", "image/jpeg", "jpeg");
        TeamExportFiles.ExportFile logo = file("logo.png", "image/png", "png!");
        TeamSnapshot.FileRef gpxRef = ref(TeamFileKind.GPX, gpx);
        TeamSnapshot.FileRef imageRef = ref(TeamFileKind.RIDE_IMAGE, image);
        TeamSnapshot.FileRef logoRef = ref(TeamFileKind.LOGO, logo);

        TeamSnapshot.Snapshot snapshot = new TeamSnapshot.Snapshot(1, "2026-11-24T10:00:00Z", team(logoRef),
                List.of(), List.of(map("map-1", false, gpxRef)), List.of(), List.of(),
                List.of(ride("ride-1", false, "map-1", imageRef)), List.of());
        when(builder.build(eq(team), eq(TeamArchiveExporter.ZIP_PATHS))).thenReturn(new TeamSnapshotBuilder.TeamExport(snapshot,
                List.of(new TeamSnapshotBuilder.ExportedFile(logoRef, logo),
                        new TeamSnapshotBuilder.ExportedFile(gpxRef, gpx),
                        new TeamSnapshotBuilder.ExportedFile(imageRef, image))));

        Map<String, byte[]> entries = unzip(export());

        assertEquals(List.of("README.txt", "team.json", "files/logo/logo.png", "files/gpx/map-1.gpx",
                "files/ride-image/ride-1.jpg"), new ArrayList<>(entries.keySet()));
        assertEquals("<gpx/>", new String(entries.get("files/gpx/map-1.gpx"), StandardCharsets.UTF_8));
        assertEquals("jpeg", new String(entries.get("files/ride-image/ride-1.jpg"), StandardCharsets.UTF_8));

        JsonNode json = new ObjectMapper().readTree(entries.get("team.json"));
        assertEquals(1, json.get("schemaVersion").asInt());
        assertEquals("files/gpx/map-1.gpx", json.get("maps").get(0).get("gpx").get("path").asText());
        assertEquals("files/ride-image/ride-1.jpg", json.get("rides").get(0).get("image").get("path").asText());
        assertEquals("files/logo/logo.png", json.get("team").get("logo").get("path").asText());
        // chaque chemin de team.json existe dans l'archive
        json.findValues("path").forEach(p -> assertTrue(entries.containsKey(p.asText()), p.asText()));

        String readme = new String(entries.get("README.txt"), StandardCharsets.UTF_8);
        assertTrue(readme.contains("N'Peloton"));
        assertTrue(readme.contains("Aucune donnée personnelle"));
    }

    @Test
    public void testDeletedElementsAreLeftOutExceptMapsStillInUse() throws Exception {
        TeamSnapshot.Snapshot snapshot = new TeamSnapshot.Snapshot(1, "2026-11-24T10:00:00Z", team(null),
                List.of(),
                List.of(map("kept", false, null), map("deleted-used", true, null), map("deleted-unused", true, null),
                        map("used-by-deleted-ride", true, null)),
                List.of(),
                List.of(new TeamSnapshot.SnapshotPublication("p1", "Publiée", "", "PUBLISHED", null, false, null),
                        new TeamSnapshot.SnapshotPublication("p2", "Supprimée", "", "PUBLISHED", null, true, null)),
                List.of(ride("r1", false, "deleted-used", null), ride("r2", true, "used-by-deleted-ride", null)),
                List.of(new TeamSnapshot.SnapshotTrip("t1", "t1", "2026-06-01", "2026-06-03", null, "REGULAR", "PUBLISHED",
                        null, "Trip", null, null, null, null, true, true, null, List.of())));
        when(builder.build(eq(team), eq(TeamArchiveExporter.ZIP_PATHS)))
                .thenReturn(new TeamSnapshotBuilder.TeamExport(snapshot, List.of()));

        JsonNode json = new ObjectMapper().readTree(unzip(export()).get("team.json"));

        assertEquals(List.of("kept", "deleted-used"), ids(json.get("maps")));
        assertTrue(json.get("maps").get(1).get("deleted").asBoolean());
        assertEquals(List.of("p1"), ids(json.get("publications")));
        assertEquals(List.of("r1"), ids(json.get("rides")));
        assertEquals(List.of(), ids(json.get("trips")));
    }

    @Test
    public void testVanishedFileIsSkipped() throws Exception {
        TeamExportFiles.ExportFile gpx = new TeamExportFiles.ExportFile(tmp.resolve("gone.gpx"), "gone.gpx",
                TeamExportFiles.GPX_CONTENT_TYPE);
        TeamSnapshot.FileRef gpxRef = ref(TeamFileKind.GPX, gpx);
        TeamSnapshot.Snapshot snapshot = new TeamSnapshot.Snapshot(1, "2026-11-24T10:00:00Z", team(null),
                List.of(), List.of(map("gone", false, gpxRef)), List.of(), List.of(), List.of(), List.of());
        when(builder.build(eq(team), eq(TeamArchiveExporter.ZIP_PATHS))).thenReturn(new TeamSnapshotBuilder.TeamExport(snapshot,
                List.of(new TeamSnapshotBuilder.ExportedFile(gpxRef, gpx))));

        assertEquals(List.of("README.txt", "team.json"), new ArrayList<>(unzip(export()).keySet()));
    }

    private static List<String> ids(JsonNode array) {
        List<String> ids = new ArrayList<>();
        array.forEach(n -> ids.add(n.get("id").asText()));
        return ids;
    }

}
