package info.tomacla.biketeam.domain.user;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserPasskeyTest {

    /**
     * "Synchronisee" suppose les deux drapeaux : eligible a la sauvegarde ET effectivement
     * sauvegardee. Une cle de securite USB est eligible a rien, un telephone peut etre eligible
     * sans que le trousseau soit actif.
     */
    @Test
    public void testSyncedAcrossDevicesRequiresBothFlags() {

        UserPasskey passkey = new UserPasskey();
        assertFalse(passkey.isSyncedAcrossDevices());

        passkey.setBackupEligible(true);
        assertFalse(passkey.isSyncedAcrossDevices());

        passkey.setBackedUp(true);
        assertTrue(passkey.isSyncedAcrossDevices());

    }

    @Test
    public void testUtcAccessorsAreNullSafe() {
        UserPasskey passkey = new UserPasskey();
        assertNull(passkey.getCreatedAtUtc());
        assertNull(passkey.getLastUsedAtUtc());
    }

    @Test
    public void testUtcAccessorsExposeFormatableTemporal() {

        UserPasskey passkey = new UserPasskey();
        Instant createdAt = Instant.parse("2026-09-15T10:15:30Z");
        passkey.setCreatedAt(createdAt);

        assertEquals(ZoneOffset.UTC, passkey.getCreatedAtUtc().getZone());
        assertEquals(createdAt, passkey.getCreatedAtUtc().toInstant());

    }

}
