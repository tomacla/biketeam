package info.tomacla.biketeam.service.async;

import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.PublicationService;
import info.tomacla.biketeam.service.RideService;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.TripService;
import info.tomacla.biketeam.service.file.FileService;
import info.tomacla.biketeam.service.heatmap.HeatmapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * For CRON expression, order to use is "second, minute, hour, day of month, month, day(s) of week"
 */
@Service
public class ScheduledTaskService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskService.class);

    @Autowired
    private RideService rideService;

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private TripService tripService;

    @Autowired
    private FileService fileService;

    @Autowired
    private HeatmapService heatmapService;

    @Autowired
    private TeamService teamService;

    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void publicationTask() {
        log.info("Executing scheduled publicationTask");
        rideService.publishRides();
        publicationService.publishPublications();
        tripService.publishTrips();
    }

    @Scheduled(fixedRate = 300000, initialDelay = 15000)
    public void cleanTmpDirectory() {
        log.info("Executing scheduled cleanTmpDirectory");
        fileService.cleanTmpDirectory();
    }

    @Scheduled(cron = "0 0 2 * * 0")
    public void regenerateHeatmaps() {
        log.info("Executing scheduled regenerateHeatmaps");
        heatmapService.generateAll();
    }

    @Scheduled(fixedRate = 600000, initialDelay = 17000)
    public void cleanDeleteTeamsDirectory() {
        log.info("Executing scheduled cleanDeleteTeamsDirectory");

        final Set<String> existingTeamIds = teamService.list().stream().map(Team::getId).collect(Collectors.toSet());
        FileRepositories.list().forEach(directory -> {
            final List<String> storedTeamsIds = fileService.listSubDirectories(directory);
            storedTeamsIds.remove(directory);
            storedTeamsIds.forEach(teamId -> {
                try {
                    if (!existingTeamIds.contains(teamId)) {
                        log.info("Delete unused directory " + directory + "/" + teamId);
                        fileService.deleteDirectory(directory, teamId);
                    }
                } catch (Exception e) {
                    log.error("Error while executing cleanDeleteTeamsDirectory", e);
                }
            });
        });

    }

}