package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.PublishedStatus;
import info.tomacla.biketeam.domain.publication.PublicationRepository;
import info.tomacla.biketeam.domain.ride.RideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
public class TaskService {

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private ConfigurationService configurationService;

    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void reportCurrentTime() {

        rideRepository.findAllByPublishedStatusAndPublishedAtLessThan(PublishedStatus.UNPUBLISHED, ZonedDateTime.now(configurationService.getTimezone())).forEach(ride -> {
            ride.setPublishedStatus(PublishedStatus.PUBLISHED);
            rideRepository.save(ride);
        });

        publicationRepository.findAllByPublishedStatusAndPublishedAtLessThan(PublishedStatus.UNPUBLISHED, ZonedDateTime.now(configurationService.getTimezone())).forEach(pub -> {
            pub.setPublishedStatus(PublishedStatus.PUBLISHED);
            publicationRepository.save(pub);
        });
    }

}
