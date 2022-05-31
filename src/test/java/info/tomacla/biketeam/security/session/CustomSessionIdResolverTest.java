package info.tomacla.biketeam.security.session;

import info.tomacla.biketeam.service.url.UrlService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomSessionIdResolverTest {

    @Test
    public void test() {

        SSOService ssoService = Mockito.mock(SSOService.class);
        Mockito.when(ssoService.getSessionIdFromSSOToken("foo")).thenReturn(Optional.of("sessionId"));

        UrlService urlService = Mockito.mock(UrlService.class);

        final CustomSessionIdResolver c = new CustomSessionIdResolver(urlService, ssoService);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("sso=foo");

        final List<String> strings = c.resolveSessionIds(request);

        assertTrue(strings.contains("sessionId"));


    }

    @Test
    public void testNoSession() {

        SSOService ssoService = Mockito.mock(SSOService.class);
        Mockito.when(ssoService.getSessionIdFromSSOToken("foo")).thenReturn(Optional.empty());

        UrlService urlService = Mockito.mock(UrlService.class);

        final CustomSessionIdResolver c = new CustomSessionIdResolver(urlService, ssoService);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("sso=foo");

        final List<String> strings = c.resolveSessionIds(request);

        assertTrue(strings.isEmpty());


    }

    @Test
    public void testNoSso() {

        SSOService ssoService = Mockito.mock(SSOService.class);
        Mockito.when(ssoService.getSessionIdFromSSOToken("foo")).thenReturn(Optional.of("sessionId"));

        UrlService urlService = Mockito.mock(UrlService.class);

        final CustomSessionIdResolver c = new CustomSessionIdResolver(urlService, ssoService);

        final MockHttpServletRequest request = new MockHttpServletRequest();

        final List<String> strings = c.resolveSessionIds(request);

        assertTrue(strings.isEmpty());


    }

}
