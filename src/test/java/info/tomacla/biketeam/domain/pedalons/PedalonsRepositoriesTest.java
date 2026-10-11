package info.tomacla.biketeam.domain.pedalons;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.domain.AbstractDBTest;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapRepository;
import info.tomacla.biketeam.domain.team.PedalonsState;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamRepository;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.domain.trip.TripRepository;
import info.tomacla.biketeam.domain.trip.TripStage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Schema Liquibase et requetes de la migration vers Pedalons, sur une vraie base.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class PedalonsRepositoriesTest extends AbstractDBTest {

    private static final String TEAM_ID = "pedalons-team";

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PedalonsMigrationRepository migrationRepository;

    @Autowired
    private PedalonsRedirectRepository redirectRepository;

    @Autowired
    private MapRepository mapRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeAll
    public void createTeam() {
        final Team team = new Team();
        team.setId(TEAM_ID);
        team.setCity("City");
        team.getDescription().setDescription("Description");
        team.setName("Test");
        teamRepository.save(team);
    }

    @Test
    public void testMigrationRows() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        String id = UUID.randomUUID().toString();
        PedalonsMigration migration = new PedalonsMigration(id, TEAM_ID, null, true, false, now);
        migrationRepository.save(migration);

        assertTrue(migrationRepository.findByIdAndTeamId(id, TEAM_ID).isPresent());
        assertTrue(migrationRepository.findByIdAndTeamId(id, "other").isEmpty());
        assertFalse(migrationRepository.existsByTeamIdAndStatusIn(TEAM_ID, PedalonsMigrationStatus.ACTIVE));

        migration.setStatus(PedalonsMigrationStatus.RUNNING, now);
        migration.setPedalonsJobId("0hx3k2m9q8r4t");
        migration.setResultJson("{\"status\":\"RUNNING\"}");
        migrationRepository.save(migration);

        assertTrue(migrationRepository.existsByTeamIdAndStatusIn(TEAM_ID, PedalonsMigrationStatus.ACTIVE));
        assertEquals(1, migrationRepository.findByStatus(PedalonsMigrationStatus.RUNNING).stream()
                .filter(m -> m.getId().equals(id)).count());
        PedalonsMigration reloaded = migrationRepository.findByTeamIdOrderByCreatedAtDesc(TEAM_ID).get(0);
        assertEquals("{\"status\":\"RUNNING\"}", reloaded.getResultJson());
        assertEquals("0hx3k2m9q8r4t", reloaded.getPedalonsJobId());

        migrationRepository.deleteByTeamId(TEAM_ID);
        assertTrue(migrationRepository.findByTeamIdOrderByCreatedAtDesc(TEAM_ID).isEmpty());
    }

    @Test
    public void testTeamStateAndRedirects() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        tx.executeWithoutResult(s -> teamRepository.updatePedalonsState(TEAM_ID, PedalonsState.MIGRATING));
        assertEquals(PedalonsState.MIGRATING, teamRepository.findById(TEAM_ID).orElseThrow().getPedalonsState());
        assertTrue(teamRepository.findByPedalonsStateIsNotNull().stream().anyMatch(t -> t.getId().equals(TEAM_ID)));

        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        tx.executeWithoutResult(s -> {
            redirectRepository.deleteByTeamId(TEAM_ID);
            redirectRepository.saveAll(List.of(
                    new PedalonsRedirect(TEAM_ID, PedalonsRedirect.TEAM, TEAM_ID, "https://p/equipes/t"),
                    new PedalonsRedirect(TEAM_ID, PedalonsRedirect.RIDE, "ride-1", "https://p/equipes/t/sorties/s")));
            teamRepository.updatePedalonsSwitch(TEAM_ID, PedalonsState.MIGRATED, "https://p/equipes/t", now);
        });

        Team team = teamRepository.findById(TEAM_ID).orElseThrow();
        assertEquals(PedalonsState.MIGRATED, team.getPedalonsState());
        assertEquals("https://p/equipes/t", team.getPedalonsUrl());
        assertEquals(now, team.getPedalonsMigratedAt());
        assertEquals(2, redirectRepository.findByTeamId(TEAM_ID).size());
        // chargement groupe du cache de redirection
        assertEquals(2, redirectRepository.findByTeamIdIn(List.of(TEAM_ID, "other")).size());

        tx.executeWithoutResult(s -> {
            redirectRepository.deleteByTeamId(TEAM_ID);
            teamRepository.updatePedalonsState(TEAM_ID, null);
        });
        assertNull(teamRepository.findById(TEAM_ID).orElseThrow().getPedalonsState());
        assertTrue(redirectRepository.findByTeamId(TEAM_ID).isEmpty());
    }

    @Test
    public void testConditionalStateTransitions() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        try {
            // groupe ordinaire : gel possible, bascule et abandon non
            assertEquals((Integer) 0, tx.execute(s -> teamRepository.switchPedalons(TEAM_ID, now)));
            assertEquals((Integer) 0, tx.execute(s -> teamRepository.clearPedalonsReady(TEAM_ID)));
            assertEquals((Integer) 1, tx.execute(s -> teamRepository.freezePedalons(TEAM_ID)));
            assertEquals(PedalonsState.MIGRATING, teamRepository.findById(TEAM_ID).orElseThrow().getPedalonsState());

            // gele : ni second gel ni bascule
            assertEquals((Integer) 0, tx.execute(s -> teamRepository.freezePedalons(TEAM_ID)));
            assertEquals((Integer) 0, tx.execute(s -> teamRepository.switchPedalons(TEAM_ID, now)));

            // pret a basculer : l'abandon efface l'URL
            tx.executeWithoutResult(s -> teamRepository.updatePedalonsSwitch(TEAM_ID, PedalonsState.READY, "https://p/equipes/t", null));
            assertEquals((Integer) 1, tx.execute(s -> teamRepository.clearPedalonsReady(TEAM_ID)));
            Team cleared = teamRepository.findById(TEAM_ID).orElseThrow();
            assertNull(cleared.getPedalonsState());
            assertNull(cleared.getPedalonsUrl());

            // pret a basculer : un nouveau gel efface l'URL en attente
            tx.executeWithoutResult(s -> teamRepository.updatePedalonsSwitch(TEAM_ID, PedalonsState.READY, "https://p/equipes/t", null));
            assertEquals((Integer) 1, tx.execute(s -> teamRepository.freezePedalons(TEAM_ID)));
            assertNull(teamRepository.findById(TEAM_ID).orElseThrow().getPedalonsUrl());

            // pret a basculer : bascule, puis plus de gel possible
            tx.executeWithoutResult(s -> teamRepository.updatePedalonsSwitch(TEAM_ID, PedalonsState.READY, "https://p/equipes/t", null));
            assertEquals((Integer) 1, tx.execute(s -> teamRepository.switchPedalons(TEAM_ID, now)));
            Team switched = teamRepository.findById(TEAM_ID).orElseThrow();
            assertEquals(PedalonsState.MIGRATED, switched.getPedalonsState());
            assertEquals("https://p/equipes/t", switched.getPedalonsUrl());
            assertEquals(now, switched.getPedalonsMigratedAt());
            assertEquals((Integer) 0, tx.execute(s -> teamRepository.freezePedalons(TEAM_ID)));
            assertEquals((Integer) 0, tx.execute(s -> teamRepository.clearPedalonsReady(TEAM_ID)));
        } finally {
            tx.executeWithoutResult(s -> teamRepository.updatePedalonsSwitch(TEAM_ID, null, null, null));
        }
    }

    @Test
    public void testSavingAStaleTeamDoesNotUndoAFreezeOrASwitch() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        try {
            // copie chargee avant le gel (formulaire d'administration, TeamService...)
            Team stale = teamRepository.findById(TEAM_ID).orElseThrow();
            assertNull(stale.getPedalonsState());

            assertEquals((Integer) 1, tx.execute(s -> teamRepository.freezePedalons(TEAM_ID)));
            stale.setName("Renamed");
            teamRepository.save(stale);

            Team reloaded = teamRepository.findById(TEAM_ID).orElseThrow();
            assertEquals("Renamed", reloaded.getName(), "the rest of the team is saved");
            assertEquals(PedalonsState.MIGRATING, reloaded.getPedalonsState(), "the freeze survives");

            // de meme pour une bascule, URL et date comprises ; et un setter ne sert a rien
            tx.executeWithoutResult(s -> teamRepository.readyPedalons(TEAM_ID, "https://p/equipes/t"));
            assertEquals((Integer) 1, tx.execute(s -> teamRepository.switchPedalons(TEAM_ID, now)));
            reloaded.setPedalonsState(null);
            reloaded.setPedalonsUrl(null);
            reloaded.setPedalonsMigratedAt(null);
            teamRepository.save(reloaded);

            Team switched = teamRepository.findById(TEAM_ID).orElseThrow();
            assertEquals(PedalonsState.MIGRATED, switched.getPedalonsState());
            assertEquals("https://p/equipes/t", switched.getPedalonsUrl());
            assertEquals(now, switched.getPedalonsMigratedAt());
        } finally {
            tx.executeWithoutResult(s -> teamRepository.updatePedalonsSwitch(TEAM_ID, null, null, null));
        }
    }

    @Test
    public void testReadyAndUnfreezeOnlyFromMigrating() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        try {
            assertEquals((Integer) 0, tx.execute(s -> teamRepository.readyPedalons(TEAM_ID, "https://p/equipes/t")));
            assertEquals((Integer) 0, tx.execute(s -> teamRepository.unfreezePedalons(TEAM_ID)));

            tx.executeWithoutResult(s -> teamRepository.freezePedalons(TEAM_ID));
            assertEquals((Integer) 1, tx.execute(s -> teamRepository.unfreezePedalons(TEAM_ID)));
            assertNull(teamRepository.findById(TEAM_ID).orElseThrow().getPedalonsState());

            tx.executeWithoutResult(s -> teamRepository.freezePedalons(TEAM_ID));
            assertEquals((Integer) 1, tx.execute(s -> teamRepository.readyPedalons(TEAM_ID, "https://p/equipes/t")));
            Team ready = teamRepository.findById(TEAM_ID).orElseThrow();
            assertEquals(PedalonsState.READY, ready.getPedalonsState());
            assertEquals("https://p/equipes/t", ready.getPedalonsUrl());
            assertNull(ready.getPedalonsMigratedAt());
            assertEquals((Integer) 0, tx.execute(s -> teamRepository.unfreezePedalons(TEAM_ID)));
        } finally {
            tx.executeWithoutResult(s -> teamRepository.updatePedalonsSwitch(TEAM_ID, null, null, null));
        }
    }

    @Test
    public void testConditionalMigrationTransitions() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        String id = UUID.randomUUID().toString();
        migrationRepository.save(new PedalonsMigration(id, TEAM_ID, null, false, false, now));
        try {
            // REQUESTED -> TRIGGERING : seulement depuis l'etat attendu
            assertEquals((Integer) 0, tx.execute(s -> migrationRepository.transition(id, PedalonsMigrationStatus.RUNNING,
                    PedalonsMigrationStatus.FAILED, now)));
            assertEquals((Integer) 1, tx.execute(s -> migrationRepository.transition(id, PedalonsMigrationStatus.REQUESTED,
                    PedalonsMigrationStatus.TRIGGERING, now)));
            PedalonsMigration triggering = migrationRepository.findById(id).orElseThrow();
            triggering.setGrantValue("bmg_grant");
            migrationRepository.save(triggering);

            assertEquals((Integer) 0, tx.execute(s -> migrationRepository.recordProgress(id, "{}", now)));
            assertEquals((Integer) 1, tx.execute(s -> migrationRepository.markRunning(id, "job", now)));
            assertEquals((Integer) 0, tx.execute(s -> migrationRepository.markRunning(id, "other", now)));
            PedalonsMigration running = migrationRepository.findById(id).orElseThrow();
            assertEquals(PedalonsMigrationStatus.RUNNING, running.getStatus());
            assertEquals("job", running.getPedalonsJobId());
            assertNull(running.getGrantValue());

            assertEquals((Integer) 1, tx.execute(s -> migrationRepository.recordProgress(id, "{\"status\":\"RUNNING\"}", now)));
            assertEquals((Integer) 1, tx.execute(s -> migrationRepository.markRunningSince(id, now)));
            assertEquals((Integer) 0, tx.execute(s -> migrationRepository.markRunningSince(id, now.plusSeconds(60))));
            assertEquals(now, migrationRepository.findById(id).orElseThrow().getRunningSince());

            // annulation par l'admin plateforme : ni succes ni echec ne passent ensuite
            assertEquals((Integer) 1, tx.execute(s -> migrationRepository.cancelActive(TEAM_ID, now)));
            assertFalse(migrationRepository.existsByTeamIdAndStatusIn(TEAM_ID, PedalonsMigrationStatus.ACTIVE));
            assertEquals((Integer) 0, tx.execute(s -> migrationRepository.markSucceeded(id, now, "{}", "https://p/t")));
            assertEquals((Integer) 0, tx.execute(s -> migrationRepository.markFailed(id, PedalonsMigrationStatus.ACTIVE,
                    now, "FAILED", null, null)));
            assertEquals((Integer) 0, tx.execute(s -> migrationRepository.recordProgress(id, "{}", now)));
            PedalonsMigration cancelled = migrationRepository.findById(id).orElseThrow();
            assertEquals(PedalonsMigrationStatus.CANCELLED, cancelled.getStatus());
            assertEquals(PedalonsMigrationRepository.CANCELLED_BY_ADMIN, cancelled.getErrorCode());
            assertEquals(now, cancelled.getFinishedAt());
            assertEquals("{\"status\":\"RUNNING\"}", cancelled.getResultJson());

            // echec : le dernier statut est garde quand aucun corps n'est fourni
            tx.executeWithoutResult(s -> migrationRepository.transition(id, PedalonsMigrationStatus.CANCELLED,
                    PedalonsMigrationStatus.RUNNING, now));
            assertEquals((Integer) 1, tx.execute(s -> migrationRepository.markFailed(id, PedalonsMigrationStatus.ACTIVE,
                    now, "TIMEOUT", "x".repeat(3000), null)));
            PedalonsMigration failed = migrationRepository.findById(id).orElseThrow();
            assertEquals(PedalonsMigrationStatus.FAILED, failed.getStatus());
            assertEquals("TIMEOUT", failed.getErrorCode());
            assertEquals(2000, failed.getErrorMessage().length());
            assertEquals("{\"status\":\"RUNNING\"}", failed.getResultJson());

            // succes : seulement depuis RUNNING
            tx.executeWithoutResult(s -> migrationRepository.transition(id, PedalonsMigrationStatus.FAILED,
                    PedalonsMigrationStatus.RUNNING, now));
            assertEquals((Integer) 1, tx.execute(s -> migrationRepository.markSucceeded(id, now, "{\"status\":\"SUCCEEDED\"}", "https://p/t")));
            PedalonsMigration succeeded = migrationRepository.findById(id).orElseThrow();
            assertEquals(PedalonsMigrationStatus.SUCCEEDED, succeeded.getStatus());
            assertEquals("https://p/t", succeeded.getPedalonsTeamUrl());
            assertEquals("{\"status\":\"SUCCEEDED\"}", succeeded.getResultJson());
        } finally {
            migrationRepository.deleteByTeamId(TEAM_ID);
        }
    }

    @Test
    public void testSummaryCounts() {
        Map map = new Map();
        map.setTeamId(TEAM_ID);
        map.setName("Map");
        map.setStartPoint(new info.tomacla.biketeam.common.geo.Point(1, 1));
        map.setEndPoint(new info.tomacla.biketeam.common.geo.Point(1, 1));
        mapRepository.save(map);
        Map deleted = new Map();
        deleted.setTeamId(TEAM_ID);
        deleted.setName("Deleted");
        deleted.setStartPoint(new info.tomacla.biketeam.common.geo.Point(1, 1));
        deleted.setEndPoint(new info.tomacla.biketeam.common.geo.Point(1, 1));
        deleted.setDeletion(true);
        mapRepository.save(deleted);

        assertEquals(2, mapRepository.findByTeamId(TEAM_ID).size());
        assertEquals(1, mapRepository.countByTeamIdAndDeletionFalse(TEAM_ID));

        Trip trip = new Trip();
        trip.setTeamId(TEAM_ID);
        trip.setTitle("Trip");
        trip.setDescription("Description");
        trip.setStartDate(LocalDate.now());
        trip.setEndDate(LocalDate.now().plusDays(1));
        trip.setMeetingTime(LocalTime.NOON);
        trip.setPublishedAt(ZonedDateTime.now());
        trip.setPublishedStatus(PublishedStatus.PUBLISHED);
        TripStage s1 = new TripStage();
        s1.setName("S1");
        s1.setDate(LocalDate.now());
        TripStage s2 = new TripStage();
        s2.setName("S2");
        s2.setDate(LocalDate.now().plusDays(1));
        trip.addStage(s1);
        trip.addStage(s2);
        tripRepository.save(trip);

        assertEquals(1, tripRepository.countByTeamIdAndDeletionFalse(TEAM_ID));
        assertEquals(2, tripRepository.countStagesOfActiveTrips(TEAM_ID));

        // cles des voyages pour le cache de redirection : une requete pour plusieurs equipes
        trip.setPermalink("trip-permalink");
        tripRepository.save(trip);
        List<info.tomacla.biketeam.domain.trip.TripKey> keys = tripRepository.findKeysByTeamIdIn(List.of(TEAM_ID, "other"));
        assertTrue(keys.contains(new info.tomacla.biketeam.domain.trip.TripKey(trip.getId(), TEAM_ID, "trip-permalink", false)));
    }

}
