package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.amqp.Queues;
import info.tomacla.biketeam.common.data.Country;
import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.feed.FeedEntity;
import info.tomacla.biketeam.domain.feed.FeedOptions;
import info.tomacla.biketeam.domain.feed.FeedSorter;
import info.tomacla.biketeam.domain.team.*;
import info.tomacla.biketeam.domain.userrole.Role;
import info.tomacla.biketeam.service.amqp.BrokerService;
import info.tomacla.biketeam.service.file.FileService;
import info.tomacla.biketeam.service.heatmap.HeatmapService;
import info.tomacla.biketeam.service.permalink.AbstractPermalinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeamService extends AbstractPermalinkService {

    private static final Logger log = LoggerFactory.getLogger(TeamService.class);

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private HeatmapService heatmapService;

    @Autowired
    private RideService rideService;

    @Autowired
    private RideTemplateService rideTemplateService;

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private TripService tripService;

    @Autowired
    private MapService mapService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private PlaceService placeService;

    @Autowired
    private BrokerService brokerService;

    public Optional<Team> get(String teamId) {
        return teamRepository.findById(teamId.toLowerCase());
    }

    public TeamConfiguration getConfiguration(String teamId) {
        return get(teamId).orElseThrow(() -> new IllegalArgumentException("Unknown team ID")).getConfiguration();
    }

    public TeamIntegration getIntegration(String teamId) {
        return get(teamId).orElseThrow(() -> new IllegalArgumentException("Unknown team ID")).getIntegration();
    }

    public TeamDescription getDescription(String teamId) {
        return get(teamId).orElseThrow(() -> new IllegalArgumentException("Unknown team ID")).getDescription();
    }

    public List<Team> getUserTeams(String userId) {
        return teamRepository.findAllByDeletionAndRoles_UserIdAndRoles_RoleIn(false, userId, Set.of(Role.ADMIN, Role.MEMBER));
    }

    public List<FeedEntity> listFeed(Team team, FeedOptions options) {
        return this.listFeed(Set.of(team.getId()), ZoneId.of(team.getConfiguration().getTimezone()), options);
    }

    public List<FeedEntity> listFeed(Set<String> teamIds, ZoneId zoneId, FeedOptions options) {

        List<FeedEntity> result = new ArrayList<>();

        // TODO parallel requests to database
        if (options.isIncludePublications()) {
            result.addAll(publicationService.searchPublications(teamIds, 0, 10,
                    ZonedDateTime.of(options.getFrom(), LocalTime.MIDNIGHT, zoneId),
                    ZonedDateTime.of(options.getTo(), LocalTime.MIDNIGHT, zoneId)
            ).getContent());
        }
        if (options.isIncludeRides()) {
            result.addAll(rideService.searchRides(teamIds, 0, 10, options.getFrom(), options.getTo(), true).getContent());
        }
        if (options.isIncludeTrips()) {
            result.addAll(tripService.searchTrips(teamIds, 0, 10, options.getFrom(), options.getTo(), true).getContent());
        }

        result = result.stream().sorted(FeedSorter.get(zoneId)).collect(Collectors.toList());

        return result;
    }

    public void save(Team team) {
        this.save(team, false);
    }

    @Transactional
    public void save(Team team, boolean newTeam) {
        log.info("Team is updated");

        TeamConfiguration teamConfiguration = team.getConfiguration();

        teamRepository.save(team);

        if (newTeam) {
            this.initTeamImage(team);
        }

        if (team.getIntegration().isHeatmapConfigured()) {
            heatmapService.generateHeatmap(team);
        }

    }

    public List<Team> list() {
        return teamRepository.findAllByDeletion(false);
    }

    public List<Team> getLast4() {
        return teamRepository.findAllByDeletionAndVisibilityIn(false, List.of(Visibility.PUBLIC, Visibility.PRIVATE),
                PageRequest.of(0, 4, Sort.by("createdAt").descending())).getContent();
    }

    public void initTeamImage(Team newTeam) {
        fileService.storeFile(getClass().getResourceAsStream("/static/css/biketeam-logo.png"),
                FileRepositories.MISC_IMAGES,
                newTeam.getId(),
                "logo.png"
        );
    }

    public void saveImage(String teamId, InputStream is, String fileName) {
        Optional<FileExtension> optionalFileExtension = FileExtension.findByFileName(fileName);
        if (optionalFileExtension.isPresent()) {
            deleteImage(teamId);
            Path newImage = fileService.getTempFileFromInputStream(is);
            fileService.storeFile(newImage, FileRepositories.MISC_IMAGES, teamId, "logo" + optionalFileExtension.get().getExtension());
        }
    }

    public void deleteImage(String teamId) {
        getImage(teamId).ifPresent(image ->
                fileService.deleteFile(FileRepositories.MISC_IMAGES, teamId, "logo" + image.getExtension().getExtension())
        );
    }

    public Optional<ImageDescriptor> getImage(String teamId) {

        Optional<FileExtension> fileExtensionExists = fileService.fileExists(FileRepositories.MISC_IMAGES, teamId, "logo", FileExtension.byPriority());

        if (fileExtensionExists.isPresent()) {

            final FileExtension extension = fileExtensionExists.get();
            final Path path = fileService.getFile(FileRepositories.MISC_IMAGES, teamId, "logo" + extension.getExtension());

            return Optional.of(ImageDescriptor.of(extension, path));

        }

        return Optional.empty();

    }

    public Page<Team> searchTeams(int page, int pageSize, String name, String city, Country country) {
        return this.searchTeams(page, pageSize, name, city, country, Sort.Order.asc("name").ignoreCase());
    }

    public Page<Team> searchTeams(int page, int pageSize, String name, String city, Country country, Sort.Order sortOrder) {

        Sort sort = Sort.by(sortOrder);
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        SearchTeamSpecification spec = new SearchTeamSpecification(
                name,
                city,
                country,
                List.of(Visibility.PUBLIC, Visibility.PRIVATE));

        return teamRepository.findAll(spec, pageable);

    }

    public void delete(String teamId) {
        get(teamId).ifPresent(team -> {
            team.setDeletion(true);
            save(team);
        });
    }


    @Override
    public boolean permalinkExists(String permalink) {
        return get(permalink).isPresent();
    }

    @RabbitListener(queues = Queues.TASK_CLEAN_TEAM_FILES)
    public void cleanTeamFiles() {
        final Set<String> existingTeamIds = list().stream().map(Team::getId).collect(Collectors.toSet());
        fileService.cleanTeamFiles(existingTeamIds);
    }

}
