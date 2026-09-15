package info.tomacla.biketeam.service;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Fusion de comptes : le compte cible ne doit perdre AUCUN moyen de connexion au passage.
 * <p>
 * Le compte source est soft-delete par la fusion, ce qui efface son hash de mot de passe : sans
 * reprise explicite, fusionner un compte email/mot de passe dans un compte Strava nu laisserait la
 * cible incomplete et bloquee sur l'ecran de completion, alors que l'ecran de confirmation promet
 * que "le compte courant sera conserve".
 */
public class UserServiceMergeTest {

    private UserRepository userRepository;
    private UserRoleService userRoleService;
    private UserService service;

    @BeforeEach
    public void setUp() throws Exception {

        userRepository = mock(UserRepository.class);
        userRoleService = mock(UserRoleService.class);

        service = new UserService();
        set(service, "userRepository", userRepository);
        set(service, "userRoleService", userRoleService);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    }

    private static void set(Object target, String field, Object value) throws Exception {
        Field f = UserService.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    private void given(User... users) {
        for (User user : users) {
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        }
    }

    @Test
    public void testMergeTransfersVerifiedEmailAndPasswordToBareTargetAccount() {

        User source = new User();
        source.setId("source-1");
        source.setVerifiedEmail("owner@example.com");
        source.setPasswordHash("bcrypt-hash");
        source.setPasswordUpdatedAt(Instant.now());
        source.setAuthTokenSeed("source-seed");

        // compte Strava nu : ni email, ni mot de passe, ni identite externe
        User target = new User();
        target.setId("target-1");
        target.setStravaId(42L);
        target.setAuthTokenSeed("target-seed");

        given(source, target);

        service.merge(source.getId(), target.getId());

        assertEquals("owner@example.com", target.getEmail());
        assertTrue(target.isEmailVerified(), "l'adresse prouvee par la source doit rester prouvee sur la cible");
        assertEquals("bcrypt-hash", target.getPasswordHash());
        assertNotNull(target.getPasswordUpdatedAt());

        // la cible est desormais complete : elle ne sera pas renvoyee sur /account/complete
        assertTrue(target.isAccountComplete());

    }

    @Test
    public void testMergeNeverOverwritesAnExistingPasswordOnTheTarget() {

        User source = new User();
        source.setId("source-2");
        source.setPasswordHash("source-hash");
        source.setAuthTokenSeed("source-seed");

        User target = new User();
        target.setId("target-2");
        target.setVerifiedEmail("target@example.com");
        target.setPasswordHash("target-hash");
        target.setAuthTokenSeed("target-seed");

        given(source, target);

        service.merge(source.getId(), target.getId());

        assertEquals("target-hash", target.getPasswordHash());
        assertEquals("target@example.com", target.getEmail());
        assertTrue(target.isEmailVerified());

    }

    @Test
    public void testMergeKeepsUnverifiedEmailUnverified() {

        User source = new User();
        source.setId("source-3");
        source.setEmail("unproven@example.com");
        source.setAuthTokenSeed("source-seed");

        User target = new User();
        target.setId("target-3");
        target.setAuthTokenSeed("target-seed");

        given(source, target);

        service.merge(source.getId(), target.getId());

        assertEquals("unproven@example.com", target.getEmail());
        assertTrue(!target.isEmailVerified(), "une adresse non prouvee ne doit jamais devenir une identite de connexion");

    }

    /**
     * Les identites externes de la source sont transferees sur la cible, et retirees de la source
     * pour que sa suppression ne les emporte pas.
     */
    @Test
    public void testMergeTransfersExternalIdentities() {

        User source = new User();
        source.setId("source-4");
        source.setGoogleId("google-sub");
        source.setFacebookId("facebook-id");
        source.setAuthTokenSeed("source-seed");

        User target = new User();
        target.setId("target-4");
        target.setStravaId(42L);
        target.setAuthTokenSeed("target-seed");

        given(source, target);

        service.merge(source.getId(), target.getId());

        assertEquals("google-sub", target.getGoogleId());
        assertEquals("facebook-id", target.getFacebookId());
        assertNull(source.getGoogleId());
        assertNull(source.getFacebookId());
        assertTrue(target.isAccountComplete());

    }

    /**
     * Une identite deja portee par la cible n'est jamais ecrasee par celle de la source.
     */
    @Test
    public void testMergeNeverOverwritesAnExistingIdentityOnTheTarget() {

        User source = new User();
        source.setId("source-5");
        source.setGoogleId("google-of-source");
        source.setFacebookId("facebook-of-source");
        source.setAuthTokenSeed("source-seed");

        User target = new User();
        target.setId("target-5");
        target.setGoogleId("google-of-target");
        target.setFacebookId("facebook-of-target");
        target.setAuthTokenSeed("target-seed");

        given(source, target);

        service.merge(source.getId(), target.getId());

        assertEquals("google-of-target", target.getGoogleId());
        assertEquals("facebook-of-target", target.getFacebookId());

    }

}
