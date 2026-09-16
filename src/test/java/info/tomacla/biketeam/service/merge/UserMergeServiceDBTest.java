package info.tomacla.biketeam.service.merge;

import info.tomacla.biketeam.domain.AbstractDBTest;
import info.tomacla.biketeam.domain.map.MapRatingRepository;
import info.tomacla.biketeam.domain.message.MessageRepository;
import info.tomacla.biketeam.domain.notification.NotificationRepository;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.domain.trip.TripRepository;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserAuthTokenRepository;
import info.tomacla.biketeam.domain.user.UserPasskeyRepository;
import info.tomacla.biketeam.domain.user.UserRepository;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.UserRoleService;
import info.tomacla.biketeam.service.file.FileService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Fusion contre une vraie base : les contraintes uniques ne se verifient qu'au flush, et l'ordre
 * des UPDATE emis par Hibernate depend de l'ordre dans lequel les comptes sont entres dans le
 * contexte de persistance, pas de l'ordre des appels a save().
 * <p>
 * Les tables enfants sont hors sujet ici : leurs repositories sont des mocks.
 */
public class UserMergeServiceDBTest extends AbstractDBTest {

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private UserMergeService service;

    @BeforeEach
    public void setUp() throws Exception {

        service = new UserMergeService(userRepository, mock(UserRoleService.class), mock(TripRepository.class),
                mock(RideRepository.class), mock(MessageRepository.class), mock(NotificationRepository.class),
                mock(MapRatingRepository.class), mock(UserAuthTokenRepository.class),
                mock(UserPasskeyRepository.class), mock(MapService.class), mock(FileService.class));

        Field f = UserMergeService.class.getDeclaredField("entityManager");
        f.setAccessible(true);
        f.set(service, entityManager);

    }

    /**
     * Cas reel en staging : la cible est deja chargee (open-in-view, utilisateur connecte) quand
     * la fusion commence. Hibernate met alors a jour la cible AVANT la source, et la cible
     * reprenait l'identifiant Google alors que la source le portait encore.
     */
    @Test
    public void testUniqueIdentifiersAreTakenOverWhenTheTargetIsLoadedFirst() {

        User target = new User();
        User source = new User();
        source.setGoogleId("google-1");
        source.setFacebookId("facebook-1");
        source.setStravaId(4242L);
        source.setStravaUserName("coureur");
        source.setVerifiedEmail("owner@example.com");

        userRepository.save(target);
        userRepository.save(source);
        entityManager.flush();
        entityManager.clear();

        // la cible entre la premiere dans le contexte de persistance
        userRepository.findById(target.getId()).orElseThrow();

        service.merge(source.getId(), target.getId());

        entityManager.flush();
        entityManager.clear();

        final User keptTarget = userRepository.findById(target.getId()).orElseThrow();
        final User emptiedSource = userRepository.findById(source.getId()).orElseThrow();

        assertEquals("google-1", keptTarget.getGoogleId());
        assertEquals("facebook-1", keptTarget.getFacebookId());
        assertEquals(4242L, keptTarget.getStravaId());
        assertEquals("coureur", keptTarget.getStravaUserName());
        assertEquals("owner@example.com", keptTarget.getEmail());
        assertTrue(keptTarget.isEmailVerified());

        assertNull(emptiedSource.getGoogleId());
        assertNull(emptiedSource.getFacebookId());
        assertNull(emptiedSource.getStravaId());
        assertNull(emptiedSource.getStravaUserName());
        assertNull(emptiedSource.getEmail());

    }

}
