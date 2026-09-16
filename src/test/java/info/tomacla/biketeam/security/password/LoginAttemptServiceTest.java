package info.tomacla.biketeam.security.password;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.LockedException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginAttemptServiceTest {

    @Test
    public void testNotLockedBelowThreshold() {

        LoginAttemptService service = new LoginAttemptService(3, 15);

        service.recordFailure("user@example.com");
        service.recordFailure("user@example.com");

        assertFalse(service.isLocked("user@example.com"));
        assertDoesNotThrow(() -> service.assertNotLocked("user@example.com"));

    }

    @Test
    public void testLockedAtThreshold() {

        LoginAttemptService service = new LoginAttemptService(3, 15);

        service.recordFailure("user@example.com");
        service.recordFailure("user@example.com");
        service.recordFailure("user@example.com");

        assertTrue(service.isLocked("user@example.com"));
        assertThrows(LockedException.class, () -> service.assertNotLocked("user@example.com"));

    }

    @Test
    public void testEmailNormalizedForCounting() {

        LoginAttemptService service = new LoginAttemptService(2, 15);

        service.recordFailure("User@Example.com");
        service.recordFailure(" user@example.com ");

        assertTrue(service.isLocked("USER@EXAMPLE.COM"));

    }

    @Test
    public void testSuccessResetsCounter() {

        LoginAttemptService service = new LoginAttemptService(2, 15);

        service.recordFailure("user@example.com");
        service.recordFailure("user@example.com");
        assertTrue(service.isLocked("user@example.com"));

        service.recordSuccess("user@example.com");
        assertFalse(service.isLocked("user@example.com"));

    }

    @Test
    public void testDistinctEmailsAreIndependent() {

        LoginAttemptService service = new LoginAttemptService(1, 15);

        service.recordFailure("first@example.com");

        assertTrue(service.isLocked("first@example.com"));
        assertFalse(service.isLocked("second@example.com"));

    }

    @Test
    public void testLockIsScopedToTheFailingOrigin() {

        LoginAttemptService service = new LoginAttemptService(2, 15);

        service.recordFailure("user@example.com", "1.1.1.1");
        service.recordFailure("user@example.com", "1.1.1.1");

        // l'origine fautive est verrouillee...
        assertTrue(service.isLocked("user@example.com", "1.1.1.1"));
        // ...mais le titulaire legitime, depuis une autre origine, n'est jamais exclu
        assertFalse(service.isLocked("user@example.com", "2.2.2.2"));

    }

    @Test
    public void testDistributedAttackLocksTheAddressGlobally() {

        LoginAttemptService service = new LoginAttemptService(1, 15);

        for (int i = 0; i < LoginAttemptService.DISTRIBUTED_ORIGINS_THRESHOLD; i++) {
            service.recordFailure("user@example.com", "10.0.0." + i);
        }

        // seuil atteint depuis assez d'origines distinctes : le verrou devient global
        assertTrue(service.isLocked("user@example.com", "192.168.0.1"));

    }

    @Test
    public void testSuccessClearsEveryOrigin() {

        LoginAttemptService service = new LoginAttemptService(1, 15);

        service.recordFailure("user@example.com", "1.1.1.1");
        assertTrue(service.isLocked("user@example.com", "1.1.1.1"));

        service.recordSuccess("user@example.com", "2.2.2.2");

        assertFalse(service.isLocked("user@example.com", "1.1.1.1"));

    }

    @Test
    public void testSlidingWindowExpiry() throws Exception {

        // fenetre glissante negative (-1 minute) : la purge considere immediatement toute
        // tentative comme perimee, simulant l'expiration sans dependre de vrais delais d'attente
        LoginAttemptService service = new LoginAttemptService(1, -1);

        service.recordFailure("user@example.com");

        // la purge a lieu a la prochaine lecture/ecriture : la tentative expiree ne compte plus
        assertFalse(service.isLocked("user@example.com"));

    }

}
