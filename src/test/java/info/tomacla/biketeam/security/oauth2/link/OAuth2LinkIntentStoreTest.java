package info.tomacla.biketeam.security.oauth2.link;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Intention explicite de liaison : garde-fou contre la liaison silencieuse d'un compte externe
 * lorsqu'une session est deja ouverte.
 */
public class OAuth2LinkIntentStoreTest {

    private OAuth2LinkIntentStore store;
    private MockHttpServletRequest request;

    @BeforeEach
    public void setUp() {
        store = new OAuth2LinkIntentStore();
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    public void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void testStoredIntentIsConsumedOnce() {

        store.storeIntent("google");

        assertTrue(store.consumeIntent("google"));
        assertFalse(store.consumeIntent("google"), "une intention n'est jamais rejouable");

    }

    @Test
    public void testNoIntentMeansNoLink() {
        assertFalse(store.consumeIntent("google"));
    }

    /**
     * Une intention posee pour un fournisseur ne vaut jamais pour un autre, et elle est effacee
     * dans tous les cas.
     */
    @Test
    public void testIntentOfAnotherProviderIsRefusedAndCleared() {

        store.storeIntent("google");

        assertFalse(store.consumeIntent("facebook"));
        assertFalse(store.consumeIntent("google"), "l'intention est effacee meme en cas de refus");

    }

    @Test
    public void testExpiredIntentIsRefused() {

        store.storeIntent("google");
        request.getSession().setAttribute(OAuth2LinkIntentStore.INTENT_EXPIRES,
                Instant.now().minus(1, ChronoUnit.MINUTES));

        assertFalse(store.consumeIntent("google"));

    }

    @Test
    public void testNothingHappensWithoutRequestContext() {

        RequestContextHolder.resetRequestAttributes();

        assertDoesNotThrow(() -> store.storeIntent("google"));
        assertFalse(store.consumeIntent("google"));

    }

    @Test
    public void testIntentIsStoredInTheSession() {

        store.storeIntent("facebook");

        assertEquals("facebook", request.getSession().getAttribute(OAuth2LinkIntentStore.INTENT));
        assertInstanceOf(Instant.class, request.getSession().getAttribute(OAuth2LinkIntentStore.INTENT_EXPIRES));

        // l'attribut est bien porte par la session, pas par la requete
        assertNull(RequestContextHolder.getRequestAttributes()
                .getAttribute(OAuth2LinkIntentStore.INTENT, RequestAttributes.SCOPE_REQUEST));

    }

}
