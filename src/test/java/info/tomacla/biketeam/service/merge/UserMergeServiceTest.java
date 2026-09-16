package info.tomacla.biketeam.service.merge;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Fusion de comptes.
 * <p>
 * Deux exigences se croisent ici :
 * <ul>
 * <li>le compte conserve ne doit perdre AUCUN moyen de connexion au passage : le compte absorbe
 *     est soft-delete juste apres, ce qui efface son hash de mot de passe ;</li>
 * <li>aucune donnee ne doit etre abandonnee sur le compte absorbe : la purge asynchrone la
 *     detruirait dans les 30 secondes.</li>
 * </ul>
 */
public class UserMergeServiceTest {

    private UserRepository userRepository;
    private UserRoleService userRoleService;
    private TripRepository tripRepository;
    private RideRepository rideRepository;
    private MessageRepository messageRepository;
    private NotificationRepository notificationRepository;
    private MapRatingRepository mapRatingRepository;
    private UserAuthTokenRepository userAuthTokenRepository;
    private UserPasskeyRepository userPasskeyRepository;
    private MapService mapService;
    private FileService fileService;
    private EntityManager entityManager;

    private UserMergeService service;

    @BeforeEach
    public void setUp() throws Exception {

        userRepository = mock(UserRepository.class);
        userRoleService = mock(UserRoleService.class);
        tripRepository = mock(TripRepository.class);
        rideRepository = mock(RideRepository.class);
        messageRepository = mock(MessageRepository.class);
        notificationRepository = mock(NotificationRepository.class);
        mapRatingRepository = mock(MapRatingRepository.class);
        userAuthTokenRepository = mock(UserAuthTokenRepository.class);
        userPasskeyRepository = mock(UserPasskeyRepository.class);
        mapService = mock(MapService.class);
        fileService = mock(FileService.class);
        entityManager = mock(EntityManager.class);

        service = new UserMergeService(userRepository, userRoleService, tripRepository, rideRepository,
                messageRepository, notificationRepository, mapRatingRepository, userAuthTokenRepository,
                userPasskeyRepository, mapService, fileService);

        Field f = UserMergeService.class.getDeclaredField("entityManager");
        f.setAccessible(true);
        f.set(service, entityManager);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapRatingRepository.findRatedMapIdsByUsers(anyString(), anyString())).thenReturn(List.of());
        when(fileService.fileExists(anyString(), anyString(), anyList())).thenReturn(Optional.empty());

    }

    private void given(User... users) {
        for (User user : users) {
            when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        }
    }

    private static User account(String id) {
        User user = new User();
        user.setId(id);
        user.setAuthTokenSeed(id + "-seed");
        return user;
    }

    // ------------------------------------------------------------------
    // Moyens de connexion
    // ------------------------------------------------------------------

    @Test
    public void testMergeTransfersVerifiedEmailAndPasswordToBareTargetAccount() {

        User source = account("source-1");
        source.setVerifiedEmail("owner@example.com");
        source.setPasswordHash("bcrypt-hash");
        source.setPasswordUpdatedAt(Instant.now());

        // compte Strava nu : ni email, ni mot de passe, ni identite externe
        User target = account("target-1");
        target.setStravaId(42L);

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

        User source = account("source-2");
        source.setPasswordHash("source-hash");

        User target = account("target-2");
        target.setVerifiedEmail("target@example.com");
        target.setPasswordHash("target-hash");

        given(source, target);

        service.merge(source.getId(), target.getId());

        assertEquals("target-hash", target.getPasswordHash());
        assertEquals("target@example.com", target.getEmail());
        assertTrue(target.isEmailVerified());

    }

    @Test
    public void testMergeKeepsUnverifiedEmailUnverified() {

        User source = account("source-3");
        source.setEmail("unproven@example.com");

        User target = account("target-3");

        given(source, target);

        service.merge(source.getId(), target.getId());

        assertEquals("unproven@example.com", target.getEmail());
        assertFalse(target.isEmailVerified(),
                "une adresse non prouvee ne doit jamais devenir une identite de connexion");

    }

    @Test
    public void testMergeTransfersExternalIdentities() {

        User source = account("source-4");
        source.setGoogleId("google-sub");
        source.setFacebookId("facebook-id");

        User target = account("target-4");
        target.setStravaId(42L);

        given(source, target);

        service.merge(source.getId(), target.getId());

        assertEquals("google-sub", target.getGoogleId());
        assertEquals("facebook-id", target.getFacebookId());
        assertNull(source.getGoogleId());
        assertNull(source.getFacebookId());
        assertTrue(target.isAccountComplete());

    }

    @Test
    public void testMergeNeverOverwritesAnExistingIdentityOnTheTarget() {

        User source = account("source-5");
        source.setGoogleId("google-of-source");
        source.setFacebookId("facebook-of-source");

        User target = account("target-5");
        target.setGoogleId("google-of-target");
        target.setFacebookId("facebook-of-target");

        given(source, target);

        service.merge(source.getId(), target.getId());

        assertEquals("google-of-target", target.getGoogleId());
        assertEquals("facebook-of-target", target.getFacebookId());

    }

    // ------------------------------------------------------------------
    // Liberation des identifiants uniques
    // ------------------------------------------------------------------

    /**
     * Les contraintes unique_strava_id, unique_google_id et unique_facebook_id portent sur TOUTES
     * les lignes, y compris celles des comptes supprimes. Un identifiant qui n'a pas pu etre repris
     * doit malgre tout quitter le compte absorbe, sinon il reste bloque pour toujours.
     */
    @Test
    public void testIdentifiersAreReleasedOnTheSourceEvenWhenNotTakenOver() {

        User source = account("source-6");
        source.setStravaId(1L);
        source.setStravaUserName("strava-source");
        source.setGoogleId("google-of-source");
        source.setFacebookId("facebook-of-source");
        source.setGarminToken("garmin-of-source");
        source.setGarminTokenSecret("garmin-secret-of-source");

        User target = account("target-6");
        target.setStravaId(2L);
        target.setStravaUserName("strava-target");
        target.setGoogleId("google-of-target");
        target.setFacebookId("facebook-of-target");
        target.setGarminToken("garmin-of-target");

        given(source, target);

        service.merge(source.getId(), target.getId());

        assertNull(source.getStravaId());
        assertNull(source.getStravaUserName());
        assertNull(source.getGoogleId());
        assertNull(source.getFacebookId());
        assertNull(source.getGarminToken());
        assertNull(source.getGarminTokenSecret());

        // la cible n'a rien perdu
        assertEquals(2L, target.getStravaId());
        assertEquals("strava-target", target.getStravaUserName());
        assertEquals("garmin-of-target", target.getGarminToken());

    }

    /**
     * stravaUserName etait recopie sur la cible sans jamais etre libere sur la source : les deux
     * lignes portaient alors la meme valeur sur une colonne unique.
     */
    @Test
    public void testStravaUserNameIsReleasedWhenTheStravaIdentityMoves() {

        User source = account("source-7");
        source.setStravaId(42L);
        source.setStravaUserName("coureur");

        User target = account("target-7");

        given(source, target);

        service.merge(source.getId(), target.getId());

        assertEquals(42L, target.getStravaId());
        assertEquals("coureur", target.getStravaUserName());
        assertNull(source.getStravaId());
        assertNull(source.getStravaUserName());

    }

    @Test
    public void testGarminTokensAreTakenOverWhenTheTargetHasNone() {

        User source = account("source-8");
        source.setGarminToken("garmin-token");
        source.setGarminTokenSecret("garmin-secret");

        User target = account("target-8");

        given(source, target);

        service.merge(source.getId(), target.getId());

        assertEquals("garmin-token", target.getGarminToken());
        assertEquals("garmin-secret", target.getGarminTokenSecret());

    }

    // ------------------------------------------------------------------
    // Equipe privee
    // ------------------------------------------------------------------

    @Test
    public void testPrivateTeamIsTakenOverWhenTheTargetHasNone() {

        User source = account("source-9");
        source.setTeamId("team-of-source");

        User target = account("target-9");

        given(source, target);

        final MergeReport report = service.merge(source.getId(), target.getId());

        assertEquals("team-of-source", target.getTeamId());
        // libere sur la source : sinon la purge asynchrone supprimerait l'equipe qu'on vient de
        // transferer, avec tout son contenu
        assertNull(source.getTeamId());
        assertFalse(report.privateTeamLost());

    }

    /**
     * Deux equipes privees ne peuvent pas cohabiter sur un meme compte (colonne unique). Le cas
     * n'est pas refuse, mais il est signale : l'ecran de confirmation doit l'annoncer avant.
     */
    @Test
    public void testLosingAPrivateTeamIsReported() {

        User source = account("source-10");
        source.setTeamId("team-of-source");

        User target = account("target-10");
        target.setTeamId("team-of-target");

        given(source, target);

        assertTrue(service.wouldLosePrivateTeam(source, target));

        final MergeReport report = service.merge(source.getId(), target.getId());

        assertTrue(report.privateTeamLost());
        assertEquals("team-of-target", target.getTeamId());
        assertEquals("team-of-source", source.getTeamId());

    }

    // ------------------------------------------------------------------
    // Donnees rattachees
    // ------------------------------------------------------------------

    @Test
    public void testEveryAttachedTableIsMovedWithDeduplicationFirst() {

        User source = account("source-11");
        User target = account("target-11");
        given(source, target);

        service.merge(source.getId(), target.getId());

        final String src = source.getId();
        final String tgt = target.getId();

        // le dedoublonnage precede toujours le deplacement : l'inverse violerait la cle primaire
        // de map_favorite et creerait des doublons de participation.
        InOrder order = inOrder(entityManager, tripRepository, rideRepository, userRepository,
                mapRatingRepository, messageRepository, notificationRepository, userAuthTokenRepository);

        order.verify(entityManager).flush();
        order.verify(tripRepository).deleteDuplicateParticipations(src, tgt);
        order.verify(tripRepository).moveParticipations(src, tgt);
        order.verify(rideRepository).deleteDuplicateParticipations(src, tgt);
        order.verify(rideRepository).moveParticipations(src, tgt);
        order.verify(userRepository).deleteDuplicateMapFavorites(src, tgt);
        order.verify(userRepository).moveMapFavorites(src, tgt);
        order.verify(mapRatingRepository).applyMostRecentRating(src, tgt);
        order.verify(mapRatingRepository).deleteDuplicateRatings(src, tgt);
        order.verify(mapRatingRepository).moveRatings(src, tgt);
        order.verify(messageRepository).moveToUser(src, tgt);
        order.verify(notificationRepository).moveToUser(src, tgt);
        order.verify(userAuthTokenRepository).consumeAllForUser(eq(src), any(Instant.class));

    }

    /**
     * Les passkeys suivent le compte conserve. Sans ce deplacement, le soft delete du compte
     * source les detruirait : l'utilisateur perdrait l'appareil avec lequel il venait
     * eventuellement de se connecter, sans aucun avertissement.
     */
    @Test
    public void testPasskeysAreMovedToTheKeptAccount() {

        User source = account("source-12");
        User target = account("target-12");
        given(source, target);

        service.merge(source.getId(), target.getId());

        verify(userPasskeyRepository).moveToUser(source.getId(), target.getId());

    }

    /**
     * map.average_rating et map.rating_count sont denormalises : sans recalcul, la moyenne derive
     * des qu'un doublon disparait.
     */
    @Test
    public void testCachedRatingsAreRecomputedForEveryMapInvolved() {

        User source = account("source-12");
        User target = account("target-12");
        given(source, target);

        when(mapRatingRepository.findRatedMapIdsByUsers(source.getId(), target.getId()))
                .thenReturn(List.of("map-a", "map-b"));

        service.merge(source.getId(), target.getId());

        verify(mapService).refreshCachedRatings("map-a");
        verify(mapService).refreshCachedRatings("map-b");

    }

    // ------------------------------------------------------------------
    // Garde-fous
    // ------------------------------------------------------------------

    @Test
    public void testMergingAnAccountIntoItselfIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> service.merge("same", "same"));
        verify(tripRepository, never()).moveParticipations(anyString(), anyString());
    }

    @Test
    public void testUnknownAccountIsRefused() {
        User target = account("target-13");
        given(target);
        when(userRepository.findById("ghost")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.merge("ghost", target.getId()));
    }

    /**
     * Fusionner vers un compte en attente de purge le detruirait avec les donnees qu'on vient de
     * lui transferer.
     */
    @Test
    public void testAccountPendingDeletionIsRefused() {

        User source = account("source-14");
        User target = account("target-14");
        target.setDeletion(true);
        given(source, target);

        assertThrows(IllegalStateException.class, () -> service.merge(source.getId(), target.getId()));
        verify(tripRepository, never()).moveParticipations(anyString(), anyString());

    }

    /**
     * Le systeme de fichiers n'est pas transactionnel : un avatar perdu ne doit jamais faire
     * echouer une fusion de comptes.
     */
    @Test
    public void testProfileImageFailureDoesNotBreakTheMerge() {

        User source = account("source-15");
        User target = account("target-15");
        given(source, target);

        when(fileService.fileExists(anyString(), anyString(), anyList()))
                .thenThrow(new RuntimeException("disk is on fire"));

        final MergeReport report = service.merge(source.getId(), target.getId());

        assertFalse(report.imageMoved());

    }

}
