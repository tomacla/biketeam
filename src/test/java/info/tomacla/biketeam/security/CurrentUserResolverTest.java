package info.tomacla.biketeam.security;

import info.tomacla.biketeam.domain.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Identifiant de l'utilisateur authentifie, lu hors contexte MVC par la chaine OAuth2.
 */
public class CurrentUserResolverTest {

    private CurrentUserResolver resolver;

    @BeforeEach
    public void setUp() {
        resolver = new CurrentUserResolver();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private OAuth2UserDetails details(String id) {
        User user = new User();
        user.setId(id);
        return OAuth2UserDetails.create(user);
    }

    @Test
    public void testNoAuthenticationMeansNoUser() {
        assertEquals(Optional.empty(), resolver.currentUserId());
    }

    @Test
    public void testAnonymousMeansNoUser() {

        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymousUser", List.of(Authorities.user())));

        assertEquals(Optional.empty(), resolver.currentUserId());

    }

    @Test
    public void testForeignPrincipalMeansNoUser() {

        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("someone", null, List.of(Authorities.user())));

        assertEquals(Optional.empty(), resolver.currentUserId());

    }

    @Test
    public void testAuthenticatedUserIsResolved() {

        OAuth2UserDetails principal = details("user-1");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));

        assertEquals(Optional.of("user-1"), resolver.currentUserId());

    }

    /**
     * Cas nominal d'un utilisateur Strava qui revient avec un cookie et veut lier son compte a
     * Google ou Facebook : le token remember-me DOIT etre accepte.
     */
    @Test
    public void testRememberMeAuthenticationIsAccepted() {

        OAuth2UserDetails principal = details("user-1");
        SecurityContextHolder.getContext().setAuthentication(
                new RememberMeAuthenticationToken("key", principal, principal.getAuthorities()));

        assertEquals(Optional.of("user-1"), resolver.currentUserId());

    }

}
