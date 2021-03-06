package info.tomacla.biketeam.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private RideService rideService;

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private FileService fileService;

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

}
