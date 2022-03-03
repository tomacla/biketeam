package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.data.Country;
import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.feed.Feed;
import info.tomacla.biketeam.domain.feed.FeedRepository;
import info.tomacla.biketeam.domain.feed.FeedSorter;
import info.tomacla.biketeam.domain.team.*;
import info.tomacla.biketeam.domain.userrole.Role;
import info.tomacla.biketeam.service.file.FileService;
import info.tomacla.biketeam.service.heatmap.HeatmapService;
import info.tomacla.biketeam.service.permalink.AbstractPermalinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
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
    private FeedRepository feedRepository;

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
        return teamRepository.findByRoles_UserIdAndRoles_RoleIn(userId, Set.of(Role.ADMIN, Role.MEMBER));
    }

    public List<Feed> listFeed(Team team) {
        return this.listFeed(Set.of(team.getId()), ZoneId.of(team.getConfiguration().getTimezone()));
    }

    public List<Feed> listFeed(Set<String> teamIds, ZoneId zoneId) {
        return feedRepository.findAllByTeamIdInAndPublishedAtLessThan(
                teamIds,
                ZonedDateTime.now(zoneId),
                PageRequest.of(0, 10, Sort.by("publishedAt").descending())).getContent()
                .stream().sorted(FeedSorter.get(zoneId)).collect(Collectors.toList());
    }

    public void save(Team team) {
        this.save(team, false);
    }

    @Transactional
    public void save(Team team, boolean newTeam) {
        log.info("Team is updated");

        TeamConfiguration teamConfiguration = team.getConfiguration();

        if ((teamConfiguration.getDefaultPage().equals(WebPage.FEED) && !teamConfiguration.isFeedVisible())
                || (teamConfiguration.getDefaultPage().equals(WebPage.RIDES) && !teamConfiguration.isRidesVisible())
                || (teamConfiguration.getDefaultPage().equals(WebPage.TRIPS) && !teamConfiguration.isTripsVisible())) {
            teamConfiguration.setDefaultPage(WebPage.MAPS);
        }

        teamRepository.save(team);

        if (newTeam) {
            this.initTeamImage(team);
        }

        if (team.getIntegration().isHeatmapConfigured()) {
            heatmapService.generateHeatmap(team);
        }

    }

    public List<Team> list() {
        return teamRepository.findAll();
    }

    public List<Team> getLast4() {
        return teamRepository.findByVisibilityIn(List.of(Visibility.PUBLIC, Visibility.PRIVATE),
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

    public Map<String, String> findAllTeamWithDomain() {
        return teamRepository.findAllTeamWithDomain()
                .stream()
                .collect(Collectors.toMap(TeamProjection::getId, TeamProjection::getDomain));
    }

    public void delete(String teamId) {
        get(teamId).ifPresent(this::delete);
    }

    @Transactional
    public void delete(Team team) {
        try {
            log.info("Request team deletion {}", team.getId());
            // delete all elements
            rideTemplateService.deleteByTeam(team.getId());
            rideService.deleteByTeam(team.getId());
            publicationService.deleteByTeam(team.getId());
            tripService.deleteByTeam(team.getId());
            mapService.deleteByTeam(team.getId());
            // remove all access to this team
            userRoleService.deleteByTeam(team.getId());
            // finaly delete the team
            teamRepository.deleteById(team.getId());
            log.info("Team deleted {}", team.getId());
        } catch (Exception e) {
            log.error("Unable to delete team " + team.getId(), e);
        }
    }

    @Override
    public boolean permalinkExists(String permalink) {
        return get(permalink).isPresent();
    }

}
