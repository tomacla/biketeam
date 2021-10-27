package info.tomacla.biketeam.service.externalpublication;

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
public class ExternalPublisher implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private List<ExternalPublicationService> getPublishers(Team team) {
        final Map<String, ExternalPublicationService> publishers = applicationContext.getBeansOfType(ExternalPublicationService.class);
        return publishers.values().stream().filter(e -> e.isApplicable(team)).collect(Collectors.toList());
    }

    public void publish(Team team, Ride ride) {
        getPublishers(team).forEach(s -> s.publish(team, ride));
    }

    public void publish(Team team, Publication publication) {
        getPublishers(team).forEach(s -> s.publish(team, publication));
    }

    public void publish(Team team, Trip trip) {
        getPublishers(team).forEach(s -> s.publish(team, trip));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
