package info.tomacla.biketeam.service.broadcast;

import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.trip.Trip;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class Broadcaster implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private List<BroadcastService> getBroadcasters(Team team) {
        final Map<String, BroadcastService> publishers = applicationContext.getBeansOfType(BroadcastService.class);
        return publishers.values().stream().filter(e -> e.isConfigured(team)).collect(Collectors.toList());
    }

    public void broadcast(Team team, Ride ride) {
        getBroadcasters(team).forEach(s -> s.broadcast(team, ride));
    }

    public void broadcast(Team team, Publication publication) {
        getBroadcasters(team).forEach(s -> s.broadcast(team, publication));
    }

    public void broadcast(Team team, Trip trip) {
        getBroadcasters(team).forEach(s -> s.broadcast(team, trip));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
