package info.tomacla.biketeam.service.broadcast;

import info.tomacla.biketeam.common.amqp.Queues;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.service.PublicationService;
import info.tomacla.biketeam.service.RideService;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.TripService;
import info.tomacla.biketeam.service.amqp.dto.TeamEntityDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class Broadcaster implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(PublicationService.class);

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private RideService rideService;

    @Autowired
    private TripService tripService;

    @Autowired
    private TeamService teamService;

    private ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @RabbitListener(queues = Queues.PUBLICATION_PUBLISHED)
    public void consumePublicationPublished(TeamEntityDTO body) {
        try {

            log.info("Received event on " + Queues.PUBLICATION_PUBLISHED);
            teamService.get(body.teamId).ifPresent(team ->
                    publicationService.get(body.teamId, body.id).ifPresent(publication ->
                            broadcast(team, publication)
                    )
            );

        } catch (Exception e) {
            log.error("Error in event " + Queues.PUBLICATION_PUBLISHED, e);
        }
    }

    @RabbitListener(queues = Queues.RIDE_PUBLISHED)
    public void consumeRidePublished(TeamEntityDTO body) {
        try {

            log.info("Received event on " + Queues.RIDE_PUBLISHED);
            teamService.get(body.teamId).ifPresent(team ->
                    rideService.get(body.teamId, body.id).ifPresent(ride ->
                            broadcast(team, ride)
                    )
            );

        } catch (Exception e) {
            log.error("Error in event " + Queues.RIDE_PUBLISHED, e);
        }
    }

    @RabbitListener(queues = Queues.TRIP_PUBLISHED)
    public void consumeTripPublished(TeamEntityDTO body) {
        try {

            log.info("Received event on " + Queues.TRIP_PUBLISHED);
            teamService.get(body.teamId).ifPresent(team ->
                    tripService.get(body.teamId, body.id).ifPresent(trip ->
                            broadcast(team, trip)
                    )
            );

        } catch (Exception e) {
            log.error("Error in event " + Queues.TRIP_PUBLISHED, e);
        }
    }

    private List<BroadcastService> getBroadcasters(Team team) {
        final Map<String, BroadcastService> publishers = applicationContext.getBeansOfType(BroadcastService.class);
        return publishers.values().stream().filter(e -> e.isConfigured(team)).collect(Collectors.toList());
    }

    private void broadcast(Team team, Ride ride) {
        getBroadcasters(team).forEach(s -> s.broadcast(team, ride));
    }

    private void broadcast(Team team, Publication publication) {
        getBroadcasters(team).forEach(s -> s.broadcast(team, publication));
    }

    private void broadcast(Team team, Trip trip) {
        getBroadcasters(team).forEach(s -> s.broadcast(team, trip));
    }

}
