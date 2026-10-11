package info.tomacla.biketeam.service.pedalons.export;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.common.geo.Point;
import info.tomacla.biketeam.domain.AbstractDBTest;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapRepository;
import info.tomacla.biketeam.domain.place.Place;
import info.tomacla.biketeam.domain.place.PlaceRepository;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.publication.PublicationRegistration;
import info.tomacla.biketeam.domain.publication.PublicationRepository;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamRepository;
import info.tomacla.biketeam.domain.template.RideGroupTemplate;
import info.tomacla.biketeam.domain.template.RideTemplate;
import info.tomacla.biketeam.domain.template.RideTemplateRepository;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.domain.trip.TripRepository;
import info.tomacla.biketeam.domain.trip.TripStage;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserRepository;
import info.tomacla.biketeam.service.file.FileService;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Instantane d'une equipe sur une vraie base : nombre de requetes independant du nombre
 * d'elements (sorties, groupes, voyages, etapes, modeles, publications, participants).
 */
public class TeamSnapshotBuilderQueryCountTest extends AbstractDBTest {

    private static final String TEAM_ID = "snapshot-queries";

    /**
     * Une requete par type d'element : lieux, cartes, modeles, publications, sorties, voyages.
     */
    private static final long EXPECTED_QUERIES = 6;

    @TempDir
    Path repository;

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlaceRepository placeRepository;
    @Autowired
    private MapRepository mapRepository;
    @Autowired
    private RideTemplateRepository rideTemplateRepository;
    @Autowired
    private PublicationRepository publicationRepository;
    @Autowired
    private RideRepository rideRepository;
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    public void testQueryCountDoesNotDependOnTheNumberOfItems() {
        final Team team = new Team();
        team.setId(TEAM_ID);
        team.setCity("City");
        team.getDescription().setDescription("Description");
        team.setName("Snapshot");
        teamRepository.save(team);

        final List<User> users = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            User user = new User();
            user.setEmail("snapshot-" + i + "@example.org");
            user.setFirstName("First" + i);
            user.setLastName("Last" + i);
            users.add(userRepository.save(user));
        }

        final List<Place> places = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Place place = new Place();
            place.setTeamId(TEAM_ID);
            place.setName("Place " + i);
            place.setAddress("Address " + i);
            places.add(placeRepository.save(place));
        }

        final List<Map> maps = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Map map = new Map();
            map.setTeamId(TEAM_ID);
            map.setName("Map " + i);
            map.setStartPoint(new Point(47.2, -1.5));
            map.setEndPoint(new Point(47.3, -1.6));
            map.setTags(new ArrayList<>(List.of("tag-a", "tag-b")));
            maps.add(mapRepository.save(map));
        }

        for (int i = 0; i < 3; i++) {
            RideTemplate template = new RideTemplate();
            template.setTeamId(TEAM_ID);
            template.setName("Template " + i);
            template.setDescription("Template");
            template.setStartPlace(places.get(0));
            template.setEndPlace(places.get(1));
            for (int g = 0; g < 2; g++) {
                RideGroupTemplate group = new RideGroupTemplate();
                group.setName("G" + g);
                group.setAverageSpeed(25);
                group.setMeetingTime(LocalTime.of(8, g));
                template.addGroup(group);
            }
            rideTemplateRepository.save(template);
        }

        for (int i = 0; i < 3; i++) {
            Publication publication = new Publication();
            publication.setTeamId(TEAM_ID);
            publication.setTitle("Publication " + i);
            publication.setContent("Content");
            publication.setPublishedStatus(PublishedStatus.PUBLISHED);
            PublicationRegistration registration = new PublicationRegistration();
            registration.setPublication(publication);
            registration.setUserName("Name " + i);
            registration.setUserEmail("registration-" + i + "@example.org");
            publication.getRegistrations().add(registration);
            publicationRepository.save(publication);
        }

        for (int i = 0; i < 3; i++) {
            Ride ride = new Ride();
            ride.setTeamId(TEAM_ID);
            ride.setTitle("Ride " + i);
            ride.setDescription("Ride");
            ride.setDate(LocalDate.of(2025, 4, 1 + i));
            ride.setPublishedAt(ZonedDateTime.now());
            ride.setStartPlace(places.get(0));
            ride.setEndPlace(places.get(1));
            for (int g = 0; g < 2; g++) {
                RideGroup group = new RideGroup();
                group.setName("G" + g);
                group.setAverageSpeed(28);
                group.setMeetingTime(LocalTime.of(9, g));
                group.setMap(maps.get(g));
                users.forEach(group::addParticipant);
                ride.addGroup(group);
            }
            rideRepository.save(ride);
        }

        for (int i = 0; i < 3; i++) {
            Trip trip = new Trip();
            trip.setTeamId(TEAM_ID);
            trip.setTitle("Trip " + i);
            trip.setDescription("Trip");
            trip.setStartDate(LocalDate.of(2025, 7, 1));
            trip.setEndDate(LocalDate.of(2025, 7, 3));
            trip.setMeetingTime(LocalTime.NOON);
            trip.setPublishedAt(ZonedDateTime.now());
            trip.setPublishedStatus(PublishedStatus.PUBLISHED);
            trip.setStartPlace(places.get(0));
            trip.setEndPlace(places.get(1));
            for (int s = 0; s < 3; s++) {
                TripStage stage = new TripStage();
                stage.setName("S" + s);
                stage.setDate(LocalDate.of(2025, 7, 1 + s));
                stage.setMap(maps.get(s));
                trip.addStage(stage);
            }
            users.forEach(trip::addParticipant);
            tripRepository.save(trip);
        }

        entityManager.flush();
        entityManager.clear();

        final FileService fileService = new FileService(repository.toString());
        final TeamSnapshotBuilder builder = new TeamSnapshotBuilder(placeRepository, mapRepository, rideTemplateRepository,
                publicationRepository, rideRepository, tripRepository,
                new PedalonsExportFiles(fileService, new FileDigestCache(), mapRepository, rideRepository, tripRepository,
                        publicationRepository),
                transactionManager);

        final Statistics statistics = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        final boolean enabled = statistics.isStatisticsEnabled();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
        final TeamSnapshot.Snapshot snapshot;
        try {
            snapshot = builder.build(team);
        } finally {
            statistics.setStatisticsEnabled(enabled);
        }
        final long queries = statistics.getPrepareStatementCount();

        // contenu complet, relations comprises
        assertEquals(2, snapshot.places().size());
        assertEquals(3, snapshot.maps().size());
        assertEquals(2, snapshot.maps().get(0).tags().size());
        assertEquals(3, snapshot.rideTemplates().size());
        assertEquals(2, snapshot.rideTemplates().get(0).groups().size());
        assertEquals(3, snapshot.publications().size());
        assertEquals(3, snapshot.rides().size());
        assertEquals(2, snapshot.rides().get(0).groups().size());
        assertNotNull(snapshot.rides().get(0).groups().get(0).mapId());
        assertNotNull(snapshot.rides().get(0).startPlaceId());
        assertEquals(3, snapshot.trips().size());
        assertEquals(3, snapshot.trips().get(0).stages().size());
        assertNotNull(snapshot.trips().get(0).stages().get(0).mapId());

        // ni requete par element, ni chargement des participants ou des inscriptions
        assertEquals(EXPECTED_QUERIES, queries, "queries while building the snapshot");
    }

}
