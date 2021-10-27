package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.FileExtension;
import info.tomacla.biketeam.common.FileRepositories;
import info.tomacla.biketeam.common.ImageDescriptor;
import info.tomacla.biketeam.common.PublishedStatus;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.domain.trip.TripIdTitleDateProjection;
import info.tomacla.biketeam.domain.trip.TripRepository;
import info.tomacla.biketeam.service.externalpublication.ExternalPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TripService {

    private static final Logger log = LoggerFactory.getLogger(TripService.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private TeamService teamService;

    @Autowired
    private ExternalPublisher externalPublisher;

    public Optional<ImageDescriptor> getImage(String teamId, String tripId) {

        Optional<FileExtension> fileExtensionExists = fileService.exists(FileRepositories.TRIP_IMAGES, teamId, tripId, FileExtension.byPriority());

        if (fileExtensionExists.isPresent()) {

            final FileExtension extension = fileExtensionExists.get();
            final Path path = fileService.get(FileRepositories.TRIP_IMAGES, teamId, tripId + extension.getExtension());

            return Optional.of(ImageDescriptor.of(extension, path));

        }

        return Optional.empty();

    }

    public void publishTrips() {
        teamService.list().forEach(team ->
                tripRepository.findAllByTeamIdAndPublishedStatusAndPublishedAtLessThan(
                        team.getId(),
                        PublishedStatus.UNPUBLISHED,
                        ZonedDateTime.now(team.getZoneId())
                ).forEach(trip -> {
                    log.info("Publishing trip {} for team {}", trip.getId(), team.getId());
                    trip.setPublishedStatus(PublishedStatus.PUBLISHED);
                    save(trip);
                    externalPublisher.publish(team, trip);
                })
        );

    }

    public void save(Trip trip) {
        tripRepository.save(trip);
    }

    public List<TripIdTitleDateProjection> listTrips(String teamId) {
        return tripRepository.findAllByTeamIdOrderByStartDateDesc(teamId);
    }

    public Optional<Trip> get(String teamId, String tripId) {
        final Optional<Trip> optionalTrip = tripRepository.findById(tripId);
        if (optionalTrip.isPresent() && optionalTrip.get().getTeamId().equals(teamId)) {
            return optionalTrip;
        }
        return Optional.empty();
    }

    public void delete(String teamId, String tripId) {
        log.info("Request trip deletion {}", tripId);
        final Optional<Trip> optionalTrip = get(teamId, tripId);
        if (optionalTrip.isPresent()) {
            final Trip trip = optionalTrip.get();
            deleteImage(trip.getTeamId(), trip.getId());
            tripRepository.delete(trip);
        }
    }

    public void removeMapIdInStages(String mapId) {
        tripRepository.removeMapIdInStages(mapId);
    }

    public void changeMapIdInStages(String oldMapId, String newMapId) {
        tripRepository.updateMapIdInStages(oldMapId, newMapId);
    }

    public Page<Trip> searchTrips(String teamId, int page, int pageSize,
                                  LocalDate from, LocalDate to) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("startDate").descending());
        return tripRepository.findByTeamIdAndStartDateBetweenAndPublishedStatus(
                teamId,
                from,
                to,
                PublishedStatus.PUBLISHED,
                pageable);
    }

    public void saveImage(String teamId, String tripId, InputStream is, String fileName) {
        Optional<FileExtension> optionalFileExtension = FileExtension.findByFileName(fileName);
        if (optionalFileExtension.isPresent()) {
            this.deleteImage(teamId, tripId);
            Path newImage = fileService.getTempFileFromInputStream(is);
            fileService.store(newImage, FileRepositories.TRIP_IMAGES, teamId, tripId + optionalFileExtension.get().getExtension());
        }
    }

    public void deleteImage(String teamId, String tripId) {
        getImage(teamId, tripId).ifPresent(image ->
                fileService.delete(FileRepositories.TRIP_IMAGES, teamId, tripId + image.getExtension().getExtension())
        );
    }

}
