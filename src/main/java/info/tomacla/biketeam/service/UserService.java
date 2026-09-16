package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.amqp.Queues;
import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.SearchUserSpecification;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserPasskeyRepository;
import info.tomacla.biketeam.domain.user.UserRepository;
import info.tomacla.biketeam.security.Authorities;
import info.tomacla.biketeam.service.amqp.dto.UserProfileImageDTO;
import info.tomacla.biketeam.service.file.FileService;
import info.tomacla.biketeam.service.merge.UserMergeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    private UserRepository userRepository;

    /**
     * @Lazy indispensable : le bean PasswordEncoder est declare par SecurityConfig, qui injecte
     * lui-meme UserDetailsService -> UserService. Sans proxy differe, le contexte echouerait
     * au demarrage sur une reference circulaire (interdite par defaut depuis Spring Boot 2.6).
     */
    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Value("${admin.strava-id}")
    private Long adminStravaId;

    @Value("${admin.email:}")
    private String adminEmail;

    @Value("${admin.password:}")
    private String adminPassword;

    @Value("${auth.rotate-legacy-seed:true}")
    private boolean rotateLegacySeed;

    @Value("${admin.first-name}")
    private String adminFirstName;

    @Value("${admin.last-name}")
    private String adminLastName;

    @Autowired
    private TeamService teamService;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserMergeService userMergeService;

    @Autowired
    private UserPasskeyRepository userPasskeyRepository;

    public Optional<User> getByStravaId(Long stravaId) {
        return userRepository.findOne(SearchUserSpecification.byStravaId(stravaId));
    }

    public Optional<User> getByFacebookId(String facebookId) {
        return userRepository.findOne(SearchUserSpecification.byFacebookId(facebookId));
    }

    public Optional<User> getByGoogleId(String googleId) {
        return userRepository.findOne(SearchUserSpecification.byGoogleId(googleId));
    }

    public Optional<User> getByEmail(String email) {
        return userRepository.findOne(SearchUserSpecification.byEmail(email));
    }

    /**
     * Vrai si l'adresse n'est utilisee par aucun autre compte actif.
     * L'index unique fonctionnel (lower(email) where deletion = false) reste le garde-fou
     * ultime en cas de course : tout appelant doit egalement traiter DataIntegrityViolationException.
     */
    public boolean isEmailAvailable(String email, String currentUserId) {
        if (Strings.isBlank(email)) {
            return false;
        }
        return getByEmail(email.trim().toLowerCase())
                .map(user -> user.getId().equals(currentUserId))
                .orElse(true);
    }

    /**
     * Rotation paresseuse de la graine de signature remember-me.
     * <p>
     * La migration a initialise auth_token_seed avec l'id utilisateur pour ne pas invalider les
     * cookies deja emis (leur signature a ete calculee avec l'id en guise de mot de passe).
     * A la premiere authentification interactive, on remplace cette valeur devinable par
     * 32 octets aleatoires. A n'appeler JAMAIS sur un auto-login remember-me : la signature
     * presentee ne correspondrait plus.
     */
    @Transactional
    public User ensureAuthTokenSeed(User user) {

        if (user == null) {
            return null;
        }

        final String seed = user.getAuthTokenSeed();
        final boolean legacySeed = seed == null || seed.equals(user.getId());

        if (legacySeed && (seed == null || rotateLegacySeed)) {
            user.setAuthTokenSeed(newAuthTokenSeed());
            return save(user);
        }

        return user;

    }

    /**
     * Invalide immediatement tous les cookies remember-me du compte.
     */
    @Transactional
    public void rotateAuthTokenSeed(String userId) {
        log.info("Rotating auth token seed of user {}", userId);
        get(userId).ifPresent(user -> {
            user.setAuthTokenSeed(newAuthTokenSeed());
            save(user);
        });
    }

    @Transactional
    public void setPassword(String userId, String rawPassword) {
        get(userId).ifPresent(user -> {
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            user.setPasswordUpdatedAt(Instant.now());
            save(user);
        });
    }

    private static String newAuthTokenSeed() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Transactional
    public User save(User user) {
        // auth_token_seed est NOT NULL en base : tout compte cree (Strava, Google, Facebook,
        // inscription) doit en porter un des sa premiere ecriture.
        if (user.getAuthTokenSeed() == null) {
            user.setAuthTokenSeed(newAuthTokenSeed());
        }
        return userRepository.save(user);
    }

    public Optional<User> get(String userId) {
        return userRepository.findById(userId);
    }

    public List<User> listAdmins() {
        return userRepository.findAll(SearchUserSpecification.admins(), Sort.by(Sort.Order.asc("firstName").ignoreCase()));
    }

    public Page<User> listUsers(String name, int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Order.asc("firstName").ignoreCase()));
        return userRepository.findAll(SearchUserSpecification.byName(name), pageRequest);
    }

    public Page<User> listTeamUsers(Team team, String name, int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Order.asc("firstName").ignoreCase()));
        return userRepository.findAll(SearchUserSpecification.byNameInTeam(name, team), pageRequest);
    }

    public List<User> listUsersWithMailActivated(Team team) {
        return userRepository.findAll(SearchUserSpecification.withEmailInTeam(team), Sort.by(Sort.Order.asc("firstName").ignoreCase()));
    }

    @Transactional
    public void promote(String userId) {
        log.info("Request user promotion to admin {}", userId);
        get(userId).ifPresent(user -> {
            user.setAdmin(true);
            save(user);
        });
    }

    @Transactional
    public void relegate(String userId) {
        log.info("Request user relegation {}", userId);
        get(userId).ifPresent(user -> {
            user.setAdmin(false);
            save(user);
        });
    }


    public boolean authorizeAdminAccess(Authentication authentication, String teamId) {
        // used in spring security config
        return authentication.getAuthorities().contains(Authorities.admin())
                || authentication.getAuthorities().contains(Authorities.teamAdmin(teamId));
    }

    public boolean authorizePublicAccess(Authentication authentication, String teamId) {

        // used in spring security config
        if (authentication.getAuthorities().contains(Authorities.admin())
                || authentication.getAuthorities().contains(Authorities.teamAdmin(teamId))) {
            return true;
        }

        final Team team = teamService.get(teamId).orElseThrow(() -> new IllegalArgumentException("Unknown team " + teamId));
        if (team.isPublic()) {
            return true;
        }

        return authentication.getAuthorities().contains(Authorities.teamUser(teamId));

    }

    public boolean authorizeAuthenticatedPublicAccess(Authentication authentication, String teamId) {

        // used in spring security config
        if (!authentication.isAuthenticated() || authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))) {
            return false;
        }

        if (authentication.getAuthorities().contains(Authorities.admin())
                || authentication.getAuthorities().contains(Authorities.teamAdmin(teamId))) {
            return true;
        }

        final Team team = teamService.get(teamId).orElseThrow(() -> new IllegalArgumentException("Unknown team " + teamId));
        if (team.isPublic()) {
            return true;
        }

        return authentication.getAuthorities().contains(Authorities.teamUser(teamId));

    }

    public Optional<ImageDescriptor> getImage(String userId) {

        Optional<FileExtension> fileExtensionExists = fileService.fileExists(FileRepositories.USER_IMAGES, userId, FileExtension.byPriority());

        if (fileExtensionExists.isPresent()) {

            final FileExtension extension = fileExtensionExists.get();
            final Path path = fileService.getFile(FileRepositories.USER_IMAGES, userId + extension.getExtension());

            return Optional.of(ImageDescriptor.of(extension, path));

        }

        return Optional.empty();

    }


    @RabbitListener(queues = Queues.TASK_DOWNLOAD_PROFILE_IMAGE)
    public void downloadUserImage(UserProfileImageDTO dto) {
        try {

            Optional<FileExtension> fileExtension = FileExtension.findByFileName(dto.profileImage);
            if (fileExtension.isPresent()) {
                RestTemplate rest = new RestTemplate();
                byte[] imageBytes = rest.getForObject(dto.profileImage, byte[].class);
                Path targetTmpFile = fileService.getTempFile("profile", fileExtension.get().getExtension());
                Files.write(targetTmpFile, imageBytes);
                fileService.storeFile(targetTmpFile, FileRepositories.USER_IMAGES, dto.id + fileExtension.get().getExtension());
            }

        } catch (Exception e) {
            log.error("Unable to download user image " + dto.profileImage);
        }
    }

    /**
     * Fusion de deux comptes : {@code sourceId} est vide de ses donnees au profit de
     * {@code targetId}, puis supprime.
     * <p>
     * Le deplacement lui-meme est delegue a {@link UserMergeService}. Le soft delete reste ici,
     * dans la meme transaction, et <strong>doit venir apres</strong> : a ce moment la source n'a
     * plus aucune donnee rattachee ni aucune equipe privee reprise, la purge asynchrone
     * (AsyncDeletionService, declenchee toutes les 30 secondes) n'a donc plus rien a detruire.
     *
     * @return le compte conserve, relu depuis la base : les requetes natives de la fusion
     * detachent tout le contexte de persistance.
     */
    @Transactional
    public User merge(String sourceId, String targetId) {

        try {

            userMergeService.merge(sourceId, targetId);

            // le compte source n'a plus ni donnees ni identifiants uniques
            this.delete(sourceId);

            return get(targetId).orElseThrow(
                    () -> new IllegalStateException("Compte conserve introuvable apres fusion"));

        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unable to merge user {} into {}", sourceId, targetId, e);
            throw new IllegalStateException("Impossible de fusionner les comptes", e);
        }

    }

    @Transactional
    public void delete(String userId) {
        log.info("Request user deletion {}", userId);
        get(userId).ifPresent(user -> {
            user.setDeletion(true);
            // hygiene : plus aucun moyen de connexion sur un compte supprime.
            // L'email n'est PAS efface : l'index unique partiel (where deletion = false)
            // libere deja l'adresse, et la conserver permet un rattrapage eventuel.
            user.setPasswordHash(null);
            user.setAuthTokenSeed(newAuthTokenSeed());
            // les passkeys sont un moyen de connexion a part entiere : les laisser en place
            // rendrait le compte supprime connectable d'un simple geste.
            userPasskeyRepository.deleteByUserId(userId);
            save(user);
        });
    }

    @PostConstruct
    public void init() {

        log.info("Initializing application data");

        if (getByStravaId(adminStravaId).isEmpty()) {

            User root = new User();
            root.setAdmin(true);
            root.setFirstName(adminFirstName);
            root.setLastName(adminLastName);
            root.setStravaId(adminStravaId);
            save(root);
        }

        // amorcage d'un administrateur sans Strava (prepare le retrait de la connexion Strava).
        // Un mot de passe deja pose n'est jamais reecrit au demarrage.
        if (!Strings.isBlank(adminEmail) && !Strings.isBlank(adminPassword)) {

            final String normalizedAdminEmail = adminEmail.trim().toLowerCase();

            if (getByEmail(normalizedAdminEmail).isEmpty()) {

                log.info("Creating admin account from admin.email");

                User admin = new User();
                admin.setAdmin(true);
                admin.setFirstName(adminFirstName);
                admin.setLastName(adminLastName);
                admin.setVerifiedEmail(normalizedAdminEmail);
                admin.setPasswordHash(passwordEncoder.encode(adminPassword));
                admin.setPasswordUpdatedAt(Instant.now());
                admin.setAuthTokenSeed(newAuthTokenSeed());
                save(admin);

            }

        }

    }


}