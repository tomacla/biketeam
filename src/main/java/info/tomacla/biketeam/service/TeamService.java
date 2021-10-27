package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.Country;
import info.tomacla.biketeam.common.FileExtension;
import info.tomacla.biketeam.common.FileRepositories;
import info.tomacla.biketeam.common.ImageDescriptor;
import info.tomacla.biketeam.domain.feed.Feed;
import info.tomacla.biketeam.domain.feed.FeedRepository;
import info.tomacla.biketeam.domain.team.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeamService {

    private static final Logger log = LoggerFactory.getLogger(TeamService.class);

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private FeedRepository feedRepository;

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

    public List<Feed> listFeed(String teamId) {
        return feedRepository.findAllByTeamIdAndPublishedAtLessThan(
                teamId,
                ZonedDateTime.now(ZoneOffset.UTC), // TODO should be user timezone and not UTC
                PageRequest.of(0, 15, Sort.by("publishedAt").descending())).getContent();
    }

    public List<Feed> listFeed(Set<String> teamIds) {
        return feedRepository.findAllByTeamIdInAndPublishedAtLessThan(
                teamIds,
                ZonedDateTime.now(ZoneOffset.UTC), // TODO should be user timezone and not UTC
                PageRequest.of(0, 15, Sort.by("publishedAt").descending())).getContent();
    }

    public void save(Team team) {
        log.info("Team is updated");

        TeamConfiguration teamConfiguration = team.getConfiguration();

        if ((teamConfiguration.getDefaultPage().equals(WebPage.FEED) && !teamConfiguration.isFeedVisible())
                || (teamConfiguration.getDefaultPage().equals(WebPage.RIDES) && !teamConfiguration.isRidesVisible())
                || (teamConfiguration.getDefaultPage().equals(WebPage.TRIPS) && !teamConfiguration.isTripsVisible())) {
            teamConfiguration.setDefaultPage(WebPage.MAPS);
        }

        teamRepository.save(team);
    }

    public List<Team> list() {
        return teamRepository.findAll();
    }

    public List<Team> getLast4() {
        return teamRepository.findAll(PageRequest.of(0, 4, Sort.by("createdAt").descending())).getContent();
    }

    public void initTeamImage(Team newTeam) {
        fileService.store(getClass().getResourceAsStream("/static/css/biketeam-logo.png"),
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
            fileService.store(newImage, FileRepositories.MISC_IMAGES, teamId, "logo" + optionalFileExtension.get().getExtension());
        }
    }

    public void deleteImage(String teamId) {
        getImage(teamId).ifPresent(image ->
                fileService.delete(FileRepositories.MISC_IMAGES, teamId, "logo" + image.getExtension().getExtension())
        );
    }

    public Optional<ImageDescriptor> getImage(String teamId) {

        Optional<FileExtension> fileExtensionExists = fileService.exists(FileRepositories.MISC_IMAGES, teamId, "logo", FileExtension.byPriority());

        if (fileExtensionExists.isPresent()) {

            final FileExtension extension = fileExtensionExists.get();
            final Path path = fileService.get(FileRepositories.MISC_IMAGES, teamId, "logo" + extension.getExtension());

            return Optional.of(ImageDescriptor.of(extension, path));

        }

        return Optional.empty();

    }

    public boolean idExists(String id) {
        return get(id).isPresent();
    }

    public Page<Team> searchTeams(int page, int pageSize, String name, String city, Country country) {

        Sort sort = Sort.by("name").ascending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        SearchTeamSpecification spec = new SearchTeamSpecification(
                name,
                city,
                country
        );

        return teamRepository.findAll(spec, pageable);

    }

    public Map<String, String> findAllTeamWithDomain() {
        return teamRepository.findAllTeamWithDomain()
                .stream()
                .collect(Collectors.toMap(TeamIdDomainProjection::getId, TeamIdDomainProjection::getDomain));
    }

}
