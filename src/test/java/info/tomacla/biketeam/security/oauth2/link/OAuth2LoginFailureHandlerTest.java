package info.tomacla.biketeam.security.oauth2.link;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Un conflit de liaison ouvre l'ecran de fusion, jamais /login?error : l'utilisateur doit pouvoir
 * decider explicitement du sort des deux comptes.
 */
public class OAuth2LoginFailureHandlerTest {

    private OAuth2LoginFailureHandler handler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    public void setUp() {
        handler = new OAuth2LoginFailureHandler();
        request = new MockHttpServletRequest("GET", "/login/oauth2/code/google");
        response = new MockHttpServletResponse();
    }

    @Test
    public void testLinkConflictOpensTheMergeScreen() throws Exception {

        handler.onAuthenticationFailure(request, response, new OAuth2AuthenticationException(
                new OAuth2Error(AccountLinkService.ACCOUNT_LINK_CONFLICT, "conflit", null), "conflit"));

        assertEquals("/account/link-conflict", response.getRedirectedUrl());

    }

    @Test
    public void testOtherOAuth2ErrorsKeepTheDefaultBehaviour() throws Exception {

        handler.onAuthenticationFailure(request, response, new OAuth2AuthenticationException(
                new OAuth2Error("invalid_token", "jeton invalide", null), "jeton invalide"));

        assertEquals("/login?error", response.getRedirectedUrl());

    }

    @Test
    public void testNonOAuth2FailuresKeepTheDefaultBehaviour() throws Exception {

        handler.onAuthenticationFailure(request, response, new BadCredentialsException("nope"));

        assertEquals("/login?error", response.getRedirectedUrl());

    }

}
