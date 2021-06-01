package info.tomacla.biketeam.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    @Autowired
    private RideService rideService;

    @Autowired
    private PublicationService publicationService;

    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void publicationTask() {
        rideService.publishRides();
        publicationService.publishPublications();
    }

}
