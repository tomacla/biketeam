package info.tomacla.biketeam.web.pedalons;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.tomacla.biketeam.service.pedalons.PedalonsRedirectResolver;
import info.tomacla.biketeam.service.pedalons.PedalonsRedirectResolver.Action;
import info.tomacla.biketeam.domain.team.PedalonsState;
import info.tomacla.biketeam.service.pedalons.PedalonsRedirectResolver.Decision;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PedalonsRedirectFilterTest {

    private PedalonsRedirectResolver resolver;
    private PedalonsRedirectFilter filter;

    @BeforeEach
    public void setUp() {
        resolver = mock(PedalonsRedirectResolver.class);
        filter = new PedalonsRedirectFilter(resolver, 302);
    }

    private MockHttpServletResponse run(String method, String uri, MockFilterChain chain) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setQueryString("utm=1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        return response;
    }

    @Test
    public void testReadOfMigratedTeamIsRedirected() throws Exception {
        when(resolver.resolve("GET", "/n-peloton/rides/r")).thenReturn(new Decision(Action.REDIRECT, "https://p/equipes/n-peloton/sorties/s"));
        MockFilterChain chain = new MockFilterChain();

        MockHttpServletResponse response = run("GET", "/n-peloton/rides/r", chain);

        // 302 par defaut (PEDALONS_REDIRECT_STATUS)
        assertEquals(302, response.getStatus());
        // query string abandonnee
        assertEquals("https://p/equipes/n-peloton/sorties/s", response.getHeader("Location"));
        assertNull(chain.getRequest());
    }

    @Test
    public void testRedirectStatusIsConfigurable() throws Exception {
        when(resolver.resolve("GET", "/n-peloton")).thenReturn(new Decision(Action.REDIRECT, "https://p/equipes/n-peloton"));
        PedalonsRedirectFilter permanent = new PedalonsRedirectFilter(resolver, 301);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/n-peloton");
        MockHttpServletResponse response = new MockHttpServletResponse();
        permanent.doFilter(request, response, new MockFilterChain());

        assertEquals(301, response.getStatus());
        assertEquals("https://p/equipes/n-peloton", response.getHeader("Location"));
    }

    @Test
    public void testApiOfMigratedTeamIsGoneWithJson() throws Exception {
        when(resolver.resolve("GET", "/api/teams/n-peloton/rides/r")).thenReturn(new Decision(Action.API_GONE, "https://p/equipes/n-peloton"));
        MockFilterChain chain = new MockFilterChain();

        MockHttpServletResponse response = run("GET", "/api/teams/n-peloton/rides/r", chain);

        assertEquals(410, response.getStatus());
        assertNull(response.getHeader("Location"));
        assertTrue(response.getContentType().startsWith("application/json"));
        JsonNode body = new ObjectMapper().readTree(response.getContentAsByteArray());
        assertEquals("TEAM_MIGRATED", body.get("code").asText());
        assertEquals("https://p/equipes/n-peloton", body.get("url").asText());
        assertFalse(body.get("message").asText().isEmpty());
        assertNull(chain.getRequest());
    }

    @Test
    public void testWriteOnMigratedTeamIsGone() throws Exception {
        when(resolver.resolve("POST", "/n-peloton/rides/r/add-message")).thenReturn(new Decision(Action.REDIRECT, "https://p/equipes/n-peloton"));
        MockFilterChain chain = new MockFilterChain();

        MockHttpServletResponse response = run("POST", "/n-peloton/rides/r/add-message", chain);

        assertEquals(410, response.getStatus());
        assertTrue(response.getContentAsString().contains("https://p/equipes/n-peloton"));
        assertTrue(response.getContentAsString().contains("déménagé"));
        assertNull(chain.getRequest());
    }

    @Test
    public void testFrozenTeamIsLocked() throws Exception {
        when(resolver.resolve("GET", "/frozen/join")).thenReturn(new Decision(Action.LOCKED, null, PedalonsState.MIGRATING));
        MockFilterChain chain = new MockFilterChain();

        MockHttpServletResponse response = run("GET", "/frozen/join", chain);

        assertEquals(423, response.getStatus());
        assertTrue(response.getContentType().startsWith("text/html"));
        final String page = response.getContentAsString();
        assertTrue(page.contains("lecture seule"));
        assertTrue(page.contains("migration de ce groupe vers Pédalons est en cours"));
        assertFalse(page.contains("échec"));
        // URL Pedalons pas encore connue : pas de lien
        assertFalse(page.contains("<a "));
        assertNull(chain.getRequest());
    }

    @Test
    public void testReadyTeamLockedPageLinksToPedalons() throws Exception {
        when(resolver.resolve("POST", "/ready/rides/r/add-message"))
                .thenReturn(new Decision(Action.LOCKED, "https://p/equipes/ready?a=1&b=\"<x>", PedalonsState.READY));

        MockHttpServletResponse response = run("POST", "/ready/rides/r/add-message", new MockFilterChain());

        assertEquals(423, response.getStatus());
        final String page = response.getContentAsString();
        assertTrue(page.contains("migration de ce groupe vers Pédalons est terminée, en attente de bascule par l&#39;administrateur"));
        assertTrue(page.contains("<a href=\"https://p/equipes/ready?a=1&amp;b=&quot;&lt;x&gt;\">Voir le groupe sur Pédalons</a>"), page);
    }

    @Test
    public void testApiWriteOnFrozenTeamIsLockedWithJson() throws Exception {
        when(resolver.resolve("PUT", "/api/teams/ready/rides/r"))
                .thenReturn(new Decision(Action.API_LOCKED, "https://p/equipes/ready"));
        when(resolver.resolve("POST", "/api/teams/frozen/rides/r/participants"))
                .thenReturn(new Decision(Action.API_LOCKED, null));
        MockFilterChain chain = new MockFilterChain();

        MockHttpServletResponse response = run("PUT", "/api/teams/ready/rides/r", chain);

        assertEquals(423, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        JsonNode body = new ObjectMapper().readTree(response.getContentAsByteArray());
        assertEquals("TEAM_FROZEN", body.get("code").asText());
        assertEquals("Ce groupe migre vers Pédalons : il est en lecture seule jusqu'à sa bascule.", body.get("message").asText());
        assertEquals("https://p/equipes/ready", body.get("url").asText());
        assertNull(chain.getRequest());

        // URL Pedalons pas encore connue : url presente, a null
        MockHttpServletResponse unknown = run("POST", "/api/teams/frozen/rides/r/participants", new MockFilterChain());
        assertEquals(423, unknown.getStatus());
        JsonNode unknownBody = new ObjectMapper().readTree(unknown.getContentAsByteArray());
        assertEquals("TEAM_FROZEN", unknownBody.get("code").asText());
        assertTrue(unknownBody.has("url"));
        assertTrue(unknownBody.get("url").isNull());
    }

    @Test
    public void testOtherRequestsPassThrough() throws Exception {
        when(resolver.resolve("GET", "/teams")).thenReturn(new Decision(Action.PASS, null));
        MockFilterChain chain = new MockFilterChain();

        MockHttpServletResponse response = run("GET", "/teams", chain);

        assertEquals(200, response.getStatus());
        assertNotNull(chain.getRequest());
    }

    @Test
    public void testResolverFailureDoesNotBreakTheSite() throws Exception {
        when(resolver.resolve(anyString(), anyString())).thenThrow(new IllegalStateException("db down"));
        FilterChain chain = mock(FilterChain.class);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/n-peloton");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    public void testResolverReceivesTheDecodedPath() throws Exception {
        when(resolver.resolve("GET", "/n-peloton/join")).thenReturn(new Decision(Action.LOCKED, null));
        when(resolver.resolve("GET", "/n-peloton/maps/Boucle_de_l\u2019Erdre"))
                .thenReturn(new Decision(Action.REDIRECT, "https://p/equipes/n-peloton/parcours/boucle"));

        // comme Spring MVC : decode, sans le contenu apres ';'
        assertEquals(423, run("GET", "/%6E-peloton/join", new MockFilterChain()).getStatus());
        assertEquals(423, run("GET", "/n-peloton;jsessionid=x/join", new MockFilterChain()).getStatus());
        assertEquals(302, run("GET", "/n-peloton/maps/Boucle_de_l%E2%80%99Erdre", new MockFilterChain()).getStatus());
    }

    @Test
    public void testUndecodablePathPassesThrough() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        MockHttpServletResponse response = run("GET", "/n-peloton/%zz", chain);

        assertEquals(200, response.getStatus());
        assertNotNull(chain.getRequest());
        verifyNoInteractions(resolver);
    }

}
