package info.tomacla.biketeam.service;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserPasskeyRepository;
import info.tomacla.biketeam.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Moyens de connexion portes par UserService : disponibilite de l'adresse, pose du mot de passe et
 * graine de signature des cookies remember-me.
 */
public class UserServiceAuthTest {

    private UserRepository userRepository;
    private UserPasskeyRepository userPasskeyRepository;
    private PasswordEncoder passwordEncoder;
    private UserService service;

    @BeforeEach
    public void setUp() {

        userRepository = mock(UserRepository.class);
        userPasskeyRepository = mock(UserPasskeyRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        service = new UserService();
        ReflectionTestUtils.setField(service, "userRepository", userRepository);
        ReflectionTestUtils.setField(service, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(service, "userPasskeyRepository", userPasskeyRepository);
        ReflectionTestUtils.setField(service, "rotateLegacySeed", true);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findOne(ArgumentMatchers.<Specification<User>>any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenAnswer(invocation -> "encoded:" + invocation.getArgument(0));

    }

    private User user(String id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        return user;
    }

    private void givenEmailOwnedBy(User owner) {
        when(userRepository.findOne(ArgumentMatchers.<Specification<User>>any())).thenReturn(Optional.of(owner));
    }

    // --- isEmailAvailable ---

    @Test
    public void testBlankAddressIsNeverAvailable() {

        assertFalse(service.isEmailAvailable(null, "user-1"));
        assertFalse(service.isEmailAvailable("", "user-1"));
        assertFalse(service.isEmailAvailable("   ", "user-1"));

    }

    @Test
    public void testUnusedAddressIsAvailable() {
        assertTrue(service.isEmailAvailable("libre@example.com", "user-1"));
    }

    @Test
    public void testAddressOfAnotherAccountIsNotAvailable() {

        givenEmailOwnedBy(user("user-2", "jean@example.com"));

        assertFalse(service.isEmailAvailable("jean@example.com", "user-1"));

    }

    /**
     * Sa propre adresse reste "disponible" : reverifier son adresse ne doit pas etre refuse.
     */
    @Test
    public void testOwnAddressIsStillAvailable() {

        givenEmailOwnedBy(user("user-1", "jean@example.com"));

        assertTrue(service.isEmailAvailable("JEAN@Example.com", "user-1"));

    }

    // --- setPassword ---

    @Test
    public void testSetPasswordStoresTheHashAndTheDate() {

        User user = user("user-1", "jean@example.com");

        service.setPassword("user-1", "motdepasse123");

        assertEquals("encoded:motdepasse123", user.getPasswordHash());
        assertNotNull(user.getPasswordUpdatedAt());
        verify(userRepository).save(user);

    }

    @Test
    public void testSetPasswordOnUnknownUserDoesNothing() {

        when(userRepository.findById("gone")).thenReturn(Optional.empty());

        service.setPassword("gone", "motdepasse123");

        verify(userRepository, never()).save(any(User.class));

    }

    // --- graine remember-me ---

    @Test
    public void testSaveAlwaysCarriesASeed() {

        User user = new User();
        user.setId("user-1");

        User saved = service.save(user);

        assertNotNull(saved.getAuthTokenSeed(), "auth_token_seed est NOT NULL en base");

    }

    @Test
    public void testSaveKeepsAnExistingSeed() {

        User user = new User();
        user.setId("user-1");
        user.setAuthTokenSeed("seed-existante");

        assertEquals("seed-existante", service.save(user).getAuthTokenSeed());

    }

    @Test
    public void testRotateAuthTokenSeedReplacesTheSeed() {

        User user = user("user-1", "jean@example.com");
        user.setAuthTokenSeed("ancienne-seed");

        service.rotateAuthTokenSeed("user-1");

        assertNotEquals("ancienne-seed", user.getAuthTokenSeed());
        assertNotNull(user.getAuthTokenSeed());

    }

    @Test
    public void testRotateAuthTokenSeedOnUnknownUserDoesNothing() {

        when(userRepository.findById("gone")).thenReturn(Optional.empty());

        service.rotateAuthTokenSeed("gone");

        verify(userRepository, never()).save(any(User.class));

    }

    /**
     * La migration a initialise auth_token_seed avec l'id utilisateur : cette valeur devinable doit
     * etre remplacee a la premiere authentification interactive.
     */
    @Test
    public void testLegacySeedEqualToTheUserIdIsRotated() {

        User user = new User();
        user.setId("user-1");
        user.setAuthTokenSeed("user-1");

        User result = service.ensureAuthTokenSeed(user);

        assertNotEquals("user-1", result.getAuthTokenSeed());
        verify(userRepository).save(user);

    }

    @Test
    public void testMissingSeedIsAlwaysGenerated() {

        User user = new User();
        user.setId("user-1");

        assertNotNull(service.ensureAuthTokenSeed(user).getAuthTokenSeed());

    }

    /**
     * Une graine deja aleatoire ne doit jamais etre changee : cela invaliderait sans raison tous
     * les cookies remember-me du compte.
     */
    @Test
    public void testRandomSeedIsLeftUntouched() {

        User user = new User();
        user.setId("user-1");
        user.setAuthTokenSeed("une-seed-parfaitement-aleatoire");

        assertEquals("une-seed-parfaitement-aleatoire", service.ensureAuthTokenSeed(user).getAuthTokenSeed());
        verify(userRepository, never()).save(any(User.class));

    }

    /**
     * Retour arriere par propriete : la rotation des graines heritees peut etre desactivee.
     */
    @Test
    public void testLegacySeedIsKeptWhenRotationIsDisabled() {

        ReflectionTestUtils.setField(service, "rotateLegacySeed", false);

        User user = new User();
        user.setId("user-1");
        user.setAuthTokenSeed("user-1");

        assertEquals("user-1", service.ensureAuthTokenSeed(user).getAuthTokenSeed());
        verify(userRepository, never()).save(any(User.class));

    }

    @Test
    public void testEnsureAuthTokenSeedToleratesNull() {
        assertNull(service.ensureAuthTokenSeed(null));
    }


    // --- suppression de compte ---

    /**
     * Le soft delete doit retirer TOUS les moyens de connexion. Une passkey laissee en place
     * rendrait le compte supprime connectable d'un simple geste, alors que le mot de passe et la
     * graine remember-me sont, eux, bien neutralises.
     */
    @Test
    public void testDeleteRemovesEveryLoginMean() {

        User user = user("user-1", "user@example.com");
        user.setPasswordHash("bcrypt-hash");
        user.setAuthTokenSeed("old-seed");

        service.delete("user-1");

        assertTrue(user.isDeletion());
        assertNull(user.getPasswordHash());
        assertNotEquals("old-seed", user.getAuthTokenSeed());
        verify(userPasskeyRepository).deleteByUserId("user-1");

    }

}
