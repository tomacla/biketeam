package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.amqp.Exchanges;
import info.tomacla.biketeam.common.amqp.Queues;
import info.tomacla.biketeam.common.amqp.RoutingKeys;
import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.common.data.Timezone;
import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.reaction.Reaction;
import info.tomacla.biketeam.domain.reaction.ReactionContent;
import info.tomacla.biketeam.domain.reaction.ReactionSummary;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.ride.RideProjection;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.domain.team.Team;
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
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RideService extends AbstractPermalinkService {

    private static final Logger log = LoggerFactory.getLogger(RideService.class);

    private final FileService fileService;

    private final RideRepository rideRepository;

    private final TeamService teamService;

    private final BrokerService brokerService;

    private final MessageService messageService;

    private final ReactionService reactionService;

    private final NotificationService notificationService;

    @Autowired
    public RideService(FileService fileService, RideRepository rideRepository, TeamService teamService, BrokerService brokerService, MessageService messageService, ReactionService reactionService, NotificationService notificationService) {
        this.fileService = fileService;
        this.rideRepository = rideRepository;
        this.teamService = teamService;
        this.brokerService = brokerService;
        this.messageService = messageService;
        this.reactionService = reactionService;
        this.notificationService = notificationService;
    }

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
            messageService.deleteByTarget(rideId);
            reactionService.deleteByTarget(rideId);
            notificationService.deleteByElement(rideId);
            deleteImage(ride.getTeamId(), ride.getId());
            rideRepository.delete(ride);
        }
    }

    public Page<Ride> searchRides(Set<String> teamIds, int page, int pageSize,
                                  LocalDate from, LocalDate to) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("date").descending());
        return rideRepository.findAllByTeamIdInAndDateBetweenAndPublishedStatus(
                teamIds,
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

    public List<ReactionSummary> getReactions(Ride ride, User user) {

        List<Reaction> reactions = reactionService.listByTarget(ride);

        Map<String, Long> reactionsCount = reactions
                .stream()
                .collect(Collectors.groupingBy(
                        reaction -> ReactionContent.valueOfUnicode(reaction.getContent()).name(),
                        Collectors.counting()
                ));

        String userReaction = null;
        if (user != null) {
            Optional<Reaction> optionalReaction = reactions.stream().filter(r -> r.getUser().equals(user)).findFirst();
            if (optionalReaction.isPresent()) {
                userReaction = ReactionContent.valueOfUnicode(optionalReaction.get().getContent()).name();
            }
        }

        List<ReactionSummary> response = new ArrayList<>();
        for (ReactionContent value : ReactionContent.values()) {
            ReactionSummary dto = new ReactionSummary(
                    value.htmlRepresentation(),
                    value.unicodeRepresentation(),
                    value.name().equals(userReaction),
                    reactionsCount.containsKey(value.name()) ? reactionsCount.get(value.name()) : 0
            );

            response.add(dto);
        }

        return response;

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

    public List<RideGroup> listRideGroupsByStartProximity(String teamId) {
        // retrieve team zone id
        ZoneId zoneId = teamService.get(teamId).map(Team::getZoneId).orElse(ZoneId.of(Timezone.DEFAULT_TIMEZONE));
        // get rides
        Page<Ride> rides = rideRepository.findAllByTeamIdInAndDateBetweenAndPublishedStatus(
                Set.of(teamId),
                LocalDate.now().minus(3, ChronoUnit.DAYS),
                LocalDate.now().plus(7, ChronoUnit.DAYS),
                PublishedStatus.PUBLISHED,
                PageRequest.of(0, 10));
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

}
