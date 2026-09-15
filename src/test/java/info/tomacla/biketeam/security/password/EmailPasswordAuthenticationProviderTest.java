package info.tomacla.biketeam.security.password;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.OAuth2UserDetails;
import info.tomacla.biketeam.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.mock.web.MockHttpServletRequest;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class EmailPasswordAuthenticationProviderTest {

    private UserService userService;
    private PasswordEncoder passwordEncoder;
    private LoginAttemptService loginAttemptService;
    private EmailPasswordAuthenticationProvider provider;

    @BeforeEach
    public void setUp() throws Exception {
        userService = mock(UserService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        loginAttemptService = mock(LoginAttemptService.class);

        provider = new EmailPasswordAuthenticationProvider();
        set(provider, "userService", userService);
        set(provider, "passwordEncoder", passwordEncoder);
        set(provider, "loginAttemptService", loginAttemptService);

        when(passwordEncoder.encode(anyString())).thenReturn("dummy-hash");
        provider.init();
    }

    private static void set(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    private User completeUser() {
        User u = new User();
        u.setId("user-1");
        u.setVerifiedEmail("user@example.com");
        u.setPasswordHash("bcrypt-hash");
        u.setAuthTokenSeed("seed");
        return u;
    }

    /**
     * Le jeton porte des details web : l'origine de la requete entre dans le comptage des echecs
     * (verrouillage par couple email/IP et non par email seul).
     */
    private Authentication authToken(String email, String password) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(email, password);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        token.setDetails(new WebAuthenticationDetails(request));
        return token;
    }

    @Test
    public void testSuccessfulAuthenticationReturnsOAuth2UserDetailsWithIdAsUsername() {

        User u = completeUser();
        when(userService.getByEmail("user@example.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("goodpassword", "bcrypt-hash")).thenReturn(true);
        when(userService.ensureAuthTokenSeed(u)).thenReturn(u);

        Authentication result = provider.authenticate(authToken("User@Example.com", "goodpassword"));

        assertEquals(true, result.isAuthenticated());
        assertEquals(OAuth2UserDetails.class, result.getPrincipal().getClass());
        OAuth2UserDetails principal = (OAuth2UserDetails) result.getPrincipal();
        assertEquals("user-1", principal.getUsername());

        verify(loginAttemptService).recordSuccess("user@example.com", "10.0.0.1");
        verify(userService).ensureAuthTokenSeed(u);

    }

    @Test
    public void testWrongPasswordThrowsGenericBadCredentials() {

        User u = completeUser();
        when(userService.getByEmail("user@example.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("wrongpassword", "bcrypt-hash")).thenReturn(false);

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> provider.authenticate(authToken("user@example.com", "wrongpassword")));

        assertEquals("Email ou mot de passe incorrect.", ex.getMessage());
        verify(loginAttemptService).recordFailure("user@example.com", "10.0.0.1");
        verify(userService, never()).ensureAuthTokenSeed(any());

    }

    @Test
    public void testUnknownUserThrowsSameGenericExceptionAndStillHashesPassword() {

        when(userService.getByEmail("unknown@example.com")).thenReturn(Optional.empty());

        BadCredentialsException ex = assertThrows(BadCredentialsException.class,
                () -> provider.authenticate(authToken("unknown@example.com", "whatever")));

        assertEquals("Email ou mot de passe incorrect.", ex.getMessage());

        // egalisation du temps de reponse : passwordEncoder.matches() doit avoir ete appele
        // meme si l'utilisateur n'existe pas, sinon la latence revele les comptes existants
        verify(passwordEncoder).matches(eq("whatever"), eq("dummy-hash"));
        verify(loginAttemptService).recordFailure("unknown@example.com", "10.0.0.1");

    }

    @Test
    public void testEmailNotVerifiedThrowsAfterPasswordValidated() {

        User u = new User();
        u.setId("user-2");
        u.setEmail("pending@example.com");
        u.setPasswordHash("bcrypt-hash");
        // emailVerified reste false

        when(userService.getByEmail("pending@example.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("goodpassword", "bcrypt-hash")).thenReturn(true);

        EmailNotVerifiedException ex = assertThrows(EmailNotVerifiedException.class,
                () -> provider.authenticate(authToken("pending@example.com", "goodpassword")));

        assertEquals("user-2", ex.getUserId());
        assertEquals("pending@example.com", ex.getEmail());

        // le mot de passe a bien ete valide avant l'exception (aucune fuite avant verification)
        verify(passwordEncoder).matches("goodpassword", "bcrypt-hash");

    }

    @Test
    public void testLockedAccountThrowsBeforeLookup() {

        doThrow(new LockedException("Trop de tentatives de connexion. Réessayez dans quelques minutes."))
                .when(loginAttemptService).assertNotLocked("user@example.com", "10.0.0.1");

        assertThrows(LockedException.class, () -> provider.authenticate(authToken("user@example.com", "whatever")));

        verify(userService, never()).getByEmail(anyString());

    }

}
