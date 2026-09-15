package info.tomacla.biketeam.service.merge;

import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.map.MapRatingRepository;
import info.tomacla.biketeam.domain.message.MessageRepository;
import info.tomacla.biketeam.domain.notification.NotificationRepository;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.domain.trip.TripRepository;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserAuthTokenRepository;
import info.tomacla.biketeam.domain.user.UserRepository;
import info.tomacla.biketeam.domain.userrole.Role;
import info.tomacla.biketeam.domain.userrole.UserRole;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.UserRoleService;
import info.tomacla.biketeam.service.file.FileService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Deplacement de toutes les donnees d'un compte vers un autre.
 * <p>
 * Extrait de {@link info.tomacla.biketeam.service.UserService}, qui delegue ici : la fusion a
 * besoin de huit repositories, et UserService est deja injecte par SecurityConfig via
 * UserDetailsService (chaque dependance supplementaire rapproche du cycle que le {@code @Lazy}
 * sur PasswordEncoder documente deja).
 * <p>
 * <strong>Ce service ne supprime pas le compte source.</strong> Il le vide de ses donnees et
 * libere ses identifiants uniques ; la suppression (soft delete) reste a la charge de
 * l'appelant, dans la meme transaction.
 * <p>
 * L'ordre des etapes est contraint, voir {@link #merge(String, String)}.
 */
@Service
public class UserMergeService {

    private static final Logger log = LoggerFactory.getLogger(UserMergeService.class);

    private final UserRepository userRepository;
    private final UserRoleService userRoleService;
    private final TripRepository tripRepository;
    private final RideRepository rideRepository;
    private final MessageRepository messageRepository;
    private final NotificationRepository notificationRepository;
    private final MapRatingRepository mapRatingRepository;
    private final UserAuthTokenRepository userAuthTokenRepository;
    private final MapService mapService;
    private final FileService fileService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public UserMergeService(UserRepository userRepository,
                            UserRoleService userRoleService,
                            TripRepository tripRepository,
                            RideRepository rideRepository,
                            MessageRepository messageRepository,
                            NotificationRepository notificationRepository,
                            MapRatingRepository mapRatingRepository,
                            UserAuthTokenRepository userAuthTokenRepository,
                            MapService mapService,
                            FileService fileService) {
        this.userRepository = userRepository;
        this.userRoleService = userRoleService;
        this.tripRepository = tripRepository;
        this.rideRepository = rideRepository;
        this.messageRepository = messageRepository;
        this.notificationRepository = notificationRepository;
        this.mapRatingRepository = mapRatingRepository;
        this.userAuthTokenRepository = userAuthTokenRepository;
        this.mapService = mapService;
        this.fileService = fileService;
    }

    /**
     * Annonce ce qu'une fusion ferait, sans rien modifier. Destine a l'ecran de confirmation :
     * le seul cas reellement destructeur (perte d'une equipe privee) doit etre montre avant.
     *
     * @return vrai si les deux comptes possedent une equipe privee. La colonne
     * {@code user_account.team_id} est unique et {@code Team} ne porte aucun autre champ de
     * proprietaire : le compte conserve ne peut pas reprendre la seconde equipe, qui sera
     * supprimee avec le compte absorbe.
     */
    public boolean wouldLosePrivateTeam(User source, User target) {
        return source.getTeamId() != null && target.getTeamId() != null;
    }

    /**
     * Deplace tout ce qui est rattache a {@code sourceId} vers {@code targetId}.
     * <p>
     * L'ordre est impose :
     * <ol>
     * <li>roles d'equipe, par manipulation d'entites : {@code Team} porte une collection
     *     {@code roles} EAGER qu'un DELETE natif laisserait perimee ;</li>
     * <li>champs scalaires du compte, puis <strong>liberation des identifiants uniques</strong>
     *     de la source et {@code saveAndFlush} : les contraintes {@code unique_strava_id},
     *     {@code unique_google_id}, {@code unique_facebook_id} et {@code strava_user_name} ne
     *     sont PAS partielles (contrairement a l'index email), un identifiant laisse sur un
     *     compte mort bloquerait definitivement le compte conserve ;</li>
     * <li>tables enfants, en SQL natif : {@code Trip.participants} et
     *     {@code RideGroup.participants} sont EAGER, les charger tirerait tout le graphe ;</li>
     * <li>recalcul des compteurs de notes, invalidation des tokens, image de profil.</li>
     * </ol>
     * Les requetes natives portent {@code clearAutomatically = true} : a la sortie, le contexte
     * de persistance est vide et <strong>toute entite lue avant l'appel est detachee</strong>.
     * L'appelant doit relire le compte conserve par son identifiant.
     *
     * @throws IllegalArgumentException identifiants inconnus ou identiques
     * @throws IllegalStateException    l'un des deux comptes est deja en attente de suppression
     */
    @Transactional(rollbackFor = Exception.class)
    public MergeReport merge(String sourceId, String targetId) {

        if (sourceId == null || targetId == null || sourceId.equals(targetId)) {
            throw new IllegalArgumentException("Unknown ids for merge");
        }

        final User source = userRepository.findById(sourceId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown ids for merge"));
        final User target = userRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown ids for merge"));

        // fusionner dans un compte en attente de purge le detruirait avec ses nouvelles donnees ;
        // fusionner depuis un tel compte ferait remonter des donnees deja repudiees.
        if (source.isDeletion() || target.isDeletion()) {
            throw new IllegalStateException("Impossible de fusionner un compte en cours de suppression");
        }

        final boolean privateTeamLost = wouldLosePrivateTeam(source, target);

        // releve AVANT toute modification : les compteurs denormalises de ces cartes devront etre
        // recalcules, y compris ceux des cartes dont une note aura disparu par dedoublonnage.
        final List<String> ratedMapIds = mapRatingRepository.findRatedMapIdsByUsers(sourceId, targetId);

        final int roles = mergeRoles(source, target);

        mergeScalars(source, target);
        releaseUniqueIdentifiers(source, target);

        userRepository.save(target);
        userRepository.save(source);

        // le flush doit preceder les requetes natives : sans lui les identifiants uniques ne sont
        // pas encore liberes en base au moment ou les UPDATE tournent, et la transaction mourrait
        // sur une contrainte au flush final.
        entityManager.flush();

        // --- tables enfants, en SQL natif ---

        tripRepository.deleteDuplicateParticipations(sourceId, targetId);
        final int trips = tripRepository.moveParticipations(sourceId, targetId);

        rideRepository.deleteDuplicateParticipations(sourceId, targetId);
        final int rideGroups = rideRepository.moveParticipations(sourceId, targetId);

        userRepository.deleteDuplicateMapFavorites(sourceId, targetId);
        final int mapFavorites = userRepository.moveMapFavorites(sourceId, targetId);

        mapRatingRepository.applyMostRecentRating(sourceId, targetId);
        mapRatingRepository.deleteDuplicateRatings(sourceId, targetId);
        final int mapRatings = mapRatingRepository.moveRatings(sourceId, targetId);

        final int messages = messageRepository.moveToUser(sourceId, targetId);
        final int notifications = notificationRepository.moveToUser(sourceId, targetId);

        userAuthTokenRepository.consumeAllForUser(sourceId, Instant.now());

        ratedMapIds.forEach(mapService::refreshCachedRatings);

        final boolean imageMoved = moveProfileImage(sourceId, targetId);

        final MergeReport report = new MergeReport(trips, rideGroups, mapFavorites, mapRatings,
                messages, notifications, roles, imageMoved, privateTeamLost);

        log.info("Merged user {} into {} : {}", sourceId, targetId, report);

        return report;

    }

    /**
     * Reprise des roles d'equipe. Un compte ne peut porter qu'un role par equipe
     * (l'identifiant de UserRole est {@code teamId + "-" + userId}) : sur une equipe commune aux
     * deux comptes, seule une promotion MEMBER -> ADMIN est possible.
     */
    private int mergeRoles(User source, User target) {

        int moved = 0;

        for (UserRole role : source.getRoles()) {

            if (role.getTeam().isMember(target)) {

                if (role.getRole().equals(Role.ADMIN) && !role.getTeam().isAdmin(target)) {
                    // orElseGet plutot que get() : isMember s'appuie sur la collection EAGER de
                    // Team, qui peut diverger de user_role apres les fusions precedentes.
                    final UserRole targetRole = userRoleService.get(role.getTeam(), target)
                            .orElseGet(() -> new UserRole(role.getTeam(), target, Role.ADMIN));
                    targetRole.setRole(Role.ADMIN);
                    userRoleService.save(targetRole);
                    moved++;
                }

            } else {
                userRoleService.save(new UserRole(role.getTeam(), target, role.getRole()));
                moved++;
            }

        }

        source.getRoles().clear();

        return moved;

    }

    /**
     * Champs du compte, toujours selon la meme regle : la cible ne perd jamais une valeur
     * qu'elle possede deja, elle ne fait que combler ses trous.
     */
    private void mergeScalars(User source, User target) {

        if (!Strings.isBlank(source.getFirstName()) && Strings.isBlank(target.getFirstName())) {
            target.setFirstName(source.getFirstName());
        }
        if (!Strings.isBlank(source.getLastName()) && Strings.isBlank(target.getLastName())) {
            target.setLastName(source.getLastName());
        }
        if (!Strings.isBlank(source.getCity()) && Strings.isBlank(target.getCity())) {
            target.setCity(source.getCity());
        }

        if (source.getStravaId() != null && target.getStravaId() == null) {
            target.setStravaId(source.getStravaId());
            target.setStravaUserName(source.getStravaUserName());
        }

        if (!Strings.isBlank(source.getEmail()) && Strings.isBlank(target.getEmail())) {
            // setVerifiedEmail des lors que la source avait prouve l'adresse : setEmail
            // remettrait emailVerified a false et ferait perdre a la cible la seule identite
            // de connexion que la fusion etait censee lui apporter.
            if (source.isEmailVerified()) {
                target.setVerifiedEmail(source.getEmail());
            } else {
                target.setEmail(source.getEmail());
            }
            source.setEmail(null);
        }

        // le mot de passe de la source est detruit par le soft delete juste apres : sans cette
        // reprise, fusionner un compte email/mot de passe dans un compte Strava nu laisserait
        // la cible incomplete et bloquee sur l'ecran de completion.
        if (target.getPasswordHash() == null && source.getPasswordHash() != null) {
            target.setPasswordHash(source.getPasswordHash());
            target.setPasswordUpdatedAt(source.getPasswordUpdatedAt());
        }

        if (!Strings.isBlank(source.getGoogleId()) && Strings.isBlank(target.getGoogleId())) {
            target.setGoogleId(source.getGoogleId());
        }
        if (!Strings.isBlank(source.getFacebookId()) && Strings.isBlank(target.getFacebookId())) {
            target.setFacebookId(source.getFacebookId());
        }

        if (!Strings.isBlank(source.getGarminToken()) && Strings.isBlank(target.getGarminToken())) {
            target.setGarminToken(source.getGarminToken());
            target.setGarminTokenSecret(source.getGarminTokenSecret());
        }

        // equipe privee : reprise seulement si la cible n'en a pas (colonne unique).
        if (source.getTeamId() != null && target.getTeamId() == null) {
            target.setTeamId(source.getTeamId());
        }

        target.setAdmin(source.isAdmin() || target.isAdmin());
        target.setEmailPublishTrips(source.isEmailPublishTrips() || target.isEmailPublishTrips());
        target.setEmailPublishPublications(source.isEmailPublishPublications() || target.isEmailPublishPublications());
        target.setEmailPublishRides(source.isEmailPublishRides() || target.isEmailPublishRides());

    }

    /**
     * Libere sur la source TOUS les identifiants uniques, qu'ils aient ete repris ou non.
     * <p>
     * Un identifiant que la cible n'a pas pu reprendre (parce qu'elle en portait deja un) resterait
     * sinon pose sur un compte mort et interdirait definitivement de relier cette identite au
     * compte conserve : {@code unique_strava_id}, {@code unique_google_id} et
     * {@code unique_facebook_id} sont des contraintes totales, elles ne filtrent pas sur
     * {@code deletion}.
     * <p>
     * L'email est la seule exception : l'index fonctionnel
     * {@code unique_user_email_lower ... where deletion = false} libere deja l'adresse, et
     * conserver la valeur permet un rattrapage en cas d'erreur. Elle n'est effacee que si la
     * cible l'a reprise (fait dans {@link #mergeScalars}).
     * <p>
     * {@code teamId} n'est libere que s'il a ete repris : sinon l'equipe privee resterait
     * orpheline au lieu d'etre supprimee avec son proprietaire.
     */
    private void releaseUniqueIdentifiers(User source, User target) {

        source.setStravaId(null);
        // bug corrige : stravaUserName etait recopie sur la cible mais jamais libere ici, ce qui
        // laissait la meme valeur sur les deux lignes.
        source.setStravaUserName(null);
        source.setGoogleId(null);
        source.setFacebookId(null);
        source.setGarminToken(null);
        source.setGarminTokenSecret(null);

        if (source.getTeamId() != null && source.getTeamId().equals(target.getTeamId())) {
            source.setTeamId(null);
        }

    }

    /**
     * Reprend l'image de profil de la source si la cible n'en a pas.
     * <p>
     * Le systeme de fichiers n'est pas transactionnel : un echec est journalise et ignore. Un
     * avatar perdu ne doit jamais faire echouer une fusion de comptes.
     */
    private boolean moveProfileImage(String sourceId, String targetId) {

        try {

            if (findImage(targetId).isPresent()) {
                return false;
            }

            final Optional<ImageDescriptor> sourceImage = findImage(sourceId);
            if (sourceImage.isEmpty()) {
                return false;
            }

            final FileExtension extension = sourceImage.get().getExtension();
            final Path path = sourceImage.get().getPath();

            fileService.storeFile(path, FileRepositories.USER_IMAGES, targetId + extension.getExtension());
            fileService.deleteFile(FileRepositories.USER_IMAGES, sourceId + extension.getExtension());

            return true;

        } catch (Exception e) {
            log.warn("Unable to move profile image from user {} to {}", sourceId, targetId, e);
            return false;
        }

    }

    private Optional<ImageDescriptor> findImage(String userId) {

        return fileService.fileExists(FileRepositories.USER_IMAGES, userId, FileExtension.byPriority())
                .map(extension -> ImageDescriptor.of(extension,
                        fileService.getFile(FileRepositories.USER_IMAGES, userId + extension.getExtension())));

    }

}
