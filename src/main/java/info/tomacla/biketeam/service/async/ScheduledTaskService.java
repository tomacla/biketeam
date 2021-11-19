package info.tomacla.biketeam.service.async;

import info.tomacla.biketeam.service.PublicationService;
import info.tomacla.biketeam.service.RideService;
import info.tomacla.biketeam.service.TripService;
import info.tomacla.biketeam.service.file.FileService;
import info.tomacla.biketeam.service.heatmap.HeatmapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
        // FIXME implement list of folders and delete if team doest not exists

        /*FileRepositories.list().forEach(directory -> {
            try {
                FileUtils.deleteDirectory(fileService.getDirectory(directory, teamId).toFile());
            } catch (IOException e) {
                log.error("Unable to delete directory {}/{}", directory, teamId);
            }
        });*/

    }

}