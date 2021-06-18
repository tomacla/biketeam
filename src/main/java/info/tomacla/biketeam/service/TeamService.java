package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.FileExtension;
import info.tomacla.biketeam.common.FileRepositories;
import info.tomacla.biketeam.common.ImageDescriptor;
import info.tomacla.biketeam.domain.team.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class TeamService {

    private static final Logger log = LoggerFactory.getLogger(TeamService.class);

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private FileService fileService;

    public Optional<Team> get(String teamId) {
        return teamRepository.findById(teamId);
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

    public void save(Team team) {
        log.info("Team is updated");

        TeamConfiguration teamConfiguration = team.getConfiguration();

        if ((teamConfiguration.getDefaultPage().equals(Page.FEED) && !teamConfiguration.isFeedVisible())
                || (teamConfiguration.getDefaultPage().equals(Page.RIDES) && !teamConfiguration.isRidesVisible())) {
            teamConfiguration.setDefaultPage(Page.MAPS);
        }

        teamRepository.save(team);
    }

    public List<Team> list() {
        return teamRepository.findAll();
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
}
