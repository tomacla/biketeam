package info.tomacla.biketeam.service.pedalons;

import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.pedalons.PedalonsRedirect;
import info.tomacla.biketeam.domain.pedalons.PedalonsRedirectRepository;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.team.PedalonsState;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamRepository;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.domain.trip.TripKey;
import info.tomacla.biketeam.domain.trip.TripRepository;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.RideService;
import info.tomacla.biketeam.service.TripService;
import info.tomacla.biketeam.service.pedalons.PedalonsRedirectResolver.Action;
import info.tomacla.biketeam.service.pedalons.PedalonsRedirectResolver.Decision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import info.tomacla.biketeam.web.pedalons.PedalonsRedirectFilter;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Resolution des redirections d'un groupe bascule (section 9.4 du plan) et du gel (9.5).
 */
public class PedalonsRedirectResolverTest {

    private static final String BASE = "https://www.pedalons.fr/equipes/n-peloton";

    private TeamRepository teamRepository;
    private PedalonsRedirectRepository redirectRepository;
    private MapService mapService;
    private RideService rideService;
    private TripService tripService;
    private TripRepository tripRepository;
    private PedalonsRedirectCache cache;
    private PedalonsRedirectResolver resolver;

    @BeforeEach
    public void setUp() {
        teamRepository = mock(TeamRepository.class);
        redirectRepository = mock(PedalonsRedirectRepository.class);
        mapService = mock(MapService.class);
        rideService = mock(RideService.class);
        tripService = mock(TripService.class);
        tripRepository = mock(TripRepository.class);
        when(mapService.get(anyString(), anyString())).thenReturn(Optional.empty());
        when(rideService.get(anyString(), anyString())).thenReturn(Optional.empty());
        when(tripService.get(anyString(), anyString())).thenReturn(Optional.empty());
        when(tripService.findById(anyString())).thenReturn(Optional.empty());
        when(tripService.findByPermalink(anyString())).thenReturn(Optional.empty());

        Team migrated = team("n-peloton", PedalonsState.MIGRATED, BASE);
        Team frozen = team("frozen", PedalonsState.MIGRATING, null);
        Team ready = team("ready", PedalonsState.READY, "https://www.pedalons.fr/equipes/ready");
        when(teamRepository.findByPedalonsStateIsNotNull()).thenReturn(List.of(migrated, frozen, ready));
        when(redirectRepository.findByTeamIdIn(List.of("n-peloton"))).thenReturn(List.of(
                redirect(PedalonsRedirect.TEAM, "n-peloton", BASE),
                redirect(PedalonsRedirect.TEAM_ABOUT, "n-peloton", BASE + "/a-propos"),
                redirect(PedalonsRedirect.ROUTES_LIST, "n-peloton", BASE + "/parcours"),
                redirect(PedalonsRedirect.ROUTE, "map-1", BASE + "/parcours/boucle-du-lac"),
                redirect(PedalonsRedirect.RIDE, "ride-1", BASE + "/sorties/sortie-du-12"),
                redirect(PedalonsRedirect.TRIP, "trip-1", BASE + "/voyages/tour-2025"),
                redirect(PedalonsRedirect.POST, "pub-1", BASE + "/articles/ag-2025")));
        when(tripRepository.findKeysByTeamIdIn(List.of("n-peloton"))).thenReturn(List.of(
                new TripKey("trip-1", "n-peloton", "tour-2025", false),
                new TripKey("trip-2", "n-peloton", "tour-2024", false),
                new TripKey("trip-3", "n-peloton", "tour-supprime", true)));

        cache = new PedalonsRedirectCache(teamRepository, redirectRepository, tripRepository);
        resolver = new PedalonsRedirectResolver(cache, mapService, rideService, tripService);
    }

    private static Team team(String id, PedalonsState state, String url) {
        Team team = new Team();
        team.setId(id);
        team.setPedalonsState(state);
        team.setPedalonsUrl(url);
        return team;
    }

    private static PedalonsRedirect redirect(String type, String id, String url) {
        return new PedalonsRedirect("n-peloton", type, id, url);
    }

    private Decision get(String path) {
        return resolver.resolve("GET", path);
    }

    private void assertRedirect(String expectedUrl, Decision decision) {
        assertEquals(Action.REDIRECT, decision.action());
        assertEquals(expectedUrl, decision.url());
    }

    @Test
    public void testTeamRoot() {
        assertRedirect(BASE, get("/n-peloton"));
        assertRedirect(BASE, get("/n-peloton/"));
        // premier segment compare en minuscules
        assertRedirect(BASE, get("/N-Peloton"));
    }

    @Test
    public void testFaqFallsBackToAboutPage() {
        assertRedirect(BASE + "/a-propos", get("/n-peloton/faq"));
    }

    @Test
    public void testMapsById() {
        assertRedirect(BASE + "/parcours", get("/n-peloton/maps"));
        assertRedirect(BASE + "/parcours", get("/n-peloton/maps/"));
        assertRedirect(BASE + "/parcours", get("/n-peloton/maps/android"));
        assertRedirect(BASE + "/parcours/boucle-du-lac", get("/n-peloton/maps/map-1"));
        assertRedirect(BASE + "/parcours/boucle-du-lac", get("/n-peloton/maps/map-1/gpx"));
        assertRedirect(BASE + "/parcours/boucle-du-lac", get("/n-peloton/maps/map-1/add-favorite"));
    }

    @Test
    public void testMapsByPermalinkAreResolvedWithTheService() {
        Map map = new Map();
        map.setId("map-1");
        map.setTeamId("n-peloton");
        when(mapService.get("n-peloton", "boucle-du-lac")).thenReturn(Optional.of(map));
        assertRedirect(BASE + "/parcours/boucle-du-lac", get("/n-peloton/maps/boucle-du-lac/fit"));
    }

    @Test
    public void testRidesAndTrips() {
        assertRedirect(BASE, get("/n-peloton/rides"));
        assertRedirect(BASE + "/sorties/sortie-du-12", get("/n-peloton/rides/ride-1"));
        assertRedirect(BASE + "/sorties/sortie-du-12", get("/n-peloton/rides/ride-1/messages"));

        Ride ride = new Ride();
        ReflectionTestUtils.setField(ride, "id", "ride-1");
        when(rideService.get("n-peloton", "sortie-du-12")).thenReturn(Optional.of(ride));
        assertRedirect(BASE + "/sorties/sortie-du-12", get("/n-peloton/rides/sortie-du-12"));

        assertRedirect(BASE, get("/n-peloton/trips/"));
        assertRedirect(BASE + "/voyages/tour-2025", get("/n-peloton/trips/trip-1/notes"));
    }

    @Test
    public void testPublications() {
        assertRedirect(BASE + "/articles/ag-2025", get("/n-peloton/publications/pub-1"));
        assertRedirect(BASE + "/articles/ag-2025", get("/n-peloton/publications/pub-1/image"));
    }

    @Test
    public void testUnresolvedFallsBackToTeamPage() {
        assertRedirect(BASE, get("/n-peloton/rides/unknown"));
        assertRedirect(BASE, get("/n-peloton/join"));
        assertRedirect(BASE, get("/n-peloton/admin/rides"));
    }

    @Test
    public void testFallbackToPedalonsUrlWithoutTeamEntry() {
        when(redirectRepository.findByTeamIdIn(List.of("n-peloton"))).thenReturn(List.of());
        cache.invalidate();
        assertRedirect(BASE, get("/n-peloton/rides/ride-1"));
    }

    @Test
    public void testApiPathsAreGoneWithTheTeamUrl() {
        // lectures comprises : un client d'API ne sait que faire d'une redirection vers du HTML
        for (Decision decision : List.of(
                get("/api/teams/n-peloton"),
                get("/api/teams/n-peloton/maps/map-1"),
                get("/api/teams/n-peloton/rides/ride-1"),
                get("/api/teams/N-Peloton/publications/pub-1"),
                resolver.resolve("POST", "/api/teams/n-peloton/rides/ride-1/participants"))) {
            assertEquals(Action.API_GONE, decision.action());
            assertEquals(BASE, decision.url());
        }
        verifyNoInteractions(mapService, rideService);
    }

    @Test
    public void testTeamReadyToSwitchStaysFrozenButIsNotRedirected() {
        assertEquals(Action.PASS, get("/ready").action());
        assertEquals(Action.PASS, get("/ready/rides/r").action());
        assertEquals(Action.PASS, resolver.resolve("GET", "/api/teams/ready/rides/r").action());
        // page 423 : URL Pedalons connue et etat READY (texte « en attente de bascule »)
        assertEquals(new Decision(Action.LOCKED, "https://www.pedalons.fr/equipes/ready", PedalonsState.READY), get("/ready/join"));
        assertEquals(Action.LOCKED, resolver.resolve("POST", "/ready/admin/rides/r").action());
        assertEquals(new Decision(Action.API_LOCKED, "https://www.pedalons.fr/equipes/ready"), resolver.resolve("PUT", "/api/teams/ready/rides/r"));
        assertEquals(Action.PASS, get("/ready/admin/pedalons").action());
        assertEquals(Action.PASS, resolver.resolve("POST", "/ready/admin/pedalons/switch").action());
    }

    @Test
    public void testCatalogTrip() {
        // id ou permalien, resolus par le cache
        assertRedirect(BASE + "/voyages/tour-2025", get("/catalog/trips/trip-1"));
        assertRedirect(BASE + "/voyages/tour-2025", get("/catalog/trips/tour-2025"));
        // voyage de l'equipe absent de la table d'URL : page d'equipe
        assertRedirect(BASE, get("/catalog/trips/trip-2"));
        assertRedirect(BASE, get("/catalog/trips/tour-2024"));
        assertRedirect(BASE, get("/catalog/trips/trip-3"));
        // permalien d'un voyage supprime : ignore, comme TripService.findByPermalink
        assertEquals(Action.PASS, get("/catalog/trips/tour-supprime").action());
        // voyage d'une autre equipe
        assertEquals(Action.PASS, get("/catalog/trips/other").action());
        verifyNoInteractions(tripService);
    }

    @Test
    public void testCatalogTripIsResolvedWithoutSqlPerRequest() {
        for (int i = 0; i < 5; i++) {
            get("/catalog/trips/trip-1");
            get("/catalog/trips/other-" + i);
        }
        // un chargement du cache : une requete par table, aucune par requete HTTP
        verify(teamRepository, times(1)).findByPedalonsStateIsNotNull();
        verify(redirectRepository, times(1)).findByTeamIdIn(List.of("n-peloton"));
        verify(tripRepository, times(1)).findKeysByTeamIdIn(List.of("n-peloton"));
        verify(redirectRepository, never()).findByTeamId(anyString());
        verifyNoInteractions(tripService);
    }

    @Test
    public void testCatalogTripWithoutMigratedTeamLooksNothingUp() {
        when(teamRepository.findByPedalonsStateIsNotNull()).thenReturn(List.of(
                team("frozen", PedalonsState.MIGRATING, null), team("ready", PedalonsState.READY, null)));
        cache.invalidate();

        assertEquals(Action.PASS, get("/catalog/trips/trip-1").action());
        assertEquals(Action.PASS, get("/catalog/trips/tour-2025").action());
        verifyNoInteractions(tripService, redirectRepository, tripRepository);
    }

    @Test
    public void testMigrationPageStaysReachable() {
        assertEquals(Action.PASS, get("/n-peloton/admin/pedalons").action());
        assertEquals(Action.PASS, get("/frozen/admin/pedalons").action());
        assertEquals(Action.PASS, resolver.resolve("POST", "/frozen/admin/pedalons/start").action());
    }

    @Test
    public void testTeamLogoIsStillServedAfterTheSwitch() {
        assertEquals(Action.PASS, get("/n-peloton/image").action());
    }

    @Test
    public void testNonMigratedTeamsAndInternalPathsPass() {
        assertEquals(Action.PASS, get("/other-team/rides/ride-1").action());
        assertEquals(Action.PASS, get("/teams").action());
        assertEquals(Action.PASS, get("/").action());
        assertEquals(Action.PASS, get("/internal/pedalons/teams/n-peloton/snapshot").action());
        verifyNoInteractions(mapService, rideService);
    }

    @Test
    public void testFrozenTeamRefusesWrites() {
        // URL Pedalons pas encore connue : page 423 sans lien, texte « migration en cours »
        assertEquals(new Decision(Action.LOCKED, null, PedalonsState.MIGRATING), resolver.resolve("POST", "/frozen/rides/r/add-message"));
        assertEquals(Action.LOCKED, resolver.resolve("POST", "/frozen/admin/rides/r").action());
        assertEquals(new Decision(Action.API_LOCKED, null), resolver.resolve("PUT", "/api/teams/frozen/rides/r"));
        assertEquals(Action.LOCKED, get("/frozen/join").action());
        assertEquals(Action.LOCKED, get("/frozen/leave").action());
        assertEquals(Action.LOCKED, get("/frozen/rides/r/add-participant/g").action());
        assertEquals(Action.LOCKED, get("/frozen/trips/t/add-participant").action());
        assertEquals(Action.LOCKED, get("/frozen/rides/r/remove-message/m").action());
        assertEquals(Action.LOCKED, get("/frozen/maps/m/add-favorite").action());
        assertEquals(Action.LOCKED, get("/frozen/admin/rides").action());
    }

    @Test
    public void testFrozenTeamServesReads() {
        assertEquals(Action.PASS, get("/frozen").action());
        assertEquals(Action.PASS, get("/frozen/rides/r").action());
        assertEquals(Action.PASS, get("/frozen/maps/m/gpx").action());
        assertEquals(Action.PASS, resolver.resolve("HEAD", "/frozen/maps").action());
        assertEquals(Action.PASS, get("/api/teams/frozen/rides").action());
    }

    @Test
    public void testCacheIsLoadedOnceUntilInvalidated() {
        get("/n-peloton");
        get("/n-peloton/rides");
        verify(teamRepository, times(1)).findByPedalonsStateIsNotNull();
        cache.invalidate();
        get("/n-peloton");
        verify(teamRepository, times(2)).findByPedalonsStateIsNotNull();
    }

    @Test
    public void testNonAsciiPermalinkIsResolvedWithTheService() {
        Map map = new Map();
        map.setId("map-1");
        map.setTeamId("n-peloton");
        // le filtre passe le chemin decode : le permalien arrive tel qu'en base
        when(mapService.get("n-peloton", "Boucle_de_l\u2019Erdre")).thenReturn(Optional.of(map));
        assertRedirect(BASE + "/parcours/boucle-du-lac", get("/n-peloton/maps/Boucle_de_l\u2019Erdre"));
    }

    @Test
    public void testEncodedPathsDoNotEscapeTheFilter() throws Exception {
        PedalonsRedirectFilter filter = new PedalonsRedirectFilter(resolver, 301);

        // gel : un segment d'equipe encode reste une ecriture sur l'equipe gelee
        MockHttpServletResponse locked = filter("GET", "/%66rozen/join", filter);
        assertEquals(PedalonsRedirectFilter.SC_LOCKED, locked.getStatus());
        MockHttpServletResponse apiLocked = filter("POST", "/api/teams/%66rozen/rides/r", filter);
        assertEquals(PedalonsRedirectFilter.SC_LOCKED, apiLocked.getStatus());
        assertTrue(apiLocked.getContentType().startsWith("application/json"));
        assertTrue(apiLocked.getContentAsString().contains("\"code\":\"TEAM_FROZEN\""));
        assertTrue(locked.getContentType().startsWith("text/html"));
        assertEquals(PedalonsRedirectFilter.SC_LOCKED, filter("GET", "/frozen;x=1/join", filter).getStatus());

        // bascule
        MockHttpServletResponse moved = filter("GET", "/%6E-peloton/rides/ride-1", filter);
        assertEquals(301, moved.getStatus());
        assertEquals(BASE + "/sorties/sortie-du-12", moved.getHeader("Location"));

        // permalien non ASCII, servi encode
        Map map = new Map();
        map.setId("map-1");
        map.setTeamId("n-peloton");
        when(mapService.get("n-peloton", "Boucle_de_l\u2019Erdre")).thenReturn(Optional.of(map));
        MockHttpServletResponse permalink = filter("GET", "/n-peloton/maps/Boucle_de_l%E2%80%99Erdre", filter);
        assertEquals(301, permalink.getStatus());
        assertEquals(BASE + "/parcours/boucle-du-lac", permalink.getHeader("Location"));
    }

    private static MockHttpServletResponse filter(String method, String uri, PedalonsRedirectFilter filter) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test
    public void testInvalidationWaitsForAnOngoingLoad() throws Exception {
        CountDownLatch loading = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Team frozen = team("frozen", PedalonsState.MIGRATING, null);
        // premier chargement : lit encore l'etat gele, puis le degel est valide pendant qu'il tourne
        when(teamRepository.findByPedalonsStateIsNotNull())
                .thenAnswer(invocation -> {
                    loading.countDown();
                    assertTrue(release.await(5, TimeUnit.SECONDS));
                    return List.of(frozen);
                })
                .thenReturn(List.of());

        Thread reader = new Thread(() -> cache.get("frozen"));
        reader.start();
        assertTrue(loading.await(5, TimeUnit.SECONDS));

        Thread invalidation = new Thread(cache::invalidate);
        invalidation.start();
        invalidation.join(200);

        release.countDown();
        reader.join(5000);
        invalidation.join(5000);
        assertFalse(reader.isAlive());
        assertFalse(invalidation.isAlive());

        // le chargement perime n'a pas survecu a l'invalidation
        assertTrue(cache.get("frozen").isEmpty());
    }

}
