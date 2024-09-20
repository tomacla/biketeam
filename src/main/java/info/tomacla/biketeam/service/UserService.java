package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.amqp.Queues;
import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.SearchUserSpecification;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserRepository;
import info.tomacla.biketeam.domain.userrole.Role;
import info.tomacla.biketeam.domain.userrole.UserRole;
import info.tomacla.biketeam.security.Authorities;
import info.tomacla.biketeam.service.amqp.dto.UserProfileImageDTO;
import info.tomacla.biketeam.service.file.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Value("${admin.strava-id}")
    private Long adminStravaId;

    @Value("${admin.first-name}")
    private String adminFirstName;

    @Value("${admin.last-name}")
    private String adminLastName;

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RideService rideService;

    @Autowired
    private TripService tripService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FileService fileService;

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

    @Transactional
    public void save(User user) {
        userRepository.save(user);
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

    @Transactional
    public void merge(String sourceId, String targetId) {

        Optional<User> optionalSource = get(sourceId);
        Optional<User> optionalTarget = get(targetId);

        if (optionalSource.isEmpty() || optionalTarget.isEmpty()) {
            throw new IllegalArgumentException("Unknown ids for merge");
        }

        try {
            User source = optionalSource.get();
            User target = optionalTarget.get();

            if (!Strings.isBlank(source.getFirstName()) && Strings.isBlank(target.getFirstName())) {
                target.setFirstName(source.getFirstName());
            }
            if (!Strings.isBlank(source.getLastName()) && Strings.isBlank(target.getLastName())) {
                target.setLastName(source.getLastName());
            }
            if (source.getStravaId() != null && target.getStravaId() == null) {
                target.setStravaId(source.getStravaId());
                target.setStravaUserName(source.getStravaUserName());
                source.setStravaId(null);
            }
            if (!Strings.isBlank(source.getCity()) && Strings.isBlank(target.getCity())) {
                target.setCity(source.getCity());
            }
            if (!Strings.isBlank(source.getEmail()) && Strings.isBlank(target.getEmail())) {
                target.setEmail(source.getEmail());
                source.setEmail(null);
            }
            if (!Strings.isBlank(source.getGoogleId()) && Strings.isBlank(target.getGoogleId())) {
                target.setGoogleId(source.getGoogleId());
                source.setGoogleId(null);
            }
            if (!Strings.isBlank(source.getFacebookId()) && Strings.isBlank(target.getFacebookId())) {
                target.setFacebookId(source.getFacebookId());
                source.setFacebookId(null);
            }

            target.setAdmin(source.isAdmin() || target.isAdmin());
            target.setEmailPublishTrips(source.isEmailPublishTrips() || target.isEmailPublishTrips());
            target.setEmailPublishPublications(source.isEmailPublishPublications() || target.isEmailPublishPublications());
            target.setEmailPublishRides(source.isEmailPublishRides() || target.isEmailPublishRides());

            for (UserRole role : source.getRoles()) {
                if (role.getTeam().isMember(target)) {
                    if (role.getRole().equals(Role.ADMIN) && !role.getTeam().isAdmin(target)) {
                        UserRole targetRole = userRoleService.get(role.getTeam(), target).get();
                        targetRole.setRole(Role.ADMIN);
                        userRoleService.save(targetRole);
                    }
                } else {
                    userRoleService.save(new UserRole(role.getTeam(), target, role.getRole()));
                }
            }

            source.getRoles().clear();

            this.delete(source.getId());
            this.save(target);

            // TODO copy participations in trips and rides

        } catch (Exception e) {

        }

    }

    @Transactional
    public void delete(String userId) {
        log.info("Request user deletion {}", userId);
        get(userId).ifPresent(user -> {
            user.setDeletion(true);
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

    }


}