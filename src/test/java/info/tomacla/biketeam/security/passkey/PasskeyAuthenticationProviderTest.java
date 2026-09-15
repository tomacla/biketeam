package info.tomacla.biketeam.security.passkey;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.OAuth2UserDetails;
import info.tomacla.biketeam.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PasskeyAuthenticationProviderTest {

    private UserService userService;
    private PasskeyAuthenticationProvider provider;

    @BeforeEach
    public void setUp() {
        userService = mock(UserService.class);
        provider = new PasskeyAuthenticationProvider();
        ReflectionTestUtils.setField(provider, "userService", userService);
    }

    private User user() {
        User u = new User();
        u.setId("user-1");
        u.setVerifiedEmail("user@example.com");
        u.setAuthTokenSeed("seed");
        return u;
    }

    /**
     * Le provider mot de passe declare supporter UsernamePasswordAuthenticationToken : les deux
     * ne doivent jamais se recouvrir, sans quoi chaque connexion passerait par le mauvais.
     */
    @Test
    public void testSupportsOnlyPasskeyToken() {
        assertTrue(provider.supports(PasskeyAuthenticationToken.class));
        assertTrue(!provider.supports(org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class));
    }

    @Test
    public void testSuccessfulAuthenticationReturnsOAuth2UserDetailsWithIdAsUsername() {

        User u = user();
        when(userService.get("user-1")).thenReturn(Optional.of(u));
        when(userService.ensureAuthTokenSeed(u)).thenReturn(u);

        Authentication result = provider.authenticate(
                PasskeyAuthenticationToken.unauthenticated("user-1", "pk-1"));

        assertInstanceOf(PasskeyAuthenticationToken.class, result);
        assertTrue(result.isAuthenticated());

        OAuth2UserDetails principal = (OAuth2UserDetails) result.getPrincipal();
        assertEquals("user-1", principal.getUsername());
        assertEquals("pk-1", ((PasskeyAuthenticationToken) result).getPasskeyId());

    }

    /**
     * Comme pour la connexion par mot de passe : la graine remember-me heritee de la migration
     * est remplacee des la premiere authentification interactive.
     */
    @Test
    public void testAuthenticationRotatesLegacySeed() {

        User u = user();
        when(userService.get("user-1")).thenReturn(Optional.of(u));
        when(userService.ensureAuthTokenSeed(u)).thenReturn(u);

        provider.authenticate(PasskeyAuthenticationToken.unauthenticated("user-1", "pk-1"));

        verify(userService).ensureAuthTokenSeed(u);

    }

    @Test
    public void testUnknownUserIsRejected() {

        when(userService.get("ghost")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () ->
                provider.authenticate(PasskeyAuthenticationToken.unauthenticated("ghost", "pk-1")));

    }

    /**
     * La suppression de compte est un soft delete : la ligne existe encore et ses passkeys
     * pourraient survivre a une migration partielle. Le refus est donc verifie ici aussi.
     */
    @Test
    public void testDeletedUserIsRejected() {

        User u = user();
        u.setDeletion(true);
        when(userService.get("user-1")).thenReturn(Optional.of(u));

        assertThrows(DisabledException.class, () ->
                provider.authenticate(PasskeyAuthenticationToken.unauthenticated("user-1", "pk-1")));

    }

    /**
     * setAuthenticated(true) doit rester impossible : un jeton ne devient authentifie que par la
     * fabrique, apres passage du provider.
     */
    @Test
    public void testTokenCannotBeMarkedAuthenticatedManually() {
        PasskeyAuthenticationToken token = PasskeyAuthenticationToken.unauthenticated("user-1", "pk-1");
        assertThrows(IllegalArgumentException.class, () -> token.setAuthenticated(true));
    }

    @Test
    public void testTokenExposesNoCredentials() {
        assertEquals(null,
                PasskeyAuthenticationToken.unauthenticated("user-1", "pk-1").getCredentials());
    }

}
