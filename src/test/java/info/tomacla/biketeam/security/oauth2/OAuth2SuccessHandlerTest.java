package info.tomacla.biketeam.security.oauth2;

import info.tomacla.biketeam.security.session.SSOService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OAuth2SuccessHandlerTest {

    @Test
    public void testWithoutState() {

        final SSOService ssoService = Mockito.mock(SSOService.class);
        OAuth2SuccessHandler h = new OAuth2SuccessHandler(ssoService);

        final MockHttpServletRequest request = new MockHttpServletRequest();

        assertEquals("/", h.determineTargetUrl(request, null));

    }

    @Test
    public void testWithFalseState() {

        final SSOService ssoService = Mockito.mock(SSOService.class);
        OAuth2SuccessHandler h = new OAuth2SuccessHandler(ssoService);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("state=toto");

        assertEquals("/", h.determineTargetUrl(request, null));

    }

    @Test
    public void testWithStateLogged() {

        final MockHttpSession session = new MockHttpSession();

        final SSOService ssoService = Mockito.mock(SSOService.class);
        Mockito.when(ssoService.getSSOToken(session.getId(), null)).thenReturn("SSO1");

        OAuth2SuccessHandler h = new OAuth2SuccessHandler(ssoService);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("state=xxxx,https://www.target.com/foo");
        request.setSession(session);

        assertEquals("https://www.target.com/foo?sso=SSO1", h.determineTargetUrl(request, null));

    }

    @Test
    public void testWithStateLoggedWithParams() {

        final MockHttpSession session = new MockHttpSession();

        final SSOService ssoService = Mockito.mock(SSOService.class);
        Mockito.when(ssoService.getSSOToken(session.getId(), null)).thenReturn("SSO1");

        OAuth2SuccessHandler h = new OAuth2SuccessHandler(ssoService);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("state=xxxx,https://www.target.com/foo?test=test");
        request.setSession(session);

        assertEquals("https://www.target.com/foo?test=test&sso=SSO1", h.determineTargetUrl(request, null));

    }

    @Test
    public void testWithStateLoggedAndRememberMe() {

        final MockHttpSession session = new MockHttpSession();

        final SSOService ssoService = Mockito.mock(SSOService.class);
        Mockito.when(ssoService.getSSOToken(session.getId(), "rm")).thenReturn("SSO1");

        OAuth2SuccessHandler h = new OAuth2SuccessHandler(ssoService);


        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("state=xxxx,https://www.target.com/foo");
        request.setSession(session);
        request.setCookies(new MockCookie("remember-me", "rm"));

        assertEquals("https://www.target.com/foo?sso=SSO1", h.determineTargetUrl(request, null));

    }

    @Test
    public void testWithStateNotLogged() {

        final SSOService ssoService = Mockito.mock(SSOService.class);
        OAuth2SuccessHandler h = new OAuth2SuccessHandler(ssoService);
        final MockHttpSession session = new MockHttpSession();

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("state=xxxx,https://www.target.com/foo");

        assertEquals("https://www.target.com/foo", h.determineTargetUrl(request, null));

    }


}
