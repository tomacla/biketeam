package info.tomacla.biketeam.security.session;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.Authorities;
import info.tomacla.biketeam.security.OAuth2UserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Reconstruction du principal en cours de requete.
 * <p>
 * Depuis Spring Security 6, l'enregistrement explicite du contexte est indispensable : sans
 * saveContext, le principal rafraichi serait perdu des la requete suivante et l'utilisateur
 * resterait bloque par AccountCompletionFilter.
 */
public class SecurityContextServiceTest {

    private SecurityContextRepository securityContextRepository;
    private SecurityContextService service;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    public void setUp() {

        securityContextRepository = mock(SecurityContextRepository.class);

        service = new SecurityContextService();
        ReflectionTestUtils.setField(service, "securityContextRepository", securityContextRepository);
        ReflectionTestUtils.setField(service, "rememberMeKey", "remember-me-key");

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        SecurityContextHolder.clearContext();

    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private User user(String id) {
        User user = new User();
        user.setId(id);
        user.setFirstName("Jean");
        user.setLastName("Dupont");
        user.setVerifiedEmail("jean@example.com");
        user.setPasswordHash("$2a$10$hash");
        return user;
    }

    private OAuth2UserDetails details(String id) {
        return OAuth2UserDetails.create(user(id));
    }

    private Authentication savedAuthentication() {

        ArgumentCaptor<SecurityContext> captor = ArgumentCaptor.forClass(SecurityContext.class);
        verify(securityContextRepository).saveContext(captor.capture(), any(), any());

        // le contexte du thread courant doit egalement avoir ete remplace
        assertSame(captor.getValue().getAuthentication(),
                SecurityContextHolder.getContext().getAuthentication());

        return captor.getValue().getAuthentication();

    }

    @Test
    public void testRefreshCurrentUserSavesContextWithFreshPrincipal() {

        OAuth2UserDetails stale = details("user-1");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(stale, null, stale.getAuthorities()));

        User updated = user("user-1");
        updated.setAdmin(true);

        service.refreshCurrentUser(updated, request, response);

        Authentication saved = savedAuthentication();

        assertInstanceOf(OAuth2UserDetails.class, saved.getPrincipal());
        assertNotSame(stale, saved.getPrincipal());
        assertEquals(Boolean.TRUE, ((OAuth2UserDetails) saved.getPrincipal()).getAttribute("admin"));
        assertTrue(saved.getAuthorities().contains(Authorities.admin()));

    }

    /**
     * Le type de token doit etre conserve : transformer une authentification remember-me en
     * authentification pleine contournerait les controles de fraicheur.
     */
    @Test
    public void testRefreshCurrentUserKeepsRememberMeTokenType() {

        OAuth2UserDetails stale = details("user-1");
        SecurityContextHolder.getContext().setAuthentication(
                new RememberMeAuthenticationToken("remember-me-key", stale, stale.getAuthorities()));

        service.refreshCurrentUser(user("user-1"), request, response);

        assertInstanceOf(RememberMeAuthenticationToken.class, savedAuthentication());

    }

    @Test
    public void testRefreshCurrentUserKeepsOAuth2TokenTypeAndRegistrationId() {

        OAuth2UserDetails stale = details("user-1");
        SecurityContextHolder.getContext().setAuthentication(
                new OAuth2AuthenticationToken(stale, stale.getAuthorities(), "google"));

        service.refreshCurrentUser(user("user-1"), request, response);

        Authentication saved = savedAuthentication();

        assertInstanceOf(OAuth2AuthenticationToken.class, saved);
        assertEquals("google", ((OAuth2AuthenticationToken) saved).getAuthorizedClientRegistrationId());

    }

    @Test
    public void testRefreshCurrentUserWithoutAuthenticationDoesNothing() {

        service.refreshCurrentUser(user("user-1"), request, response);

        verify(securityContextRepository, never()).saveContext(any(), any(), any());

    }

    @Test
    public void testRefreshCurrentUserWithNullUserDoesNothing() {

        OAuth2UserDetails stale = details("user-1");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(stale, null, stale.getAuthorities()));

        service.refreshCurrentUser(null, request, response);

        verify(securityContextRepository, never()).saveContext(any(), any(), any());

    }

    @Test
    public void testAddAuthorityKeepsPrincipalAndAddsAuthority() {

        OAuth2UserDetails principal = details("user-1");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of(Authorities.user())));

        service.addAuthority(Authorities.teamAdmin("team-1"), request, response);

        Authentication saved = savedAuthentication();

        assertSame(principal, saved.getPrincipal());
        assertTrue(saved.getAuthorities().contains(Authorities.user()));
        assertTrue(saved.getAuthorities().contains(Authorities.teamAdmin("team-1")));

    }

    @Test
    public void testAddAuthorityNeverDuplicates() {

        OAuth2UserDetails principal = details("user-1");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of(Authorities.user())));

        service.addAuthority(Authorities.user(), request, response);

        Authentication saved = savedAuthentication();

        assertEquals(1, saved.getAuthorities().size());

    }

    @Test
    public void testAddAuthorityWithoutAuthenticationDoesNothing() {

        service.addAuthority(Authorities.user(), request, response);

        verify(securityContextRepository, never()).saveContext(any(), any(), any());

    }

}
