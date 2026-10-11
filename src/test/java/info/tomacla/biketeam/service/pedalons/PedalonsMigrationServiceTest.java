package info.tomacla.biketeam.service.pedalons;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import info.tomacla.biketeam.domain.pedalons.PedalonsMigration;
import info.tomacla.biketeam.domain.pedalons.PedalonsMigrationRepository;
import info.tomacla.biketeam.domain.pedalons.PedalonsMigrationStatus;
import info.tomacla.biketeam.domain.pedalons.PedalonsRedirect;
import info.tomacla.biketeam.domain.pedalons.PedalonsRedirectRepository;
import info.tomacla.biketeam.domain.team.PedalonsState;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamRepository;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.pedalons.export.TeamSnapshotBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Etats d'une demande de migration (section 9.2 du plan), gel et bascule.
 */
public class PedalonsMigrationServiceTest {

    private static final String KEY = "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=";
    private static final String TEAM_ID = "n-peloton";
    private static final String GRANT = "bmg_" + "Q".repeat(43);
    private static final Instant NOW = Instant.parse("2026-09-22T10:00:00Z");

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final java.util.Map<String, PedalonsMigration> store = new LinkedHashMap<>();
    private PedalonsMigrationRepository migrationRepository;
    private PedalonsRedirectRepository redirectRepository;
    private TeamRepository teamRepository;
    private PedalonsClient client;
    private PedalonsRedirectCache cache;
    private final List<Long> sleeps = new ArrayList<>();

    private Team team;
    private User user;
    private PedalonsMigrationService service;

    @BeforeEach
    public void setUp() {
        PedalonsProperties properties = new PedalonsProperties("https://www.pedalons.fr", "https://www.pedalons.fr", KEY,
                "trigger", "export", "https://biketeam.example");

        migrationRepository = mock(PedalonsMigrationRepository.class);
        when(migrationRepository.save(any())).thenAnswer(i -> {
            PedalonsMigration m = i.getArgument(0);
            store.put(m.getId(), m);
            return m;
        });
        when(migrationRepository.findById(anyString())).thenAnswer(i -> Optional.ofNullable(store.get(i.<String>getArgument(0))));
        when(migrationRepository.findByIdAndTeamId(anyString(), anyString())).thenAnswer(i -> Optional.ofNullable(store.get(i.<String>getArgument(0)))
                .filter(m -> m.getTeamId().equals(i.getArgument(1))));
        when(migrationRepository.findByTeamIdAndStatusIn(anyString(), any())).thenAnswer(i -> store.values().stream()
                .filter(m -> m.getTeamId().equals(i.getArgument(0)) && i.<Collection<?>>getArgument(1).contains(m.getStatus()))
                .toList());
        when(migrationRepository.existsByTeamIdAndStatusIn(anyString(), any())).thenAnswer(i -> store.values().stream()
                .anyMatch(m -> m.getTeamId().equals(i.getArgument(0)) && i.<Collection<?>>getArgument(1).contains(m.getStatus())));
        when(migrationRepository.findByTeamIdOrderByCreatedAtDesc(anyString())).thenAnswer(i -> store.values().stream()
                .filter(m -> m.getTeamId().equals(i.getArgument(0)))
                .sorted(Comparator.comparing(PedalonsMigration::getCreatedAt).reversed())
                .toList());
        when(migrationRepository.findByStatus(any())).thenAnswer(i -> store.values().stream()
                .filter(m -> m.getStatus() == i.getArgument(0)).toList());
        // UPDATE conditionnels, emules sur le magasin en memoire
        when(migrationRepository.transition(anyString(), any(), any(), any())).thenAnswer(i -> {
            PedalonsMigration m = store.get(i.<String>getArgument(0));
            if (m == null || m.getStatus() != i.getArgument(1)) {
                return 0;
            }
            m.setStatus(i.getArgument(2), i.getArgument(3));
            return 1;
        });
        when(migrationRepository.markRunning(anyString(), anyString(), any())).thenAnswer(i -> {
            PedalonsMigration m = store.get(i.<String>getArgument(0));
            if (m == null || m.getStatus() != PedalonsMigrationStatus.TRIGGERING) {
                return 0;
            }
            m.setStatus(PedalonsMigrationStatus.RUNNING, i.getArgument(2));
            m.setPedalonsJobId(i.getArgument(1));
            m.setGrantValue(null);
            return 1;
        });
        when(migrationRepository.recordProgress(anyString(), any(), any())).thenAnswer(i -> {
            PedalonsMigration m = store.get(i.<String>getArgument(0));
            if (m == null || m.getStatus() != PedalonsMigrationStatus.RUNNING) {
                return 0;
            }
            m.setResultJson(i.getArgument(1));
            m.setUpdatedAt(i.getArgument(2));
            return 1;
        });
        when(migrationRepository.markRunningSince(anyString(), any())).thenAnswer(i -> {
            PedalonsMigration m = store.get(i.<String>getArgument(0));
            if (m == null || m.getStatus() != PedalonsMigrationStatus.RUNNING || m.getRunningSince() != null) {
                return 0;
            }
            m.setRunningSince(i.getArgument(1));
            return 1;
        });
        when(migrationRepository.markSucceeded(anyString(), any(), any(), any())).thenAnswer(i -> {
            PedalonsMigration m = store.get(i.<String>getArgument(0));
            if (m == null || m.getStatus() != PedalonsMigrationStatus.RUNNING) {
                return 0;
            }
            m.setStatus(PedalonsMigrationStatus.SUCCEEDED, i.getArgument(1));
            m.setFinishedAt(i.getArgument(1));
            m.setResultJson(i.getArgument(2));
            m.setPedalonsTeamUrl(i.getArgument(3));
            return 1;
        });
        when(migrationRepository.markFailed(anyString(), any(), any(), any(), any(), any())).thenAnswer(i -> {
            PedalonsMigration m = store.get(i.<String>getArgument(0));
            if (m == null || !i.<Collection<?>>getArgument(1).contains(m.getStatus())) {
                return 0;
            }
            m.setStatus(PedalonsMigrationStatus.FAILED, i.getArgument(2));
            m.setFinishedAt(i.getArgument(2));
            m.setError(i.getArgument(3), i.getArgument(4));
            m.setGrantValue(null);
            if (i.getArgument(5) != null) {
                m.setResultJson(i.getArgument(5));
            }
            return 1;
        });
        when(migrationRepository.cancelActive(anyString(), any())).thenAnswer(i -> {
            int count = 0;
            for (PedalonsMigration m : store.values()) {
                if (m.getTeamId().equals(i.getArgument(0)) && m.getStatus().isActive()) {
                    m.setStatus(PedalonsMigrationStatus.CANCELLED, i.getArgument(1));
                    m.setFinishedAt(i.getArgument(1));
                    m.setError(PedalonsMigrationRepository.CANCELLED_BY_ADMIN, "cancelled");
                    m.setGrantValue(null);
                    count++;
                }
            }
            return count;
        });

        team = new Team();
        team.setId(TEAM_ID);
        team.setName("N'Peloton");

        teamRepository = mock(TeamRepository.class);
        when(teamRepository.findById(TEAM_ID)).thenAnswer(i -> Optional.of(team));
        when(teamRepository.updatePedalonsState(eq(TEAM_ID), any())).thenAnswer(i -> {
            team.setPedalonsState(i.getArgument(1));
            return 1;
        });
        when(teamRepository.freezePedalons(TEAM_ID)).thenAnswer(i -> {
            if (team.getPedalonsState() != null && team.getPedalonsState() != PedalonsState.READY) {
                return 0;
            }
            team.setPedalonsState(PedalonsState.MIGRATING);
            team.setPedalonsUrl(null);
            return 1;
        });
        when(teamRepository.clearPedalonsReady(TEAM_ID)).thenAnswer(i -> {
            if (team.getPedalonsState() != PedalonsState.READY) {
                return 0;
            }
            team.setPedalonsState(null);
            team.setPedalonsUrl(null);
            return 1;
        });
        when(teamRepository.switchPedalons(eq(TEAM_ID), any())).thenAnswer(i -> {
            if (team.getPedalonsState() != PedalonsState.READY) {
                return 0;
            }
            team.setPedalonsState(PedalonsState.MIGRATED);
            team.setPedalonsMigratedAt(i.getArgument(1));
            return 1;
        });
        when(teamRepository.readyPedalons(eq(TEAM_ID), any())).thenAnswer(i -> {
            if (team.getPedalonsState() != PedalonsState.MIGRATING) {
                return 0;
            }
            team.setPedalonsState(PedalonsState.READY);
            team.setPedalonsUrl(i.getArgument(1));
            return 1;
        });
        when(teamRepository.unfreezePedalons(TEAM_ID)).thenAnswer(i -> {
            if (team.getPedalonsState() != PedalonsState.MIGRATING) {
                return 0;
            }
            team.setPedalonsState(null);
            return 1;
        });
        when(teamRepository.updatePedalonsSwitch(eq(TEAM_ID), any(), any(), any())).thenAnswer(i -> {
            team.setPedalonsState(i.getArgument(1));
            team.setPedalonsUrl(i.getArgument(2));
            team.setPedalonsMigratedAt(i.getArgument(3));
            return 1;
        });

        redirectRepository = mock(PedalonsRedirectRepository.class);
        client = mock(PedalonsClient.class);
        cache = mock(PedalonsRedirectCache.class);

        TeamSnapshotBuilder snapshotBuilder = mock(TeamSnapshotBuilder.class);
        when(snapshotBuilder.summary(any())).thenReturn(new PedalonsSummary(1, 2, 3, 0, 1, 2, 4, true, false));

        service = new PedalonsMigrationService(properties, migrationRepository, redirectRepository, teamRepository,
                new PedalonsRequestTokenSigner(properties), client, snapshotBuilder, cache,
                mock(PlatformTransactionManager.class));
        service.setClock(Clock.fixed(NOW, ZoneOffset.UTC));
        service.setSleeper(sleeps::add);

        user = new User();
        user.setId("user-1");
        user.setFirstName("Jane");
        user.setLastName("D.");
    }

    private PedalonsClient.Response response(int status, String json) throws Exception {
        return new PedalonsClient.Response(status, json == null ? NullNode.getInstance() : objectMapper.readTree(json), json);
    }

    private PedalonsMigration request(String id, boolean dryRun, Instant createdAt) {
        PedalonsMigration m = new PedalonsMigration(id, TEAM_ID, "user-1", dryRun, false, createdAt);
        store.put(id, m);
        return m;
    }

    private PedalonsMigration running(String id, boolean dryRun) {
        PedalonsMigration m = request(id, dryRun, NOW.minus(Duration.ofMinutes(5)));
        m.setStatus(PedalonsMigrationStatus.RUNNING, NOW);
        m.setTriggeredAt(NOW.minus(Duration.ofMinutes(4)));
        m.setRunningSince(NOW.minus(Duration.ofMinutes(3)));
        m.setPedalonsJobId("0hx3k2m9q8r4t");
        if (!dryRun) {
            team.setPedalonsState(PedalonsState.MIGRATING);
        }
        return m;
    }

    // --- demande ---

    @Test
    public void testStartCreatesARequestAndRedirectsToPedalons() {
        PedalonsMigration previous = request("old", true, NOW.minus(Duration.ofMinutes(3)));

        String url = service.start(team, user, true, false);

        assertTrue(url.startsWith("https://www.pedalons.fr/migration-biketeam?request="));
        assertEquals(PedalonsMigrationStatus.CANCELLED, previous.getStatus());

        PedalonsMigration created = store.values().stream().filter(m -> !m.getId().equals("old")).findFirst().orElseThrow();
        assertEquals(PedalonsMigrationStatus.REQUESTED, created.getStatus());
        assertEquals("user-1", created.getRequestedBy());
        assertTrue(created.isDryRun());
        assertFalse(created.isReset());

        String token = URLDecoder.decode(url.substring(url.indexOf('=') + 1), StandardCharsets.UTF_8);
        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
        assertTrue(payload.contains("\"jti\":\"" + created.getId() + "\""));
        assertTrue(payload.contains("\"requestedBy\":\"Jane D.\""));
        assertTrue(payload.contains("\"returnUrl\":\"https://biketeam.example/n-peloton/admin/pedalons/callback\""));
        assertTrue(payload.contains("\"iat\":" + NOW.getEpochSecond()));
    }

    @Test
    public void testStartIsRefusedWhileAMigrationIsActiveOrAfterTheSwitch() {
        running("active", true);
        assertThrows(PedalonsMigrationException.class, () -> service.start(team, user, true, false));

        store.clear();
        team.setPedalonsState(PedalonsState.MIGRATED);
        assertThrows(PedalonsMigrationException.class, () -> service.start(team, user, true, false));
        assertTrue(store.isEmpty());
    }

    @Test
    public void testStartIsAllowedWhileASwitchIsPending() {
        team.setPedalonsState(PedalonsState.READY);

        String url = service.start(team, user, true, false);

        assertTrue(url.startsWith("https://www.pedalons.fr/migration-biketeam?request="));
        assertEquals(PedalonsState.READY, team.getPedalonsState(), "nothing changes until Pedalons confirms");
    }

    // --- retour de Pedalons ---

    @Test
    public void testCancelledOnPedalons() {
        PedalonsMigration m = request("req", false, NOW.minus(Duration.ofMinutes(2)));

        service.callback(team, user, "req", null, "cancelled");

        assertEquals(PedalonsMigrationStatus.CANCELLED, m.getStatus());
        verifyNoInteractions(client);
        assertNull(team.getPedalonsState());
    }

    @Test
    public void testUnknownExpiredOrForeignRequestIsRefused() {
        request("old", true, NOW.minus(PedalonsMigrationService.REQUEST_ACCEPTANCE));
        assertThrows(PedalonsMigrationException.class, () -> service.callback(team, user, "old", GRANT, null));
        assertThrows(PedalonsMigrationException.class, () -> service.callback(team, user, "missing", GRANT, null));

        Team other = new Team();
        other.setId("other");
        request("req", true, NOW);
        assertThrows(PedalonsMigrationException.class, () -> service.callback(other, user, "req", GRANT, null));

        assertThrows(PedalonsMigrationException.class, () -> service.callback(team, user, "req", "not-a-grant", null));

        // demande faite par un autre administrateur du groupe
        User otherAdmin = new User();
        otherAdmin.setId("user-2");
        assertThrows(PedalonsMigrationException.class, () -> service.callback(team, otherAdmin, "req", GRANT, null));
        assertThrows(PedalonsMigrationException.class, () -> service.callback(team, null, "req", GRANT, null));
        assertEquals(PedalonsMigrationStatus.REQUESTED, store.get("req").getStatus());
        verifyNoInteractions(client);
    }

    @Test
    public void testDefinitiveMigrationFreezesTheTeamAndStartsTheJob() throws Exception {
        PedalonsMigration m = request("req", false, NOW.minus(Duration.ofMinutes(2)));
        when(client.trigger("req", GRANT, TEAM_ID, false, false))
                .thenReturn(response(202, "{\"jobId\":\"0hx3k2m9q8r4t\",\"status\":\"QUEUED\",\"targetTeamSlug\":\"n-peloton\"}"));

        service.callback(team, user, "req", GRANT, null);

        assertEquals(PedalonsMigrationStatus.RUNNING, m.getStatus());
        assertEquals("0hx3k2m9q8r4t", m.getPedalonsJobId());
        assertNull(m.getGrantValue(), "grant forgotten once the job exists");
        assertEquals(NOW, m.getTriggeredAt());
        assertEquals(PedalonsState.MIGRATING, team.getPedalonsState());
        verify(cache, atLeastOnce()).invalidate();
    }

    @Test
    public void testNewDefinitiveMigrationReplacesAPendingSwitch() throws Exception {
        team.setPedalonsState(PedalonsState.READY);
        team.setPedalonsUrl("https://www.pedalons.fr/equipes/n-peloton");
        request("req", false, NOW);
        when(client.trigger(anyString(), anyString(), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(response(202, "{\"jobId\":\"job\"}"));

        service.callback(team, user, "req", GRANT, null);

        assertEquals(PedalonsState.MIGRATING, team.getPedalonsState());
        assertNull(team.getPedalonsUrl());
        verify(redirectRepository).deleteByTeamId(TEAM_ID);
    }

    @Test
    public void testDryRunDropsAPendingSwitch() throws Exception {
        team.setPedalonsState(PedalonsState.READY);
        team.setPedalonsUrl("https://www.pedalons.fr/equipes/n-peloton");
        request("req", true, NOW);
        when(client.trigger(anyString(), anyString(), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(response(202, "{\"jobId\":\"job\"}"));

        service.callback(team, user, "req", GRANT, null);

        assertNull(team.getPedalonsState(), "a dry run is never switchable, and its job invalidates the pending URL map");
        verify(redirectRepository).deleteByTeamId(TEAM_ID);
        verify(teamRepository, never()).freezePedalons(anyString());
    }

    @Test
    public void testCallbackIsRefusedOnceTheTeamIsSwitched() {
        request("req", false, NOW);
        team.setPedalonsState(PedalonsState.MIGRATED);

        assertThrows(PedalonsMigrationException.class, () -> service.callback(team, user, "req", GRANT, null));

        assertEquals(PedalonsState.MIGRATED, team.getPedalonsState());
        verifyNoInteractions(client);
    }

    @Test
    public void testDryRunDoesNotFreezeTheTeam() throws Exception {
        PedalonsMigration m = request("req", true, NOW);
        when(client.trigger(anyString(), anyString(), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(response(200, "{\"jobId\":\"job\",\"status\":\"RUNNING\"}"));

        service.callback(team, user, "req", GRANT, null);

        assertEquals(PedalonsMigrationStatus.RUNNING, m.getStatus());
        assertNull(team.getPedalonsState());
        verify(teamRepository, never()).updatePedalonsState(anyString(), any());
        verify(teamRepository, never()).freezePedalons(anyString());
        verifyNoInteractions(redirectRepository);
    }

    @Test
    public void testRefusedTriggerFailsAndUnfreezes() throws Exception {
        PedalonsMigration m = request("req", false, NOW);
        when(client.trigger(anyString(), anyString(), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(response(409, "{\"code\":\"BIKETEAM_MIGRATION_RUNNING\",\"activeJobId\":\"x\"}"));

        assertThrows(PedalonsMigrationException.class, () -> service.callback(team, user, "req", GRANT, null));

        assertEquals(PedalonsMigrationStatus.FAILED, m.getStatus());
        assertEquals("BIKETEAM_MIGRATION_RUNNING", m.getErrorCode());
        assertNull(m.getGrantValue());
        assertNull(team.getPedalonsState(), "unfrozen");
        verify(client, times(1)).trigger(anyString(), anyString(), anyString(), anyBoolean(), anyBoolean());
    }

    @Test
    public void testServerErrorsAreRetriedWithTheSameGrant() throws Exception {
        PedalonsMigration m = request("req", true, NOW);
        when(client.trigger("req", GRANT, TEAM_ID, true, false))
                .thenReturn(response(503, null), response(0, null), response(202, "{\"jobId\":\"job\"}"));

        service.callback(team, user, "req", GRANT, null);

        assertEquals(PedalonsMigrationStatus.RUNNING, m.getStatus());
        assertEquals(List.of(2000L, 5000L), sleeps);
    }

    @Test
    public void testUnreachablePedalonsFailsAfterThreeAttempts() throws Exception {
        PedalonsMigration m = request("req", false, NOW);
        when(client.trigger(anyString(), anyString(), anyString(), anyBoolean(), anyBoolean())).thenReturn(response(0, null));

        assertThrows(PedalonsMigrationException.class, () -> service.callback(team, user, "req", GRANT, null));

        assertEquals(PedalonsMigrationStatus.FAILED, m.getStatus());
        assertEquals("TRIGGER_UNAVAILABLE", m.getErrorCode());
        assertNull(team.getPedalonsState());
        verify(client, times(3)).trigger(anyString(), anyString(), anyString(), anyBoolean(), anyBoolean());
    }

    // --- suivi ---

    private static final String SUCCEEDED = "{\"jobId\":\"0hx3k2m9q8r4t\",\"status\":\"SUCCEEDED\","
            + "\"progress\":{\"phase\":\"DONE\",\"done\":0,\"total\":0},"
            + "\"targetTeam\":{\"slug\":\"n-peloton\",\"name\":\"N'Peloton\",\"url\":\"https://www.pedalons.fr/equipes/n-peloton\"},"
            + "\"error\":null,"
            + "\"urlMap\":{"
            + "\"TEAM\":{\"n-peloton\":\"https://www.pedalons.fr/equipes/n-peloton\"},"
            + "\"RIDE\":{\"ride-1\":\"https://www.pedalons.fr/equipes/n-peloton/sorties/s\"},"
            + "\"UNKNOWN\":{\"x\":\"https://www.pedalons.fr/x\"},"
            + "\"POST\":{\"pub-1\":\"javascript:alert(1)\"}}}";

    @Test
    public void testSucceededDefinitiveLeavesTheTeamReadyToSwitch() throws Exception {
        PedalonsMigration m = running("req", false);
        when(client.status("0hx3k2m9q8r4t")).thenReturn(response(200, SUCCEEDED));

        service.pollAll();

        assertEquals(PedalonsMigrationStatus.SUCCEEDED, m.getStatus());
        assertEquals(NOW, m.getFinishedAt());
        assertNull(m.getSwitchedAt(), "the switch waits for the team admin");
        assertEquals("https://www.pedalons.fr/equipes/n-peloton", m.getPedalonsTeamUrl());
        assertEquals(SUCCEEDED, m.getResultJson());
        // toujours gele (READY), mais pas redirige
        assertEquals(PedalonsState.READY, team.getPedalonsState());
        assertEquals("https://www.pedalons.fr/equipes/n-peloton", team.getPedalonsUrl());
        assertNull(team.getPedalonsMigratedAt());

        verify(redirectRepository).deleteByTeamId(TEAM_ID);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PedalonsRedirect>> captor = ArgumentCaptor.forClass(List.class);
        verify(redirectRepository).saveAll(captor.capture());
        List<PedalonsRedirect> saved = captor.getValue();
        assertEquals(2, saved.size(), "unknown types and non-http URLs are dropped");
        assertTrue(saved.stream().anyMatch(r -> r.getEntityType().equals("RIDE") && r.getEntityId().equals("ride-1")));
        verify(cache).invalidate();
    }

    @Test
    public void testSucceededDryRunDoesNotSwitch() throws Exception {
        PedalonsMigration m = running("req", true);
        when(client.status("0hx3k2m9q8r4t")).thenReturn(response(200, SUCCEEDED));

        service.pollAll();

        assertEquals(PedalonsMigrationStatus.SUCCEEDED, m.getStatus());
        assertNull(m.getSwitchedAt());
        assertEquals("https://www.pedalons.fr/equipes/n-peloton", m.getPedalonsTeamUrl());
        assertNull(team.getPedalonsState());
        verifyNoInteractions(redirectRepository);
    }

    @Test
    public void testProgressIsRecorded() throws Exception {
        PedalonsMigration m = running("req", false);
        String body = "{\"status\":\"RUNNING\",\"progress\":{\"phase\":\"ROUTES\",\"done\":12,\"total\":2601}}";
        when(client.status("0hx3k2m9q8r4t")).thenReturn(response(200, body));

        service.pollAll();

        assertEquals(PedalonsMigrationStatus.RUNNING, m.getStatus());
        assertEquals(body, m.getResultJson());
        assertEquals(PedalonsState.MIGRATING, team.getPedalonsState());
    }

    @Test
    public void testFailedAttemptAwaitingRetryIsRecordedButNotAnError() throws Exception {
        PedalonsMigration m = running("req", false);
        String body = "{\"status\":\"QUEUED\",\"attempt\":1,\"progress\":{\"phase\":\"QUEUED\",\"done\":0,\"total\":0},"
                + "\"lastAttemptError\":{\"code\":\"EXPORT_UNAVAILABLE\",\"message\":\"biketeam export returned HTTP 502\"},\"error\":null}";
        when(client.status("0hx3k2m9q8r4t")).thenReturn(response(200, body));

        service.pollAll();

        // la tentative echouee ne termine pas la migration : statut conserve tel quel pour la page de suivi
        assertEquals(PedalonsMigrationStatus.RUNNING, m.getStatus());
        assertEquals(body, m.getResultJson());
        assertNull(m.getErrorCode());
        assertNull(m.getErrorMessage());
        assertEquals(PedalonsState.MIGRATING, team.getPedalonsState());
    }

    @Test
    public void testFailedJobUnfreezesTheTeam() throws Exception {
        PedalonsMigration m = running("req", false);
        when(client.status("0hx3k2m9q8r4t")).thenReturn(response(200,
                "{\"status\":\"FAILED\",\"error\":{\"code\":\"BIKETEAM_SLUG_CONFLICT\",\"message\":\"Slug taken\"}}"));

        service.pollAll();

        assertEquals(PedalonsMigrationStatus.FAILED, m.getStatus());
        assertEquals("BIKETEAM_SLUG_CONFLICT", m.getErrorCode());
        assertEquals("Slug taken", m.getErrorMessage());
        assertNull(team.getPedalonsState());
    }

    @Test
    public void testLostJob() throws Exception {
        PedalonsMigration m = running("req", false);
        when(client.status("0hx3k2m9q8r4t")).thenReturn(response(404,
                "{\"code\":\"BIKETEAM_JOB_NOT_FOUND\",\"message\":\"Unknown job\"}"));

        service.pollAll();

        assertEquals(PedalonsMigrationStatus.FAILED, m.getStatus());
        assertEquals("JOB_LOST", m.getErrorCode());
        assertNull(team.getPedalonsState());
    }

    @Test
    public void testOther404sAreTransientUntilTheTimeout() throws Exception {
        PedalonsMigration m = running("req", false);
        when(client.status("0hx3k2m9q8r4t")).thenReturn(
                response(404, null),
                new PedalonsClient.Response(404, NullNode.getInstance(), "<html><body>404 Not Found</body></html>"),
                response(404, "{\"code\":\"NOT_FOUND\"}"));

        service.pollAll();
        service.pollAll();
        service.pollAll();

        assertEquals(PedalonsMigrationStatus.RUNNING, m.getStatus());
        assertEquals(PedalonsState.MIGRATING, team.getPedalonsState());

        m.setRunningSince(NOW.minus(Duration.ofHours(7)));
        service.pollAll();
        assertEquals(PedalonsMigrationStatus.FAILED, m.getStatus());
        assertEquals("TIMEOUT", m.getErrorCode());
        assertNull(team.getPedalonsState());
    }

    @Test
    public void testNetworkErrorsAreRetriedUntilTheTimeout() throws Exception {
        PedalonsMigration m = running("req", false);
        when(client.status("0hx3k2m9q8r4t")).thenReturn(response(0, null));

        service.pollAll();
        assertEquals(PedalonsMigrationStatus.RUNNING, m.getStatus());

        m.setRunningSince(NOW.minus(Duration.ofHours(7)));
        service.pollAll();
        assertEquals(PedalonsMigrationStatus.FAILED, m.getStatus());
        assertEquals("TIMEOUT", m.getErrorCode());
        assertNull(team.getPedalonsState());
    }

    @Test
    public void testStuckTriggeringIsReplayedAndOldRequestsExpire() throws Exception {
        PedalonsMigration stuck = request("stuck", true, NOW.minus(Duration.ofMinutes(20)));
        stuck.setStatus(PedalonsMigrationStatus.TRIGGERING, NOW);
        stuck.setTriggeredAt(NOW.minus(Duration.ofMinutes(15)));
        stuck.setGrantValue(GRANT);
        PedalonsMigration old = request("old", true, NOW.minus(PedalonsMigrationService.REQUEST_ACCEPTANCE));
        when(client.trigger("stuck", GRANT, TEAM_ID, true, false)).thenReturn(response(200, "{\"jobId\":\"job\"}"));

        service.pollAll();

        assertEquals(PedalonsMigrationStatus.RUNNING, stuck.getStatus());
        assertEquals(PedalonsMigrationStatus.EXPIRED, old.getStatus());
    }

    @Test
    public void testTriggeringIsReplayedWithinTheGrantLifetime() throws Exception {
        // pas un declenchement encore en vol (3 essais au bout de leurs delais reseau)...
        assertTrue(PedalonsMigrationService.TRIGGERING_STUCK_AFTER.compareTo(PedalonsMigrationService.MAX_TRIGGER_DURATION) > 0);
        // ...mais rejoue avant l'expiration du grant, meme au pire tick du poller
        assertTrue(PedalonsMigrationService.TRIGGERING_STUCK_AFTER.plusMillis(PedalonsMigrationPoller.INTERVAL_MS)
                .compareTo(PedalonsMigrationService.PEDALONS_GRANT_TTL) < 0);

        PedalonsMigration recent = request("recent", true, NOW.minus(Duration.ofMinutes(5)));
        recent.setStatus(PedalonsMigrationStatus.TRIGGERING, NOW);
        recent.setTriggeredAt(NOW.minus(PedalonsMigrationService.TRIGGERING_STUCK_AFTER));
        recent.setGrantValue(GRANT);
        service.pollAll();
        assertEquals(PedalonsMigrationStatus.TRIGGERING, recent.getStatus(), "not stuck yet");
        verifyNoInteractions(client);

        recent.setTriggeredAt(NOW.minus(PedalonsMigrationService.TRIGGERING_STUCK_AFTER).minusSeconds(1));
        when(client.trigger("recent", GRANT, TEAM_ID, true, false)).thenReturn(response(200, "{\"jobId\":\"job\"}"));
        service.pollAll();
        assertEquals(PedalonsMigrationStatus.RUNNING, recent.getStatus());
    }

    @Test
    public void testAFailedTriggerDoesNotUndoAConcurrentSuccessfulOne() throws Exception {
        PedalonsMigration m = request("req", false, NOW);
        m.setStatus(PedalonsMigrationStatus.TRIGGERING, NOW);
        m.setTriggeredAt(NOW);
        m.setGrantValue(GRANT);
        team.setPedalonsState(PedalonsState.MIGRATING);
        // pendant cet essai, un autre declenchement du meme grant obtient le job
        when(client.trigger(anyString(), anyString(), anyString(), anyBoolean(), anyBoolean())).thenAnswer(i -> {
            m.setStatus(PedalonsMigrationStatus.RUNNING, NOW);
            m.setPedalonsJobId("job");
            return response(0, null);
        });

        assertEquals(PedalonsMigrationStatus.RUNNING, service.trigger("req"));

        assertEquals(PedalonsMigrationStatus.RUNNING, m.getStatus());
        assertNull(m.getErrorCode());
        assertEquals(PedalonsState.MIGRATING, team.getPedalonsState(), "still frozen");
        verify(cache, never()).invalidate();
    }

    @Test
    public void testCallbackIsAcceptedUntilTheTokenExpiryPlusTheGrace() throws Exception {
        // Pedalons confirme jusqu'a exp + 60 s et le grant vit ensuite 10 min : tout cela tient dans la grace
        assertTrue(PedalonsMigrationService.CALLBACK_GRACE.compareTo(
                Duration.ofSeconds(60).plus(PedalonsMigrationService.PEDALONS_GRANT_TTL)) > 0);
        Instant lastAccepted = NOW.minus(PedalonsMigrationService.REQUEST_ACCEPTANCE).plusSeconds(1);
        Instant firstRefused = NOW.minus(PedalonsMigrationService.REQUEST_ACCEPTANCE);
        assertEquals(Duration.ofMinutes(75), PedalonsMigrationService.REQUEST_ACCEPTANCE);

        PedalonsMigration late = request("late", true, lastAccepted);
        PedalonsMigration tooLate = request("tooLate", true, firstRefused);
        service.pollAll();
        assertEquals(PedalonsMigrationStatus.REQUESTED, late.getStatus(), "kept while a grant may still arrive");
        assertEquals(PedalonsMigrationStatus.EXPIRED, tooLate.getStatus());

        tooLate.setStatus(PedalonsMigrationStatus.REQUESTED, NOW);
        assertThrows(PedalonsMigrationException.class, () -> service.callback(team, user, "tooLate", GRANT, null));

        when(client.trigger("late", GRANT, TEAM_ID, true, false)).thenReturn(response(202, "{\"jobId\":\"job\"}"));
        service.callback(team, user, "late", GRANT, null);
        assertEquals(PedalonsMigrationStatus.RUNNING, late.getStatus());
    }

    @Test
    public void testNothingHappensWhenDisabled() {
        PedalonsProperties disabled = new PedalonsProperties("", "", "", "", "", "https://biketeam.example");
        PedalonsMigrationService off = new PedalonsMigrationService(disabled, migrationRepository, redirectRepository,
                teamRepository, new PedalonsRequestTokenSigner(disabled), client, mock(TeamSnapshotBuilder.class), cache,
                mock(PlatformTransactionManager.class));
        running("req", false);

        off.pollAll();

        verifyNoInteractions(client);
        assertThrows(PedalonsMigrationException.class, () -> off.start(team, user, true, false));
    }

    // --- bascule ---

    @Test
    public void testSwitchAfterReadingTheReport() throws Exception {
        PedalonsMigration m = running("req", false);
        when(client.status("0hx3k2m9q8r4t")).thenReturn(response(200, SUCCEEDED));
        service.pollAll();
        PedalonsMigration pending = request("pending", true, NOW.minus(Duration.ofMinutes(1)));
        clearInvocations(cache);

        String message = service.switchTeam(team);

        assertTrue(message.contains("basculé"));
        assertEquals(PedalonsState.MIGRATED, team.getPedalonsState());
        assertEquals(NOW, team.getPedalonsMigratedAt());
        assertEquals("https://www.pedalons.fr/equipes/n-peloton", team.getPedalonsUrl());
        assertEquals(NOW, m.getSwitchedAt());
        assertEquals(PedalonsMigrationStatus.CANCELLED, pending.getStatus(), "a request confirmed later would target a switched team");
        verify(cache).invalidate();
    }

    @Test
    public void testSwitchIsRefusedWithoutASucceededDefinitiveMigration() {
        assertThrows(PedalonsMigrationException.class, () -> service.switchTeam(team));
        assertNull(team.getPedalonsState());

        running("req", false);
        assertThrows(PedalonsMigrationException.class, () -> service.switchTeam(team));
        assertEquals(PedalonsState.MIGRATING, team.getPedalonsState());

        team.setPedalonsState(PedalonsState.MIGRATED);
        store.clear();
        assertThrows(PedalonsMigrationException.class, () -> service.switchTeam(team));
        verify(cache, never()).invalidate();
    }

    // --- annulation de la bascule ---

    @Test
    public void testUnswitch() {
        team.setPedalonsState(PedalonsState.MIGRATED);

        service.unswitch(TEAM_ID);

        verify(redirectRepository).deleteByTeamId(TEAM_ID);
        assertNull(team.getPedalonsState());
        verify(cache).invalidate();
    }

    @Test
    public void testUnswitchCancelsTheActiveMigrationAndIgnoresWhatPedalonsReportsLater() throws Exception {
        PedalonsMigration m = running("req", false);
        assertTrue(migrationRepository.existsByTeamIdAndStatusIn(TEAM_ID, PedalonsMigrationStatus.ACTIVE));

        service.unswitch(TEAM_ID);

        assertEquals(PedalonsMigrationStatus.CANCELLED, m.getStatus());
        assertEquals(PedalonsMigrationRepository.CANCELLED_BY_ADMIN, m.getErrorCode());
        assertEquals(NOW, m.getFinishedAt());
        assertNull(team.getPedalonsState(), "unfrozen");
        // garde de l'export (PedalonsExportController) : plus de migration active, plus d'export
        assertFalse(migrationRepository.existsByTeamIdAndStatusIn(TEAM_ID, PedalonsMigrationStatus.ACTIVE));

        // le poller ne suit plus le job...
        service.pollAll();
        verify(client, never()).status(anyString());
        // ...et un succes ou un echec rapporte ensuite ne change rien
        service.succeed("req", response(200, SUCCEEDED));
        service.fail("req", "FAILED", "late", null);
        assertEquals(PedalonsMigrationStatus.CANCELLED, m.getStatus());
        assertEquals(PedalonsMigrationRepository.CANCELLED_BY_ADMIN, m.getErrorCode());
        assertNull(team.getPedalonsState(), "not READY");
        verify(redirectRepository, never()).saveAll(any());
        verify(teamRepository, never()).readyPedalons(anyString(), any());
    }

    @Test
    public void testUnswitchDuringTheSuccessWins() throws Exception {
        PedalonsMigration m = running("req", false);
        // l'annulation est validee juste apres la verification du gel par succeed()
        when(teamRepository.findById(TEAM_ID)).thenAnswer(i -> {
            Optional<Team> current = Optional.of(team).map(t -> {
                Team copy = new Team();
                copy.setId(TEAM_ID);
                copy.setPedalonsState(t.getPedalonsState());
                return copy;
            });
            service.unswitch(TEAM_ID);
            return current;
        });
        when(client.status("0hx3k2m9q8r4t")).thenReturn(response(200, SUCCEEDED));

        service.pollAll();

        assertEquals(PedalonsMigrationStatus.CANCELLED, m.getStatus());
        assertNull(team.getPedalonsState());
        verify(redirectRepository, never()).saveAll(any());
    }

    @Test
    public void testUnswitchDuringTheTriggerWins() throws Exception {
        PedalonsMigration m = request("req", false, NOW);
        when(client.trigger(anyString(), anyString(), anyString(), anyBoolean(), anyBoolean())).thenAnswer(i -> {
            service.unswitch(TEAM_ID);
            return response(202, "{\"jobId\":\"job\"}");
        });

        PedalonsMigrationException e = assertThrows(PedalonsMigrationException.class,
                () -> service.callback(team, user, "req", GRANT, null));

        assertTrue(e.getMessage().contains("annulée"));
        assertEquals(PedalonsMigrationStatus.CANCELLED, m.getStatus());
        assertNull(m.getPedalonsJobId());
        assertNull(m.getGrantValue());
        assertNull(team.getPedalonsState());
        assertFalse(migrationRepository.existsByTeamIdAndStatusIn(TEAM_ID, PedalonsMigrationStatus.ACTIVE));
    }

    @Test
    public void testSuccessOfATeamNoLongerFrozenIsNotApplied() throws Exception {
        PedalonsMigration m = running("req", false);
        team.setPedalonsState(null);
        when(client.status("0hx3k2m9q8r4t")).thenReturn(response(200, SUCCEEDED));

        service.pollAll();

        assertEquals(PedalonsMigrationStatus.FAILED, m.getStatus());
        assertEquals("TEAM_NOT_FROZEN", m.getErrorCode());
        assertNull(team.getPedalonsState());
        verify(redirectRepository, never()).saveAll(any());
    }

    // --- delais ---

    @Test
    public void testAQueuedJobIsNotTimedOutByTheExecutionDelay() throws Exception {
        PedalonsMigration m = running("req", false);
        m.setRunningSince(null);
        m.setTriggeredAt(NOW.minus(Duration.ofHours(7)));
        when(client.status("0hx3k2m9q8r4t")).thenReturn(response(200, "{\"status\":\"QUEUED\",\"progress\":{\"phase\":\"QUEUED\"}}"));

        service.pollAll();

        assertEquals(PedalonsMigrationStatus.RUNNING, m.getStatus(), "still waiting behind another team");
        assertNull(m.getRunningSince());
        assertEquals(PedalonsState.MIGRATING, team.getPedalonsState(), "still frozen");
        assertTrue(migrationRepository.existsByTeamIdAndStatusIn(TEAM_ID, PedalonsMigrationStatus.ACTIVE), "export still served");

        // premier RUNNING : le delai d'execution part de maintenant
        when(client.status("0hx3k2m9q8r4t")).thenReturn(response(200, "{\"status\":\"RUNNING\"}"));
        service.pollAll();
        assertEquals(NOW, m.getRunningSince());
        assertEquals(PedalonsMigrationStatus.RUNNING, m.getStatus());

        // jusqu'a RUNNING_TIMEOUT inclus, puis TIMEOUT
        m.setRunningSince(NOW.minus(PedalonsMigrationService.RUNNING_TIMEOUT));
        service.pollAll();
        assertEquals(PedalonsMigrationStatus.RUNNING, m.getStatus());
        m.setRunningSince(NOW.minus(PedalonsMigrationService.RUNNING_TIMEOUT).minusSeconds(1));
        service.pollAll();
        assertEquals(PedalonsMigrationStatus.FAILED, m.getStatus());
        assertEquals("TIMEOUT", m.getErrorCode());
        assertNull(team.getPedalonsState());
    }

    @Test
    public void testAJobQueuedForTooLongTimesOut() throws Exception {
        assertTrue(PedalonsMigrationService.QUEUED_TIMEOUT.compareTo(PedalonsMigrationService.RUNNING_TIMEOUT.multipliedBy(4)) >= 0);
        PedalonsMigration m = running("req", false);
        m.setRunningSince(null);
        m.setTriggeredAt(NOW.minus(PedalonsMigrationService.QUEUED_TIMEOUT));
        when(client.status("0hx3k2m9q8r4t")).thenReturn(response(200, "{\"status\":\"QUEUED\"}"));

        service.pollAll();
        assertEquals(PedalonsMigrationStatus.RUNNING, m.getStatus());

        m.setTriggeredAt(NOW.minus(PedalonsMigrationService.QUEUED_TIMEOUT).minusSeconds(1));
        service.pollAll();
        assertEquals(PedalonsMigrationStatus.FAILED, m.getStatus());
        assertEquals("TIMEOUT", m.getErrorCode());
        assertTrue(m.getErrorMessage().contains("queued"));
        assertNull(team.getPedalonsState());
    }

    @Test
    public void testAJobRequeuedAfterRunningWaitsForTheQueueDelay() throws Exception {
        PedalonsMigration m = running("req", false);
        // nouvelle tentative de Pedalons : de nouveau QUEUED, 7 h apres le premier RUNNING
        m.setRunningSince(NOW.minus(Duration.ofHours(7)));
        when(client.status("0hx3k2m9q8r4t")).thenReturn(response(200, "{\"status\":\"QUEUED\"}"));

        service.pollAll();

        assertEquals(PedalonsMigrationStatus.RUNNING, m.getStatus());
        assertEquals(PedalonsState.MIGRATING, team.getPedalonsState());
    }

    @Test
    public void testUrlValidation() {
        assertEquals("https://www.pedalons.fr/a", PedalonsMigrationService.validUrl("https://www.pedalons.fr/a"));
        assertNull(PedalonsMigrationService.validUrl("javascript:alert(1)"));
        assertNull(PedalonsMigrationService.validUrl("//evil.example"));
        assertNull(PedalonsMigrationService.validUrl(""));
        assertNull(PedalonsMigrationService.validUrl(null));
    }

}
