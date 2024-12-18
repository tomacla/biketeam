package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.amqp.Exchanges;
import info.tomacla.biketeam.common.amqp.Queues;
import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.trip.SearchTripSpecification;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.domain.trip.TripRepository;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.amqp.BrokerService;
import info.tomacla.biketeam.service.amqp.dto.TeamEntityDTO;
import info.tomacla.biketeam.service.file.FileService;
import info.tomacla.biketeam.service.file.ThumbnailService;
import info.tomacla.biketeam.service.image.ImageService;
import info.tomacla.biketeam.service.permalink.AbstractPermalinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TripService extends AbstractPermalinkService {

    private static final Logger log = LoggerFactory.getLogger(TripService.class);

    private TripRepository tripRepository;

    private TeamService teamService;

    private BrokerService brokerService;
    private ImageService imageService;

    @Autowired
    public TripService(FileService fileService, TripRepository tripRepository, TeamService teamService, BrokerService brokerService, ThumbnailService thumbnailService) {
        this.tripRepository = tripRepository;
        this.teamService = teamService;
        this.brokerService = brokerService;
        this.imageService = new ImageService(FileRepositories.TRIP_IMAGES, fileService, thumbnailService);
    }

    public Optional<Trip> get(String teamId, String tripIdOrPermalink) {

        Optional<Trip> optionalTrip = tripRepository.findById(tripIdOrPermalink);
        if (optionalTrip.isPresent() && optionalTrip.get().getTeamId().equals(teamId)) {
            return optionalTrip;
        }

        optionalTrip = findByPermalink(tripIdOrPermalink);
        if (optionalTrip.isPresent() && optionalTrip.get().getTeamId().equals(teamId)) {
            return optionalTrip;
        }


        return Optional.empty();
    }


    @RabbitListener(queues = Queues.TASK_PUBLISH_TRIPS)
    public void publishTrips() {
        teamService.list().forEach(team -> {

                    SearchTripSpecification spec = new SearchTripSpecification(
                            false, null, null,
                            null, null, Set.of(team.getId()),
                            PublishedStatus.UNPUBLISHED, null, ZonedDateTime.now(team.getZoneId()),
                            null, null, null);

                    tripRepository.findAll(spec).forEach(trip -> {
                        log.info("Publishing trip {} for team {}", trip.getId(), team.getId());
                        trip.setPublishedStatus(PublishedStatus.PUBLISHED);
                        save(trip);
                        if (trip.isListedInFeed()) {
                            brokerService.sendToBroker(Exchanges.PUBLISH_TRIP, TeamEntityDTO.valueOf(trip.getTeamId(), trip.getId()));
                        }
                    });
                }
        );

    }

    @Transactional
    public Trip save(Trip trip) {
        return tripRepository.save(trip);
    }

    @Transactional
    public void delete(String teamId, String tripId) {
        log.info("Request trip deletion {}", tripId);
        get(teamId, tripId).ifPresent(trip -> {
            trip.setDeletion(true);
            save(trip);
        });
    }

    public Page<Trip> listTrips(String teamId, String title, int page, int pageSize) {
        return tripRepository.findAll(SearchTripSpecification.byTitleInTeam(teamId, title), PageRequest.of(page, pageSize, Sort.by("publishedAt").descending()));
    }


    public Page<Trip> searchTrips(Set<String> teamIds, LocalDate from, LocalDate to, Boolean listedInFeed, Boolean publishToCatalog, int page, int pageSize) {

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("startDate").descending());

        SearchTripSpecification spec = new SearchTripSpecification(
                false, null, null, listedInFeed, null, teamIds, PublishedStatus.PUBLISHED, null, null,
                from, to, publishToCatalog);

        return tripRepository.findAll(spec, pageable);
    }

    public List<Trip> searchUpcomingTripsByUser(User user, Set<String> teamIds, LocalDate from, LocalDate to) {
        Pageable pageable = PageRequest.of(0, 100, Sort.by("startDate").descending());
        SearchTripSpecification spec = SearchTripSpecification.upcomingByUser(user, teamIds, from, to);
        return tripRepository.findAll(spec, pageable).getContent();

    }

    public Optional<ImageDescriptor> getImage(String teamId, String tripId) {
        final Optional<Trip> optionalTrip = get(teamId, tripId);
        if (optionalTrip.isPresent()) {
            return imageService.get(teamId, tripId);
        }
        return Optional.empty();
    }

    public void saveImage(String teamId, String tripId, InputStream is, String fileName) {
        try {
            imageService.save(teamId, tripId, is, fileName);
        } catch (IOException e) {
            throw new RuntimeException("Unable to save image", e);
        }
    }

    public void deleteImage(String teamId, String tripId) {
        imageService.delete(teamId, tripId);
    }

    public Optional<Trip> findByPermalink(String permalink) {
        return tripRepository.findOne(SearchTripSpecification.byPermalink(permalink));
    }

    public Optional<Trip> findById(String id) {
        return tripRepository.findById(id);
    }

    @Override
    public boolean permalinkExists(String permalink) {
        return findByPermalink(permalink).isPresent();
    }

    public void removeParticipant(String userId) {
        tripRepository.removeParticipant(userId);
    }


}
