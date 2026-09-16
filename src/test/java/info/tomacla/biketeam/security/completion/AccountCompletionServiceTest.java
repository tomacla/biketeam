package info.tomacla.biketeam.security.completion;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.Authorities;
import info.tomacla.biketeam.security.OAuth2UserDetails;
import info.tomacla.biketeam.service.UserService;
import info.tomacla.biketeam.service.mail.MailSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Ordre de resolution de l'etat de completion : cache de session positif, attribut du principal,
 * cache de session negatif, base.
 * <p>
 * Le scenario de regression verrouille ici est la boucle de redirection infinie : un principal
 * fige disant "incomplet" alors que le compte a ete complete depuis un autre navigateur. Sans le
 * TRUE de session pose par markComplete, le filtre renverrait indefiniment vers /account/complete.
 */
public class AccountCompletionServiceTest {

    private UserService userService;
    private MailSenderService mailSenderService;
    private AccountCompletionService service;

    @BeforeEach
    public void setUp() {

        userService = mock(UserService.class);
        mailSenderService = mock(MailSenderService.class);

        service = new AccountCompletionService();
        ReflectionTestUtils.setField(service, "userService", userService);
        ReflectionTestUtils.setField(service, "mailSenderService", mailSenderService);
        ReflectionTestUtils.setField(service, "mode", AccountCompletionMode.ENFORCED);

        when(mailSenderService.isSmtpConfigured()).thenReturn(true);

    }

    private Authentication authentication(String userId, Boolean accountComplete) {

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", userId);
        if (accountComplete != null) {
            attributes.put("accountComplete", accountComplete);
        }

        OAuth2UserDetails details = new OAuth2UserDetails();
        details.setAttributes(attributes);
        details.setAuthorities(List.of(Authorities.user()));

        return UsernamePasswordAuthenticationToken.authenticated(details, null, details.getAuthorities());

    }

    private User user(String id, boolean complete) {
        User user = new User();
        user.setId(id);
        if (complete) {
            user.setPasswordHash("$2a$10$hash");
            user.setVerifiedEmail("user@example.com");
        }
        return user;
    }

    // --- enforcementEnabled ---

    @Test
    public void testEnforcementEnabledRequiresSmtp() {

        assertTrue(service.enforcementEnabled());

        when(mailSenderService.isSmtpConfigured()).thenReturn(false);
        assertFalse(service.enforcementEnabled(), "sans SMTP l'utilisateur serait enferme dans la boucle");

    }

    @Test
    public void testEnforcementDisabledByProperty() {

        ReflectionTestUtils.setField(service, "mode", AccountCompletionMode.SUGGESTED);
        assertFalse(service.enforcementEnabled());

        ReflectionTestUtils.setField(service, "mode", AccountCompletionMode.OFF);
        assertFalse(service.enforcementEnabled());

    }

    @Test
    public void testInitDoesNotFailWhenSmtpMissing() {

        when(mailSenderService.isSmtpConfigured()).thenReturn(false);
        ReflectionTestUtils.setField(service, "configuredMode", "ENFORCED");

        service.init();

        assertFalse(service.enforcementEnabled());

    }

    // --- suggestionEnabled ---

    /**
     * L'incitation ne depend pas du SMTP : la page de completion permet aussi de lier Google ou
     * Facebook, l'utilisateur sollicite a donc toujours un chemin de sortie.
     */
    @Test
    public void testSuggestionDoesNotRequireSmtp() {

        when(mailSenderService.isSmtpConfigured()).thenReturn(false);

        ReflectionTestUtils.setField(service, "mode", AccountCompletionMode.SUGGESTED);
        assertTrue(service.suggestionEnabled());

        ReflectionTestUtils.setField(service, "mode", AccountCompletionMode.ENFORCED);
        assertTrue(service.suggestionEnabled());

    }

    @Test
    public void testSuggestionDisabledWhenOff() {

        ReflectionTestUtils.setField(service, "mode", AccountCompletionMode.OFF);

        assertFalse(service.suggestionEnabled());
        assertFalse(service.enforcementEnabled());

    }

    // --- resolution de la propriete ---

    @Test
    public void testModeParsingIsLenient() {

        ReflectionTestUtils.setField(service, "configuredMode", " enforced ");
        service.init();
        assertEquals(AccountCompletionMode.ENFORCED, service.getMode());

        ReflectionTestUtils.setField(service, "configuredMode", "off");
        service.init();
        assertEquals(AccountCompletionMode.OFF, service.getMode());

    }

    /**
     * Une valeur de configuration erronee ne doit pas empecher le demarrage.
     */
    @Test
    public void testUnknownModeFallsBackToSuggested() {

        ReflectionTestUtils.setField(service, "configuredMode", "n_importe_quoi");
        service.init();
        assertEquals(AccountCompletionMode.SUGGESTED, service.getMode());

        ReflectionTestUtils.setField(service, "configuredMode", "");
        service.init();
        assertEquals(AccountCompletionMode.SUGGESTED, service.getMode());

        ReflectionTestUtils.setField(service, "configuredMode", null);
        service.init();
        assertEquals(AccountCompletionMode.SUGGESTED, service.getMode());

    }

    // --- principal non applicable ---

    @Test
    public void testNullAuthenticationIsConsideredComplete() {
        assertTrue(service.isComplete(null, new MockHttpServletRequest()));
    }

    @Test
    public void testForeignPrincipalIsConsideredComplete() {

        Authentication other = UsernamePasswordAuthenticationToken.authenticated(
                "some-string-principal", null, List.of(Authorities.user()));

        assertTrue(service.isComplete(other, new MockHttpServletRequest()));
        verify(userService, never()).get(any());

    }

    // --- 1. cache de session positif ---

    @Test
    public void testPositiveSessionCacheWinsOverIncompletePrincipal() {

        // scenario de regression : le principal a ete fige a la connexion et dit "incomplet",
        // le compte a depuis ete complete depuis un autre navigateur.
        Authentication authentication = authentication("user-1", false);

        MockHttpServletRequest request = new MockHttpServletRequest();

        assertFalse(service.isComplete(authentication, request), "principal fige : encore incomplet");

        // sortie de secours de la boucle de redirection
        service.markComplete(request);

        assertTrue(service.isComplete(authentication, request));
        verify(userService, never()).get(any());

    }

    @Test
    public void testPositiveSessionCacheWinsOverDatabase() {

        Authentication authentication = authentication("user-1", null);
        MockHttpServletRequest request = new MockHttpServletRequest();
        service.markComplete(request);

        assertTrue(service.isComplete(authentication, request));
        verify(userService, never()).get(any());

    }

    // --- 2. attribut du principal ---

    @Test
    public void testPrincipalAttributeUsedWhenNoSessionCache() {

        assertTrue(service.isComplete(authentication("user-1", true), new MockHttpServletRequest()));
        assertFalse(service.isComplete(authentication("user-2", false), new MockHttpServletRequest()));

        verify(userService, never()).get(any());

    }

    // --- 3. cache de session negatif ---

    @Test
    public void testNegativeSessionCacheUsedWhenPrincipalHasNoAttribute() {

        // principal issu d'une session serialisee avant le deploiement : aucun attribut
        Authentication authentication = authentication("user-1", null);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(AccountCompletionService.SESSION_ATTR, Boolean.FALSE);

        assertFalse(service.isComplete(authentication, request));
        verify(userService, never()).get(any());

    }

    @Test
    public void testPrincipalAttributeWinsOverNegativeSessionCache() {

        Authentication authentication = authentication("user-1", true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(AccountCompletionService.SESSION_ATTR, Boolean.FALSE);

        assertTrue(service.isComplete(authentication, request));

    }

    // --- 4. relecture en base ---

    @Test
    public void testDatabaseReadAndCachedInSession() {

        when(userService.get("user-1")).thenReturn(Optional.of(user("user-1", true)));

        Authentication authentication = authentication("user-1", null);
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertTrue(service.isComplete(authentication, request));
        assertEquals(Boolean.TRUE, request.getSession(false).getAttribute(AccountCompletionService.SESSION_ATTR));

        // la valeur mise en cache evite toute relecture
        assertTrue(service.isComplete(authentication, request));
        verify(userService, times(1)).get("user-1");

    }

    @Test
    public void testDatabaseReadOfIncompleteAccountIsCachedAsFalse() {

        when(userService.get("user-1")).thenReturn(Optional.of(user("user-1", false)));

        Authentication authentication = authentication("user-1", null);
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertFalse(service.isComplete(authentication, request));
        assertEquals(Boolean.FALSE, request.getSession(false).getAttribute(AccountCompletionService.SESSION_ATTR));

    }

    @Test
    public void testUnknownUserIsIncomplete() {

        when(userService.get("user-1")).thenReturn(Optional.empty());

        assertFalse(service.isComplete(authentication("user-1", null), new MockHttpServletRequest()));

    }

    @Test
    public void testDatabaseReadWithoutRequestDoesNotFail() {

        when(userService.get("user-1")).thenReturn(Optional.of(user("user-1", true)));

        assertTrue(service.isComplete(authentication("user-1", null), null));

    }

    // --- markComplete / invalidate ---

    @Test
    public void testMarkCompleteCreatesSessionWhenAbsent() {

        MockHttpServletRequest request = new MockHttpServletRequest();

        service.markComplete(request);

        assertNotNull(request.getSession(false));
        assertEquals(Boolean.TRUE, request.getSession(false).getAttribute(AccountCompletionService.SESSION_ATTR));

    }

    @Test
    public void testMarkCompleteWithoutRequestDoesNotFail() {
        assertDoesNotThrow(() -> service.markComplete(null));
    }

    @Test
    public void testInvalidateRemovesCachedValue() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        service.markComplete(request);

        service.invalidate(request);

        assertNull(request.getSession(false).getAttribute(AccountCompletionService.SESSION_ATTR));

    }

    @Test
    public void testInvalidateNeverCreatesSession() {

        MockHttpServletRequest request = new MockHttpServletRequest();

        service.invalidate(request);

        assertNull(request.getSession(false));
        assertDoesNotThrow(() -> service.invalidate(null));

    }

    /**
     * Apres invalidation, la base redevient la source de verite : c'est le chemin emprunte par la
     * confirmation d'adresse et par la fusion de comptes.
     */
    @Test
    public void testInvalidateForcesDatabaseReadAgain() {

        when(userService.get("user-1")).thenReturn(Optional.of(user("user-1", false)));

        Authentication authentication = authentication("user-1", null);
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertFalse(service.isComplete(authentication, request));

        service.invalidate(request);
        when(userService.get("user-1")).thenReturn(Optional.of(user("user-1", true)));

        assertTrue(service.isComplete(authentication, request));
        verify(userService, times(2)).get("user-1");

    }

}
