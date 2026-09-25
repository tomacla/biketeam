package info.tomacla.biketeam.service.pedalons.export;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.common.geo.Point;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapRepository;
import info.tomacla.biketeam.domain.map.WindDirection;
import info.tomacla.biketeam.domain.place.Place;
import info.tomacla.biketeam.domain.place.PlaceRepository;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.publication.PublicationRepository;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.Visibility;
import info.tomacla.biketeam.domain.template.RideGroupTemplate;
import info.tomacla.biketeam.domain.template.RideTemplate;
import info.tomacla.biketeam.domain.template.RideTemplateRepository;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.domain.trip.TripRepository;
import info.tomacla.biketeam.domain.trip.TripStage;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.file.FileService;
import info.tomacla.biketeam.service.pedalons.PedalonsSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Instantane d'une equipe : champs, formats (section 6.1 du plan), ordre d'affichage (6.3) et
 * exclusion de toute donnee de personne.
 */
public class TeamSnapshotBuilderTest {

    private static final String TEAM_ID = "n-peloton";

    @TempDir
    Path repository;

    private PlaceRepository placeRepository;
    private MapRepository mapRepository;
    private RideTemplateRepository rideTemplateRepository;
    private PublicationRepository publicationRepository;
    private RideRepository rideRepository;
    private TripRepository tripRepository;

    private FileService fileService;
    private TeamSnapshotBuilder builder;
    /** Etat de la transaction au moment de chaque calcul d'empreinte. */
    private final List<Boolean> digestInTransaction = new java.util.ArrayList<>();
    /** Etat de la transaction au moment de chaque lecture en base. */
    private final List<Boolean> readInTransaction = new java.util.ArrayList<>();
    private Team team;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() throws Exception {

        placeRepository = mock(PlaceRepository.class);
        mapRepository = mock(MapRepository.class);
        rideTemplateRepository = mock(RideTemplateRepository.class);
        publicationRepository = mock(PublicationRepository.class);
        rideRepository = mock(RideRepository.class);
        tripRepository = mock(TripRepository.class);

        fileService = spy(new FileService(repository.toString()));
        FileDigestCache digestCache = new FileDigestCache() {
            @Override
            public String md5(Path file, long size, long modifiedMillis) {
                digestInTransaction.add(TransactionSynchronizationManager.isActualTransactionActive());
                return super.md5(file, size, modifiedMillis);
            }
        };
        PedalonsExportFiles files = new PedalonsExportFiles(fileService, digestCache, mapRepository,
                rideRepository, tripRepository, publicationRepository);

        builder = new TeamSnapshotBuilder(placeRepository, mapRepository, rideTemplateRepository, publicationRepository,
                rideRepository, tripRepository, files, new StubTransactionManager());
        builder.setClock(Clock.fixed(Instant.parse("2026-09-22T10:16:11.123Z"), ZoneOffset.UTC));

        team = new Team();
        team.setId(TEAM_ID);
        team.setName("N'Peloton");
        team.setCity("Nantes");
        team.setCreatedAt(LocalDate.of(2019, 3, 2));
        team.setVisibility(Visibility.PRIVATE_UNLISTED);
        team.getConfiguration().setTimezone("America/Montreal");
        team.getConfiguration().setMarkdownPage("# FAQ\nQuestions");
        team.getDescription().setDescription("Texte libre");
        team.getDescription().setAddressCity("Nantes");
        team.getDescription().setEmail("contact@example.org");

        // un membre, pour verifier qu'aucune personne ne fuit dans l'instantane
        User member = new User();
        member.setId("user-1");
        member.setFirstName("Jean");
        member.setLastName("Membre");
        member.setEmail("jean.membre@example.org");

        Place placeB = place("place-b", "Place B", null);
        Place placeA = place("place-a", "Place A", new Point(47.2129, -1.5588));
        when(placeRepository.findByTeamId(TEAM_ID)).thenReturn(List.of(placeB, placeA));

        Map map = map("map-1", false);
        map.setTags(List.of("lac", " ", "plat"));
        Map deletedMap = map("map-0", true);
        when(mapRepository.findByTeamId(TEAM_ID)).thenReturn(List.of(map, deletedMap));
        writeFile("gpx", "map-1.gpx", "<gpx>1</gpx>");
        writeFile("gpx", "map-0.gpx", "<gpx>0</gpx>");

        RideTemplate template = new RideTemplate();
        ReflectionTestUtils.setField(template, "id", "template-1");
        template.setTeamId(TEAM_ID);
        template.setName("Sortie du samedi");
        template.setDescription("Samedi");
        template.setIncrement(7);
        template.setStartPlace(placeA);
        template.addGroup(groupTemplate("gt-2", "B", LocalTime.of(8, 0)));
        template.addGroup(groupTemplate("gt-1", "A", LocalTime.of(9, 0)));
        when(rideTemplateRepository.findByTeamId(TEAM_ID)).thenReturn(List.of(template));

        Publication publication = new Publication();
        ReflectionTestUtils.setField(publication, "id", "pub-1");
        publication.setTeamId(TEAM_ID);
        publication.setTitle("AG 2025");
        publication.setContent("Contenu");
        publication.setPublishedStatus(PublishedStatus.PUBLISHED);
        publication.setPublishedAt(ZonedDateTime.of(2025, 1, 10, 19, 0, 0, 0, ZoneId.of("Europe/Paris")));
        publication.setAllowRegistration(true);
        when(publicationRepository.findByTeamId(TEAM_ID)).thenReturn(List.of(publication));
        writeFile("pub-images", "pub-1.jpeg", "jpeg");

        Ride ride = new Ride();
        ReflectionTestUtils.setField(ride, "id", "ride-1");
        ride.setTeamId(TEAM_ID);
        ride.setPermalink("sortie-du-12");
        ride.setDate(LocalDate.of(2025, 4, 12));
        ride.setTitle("Sortie du 12");
        ride.setDescription("Description");
        ride.setPublishedStatus(PublishedStatus.PUBLISHED);
        ride.setPublishedAt(ZonedDateTime.of(2025, 4, 5, 9, 0, 0, 0, ZoneOffset.UTC));
        ride.setListedInFeed(true);
        ride.setStartPlace(placeA);
        RideGroup late = rideGroup("g-late", "A", LocalTime.of(9, 0), null);
        RideGroup earlyB = rideGroup("g-early-b", "B", LocalTime.of(8, 0), map);
        RideGroup earlyA = rideGroup("g-early-a", "A", LocalTime.of(8, 0), null);
        RideGroup noTime = rideGroup("g-none", "0", null, null);
        earlyA.addParticipant(member);
        ride.addGroup(late);
        ride.addGroup(earlyB);
        ride.addGroup(noTime);
        ride.addGroup(earlyA);
        when(rideRepository.findByTeamId(TEAM_ID)).thenReturn(List.of(ride));
        writeFile("ride-images", "ride-1.png", "png");

        Trip trip = new Trip();
        ReflectionTestUtils.setField(trip, "id", "trip-1");
        trip.setTeamId(TEAM_ID);
        trip.setPermalink("tour-2025");
        trip.setStartDate(LocalDate.of(2025, 7, 1));
        trip.setEndDate(LocalDate.of(2025, 7, 5));
        trip.setMeetingTime(LocalTime.of(7, 30));
        trip.setTitle("Tour 2025");
        trip.setDescription("Tour");
        trip.setPublishedStatus(PublishedStatus.PUBLISHED);
        trip.setPublishedAt(ZonedDateTime.of(2025, 3, 1, 12, 0, 0, 0, ZoneOffset.UTC));
        trip.addStage(stage("s-2b", LocalDate.of(2025, 7, 2), "Étape 2 bis", map));
        trip.addStage(stage("s-1", LocalDate.of(2025, 7, 1), "Étape 1", null));
        trip.addStage(stage("s-2a", LocalDate.of(2025, 7, 2), "Étape 2", null));
        trip.addParticipant(member);
        when(tripRepository.findByTeamId(TEAM_ID)).thenReturn(List.of(trip));

        writeFile("misc", "logo.png", "logo");
    }

    private void writeFile(String directory, String fileName, String content) throws Exception {
        Path dir = repository.resolve(directory).resolve(TEAM_ID);
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(fileName), content, StandardCharsets.UTF_8);
    }

    private static Place place(String id, String name, Point point) {
        Place place = new Place();
        place.setId(id);
        place.setTeamId(TEAM_ID);
        place.setName(name);
        place.setAddress("Adresse " + name);
        place.setPoint(point);
        place.setStartPlace(true);
        return place;
    }

    private static Map map(String id, boolean deleted) {
        Map map = new Map();
        map.setId(id);
        map.setTeamId(TEAM_ID);
        map.setPermalink("boucle-" + id);
        map.setName("Boucle " + id);
        map.setLength(84210.0);
        map.setPositiveElevation(612.0);
        map.setNegativeElevation(611.0);
        map.setPostedAt(LocalDate.of(2023, 4, 11));
        map.setStartPoint(new Point(47.21, -1.55));
        map.setEndPoint(new Point(47.22, -1.56));
        map.setWindDirection(WindDirection.NORTH_EAST);
        map.setDeletion(deleted);
        return map;
    }

    private static RideGroupTemplate groupTemplate(String id, String name, LocalTime meetingTime) {
        RideGroupTemplate group = new RideGroupTemplate();
        group.setId(id);
        group.setName(name);
        group.setAverageSpeed(28.0);
        group.setMeetingTime(meetingTime);
        return group;
    }

    private static RideGroup rideGroup(String id, String name, LocalTime meetingTime, Map map) {
        RideGroup group = new RideGroup();
        group.setId(id);
        group.setName(name);
        group.setAverageSpeed(30.0);
        // une heure nulle existe en base alors que le setter la refuse
        ReflectionTestUtils.setField(group, "meetingTime", meetingTime);
        group.setMap(map);
        return group;
    }

    private static TripStage stage(String id, LocalDate date, String name, Map map) {
        TripStage stage = new TripStage();
        stage.setId(id);
        stage.setDate(date);
        stage.setName(name);
        stage.setMap(map);
        return stage;
    }

    private JsonNode snapshot() {
        return objectMapper.valueToTree(builder.build(team));
    }

    @Test
    public void testTeamFields() {
        JsonNode json = snapshot();

        assertEquals(1, json.get("schemaVersion").asInt());
        assertEquals("2026-09-22T10:16:11.123Z", json.get("generatedAt").asText());

        JsonNode t = json.get("team");
        assertEquals(TEAM_ID, t.get("id").asText());
        assertEquals("N'Peloton", t.get("name").asText());
        assertEquals("FR", t.get("country").asText());
        assertEquals("2019-03-02", t.get("createdAt").asText());
        assertEquals("PRIVATE_UNLISTED", t.get("visibility").asText());
        assertEquals("America/Montreal", t.get("timezone").asText());
        assertEquals("# FAQ\nQuestions", t.get("markdownPage").asText());
        assertEquals("Nantes", t.get("description").get("addressPostalCity").asText());
        // champ documente toujours present, a null
        assertTrue(t.get("description").has("twitter"));
        assertTrue(t.get("description").get("twitter").isNull());

        JsonNode logo = t.get("logo");
        assertEquals("LOGO", logo.get("kind").asText());
        assertEquals("/internal/pedalons/teams/n-peloton/files/logo/n-peloton", logo.get("path").asText());
        assertEquals("logo.png", logo.get("fileName").asText());
        assertEquals("image/png", logo.get("contentType").asText());
        assertEquals(4, logo.get("size").asLong());
        assertEquals(FileDigestCache.compute(repository.resolve("misc").resolve(TEAM_ID).resolve("logo.png")),
                logo.get("md5").asText());
        assertEquals(32, logo.get("md5").asText().length());
    }

    @Test
    public void testTopLevelArraysAreSortedById() {
        JsonNode json = snapshot();
        assertEquals("place-a", json.get("places").get(0).get("id").asText());
        assertEquals("place-b", json.get("places").get(1).get("id").asText());
        assertTrue(json.get("places").get(1).get("lat").isNull());
        assertEquals(47.2129, json.get("places").get(0).get("lat").asDouble());
        assertEquals("map-0", json.get("maps").get(0).get("id").asText());
        assertEquals("map-1", json.get("maps").get(1).get("id").asText());
    }

    @Test
    public void testMaps() throws Exception {
        JsonNode maps = snapshot().get("maps");

        JsonNode deleted = maps.get(0);
        assertTrue(deleted.get("deleted").asBoolean());
        // carte supprimee : exportee (Pedalons met son parcours a la corbeille) mais sans GPX
        assertTrue(deleted.get("gpx").isNull());

        JsonNode map = maps.get(1);
        assertFalse(map.get("deleted").asBoolean());
        assertEquals("NORTH_EAST", map.get("windDirection").asText());
        assertEquals("2023-04-11", map.get("postedAt").asText());
        assertEquals(84210.0, map.get("length").asDouble());
        assertEquals(47.21, map.get("startPoint").get("lat").asDouble());
        assertEquals(List.of("lac", "plat"), List.of(map.get("tags").get(0).asText(), map.get("tags").get(1).asText()));
        assertEquals(2, map.get("tags").size());

        JsonNode gpx = map.get("gpx");
        assertEquals("GPX", gpx.get("kind").asText());
        assertEquals("/internal/pedalons/teams/n-peloton/files/gpx/map-1", gpx.get("path").asText());
        assertEquals("map-1.gpx", gpx.get("fileName").asText());
        assertEquals("application/gpx+xml", gpx.get("contentType").asText());
        assertEquals(Files.size(repository.resolve("gpx").resolve(TEAM_ID).resolve("map-1.gpx")), gpx.get("size").asLong());
    }

    @Test
    public void testRideGroupsFollowBiketeamDisplayOrder() {
        JsonNode ride = snapshot().get("rides").get(0);

        assertEquals("2025-04-12", ride.get("date").asText());
        assertEquals("2025-04-05T09:00:00Z", ride.get("publishedAt").asText());
        assertEquals("place-a", ride.get("startPlaceId").asText());
        assertTrue(ride.get("endPlaceId").isNull());
        assertEquals("RIDE_IMAGE", ride.get("image").get("kind").asText());
        assertEquals("/internal/pedalons/teams/n-peloton/files/ride-image/ride-1", ride.get("image").get("path").asText());

        JsonNode groups = ride.get("groups");
        assertEquals(4, groups.size());
        // heure, puis nom ; heure nulle en dernier
        assertEquals("g-early-a", groups.get(0).get("id").asText());
        assertEquals("g-early-b", groups.get(1).get("id").asText());
        assertEquals("g-late", groups.get(2).get("id").asText());
        assertEquals("g-none", groups.get(3).get("id").asText());
        // secondes toujours presentes
        assertEquals("08:00:00", groups.get(0).get("meetingTime").asText());
        assertTrue(groups.get(3).get("meetingTime").isNull());
        assertEquals("map-1", groups.get(1).get("mapId").asText());
    }

    @Test
    public void testTripStagesAndTemplatesOrder() {
        JsonNode json = snapshot();

        JsonNode trip = json.get("trips").get(0);
        assertEquals("07:30:00", trip.get("meetingTime").asText());
        assertTrue(trip.get("image").isNull());
        JsonNode stages = trip.get("stages");
        assertEquals("s-1", stages.get(0).get("id").asText());
        assertEquals("s-2a", stages.get(1).get("id").asText());
        assertEquals("s-2b", stages.get(2).get("id").asText());
        assertEquals("2025-07-02", stages.get(2).get("date").asText());
        assertEquals("map-1", stages.get(2).get("mapId").asText());

        // modeles : nom seul, l'heure n'intervient pas
        JsonNode groups = json.get("rideTemplates").get(0).get("groups");
        assertEquals("gt-1", groups.get(0).get("id").asText());
        assertEquals("gt-2", groups.get(1).get("id").asText());
        assertEquals("09:00:00", groups.get(0).get("meetingTime").asText());
        assertEquals(7, json.get("rideTemplates").get(0).get("increment").asInt());
    }

    @Test
    public void testPublicationImageUsesJpegAndInstantIsUtc() {
        JsonNode publication = snapshot().get("publications").get(0);
        assertEquals("2025-01-10T18:00:00Z", publication.get("publishedAt").asText());
        assertEquals("PUBLISHED", publication.get("publishedStatus").asText());
        assertEquals("pub-1.jpeg", publication.get("image").get("fileName").asText());
        assertEquals("image/jpeg", publication.get("image").get("contentType").asText());
        assertFalse(publication.has("allowRegistration"));
        assertFalse(publication.has("registrations"));
    }

    @Test
    public void testNoPersonalDataIsExported() throws Exception {
        String json = objectMapper.writeValueAsString(builder.build(team));
        assertFalse(json.contains("user-1"));
        assertFalse(json.contains("jean.membre@example.org"));
        assertFalse(json.contains("Membre"));
        assertFalse(json.contains("participants"));
        assertFalse(json.contains("\"roles\""));
        assertFalse(json.contains("messages"));
    }

    @Test
    public void testFilesAreFoundWithOneListingPerDirectory() {
        // plusieurs elements avec et sans fichier : toujours une liste par repertoire
        Ride other = new Ride();
        ReflectionTestUtils.setField(other, "id", "ride-2");
        other.setTeamId(TEAM_ID);
        Ride ride = rideRepository.findByTeamId(TEAM_ID).get(0);
        when(rideRepository.findByTeamId(TEAM_ID)).thenReturn(List.of(ride, other));
        clearInvocations(fileService);

        JsonNode json = snapshot();

        assertEquals("ride-1.png", json.get("rides").get(0).get("image").get("fileName").asText());
        assertTrue(json.get("rides").get(1).get("image").isNull());
        assertEquals("map-1.gpx", json.get("maps").get(1).get("gpx").get("fileName").asText());
        assertEquals("pub-1.jpeg", json.get("publications").get(0).get("image").get("fileName").asText());
        assertEquals("logo.png", json.get("team").get("logo").get("fileName").asText());

        verify(fileService, never()).fileExists(anyString(), anyString(), anyString());
        verify(fileService, never()).getFile(anyString(), anyString(), anyString());
        for (String directory : List.of("gpx", "ride-images", "trip-images", "pub-images", "misc")) {
            verify(fileService, atMostOnce()).listFileNames(directory, TEAM_ID);
        }
        verify(fileService, times(5)).listFileNames(anyString(), anyString());
    }

    @Test
    public void testSummaryCountsNonDeletedItems() {
        when(placeRepository.countByTeamId(TEAM_ID)).thenReturn(2L);
        when(mapRepository.countByTeamIdAndDeletionFalse(TEAM_ID)).thenReturn(1L);
        when(rideRepository.countByTeamIdAndDeletionFalse(TEAM_ID)).thenReturn(1L);
        when(rideTemplateRepository.countByTeamId(TEAM_ID)).thenReturn(1L);
        when(tripRepository.countByTeamIdAndDeletionFalse(TEAM_ID)).thenReturn(1L);
        when(tripRepository.countStagesOfActiveTrips(TEAM_ID)).thenReturn(3L);
        when(publicationRepository.countByTeamIdAndDeletionFalse(TEAM_ID)).thenReturn(1L);

        PedalonsSummary summary = builder.summary(team);
        assertEquals(2, summary.getPlaces());
        assertEquals(1, summary.getRoutes());
        assertEquals(3, summary.getTripStages());
        assertTrue(summary.isFaqPage());
        assertTrue(summary.isLogo());
    }

    @Test
    public void testFileResolutionChecksOwnershipAndDeletion() {
        PedalonsExportFiles files = (PedalonsExportFiles) ReflectionTestUtils.getField(builder, "files");

        Map foreign = map("map-9", false);
        foreign.setTeamId("other-team");
        when(mapRepository.findById("map-9")).thenReturn(java.util.Optional.of(foreign));
        Map deleted = map("map-0", true);
        when(mapRepository.findById("map-0")).thenReturn(java.util.Optional.of(deleted));
        Map own = map("map-1", false);
        when(mapRepository.findById("map-1")).thenReturn(java.util.Optional.of(own));

        assertTrue(files.resolve(PedalonsFileKind.GPX, TEAM_ID, "map-9").isEmpty());
        assertTrue(files.resolve(PedalonsFileKind.GPX, TEAM_ID, "map-0").isEmpty());
        assertTrue(files.resolve(PedalonsFileKind.GPX, TEAM_ID, "map-1").isPresent());
        assertTrue(files.resolve(PedalonsFileKind.LOGO, TEAM_ID, TEAM_ID).isPresent());
        assertTrue(files.resolve(PedalonsFileKind.LOGO, TEAM_ID, "other-team").isEmpty());
    }

    @Test
    public void testUnreadableFileIsExportedAsMissing() {
        PedalonsExportFiles files = (PedalonsExportFiles) ReflectionTestUtils.getField(builder, "files");
        PedalonsExportFiles.ExportFile vanished = new PedalonsExportFiles.ExportFile(
                repository.resolve("vanished.gpx"), "vanished.gpx", PedalonsExportFiles.GPX_CONTENT_TYPE);

        // fichier disparu entre la resolution et la lecture : l'element part sans fichier
        assertNull(files.toRef(PedalonsFileKind.GPX, TEAM_ID, "map-1", vanished));
    }

    /**
     * Gestionnaire de transactions sans ressource : suffit a ce que
     * TransactionSynchronizationManager.isActualTransactionActive() reflete la transaction.
     */
    private static final class StubTransactionManager extends AbstractPlatformTransactionManager {
        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
        }
    }

    @Test
    public void testFilesAreDigestedOutsideTheReadTransaction() {
        when(mapRepository.findByTeamId(TEAM_ID)).thenAnswer(i -> {
            readInTransaction.add(TransactionSynchronizationManager.isActualTransactionActive());
            Map map = map("map-1", false);
            return List.of(map);
        });
        digestInTransaction.clear();

        TeamSnapshot.Snapshot snapshot = builder.build(team);

        assertEquals(List.of(true), readInTransaction, "entities are read in a transaction");
        assertFalse(digestInTransaction.isEmpty());
        assertFalse(digestInTransaction.contains(true), "no file digest while a transaction is open");
        assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
        // references completes apres coup : taille et empreinte
        TeamSnapshot.FileRef gpx = snapshot.maps().get(0).gpx();
        assertEquals(FileDigestCache.compute(repository.resolve("gpx").resolve(TEAM_ID).resolve("map-1.gpx")), gpx.md5());
        assertEquals("<gpx>1</gpx>".length(), gpx.size());
        assertNotNull(snapshot.team().logo().md5());
        assertNotNull(snapshot.publications().get(0).image().md5());
    }

    @Test
    public void testSetOfGroupsIsNeverNull() {
        Ride ride = new Ride();
        ReflectionTestUtils.setField(ride, "id", "ride-2");
        ride.setTeamId(TEAM_ID);
        ReflectionTestUtils.setField(ride, "groups", Set.of());
        when(rideRepository.findByTeamId(TEAM_ID)).thenReturn(List.of(ride));

        JsonNode groups = snapshot().get("rides").get(0).get("groups");
        assertTrue(groups.isArray());
        assertEquals(0, groups.size());
    }

}
