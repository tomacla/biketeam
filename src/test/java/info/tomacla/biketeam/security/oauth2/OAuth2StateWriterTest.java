package info.tomacla.biketeam.security.oauth2;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.url.UrlService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class OAuth2StateWriterTest {


    @Test
    public void test() {

        final Team team = new Team();

        TeamService teamService = Mockito.mock(TeamService.class);
        Mockito.when(teamService.get("test")).thenReturn(Optional.of(team));

        UrlService urlService = Mockito.mock(UrlService.class);
        Mockito.when(urlService.getTeamUrl(team)).thenReturn("http://teamurl.com");

        OAuth2StateWriter stateWriter = new OAuth2StateWriter(urlService, teamService);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("requestUri=/test/target");

        final String key = stateWriter.generateKey(request);

        assertEquals("http://teamurl.com/target", key.split(",")[1]);

    }

    @Test
    public void testUnknownTeam() {

        TeamService teamService = Mockito.mock(TeamService.class);
        Mockito.when(teamService.get("test")).thenReturn(Optional.empty());

        UrlService urlService = Mockito.mock(UrlService.class);
        Mockito.when(urlService.getUrlWithSuffix("/test/target")).thenReturn("http://teamurl.com/target");

        OAuth2StateWriter stateWriter = new OAuth2StateWriter(urlService, teamService);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("requestUri=/test/target");

        final String key = stateWriter.generateKey(request);

        assertEquals("http://teamurl.com/target", key.split(",")[1]);

    }

    @Test
    public void testWihtoutRequestUri() {

        TeamService teamService = Mockito.mock(TeamService.class);
        UrlService urlService = Mockito.mock(UrlService.class);
        OAuth2StateWriter stateWriter = new OAuth2StateWriter(urlService, teamService);

        final MockHttpServletRequest request = new MockHttpServletRequest();

        final String key = stateWriter.generateKey(request);

        assertNotNull(key);

    }


}
