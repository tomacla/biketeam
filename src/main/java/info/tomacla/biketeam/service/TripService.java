package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.amqp.Exchanges;
import info.tomacla.biketeam.common.amqp.Queues;
import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.domain.trip.TripIdTitleDateProjection;
import info.tomacla.biketeam.domain.trip.TripRepository;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.amqp.BrokerService;
import info.tomacla.biketeam.service.amqp.dto.TeamEntityDTO;
import info.tomacla.biketeam.service.file.FileService;
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

import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TripService extends AbstractPermalinkService {

    private static final Logger log = LoggerFactory.getLogger(TripService.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private TeamService teamService;

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private ReactionService reactionService;

    public Optional<Trip> get(String teamId, String tripId) {
        Optional<Trip> optionalTrip = tripRepository.findById(tripId);
        if (optionalTrip.isPresent() && optionalTrip.get().getTeamId().equals(teamId)) {
            return optionalTrip;
        }

        optionalTrip = tripRepository.findByPermalink(tripId);
        if (optionalTrip.isPresent() && optionalTrip.get().getTeamId().equals(teamId)) {
            return optionalTrip;
        }


        return Optional.empty();
    }

    public Optional<ImageDescriptor> getImage(String teamId, String tripId) {

        final Optional<Trip> optionalTrip = get(teamId, tripId);

        if (optionalTrip.isPresent()) {

            final Trip trip = optionalTrip.get();

            Optional<FileExtension> fileExtensionExists = fileService.fileExists(FileRepositories.TRIP_IMAGES, teamId, trip.getId(), FileExtension.byPriority());

            if (fileExtensionExists.isPresent()) {

                final FileExtension extension = fileExtensionExists.get();
                final Path path = fileService.getFile(FileRepositories.TRIP_IMAGES, teamId, trip.getId() + extension.getExtension());

                return Optional.of(ImageDescriptor.of(extension, path));

            }

        }

        return Optional.empty();

    }

    @RabbitListener(queues = Queues.TASK_PUBLISH_TRIPS)
    public void publishTrips() {
        teamService.list().forEach(team ->
                tripRepository.findAllByDeletionAndTeamIdAndPublishedStatusAndPublishedAtLessThan(
                        false,
                        team.getId(),
                        PublishedStatus.UNPUBLISHED,
                        ZonedDateTime.now(team.getZoneId())
                ).forEach(trip -> {
                    log.info("Publishing trip {} for team {}", trip.getId(), team.getId());
                    trip.setPublishedStatus(PublishedStatus.PUBLISHED);
                    save(trip);
                    if (trip.isListedInFeed()) {
                        brokerService.sendToBroker(Exchanges.PUBLISH_TRIP, TeamEntityDTO.valueOf(trip.getTeamId(), trip.getId()));
                    }
                })
        );

    }

    @Transactional
    public void save(Trip trip) {
        tripRepository.save(trip);
    }

    public List<TripIdTitleDateProjection> listTrips(String teamId) {
        return tripRepository.findAllByDeletionAndTeamIdOrderByStartDateDesc(false, teamId);
    }


    public Page<Trip> searchTrips(Set<String> teamIds, int page, int pageSize,
                                  LocalDate from, LocalDate to, boolean listedInFeed) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("startDate").descending());
        return tripRepository.findAllByDeletionAndListedInFeedAndTeamIdInAndStartDateBetweenAndPublishedStatus(
                false,
                listedInFeed,
                teamIds,
                from,
                to,
                PublishedStatus.PUBLISHED,
                pageable);
    }

    public List<Trip> searchTripsByUser(User connectedUser, LocalDate from) {
        return tripRepository.findAllByDeletionAndParticipants_IdAndEndDateGreaterThanAndPublishedStatus(
                false,
                connectedUser.getId(),
                from,
                PublishedStatus.PUBLISHED
        );
    }

    public void saveImage(String teamId, String tripId, InputStream is, String fileName) {
        Optional<FileExtension> optionalFileExtension = FileExtension.findByFileName(fileName);
        if (optionalFileExtension.isPresent()) {
            this.deleteImage(teamId, tripId);
            Path newImage = fileService.getTempFileFromInputStream(is);
            fileService.storeFile(newImage, FileRepositories.TRIP_IMAGES, teamId, tripId + optionalFileExtension.get().getExtension());
        }
    }

    public void deleteImage(String teamId, String tripId) {
        getImage(teamId, tripId).ifPresent(image ->
                fileService.deleteFile(FileRepositories.TRIP_IMAGES, teamId, tripId + image.getExtension().getExtension())
        );
    }

    public boolean permalinkExists(String permalink) {
        return tripRepository.findByPermalink(permalink).isPresent();
    }

    public void deleteByTeam(String teamId) {
        tripRepository.findAllByDeletionAndTeamIdOrderByStartDateDesc(false, teamId).stream().map(TripIdTitleDateProjection::getId).forEach(tripRepository::deleteById);
    }

    public void removeParticipant(String userId) {
        tripRepository.removeParticipant(userId);
    }

    public void delete(String teamId, String tripId) {
        log.info("Request trip deletion {}", tripId);
        get(teamId, tripId).ifPresent(trip -> {
            trip.setDeletion(true);
            save(trip);
        });
    }


}
