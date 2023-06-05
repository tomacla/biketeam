package info.tomacla.biketeam.service.deletion;

import info.tomacla.biketeam.common.amqp.Queues;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamRepository;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.domain.trip.TripRepository;
import info.tomacla.biketeam.service.*;
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
    private RideRepository rideRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ReactionService reactionService;

    @Autowired
    private MapService mapService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private PlaceService placeService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RideTemplateService rideTemplateService;

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private RideService rideService;

    @Autowired
    private TripService tripService;

    @RabbitListener(queues = Queues.TASK_PERFORM_DELETION)
    public void performEffectiveDeletion() {
        try {
            rideRepository.findAllByDeletion(true).forEach(ride -> this.deleteRide(ride.getId()));
            tripRepository.findAllByDeletion(true).forEach(trip -> this.deleteTrip(trip.getId()));
            teamRepository.findAllByDeletion(true).forEach(team -> this.deleteTeam(team.getId()));
        } catch (Exception e) {
            log.error("Error in event " + Queues.TASK_PERFORM_DELETION, e);
        }
    }

    @Transactional
    public void deleteRide(String rideId) {
        log.info("Request ride deletion {}", rideId);
        final Optional<Ride> optionalRide = rideRepository.findById(rideId);
        if (optionalRide.isPresent()) {
            final Ride ride = optionalRide.get();
            messageService.deleteByTarget(rideId);
            reactionService.deleteByTarget(rideId);
            notificationService.deleteByElement(rideId);
            rideService.deleteImage(ride.getTeamId(), ride.getId());
            rideRepository.delete(ride);
        }
    }

    @Transactional
    public void deleteTrip(String tripId) {
        log.info("Request trip deletion {}", tripId);
        final Optional<Trip> optionalTrip = tripRepository.findById(tripId);
        if (optionalTrip.isPresent()) {
            final Trip trip = optionalTrip.get();
            messageService.deleteByTarget(tripId);
            reactionService.deleteByTarget(tripId);
            notificationService.deleteByElement(tripId);
            tripService.deleteImage(trip.getTeamId(), trip.getId());
            tripRepository.delete(trip);
        }
    }

    @Transactional
    public void deleteTeam(String teamId) {

        log.info("Request team deletion {}", teamId);
        final Optional<Team> optionalTeam = teamRepository.findById(teamId);
        if (optionalTeam.isPresent()) {
            final Team team = optionalTeam.get();
            // delete all elements
            messageService.deleteByTarget(team.getId());
            rideTemplateService.deleteByTeam(team.getId());
            rideService.deleteByTeam(team.getId());
            publicationService.deleteByTeam(team.getId());
            tripService.deleteByTeam(team.getId());
            mapService.deleteByTeam(team.getId());
            placeService.deleteByTeam(team.getId());
            // remove all access to this team
            userRoleService.deleteByTeam(team.getId());
            // finaly delete the team
            teamRepository.deleteById(team.getId());
        }

    }

}
