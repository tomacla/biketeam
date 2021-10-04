package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.FileExtension;
import info.tomacla.biketeam.common.FileRepositories;
import info.tomacla.biketeam.common.ImageDescriptor;
import info.tomacla.biketeam.common.PublishedStatus;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideIdTitleDateProjection;
import info.tomacla.biketeam.domain.ride.RideRepository;
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
public class RideService {

    private static final Logger log = LoggerFactory.getLogger(RideService.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private FacebookService facebookService;

    @Autowired
    private MailService mailService;

    @Autowired
    private TeamService teamService;

    public Optional<ImageDescriptor> getImage(String teamId, String rideId) {

        Optional<FileExtension> fileExtensionExists = fileService.exists(FileRepositories.RIDE_IMAGES, teamId, rideId, FileExtension.byPriority());

        if (fileExtensionExists.isPresent()) {

            final FileExtension extension = fileExtensionExists.get();
            final Path path = fileService.get(FileRepositories.RIDE_IMAGES, teamId, rideId + extension.getExtension());

            return Optional.of(ImageDescriptor.of(extension, path));

        }

        return Optional.empty();

    }

    public void publishRides() {
        teamService.list().forEach(team ->
                rideRepository.findAllByTeamIdAndPublishedStatusAndPublishedAtLessThan(
                        team.getId(),
                        PublishedStatus.UNPUBLISHED,
                        ZonedDateTime.now(team.getZoneId())
                ).forEach(ride -> {
                    log.info("Publishing ride {} for team {}", ride.getId(), team.getId());
                    ride.setPublishedStatus(PublishedStatus.PUBLISHED);
                    save(ride);
                    facebookService.publish(team, ride);
                    mailService.publish(team, ride);
                })
        );

    }

    public void save(Ride ride) {
        rideRepository.save(ride);
    }

    public List<RideIdTitleDateProjection> listRides(String teamId) {
        return rideRepository.findAllByTeamIdOrderByDateDesc(teamId);
    }

    public Optional<Ride> get(String teamId, String rideId) {
        final Optional<Ride> optionalRide = rideRepository.findById(rideId);
        if (optionalRide.isPresent() && optionalRide.get().getTeamId().equals(teamId)) {
            return optionalRide;
        }
        return Optional.empty();
    }

    public void delete(String teamId, String rideId) {
        log.info("Request ride deletion {}", rideId);
        final Optional<Ride> optionalRide = get(teamId, rideId);
        if (optionalRide.isPresent()) {
            final Ride ride = optionalRide.get();
            deleteImage(ride.getTeamId(), ride.getId());
            rideRepository.delete(ride);
        }
    }

    public void removeMapIdInGroups(String mapId) {
        rideRepository.removeMapIdInGroups(mapId);
    }

    public void changeMapIdInGroups(String oldMapId, String newMapId) {
        rideRepository.updateMapIdInGroups(oldMapId, newMapId);
    }

    public Page<Ride> searchRides(String teamId, int page, int pageSize,
                                  LocalDate from, LocalDate to) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("date").descending());
        return rideRepository.findByTeamIdAndDateBetweenAndPublishedStatus(
                teamId,
                from,
                to,
                PublishedStatus.PUBLISHED,
                pageable);
    }

    public void saveImage(String teamId, String rideId, InputStream is, String fileName) {
        Optional<FileExtension> optionalFileExtension = FileExtension.findByFileName(fileName);
        if (optionalFileExtension.isPresent()) {
            this.deleteImage(teamId, rideId);
            Path newImage = fileService.getTempFileFromInputStream(is);
            fileService.store(newImage, FileRepositories.RIDE_IMAGES, teamId, rideId + optionalFileExtension.get().getExtension());
        }
    }

    public void deleteImage(String teamId, String rideId) {
        getImage(teamId, rideId).ifPresent(image ->
                fileService.delete(FileRepositories.RIDE_IMAGES, teamId, rideId + image.getExtension().getExtension())
        );
    }

}
