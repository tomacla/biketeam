package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.amqp.Exchanges;
import info.tomacla.biketeam.common.amqp.Queues;
import info.tomacla.biketeam.common.amqp.RoutingKeys;
import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideProjection;
import info.tomacla.biketeam.domain.ride.RideRepository;
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

@Service
public class RideService extends AbstractPermalinkService {

    private static final Logger log = LoggerFactory.getLogger(RideService.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private TeamService teamService;

    @Autowired
    private BrokerService brokerService;

    public Optional<ImageDescriptor> getImage(String teamId, String rideId) {

        final Optional<Ride> optionalRide = get(teamId, rideId);
        if (optionalRide.isPresent()) {

            final Ride ride = optionalRide.get();

            Optional<FileExtension> fileExtensionExists = fileService.fileExists(FileRepositories.RIDE_IMAGES, teamId, ride.getId(), FileExtension.byPriority());

            if (fileExtensionExists.isPresent()) {

                final FileExtension extension = fileExtensionExists.get();
                final Path path = fileService.getFile(FileRepositories.RIDE_IMAGES, teamId, ride.getId() + extension.getExtension());

                return Optional.of(ImageDescriptor.of(extension, path));

            }

        }

        return Optional.empty();

    }

    @RabbitListener(queues = Queues.TASK_PUBLISH_RIDES)
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
                    brokerService.sendToBroker(Exchanges.EVENT, RoutingKeys.RIDE_PUBLISHED,
                            TeamEntityDTO.valueOf(ride.getTeamId(), ride.getId()));
                })
        );

    }

    @Transactional
    public void save(Ride ride) {
        rideRepository.save(ride);
    }

    public List<RideProjection> listRides(String teamId) {
        return rideRepository.findAllByTeamIdOrderByDateDesc(teamId);
    }

    public Optional<Ride> get(String teamId, String rideId) {

        Optional<Ride> optionalRide = rideRepository.findById(rideId);
        if (optionalRide.isPresent() && optionalRide.get().getTeamId().equals(teamId)) {
            return optionalRide;
        }

        optionalRide = rideRepository.findByPermalink(rideId);
        if (optionalRide.isPresent() && optionalRide.get().getTeamId().equals(teamId)) {
            return optionalRide;
        }

        return Optional.empty();

    }

    @Transactional
    public void delete(String teamId, String rideId) {
        log.info("Request ride deletion {}", rideId);
        final Optional<Ride> optionalRide = get(teamId, rideId);
        if (optionalRide.isPresent()) {
            final Ride ride = optionalRide.get();
            deleteImage(ride.getTeamId(), ride.getId());
            rideRepository.delete(ride);
        }
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
            fileService.storeFile(newImage, FileRepositories.RIDE_IMAGES, teamId, rideId + optionalFileExtension.get().getExtension());
        }
    }

    public void deleteImage(String teamId, String rideId) {
        getImage(teamId, rideId).ifPresent(image ->
                fileService.deleteFile(FileRepositories.RIDE_IMAGES, teamId, rideId + image.getExtension().getExtension())
        );
    }

    public boolean permalinkExists(String permalink) {
        return rideRepository.findByPermalink(permalink).isPresent();
    }

    public void deleteByTeam(String teamId) {
        rideRepository.findAllByTeamIdOrderByDateDesc(teamId).stream().map(RideProjection::getId).forEach(rideRepository::deleteById);
    }

    public void deleteByUser(String userId) {

        rideRepository.deleteByUserId(userId);
    }
}
