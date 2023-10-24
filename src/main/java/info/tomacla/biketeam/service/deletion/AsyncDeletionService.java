package info.tomacla.biketeam.service.deletion;

import info.tomacla.biketeam.common.amqp.Queues;
import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapRepository;
import info.tomacla.biketeam.domain.map.SearchMapSpecification;
import info.tomacla.biketeam.domain.message.MessageRepository;
import info.tomacla.biketeam.domain.notification.NotificationRepository;
import info.tomacla.biketeam.domain.place.PlaceRepository;
import info.tomacla.biketeam.domain.place.SearchPlaceSpecification;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.publication.PublicationRepository;
import info.tomacla.biketeam.domain.publication.SearchPublicationSpecification;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.domain.ride.SearchRideSpecification;
import info.tomacla.biketeam.domain.team.SearchTeamSpecification;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamRepository;
import info.tomacla.biketeam.domain.template.RideTemplateRepository;
import info.tomacla.biketeam.domain.template.SearchRideTemplateSpecification;
import info.tomacla.biketeam.domain.trip.SearchTripSpecification;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.domain.trip.TripRepository;
import info.tomacla.biketeam.domain.user.SearchUserSpecification;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserRepository;
import info.tomacla.biketeam.domain.userrole.UserRoleRepository;
import info.tomacla.biketeam.service.file.FileService;
import info.tomacla.biketeam.service.image.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AsyncDeletionService {

    private static final Logger log = LoggerFactory.getLogger(AsyncDeletionService.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MessageRepository messageRepository;


    @Autowired
    private MapRepository mapRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RideTemplateRepository rideTemplateRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private UserRepository userRepository;

    @RabbitListener(queues = Queues.TASK_PERFORM_DELETION)
    // ASYNC ENTITIES : map, publication, ride, team, trip, user
    public void performEffectiveDeletion() {
        try {

            mapRepository.findAll(SearchMapSpecification.readyForDeletion()).forEach(map -> this.deleteMap(map.getId()));
            publicationRepository.findAll(SearchPublicationSpecification.readyForDeletion()).forEach(e -> this.deletePublication(e.getId()));
            rideRepository.findAll(SearchRideSpecification.readyForDeletion()).forEach(ride -> this.deleteRide(ride.getId()));
            tripRepository.findAll(SearchTripSpecification.readyForDeletion()).forEach(trip -> this.deleteTrip(trip.getId()));
            teamRepository.findAll(SearchTeamSpecification.readyForDeletion()).forEach(team -> this.deleteTeam(team.getId()));
            userRepository.findAll(SearchUserSpecification.readyForDeletion()).forEach(user -> this.deleteUser(user.getId()));

        } catch (Exception e) {
            log.error("Error in event " + Queues.TASK_PERFORM_DELETION, e);
        }
    }

    @Transactional
    public void deletePublication(String publicationId) {
        log.info("Request publication deletion {}", publicationId);
        final Optional<Publication> optionalPublication = publicationRepository.findById(publicationId);
        if (optionalPublication.isPresent()) {
            final Publication publication = optionalPublication.get();
            ImageService imageService = new ImageService(FileRepositories.PUBLICATION_IMAGES, fileService);
            messageRepository.deleteByTargetId(publicationId);
            notificationRepository.deleteByElementId(publicationId);
            imageService.delete(publication.getTeamId(), publication.getId());
            publicationRepository.delete(publication);
        }
    }

    @Transactional
    public void deleteRide(String rideId) {
        log.info("Request ride deletion {}", rideId);
        final Optional<Ride> optionalRide = rideRepository.findById(rideId);
        if (optionalRide.isPresent()) {
            final Ride ride = optionalRide.get();
            ImageService imageService = new ImageService(FileRepositories.RIDE_IMAGES, fileService);
            messageRepository.deleteByTargetId(rideId);
            notificationRepository.deleteByElementId(rideId);
            imageService.delete(ride.getTeamId(), ride.getId());
            rideRepository.delete(ride);
        }
    }

    @Transactional
    public void deleteTrip(String tripId) {
        log.info("Request trip deletion {}", tripId);
        final Optional<Trip> optionalTrip = tripRepository.findById(tripId);
        if (optionalTrip.isPresent()) {
            final Trip trip = optionalTrip.get();
            ImageService imageService = new ImageService(FileRepositories.TRIP_IMAGES, fileService);
            messageRepository.deleteByTargetId(tripId);
            notificationRepository.deleteByElementId(tripId);
            imageService.delete(trip.getTeamId(), trip.getId());
            tripRepository.delete(trip);
        }
    }

    @Transactional
    public void deleteMap(String mapId) {
        log.info("Request map deletion {}", mapId);
        Optional<Map> optionalMap = mapRepository.findById(mapId);
        if (optionalMap.isPresent()) {
            final Map map = optionalMap.get();
            mapRepository.removeMapIdInGroups(map.getId());
            mapRepository.removeMapIdInStages(map.getId());
            fileService.deleteFile(FileRepositories.GPX_FILES, map.getTeamId(), map.getId() + ".gpx");
            fileService.deleteFile(FileRepositories.MAP_IMAGES, map.getTeamId(), map.getId() + ".png");
            mapRepository.delete(map);
        }
        log.info("Map deleted {}", mapId);
    }

    @Transactional
    public void deleteTeam(String teamId) {

        log.info("Request team deletion {}", teamId);
        final Optional<Team> optionalTeam = teamRepository.findById(teamId);
        if (optionalTeam.isPresent()) {

            final Team team = optionalTeam.get();

            // delete nested elements

            rideTemplateRepository.findAll(SearchRideTemplateSpecification.allInTeam(teamId))
                    .stream().forEach(rideTemplateRepository::delete);

            placeRepository.findAll(SearchPlaceSpecification.allInTeam(teamId))
                    .stream().forEach(placeRepository::delete);


            rideRepository.findAll(SearchRideSpecification.allInTeam(teamId))
                    .stream()
                    .map(Ride::getId)
                    .forEach(this::deleteRide);

            tripRepository.findAll(SearchTripSpecification.allInTeam(teamId))
                    .stream()
                    .map(Trip::getId)
                    .forEach(this::deleteTrip);

            publicationRepository.findAll(SearchPublicationSpecification.allInTeam(teamId))
                    .stream()
                    .map(Publication::getId)
                    .forEach(this::deletePublication);

            mapRepository.findAll(SearchMapSpecification.allInTeam(teamId))
                    .stream()
                    .map(Map::getId)
                    .forEach(this::deleteMap);

            userRoleRepository.deleteByTeamId(teamId);

            // finaly delete the team
            teamRepository.delete(team);
        }

    }


    @Transactional
    public void deleteUser(String userId) {

        log.info("Request user deletion {}", userId);
        final Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {

            User user = optionalUser.get();
            String teamIdToDelete = user.getTeamId();

            rideRepository.removeParticipant(user.getId());
            tripRepository.removeParticipant(user.getId());

            notificationRepository.deleteByUserId(user.getId());
            messageRepository.deleteByUserId(user.getId());
            userRoleRepository.deleteByUserId(user.getId());

            // finaly delete the user
            userRepository.delete(user);

            // delete private team
            if (teamIdToDelete != null) {
                this.deleteTeam(teamIdToDelete);
            }

        }

    }


}
