package info.tomacla.biketeam.security.oauth2.link;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.CurrentUserResolver;
import info.tomacla.biketeam.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.servlet.http.HttpSession;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Les 6 lignes de la table de decision du §5.3 de la liaison OAuth2.
 */
public class AccountLinkServiceTest {

    private UserService userService;
    private CurrentUserResolver currentUserResolver;
    private OAuth2LinkIntentStore linkIntentStore;
    private AccountLinkService service;

    @BeforeEach
    public void setUp() {
        userService = mock(UserService.class);
        currentUserResolver = mock(CurrentUserResolver.class);
        linkIntentStore = mock(OAuth2LinkIntentStore.class);
        service = new AccountLinkService(userService, currentUserResolver, linkIntentStore);
    }

    private User user(String id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    @Test
    public void testNoSessionUnknownIdentity_createsAccount() {

        when(currentUserResolver.currentUserId()).thenReturn(Optional.empty());

        Optional<User> target = service.resolveLinkTarget("google", "google-sub-1", Optional.empty(), "new@example.com", true);

        assertTrue(target.isEmpty());
        verify(linkIntentStore, never()).consumeIntent(anyString());

    }

    @Test
    public void testNoSessionKnownIdentity_normalLogin() {

        when(currentUserResolver.currentUserId()).thenReturn(Optional.empty());
        User owner = user("owner-1");

        Optional<User> target = service.resolveLinkTarget("google", "google-sub-1", Optional.of(owner), "owner@example.com", true);

        assertTrue(target.isEmpty());

    }

    /**
     * Ligne critique : authentifie SANS intention -> jamais de liaison, meme si l'identite est
     * inconnue. Un clic sur "Connexion avec Google" alors qu'une session est ouverte doit rester
     * une connexion normale (changement de compte), pas une liaison silencieuse.
     */
    @Test
    public void testAuthenticatedWithoutIntent_neverLinks() {

        when(currentUserResolver.currentUserId()).thenReturn(Optional.of("current-1"));
        when(linkIntentStore.consumeIntent("google")).thenReturn(false);

        Optional<User> target = service.resolveLinkTarget("google", "google-sub-1", Optional.empty(), "new@example.com", true);

        assertTrue(target.isEmpty());
        verify(userService, never()).get(anyString());

    }

    @Test
    public void testAuthenticatedWithIntentUnknownIdentityFreeEmail_links() {

        when(currentUserResolver.currentUserId()).thenReturn(Optional.of("current-1"));
        when(linkIntentStore.consumeIntent("google")).thenReturn(true);
        User current = user("current-1");
        when(userService.get("current-1")).thenReturn(Optional.of(current));
        when(userService.getByEmail("new@example.com")).thenReturn(Optional.empty());

        Optional<User> target = service.resolveLinkTarget("google", "google-sub-1", Optional.empty(), "new@example.com", true);

        assertTrue(target.isPresent());
        assertEquals("current-1", target.get().getId());

    }

    @Test
    public void testAuthenticatedWithIntentIdentityAlreadyOnCurrentAccount_refreshOnly() {

        when(currentUserResolver.currentUserId()).thenReturn(Optional.of("current-1"));
        when(linkIntentStore.consumeIntent("google")).thenReturn(true);
        User current = user("current-1");
        when(userService.get("current-1")).thenReturn(Optional.of(current));

        Optional<User> target = service.resolveLinkTarget("google", "google-sub-1", Optional.of(current), "current@example.com", true);

        assertTrue(target.isPresent());
        assertEquals("current-1", target.get().getId());

    }

    @Test
    public void testAuthenticatedWithIntentIdentityBelongsToAnotherAccount_conflict() {

        when(currentUserResolver.currentUserId()).thenReturn(Optional.of("current-1"));
        when(linkIntentStore.consumeIntent("google")).thenReturn(true);
        User current = user("current-1");
        User other = user("other-1");
        when(userService.get("current-1")).thenReturn(Optional.of(current));

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> service.resolveLinkTarget("google", "google-sub-1", Optional.of(other), "other@example.com", true));

        assertEquals(AccountLinkService.ACCOUNT_LINK_CONFLICT, ex.getError().getErrorCode());

    }

    @Test
    public void testAuthenticatedWithIntentEmailBelongsToAnotherAccount_conflict() {

        when(currentUserResolver.currentUserId()).thenReturn(Optional.of("current-1"));
        when(linkIntentStore.consumeIntent("google")).thenReturn(true);
        User current = user("current-1");
        User emailOwner = user("other-2");
        when(userService.get("current-1")).thenReturn(Optional.of(current));
        when(userService.getByEmail("taken@example.com")).thenReturn(Optional.of(emailOwner));

        OAuth2AuthenticationException ex = assertThrows(OAuth2AuthenticationException.class,
                () -> service.resolveLinkTarget("google", "google-sub-1", Optional.empty(), "taken@example.com", true));

        assertEquals(AccountLinkService.ACCOUNT_LINK_CONFLICT, ex.getError().getErrorCode());

    }

    @Test
    public void testAuthenticatedWithIntentEmailUnprovenNotChecked() {

        when(currentUserResolver.currentUserId()).thenReturn(Optional.of("current-1"));
        when(linkIntentStore.consumeIntent("facebook")).thenReturn(true);
        User current = user("current-1");
        when(userService.get("current-1")).thenReturn(Optional.of(current));

        // emailProven = false : aucune verification d'appartenance de l'email n'est effectuee
        Optional<User> target = service.resolveLinkTarget("facebook", "facebook-id-1", Optional.empty(), "unproven@example.com", false);

        assertTrue(target.isPresent());
        verify(userService, never()).getByEmail(anyString());

    }

    @Test
    public void testApplyProviderEmailSkippedWhenTaken() {

        User target = user("current-1");
        when(userService.isEmailAvailable("taken@example.com", "current-1")).thenReturn(false);

        service.applyProviderEmail(target, "taken@example.com", true);

        assertNull(target.getEmail());

    }

    @Test
    public void testApplyProviderEmailSetsVerifiedWhenProvenAndAvailable() {

        User target = user("current-1");
        when(userService.isEmailAvailable("free@example.com", "current-1")).thenReturn(true);

        service.applyProviderEmail(target, "free@example.com", true);

        assertEquals("free@example.com", target.getEmail());
        assertTrue(target.isEmailVerified());

    }

    @Test
    public void testApplyProviderEmailNeverOverwritesVerifiedAddressWithUnprovenOne() {

        User target = user("current-1");
        target.setVerifiedEmail("proven@example.com");

        service.applyProviderEmail(target, "unproven@example.com", false);

        assertEquals("proven@example.com", target.getEmail());
        assertTrue(target.isEmailVerified());
        verify(userService, never()).isEmailAvailable(anyString(), anyString());

    }

    @Test
    public void testApplyProviderEmailSetsUnverifiedWhenNotProven() {

        User target = user("current-1");
        when(userService.isEmailAvailable("free@example.com", "current-1")).thenReturn(true);

        service.applyProviderEmail(target, "free@example.com", false);

        assertEquals("free@example.com", target.getEmail());
        assertFalse(target.isEmailVerified());

    }

    // --- etat de fusion en attente, porte par la session ---

    /**
     * Le conflit depose en session l'identifiant du compte adverse, le fournisseur et le sujet
     * prouve : sans ces trois valeurs, l'ecran de fusion ne pourrait pas realiser la liaison.
     */
    @Test
    public void testConflictStoresThePendingMergeInTheSession() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {

            when(currentUserResolver.currentUserId()).thenReturn(Optional.of("user-1"));
            when(linkIntentStore.consumeIntent("google")).thenReturn(true);
            when(userService.get("user-1")).thenReturn(Optional.of(user("user-1")));

            assertThrows(OAuth2AuthenticationException.class, () -> service.resolveLinkTarget(
                    "google", "google-sub-1", Optional.of(user("user-2")), null, false));

            HttpSession session = request.getSession();
            assertEquals(Optional.of("user-2"), service.getPendingMergeUserId(session));
            assertEquals(Optional.of("google"), service.getPendingMergeProvider(session));
            assertEquals(Optional.of("google-sub-1"), service.getPendingMergeSubject(session));

            service.clearPendingMerge(session);

            assertTrue(service.getPendingMergeUserId(session).isEmpty());
            assertTrue(service.getPendingMergeProvider(session).isEmpty());
            assertTrue(service.getPendingMergeSubject(session).isEmpty());

        } finally {
            RequestContextHolder.resetRequestAttributes();
        }

    }

    @Test
    public void testPendingMergeIsEmptyWithoutSession() {

        assertTrue(service.getPendingMergeUserId(null).isEmpty());
        assertTrue(service.getPendingMergeProvider(null).isEmpty());
        assertTrue(service.getPendingMergeSubject(null).isEmpty());
        assertDoesNotThrow(() -> service.clearPendingMerge(null));

    }

    @Test
    public void testPendingMergeIsEmptyWhenNothingWasStored() {
        assertTrue(service.getPendingMergeUserId(new MockHttpServletRequest().getSession()).isEmpty());
    }

    /**
     * Le conflit doit rester levable hors contexte de requete (aucun NPE), meme si l'etat ne peut
     * alors pas etre depose en session.
     */
    @Test
    public void testConflictWithoutRequestContextStillFails() {

        when(currentUserResolver.currentUserId()).thenReturn(Optional.of("user-1"));
        when(linkIntentStore.consumeIntent("google")).thenReturn(true);
        when(userService.get("user-1")).thenReturn(Optional.of(user("user-1")));

        assertThrows(OAuth2AuthenticationException.class, () -> service.resolveLinkTarget(
                "google", "google-sub-1", Optional.of(user("user-2")), null, false));

    }

    /**
     * Session pointant vers un compte disparu : comportement de connexion normal, jamais de
     * liaison sur un compte inexistant.
     */
    @Test
    public void testUnknownCurrentUserFallsBackToNormalLogin() {

        when(currentUserResolver.currentUserId()).thenReturn(Optional.of("gone"));
        when(linkIntentStore.consumeIntent("google")).thenReturn(true);
        when(userService.get("gone")).thenReturn(Optional.empty());

        assertTrue(service.resolveLinkTarget("google", "sub", Optional.empty(), "x@example.com", true).isEmpty());

    }

    @Test
    public void testApplyProviderEmailIgnoresBlankAddress() {

        User user = user("user-1");
        user.setVerifiedEmail("jean@example.com");

        service.applyProviderEmail(user, "  ", true);

        assertEquals("jean@example.com", user.getEmail());
        verify(userService, never()).isEmailAvailable(anyString(), anyString());

    }

}
