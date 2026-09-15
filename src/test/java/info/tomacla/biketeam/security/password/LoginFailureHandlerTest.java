package info.tomacla.biketeam.security.password;

import info.tomacla.biketeam.domain.user.UserAuthTokenType;
import info.tomacla.biketeam.service.auth.AuthMailService;
import info.tomacla.biketeam.service.auth.RateLimitService;
import info.tomacla.biketeam.service.auth.UserAuthTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Echecs de connexion par mot de passe.
 * <p>
 * Le renvoi automatique d'un lien de confirmation est le seul chemin non authentifie declenchant
 * un mail sans passer par le comptage d'echecs : la limitation de debit et son caractere
 * inconditionnel cote reponse sont verrouilles ici.
 */
public class LoginFailureHandlerTest {

    private UserAuthTokenService userAuthTokenService;
    private AuthMailService authMailService;
    private RateLimitService rateLimitService;
    private LoginFailureHandler handler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    public void setUp() {

        userAuthTokenService = mock(UserAuthTokenService.class);
        authMailService = mock(AuthMailService.class);
        rateLimitService = mock(RateLimitService.class);

        handler = new LoginFailureHandler();
        ReflectionTestUtils.setField(handler, "userAuthTokenService", userAuthTokenService);
        ReflectionTestUtils.setField(handler, "authMailService", authMailService);
        ReflectionTestUtils.setField(handler, "rateLimitService", rateLimitService);

        when(userAuthTokenService.getEmailVerificationValidity()).thenReturn(Duration.ofHours(24));
        when(userAuthTokenService.isThrottled(anyString(), any(), anyInt())).thenReturn(false);
        when(rateLimitService.tryAcquire(anyString(), anyInt())).thenReturn(true);
        when(rateLimitService.clientKey(any(), anyString())).thenReturn("login-email-verification:1.2.3.4");
        when(userAuthTokenService.create(anyString(), any(), anyString(), any())).thenReturn("clear-token");

        request = new MockHttpServletRequest("POST", "/login");
        response = new MockHttpServletResponse();

    }

    @Test
    public void testBadCredentialsRedirectsToLoginError() throws Exception {

        handler.onAuthenticationFailure(request, response, new BadCredentialsException("nope"));

        assertEquals("/login?error", response.getRedirectedUrl());
        verifyNoInteractions(authMailService);

    }

    @Test
    public void testLockedAccountRedirectsToDedicatedError() throws Exception {

        handler.onAuthenticationFailure(request, response, new LockedException("locked"));

        assertEquals("/login?error=locked", response.getRedirectedUrl());
        verifyNoInteractions(authMailService);

    }

    @Test
    public void testUnverifiedEmailResendsVerificationAndRedirectsToPending() throws Exception {

        handler.onAuthenticationFailure(request, response,
                new EmailNotVerifiedException("user-1", "user@example.com"));

        verify(userAuthTokenService).create("user-1", UserAuthTokenType.EMAIL_VERIFICATION,
                "user@example.com", Duration.ofHours(24));
        verify(authMailService).sendEmailVerification("user@example.com", "clear-token");

        assertEquals("/register/pending", response.getRedirectedUrl());
        assertEquals("user@example.com",
                request.getSession().getAttribute(LoginFailureHandler.REGISTER_PENDING_EMAIL));

    }

    /**
     * Limitation par compte : aucun mail, mais la reponse reste strictement identique.
     */
    @Test
    public void testThrottledByAccountSendsNoMailButKeepsTheSameAnswer() throws Exception {

        when(userAuthTokenService.isThrottled("user-1", UserAuthTokenType.EMAIL_VERIFICATION, 5)).thenReturn(true);

        handler.onAuthenticationFailure(request, response,
                new EmailNotVerifiedException("user-1", "user@example.com"));

        verify(authMailService, never()).sendEmailVerification(anyString(), anyString());
        assertEquals("/register/pending", response.getRedirectedUrl());
        assertEquals("user@example.com",
                request.getSession().getAttribute(LoginFailureHandler.REGISTER_PENDING_EMAIL));

    }

    @Test
    public void testThrottledByClientSendsNoMailButKeepsTheSameAnswer() throws Exception {

        when(rateLimitService.tryAcquire(anyString(), anyInt())).thenReturn(false);

        handler.onAuthenticationFailure(request, response,
                new EmailNotVerifiedException("user-1", "user@example.com"));

        verify(authMailService, never()).sendEmailVerification(anyString(), anyString());
        assertEquals("/register/pending", response.getRedirectedUrl());

    }

    /**
     * Le & non court-circuitant est volontaire : la limitation par IP doit enregistrer la
     * tentative meme lorsque le compte est deja limite, sinon un attaquant echappe au comptage
     * par IP en saturant un seul compte.
     */
    @Test
    public void testClientCounterIsAlwaysIncrementedEvenWhenAccountIsThrottled() throws Exception {

        when(userAuthTokenService.isThrottled("user-1", UserAuthTokenType.EMAIL_VERIFICATION, 5)).thenReturn(true);

        handler.onAuthenticationFailure(request, response,
                new EmailNotVerifiedException("user-1", "user@example.com"));

        verify(rateLimitService).tryAcquire("login-email-verification:1.2.3.4", 10);

    }

    /**
     * Un echec d'envoi ne doit jamais faire echouer la requete de connexion.
     */
    @Test
    public void testMailFailureDoesNotBreakTheRedirect() throws Exception {

        doThrow(new IllegalStateException("smtp down"))
                .when(authMailService).sendEmailVerification(anyString(), anyString());

        handler.onAuthenticationFailure(request, response,
                new EmailNotVerifiedException("user-1", "user@example.com"));

        assertEquals("/register/pending", response.getRedirectedUrl());

    }

}
