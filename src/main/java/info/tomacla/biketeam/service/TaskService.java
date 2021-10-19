package info.tomacla.biketeam.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    // second, minute, hour, day of month, month, day(s) of week

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private RideService rideService;

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private FileService fileService;

    @Autowired
    private HeatmapService heatmapService;

    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void publicationTask() {
        log.info("Executing scheduled publicationTask");
        rideService.publishRides();
        publicationService.publishPublications();
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

}
