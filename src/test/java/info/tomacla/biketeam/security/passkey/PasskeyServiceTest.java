package info.tomacla.biketeam.security.passkey;

import com.webauthn4j.data.AuthenticatorTransport;
import info.tomacla.biketeam.domain.user.UserPasskey;
import info.tomacla.biketeam.domain.user.UserPasskeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PasskeyServiceTest {

    private UserPasskeyRepository repository;
    private PasskeyService service;

    @BeforeEach
    public void setUp() {
        repository = mock(UserPasskeyRepository.class);
        service = new PasskeyService();
        ReflectionTestUtils.setField(service, "userPasskeyRepository", repository);
    }

    @Test
    public void testDeleteIsRestrictedToOwner() {

        when(repository.findByIdAndUserId("pk-1", "user-2")).thenReturn(Optional.empty());

        assertFalse(service.delete("pk-1", "user-2"));
        verify(repository, never()).delete(any());

    }

    @Test
    public void testDeleteRemovesOwnedPasskey() {

        UserPasskey passkey = new UserPasskey();
        when(repository.findByIdAndUserId("pk-1", "user-1")).thenReturn(Optional.of(passkey));

        assertTrue(service.delete("pk-1", "user-1"));
        verify(repository).delete(passkey);

    }

    /**
     * Le compteur ne recule jamais et la date d'usage est posee : c'est ce qui permet a webauthn4j
     * de detecter un authentificateur clone a l'assertion suivante.
     */
    @Test
    public void testRecordUsageUpdatesCounterAndTimestamp() {

        UserPasskey passkey = new UserPasskey();
        passkey.setSignCount(4);
        when(repository.findById("pk-1")).thenReturn(Optional.of(passkey));

        service.recordUsage("pk-1", 9, true, true);

        assertEquals(9, passkey.getSignCount());
        assertTrue(passkey.isUvInitialized());
        assertTrue(passkey.isBackedUp());
        assertTrue(passkey.getLastUsedAt() != null);
        verify(repository).save(passkey);

    }

    /**
     * uvInitialized ne retombe jamais a false : une assertion sans verification d'utilisateur ne
     * doit pas effacer le fait que la passkey en est capable.
     */
    @Test
    public void testRecordUsageNeverClearsUvInitialized() {

        UserPasskey passkey = new UserPasskey();
        passkey.setUvInitialized(true);
        when(repository.findById("pk-1")).thenReturn(Optional.of(passkey));

        service.recordUsage("pk-1", 1, false, false);

        assertTrue(passkey.isUvInitialized());

    }

    @Test
    public void testTransportsRoundTrip() {

        Set<AuthenticatorTransport> transports = Set.of(AuthenticatorTransport.INTERNAL);
        String csv = PasskeyService.fromTransports(transports);

        assertEquals("internal", csv);
        assertEquals(transports, PasskeyService.toTransports(csv));

    }

    @Test
    public void testTransportsEmptyIsNull() {
        assertNull(PasskeyService.fromTransports(Set.of()));
        assertNull(PasskeyService.fromTransports(null));
        assertTrue(PasskeyService.toTransports(null).isEmpty());
        assertTrue(PasskeyService.toTransports("  ").isEmpty());
    }

    @Test
    public void testSanitizeLabelFallsBackToDefault() {
        assertEquals("Passkey", PasskeyService.sanitizeLabel(null));
        assertEquals("Passkey", PasskeyService.sanitizeLabel("   "));
        assertEquals("Passkey", PasskeyService.sanitizeLabel("\n\t"));
    }

    @Test
    public void testSanitizeLabelStripsControlCharactersAndTruncates() {
        assertEquals("mon telephone", PasskeyService.sanitizeLabel("mon\ntelephone"));
        assertEquals(80, PasskeyService.sanitizeLabel("x".repeat(200)).length());
    }

}
