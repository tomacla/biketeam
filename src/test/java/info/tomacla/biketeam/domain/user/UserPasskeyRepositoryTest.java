package info.tomacla.biketeam.domain.user;

import info.tomacla.biketeam.domain.AbstractDBTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifie le schema reel : type binaire de la cle publique, unicite du credential id et
 * recherches utilisees a la connexion. Ces points ne sont pas couvrables par un mock, et ce sont
 * exactement ceux qu'une erreur de changeset Liquibase casserait silencieusement.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class UserPasskeyRepositoryTest extends AbstractDBTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPasskeyRepository userPasskeyRepository;

    private User user(String email) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName("foo");
        user.setLastName("bar");
        return userRepository.save(user);
    }

    private UserPasskey passkey(String userId, String credentialId, byte[] publicKey) {
        UserPasskey passkey = new UserPasskey();
        passkey.setUserId(userId);
        passkey.setCredentialId(credentialId);
        passkey.setAttestedCredentialData(publicKey);
        passkey.setLabel("Mon telephone");
        passkey.setCreatedAt(Instant.now());
        return userPasskeyRepository.save(passkey);
    }

    @Test
    public void testPublicKeyIsStoredAsBinaryAndReadBackIntact() {

        final User owner = user(UUID.randomUUID() + "@example.com");
        final byte[] publicKey = new byte[]{0, 1, 2, (byte) 0xFF, (byte) 0x80, 0};
        final String credentialId = UUID.randomUUID().toString();

        passkey(owner.getId(), credentialId, publicKey);

        final UserPasskey reloaded = userPasskeyRepository.findByCredentialId(credentialId).orElseThrow();

        assertArrayEquals(publicKey, reloaded.getAttestedCredentialData());
        assertEquals(owner.getId(), reloaded.getUserId());
        assertEquals(0, reloaded.getSignCount());

    }

    /**
     * L'identifiant de credential est la SEULE cle de recherche a la connexion : un doublon
     * rendrait la resolution du compte ambigue.
     */
    @Test
    public void testCredentialIdIsUniqueAcrossAllAccounts() {

        final User first = user(UUID.randomUUID() + "@example.com");
        final User second = user(UUID.randomUUID() + "@example.com");
        final String credentialId = UUID.randomUUID().toString();

        passkey(first.getId(), credentialId, new byte[]{1});

        assertThrows(DataIntegrityViolationException.class,
                () -> passkey(second.getId(), credentialId, new byte[]{2}));

    }

    @Test
    public void testFindAndCountByUser() {

        final User owner = user(UUID.randomUUID() + "@example.com");

        passkey(owner.getId(), UUID.randomUUID().toString(), new byte[]{1});
        passkey(owner.getId(), UUID.randomUUID().toString(), new byte[]{2});

        assertEquals(2, userPasskeyRepository.countByUserId(owner.getId()));
        assertEquals(2, userPasskeyRepository.findByUserIdOrderByCreatedAtAsc(owner.getId()).size());

    }

    /**
     * findByIdAndUserId est la garde de la suppression : l'identifiant vient de l'URL, il ne doit
     * jamais suffire a atteindre la passkey d'un autre compte.
     */
    @Test
    public void testFindByIdAndUserIdIsScopedToTheOwner() {

        final User owner = user(UUID.randomUUID() + "@example.com");
        final User other = user(UUID.randomUUID() + "@example.com");

        final UserPasskey passkey = passkey(owner.getId(), UUID.randomUUID().toString(), new byte[]{1});

        assertTrue(userPasskeyRepository.findByIdAndUserId(passkey.getId(), owner.getId()).isPresent());
        assertTrue(userPasskeyRepository.findByIdAndUserId(passkey.getId(), other.getId()).isEmpty());

    }

    @Test
    @Transactional
    public void testMoveToUserReattachesEveryPasskey() {

        final User source = user(UUID.randomUUID() + "@example.com");
        final User target = user(UUID.randomUUID() + "@example.com");

        final String credentialId = UUID.randomUUID().toString();
        passkey(source.getId(), credentialId, new byte[]{1});

        assertEquals(1, userPasskeyRepository.moveToUser(source.getId(), target.getId()));

        assertEquals(target.getId(),
                userPasskeyRepository.findByCredentialId(credentialId).orElseThrow().getUserId());

    }

    @Test
    @Transactional
    public void testDeleteByUserIdRemovesEveryPasskey() {

        final User owner = user(UUID.randomUUID() + "@example.com");
        passkey(owner.getId(), UUID.randomUUID().toString(), new byte[]{1});
        passkey(owner.getId(), UUID.randomUUID().toString(), new byte[]{2});

        userPasskeyRepository.deleteByUserId(owner.getId());

        assertEquals(0, userPasskeyRepository.countByUserId(owner.getId()));

    }

}
