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
    private ConfigurationService configurationService;

    public Optional<ImageDescriptor> getImage(String rideId) {

        Optional<FileExtension> fileExtensionExists = fileService.exists(FileRepositories.RIDE_IMAGES, rideId, FileExtension.byPriority());

        if (fileExtensionExists.isPresent()) {

            final FileExtension extension = fileExtensionExists.get();
            final Path path = fileService.get(FileRepositories.RIDE_IMAGES, rideId + extension.getExtension());

            return Optional.of(ImageDescriptor.of(extension, path));

        }

        return Optional.empty();

    }

    public void publishRides() {
        rideRepository.findAllByPublishedStatusAndPublishedAtLessThan(PublishedStatus.UNPUBLISHED, ZonedDateTime.now(configurationService.getTimezone()))
                .forEach(ride -> {
                    log.info("Publishing ride {}", ride.getId());
                    ride.setPublishedStatus(PublishedStatus.PUBLISHED);
                    save(ride);
                    facebookService.publish(ride);
                    mailService.publish(ride);
                });
    }

    public void save(Ride ride) {
        rideRepository.save(ride);
    }

    public List<RideIdTitleDateProjection> listRides() {
        return rideRepository.findAllByOrderByDateDesc();
    }

    public Optional<Ride> get(String rideId) {
        return rideRepository.findById(rideId);
    }

    public void delete(String rideId) {
        log.info("Request ride deletion {}", rideId);
        get(rideId).ifPresent(ride -> rideRepository.delete(ride));
    }

    public Page<Ride> searchRides(int page, int pageSize,
                                  LocalDate from, LocalDate to) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("date").descending());
        return rideRepository.findByDateBetweenAndPublishedStatus(
                from,
                to,
                PublishedStatus.PUBLISHED,
                pageable);
    }

    public void saveImage(String rideId, InputStream is, String fileName) {
        Optional<FileExtension> optionalFileExtension = FileExtension.findByFileName(fileName);
        if (optionalFileExtension.isPresent()) {
            Path newImage = fileService.getTempFileFromInputStream(is);
            fileService.store(newImage, FileRepositories.RIDE_IMAGES, rideId + optionalFileExtension.get().getExtension());
        }
    }

}
