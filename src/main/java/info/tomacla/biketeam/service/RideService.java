package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.amqp.Exchanges;
import info.tomacla.biketeam.common.amqp.Queues;
import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.common.data.Timezone;
import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.domain.ride.SearchRideSpecification;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.amqp.BrokerService;
import info.tomacla.biketeam.service.amqp.dto.TeamEntityDTO;
import info.tomacla.biketeam.service.file.FileService;
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

import java.io.InputStream;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RideService extends AbstractPermalinkService {

    private static final Logger log = LoggerFactory.getLogger(RideService.class);

    private final RideRepository rideRepository;

    private final TeamService teamService;

    private final BrokerService brokerService;

    private final ImageService imageService;

    @Autowired
    public RideService(FileService fileService, RideRepository rideRepository, TeamService teamService, BrokerService brokerService) {
        this.rideRepository = rideRepository;
        this.teamService = teamService;
        this.brokerService = brokerService;
        this.imageService = new ImageService(FileRepositories.RIDE_IMAGES, fileService);
    }

    public Optional<Ride> get(String teamId, String rideIdOrPermalink) {

        Optional<Ride> optionalRide = rideRepository.findById(rideIdOrPermalink);
        if (optionalRide.isPresent() && optionalRide.get().getTeamId().equals(teamId)) {
            return optionalRide;
        }

        optionalRide = findByPermalink(rideIdOrPermalink);
        if (optionalRide.isPresent() && optionalRide.get().getTeamId().equals(teamId)) {
            return optionalRide;
        }

        return Optional.empty();

    }

    public Optional<Ride> findByPermalink(String permalink) {
        return rideRepository.findOne(SearchRideSpecification.byPermalink(permalink));
    }

    @Override
    public boolean permalinkExists(String permalink) {
        return findByPermalink(permalink).isPresent();
    }

    @Transactional
    public void save(Ride ride) {
        rideRepository.save(ride);
    }

    @Transactional
    public void delete(String teamId, String rideId) {
        log.info("Request ride deletion {}", rideId);
        get(teamId, rideId).ifPresent(ride -> {
            ride.setDeletion(true);
            save(ride);
        });
    }


    public Page<Ride> listRides(String teamId, String title, int page, int pageSize) {
        return rideRepository.findAll(SearchRideSpecification.byTitleInTeam(teamId, title),
                PageRequest.of(page, pageSize, Sort.by("date").descending()));
    }

    public Page<Ride> searchRides(Set<String> teamIds, LocalDate from, LocalDate to, Boolean listedInFeed, int page, int pageSize) {
        return rideRepository.findAll(
                new SearchRideSpecification(
                        false,
                        null,
                        null,
                        listedInFeed,
                        null,
                        teamIds,
                        PublishedStatus.PUBLISHED,
                        null,
                        null,
                        from,
                        to),
                PageRequest.of(page, pageSize, Sort.by("date").descending()));
    }

    public List<Ride> searchUpcomingRidesByUser(User user, Set<String> teamIds, LocalDate from, LocalDate to) {
        Pageable pageable = PageRequest.of(0, 100, Sort.by("date").descending());
        return rideRepository.findAll(SearchRideSpecification.upcomingRidesByUser(teamIds, user, from, to), pageable).getContent();
    }

    public List<RideGroup> listRideGroupsByStartProximity(String teamId) {

        // retrieve team zone id
        ZoneId zoneId = teamService.get(teamId).map(Team::getZoneId).orElse(ZoneId.of(Timezone.DEFAULT_TIMEZONE));

        // get rides
        List<Ride> rides = rideRepository.findAll(new SearchRideSpecification(
                false,
                null,
                null,
                null,
                null,
                Set.of(teamId),
                PublishedStatus.PUBLISHED,
                null,
                null,
                LocalDate.now().minus(3, ChronoUnit.DAYS),
                LocalDate.now().plus(7, ChronoUnit.DAYS)));

        // now
        Instant now = Instant.now();

        // compare for nearest start
        Comparator<RideGroupStart> nearesetComparator = Comparator.comparing(ms -> Duration.between(ms.start(), now).abs());
        Comparator<RideGroupStart> idComparator = Comparator.comparing(ms -> ms.rideGroup().getMap().getId());
        // then compare by rideGroup id if duration is the same
        Comparator<RideGroupStart> comparator = nearesetComparator.thenComparing(idComparator);

        return rides.stream()
                // retrieve maps with start
                .flatMap(ride ->
                        ride.getGroups().stream()
                                // only groups with a rideGroup
                                .filter(rideGroup -> rideGroup.getMap() != null)
                                .map(rideGroup -> new RideGroupStart(
                                        rideGroup,
                                        // ride start as instant
                                        ride.getDate().atTime(rideGroup.getMeetingTime()).atZone(zoneId).toInstant()
                                ))
                )
                // sort by nearest starts
                .sorted(comparator)
                .map(RideGroupStart::rideGroup)
                .toList();

    }

    @RabbitListener(queues = Queues.TASK_PUBLISH_RIDES)
    public void publishRides() {
        teamService.list().forEach(team -> {

                    SearchRideSpecification spec = new SearchRideSpecification(false,
                            null,
                            null,
                            null,
                            null,
                            Set.of(team.getId()),
                            PublishedStatus.UNPUBLISHED,
                            null, ZonedDateTime.now(team.getZoneId()), null, null);

                    rideRepository.findAll(spec).forEach(ride -> {
                        log.info("Publishing ride {} for team {}", ride.getId(), team.getId());
                        ride.setPublishedStatus(PublishedStatus.PUBLISHED);
                        save(ride);
                        if (ride.isListedInFeed()) {
                            brokerService.sendToBroker(Exchanges.PUBLISH_RIDE, TeamEntityDTO.valueOf(ride.getTeamId(), ride.getId()));
                        }
                    });

                }
        );

    }


    public String getShortName(Ride ride) {
        StringBuilder result = new StringBuilder();
        String title = ride.getTitle();
        boolean inWord = false;
        for (char c : title.toCharArray()) {
            boolean add = false;
            if (inWord) {
                if (!Character.isLetter(c)) {
                    inWord = false;
                }
            } else {
                if (Character.isLetter(c)) {
                    inWord = true;
                    add = true;
                }
            }
            if (Character.getType(c) == Character.UPPERCASE_LETTER || Character.isDigit(c)) {
                add = true;
            }
            if (add) {
                result.append(c);
            }
        }
        if (result.length() == 0) {
            return title;
        } else {
            return result.toString();
        }
    }


    record RideGroupStart(RideGroup rideGroup, Instant start) {
    }


    public void saveImage(String teamId, String rideId, InputStream is, String fileName) {
        imageService.save(teamId, rideId, is, fileName);
    }

    public void deleteImage(String teamId, String rideId) {
        imageService.delete(teamId, rideId);
    }

    public Optional<ImageDescriptor> getImage(String teamId, String rideId) {
        final Optional<Ride> optionalRide = get(teamId, rideId);
        if (optionalRide.isPresent()) {
            return imageService.get(teamId, rideId);
        }
        return Optional.empty();
    }

}
