package info.tomacla.biketeam.service.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Limitation de debit des routes non authentifiees declenchant un envoi de mail.
 */
public class RateLimitServiceTest {

    private RateLimitService service;

    @BeforeEach
    public void setUp() {
        service = new RateLimitService();
    }

    @Test
    public void testQuotaIsEnforcedOnTheSameKey() {

        assertTrue(service.tryAcquire("register:1.2.3.4", 3));
        assertTrue(service.tryAcquire("register:1.2.3.4", 3));
        assertTrue(service.tryAcquire("register:1.2.3.4", 3));

        assertFalse(service.tryAcquire("register:1.2.3.4", 3));
        assertFalse(service.tryAcquire("register:1.2.3.4", 3));

    }

    @Test
    public void testKeysAreIndependent() {

        assertTrue(service.tryAcquire("register:1.2.3.4", 1));
        assertFalse(service.tryAcquire("register:1.2.3.4", 1));

        assertTrue(service.tryAcquire("register:5.6.7.8", 1));
        assertTrue(service.tryAcquire("forgot-password:1.2.3.4", 1));

    }

    /**
     * Une tentative refusee ne doit pas gonfler le compteur au point d'interdire une eventuelle
     * augmentation de quota : le refus a lieu AVANT l'enregistrement.
     */
    @Test
    public void testRefusedAttemptIsNotCounted() {

        assertTrue(service.tryAcquire("key", 1));
        assertFalse(service.tryAcquire("key", 1));

        // le quota passe a 2 : une seule tentative a ete reellement enregistree
        assertTrue(service.tryAcquire("key", 2));
        assertFalse(service.tryAcquire("key", 2));

    }

    @Test
    public void testNullKeyIsAlwaysRefused() {
        assertFalse(service.tryAcquire(null, 10));
    }

    @Test
    public void testZeroOrNegativeQuotaIsAlwaysRefused() {

        assertFalse(service.tryAcquire("key", 0));
        assertFalse(service.tryAcquire("key", -1));

    }

    /**
     * Au dela de 10 000 cles suivies, une purge est declenchee. Elle ne doit jamais remettre a zero
     * les compteurs encore dans la fenetre : ce serait une porte de sortie triviale (saturer la
     * table de cles pour effacer son propre compteur).
     */
    @Test
    public void testPurgeKeepsLiveCounters() {

        assertTrue(service.tryAcquire("victime", 1));

        for (int i = 0; i < 10_002; i++) {
            service.tryAcquire("bourrage-" + i, 1);
        }

        assertFalse(service.tryAcquire("victime", 1), "le compteur encore dans la fenetre est conserve");

    }

    @Test
    public void testClientKeyUsesRemoteAddress() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.7");

        assertEquals("register:10.0.0.7", service.clientKey(request, "register"));

    }

    @Test
    public void testClientKeyWithoutRequest() {
        assertEquals("register:unknown", service.clientKey(null, "register"));
    }

    /**
     * Deux prefixes differents pour la meme adresse IP ne partagent jamais leur compteur.
     */
    @Test
    public void testClientKeysOfDifferentPrefixesDoNotShareCounters() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.7");

        assertTrue(service.tryAcquire(service.clientKey(request, "register"), 1));
        assertFalse(service.tryAcquire(service.clientKey(request, "register"), 1));
        assertTrue(service.tryAcquire(service.clientKey(request, "forgot-password"), 1));

    }

}
