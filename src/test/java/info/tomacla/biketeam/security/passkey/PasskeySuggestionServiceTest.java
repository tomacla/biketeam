package info.tomacla.biketeam.security.passkey;

import info.tomacla.biketeam.domain.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PasskeySuggestionServiceTest {

    private PasskeyService passkeyService;
    private PasskeySuggestionService service;
    private User user;

    @BeforeEach
    public void setUp() {

        passkeyService = mock(PasskeyService.class);
        service = new PasskeySuggestionService();
        ReflectionTestUtils.setField(service, "passkeyService", passkeyService);

        user = new User();
        user.setId("user-1");

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));

    }

    @AfterEach
    public void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void testSuggestionNeededWithoutPasskey() {
        when(passkeyService.countByUser("user-1")).thenReturn(0L);
        assertTrue(service.suggestionNeeded(user));
    }

    @Test
    public void testNoSuggestionWithPasskey() {
        when(passkeyService.countByUser("user-1")).thenReturn(2L);
        assertFalse(service.suggestionNeeded(user));
    }

    @Test
    public void testResultIsCachedInSession() {

        when(passkeyService.countByUser("user-1")).thenReturn(0L);

        service.suggestionNeeded(user);
        service.suggestionNeeded(user);
        service.suggestionNeeded(user);

        verify(passkeyService, times(1)).countByUser("user-1");

    }

    @Test
    public void testInvalidateForcesANewCount() {

        when(passkeyService.countByUser("user-1")).thenReturn(0L);
        service.suggestionNeeded(user);

        service.invalidate();
        when(passkeyService.countByUser("user-1")).thenReturn(1L);

        assertFalse(service.suggestionNeeded(user));
        verify(passkeyService, times(2)).countByUser("user-1");

    }

    /**
     * Hors requete (tache planifiee, rendu de mail) : aucune suggestion, et surtout aucune
     * requete en base pour une page que personne n'affiche.
     */
    @Test
    public void testNoSuggestionOutsideOfARequest() {

        RequestContextHolder.resetRequestAttributes();

        assertFalse(service.suggestionNeeded(user));
        verify(passkeyService, times(0)).countByUser("user-1");

    }

    @Test
    public void testNoSuggestionWithoutUser() {
        assertFalse(service.suggestionNeeded(null));
    }

}
