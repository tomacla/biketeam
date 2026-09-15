package info.tomacla.biketeam.security.passkey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PasskeyChallengeStoreTest {

    private PasskeyChallengeStore store;

    @BeforeEach
    public void setUp() {
        PasskeyProperties properties = mock(PasskeyProperties.class);
        when(properties.getChallengeValidity()).thenReturn(Duration.ofMinutes(5));
        store = new PasskeyChallengeStore();
        ReflectionTestUtils.setField(store, "passkeyProperties", properties);
    }

    @Test
    public void testRegistrationChallengeIsStoredAndBoundToUser() {

        MockHttpServletRequest request = new MockHttpServletRequest();

        PasskeyChallenge created = store.newRegistrationChallenge(request, "user-1");

        Optional<PasskeyChallenge> consumed = store.consumeRegistrationChallenge(request);

        assertTrue(consumed.isPresent());
        assertArrayEquals(created.getValue(), consumed.get().getValue());
        assertEquals("user-1", consumed.get().getUserId());

    }

    /**
     * Le rejeu est la seule attaque que le challenge protege : il doit disparaitre au premier
     * usage, reussi ou non.
     */
    @Test
    public void testChallengeIsConsumedOnlyOnce() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        store.newAuthenticationChallenge(request);

        assertTrue(store.consumeAuthenticationChallenge(request).isPresent());
        assertTrue(store.consumeAuthenticationChallenge(request).isEmpty());

    }

    @Test
    public void testRegistrationAndAuthenticationChallengesAreIndependent() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        store.newRegistrationChallenge(request, "user-1");

        assertTrue(store.consumeAuthenticationChallenge(request).isEmpty());
        assertTrue(store.consumeRegistrationChallenge(request).isPresent());

    }

    @Test
    public void testExpiredChallengeIsRejected() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        store.newAuthenticationChallenge(request);

        // on remplace l'entree par un challenge deja perime
        request.getSession().setAttribute(PasskeyChallengeStore.AUTHENTICATION_ATTR,
                new PasskeyChallenge(new byte[]{1, 2, 3}, Instant.now().minusSeconds(1), null));

        assertTrue(store.consumeAuthenticationChallenge(request).isEmpty());

    }

    /**
     * Aucune session ne doit etre creee a la consommation : une reponse forgee ne doit pas
     * suffire a faire naitre une session cote serveur.
     */
    @Test
    public void testConsumeWithoutSessionCreatesNone() {

        MockHttpServletRequest request = new MockHttpServletRequest();

        assertTrue(store.consumeAuthenticationChallenge(request).isEmpty());
        assertEquals(null, request.getSession(false));

    }

    @Test
    public void testChallengesAreNotPredictable() {

        MockHttpServletRequest first = new MockHttpServletRequest();
        MockHttpServletRequest second = new MockHttpServletRequest();

        PasskeyChallenge a = store.newAuthenticationChallenge(first);
        PasskeyChallenge b = store.newAuthenticationChallenge(second);

        assertNotNull(a.getValue());
        assertEquals(32, a.getValue().length);
        assertFalse(java.util.Arrays.equals(a.getValue(), b.getValue()));

    }

}
