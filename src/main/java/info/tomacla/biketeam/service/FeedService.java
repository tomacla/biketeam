package info.tomacla.biketeam.service;

import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.publication.PublicationRepository;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.service.feed.FeedItem;
import info.tomacla.biketeam.service.feed.FeedItemType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FeedService {

    private final RideRepository rideRepository;
    private final PublicationRepository publicationRepository;

    @Autowired
    public FeedService(RideRepository rideRepository, PublicationRepository publicationRepository) {
        this.rideRepository = rideRepository;
        this.publicationRepository = publicationRepository;
    }


    public List<FeedItem> getFeed() {

        var result = new ArrayList<FeedItem>();

        rideRepository.findAllByPublishedAtLessThan(ZonedDateTime.now()).stream().map(this::convertRide).forEach(result::add);
        publicationRepository.findAllByPublishedAtLessThan(ZonedDateTime.now()).stream().map(this::convertPublication).forEach(result::add);

        result.sort((f1, f2) -> f2.getDate().compareTo(f1.getDate()));
        return result;

    }

    private FeedItem convertRide(Ride ride) {
        return new FeedItem(
                ride.getId(),
                FeedItemType.RIDE,
                ride.getDate(),
                ride.getTitle(),
                ride.getDescription(),
                "/rides/" + ride.getId(),
                ride.isImaged() ? "/api/rides/" + ride.getId() + "/image" : null
        );
    }

    private FeedItem convertPublication(Publication publication) {
        return new FeedItem(
                publication.getId(),
                FeedItemType.PUBLICATION,
                publication.getPostedAt().toLocalDate(),
                publication.getTitle(),
                publication.getContent(),
                null,
                publication.isImaged() ? "/api/publications/" + publication.getId() + "/image" : null
        );
    }

}
